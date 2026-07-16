package com.atstudio.atstudio.service.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SocialAccountRepository;
import com.atstudio.atstudio.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final RestClient restClient;

    @Value("${oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${oauth2.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${oauth2.kakao.redirect-uri:}")
    private String kakaoRedirectUri;

    @Value("${oauth2.naver.client-id:}")
    private String naverClientId;

    @Value("${oauth2.naver.client-secret:}")
    private String naverClientSecret;

    @Value("${oauth2.naver.redirect-uri:}")
    private String naverRedirectUri;

    @Transactional
    public User processSocialLogin(SocialProvider provider, String authorizationCode, String codeVerifier) {
        // 1. Authorization Code -> Provider Access Token 교환 (with PKCE)
        String socialAccessToken = exchangeCodeForToken(provider, authorizationCode, codeVerifier);

        // 2. Provider User Info 조회
        SocialUserInfo userInfo = fetchUserInfo(provider, socialAccessToken);

        // 3. 기존 소셜 계정 확인
        Optional<SocialAccount> existingSocial = socialAccountRepository
                .findByProviderAndProviderId(provider, userInfo.providerId());

        if (existingSocial.isPresent()) {
            return existingSocial.get().getUser();
        }

        // 4. 이메일 중복 확인 (SEC-09: 자동 연결 금지)
        if (userInfo.email() != null && userRepository.findByEmail(userInfo.email()).isPresent()) {
            throw new BusinessException(BUSINESS_ERROR.EMAIL_ALREADY_REGISTERED);
        }

        // 5. 신규 사용자 생성 (최소 정보)
        String tempNickname = generateTempNickname(provider, userInfo.providerId());
        String emailValue = userInfo.email() != null
                ? userInfo.email()
                : provider.name().toLowerCase() + "_" + userInfo.providerId().hashCode();

        User user = User.builder()
                .email(emailValue)
                .nickname(tempNickname)
                .password(null)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        user = userRepository.save(user);

        // 6. SocialAccount 생성
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(userInfo.providerId())
                .build();
        socialAccountRepository.save(socialAccount);

        return user;
    }

    private String generateTempNickname(SocialProvider provider, String providerId) {
        String hash = Integer.toHexString(Math.abs(providerId.hashCode()));
        return provider.name().substring(0, 1) + "_" + hash.substring(0, Math.min(6, hash.length()));
    }

    private String exchangeCodeForToken(SocialProvider provider, String code, String codeVerifier) {
        return switch (provider) {
            case GOOGLE -> exchangeGoogleToken(code, codeVerifier);
            case KAKAO -> exchangeKakaoToken(code, codeVerifier);
            case NAVER -> exchangeNaverToken(code, codeVerifier);
        };
    }

    private String exchangeGoogleToken(String code, String codeVerifier) {
        return executeProviderRequest(() -> requireResponse(restClient.post()
                        .uri("https://oauth2.googleapis.com/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .body(buildTokenRequestBody(
                                code,
                                googleClientId,
                                googleClientSecret,
                                googleRedirectUri,
                                codeVerifier))
                        .retrieve()
                        .body(OAuthTokenResponse.class))
                .requiredAccessToken());
    }

    private String exchangeKakaoToken(String code, String codeVerifier) {
        return executeProviderRequest(() -> requireResponse(restClient.post()
                        .uri("https://kauth.kakao.com/oauth/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .body(buildTokenRequestBody(
                                code,
                                kakaoClientId,
                                kakaoClientSecret,
                                kakaoRedirectUri,
                                codeVerifier))
                        .retrieve()
                        .body(OAuthTokenResponse.class))
                .requiredAccessToken());
    }

    private String exchangeNaverToken(String code, String codeVerifier) {
        return executeProviderRequest(() -> requireResponse(restClient.post()
                        .uri("https://nid.naver.com/oauth2.0/token")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .body(buildTokenRequestBody(
                                code,
                                naverClientId,
                                naverClientSecret,
                                naverRedirectUri,
                                codeVerifier))
                        .retrieve()
                        .body(OAuthTokenResponse.class))
                .requiredAccessToken());
    }

    private String buildTokenRequestBody(String code, String clientId, String clientSecret,
                                         String redirectUri, String codeVerifier) {
        var builder = UriComponentsBuilder.newInstance()
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            builder.queryParam("code_verifier", codeVerifier);
        }
        return builder.build().encode().getQuery();
    }

    private SocialUserInfo fetchUserInfo(SocialProvider provider, String accessToken) {
        return switch (provider) {
            case GOOGLE -> fetchGoogleUserInfo(accessToken);
            case KAKAO -> fetchKakaoUserInfo(accessToken);
            case NAVER -> fetchNaverUserInfo(accessToken);
        };
    }

    private SocialUserInfo fetchGoogleUserInfo(String accessToken) {
        return executeProviderRequest(() -> requireResponse(restClient.get()
                        .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .body(GoogleUserInfoResponse.class))
                .toSocialUserInfo());
    }

    private SocialUserInfo fetchKakaoUserInfo(String accessToken) {
        return executeProviderRequest(() -> requireResponse(restClient.get()
                        .uri("https://kapi.kakao.com/v2/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .body(KakaoUserInfoResponse.class))
                .toSocialUserInfo());
    }

    private SocialUserInfo fetchNaverUserInfo(String accessToken) {
        return executeProviderRequest(() -> requireResponse(restClient.get()
                        .uri("https://openapi.naver.com/v1/nid/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .body(NaverUserInfoResponse.class))
                .toSocialUserInfo());
    }

    private <T> T executeProviderRequest(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException | IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
    }

    private static <T> T requireResponse(T response) {
        if (response == null) {
            throw invalidProviderResponse();
        }
        return response;
    }

    private static String requiredText(JsonNode value) {
        if (value == null || !value.isTextual() || value.textValue().isBlank()) {
            throw invalidProviderResponse();
        }
        return value.textValue();
    }

    private static String requiredKakaoId(JsonNode value) {
        if (value == null) {
            throw invalidProviderResponse();
        }
        if (value.isTextual()) {
            return requiredText(value);
        }
        if (value.isIntegralNumber()) {
            return value.bigIntegerValue().toString();
        }
        throw invalidProviderResponse();
    }

    private static String optionalText(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        return requiredText(value);
    }

    private static JsonNode requiredObject(JsonNode value) {
        if (value == null || !value.isObject()) {
            throw invalidProviderResponse();
        }
        return value;
    }

    private static void rejectProviderError(JsonNode... errorFields) {
        for (JsonNode errorField : errorFields) {
            if (errorField != null && !errorField.isNull()) {
                throw invalidProviderResponse();
            }
        }
    }

    private static IllegalArgumentException invalidProviderResponse() {
        return new IllegalArgumentException("Invalid OAuth provider response");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OAuthTokenResponse(
            @JsonProperty("access_token") JsonNode accessToken,
            JsonNode error) {

        String requiredAccessToken() {
            rejectProviderError(error);
            return requiredText(accessToken);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleUserInfoResponse(
            JsonNode id,
            JsonNode email,
            JsonNode name,
            JsonNode error) {

        SocialUserInfo toSocialUserInfo() {
            rejectProviderError(error);
            return new SocialUserInfo(requiredText(id), optionalText(email), optionalText(name));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record KakaoUserInfoResponse(
            JsonNode id,
            @JsonProperty("kakao_account") JsonNode kakaoAccount,
            JsonNode code,
            JsonNode msg) {

        SocialUserInfo toSocialUserInfo() {
            rejectProviderError(code, msg);
            JsonNode account = requiredObject(kakaoAccount);
            JsonNode profile = requiredObject(account.get("profile"));
            return new SocialUserInfo(
                    requiredKakaoId(id),
                    optionalText(account.get("email")),
                    optionalText(profile.get("nickname")));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NaverUserInfoResponse(
            @JsonProperty("resultcode") JsonNode resultCode,
            JsonNode message,
            JsonNode response) {

        SocialUserInfo toSocialUserInfo() {
            if (resultCode == null
                    || !resultCode.isTextual()
                    || !"00".equals(resultCode.textValue())) {
                throw invalidProviderResponse();
            }
            JsonNode userInfo = requiredObject(response);
            return new SocialUserInfo(
                    requiredText(userInfo.get("id")),
                    optionalText(userInfo.get("email")),
                    optionalText(userInfo.get("name")));
        }
    }
}
