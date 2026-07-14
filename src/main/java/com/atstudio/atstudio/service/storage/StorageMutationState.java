package com.atstudio.atstudio.service.storage;

public enum StorageMutationState {
    PREPARED,
    COMMITTED,
    ROLLBACK_CLEANUP,
    AFTER_COMMIT_DELETE,
    RETRY,
    DONE,
    FAILED
}
