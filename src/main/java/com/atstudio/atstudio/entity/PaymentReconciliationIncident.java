package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_reconciliation_incidents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_reconciliation_incidents_dedupe",
                        columnNames = "dedupe_key"
                )
        },
        indexes = {
                @Index(name = "idx_payment_reconciliation_incidents_status_last", columnList = "status,last_detected_at"),
                @Index(name = "idx_payment_reconciliation_incidents_order", columnList = "order_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentReconciliationIncident extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dedupe_key", nullable = false, unique = true, length = 255)
    private String dedupeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false, length = 60)
    private PaymentReconciliationIssueType issueType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentReconciliationIncidentStatus status = PaymentReconciliationIncidentStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentReconciliationIncidentSeverity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id")
    private PaymentOrder paymentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_agreement_id")
    private BillingAgreement billingAgreement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_id", length = 64)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentProviderType provider;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentPurpose purpose;

    @Column(name = "local_status", length = 50)
    private String localStatus;

    @Column(name = "provider_status", length = 50)
    private String providerStatus;

    @Column(name = "local_amount", precision = 10, scale = 2)
    private BigDecimal localAmount;

    @Column(name = "provider_amount", precision = 10, scale = 2)
    private BigDecimal providerAmount;

    @Column(name = "provider_transaction_id", length = 200)
    private String providerTransactionId;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Builder.Default
    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount = 1;

    @Column(name = "first_detected_at", nullable = false)
    private LocalDateTime firstDetectedAt;

    @Column(name = "last_detected_at", nullable = false)
    private LocalDateTime lastDetectedAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    public void recordDetection(
            PaymentOrder paymentOrder,
            BillingAgreement billingAgreement,
            User user,
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
            PaymentReconciliationIncidentSeverity severity,
            LocalDateTime detectedAt) {
        this.paymentOrder = paymentOrder;
        this.billingAgreement = billingAgreement;
        this.user = user;
        this.orderId = orderId;
        this.provider = provider;
        this.purpose = purpose;
        this.localStatus = localStatus;
        this.providerStatus = providerStatus;
        this.localAmount = localAmount;
        this.providerAmount = providerAmount;
        this.providerTransactionId = providerTransactionId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.severity = severity;
        this.occurrenceCount += 1;
        this.lastDetectedAt = detectedAt;

        if (status == PaymentReconciliationIncidentStatus.RESOLVED) {
            this.status = PaymentReconciliationIncidentStatus.OPEN;
            this.notifiedAt = null;
            this.resolvedAt = null;
            this.resolutionNote = null;
        }
    }

    public void changeStatus(
            PaymentReconciliationIncidentStatus status,
            String note,
            LocalDateTime changedAt) {
        this.status = status;
        this.resolutionNote = note;
        if (status == PaymentReconciliationIncidentStatus.RESOLVED
                || status == PaymentReconciliationIncidentStatus.IGNORED) {
            this.resolvedAt = changedAt;
        } else {
            this.resolvedAt = null;
        }
        if (status == PaymentReconciliationIncidentStatus.OPEN) {
            this.notifiedAt = null;
        }
    }

    public boolean shouldNotify() {
        return status == PaymentReconciliationIncidentStatus.OPEN && notifiedAt == null;
    }

    public void markNotified(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
}
