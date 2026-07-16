package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminPaymentSettlementResponse(
        Long id,
        PaymentSettlementSource source,
        PaymentProviderType provider,
        PaymentSettlementStatus status,
        String orderId,
        String providerReference,
        String providerSettlementReference,
        Long paymentOrderId,
        Long subscriptionPaymentId,
        Long userId,
        String userNickname,
        BigDecimal grossAmount,
        BigDecimal refundAmount,
        BigDecimal feeAmount,
        BigDecimal vatAmount,
        BigDecimal netSettlementAmount,
        String currency,
        LocalDate settlementBaseDate,
        LocalDate settlementPayoutDate,
        String providerStatus,
        String mismatchReason,
        String sourceFileName,
        Integer sourceRowNumber,
        String operatorNote,
        Long ignoredBy,
        LocalDateTime ignoredAt,
        LocalDateTime reconciledAt,
        LocalDateTime createdAt) {

    public static AdminPaymentSettlementResponse from(PaymentSettlement settlement) {
        return new AdminPaymentSettlementResponse(
                settlement.getId(),
                settlement.getSource(),
                settlement.getProvider(),
                settlement.getStatus(),
                settlement.getOrderId(),
                ProviderSupportReference.from(settlement.getProviderPaymentKey()),
                ProviderSupportReference.from(settlement.getProviderSettlementId()),
                settlement.getPaymentOrder() == null ? null : settlement.getPaymentOrder().getId(),
                settlement.getSubscriptionPayment() == null ? null : settlement.getSubscriptionPayment().getId(),
                settlement.getUser() == null ? null : settlement.getUser().getId(),
                settlement.getUser() == null ? null : settlement.getUser().getNickname(),
                settlement.getGrossAmount(),
                settlement.getRefundAmount(),
                settlement.getFeeAmount(),
                settlement.getVatAmount(),
                settlement.getNetSettlementAmount(),
                settlement.getCurrency(),
                settlement.getSettlementBaseDate(),
                settlement.getSettlementPayoutDate(),
                settlement.getProviderStatus(),
                settlement.getMismatchReason(),
                settlement.getSourceFileName(),
                settlement.getSourceRowNumber(),
                settlement.getOperatorNote(),
                settlement.getIgnoredBy() == null ? null : settlement.getIgnoredBy().getId(),
                settlement.getIgnoredAt(),
                settlement.getReconciledAt(),
                settlement.getCreatedAt());
    }
}
