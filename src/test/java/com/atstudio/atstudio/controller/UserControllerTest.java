package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.user.UserDetailResponse;
import com.atstudio.atstudio.dto.user.UserListItemResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("UserController Admin 권한 테스트")
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean UserService userService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── GET /api/users (ADMIN 전용) ───────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users - 비인증 → 401")
    void getUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/users - 일반 유저 → 403")
    void getUsers_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users - ADMIN → 200")
    void getUsers_adminRole_returns200() throws Exception {
        given(userService.getUsers(any(), any(), anyInt(), anyInt())).willReturn(
                ResponseDTO.<UserListItemResponse>withAll()
                        .dataList(List.of())
                        .pageInfo(PageInfo.of(1, 20, 0, 10))
                        .build());

        mockMvc.perform(get("/api/users")).andExpect(status().isOk());
    }

    // ── GET /api/users/{id} (ADMIN 전용) ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/users/{id} - 비인증 → 401")
    void getUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/users/{id} - 일반 유저 → 403")
    void getUser_userRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/users/{id} - ADMIN → 200")
    void getUser_adminRole_returns200() throws Exception {
        given(userService.getUser(anyLong())).willReturn(
                new UserDetailResponse(
                        1L,
                        "nick",
                        "user@test.com",
                        "010-0000-0000",
                        "02-000-0000",
                        "EDITOR",
                        "ATStudio Partner",
                        "BUSINESS",
                        "USER",
                        true,
                        null));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nickname").value("nick"))
                .andExpect(jsonPath("$.data.email").value("user@test.com"))
                .andExpect(jsonPath("$.data.phonePersonal").value("010-0000-0000"))
                .andExpect(jsonPath("$.data.phoneCompany").value("02-000-0000"))
                .andExpect(jsonPath("$.data.job").value("EDITOR"))
                .andExpect(jsonPath("$.data.companyName").value("ATStudio Partner"))
                .andExpect(jsonPath("$.data.userType").value("BUSINESS"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.isVerified").value(true))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.accessToken").doesNotExist())
                .andExpect(jsonPath("$.data.refreshToken").doesNotExist());
    }

    // ── PUT /api/users/{id} (ADMIN 전용) ──────────────────────────────────────

    @Test
    @DisplayName("PUT /api/users/{id} - 비인증 → 401")
    void updateUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\",\"isVerified\":true}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/users/{id} - 일반 유저 → 403")
    void updateUser_userRole_returns403() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\",\"isVerified\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - ADMIN → 200")
    void updateUser_adminRole_returns200() throws Exception {
        given(userService.updateUserByAdmin(anyLong(), anyLong(), any())).willReturn(
                new UserDetailResponse(1L, "nick", "user@test.com", null, null, null, null, "INDIVIDUAL", "USER", true, null));

        mockMvc.perform(put("/api/users/1")
                        .with(user(adminDetails(99L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\",\"isVerified\":true,\"reason\":\"ticket 14\"}"))
                .andExpect(status().isOk());
        verify(userService).updateUserByAdmin(
                eq(99L),
                eq(1L),
                argThat(request -> "ticket 14".equals(request.getReason())));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 500자를 넘는 운영 사유는 400")
    void updateUser_reasonOverLimit_returns400() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(user(adminDetails(99L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\",\"reason\":\"" + "x".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserByAdmin(anyLong(), anyLong(), any());
    }

    private CustomUserDetails adminDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("admin@test.com")
                .password("encoded")
                .role(UserRole.ADMIN)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}
