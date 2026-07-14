package com.atstudio.atstudio.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageMutationRecoveryService {

    private final StorageMutationJournalService journalService;
    private final StorageCleanupService cleanupService;
    private final StorageReferenceChecker referenceChecker;

    @Value("${app.storage.recovery.batch-size:50}")
    private int batchSize;

    @Value("${app.storage.recovery.max-attempts:8}")
    private int maxAttempts;

    @Value("${app.storage.recovery.stale-seconds:300}")
    private long staleSeconds;

    @Value("${app.storage.recovery.claim-seconds:120}")
    private long claimSeconds;

    @EventListener(ApplicationReadyEvent.class)
    public void recoverOnStartup() {
        recoverBatch();
    }

    @Scheduled(fixedDelayString = "${app.storage.recovery.interval-ms:60000}")
    public RecoveryResult recoverBatch() {
        LocalDateTime now = LocalDateTime.now();
        List<StorageMutationSnapshot> claimed = journalService.claimBatch(
                now,
                now.minusSeconds(staleSeconds),
                now.plusSeconds(claimSeconds),
                batchSize,
                maxAttempts);

        int completed = 0;
        int retained = 0;
        int retried = 0;
        for (StorageMutationSnapshot mutation : claimed) {
            RecoveryOutcome outcome = recover(mutation, now);
            switch (outcome) {
                case COMPLETED -> completed++;
                case RETAINED -> retained++;
                case RETRIED -> retried++;
            }
        }

        if (!claimed.isEmpty()) {
            log.info(
                    "Storage recovery processed. claimed={}, completed={}, retained={}, retried={}",
                    claimed.size(),
                    completed,
                    retained,
                    retried);
        }
        return new RecoveryResult(claimed.size(), completed, retained, retried);
    }

    private RecoveryOutcome recover(StorageMutationSnapshot mutation, LocalDateTime now) {
        try {
            return switch (mutation.mutationType()) {
                case CREATE -> recoverCreate(mutation, now);
                case REPLACE -> recoverReplace(mutation, now);
                case DELETE -> recoverDelete(mutation, now);
            };
        } catch (RuntimeException exception) {
            scheduleRetry(mutation, "RECOVERY_EXCEPTION", now);
            return RecoveryOutcome.RETRIED;
        }
    }

    private RecoveryOutcome recoverCreate(StorageMutationSnapshot mutation, LocalDateTime now) {
        if (referenceChecker.isReferenced(mutation.domain(), mutation.newKey())) {
            complete(mutation, "RECOVERED_CREATE_COMMIT");
            return RecoveryOutcome.COMPLETED;
        }
        return cleanupNew(mutation, now);
    }

    private RecoveryOutcome recoverReplace(StorageMutationSnapshot mutation, LocalDateTime now) {
        if (referenceChecker.isReferenced(mutation.domain(), mutation.newKey())) {
            return cleanupOld(mutation, now);
        }
        return cleanupNew(mutation, now);
    }

    private RecoveryOutcome recoverDelete(StorageMutationSnapshot mutation, LocalDateTime now) {
        if (referenceChecker.isReferenced(mutation.domain(), mutation.oldKey())) {
            complete(mutation, "RECOVERED_DELETE_ROLLBACK");
            return RecoveryOutcome.RETAINED;
        }
        return cleanupOld(mutation, now);
    }

    private RecoveryOutcome cleanupNew(StorageMutationSnapshot mutation, LocalDateTime now) {
        StorageCleanupService.CleanupOutcome outcome = cleanupService.cleanupNew(
                mutation.storageRoot(),
                mutation.operationId(),
                mutation.newKey());
        if (outcome == StorageCleanupService.CleanupOutcome.DONE) {
            complete(mutation, "RECOVERED_NEW_DELETE");
            return RecoveryOutcome.COMPLETED;
        }
        scheduleRetry(mutation, "RECOVERY_NEW_DELETE_FAILED", now);
        return RecoveryOutcome.RETRIED;
    }

    private RecoveryOutcome cleanupOld(StorageMutationSnapshot mutation, LocalDateTime now) {
        StorageCleanupService.CleanupOutcome outcome = cleanupService.cleanupOld(
                mutation.domain(),
                mutation.storageRoot(),
                mutation.oldKey());
        return switch (outcome) {
            case DONE -> {
                complete(mutation, "RECOVERED_OLD_DELETE");
                yield RecoveryOutcome.COMPLETED;
            }
            case SHARED_REFERENCE -> {
                complete(mutation, "SHARED_REFERENCE_RETAINED");
                yield RecoveryOutcome.RETAINED;
            }
            case FAILED -> {
                scheduleRetry(mutation, "RECOVERY_OLD_DELETE_FAILED", now);
                yield RecoveryOutcome.RETRIED;
            }
        };
    }

    private void complete(StorageMutationSnapshot mutation, String reasonCode) {
        journalService.transition(mutation.id(), StorageMutationState.DONE, reasonCode);
    }

    private void scheduleRetry(
            StorageMutationSnapshot mutation,
            String reasonCode,
            LocalDateTime now) {
        journalService.retryClaimed(
                mutation.id(),
                reasonCode,
                now.plusSeconds(backoffSeconds(mutation.attemptCount())),
                maxAttempts);
    }

    private long backoffSeconds(int attemptCount) {
        int exponent = Math.max(0, Math.min(10, attemptCount - 1));
        return Math.min(21_600L, 30L * (1L << exponent));
    }

    private enum RecoveryOutcome {
        COMPLETED,
        RETAINED,
        RETRIED
    }

    public record RecoveryResult(int claimed, int completed, int retained, int retried) {
    }
}
