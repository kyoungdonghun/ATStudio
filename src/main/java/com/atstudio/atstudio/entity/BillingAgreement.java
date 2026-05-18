package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
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
                @Index(name = "idx_billing_agreements_status_next", columnList = "status,next_billing_at")
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

    @Column(name = "last_charged_at")
    private LocalDateTime lastChargedAt;

    @Builder.Default
    @Column(name = "failure_count", nullable = false)
    private int failureCount = 0;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

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
        this.failureCount = 0;
        this.cancelledAt = null;
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
        this.lastChargedAt = null;
        this.failureCount = 0;
        this.cancelledAt = null;
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
    }

    public void clearIssuedKey() {
        this.billingKeyCiphertext = null;
        this.billingKeyFingerprint = null;
        this.payMethod = null;
        this.maskedMethod = null;
        this.nextBillingAt = null;
        this.lastChargedAt = null;
    }

    public void recordSuccessfulCharge(LocalDate nextBillingAt) {
        this.lastChargedAt = LocalDateTime.now();
        this.nextBillingAt = nextBillingAt;
        this.failureCount = 0;
        this.status = BillingAgreementStatus.ACTIVE;
    }

    public void recordFailedCharge() {
        this.failureCount += 1;
    }

    public void recordFailedCharge(LocalDate nextBillingAt) {
        this.failureCount += 1;
        this.nextBillingAt = nextBillingAt;
    }

    public void suspend() {
        this.status = BillingAgreementStatus.SUSPENDED;
    }

    public void cancel() {
        this.status = BillingAgreementStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = BillingAgreementStatus.EXPIRED;
    }

    public boolean isChargeableOn(LocalDate today) {
        return status == BillingAgreementStatus.ACTIVE
                && nextBillingAt != null
                && !nextBillingAt.isAfter(today);
    }

    public boolean isOwnedBy(User user) {
        return user != null && this.user != null && Objects.equals(this.user.getId(), user.getId());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
