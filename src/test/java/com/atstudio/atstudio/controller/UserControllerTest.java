package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.user.UserDetailResponse;
import com.atstudio.atstudio.dto.user.UserListItemResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                new UserDetailResponse(1L, "nick", "user@test.com", null, null, null, "INDIVIDUAL", "USER", false, null));

        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/users/{id} - ADMIN → 200")
    void updateUser_adminRole_returns200() throws Exception {
        given(userService.updateUserByAdmin(anyLong(), any())).willReturn(
                new UserDetailResponse(1L, "nick", "user@test.com", null, null, null, "INDIVIDUAL", "USER", true, null));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\",\"isVerified\":true}"))
                .andExpect(status().isOk());
    }
}
