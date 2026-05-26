package com.atstudio.atstudio.dto.payment;

import java.util.List;
import java.util.Map;

public record AdminPaymentSettlementImportResponse(
        String importBatchKey,
        int totalRows,
        int importedRows,
        int skippedDuplicateRows,
        int failedRows,
        Map<String, Integer> statusCounts,
        List<AdminPaymentSettlementImportErrorResponse> errors) {
}
