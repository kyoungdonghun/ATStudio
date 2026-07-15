package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
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
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        JpaConfig.class,
        PaymentOperationAuditLogService.class,
        PaymentRefundTransactionService.class,
        AdminPaymentRefundService.class,
        PaymentRefundResilienceIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("WI-20260715-ATS-003 payment refund lease recovery tests")
class PaymentRefundResilienceIntegrationTest {

    @Autowired AdminPaymentRefundService service;
    @Autowired PaymentRefundTransactionService transactionService;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired PaymentRefundRepository paymentRefundRepository;
    @Autowired PaymentReconciliationIncidentRepository incidentRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired TestPaymentRefundProvider refundProvider;
    @Autowired EntityManager entityManager;

    @AfterEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        incidentRepository.deleteAll();
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
    @DisplayName("crash after claim is reclaimed at 15 minutes on the same row and key")
    void crashAfterClaimIsReclaimedAtLeaseBoundary() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        LocalDateTime firstClaimAt = firstClaimAt(fixture);

        PaymentRefundTransactionService.RefundExecutionClaim abandonedClaim =
                transactionService.claimExecution(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        "claim before crash",
                        firstClaimAt);

        assertThat(abandonedClaim.refundId()).isEqualTo(fixture.refundID());
        assertThat(abandonedClaim.provider()).isEqualTo(PaymentProviderType.TOSS_BILLING);
        assertThat(abandonedClaim.providerPaymentKey()).isEqualTo("payment_key_wi018");
        assertThat(abandonedClaim.orderId()).isEqualTo("ORDER-REFUND-WI018");
        assertThat(abandonedClaim.amount()).isEqualByComparingTo("4000");
        assertThat(abandonedClaim.currency()).isEqualTo("KRW");
        assertThat(abandonedClaim.reason()).isEqualTo(PaymentRefundReasonCode.CUSTOMER_REQUEST.name());
        assertThat(abandonedClaim.idempotencyKey()).isEqualTo("ATS-REFUND-WI018");
        assertThat(abandonedClaim.leaseStartedAt()).isEqualTo(firstClaimAt);
        assertThat(refundProvider.callCount()).isZero();

        ResponseDTO<AdminPaymentRefundResponse> response = service.executeRefundAt(
                fixture.refundID(),
                actor(fixture.adminID()),
                new AdminPaymentRefundExecuteRequest("stale recovery"),
                firstClaimAt.plusMinutes(15));

