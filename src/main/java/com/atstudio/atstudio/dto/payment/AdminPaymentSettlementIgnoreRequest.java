package com.atstudio.atstudio.dto.payment;

import jakarta.validation.constraints.Size;

public record AdminPaymentSettlementIgnoreRequest(
        @Size(max = 500)
        String note) {
}
