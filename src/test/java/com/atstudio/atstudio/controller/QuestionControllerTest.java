package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.question.AnswerResponse;
import com.atstudio.atstudio.dto.question.AttachmentResponse;
import com.atstudio.atstudio.dto.question.QuestionAttachmentDownload;
import com.atstudio.atstudio.dto.question.QuestionListItemResponse;
import com.atstudio.atstudio.dto.question.QuestionResponse;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("QuestionController 권한 테스트")
class QuestionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired QuestionController questionController;
    @MockitoBean QuestionService questionService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final QuestionResponse MOCK_QUESTION = new QuestionResponse(
            1L, "제목", "내용", "DOWNLOAD", false, "OPEN",
            new QuestionResponse.QuestionUserInfo(1L, "user1"),
            List.of(), List.of(), LocalDateTime.now()
    );

    private static final AnswerResponse MOCK_ANSWER = new AnswerResponse(
            1L, "답변", new AnswerResponse.AnswerUserInfo(99L, "admin", "ADMIN"),
            LocalDateTime.now()
    );

    // ── 8.1 POST /api/questions ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/questions - 비인증 → 401")
    void createQuestion_unauthenticated_returns401() throws Exception {
        mockMvc.perform(multipart("/api/questions")
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("category", "DOWNLOAD")
                        .param("isPublic", "false"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/questions - 인증된 유저 → 201")
    void createQuestion_authenticated_returns201() throws Exception {
        QuestionResponse response = new QuestionResponse(
                1L, "title", "content", "DOWNLOAD", false, "OPEN", null,
                List.of(new AttachmentResponse(7L, "evidence.html", 24L)),
                null, LocalDateTime.now()
        );
        given(questionService.createQuestion(any(), any())).willReturn(response);

        mockMvc.perform(multipart("/api/questions")
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("category", "DOWNLOAD")
                        .param("isPublic", "false"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.attachments[0].originalName").value("evidence.html"))
                .andExpect(jsonPath("$.data.attachments[0].filePath").doesNotExist());
    }

    // ── 8.2 POST /api/questions/{id}/answers ────────────────────────────────

    @Test
    @DisplayName("POST /api/questions/{id}/answers - 비인증 → 401")
    void createAnswer_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"답변 내용\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/questions/{id}/answers - 인증됨 → 201")
    void createAnswer_authenticated_returns201() throws Exception {
        given(questionService.createAnswer(anyLong(), any(), any())).willReturn(MOCK_ANSWER);

        mockMvc.perform(post("/api/questions/1/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"답변 내용\"}"))
                .andExpect(status().isCreated());
    }

    // ── 8.3 GET /api/questions ──────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/questions - 비인증 → 401")
    void getQuestions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/questions - 인증됨 → 200")
    void getQuestions_authenticated_returns200() throws Exception {
        given(questionService.getQuestions(anyInt(), anyInt(), any(), any(), any(), any()))
                .willReturn(ResponseDTO.<QuestionListItemResponse>builder().dataList(List.of()).build());

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk());
    }

    // ── 8.4 GET /api/questions/{id} ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/questions/{id} - 비인증 → 401")
    void getQuestion_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/questions/{id} - 인증됨 → 200")
    void getQuestion_authenticated_returns200() throws Exception {
        given(questionService.getQuestion(anyLong(), any())).willReturn(MOCK_QUESTION);

        mockMvc.perform(get("/api/questions/1"))
                .andExpect(status().isOk());
    }

    // ── 8.5 GET /api/questions/{id}/attachments/{id} ────────────────────────

    @Test
    @DisplayName("GET /api/questions/{id}/attachments/{id} - 비인증 → 401")
    void downloadAttachment_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/questions/1/attachments/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/questions/{id}/attachments/{id} - 인증됨 → 200")
    void downloadAttachment_authenticated_returns200() throws Exception {
        byte[] body = "<script>alert(1)</script>".getBytes();
        given(questionService.downloadAttachment(anyLong(), anyLong(), any()))
                .willReturn(new QuestionAttachmentDownload(
                        new ByteArrayResource(body),
                        "evidence.html"
                ));

        MvcResult result = mockMvc.perform(get("/api/questions/1/attachments/1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''evidence.html"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store, private"))
                .andExpect(header().string(HttpHeaders.PRAGMA, "no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string(
                        "Content-Security-Policy",
                        "default-src 'none'; sandbox"))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "none"));
    }

    @Test
    @DisplayName("GET /api/questions/{id}/attachments/{id} - streams the service Resource without buffering")
    void downloadAttachment_streamsServiceResourceWithoutControllerBuffering() throws Exception {
        byte[] body = new byte[]{1, 2, 3};
        Resource resource = new ByteArrayResource(body);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(questionService.downloadAttachment(1L, 2L, userDetails))
                .willReturn(new QuestionAttachmentDownload(resource, "evidence.bin"));

        ResponseEntity<StreamingResponseBody> response = questionController.downloadAttachment(
                1L,
                2L,
                userDetails
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        response.getBody().writeTo(outputStream);
        assertArrayEquals(body, outputStream.toByteArray());
        verify(questionService).downloadAttachment(1L, 2L, userDetails);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/questions/{id}/attachments/{id} - Range and header injection stay fail closed")
    void downloadAttachment_rangeAndHeaderInjection_returnsSafeFullAttachment() throws Exception {
        byte[] body = new byte[]{1, 2, 3, 4};
        given(questionService.downloadAttachment(anyLong(), anyLong(), any()))
                .willReturn(new QuestionAttachmentDownload(
                        new ByteArrayResource(body),
                        "report\r\nX-Evil: injected.html"
                ));

        MvcResult result = mockMvc.perform(get("/api/questions/1/attachments/1")
                        .header(HttpHeaders.RANGE, "bytes=0-1"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''report%0D%0AX-Evil%3A%20injected.html"))
                .andExpect(header().doesNotExist("X-Evil"))
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "none"));
    }

    @Test
    @DisplayName("GET /uploads/questions/** - unauthenticated static access is denied")
    void staticQuestionAttachments_unauthenticatedDenied() throws Exception {
        mockMvc.perform(get("/uploads/questions/attachments/evidence.html"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(questionService);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /uploads/questions/** - USER static access is denied")
    void staticQuestionAttachments_userDenied() throws Exception {
        mockMvc.perform(get("/uploads/questions/attachments/evidence.html"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(questionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /uploads/questions/** - ADMIN static access is denied")
    void staticQuestionAttachments_adminDenied() throws Exception {
        mockMvc.perform(get("/uploads/questions/attachments/evidence.html"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(questionService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /uploads/questions/** - encoded traversal is denied")
    void staticQuestionAttachments_encodedTraversalDenied() throws Exception {
        mockMvc.perform(get("/uploads/questions/%2e%2e/%2e%2e/evidence.html"))
                .andExpect(status().is4xxClientError());

        verifyNoInteractions(questionService);
    }

    // ── 8.6 PUT /api/questions/{id}/status ──────────────────────────────────

    @Test
    @DisplayName("PUT /api/questions/{id}/status - 비인증 → 401")
    void updateStatus_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/questions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/questions/{id}/status - 일반 유저 → 403")
    void updateStatus_userRole_returns403() throws Exception {
        mockMvc.perform(put("/api/questions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/questions/{id}/status - ADMIN → 200")
    void updateStatus_adminRole_returns200() throws Exception {
        given(questionService.updateQuestionStatus(anyLong(), any())).willReturn(MOCK_QUESTION);

        mockMvc.perform(put("/api/questions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk());
    }

    // ── 8.7 DELETE /api/questions/{id} ──────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/questions/{id} - 비인증 → 401")
    void deleteQuestion_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/questions/{id} - 인증됨 → 204")
    void deleteQuestion_authenticated_returns204() throws Exception {
        doNothing().when(questionService).deleteQuestion(anyLong(), any());

        mockMvc.perform(delete("/api/questions/1"))
                .andExpect(status().isNoContent());
    }
}
