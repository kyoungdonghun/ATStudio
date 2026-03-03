package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.repository.SocialAccountRepository;
import com.atstudio.atstudio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Map;
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

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao 토큰 교환 null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void exchangeKakaoToken_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver 토큰 교환 null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void exchangeNaverToken_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code"))
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
            when(tokenResponseSpec.body(Map.class)).thenReturn(null);
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
            mockUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao userInfo null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void fetchKakaoUserInfo_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            mockUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver userInfo null 응답 -> SOCIAL_AUTH_FAILED 예외")
        void fetchNaverUserInfo_nullResponse_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            mockUserInfoReturningNull();

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Kakao kakao_account null -> SOCIAL_AUTH_FAILED 예외")
        void fetchKakaoUserInfo_nullAccount_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            // userInfo 응답은 있지만 kakao_account가 null
            mockUserInfoReturning(Map.of("id", "12345"));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code"))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.SOCIAL_AUTH_FAILED));
        }

        @Test
        @DisplayName("Naver response null (body 내 response 키 없음) -> SOCIAL_AUTH_FAILED 예외")
        void fetchNaverUserInfo_nullResponseInBody_throwsSocialAuthFailed() {
            mockTokenExchangeReturningValidToken();
            // body는 있지만 response 키가 없음
            mockUserInfoReturning(Map.of("result", "success"));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.NAVER, "auth-code"))
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
            when(tokenResponseSpec.body(Map.class)).thenReturn(Map.of("access_token", "valid-token"));
        }

        private void mockUserInfoReturningNull() {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(Map.class)).thenReturn(null);
        }

        private void mockUserInfoReturning(Map<String, Object> response) {
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(Map.class)).thenReturn(response);
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
            when(tokenResponseSpec.body(Map.class)).thenReturn(Map.of("access_token", "valid-token"));

            // userInfo 응답: kakao_account 존재하지만 profile이 null
            when(restClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
            when(userInfoResponseSpec.body(Map.class)).thenReturn(
                    Map.of("id", "12345", "kakao_account", new java.util.HashMap<>(Map.of("email", "user@kakao.com"))));

            assertThatThrownBy(() -> oAuth2Service.processSocialLogin(SocialProvider.KAKAO, "auth-code"))
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
        when(tokenResponseSpec.body(Map.class)).thenReturn(Map.of("access_token", "valid-token"));

        // userInfo (Google)
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(userInfoResponseSpec);
        when(userInfoResponseSpec.body(Map.class)).thenReturn(
                Map.of("id", "google-123", "email", "user@gmail.com", "name", "Test User"));

        User existingUser = User.builder().email("user@gmail.com").nickname("tester").build();
        SocialAccount socialAccount = SocialAccount.builder()
                .user(existingUser)
                .provider(SocialProvider.GOOGLE)
                .providerId("google-123")
                .build();

        when(socialAccountRepository.findByProviderAndProviderId(SocialProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(socialAccount));

        User result = oAuth2Service.processSocialLogin(SocialProvider.GOOGLE, "auth-code");

        assertThat(result).isEqualTo(existingUser);
        verify(userRepository, never()).save(any());
    }
}
