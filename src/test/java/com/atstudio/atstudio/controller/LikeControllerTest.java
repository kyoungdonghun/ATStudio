package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.LikeService;
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
@DisplayName("LikeController 권한 테스트")
class LikeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean LikeService likeService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/likes/{trackId} (인증 필요) ─────────────────────────────────

    @Test
    @DisplayName("POST /api/likes/{trackId} - 비인증 → 401")
    void addLike_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/likes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/likes/{trackId} - 인증 사용자 → 201")
    void addLike_authenticated_returns201() throws Exception {
        doNothing().when(likeService).addLike(anyLong(), any());

        mockMvc.perform(post("/api/likes/1"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/likes (인증 필요) ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/likes - 비인증 → 401")
    void getMyLikes_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/likes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/likes - 인증 사용자 → 200")
    void getMyLikes_authenticated_returns200() throws Exception {
        given(likeService.getMyLikes(any())).willReturn(List.of());

        mockMvc.perform(get("/api/likes"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/likes/{trackId} (인증 필요) ───────────────────────────────

    @Test
    @DisplayName("DELETE /api/likes/{trackId} - 비인증 → 401")
    void removeLike_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/likes/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/likes/{trackId} - 인증 사용자 → 204")
    void removeLike_authenticated_returns204() throws Exception {
        doNothing().when(likeService).removeLike(anyLong(), any());

        mockMvc.perform(delete("/api/likes/1"))
                .andExpect(status().isNoContent());
    }
}
