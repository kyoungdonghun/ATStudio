package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/tags - raw 200자 초과도 안정적인 400 TAG_NAME_INVALID")
    void createTag_rawOverflow_returnsStableDomainError() throws Exception {
        given(tagService.createTag(any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.TAG_NAME_INVALID));
        String rawName = "가".repeat(201);

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + rawName + "\",\"type\":\"MOOD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("TAG_NAME_INVALID"))
                .andExpect(jsonPath("$.message").value("태그 이름 형식을 확인해주세요."));
        verify(tagService).createTag(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/tags - 중복이면 안정적인 409 TAG_NAME_DUPLICATED")
    void createTag_duplicate_returnsStableDomainError() throws Exception {
        given(tagService.createTag(any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED));

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hip Hop\",\"type\":\"GENRE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("TAG_NAME_DUPLICATED"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 태그 이름입니다."));
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
    @DisplayName("GET /api/tags/available - Instrument 반복 파라미터와 dataList 계약")
    void getAvailableTags_preservesInstrumentValues() throws Exception {
        given(tagService.getAvailableTags(
                isNull(),
                isNull(),
                eq(java.util.List.of("Piano, Synth", "808 #Kit")),
                eq(java.util.List.of("쇼츠 용")),
                isNull(),
                isNull()))
                .willReturn(java.util.List.of(
                        new TagResponse(3L, "Piano, Synth", TagType.INSTRUMENT, null)));

        mockMvc.perform(get("/api/tags/available")
                        .queryParam("instrument", "Piano, Synth", "808 #Kit")
                        .queryParam("usage", "쇼츠 용"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList[0].name").value("Piano, Synth"))
                .andExpect(jsonPath("$.dataList[0].type").value("INSTRUMENT"));

        verify(tagService).getAvailableTags(
                isNull(),
                isNull(),
                eq(java.util.List.of("Piano, Synth", "808 #Kit")),
                eq(java.util.List.of("쇼츠 용")),
                isNull(),
                isNull());
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
