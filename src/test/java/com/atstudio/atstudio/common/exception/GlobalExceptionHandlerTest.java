package com.atstudio.atstudio.common.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("GlobalExceptionHandler 통합 테스트")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FakeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── FakeController (테스트용 가짜 컨트롤러) ────────────────────────────────

    @RestController
    static class FakeController {

        @GetMapping("/ex/business")
        void business() {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }

        @GetMapping("/ex/technic")
        void technic() {
            throw new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);
        }

        @GetMapping("/ex/integrity")
        void integrity() {
            throw new DataIntegrityViolationException("duplicate key value");
        }

        @GetMapping("/ex/constraint")
        void constraint() {
            throw new ConstraintViolationException("validation failed", Set.of());
        }

        @GetMapping("/ex/unknown")
        void unknown() {
            throw new RuntimeException("unexpected internal error");
        }

        @PostMapping("/ex/post-only")
        void postOnly() { /* POST만 허용 — 다른 메서드로 호출 시 405 */ }
    }

    // ── 직접 throw 핸들러 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("BusinessException → 해당 HTTP 상태 + errorCode 포함 + 안전한 메시지")
    void businessException_returnsCorrectResponse() throws Exception {
        mockMvc.perform(get("/ex/business").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("요청하신 정보를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("TechnicException → 500 + errorCode 필드 없음 (NON_NULL)")
    void technicException_returns500WithNoErrorCode() throws Exception {
        mockMvc.perform(get("/ex/technic").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").value("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    // ── Fallback 핸들러 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("DataIntegrityViolationException → 409 DATA_INTEGRITY_VIOLATION (Fallback)")
    void dataIntegrityViolation_fallback() throws Exception {
        mockMvc.perform(get("/ex/integrity").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.errorCode").value("DATA_INTEGRITY_VIOLATION"));
    }

    @Test
    @DisplayName("ConstraintViolationException → 400 INVALID_VALID (Fallback)")
    void constraintViolation_fallback() throws Exception {
        mockMvc.perform(get("/ex/constraint").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("INVALID_VALID"));
    }

    @Test
    @DisplayName("알 수 없는 RuntimeException → 500 UNEXPECTED_ERROR + errorCode 없음 (Fallback)")
    void unknownException_fallback() throws Exception {
        mockMvc.perform(get("/ex/unknown").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.message").value("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    @Test
    @DisplayName("허용되지 않은 HTTP 메서드 → 405 METHOD_NOT_ALLOWED (Fallback)")
    void methodNotAllowed_fallback() throws Exception {
        // /ex/post-only는 POST만 허용 → GET으로 요청 시 405
        mockMvc.perform(get("/ex/post-only").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"));
    }

    // ── 응답 구조 공통 검증 ────────────────────────────────────────────────────

    @Test
    @DisplayName("모든 에러 응답에는 status, error, message 필드가 항상 존재한다")
    void allErrorResponses_haveRequiredFields() throws Exception {
        // BusinessException으로 대표 검증
        mockMvc.perform(get("/ex/business").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("TechnicException 응답에는 내부 기술 정보가 포함되지 않는다")
    void technicException_clientMessageIsSafe() throws Exception {
        mockMvc.perform(get("/ex/technic").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsStringIgnoringCase("sql"))))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsStringIgnoringCase("exception"))))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsStringIgnoringCase("데이터베이스"))));
    }
}
