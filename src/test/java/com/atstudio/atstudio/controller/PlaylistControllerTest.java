package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.playlist.PlaylistDetailResponse;
import com.atstudio.atstudio.dto.playlist.PlaylistResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.PlaylistService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("PlaylistController 권한 테스트")
class PlaylistControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean PlaylistService playlistService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/playlists ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/playlists - 비인증 → 401")
    void createPlaylist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/playlists")
                        .param("title", "Test Playlist"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/playlists - 인증 사용자 → 201")
    void createPlaylist_authenticated_returns201() throws Exception {
        given(playlistService.createPlaylist(any(), any(), any()))
                .willReturn(new PlaylistResponse(1L, "Test Playlist", null, null, 0, null));

        mockMvc.perform(multipart("/api/playlists")
                        .param("title", "Test Playlist"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/playlists ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/playlists - 비인증 → 401")
    void getMyPlaylists_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/playlists - 인증 사용자 → 200")
    void getMyPlaylists_authenticated_returns200() throws Exception {
        given(playlistService.getMyPlaylists(any())).willReturn(List.of());

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk());
    }

    // ── GET /api/playlists/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/playlists/{id} - 비인증 → 401")
    void getPlaylistDetail_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/playlists/{id} - 인증 사용자 → 200")
    void getPlaylistDetail_authenticated_returns200() throws Exception {
        given(playlistService.getPlaylistDetail(anyLong(), any()))
                .willReturn(new PlaylistDetailResponse(1L, "Test", null, null, List.of(), null, null));

        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isOk());
    }

    // ── POST /api/playlists/{id}/tracks ──────────────────────────────────────

    @Test
    @DisplayName("POST /api/playlists/{id}/tracks - 비인증 → 401")
    void addTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/playlists/1/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/playlists/{id}/tracks - 인증 사용자 → 201")
    void addTrack_authenticated_returns201() throws Exception {
        doNothing().when(playlistService).addTrack(anyLong(), any(), any());

        mockMvc.perform(post("/api/playlists/1/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trackId\":1}"))
                .andExpect(status().isCreated());
    }

    // ── PUT /api/playlists/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/playlists/{id} - 비인증 → 401")
    void updatePlaylist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/playlists/1")
                        .param("title", "Updated")
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/playlists/{id} - 인증 사용자 → 200")
    void updatePlaylist_authenticated_returns200() throws Exception {
        given(playlistService.updatePlaylist(anyLong(), any(), any(), any()))
                .willReturn(new PlaylistResponse(1L, "Updated", null, null, 0, null));

        mockMvc.perform(multipart("/api/playlists/1")
                        .param("title", "Updated")
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk());
    }

    // ── PUT /api/playlists/{id}/tracks ────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/playlists/{id}/tracks - 비인증 → 401")
    void reorderTracks_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/playlists/1/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tracks\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/playlists/{id}/tracks - 인증 사용자 → 200")
    void reorderTracks_authenticated_returns200() throws Exception {
        doNothing().when(playlistService).reorderTracks(anyLong(), any(), any());

        mockMvc.perform(put("/api/playlists/1/tracks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tracks\":[{\"trackId\":1,\"trackOrder\":0}]}"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/playlists/{id}/tracks/{trackId} ───────────────────────────

    @Test
    @DisplayName("DELETE /api/playlists/{id}/tracks/{trackId} - 비인증 → 401")
    void removeTrack_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/playlists/1/tracks/5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/playlists/{id}/tracks/{trackId} - 인증 사용자 → 204")
    void removeTrack_authenticated_returns204() throws Exception {
        doNothing().when(playlistService).removeTrack(anyLong(), anyLong(), any());

        mockMvc.perform(delete("/api/playlists/1/tracks/5"))
                .andExpect(status().isNoContent());
    }

    // ── DELETE /api/playlists/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/playlists/{id} - 비인증 → 401")
    void deletePlaylist_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/playlists/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/playlists/{id} - 인증 사용자 → 204")
    void deletePlaylist_authenticated_returns204() throws Exception {
        doNothing().when(playlistService).deletePlaylist(anyLong(), any());

        mockMvc.perform(delete("/api/playlists/1"))
                .andExpect(status().isNoContent());
    }
}
