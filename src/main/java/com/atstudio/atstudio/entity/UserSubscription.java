package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BillingCycle billingCycle;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startedAt;

    @Column(nullable = false)
    private LocalDate expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_subscription_id")
    private Subscription pendingSubscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_billing_cycle", length = 10)
    private BillingCycle pendingBillingCycle;

    public void schedulePendingChange(Subscription pendingSub, BillingCycle cycle) {
        this.pendingSubscription = pendingSub;
        this.pendingBillingCycle = cycle;
    }

    public void clearPendingChange() {
        this.pendingSubscription = null;
        this.pendingBillingCycle = null;
    }

    public void upgrade(Subscription newSubscription, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
        this.subscription = newSubscription;
        this.billingCycle = newBillingCycle;
        this.startedAt = LocalDate.now();
        this.expiresAt = newExpiresAt;
        this.pendingSubscription = null;
        this.pendingBillingCycle = null;
    }

    public void upgradeKeepingPeriod(Subscription newSubscription, BillingCycle nextBillingCycle) {
        this.subscription = newSubscription;
        if (nextBillingCycle != null && nextBillingCycle != this.billingCycle) {
            this.pendingSubscription = newSubscription;
            this.pendingBillingCycle = nextBillingCycle;
        } else {
            this.pendingSubscription = null;
            this.pendingBillingCycle = null;
        }
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void reactivate() {
        if (this.status != SubscriptionStatus.EXPIRED) {
            this.status = SubscriptionStatus.ACTIVE;
        }
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.pendingSubscription = null;
        this.pendingBillingCycle = null;
    }

    public void applyPending() {
        if (this.pendingSubscription == null) return;
        this.subscription = this.pendingSubscription;
        this.billingCycle = this.pendingBillingCycle;
        this.startedAt = LocalDate.now();
        this.expiresAt = this.pendingBillingCycle == BillingCycle.YEARLY
                ? this.startedAt.plusYears(1)
                : this.startedAt.plusMonths(1);
        this.status = SubscriptionStatus.ACTIVE;
        this.pendingSubscription = null;
        this.pendingBillingCycle = null;
    }

    public void startNewSubscription(
            Subscription newSubscription,
            BillingCycle newBillingCycle,
            LocalDate newStartedAt,
            LocalDate newExpiresAt) {
        this.subscription = newSubscription;
        this.billingCycle = newBillingCycle;
        this.startedAt = newStartedAt;
        this.expiresAt = newExpiresAt;
        this.status = SubscriptionStatus.ACTIVE;
        this.pendingSubscription = null;
        this.pendingBillingCycle = null;
    }

    public boolean hasPending() {
        return this.pendingSubscription != null || this.pendingBillingCycle != null;
    }

    public void adminUpdate(SubscriptionStatus newStatus, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
        if (newStatus != null) this.status = newStatus;
        if (newBillingCycle != null) this.billingCycle = newBillingCycle;
        if (newExpiresAt != null) this.expiresAt = newExpiresAt;
    }

    public void applyEntitlementCorrection(
            Subscription targetSubscription,
            BillingCycle targetBillingCycle,
            SubscriptionStatus targetStatus,
            LocalDate targetExpiresAt,
            boolean clearPendingChange) {
        this.subscription = targetSubscription;
        this.billingCycle = targetBillingCycle;
        this.status = targetStatus;
        this.expiresAt = targetExpiresAt;
        if (clearPendingChange) {
            clearPendingChange();
        }
    }
}
