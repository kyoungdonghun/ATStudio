package com.atstudio.atstudio.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageMutationCoordinator {

    private static final int MAX_ATTEMPTS = 8;

    private final StorageService storageService;
    private final StorageMutationJournalService journalService;
    private final StorageCleanupService cleanupService;

    public String store(
            StorageDomain domain,
            StorageRoot root,
            MultipartFile file,
            String directory) {
        return writeAll(domain, root, List.of(StorageWriteRequest.create(file, directory))).get(0);
    }

    public String replace(
            StorageDomain domain,
            StorageRoot root,
            MultipartFile file,
            String directory,
            String oldKey) {
        return writeAll(domain, root, List.of(StorageWriteRequest.replace(file, directory, oldKey))).get(0);
    }

    public List<String> storeAll(
            StorageDomain domain,
            StorageRoot root,
            List<MultipartFile> files,
            String directory) {
        return writeAll(
                domain,
                root,
                files.stream().map(file -> StorageWriteRequest.create(file, directory)).toList()
        );
    }

    public List<String> writeAll(
            StorageDomain domain,
            StorageRoot root,
            List<StorageWriteRequest> writes) {
        requireTransactionSynchronization();
        if (writes == null || writes.isEmpty()) {
            return List.of();
        }

        String operationId = UUID.randomUUID().toString();
        List<StorageMutationDraft> drafts = writes.stream()
                .map(write -> toDraft(operationId, domain, root, write))
                .toList();

        int stagedCount = 0;
        try {
            for (int index = 0; index < writes.size(); index++) {
                StorageWriteRequest write = writes.get(index);
                storageService.stage(root, operationId, drafts.get(index).newKey(), write.file());
                stagedCount++;
            }
        } catch (RuntimeException exception) {
            cleanupInterruptedPreparation(drafts.subList(0, stagedCount));
            throw exception;
        }

        List<Long> mutationIds;
        try {
            mutationIds = journalService.prepare(drafts);
        } catch (RuntimeException exception) {
            drafts.forEach(draft -> cleanupService.cleanupNew(root, operationId, draft.newKey()));
            throw exception;
        }

        try {
            drafts.forEach(draft -> storageService.promote(root, operationId, draft.newKey()));
        } catch (RuntimeException exception) {
            cleanupPreparedMutations(mutationIds, drafts);
            throw exception;
        }

        registerSynchronization(mutationIds, drafts);
        return drafts.stream().map(StorageMutationDraft::newKey).toList();
    }

    public void deleteAfterCommit(
            StorageDomain domain,
            StorageRoot root,
            String oldKey) {
        if (oldKey == null || oldKey.isBlank()) {
            return;
        }
        deleteAfterCommit(domain, root, List.of(oldKey));
    }

    public void deleteAfterCommit(
            StorageDomain domain,
            StorageRoot root,
            List<String> oldKeys) {
        requireTransactionSynchronization();
        List<String> keys = oldKeys == null
                ? List.of()
                : oldKeys.stream().filter(key -> key != null && !key.isBlank()).distinct().toList();
        if (keys.isEmpty()) {
            return;
        }
        String operationId = UUID.randomUUID().toString();
        List<StorageMutationDraft> drafts = keys.stream()
                .map(key -> new StorageMutationDraft(
                        operationId,
                        domain,
                        StorageMutationType.DELETE,
                        root,
                        null,
                        key))
                .toList();
        List<Long> mutationIds = journalService.prepare(drafts);
        registerSynchronization(mutationIds, drafts);
    }

    private StorageMutationDraft toDraft(
            String operationId,
            StorageDomain domain,
            StorageRoot root,
            StorageWriteRequest write) {
        if (write == null || write.file() == null || write.file().isEmpty()) {
            throw new IllegalArgumentException("A non-empty file is required");
        }
        String newKey = storageService.generateKey(write.directory(), write.file().getOriginalFilename());
        StorageMutationType mutationType = write.oldKey() == null
                ? StorageMutationType.CREATE
                : StorageMutationType.REPLACE;
        return new StorageMutationDraft(
                operationId,
                domain,
                mutationType,
                root,
                newKey,
                write.oldKey()
        );
    }

    private void registerSynchronization(
            List<Long> mutationIds,
            List<StorageMutationDraft> drafts) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runCallbackSafely(
                        drafts.get(0).operationId(),
                        () -> StorageMutationCoordinator.this.afterCommit(mutationIds, drafts));
            }

            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    runCallbackSafely(drafts.get(0).operationId(), () -> afterRollback(mutationIds, drafts));
                }
            }
        });
    }

    private void afterCommit(List<Long> mutationIds, List<StorageMutationDraft> drafts) {
        journalService.transition(mutationIds, StorageMutationState.COMMITTED, "TX_COMMITTED");
        for (int index = 0; index < drafts.size(); index++) {
            Long mutationId = mutationIds.get(index);
            StorageMutationDraft draft = drafts.get(index);
            if (draft.oldKey() == null) {
                journalService.transition(mutationId, StorageMutationState.DONE, "CREATE_COMMITTED");
                continue;
            }
            journalService.transition(
                    mutationId,
                    StorageMutationState.AFTER_COMMIT_DELETE,
                    "OLD_DELETE_PENDING");
            completeOldCleanup(mutationId, draft);
        }
    }

    private void afterRollback(List<Long> mutationIds, List<StorageMutationDraft> drafts) {
        for (int index = 0; index < drafts.size(); index++) {
            Long mutationId = mutationIds.get(index);
            StorageMutationDraft draft = drafts.get(index);
            if (draft.newKey() == null) {
                journalService.transition(mutationId, StorageMutationState.DONE, "TX_ROLLED_BACK_NO_DELETE");
                continue;
            }
            journalService.transition(
                    mutationId,
                    StorageMutationState.ROLLBACK_CLEANUP,
                    "NEW_DELETE_PENDING");
            completeNewCleanup(mutationId, draft);
        }
    }

    private void cleanupInterruptedPreparation(List<StorageMutationDraft> drafts) {
        if (drafts.isEmpty()) {
            return;
        }
        try {
            List<Long> mutationIds = journalService.prepare(drafts);
            cleanupPreparedMutations(mutationIds, drafts);
        } catch (RuntimeException journalFailure) {
            drafts.forEach(draft -> cleanupService.cleanupNew(
                    draft.storageRoot(),
                    draft.operationId(),
                    draft.newKey()));
        }
    }

    private void cleanupPreparedMutations(
            List<Long> mutationIds,
            List<StorageMutationDraft> drafts) {
        for (int index = 0; index < drafts.size(); index++) {
            Long mutationId = mutationIds.get(index);
            StorageMutationDraft draft = drafts.get(index);
            journalService.transition(
                    mutationId,
                    StorageMutationState.ROLLBACK_CLEANUP,
                    "PRE_COMMIT_FAILURE");
            completeNewCleanup(mutationId, draft);
        }
    }

    private void completeNewCleanup(Long mutationId, StorageMutationDraft draft) {
        StorageCleanupService.CleanupOutcome outcome = cleanupService.cleanupNew(
                draft.storageRoot(),
                draft.operationId(),
                draft.newKey());
        if (outcome == StorageCleanupService.CleanupOutcome.DONE) {
            journalService.transition(mutationId, StorageMutationState.DONE, "NEW_DELETE_DONE");
        } else {
            recordRetry(mutationId, "NEW_DELETE_FAILED");
        }
    }

    private void completeOldCleanup(Long mutationId, StorageMutationDraft draft) {
        StorageCleanupService.CleanupOutcome outcome = cleanupService.cleanupOld(
                draft.domain(),
                draft.storageRoot(),
                draft.oldKey());
        switch (outcome) {
            case DONE -> journalService.transition(mutationId, StorageMutationState.DONE, "OLD_DELETE_DONE");
            case SHARED_REFERENCE -> journalService.transition(
                    mutationId,
                    StorageMutationState.DONE,
                    "SHARED_REFERENCE_RETAINED");
            case FAILED -> recordRetry(mutationId, "OLD_DELETE_FAILED");
        }
    }

    private void recordRetry(Long mutationId, String reasonCode) {
        journalService.recordCleanupFailure(
                mutationId,
                reasonCode,
                LocalDateTime.now().plusSeconds(30),
                MAX_ATTEMPTS);
    }

    private void requireTransactionSynchronization() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Storage mutation requires an active transaction");
        }
    }

    private void runCallbackSafely(String operationId, Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException exception) {
            log.warn(
                    "Storage mutation callback deferred to recovery. operationId={}, failureType={}",
                    operationId,
                    exception.getClass().getSimpleName());
        }
    }
}
