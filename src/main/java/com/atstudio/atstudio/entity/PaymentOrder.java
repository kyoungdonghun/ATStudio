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
        name = "payment_orders",
        indexes = {
                @Index(name = "idx_payment_orders_user_status", columnList = "user_id,status")
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

    public void markDone(String pgTransactionId, UserSubscription userSubscription, String providerPayload) {
        this.status = PaymentOrderStatus.DONE;
        this.pgTransactionId = pgTransactionId;
        this.userSubscription = userSubscription;
        this.providerPayload = providerPayload;
        this.failureCode = null;
        this.failureMessage = null;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markFailed(String code, String message) {
        this.status = PaymentOrderStatus.FAILED;
        this.failureCode = code;
        this.failureMessage = message;
    }

    public void markCancelled(String reason) {
        this.status = PaymentOrderStatus.CANCELLED;
        this.failureCode = "CANCELLED";
        this.failureMessage = reason;
    }

    public void markExpired() {
        this.status = PaymentOrderStatus.EXPIRED;
        this.failureCode = "EXPIRED";
        this.failureMessage = "Payment order expired.";
    }
}
