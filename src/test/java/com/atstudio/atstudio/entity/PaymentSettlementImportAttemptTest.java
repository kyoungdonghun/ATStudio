package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentSettlementImportAttempt entity tests")
class PaymentSettlementImportAttemptTest {

    @Test
    @DisplayName("completed attempt enforces count conservation and a stable public batch identity")
    void completesOnlyWithConservedCounts() {
        PaymentSettlementImportAttempt attempt = attempt();
        ReflectionTestUtils.setField(attempt, "id", 41L);

        attempt.complete(4, 1, 2, 1, LocalDateTime.of(2026, 8, 12, 9, 0));

        assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(attempt.importBatchKey()).isEqualTo("ATS-SETTLE-ATTEMPT-41");
        assertThat(attempt.getTotalRows())
                .isEqualTo(attempt.getImportedRows() + attempt.getDuplicateRows() + attempt.getFailedRows());
        assertThatThrownBy(() -> attempt.fail("LATE_FAILURE", LocalDateTime.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("attempt rejects unconserved or negative completion counts")
    void rejectsInvalidCompletionCounts() {
        assertThatThrownBy(() -> attempt().complete(3, 1, 1, 0, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> attempt().complete(0, -1, 0, 1, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("failed attempt retains only a bounded internal failure code")
    void boundsFailureCode() {
        PaymentSettlementImportAttempt attempt = attempt();

        attempt.fail("F".repeat(150), LocalDateTime.of(2026, 8, 12, 9, 0));

        assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.FAILED);
        assertThat(attempt.getFailureCode()).hasSize(100);
    }

    private PaymentSettlementImportAttempt attempt() {
        return PaymentSettlementImportAttempt.builder()
                .keyDigest("a".repeat(64))
                .actorUser(User.builder().id(1L).build())
                .operatorNote("bounded note")
                .build();
    }
}
