package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;

abstract class BillingAgreementCleanupIntegrationTestSupport {

    @Autowired BillingAgreementApplicationService applicationService;
    @Autowired WithdrawalBillingCleanupService withdrawalCleanupService;
    @Autowired BillingAgreementCleanupTransactionService cleanupTransactionService;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired PaymentReconciliationIncidentRepository incidentRepository;
    @Autowired TestCleanupProvider cleanupProvider;
    @Autowired EntityManager entityManager;

    @MockitoBean BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean PaymentCommandTransactionService paymentCommandTransactionService;
    @MockitoBean EmailService emailService;

    @BeforeEach
    void resetCleanupProvider() {
        cleanupProvider.reset();
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-billing-key");
    }

    @AfterEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        incidentRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
    }

    Fixture persistFixture(boolean deletedUser) {
        User user = User.builder()
                .nickname(deletedUser ? "withdrawn-user" : "cancellation-user")
                .email((deletedUser ? "withdrawn" : "cancellation") + "@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        if (deletedUser) {
            user.withdraw();
        }
        user = userRepository.saveAndFlush(user);
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Cleanup plan")
                .description("Package C integration plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
        UserSubscription userSubscription = userSubscriptionRepository.saveAndFlush(
                UserSubscription.builder()
                        .user(user)
                        .subscription(subscription)
                        .billingCycle(BillingCycle.MONTHLY)
                        .startedAt(LocalDate.now())
                        .expiresAt(LocalDate.now().plusMonths(1))
                        .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS)
                .providerCustomerKey("cleanup-customer-" + user.getId())
                .build();
        agreement.activate(
                "encrypted-key",
                "fingerprint",
                "CARD",
                "****1234",
                LocalDate.now().plusMonths(1));
        if (deletedUser) {
            agreement.cancel();
            userSubscription.cancel();
        }
        agreement = billingAgreementRepository.saveAndFlush(agreement);
        return new Fixture(user.getId(), agreement.getId(), userSubscription.getId());
    }

    BillingAgreement reloadAgreement(Long agreementID) {
        entityManager.clear();
        return billingAgreementRepository.findById(agreementID).orElseThrow();
    }

    UserSubscription reloadSubscription(Long subscriptionID) {
        entityManager.clear();
        return userSubscriptionRepository.findById(subscriptionID).orElseThrow();
    }

    CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("cancellation@test.com")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    record Fixture(Long userID, Long agreementID, Long subscriptionID) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestCleanupProvider testCleanupProvider() {
            return new TestCleanupProvider();
        }
    }

    static final class TestCleanupProvider implements RecurringPaymentProvider {

        private final List<String> calls = new ArrayList<>();
        private BillingAgreementCancelResult cancelResult;
        private RuntimeException cancelException;
        private Runnable cancelProbe;
        private boolean transactionActiveAtCancel;

        void reset() {
            calls.clear();
            cancelResult = BillingAgreementCancelResult.success("{}");
            cancelException = null;
            cancelProbe = () -> { };
            transactionActiveAtCancel = true;
        }

        void cancelResult(BillingAgreementCancelResult cancelResult) {
            this.cancelResult = cancelResult;
        }

        void cancelException(RuntimeException cancelException) {
            this.cancelException = cancelException;
        }

        void cancelProbe(Runnable cancelProbe) {
            this.cancelProbe = cancelProbe;
        }

        List<String> calls() {
            return List.copyOf(calls);
        }

        boolean transactionActiveAtCancel() {
            return transactionActiveAtCancel;
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS;
        }

        @Override
        public BillingAgreementPrepareResult prepareAgreement(BillingAgreementPrepareCommand command) {
            throw new UnsupportedOperationException("Prepare is outside Package C tests.");
        }

        @Override
        public BillingAgreementConfirmResult confirmAgreement(BillingAgreementConfirmCommand command) {
            throw new UnsupportedOperationException("Confirm is outside Package C tests.");
        }

        @Override
        public BillingChargeResult charge(BillingChargeCommand command) {
            throw new UnsupportedOperationException("Charge is outside Package C tests.");
        }

        @Override
        public BillingAgreementCancelResult cancelAgreement(BillingAgreementCancelCommand command) {
            transactionActiveAtCancel = TransactionSynchronizationManager.isActualTransactionActive();
            if (transactionActiveAtCancel) {
                throw new AssertionError("Provider cleanup ran inside a local transaction.");
            }
            calls.add("cancel");
            cancelProbe.run();
            if (cancelException != null) {
                throw cancelException;
            }
            return cancelResult;
        }
    }
}
