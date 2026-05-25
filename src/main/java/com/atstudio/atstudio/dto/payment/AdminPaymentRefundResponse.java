package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentRefundResponse(
        Long id,
        Long subscriptionPaymentId,
        Long paymentOrderId,
        String orderId,
        Long userId,
        String userNickname,
        PaymentProviderType provider,
        PaymentRefundStatus status,
        BigDecimal amount,
        String currency,
        PaymentRefundReasonCode reasonCode,
        String reasonNote,
        String idempotencyKey,
        String providerPaymentKey,
        String providerRefundTransactionId,
        String failureCode,
        String failureMessage,
        Long requestedById,
        String requestedByEmail,
        Long approvedById,
        String approvedByEmail,
        Long executedById,
        String executedByEmail,
        LocalDateTime approvedAt,
        LocalDateTime executedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminPaymentRefundResponse from(PaymentRefund refund) {
        return new AdminPaymentRefundResponse(
                refund.getId(),
                refund.getSubscriptionPayment().getId(),
                refund.getPaymentOrder().getId(),
                refund.getPaymentOrder().getOrderId(),
                refund.getUser().getId(),
                refund.getUser().getNickname(),
                refund.getProvider(),
                refund.getStatus(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getReasonCode(),
                refund.getReasonNote(),
                refund.getIdempotencyKey(),
                refund.getProviderPaymentKey(),
                refund.getProviderRefundTransactionId(),
                refund.getFailureCode(),
                refund.getFailureMessage(),
                refund.getRequestedBy() == null ? null : refund.getRequestedBy().getId(),
                refund.getRequestedBy() == null ? null : refund.getRequestedBy().getEmail(),
                refund.getApprovedBy() == null ? null : refund.getApprovedBy().getId(),
                refund.getApprovedBy() == null ? null : refund.getApprovedBy().getEmail(),
                refund.getExecutedBy() == null ? null : refund.getExecutedBy().getId(),
                refund.getExecutedBy() == null ? null : refund.getExecutedBy().getEmail(),
                refund.getApprovedAt(),
                refund.getExecutedAt(),
                refund.getCreatedAt(),
                refund.getUpdatedAt());
    }
}
