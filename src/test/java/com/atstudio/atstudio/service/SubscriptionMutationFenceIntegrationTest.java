package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.subscription.ChangeSubscriptionRequest;
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
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.session_factory.statement_inspector="
        + "com.atstudio.atstudio.service.SubscriptionMutationFenceIntegrationTest$UpdateInspector")
@Import({JpaConfig.class, PaymentCommandKeyFactory.class, PaymentCommandTransactionService.class,
        SubscriptionUpgradePaymentExecutor.class, UserSubscriptionService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class SubscriptionMutationFenceIntegrationTest {

    @Autowired UserSubscriptionService service;
    @Autowired PaymentCommandTransactionService commands;
    @Autowired SubscriptionUpgradePaymentExecutor providerExecutor;
    @Autowired UserRepository users;
    @MockitoSpyBean SubscriptionRepository plans;
    @MockitoSpyBean UserSubscriptionRepository subscriptions;
    @MockitoSpyBean BillingAgreementRepository agreements;
    @Autowired PaymentOrderRepository orders;
    @Autowired SubscriptionPaymentRepository payments;
    @Autowired TransactionTemplate transactions;
    @Autowired EntityManagerFactory entityManagerFactory;
    @Autowired BillingAgreementCommandIntegrationTestSupport.TestRecurringPaymentProvider provider;
    @MockitoBean BillingKeyCrypto crypto;
    @MockitoBean PaymentReceiptEvidenceService receipts;
    @MockitoBean PaymentReconciliationIncidentService incidents;
    @MockitoBean PlaylistService playlists;

    private final ThreadLocal<Boolean> mutationThread = ThreadLocal.withInitial(() -> false);
    private final AtomicReference<Gate> beforeMutationRead = new AtomicReference<>();
    private final AtomicReference<Gate> afterLockedSource = new AtomicReference<>();
    private final AtomicReference<CountDownLatch> finalizerLockAttempt = new AtomicReference<>();

    @BeforeEach
    void configureProbes() {
        provider.reset();
        given(crypto.decrypt("synthetic-cipher")).willReturn("billing_raw_key");
        UpdateInspector.updates.clear();
        Answer<?> agreementDelegate = mockingDetails(agreements).getMockCreationSettings().getDefaultAnswer();
        // Both entry points allow this same regression to exercise the original service on a red run.
        doAnswer(call -> atMutationBoundary(call, agreementDelegate)).when(agreements)
                .findByUserIDAndProviderForUpdate(anyLong(), eq(PaymentProviderType.TOSS));
        doAnswer(call -> atMutationBoundary(call, agreementDelegate)).when(agreements)
                .findByUserAndProvider(any(User.class), eq(PaymentProviderType.TOSS));
        doAnswer(call -> {
            CountDownLatch attempt = finalizerLockAttempt.get();
            if (!mutationThread.get() && attempt != null) {
                attempt.countDown();
            }
            return agreementDelegate.answer(call);
        }).when(agreements).findByIDForUpdate(anyLong());
        Answer<?> planDelegate = mockingDetails(plans).getMockCreationSettings().getDefaultAnswer();
        doAnswer(call -> atMutationBoundary(call, planDelegate)).when(plans).findById(anyLong());
        Answer<?> subscriptionDelegate = mockingDetails(subscriptions).getMockCreationSettings().getDefaultAnswer();
        doAnswer(call -> {
            Object result = subscriptionDelegate.answer(call);
            Gate gate = afterLockedSource.get();
            if (mutationThread.get() && gate != null) {
                gate.pause();
            }
            return result;
        }).when(subscriptions).findActiveByUserForUpdate(any(User.class), any(LocalDate.class));
    }

    @AfterEach
    void cleanIsolatedDatabase() {
        beforeMutationRead.set(null);
        afterLockedSource.set(null);
        finalizerLockAttempt.set(null);
        payments.deleteAll();
        orders.deleteAll();
        agreements.deleteAll();
        subscriptions.deleteAll();
        plans.deleteAll();
        users.deleteAll();
        UpdateInspector.phase.remove();
    }

    @Test
    void legacyUnlockedCancellationControlDemonstratesStaleFullRowOverwrite() throws Exception {
        Fixture fixture = preparedSuccess(false);
        interleaveBeforeUpgradeCommit(fixture, () -> transactions.executeWithoutResult(ignored -> {
            // Test-only control matching the old selfCancel read/mutation order, not a product bypass.
            User user = users.findById(fixture.userID()).orElseThrow();
            UserSubscription stale = subscriptions.findActiveByUser(user, LocalDate.now()).orElseThrow();
            stale.cancel();
            agreements.findByUserAndProvider(user, PaymentProviderType.TOSS).orElseThrow().cancel();
        }));
        Snapshot result = snapshot(fixture);
        assertThat(result.planID()).isEqualTo(fixture.basicID());
        assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertPaidOnce(fixture);
        assertThat(UpdateInspector.updates).extracting(Update::phase).containsExactly("UPGRADE", "MUTATION");
        assertThat(UpdateInspector.updates.get(1).sql()).contains("subscription_id", "billing_cycle", "expires_at");
        commands.finalizeUpgrade(fixture.userID(), fixture.agreementID(), fixture.orderID());
        assertThat(snapshot(fixture).planID()).isEqualTo(fixture.basicID());
    }

    @ParameterizedTest
    @EnumSource(Mutation.class)
    void actualMutationStartingBeforeFinalizationKeepsPaidTierAndRevalidatesCurrentState(Mutation mutation)
            throws Exception {
        Fixture fixture = preparedSuccess(mutation == Mutation.REACTIVATE);
        interleaveBeforeUpgradeCommit(fixture, () -> mutate(fixture, mutation));
        Snapshot current = snapshot(fixture);
        assertThat(current.planID()).isEqualTo(fixture.premiumID());
        assertThat(current.startedAt()).isEqualTo(fixture.startedAt());
        assertThat(current.expiresAt()).isEqualTo(fixture.expiresAt());
        assertThat(current.cycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(current.status()).isEqualTo(mutation == Mutation.CANCEL
                ? SubscriptionStatus.CANCELLED : SubscriptionStatus.ACTIVE);
        assertThat(current.agreementStatus()).isEqualTo(mutation == Mutation.CANCEL
                ? BillingAgreementStatus.CANCELLED : BillingAgreementStatus.ACTIVE);
        if (mutation == Mutation.CLEAR_PENDING) {
            assertThat(current.pendingPlanID()).isNull();
            assertThat(current.pendingCycle()).isNull();
        } else {
            assertThat(current.pendingPlanID()).isEqualTo(mutation == Mutation.PENDING_CYCLE
                    ? fixture.basicID() : fixture.premiumID());
            assertThat(current.pendingCycle()).isEqualTo(BillingCycle.YEARLY);
        }
        assertPaidOnce(fixture);
        commands.finalizeUpgrade(fixture.userID(), fixture.agreementID(), fixture.orderID());
        assertThat(snapshot(fixture)).isEqualTo(current);
        assertPaidOnce(fixture);
        assertThat(UpdateInspector.updates).extracting(Update::phase).startsWith("UPGRADE");
    }

    @Test
    void cancellationHoldingLocksRejectsUnresolvedIntentThenWaitingFinalizerCompletes() throws Exception {
        Fixture fixture = preparedSuccess(false);
        Gate locked = new Gate();
        afterLockedSource.set(locked);
        CountDownLatch finalizerAttempted = new CountDownLatch(1);
        finalizerLockAttempt.set(finalizerAttempted);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> cancel = executor.submit(() -> onMutationThread(() -> assertBusy(fixture, Mutation.CANCEL)));
            await(locked.entered);
            Future<?> finalize = executor.submit(() -> commands.finalizeUpgrade(
                    fixture.userID(), fixture.agreementID(), fixture.orderID()));
            await(finalizerAttempted);
            assertThat(finalize.isDone()).isFalse();
            locked.release.countDown();
            cancel.get(10, TimeUnit.SECONDS);
            finalize.get(10, TimeUnit.SECONDS);
        } finally {
            locked.release.countDown();
            stop(executor);
        }
        Snapshot current = snapshot(fixture);
        assertThat(current.planID()).isEqualTo(fixture.premiumID());
        assertThat(current.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(current.expiresAt()).isEqualTo(fixture.expiresAt());
        assertPaidOnce(fixture);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void heldProviderAndUnknownOutcomeKeepLocalMutationsBusyUntilDone(boolean unknown) throws Exception {
        Fixture fixture = subscriptionFixture(false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        provider.chargeProbe(() -> {
            entered.countDown();
            await(release);
        });
        if (unknown) {
            provider.chargeResult(null);
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> upgrade = executor.submit(() -> {
                Runnable action = () -> service.changeSubscription(actor(fixture),
                        new ChangeSubscriptionRequest(fixture.premiumID(), BillingCycle.YEARLY));
                if (unknown) {
                    assertThatThrownBy(action::run).isInstanceOf(BusinessException.class);
                } else {
                    action.run();
                }
            });
            await(entered);
            Snapshot before = snapshot(fixture);
            for (Mutation mutation : Mutation.values()) {
                assertBusy(fixture, mutation);
            }
            assertThat(snapshot(fixture)).isEqualTo(before);
            assertThat(orders.count()).isEqualTo(1);
            release.countDown();
            upgrade.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            stop(executor);
        }
        PaymentOrder order = orders.findAll().get(0);
        Fixture completed = fixture.withOrderID(order.getOrderId());
        if (unknown) {
            assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);
            for (Mutation mutation : Mutation.values()) {
                assertBusy(fixture, mutation);
            }
            commands.recordProviderSuccessFromReconciliation(fixture.agreementID(), order.getOrderId(),
                    "synthetic-mutation-recovered", "{}", LocalDateTime.now().minusMinutes(15));
            commands.finalizeUpgrade(fixture.userID(), fixture.agreementID(), order.getOrderId());
        }
        assertPaidOnce(completed);
        service.selfCancel(actor(fixture));
        Snapshot cancelled = snapshot(fixture);
        assertThat(cancelled.planID()).isEqualTo(fixture.premiumID());
        assertThat(cancelled.status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(cancelled.expiresAt()).isEqualTo(fixture.expiresAt());
        service.reactivate(actor(fixture));
        assertThat(snapshot(fixture).status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertPaidOnce(completed);
    }

    @Test
    void busyLocalCancellationDoesNotPreventExactUpgradeFinalizeOnlyRetry() {
        Fixture fixture = preparedSuccess(false);
        assertBusy(fixture, Mutation.CANCEL);
        service.changeSubscription(actor(fixture),
                new ChangeSubscriptionRequest(fixture.premiumID(), BillingCycle.YEARLY));
        assertPaidOnce(fixture);
        service.selfCancel(actor(fixture));
        assertThat(snapshot(fixture).status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(snapshot(fixture).planID()).isEqualTo(fixture.premiumID());
    }

    @Test
    void definitiveProviderFailureAllowsOrdinaryCancellationWithoutARefund() {
        Fixture fixture = subscriptionFixture(false);
        provider.chargeResult(BillingChargeResult.failure("SYNTHETIC_DECLINE", "Synthetic decline."));
        assertThatThrownBy(() -> service.changeSubscription(actor(fixture),
                new ChangeSubscriptionRequest(fixture.premiumID(), BillingCycle.YEARLY)))
                .isInstanceOf(BusinessException.class);
        assertThat(orders.findAll().get(0).getStatus()).isEqualTo(PaymentOrderStatus.FAILED);
        service.selfCancel(actor(fixture));
        assertThat(snapshot(fixture).status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(snapshot(fixture).expiresAt()).isEqualTo(fixture.expiresAt());
        assertThat(payments.count()).isZero();
        assertThat(provider.calls()).containsExactly("charge");
    }

    private void assertBusy(Fixture fixture, Mutation mutation) {
        assertThatThrownBy(() -> mutate(fixture, mutation)).isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));
    }

    private Object atMutationBoundary(InvocationOnMock call, Answer<?> delegate) throws Throwable {
        Gate gate = beforeMutationRead.get();
        if (mutationThread.get() && gate != null) {
            gate.pause();
        }
        return delegate.answer(call);
    }

    private void interleaveBeforeUpgradeCommit(Fixture fixture, Runnable mutation) throws Exception {
        Gate gate = new Gate();
        beforeMutationRead.set(gate);
        AtomicReference<Object> finalizationTransaction = new AtomicReference<>();
        List<String> commits = new CopyOnWriteArrayList<>();
        doAnswer(call -> {
            finalizationTransaction.set(TransactionSynchronizationManager.getResource(entityManagerFactory));
            return null;
        }).when(receipts).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class), any(SubscriptionPayment.class), any(String.class));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> delayedMutation = executor.submit(() -> onMutationThread(() -> {
                mutation.run();
                commits.add("MUTATION");
            }));
            await(gate.entered);
            UpdateInspector.phase.set("UPGRADE");
            commands.finalizeUpgrade(fixture.userID(), fixture.agreementID(), fixture.orderID());
            commits.add("UPGRADE");
            assertThat(gate.transaction.get()).isNotNull().isNotSameAs(finalizationTransaction.get());
            assertThat(finalizationTransaction.get()).isNotNull();
            gate.release.countDown();
            delayedMutation.get(10, TimeUnit.SECONDS);
        } finally {
            gate.release.countDown();
            UpdateInspector.phase.remove();
            stop(executor);
        }
        assertThat(commits).containsExactly("UPGRADE", "MUTATION");
    }

    private void onMutationThread(Runnable action) {
        mutationThread.set(true);
        UpdateInspector.phase.set("MUTATION");
        try {
            action.run();
        } finally {
            mutationThread.remove();
            UpdateInspector.phase.remove();
        }
    }

    private void mutate(Fixture fixture, Mutation mutation) {
        switch (mutation) {
            case CANCEL -> service.selfCancel(actor(fixture));
            case REACTIVATE -> service.reactivate(actor(fixture));
            case PENDING_CYCLE -> service.changeSubscription(actor(fixture),
                    new ChangeSubscriptionRequest(fixture.basicID(), BillingCycle.YEARLY));
            case CLEAR_PENDING -> service.changeSubscription(actor(fixture),
                    new ChangeSubscriptionRequest(fixture.premiumID(), BillingCycle.MONTHLY));
        }
    }

    private Fixture preparedSuccess(boolean cancelled) {
        Fixture fixture = subscriptionFixture(cancelled);
        PaymentCommandTransactionService.UpgradeClaim claim = commands.claimUpgrade(fixture.userID(), fixture.subscriptionID(),
                fixture.premiumID(), BillingCycle.YEARLY, LocalDateTime.now());
        BillingChargeResult charged = providerExecutor.charge(claim);
        commands.recordProviderSuccess(fixture.agreementID(), claim.orderID(), charged.transactionId(),
                charged.providerPayload(), charged.payMethod(), charged.maskedMethod());
        UpdateInspector.updates.clear();
        return fixture.withOrderID(claim.orderID());
    }

    private Fixture subscriptionFixture(boolean cancelled) {
        User user = users.saveAndFlush(User.builder().nickname("mutation-user").email("mutation@example.invalid")
                .password("synthetic-password").userType(UserType.INDIVIDUAL).role(UserRole.USER).build());
        Subscription basic = plan("Basic", 9900);
        Subscription premium = plan("Premium", 19900);
        LocalDate startedAt = LocalDate.now().minusDays(15);
        LocalDate expiresAt = LocalDate.now().plusDays(15);
        UserSubscription subscription = subscriptions.saveAndFlush(UserSubscription.builder()
                .user(user).subscription(basic).billingCycle(BillingCycle.MONTHLY)
                .status(cancelled ? SubscriptionStatus.CANCELLED : SubscriptionStatus.ACTIVE)
                .startedAt(startedAt).expiresAt(expiresAt).build());
        BillingAgreement agreement = BillingAgreement.builder().user(user).provider(PaymentProviderType.TOSS)
                .providerCustomerKey("synthetic-mutation-customer").build();
        agreement.activate("synthetic-cipher", "synthetic-fingerprint", "CARD", "1234", expiresAt);
        if (cancelled) {
            agreement.cancel();
        }
        agreements.saveAndFlush(agreement);
        return new Fixture(user.getId(), subscription.getId(), agreement.getId(), basic.getId(), premium.getId(),
                null, startedAt, expiresAt);
    }

    private Subscription plan(String name, long monthly) {
        return plans.saveAndFlush(Subscription.builder().name(name).description("WI011 F1 synthetic plan")
                .userType(UserType.INDIVIDUAL).priceMonthly(BigDecimal.valueOf(monthly))
                .priceYearly(BigDecimal.valueOf(monthly * 10)).downloadPerDay(10).maxWhitelistChannels(3)
                .maxPlaylists(5).build());
    }

    private Snapshot snapshot(Fixture fixture) {
        return transactions.execute(ignored -> {
            UserSubscription current = subscriptions.findById(fixture.subscriptionID()).orElseThrow();
            BillingAgreement agreement = agreements.findById(fixture.agreementID()).orElseThrow();
            return new Snapshot(current.getSubscription().getId(), current.getStatus(), current.getBillingCycle(),
                    current.getStartedAt(), current.getExpiresAt(), current.getPendingSubscription() == null
                    ? null : current.getPendingSubscription().getId(), current.getPendingBillingCycle(), agreement.getStatus());
        });
    }

    private void assertPaidOnce(Fixture fixture) {
        PaymentOrder order = orders.findByOrderId(fixture.orderID()).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(orders.count()).isEqualTo(1);
        assertThat(payments.count()).isEqualTo(1);
        SubscriptionPayment payment = payments.findByPaymentOrder(order).orElseThrow();
        assertThat(payment.getSubscription().getId()).isEqualTo(fixture.premiumID());
        assertThat(payment.getAmount()).isEqualByComparingTo(order.getAmount());
        assertThat(provider.calls()).containsExactly("charge");
    }

    private CustomUserDetails actor(Fixture fixture) {
        return CustomUserDetails.builder().id(fixture.userID()).email("mutation@example.invalid")
                .password("synthetic-password").role(UserRole.USER).isDeleted(false).isProfileComplete(true).build();
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Deterministic mutation interleaving timed out.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static void stop(ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }

    private final class Gate {
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final AtomicBoolean used = new AtomicBoolean();
        private final AtomicReference<Object> transaction = new AtomicReference<>();

        void pause() {
            if (used.compareAndSet(false, true)) {
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
                transaction.set(TransactionSynchronizationManager.getResource(entityManagerFactory));
                entered.countDown();
                await(release);
            }
        }
    }

    public static final class UpdateInspector implements StatementInspector {
        private static final long serialVersionUID = 1L;
        static final ThreadLocal<String> phase = new ThreadLocal<>();
        static final List<Update> updates = new CopyOnWriteArrayList<>();

        @Override
        public String inspect(String sql) {
            String normalized = sql.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
            if (phase.get() != null && normalized.startsWith("update user_subscriptions ")) {
                updates.add(new Update(phase.get(), normalized));
            }
            return sql;
        }
    }

    private enum Mutation { CANCEL, REACTIVATE, PENDING_CYCLE, CLEAR_PENDING }
    private record Fixture(Long userID, Long subscriptionID, Long agreementID, Long basicID, Long premiumID,
                           String orderID, LocalDate startedAt, LocalDate expiresAt) {
        Fixture withOrderID(String orderID) {
            return new Fixture(userID, subscriptionID, agreementID, basicID, premiumID, orderID, startedAt, expiresAt);
        }
    }
    private record Snapshot(Long planID, SubscriptionStatus status, BillingCycle cycle, LocalDate startedAt,
                            LocalDate expiresAt, Long pendingPlanID, BillingCycle pendingCycle,
                            BillingAgreementStatus agreementStatus) { }
    private record Update(String phase, String sql) { }
}
