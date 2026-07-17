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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        assertThat(reloadSubscription(fixture.userSubscriptionID()).getSubscription().getId())
                .isEqualTo(fixture.currentSubscriptionID());
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
        assertThat(recurringPaymentProvider.lastChargeCommand().orderName())
                .isEqualTo("AT.M Premium Upgrade");

        service.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY));

        PaymentOrder finalized = reloadOrder();
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
