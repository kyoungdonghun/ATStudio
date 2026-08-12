package com.atstudio.atstudio.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPaymentSettlementIgnoreRequest(
        @NotBlank
        @Size(max = 500)
        String note) {

    public AdminPaymentSettlementIgnoreRequest {
        note = note == null ? null : note.trim();
    }
}
