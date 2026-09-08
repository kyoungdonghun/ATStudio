package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.subscription.ChangeSubscriptionRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        SubscriptionUpgradePaymentExecutor.class,
        UserSubscriptionService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Subscription upgrade payment command integration tests")
class SubscriptionUpgradeCommandIntegrationTest {

    @Autowired UserSubscriptionService service;
    @Autowired PaymentCommandTransactionService commandTransactions;
    @Autowired SubscriptionUpgradePaymentExecutor subscriptionUpgradePaymentExecutor;
    @Autowired com.atstudio.atstudio.repository.UserRepository userRepository;
    @Autowired com.atstudio.atstudio.repository.SubscriptionRepository subscriptionRepository;
    @Autowired com.atstudio.atstudio.repository.UserSubscriptionRepository userSubscriptionRepository;
    @Autowired com.atstudio.atstudio.repository.BillingAgreementRepository billingAgreementRepository;
    @Autowired com.atstudio.atstudio.repository.PaymentOrderRepository paymentOrderRepository;
    @Autowired com.atstudio.atstudio.repository.SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired BillingAgreementCommandIntegrationTestSupport.TestRecurringPaymentProvider recurringPaymentProvider;
    @Autowired EntityManager entityManager;
    @Autowired TransactionTemplate transactionTemplate;

    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean PaymentReconciliationIncidentService incidentService;
    @MockitoBean PlaylistService playlistService;

    @AfterEach
    void cleanDatabase() {
        subscriptionPaymentRepository.deleteAll();
        paymentOrderRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("upgrade executor rejects an active transaction instead of suspending it")
    void upgradeExecutorRejectsActiveTransaction() {
        recurringPaymentProvider.reset();
        PaymentCommandTransactionService.UpgradeClaim claim =
                new PaymentCommandTransactionService.UpgradeClaim(
                        PaymentCommandTransactionService.UpgradeAction.CALL_PROVIDER,
                        200L,
                        "ATS-UPG-TX-BOUNDARY",
                        "encrypted-key",
                        "ats_upgrade_customer",
                        "AT.M Premium Upgrade",
                        BigDecimal.valueOf(5000),
                        "upgrade@test.com",
                        "upgrade-user",
                        "subscription-upgrade-ATS-UPG-TX-BOUNDARY-attempt-1",
                        BillingCycle.MONTHLY);

        assertThatThrownBy(() -> transactionTemplate.execute(
                status -> subscriptionUpgradePaymentExecutor.charge(claim)))
                .isInstanceOf(IllegalTransactionStateException.class);
        assertThat(recurringPaymentProvider.calls()).isEmpty();
    }

    @Test
    @DisplayName("provider success is durable and retry finalizes upgrade without a second charge")
    void providerSuccessRetryFinalizesWithoutSecondCharge() {
        Fixture fixture = persistUpgradeFixture();
        BillingAgreement agreement = billingAgreementRepository.findAll().get(0);
        LocalDateTime prior = LocalDateTime.now().minusDays(1).withNano(0);
        ReflectionTestUtils.setField(agreement, "lastChargedAt", prior);
        billingAgreementRepository.saveAndFlush(agreement);
        doThrow(new IllegalStateException("forced upgrade finalization failure"))
                .doNothing()
                .when(paymentReceiptEvidenceService)
                .publishSuccessfulChargeEvidence(
                        any(PaymentOrder.class),
                        any(SubscriptionPayment.class),
                        any(String.class));

        assertThatThrownBy(() -> service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forced upgrade finalization failure");

        PaymentOrder providerSucceeded = reloadOrder();
        assertThat(providerSucceeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(providerSucceeded.getUpgradeTargetBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(providerSucceeded.getProviderIdempotencyKey())
                .isEqualTo("subscription-upgrade-" + providerSucceeded.getOrderId() + "-attempt-1");
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(billingAgreementRepository.findById(agreement.getId()).orElseThrow().getLastChargedAt())
                .isEqualTo(prior);
        assertThat(reloadSubscription(fixture.userSubscriptionID()).getSubscription().getId())
                .isEqualTo(fixture.currentSubscriptionID());
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
        assertThat(recurringPaymentProvider.lastChargeCommand().orderName())
                .isEqualTo("AT.M Premium Upgrade");

        service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY));

        PaymentOrder finalized = reloadOrder();
        LocalDateTime chargedAt = billingAgreementRepository.findById(agreement.getId()).orElseThrow().getLastChargedAt();
        assertThat(chargedAt).isAfter(prior);
        commandTransactions.finalizeUpgrade(fixture.userID(), agreement.getId(), finalized.getOrderId());
        assertThat(billingAgreementRepository.findById(agreement.getId()).orElseThrow().getLastChargedAt())
                .isEqualTo(chargedAt);
        assertThat(billingAgreementRepository.findById(agreement.getId()).orElseThrow().getNextBillingAt())
                .isEqualTo(agreement.getNextBillingAt());
        assertThat(finalized.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(reloadSubscription(fixture.userSubscriptionID()).getSubscription().getId())
                .isEqualTo(fixture.targetSubscriptionID());
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
        verify(paymentReceiptEvidenceService, times(2)).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));
    }

    @Test
    @DisplayName("failed explicit retry reuses the command and increments the persisted attempt")
    void failedExplicitRetryReusesCommandAndIncrementsAttempt() {
        Fixture fixture = persistUpgradeFixture();
        recurringPaymentProvider.chargeResult(BillingChargeResult.failure("DECLINED", "Upgrade declined."));

        assertThatThrownBy(() -> service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED));

        PaymentOrder failed = reloadOrder();
        String orderID = failed.getOrderId();
        assertThat(failed.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(failed.getProviderAttempt()).isEqualTo(1);

        recurringPaymentProvider.chargeResult(BillingChargeResult.success(
                "tx_upgrade_retry",
                "CARD",
                "1234",
                "{\"paymentKey\":\"retry\"}"));
        service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY));

