package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareRequest;
import com.atstudio.atstudio.dto.payment.BillingAgreementPrepareResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
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
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

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
        BillingAgreementPrepareTransactionService.class,
        BillingAgreementApplicationService.class,
        BillingAgreementPrepareMysqlConcurrencyIntegrationTest.ProviderConfiguration.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "ATSTUDIO_MYSQL_PROOF_ENABLED", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
@DisplayName("WI-20260809-ATS-033 disposable MySQL billing prepare concurrency proof")
class BillingAgreementPrepareMysqlConcurrencyIntegrationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");
    private static final String FIRST_RACE_KEY = "123e4567-e89b-42d3-a456-426614174001";
    private static final String EXISTING_AGREEMENT_RACE_KEY =
            "123e4567-e89b-42d3-a456-426614174002";
    private static final String CHANGED_PLAN_RACE_KEY =
            "123e4567-e89b-42d3-a456-426614174003";
    private static final BigDecimal FIRST_PLAN_AMOUNT = BigDecimal.valueOf(9900);
    private static final BigDecimal SECOND_PLAN_AMOUNT = BigDecimal.valueOf(19900);
    private static final List<String> TESTED_TABLES = List.of(
            "users",
            "subscriptions",
            "user_subscriptions",
            "billing_agreements",
            "payment_orders");
    private static final List<String> MUTATED_TABLES = List.of(
            "payment_orders",
            "billing_agreements",
            "user_subscriptions",
            "subscriptions",
            "users");

    @Autowired BillingAgreementApplicationService service;
    @Autowired PaymentCommandKeyFactory commandKeyFactory;
    @Autowired UserRepository userRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired UserSubscriptionRepository userSubscriptionRepository;
    @MockitoSpyBean BillingAgreementRepository billingAgreementRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;
    @Autowired TestPrepareProvider provider;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired EntityManagerFactory entityManagerFactory;

    @MockitoBean BillingCustomerKeyGenerator billingCustomerKeyGenerator;
    @MockitoBean BillingKeyCrypto billingKeyCrypto;
    @MockitoBean PaymentCommandTransactionService paymentCommandTransactionService;
    @MockitoBean BillingAgreementCleanupTransactionService billingAgreementCleanupTransactionService;
    @MockitoBean BillingAgreementCleanupProviderExecutor billingAgreementCleanupProviderExecutor;
    @MockitoBean PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    @MockitoBean PlaylistService playlistService;
    @MockitoBean EmailService emailService;

    private final AtomicInteger customerSequence = new AtomicInteger();
    private final AtomicReference<FirstAgreementRaceProbe> firstAgreementRaceProbe =
            new AtomicReference<>();
    private Answer<Object> billingAgreementRepositoryDelegate;

    @BeforeAll
    void verifyMySqlAndInnoDbContract() {
        assertDisposableTarget();
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        if (version == null || !version.startsWith("8.")) {
            throw new AssertionError("MySQL major version 8 is required for WI-033.");
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
                (RowCallbackHandler) resultSet -> engines.put(
                        resultSet.getString(1).toLowerCase(Locale.ROOT),
                        resultSet.getString(2)));
        if (engines.size() != TESTED_TABLES.size()
                || TESTED_TABLES.stream()
                .anyMatch(table -> !"InnoDB".equalsIgnoreCase(engines.get(table)))) {
            throw new AssertionError("Every WI-033 table must exist and use InnoDB.");
        }

        System.out.println("mysql.version=" + version);
        System.out.println("mysql.transactionIsolation=" + isolation);
        System.out.println("mysql.engineContract=InnoDB; testedTableCount=" + TESTED_TABLES.size());
    }

    @BeforeEach
    void resetDisposableFixtures() {
        firstAgreementRaceProbe.set(null);
        truncateDisposableFixtures();
        provider.reset();
        customerSequence.set(0);
        given(billingCustomerKeyGenerator.generate())
                .willAnswer(ignored -> "ats_mysql_prepare_customer_"
                        + customerSequence.incrementAndGet());
        configureBillingAgreementRepositorySpy();
    }

    @AfterEach
    void cleanDisposableFixtures() {
        firstAgreementRaceProbe.set(null);
        truncateDisposableFixtures();
    }

    @Test
    @DisplayName("race 1: same key and tuple create one agreement and one committed order")
    void race1_firstAgreementConvergesToCommittedWinner() {
        Fixture fixture = persistFixture("wi33-r1", FIRST_PLAN_AMOUNT);
        FirstAgreementRaceProbe raceProbe =
                new FirstAgreementRaceProbe(
                        entityManagerFactory,
                        billingAgreementRepositoryDelegate);
        firstAgreementRaceProbe.set(raceProbe);

        MysqlRaceTestSupport.RacePair<BillingAgreementPrepareResponse> race;
        try {
            race = MysqlRaceTestSupport.runPair(
                    () -> prepare(fixture.userID(), fixture.planID(), FIRST_RACE_KEY),
                    () -> prepare(fixture.userID(), fixture.planID(), FIRST_RACE_KEY));
        } finally {
            firstAgreementRaceProbe.compareAndSet(raceProbe, null);
            raceProbe.releaseWaiters();
        }
        List<BillingAgreementPrepareResponse> responses = requireTwoSuccesses(race);

        assertThat(responses.get(1)).isEqualTo(responses.get(0));
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        assertThat(userSubscriptionRepository.count()).isZero();
        PersistedOrder winner = assertCommittedWinner(
                responses.get(0),
                fixture.userID(),
                fixture.planID(),
                FIRST_PLAN_AMOUNT,
                FIRST_RACE_KEY);
        String expectedCommandKey = commandKeyFactory.billingAgreementPrepare(
                fixture.userID(), FIRST_RACE_KEY);
        assertThat(jdbcTemplate.queryForList(
                "SELECT command_key FROM payment_orders", String.class))
                .containsExactly(expectedCommandKey);

        assertThat(raceProbe.emptyProbeCount()).isEqualTo(2);
        assertThat(raceProbe.initialEnsureTransactions()).hasSize(2);
        assertThat(raceProbe.initialEnsureTransactions().get(0))
                .isNotSameAs(raceProbe.initialEnsureTransactions().get(1));
        assertThat(raceProbe.insertPathCount()).isEqualTo(2);
        assertThat(raceProbe.firstInsertFlushed()).isTrue();
        assertThat(raceProbe.secondInsertPathEntered()).isTrue();
        assertThat(raceProbe.secondRealInsertStarted()).isTrue();
        assertThat(raceProbe.secondRealInsertFinished()).isTrue();
        assertThat(raceProbe.unexpectedInsertFailures()).isEmpty();
        assertThat(raceProbe.namedUniqueViolations()).hasSize(1);
        assertNamedAgreementUniqueViolation(raceProbe.namedUniqueViolations().get(0));
        assertThat(raceProbe.freshRereadCount()).isGreaterThanOrEqualTo(1);
        assertThat(raceProbe.winnerAgreementID())
                .isEqualTo(winner.billingAgreementID());
        assertThat(raceProbe.freshRereadAgreementIDs())
                .isNotEmpty()
                .containsOnly(winner.billingAgreementID());
        assertThat(provider.transactionStates()).containsExactly(false, false);
    }

    @Test
    @DisplayName("race 2: same key and tuple with an existing agreement create one order")
    void race2_existingAgreementConvergesToCommittedWinner() {
        Fixture fixture = persistFixture("wi33-r2", FIRST_PLAN_AMOUNT);
        BillingAgreement existingAgreement = billingAgreementRepository.saveAndFlush(
                BillingAgreement.builder()
                        .user(userRepository.findById(fixture.userID()).orElseThrow())
                        .provider(PaymentProviderType.TOSS)
                        .providerCustomerKey("ats_mysql_existing_customer")
                        .build());

        MysqlRaceTestSupport.RacePair<BillingAgreementPrepareResponse> race =
                MysqlRaceTestSupport.runPair(
                        () -> prepare(
                                fixture.userID(),
                                fixture.planID(),
                                EXISTING_AGREEMENT_RACE_KEY),
                        () -> prepare(
                                fixture.userID(),
                                fixture.planID(),
                                EXISTING_AGREEMENT_RACE_KEY));
        List<BillingAgreementPrepareResponse> responses = requireTwoSuccesses(race);

        assertThat(responses.get(1)).isEqualTo(responses.get(0));
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);
        PersistedOrder winner = assertCommittedWinner(
                responses.get(0),
                fixture.userID(),
                fixture.planID(),
                FIRST_PLAN_AMOUNT,
                EXISTING_AGREEMENT_RACE_KEY);
        assertThat(winner.billingAgreementID()).isEqualTo(existingAgreement.getId());
        assertThat(provider.transactionStates()).hasSize(2).containsOnly(false);
    }

    @Test
    @DisplayName("race 3: one of two changed-plan attempts wins and the other conflicts")
    void race3_changedPlanProducesOneWinnerAndOneExactConflict() {
        User user = persistUser("wi33-r3-user");
        Subscription firstPlan = persistPlan("wi33-r3-first", FIRST_PLAN_AMOUNT);
        Subscription secondPlan = persistPlan("wi33-r3-second", SECOND_PLAN_AMOUNT);

        MysqlRaceTestSupport.RacePair<BillingAgreementPrepareResponse> race =
                MysqlRaceTestSupport.runPair(
                        () -> prepare(user.getId(), firstPlan.getId(), CHANGED_PLAN_RACE_KEY),
                        () -> prepare(user.getId(), secondPlan.getId(), CHANGED_PLAN_RACE_KEY));
        List<MysqlRaceTestSupport.RaceOutcome<BillingAgreementPrepareResponse>> successes =
                race.outcomes().stream()
                        .filter(MysqlRaceTestSupport.RaceOutcome::succeeded)
                        .toList();
        List<MysqlRaceTestSupport.RaceOutcome<BillingAgreementPrepareResponse>> failures =
                race.outcomes().stream()
                        .filter(outcome -> !outcome.succeeded())
                        .toList();

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(1);
        assertThat(MysqlRaceTestSupport.exactBusinessError(failures.get(0)))
                .isEqualTo(BUSINESS_ERROR.PAYMENT_PREPARE_ATTEMPT_CONFLICT);
        assertThat(billingAgreementRepository.count()).isEqualTo(1);
        assertThat(paymentOrderRepository.count()).isEqualTo(1);

        BillingAgreementPrepareResponse winnerResponse = successes.get(0).value();
        Long losingPlanID = winnerResponse.subscriptionId().equals(firstPlan.getId())
                ? secondPlan.getId()
                : firstPlan.getId();
        BigDecimal winnerAmount = winnerResponse.subscriptionId().equals(firstPlan.getId())
                ? FIRST_PLAN_AMOUNT
                : SECOND_PLAN_AMOUNT;
        assertThat(winnerResponse.subscriptionId())
                .isIn(firstPlan.getId(), secondPlan.getId())
                .isNotEqualTo(losingPlanID);

        PersistedOrder winner = assertCommittedWinner(
                winnerResponse,
                user.getId(),
                winnerResponse.subscriptionId(),
                winnerAmount,
                CHANGED_PLAN_RACE_KEY);
        assertThat(winner.subscriptionID()).isNotEqualTo(losingPlanID);
        assertThat(winner.commandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(
                        user.getId(), CHANGED_PLAN_RACE_KEY));
        assertThat(provider.transactionStates()).containsExactly(false);
    }

    @SuppressWarnings("unchecked")
    private void configureBillingAgreementRepositorySpy() {
        Answer<Object> repositoryDelegate =
                (Answer<Object>) Mockito.mockingDetails(billingAgreementRepository)
                        .getMockCreationSettings()
                        .getDefaultAnswer();
        billingAgreementRepositoryDelegate = repositoryDelegate;
        doAnswer(invocation -> {
            Optional<BillingAgreement> existing =
                    callRealAgreementLookup(invocation, repositoryDelegate);
            FirstAgreementRaceProbe raceProbe = firstAgreementRaceProbe.get();
            if (raceProbe != null) {
                raceProbe.afterNonLockingProbe(existing);
            }
            return existing;
        }).when(billingAgreementRepository).findByUserAndProvider(
                any(User.class), eq(PaymentProviderType.TOSS));

        doAnswer(invocation -> {
            FirstAgreementRaceProbe raceProbe = firstAgreementRaceProbe.get();
            if (raceProbe == null) {
                return repositoryDelegate.answer(invocation);
            }
            return raceProbe.saveAndFlush(invocation);
        }).when(billingAgreementRepository).saveAndFlush(any(BillingAgreement.class));
    }

    @SuppressWarnings("unchecked")
    private Optional<BillingAgreement> callRealAgreementLookup(
            InvocationOnMock invocation,
            Answer<Object> repositoryDelegate)
            throws Throwable {
        return (Optional<BillingAgreement>) repositoryDelegate.answer(invocation);
    }

    private void assertNamedAgreementUniqueViolation(
            DataIntegrityViolationException exception) {
        assertThat(normalizedMostSpecificCauseMessage(exception))
                .contains("uq_billing_agreements_user_provider")
                .doesNotContain("deadlock", "lock wait timeout", "timeout");
    }

    private static boolean namesAgreementUniqueConstraint(
            DataIntegrityViolationException exception) {
        return normalizedMostSpecificCauseMessage(exception)
                .contains("uq_billing_agreements_user_provider");
    }

    private static String normalizedMostSpecificCauseMessage(
            DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        return message == null ? "" : message.toLowerCase(Locale.ROOT);
    }

    private BillingAgreementPrepareResponse prepare(Long userID, Long planID, String key) {
        return service.prepareBillingAgreement(
                userDetails(userID),
                new BillingAgreementPrepareRequest(
                        planID,
                        BillingCycle.MONTHLY,
                        PaymentPurpose.SUBSCRIBE),
                key);
    }

    private List<BillingAgreementPrepareResponse> requireTwoSuccesses(
            MysqlRaceTestSupport.RacePair<BillingAgreementPrepareResponse> race) {
        List<BillingAgreementPrepareResponse> responses = new ArrayList<>();
        for (MysqlRaceTestSupport.RaceOutcome<BillingAgreementPrepareResponse> outcome
                : race.outcomes()) {
            MysqlRaceTestSupport.assertSucceeded(outcome);
            responses.add(outcome.value());
        }
        assertThat(responses).hasSize(2);
        return responses;
    }

    private PersistedOrder assertCommittedWinner(
            BillingAgreementPrepareResponse response,
            Long userID,
            Long planID,
            BigDecimal amount,
            String rawKey) {
        PersistedOrder order = jdbcTemplate.queryForObject(
                "SELECT order_id, command_key, user_id, subscription_id, "
                        + "billing_agreement_id, purpose, billing_cycle, amount, status "
                        + "FROM payment_orders WHERE order_id = ?",
                (resultSet, rowNumber) -> persistedOrder(resultSet),
                response.orderId());

        assertThat(order).isNotNull();
        assertThat(order.orderID()).isEqualTo(response.orderId());
        assertThat(order.commandKey())
                .isEqualTo(commandKeyFactory.billingAgreementPrepare(userID, rawKey))
                .doesNotContain(rawKey);
        assertThat(order.userID()).isEqualTo(userID);
        assertThat(order.subscriptionID()).isEqualTo(planID);
        assertThat(order.billingAgreementID()).isPositive();
        assertThat(order.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE.name());
        assertThat(order.billingCycle()).isEqualTo(BillingCycle.MONTHLY.name());
        assertThat(order.amount()).isEqualByComparingTo(amount);
        assertThat(order.status()).isEqualTo(PaymentOrderStatus.IN_PROGRESS.name());
        assertThat(response.subscriptionId()).isEqualTo(planID);
        assertThat(response.purpose()).isEqualTo(PaymentPurpose.SUBSCRIBE);
        assertThat(response.billingCycle()).isEqualTo(BillingCycle.MONTHLY);
        assertThat(response.amount()).isEqualByComparingTo(amount);
        return order;
    }

    private PersistedOrder persistedOrder(ResultSet resultSet) throws SQLException {
        return new PersistedOrder(
                resultSet.getString("order_id"),
                resultSet.getString("command_key"),
                resultSet.getLong("user_id"),
                resultSet.getLong("subscription_id"),
                resultSet.getLong("billing_agreement_id"),
                resultSet.getString("purpose"),
                resultSet.getString("billing_cycle"),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("status"));
    }

    private Fixture persistFixture(String label, BigDecimal amount) {
        User user = persistUser(label + "-user");
        Subscription plan = persistPlan(label + "-plan", amount);
        return new Fixture(user.getId(), plan.getId());
    }

    private User persistUser(String label) {
        return userRepository.saveAndFlush(User.builder()
                .nickname(label)
                .email(label + "@example.invalid")
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build());
    }

    private Subscription persistPlan(String name, BigDecimal monthlyAmount) {
        return subscriptionRepository.saveAndFlush(Subscription.builder()
                .name(name)
                .description("WI-033 disposable MySQL prepare proof")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(monthlyAmount)
                .priceYearly(monthlyAmount.multiply(BigDecimal.TEN))
                .downloadPerDay(10)
                .maxWhitelistChannels(3)
                .maxPlaylists(5)
                .build());
    }

    private CustomUserDetails userDetails(Long userID) {
        return CustomUserDetails.builder()
                .id(userID)
                .email("mysql-prepare@example.invalid")
                .password("pw")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }

    private void truncateDisposableFixtures() {
        assertDisposableTarget();
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            assertDisposableTarget(connection);
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
    }

    private void assertDisposableTarget() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        assertDisposableDatabaseName(database);
    }

    private void assertDisposableTarget(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT DATABASE()")) {
            String database = resultSet.next() ? resultSet.getString(1) : null;
            assertDisposableDatabaseName(database);
        }
    }

    private void assertDisposableDatabaseName(String database) {
        if (database == null || !DISPOSABLE_DATABASE.matcher(database).matches()) {
            throw new AssertionError("Datasource target failed the guarded disposable-name check.");
        }
    }

    private record Fixture(Long userID, Long planID) {
    }

    private record PersistedOrder(
            String orderID,
            String commandKey,
            Long userID,
            Long subscriptionID,
            Long billingAgreementID,
            String purpose,
            String billingCycle,
            BigDecimal amount,
            String status) {
    }

    private static final class FirstAgreementRaceProbe {

        private static final Duration PHASE_TIMEOUT = Duration.ofSeconds(10);

        private final EntityManagerFactory entityManagerFactory;
        private final Answer<Object> repositoryDelegate;
        private final CountDownLatch bothEmptyProbes = new CountDownLatch(2);
        private final CountDownLatch firstInsertFlushed = new CountDownLatch(1);
        private final CountDownLatch secondInsertPathEntered = new CountDownLatch(1);
        private final CountDownLatch secondRealInsertStarted = new CountDownLatch(1);
        private final CountDownLatch secondRealInsertFinished = new CountDownLatch(1);
        private final AtomicBoolean firstInsertFlushObserved = new AtomicBoolean();
        private final AtomicBoolean secondInsertPathObserved = new AtomicBoolean();
        private final AtomicBoolean secondRealInsertStartObserved = new AtomicBoolean();
        private final AtomicBoolean secondRealInsertFinishObserved = new AtomicBoolean();
        private final AtomicInteger emptyProbeCount = new AtomicInteger();
        private final AtomicInteger insertPathCount = new AtomicInteger();
        private final AtomicInteger freshRereadCount = new AtomicInteger();
        private final Map<Thread, Object> initialTransactionsByThread =
                new ConcurrentHashMap<>();
        private final java.util.Set<Thread> namedUniqueLoserThreads =
                ConcurrentHashMap.newKeySet();
        private final List<Object> initialEnsureTransactions =
                new CopyOnWriteArrayList<>();
        private final List<Long> freshRereadAgreementIDs =
                new CopyOnWriteArrayList<>();
        private final List<DataIntegrityViolationException> namedUniqueViolations =
                new CopyOnWriteArrayList<>();
        private final List<Throwable> unexpectedInsertFailures =
                new CopyOnWriteArrayList<>();
        private final AtomicReference<Long> winnerAgreementID = new AtomicReference<>();

        private FirstAgreementRaceProbe(
                EntityManagerFactory entityManagerFactory,
                Answer<Object> repositoryDelegate) {
            this.entityManagerFactory = entityManagerFactory;
            this.repositoryDelegate = repositoryDelegate;
        }

        private void afterNonLockingProbe(Optional<BillingAgreement> existing) {
            Thread thread = Thread.currentThread();
            Object transaction = currentTransaction();
            if (namedUniqueLoserThreads.contains(thread)) {
                if (existing.isEmpty()) {
                    throw new AssertionError(
                            "The duplicate loser did not reread the committed agreement.");
                }
                Object failedTransaction = initialTransactionsByThread.get(thread);
                if (failedTransaction == null || failedTransaction == transaction) {
                    throw new AssertionError(
                            "The duplicate loser reread did not use a fresh transaction.");
                }
                freshRereadAgreementIDs.add(existing.orElseThrow().getId());
                freshRereadCount.incrementAndGet();
                return;
            }

            if (existing.isPresent()) {
                throw new AssertionError(
                        "Both first-agreement transactions must observe absence before insert.");
            }
            int probeOrdinal = emptyProbeCount.incrementAndGet();
            if (probeOrdinal > 2
                    || initialTransactionsByThread.putIfAbsent(thread, transaction) != null) {
                throw new AssertionError("Unexpected first-agreement probe sequence.");
            }
            initialEnsureTransactions.add(transaction);
            bothEmptyProbes.countDown();
            MysqlRaceTestSupport.await(
                    bothEmptyProbes,
                    PHASE_TIMEOUT,
                    "both ensure transactions did not complete their empty probes");
        }

        private BillingAgreement saveAndFlush(InvocationOnMock invocation) throws Throwable {
            requireInitialEnsureTransaction();
            int insertOrdinal = insertPathCount.incrementAndGet();
            if (insertOrdinal == 1) {
                return insertAndHoldWinner(invocation);
            }
            if (insertOrdinal == 2) {
                return insertAndCaptureDuplicateLoser(invocation);
            }
            throw new AssertionError("More than two first-agreement insert paths were entered.");
        }

        private BillingAgreement insertAndHoldWinner(InvocationOnMock invocation) throws Throwable {
            BillingAgreement inserted;
            try {
                inserted = callRealSaveAndFlush(invocation);
            } catch (Throwable failure) {
                unexpectedInsertFailures.add(failure);
                throw failure;
            }
            if (inserted.getId() == null
                    || !winnerAgreementID.compareAndSet(null, inserted.getId())) {
                AssertionError failure =
                        new AssertionError("The first real agreement flush did not identify one winner.");
                unexpectedInsertFailures.add(failure);
                throw failure;
            }

            firstInsertFlushObserved.set(true);
            firstInsertFlushed.countDown();
            // Repository return is the release point for the surrounding ensure transaction commit.
            MysqlRaceTestSupport.await(
                    secondInsertPathEntered,
                    PHASE_TIMEOUT,
                    "the duplicate agreement insert path was not entered");
            MysqlRaceTestSupport.await(
                    secondRealInsertStarted,
                    PHASE_TIMEOUT,
                    "the duplicate agreement did not reach the real insert boundary");
            if (secondRealInsertFinished.getCount() == 0) {
                throw new AssertionError(
                        "The duplicate insert finished before the winner commit release.");
            }
            return inserted;
        }

        private BillingAgreement insertAndCaptureDuplicateLoser(InvocationOnMock invocation)
                throws Throwable {
            MysqlRaceTestSupport.await(
                    firstInsertFlushed,
                    PHASE_TIMEOUT,
                    "the winner agreement insert did not flush before the duplicate insert");
            secondInsertPathObserved.set(true);
            secondInsertPathEntered.countDown();
            secondRealInsertStartObserved.set(true);
            secondRealInsertStarted.countDown();
            try {
                return callRealSaveAndFlush(invocation);
            } catch (DataIntegrityViolationException exception) {
                if (namesAgreementUniqueConstraint(exception)) {
                    namedUniqueViolations.add(exception);
                    namedUniqueLoserThreads.add(Thread.currentThread());
                } else {
                    unexpectedInsertFailures.add(exception);
                }
                throw exception;
            } catch (Throwable failure) {
                unexpectedInsertFailures.add(failure);
                throw failure;
            } finally {
                secondRealInsertFinishObserved.set(true);
                secondRealInsertFinished.countDown();
            }
        }

        private BillingAgreement callRealSaveAndFlush(InvocationOnMock invocation)
                throws Throwable {
            return (BillingAgreement) repositoryDelegate.answer(invocation);
        }

        private void requireInitialEnsureTransaction() {
            Object initialTransaction = initialTransactionsByThread.get(Thread.currentThread());
            if (initialTransaction == null || initialTransaction != currentTransaction()) {
                throw new AssertionError(
                        "Agreement insert did not remain in its probed ensure transaction.");
            }
        }

        private Object currentTransaction() {
            if (!TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new AssertionError("Agreement repository phase ran outside a transaction.");
            }
            Object transaction =
                    TransactionSynchronizationManager.getResource(entityManagerFactory);
            if (transaction == null) {
                throw new AssertionError("No transaction-scoped EntityManager was bound.");
            }
            return transaction;
        }

        private void releaseWaiters() {
            release(bothEmptyProbes);
            release(firstInsertFlushed);
            release(secondInsertPathEntered);
            release(secondRealInsertStarted);
            release(secondRealInsertFinished);
        }

        private void release(CountDownLatch latch) {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
        }

        private int emptyProbeCount() {
            return emptyProbeCount.get();
        }

        private List<Object> initialEnsureTransactions() {
            return List.copyOf(initialEnsureTransactions);
        }

        private int insertPathCount() {
            return insertPathCount.get();
        }

        private boolean firstInsertFlushed() {
            return firstInsertFlushObserved.get();
        }

        private boolean secondInsertPathEntered() {
            return secondInsertPathObserved.get();
        }

        private boolean secondRealInsertStarted() {
            return secondRealInsertStartObserved.get();
        }

        private boolean secondRealInsertFinished() {
            return secondRealInsertFinishObserved.get();
        }

        private int freshRereadCount() {
            return freshRereadCount.get();
        }

        private List<Long> freshRereadAgreementIDs() {
            return List.copyOf(freshRereadAgreementIDs);
        }

        private List<DataIntegrityViolationException> namedUniqueViolations() {
            return List.copyOf(namedUniqueViolations);
        }

        private List<Throwable> unexpectedInsertFailures() {
            return List.copyOf(unexpectedInsertFailures);
        }

        private Long winnerAgreementID() {
            return winnerAgreementID.get();
        }
    }

    @TestConfiguration
    static class ProviderConfiguration {

        @Bean
        TestPrepareProvider testPrepareProvider() {
            return new TestPrepareProvider();
        }
    }

    static class TestPrepareProvider implements RecurringPaymentProvider {

        private final List<Boolean> transactionStates = new CopyOnWriteArrayList<>();

        void reset() {
            transactionStates.clear();
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
            return true;
        }

        @Override
        @Transactional(propagation = Propagation.NEVER)
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
