package com.atstudio.atstudio.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BillingAgreementConfirmRequest(
        @NotBlank String orderId,
        @NotBlank String authKey,
        @NotBlank String customerKey,
        @NotNull BigDecimal amount
) {
}