        PaymentOrder retried = reloadOrder();
        assertThat(retried.getOrderId()).isEqualTo(orderID);
        assertThat(retried.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(retried.getProviderAttempt()).isEqualTo(2);
        assertThat(retried.getProviderIdempotencyKey())
                .isEqualTo("subscription-upgrade-" + orderID + "-attempt-2");
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge", "charge");
    }

    @Test
    @DisplayName("ambiguous provider failure stays pending and is not charged again")
    void ambiguousProviderFailureStaysPendingWithoutBlindRetry() {
        Fixture fixture = persistUpgradeFixture();
        recurringPaymentProvider.chargeProbe(() -> {
            throw new IllegalStateException("simulated provider transport failure");
        });

        assertThatThrownBy(() -> service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED));

        PaymentOrder pending = reloadOrder();
        assertThat(pending.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");

        assertThatThrownBy(() -> service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @Test
    @DisplayName("concurrent duplicate while processing does not create another provider charge")
    void duplicateWhileProcessingDoesNotChargeAgain() throws Exception {
        Fixture fixture = persistUpgradeFixture();
        CountDownLatch providerEntered = new CountDownLatch(1);
        CountDownLatch releaseProvider = new CountDownLatch(1);
        recurringPaymentProvider.chargeProbe(() -> {
            providerEntered.countDown();
            try {
                releaseProvider.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Void> first = CompletableFuture.runAsync(() -> service.changeSubscription(
                    userDetails(fixture.userID()),
                    new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)), executor);
            assertThat(providerEntered.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> service.changeSubscription(
                    userDetails(fixture.userID()),
                    new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

            releaseProvider.countDown();
            first.get(5, TimeUnit.SECONDS);
        } finally {
            releaseProvider.countDown();
            executor.shutdownNow();
        }

        PaymentOrder finalized = reloadOrder();
        assertThat(finalized.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void distinctCycleOrTargetCannotChargeWhileFirstIntentIsOutsideTransaction(boolean differentTarget) throws Exception {
        Fixture fixture = persistUpgradeFixture();
        Long competingTarget = differentTarget ? persistHigherPlan().getId() : fixture.targetSubscriptionID();
        CountDownLatch providerEntered = new CountDownLatch(1);
        CountDownLatch releaseProvider = new CountDownLatch(1);
        AtomicInteger providerEntries = new AtomicInteger();
        recurringPaymentProvider.chargeResponder(command -> BillingChargeResult.success(
                "tx_distinct_" + command.orderId(), "CARD", "1234", "{}"));
        recurringPaymentProvider.chargeProbe(() -> {
            if (providerEntries.incrementAndGet() == 1) {
                providerEntered.countDown();
                try {
                    if (!releaseProvider.await(10, TimeUnit.SECONDS)) {
                        throw new AssertionError("Timed out waiting for the competing request.");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
            }
        });
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Void> first = CompletableFuture.runAsync(() -> service.changeSubscription(
                    userDetails(fixture.userID()),
                    new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)), executor);
            assertThat(providerEntered.await(10, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                    new ChangeSubscriptionRequest(competingTarget, BillingCycle.YEARLY)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));
            releaseProvider.countDown();
            first.get(10, TimeUnit.SECONDS);
        } finally {
            releaseProvider.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(providerEntries).hasValue(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(reloadOrder().getPgTransactionId()).isEqualTo("tx_distinct_" + reloadOrder().getOrderId());
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unknownIntentFencesOtherCyclesAndTargetsUntilReconciled(boolean differentTarget) {
        Fixture fixture = persistUpgradeFixture();
        Long competingTarget = differentTarget ? persistHigherPlan().getId() : fixture.targetSubscriptionID();
        recurringPaymentProvider.chargeResult(null);
        assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class);
        PaymentOrder unknown = reloadOrder();
        assertThat(unknown.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(competingTarget, BillingCycle.YEARLY)))
                .isInstanceOf(BusinessException.class);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);

        Long agreementID = billingAgreementRepository.findAll().get(0).getId();
        commandTransactions.recordProviderSuccessFromReconciliation(agreementID, unknown.getOrderId(),
                "tx_unknown_distinct", "{}", LocalDateTime.now().minusMinutes(15));
        commandTransactions.finalizeUpgrade(fixture.userID(), agreementID, unknown.getOrderId());
        commandTransactions.finalizeUpgrade(fixture.userID(), agreementID, unknown.getOrderId());
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @Test
    void durableSuccessFencesCompetingIntentAndRecoversWithOriginalCurrentPeriodPrice() {
        Fixture fixture = persistUpgradeFixture();
        transactionTemplate.executeWithoutResult(ignored -> {
            UserSubscription source = userSubscriptionRepository.findByIdForUpdate(fixture.userSubscriptionID()).orElseThrow();
            source.startNewSubscription(source.getSubscription(), BillingCycle.YEARLY,
                    LocalDate.now().minusMonths(6), LocalDate.now().plusMonths(6));
        });
        UserSubscription before = reloadSubscription(fixture.userSubscriptionID());
        doThrow(new IllegalStateException("synthetic finalize rollback")).doNothing()
                .when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                        any(PaymentOrder.class), any(SubscriptionPayment.class), any(String.class));
        assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(IllegalStateException.class);
        PaymentOrder succeeded = reloadOrder();
        assertThat(succeeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(succeeded.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.YEARLY)))
                .isInstanceOf(BusinessException.class);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);

        service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY));
        UserSubscription finalized = reloadSubscription(fixture.userSubscriptionID());
        assertThat(finalized.getStartedAt()).isEqualTo(before.getStartedAt());
        assertThat(finalized.getExpiresAt()).isEqualTo(before.getExpiresAt());
        assertThat(finalized.getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(finalized.getPendingBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(finalized.getPendingSubscription().getId()).isEqualTo(fixture.targetSubscriptionID());
        assertThat(subscriptionPaymentRepository.findByPaymentOrder(reloadOrder()).orElseThrow().getAmount())
                .isEqualByComparingTo(succeeded.getAmount());
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PLAN", "PERIOD", "CYCLE", "PRICE", "STATUS"})
    void providerSuccessCannotFinalizeAgainstChangedPricedSource(String change) {
        Fixture fixture = persistUpgradeFixture();
        Long otherPlanID = persistHigherPlan().getId();
        recurringPaymentProvider.chargeProbe(() -> transactionTemplate.executeWithoutResult(ignored -> {
            UserSubscription source = userSubscriptionRepository.findByIdForUpdate(fixture.userSubscriptionID()).orElseThrow();
            switch (change) {
                case "PLAN" -> source.upgradeKeepingPeriod(subscriptionRepository.findById(otherPlanID).orElseThrow(), null);
                case "PERIOD" -> source.adminUpdate(null, null, source.getExpiresAt().plusDays(1));
                case "CYCLE" -> source.adminUpdate(null, BillingCycle.YEARLY, null);
                case "PRICE" -> ReflectionTestUtils.setField(source.getSubscription(), "priceMonthly", BigDecimal.valueOf(10000));
                case "STATUS" -> source.cancel();
                default -> throw new AssertionError(change);
            }
        }));
        assertThatThrownBy(() -> service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class);
        PaymentOrder succeeded = reloadOrder();
        assertThat(succeeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        Long agreementID = billingAgreementRepository.findAll().get(0).getId();
        assertThatThrownBy(() -> commandTransactions.finalizeUpgrade(fixture.userID(), agreementID, succeeded.getOrderId()))
                .isInstanceOf(BusinessException.class);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @Test
    void completedReplayRemainsReadOnlyAfterLaterLifecycleChange() {
        Fixture fixture = persistUpgradeFixture();
        service.changeSubscription(userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY));
        PaymentOrder done = reloadOrder();
        ReflectionTestUtils.setField(done, "commandKey", done.getCommandKey().split(":SOURCE:")[0]);
        paymentOrderRepository.saveAndFlush(done);
        transactionTemplate.executeWithoutResult(ignored -> userSubscriptionRepository
                .findByIdForUpdate(fixture.userSubscriptionID()).orElseThrow().expire());
        Long agreementID = billingAgreementRepository.findAll().get(0).getId();
        commandTransactions.finalizeUpgrade(fixture.userID(), agreementID, done.getOrderId());
        assertThat(reloadSubscription(fixture.userSubscriptionID()).getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @ParameterizedTest
    @EnumSource(value = PaymentOrderStatus.class,
            names = {"READY", "IN_PROGRESS", "PROCESSING", "PROVIDER_SUCCEEDED", "PENDING_PROVIDER_CONFIRMATION"})
    void legacyUnboundUnfinishedUpgradeIsNotRetrofittedOrRecharged(PaymentOrderStatus status) {
        Fixture fixture = persistUpgradeFixture();
        commandTransactions.claimUpgrade(fixture.userID(), fixture.userSubscriptionID(),
                fixture.targetSubscriptionID(), BillingCycle.MONTHLY, LocalDateTime.now());
        PaymentOrder legacy = reloadOrder();
        String originalKey = legacy.getCommandKey().split(":SOURCE:")[0];
        ReflectionTestUtils.setField(legacy, "commandKey", originalKey);
        ReflectionTestUtils.setField(legacy, "status", status);
        ReflectionTestUtils.setField(legacy, "pgTransactionId", "tx_legacy_unresolved");
        paymentOrderRepository.saveAndFlush(legacy);
        Long agreementID = billingAgreementRepository.findAll().get(0).getId();

        assertThatThrownBy(() -> commandTransactions.claimUpgrade(fixture.userID(), fixture.userSubscriptionID(),
                fixture.targetSubscriptionID(), BillingCycle.MONTHLY, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> commandTransactions.claimUpgrade(fixture.userID(), fixture.userSubscriptionID(),
                fixture.targetSubscriptionID(), BillingCycle.YEARLY, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> commandTransactions.finalizeUpgrade(fixture.userID(), agreementID, legacy.getOrderId()))
                .isInstanceOf(BusinessException.class);
        assertThat(reloadOrder().getCommandKey()).isEqualTo(originalKey);
        assertThat(reloadOrder().getStatus()).isEqualTo(status);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(recurringPaymentProvider.calls()).isEmpty();
    }

    private Subscription persistHigherPlan() {
        return subscriptionRepository.saveAndFlush(Subscription.builder().name("Deluxe")
                .description("Distinct competing upgrade target").userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(29900)).priceYearly(BigDecimal.valueOf(299000))
                .downloadPerDay(30).maxWhitelistChannels(7).maxPlaylists(15).build());
    }

    private Fixture persistUpgradeFixture() {
        recurringPaymentProvider.reset();
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        doNothing().when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));

        User user = userRepository.saveAndFlush(User.builder()
                .nickname("upgrade-user")
                .email("upgrade@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        Subscription currentPlan = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Basic")
                .description("Current plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        Subscription targetPlan = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Premium")
                .description("Target plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(19900))
                .priceYearly(BigDecimal.valueOf(199000))
                .downloadPerDay(20)
                .maxWhitelistChannels(5)
                .maxPlaylists(10)
                .build());
        UserSubscription subscription = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(15))
                .expiresAt(LocalDate.now().plusDays(15))
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_upgrade_customer")
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", subscription.getExpiresAt());
        billingAgreementRepository.saveAndFlush(agreement);
        return new Fixture(
                user.getId(),
                subscription.getId(),
                currentPlan.getId(),
                targetPlan.getId());
    }

    private PaymentOrder reloadOrder() {
        entityManager.clear();
        return paymentOrderRepository.findAll().get(0);
    }

    private UserSubscription reloadSubscription(Long subscriptionID) {
        entityManager.clear();
        return userSubscriptionRepository.findById(subscriptionID).orElseThrow();
    }

    private CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("upgrade@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    record Fixture(
            Long userID,
            Long userSubscriptionID,
            Long currentSubscriptionID,
            Long targetSubscriptionID) {
    }
}
