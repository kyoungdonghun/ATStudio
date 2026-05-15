package com.atstudio.atstudio.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtConfig validation")
class JwtConfigTest {

    @Test
    @DisplayName("Missing secret shows local bootstrap guidance")
    void validate_missingSecret_throwsHelpfulMessage() {
        JwtConfig config = new JwtConfig();

        ReflectionTestUtils.setField(config, "secret", "");

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing JWT secret")
                .hasMessageContaining("application-local.yml");
    }

    @Test
    @DisplayName("Non-Base64 secret is rejected with format guidance")
    void validate_invalidBase64Secret_throwsHelpfulMessage() {
        JwtConfig config = new JwtConfig();

        ReflectionTestUtils.setField(config, "secret", "not-base64");

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64-encoded");
    }

    @Test
    @DisplayName("Short decoded secret is rejected")
    void validate_shortSecret_throwsHelpfulMessage() {
        JwtConfig config = new JwtConfig();

        ReflectionTestUtils.setField(config, "secret", "dG9vLXNob3J0");

        assertThatThrownBy(config::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 256 bits");
    }
}
