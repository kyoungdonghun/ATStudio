package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;

import java.time.LocalDateTime;

public record AdminPaymentSettlementImportAttemptResponse(
        Long attemptId,
        String importBatchKey,
        Long actorUserId,
        PaymentSettlementImportAttemptState state,
        int totalRows,
        int importedRows,
        int skippedDuplicateRows,
        int failedRows,
        String operatorNote,
        String failureCode,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static AdminPaymentSettlementImportAttemptResponse from(PaymentSettlementImportAttempt attempt) {
        return new AdminPaymentSettlementImportAttemptResponse(
                attempt.getId(),
                attempt.importBatchKey(),
                attempt.getActorUser().getId(),
                attempt.getState(),
                attempt.getTotalRows(),
                attempt.getImportedRows(),
                attempt.getDuplicateRows(),
                attempt.getFailedRows(),
                attempt.getOperatorNote(),
                attempt.getFailureCode(),
                attempt.getCompletedAt(),
                attempt.getCreatedAt(),
                attempt.getUpdatedAt());
    }
}
