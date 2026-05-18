package com.atstudio.atstudio.service.payment.provider.recurring;

public record BillingAgreementConfirmCommand(
        String authKey,
        String providerCustomerKey
) {
}
