package com.atstudio.atstudio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                // 이유:
                // - CSRF: Cross-Site Request Forgery 공격 방어 토큰
                // - REST API 서버에서는 보통 비활성화 (JWT 사용 시)
                // - Thymeleaf 폼 사용 시에는 나중에 활성화 필요
                // ⚠️ 운영 환경에서는 신중하게 판단!

                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll()
                        // 이유: 개발 중에는 모든 요청 허용
                        // 나중에 이 부분을 수정해서:
                        // - /api/public/** → 누구나 접근
                        // - /api/user/** → 로그인 필요
                        // - /api/admin/** → 관리자만 접근
                        // 이런 식으로 세밀하게 제어
                );
        return http.build();
    }

}
