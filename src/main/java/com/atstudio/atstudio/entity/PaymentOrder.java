package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "payment_orders",
        indexes = {
                @Index(name = "idx_payment_orders_user_status", columnList = "user_id,status"),
                @Index(
                        name = "idx_payment_orders_status_processing",
                        columnList = "status,processing_started_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_payment_orders_command_key",
                        columnNames = "command_key"),
                @UniqueConstraint(
                        name = "uq_payment_orders_provider_attempt_key",
                        columnNames = {"provider", "provider_idempotency_key"}),
                @UniqueConstraint(
                        name = "uq_payment_orders_renewal_period",
                        columnNames = {
                                "billing_agreement_id",
                                "user_subscription_id",
                                "purpose",
                                "billing_period_start"
                        })
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "command_key", length = 191)
    private String commandKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentOrderStatus status = PaymentOrderStatus.READY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_id")
    private UserSubscription userSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_agreement_id")
    private BillingAgreement billingAgreement;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 10)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "upgrade_target_billing_cycle", length = 10)
    private BillingCycle upgradeTargetBillingCycle;

    @Column(name = "billing_period_start")
    private LocalDate billingPeriodStart;

    @Builder.Default
    @Column(name = "provider_attempt", nullable = false)
    private int providerAttempt = 0;

    @Column(name = "provider_idempotency_key", length = 100)
    private String providerIdempotencyKey;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "KRW";

    @Column(name = "pg_transaction_id", length = 200)
    private String pgTransactionId;

    @Column(name = "provider_payload", columnDefinition = "TEXT")
    private String providerPayload;

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    public boolean isOwnedBy(User user) {
        return user != null && this.user != null && Objects.equals(this.user.getId(), user.getId());
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public void markInProgress(String providerPayload) {
        if (status == PaymentOrderStatus.READY) {
            this.status = PaymentOrderStatus.IN_PROGRESS;
        }
        this.providerPayload = providerPayload;
    }

    public void claimProviderAttempt(
            String commandKey,
            String providerIdempotencyKey,
            LocalDateTime processingStartedAt) {
        if (isBlank(commandKey) || isBlank(providerIdempotencyKey) || processingStartedAt == null) {
            throw new IllegalArgumentException("Payment command claim fields are required.");
        }
        if (this.commandKey != null && !this.commandKey.equals(commandKey)) {
            throw new IllegalStateException("Payment command key cannot be changed.");
        }
        if (status != PaymentOrderStatus.READY
                && status != PaymentOrderStatus.IN_PROGRESS
                && status != PaymentOrderStatus.FAILED) {
            throw new IllegalStateException("Payment order cannot claim a provider attempt from " + status + ".");
        }
        requirePurposeFields();

        this.commandKey = commandKey;
        this.providerAttempt += 1;
        this.providerIdempotencyKey = providerIdempotencyKey;
        this.processingStartedAt = processingStartedAt;
        this.status = PaymentOrderStatus.PROCESSING;
        this.failureCode = null;
        this.failureMessage = null;
    }

    public void markProviderSucceeded(String pgTransactionId, String providerPayload) {
        if (isBlank(pgTransactionId)) {
            throw new IllegalArgumentException("Provider transaction ID is required.");
        }
        requirePurposeFields();
        if (status == PaymentOrderStatus.PROVIDER_SUCCEEDED
                && Objects.equals(this.pgTransactionId, pgTransactionId)) {
            return;
        }
        if (status != PaymentOrderStatus.PROCESSING) {
            throw new IllegalStateException("Payment order cannot record provider success from " + status + ".");
        }

        this.status = PaymentOrderStatus.PROVIDER_SUCCEEDED;
        this.pgTransactionId = pgTransactionId;
        this.providerPayload = providerPayload;
        this.failureCode = null;
        this.failureMessage = null;
        this.processingStartedAt = null;
    }

    public void markProviderSucceededFromReconciliation(
            String pgTransactionId,
            String providerPayload,
            LocalDateTime staleBefore) {
        if (isBlank(pgTransactionId)) {
            throw new IllegalArgumentException("Provider transaction ID is required.");
        }
        if (status == PaymentOrderStatus.PROCESSING && !isProcessingStale(staleBefore)) {
            throw new IllegalStateException("Fresh payment processing cannot be reconciled as provider success.");
        }
        if (status != PaymentOrderStatus.PROCESSING
                && status != PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION) {
            throw new IllegalStateException(
                    "Payment order cannot reconcile provider success from " + status + ".");
        }
        if (this.pgTransactionId != null && !Objects.equals(this.pgTransactionId, pgTransactionId)) {
            throw new IllegalStateException("Provider transaction ID cannot be changed.");
        }
        requirePurposeFields();

        this.status = PaymentOrderStatus.PROVIDER_SUCCEEDED;
        this.pgTransactionId = pgTransactionId;
        this.providerPayload = providerPayload;
        this.failureCode = null;
        this.failureMessage = null;
        this.processingStartedAt = null;
    }

    public void markProviderOutcomeUnknown(String code, String message) {
        if (status != PaymentOrderStatus.PROCESSING) {
            throw new IllegalStateException("Payment order cannot record an unknown provider outcome from " + status + ".");
        }

        this.status = PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION;
        this.failureCode = code;
        this.failureMessage = message;
        this.processingStartedAt = null;
    }

    public boolean isProcessingStale(LocalDateTime staleBefore) {
        return staleBefore != null
                && status == PaymentOrderStatus.PROCESSING
                && processingStartedAt != null
                && !processingStartedAt.isAfter(staleBefore);
    }

    public void markDone(String pgTransactionId, UserSubscription userSubscription, String providerPayload) {
        this.status = PaymentOrderStatus.DONE;
        this.pgTransactionId = pgTransactionId;
        this.userSubscription = userSubscription;
        this.providerPayload = providerPayload;
        this.failureCode = null;
        this.failureMessage = null;
        this.processingStartedAt = null;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed(String code, String message) {
        this.status = PaymentOrderStatus.FAILED;
        this.failureCode = code;
        this.failureMessage = message;
        this.processingStartedAt = null;
    }

    public void markCancelled(String reason) {
        this.status = PaymentOrderStatus.CANCELLED;
        this.failureCode = "CANCELLED";
        this.failureMessage = reason;
        this.processingStartedAt = null;
    }

    public void markExpired() {
        this.status = PaymentOrderStatus.EXPIRED;
        this.failureCode = "EXPIRED";
        this.failureMessage = "Payment order expired.";
        this.processingStartedAt = null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void requirePurposeFields() {
        if (purpose == PaymentPurpose.UPGRADE && upgradeTargetBillingCycle == null) {
            throw new IllegalStateException("Upgrade target billing cycle is required.");
        }
        if (purpose != PaymentPurpose.UPGRADE && upgradeTargetBillingCycle != null) {
            throw new IllegalStateException("Upgrade target billing cycle is allowed only for upgrade orders.");
        }
    }
}
