package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.DownloadQueueService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("DownloadQueueController 권한 테스트")
class DownloadQueueControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DownloadQueueService downloadQueueService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/download-queue/{trackId} (인증 필요) ────────────────────────

    @Test
    @DisplayName("POST /api/download-queue/{trackId} - 비인증 → 401")
    void addToQueue_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/download-queue/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/download-queue/{trackId} - 인증 사용자 → 201")
    void addToQueue_authenticated_returns201() throws Exception {
        doNothing().when(downloadQueueService).addToQueue(anyLong(), any());

        mockMvc.perform(post("/api/download-queue/1"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/download-queue (인증 필요) ───────────────────────────────────

    @Test
    @DisplayName("GET /api/download-queue - 비인증 → 401")
    void getMyQueue_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/download-queue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/download-queue - 인증 사용자 → 200")
    void getMyQueue_authenticated_returns200() throws Exception {
        given(downloadQueueService.getMyQueue(any())).willReturn(List.of());

        mockMvc.perform(get("/api/download-queue"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/download-queue/{trackId} (인증 필요) ──────────────────────

    @Test
    @DisplayName("DELETE /api/download-queue/{trackId} - 비인증 → 401")
    void removeFromQueue_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/download-queue/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/download-queue/{trackId} - 인증 사용자 → 204")
    void removeFromQueue_authenticated_returns204() throws Exception {
        doNothing().when(downloadQueueService).removeFromQueue(anyLong(), any());

        mockMvc.perform(delete("/api/download-queue/1"))
                .andExpect(status().isNoContent());
    }
}
