package com.atstudio.atstudio.dto.storage;

import java.time.Instant;
import java.util.List;

public record StorageIntegrityReportResponse(
        Instant checkedAt,
        int checkedReferenceCount,
        int availableReferenceCount,
        int missingReferenceCount,
        boolean issueListTruncated,
        List<StorageIntegrityIssueResponse> issues) {

    public boolean healthy() {
        return missingReferenceCount == 0;
    }
}
