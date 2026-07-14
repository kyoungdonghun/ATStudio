package com.atstudio.atstudio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class CorsConfig {

    private final AcceptanceProperties acceptanceProperties;
    private final String allowedOriginsRaw;

    public CorsConfig(
            AcceptanceProperties acceptanceProperties,
            @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
            String allowedOriginsRaw
    ) {
        this.acceptanceProperties = acceptanceProperties;
        this.allowedOriginsRaw = allowedOriginsRaw;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        Set<String> origins = new LinkedHashSet<>();
        Arrays.stream(allowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .forEach(origins::add);
        if (acceptanceProperties.isEnabled()) {
            origins.add(AcceptancePublicUrls.from(acceptanceProperties.getPublicBaseUrl()).baseUrl());
        }
        config.setAllowedOrigins(List.copyOf(origins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
