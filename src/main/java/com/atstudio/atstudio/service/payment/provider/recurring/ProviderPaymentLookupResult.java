package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.math.BigDecimal;

public record ProviderPaymentLookupResult(
        PaymentProviderType provider,
        boolean found,
        String orderId,
        String transactionId,
        String status,
        BigDecimal totalAmount,
        String providerPayload,
        String failureCode,
        String failureMessage
) {

    public static ProviderPaymentLookupResult found(
            PaymentProviderType provider,
            String orderId,
            String transactionId,
            String status,
            BigDecimal totalAmount,
            String providerPayload) {
        return new ProviderPaymentLookupResult(
                provider,
                true,
                orderId,
                transactionId,
                status,
                totalAmount,
                providerPayload,
                null,
                null);
    }

    public static ProviderPaymentLookupResult notFound(
            PaymentProviderType provider,
            String orderId,
            String failureCode,
            String failureMessage) {
        return new ProviderPaymentLookupResult(
                provider,
                false,
                orderId,
                null,
                null,
                null,
                null,
                failureCode,
                failureMessage);
    }

    public static ProviderPaymentLookupResult failure(
            PaymentProviderType provider,
            String orderId,
            String failureCode,
            String failureMessage) {
        return new ProviderPaymentLookupResult(
                provider,
                false,
                orderId,
                null,
                null,
                null,
                null,
                failureCode,
                failureMessage);
    }

    public boolean providerDone() {
        return found && "DONE".equals(status);
    }

    public boolean lookupFailure() {
        return !found && !"NOT_FOUND_PAYMENT".equals(failureCode);
    }
}

