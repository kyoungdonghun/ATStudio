package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.math.BigDecimal;

public record AdminPaymentRefundPreviewResponse(
        Long subscriptionPaymentId,
        Long paymentOrderId,
        String orderId,
        Long userId,
        String userNickname,
        PaymentProviderType provider,
        BigDecimal originalAmount,
        BigDecimal alreadyRefundedOrReservedAmount,
        BigDecimal refundableAmount,
        String providerReference,
        boolean refundable,
        String reason
) {
    public static AdminPaymentRefundPreviewResponse of(
            SubscriptionPayment payment,
            BigDecimal alreadyRefundedOrReservedAmount,
            BigDecimal refundableAmount,
            boolean refundable,
            String reason) {
        return new AdminPaymentRefundPreviewResponse(
                payment.getId(),
                payment.getPaymentOrder() == null ? null : payment.getPaymentOrder().getId(),
                payment.getPaymentOrder() == null ? null : payment.getPaymentOrder().getOrderId(),
                payment.getUser().getId(),
                payment.getUser().getNickname(),
                payment.getProvider(),
                payment.getAmount(),
                alreadyRefundedOrReservedAmount,
                refundableAmount,
                ProviderSupportReference.from(payment.getPgTransactionId()),
                refundable,
                reason);
    }
}
