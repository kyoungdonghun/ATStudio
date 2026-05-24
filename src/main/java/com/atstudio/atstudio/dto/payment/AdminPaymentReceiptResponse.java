package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentReceiptStatus;
import com.atstudio.atstudio.entity.enums.PaymentReceiptType;

import java.time.LocalDateTime;

public record AdminPaymentReceiptResponse(
        Long id,
        Long userId,
        String userNickname,
        Long paymentOrderId,
        String orderId,
        Long subscriptionPaymentId,
        PaymentProviderType provider,
        PaymentReceiptType type,
        PaymentReceiptStatus status,
        String providerPaymentKey,
        String receiptKey,
        String receiptUrl,
        LocalDateTime issuedAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {

    public static AdminPaymentReceiptResponse from(PaymentReceipt receipt) {
        return new AdminPaymentReceiptResponse(
                receipt.getId(),
                receipt.getUser().getId(),
                receipt.getUser().getNickname(),
                receipt.getPaymentOrder().getId(),
                receipt.getPaymentOrder().getOrderId(),
                receipt.getSubscriptionPayment().getId(),
                receipt.getProvider(),
                receipt.getType(),
                receipt.getStatus(),
                receipt.getProviderPaymentKey(),
                receipt.getReceiptKey(),
                receipt.getReceiptUrl(),
                receipt.getIssuedAt(),
                receipt.getCancelledAt(),
                receipt.getCreatedAt()
        );
    }
}
