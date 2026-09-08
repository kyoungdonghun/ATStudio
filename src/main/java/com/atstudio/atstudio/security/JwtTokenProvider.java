package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.JwtConfig;
import com.atstudio.atstudio.entity.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;
    private final Clock clock;
    private SecretKey key;

    @Autowired
    public JwtTokenProvider(JwtConfig jwtConfig) {
        this(jwtConfig, Clock.systemUTC());
    }

    JwtTokenProvider(JwtConfig jwtConfig, Clock clock) {
        this.jwtConfig = jwtConfig;
        this.clock = clock;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
    }

    public String generateAccessToken(Long userID, UserRole role) {
        Date now = Date.from(clock.instant());
        Date expiry = new Date(now.getTime() + jwtConfig.getAccessTokenExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userID))
                .claim("token_use", "access")
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userID) {
        Date now = Date.from(clock.instant());
        Date expiry = new Date(now.getTime() + jwtConfig.getRefreshTokenExpiration());
        return Jwts.builder()
                .subject(String.valueOf(userID))
                .claim("token_use", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserID(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public Long getUserIDAllowExpired(String token) {
        try {
            return Long.parseLong(parseToken(token).getSubject());
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public TokenValidationResult validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    public TokenValidationResult validateRefreshToken(String token) {
        return validateToken(token, "refresh");
    }

    private TokenValidationResult validateToken(String token, String expectedUse) {
        try {
            Claims claims;
            boolean expired = false;
            try {
                claims = parseToken(token);
            } catch (ExpiredJwtException e) {
                claims = e.getClaims();
                expired = true;
            }
            // Wrong-purpose and legacy tokens are invalid even when expired.
            if (!expectedUse.equals(claims.get("token_use", String.class))
                    || claims.getExpiration() == null
                    || Long.parseLong(claims.getSubject()) <= 0) {
                return TokenValidationResult.INVALID;
            }
            if ("refresh".equals(expectedUse)
                    && (claims.getId() == null || claims.getId().isBlank())) {
                return TokenValidationResult.INVALID;
            }
            return expired ? TokenValidationResult.EXPIRED : TokenValidationResult.VALID;
        } catch (JwtException | IllegalArgumentException e) {
            return TokenValidationResult.INVALID;
        }
    }

    public long getAccessTokenExpiration() {
        return jwtConfig.getAccessTokenExpiration();
    }
}
