package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
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

@Entity
@Table(
        name = "payment_settlements",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payment_settlements_deduplication_key",
                columnNames = "deduplication_key"
        ),
        indexes = {
                @Index(name = "idx_payment_settlements_status_created", columnList = "status,created_at"),
                @Index(name = "idx_payment_settlements_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_settlements_payment_key", columnList = "provider_payment_key"),
                @Index(name = "idx_payment_settlements_base_date", columnList = "settlement_base_date")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentSettlementSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentProviderType provider;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PaymentSettlementStatus status = PaymentSettlementStatus.IMPORTED;

    @Column(name = "deduplication_key", nullable = false, length = 64)
    private String deduplicationKey;

    @Column(name = "import_batch_key", nullable = false, length = 64)
    private String importBatchKey;

    @Column(name = "source_file_name", length = 255)
    private String sourceFileName;

    @Column(name = "source_row_number")
    private Integer sourceRowNumber;

    @Column(name = "provider_settlement_id", length = 200)
    private String providerSettlementId;

    @Column(name = "provider_payment_key", length = 200)
    private String providerPaymentKey;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id")
    private PaymentOrder paymentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_payment_id")
    private SubscriptionPayment subscriptionPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Builder.Default
    @Column(name = "refund_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "fee_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "vat_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "net_settlement_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSettlementAmount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "KRW";

    @Column(name = "settlement_base_date", nullable = false)
    private LocalDate settlementBaseDate;

    @Column(name = "settlement_payout_date")
    private LocalDate settlementPayoutDate;

    @Column(name = "provider_status", length = 100)
    private String providerStatus;

    @Column(name = "mismatch_reason", length = 500)
    private String mismatchReason;

    @Column(name = "operator_note", length = 500)
    private String operatorNote;

    @Column(name = "source_payload", columnDefinition = "TEXT")
    private String sourcePayload;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ignored_by")
    private User ignoredBy;

    @Column(name = "ignored_at")
    private LocalDateTime ignoredAt;

    public void applyReconciliation(
            PaymentSettlementStatus status,
            PaymentOrder paymentOrder,
            SubscriptionPayment subscriptionPayment,
            User user,
            String mismatchReason) {
        this.status = status;
        this.paymentOrder = paymentOrder;
        this.subscriptionPayment = subscriptionPayment;
        this.user = user;
        this.mismatchReason = truncate(mismatchReason, 500);
        this.reconciledAt = LocalDateTime.now();
    }

    public void ignore(User actor, String note) {
        this.status = PaymentSettlementStatus.IGNORED;
        this.ignoredBy = actor;
        this.ignoredAt = LocalDateTime.now();
        this.operatorNote = truncate(note, 500);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
