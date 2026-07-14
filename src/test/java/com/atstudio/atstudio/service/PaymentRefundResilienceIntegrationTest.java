package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentOperationAuditLogService.class,
        PaymentRefundTransactionService.class,
        AdminPaymentRefundService.class,
        PaymentRefundResilienceIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("WI-018 payment refund resilience integration tests")
class PaymentRefundResilienceIntegrationTest {

    @Autowired AdminPaymentRefundService service;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired PaymentRefundRepository paymentRefundRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired TestPaymentRefundProvider refundProvider;
    @Autowired EntityManager entityManager;

    @AfterEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        paymentRefundRepository.deleteAll();
        subscriptionPaymentRepository.deleteAll();
        paymentOrderRepository.deleteAll();
        billingAgreementRepository.deleteAll();
        userSubscriptionRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        refundProvider.reset();
    }

    @Test
    @DisplayName("provider exception commits pending refund and retry reuses the same idempotency key")
    void providerExceptionCommitsPendingAndRetryReusesIdempotencyKey() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        refundProvider.failNextWith(new IllegalStateException("network timeout"));

        ResponseDTO<AdminPaymentRefundResponse> firstResponse = service.executeRefund(
                fixture.refundID(),
                actor(fixture.adminID()),
                new AdminPaymentRefundExecuteRequest("first attempt"));

        PaymentRefund pending = reloadRefund(fixture.refundID());
        assertThat(firstResponse.getData().status()).isEqualTo(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getStatus()).isEqualTo(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getIdempotencyKey()).isEqualTo("ATS-REFUND-WI018");
        assertThat(refundProvider.callCount()).isEqualTo(1);
        assertThat(refundProvider.commands()).extracting(PaymentRefundProviderCommand::idempotencyKey)
                .containsExactly("ATS-REFUND-WI018");

        refundProvider.result(PaymentRefundProviderResult.success(
                "refund_tx_wi018",
                "{\"cancel\":\"ok\"}"));
        ResponseDTO<AdminPaymentRefundResponse> retryResponse = service.executeRefund(
                fixture.refundID(),
                actor(fixture.adminID()),
                new AdminPaymentRefundExecuteRequest("retry"));

        PaymentRefund succeeded = reloadRefund(fixture.refundID());
        assertThat(retryResponse.getData().status()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(succeeded.getStatus()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(succeeded.getProviderRefundTransactionId()).isEqualTo("refund_tx_wi018");
        assertThat(refundProvider.callCount()).isEqualTo(2);
        assertThat(refundProvider.commands()).extracting(PaymentRefundProviderCommand::idempotencyKey)
                .containsExactly("ATS-REFUND-WI018", "ATS-REFUND-WI018");
        assertThat(auditLogRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("concurrent refund reservations do not exceed the committed source payment amount")
    void concurrentRefundReservationsDoNotExceedSourcePaymentAmount() throws Exception {
        RefundFixture fixture = persistRefundSource(BigDecimal.valueOf(9900));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Boolean> first = CompletableFuture.supplyAsync(
                    () -> createRefundAfterStart(fixture, start),
                    executor);
            CompletableFuture<Boolean> second = CompletableFuture.supplyAsync(
                    () -> createRefundAfterStart(fixture, start),
                    executor);

            start.countDown();
            List<Boolean> results = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));

            assertThat(results).containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }

        entityManager.clear();
        List<PaymentRefund> refunds = paymentRefundRepository.findAll();
        assertThat(refunds).hasSize(1);
        assertThat(refunds.get(0).getAmount()).isEqualByComparingTo("6000");
        assertThat(refunds.get(0).getStatus()).isEqualTo(PaymentRefundStatus.REQUESTED);
        assertThat(refundProvider.callCount()).isZero();
    }

    private boolean createRefundAfterStart(RefundFixture fixture, CountDownLatch start) {
        try {
            start.await(5, TimeUnit.SECONDS);
            service.createRefund(
                    actor(fixture.adminID()),
                    new AdminPaymentRefundCreateRequest(
                            fixture.subscriptionPaymentID(),
                            BigDecimal.valueOf(6000),
                            PaymentRefundReasonCode.CUSTOMER_REQUEST,
                            "concurrent reservation"));
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private RefundFixture persistApprovedRefund(BigDecimal paymentAmount, BigDecimal refundAmount) {
        RefundFixture fixture = persistRefundSource(paymentAmount);
        PaymentRefund refund = paymentRefundRepository.saveAndFlush(PaymentRefund.builder()
                .subscriptionPayment(subscriptionPaymentRepository.findById(fixture.subscriptionPaymentID()).orElseThrow())
                .paymentOrder(paymentOrderRepository.findById(fixture.paymentOrderID()).orElseThrow())
                .user(userRepository.findById(fixture.userID()).orElseThrow())
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentRefundStatus.APPROVED)
                .amount(refundAmount)
                .currency("KRW")
                .reasonCode(PaymentRefundReasonCode.CUSTOMER_REQUEST)
                .reasonNote("approved")
                .idempotencyKey("ATS-REFUND-WI018")
                .providerPaymentKey("payment_key_wi018")
                .requestedBy(userRepository.findById(fixture.adminID()).orElseThrow())
                .approvedBy(userRepository.findById(fixture.adminID()).orElseThrow())
                .build());
        return new RefundFixture(
                fixture.userID(),
                fixture.adminID(),
                fixture.paymentOrderID(),
                fixture.subscriptionPaymentID(),
                refund.getId());
    }

    private RefundFixture persistRefundSource(BigDecimal paymentAmount) {
        User user = userRepository.saveAndFlush(User.builder()
                .nickname("refund-user")
                .email("refund-user@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
        User admin = userRepository.saveAndFlush(User.builder()
                .nickname("refund-admin")
                .email("refund-admin@test.com")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
        Subscription subscription = subscriptionRepository.saveAndFlush(Subscription.builder()
                .name("Refund Plan")
                .description("Refund integration plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(paymentAmount)
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
                .startedAt(LocalDate.of(2026, 6, 1))
                .expiresAt(LocalDate.of(2026, 7, 1))
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("refund-customer")
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "1234", userSubscription.getExpiresAt());
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder order = PaymentOrder.builder()
                .orderId("ORDER-REFUND-WI018")
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentOrderStatus.DONE)
                .subscription(subscription)
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(paymentAmount)
                .currency("KRW")
                .pgTransactionId("payment_key_wi018")
                .expiresAt(LocalDateTime.of(2026, 6, 1, 10, 0))
                .build();
        paymentOrderRepository.saveAndFlush(order);
        SubscriptionPayment payment = subscriptionPaymentRepository.saveAndFlush(SubscriptionPayment.builder()
                .user(user)
                .userSubscription(userSubscription)
                .subscription(subscription)
                .paymentOrder(order)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .provider(PaymentProviderType.TOSS_BILLING)
                .amount(paymentAmount)
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment_key_wi018")
                .build());
        return new RefundFixture(user.getId(), admin.getId(), order.getId(), payment.getId(), null);
    }

    private PaymentRefund reloadRefund(Long refundID) {
        entityManager.clear();
        return paymentRefundRepository.findById(refundID).orElseThrow();
    }

    private CustomUserDetails actor(Long adminID) {
        return CustomUserDetails.builder()
                .id(adminID)
                .email("refund-admin@test.com")
                .role(UserRole.ADMIN)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    record RefundFixture(
            Long userID,
            Long adminID,
            Long paymentOrderID,
            Long subscriptionPaymentID,
            Long refundID) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestPaymentRefundProvider testPaymentRefundProvider() {
            return new TestPaymentRefundProvider();
        }
    }

    static final class TestPaymentRefundProvider implements PaymentRefundProvider {

        private final List<PaymentRefundProviderCommand> commands = new CopyOnWriteArrayList<>();
        private final AtomicInteger callCount = new AtomicInteger();
        private PaymentRefundProviderResult result = PaymentRefundProviderResult.success(
                "refund_tx_default",
                "{}");
        private RuntimeException nextException;

        void reset() {
            commands.clear();
            callCount.set(0);
            result = PaymentRefundProviderResult.success("refund_tx_default", "{}");
            nextException = null;
        }

        void result(PaymentRefundProviderResult result) {
            this.result = result;
        }

        void failNextWith(RuntimeException exception) {
            this.nextException = exception;
        }

        int callCount() {
            return callCount.get();
        }

        List<PaymentRefundProviderCommand> commands() {
            return List.copyOf(commands);
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS_BILLING;
        }

        @Override
        public PaymentRefundProviderResult cancelPayment(PaymentRefundProviderCommand command) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new AssertionError("Refund Provider call ran inside a local transaction.");
            }
            callCount.incrementAndGet();
            commands.add(command);
            if (nextException != null) {
                RuntimeException exception = nextException;
                nextException = null;
                throw exception;
            }
            return result;
        }
    }
}
