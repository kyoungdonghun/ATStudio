package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "billing_agreements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_billing_agreements_user_provider",
                        columnNames = {"user_id", "provider"}
                ),
                @UniqueConstraint(
                        name = "uq_billing_agreements_provider_customer",
                        columnNames = {"provider", "provider_customer_key"}
                )
        },
        indexes = {
                @Index(name = "idx_billing_agreements_status_next", columnList = "status,next_billing_at"),
                @Index(
                        name = "idx_billing_agreements_renewal_retry",
                        columnList = "status,renewal_retry_at,id"),
                @Index(
                        name = "idx_billing_agreements_cleanup",
                        columnList = "billing_key_cleanup_status,billing_key_cleanup_started_at,id"),
                @Index(
                        name = "idx_billing_agreements_local_reconciliation",
                        columnList = "status,id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingAgreementStatus status = BillingAgreementStatus.READY;

    @Column(name = "provider_customer_key", nullable = false, length = 300)
    private String providerCustomerKey;

    @Column(name = "billing_key_ciphertext", length = 1000)
    private String billingKeyCiphertext;

    @Column(name = "billing_key_fingerprint", length = 128)
    private String billingKeyFingerprint;

    @Column(name = "pay_method", length = 50)
    private String payMethod;

    @Column(name = "masked_method", length = 100)
    private String maskedMethod;

    @Column(name = "next_billing_at")
    private LocalDate nextBillingAt;

    @Column(name = "renewal_retry_at")
    private LocalDate renewalRetryAt;

    @Column(name = "last_charged_at")
    private LocalDateTime lastChargedAt;

    @Builder.Default
    @Column(name = "failure_count", nullable = false)
    private int failureCount = 0;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "billing_key_cleanup_status", nullable = false, length = 40)
    private BillingKeyCleanupStatus billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;

    @Column(name = "billing_key_cleanup_started_at")
    private LocalDateTime billingKeyCleanupStartedAt;

    public void activate(
            String billingKeyCiphertext,
            String billingKeyFingerprint,
            String payMethod,
            String maskedMethod,
            LocalDate nextBillingAt) {
        if (isBlank(billingKeyCiphertext) || isBlank(billingKeyFingerprint)) {
            throw new IllegalArgumentException("Billing key ciphertext and fingerprint are required.");
        }

        this.billingKeyCiphertext = billingKeyCiphertext;
        this.billingKeyFingerprint = billingKeyFingerprint;
        this.payMethod = payMethod;
        this.maskedMethod = maskedMethod;
        this.nextBillingAt = nextBillingAt;
        this.renewalRetryAt = null;
        this.failureCount = 0;
        this.cancelledAt = null;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
        this.status = BillingAgreementStatus.ACTIVE;
    }

    public void prepareRegistration(String providerCustomerKey) {
        if (isBlank(providerCustomerKey)) {
            throw new IllegalArgumentException("Provider customer key is required.");
        }

        this.providerCustomerKey = providerCustomerKey;
        this.billingKeyCiphertext = null;
        this.billingKeyFingerprint = null;
        this.payMethod = null;
        this.maskedMethod = null;
        this.nextBillingAt = null;
        this.renewalRetryAt = null;
        this.lastChargedAt = null;
        this.failureCount = 0;
        this.cancelledAt = null;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
        this.status = BillingAgreementStatus.READY;
    }

    public void storeIssuedKey(
            String billingKeyCiphertext,
            String billingKeyFingerprint,
            String payMethod,
            String maskedMethod) {
        if (isBlank(billingKeyCiphertext) || isBlank(billingKeyFingerprint)) {
            throw new IllegalArgumentException("Billing key ciphertext and fingerprint are required.");
        }

        this.billingKeyCiphertext = billingKeyCiphertext;
        this.billingKeyFingerprint = billingKeyFingerprint;
        this.payMethod = payMethod;
        this.maskedMethod = maskedMethod;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
    }

    public void clearIssuedKey() {
        this.billingKeyCiphertext = null;
        this.billingKeyFingerprint = null;
        this.payMethod = null;
        this.maskedMethod = null;
        this.nextBillingAt = null;
        this.renewalRetryAt = null;
        this.lastChargedAt = null;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
    }

    public void expireIssuedKey() {
        this.billingKeyCiphertext = null;
        this.billingKeyFingerprint = null;
        this.payMethod = null;
        this.maskedMethod = null;
        this.nextBillingAt = null;
        this.renewalRetryAt = null;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
        this.status = BillingAgreementStatus.EXPIRED;
    }

    public void recordSuccessfulCharge(LocalDate nextBillingAt) {
        this.lastChargedAt = LocalDateTime.now();
        this.nextBillingAt = nextBillingAt;
        this.renewalRetryAt = null;
        this.failureCount = 0;
        this.status = BillingAgreementStatus.ACTIVE;
    }

    public void resume(LocalDate nextBillingAt) {
        if (isBlank(billingKeyCiphertext) || isBlank(billingKeyFingerprint)) {
            throw new IllegalArgumentException("Issued billing key is required to resume agreement.");
        }

        this.nextBillingAt = nextBillingAt;
        this.renewalRetryAt = null;
        this.failureCount = 0;
        this.cancelledAt = null;
        this.status = BillingAgreementStatus.ACTIVE;
    }

    public void recordFailedCharge() {
        this.failureCount += 1;
    }

    public void recordFailedCharge(LocalDate renewalRetryAt) {
        if (renewalRetryAt == null) {
            throw new IllegalArgumentException("Renewal retry date is required.");
        }

        this.failureCount += 1;
        this.renewalRetryAt = renewalRetryAt;
    }

    public void consumeRenewalRetry() {
        if (renewalRetryAt == null) {
            throw new IllegalStateException("Renewal retry date is not scheduled.");
        }

        this.renewalRetryAt = null;
    }

    public void suspend() {
        this.renewalRetryAt = null;
        this.status = BillingAgreementStatus.SUSPENDED;
    }

    public void cancel() {
        this.renewalRetryAt = null;
        this.status = BillingAgreementStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void expire() {
        this.renewalRetryAt = null;
        this.status = BillingAgreementStatus.EXPIRED;
    }

    public void markBillingKeyCleanupRequired() {
        requireCancelledAgreementWithRetainedKey();
        if (billingKeyCleanupStatus != BillingKeyCleanupStatus.FAILED
                && billingKeyCleanupStatus != BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION) {
            throw new IllegalStateException(
                    "Billing key cleanup cannot be retried from " + billingKeyCleanupStatus + ".");
        }

        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.REQUIRED;
        this.billingKeyCleanupStartedAt = null;
    }

    public void claimBillingKeyCleanup(LocalDateTime startedAt) {
        requireCancelledAgreementWithRetainedKey();
        if (startedAt == null) {
            throw new IllegalArgumentException("Billing key cleanup start time is required.");
        }
        if (billingKeyCleanupStatus != BillingKeyCleanupStatus.NONE
                && billingKeyCleanupStatus != BillingKeyCleanupStatus.REQUIRED) {
            throw new IllegalStateException(
                    "Billing key cleanup cannot be claimed from " + billingKeyCleanupStatus + ".");
        }

        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.PROCESSING;
        this.billingKeyCleanupStartedAt = toSecondPrecision(startedAt);
    }

    public void markBillingKeyCleanupSucceeded(LocalDateTime leaseStartedAt) {
        requireBillingKeyCleanupLease(leaseStartedAt);
        this.billingKeyCiphertext = null;
        this.billingKeyFingerprint = null;
        this.payMethod = null;
        this.maskedMethod = null;
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.NONE;
        this.billingKeyCleanupStartedAt = null;
    }

    public void markBillingKeyCleanupFailed(LocalDateTime leaseStartedAt) {
        requireBillingKeyCleanupLease(leaseStartedAt);
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.FAILED;
        this.billingKeyCleanupStartedAt = null;
    }

    public void markBillingKeyCleanupPendingProviderConfirmation(LocalDateTime leaseStartedAt) {
        requireBillingKeyCleanupLease(leaseStartedAt);
        this.billingKeyCleanupStatus = BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION;
        this.billingKeyCleanupStartedAt = null;
    }

    public boolean isBillingKeyCleanupProcessingStale(LocalDateTime staleBefore) {
        return staleBefore != null
                && billingKeyCleanupStatus == BillingKeyCleanupStatus.PROCESSING
                && billingKeyCleanupStartedAt != null
                && !billingKeyCleanupStartedAt.isAfter(toSecondPrecision(staleBefore));
    }

    public boolean isChargeableOn(LocalDate today) {
        return status == BillingAgreementStatus.ACTIVE
                && nextBillingAt != null
                && !nextBillingAt.isAfter(today);
    }

    public boolean isInitialSubscriptionFinalizationEligible() {
        return status == BillingAgreementStatus.READY
                && billingKeyCleanupStatus == BillingKeyCleanupStatus.NONE
                && cancelledAt == null
                && !isBlank(billingKeyCiphertext)
                && !isBlank(billingKeyFingerprint);
    }

    public boolean isOwnedBy(User user) {
        return user != null && this.user != null && Objects.equals(this.user.getId(), user.getId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void requireCancelledAgreementWithRetainedKey() {
        if (status != BillingAgreementStatus.CANCELLED || isBlank(billingKeyCiphertext)) {
            throw new IllegalStateException("Cancelled agreement with a retained billing key is required.");
        }
    }

    private void requireBillingKeyCleanupLease(LocalDateTime leaseStartedAt) {
        if (leaseStartedAt == null
                || billingKeyCleanupStatus != BillingKeyCleanupStatus.PROCESSING
                || !Objects.equals(billingKeyCleanupStartedAt, toSecondPrecision(leaseStartedAt))) {
            throw new IllegalStateException("Billing key cleanup lease is no longer active.");
        }
    }

    private LocalDateTime toSecondPrecision(LocalDateTime value) {
        return value.withNano(0);
    }
}
