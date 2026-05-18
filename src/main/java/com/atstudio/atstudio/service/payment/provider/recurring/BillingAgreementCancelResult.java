package com.atstudio.atstudio.service.payment.provider.recurring;

public record BillingAgreementCancelResult(
        boolean success,
        String providerPayload,
        String failureCode,
        String failureMessage
) {
    public static BillingAgreementCancelResult success(String providerPayload) {
        return new BillingAgreementCancelResult(true, providerPayload, null, null);
    }

    public static BillingAgreementCancelResult failure(String code, String message) {
        return new BillingAgreementCancelResult(false, null, code, message);
    }
}
