package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.certification.CompanyCertificationResponse;
import com.atstudio.atstudio.dto.certification.CompanyCertificationSummaryResponse;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.service.CompanyCertificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("CompanyCertificationController 권한 테스트")
class CompanyCertificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CompanyCertificationService certificationService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final CompanyCertificationResponse MOCK_RESPONSE =
            new CompanyCertificationResponse(
                    1L, "PENDING", null, null,
                    "/uploads/company-docs/1/", null,
                    LocalDateTime.now());

    private static final CompanyCertificationResponse MOCK_APPROVED_RESPONSE =
            new CompanyCertificationResponse(
                    1L, "APPROVED", "서류 확인 완료", "BIZ-test-uuid",
                    "/uploads/company-docs/1/", LocalDateTime.now(),
                    LocalDateTime.now());

    // ── 13.1 POST /api/company-certifications ────────────────────────────────

    @Test
    @DisplayName("POST /api/company-certifications - 비인증 -> 401")
    void apply_unauthenticated_returns401() throws Exception {
        MockMultipartFile doc = new MockMultipartFile(
                "documents", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/company-certifications").file(doc))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/company-certifications - 인증 유저 -> 201")
    void apply_success() throws Exception {
        given(certificationService.apply(any(), anyList())).willReturn(MOCK_RESPONSE);

        MockMultipartFile doc = new MockMultipartFile(
                "documents", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/company-certifications").file(doc))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/company-certifications - 비BUSINESS 회원 -> 403")
    void apply_forbidden_nonBusiness() throws Exception {
        given(certificationService.apply(any(), anyList()))
                .willThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        MockMultipartFile doc = new MockMultipartFile(
                "documents", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/company-certifications").file(doc))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/company-certifications - 중복 신청 -> 409")
    void apply_conflict() throws Exception {
        given(certificationService.apply(any(), anyList()))
                .willThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE));

        MockMultipartFile doc = new MockMultipartFile(
                "documents", "doc.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/company-certifications").file(doc))
                .andExpect(status().isConflict());
    }

    // ── 13.2 GET /api/company-certifications/me ──────────────────────────────

    @Test
    @DisplayName("GET /api/company-certifications/me - 비인증 -> 401")
    void getMyStatus_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/company-certifications/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/company-certifications/me - 인증 유저 -> 200")
    void getMyStatus_success() throws Exception {
        given(certificationService.getMyStatus(any())).willReturn(MOCK_RESPONSE);

        mockMvc.perform(get("/api/company-certifications/me"))
                .andExpect(status().isOk());
    }

    // ── 13.3 GET /api/company-certifications ─────────────────────────────────

    @Test
    @DisplayName("GET /api/company-certifications - 비인증 -> 401")
    void listAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/company-certifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/company-certifications - 일반 유저 -> 403")
    void listAll_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/company-certifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/company-certifications - ADMIN -> 200")
    void listAll_success() throws Exception {
        given(certificationService.listAll(any(), anyInt(), anyInt()))
                .willReturn(ResponseDTO.<CompanyCertificationSummaryResponse>builder()
                        .dataList(List.of()).build());

        mockMvc.perform(get("/api/company-certifications"))
                .andExpect(status().isOk());
    }

    // ── 13.4 GET /api/company-certifications/{certificationId} ───────────────

    @Test
    @DisplayName("GET /api/company-certifications/1 - 비인증 -> 401")
    void getDetail_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/company-certifications/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/company-certifications/1 - 일반 유저 -> 403")
    void getDetail_forbidden_user() throws Exception {
        mockMvc.perform(get("/api/company-certifications/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/company-certifications/1 - ADMIN -> 200")
    void getDetail_success() throws Exception {
        given(certificationService.getDetail(1L)).willReturn(MOCK_RESPONSE);

        mockMvc.perform(get("/api/company-certifications/1"))
                .andExpect(status().isOk());
    }

    // ── 13.5 PUT /api/company-certifications/{certificationId} ───────────────

    @Test
    @DisplayName("PUT /api/company-certifications/1 - 비인증 -> 401")
    void processReview_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/company-certifications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"adminNote\":\"OK\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/company-certifications/1 - 일반 유저 -> 403")
    void processReview_forbidden() throws Exception {
        mockMvc.perform(put("/api/company-certifications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"adminNote\":\"OK\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/company-certifications/1 - ADMIN 승인 -> 200")
    void processReview_approve() throws Exception {
        given(certificationService.processReview(anyLong(), any()))
                .willReturn(MOCK_APPROVED_RESPONSE);

        mockMvc.perform(put("/api/company-certifications/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"adminNote\":\"서류 확인 완료\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/company-certifications/99 - 미존재 -> 404")
    void processReview_notFound() throws Exception {
        given(certificationService.processReview(anyLong(), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        mockMvc.perform(put("/api/company-certifications/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\",\"adminNote\":\"OK\"}"))
                .andExpect(status().isNotFound());
    }
}
