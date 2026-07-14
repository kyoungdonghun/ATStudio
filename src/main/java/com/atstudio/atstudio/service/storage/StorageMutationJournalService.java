package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.repository.StorageMutationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageMutationJournalService {

    private static final List<StorageMutationState> READY_STATES = List.of(
            StorageMutationState.COMMITTED,
            StorageMutationState.ROLLBACK_CLEANUP,
            StorageMutationState.AFTER_COMMIT_DELETE,
            StorageMutationState.RETRY
    );

    private final StorageMutationRepository mutationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> prepare(List<StorageMutationDraft> drafts) {
        List<StorageMutation> mutations = drafts.stream()
                .map(draft -> StorageMutation.prepared(
                        draft.operationId(),
                        draft.domain(),
                        draft.mutationType(),
                        draft.storageRoot(),
                        draft.newKey(),
                        draft.oldKey()))
                .toList();
        return mutationRepository.saveAllAndFlush(mutations).stream()
                .map(StorageMutation::getId)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transition(List<Long> mutationIds, StorageMutationState state, String reasonCode) {
        mutationRepository.findAllById(mutationIds)
                .forEach(mutation -> mutation.transition(state, reasonCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transition(Long mutationId, StorageMutationState state, String reasonCode) {
        mutationRepository.findById(mutationId)
                .ifPresent(mutation -> mutation.transition(state, reasonCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCleanupFailure(
            Long mutationId,
            String reasonCode,
            LocalDateTime nextAttemptAt,
            int maxAttempts) {
        mutationRepository.findById(mutationId).ifPresent(mutation -> {
            if (mutation.getAttemptCount() + 1 >= maxAttempts) {
                mutation.transition(StorageMutationState.FAILED, reasonCode);
            } else {
                mutation.scheduleRetry(reasonCode, nextAttemptAt, true);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<StorageMutationSnapshot> claimBatch(
            LocalDateTime now,
            LocalDateTime staleBefore,
            LocalDateTime claimUntil,
            int batchSize,
            int maxAttempts) {
        List<StorageMutation> claimed = mutationRepository.findRecoveryCandidates(
                StorageMutationState.PREPARED,
                READY_STATES,
                now,
                staleBefore,
                maxAttempts,
                PageRequest.of(0, batchSize));
        claimed.forEach(mutation -> mutation.claim(claimUntil));
        return claimed.stream().map(this::snapshot).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryClaimed(
            Long mutationId,
            String reasonCode,
            LocalDateTime nextAttemptAt,
            int maxAttempts) {
        mutationRepository.findById(mutationId).ifPresent(mutation -> {
            if (mutation.getAttemptCount() >= maxAttempts) {
                mutation.transition(StorageMutationState.FAILED, reasonCode);
            } else {
                mutation.scheduleRetry(reasonCode, nextAttemptAt, false);
            }
        });
    }

    private StorageMutationSnapshot snapshot(StorageMutation mutation) {
        return new StorageMutationSnapshot(
                mutation.getId(),
                mutation.getOperationId(),
                mutation.getDomain(),
                mutation.getMutationType(),
                mutation.getStorageRoot(),
                mutation.getNewKey(),
                mutation.getOldKey(),
                mutation.getState(),
                mutation.getAttemptCount()
        );
    }
}
