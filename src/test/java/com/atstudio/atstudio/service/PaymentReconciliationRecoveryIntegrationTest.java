package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        PaymentReconciliationTransactionService.class,
        PaymentReconciliationService.class,
        PaymentReconciliationIncidentService.class,
        PaymentOperationAuditLogService.class,
        PaymentReconciliationRecoveryIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Payment reconciliation finalize-only recovery integration tests")
class PaymentReconciliationRecoveryIntegrationTest {

    private static final BigDecimal MONTHLY_AMOUNT = BigDecimal.valueOf(9900);

    @Autowired PaymentReconciliationService service;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired PaymentReconciliationIncidentRepository incidentRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired PaymentCommandTransactionService paymentCommandTransactions;
    @Autowired TestPaymentProvider paymentProvider;
    @Autowired EntityManager entityManager;

    @MockitoBean PlaylistService playlistService;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean EmailService emailService;

    @BeforeEach
    void resetProvider() {
        paymentProvider.reset();
    }

    @AfterEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        incidentRepository.deleteAll();
        subscriptionPaymentRepository.deleteAll();
        paymentOrderRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("exact DONE evidence finalizes SUBSCRIBE UPGRADE and RENEWAL once with lookup outside a transaction")
    void exactDoneEvidence_finalizesAllPurposesWithoutCharge() {
        RecoveryFixture subscribe = persistSubscribeFixture();
        RecoveryFixture upgrade = persistUpgradeFixture();
        RecoveryFixture renewal = persistRenewalFixture();
        paymentProvider.respondExact(subscribe.order());
        paymentProvider.respondExact(upgrade.order());
        paymentProvider.respondExact(renewal.order());

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(3);
        assertThat(result.finalizedOrders()).isEqualTo(3);
        assertThat(result.issues()).isEmpty();
        assertThat(paymentProvider.lookupTransactionStates()).containsExactly(false, false, false);
        assertThat(paymentProvider.lookupCalls()).isEqualTo(3);
        assertThat(paymentProvider.chargeCalls()).isZero();

        entityManager.clear();
        assertThat(paymentOrderRepository.findAll())
                .extracting(PaymentOrder::getStatus)
                .containsOnly(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(3);
        assertThat(userSubscriptionRepository.findById(upgrade.userSubscriptionID()).orElseThrow()
                .getSubscription().getId()).isEqualTo(upgrade.targetSubscriptionID());
        assertThat(userSubscriptionRepository.findById(renewal.userSubscriptionID()).orElseThrow()
                .getExpiresAt()).isEqualTo(renewal.billingPeriodStart().plusMonths(1));
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(auditLogRepository.count()).isEqualTo(6);
    }

    @Test
    @DisplayName("amount mismatch is Incident-only and a later exact lookup finalizes and resolves both Incidents")
    void mismatchThenExactEvidence_convergesWithoutCharge() {
        RecoveryFixture renewal = persistRenewalFixture();
        paymentProvider.respond(renewal.order().getOrderId(), ProviderPaymentLookupResult.found(
                PaymentProviderType.TOSS_BILLING,
                renewal.order().getOrderId(),
                transactionID(renewal.order()),
                "DONE",
                renewal.order().getAmount().add(BigDecimal.ONE),
                "KRW",
                "{}"));

        PaymentReconciliationService.ProviderReconciliationResult mismatch = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(mismatch.amountMismatches()).isEqualTo(1);
        assertThat(paymentOrderRepository.findById(renewal.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.OPEN);

        paymentProvider.respondExact(renewal.order());
        PaymentReconciliationService.ProviderReconciliationResult recovered = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(recovered.finalizedOrders()).isEqualTo(1);
        assertThat(paymentOrderRepository.findById(renewal.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(incidentRepository.findAll())
                .hasSize(2)
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(paymentProvider.lookupTransactionStates()).containsOnly(false);
        assertThat(paymentProvider.chargeCalls()).isZero();
    }

    @Test
    @DisplayName("finalizer failure retains PROVIDER_SUCCEEDED and the next reconciliation remains finalize-only")
    void finalizerFailure_retainsProviderSuccessForFinalizeOnlyRetry() {
        RecoveryFixture renewal = persistRenewalFixture();
        paymentProvider.respondExact(renewal.order());
        doThrow(new IllegalStateException("forced local finalization failure"))
                .doNothing()
                .when(paymentReceiptEvidenceService)
                .publishSuccessfulChargeEvidence(
                        any(PaymentOrder.class),
                        any(com.atstudio.atstudio.entity.SubscriptionPayment.class),
                        any(String.class));

        PaymentReconciliationService.ProviderReconciliationResult failed = service.reconcileProviderLedger();

        entityManager.clear();
        PaymentOrder providerSucceeded = paymentOrderRepository.findById(renewal.order().getId()).orElseThrow();
        assertThat(failed.finalizedOrders()).isZero();
        assertThat(failed.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::failureMessage)
                .isEqualTo("IllegalStateException");
        assertThat(providerSucceeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(providerSucceeded.getPgTransactionId()).isEqualTo(transactionID(renewal.order()));
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.OPEN);

        PaymentReconciliationService.ProviderReconciliationResult recovered = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(recovered.finalizedOrders()).isEqualTo(1);
        assertThat(paymentOrderRepository.findById(renewal.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(paymentProvider.lookupCalls()).isEqualTo(2);
        assertThat(paymentProvider.lookupTransactionStates()).containsOnly(false);
        assertThat(paymentProvider.chargeCalls()).isZero();
    }

    @Test
    @DisplayName("stale PROCESSING is eligible for exact DONE recovery")
    void staleProcessing_isRecoverable() {
        RecoveryFixture stale = persistRenewalFixture(
                PaymentOrderStatus.PROCESSING,
                LocalDateTime.now().minusMinutes(16));
        paymentProvider.respondExact(stale.order());

        PaymentReconciliationService.ProviderReconciliationResult recovered = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(recovered.finalizedOrders()).isEqualTo(1);
        assertThat(paymentOrderRepository.findById(stale.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(paymentProvider.lookupTransactionStates()).containsExactly(false);
        assertThat(paymentProvider.chargeCalls()).isZero();
    }

    @Test
    @DisplayName("normal renewal finalizer winning after provider lookup still resolves reconciliation Incident")
    void normalRenewalFinalizerWinningAfterLookupConvergesAndResolvesIncident() {
        RecoveryFixture renewal = persistRenewalFixture(
                PaymentOrderStatus.PROCESSING,
                LocalDateTime.now().minusMinutes(16));
        paymentProvider.respondExact(renewal.order());
        paymentProvider.beforeLookupReturn(() -> {
            paymentCommandTransactions.recordProviderSuccess(
                    renewal.order().getBillingAgreement().getId(),
                    renewal.order().getOrderId(),
                    transactionID(renewal.order()),
                    "{\"paymentKey\":\"" + transactionID(renewal.order()) + "\"}",
                    null,
                    null);
            paymentCommandTransactions.finalizeRenewal(
                    renewal.order().getBillingAgreement().getId(),
                    renewal.order().getOrderId());
        });

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(result.finalizedOrders()).isEqualTo(1);
        assertThat(result.issues()).isEmpty();
        assertThat(paymentOrderRepository.findById(renewal.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(userSubscriptionRepository.findById(renewal.userSubscriptionID()).orElseThrow().getExpiresAt())
                .isEqualTo(renewal.billingPeriodStart().plusMonths(1));
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(paymentProvider.lookupCalls()).isEqualTo(1);
        assertThat(paymentProvider.chargeCalls()).isZero();
    }

    @Test
    @DisplayName("fresh PROCESSING remains owned by the live command")
    void freshProcessing_isNotLookedUpOrMutated() {
        RecoveryFixture fresh = persistRenewalFixture(
                PaymentOrderStatus.PROCESSING,
                LocalDateTime.now().minusMinutes(1));
        paymentProvider.respondExact(fresh.order());

        PaymentReconciliationService.ProviderReconciliationResult skipped = service.reconcileProviderLedger();

        entityManager.clear();
        assertThat(skipped.checkedOrders()).isZero();
        assertThat(paymentOrderRepository.findById(fresh.order().getId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.PROCESSING);
        assertThat(paymentProvider.lookupCalls()).isZero();
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(incidentRepository.count()).isZero();
        assertThat(paymentProvider.chargeCalls()).isZero();
    }

    private RecoveryFixture persistSubscribeFixture() {
        User user = persistUser("reconcile-subscribe");
        Subscription plan = persistPlan("Reconcile Subscribe", MONTHLY_AMOUNT);
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-reconcile-subscribe")
                .build();
        agreement.storeIssuedKey("encrypted-subscribe", "fingerprint-subscribe", "CARD", "****1111");
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder order = reconciliationOrder(
                "ORDER-RECON-SUBSCRIBE",
                "BILLING_CONFIRM:ORDER-RECON-SUBSCRIBE",
                user,
                plan,
                null,
                agreement,
                PaymentPurpose.SUBSCRIBE,
                BillingCycle.MONTHLY,
                null,
                null,
                MONTHLY_AMOUNT,
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                null);
        return new RecoveryFixture(order, null, null, null);
    }

    private RecoveryFixture persistUpgradeFixture() {
        User user = persistUser("reconcile-upgrade");
        Subscription currentPlan = persistPlan("Reconcile Upgrade Current", MONTHLY_AMOUNT);
        Subscription targetPlan = persistPlan("Reconcile Upgrade Target", BigDecimal.valueOf(19900));
        UserSubscription current = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(15))
                .expiresAt(LocalDate.now().plusDays(15))
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-reconcile-upgrade")
                .build();
        agreement.activate("encrypted-upgrade", "fingerprint-upgrade", "CARD", "****2222", current.getExpiresAt());
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder order = reconciliationOrder(
                "ORDER-RECON-UPGRADE",
                "UPGRADE:" + current.getId() + ":persisted-target",
                user,
                targetPlan,
                current,
                agreement,
                PaymentPurpose.UPGRADE,
                BillingCycle.MONTHLY,
                BillingCycle.MONTHLY,
                null,
                BigDecimal.valueOf(5000),
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                null);
        return new RecoveryFixture(order, current.getId(), targetPlan.getId(), null);
    }

    private RecoveryFixture persistRenewalFixture() {
        return persistRenewalFixture(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
    }

    private RecoveryFixture persistRenewalFixture(
            PaymentOrderStatus status,
            LocalDateTime processingStartedAt) {
        LocalDate periodStart = LocalDate.now();
        User user = persistUser("reconcile-renewal");
        Subscription plan = persistPlan("Reconcile Renewal", MONTHLY_AMOUNT);
        UserSubscription current = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(plan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(periodStart.minusMonths(1))
                .expiresAt(periodStart)
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-reconcile-renewal")
                .build();
        agreement.activate("encrypted-renewal", "fingerprint-renewal", "CARD", "****3333", periodStart);
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder order = reconciliationOrder(
                "ORDER-RECON-RENEWAL",
                "RENEWAL:%d:%d:%s".formatted(agreement.getId(), current.getId(), periodStart),
                user,
                plan,
                current,
                agreement,
                PaymentPurpose.RENEWAL,
                BillingCycle.MONTHLY,
                null,
                periodStart,
                MONTHLY_AMOUNT,
                status,
                processingStartedAt);
        return new RecoveryFixture(order, current.getId(), null, periodStart);
    }

    private PaymentOrder reconciliationOrder(
            String orderID,
            String commandKey,
            User user,
            Subscription plan,
            UserSubscription userSubscription,
            BillingAgreement agreement,
            PaymentPurpose purpose,
            BillingCycle billingCycle,
            BillingCycle upgradeTargetBillingCycle,
            LocalDate billingPeriodStart,
            BigDecimal amount,
            PaymentOrderStatus status,
            LocalDateTime processingStartedAt) {
        return paymentOrderRepository.saveAndFlush(PaymentOrder.builder()
                .orderId(orderID)
                .commandKey(commandKey)
                .user(user)
                .purpose(purpose)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(status)
                .subscription(plan)
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(billingCycle)
                .upgradeTargetBillingCycle(upgradeTargetBillingCycle)
                .billingPeriodStart(billingPeriodStart)
                .providerAttempt(1)
                .processingStartedAt(processingStartedAt)
                .amount(amount)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build());
    }

    private User persistUser(String label) {
        return userRepository.saveAndFlush(User.builder()
                .nickname(label)
                .email(label + "@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
    }

    private Subscription persistPlan(String name, BigDecimal monthlyAmount) {
        return subscriptionRepository.saveAndFlush(Subscription.builder()
                .name(name)
                .description("Reconciliation integration plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(monthlyAmount)
                .priceYearly(monthlyAmount.multiply(BigDecimal.TEN))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
    }

    private static String transactionID(PaymentOrder order) {
        return "tx-" + order.getOrderId();
    }

    record RecoveryFixture(
            PaymentOrder order,
            Long userSubscriptionID,
            Long targetSubscriptionID,
            LocalDate billingPeriodStart) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestPaymentProvider testPaymentProvider() {
            return new TestPaymentProvider();
        }
    }

    static class TestPaymentProvider implements PaymentStatusLookupProvider, RecurringPaymentProvider {

        private final Map<String, ProviderPaymentLookupResult> responses = new HashMap<>();
        private final List<Boolean> lookupTransactionStates = new ArrayList<>();
        private Runnable beforeLookupReturn;
        private int lookupCalls;
        private int chargeCalls;

        void reset() {
            responses.clear();
            lookupTransactionStates.clear();
            beforeLookupReturn = null;
            lookupCalls = 0;
            chargeCalls = 0;
        }

        void respondExact(PaymentOrder order) {
            respond(order.getOrderId(), ProviderPaymentLookupResult.found(
                    getProviderType(),
                    order.getOrderId(),
                    transactionID(order),
                    "DONE",
                    order.getAmount(),
                    order.getCurrency(),
                    "{\"paymentKey\":\"" + transactionID(order) + "\"}"));
        }

        void respond(String orderID, ProviderPaymentLookupResult result) {
            responses.put(orderID, result);
        }

        void beforeLookupReturn(Runnable callback) {
            beforeLookupReturn = callback;
        }

        int lookupCalls() {
            return lookupCalls;
        }

        int chargeCalls() {
            return chargeCalls;
        }

        List<Boolean> lookupTransactionStates() {
            return List.copyOf(lookupTransactionStates);
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS_BILLING;
        }

        @Override
        @Transactional(propagation = Propagation.NEVER)
        public ProviderPaymentLookupResult findPaymentByOrderId(String orderId) {
            lookupCalls++;
            lookupTransactionStates.add(TransactionSynchronizationManager.isActualTransactionActive());
            if (beforeLookupReturn != null) {
                Runnable callback = beforeLookupReturn;
                beforeLookupReturn = null;
                callback.run();
            }
            return responses.get(orderId);
        }

        @Override
        public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
            throw new UnsupportedOperationException("Agreement preparation is outside reconciliation tests.");
        }

        @Override
        public BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command) {
            throw new UnsupportedOperationException("Agreement confirmation is outside reconciliation tests.");
        }

        @Override
        public BillingChargeResult charge(BillingChargeCommand command) {
            chargeCalls++;
            throw new AssertionError("Reconciliation must never issue a provider charge.");
        }

        @Override
        public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
            throw new UnsupportedOperationException("Agreement cancellation is outside reconciliation tests.");
        }
    }
}
