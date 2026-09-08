package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.JwtConfig;
import com.atstudio.atstudio.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenPurposeTest {
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci1hdHN0dWRpby10ZXN0aW5nLW9ubHktMjAyNg==";
    private static final Instant NOW = Instant.parse("2026-09-09T00:00:00Z");
    private final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtConfig config = mock(JwtConfig.class);
        when(config.getSecret()).thenReturn(TEST_SECRET);
        when(config.getAccessTokenExpiration()).thenReturn(3600000L);
        when(config.getRefreshTokenExpiration()).thenReturn(1209600000L);
        provider = new JwtTokenProvider(config, Clock.fixed(NOW, ZoneOffset.UTC));
        provider.init();
    }

    @Test
    void generatedTokensHaveStrictPurposesAndPreserveRoleAndTtl() {
        for (UserRole role : UserRole.values()) {
            String access = provider.generateAccessToken(7L, role);
            Claims claims = provider.parseToken(access);
            assertThat(claims.get("token_use")).isEqualTo("access");
            assertThat(claims.get("role")).isEqualTo(role.name());
            assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plusSeconds(3600));
            assertThat(provider.validateAccessToken(access)).isEqualTo(TokenValidationResult.VALID);
            assertThat(provider.validateRefreshToken(access)).isEqualTo(TokenValidationResult.INVALID);
        }
        String refresh = provider.generateRefreshToken(7L);
        Claims claims = provider.parseToken(refresh);
        assertThat(claims.get("token_use")).isEqualTo("refresh");
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plusSeconds(1209600));
        assertThat(claims).doesNotContainKeys("email", "password", "role");
        assertThat(provider.validateRefreshToken(refresh)).isEqualTo(TokenValidationResult.VALID);
        assertThat(provider.validateAccessToken(refresh)).isEqualTo(TokenValidationResult.INVALID);
    }

    @Test
    void everyRefreshIssuanceIsUniqueEvenAtTheExactSameInstant() {
        var tokens = new HashSet<String>();
        var ids = new HashSet<String>();
        for (int index = 0; index < 100; index++) {
            String token = provider.generateRefreshToken(7L);
            Claims claims = provider.parseToken(token);
            assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW);
            assertThat(UUID.fromString(claims.getId()).version()).isEqualTo(4);
            assertThat(tokens.add(token)).isTrue();
            assertThat(ids.add(claims.getId())).isTrue();
        }
    }

    @Test
    void typelessAndUnknownPurposeTokensAreNeverAcceptedIncludingExpiredOnes() {
        for (long offset : new long[]{60, -60}) {
            for (Map<String, Object> claims : java.util.List.of(
                    Map.<String, Object>of(), Map.<String, Object>of("token_use", "other"),
                    Map.<String, Object>of("token_use", 7))) {
                String token = signed("7", claims, offset);
                assertThat(provider.validateAccessToken(token)).isEqualTo(TokenValidationResult.INVALID);
                assertThat(provider.validateRefreshToken(token)).isEqualTo(TokenValidationResult.INVALID);
            }
        }
    }

    @Test
    void expiredResultIsReservedForTheCorrectPurpose() {
        String access = signed("7", Map.of("token_use", "access"), -60L);
        String refresh = signed("7", Map.of("token_use", "refresh", "jti", "issuance"), -60L);
        assertThat(provider.validateAccessToken(access)).isEqualTo(TokenValidationResult.EXPIRED);
        assertThat(provider.validateRefreshToken(access)).isEqualTo(TokenValidationResult.INVALID);
        assertThat(provider.validateRefreshToken(refresh)).isEqualTo(TokenValidationResult.EXPIRED);
        assertThat(provider.validateAccessToken(refresh)).isEqualTo(TokenValidationResult.INVALID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-number", "0", "-1", "9223372036854775808"})
    void invalidSubjectsFailClosed(String subject) {
        assertThat(provider.validateAccessToken(signed(subject, Map.of("token_use", "access"), 60L)))
                .isEqualTo(TokenValidationResult.INVALID);
    }

    @Test
    void expiryAndRefreshIdentityAreMandatory() {
        assertThat(provider.validateAccessToken(signed("7", Map.of("token_use", "access"), null)))
                .isEqualTo(TokenValidationResult.INVALID);
        for (Map<String, Object> claims : java.util.List.of(
                Map.<String, Object>of("token_use", "refresh"),
                Map.<String, Object>of("token_use", "refresh", "jti", " "))) {
            assertThat(provider.validateRefreshToken(signed("7", claims, 60L)))
                    .isEqualTo(TokenValidationResult.INVALID);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"garbage", "a.b.c"})
    void malformedInputFailsClosedWithoutThrowing(String token) {
        assertThat(provider.validateAccessToken(token)).isEqualTo(TokenValidationResult.INVALID);
        assertThat(provider.validateRefreshToken(token)).isEqualTo(TokenValidationResult.INVALID);
    }

    @Test
    void aDifferentSigningKeyCannotAuthorizeEitherPurpose() {
        SecretKey otherKey = Jwts.SIG.HS256.key().build();
        String token = Jwts.builder().subject("7").claim("token_use", "refresh")
                .id("issuance").expiration(Date.from(NOW.plusSeconds(60))).signWith(otherKey).compact();
        assertThat(provider.validateAccessToken(token)).isEqualTo(TokenValidationResult.INVALID);
        assertThat(provider.validateRefreshToken(token)).isEqualTo(TokenValidationResult.INVALID);
    }

    private String signed(String subject, Map<String, Object> claims, Long expiryOffset) {
        var builder = Jwts.builder().subject(subject).claims(claims).issuedAt(Date.from(NOW));
        if (expiryOffset != null) {
            builder.expiration(Date.from(NOW.plusSeconds(expiryOffset)));
        }
        return builder.signWith(key).compact();
    }
}
