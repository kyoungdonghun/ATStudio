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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
