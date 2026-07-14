package com.atstudio.atstudio.service.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageCleanupService {

    private final StorageService storageService;
    private final StorageReferenceChecker referenceChecker;

    public CleanupOutcome cleanupNew(StorageRoot root, String operationId, String newKey) {
        if (newKey == null || newKey.isBlank()) {
            return CleanupOutcome.DONE;
        }
        try {
            StorageDeleteResult staged = storageService.deleteStaged(root, operationId, newKey);
            StorageDeleteResult promoted = storageService.delete(root, newKey);
            return staged.isSuccess() && promoted.isSuccess()
                    ? CleanupOutcome.DONE
                    : CleanupOutcome.FAILED;
        } catch (RuntimeException exception) {
            return CleanupOutcome.FAILED;
        }
    }

    public CleanupOutcome cleanupOld(StorageDomain domain, StorageRoot root, String oldKey) {
        if (oldKey == null || oldKey.isBlank()) {
            return CleanupOutcome.DONE;
        }
        try {
            if (referenceChecker.isReferenced(domain, oldKey)) {
                return CleanupOutcome.SHARED_REFERENCE;
            }
            return storageService.delete(root, oldKey).isSuccess()
                    ? CleanupOutcome.DONE
                    : CleanupOutcome.FAILED;
        } catch (RuntimeException exception) {
            return CleanupOutcome.FAILED;
        }
    }

    public enum CleanupOutcome {
        DONE,
        SHARED_REFERENCE,
        FAILED
    }
}
