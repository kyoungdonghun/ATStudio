package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationState;
import com.atstudio.atstudio.service.storage.StorageMutationType;
import com.atstudio.atstudio.service.storage.StorageRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "storage_mutations",
        indexes = {
                @Index(name = "idx_storage_mutations_recovery", columnList = "state,next_attempt_at,id"),
                @Index(name = "idx_storage_mutations_operation_id", columnList = "operation_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StorageMutation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_id", nullable = false, length = 36)
    private String operationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StorageDomain domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false, length = 16)
    private StorageMutationType mutationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_root", nullable = false, length = 16)
    private StorageRoot storageRoot;

    @Column(name = "new_key", length = 500)
    private String newKey;

    @Column(name = "old_key", length = 500)
    private String oldKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StorageMutationState state;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "reason_code", length = 64)
    private String reasonCode;

    public static StorageMutation prepared(
            String operationId,
            StorageDomain domain,
            StorageMutationType mutationType,
            StorageRoot storageRoot,
            String newKey,
            String oldKey) {
        return new StorageMutation(
                null,
                operationId,
                domain,
                mutationType,
                storageRoot,
                newKey,
                oldKey,
                StorageMutationState.PREPARED,
                0,
                null,
                "PREPARED"
        );
    }

    public void transition(StorageMutationState nextState, String reasonCode) {
        this.state = nextState;
        this.reasonCode = reasonCode;
        if (nextState == StorageMutationState.DONE || nextState == StorageMutationState.FAILED) {
            this.nextAttemptAt = null;
        }
    }

    public void claim(LocalDateTime claimUntil) {
        this.attemptCount++;
        this.nextAttemptAt = claimUntil;
    }

    public void scheduleRetry(String reasonCode, LocalDateTime nextAttemptAt, boolean incrementAttempt) {
        if (incrementAttempt) {
            this.attemptCount++;
        }
        this.state = StorageMutationState.RETRY;
        this.reasonCode = reasonCode;
        this.nextAttemptAt = nextAttemptAt;
    }
}
