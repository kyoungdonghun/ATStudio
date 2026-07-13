package com.atstudio.atstudio.config;

import com.atstudio.atstudio.security.AuthRateLimitFilter;
import com.atstudio.atstudio.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"인증이 필요합니다. 다시 로그인해주세요.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"해당 정보를 열람할 수 없습니다.\"}");
                })
            )
            .authorizeHttpRequests(auth -> auth
                // PUBLIC
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/social/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/verify-email").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/public-capabilities").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-phone").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/utils/check-nickname").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks/admin", "/api/tracks/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tracks/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tracks/*/stream").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tags/available").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subscriptions").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subscriptions/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/subscriptions/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/notices/*/attachments/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/settings/*").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/settings/*").hasRole("ADMIN")
                .requestMatchers("/uploads/tracks/audio/**").denyAll()
                .requestMatchers(HttpMethod.GET, "/uploads/company-docs/**").hasRole("ADMIN")
                // Swagger (dev only -- SEC-15: checked at application level via profile)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // USER — /api/users/me must precede ADMIN wildcard /api/users/*
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/users/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me/complete-profile").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/me/password").authenticated()
                // ADMIN
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/*/licenses", "/api/users/*/licenses/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tracks").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tracks/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tracks/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/tags").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tags/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tags/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/notices").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/notices/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/notices/*").hasRole("ADMIN")
                // Album - public read, ADMIN write
                .requestMatchers(HttpMethod.GET, "/api/albums", "/api/albums/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/albums").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/albums/*/tracks").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/albums/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/albums/*/tracks").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/albums/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/albums/*/tracks/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/company-certifications").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/company-certifications/me/documents").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/company-certifications/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/company-certifications").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/company-certifications/*/documents/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/company-certifications/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/company-certifications/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/questions/*/status").hasRole("ADMIN")
                // User Subscriptions - /me endpoints are AUTH (caught by authenticated() below)
                .requestMatchers(HttpMethod.GET, "/api/user-subscriptions/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/user-subscriptions/me").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user-subscriptions/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/user-subscriptions").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/user-subscriptions/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/user-subscriptions/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/user-subscriptions/*").hasRole("ADMIN")
                // Admin dashboard
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // All other /api/** require authentication
                .requestMatchers("/api/**").authenticated()
                // Static resources
                .anyRequest().permitAll()
            )
            .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
