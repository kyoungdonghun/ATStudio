package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CompanyCertification 상태 전이 검증")
class CompanyCertificationTest {

    @Nested
    @DisplayName("유효한 상태 전이")
    class ValidTransitions {

        @Test
        @DisplayName("PENDING -> APPROVED 성공")
        void pendingToApproved() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.PENDING);
            LocalDateTime approvedAt = LocalDateTime.now();

            cert.process(CompanyCertificationStatus.APPROVED, "승인", "BIZ-001", approvedAt);

            assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.APPROVED);
            assertThat(cert.getCertificationCode()).isEqualTo("BIZ-001");
            assertThat(cert.getApprovedAt()).isEqualTo(approvedAt);
        }

        @Test
        @DisplayName("PENDING -> REVISION_REQUESTED 성공")
        void pendingToRevisionRequested() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.PENDING);

            cert.process(CompanyCertificationStatus.REVISION_REQUESTED, "서류 보완 필요", null, null);

            assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.REVISION_REQUESTED);
            assertThat(cert.getAdminNote()).isEqualTo("서류 보완 필요");
        }

        @Test
        @DisplayName("PENDING -> REJECTED 성공")
        void pendingToRejected() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.PENDING);

            cert.process(CompanyCertificationStatus.REJECTED, "부적격", null, null);

            assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.REJECTED);
        }

        @Test
        @DisplayName("REVISION_REQUESTED -> PENDING 성공 (재신청)")
        void revisionRequestedToPending() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.REVISION_REQUESTED);

            cert.process(CompanyCertificationStatus.PENDING, null, null, null);

            assertThat(cert.getStatus()).isEqualTo(CompanyCertificationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("유효하지 않은 상태 전이")
    class InvalidTransitions {

        @Test
        @DisplayName("APPROVED -> PENDING 실패 - INVALID_STATE_TRANSITION")
        void approvedToPending() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.APPROVED);

            assertThatThrownBy(() -> cert.process(
                    CompanyCertificationStatus.PENDING, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @Test
        @DisplayName("REJECTED -> PENDING 실패 - INVALID_STATE_TRANSITION")
        void rejectedToPending() {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.REJECTED);

            assertThatThrownBy(() -> cert.process(
                    CompanyCertificationStatus.PENDING, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }

        @ParameterizedTest
        @EnumSource(value = CompanyCertificationStatus.class,
                names = {"APPROVED", "REVISION_REQUESTED", "REJECTED"})
        @DisplayName("REVISION_REQUESTED -> APPROVED/REVISION_REQUESTED/REJECTED 실패")
        void revisionRequestedToInvalid(CompanyCertificationStatus target) {
            CompanyCertification cert = buildCertification(CompanyCertificationStatus.REVISION_REQUESTED);

            assertThatThrownBy(() -> cert.process(target, null, null, null))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }
    }

    private CompanyCertification buildCertification(CompanyCertificationStatus status) {
        return CompanyCertification.builder()
                .status(status)
                .documentPath("/uploads/test/")
                .build();
    }
}
