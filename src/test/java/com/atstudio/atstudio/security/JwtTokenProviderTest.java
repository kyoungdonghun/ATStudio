package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.JwtConfig;
import com.atstudio.atstudio.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    // test/resources/application.yml 의 jwt.secret 과 동일 (44 bytes → >= 32 bytes)
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1hdHN0dWRpby10ZXN0aW5nLW9ubHktMjAyNg==";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtConfig config = mock(JwtConfig.class);
        when(config.getSecret()).thenReturn(TEST_SECRET);
        when(config.getAccessTokenExpiration()).thenReturn(3600000L);
        when(config.getRefreshTokenExpiration()).thenReturn(1209600000L);
        provider = new JwtTokenProvider(config);
        provider.init();
    }

    @Test
    @DisplayName("Access Token 생성 - sub에 userID, role claim 포함")
    void generateAccessToken_containsUserIdAndRole() {
        String token = provider.generateAccessToken(1L, UserRole.USER);

        assertThat(token).isNotBlank();
        assertThat(provider.getUserID(token)).isEqualTo(1L);
        assertThat(provider.getRole(token)).isEqualTo("USER");
    }

    @Test
    @DisplayName("Refresh Token 생성 - sub에 userID 포함")
    void generateRefreshToken_containsUserId() {
        String token = provider.generateRefreshToken(42L);

        assertThat(token).isNotBlank();
        assertThat(provider.getUserID(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("유효한 토큰 → VALID 반환")
    void validateToken_valid_returnsValid() {
        String token = provider.generateAccessToken(1L, UserRole.USER);

        assertThat(provider.validateToken(token)).isEqualTo(TokenValidationResult.VALID);
    }

    @Test
    @DisplayName("만료된 토큰 → EXPIRED 반환 (예외 아님)")
    void validateToken_expired_returnsExpired() {
        String expiredToken = buildExpiredToken(1L);

        assertThat(provider.validateToken(expiredToken)).isEqualTo(TokenValidationResult.EXPIRED);
    }

    @Test
    @DisplayName("변조된 토큰 → INVALID 반환 (예외 아님)")
    void validateToken_tampered_returnsInvalid() {
        String token = provider.generateAccessToken(1L, UserRole.USER);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(provider.validateToken(tampered)).isEqualTo(TokenValidationResult.INVALID);
    }

    @Test
    @DisplayName("만료 토큰에서 getUserIDAllowExpired → userID 정상 추출")
    void getUserIDAllowExpired_expiredToken_extractsId() {
        String expiredToken = buildExpiredToken(99L);

        assertThat(provider.getUserIDAllowExpired(expiredToken)).isEqualTo(99L);
    }

    @Test
    @DisplayName("Access Token에 email claim 없음 (PII 보호 - SEC-02)")
    void generateAccessToken_noEmailClaim() {
        String token = provider.generateAccessToken(1L, UserRole.USER);
        io.jsonwebtoken.Claims claims = provider.parseToken(token);

        assertThat(claims.get("email")).isNull();
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private String buildExpiredToken(Long userId) {
        JwtConfig expiredConfig = mock(JwtConfig.class);
        when(expiredConfig.getSecret()).thenReturn(TEST_SECRET);
        when(expiredConfig.getAccessTokenExpiration()).thenReturn(0L);
        when(expiredConfig.getRefreshTokenExpiration()).thenReturn(0L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredConfig);
        expiredProvider.init();
        return expiredProvider.generateRefreshToken(userId);
    }
}
