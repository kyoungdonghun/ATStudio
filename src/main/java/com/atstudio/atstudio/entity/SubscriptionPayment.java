package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "subscription_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_subscription_payments_order",
                        columnNames = "payment_order_id"),
                @UniqueConstraint(
                        name = "uq_subscription_payments_provider_transaction",
                        columnNames = {"provider", "pg_transaction_id"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_subscription_id", nullable = false)
    private UserSubscription userSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id")
    private PaymentOrder paymentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_agreement_id")
    private BillingAgreement billingAgreement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus paymentStatus = PaymentStatus.READY;

    @Column(name = "pg_transaction_id", length = 200)
    private String pgTransactionId;
}
