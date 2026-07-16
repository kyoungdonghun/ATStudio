package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminSubscriptionPaymentResponse(
        Long id,
        Long userId,
        String userNickname,
        String orderId,
        String subscriptionName,
        BillingCycle billingCycle,
        PaymentProviderType provider,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        String providerReference,
        LocalDateTime createdAt
) {

    public static AdminSubscriptionPaymentResponse from(SubscriptionPayment payment) {
        return new AdminSubscriptionPaymentResponse(
                payment.getId(),
                payment.getUser().getId(),
                payment.getUser().getNickname(),
                payment.getPaymentOrder() == null ? null : payment.getPaymentOrder().getOrderId(),
                payment.getSubscription().getName(),
                payment.getBillingCycle(),
                payment.getProvider(),
                payment.getAmount(),
                payment.getPaymentStatus(),
                ProviderSupportReference.from(payment.getPgTransactionId()),
                payment.getCreatedAt()
        );
    }
}
