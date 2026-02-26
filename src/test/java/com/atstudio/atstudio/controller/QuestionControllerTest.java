package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.dto.question.AnswerResponse;
import com.atstudio.atstudio.dto.question.QuestionListItemResponse;
import com.atstudio.atstudio.dto.question.QuestionResponse;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("QuestionController 권한 테스트")
class QuestionControllerTest {

    @Autowired MockMvc mockMvc;
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
        given(questionService.createQuestion(any(), any())).willReturn(MOCK_QUESTION);

        mockMvc.perform(multipart("/api/questions")
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("category", "DOWNLOAD")
                        .param("isPublic", "false"))
                .andExpect(status().isCreated());
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
        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2, 3}) {
            @Override public String getFilename() { return "file.png"; }
        };
        given(questionService.downloadAttachment(anyLong(), anyLong(), any())).willReturn(resource);

        mockMvc.perform(get("/api/questions/1/attachments/1"))
                .andExpect(status().isOk());
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
