package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        BillingAgreementPrepareTransactionService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementPrepareIdempotencyIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Billing agreement prepare idempotency integration tests (supplemental H2)")
class BillingAgreementPrepareIdempotencyIntegrationTest {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174000";
    private static final String FRESH_PREPARE_KEY = "223e4567-e89b-42d3-a456-426614174000";

    @Autowired BillingAgreementApplicationService service;
    @Autowired BillingAgreementPrepareTransactionService prepareTransactions;
    @Autowired PaymentCommandKeyFactory commandKeyFactory;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired TestPrepareProvider provider;

    @MockitoBean BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean PaymentCommandTransactionService paymentCommandTransactionService;
    @MockitoBean BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    @MockitoBean BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean PlaylistService playlistService;
    @MockitoBean EmailService emailService;

    private final AtomicInteger customerSequence = new AtomicInteger();

    @BeforeEach
    void setUp() {
        provider.reset();
        given(billingCustomerKeyGenerator.generate())
                .willAnswer(ignored -> "ats_prepare_customer_" + customerSequence.incrementAndGet());
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
    @DisplayName("invalid key fails before durable state or Provider interaction")
    void invalidKeyFailsBeforeEffects() {
        assertThatThrownBy(() -> service.prepareBillingAgreement(
                userDetails(999L),
                request(999L, BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                "not-a-uuid"))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_PREPARE_IDEMPOTENCY_KEY_INVALID));

        assertThat(paymentOrderRepository.count()).isZero();
        assertThat(billingAgreementRepository.count()).isZero();
        assertThat(provider.calls()).isZero();
    }

