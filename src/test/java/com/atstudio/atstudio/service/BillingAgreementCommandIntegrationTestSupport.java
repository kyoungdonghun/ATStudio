package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.BDDMockito.given;

abstract class BillingAgreementCommandIntegrationTestSupport {

    static final String ORDER_ID = "ORDER-WI-005";
    static final String CUSTOMER_KEY = "ats_billing_customer_wi005";
    static final BigDecimal AMOUNT = BigDecimal.valueOf(9900);

    @Autowired BillingAgreementApplicationService service;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired PaymentReconciliationIncidentRepository incidentRepository;
    @Autowired TestRecurringPaymentProvider recurringPaymentProvider;
    @Autowired EntityManager entityManager;

    @MockitoBean PlaylistService playlistService;
    @MockitoBean BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    @MockitoBean BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    @MockitoBean EmailService emailService;

    @BeforeEach
    void resetProvider() {
        recurringPaymentProvider.reset();
        given(billingKeyCrypto.encrypt("billing_raw_key"))
                .willReturn(new BillingKeyCrypto.ProtectedBillingKey("encrypted-key", "fingerprint"));
    }

    @AfterEach
    void cleanDatabase() {
        incidentRepository.deleteAll();
        subscriptionPaymentRepository.deleteAll();
        paymentOrderRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    Fixture persistPreparedOrder() {
        User user = userRepository.saveAndFlush(User.builder()
                .nickname("wi005-user")
                .email("wi005@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Basic")
                .description("WI-005 integration plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(AMOUNT)
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        BillingAgreement agreement = billingAgreementRepository.saveAndFlush(BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey(CUSTOMER_KEY)
                .build());
        PaymentOrder order = PaymentOrder.builder()
                .orderId(ORDER_ID)
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
        order.markInProgress("{\"phase\":\"prepare\"}");
        paymentOrderRepository.saveAndFlush(order);
        return new Fixture(user.getId(), agreement.getId());
    }

    CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("wi005@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    PaymentOrder reloadOrder() {
        entityManager.clear();
        return paymentOrderRepository.findByOrderId(ORDER_ID).orElseThrow();
    }

    BillingAgreement reloadAgreement(Long agreementID) {
        entityManager.clear();
        return billingAgreementRepository.findById(agreementID).orElseThrow();
    }

    record Fixture(Long userID, Long agreementID) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestRecurringPaymentProvider recurringPaymentProvider() {
            return new TestRecurringPaymentProvider();
        }
    }

    static final class TestRecurringPaymentProvider implements RecurringPaymentProvider {

        private final List<String> calls = new ArrayList<>();
        private BillingAgreementConfirmResult confirmResult;
        private BillingChargeResult chargeResult;
        private final Deque<BillingChargeResult> chargeResults = new LinkedList<>();
        private BillingAgreementCancelResult cancelResult;
        private RuntimeException confirmException;
        private RuntimeException chargeException;
        private RuntimeException cancelException;
        private Runnable confirmProbe = () -> { };
        private Runnable chargeProbe = () -> { };
        private Runnable cancelProbe = () -> { };
        private BillingChargeCommand lastChargeCommand;

        void reset() {
            calls.clear();
            confirmResult = BillingAgreementConfirmResult.success(
                    "billing_raw_key",
                    "CARD",
                    "1234",
                    "{\"method\":\"CARD\"}");
            chargeResult = BillingChargeResult.success(
                    "tx_wi005",
                    "CARD",
                    "1234",
                    "{\"paymentKey\":\"pay_wi005\"}");
            chargeResults.clear();
            cancelResult = BillingAgreementCancelResult.success("{}");
            confirmException = null;
            chargeException = null;
            cancelException = null;
            confirmProbe = () -> { };
            chargeProbe = () -> { };
            cancelProbe = () -> { };
            lastChargeCommand = null;
        }

        void confirmResult(BillingAgreementConfirmResult result) {
            this.confirmResult = result;
        }

        void chargeResult(BillingChargeResult result) {
            this.chargeResult = result;
        }

        void chargeResults(BillingChargeResult... results) {
            chargeResults.clear();
            chargeResults.addAll(Arrays.asList(results));
        }

        void cancelResult(BillingAgreementCancelResult result) {
            this.cancelResult = result;
        }

        void confirmProbe(Runnable probe) {
            this.confirmProbe = probe;
        }

        void chargeProbe(Runnable probe) {
            this.chargeProbe = probe;
        }

        void cancelProbe(Runnable probe) {
            this.cancelProbe = probe;
        }

        List<String> calls() {
            return List.copyOf(calls);
        }

        BillingChargeCommand lastChargeCommand() {
            return lastChargeCommand;
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS;
        }

        @Override
        public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
            throw new UnsupportedOperationException("Prepare is outside this focused test.");
        }

        @Override
        public BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command) {
            assertNoTransaction();
            calls.add("confirm");
            confirmProbe.run();
            if (confirmException != null) {
                throw confirmException;
            }
            return confirmResult;
        }

        @Override
        public BillingChargeResult charge(BillingChargeCommand command) {
            assertNoTransaction();
            calls.add("charge");
            lastChargeCommand = command;
            chargeProbe.run();
            if (chargeException != null) {
                throw chargeException;
            }
            return chargeResults.isEmpty() ? chargeResult : chargeResults.removeFirst();
        }

        @Override
        public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
            assertNoTransaction();
            calls.add("cancel");
            cancelProbe.run();
            if (cancelException != null) {
                throw cancelException;
            }
            return cancelResult;
        }

        private void assertNoTransaction() {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new AssertionError("Provider call ran inside a local transaction.");
            }
        }
    }
}
