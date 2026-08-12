package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_settlement_import_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payment_settlement_import_attempts_key_digest",
                columnNames = "key_digest"
        ),
        indexes = {
                @Index(
                        name = "idx_payment_settlement_import_attempts_actor_created",
                        columnList = "actor_user_id,created_at"),
                @Index(
                        name = "idx_payment_settlement_import_attempts_state_created",
                        columnList = "state,created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentSettlementImportAttempt extends BaseEntity {

    private static final int MAX_FAILURE_CODE_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_digest", nullable = false, length = 64, updatable = false)
    private String keyDigest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false, updatable = false)
    private User actorUser;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentSettlementImportAttemptState state = PaymentSettlementImportAttemptState.PROCESSING;

    @Builder.Default
    @Column(name = "total_rows", nullable = false)
    private int totalRows = 0;

    @Builder.Default
    @Column(name = "imported_rows", nullable = false)
    private int importedRows = 0;

    @Builder.Default
    @Column(name = "duplicate_rows", nullable = false)
    private int duplicateRows = 0;

    @Builder.Default
    @Column(name = "failed_rows", nullable = false)
    private int failedRows = 0;

    @Column(name = "operator_note", length = 500)
    private String operatorNote;

    @Column(name = "failure_code", length = MAX_FAILURE_CODE_LENGTH)
    private String failureCode;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public void complete(
            int totalRows,
            int importedRows,
            int duplicateRows,
            int failedRows,
            LocalDateTime completedAt) {
        requireProcessing();
        requireNonNegative(totalRows, importedRows, duplicateRows, failedRows);
        if (totalRows != importedRows + duplicateRows + failedRows) {
            throw new IllegalArgumentException("Completed attempt counts must be conserved.");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("completedAt is required.");
        }
        this.totalRows = totalRows;
        this.importedRows = importedRows;
        this.duplicateRows = duplicateRows;
        this.failedRows = failedRows;
        this.state = PaymentSettlementImportAttemptState.COMPLETED;
        this.failureCode = null;
        this.completedAt = completedAt;
    }

    public void fail(String failureCode, LocalDateTime completedAt) {
        requireProcessing();
        if (failureCode == null || failureCode.isBlank() || completedAt == null) {
            throw new IllegalArgumentException("failureCode and completedAt are required.");
        }
        this.state = PaymentSettlementImportAttemptState.FAILED;
        this.failureCode = truncate(failureCode.trim(), MAX_FAILURE_CODE_LENGTH);
        this.completedAt = completedAt;
    }

    public String importBatchKey() {
        if (id == null) {
            throw new IllegalStateException("Attempt identity is not assigned.");
        }
        return "ATS-SETTLE-ATTEMPT-" + id;
    }

    private void requireProcessing() {
        if (state != PaymentSettlementImportAttemptState.PROCESSING) {
            throw new IllegalStateException("Settlement import attempt is already terminal.");
        }
    }

    private void requireNonNegative(int... values) {
        for (int value : values) {
            if (value < 0) {
                throw new IllegalArgumentException("Attempt counts cannot be negative.");
            }
        }
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
