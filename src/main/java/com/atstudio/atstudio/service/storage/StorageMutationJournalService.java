package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.repository.StorageMutationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Set<String> inFlightOperations = ConcurrentHashMap.newKeySet();
    private final java.util.Map<String, StorageContinuityGuard.Lease> leases = new ConcurrentHashMap<>();
    private StorageContinuityGuard continuity = StorageContinuityGuard.disabled();

    @Autowired
    void setContinuity(StorageContinuityGuard continuity) { this.continuity = continuity; }

    public StorageContinuityGuard.Lease enterRecovery() { return continuity.enter(); }

    public void registerInFlight(String operationId) {
        StorageContinuityGuard.Lease lease = continuity.enter();
        if (leases.putIfAbsent(operationId, lease) != null) {
            lease.close();
            throw new IllegalStateException("Storage operation already admitted");
        }
        inFlightOperations.add(operationId);
    }

    public void releaseInFlight(String operationId) {
        inFlightOperations.remove(operationId);
        StorageContinuityGuard.Lease lease = leases.remove(operationId);
        if (lease != null) lease.close();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public void deferPreserved(Long mutationId, boolean alreadyClaimed) {
        mutationRepository.findById(mutationId).ifPresent(mutation -> {
            mutation.deferPreserved(LocalDateTime.now().plusSeconds(30), alreadyClaimed);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
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
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public void transition(List<Long> mutationIds, StorageMutationState state, String reasonCode) {
        mutationRepository.findAllById(mutationIds)
                .forEach(mutation -> mutation.transition(state, reasonCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
    public void transition(Long mutationId, StorageMutationState state, String reasonCode) {
        mutationRepository.findById(mutationId)
                .ifPresent(mutation -> mutation.transition(state, reasonCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
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
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
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
                PageRequest.of(0, batchSize)).stream()
                // Single-runtime ownership lasts through business transaction completion.
                .filter(mutation -> !inFlightOperations.contains(mutation.getOperationId()))
                .toList();
        claimed.forEach(mutation -> mutation.claim(claimUntil));
        return claimed.stream().map(this::snapshot).toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @com.atstudio.atstudio.service.storage.StorageReferenceMutation
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
