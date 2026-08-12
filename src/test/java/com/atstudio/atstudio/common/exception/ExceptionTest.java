package com.atstudio.atstudio.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("예외 클래스 및 에러코드 ENUM 테스트")
class ExceptionTest {

    // ── BUSINESS_ERROR ENUM ───────────────────────────────────────────────────

    @ParameterizedTest(name = "BUSINESS_ERROR.{0}: 필수 필드 null 없음")
    @EnumSource(BUSINESS_ERROR.class)
    @DisplayName("BUSINESS_ERROR: 모든 값의 status/clientMessage/developerMessage는 null이 아니다")
    void businessError_allValuesHaveNonNullFields(BUSINESS_ERROR error) {
        assertThat(error.getStatus()).isNotNull();
        assertThat(error.getClientMessage()).isNotBlank();
        assertThat(error.getDeveloperMessage()).isNotBlank();
    }

    @ParameterizedTest(name = "BUSINESS_ERROR.{0}: 정상 비즈니스 오류는 4xx 상태코드")
    @EnumSource(
            value = BUSINESS_ERROR.class,
            names = "SETTLEMENT_IMPORT_ORCHESTRATION_FAILED",
            mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("BUSINESS_ERROR: 정상 비즈니스 오류는 모두 4xx 상태코드를 가진다")
    void businessError_normalValuesAre4xx(BUSINESS_ERROR error) {
        int statusCode = error.getStatus().value();
        assertThat(statusCode).isBetween(400, 499);
    }

    @Test
    @DisplayName("BUSINESS_ERROR: 정산 가져오기 오케스트레이션 실패는 명시적으로 500이다")
    void businessError_settlementImportOrchestrationFailureIs500() {
        assertThat(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED.getStatus())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("BUSINESS_ERROR: clientMessage는 개발용 기술 용어를 포함하지 않는다")
    void businessError_clientMessageHasNoTechnicalTerms() {
        for (BUSINESS_ERROR error : BUSINESS_ERROR.values()) {
            String clientMsg = error.getClientMessage();
            assertThat(clientMsg)
                    .as("BUSINESS_ERROR.%s clientMessage should not contain technical terms", error.name())
                    .doesNotContainIgnoringCase("SQL")
                    .doesNotContainIgnoringCase("Exception")
                    .doesNotContainIgnoringCase("NullPointer");
        }
    }

    // ── TECHNIC_ERROR ENUM ────────────────────────────────────────────────────

    @ParameterizedTest(name = "TECHNIC_ERROR.{0}: 필수 필드 null 없음")
    @EnumSource(TECHNIC_ERROR.class)
    @DisplayName("TECHNIC_ERROR: 모든 값(6개)의 status/clientMessage/developerMessage는 null이 아니다")
    void technicError_allValuesHaveNonNullFields(TECHNIC_ERROR error) {
        assertThat(error.getStatus()).isNotNull();
        assertThat(error.getClientMessage()).isNotBlank();
        assertThat(error.getDeveloperMessage()).isNotBlank();
    }

    @ParameterizedTest(name = "TECHNIC_ERROR.{0}: 5xx 상태코드")
    @EnumSource(TECHNIC_ERROR.class)
    @DisplayName("TECHNIC_ERROR: 모든 값은 5xx 상태코드를 가진다")
    void technicError_allValuesAre5xx(TECHNIC_ERROR error) {
        int statusCode = error.getStatus().value();
        assertThat(statusCode).isBetween(500, 599);
    }

    @ParameterizedTest(name = "TECHNIC_ERROR.{0}: clientMessage는 안전한 메시지")
    @EnumSource(TECHNIC_ERROR.class)
    @DisplayName("TECHNIC_ERROR: clientMessage는 내부 기술 정보를 노출하지 않는다")
    void technicError_clientMessageIsSafe(TECHNIC_ERROR error) {
        String clientMsg = error.getClientMessage();
        assertThat(clientMsg)
                .doesNotContainIgnoringCase("SQL")
                .doesNotContainIgnoringCase("데이터베이스")
                .doesNotContainIgnoringCase("무결성")
                .doesNotContainIgnoringCase("Exception");
    }

    // ── BusinessException ─────────────────────────────────────────────────────

    @Test
    @DisplayName("BusinessException: 생성 시 에러코드를 그대로 보유한다")
    void businessException_holdsErrorCode() {
        BusinessException ex = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        assertThat(ex.getErrorCode()).isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("BusinessException: getStatus()는 ENUM의 status를 반환한다")
    void businessException_returnsCorrectStatus() {
        BusinessException ex = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("BusinessException: getClientMessage()는 ENUM의 clientMessage를 반환한다")
    void businessException_returnsClientMessage() {
        BusinessException ex = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        assertThat(ex.getClientMessage()).isEqualTo("요청하신 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("BusinessException: getDeveloperMessage()는 ENUM의 developerMessage를 반환한다")
    void businessException_returnsDeveloperMessage() {
        BusinessException ex = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        assertThat(ex.getDeveloperMessage()).isEqualTo("요청 리소스가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("BusinessException: getMessage()는 developerMessage를 반환한다 (로깅용)")
    void businessException_getMessageReturnsDeveloperMessage() {
        BusinessException ex = new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);

        assertThat(ex.getMessage()).isEqualTo(ex.getDeveloperMessage());
    }

    // ── TechnicException ──────────────────────────────────────────────────────

    @Test
    @DisplayName("TechnicException: 생성 시 에러코드를 그대로 보유한다")
    void technicException_holdsErrorCode() {
        TechnicException ex = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);

        assertThat(ex.getErrorCode()).isEqualTo(TECHNIC_ERROR.UNEXPECTED_ERROR);
    }

    @Test
    @DisplayName("TechnicException: getStatus()는 ENUM의 status를 반환한다")
    void technicException_returnsCorrectStatus() {
        TechnicException ex = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("TechnicException: getClientMessage()는 안전한 메시지를 반환한다")
    void technicException_returnsClientMessage() {
        TechnicException ex = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);

        assertThat(ex.getClientMessage()).isEqualTo("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    @DisplayName("TechnicException: getMessage()는 developerMessage를 반환한다 (로깅용)")
    void technicException_getMessageReturnsDeveloperMessage() {
        TechnicException ex = new TechnicException(TECHNIC_ERROR.UNEXPECTED_ERROR);

        assertThat(ex.getMessage()).isEqualTo(ex.getDeveloperMessage());
    }
}
