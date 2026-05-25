package com.atstudio.atstudio.service.payment.provider.refund;

public record PaymentRefundProviderResult(
        boolean success,
        boolean pendingConfirmation,
        String providerRefundTransactionId,
        String providerPayload,
        String failureCode,
        String failureMessage
) {
    public static PaymentRefundProviderResult success(
            String providerRefundTransactionId,
            String providerPayload) {
        return new PaymentRefundProviderResult(
                true,
                false,
                providerRefundTransactionId,
                providerPayload,
                null,
                null);
    }

    public static PaymentRefundProviderResult failure(
            String failureCode,
            String failureMessage,
            String providerPayload) {
        return new PaymentRefundProviderResult(
                false,
                false,
                null,
                providerPayload,
                failureCode,
                failureMessage);
    }

    public static PaymentRefundProviderResult pending(
            String failureCode,
            String failureMessage,
            String providerPayload) {
        return new PaymentRefundProviderResult(
                false,
                true,
                null,
                providerPayload,
                failureCode,
                failureMessage);
    }
}
