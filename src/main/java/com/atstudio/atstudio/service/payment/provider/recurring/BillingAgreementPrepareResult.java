package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.util.Map;

public record BillingAgreementPrepareResult(
        PaymentProviderType provider,
        String checkoutType,
        String providerPayload,
        Map<String, String> checkoutMetadata
) {
    public BillingAgreementPrepareResult {
        checkoutMetadata = checkoutMetadata == null ? Map.of() : Map.copyOf(checkoutMetadata);
    }
}
