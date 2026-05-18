package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class RecurringRenewalService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final int GRACE_DAYS = 3;
    private static final int MAX_RETRY_COUNT = 3;
    private static final List<PaymentOrderStatus> RENEWAL_ORDER_STATUSES = List.of(
            PaymentOrderStatus.READY,
            PaymentOrderStatus.IN_PROGRESS,
            PaymentOrderStatus.FAILED,
            PaymentOrderStatus.DONE
    );

    private final BillingAgreementRepository billingAgreementRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final BillingKeyCrypto billingKeyCrypto;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public RecurringRenewalService(
            BillingAgreementRepository billingAgreementRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            SubscriptionPaymentRepository subscriptionPaymentRepository,
            BillingKeyCrypto billingKeyCrypto,
            List<RecurringPaymentProvider> recurringProviders) {
        this.billingAgreementRepository = billingAgreementRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.billingKeyCrypto = billingKeyCrypto;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(RecurringPaymentProvider::getProviderType, Function.identity()));
    }

    @Transactional
    public RenewalRunResult processDueRenewals() {
        return processDueRenewals(LocalDate.now());
    }

    @Transactional
    public RenewalRunResult processDueRenewals(LocalDate today) {
        List<BillingAgreement> dueAgreements = billingAgreementRepository
                .findByStatusAndNextBillingAtLessThanEqual(BillingAgreementStatus.ACTIVE, today);

        int attempted = 0;
        int succeeded = 0;
        int failed = 0;
        int skipped = 0;

        for (BillingAgreement agreement : dueAgreements) {
            RenewalOutcome outcome = processAgreement(agreement, today);
            attempted += outcome.attempted();
            succeeded += outcome.succeeded();
            failed += outcome.failed();
            skipped += outcome.skipped();
        }

        if (attempted > 0 || skipped > 0) {
            log.info("Recurring renewal processed: attempted={}, succeeded={}, failed={}, skipped={}",
                    attempted, succeeded, failed, skipped);
        }
        return new RenewalRunResult(attempted, succeeded, failed, skipped);
    }

    private RenewalOutcome processAgreement(BillingAgreement agreement, LocalDate today) {
        if (agreement.getStatus() != BillingAgreementStatus.ACTIVE || agreement.getProvider() != RECURRING_PROVIDER) {
            return RenewalOutcome.skip();
        }
        if (isBlank(agreement.getBillingKeyCiphertext())) {
            agreement.suspend();
            return RenewalOutcome.skip();
        }

        Optional<UserSubscription> activeSubscription =
                userSubscriptionRepository.findActiveByUser(agreement.getUser(), today);
        if (activeSubscription.isEmpty()) {
            suspendAgreementWithoutActiveSubscription(agreement, today);
            return RenewalOutcome.skip();
        }

        UserSubscription subscription = activeSubscription.get();
        PaymentOrder order = findOrCreateRenewalOrder(agreement, subscription);
        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return RenewalOutcome.skip();
        }

        LocalDate graceEndsAt = graceEndsAt(order);
        if (today.isAfter(graceEndsAt)) {
            finalizeRenewalFailure(agreement, subscription, graceEndsAt, today);
            return RenewalOutcome.failedWithoutAttempt();
        }

        BillingChargeResult chargeResult = recurringProvider().charge(new BillingChargeCommand(
                billingKeyCrypto.decrypt(agreement.getBillingKeyCiphertext()),
                agreement.getProviderCustomerKey(),
                order.getOrderId(),
                orderName(order),
                order.getAmount(),
                agreement.getUser().getEmail(),
                agreement.getUser().getNickname(),
                idempotencyKey(order, agreement)
        ));

        if (chargeResult.success()) {
            applySuccessfulRenewal(order, agreement, subscription, chargeResult);
            return RenewalOutcome.success();
        }

        applyFailedRenewal(order, agreement, subscription, chargeResult, graceEndsAt, today);
        return RenewalOutcome.failure();
    }

    private PaymentOrder findOrCreateRenewalOrder(
            BillingAgreement agreement,
            UserSubscription subscription) {
        Optional<PaymentOrder> existingOrder = paymentOrderRepository
                .findFirstByBillingAgreementAndPurposeAndStatusInOrderByCreatedAtDesc(
                        agreement,
                        PaymentPurpose.RENEWAL,
                        RENEWAL_ORDER_STATUSES);
        if (existingOrder.isPresent() && shouldReuseRenewalOrder(existingOrder.get(), agreement)) {
            return existingOrder.get();
        }

        return paymentOrderRepository.save(PaymentOrder.builder()
                .orderId(renewalOrderId(agreement))
                .user(agreement.getUser())
                .purpose(PaymentPurpose.RENEWAL)
                .provider(RECURRING_PROVIDER)
                .subscription(renewalSubscription(subscription))
                .userSubscription(subscription)
                .billingAgreement(agreement)
                .billingCycle(renewalBillingCycle(subscription))
                .amount(priceFor(renewalSubscription(subscription), renewalBillingCycle(subscription)))
                .currency("KRW")
                .expiresAt(graceExpiresAt(agreement.getNextBillingAt()))
                .build());
    }

    private boolean shouldReuseRenewalOrder(PaymentOrder order, BillingAgreement agreement) {
        return order.getStatus() != PaymentOrderStatus.DONE
                || renewalPeriodStart(order).equals(agreement.getNextBillingAt());
    }

    private void applySuccessfulRenewal(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription currentSubscription,
            BillingChargeResult chargeResult) {
        LocalDate periodStart = renewalPeriodStart(order);
        LocalDate newExpiresAt = expiresAt(periodStart, order.getBillingCycle());
        currentSubscription.startNewSubscription(
                order.getSubscription(),
                order.getBillingCycle(),
                periodStart,
                newExpiresAt);
        subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                .paymentOrder(order)
                .billingAgreement(agreement)
                .provider(order.getProvider())
                .user(order.getUser())
                .userSubscription(currentSubscription)
                .subscription(order.getSubscription())
                .billingCycle(order.getBillingCycle())
                .amount(order.getAmount())
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId(chargeResult.transactionId())
                .build());
        order.markDone(chargeResult.transactionId(), currentSubscription, chargeResult.providerPayload());
        agreement.recordSuccessfulCharge(newExpiresAt);
    }

    private void applyFailedRenewal(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription subscription,
            BillingChargeResult chargeResult,
            LocalDate graceEndsAt,
            LocalDate today) {
        order.markFailed(chargeResult.failureCode(), chargeResult.failureMessage());
        LocalDate nextRetryAt = today.plusDays(1).isAfter(graceEndsAt)
                ? graceEndsAt
                : today.plusDays(1);
        agreement.recordFailedCharge(nextRetryAt);
        if (subscription.getExpiresAt().isBefore(graceEndsAt)) {
            subscription.adminUpdate(null, null, graceEndsAt);
        }
        if (agreement.getFailureCount() >= MAX_RETRY_COUNT || !today.isBefore(graceEndsAt)) {
            finalizeRenewalFailure(agreement, subscription, graceEndsAt, today);
        }
    }

    private void finalizeRenewalFailure(
            BillingAgreement agreement,
            UserSubscription subscription,
            LocalDate graceEndsAt,
            LocalDate today) {
        agreement.suspend();
        if (subscription.getExpiresAt().isBefore(graceEndsAt)) {
            subscription.adminUpdate(null, null, graceEndsAt);
        }
        if (today.isAfter(graceEndsAt)) {
            subscription.expire();
        }
    }

    private void suspendAgreementWithoutActiveSubscription(BillingAgreement agreement, LocalDate today) {
        agreement.suspend();
        userSubscriptionRepository.findByUser(agreement.getUser())
                .filter(subscription -> subscription.getExpiresAt().isBefore(today))
                .ifPresent(UserSubscription::expire);
    }

    private RecurringPaymentProvider recurringProvider() {
        RecurringPaymentProvider provider = recurringProviders.get(RECURRING_PROVIDER);
        if (provider == null) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED);
        }
        return provider;
    }

    private Subscription renewalSubscription(UserSubscription subscription) {
        return subscription.getPendingSubscription() == null
                ? subscription.getSubscription()
                : subscription.getPendingSubscription();
    }

    private BillingCycle renewalBillingCycle(UserSubscription subscription) {
        return subscription.getPendingBillingCycle() == null
                ? subscription.getBillingCycle()
                : subscription.getPendingBillingCycle();
    }

    private BigDecimal priceFor(Subscription subscription, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
    }

    private LocalDate expiresAt(LocalDate startedAt, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1)
                : startedAt.plusYears(1);
    }

    private LocalDateTime graceExpiresAt(LocalDate periodStart) {
        return periodStart.plusDays(GRACE_DAYS).atTime(LocalTime.MAX);
    }

    private LocalDate graceEndsAt(PaymentOrder order) {
        return order.getExpiresAt().toLocalDate();
    }

    private LocalDate renewalPeriodStart(PaymentOrder order) {
        return order.getExpiresAt().toLocalDate().minusDays(GRACE_DAYS);
    }

    private String renewalOrderId(BillingAgreement agreement) {
        return "ATS-REN-" + agreement.getNextBillingAt().toString().replace("-", "")
                + "-" + Integer.toUnsignedString(agreement.getProviderCustomerKey().hashCode()).toUpperCase();
    }

    private String idempotencyKey(PaymentOrder order, BillingAgreement agreement) {
        return "renewal-" + order.getOrderId() + "-attempt-" + (agreement.getFailureCount() + 1);
    }

    private String orderName(PaymentOrder order) {
        return order.getSubscription().getName() + " recurring renewal";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record RenewalRunResult(int attempted, int succeeded, int failed, int skipped) {
    }

    private record RenewalOutcome(int attempted, int succeeded, int failed, int skipped) {
        private static RenewalOutcome success() {
            return new RenewalOutcome(1, 1, 0, 0);
        }

        private static RenewalOutcome failure() {
            return new RenewalOutcome(1, 0, 1, 0);
        }

        private static RenewalOutcome failedWithoutAttempt() {
            return new RenewalOutcome(0, 0, 1, 0);
        }

        private static RenewalOutcome skip() {
            return new RenewalOutcome(0, 0, 0, 1);
        }
    }
}
