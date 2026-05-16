package com.atstudio.atstudio.service.payment.provider;

import java.util.Map;

public record PaymentProviderPrepareResult(
        String checkoutType,
        String confirmToken,
        String providerPayload,
        Map<String, String> checkoutMetadata
) {
    public PaymentProviderPrepareResult {
        checkoutMetadata = checkoutMetadata == null ? Map.of() : Map.copyOf(checkoutMetadata);
    }

    public PaymentProviderPrepareResult(String checkoutType, String confirmToken, String providerPayload) {
        this(checkoutType, confirmToken, providerPayload, Map.of());
    }
}
