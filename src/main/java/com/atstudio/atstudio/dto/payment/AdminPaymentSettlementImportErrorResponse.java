package com.atstudio.atstudio.dto.payment;

public record AdminPaymentSettlementImportErrorResponse(
        int rowNumber,
        String message) {
}
