package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.repository.SocialAccountRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service 단위 테스트")
class OAuth2ServiceTest {

    @Mock UserRepository userRepository;
    @Mock SocialAccountRepository socialAccountRepository;
    @Mock com.atstudio.atstudio.service.PlaylistService playlistService;
    @Mock RestClient restClient;

    @InjectMocks OAuth2Service oAuth2Service;

    // RestClient chaining mocks
    @Mock RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock RestClient.RequestBodySpec requestBodySpec;
    @Mock RestClient.ResponseSpec tokenResponseSpec;
    @Mock RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock RestClient.RequestHeadersSpec requestHeadersSpec;
    @Mock RestClient.ResponseSpec userInfoResponseSpec;

    // ── Token Exchange Null Response (M-9) ─────────────────────────────────────

    @Nested
    @DisplayName("토큰 교환 null 응답 처리 (M-9)")
    class TokenExchangeNullResponse {

        @Test
        @DisplayName("Google 토큰 교환 null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void exchangeGoogleToken_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao 토큰 교환 null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void exchangeKakaoToken_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver 토큰 교환 null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void exchangeNaverToken_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        private void mockTokenExchangeReturningNull() {
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(tokenResponseSpec);
            when(tokenResponseSpec.body(OAuth2Service.OAuthTokenResponse.class)).thenReturn(null);
        }
    }

    // ── UserInfo Fetch Null Response (M-9) ─────────────────────────────────────

    @Nested
    @DisplayName("UserInfo 조회 null 응답 처리 (M-9)")
    class UserInfoNullResponse {

        @Test
        @DisplayName("Google userInfo null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void fetchGoogleUserInfo_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            mockGoogleUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao userInfo null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void fetchKakaoUserInfo_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            mockKakaoUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver userInfo null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void fetchNaverUserInfo_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            mockNaverUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao kakao_account null -> SOCIAL_AUTH_FAILED 예외")
        void fetchKakaoUserInfo_nullAccount_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            // userInfo 응답은 있지만 kakao_account가 null
            mockKakaoUserInfo(new OAuth2Service.KakaoUserInfoResponse(text("12345"), null, null, null));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver response null (body 내 response 키 없음) -> SOCIAL_AUTH_FAILED 예외")
        void fetchNaverUserInfo_nullResponseInBody_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            // body는 있지만 response 키가 없음
            mockNaverUserInfo(new OAuth2Service.NaverUserInfoResponse(null, null, null));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        private void mockTokenExchangeReturningValidToken() {
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(tokenResponseSpec);
            when(tokenResponseSpec.body(OAuth2Service.OAuthTokenResponse.class))
                    .thenReturn(new OAuth2Service.OAuthTokenResponse(text("valid-token"), null));
        }

        private void mockGoogleUserInfoReturningNull() {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(OAuth2Service.GoogleUserInfoResponse.class)).thenReturn(null);
        }

