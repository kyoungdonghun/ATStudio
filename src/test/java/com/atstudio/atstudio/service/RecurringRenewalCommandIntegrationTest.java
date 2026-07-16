package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        RecurringRenewalService.class,
        RecurringRenewalCommandIntegrationTest.PaymentConfiguration.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Recurring renewal payment command integration tests")
class RecurringRenewalCommandIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class PaymentConfiguration {

        @Bean
        PaymentProperties paymentProperties() {
            PaymentProperties properties = new PaymentProperties();
            properties.setSchedulerZone("Asia/Seoul");
            return properties;
        }
    }

    @Autowired RecurringRenewalService service;
    @Autowired com.atstudio.atstudio.repository.UserRepository userRepository;
    @Autowired com.atstudio.atstudio.repository.SubscriptionRepository subscriptionRepository;
    @Autowired com.atstudio.atstudio.repository.UserSubscriptionRepository userSubscriptionRepository;
    @Autowired com.atstudio.atstudio.repository.BillingAgreementRepository billingAgreementRepository;
    @Autowired com.atstudio.atstudio.repository.PaymentOrderRepository paymentOrderRepository;
    @Autowired com.atstudio.atstudio.repository.SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired BillingAgreementCommandIntegrationTestSupport.TestRecurringPaymentProvider recurringPaymentProvider;
    @Autowired EntityManager entityManager;

    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean EmailService emailService;
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
    @DisplayName("old renewal period order is never reused for the current agreement/subscription period")
    void oldPeriodOrderIsNotReused() {
        LocalDate currentDue = LocalDate.of(2026, 8, 17);
        Fixture fixture = persistRenewalFixture("old-period", currentDue);
        PaymentOrder oldOrder = paymentOrderRepository.saveAndFlush(PaymentOrder.builder()
                .orderId("OLD-REN")
                .commandKey("RENEWAL:%d:%d:%s".formatted(
                        fixture.agreementID(),
                        fixture.userSubscriptionID(),
                        currentDue.minusMonths(1)))
                .user(fixture.user())
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS_BILLING)
                .subscription(fixture.subscription())
                .userSubscription(fixture.userSubscription())
                .billingAgreement(fixture.agreement())
                .billingCycle(BillingCycle.MONTHLY)
                .billingPeriodStart(currentDue.minusMonths(1))
                .amount(BigDecimal.valueOf(9900))
                .currency("KRW")
                .expiresAt(currentDue.minusMonths(1).plusDays(3).atTime(LocalTime.MAX))
                .build());
        oldOrder.markFailed("DECLINED", "Old period failure.");
        paymentOrderRepository.saveAndFlush(oldOrder);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(currentDue);

        assertThat(result.succeeded()).isEqualTo(1);
        entityManager.clear();
        List<PaymentOrder> orders = paymentOrderRepository.findAll();
        assertThat(orders).hasSize(2);
        PaymentOrder currentOrder = orders.stream()
                .filter(order -> !"OLD-REN".equals(order.getOrderId()))
                .findFirst()
                .orElseThrow();
        assertThat(paymentOrderRepository.findByOrderId("OLD-REN").orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(currentOrder.getBillingPeriodStart()).isEqualTo(currentDue);
        assertThat(currentOrder.getCommandKey()).isEqualTo("RENEWAL:%d:%d:%s".formatted(
                fixture.agreementID(),
                fixture.userSubscriptionID(),
                currentDue));
        assertThat(currentOrder.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(currentOrder.getProviderAttempt()).isEqualTo(1);
    }

    @Test
    @DisplayName("first agreement success stays committed when a later agreement fails")
    void firstAgreementCommitSurvivesLaterAgreementFailure() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        Fixture first = persistRenewalFixture("batch-a", due);
        Fixture second = persistRenewalFixture("batch-b", due);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        recurringPaymentProvider.chargeResults(
                BillingChargeResult.success("tx_first", "CARD", "1234", "{\"paymentKey\":\"first\"}"),
                BillingChargeResult.failure("DECLINED", "Second renewal declined."));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(1);
        entityManager.clear();

        PaymentOrder firstOrder = renewalOrderFor(first.agreementID());
        PaymentOrder secondOrder = renewalOrderFor(second.agreementID());
        assertThat(firstOrder.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(secondOrder.getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.findByPaymentOrder(firstOrder)).isPresent();
        assertThat(subscriptionPaymentRepository.findByPaymentOrder(secondOrder)).isEmpty();
        assertThat(billingAgreementRepository.findById(first.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due.plusMonths(1));
        assertThat(billingAgreementRepository.findById(second.agreementID()).orElseThrow().getFailureCount())
                .isEqualTo(1);
        assertThat(billingAgreementRepository.findById(second.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due);
        assertThat(billingAgreementRepository.findById(second.agreementID()).orElseThrow().getRenewalRetryAt())
                .isEqualTo(due.plusDays(1));
    }

    @Test
    @DisplayName("a next-day deterministic retry reuses the period order and advances only attempt identity")
    void deterministicRetryReusesPeriodOrderAndCommand() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        Fixture fixture = persistRenewalFixture("two-day-retry", due);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        recurringPaymentProvider.chargeResults(
                BillingChargeResult.failure("DECLINED", "First renewal attempt declined."),
                BillingChargeResult.success("tx_retry_success", "CARD", "1234", "{}"));

        RecurringRenewalService.RenewalRunResult first = service.processDueRenewals(due);

        assertThat(first.failed()).isEqualTo(1);
        entityManager.clear();
        PaymentOrder firstAttempt = renewalOrderFor(fixture.agreementID());
        Long retainedOrderID = firstAttempt.getId();
        String retainedOrderReference = firstAttempt.getOrderId();
        String retainedCommandKey = firstAttempt.getCommandKey();
        String firstAttemptKey = firstAttempt.getProviderIdempotencyKey();
        assertThat(firstAttempt.getBillingPeriodStart()).isEqualTo(due);
        assertThat(firstAttempt.getProviderAttempt()).isEqualTo(1);
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due);
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getRenewalRetryAt())
                .isEqualTo(due.plusDays(1));

        RecurringRenewalService.RenewalRunResult second = service.processDueRenewals(due.plusDays(1));

        assertThat(second.succeeded()).isEqualTo(1);
        entityManager.clear();
        PaymentOrder retried = renewalOrderFor(fixture.agreementID());
        assertThat(retried.getId()).isEqualTo(retainedOrderID);
        assertThat(retried.getOrderId()).isEqualTo(retainedOrderReference);
        assertThat(retried.getCommandKey()).isEqualTo(retainedCommandKey);
        assertThat(retried.getBillingPeriodStart()).isEqualTo(due);
        assertThat(retried.getProviderAttempt()).isEqualTo(2);
        assertThat(retried.getProviderIdempotencyKey())
                .isNotEqualTo(firstAttemptKey)
                .endsWith("attempt-2");
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge", "charge");
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due.plusMonths(1));
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getRenewalRetryAt())
                .isNull();
    }

    @Test
    @DisplayName("an ambiguous renewal is not selected for a later automatic charge")
    void ambiguousRenewalIsNotSelectedForLaterCharge() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        Fixture fixture = persistRenewalFixture("ambiguous-retry", due);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        recurringPaymentProvider.chargeResults((BillingChargeResult) null);

        RecurringRenewalService.RenewalRunResult first = service.processDueRenewals(due);
        RecurringRenewalService.RenewalRunResult nextDay = service.processDueRenewals(due.plusDays(1));

        assertThat(first.failed()).isEqualTo(1);
        assertThat(nextDay).isEqualTo(new RecurringRenewalService.RenewalRunResult(0, 0, 0, 0));
        entityManager.clear();
        PaymentOrder pending = renewalOrderFor(fixture.agreementID());
        assertThat(pending.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getProviderAttempt()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge");
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getNextBillingAt())
                .isEqualTo(due);
        assertThat(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow().getRenewalRetryAt())
                .isNull();
    }

    @Test
    @DisplayName("an ambiguous next-day retry consumes the prior retry date and is not charged again")
    void ambiguousDeterministicRetryConsumesRetryDate() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        Fixture fixture = persistRenewalFixture("ambiguous-day-two", due);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        recurringPaymentProvider.chargeResults(
                BillingChargeResult.failure("DECLINED", "First renewal attempt declined."),
                null);

        RecurringRenewalService.RenewalRunResult first = service.processDueRenewals(due);
        RecurringRenewalService.RenewalRunResult retry = service.processDueRenewals(due.plusDays(1));
        RecurringRenewalService.RenewalRunResult later = service.processDueRenewals(due.plusDays(2));

        assertThat(first.failed()).isEqualTo(1);
        assertThat(retry.failed()).isEqualTo(1);
        assertThat(later).isEqualTo(new RecurringRenewalService.RenewalRunResult(0, 0, 0, 0));
        entityManager.clear();
        PaymentOrder pending = renewalOrderFor(fixture.agreementID());
        BillingAgreement agreement = billingAgreementRepository.findById(fixture.agreementID()).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getProviderAttempt()).isEqualTo(2);
        assertThat(agreement.getNextBillingAt()).isEqualTo(due);
        assertThat(agreement.getRenewalRetryAt()).isNull();
        assertThat(recurringPaymentProvider.calls()).containsExactly("charge", "charge");
    }

    @Test
    @DisplayName("null and blank-success provider results become pending and later agreements continue")
    void malformedProviderResultsBecomePendingAndBatchContinues() {
        LocalDate due = LocalDate.of(2026, 8, 17);
        Fixture nullResult = persistRenewalFixture("null-result", due);
        Fixture blankTransaction = persistRenewalFixture("blank-transaction", due);
        Fixture laterSuccess = persistRenewalFixture("later-success", due);
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        recurringPaymentProvider.chargeResults(
                null,
                BillingChargeResult.success(" ", "CARD", "1234", "{\"paymentKey\":\"blank\"}"),
                BillingChargeResult.success("tx_later_success", "CARD", "1234", "{\"paymentKey\":\"later\"}"));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(due);

        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(2);
        entityManager.clear();

        assertThat(renewalOrderFor(nullResult.agreementID()).getStatus())
                .isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(renewalOrderFor(blankTransaction.agreementID()).getStatus())
                .isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(renewalOrderFor(laterSuccess.agreementID()).getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.findByPaymentOrder(renewalOrderFor(laterSuccess.agreementID())))
                .isPresent();
    }

    private Fixture persistRenewalFixture(String label, LocalDate due) {
        recurringPaymentProvider.reset();
        User user = userRepository.saveAndFlush(User.builder()
                .nickname(label)
                .email(label + "@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Basic " + label)
                .description("Renewal integration plan")
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
        return new Fixture(user, subscription, userSubscription, agreement);
    }

    private PaymentOrder renewalOrderFor(Long agreementID) {
        return paymentOrderRepository.findAll().stream()
                .filter(order -> order.getCommandKey() != null)
                .filter(order -> order.getCommandKey().startsWith("RENEWAL:" + agreementID + ":"))
                .filter(order -> order.getPurpose() == PaymentPurpose.RENEWAL)
                .max(Comparator.comparing(PaymentOrder::getId))
                .orElseThrow();
    }

    record Fixture(
            User user,
            Subscription subscription,
            UserSubscription userSubscription,
            BillingAgreement agreement) {

        Long agreementID() {
            return agreement.getId();
        }

        Long userSubscriptionID() {
            return userSubscription.getId();
        }
    }
}
