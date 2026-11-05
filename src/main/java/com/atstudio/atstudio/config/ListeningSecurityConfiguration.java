package com.atstudio.atstudio.config;

import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.security.AuthRateLimitFilter;
import com.atstudio.atstudio.security.AcceptanceHostFilter;
import com.atstudio.atstudio.security.JwtAuthenticationFilter;
import com.atstudio.atstudio.security.ListeningAccessFilter;
import com.atstudio.atstudio.security.TrustedClientIdentityResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.listening.enabled", havingValue = "true")
public class ListeningSecurityConfiguration {
    @Bean
    @Order(0)
    SecurityFilterChain listeningSecurity(HttpSecurity http, ListeningProperties properties,
            TrustedClientIdentityResolver identity, TrackRepository tracks,
            JwtAuthenticationFilter jwt, AuthRateLimitFilter rateLimit, AcceptanceHostFilter host) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((req, res, error) -> res.sendError(401))
                        .accessDeniedHandler((req, res, error) -> res.sendError(403)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ListeningAccessFilter::publicRequest).permitAll()
                        .requestMatchers("/login", "/admin", "/admin/**", "/api/auth/login",
                                "/api/auth/refresh", "/api/utils/public-capabilities").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimit, JwtAuthenticationFilter.class)
                .addFilterBefore(new ListeningAccessFilter(properties, identity, tracks), AuthRateLimitFilter.class)
                .addFilterBefore(host, ListeningAccessFilter.class);
        return http.build();
    }
}
