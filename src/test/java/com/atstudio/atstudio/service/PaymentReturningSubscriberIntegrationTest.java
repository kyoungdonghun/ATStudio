package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@DataJpaTest
@Import({JpaConfig.class, PaymentCommandKeyFactory.class, PaymentCommandTransactionService.class,
        BillingAgreementPrepareTransactionService.class, BillingAgreementApplicationService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PaymentReturningSubscriberIntegrationTest extends BillingAgreementCommandIntegrationTestSupport {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174000";
    private static final String HISTORY_ORDER = "RETURN-HISTORY";

    @Autowired PaymentCommandTransactionService commands;
    @Autowired PaymentCommandKeyFactory keyFactory;
    @Autowired TransactionTemplate transactions;
    @MockitoBean PaymentReconciliationIncidentService incidentService;

    @ParameterizedTest
    @EnumSource(SubscriptionStatus.class)
    void expiredHistoryPurchasesFullPriceAndReplaysWithoutReplacingHistory(SubscriptionStatus status) {
        ReturningFixture fixture = persistReturningSubscriber(status);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        String boundKey = order(prepared).getCommandKey();
        assertThat(boundKey).startsWith(keyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY) + ":SOURCE:")
                .doesNotContain(PREPARE_KEY);
        assertThat(prepare(fixture).orderId()).isEqualTo(prepared.orderId());
        assertThat(prepared.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE);
        assertThat(prepared.amount()).isEqualByComparingTo(AMOUNT);
        assertThat(paymentOrderRepository.findRecoveryByCommandKeyAndUserID(
                keyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY), fixture.userID()))
                .get().extracting(PaymentOrder::getOrderId).isEqualTo(prepared.orderId());

        service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared));
        service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared));
        assertFinalized(fixture, prepared);
        assertThat(order(prepared).getCommandKey()).isEqualTo(boundKey);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare", "prepare", "confirm", "charge");
        assertThat(recurringPaymentProvider.lastChargeCommand().amount()).isEqualByComparingTo(AMOUNT);
    }

    @Test
    void durableSuccessRecoversAfterLocalRollbackAndCheckoutExpiryWithoutAnotherCharge() {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.EXPIRED);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        doThrow(new IllegalStateException("synthetic local failure")).doNothing()
                .when(playlistService).createDefaultPlaylist(any(User.class));

        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared)))
                .isInstanceOf(IllegalStateException.class).hasMessage("synthetic local failure");
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(source(fixture).getExpiresAt()).isEqualTo(fixture.expiredAt());
        PaymentOrder expiredCheckout = order(prepared);
        ReflectionTestUtils.setField(expiredCheckout, "expiresAt", LocalDateTime.now().minusMinutes(1));
        paymentOrderRepository.saveAndFlush(expiredCheckout);

        service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared));
        commands.finalizeInitialCharge(fixture.userID(), fixture.agreementID(), prepared.orderId());
        assertFinalized(fixture, prepared);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare", "confirm", "charge");
    }

    @Test
    void unknownReturningPurchaseReconcilesAndFinalizesWithoutRepeatingProvider() {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.CANCELLED);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        recurringPaymentProvider.chargeResult(null);
        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared)))
                .isInstanceOf(BusinessException.class);
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);

        commands.recordProviderSuccessFromReconciliation(fixture.agreementID(), prepared.orderId(),
                "tx_return_reconciled", "{}", LocalDateTime.now().minusMinutes(15));
        commands.finalizeInitialCharge(fixture.userID(), fixture.agreementID(), prepared.orderId());
        assertFinalized(fixture, prepared);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare", "confirm", "charge");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void changedSourceBeforeConfirmIsRejectedEvenWhenReplacementIsAlsoExpired(boolean activeReplacement) {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.EXPIRED);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        replaceSource(fixture, activeReplacement);

        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> prepare(fixture)).isInstanceOf(BusinessException.class);
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.IN_PROGRESS);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void changedSourceAfterChargeKeepsDurableSuccessButCannotOverwriteReplacement(boolean activeReplacement) {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.EXPIRED);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        recurringPaymentProvider.chargeProbe(() -> replaceSource(fixture, activeReplacement));

        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared)))
                .isInstanceOf(BusinessException.class);
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThatThrownBy(() -> commands.finalizeInitialCharge(
                fixture.userID(), fixture.agreementID(), prepared.orderId())).isInstanceOf(BusinessException.class);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(source(fixture).getBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare", "confirm", "charge");
    }

    @Test
    void cancelledAgreementAfterChargeStillRejectsReturningPurchaseFinalization() {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.EXPIRED);
        BillingAgreementPrepareResponse prepared = prepare(fixture);
        recurringPaymentProvider.chargeProbe(() -> transactions.executeWithoutResult(ignored ->
                billingAgreementRepository.findByIDForUpdate(fixture.agreementID()).orElseThrow().cancel()));

        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()), confirm(prepared)))
                .isInstanceOf(BusinessException.class);
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(source(fixture).getExpiresAt()).isEqualTo(fixture.expiredAt());
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
    }

    @Test
    void legacyOrderWithoutSourceEvidenceCannotAdoptRetainedHistory() {
        Fixture fixture = persistPreparedOrder();
        User user = userRepository.findById(fixture.userID()).orElseThrow();
        Subscription plan = subscriptionRepository.findAll().get(0);
        userSubscriptionRepository.saveAndFlush(UserSubscription.builder().user(user).subscription(plan)
                .status(SubscriptionStatus.EXPIRED).billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now().minusMonths(2)).expiresAt(LocalDate.now().minusMonths(1)).build());
        assertThatThrownBy(() -> service.confirmBillingAgreement(userDetails(fixture.userID()),
                new BillingAgreementConfirmRequest(ORDER_ID, "synthetic-auth", CUSTOMER_KEY, AMOUNT)))
                .isInstanceOf(BusinessException.class);
        assertThat(recurringPaymentProvider.calls()).isEmpty();
    }

    @Test
    void multipleSourceSnapshotsForOneRequestFailExplicitlyWithoutChoosingOrCreatingAnOrder() {
        ReturningFixture fixture = persistReturningSubscriber(SubscriptionStatus.EXPIRED);
        prepare(fixture);
        String baseKey = keyFactory.billingAgreementPrepare(fixture.userID(), PREPARE_KEY);
        replaceSource(fixture, false);
        UserSubscription changed = source(fixture);
        PaymentOrder conflicting = PaymentOrder.builder().orderId("RETURN-CONFLICTING-SNAPSHOT")
                .commandKey(keyFactory.bindSubscriptionSource(baseKey, changed))
                .user(userRepository.findById(fixture.userID()).orElseThrow())
                .subscription(subscriptionRepository.findById(fixture.planID()).orElseThrow())
                .userSubscription(changed)
                .billingAgreement(billingAgreementRepository.findById(fixture.agreementID()).orElseThrow())
                .purpose(PaymentPurpose.SUBSCRIBE).provider(PaymentProviderType.TOSS)
                .billingCycle(BillingCycle.MONTHLY).amount(AMOUNT)
                .expiresAt(LocalDateTime.now().plusMinutes(10)).build();
        paymentOrderRepository.saveAndFlush(conflicting);

        assertThatThrownBy(() -> paymentOrderRepository.findRecoveryByCommandKeyAndUserID(baseKey, fixture.userID()))
                .isInstanceOf(DataIntegrityViolationException.class).hasMessage("Ambiguous payment command identity.");
        assertThatThrownBy(() -> prepare(fixture))
                .isInstanceOf(DataIntegrityViolationException.class).hasMessage("Ambiguous payment command identity.");
        assertThat(paymentOrderRepository.count()).isEqualTo(3);
        assertThat(recurringPaymentProvider.calls()).containsExactly("prepare");
    }

    private BillingAgreementPrepareResponse prepare(ReturningFixture fixture) {
        return service.prepareBillingAgreement(userDetails(fixture.userID()), new BillingAgreementPrepareRequest(
                fixture.planID(), BillingCycle.MONTHLY, PaymentPurpose.SUBSCRIBE), PREPARE_KEY);
    }

    private BillingAgreementConfirmRequest confirm(BillingAgreementPrepareResponse prepared) {
        return new BillingAgreementConfirmRequest(prepared.orderId(), "synthetic-auth",
                prepared.checkout().customerKey(), prepared.amount());
    }

    private PaymentOrder order(BillingAgreementPrepareResponse prepared) {
        return paymentOrderRepository.findByOrderId(prepared.orderId()).orElseThrow();
    }

    private UserSubscription source(ReturningFixture fixture) {
        return userSubscriptionRepository.findById(fixture.sourceID()).orElseThrow();
    }

    private void replaceSource(ReturningFixture fixture, boolean active) {
        transactions.executeWithoutResult(ignored -> {
            UserSubscription source = userSubscriptionRepository.findByIdForUpdate(fixture.sourceID()).orElseThrow();
            LocalDate start = active ? LocalDate.now() : LocalDate.now().minusYears(2);
            source.startNewSubscription(source.getSubscription(), BillingCycle.YEARLY, start, start.plusYears(1));
        });
    }

    private void assertFinalized(ReturningFixture fixture, BillingAgreementPrepareResponse prepared) {
        assertThat(order(prepared).getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(userSubscriptionRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(2);
        UserSubscription current = source(fixture);
        assertThat(current.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(current.getStartedAt()).isEqualTo(LocalDate.now());
        assertThat(current.getExpiresAt()).isEqualTo(LocalDate.now().plusMonths(1));
        assertThat(current.getPendingSubscription()).isNull();
        assertThat(current.getPendingBillingCycle()).isNull();
        SubscriptionPayment history = subscriptionPaymentRepository.findByPaymentOrder(
                paymentOrderRepository.findByOrderId(HISTORY_ORDER).orElseThrow()).orElseThrow();
        assertThat(history.getPgTransactionId()).isEqualTo("tx_return_history");
        assertThat(history.getAmount()).isEqualByComparingTo(AMOUNT);
        assertThat(history.getPaymentStatus()).isEqualTo(PaymentStatus.DONE);
        SubscriptionPayment purchased = subscriptionPaymentRepository.findByPaymentOrder(order(prepared)).orElseThrow();
        assertThat(purchased.getUserSubscription().getId()).isEqualTo(fixture.sourceID());
        assertThat(purchased.getAmount()).isEqualByComparingTo(AMOUNT);
    }

    private ReturningFixture persistReturningSubscriber(SubscriptionStatus status) {
        User user = userRepository.saveAndFlush(User.builder().nickname("returning-user")
                .email("returning@example.invalid").password("synthetic-password")
                .userType(UserType.INDIVIDUAL).role(UserRole.USER).build());
        Subscription plan = subscriptionRepository.saveAndFlush(Subscription.builder().name("Returning Plan")
                .description("PAY-01 synthetic plan").userType(UserType.INDIVIDUAL)
                .priceMonthly(AMOUNT).priceYearly(AMOUNT.multiply(java.math.BigDecimal.TEN))
                .downloadPerDay(10).maxWhitelistChannels(3).maxPlaylists(5).build());
        LocalDate expiredAt = LocalDate.now().minusMonths(1);
        UserSubscription source = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user).subscription(plan).billingCycle(BillingCycle.MONTHLY).status(status)
                .startedAt(expiredAt.minusMonths(1)).expiresAt(expiredAt).build());
        BillingAgreement agreement = BillingAgreement.builder().user(user).provider(PaymentProviderType.TOSS)
                .providerCustomerKey(CUSTOMER_KEY).build();
        agreement.activate("synthetic-old-cipher", "synthetic-old-fingerprint", "CARD", "1234", expiredAt);
        agreement.cancel();
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder history = PaymentOrder.builder().orderId(HISTORY_ORDER).commandKey("BILLING_CONFIRM:RETURN-HISTORY")
                .user(user).subscription(plan).userSubscription(source).billingAgreement(agreement)
                .purpose(PaymentPurpose.SUBSCRIBE).provider(PaymentProviderType.TOSS).billingCycle(BillingCycle.MONTHLY)
                .amount(AMOUNT).expiresAt(expiredAt.atStartOfDay()).build();
        history.markDone("tx_return_history", source, "{}");
        paymentOrderRepository.saveAndFlush(history);
        subscriptionPaymentRepository.saveAndFlush(SubscriptionPayment.builder().paymentOrder(history)
                .billingAgreement(agreement).provider(PaymentProviderType.TOSS).user(user).userSubscription(source)
                .subscription(plan).billingCycle(BillingCycle.MONTHLY).amount(AMOUNT)
                .paymentStatus(PaymentStatus.DONE).pgTransactionId("tx_return_history").build());
        return new ReturningFixture(user.getId(), plan.getId(), source.getId(), agreement.getId(), expiredAt);
    }

    private record ReturningFixture(Long userID, Long planID, Long sourceID, Long agreementID, LocalDate expiredAt) { }
}
