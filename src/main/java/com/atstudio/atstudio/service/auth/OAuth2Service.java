package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SocialAccountRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final PlaylistService playlistService;
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

        // 7. 기본 재생목록 생성 (신규 소셜 가입)
        playlistService.createDefaultPlaylist(user);

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

    @SuppressWarnings("unchecked")
    private String exchangeGoogleToken(String code, String codeVerifier) {
        Map<String, Object> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(buildTokenRequestBody(code, googleClientId, googleClientSecret, googleRedirectUri, codeVerifier))
                .retrieve()
                .body(Map.class);
        if (response == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private String exchangeKakaoToken(String code, String codeVerifier) {
        Map<String, Object> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(buildTokenRequestBody(code, kakaoClientId, kakaoClientSecret, kakaoRedirectUri, codeVerifier))
                .retrieve()
                .body(Map.class);
        if (response == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return (String) response.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private String exchangeNaverToken(String code, String codeVerifier) {
        Map<String, Object> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(buildTokenRequestBody(code, naverClientId, naverClientSecret, naverRedirectUri, codeVerifier))
                .retrieve()
                .body(Map.class);
        if (response == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return (String) response.get("access_token");
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

    @SuppressWarnings("unchecked")
    private SocialUserInfo fetchGoogleUserInfo(String accessToken) {
        Map<String, Object> info = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
        if (info == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return new SocialUserInfo(
                (String) info.get("id"),
                (String) info.get("email"),
                (String) info.get("name"));
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo fetchKakaoUserInfo(String accessToken) {
        Map<String, Object> info = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
        if (info == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        Map<String, Object> account = (Map<String, Object>) info.get("kakao_account");
        if (account == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");
        if (profile == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return new SocialUserInfo(
                String.valueOf(info.get("id")),
                (String) account.get("email"),
                (String) profile.get("nickname"));
    }

    @SuppressWarnings("unchecked")
    private SocialUserInfo fetchNaverUserInfo(String accessToken) {
        Map<String, Object> body = restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);
        if (body == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        Map<String, Object> response = (Map<String, Object>) body.get("response");
        if (response == null) {
            throw new BusinessException(BUSINESS_ERROR.SOCIAL_AUTH_FAILED);
        }
        return new SocialUserInfo(
                (String) response.get("id"),
                (String) response.get("email"),
                (String) response.get("name"));
    }
}