    @Test
    @DisplayName("same key and exact tuple replay the same authoritative response")
    void exactReplayReturnsSameOrderAndResponse() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);

        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        BillingAgreementPrepareResponse replay = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);

        assertThat(replay).isEqualTo(first);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        PaymentOrder order = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(order.getCommandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY))
                .doesNotContain(PREPARE_KEY);
        assertThat(provider.transactionStates()).containsExactly(false, false);
    }

    @Test
    @DisplayName("unexpired READY claim is reusable and finalized without creating another order")
    void readyClaimIsReusable() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        String commandKey = commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY);
        Long agreementID = prepareTransactions.ensureAgreement(
                fixture.userID(),
                request(fixture.planID(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                LocalDateTime.now());

        assertThat(agreementID).isNotNull();
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isZero();

        BillingAgreementPrepareTransactionService.PrepareClaim claim = prepareTransactions.claim(
                fixture.userID(),
                request(fixture.planID(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                commandKey,
                java.time.LocalDateTime.now());

        BillingAgreementPrepareResponse response = prepare(
                fixture, PREPARE_KEY, BillingCycle.MONTHLY);

        assertThat(response.orderId()).isEqualTo(claim.orderID());
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.findByOrderId(response.orderId()).orElseThrow().getCommandKey())
                .isEqualTo(commandKey);
        assertThat(provider.transactionStates()).containsExactly(false);
    }

    @Test
    @DisplayName("same claimed key with changed plan or cycle returns stable conflict without Provider")
    void changedTupleConflictsWithoutProvider() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        Subscription otherPlan = persistPlan("Other", UserType.INDIVIDUAL);
        Subscription otherAudience = persistPlan("Business", UserType.BUSINESS);
        BillingAgreementPrepareResponse first = prepare(
                fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        PaymentOrder original = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        String originalCommandKey = original.getCommandKey();
        String originalPayload = original.getProviderPayload();
        PaymentOrderStatus originalStatus = original.getStatus();
        int callsAfterClaim = provider.calls();

        assertPrepareConflict(() -> service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                request(otherPlan.getId(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY));
        assertPrepareConflict(() -> service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                request(fixture.planID(), BillingCycle.YEARLY, PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY));
        assertPrepareConflict(() -> service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                request(fixture.planID(), BillingCycle.MONTHLY, PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY));
        assertPrepareConflict(() -> service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                request(otherAudience.getId(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY));

        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        PaymentOrder unchanged = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(unchanged.getCommandKey()).isEqualTo(originalCommandKey);
        assertThat(unchanged.getProviderPayload()).isEqualTo(originalPayload);
        assertThat(unchanged.getStatus()).isEqualTo(originalStatus);
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);
    }

    @Test
    @DisplayName("an unrelated eligibility error keeps its authoritative code for an exact claim")
    void claimedAttemptDoesNotTranslateEligibilityErrorToConflict() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(
                fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        User user = userRepository.findById(fixture.userID()).orElseThrow();
        ReflectionTestUtils.setField(user, "userType", UserType.BUSINESS);
        userRepository.saveAndFlush(user);
        int callsAfterClaim = provider.calls();

        assertPrepareError(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);

        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow().getCommandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY));
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);
    }

    @Test
    @DisplayName("expired claimed key returns expired and a fresh key preserves old history")
    void expiredAttemptRequiresFreshKeyWithoutChangingOldOrder() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        PaymentOrder order = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        String oldCommandKey = order.getCommandKey();
        LocalDateTime expiredAt = LocalDateTime.now().minusSeconds(1).withNano(0);
        ReflectionTestUtils.setField(order, "expiresAt", expiredAt);
        paymentOrderRepository.saveAndFlush(order);
        int callsAfterClaim = provider.calls();

        assertPrepareError(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                BUSINESS_ERROR.PAYMENT_ORDER_EXPIRED);
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);

        BillingAgreementPrepareResponse fresh = prepare(
                fixture, FRESH_PREPARE_KEY, BillingCycle.MONTHLY);

        PaymentOrder oldOrder = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(fresh.orderId()).isNotEqualTo(first.orderId());
        assertThat(paymentOrderRepository.count()).isEqualTo(2);
        assertThat(oldOrder.getCommandKey()).isEqualTo(oldCommandKey);
        assertThat(oldOrder.getExpiresAt()).isEqualTo(expiredAt);
        assertThat(oldOrder.getStatus()).isEqualTo(PaymentOrderStatus.IN_PROGRESS);
        assertThat(provider.calls()).isEqualTo(callsAfterClaim + 1);
    }

    @ParameterizedTest(name = "{0} prepare history is safely replaceable only with a fresh key")
    @EnumSource(value = PaymentOrderStatus.class, names = {"FAILED", "CANCELLED"})
    @DisplayName("safe terminal claimed key returns terminal and preserves old history")
    void terminalAttemptRequiresFreshKeyWithoutChangingOldOrder(PaymentOrderStatus terminalStatus) {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        PaymentOrder order = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        String oldCommandKey = order.getCommandKey();
        if (terminalStatus == PaymentOrderStatus.FAILED) {
            order.markFailed("DECLINED", "test terminal state");
        } else {
            order.markCancelled("test terminal state");
        }
        paymentOrderRepository.saveAndFlush(order);
        int callsAfterClaim = provider.calls();

        assertPrepareErrorName(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                "PAYMENT_ORDER_TERMINAL");
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);

        BillingAgreementPrepareResponse fresh = prepare(
                fixture, FRESH_PREPARE_KEY, BillingCycle.MONTHLY);

        PaymentOrder oldOrder = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(fresh.orderId()).isNotEqualTo(first.orderId());
        assertThat(paymentOrderRepository.count()).isEqualTo(2);
        assertThat(oldOrder.getCommandKey()).isEqualTo(oldCommandKey);
        assertThat(oldOrder.getStatus()).isEqualTo(terminalStatus);
        assertThat(provider.calls()).isEqualTo(callsAfterClaim + 1);
    }

    @ParameterizedTest(name = "{0} prepare history remains non-replaceable")
    @EnumSource(
            value = PaymentOrderStatus.class,
            names = {"PROCESSING", "PROVIDER_SUCCEEDED", "PENDING_PROVIDER_CONFIRMATION"})
    @DisplayName("in-flight claimed keys retain the authoritative invalid-state error")
    void inFlightAttemptRetainsInvalidState(PaymentOrderStatus inFlightStatus) {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        PaymentOrder order = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        moveToStatus(order, inFlightStatus);
        paymentOrderRepository.saveAndFlush(order);
        int callsAfterClaim = provider.calls();

        assertPrepareError(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);

        PaymentOrder unchanged = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(unchanged.getStatus()).isEqualTo(inFlightStatus);
        assertThat(unchanged.getCommandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY));
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);
    }

    @Test
    @DisplayName("DONE prepare history retains the authoritative invalid-state error")
    void doneAttemptRetainsInvalidState() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        PaymentOrder order = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        ReflectionTestUtils.setField(order, "status", PaymentOrderStatus.DONE);
        paymentOrderRepository.saveAndFlush(order);
        int callsAfterClaim = provider.calls();

        assertPrepareError(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);

        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);
    }

    @Test
    @DisplayName("an active agreement retains its authoritative non-replaceable error")
    void activeAgreementRetainsAuthoritativeError() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        BillingAgreementPrepareResponse first = prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
        BillingAgreement agreement = billingAgreementRepository.findAll().get(0);
        agreement.activate(
                "encrypted-key",
                "fingerprint",
                "CARD",
                "1234",
                java.time.LocalDate.now().plusMonths(1));
        billingAgreementRepository.saveAndFlush(agreement);
        int callsAfterClaim = provider.calls();

        assertPrepareError(
                () -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY),
                BUSINESS_ERROR.BILLING_AGREEMENT_ALREADY_ACTIVE);

        assertThat(paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow().getCommandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY));
        assertThat(provider.calls()).isEqualTo(callsAfterClaim);
    }

    @Test
    @DisplayName("legacy null command-key history is ignored and remains unchanged")
    void legacyNullCommandKeyIsIgnored() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        User user = userRepository.findById(fixture.userID()).orElseThrow();
        Subscription subscription = subscriptionRepository.findById(fixture.planID()).orElseThrow();
        BillingAgreement agreement = billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_legacy_prepare_customer")
                .build());
        PaymentOrder legacy = PaymentOrder.builder()
                .orderId("ATS-BILL-LEGACY-NULL")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS)
                .subscription(subscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(subscription.getPriceMonthly())
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        legacy.markInProgress("{\"phase\":\"legacy\"}");
        paymentOrderRepository.saveAndFlush(legacy);

        BillingAgreementPrepareResponse response = prepare(
                fixture, PREPARE_KEY, BillingCycle.MONTHLY);

        PaymentOrder unchangedLegacy = paymentOrderRepository
                .findByOrderId("ATS-BILL-LEGACY-NULL")
                .orElseThrow();
        assertThat(response.orderId()).isNotEqualTo(unchangedLegacy.getOrderId());
        assertThat(paymentOrderRepository.count()).isEqualTo(2);
        assertThat(unchangedLegacy.getCommandKey()).isNull();
        assertThat(unchangedLegacy.getProviderPayload()).isEqualTo("{\"phase\":\"legacy\"}");
        assertThat(unchangedLegacy.getStatus()).isEqualTo(PaymentOrderStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("same raw key under another owner cannot select the original order")
    void sameRawKeyIsOwnerScoped() {
        Fixture firstOwner = persistFixture(UserType.INDIVIDUAL);
        User secondUser = persistUser(UserType.INDIVIDUAL);

        BillingAgreementPrepareResponse first = prepare(firstOwner, PREPARE_KEY, BillingCycle.MONTHLY);
        BillingAgreementPrepareResponse second = service.prepareBillingAgreement(
                userDetails(secondUser.getId()),
                request(firstOwner.planID(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE),
                PREPARE_KEY);

        assertThat(second.orderId()).isNotEqualTo(first.orderId());
        assertThat(paymentOrderRepository.count()).isEqualTo(2);
        PaymentOrder firstOrder = paymentOrderRepository.findByOrderId(first.orderId()).orElseThrow();
        PaymentOrder secondOrder = paymentOrderRepository.findByOrderId(second.orderId()).orElseThrow();
        assertThat(firstOrder.getUser().getId()).isEqualTo(firstOwner.userID());
        assertThat(secondOrder.getUser().getId()).isEqualTo(secondUser.getId());
        assertThat(firstOrder.getCommandKey()).isNotEqualTo(secondOrder.getCommandKey());
        assertThat(provider.calls()).isEqualTo(2);
    }

    @Test
    @DisplayName("Provider without pure deterministic prepare capability fails closed before claim")
    void providerWithoutPrepareCapabilityFailsClosed() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        provider.attestsPurePrepare(false);

        assertThatThrownBy(() -> prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED));

        assertThat(paymentOrderRepository.count()).isZero();
        assertThat(billingAgreementRepository.count()).isZero();
        assertThat(provider.calls()).isZero();
    }

    @Test
    @DisplayName("PAYMENT_ORDER_TERMINAL is an HTTP 409 business error")
    void terminalErrorUsesHttpConflict() {
        assertThat(BUSINESS_ERROR.PAYMENT_ORDER_TERMINAL.getStatus().value()).isEqualTo(409);
    }

    @Test
    @DisplayName("concurrent same-key replay with an existing agreement converges to one order")
    void existingAgreementRaceConverges() throws Exception {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);
        User user = userRepository.findById(fixture.userID()).orElseThrow();
        billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("ats_existing_customer")
                .build());

        List<BillingAgreementPrepareResponse> responses = raceSamePrepare(fixture);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(1)).isEqualTo(responses.get(0));
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(provider.transactionStates()).containsOnly(false);
    }

    @Test
    @DisplayName("concurrent same-key first agreement race converges after winner commit")
    void firstAgreementRaceConverges() throws Exception {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);

        List<BillingAgreementPrepareResponse> responses = raceSamePrepare(fixture);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(1)).isEqualTo(responses.get(0));
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(provider.transactionStates()).containsOnly(false);
    }

    @Test
    @DisplayName("prepare Provider descriptor is invoked outside every local transaction")
    void providerPrepareRunsOutsideTransaction() {
        Fixture fixture = persistFixture(UserType.INDIVIDUAL);

        prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);

        assertThat(provider.transactionStates()).containsExactly(false);
    }

    private BillingAgreementPrepareResponse prepare(
            Fixture fixture,
            String key,
            BillingCycle billingCycle) {
        return service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                request(fixture.planID(), billingCycle, PaymentPurpose.SUBSCRIBE),
                key);
    }

    private void assertPrepareConflict(Runnable invocation) {
        assertPrepareError(invocation, BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
    }

    private void assertPrepareError(Runnable invocation, BUSINESS_ERROR expected) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(expected));
    }

    private void assertPrepareErrorName(Runnable invocation, String expectedName) {
        assertThatThrownBy(invocation::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode().name())
                        .isEqualTo(expectedName));
    }

    private void moveToStatus(PaymentOrder order, PaymentOrderStatus status) {
        order.claimProviderAttempt(
                order.getCommandKey(),
                "test-provider-attempt-1",
                LocalDateTime.now());
        if (status == PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            order.markProviderSucceeded("test-provider-transaction", "{}");
        } else if (status == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION) {
            order.markProviderOutcomeUnknown("UNKNOWN", "test unknown outcome");
        }
    }

    private Fixture persistFixture(UserType userType) {
        User user = persistUser(userType);
        Subscription plan = persistPlan("Prepare", userType);
        return new Fixture(user.getId(), plan.getId());
    }

    private User persistUser(UserType userType) {
        return userRepository.saveAndFlush(User.builder()
                .nickname("prepare-user-" + customerSequence.incrementAndGet())
                .email("prepare-" + customerSequence.incrementAndGet() + "@example.invalid")
                .password("pw")
                .userType(userType)
                .role(UserRole.USER)
                .build());
    }

    private List<BillingAgreementPrepareResponse> raceSamePrepare(Fixture fixture) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<BillingAgreementPrepareResponse> first = executor.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                return prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
            });
            Future<BillingAgreementPrepareResponse> second = executor.submit(() -> {
                start.await(5, TimeUnit.SECONDS);
                return prepare(fixture, PREPARE_KEY, BillingCycle.MONTHLY);
            });
            start.countDown();
            return List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private Subscription persistPlan(String name, UserType userType) {
        return subscriptionRepository.saveAndFlush(Subscription.builder()
                .name(name + "-" + customerSequence.incrementAndGet())
                .description("Focused prepare test")
                .userType(userType)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
    }

    private BillingAgreementPrepareRequest request(
            Long planID,
            BillingCycle cycle,
            PaymentPurpose purpose) {
        return new BillingAgreementPrepareRequest(planID, cycle, purpose);
    }

    private CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("prepare@example.invalid")
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

        private final List<Boolean> transactionStates = new CopyOnWriteArrayList<>();
        private volatile boolean attestsPurePrepare = true;

        void reset() {
            transactionStates.clear();
            attestsPurePrepare = true;
        }

        void attestsPurePrepare(boolean value) {
            attestsPurePrepare = value;
        }

        int calls() {
            return transactionStates.size();
        }

        List<Boolean> transactionStates() {
            return List.copyOf(transactionStates);
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS;
        }

        @Override
        public boolean supportsPureDeterministicPrepare() {
            return attestsPurePrepare;
        }

        @Override
        public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
            transactionStates.add(TransactionSynchronizationManager.isActualTransactionActive());
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
            throw new UnsupportedOperationException("Confirm is outside this focused test.");
        }

        @Override
        public BillingChargeResult charge(BillingChargeCommand command) {
            throw new UnsupportedOperationException("Charge is outside this focused test.");
        }

        @Override
        public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
            throw new UnsupportedOperationException("Cancel is outside this focused test.");
        }
    }
}
