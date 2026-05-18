package com.atstudio.atstudio.service.payment.provider.recurring;

public record BillingAgreementConfirmResult(
        boolean success,
        String billingKey,
        String payMethod,
        String maskedMethod,
        String providerPayload,
        String failureCode,
        String failureMessage
) {
    public static BillingAgreementConfirmResult success(
            String billingKey,
            String payMethod,
            String maskedMethod,
            String providerPayload) {
        return new BillingAgreementConfirmResult(
                true,
                billingKey,
                payMethod,
                maskedMethod,
                providerPayload,
                null,
                null);
    }

    public static BillingAgreementConfirmResult failure(String code, String message) {
        return new BillingAgreementConfirmResult(false, null, null, null, null, code, message);
    }
}
