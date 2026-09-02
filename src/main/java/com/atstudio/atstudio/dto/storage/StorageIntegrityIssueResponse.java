package com.atstudio.atstudio.dto.storage;

import com.atstudio.atstudio.service.storage.StorageRoot;

/**
 * An opaque operational reference. Storage keys and original filenames are deliberately omitted.
 */
public record StorageIntegrityIssueResponse(
        String domain,
        StorageRoot storageRoot,
        Long recordId,
        String referenceType) {
}
