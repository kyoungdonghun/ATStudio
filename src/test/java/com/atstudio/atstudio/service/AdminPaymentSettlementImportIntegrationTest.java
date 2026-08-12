package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportAttemptResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentReceiptRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.PaymentSettlementImportAttemptRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@DataJpaTest
@Import({
        JpaConfig.class,
        AdminPaymentSettlementService.class,
        AdminPaymentSettlementAttemptTransactionService.class,
        AdminPaymentSettlementRowTransactionService.class,
        PaymentSettlementCsvParser.class,
        PaymentCommandKeyFactory.class,
        PaymentOperationAuditLogService.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DisplayName("Settlement CSV import attempt H2 integration tests")
class AdminPaymentSettlementImportIntegrationTest {

    @Autowired AdminPaymentSettlementService service;
    @Autowired PaymentSettlementRepository settlementRepository;
    @Autowired PaymentSettlementImportAttemptRepository attemptRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired UserRepository userRepository;
    @MockitoSpyBean PaymentOrderRepository paymentOrderRepository;
    @MockitoSpyBean SubscriptionPaymentRepository subscriptionPaymentRepository;
    @MockitoSpyBean PaymentRefundRepository paymentRefundRepository;
    @MockitoSpyBean UserSubscriptionRepository userSubscriptionRepository;
    @MockitoSpyBean BillingAgreementRepository billingAgreementRepository;
    @MockitoSpyBean PaymentReceiptRepository paymentReceiptRepository;
    @MockitoBean RecurringPaymentProvider recurringPaymentProvider;
    @MockitoBean PaymentRefundProvider paymentRefundProvider;
    @MockitoBean EmailService emailService;
    @MockitoSpyBean AdminPaymentSettlementRowTransactionService rowTransactionService;

    @Test
    @DisplayName("concurrent same-dedup imports produce one settlement, one audit, and complementary outcomes")
    void concurrentSameDedupImportsAreDeterministic() throws Exception {
        User admin = admin("concurrent");
        CustomUserDetails actor = actor(admin);
        MockMultipartFile file = csv("ORDER-WI056-CONCURRENT");
        CountDownLatch bothRowsEntered = new CountDownLatch(2);
        doAnswer(invocation -> {
            bothRowsEntered.countDown();
            if (!bothRowsEntered.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrent row transaction gate timed out.");
            }
            return invocation.callRealMethod();
        }).when(rowTransactionService).persistImported(any(), any());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<AdminPaymentSettlementImportResponse> first = executor.submit(() -> service.importSettlements(
                    actor,
                    file,
                    "concurrent-a",
                    "10000000-0000-4000-8000-000000000001").getData());
            Future<AdminPaymentSettlementImportResponse> second = executor.submit(() -> service.importSettlements(
                    actor,
                    file,
                    "concurrent-b",
                    "10000000-0000-4000-8000-000000000002").getData());

            List<AdminPaymentSettlementImportResponse> results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS));
            assertThat(results).extracting(AdminPaymentSettlementImportResponse::importedRows)
                    .containsExactlyInAnyOrder(1, 0);
            assertThat(results).extracting(AdminPaymentSettlementImportResponse::skippedDuplicateRows)
                    .containsExactlyInAnyOrder(0, 1);
            assertThat(results).allSatisfy(this::assertConserved);
        } finally {
            executor.shutdownNow();
        }

        assertThat(settlementsFor("ORDER-WI056-CONCURRENT")).hasSize(1);
        assertThat(importAuditsFor("ORDER-WI056-CONCURRENT")).hasSize(1);
        assertThat(attemptsFor(admin)).hasSize(2).allSatisfy(attempt -> {
            assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
            assertThat(attempt.getTotalRows()).isEqualTo(1);
            assertThat(attempt.getImportedRows() + attempt.getDuplicateRows() + attempt.getFailedRows())
                    .isEqualTo(1);
        });
    }

    @Test
    @DisplayName("all-duplicate import retains a completed attempt without another settlement or row audit")
    void allDuplicateImportRetainsAttempt() {
        User admin = admin("all-duplicate");
        CustomUserDetails actor = actor(admin);
        MockMultipartFile file = csv("ORDER-WI056-ALL-DUPLICATE");
        service.importSettlements(
                actor,
                file,
                "first",
                "20000000-0000-4000-8000-000000000001");

        AdminPaymentSettlementImportResponse duplicate = service.importSettlements(
                actor,
                file,
                "all duplicate",
                "20000000-0000-4000-8000-000000000002").getData();

        assertThat(duplicate.importedRows()).isZero();
        assertThat(duplicate.skippedDuplicateRows()).isEqualTo(1);
        assertThat(duplicate.failedRows()).isZero();
        assertConserved(duplicate);
        assertThat(settlementsFor("ORDER-WI056-ALL-DUPLICATE")).hasSize(1);
        assertThat(importAuditsFor("ORDER-WI056-ALL-DUPLICATE")).hasSize(1);
        assertThat(attemptsFor(admin)).hasSize(2);
        PaymentSettlementImportAttempt retained = attemptsFor(admin).stream()
                .filter(attempt -> attempt.getDuplicateRows() == 1)
                .findFirst()
                .orElseThrow();
        assertThat(retained.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(retained.getOperatorNote()).isEqualTo("all duplicate");
    }

    @Test
    @DisplayName("Unicode line separator rows fail independently without Settlement or audit evidence")
    void unicodeLineSeparatorRowsDoNotPersistOrAuditRejectedEvidence() {
        User admin = admin("unicode-sep");
        CustomUserDetails actor = actor(admin);
        long settlementCountBefore = settlementRepository.count();
        long auditCountBefore = auditLogRepository.count();
        String header = "provider,provider_payment_key,provider_settlement_id,order_id,gross_amount,"
                + "net_settlement_amount,settlement_base_date,provider_status\n";
        String validOrderID = "ORDER-\uC8FC\uBB38";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unicode-separators.csv",
                "text/csv",
                (header
                        + "TO\u2028SS,Pay-Key,Settle-ID,ORDER-PROVIDER-LS,10,10,2026-08-01,DONE\n"
                        + "TO\u2029SS,Pay-Key,Settle-ID,ORDER-PROVIDER-PS,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay\u2028Key,Settle-ID,ORDER-PAYMENT-LS,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay\u2029Key,Settle-ID,ORDER-PAYMENT-PS,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay-Key,Settle\u2028ID,ORDER-SETTLEMENT-LS,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay-Key,Settle\u2029ID,ORDER-SETTLEMENT-PS,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay-Key,Settle-ID,Order\u2028ID,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay-Key,Settle-ID,Order\u2029ID,10,10,2026-08-01,DONE\n"
                        + "TOSS,Pay-Key,Settle-ID,ORDER-STATUS-LS,10,10,2026-08-01,Done\u2028Next\n"
                        + "TOSS,Pay-Key,Settle-ID,ORDER-STATUS-PS,10,10,2026-08-01,Done\u2029Next\n"
                        + "TOSS,Pay-\uACB0\uC81C,Settle-\uC815\uC0B0," + validOrderID
                        + ",10,10,2026-08-01,Done-\uC644\uB8CC\n")
                        .getBytes(StandardCharsets.UTF_8));

        AdminPaymentSettlementImportResponse result = service.importSettlements(
                actor,
                file,
                "unicode separator validation",
                "70000000-0000-4000-8000-000000000001").getData();

        assertThat(result.totalRows()).isEqualTo(11);
        assertThat(result.importedRows()).isEqualTo(1);
        assertThat(result.skippedDuplicateRows()).isZero();
        assertThat(result.failedRows()).isEqualTo(10);
        assertThat(result.errors()).hasSize(10);
        assertConserved(result);
        assertThat(settlementRepository.count()).isEqualTo(settlementCountBefore + 1);
        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
        assertThat(settlementRepository.findAll())
                .filteredOn(settlement -> result.importBatchKey().equals(settlement.getImportBatchKey()))
                .singleElement()
                .satisfies(settlement -> {
                    assertThat(settlement.getOrderId()).isEqualTo(validOrderID);
                    assertThat(settlement.getProviderPaymentKey()).isEqualTo("Pay-\uACB0\uC81C");
                    assertThat(settlement.getProviderSettlementId()).isEqualTo("Settle-\uC815\uC0B0");
                    assertThat(settlement.getProviderStatus()).isEqualTo("Done-\uC644\uB8CC");
                });
        assertThat(importAuditsFor(validOrderID)).hasSize(1);
        assertThat(attemptsFor(admin)).singleElement().satisfies(attempt -> {
            assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
            assertThat(attempt.getTotalRows()).isEqualTo(11);
            assertThat(attempt.getImportedRows()).isEqualTo(1);
            assertThat(attempt.getDuplicateRows()).isZero();
            assertThat(attempt.getFailedRows()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("durably equivalent amount forms deduplicate across different import keys at scale two")
    void canonicalAmountsDeduplicateAcrossDifferentImportKeys() {
        User admin = admin("canon-amounts");
        CustomUserDetails actor = actor(admin);
        String orderID = "ORDER-WI067-CANONICAL-AMOUNTS";
        MockMultipartFile omittedZeros = new MockMultipartFile(
                "file",
                "omitted-zeros.csv",
                "text/csv",
                ("provider,order_id,gross_amount,net_settlement_amount,settlement_base_date\n"
                        + "TOSS," + orderID + ",10,10.0,2026-08-12\n")
                        .getBytes(StandardCharsets.UTF_8));
        MockMultipartFile explicitZeros = new MockMultipartFile(
                "file",
                "explicit-zeros.csv",
                "text/csv",
                ("provider,order_id,gross_amount,refund_amount,fee_amount,vat_amount,"
                        + "net_settlement_amount,settlement_base_date\n"
                        + "TOSS," + orderID + ",10.00,0.00,0.0,0,10.00,2026-08-12\n")
                        .getBytes(StandardCharsets.UTF_8));

        AdminPaymentSettlementImportResponse first = service.importSettlements(
                actor,
                omittedZeros,
                "omitted zeros",
                "21000000-0000-4000-8000-000000000001").getData();
        AdminPaymentSettlementImportResponse duplicate = service.importSettlements(
                actor,
                explicitZeros,
                "explicit zeros",
                "21000000-0000-4000-8000-000000000002").getData();

        assertThat(first.importedRows()).isEqualTo(1);
        assertThat(duplicate.importedRows()).isZero();
        assertThat(duplicate.skippedDuplicateRows()).isEqualTo(1);
        assertConserved(first);
        assertConserved(duplicate);
        assertThat(settlementsFor(orderID)).singleElement().satisfies(settlement -> {
            assertThat(settlement.getGrossAmount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(settlement.getRefundAmount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(settlement.getFeeAmount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(settlement.getVatAmount()).isEqualTo(new BigDecimal("0.00"));
            assertThat(settlement.getNetSettlementAmount()).isEqualTo(new BigDecimal("10.00"));
            assertThat(settlement.getGrossAmount().scale()).isEqualTo(2);
            assertThat(settlement.getRefundAmount().scale()).isEqualTo(2);
            assertThat(settlement.getFeeAmount().scale()).isEqualTo(2);
            assertThat(settlement.getVatAmount().scale()).isEqualTo(2);
            assertThat(settlement.getNetSettlementAmount().scale()).isEqualTo(2);
        });
        assertThat(importAuditsFor(orderID)).hasSize(1);
        assertThat(attemptsFor(admin)).hasSize(2);
    }

    @Test
    @DisplayName("same key never reprocesses and header recovery exposes only durable aggregate outcome")
    void sameKeyNeverReprocessesAndRecovers() {
        User admin = admin("same-key");
        CustomUserDetails actor = actor(admin);
        String rawKey = "30000000-0000-4000-8000-000000000001";
        MockMultipartFile file = csv("ORDER-WI056-SAME-KEY");
        AdminPaymentSettlementImportResponse first = service.importSettlements(
                actor,
                file,
                "same key",
                rawKey).getData();

        assertThatThrownBy(() -> service.importSettlements(actor, file, "changed note", rawKey))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_COMPLETED));

        AdminPaymentSettlementImportAttemptResponse recovered = service.recoverImportAttempt(actor, rawKey)
                .getData();
        AdminPaymentSettlementImportAttemptResponse numericDetail = service
                .getImportAttempt(recovered.attemptId())
                .getData();
        assertThat(recovered.state()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(recovered.importBatchKey()).isEqualTo(first.importBatchKey());
        assertThat(recovered.totalRows()).isEqualTo(1);
        assertThat(numericDetail).isEqualTo(recovered);
        assertThat(service.listImportAttempts(1, 20).getDataList())
                .extracting(AdminPaymentSettlementImportAttemptResponse::attemptId)
                .contains(recovered.attemptId());
        assertThat(settlementsFor("ORDER-WI056-SAME-KEY")).singleElement()
                .satisfies(settlement -> assertThat(settlement.getSourcePayload()).isNull());
        assertThat(importAuditsFor("ORDER-WI056-SAME-KEY")).hasSize(1);
        assertThat(attemptsFor(admin)).singleElement().satisfies(attempt -> {
            assertThat(attempt.getKeyDigest()).hasSize(64).doesNotContain(rawKey);
            assertThat(attempt.getOperatorNote()).isEqualTo("same key");
        });
    }

    @Test
    @DisplayName("the same raw key is isolated by ADMIN owner during concurrent imports and recovery")
    void sameRawKeyIsIsolatedByOwnerDuringConcurrentImports() throws Exception {
        User adminA = admin("owner-a");
        User adminB = admin("owner-b");
        CustomUserDetails actorA = actor(adminA);
        CustomUserDetails actorB = actor(adminB);
        String rawKey = "50000000-0000-4000-8000-000000000001";
        String orderIDA = "ORDER-WI056-OWNER-A";
        String orderIDB = "ORDER-WI056-OWNER-B";
        CountDownLatch bothImportsReady = new CountDownLatch(2);
        CountDownLatch startImports = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        AdminPaymentSettlementImportResponse resultA;
        AdminPaymentSettlementImportResponse resultB;

        try {
            Future<AdminPaymentSettlementImportResponse> futureA = executor.submit(() -> {
                bothImportsReady.countDown();
                if (!startImports.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Owner A import start gate timed out.");
                }
                return service.importSettlements(actorA, csv(orderIDA), "owner-a", rawKey).getData();
            });
            Future<AdminPaymentSettlementImportResponse> futureB = executor.submit(() -> {
                bothImportsReady.countDown();
                if (!startImports.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Owner B import start gate timed out.");
                }
                return service.importSettlements(actorB, csv(orderIDB), "owner-b", rawKey).getData();
            });

            assertThat(bothImportsReady.await(5, TimeUnit.SECONDS)).isTrue();
            startImports.countDown();
            resultA = futureA.get(10, TimeUnit.SECONDS);
            resultB = futureB.get(10, TimeUnit.SECONDS);
        } finally {
            startImports.countDown();
            executor.shutdownNow();
        }

        assertThat(resultA.importedRows()).isEqualTo(1);
        assertThat(resultA.skippedDuplicateRows()).isZero();
        assertThat(resultA.failedRows()).isZero();
        assertThat(resultB.importedRows()).isEqualTo(1);
        assertThat(resultB.skippedDuplicateRows()).isZero();
        assertThat(resultB.failedRows()).isZero();
        assertConserved(resultA);
        assertConserved(resultB);

        AdminPaymentSettlementImportAttemptResponse recoveredA = service
                .recoverImportAttempt(actorA, rawKey)
                .getData();
        AdminPaymentSettlementImportAttemptResponse recoveredB = service
                .recoverImportAttempt(actorB, rawKey)
                .getData();
        assertThat(recoveredA.state()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(recoveredB.state()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(recoveredA.actorUserId()).isEqualTo(adminA.getId()).isNotEqualTo(adminB.getId());
        assertThat(recoveredB.actorUserId()).isEqualTo(adminB.getId()).isNotEqualTo(adminA.getId());
        assertThat(recoveredA.attemptId()).isNotEqualTo(recoveredB.attemptId());
        assertThat(recoveredA.importBatchKey())
                .isEqualTo(resultA.importBatchKey())
                .isNotEqualTo(recoveredB.importBatchKey());
        assertThat(recoveredB.importBatchKey()).isEqualTo(resultB.importBatchKey());

        PaymentSettlementImportAttempt durableA = attemptRepository
                .findWithActorById(recoveredA.attemptId())
                .orElseThrow();
        PaymentSettlementImportAttempt durableB = attemptRepository
                .findWithActorById(recoveredB.attemptId())
                .orElseThrow();
        assertThat(durableA.getKeyDigest()).matches("^[0-9a-f]{64}$").doesNotContain(rawKey);
        assertThat(durableB.getKeyDigest()).matches("^[0-9a-f]{64}$").doesNotContain(rawKey);
        assertThat(durableA.getKeyDigest()).isNotEqualTo(durableB.getKeyDigest());
        assertThat(durableA.getActorUser().getId()).isEqualTo(adminA.getId());
        assertThat(durableB.getActorUser().getId()).isEqualTo(adminB.getId());
        assertThat(settlementsFor(orderIDA)).singleElement()
                .satisfies(settlement -> assertThat(settlement.getImportBatchKey())
                        .isEqualTo(recoveredA.importBatchKey()));
        assertThat(settlementsFor(orderIDB)).singleElement()
                .satisfies(settlement -> assertThat(settlement.getImportBatchKey())
                        .isEqualTo(recoveredB.importBatchKey()));
    }

    @Test
    @DisplayName("synthetic import and recovery have no payment-domain or external side effects")
    void importAndRecoveryHaveZeroForbiddenSideEffects() {
        User admin = admin("no-effects");
        CustomUserDetails actor = actor(admin);
        PaymentDomainCounts domainCountsBefore = paymentDomainCounts();
        long settlementCountBefore = settlementRepository.count();
        long attemptCountBefore = attemptRepository.count();
        long auditCountBefore = auditLogRepository.count();
        String rawKey = "60000000-0000-4000-8000-000000000001";
        String orderID = "ORDER-WI056-ZERO-SIDE-EFFECTS";
        clearForbiddenRepositoryInvocations();

        AdminPaymentSettlementImportResponse imported = service.importSettlements(
                actor,
                csv(orderID),
                "zero-side-effects",
                rawKey).getData();
        AdminPaymentSettlementImportAttemptResponse recovered = service
                .recoverImportAttempt(actor, rawKey)
                .getData();

        assertThat(imported.importedRows()).isEqualTo(1);
        assertThat(imported.skippedDuplicateRows()).isZero();
        assertThat(imported.failedRows()).isZero();
        assertConserved(imported);
        assertThat(recovered.state()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
        assertThat(recovered.importBatchKey()).isEqualTo(imported.importBatchKey());
        assertThat(recovered.actorUserId()).isEqualTo(admin.getId());

        verify(paymentOrderRepository).findByOrderId(orderID);
        verifyNoMoreInteractions(paymentOrderRepository);
        verifyNoInteractions(
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userSubscriptionRepository,
                billingAgreementRepository,
                paymentReceiptRepository);
        clearForbiddenRepositoryInvocations();

        assertThat(paymentDomainCounts()).isEqualTo(domainCountsBefore);
        assertThat(settlementRepository.count()).isEqualTo(settlementCountBefore + 1);
        assertThat(attemptRepository.count()).isEqualTo(attemptCountBefore + 1);
        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
        assertThat(settlementsFor(orderID)).hasSize(1);
        assertThat(importAuditsFor(orderID)).hasSize(1);
        verifyNoInteractions(recurringPaymentProvider, paymentRefundProvider, emailService);
    }

    @Test
    @DisplayName("a duplicate collision does not erase an unrelated valid row in the same attempt")
    void multiRowCollisionIsIsolated() throws Exception {
        User admin = admin("multi-row");
        CustomUserDetails actor = actor(admin);
        String sharedOrderID = "ORDER-WI056-MULTI-SHARED";
        String firstUniqueOrderID = "ORDER-WI056-MULTI-FIRST-UNIQUE";
        String secondUniqueOrderID = "ORDER-WI056-MULTI-SECOND-UNIQUE";
        CountDownLatch sharedRowsEntered = new CountDownLatch(2);
        doAnswer(invocation -> {
            PaymentSettlement settlement = invocation.getArgument(1, PaymentSettlement.class);
            if (sharedOrderID.equals(settlement.getOrderId())) {
                sharedRowsEntered.countDown();
                if (!sharedRowsEntered.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Concurrent shared-row gate timed out.");
                }
            }
            return invocation.callRealMethod();
        }).when(rowTransactionService).persistImported(any(), any());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<AdminPaymentSettlementImportResponse> results;

        try {
            Future<AdminPaymentSettlementImportResponse> first = executor.submit(() -> service.importSettlements(
                    actor,
                    csv(sharedOrderID, firstUniqueOrderID),
                    "multi-row-first",
                    "40000000-0000-4000-8000-000000000001").getData());
            Future<AdminPaymentSettlementImportResponse> second = executor.submit(() -> service.importSettlements(
                    actor,
                    csv(sharedOrderID, secondUniqueOrderID),
                    "multi-row-second",
                    "40000000-0000-4000-8000-000000000002").getData());

            results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }

        assertThat(results).extracting(AdminPaymentSettlementImportResponse::importedRows)
                .containsExactlyInAnyOrder(2, 1);
        assertThat(results).extracting(AdminPaymentSettlementImportResponse::skippedDuplicateRows)
                .containsExactlyInAnyOrder(0, 1);
        assertThat(results).allSatisfy(result -> {
            assertThat(result.failedRows()).isZero();
            assertConserved(result);
        });

        AdminPaymentSettlementImportResponse winner = results.stream()
                .filter(result -> result.importedRows() == 2)
                .findFirst()
                .orElseThrow();
        AdminPaymentSettlementImportResponse loser = results.stream()
                .filter(result -> result.skippedDuplicateRows() == 1)
                .findFirst()
                .orElseThrow();
        assertThat(winner.skippedDuplicateRows()).isZero();
        assertThat(loser.importedRows()).isEqualTo(1);

        List<PaymentSettlement> sharedSettlements = settlementsFor(sharedOrderID);
        assertThat(sharedSettlements).hasSize(1);
        assertThat(sharedSettlements.get(0).getImportBatchKey()).isEqualTo(winner.importBatchKey());
        assertThat(importAuditsFor(sharedOrderID)).hasSize(1);

        assertThat(settlementsFor(firstUniqueOrderID)).hasSize(1);
        assertThat(settlementsFor(secondUniqueOrderID)).hasSize(1);
        assertThat(importAuditsFor(firstUniqueOrderID)).hasSize(1);
        assertThat(importAuditsFor(secondUniqueOrderID)).hasSize(1);
        assertThat(List.of(
                settlementsFor(firstUniqueOrderID).get(0),
                settlementsFor(secondUniqueOrderID).get(0)))
                .extracting(PaymentSettlement::getImportBatchKey)
                .containsExactlyInAnyOrder(winner.importBatchKey(), loser.importBatchKey());

        assertThat(attemptsFor(admin)).hasSize(2).allSatisfy(attempt -> {
            assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
            assertThat(attempt.getTotalRows()).isEqualTo(2);
            assertThat(attempt.getFailedRows()).isZero();
            assertThat(attempt.getImportedRows() + attempt.getDuplicateRows() + attempt.getFailedRows())
                    .isEqualTo(2);
        });
        assertThat(attemptsFor(admin)).extracting(PaymentSettlementImportAttempt::importBatchKey)
                .containsExactlyInAnyOrder(winner.importBatchKey(), loser.importBatchKey());
    }

    private void assertConserved(AdminPaymentSettlementImportResponse response) {
        assertThat(response.totalRows()).isEqualTo(
                response.importedRows() + response.skippedDuplicateRows() + response.failedRows());
        assertThat(response.statusCounts().values().stream().mapToInt(Integer::intValue).sum())
                .isEqualTo(response.importedRows());
    }

    private User admin(String label) {
        return userRepository.saveAndFlush(User.builder()
                .nickname("wi056-" + label)
                .email("wi056-" + label + "@test.com")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.ADMIN)
                .build());
    }

    private CustomUserDetails actor(User admin) {
        return CustomUserDetails.builder()
                .id(admin.getId())
                .email(admin.getEmail())
                .role(UserRole.ADMIN)
                .build();
    }

    private MockMultipartFile csv(String... orderIDs) {
        StringBuilder content = new StringBuilder(
                "provider,order_id,gross_amount,net_settlement_amount,settlement_base_date\n");
        for (String orderID : orderIDs) {
            content.append("TOSS,")
                    .append(orderID)
                    .append(",9900,9900,2026-08-12\n");
        }
        return new MockMultipartFile(
                "file",
                "synthetic-settlements.csv",
                "text/csv",
                content.toString().getBytes(StandardCharsets.UTF_8));
    }

    private List<PaymentSettlement> settlementsFor(String orderID) {
        return settlementRepository.findAll().stream()
                .filter(settlement -> orderID.equals(settlement.getOrderId()))
                .toList();
    }

    private List<PaymentOperationAuditLog> importAuditsFor(String orderID) {
        return auditLogRepository.findAll().stream()
                .filter(audit -> audit.getAction() == PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED)
                .filter(audit -> orderID.equals(audit.getOrderId()))
                .toList();
    }

    private List<PaymentSettlementImportAttempt> attemptsFor(User admin) {
        return attemptRepository.findAll().stream()
                .filter(attempt -> attempt.getActorUser().getId().equals(admin.getId()))
                .toList();
    }

    private PaymentDomainCounts paymentDomainCounts() {
        return new PaymentDomainCounts(
                paymentOrderRepository.count(),
                subscriptionPaymentRepository.count(),
                paymentRefundRepository.count(),
                userSubscriptionRepository.count(),
                billingAgreementRepository.count(),
                paymentReceiptRepository.count());
    }

    private void clearForbiddenRepositoryInvocations() {
        clearInvocations(
                paymentOrderRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userSubscriptionRepository,
                billingAgreementRepository,
                paymentReceiptRepository);
    }

    private record PaymentDomainCounts(
            long paymentOrders,
            long subscriptionPayments,
            long paymentRefunds,
            long userSubscriptions,
            long billingAgreements,
            long paymentReceipts) {
    }
}
