package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.auth.AuthResponse;
import com.atstudio.atstudio.dto.auth.LoginRequest;
import com.atstudio.atstudio.dto.auth.RefreshRequest;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.JwtTokenProvider;
import com.atstudio.atstudio.security.TokenValidationResult;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock UserRepository userRepository;
    @Mock OAuth2Service oAuth2Service;
    @Mock PasswordLoginPolicy passwordLoginPolicy;

    @InjectMocks AuthService authService;

    // ── login() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login() 성공 - AuthResponse 반환 및 SHA-256 hashed refreshToken DB 저장")
    void login_success_storesHashedTokenAndReturnsResponse() {
        User user = buildUser(1L, false);
        CustomUserDetails userDetails = CustomUserDetails.from(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtTokenProvider.generateAccessToken(1L, UserRole.USER)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");
        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(user.getRefreshToken()).isEqualTo(sha256("refresh-token"));
    }

    @Test
    @DisplayName("login() 실패 - 잘못된 비밀번호 → BadCredentialsException 전파")
    void login_badCredentials_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("login() 실패 - 이메일 로그인 비활성화 시 PASSWORD_LOGIN_DISABLED 예외")
    void login_disabled_throwsPasswordLoginDisabled() {
        doThrow(new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED))
                .when(passwordLoginPolicy).ensureEnabled();

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED));

        verifyNoInteractions(authenticationManager);
    }

    // ── refresh() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("refresh() 성공 - VALID 토큰 + DB 해시 일치 → 토큰 rotation")
    void refresh_validToken_rotatesTokens() {
        User user = buildUser(1L, false);
        user.updateRefreshToken(sha256("old-refresh"));

        when(jwtTokenProvider.validateToken("old-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("old-refresh")).thenReturn(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old-refresh");
        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(user.getRefreshToken()).isEqualTo(sha256("new-refresh"));
    }

    @Test
    @DisplayName("refresh() 실패 - EXPIRED 토큰 → REFRESH_TOKEN_EXPIRED 예외 (CR-P-005)")
    void refresh_expiredToken_throwsRefreshTokenExpired() {
        when(jwtTokenProvider.validateToken("expired-refresh")).thenReturn(TokenValidationResult.EXPIRED);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_EXPIRED));
    }

    @Test
    @DisplayName("refresh() 실패 - INVALID 토큰 → REFRESH_TOKEN_INVALID 예외")
    void refresh_invalidToken_throwsException() {
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(TokenValidationResult.INVALID);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("invalid-token");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_INVALID));
    }

    @Test
    @DisplayName("refresh() 실패 - DB 해시 불일치 시 최신 세션 보존 + REFRESH_TOKEN_INVALID 예외")
    void refresh_dbHashMismatch_preservesCurrentSessionAndThrows() {
        User user = buildUser(1L, false);
        user.updateRefreshToken(sha256("other-refresh"));

        when(jwtTokenProvider.validateToken("some-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("some-refresh")).thenReturn(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_INVALID));

        assertThat(user.getRefreshToken()).isEqualTo(sha256("other-refresh"));
    }

    @Test
    @DisplayName("refresh() 실패 - 탈퇴 계정 → ACCOUNT_DEACTIVATED 예외")
    void refresh_staleReplayAfterSuccessfulRotationPreservesNewSession() {
        User user = buildUser(1L, false);
        user.updateRefreshToken(sha256("old-refresh"));

        when(jwtTokenProvider.validateToken("old-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("old-refresh")).thenReturn(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(1L, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old-refresh");

        AuthResponse refreshed = authService.refresh(request);

        assertThat(refreshed.refreshToken()).isEqualTo("new-refresh");
        assertThat(user.getRefreshToken()).isEqualTo(sha256("new-refresh"));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_INVALID));

        assertThat(user.getRefreshToken()).isEqualTo(sha256("new-refresh"));
    }

    @Test
    @DisplayName("refresh() 실패 - 삭제 계정은 ACCOUNT_DEACTIVATED 예외")
    void refresh_deletedUser_throwsAccountDeactivated() {
        User user = buildUser(1L, false);
        user.withdraw();                        // isDeleted=true, refreshToken=null
        user.updateRefreshToken(sha256("some-refresh"));  // token back for test scenario

        when(jwtTokenProvider.validateToken("some-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("some-refresh")).thenReturn(1L);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ACCOUNT_DEACTIVATED));
    }

    @Test
    @DisplayName("logout() 성공 - 현재 refresh session 폐기 및 반복 호출 멱등 처리")
    void logout_repeatedCalls_clearRefreshSessionIdempotently() {
        User user = buildUser(1L, false);
        user.updateRefreshToken(sha256("current-refresh"));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));

        authService.logout(1L);
        authService.logout(1L);

        assertThat(user.getRefreshToken()).isNull();
        verify(userRepository, times(2)).findByIdForUpdate(1L);
    }

    @Test
    @DisplayName("logout() 성공 - 인증 후 사용자 행이 없어도 멱등 처리")
    void logout_missingUser_isIdempotent() {
        when(userRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        authService.logout(99L);

        verify(userRepository).findByIdForUpdate(99L);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout() 이후 stale refresh 재사용은 거부된다")
    void logout_thenRefreshReplayFails() {
        User user = buildUser(1L, false);
        user.updateRefreshToken(sha256("stale-refresh"));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.validateToken("stale-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("stale-refresh")).thenReturn(1L);

        authService.logout(1L);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("stale-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_INVALID));
        assertThat(user.getRefreshToken()).isNull();
    }

    private User buildUser(Long id, boolean deleted) {
        User user = User.builder()
                .nickname("tester")
                .email("user@test.com")
                .password("encoded-password")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        if (deleted) user.withdraw();
        return user;
    }

    /** Mirror of AuthService.sha256() for test assertions */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
