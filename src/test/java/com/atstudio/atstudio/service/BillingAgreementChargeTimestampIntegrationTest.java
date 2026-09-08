package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementPrepareResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        BillingAgreementPrepareTransactionService.class,
        PaymentCommandTransactionService.class,
        PaymentReconciliationIncidentService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementCommandIntegrationTestSupport.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Billing agreement monetary charge timestamp integration tests")
class BillingAgreementChargeTimestampIntegrationTest extends BillingAgreementCommandIntegrationTestSupport {

    private static final String PREPARE_KEY = "123e4567-e89b-42d3-a456-426614174003";
    private static final LocalDateTime PREVIOUS_CHARGE = LocalDateTime.of(2026, 8, 1, 9, 30);

    @Autowired PaymentCommandTransactionService commandTransactions;
    @Autowired DataSource dataSource;
    @MockitoSpyBean TestRecurringPaymentProvider provider;

    @BeforeEach
    void configurePurePrepare() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).startsWith("jdbc:h2:mem:");
        }
        doReturn(true).when(provider).supportsPureDeterministicPrepare();
        doReturn(prepareResult()).when(provider).prepareAgreement(any(BillingAgreementPrepareCommand.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("zero-amount prepare, confirmation and completed replay preserve previous or null charge time")
    void registrationPreservesChargeTimestamp(boolean previouslyCharged) {
        LocalDateTime prior = previouslyCharged ? PREVIOUS_CHARGE : null;
        RegistrationFixture fixture = persistRegistrationFixture(prior, SubscriptionStatus.ACTIVE);

        BillingAgreementPrepareResponse prepared = prepareRegistration(fixture);

        assertThat(prepared.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(prepared.purpose()).isEqualTo(PaymentPurpose.BILLING_AGREEMENT);
        assertThat(reloadAgreement(fixture.agreementID()).getLastChargedAt()).isEqualTo(prior);
        assertThat(prepareRegistration(fixture)).isEqualTo(prepared);
        confirmRegistrationAndAssertUnchangedHistory(fixture, prepared, prior);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("key cleanup and failed prepare retain charge history through same-key retry for cancelled grace access")
    void failedPreparationAndCleanupPreserveHistoryOnRetry(boolean previouslyCharged) {
        LocalDateTime prior = previouslyCharged ? PREVIOUS_CHARGE : null;
        RegistrationFixture fixture = persistRegistrationFixture(prior, SubscriptionStatus.CANCELLED);

        commandTransactions.clearIssuedBillingKeyAfterCleanup(fixture.agreementID());
        commandTransactions.clearIssuedBillingKeyAfterCleanup(fixture.agreementID());
        BillingAgreement cleaned = reloadAgreement(fixture.agreementID());
        assertThat(cleaned.getLastChargedAt()).isEqualTo(prior);
        assertThat(cleaned.getBillingKeyCiphertext()).isNull();
        assertThat(cleaned.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        doThrow(new IllegalStateException("forced pure-prepare failure"))
                .doReturn(prepareResult())
                .when(provider).prepareAgreement(any(BillingAgreementPrepareCommand.class));

        assertThatThrownBy(() -> prepareRegistration(fixture))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forced pure-prepare failure");

        PaymentOrder interrupted = paymentOrderRepository.findAll().get(0);
        assertThat(interrupted.getStatus()).isEqualTo(PaymentOrderStatus.READY);
        assertThat(reloadAgreement(fixture.agreementID()).getLastChargedAt()).isEqualTo(prior);
        assertThat(reloadAgreement(fixture.agreementID()).getStatus()).isEqualTo(BillingAgreementStatus.READY);
        BillingAgreementPrepareResponse retry = prepareRegistration(fixture);
        assertThat(retry.orderId()).isEqualTo(interrupted.getOrderId());
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        confirmRegistrationAndAssertUnchangedHistory(fixture, retry, prior);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("positive initial subscription updates charge time once and completed replay does not rewrite it")
    void monetarySubscriptionUpdatesChargeTimestampOnce(boolean previouslyCharged) {
        Fixture fixture = persistPreparedOrder();
        BillingAgreement agreement = reloadAgreement(fixture.agreementID());
        LocalDateTime prior = previouslyCharged ? PREVIOUS_CHARGE : null;
        ReflectionTestUtils.setField(agreement, "lastChargedAt", prior);
        billingAgreementRepository.saveAndFlush(agreement);
        BillingAgreementConfirmRequest request = new BillingAgreementConfirmRequest(
                ORDER_ID, "auth_key", CUSTOMER_KEY, AMOUNT);
        LocalDateTime before = LocalDateTime.now().withNano(0);

        service.confirmBillingAgreement(userDetails(fixture.userID()), request);

        BillingAgreement charged = reloadAgreement(fixture.agreementID());
        LocalDateTime chargedAt = charged.getLastChargedAt();
        assertThat(chargedAt).isBetween(before, LocalDateTime.now().plusSeconds(1));
        assertThat(chargedAt).isNotEqualTo(prior);
        assertThat(charged.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(charged.getNextBillingAt()).isEqualTo(userSubscriptionRepository.findAll().get(0).getExpiresAt());
        assertThat(subscriptionPaymentRepository.findAll()).singleElement()
                .satisfies(payment -> assertThat(payment.getAmount()).isEqualByComparingTo(AMOUNT));

        commandTransactions.finalizeInitialCharge(fixture.userID(), fixture.agreementID(), ORDER_ID);
        service.confirmBillingAgreement(userDetails(fixture.userID()), request);

        assertThat(reloadAgreement(fixture.agreementID()).getLastChargedAt()).isEqualTo(chargedAt);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm", "charge");
    }

    private void confirmRegistrationAndAssertUnchangedHistory(
            RegistrationFixture fixture,
            BillingAgreementPrepareResponse prepared,
            LocalDateTime prior) {
        BillingAgreementConfirmRequest request = new BillingAgreementConfirmRequest(
                prepared.orderId(), "auth_key", CUSTOMER_KEY, BigDecimal.ZERO);
        var confirmed = service.confirmBillingAgreement(userDetails(fixture.userID()), request);
        BillingAgreement registered = reloadAgreement(fixture.agreementID());
        assertThat(registered.getLastChargedAt()).isEqualTo(prior);
        assertThat(registered.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(registered.getBillingKeyCiphertext()).isEqualTo("encrypted-key");
        assertThat(registered.getBillingKeyFingerprint()).isEqualTo("fingerprint");
        assertThat(registered.getNextBillingAt()).isEqualTo(fixture.subscription().getExpiresAt());
        assertThat(registered.getFailureCount()).isZero();
        assertThat(registered.getRenewalRetryAt()).isNull();
        assertThat(registered.getCancelledAt()).isNull();
        assertThat(registered.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.NONE);

        assertThat(commandTransactions.finalizeInitialCharge(
                fixture.userID(), fixture.agreementID(), prepared.orderId())).isEqualTo(confirmed);
        assertThat(service.confirmBillingAgreement(userDetails(fixture.userID()), request)).isEqualTo(confirmed);
        assertThat(reloadAgreement(fixture.agreementID()).getLastChargedAt()).isEqualTo(prior);
        UserSubscription unchanged = userSubscriptionRepository.findById(fixture.subscription().getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(fixture.subscription().getStatus());
        assertThat(unchanged.getSubscription().getId()).isEqualTo(fixture.planID());
        assertThat(unchanged.getBillingCycle()).isEqualTo(fixture.subscription().getBillingCycle());
        assertThat(unchanged.getStartedAt()).isEqualTo(fixture.subscription().getStartedAt());
        assertThat(unchanged.getExpiresAt()).isEqualTo(fixture.subscription().getExpiresAt());
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.findByOrderId(prepared.orderId()).orElseThrow().getStatus())
                .isEqualTo(PaymentOrderStatus.DONE);
        assertThat(recurringPaymentProvider.calls()).containsExactly("confirm");
        verifyNoInteractions(paymentReceiptEvidenceService, emailService);
    }

    private BillingAgreementPrepareResponse prepareRegistration(RegistrationFixture fixture) {
        return service.prepareBillingAgreement(
                userDetails(fixture.userID()),
                new BillingAgreementPrepareRequest(fixture.planID(), BillingCycle.MONTHLY, PaymentPurpose.BILLING_AGREEMENT),
                PREPARE_KEY);
    }

    private BillingAgreementPrepareResult prepareResult() {
        return new BillingAgreementPrepareResult(
                PaymentProviderType.TOSS, "TOSS_BILLING_AUTH", "{\"phase\":\"prepare\"}",
                Map.of("clientKey", "test-client-key", "customerKey", CUSTOMER_KEY,
                        "successUrl", "http://localhost/success", "failUrl", "http://localhost/fail", "method", "CARD"));
    }

    private RegistrationFixture persistRegistrationFixture(LocalDateTime prior, SubscriptionStatus status) {
        User user = userRepository.saveAndFlush(User.builder()
                .nickname("charge-time-user").email("charge-time@example.invalid").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.USER).build());
        Subscription plan = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Basic").description("Charge timestamp integration plan")
                .userType(UserType.INDIVIDUAL).priceMonthly(AMOUNT).priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10).maxWhitelistChannels(3).maxPlaylists(5).build());
        UserSubscription subscription = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user).subscription(plan).billingCycle(BillingCycle.MONTHLY).status(status)
                .startedAt(LocalDate.now().minusDays(15)).expiresAt(LocalDate.now().plusDays(15)).build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user).provider(PaymentProviderType.TOSS).providerCustomerKey(CUSTOMER_KEY)
                .lastChargedAt(prior).build();
        agreement.activate("old-cipher", "old-fingerprint", "CARD", "1234", subscription.getExpiresAt());
        if (status == SubscriptionStatus.CANCELLED) {
            agreement.cancel();
        } else {
            agreement.suspend();
        }
        billingAgreementRepository.saveAndFlush(agreement);
        return new RegistrationFixture(user.getId(), plan.getId(), agreement.getId(), subscription);
    }

    private record RegistrationFixture(Long userID, Long planID, Long agreementID, UserSubscription subscription) {
    }
}
