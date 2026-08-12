package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.notice.NoticeResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.NoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("NoticeController 권한 테스트")
class NoticeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean NoticeService noticeService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final NoticeResponse MOCK_NOTICE =
            new NoticeResponse(1L, "공지제목", "공지내용", false, 0L, null, null, null);

    // ── POST /api/notices (ADMIN 전용) ────────────────────────────────────────

    @Test
    @DisplayName("POST /api/notices - 비인증 → 401")
    void createNotice_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/notices")
                        .param("title", "공지")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/notices - 일반 유저 → 403")
    void createNotice_userRole_returns403() throws Exception {
        mockMvc.perform(multipart("/api/notices")
                        .param("title", "공지")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/notices - ADMIN → 201")
    void createNotice_adminRole_returns201() throws Exception {
        given(noticeService.createNotice(any(), any())).willReturn(MOCK_NOTICE);

        mockMvc.perform(multipart("/api/notices")
                        .param("title", "공지")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isCreated());
    }

    // ── GET /api/notices (PUBLIC) ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/notices - 비인증도 접근 가능 (PUBLIC) → 200")
    void getNotices_unauthenticated_returns200() throws Exception {
        given(noticeService.getNotices(anyInt(), anyInt(), any())).willReturn(null);

        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk());
    }

    // ── GET /api/notices/{id} (PUBLIC) ────────────────────────────────────────

    @Test
    @DisplayName("GET /api/notices/{id} - 비인증도 접근 가능 (PUBLIC) → 200")
    void getNotice_unauthenticated_returns200() throws Exception {
        given(noticeService.getNotice(anyLong())).willReturn(MOCK_NOTICE);

        mockMvc.perform(get("/api/notices/1"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/notices/{id} (ADMIN 전용) ────────────────────────────────────

    @Test
    @DisplayName("PUT /api/notices/{id} - 비인증 → 401")
    void updateNotice_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipartPut("/api/notices/1")
                        .param("title", "수정")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/notices/{id} - 일반 유저 → 403")
    void updateNotice_userRole_returns403() throws Exception {
        mockMvc.perform(multipartPut("/api/notices/1")
                        .param("title", "수정")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/notices/{id} - ADMIN → 200")
    void updateNotice_adminRole_returns200() throws Exception {
        given(noticeService.updateNotice(anyLong(), any(), any())).willReturn(MOCK_NOTICE);

        mockMvc.perform(multipartPut("/api/notices/1")
                        .param("title", "수정")
                        .param("content", "내용")
                        .param("isPinned", "false"))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/notices/{id} (ADMIN 전용) ─────────────────────────────────

    @Test
    @DisplayName("DELETE /api/notices/{id} - 비인증 → 401")
    void deleteNotice_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/notices/{id} - 일반 유저 → 403")
    void deleteNotice_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/notices/{id} - ADMIN → 204")
    void deleteNotice_adminRole_returns204() throws Exception {
        doNothing().when(noticeService).deleteNotice(anyLong(), any());

        mockMvc.perform(delete("/api/notices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET notice attachment remains public and forces the full safe download headers")
    void downloadAttachment_unauthenticated_forcesSafeHeaders() throws Exception {
        byte[] body = "<html><script>alert(1)</script>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(body) {
            @Override
            public String getFilename() {
                return "generated.html";
            }
        };
        given(noticeService.downloadAttachment(1L, 7L)).willReturn(resource);

        mockMvc.perform(get("/api/notices/1/attachments/7"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''generated.html"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store, private"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(
                        "Content-Security-Policy",
                        "default-src 'none'; sandbox"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"));
    }

    @Test
    @DisplayName("GET notice attachment percent-encodes CRLF and disposition delimiters")
    void downloadAttachment_maliciousFilename_cannotInjectResponseHeader() throws Exception {
        ByteArrayResource resource = new ByteArrayResource(new byte[] {1, 2, 3}) {
            @Override
            public String getFilename() {
                return "report\r\nX-Evil: injected\"; filename=\"owned.html";
            }
        };
        given(noticeService.downloadAttachment(1L, 8L)).willReturn(resource);

        mockMvc.perform(get("/api/notices/1/attachments/8"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''report%0D%0AX-Evil%3A%20injected%22%3B%20filename%3D%22owned.html"))
                .andExpect(header().doesNotExist("X-Evil"));
    }

    private MockMultipartHttpServletRequestBuilder multipartPut(String urlTemplate, Object... uriVars) {
        return multipart(urlTemplate, uriVars)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
    }
}
