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

    public void upgrade(Subscription newSubscription, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
        this.subscription = newSubscription;
        this.billingCycle = newBillingCycle;
        this.startedAt = LocalDate.now();
        this.expiresAt = newExpiresAt;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void adminUpdate(SubscriptionStatus newStatus, BillingCycle newBillingCycle, LocalDate newExpiresAt) {
        if (newStatus != null) this.status = newStatus;
        if (newBillingCycle != null) this.billingCycle = newBillingCycle;
        if (newExpiresAt != null) this.expiresAt = newExpiresAt;
    }
}
