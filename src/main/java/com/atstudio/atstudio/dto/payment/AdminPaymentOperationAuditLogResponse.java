package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.time.LocalDateTime;

public record AdminPaymentOperationAuditLogResponse(
        Long id,
        PaymentOperationAuditAction action,
        PaymentOperationAuditTargetType targetType,
        Long targetId,
        Long actorUserId,
        String actorEmail,
        Long targetUserId,
        String targetUserNickname,
        Long paymentOrderId,
        String orderId,
        Long subscriptionPaymentId,
        Long reconciliationIncidentId,
        PaymentProviderType provider,
        String providerReference,
        String beforeStatus,
        String afterStatus,
        String reasonCode,
        String note,
        LocalDateTime createdAt
) {

    public static AdminPaymentOperationAuditLogResponse from(PaymentOperationAuditLog log) {
        return new AdminPaymentOperationAuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getActorUser() == null ? null : log.getActorUser().getId(),
                log.getActorUser() == null ? null : log.getActorUser().getEmail(),
                log.getTargetUser() == null ? null : log.getTargetUser().getId(),
                log.getTargetUser() == null ? null : log.getTargetUser().getNickname(),
                log.getPaymentOrder() == null ? null : log.getPaymentOrder().getId(),
                log.getOrderId(),
                log.getSubscriptionPayment() == null ? null : log.getSubscriptionPayment().getId(),
                log.getReconciliationIncident() == null ? null : log.getReconciliationIncident().getId(),
                log.getProvider(),
                ProviderSupportReference.from(log.getProviderTransactionId()),
                log.getBeforeStatus(),
                log.getAfterStatus(),
                log.getReasonCode(),
                ProviderSupportReference.sanitizeFreeText(log.getNote()),
                log.getCreatedAt()
        );
    }
}
