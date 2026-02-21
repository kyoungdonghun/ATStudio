package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.LicenseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("LicenseController 권한 테스트")
class LicenseControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean LicenseService licenseService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── GET /api/licenses/me (인증 필요) ─────────────────────────────────────

    @Test
    @DisplayName("GET /api/licenses/me - 비인증 → 401")
    void getMyLicenses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/licenses/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/licenses/me - 인증 사용자 → 보안 통과")
    void getMyLicenses_authenticated_securityPasses() throws Exception {
        given(licenseService.getMyLicenses(any(), anyInt(), anyInt())).willReturn(
                com.atstudio.atstudio.common.dto.ResponseDTO.<com.atstudio.atstudio.dto.license.LicenseListItemResponse>builder()
                        .message("ok").dataList(List.of())
                        .pageInfo(com.atstudio.atstudio.common.dto.PageInfo.of(1, 20, 0, 10))
                        .build());

        mockMvc.perform(get("/api/licenses/me"))
                .andExpect(status().isOk());
    }

    // ── GET /api/users/{id}/licenses (ADMIN 전용 — @PreAuthorize) ─────────────

    @Test
    @DisplayName("GET /api/users/{id}/licenses - 비인증 → 401")
    void getUserLicenses_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/1/licenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/users/{id}/licenses - 일반 유저 → 403")
    void getUserLicenses_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users/1/licenses"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users/{id}/licenses - ADMIN → 보안 통과")
    void getUserLicenses_adminRole_securityPasses() throws Exception {
        given(licenseService.getUserLicenses(anyLong(), anyInt(), anyInt())).willReturn(
                com.atstudio.atstudio.common.dto.ResponseDTO.<com.atstudio.atstudio.dto.license.LicenseListItemResponse>builder()
                        .message("ok").dataList(List.of())
                        .pageInfo(com.atstudio.atstudio.common.dto.PageInfo.of(1, 20, 0, 10))
                        .build());

        mockMvc.perform(get("/api/users/1/licenses"))
                .andExpect(status().isOk());
    }
}