        private void mockKakaoUserInfoReturningNull() {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(OAuth2Service.KakaoUserInfoResponse.class)).thenReturn(null);
        }

        private void mockNaverUserInfoReturningNull() {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(OAuth2Service.NaverUserInfoResponse.class)).thenReturn(null);
        }

        private void mockKakaoUserInfo(OAuth2Service.KakaoUserInfoResponse response) {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(OAuth2Service.KakaoUserInfoResponse.class)).thenReturn(response);
        }
    }

    // ── Kakao profile null 처리 (CR-M-4) ───────────────────────────────────────

    @Nested
    @DisplayName("Kakao profile=null 처리 (CR-M-4)")
    class KakaoProfileNull {

        @Test
        @DisplayName("Kakao profile null -> SOCIAL_AUTH_FAILED 예외")
        void fetchKakaoUserInfo_nullProfile_throwsSocialAuthFailed() {
            // token exchange 성공
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
            when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenReturn(tokenResponseSpec);
            when(tokenResponseSpec.body(OAuth2Service.OAuthTokenResponse.class))
                    .thenReturn(new OAuth2Service.OAuthTokenResponse(text("valid-token"), null));

            // userInfo 응답: kakao_account 존재하지만 profile이 null
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            JsonNode account = JsonNodeFactory.instance.objectNode().put("email", "user@kakao.com");
            when(userInfoResponseSpec.body(OAuth2Service.KakaoUserInfoResponse.class)).thenReturn(
                    new OAuth2Service.KakaoUserInfoResponse(text("12345"), account, null, null));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code", null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }
    }

    // ── 정상 플로우 (기존 소셜 계정 매칭) ──────────────────────────────────────

    @Test
    @DisplayName("processSocialLogin() - 기존 소셜 계정 존재 시 해당 User 반환")
    void processSocialLogin_existingSocialAccount_returnsUser() {
        // token exchange
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(tokenResponseSpec);
        when(tokenResponseSpec.body(OAuth2Service.OAuthTokenResponse.class))
                .thenReturn(new OAuth2Service.OAuthTokenResponse(text("valid-token"), null));

        // userInfo (Google)
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
        when(userInfoResponseSpec.body(OAuth2Service.GoogleUserInfoResponse.class)).thenReturn(
                new OAuth2Service.GoogleUserInfoResponse(
                        text("google-123"), text("user@gmail.com"), text("Test User"), null));

        User existingUser = User.builder().email("user@gmail.com").nickname("tester").build();
        SocialAccount socialAccount = SocialAccount.builder()
                .user(existingUser)
                .provider(SocialProvider.GOOGLE)
                .providerId("google-123")
                .build();

        when(socialAccountRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(socialAccount));

        User result = oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null);

        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("blank access token is rejected with SOCIAL_AUTH_FAILED")
    void processSocialLogin_blankAccessToken_throwsSocialAuthFailed() {
        mockTokenExchange(new OAuth2Service.OAuthTokenResponse(text("  "), null));

        assertSocialAuthFailed(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null));
    }

    @Test
    @DisplayName("provider token error is rejected with SOCIAL_AUTH_FAILED")
    void processSocialLogin_providerTokenError_throwsSocialAuthFailed() {
        mockTokenExchange(new OAuth2Service.OAuthTokenResponse(null, text("invalid_grant")));

        assertSocialAuthFailed(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null));
    }

    @Test
    @DisplayName("wrong-type Google provider id is rejected with SOCIAL_AUTH_FAILED")
    void processSocialLogin_wrongTypeGoogleProviderId_throwsSocialAuthFailed() {
        mockTokenExchange(new OAuth2Service.OAuthTokenResponse(text("valid-token"), null));
        mockGoogleUserInfo(new OAuth2Service.GoogleUserInfoResponse(number(123), null, null, null));

        assertSocialAuthFailed(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code", null));
    }

    @Test
    @DisplayName("Naver missing resultcode is rejected with SOCIAL_AUTH_FAILED")
    void processSocialLogin_naverMissingResultCode_throwsSocialAuthFailed() {
        mockTokenExchange(new OAuth2Service.OAuthTokenResponse(text("valid-token"), null));
        mockNaverUserInfo(new OAuth2Service.NaverUserInfoResponse(
                null, null, JsonNodeFactory.instance.objectNode().put("id", "naver-123")));

        assertSocialAuthFailed(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code", null));
    }

    private void mockTokenExchange(OAuth2Service.OAuthTokenResponse response) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(tokenResponseSpec);
        when(tokenResponseSpec.body(OAuth2Service.OAuthTokenResponse.class)).thenReturn(response);
    }

    private void mockGoogleUserInfo(OAuth2Service.GoogleUserInfoResponse response) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
        when(userInfoResponseSpec.body(OAuth2Service.GoogleUserInfoResponse.class)).thenReturn(response);
    }

    private void mockNaverUserInfo(OAuth2Service.NaverUserInfoResponse response) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
        when(userInfoResponseSpec.body(OAuth2Service.NaverUserInfoResponse.class)).thenReturn(response);
    }

    private static JsonNode text(String value) {
        return JsonNodeFactory.instance.textNode(value);
    }

    private static JsonNode number(int value) {
        return JsonNodeFactory.instance.numberNode(value);
    }

    private static void assertSocialAuthFailed(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
    }
}
