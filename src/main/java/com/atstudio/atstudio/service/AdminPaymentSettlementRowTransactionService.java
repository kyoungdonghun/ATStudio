package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminPaymentSettlementRowTransactionService {

    private static final Collection<PaymentRefundStatus> SETTLED_REFUND_STATUSES =
            List.of(PaymentRefundStatus.SUCCEEDED);

    private final PaymentSettlementRepository paymentSettlementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentOperationAuditLogService auditLogService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentSettlementStatus persistImported(
            CustomUserDetails actorDetails,
            PaymentSettlement settlement) {
        reconcile(settlement);
        PaymentSettlement saved = paymentSettlementRepository.saveAndFlush(settlement);
        auditLogService.recordPaymentSettlementEvent(
                actorDetails,
                saved,
                PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED,
                null,
                saved.getStatus(),
                "Settlement row imported.");
        return saved.getStatus();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReconciliationRowResult persistMissing(
            CustomUserDetails actorDetails,
            Long paymentID,
            String deduplicationKey,
            String batchKey,
            LocalDate settlementBaseDate) {
        SubscriptionPayment payment = subscriptionPaymentRepository.findWithGraphById(paymentID)
                .orElseThrow(() -> new IllegalStateException("Subscription payment is unavailable."));
        if (payment.getPaymentOrder() == null) {
            throw new IllegalArgumentException("Local payment has no payment order.");
        }
        String orderID = payment.getPaymentOrder().getOrderId();
        if (paymentSettlementRepository.existsByOrderIdAndSourceNot(
                orderID,
                PaymentSettlementSource.SYSTEM_RECONCILIATION)) {
            return ReconciliationRowResult.duplicate();
        }

        BigDecimal refundAmount = paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(
                payment,
                SETTLED_REFUND_STATUSES);
        PaymentProviderType provider = payment.getProvider() == null
                ? payment.getPaymentOrder().getProvider()
                : payment.getProvider();
        PaymentSettlement settlement = PaymentSettlement.builder()
                .source(PaymentSettlementSource.SYSTEM_RECONCILIATION)
                .provider(provider)
                .status(PaymentSettlementStatus.PROVIDER_SETTLEMENT_NOT_FOUND)
                .deduplicationKey(deduplicationKey)
                .importBatchKey(batchKey)
                .sourceFileName("system-reconciliation")
                .orderId(orderID)
                .providerPaymentKey(payment.getPgTransactionId())
                .paymentOrder(payment.getPaymentOrder())
                .subscriptionPayment(payment)
                .user(payment.getUser())
                .grossAmount(payment.getAmount())
                .refundAmount(refundAmount)
                .feeAmount(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .netSettlementAmount(payment.getAmount().subtract(refundAmount))
                .currency("KRW")
                .settlementBaseDate(settlementBaseDate)
                .mismatchReason("No imported provider settlement evidence found for local payment.")
                .reconciledAt(LocalDateTime.now())
                .build();
        PaymentSettlement saved = paymentSettlementRepository.saveAndFlush(settlement);
        auditLogService.recordPaymentSettlementEvent(
                actorDetails,
                saved,
                PaymentOperationAuditAction.PAYMENT_SETTLEMENT_RECONCILED,
                null,
                saved.getStatus(),
                "Missing provider settlement evidence generated.");
        return ReconciliationRowResult.imported(saved.getStatus());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean exactDeduplicationWinnerExists(String deduplicationKey) {
        return paymentSettlementRepository.findByDeduplicationKey(deduplicationKey).isPresent();
    }

    private void reconcile(PaymentSettlement settlement) {
        Optional<PaymentOrder> order = paymentOrderRepository.findByOrderId(settlement.getOrderId());
        Optional<SubscriptionPayment> payment = Optional.empty();
        if (order.isPresent()) {
            payment = subscriptionPaymentRepository.findByPaymentOrder(order.get());
        }
        if (payment.isEmpty() && hasText(settlement.getProviderPaymentKey())) {
            payment = subscriptionPaymentRepository.findFirstByPgTransactionId(settlement.getProviderPaymentKey());
        }

        if (payment.isEmpty()) {
            settlement.applyReconciliation(
                    PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND,
                    order.orElse(null),
                    null,
                    order.map(PaymentOrder::getUser).orElse(null),
                    "Local finalized subscription payment was not found.");
            return;
        }

        SubscriptionPayment subscriptionPayment = payment.get();
        BigDecimal localRefundAmount = paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(
                subscriptionPayment,
                SETTLED_REFUND_STATUSES);
        List<String> mismatches = new ArrayList<>();
        if (!sameAmount(settlement.getGrossAmount(), subscriptionPayment.getAmount())) {
            mismatches.add("gross_amount local=" + subscriptionPayment.getAmount()
                    + " provider=" + settlement.getGrossAmount());
        }
        if (!sameAmount(settlement.getRefundAmount(), localRefundAmount)) {
            mismatches.add("refund_amount local=" + localRefundAmount
                    + " provider=" + settlement.getRefundAmount());
        }
        BigDecimal expectedNet = settlement.getGrossAmount()
                .subtract(settlement.getRefundAmount())
                .subtract(settlement.getFeeAmount())
                .subtract(settlement.getVatAmount());
        if (!sameAmount(settlement.getNetSettlementAmount(), expectedNet)) {
            mismatches.add("net_settlement_amount expected=" + expectedNet
                    + " provider=" + settlement.getNetSettlementAmount());
        }
        PaymentSettlementStatus status = mismatches.isEmpty()
                ? PaymentSettlementStatus.MATCHED
                : PaymentSettlementStatus.MISMATCHED;
        settlement.applyReconciliation(
                status,
                subscriptionPayment.getPaymentOrder(),
                subscriptionPayment,
                subscriptionPayment.getUser(),
                mismatches.isEmpty() ? null : String.join("; ", mismatches));
    }

    private boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.compareTo(right) == 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public record ReconciliationRowResult(boolean imported, PaymentSettlementStatus status) {

        static ReconciliationRowResult imported(PaymentSettlementStatus status) {
            return new ReconciliationRowResult(true, status);
        }

        static ReconciliationRowResult duplicate() {
            return new ReconciliationRowResult(false, null);
        }
    }
}
