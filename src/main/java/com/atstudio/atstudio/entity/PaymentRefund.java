package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "payment_refunds",
        indexes = {
                @Index(name = "idx_payment_refunds_status_created", columnList = "status,created_at"),
                @Index(name = "idx_payment_refunds_payment", columnList = "subscription_payment_id"),
                @Index(name = "idx_payment_refunds_user_created", columnList = "user_id,created_at"),
                @Index(
                        name = "idx_payment_refunds_status_processing",
                        columnList = "status,processing_started_at,id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentRefund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_payment_id", nullable = false)
    private SubscriptionPayment subscriptionPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    private PaymentOrder paymentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentRefundStatus status = PaymentRefundStatus.REQUESTED;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "KRW";

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 100)
    private PaymentRefundReasonCode reasonCode;

    @Column(name = "reason_note", length = 500)
    private String reasonNote;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "provider_payment_key", nullable = false, length = 200)
    private String providerPaymentKey;

    @Column(name = "provider_refund_transaction_id", length = 200)
    private String providerRefundTransactionId;

    @Column(name = "provider_payload", columnDefinition = "TEXT")
    private String providerPayload;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    private User executedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    public PaymentOperationAuditAction approve(User actor) {
        this.status = PaymentRefundStatus.APPROVED;
        this.approvedBy = actor;
        this.approvedAt = LocalDateTime.now();
        return PaymentOperationAuditAction.PAYMENT_REFUND_APPROVED;
    }

    public PaymentOperationAuditAction markProcessing(User actor) {
        return markProcessing(actor, LocalDateTime.now());
    }

    public PaymentOperationAuditAction markProcessing(User actor, LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Refund processing start time is required.");
        }
        if (status != PaymentRefundStatus.APPROVED
                && status != PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION) {
            throw new IllegalStateException("Refund cannot be claimed from " + status + ".");
        }

        this.status = PaymentRefundStatus.PROCESSING;
        this.executedBy = actor;
        this.processingStartedAt = toSecondPrecision(now);
        return PaymentOperationAuditAction.PAYMENT_REFUND_PROCESSING;
    }

    public PaymentOperationAuditAction reclaimProcessing(
            User actor,
            LocalDateTime expectedLeaseStartedAt,
            LocalDateTime now) {
        requireActiveLease(expectedLeaseStartedAt);
        if (now == null) {
            throw new IllegalArgumentException("Refund processing start time is required.");
        }

        this.executedBy = actor;
        this.processingStartedAt = toSecondPrecision(now);
        return PaymentOperationAuditAction.PAYMENT_REFUND_PROCESSING;
    }

    public PaymentOperationAuditAction markSucceeded(String providerRefundTransactionId, String providerPayload) {
        return markSucceeded(providerRefundTransactionId, providerPayload, processingStartedAt);
    }

    public PaymentOperationAuditAction markSucceeded(
            String providerRefundTransactionId,
            String providerPayload,
            LocalDateTime leaseStartedAt) {
        requireActiveLease(leaseStartedAt);
        if (providerRefundTransactionId == null || providerRefundTransactionId.isBlank()) {
            throw new IllegalArgumentException("Provider refund transaction ID is required.");
        }

        this.status = PaymentRefundStatus.SUCCEEDED;
        this.providerRefundTransactionId = providerRefundTransactionId;
        this.providerPayload = providerPayload;
        this.failureCode = null;
        this.failureMessage = null;
        this.processingStartedAt = null;
        this.executedAt = LocalDateTime.now();
        return PaymentOperationAuditAction.PAYMENT_REFUND_SUCCEEDED;
    }

    public PaymentOperationAuditAction markPendingProviderConfirmation(
            String failureCode,
            String failureMessage,
            String providerPayload) {
        return markPendingProviderConfirmation(
                failureCode,
                failureMessage,
                providerPayload,
                processingStartedAt);
    }

    public PaymentOperationAuditAction markPendingProviderConfirmation(
            String failureCode,
            String failureMessage,
            String providerPayload,
            LocalDateTime leaseStartedAt) {
        requireActiveLease(leaseStartedAt);

        this.status = PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.providerPayload = providerPayload;
        this.processingStartedAt = null;
        return PaymentOperationAuditAction.PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION;
    }

    public PaymentOperationAuditAction markFailed(
            String failureCode,
            String failureMessage,
            String providerPayload) {
        return markFailed(failureCode, failureMessage, providerPayload, processingStartedAt);
    }

    public PaymentOperationAuditAction markFailed(
            String failureCode,
            String failureMessage,
            String providerPayload,
            LocalDateTime leaseStartedAt) {
        requireActiveLease(leaseStartedAt);

        this.status = PaymentRefundStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.providerPayload = providerPayload;
        this.processingStartedAt = null;
        return PaymentOperationAuditAction.PAYMENT_REFUND_FAILED;
    }

    private void requireActiveLease(LocalDateTime leaseStartedAt) {
        if (leaseStartedAt == null
                || status != PaymentRefundStatus.PROCESSING
                || !Objects.equals(processingStartedAt, toSecondPrecision(leaseStartedAt))) {
            throw new IllegalStateException("Refund processing lease is no longer active.");
        }
    }

    private LocalDateTime toSecondPrecision(LocalDateTime value) {
        return value.withNano(0);
    }
}
