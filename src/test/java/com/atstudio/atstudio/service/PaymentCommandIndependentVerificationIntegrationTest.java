package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
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
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementApplicationService.class,
        SubscriptionUpgradePaymentExecutor.class,
        UserSubscriptionService.class,
        RecurringRenewalService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("WI-018 payment command independent verification tests")
class PaymentCommandIndependentVerificationIntegrationTest
        extends BillingAgreementCommandIntegrationTestSupport {

    @Autowired UserSubscriptionService userSubscriptionService;
    @Autowired RecurringRenewalService recurringRenewalService;
    @Autowired PaymentCommandTransactionService paymentCommandTransactions;

    @Test
    @DisplayName("concurrent initial billing confirm converges to one provider charge and one committed payment")
    void concurrentInitialBillingConfirmConverges() throws Exception {
        Fixture fixture = persistPreparedOrder();
        BillingAgreementConfirmRequest request = new BillingAgreementConfirmRequest(
                ORDER_ID,
                "auth_key",
                CUSTOMER_KEY,
                AMOUNT);
        CountDownLatch providerEntered = new CountDownLatch(1);
        CountDownLatch releaseProvider = new CountDownLatch(1);
        recurringPaymentProvider.confirmProbe(() -> awaitProviderProbe(providerEntered, releaseProvider));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Void> first = CompletableFuture.runAsync(() -> service.confirmBillingAgreement(
                    userDetails(fixture.userID()),
                    request), executor);
            assertThat(providerEntered.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), request))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

            releaseProvider.countDown();
            first.get(5, TimeUnit.SECONDS);
        } finally {
            releaseProvider.countDown();
            executor.shutdownNow();
        }

        PaymentOrder order = reloadOrder();
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(order.getProviderAttempt()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(userSubscriptionRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge");
        assertThat(recurringPaymentProvider.lastChargeCommand().idempotencyKey())
                .isEqualTo("billing-initial-" + ORDER_ID + "-attempt-1");
    }

    @Test
    @DisplayName("ambiguous upgrade result is durable and retry refuses blind provider replay")
    void ambiguousUpgradeResultRefusesBlindReplay() {
        UpgradeFixture fixture = persistUpgradeFixture();
        recurringPaymentProvider.chargeResult(null);

        assertThatThrownBy(() -> userSubscriptionService.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED));

        PaymentOrder pending = reloadOnlyOrder();
        assertThat(pending.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getProviderAttempt()).isEqualTo(1);
        assertThat(pending.getUpgradeTargetBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");

        assertThatThrownBy(() -> userSubscriptionService.changeSubscription(
                userDetails(fixture.userID()),
                new ChangeSubscriptionRequest(fixture.targetSubscriptionID(), BillingCycle.MONTHLY)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @Test
    @DisplayName("renewal provider success plus local failure retries finalize-only without another charge")
    void renewalProviderSuccessRetryFinalizesOnly() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        RenewalFixture fixture = persistRenewalFixture("finalize-only", due);
        doThrow(new IllegalStateException("forced renewal evidence failure"))
                .doNothing()
                .when(paymentReceiptEvidenceService)
                .publishSuccessfulChargeEvidence(
                        any(PaymentOrder.class),
                        any(SubscriptionPayment.class),
                        any(String.class));

        RecurringRenewalService.RenewalRunResult first = recurringRenewalService.processDueRenewals(due);

        PaymentOrder providerSucceeded = renewalOrderFor(fixture.agreementID());
        assertThat(first.failed()).isEqualTo(1);
        assertThat(providerSucceeded.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");

        RecurringRenewalService.RenewalRunResult retry = recurringRenewalService.processDueRenewals(due);

        PaymentOrder finalized = renewalOrderFor(fixture.agreementID());
        assertThat(retry.succeeded()).isEqualTo(1);
        assertThat(finalized.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due.plusMonths(1));
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    @Test
    @DisplayName("concurrent renewal workers converge to one provider charge and one committed period")
    void concurrentRenewalWorkersConverge() throws Exception {
        LocalDate due = LocalDate.of(2026, 8, 17);
        RenewalFixture fixture = persistRenewalFixture("concurrent-renewal", due);
        CountDownLatch providerEntered = new CountDownLatch(1);
        CountDownLatch releaseProvider = new CountDownLatch(1);
        recurringPaymentProvider.chargeProbe(() -> awaitProviderProbe(providerEntered, releaseProvider));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<RecurringRenewalService.RenewalRunResult> first =
                    CompletableFuture.supplyAsync(() -> recurringRenewalService.processDueRenewals(due), executor);
            assertThat(providerEntered.await(5, TimeUnit.SECONDS)).isTrue();

            assertThatThrownBy(() -> paymentCommandTransactions.claimRenewal(
                    fixture.agreementID(),
                    due,
                    java.time.LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

            releaseProvider.countDown();
            RecurringRenewalService.RenewalRunResult completed = first.get(5, TimeUnit.SECONDS);

            assertThat(completed.succeeded()).isEqualTo(1);
        } finally {
            releaseProvider.countDown();
            executor.shutdownNow();
        }

        PaymentOrder order = renewalOrderFor(fixture.agreementID());
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(order.getProviderAttempt()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
    }

    private UpgradeFixture persistUpgradeFixture() {
        recurringPaymentProvider.reset();
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        doNothing().when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));

        User user = userRepository.saveAndFlush(User.builder()
                .nickname("wi018-upgrade-user")
                .email("wi018-upgrade@test.com")
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
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_upgrade_customer_wi018")
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", subscription.getExpiresAt());
        billingAgreementRepository.saveAndFlush(agreement);
        return new UpgradeFixture(user.getId(), targetPlan.getId());
    }

    private RenewalFixture persistRenewalFixture(String label, LocalDate due) {
        recurringPaymentProvider.reset();
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        doNothing().when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));

        User user = userRepository.saveAndFlush(User.builder()
                .nickname(label)
                .email(label + "@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Basic " + label)
                .description("Renewal plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        UserSubscription userSubscription = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(due.minusMonths(1))
                .expiresAt(due)
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-" + label)
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", due);
        billingAgreementRepository.saveAndFlush(agreement);
        return new RenewalFixture(agreement.getId());
    }

    private PaymentOrder reloadOnlyOrder() {
        entityManager.clear();
        return paymentOrderRepository.findAll().get(0);
    }

    private PaymentOrder renewalOrderFor(Long agreementID) {
        entityManager.clear();
        return paymentOrderRepository.findAll().stream()
                .filter(order -> order.getCommandKey() != null)
                .filter(order -> order.getCommandKey().startsWith("RENEWAL:" + agreementID + ":"))
                .findFirst()
                .orElseThrow();
    }

    private void awaitProviderProbe(CountDownLatch providerEntered, CountDownLatch releaseProvider) {
        providerEntered.countDown();
        try {
            releaseProvider.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private record UpgradeFixture(Long userID, Long targetSubscriptionID) {
    }

    private record RenewalFixture(Long agreementID) {
    }
}
