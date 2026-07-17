package com.atstudio.atstudio.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:1209600000}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "Missing JWT secret. Set JWT_SECRET, or explicitly load the repository-root "
                    + "application-local.yml with "
                    + "SPRING_CONFIG_ADDITIONAL_LOCATION=file:./application-local.yml after creating it "
                    + "from application-local.example.yml.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (DecodingException e) {
            throw new IllegalStateException(
                "JWT secret must be Base64-encoded. Update JWT_SECRET, or update application-local.yml "
                    + "and explicitly load it with SPRING_CONFIG_ADDITIONAL_LOCATION, using a Base64 "
                    + "32-byte secret.",
                e);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret key must be at least 256 bits (32 bytes) after Base64 decoding. Current: "
                    + keyBytes.length + " bytes.");
        }
    }
}
