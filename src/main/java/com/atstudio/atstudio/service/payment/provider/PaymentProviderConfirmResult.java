package com.atstudio.atstudio.service.payment.provider;

public record PaymentProviderConfirmResult(
        boolean success,
        String transactionId,
        String providerPayload,
        String failureCode,
        String failureMessage
) {
    public static PaymentProviderConfirmResult success(String transactionId, String providerPayload) {
        return new PaymentProviderConfirmResult(true, transactionId, providerPayload, null, null);
    }

    public static PaymentProviderConfirmResult failure(String code, String message) {
        return new PaymentProviderConfirmResult(false, null, null, code, message);
    }
}
