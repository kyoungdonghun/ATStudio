package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.PlayHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("PlayHistoryController 권한 테스트")
class PlayHistoryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PlayHistoryService playHistoryService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/play-histories (인증 필요) ──────────────────────────────────

    @Test
    @DisplayName("POST /api/play-histories - 비인증 → 401")
    void save_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/play-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/play-histories - 인증 사용자 → 201")
    void save_authenticated_returns201() throws Exception {
        doNothing().when(playHistoryService).savePlayHistory(any(), any());

        mockMvc.perform(post("/api/play-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackId\":1}"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/play-histories (인증 필요) ───────────────────────────────────

    @Test
    @DisplayName("GET /api/play-histories - 비인증 → 401")
    void getMyHistory_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/play-histories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/play-histories - 인증 사용자 → 200")
    void getMyHistory_authenticated_returns200() throws Exception {
        given(playHistoryService.getMyHistory(any(), anyInt(), anyInt())).willReturn(null);

        mockMvc.perform(get("/api/play-histories"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/play-histories (인증 필요) ────────────────────────────────

    @Test
    @DisplayName("DELETE /api/play-histories - 비인증 → 401")
    void delete_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/play-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"historyIds\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/play-histories - 인증 사용자 → 204")
    void delete_authenticated_returns204() throws Exception {
        doNothing().when(playHistoryService).deleteHistory(any(), any());

        mockMvc.perform(delete("/api/play-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"historyIds\":[]}"))
                .andExpect(status().isNoContent());
    }
}
