package com.atstudio.atstudio.service.storage;

public record StorageMutationSnapshot(
        Long id,
        String operationId,
        StorageDomain domain,
        StorageMutationType mutationType,
        StorageRoot storageRoot,
        String newKey,
        String oldKey,
        StorageMutationState state,
        int attemptCount
) {
}
