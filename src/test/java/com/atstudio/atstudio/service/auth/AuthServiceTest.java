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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock PasswordEncoder passwordEncoder;
    @Mock OAuth2Service oAuth2Service;

    @InjectMocks AuthService authService;

    // ── login() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login() 성공 - AuthResponse 반환 및 hashed refreshToken DB 저장")
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
        when(passwordEncoder.encode("refresh-token")).thenReturn("hashed-refresh");

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password");
        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(user.getRefreshToken()).isEqualTo("hashed-refresh");
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

    // ── refresh() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("refresh() 성공 - VALID 토큰 + DB 해시 일치 → 토큰 rotation")
    void refresh_validToken_rotatesTokens() {
        User user = buildUser(1L, false);
        user.updateRefreshToken("hashed-old");

        when(jwtTokenProvider.validateToken("old-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserIDAllowExpired("old-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-refresh", "hashed-old")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
        when(passwordEncoder.encode("new-refresh")).thenReturn("hashed-new");

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old-refresh");
        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(user.getRefreshToken()).isEqualTo("hashed-new");
    }

    @Test
    @DisplayName("refresh() 성공 - EXPIRED 토큰도 rotation 가능 (getUserIDAllowExpired 경로)")
    void refresh_expiredToken_stillRotates() {
        User user = buildUser(1L, false);
        user.updateRefreshToken("hashed-old");

        when(jwtTokenProvider.validateToken("expired-refresh")).thenReturn(TokenValidationResult.EXPIRED);
        when(jwtTokenProvider.getUserIDAllowExpired("expired-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("expired-refresh", "hashed-old")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(1L, UserRole.USER)).thenReturn("new-access");
        when(jwtTokenProvider.generateRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
        when(passwordEncoder.encode("new-refresh")).thenReturn("hashed-new");

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired-refresh");
        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access");
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
    @DisplayName("refresh() 실패 - DB 해시 불일치 → clearRefreshToken() 호출 + REFRESH_TOKEN_INVALID 예외")
    void refresh_dbHashMismatch_clearsTokenAndThrows() {
        User user = buildUser(1L, false);
        user.updateRefreshToken("hashed-other");

        when(jwtTokenProvider.validateToken("some-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserIDAllowExpired("some-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("some-refresh", "hashed-other")).thenReturn(false);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.REFRESH_TOKEN_INVALID));

        assertThat(user.getRefreshToken()).isNull();  // clearRefreshToken() 호출 검증
    }

    @Test
    @DisplayName("refresh() 실패 - 탈퇴 계정 → ACCOUNT_DEACTIVATED 예외")
    void refresh_deletedUser_throwsAccountDeactivated() {
        User user = buildUser(1L, false);
        user.withdraw();                        // isDeleted=true, refreshToken=null
        user.updateRefreshToken("hashed-refresh");  // token back for test scenario

        when(jwtTokenProvider.validateToken("some-refresh")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserIDAllowExpired("some-refresh")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("some-refresh", "hashed-refresh")).thenReturn(true);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("some-refresh");

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.ACCOUNT_DEACTIVATED));
    }

    // ── helper ────────────────────────────────────────────────────────────────

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
}
