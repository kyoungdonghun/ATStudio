package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.config.JwtConfig;
import com.atstudio.atstudio.dto.auth.AuthResponse;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.security.JwtTokenProvider;
import com.atstudio.atstudio.service.UserService;
import com.atstudio.atstudio.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.atstudio.atstudio.dto.user.UserResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("SecurityFilterChain 통합 테스트")
class SecurityFilterChainTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired JwtConfig jwtConfig;

    @MockitoBean AuthService authService;
    @MockitoBean UserService userService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── 인증 보호 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/me - 토큰 없이 → 401 Unauthorized")
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/users/me - 만료된 토큰 → 401 + X-Token-Expired: true 헤더")
    void protectedEndpoint_expiredToken_returns401WithHeader() throws Exception {
        String expiredToken = buildExpiredToken(1L);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Token-Expired", "true"));
    }

    // ── PUBLIC 엔드포인트 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login - 토큰 없이 접근 가능 (PUBLIC)")
    void login_publicEndpoint_notBlocked() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("access", "refresh", "Bearer", 3600L));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/login - repeated attempts from same IP => 429 Too Many Requests")
    void login_rateLimitExceeded_returns429() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("access", "refresh", "Bearer", 3600L));

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .with(remoteAddr("10.0.0.25"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/auth/login")
                        .with(remoteAddr("10.0.0.25"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.errorCode").value("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("GET /api/users/me - 유효한 토큰 → 401 아님 (필터 통과)")
    void protectedEndpoint_validToken_filterPasses() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(false)
                .build();
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);

        // UserService.getMyProfile()가 아직 mock이므로 404 등 비즈니스 오류 가능,
        // 그러나 401(Security 필터 차단)이 아님을 검증
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result ->
                        assertNotUnauthorized(result.getResponse().getStatus()));
    }

    // ── CR-P-001: /api/users/me — USER 역할 허용 ─────────────────────────────

    @Test
    @DisplayName("GET /api/users/me - USER 토큰 → 200 (CR-P-001 fix)")
    void getUsersMe_userToken_returns200() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        CustomUserDetails userDetails = buildUserDetails(1L);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);
        when(userService.getMyProfile(1L)).thenReturn(
                new UserResponse(1L, "nick", "user@test.com", null, null, null, null, "INDIVIDUAL", "USER", false, null));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/me - USER 토큰 → 200 (CR-P-001 fix)")
    void putUsersMe_userToken_returns200() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        CustomUserDetails userDetails = buildUserDetails(1L);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);
        when(userService.updateMyProfile(anyLong(), any())).thenReturn(
                new UserResponse(1L, "nick", "user@test.com", null, null, null, null, "INDIVIDUAL", "USER", false, null));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"newNick\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/users/me - USER 토큰 → 204 (CR-P-001 fix)")
    void deleteUsersMe_userToken_returns204() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        CustomUserDetails userDetails = buildUserDetails(1L);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);
        doNothing().when(userService).withdraw(anyLong(), any());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pass123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/users/me/complete-profile - USER 토큰 → 403 아님 (CR-P-001 fix)")
    void putUsersMeCompleteProfile_userToken_notForbidden() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        CustomUserDetails userDetails = buildUserDetails(1L);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);

        mockMvc.perform(put("/api/users/me/complete-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"nick\",\"phonePersonal\":\"010-1234-5678\",\"job\":\"EDITOR\",\"userType\":\"INDIVIDUAL\"}"))
                .andExpect(result -> assertNotForbidden(result.getResponse().getStatus()));
    }

    @Test
    @DisplayName("PUT /api/users/me/password - USER 토큰 → 204 (CR-C-003)")
    void putUsersMePassword_userToken_returns204() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        CustomUserDetails userDetails = buildUserDetails(1L);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(userDetails);
        doNothing().when(userService).updatePassword(anyLong(), any());

        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old\",\"newPassword\":\"new12345\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /api/users/me/password - 비인증 → 401")
    void putUsersMePassword_noToken_returns401() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old\",\"newPassword\":\"new12345\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("user@test.com")
                .password("encoded")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(false)
                .build();
    }

    private String buildExpiredToken(Long userId) {
        JwtConfig expiredConfig = org.mockito.Mockito.mock(JwtConfig.class);
        when(expiredConfig.getSecret()).thenReturn(jwtConfig.getSecret());
        when(expiredConfig.getAccessTokenExpiration()).thenReturn(0L);
        when(expiredConfig.getRefreshTokenExpiration()).thenReturn(0L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredConfig);
        expiredProvider.init();
        return expiredProvider.generateAccessToken(userId, UserRole.USER);
    }

    private void assertNotUnauthorized(int status) {
        if (status == 401) {
            throw new AssertionError("Expected status NOT to be 401, but was 401");
        }
    }

    private void assertNotForbidden(int status) {
        if (status == 403) {
            throw new AssertionError("Expected status NOT to be 403, but was 403");
        }
    }

    private RequestPostProcessor remoteAddr(String remoteAddr) {
        return request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        };
    }
}
