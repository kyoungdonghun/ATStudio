package com.atstudio.atstudio.service.storage;

public record StorageMutationDraft(
        String operationId,
        StorageDomain domain,
        StorageMutationType mutationType,
        StorageRoot storageRoot,
        String newKey,
        String oldKey
) {
}
