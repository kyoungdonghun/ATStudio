package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentRefundReasonCode;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
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
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.datasource.hikari.maximum-pool-size=12",
        "spring.datasource.hikari.connection-timeout=5000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        JpaConfig.class,
        PaymentProperties.class,
        PaymentCommandKeyFactory.class,
        PaymentCommandTransactionService.class,
        BillingAgreementCleanupTransactionService.class,
        PaymentOperationAuditLogService.class,
        PaymentRefundTransactionService.class,
        AdminPaymentRefundService.class,
        PaymentReconciliationTransactionService.class,
        PaymentReconciliationService.class,
        PaymentReconciliationIncidentService.class,
        PaymentMysqlConcurrencyIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "ATSTUDIO_MYSQL_PROOF_ENABLED", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
@DisplayName("WI-20260715-ATS-007 disposable MySQL concurrency proof")
class PaymentMysqlConcurrencyIntegrationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_wi007_\\d{8}_[a-z0-9]{8}$");
    private static final BigDecimal MONTHLY_AMOUNT = BigDecimal.valueOf(9900);
    private static final List<String> TESTED_TABLES = List.of(
            "users",
            "subscriptions",
            "user_subscriptions",
            "billing_agreements",
            "payment_orders",
            "subscription_payments",
            "payment_refunds",
            "payment_reconciliation_incidents",
            "payment_operation_audit_logs");
    private static final List<String> MUTATED_TABLES = List.of(
            "payment_operation_audit_logs",
            "payment_reconciliation_incidents",
            "payment_refunds",
            "subscription_payments",
            "payment_orders",
            "billing_agreements",
            "user_subscriptions",
            "subscriptions",
            "users");

    @Autowired PaymentCommandTransactionService paymentCommandTransactions;
    @Autowired BillingAgreementCleanupTransactionService cleanupTransactions;
    @Autowired AdminPaymentRefundService refundService;
    @Autowired PaymentRefundTransactionService refundTransactions;
    @Autowired PaymentReconciliationService reconciliationService;

    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @Autowired BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Autowired PaymentRefundRepository paymentRefundRepository;
    @Autowired PaymentReconciliationIncidentRepository incidentRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;

    @Autowired TestStatusLookupProvider statusLookupProvider;
    @Autowired TestRefundProvider refundProvider;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @MockitoBean PlaylistService playlistService;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean EmailService emailService;

    @BeforeAll
    void verifyMySqlAndInnoDbContract() {
        assertDisposableTarget();
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        if (version == null || !version.startsWith("8.")) {
            throw new AssertionError("MySQL major version 8 is required for Package G.");
        }

        String isolation = jdbcTemplate.queryForObject("SELECT @@transaction_isolation", String.class);
        if (isolation == null || isolation.isBlank()) {
            throw new AssertionError("MySQL transaction isolation could not be read.");
        }

        Map<String, String> engines = new ConcurrentHashMap<>();
        String tableList = TESTED_TABLES.stream()
                .map(table -> "'" + table + "'")
                .reduce((left, right) -> left + "," + right)
                .orElseThrow();
        jdbcTemplate.query(
                "SELECT table_name, engine FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() AND table_name IN (" + tableList + ")",
                (RowCallbackHandler) resultSet -> {
                    engines.put(resultSet.getString(1).toLowerCase(Locale.ROOT), resultSet.getString(2));
                });
        if (engines.size() != TESTED_TABLES.size()
                || TESTED_TABLES.stream().anyMatch(table -> !"InnoDB".equalsIgnoreCase(engines.get(table)))) {
            throw new AssertionError("Every Package G table must exist and use InnoDB.");
        }

        System.out.println("mysql.version=" + version);
        System.out.println("mysql.transactionIsolation=" + isolation);
        System.out.println("mysql.engineContract=InnoDB; testedTableCount=" + TESTED_TABLES.size());
    }

    @BeforeEach
    void resetDisposableFixtures() {
        assertDisposableTarget();
        entityManager.clear();
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");
                try {
                    for (String table : MUTATED_TABLES) {
                        statement.execute("TRUNCATE TABLE `" + table + "`");
                    }
                } finally {
                    statement.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }
            return null;
        });
        statusLookupProvider.reset();
        refundProvider.reset();
        reset(paymentReceiptEvidenceService, playlistService, emailService);
        doNothing().when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));
    }

    @Test
    @DisplayName("race 1: two first renewal claims leave one command and one exact in-progress loser")
    void race1_twoFirstRenewalClaimsConverge() {
        LocalDate due = LocalDate.now();
        RenewalFixture fixture = persistRenewalFixture("mysql-r1", due);
        LocalDateTime claimedAt = LocalDateTime.now().withNano(0);

        MysqlRaceTestSupport.RacePair<PaymentCommandTransactionService.RenewalClaim> race =
                MysqlRaceTestSupport.runPair(
                        () -> paymentCommandTransactions.claimRenewal(fixture.agreementID(), due, claimedAt),
                        () -> paymentCommandTransactions.claimRenewal(fixture.agreementID(), due, claimedAt));

        MysqlRaceTestSupport.RaceOutcome<PaymentCommandTransactionService.RenewalClaim> winner = onlySuccess(race);
        assertExactLoser(race, BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        assertThat(winner.value().action())
                .isEqualTo(PaymentCommandTransactionService.RenewalAction.CALL_PROVIDER);

        PaymentOrder order = reloadRenewalOrder(fixture.agreementID());
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.PROCESSING);
        assertThat(order.getProviderAttempt()).isEqualTo(1);
        assertThat(order.getOrderId()).isEqualTo(winner.value().orderID());
        assertThat(order.getCommandKey()).isEqualTo(
                "RENEWAL:%d:%d:%s".formatted(fixture.agreementID(), fixture.userSubscriptionID(), due));
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isZero();
    }

    @Test
    @DisplayName("race 2: two day-two retry claims reuse one row and open one new attempt")
    void race2_dayTwoRetryReusesOneCommand() {
        LocalDate due = LocalDate.now();
        RenewalFixture fixture = persistRenewalFixture("mysql-r2", due);
        LocalDateTime firstClaimedAt = LocalDateTime.now().withNano(0);
        PaymentCommandTransactionService.RenewalClaim firstClaim =
                paymentCommandTransactions.claimRenewal(fixture.agreementID(), due, firstClaimedAt);
        PaymentOrder firstAttempt = reloadRenewalOrder(fixture.agreementID());
        String firstOrderID = firstAttempt.getOrderId();
        String firstCommandKey = firstAttempt.getCommandKey();
        String firstAttemptKey = firstAttempt.getProviderIdempotencyKey();

        paymentCommandTransactions.recordRenewalProviderFailure(
                fixture.agreementID(),
                firstClaim.orderID(),
                "DECLINED",
                "deterministic failure",
                PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                due);

        LocalDate retryDate = due.plusDays(1);
        LocalDateTime retryClaimedAt = firstClaimedAt.plusDays(1);
        MysqlRaceTestSupport.RacePair<PaymentCommandTransactionService.RenewalClaim> race =
                MysqlRaceTestSupport.runPair(
                        () -> paymentCommandTransactions.claimRenewal(
                                fixture.agreementID(), retryDate, retryClaimedAt),
                        () -> paymentCommandTransactions.claimRenewal(
                                fixture.agreementID(), retryDate, retryClaimedAt));

        MysqlRaceTestSupport.RaceOutcome<PaymentCommandTransactionService.RenewalClaim> winner = onlySuccess(race);
        assertExactLoser(race, BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        PaymentOrder retried = reloadRenewalOrder(fixture.agreementID());
        BillingAgreement agreement = reloadAgreement(fixture.agreementID());

        assertThat(winner.value().action())
                .isEqualTo(PaymentCommandTransactionService.RenewalAction.CALL_PROVIDER);
        assertThat(retried.getOrderId()).isEqualTo(firstOrderID);
        assertThat(retried.getCommandKey()).isEqualTo(firstCommandKey);
        assertThat(retried.getBillingPeriodStart()).isEqualTo(due);
        assertThat(retried.getProviderAttempt()).isEqualTo(2);
        assertThat(retried.getProviderIdempotencyKey()).isNotEqualTo(firstAttemptKey);
        assertThat(agreement.getNextBillingAt()).isEqualTo(due);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("race 3: duplicate upgrade claim and finalizer-cancellation ordering converge without deadlock")
    void race3_upgradeFinalizerAndCancellationConverge() {
        UpgradeFixture fixture = persistUpgradeFixture("mysql-r3");
        LocalDateTime claimedAt = LocalDateTime.now().withNano(0);

        MysqlRaceTestSupport.RacePair<PaymentCommandTransactionService.UpgradeClaim> claimRace =
                MysqlRaceTestSupport.runPair(
                        () -> paymentCommandTransactions.claimUpgrade(
                                fixture.userID(),
                                fixture.userSubscriptionID(),
                                fixture.targetPlanID(),
                                BillingCycle.MONTHLY,
                                claimedAt),
                        () -> paymentCommandTransactions.claimUpgrade(
                                fixture.userID(),
                                fixture.userSubscriptionID(),
                                fixture.targetPlanID(),
                                BillingCycle.MONTHLY,
                                claimedAt));
        PaymentCommandTransactionService.UpgradeClaim claim = onlySuccess(claimRace).value();
        assertExactLoser(claimRace, BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        paymentCommandTransactions.recordProviderSuccess(
                claim.agreementID(),
                claim.orderID(),
                "tx-mysql-r3",
                "{}",
                "CARD",
                "****1234");

        CountDownLatch finalizerHoldingLocks = new CountDownLatch(1);
        CountDownLatch releaseFinalizer = new CountDownLatch(1);
        CountDownLatch cancellationInvoked = new CountDownLatch(1);
        doAnswer(invocation -> {
            finalizerHoldingLocks.countDown();
            MysqlRaceTestSupport.await(
                    releaseFinalizer,
                    Duration.ofSeconds(10),
                    "upgrade finalizer release timed out");
            return null;
        }).when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<MysqlRaceTestSupport.RaceOutcome<Object>> finalizer =
                    CompletableFuture.supplyAsync(
                            () -> MysqlRaceTestSupport.capture(() -> paymentCommandTransactions.finalizeUpgrade(
                                    fixture.userID(), claim.agreementID(), claim.orderID())),
                            executor);
            MysqlRaceTestSupport.await(
                    finalizerHoldingLocks,
                    Duration.ofSeconds(10),
                    "upgrade finalizer did not reach its locked evidence phase");

            CompletableFuture<MysqlRaceTestSupport.RaceOutcome<Object>> cancellation =
                    CompletableFuture.supplyAsync(
                            () -> MysqlRaceTestSupport.capture(() -> {
                                cancellationInvoked.countDown();
                                return cleanupTransactions.claimUserCancellation(
                                        fixture.userID(),
                                        LocalDateTime.now().withNano(0));
                            }),
                            executor);
            MysqlRaceTestSupport.await(
                    cancellationInvoked,
                    Duration.ofSeconds(5),
                    "cancellation worker did not start");
            releaseFinalizer.countDown();

            MysqlRaceTestSupport.assertSucceeded(MysqlRaceTestSupport.getStrict(finalizer, "upgrade finalizer"));
            MysqlRaceTestSupport.RaceOutcome<Object> cancellationOutcome =
                    MysqlRaceTestSupport.getStrict(cancellation, "agreement cancellation");
            MysqlRaceTestSupport.assertSucceeded(cancellationOutcome);
            BillingAgreementCleanupTransactionService.UserCancellationClaim cleanupClaim =
                    (BillingAgreementCleanupTransactionService.UserCancellationClaim) cancellationOutcome.value();
            assertThat(cleanupClaim.action())
                    .isEqualTo(BillingAgreementCleanupTransactionService.CleanupAction.CALL_PROVIDER);
        } finally {
            releaseFinalizer.countDown();
            stopExecutor(executor);
        }

        PaymentOrder order = reloadOrder(claim.orderID());
        BillingAgreement agreement = reloadAgreement(claim.agreementID());
        UserSubscription subscription = reloadUserSubscription(fixture.userSubscriptionID());
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(subscription.getSubscription().getId()).isEqualTo(fixture.targetPlanID());
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(agreement.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
        assertThat(agreement.getBillingKeyCleanupStartedAt()).isNotNull();
        assertThat(agreement.getBillingKeyCiphertext()).isNotBlank();
    }

    @Test
    @DisplayName("race 4: two provider-success finalizers create one payment and one entitlement transition")
    void race4_twoFinalizersConverge() {
        LocalDate due = LocalDate.now();
        RenewalFixture fixture = persistRenewalFixture("mysql-r4", due);
        PaymentCommandTransactionService.RenewalClaim claim = paymentCommandTransactions.claimRenewal(
                fixture.agreementID(),
                due,
                LocalDateTime.now().withNano(0));
        paymentCommandTransactions.recordProviderSuccess(
                fixture.agreementID(),
                claim.orderID(),
                "tx-mysql-r4",
                "{}",
                "CARD",
                "****1234");

        MysqlRaceTestSupport.RacePair<Void> race = MysqlRaceTestSupport.runPair(
                () -> {
                    paymentCommandTransactions.finalizeRenewal(fixture.agreementID(), claim.orderID());
                    return null;
                },
                () -> {
                    paymentCommandTransactions.finalizeRenewal(fixture.agreementID(), claim.orderID());
                    return null;
                });
        race.outcomes().forEach(MysqlRaceTestSupport::assertSucceeded);

        PaymentOrder order = reloadOrder(claim.orderID());
        BillingAgreement agreement = reloadAgreement(fixture.agreementID());
        UserSubscription subscription = reloadUserSubscription(fixture.userSubscriptionID());
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(subscriptionPaymentRepository.count()).isEqualTo(1);
        assertThat(subscription.getStartedAt()).isEqualTo(due);
        assertThat(subscription.getExpiresAt()).isEqualTo(due.plusMonths(1));
        assertThat(agreement.getNextBillingAt()).isEqualTo(due.plusMonths(1));
    }

    @Test
    @DisplayName("race 5: two fresh refund reservations never exceed the source payment")
    void race5_twoRefundReservationsHaveExactValidationLoser() {
        RefundFixture fixture = persistRefundSource("mysql-r5", MONTHLY_AMOUNT);
        AdminPaymentRefundCreateRequest request = new AdminPaymentRefundCreateRequest(
                fixture.subscriptionPaymentID(),
                BigDecimal.valueOf(6000),
                PaymentRefundReasonCode.CUSTOMER_REQUEST,
                "mysql concurrent reservation");

        var race = MysqlRaceTestSupport.runPair(
                () -> refundService.createRefund(adminActor(fixture.adminID()), request),
                () -> refundService.createRefund(adminActor(fixture.adminID()), request));

        onlySuccess(race);
        assertExactLoser(race, BUSINESS_ERROR.INVALID_ARGUMENT);
        entityManager.clear();
        List<PaymentRefund> refunds = paymentRefundRepository.findAll();
        BigDecimal reserved = refunds.stream()
                .map(PaymentRefund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(refunds).hasSize(1);
        assertThat(reserved).isEqualByComparingTo("6000");
        assertThat(reserved).isLessThanOrEqualTo(MONTHLY_AMOUNT);
        assertThat(refunds.get(0).getStatus()).isEqualTo(PaymentRefundStatus.REQUESTED);
        assertThat(auditLogRepository.count()).isEqualTo(1);
        assertThat(refundProvider.callCount()).isZero();
    }

    @Test
    @DisplayName("race 6: two stale refund reclaimers leave one lease and fence the old result")
    void race6_twoStaleRefundReclaimersFenceOldResult() {
        RefundFixture fixture = persistApprovedRefund("mysql-r6", MONTHLY_AMOUNT, BigDecimal.valueOf(4000));
        PaymentRefund initialRefund = reloadRefund(fixture.refundID());
        LocalDateTime firstClaimAt = initialRefund.getCreatedAt().plusSeconds(1).withNano(0);
        PaymentRefundTransactionService.RefundExecutionClaim firstClaim = refundTransactions.claimExecution(
                fixture.refundID(),
                adminActor(fixture.adminID()),
                "abandoned claim",
                firstClaimAt);
        LocalDateTime reclaimAt = firstClaim.leaseStartedAt().plusMinutes(15);

        var race = MysqlRaceTestSupport.runPair(
                () -> refundTransactions.claimExecution(
                        fixture.refundID(),
                        adminActor(fixture.adminID()),
                        "stale reclaim one",
                        reclaimAt),
                () -> refundTransactions.claimExecution(
                        fixture.refundID(),
                        adminActor(fixture.adminID()),
                        "stale reclaim two",
                        reclaimAt));

        PaymentRefundTransactionService.RefundExecutionClaim replacement = onlySuccess(race).value();
        assertExactLoser(race, BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        assertThat(replacement.leaseStartedAt()).isEqualTo(reclaimAt);
        assertThat(replacement.refundId()).isEqualTo(firstClaim.refundId());
        assertThat(replacement.idempotencyKey()).isEqualTo(firstClaim.idempotencyKey());

        MysqlRaceTestSupport.RaceOutcome<Object> delayedOldResult = MysqlRaceTestSupport.capture(() ->
                refundTransactions.recordExecutionResult(
                        fixture.refundID(),
                        adminActor(fixture.adminID()),
                        firstClaim.leaseStartedAt(),
                        PaymentRefundProviderResult.success("old-result", "{}")));
        assertThat(MysqlRaceTestSupport.exactBusinessError(delayedOldResult))
                .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION);

        PaymentRefund persisted = reloadRefund(fixture.refundID());
        assertThat(persisted.getStatus()).isEqualTo(PaymentRefundStatus.PROCESSING);
        assertThat(persisted.getProcessingStartedAt()).isEqualTo(reclaimAt);
        assertThat(persisted.getProviderRefundTransactionId()).isNull();
        assertThat(paymentRefundRepository.count()).isEqualTo(1);
        assertThat(refundProvider.callCount()).isZero();
    }

    @Test
    @DisplayName("race 7: reconciliation finalize-only and normal finalizer share one provider owner and resolve Incident")
    void race7_reconciliationAndNormalFinalizerConverge() {
        ReconciliationFixture fixture = persistReconciliationRenewalFixture("mysql-r7");
        statusLookupProvider.respondExact(fixture.order());
        doThrow(new IllegalStateException("forced local finalization failure"))
                .when(paymentReceiptEvidenceService)
                .publishSuccessfulChargeEvidence(
                        any(PaymentOrder.class),
                        any(SubscriptionPayment.class),
                        any(String.class));

        PaymentReconciliationService.ProviderReconciliationResult setup =
                reconciliationService.reconcileProviderLedger();
        assertThat(setup.finalizedOrders()).isZero();
        assertThat(reloadOrder(fixture.order().getOrderId()).getStatus())
                .isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(subscriptionPaymentRepository.count()).isZero();
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.OPEN);

        reset(paymentReceiptEvidenceService);
        doNothing().when(paymentReceiptEvidenceService).publishSuccessfulChargeEvidence(
                any(PaymentOrder.class),
                any(SubscriptionPayment.class),
                any(String.class));
        CountDownLatch lookupEntered = new CountDownLatch(1);
        CountDownLatch finalizerStart = new CountDownLatch(1);
        statusLookupProvider.blockNextLookup(lookupEntered, finalizerStart);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<MysqlRaceTestSupport.RaceOutcome<Object>> reconciliation =
                    CompletableFuture.supplyAsync(
                            () -> MysqlRaceTestSupport.capture(
                                    () -> reconciliationService.reconcileProviderLedger()),
                            executor);
            MysqlRaceTestSupport.await(
                    lookupEntered,
                    Duration.ofSeconds(10),
                    "reconciliation did not reach provider lookup");

            CompletableFuture<MysqlRaceTestSupport.RaceOutcome<Object>> normalFinalizer =
                    CompletableFuture.supplyAsync(
                            () -> MysqlRaceTestSupport.capture(() -> {
                                MysqlRaceTestSupport.await(
                                        finalizerStart,
                                        Duration.ofSeconds(10),
                                        "normal finalizer start timed out");
                                paymentCommandTransactions.finalizeRenewal(
                                        fixture.agreementID(),
                                        fixture.order().getOrderId());
                                return null;
                            }),
                            executor);
            finalizerStart.countDown();

            MysqlRaceTestSupport.assertSucceeded(
                    MysqlRaceTestSupport.getStrict(reconciliation, "reconciliation finalizer"));
            MysqlRaceTestSupport.assertSucceeded(
                    MysqlRaceTestSupport.getStrict(normalFinalizer, "normal finalizer"));
        } finally {
            finalizerStart.countDown();
            stopExecutor(executor);
        }

        PaymentOrder order = reloadOrder(fixture.order().getOrderId());
        List<SubscriptionPayment> payments = subscriptionPaymentRepository.findAll();
        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.DONE);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getPgTransactionId()).isEqualTo(fixture.transactionID());
        assertThat(payments.get(0).getPaymentOrder().getId()).isEqualTo(order.getId());
        assertThat(incidentRepository.findAll())
                .extracting(PaymentReconciliationIncident::getStatus)
                .containsOnly(PaymentReconciliationIncidentStatus.RESOLVED);
        assertThat(statusLookupProvider.lookupCalls()).isEqualTo(2);
        assertThat(statusLookupProvider.lookupTransactionStates()).containsOnly(false);
    }

    private RenewalFixture persistRenewalFixture(String label, LocalDate due) {
        User user = persistUser(label + "-user", UserRole.USER);
        Subscription plan = persistPlan(label + "-plan", MONTHLY_AMOUNT);
        UserSubscription userSubscription = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(plan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(due.minusMonths(1))
                .expiresAt(due)
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-" + label)
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", due);
        billingAgreementRepository.saveAndFlush(agreement);
        return new RenewalFixture(user.getId(), userSubscription.getId(), agreement.getId());
    }

    private UpgradeFixture persistUpgradeFixture(String label) {
        User user = persistUser(label + "-user", UserRole.USER);
        Subscription currentPlan = persistPlan(label + "-current", MONTHLY_AMOUNT);
        Subscription targetPlan = persistPlan(label + "-target", BigDecimal.valueOf(19900));
        UserSubscription current = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(1))
                .expiresAt(LocalDate.now().plusMonths(1))
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-" + label)
                .build();
        agreement.activate(
                "encrypted-key",
                "fingerprint",
                "CARD",
                "****1234",
                current.getExpiresAt());
        billingAgreementRepository.saveAndFlush(agreement);
        return new UpgradeFixture(user.getId(), current.getId(), targetPlan.getId(), agreement.getId());
    }

    private RefundFixture persistRefundSource(String label, BigDecimal paymentAmount) {
        User user = persistUser(label + "-user", UserRole.USER);
        User admin = persistUser(label + "-admin", UserRole.ADMIN);
        Subscription plan = persistPlan(label + "-plan", paymentAmount);
        LocalDate startedAt = LocalDate.now().minusMonths(1);
        LocalDate expiresAt = LocalDate.now().plusMonths(1);
        UserSubscription userSubscription = userSubscriptionRepository.saveAndFlush(UserSubscription.builder()
                .user(user)
                .subscription(plan)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build());
        BillingAgreement agreement = BillingAgreement.builder()
                .user(user)
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("customer-" + label)
                .build();
        agreement.activate("encrypted-key", "fingerprint", "CARD", "****1234", expiresAt);
        billingAgreementRepository.saveAndFlush(agreement);
        PaymentOrder order = paymentOrderRepository.saveAndFlush(PaymentOrder.builder()
                .orderId("ORDER-" + label.toUpperCase(Locale.ROOT))
                .commandKey("SUBSCRIBE:" + label)
                .user(user)
                .purpose(PaymentPurpose.SUBSCRIBE)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentOrderStatus.DONE)
                .subscription(plan)
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .amount(paymentAmount)
                .currency("KRW")
                .pgTransactionId("payment-key-" + label)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build());
        SubscriptionPayment payment = subscriptionPaymentRepository.saveAndFlush(SubscriptionPayment.builder()
                .user(user)
                .userSubscription(userSubscription)
                .subscription(plan)
                .paymentOrder(order)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .provider(PaymentProviderType.TOSS_BILLING)
                .amount(paymentAmount)
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId("payment-key-" + label)
                .build());
        return new RefundFixture(user.getId(), admin.getId(), order.getId(), payment.getId(), null);
    }

    private RefundFixture persistApprovedRefund(
            String label,
            BigDecimal paymentAmount,
            BigDecimal refundAmount) {
        RefundFixture source = persistRefundSource(label, paymentAmount);
        PaymentRefund refund = paymentRefundRepository.saveAndFlush(PaymentRefund.builder()
                .subscriptionPayment(subscriptionPaymentRepository.findById(
                        source.subscriptionPaymentID()).orElseThrow())
                .paymentOrder(paymentOrderRepository.findById(source.paymentOrderID()).orElseThrow())
                .user(userRepository.findById(source.userID()).orElseThrow())
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentRefundStatus.APPROVED)
                .amount(refundAmount)
                .currency("KRW")
                .reasonCode(PaymentRefundReasonCode.CUSTOMER_REQUEST)
                .reasonNote("approved")
                .idempotencyKey("ATS-REFUND-" + label.toUpperCase(Locale.ROOT))
                .providerPaymentKey("payment-key-" + label)
                .requestedBy(userRepository.findById(source.adminID()).orElseThrow())
                .approvedBy(userRepository.findById(source.adminID()).orElseThrow())
                .build());
        return new RefundFixture(
                source.userID(),
                source.adminID(),
                source.paymentOrderID(),
                source.subscriptionPaymentID(),
                refund.getId());
    }

    private ReconciliationFixture persistReconciliationRenewalFixture(String label) {
        LocalDate periodStart = LocalDate.now();
        RenewalFixture renewal = persistRenewalFixture(label, periodStart);
        User user = userRepository.findById(renewal.userID()).orElseThrow();
        UserSubscription userSubscription = userSubscriptionRepository
                .findById(renewal.userSubscriptionID()).orElseThrow();
        BillingAgreement agreement = billingAgreementRepository.findById(renewal.agreementID()).orElseThrow();
        String transactionID = "tx-" + label;
        PaymentOrder order = paymentOrderRepository.saveAndFlush(PaymentOrder.builder()
                .orderId("ORDER-" + label.toUpperCase(Locale.ROOT))
                .commandKey("RENEWAL:%d:%d:%s".formatted(
                        agreement.getId(), userSubscription.getId(), periodStart))
                .user(user)
                .purpose(PaymentPurpose.RENEWAL)
                .provider(PaymentProviderType.TOSS_BILLING)
                .status(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION)
                .subscription(userSubscription.getSubscription())
                .userSubscription(userSubscription)
                .billingAgreement(agreement)
                .billingCycle(BillingCycle.MONTHLY)
                .billingPeriodStart(periodStart)
                .providerAttempt(1)
                .amount(MONTHLY_AMOUNT)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build());
        return new ReconciliationFixture(order, agreement.getId(), transactionID);
    }

    private User persistUser(String label, UserRole role) {
        return userRepository.saveAndFlush(User.builder()
                .nickname(label)
                .email(label + "@example.invalid")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(role)
                .build());
    }

    private Subscription persistPlan(String name, BigDecimal monthlyAmount) {
        return subscriptionRepository.saveAndFlush(Subscription.builder()
                .name(name)
                .description("Package G MySQL proof plan")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(monthlyAmount)
                .priceYearly(monthlyAmount.multiply(BigDecimal.TEN))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
    }

    private PaymentOrder reloadRenewalOrder(Long agreementID) {
        entityManager.clear();
        return paymentOrderRepository.findAll().stream()
                .filter(order -> order.getBillingAgreement() != null)
                .filter(order -> agreementID.equals(order.getBillingAgreement().getId()))
                .filter(order -> order.getPurpose() == PaymentPurpose.RENEWAL)
                .findFirst()
                .orElseThrow();
    }

    private PaymentOrder reloadOrder(String orderID) {
        entityManager.clear();
        return paymentOrderRepository.findByOrderId(orderID).orElseThrow();
    }

    private BillingAgreement reloadAgreement(Long agreementID) {
        entityManager.clear();
        return billingAgreementRepository.findById(agreementID).orElseThrow();
    }

    private UserSubscription reloadUserSubscription(Long subscriptionID) {
        entityManager.clear();
        return userSubscriptionRepository.findById(subscriptionID).orElseThrow();
    }

    private PaymentRefund reloadRefund(Long refundID) {
        entityManager.clear();
        return paymentRefundRepository.findById(refundID).orElseThrow();
    }

    private CustomUserDetails adminActor(Long adminID) {
        return CustomUserDetails.builder()
                .id(adminID)
                .email("package-g-admin@example.invalid")
                .role(UserRole.ADMIN)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    private void assertDisposableTarget() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (database == null || !DISPOSABLE_DATABASE.matcher(database).matches()) {
            throw new AssertionError("Datasource target failed the WI007 disposable-name guard.");
        }
    }

    private static <T> MysqlRaceTestSupport.RaceOutcome<T> onlySuccess(
            MysqlRaceTestSupport.RacePair<T> race) {
        List<MysqlRaceTestSupport.RaceOutcome<T>> successes = race.outcomes().stream()
                .filter(MysqlRaceTestSupport.RaceOutcome::succeeded)
                .toList();
        assertThat(successes).hasSize(1);
        return successes.get(0);
    }

    private static void assertExactLoser(
            MysqlRaceTestSupport.RacePair<?> race,
            BUSINESS_ERROR expected) {
        List<MysqlRaceTestSupport.RaceOutcome<?>> failures = new ArrayList<>();
        for (MysqlRaceTestSupport.RaceOutcome<?> outcome : race.outcomes()) {
            if (!outcome.succeeded()) {
                failures.add(outcome);
            }
        }
        assertThat(failures).hasSize(1);
        assertThat(MysqlRaceTestSupport.exactBusinessError(failures.get(0))).isEqualTo(expected);
    }

    private static void stopExecutor(ExecutorService executor) {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Executor did not stop within five seconds.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while stopping executor.", exception);
        }
    }

    record RenewalFixture(Long userID, Long userSubscriptionID, Long agreementID) {
    }

    record UpgradeFixture(Long userID, Long userSubscriptionID, Long targetPlanID, Long agreementID) {
    }

    record RefundFixture(
            Long userID,
            Long adminID,
            Long paymentOrderID,
            Long subscriptionPaymentID,
            Long refundID) {
    }

    record ReconciliationFixture(PaymentOrder order, Long agreementID, String transactionID) {
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestStatusLookupProvider testStatusLookupProvider() {
            return new TestStatusLookupProvider();
        }

        @Bean
        TestRefundProvider testRefundProvider() {
            return new TestRefundProvider();
        }
    }

    static class TestStatusLookupProvider implements PaymentStatusLookupProvider {

        private final Map<String, ProviderPaymentLookupResult> responses = new ConcurrentHashMap<>();
        private final List<Boolean> lookupTransactionStates = new CopyOnWriteArrayList<>();
        private final AtomicInteger lookupCalls = new AtomicInteger();
        private volatile CountDownLatch nextLookupEntered;
        private volatile CountDownLatch nextLookupRelease;

        void reset() {
            responses.clear();
            lookupTransactionStates.clear();
            lookupCalls.set(0);
            nextLookupEntered = null;
            nextLookupRelease = null;
        }

        void respondExact(PaymentOrder order) {
            String transactionID = "tx-mysql-r7";
            responses.put(order.getOrderId(), ProviderPaymentLookupResult.found(
                    getProviderType(),
                    order.getOrderId(),
                    transactionID,
                    "DONE",
                    order.getAmount(),
                    order.getCurrency(),
                    "{\"paymentKey\":\"" + transactionID + "\"}"));
        }

        void blockNextLookup(CountDownLatch entered, CountDownLatch release) {
            nextLookupEntered = entered;
            nextLookupRelease = release;
        }

        int lookupCalls() {
            return lookupCalls.get();
        }

        List<Boolean> lookupTransactionStates() {
            return List.copyOf(lookupTransactionStates);
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS_BILLING;
        }

        @Override
        @Transactional(propagation = Propagation.NEVER)
        public ProviderPaymentLookupResult findPaymentByOrderId(String orderId) {
            lookupCalls.incrementAndGet();
            lookupTransactionStates.add(TransactionSynchronizationManager.isActualTransactionActive());
            CountDownLatch entered = nextLookupEntered;
            CountDownLatch release = nextLookupRelease;
            nextLookupEntered = null;
            nextLookupRelease = null;
            if (entered != null) {
                entered.countDown();
            }
            if (release != null) {
                MysqlRaceTestSupport.await(
                        release,
                        Duration.ofSeconds(10),
                        "provider lookup release timed out");
            }
            return responses.get(orderId);
        }
    }

    static final class TestRefundProvider implements PaymentRefundProvider {

        private final AtomicInteger calls = new AtomicInteger();

        void reset() {
            calls.set(0);
        }

        int callCount() {
            return calls.get();
        }

        @Override
        public PaymentProviderType getProviderType() {
            return PaymentProviderType.TOSS_BILLING;
        }

        @Override
        public PaymentRefundProviderResult cancelPayment(PaymentRefundProviderCommand command) {
            calls.incrementAndGet();
            return PaymentRefundProviderResult.success("unexpected-provider-call", "{}");
        }
    }
}