        PaymentRefund succeeded = reloadRefund(fixture.refundID());
        assertThat(response.getData().status()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(succeeded.getStatus()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(succeeded.getId()).isEqualTo(fixture.refundID());
        assertThat(succeeded.getIdempotencyKey()).isEqualTo("ATS-REFUND-WI018");
        assertThat(paymentRefundRepository.count()).isEqualTo(1);
        assertThat(refundProvider.commands()).containsExactly(commandFrom(abandonedClaim));
        assertThat(auditLogRepository.findAll())
                .anySatisfy(log -> assertThat(log.getNote()).startsWith("STALE_RECLAIM"));
    }

    @Test
    @DisplayName("provider success before result persistence replays the exact command and converges once")
    void providerSuccessBeforeResultPersistenceReplaysExactCommand() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        LocalDateTime firstClaimAt = firstClaimAt(fixture);
        PaymentRefundTransactionService.RefundExecutionClaim firstClaim =
                transactionService.claimExecution(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        "first claim",
                        firstClaimAt);
        transactionService.validateClaimForExecution(firstClaim);

        PaymentRefundProviderResult lostResult = refundProvider.cancelPayment(commandFrom(firstClaim));
        assertThat(lostResult.success()).isTrue();
        assertThat(reloadRefund(fixture.refundID()).getStatus()).isEqualTo(PaymentRefundStatus.PROCESSING);

        service.executeRefundAt(
                fixture.refundID(),
                actor(fixture.adminID()),
                new AdminPaymentRefundExecuteRequest("recover lost result"),
                firstClaimAt.plusMinutes(15));

        PaymentRefund succeeded = reloadRefund(fixture.refundID());
        assertThat(succeeded.getStatus()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(succeeded.getProviderRefundTransactionId()).isEqualTo("refund_tx_default");
        assertThat(succeeded.getIdempotencyKey()).isEqualTo(firstClaim.idempotencyKey());
        assertThat(paymentRefundRepository.count()).isEqualTo(1);
        assertThat(refundProvider.commands()).containsExactly(
                commandFrom(firstClaim),
                commandFrom(firstClaim));
    }

    @Test
    @DisplayName("an old result is fenced after a stale reclaim")
    void oldResultIsFencedAfterStaleReclaim() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        LocalDateTime firstClaimAt = firstClaimAt(fixture);
        PaymentRefundTransactionService.RefundExecutionClaim oldClaim =
                transactionService.claimExecution(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        "old claim",
                        firstClaimAt);
        PaymentRefundTransactionService.RefundExecutionClaim currentClaim =
                transactionService.claimExecution(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        "replacement claim",
                        firstClaimAt.plusMinutes(15));

        List<PaymentRefundProviderResult> delayedResults = List.of(
                PaymentRefundProviderResult.success("old_result", "{}"),
                PaymentRefundProviderResult.failure("OLD_FAILURE", "old failure", "{}"),
                PaymentRefundProviderResult.pending("OLD_PENDING", "old pending", "{}"));
        delayedResults.forEach(result -> assertInvalidTransition(() ->
                transactionService.recordExecutionResult(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        oldClaim.leaseStartedAt(),
                        result)));
        assertInvalidTransition(() -> transactionService.recordExecutionResult(
                fixture.refundID(),
                actor(fixture.adminID()),
                oldClaim.leaseStartedAt(),
                null));
        assertInvalidTransition(() -> transactionService.recordExecutionException(
                fixture.refundID(),
                actor(fixture.adminID()),
                oldClaim.leaseStartedAt(),
                new IllegalStateException("delayed exception")));
        assertInvalidTransition(() -> transactionService.recordReplayUnavailable(
                fixture.refundID(),
                actor(fixture.adminID()),
                oldClaim.leaseStartedAt()));

        PaymentRefund stillClaimed = reloadRefund(fixture.refundID());
        assertThat(stillClaimed.getStatus()).isEqualTo(PaymentRefundStatus.PROCESSING);
        assertThat(stillClaimed.getProcessingStartedAt()).isEqualTo(currentClaim.leaseStartedAt());
        assertThat(stillClaimed.getProviderRefundTransactionId()).isNull();

        transactionService.recordExecutionResult(
                fixture.refundID(),
                actor(fixture.adminID()),
                currentClaim.leaseStartedAt(),
                PaymentRefundProviderResult.success("current_result", "{}"));
        assertThat(reloadRefund(fixture.refundID()).getProviderRefundTransactionId())
                .isEqualTo("current_result");
    }

    @Test
    @DisplayName("two stale reclaimers produce one lease owner and one exact invalid-transition loser")
    void twoStaleReclaimersProduceOneExactLoser() throws Exception {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        LocalDateTime firstClaimAt = firstClaimAt(fixture);
        transactionService.claimExecution(
                fixture.refundID(),
                actor(fixture.adminID()),
                "abandoned claim",
                firstClaimAt);
        LocalDateTime reclaimAt = firstClaimAt.plusMinutes(15);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<StaleClaimAttempt> first = CompletableFuture.supplyAsync(
                    () -> claimStaleAfterStart(fixture, reclaimAt, start),
                    executor);
            CompletableFuture<StaleClaimAttempt> second = CompletableFuture.supplyAsync(
                    () -> claimStaleAfterStart(fixture, reclaimAt, start),
                    executor);

            start.countDown();
            List<StaleClaimAttempt> results = List.of(
                    first.get(5, TimeUnit.SECONDS),
                    second.get(5, TimeUnit.SECONDS));

            assertThat(results).filteredOn(StaleClaimAttempt::claimed).hasSize(1);
            assertThat(results)
                    .filteredOn(attempt -> !attempt.claimed())
                    .singleElement()
                    .extracting(StaleClaimAttempt::error)
                    .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        } finally {
            executor.shutdownNow();
        }

