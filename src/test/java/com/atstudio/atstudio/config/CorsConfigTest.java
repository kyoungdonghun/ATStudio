package com.atstudio.atstudio.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void usesExplicitOriginsWithoutWildcardCloudflarePattern() {
        AcceptanceProperties acceptance = new AcceptanceProperties();
        CorsConfiguration configuration = configuration(
                new CorsConfig(acceptance, "http://localhost:5173, https://client.example")
        );

        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://client.example");
        assertThat(configuration.getAllowedOriginPatterns()).isNullOrEmpty();
    }

    @Test
    void addsOnlyTheValidatedAcceptanceOrigin() {
        AcceptanceProperties acceptance = new AcceptanceProperties();
        acceptance.setEnabled(true);
        acceptance.setPublicBaseUrl("https://demo.trycloudflare.com");
        CorsConfiguration configuration = configuration(
                new CorsConfig(acceptance, "http://localhost:5173")
        );

        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:5173", "https://demo.trycloudflare.com");
        assertThat(configuration.getAllowedOriginPatterns()).isNullOrEmpty();
    }

    private CorsConfiguration configuration(CorsConfig config) {
        CorsConfigurationSource source = config.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tracks");
        return source.getCorsConfiguration(request);
    }
}
