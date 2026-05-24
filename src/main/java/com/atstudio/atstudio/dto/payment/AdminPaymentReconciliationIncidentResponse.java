package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentReconciliationIncidentResponse(
        Long id,
        String dedupeKey,
        PaymentReconciliationIssueType issueType,
        PaymentReconciliationIncidentStatus status,
        PaymentReconciliationIncidentSeverity severity,
        Long paymentOrderId,
        Long billingAgreementId,
        Long userId,
        String userNickname,
        String orderId,
        PaymentProviderType provider,
        PaymentPurpose purpose,
        String localStatus,
        String providerStatus,
        BigDecimal localAmount,
        BigDecimal providerAmount,
        String providerTransactionId,
        String failureCode,
        String failureMessage,
        int occurrenceCount,
        LocalDateTime firstDetectedAt,
        LocalDateTime lastDetectedAt,
        LocalDateTime notifiedAt,
        LocalDateTime resolvedAt,
        String resolutionNote,
        LocalDateTime createdAt
) {

    public static AdminPaymentReconciliationIncidentResponse from(PaymentReconciliationIncident incident) {
        return new AdminPaymentReconciliationIncidentResponse(
                incident.getId(),
                incident.getDedupeKey(),
                incident.getIssueType(),
                incident.getStatus(),
                incident.getSeverity(),
                incident.getPaymentOrder() == null ? null : incident.getPaymentOrder().getId(),
                incident.getBillingAgreement() == null ? null : incident.getBillingAgreement().getId(),
                incident.getUser() == null ? null : incident.getUser().getId(),
                incident.getUser() == null ? null : incident.getUser().getNickname(),
                incident.getOrderId(),
                incident.getProvider(),
                incident.getPurpose(),
                incident.getLocalStatus(),
                incident.getProviderStatus(),
                incident.getLocalAmount(),
                incident.getProviderAmount(),
                incident.getProviderTransactionId(),
                incident.getFailureCode(),
                incident.getFailureMessage(),
                incident.getOccurrenceCount(),
                incident.getFirstDetectedAt(),
                incident.getLastDetectedAt(),
                incident.getNotifiedAt(),
                incident.getResolvedAt(),
                incident.getResolutionNote(),
                incident.getCreatedAt()
        );
    }
}
