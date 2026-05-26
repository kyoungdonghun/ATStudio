package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentEntitlementCorrection;
import com.atstudio.atstudio.entity.PaymentReceipt;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentOperationAuditLogService {

    private static final int MAX_NOTE_LENGTH = 500;

    private final PaymentOperationAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordReconciliationIncidentStatusUpdate(
            CustomUserDetails actorDetails,
            PaymentReconciliationIncident incident,
            PaymentReconciliationIncidentStatus beforeStatus,
            PaymentReconciliationIncidentStatus afterStatus,
            String note) {
        User actor = resolveActor(actorDetails);
        auditLogRepository.save(PaymentOperationAuditLog.builder()
                .action(PaymentOperationAuditAction.RECONCILIATION_INCIDENT_STATUS_UPDATE)
                .targetType(PaymentOperationAuditTargetType.RECONCILIATION_INCIDENT)
                .targetId(incident.getId())
                .actorUser(actor)
                .targetUser(incident.getUser())
                .paymentOrder(incident.getPaymentOrder())
                .reconciliationIncident(incident)
                .provider(incident.getProvider())
                .orderId(incident.getOrderId())
                .providerTransactionId(incident.getProviderTransactionId())
                .beforeStatus(statusName(beforeStatus))
                .afterStatus(statusName(afterStatus))
                .reasonCode(incident.getIssueType().name())
                .note(truncate(note, MAX_NOTE_LENGTH))
                .build());
    }

    @Transactional
    public void recordReceiptEvidenceCreated(PaymentReceipt receipt) {
        auditLogRepository.save(PaymentOperationAuditLog.builder()
                .action(PaymentOperationAuditAction.RECEIPT_EVIDENCE_CREATED)
                .targetType(PaymentOperationAuditTargetType.PAYMENT_RECEIPT)
                .targetId(receipt.getId())
                .targetUser(receipt.getUser())
                .paymentOrder(receipt.getPaymentOrder())
                .subscriptionPayment(receipt.getSubscriptionPayment())
                .provider(receipt.getProvider())
                .orderId(receipt.getPaymentOrder().getOrderId())
                .providerTransactionId(receipt.getProviderPaymentKey())
                .afterStatus(receipt.getStatus().name())
                .reasonCode(receipt.getType().name())
                .note("Payment receipt evidence stored.")
                .build());
    }

    @Transactional
    public void recordPaymentRefundEvent(
            CustomUserDetails actorDetails,
            PaymentRefund refund,
            PaymentOperationAuditAction action,
            PaymentRefundStatus beforeStatus,
            PaymentRefundStatus afterStatus,
            String note) {
        User actor = resolveActor(actorDetails);
        auditLogRepository.save(PaymentOperationAuditLog.builder()
                .action(action)
                .targetType(PaymentOperationAuditTargetType.PAYMENT_REFUND)
                .targetId(refund.getId())
                .actorUser(actor)
                .targetUser(refund.getUser())
                .paymentOrder(refund.getPaymentOrder())
                .subscriptionPayment(refund.getSubscriptionPayment())
                .provider(refund.getProvider())
                .orderId(refund.getPaymentOrder().getOrderId())
                .providerTransactionId(refund.getProviderRefundTransactionId() == null
                        ? refund.getProviderPaymentKey()
                        : refund.getProviderRefundTransactionId())
                .beforeStatus(statusName(beforeStatus))
                .afterStatus(statusName(afterStatus))
                .reasonCode(refund.getReasonCode().name())
                .note(truncate(note, MAX_NOTE_LENGTH))
                .build());
    }

    @Transactional
    public void recordPaymentEntitlementCorrectionEvent(
            CustomUserDetails actorDetails,
            PaymentEntitlementCorrection correction,
            PaymentOperationAuditAction action,
            PaymentEntitlementCorrectionStatus beforeStatus,
            PaymentEntitlementCorrectionStatus afterStatus,
            String note) {
        User actor = resolveActor(actorDetails);
        auditLogRepository.save(PaymentOperationAuditLog.builder()
                .action(action)
                .targetType(PaymentOperationAuditTargetType.PAYMENT_ENTITLEMENT_CORRECTION)
                .targetId(correction.getId())
                .actorUser(actor)
                .targetUser(correction.getUser())
                .paymentOrder(correction.getPaymentOrder())
                .subscriptionPayment(correction.getSubscriptionPayment())
                .provider(correction.getProvider())
                .orderId(correction.getPaymentOrder().getOrderId())
                .providerTransactionId(correction.getPaymentRefund().getProviderRefundTransactionId() == null
                        ? correction.getPaymentRefund().getProviderPaymentKey()
                        : correction.getPaymentRefund().getProviderRefundTransactionId())
                .beforeStatus(statusName(beforeStatus))
                .afterStatus(statusName(afterStatus))
                .reasonCode(correction.getAction().name())
                .note(truncate(note, MAX_NOTE_LENGTH))
                .build());
    }

    @Transactional
    public void recordPaymentSettlementEvent(
            CustomUserDetails actorDetails,
            PaymentSettlement settlement,
            PaymentOperationAuditAction action,
            PaymentSettlementStatus beforeStatus,
            PaymentSettlementStatus afterStatus,
            String note) {
        User actor = resolveActor(actorDetails);
        auditLogRepository.save(PaymentOperationAuditLog.builder()
                .action(action)
                .targetType(PaymentOperationAuditTargetType.PAYMENT_SETTLEMENT)
                .targetId(settlement.getId())
                .actorUser(actor)
                .targetUser(settlement.getUser())
                .paymentOrder(settlement.getPaymentOrder())
                .subscriptionPayment(settlement.getSubscriptionPayment())
                .provider(settlement.getProvider())
                .orderId(settlement.getOrderId())
                .providerTransactionId(settlement.getProviderPaymentKey())
                .beforeStatus(statusName(beforeStatus))
                .afterStatus(statusName(afterStatus))
                .reasonCode(settlement.getSource().name())
                .note(truncate(note, MAX_NOTE_LENGTH))
                .build());
    }

    private User resolveActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            return null;
        }
        return userRepository.findById(actorDetails.getId()).orElse(null);
    }

    private String statusName(PaymentReconciliationIncidentStatus status) {
        return status == null ? null : status.name();
    }

    private String statusName(PaymentRefundStatus status) {
        return status == null ? null : status.name();
    }

    private String statusName(PaymentEntitlementCorrectionStatus status) {
        return status == null ? null : status.name();
    }

    private String statusName(PaymentSettlementStatus status) {
        return status == null ? null : status.name();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
