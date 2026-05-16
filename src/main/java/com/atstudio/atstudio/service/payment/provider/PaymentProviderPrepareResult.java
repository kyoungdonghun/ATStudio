package com.atstudio.atstudio.service.payment.provider;

public record PaymentProviderPrepareResult(
        String checkoutType,
        String confirmToken,
        String providerPayload
) {}
