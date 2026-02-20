package com.atstudio.atstudio.config;

import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void validate() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret key must be at least 256 bits (32 bytes). Current: " + keyBytes.length + " bytes.");
        }
    }
}
