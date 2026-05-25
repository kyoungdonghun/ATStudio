package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionAction;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_entitlement_corrections",
        indexes = {
                @Index(name = "idx_payment_entitlement_corrections_status_created", columnList = "status,created_at"),
                @Index(name = "idx_payment_entitlement_corrections_refund", columnList = "payment_refund_id"),
                @Index(name = "idx_payment_entitlement_corrections_user_created", columnList = "user_id,created_at")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentEntitlementCorrection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_refund_id", nullable = false)
    private PaymentRefund paymentRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_payment_id", nullable = false)
    private SubscriptionPayment subscriptionPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    private PaymentOrder paymentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_id", nullable = false)
    private UserSubscription userSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentEntitlementCorrectionStatus status = PaymentEntitlementCorrectionStatus.REQUESTED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private PaymentEntitlementCorrectionAction action = PaymentEntitlementCorrectionAction.SET_SUBSCRIPTION_STATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_subscription_id", nullable = false)
    private Subscription beforeSubscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_billing_cycle", nullable = false, length = 10)
    private BillingCycle beforeBillingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_status", nullable = false, length = 20)
    private SubscriptionStatus beforeStatus;

    @Column(name = "before_expires_at", nullable = false)
    private LocalDate beforeExpiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_pending_subscription_id")
    private Subscription beforePendingSubscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_pending_billing_cycle", length = 10)
    private BillingCycle beforePendingBillingCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_subscription_id", nullable = false)
    private Subscription targetSubscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_billing_cycle", nullable = false, length = 10)
    private BillingCycle targetBillingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_status", nullable = false, length = 20)
    private SubscriptionStatus targetStatus;

    @Column(name = "target_expires_at", nullable = false)
    private LocalDate targetExpiresAt;

    @Column(name = "clear_pending_change", nullable = false)
    private boolean clearPendingChange;

    @Column(name = "cancel_billing_agreement", nullable = false)
    private boolean cancelBillingAgreement;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_billing_agreement_status", length = 20)
    private BillingAgreementStatus beforeBillingAgreementStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_billing_agreement_status", length = 20)
    private BillingAgreementStatus afterBillingAgreementStatus;

    @Column(name = "reason_note", length = 500)
    private String reasonNote;

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

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    public PaymentOperationAuditAction approve(User actor) {
        this.status = PaymentEntitlementCorrectionStatus.APPROVED;
        this.approvedBy = actor;
        this.approvedAt = LocalDateTime.now();
        return PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_APPROVED;
    }

    public PaymentOperationAuditAction markProcessing(User actor) {
        this.status = PaymentEntitlementCorrectionStatus.PROCESSING;
        this.executedBy = actor;
        return PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING;
    }

    public PaymentOperationAuditAction markSucceeded(BillingAgreementStatus afterBillingAgreementStatus) {
        this.status = PaymentEntitlementCorrectionStatus.SUCCEEDED;
        this.afterBillingAgreementStatus = afterBillingAgreementStatus;
        this.failureCode = null;
        this.failureMessage = null;
        this.executedAt = LocalDateTime.now();
        return PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED;
    }

    public PaymentOperationAuditAction markFailed(String failureCode, String failureMessage) {
        this.status = PaymentEntitlementCorrectionStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        return PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_FAILED;
    }
}