        PaymentRefund claimed = reloadRefund(fixture.refundID());
        assertThat(claimed.getStatus()).isEqualTo(PaymentRefundStatus.PROCESSING);
        assertThat(claimed.getProcessingStartedAt()).isEqualTo(reclaimAt);
        assertThat(refundProvider.callCount()).isZero();
    }

    @ParameterizedTest(name = "tampered {0} is rejected")
    @ValueSource(strings = {"amount", "orderId", "providerPaymentKey"})
    @DisplayName("same-key command changes are rejected before provider invocation")
    void changedSameKeyCommandIsRejectedBeforeProvider(String changedField) {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        PaymentRefundTransactionService.RefundExecutionClaim claim =
                transactionService.claimExecution(
                        fixture.refundID(),
                        actor(fixture.adminID()),
                        "claim",
                        firstClaimAt(fixture));
        PaymentRefundTransactionService.RefundExecutionClaim tampered = tamper(claim, changedField);

        assertThatThrownBy(() -> transactionService.validateClaimForExecution(tampered))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        assertThat(refundProvider.callCount()).isZero();
        assertThat(reloadRefund(fixture.refundID()).getProcessingStartedAt())
                .isEqualTo(claim.leaseStartedAt());
    }

    @Test
    @DisplayName("elapsed replay ceiling records pending incident without provider mutation")
    void elapsedReplayCeilingRecordsPendingIncidentWithoutMutation() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        PaymentRefund persisted = reloadRefund(fixture.refundID());
        LocalDateTime firstClaimAt = persisted.getCreatedAt().plusHours(1).withNano(0);
        transactionService.claimExecution(
                fixture.refundID(),
                actor(fixture.adminID()),
                "old claim",
                firstClaimAt);

        ResponseDTO<AdminPaymentRefundResponse> response = service.executeRefundAt(
                fixture.refundID(),
                actor(fixture.adminID()),
                new AdminPaymentRefundExecuteRequest("lookup only"),
                persisted.getCreatedAt().plusHours(24).plusSeconds(1).withNano(0));

        PaymentRefund pending = reloadRefund(fixture.refundID());
        PaymentReconciliationIncident incident = incidentRepository
                .findByDedupeKey("refund-replay-unavailable:" + fixture.refundID())
                .orElseThrow();
        assertThat(response.getData().status()).isEqualTo(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getStatus()).isEqualTo(PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION);
        assertThat(pending.getFailureCode()).isEqualTo("REFUND_REPLAY_CEILING_ELAPSED");
        assertThat(pending.getIdempotencyKey()).isEqualTo("ATS-REFUND-WI018");
        assertThat(paymentRefundRepository.count()).isEqualTo(1);
        assertThat(refundProvider.callCount()).isZero();
        assertThat(incident.getStatus()).isEqualTo(PaymentReconciliationIncidentStatus.OPEN);
        assertThat(incident.getIssueType()).isEqualTo(PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED);
        assertThat(incident.getFailureCode()).isEqualTo("REFUND_REPLAY_CEILING_ELAPSED");
    }

    @Test
    @DisplayName("fresh processing rejects a competing claim before the 15-minute boundary")
    void freshProcessingRejectsCompetingClaim() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        LocalDateTime firstClaimAt = firstClaimAt(fixture);
        transactionService.claimExecution(
                fixture.refundID(),
                actor(fixture.adminID()),
                "first",
                firstClaimAt);

        assertThatThrownBy(() -> transactionService.claimExecution(
                fixture.refundID(),
                actor(fixture.adminID()),
                "too early",
                firstClaimAt.plusMinutes(15).minusSeconds(1)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        assertThat(reloadRefund(fixture.refundID()).getProcessingStartedAt()).isEqualTo(firstClaimAt);
        assertThat(refundProvider.callCount()).isZero();
    }

    @Test
    @DisplayName("stale projection is ordered by the lease index and bounded by pageable")
    void staleProjectionIsOrderedAndBounded() {
        RefundFixture fixture = persistApprovedRefund(BigDecimal.valueOf(9900), BigDecimal.valueOf(4000));
        PaymentRefund sibling = persistSiblingApprovedRefund(fixture, "ATS-REFUND-WI003-SIBLING");
        LocalDateTime firstClaimAt = firstClaimAt(fixture);
        transactionService.claimExecution(
                fixture.refundID(),
                actor(fixture.adminID()),
                "first stale candidate",
                firstClaimAt);
        transactionService.claimExecution(
                sibling.getId(),
                actor(fixture.adminID()),
                "second stale candidate",
                firstClaimAt.plusMinutes(1));

        List<Long> firstPage = paymentRefundRepository.findStaleProcessingIds(
                PaymentRefundStatus.PROCESSING,
                firstClaimAt.plusMinutes(1),
                PageRequest.of(0, 1));

        assertThat(firstPage).containsExactly(fixture.refundID());
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
            CompletableFuture<ReservationAttempt> first = CompletableFuture.supplyAsync(
                    () -> createRefundAfterStart(fixture, start),
                    executor);
            CompletableFuture<ReservationAttempt> second = CompletableFuture.supplyAsync(
                    () -> createRefundAfterStart(fixture, start),
                    executor);

            start.countDown();
            List<ReservationAttempt> results = List.of(
                    first.get(5, TimeUnit.SECONDS),
                    second.get(5, TimeUnit.SECONDS));

            assertThat(results).filteredOn(ReservationAttempt::created).hasSize(1);
            assertThat(results)
                    .filteredOn(attempt -> !attempt.created())
                    .singleElement()
                    .extracting(ReservationAttempt::error)
                    .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT);
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

    private ReservationAttempt createRefundAfterStart(RefundFixture fixture, CountDownLatch start) {
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting to start concurrent refund reservation.");
            }
            service.createRefund(
                    actor(fixture.adminID()),
                    new AdminPaymentRefundCreateRequest(
                            fixture.subscriptionPaymentID(),
                            BigDecimal.valueOf(6000),
                            PaymentRefundReasonCode.CUSTOMER_REQUEST,
                            "concurrent reservation"));
            return new ReservationAttempt(true, null);
        } catch (BusinessException exception) {
            return new ReservationAttempt(false, exception.getErrorCode());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Concurrent refund reservation was interrupted.", exception);
        }
    }

    private StaleClaimAttempt claimStaleAfterStart(
            RefundFixture fixture,
            LocalDateTime reclaimAt,
            CountDownLatch start) {
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting to start stale refund reclaim.");
            }
            transactionService.claimExecution(
                    fixture.refundID(),
                    actor(fixture.adminID()),
                    "concurrent stale reclaim",
                    reclaimAt);
            return new StaleClaimAttempt(true, null);
        } catch (BusinessException exception) {
            return new StaleClaimAttempt(false, exception.getErrorCode());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Concurrent stale refund reclaim was interrupted.", exception);
        }
    }

    private LocalDateTime firstClaimAt(RefundFixture fixture) {
        return reloadRefund(fixture.refundID()).getCreatedAt().plusSeconds(1).withNano(0);
    }

    private void assertInvalidTransition(Runnable operation) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
    }

    private PaymentRefundProviderCommand commandFrom(
            PaymentRefundTransactionService.RefundExecutionClaim claim) {
        return new PaymentRefundProviderCommand(
                claim.providerPaymentKey(),
                claim.orderId(),
                claim.amount(),
                claim.reason(),
                claim.idempotencyKey());
    }

    private PaymentRefundTransactionService.RefundExecutionClaim tamper(
            PaymentRefundTransactionService.RefundExecutionClaim claim,
            String changedField) {
        return new PaymentRefundTransactionService.RefundExecutionClaim(
                claim.refundId(),
                claim.provider(),
                "providerPaymentKey".equals(changedField)
                        ? claim.providerPaymentKey() + "-changed"
                        : claim.providerPaymentKey(),
                "orderId".equals(changedField) ? claim.orderId() + "-changed" : claim.orderId(),
                "amount".equals(changedField) ? claim.amount().add(BigDecimal.ONE) : claim.amount(),
                claim.currency(),
                claim.reason(),
                claim.idempotencyKey(),
                claim.leaseStartedAt(),
                claim.executionMode());
    }

    private PaymentRefund persistSiblingApprovedRefund(RefundFixture fixture, String idempotencyKey) {
        return paymentRefundRepository.saveAndFlush(PaymentRefund.builder()
                .subscriptionPayment(subscriptionPaymentRepository.findById(fixture.subscriptionPaymentID()).orElseThrow())
                .paymentOrder(paymentOrderRepository.findById(fixture.paymentOrderID()).orElseThrow())
                .user(userRepository.findById(fixture.userID()).orElseThrow())
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentRefundStatus.APPROVED)
                .amount(BigDecimal.valueOf(100))
                .currency("KRW")
                .reasonCode(PaymentRefundReasonCode.CUSTOMER_REQUEST)
                .reasonNote("sibling")
                .idempotencyKey(idempotencyKey)
                .providerPaymentKey("payment_key_wi018")
                .requestedBy(userRepository.findById(fixture.adminID()).orElseThrow())
                .approvedBy(userRepository.findById(fixture.adminID()).orElseThrow())
                .build());
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

    record StaleClaimAttempt(boolean claimed, BUSINESS_ERROR error) {
    }

    record ReservationAttempt(boolean created, BUSINESS_ERROR error) {
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
