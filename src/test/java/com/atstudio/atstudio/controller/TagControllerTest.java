package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.TagService;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("TagController 권한 테스트")
class TagControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TagService tagService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── POST /api/tags (ADMIN 전용) ───────────────────────────────────────────

    @Test
    @DisplayName("POST /api/tags - 비인증 → 401")
    void createTag_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Happy\",\"type\":\"MOOD\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/tags - 일반 유저 → 403")
    void createTag_userRole_returns403() throws Exception {
        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Happy\",\"type\":\"MOOD\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/tags - ADMIN → 201")
    void createTag_adminRole_returns201() throws Exception {
        given(tagService.createTag(any())).willReturn(
                new TagResponse(1L, "Happy", TagType.MOOD, null));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Happy\",\"type\":\"MOOD\"}"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/tags (PUBLIC) ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/tags - 비인증도 접근 가능 (PUBLIC)")
    void getTags_unauthenticated_returns200() throws Exception {
        given(tagService.getAllTags(any())).willReturn(java.util.List.of());

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/tags/available - 악기 필터 파라미터 전달")
    void getAvailableTags_withInstrumentFilter_passesParamsToService() throws Exception {
        given(tagService.getAvailableTags("Pop", "Happy", "Piano", 60, 120))
                .willReturn(List.of());

        mockMvc.perform(get("/api/tags/available")
                        .param("genre", "Pop")
                        .param("mood", "Happy")
                        .param("instrument", "Piano")
                        .param("bpmMin", "60")
                        .param("bpmMax", "120"))
                .andExpect(status().isOk());

        verify(tagService).getAvailableTags("Pop", "Happy", "Piano", 60, 120);
    }

    // ── PUT /api/tags/{id} (ADMIN 전용) ───────────────────────────────────────

    @Test
    @DisplayName("PUT /api/tags/{id} - 비인증 → 401")
    void updateTag_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\",\"type\":\"MOOD\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/tags/{id} - 일반 유저 → 403")
    void updateTag_userRole_returns403() throws Exception {
        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\",\"type\":\"MOOD\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tags/{id} - ADMIN → 200")
    void updateTag_adminRole_returns200() throws Exception {
        given(tagService.updateTag(anyLong(), any())).willReturn(
                new TagResponse(1L, "NewName", TagType.MOOD, null));

        mockMvc.perform(put("/api/tags/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\",\"type\":\"MOOD\"}"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/tags/{id} (ADMIN 전용) ────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/tags/{id} - 비인증 → 401")
    void deleteTag_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/tags/{id} - 일반 유저 → 403")
    void deleteTag_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tags/{id} - ADMIN → 204")
    void deleteTag_adminRole_returns204() throws Exception {
        doNothing().when(tagService).deleteTag(anyLong());

        mockMvc.perform(delete("/api/tags/1"))
                .andExpect(status().isNoContent());
    }
}
