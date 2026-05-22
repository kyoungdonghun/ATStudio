package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentOrderResponse(
        Long id,
        String orderId,
        Long userId,
        String userNickname,
        PaymentPurpose purpose,
        PaymentProviderType provider,
        PaymentOrderStatus status,
        String subscriptionName,
        BillingCycle billingCycle,
        BigDecimal amount,
        String currency,
        String failureCode,
        String failureMessage,
        LocalDateTime expiresAt,
        LocalDateTime confirmedAt,
        LocalDateTime createdAt
) {

    public static AdminPaymentOrderResponse from(PaymentOrder order) {
        return new AdminPaymentOrderResponse(
                order.getId(),
                order.getOrderId(),
                order.getUser().getId(),
                order.getUser().getNickname(),
                order.getPurpose(),
                order.getProvider(),
                order.getStatus(),
                order.getSubscription().getName(),
                order.getBillingCycle(),
                order.getAmount(),
                order.getCurrency(),
                order.getFailureCode(),
                order.getFailureMessage(),
                order.getExpiresAt(),
                order.getConfirmedAt(),
                order.getCreatedAt()
        );
    }
}
