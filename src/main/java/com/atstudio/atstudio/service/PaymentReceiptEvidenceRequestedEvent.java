package com.atstudio.atstudio.service;

public record PaymentReceiptEvidenceRequestedEvent(
        Long paymentOrderId,
        Long subscriptionPaymentId,
        String providerPayload
) {
}
