package com.atstudio.atstudio.dto.payment;

public record BillingAgreementCheckoutResponse(
        String type,
        String clientKey,
        String customerKey,
        String successUrl,
        String failUrl,
        String method
) {
}
