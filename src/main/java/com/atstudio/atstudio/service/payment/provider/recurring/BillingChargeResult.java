package com.atstudio.atstudio.service.payment.provider.recurring;

public record BillingChargeResult(
        boolean success,
        String transactionId,
        String payMethod,
        String maskedMethod,
        String providerPayload,
        String failureCode,
        String failureMessage
) {
    public static BillingChargeResult success(
            String transactionId,
            String payMethod,
            String maskedMethod,
            String providerPayload) {
        return new BillingChargeResult(
                true,
                transactionId,
                payMethod,
                maskedMethod,
                providerPayload,
                null,
                null);
    }

    public static BillingChargeResult failure(String code, String message) {
        return new BillingChargeResult(false, null, null, null, null, code, message);
    }
}
