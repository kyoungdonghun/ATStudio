package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingCustomerKeyGenerator;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementConfirmResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareResult;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        BillingAgreementPrepareTransactionService.class,
        PaymentCommandTransactionService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementPrepareToConfirmIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Billing agreement prepare-to-confirm command fence integration tests")
class BillingAgreementPrepareToConfirmIntegrationTest {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174000";
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(9900);

    @Autowired BillingAgreementApplicationService applicationService;
    @Autowired PaymentCommandTransactionService commandTransactions;
    @Autowired PaymentCommandKeyFactory commandKeyFactory;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired TestPrepareProvider provider;

    @MockitoBean BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    @MockitoBean BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean PaymentReconciliationIncidentService paymentReconciliationIncidentService;
    @MockitoBean PlaylistService playlistService;
    @MockitoBean EmailService emailService;

    private final AtomicInteger sequence = new AtomicInteger();

    @BeforeEach
    void setUp() {
        provider.reset();
        given(billingCustomerKeyGenerator.generate())
                .willAnswer(ignored -> "ats_confirm_customer_" + sequence.incrementAndGet());
    }

    @AfterEach
    void cleanDatabase() {
        paymentOrderRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("real prepare digest survives confirm claim while Provider attempt uses its own fence")
    void preparedDigestSurvivesConfirmClaim() {
        Fixture fixture = persistFixture();
        BillingAgreementPrepareResponse prepared = applicationService.prepareBillingAgreement(
                userDetails(fixture.userID()),
                new BillingAgreementPrepareRequest(
                        fixture.planID(),
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);
        PaymentOrder preparedOrder = paymentOrderRepository
                .findByOrderId(prepared.orderId())
                .orElseThrow();
        String prepareDigest = preparedOrder.getCommandKey();

        PaymentCommandTransactionService.BillingConfirmClaim claim =
                commandTransactions.claimBillingConfirm(
                        fixture.userID(),
                        prepared.orderId(),
                        prepared.checkout().customerKey(),
                        prepared.amount(),
                        LocalDateTime.now());

        PaymentOrder claimedOrder = paymentOrderRepository
                .findByOrderId(prepared.orderId())
                .orElseThrow();
        assertThat(claim.action())
                .isEqualTo(PaymentCommandTransactionService.BillingConfirmAction.CALL_PROVIDER);
        assertThat(claimedOrder.getCommandKey()).isEqualTo(prepareDigest);
        assertThat(claimedOrder.getCommandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY));
        assertThat(claim.providerIdempotencyKey())
                .isEqualTo(commandKeyFactory.billingInitialAttempt(prepared.orderId(), 1));
        assertThat(claimedOrder.getProviderIdempotencyKey())
                .isEqualTo(claim.providerIdempotencyKey());
        assertThat(claimedOrder.getProviderAttempt()).isEqualTo(1);
        assertThat(claimedOrder.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
        assertThat(provider.prepareCalls()).isEqualTo(1);
        assertThat(provider.externalEffectCalls()).isZero();
    }

    @Test
    @DisplayName("legacy null command key receives the existing confirm fallback once")
    void nullCommandKeyUsesBillingConfirmFallback() {
        Fixture fixture = persistFixture();
        User user = userRepository.findById(fixture.userID()).orElseThrow();
        Subscription subscription = subscriptionRepository.findById(fixture.planID()).orElseThrow();
        BillingAgreement agreement = billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_legacy_confirm_customer")
                .build());
        PaymentOrder legacyOrder = PaymentOrder.builder()
                .orderId("ATS-BILL-LEGACY-CONFIRM")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS)
                .subscription(subscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(AMOUNT)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        legacyOrder.markInProgress("{\"phase\":\"prepare\"}");
        paymentOrderRepository.saveAndFlush(legacyOrder);

        PaymentCommandTransactionService.BillingConfirmClaim claim =
                commandTransactions.claimBillingConfirm(
                        fixture.userID(),
                        legacyOrder.getOrderId(),
                        agreement.getProviderCustomerKey(),
                        AMOUNT,
                        LocalDateTime.now());

        PaymentOrder claimedOrder = paymentOrderRepository
                .findByOrderId(legacyOrder.getOrderId())
                .orElseThrow();
        assertThat(claimedOrder.getCommandKey())
                .isEqualTo(commandKeyFactory.billingConfirm(legacyOrder.getOrderId()));
        assertThat(claim.providerIdempotencyKey())
                .isEqualTo(commandKeyFactory.billingInitialAttempt(legacyOrder.getOrderId(), 1));
        assertThat(claimedOrder.getProviderIdempotencyKey())
                .isEqualTo(claim.providerIdempotencyKey());
        assertThat(provider.prepareCalls()).isZero();
        assertThat(provider.externalEffectCalls()).isZero();
    }

    @Test
    @DisplayName("same raw prepare UUID stays owner-scoped through confirm authorization")
    void sameRawKeyRemainsOwnerIsolatedAtConfirm() {
        Fixture firstOwner = persistFixture();
        Fixture secondOwner = persistFixture();
        BillingAgreementPrepareResponse first = applicationService.prepareBillingAgreement(
                userDetails(firstOwner.userID()),
                new BillingAgreementPrepareRequest(
                        firstOwner.planID(),
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);
        BillingAgreementPrepareResponse second = applicationService.prepareBillingAgreement(
                userDetails(secondOwner.userID()),
                new BillingAgreementPrepareRequest(
                        secondOwner.planID(),
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);

        PaymentOrder firstOrder = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        PaymentOrder secondOrder = paymentOrderRepository.findByOrderId(second.orderId()).orElseThrow();
        String firstCommandKey = firstOrder.getCommandKey();
        assertThat(first.orderId()).isNotEqualTo(second.orderId());
        assertThat(firstCommandKey).isNotEqualTo(secondOrder.getCommandKey());

        assertThatThrownBy(() -> commandTransactions.claimBillingConfirm(
                secondOwner.userID(),
                first.orderId(),
                first.checkout().customerKey(),
                first.amount(),
                LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));

        PaymentOrder unchangedFirst = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(unchangedFirst.getStatus()).isEqualTo(PaymentOrderStatus.IN_PROGRESS);
        assertThat(unchangedFirst.getCommandKey()).isEqualTo(firstCommandKey);
        assertThat(provider.prepareCalls()).isEqualTo(2);
        assertThat(provider.externalEffectCalls()).isZero();
    }

    private Fixture persistFixture() {
        User user = userRepository.saveAndFlush(User.builder()
                .nickname("confirm-user-" + sequence.incrementAndGet())
                .email("confirm-" + sequence.incrementAndGet() + "@example.invalid")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Confirm-" + sequence.incrementAndGet())
                .description("Focused prepare-to-confirm test")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(AMOUNT)
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        return new Fixture(user.getId(), subscription.getId());
    }

    private CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("confirm@example.invalid")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    private record Fixture(Long userID, Long planID) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestPrepareProvider testPrepareProvider() {
            return new TestPrepareProvider();
        }
    }

    static final class TestPrepareProvider implements RecurringPaymentProvider {

        private final AtomicInteger prepareCalls = new AtomicInteger();
        private final AtomicInteger externalEffectCalls = new AtomicInteger();

        void reset() {
            prepareCalls.set(0);
            externalEffectCalls.set(0);
        }

        int prepareCalls() {
            return prepareCalls.get();
        }

        int externalEffectCalls() {
            return externalEffectCalls.get();
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS;
        }

        @Override
        public boolean supportsPureDeterministicPrepare() {
            return true;
        }

        @Override
        public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            prepareCalls.incrementAndGet();
            return new BillingAgreementPrepareResult(
                    PaymentProviderType.TOSS,
                    "TOSS_BILLING_AUTH",
                    "{\"phase\":\"prepare\"}",
                    Map.of(
                            "clientKey", "test-client-key",
                            "customerKey", command.providerCustomerKey(),
                            "successUrl", "http://localhost/success",
                            "failUrl", "http://localhost/fail",
                            "method", "CARD"));
        }

        @Override
        public BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command) {
            externalEffectCalls.incrementAndGet();
            throw new AssertionError("Confirm Provider must not run in this focused test.");
        }

        @Override
        public BillingChargeResult charge(BillingChargeCommand command) {
            externalEffectCalls.incrementAndGet();
            throw new AssertionError("Charge Provider must not run in this focused test.");
        }

        @Override
        public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
            externalEffectCalls.incrementAndGet();
            throw new AssertionError("Cancel Provider must not run in this focused test.");
        }
    }
}
