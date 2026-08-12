package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.PaymentSettlementImportAttemptRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
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
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.datasource.hikari.maximum-pool-size=6",
        "spring.datasource.hikari.connection-timeout=5000",
        "spring.datasource.hikari.transaction-isolation=TRANSACTION_REPEATABLE_READ"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
@EnabledIfEnvironmentVariable(
        named = "ATSTUDIO_SETTLEMENT_MYSQL_PROOF_ENABLED",
        matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
@DisplayName("WI-20260809-ATS-067 disposable MySQL settlement concurrency proof")
class AdminPaymentSettlementMysqlConcurrencyIntegrationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");

    @Autowired AdminPaymentSettlementService service;
    @Autowired PaymentSettlementRepository settlementRepository;
    @Autowired PaymentSettlementImportAttemptRepository attemptRepository;
    @Autowired PaymentOperationAuditLogRepository auditLogRepository;
    @Autowired UserRepository userRepository;
    @Autowired JdbcTemplate jdbcTemplate;
    @MockitoSpyBean AdminPaymentSettlementRowTransactionService rowTransactionService;

    @BeforeAll
    void verifyDisposableMySqlContract() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
        String isolation = jdbcTemplate.queryForObject("SELECT @@transaction_isolation", String.class);
        Integer innodbTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables "
                        + "WHERE table_schema = DATABASE() "
                        + "AND table_name IN ('payment_settlements', "
                        + "'payment_settlement_import_attempts', "
                        + "'payment_operation_audit_logs', 'users') "
                        + "AND UPPER(engine) = 'INNODB'",
                Integer.class);

        assertThat(database).matches(DISPOSABLE_DATABASE);
        assertThat(version).startsWith("8.");
        assertThat(isolation).isEqualToIgnoringCase("REPEATABLE-READ");
        assertThat(innodbTableCount).isEqualTo(4);
    }

    @BeforeEach
    void clearDisposableSettlementFixture() {
        jdbcTemplate.update("DELETE FROM payment_operation_audit_logs");
        jdbcTemplate.update("DELETE FROM payment_settlements");
        jdbcTemplate.update("DELETE FROM payment_settlement_import_attempts");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    @DisplayName("different operation keys with one dedup key retain one settlement and complementary outcomes")
    void concurrentDifferentOperationKeysWithSameDedupKeyAreComplementary() {
        User admin = admin("dedup");
        CustomUserDetails actor = actor(admin);
        String orderID = "ORDER-WI067-MYSQL-DEDUP";
        CountDownLatch bothRowsEntered = new CountDownLatch(2);
        doAnswer(invocation -> {
            bothRowsEntered.countDown();
            MysqlRaceTestSupport.await(
                    bothRowsEntered,
                    Duration.ofSeconds(5),
                    "both settlement rows did not reach the persistence race");
            return invocation.callRealMethod();
        }).when(rowTransactionService).persistImported(any(), any());

        MysqlRaceTestSupport.RacePair<AdminPaymentSettlementImportResponse> race =
                MysqlRaceTestSupport.runPair(
                        () -> importOne(actor, orderID, "61000000-0000-4000-8000-000000000001"),
                        () -> importOne(actor, orderID, "61000000-0000-4000-8000-000000000002"));

        race.outcomes().forEach(MysqlRaceTestSupport::assertSucceeded);
        List<AdminPaymentSettlementImportResponse> results = race.outcomes().stream()
                .map(MysqlRaceTestSupport.RaceOutcome::value)
                .toList();
        assertThat(results).extracting(AdminPaymentSettlementImportResponse::importedRows)
                .containsExactlyInAnyOrder(1, 0);
        assertThat(results).extracting(AdminPaymentSettlementImportResponse::skippedDuplicateRows)
                .containsExactlyInAnyOrder(0, 1);
        assertThat(results).allSatisfy(response -> {
            assertThat(response.totalRows()).isEqualTo(1);
            assertThat(response.failedRows()).isZero();
            assertThat(response.omittedErrorCount()).isZero();
            assertConserved(response);
        });
        assertThat(results.stream().filter(response -> response.importedRows() == 1).findFirst().orElseThrow())
                .satisfies(response -> assertThat(response.statusCounts())
                        .containsEntry(PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND.name(), 1)
                        .hasSize(1));
        assertThat(results.stream().filter(response -> response.skippedDuplicateRows() == 1)
                .findFirst().orElseThrow().statusCounts()).isEmpty();

        assertThat(settlementRepository.findAll()).singleElement().satisfies(settlement -> {
            assertThat(settlement.getOrderId()).isEqualTo(orderID);
            assertThat(results).extracting(AdminPaymentSettlementImportResponse::importBatchKey)
                    .contains(settlement.getImportBatchKey());
        });
        assertThat(importAudits()).singleElement();
        assertThat(attemptRepository.findAll()).hasSize(2).allSatisfy(attempt -> {
            assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
            assertThat(attempt.getTotalRows()).isEqualTo(1);
            assertThat(attempt.getImportedRows() + attempt.getDuplicateRows() + attempt.getFailedRows())
                    .isEqualTo(1);
        });
    }

    @Test
    @DisplayName("same owner and operation key create one attempt and never process twice")
    void concurrentSameOwnerAndOperationKeyCreateOneAttempt() {
        User admin = admin("same-operation");
        CustomUserDetails actor = actor(admin);
        String orderID = "ORDER-WI067-MYSQL-SAME-OP";
        String operationKey = "62000000-0000-4000-8000-000000000001";
        CountDownLatch loserObserved = new CountDownLatch(1);
        doAnswer(invocation -> {
            MysqlRaceTestSupport.await(
                    loserObserved,
                    Duration.ofSeconds(5),
                    "same-key loser did not observe the processing attempt");
            return invocation.callRealMethod();
        }).when(rowTransactionService).persistImported(any(), any());

        MysqlRaceTestSupport.RacePair<AdminPaymentSettlementImportResponse> race =
                MysqlRaceTestSupport.runPair(
                        () -> importSameKey(actor, orderID, operationKey, loserObserved),
                        () -> importSameKey(actor, orderID, operationKey, loserObserved));

        assertThat(race.outcomes().stream().filter(MysqlRaceTestSupport.RaceOutcome::succeeded))
                .singleElement()
                .satisfies(outcome -> {
                    assertThat(outcome.value().importedRows()).isEqualTo(1);
                    assertThat(outcome.value().skippedDuplicateRows()).isZero();
                    assertConserved(outcome.value());
                });
        assertThat(race.outcomes().stream().filter(outcome -> !outcome.succeeded()))
                .singleElement()
                .satisfies(outcome -> assertThat(MysqlRaceTestSupport.exactBusinessError(outcome))
                        .isEqualTo(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_IN_PROGRESS));
        assertThat(attemptRepository.findAll()).singleElement().satisfies(attempt -> {
            assertThat(attempt.getState()).isEqualTo(PaymentSettlementImportAttemptState.COMPLETED);
            assertThat(attempt.getTotalRows()).isEqualTo(1);
            assertThat(attempt.getImportedRows()).isEqualTo(1);
            assertThat(attempt.getDuplicateRows()).isZero();
            assertThat(attempt.getFailedRows()).isZero();
        });
        assertThat(settlementRepository.findAll()).singleElement()
                .satisfies(settlement -> assertThat(settlement.getOrderId()).isEqualTo(orderID));
        assertThat(importAudits()).singleElement();
    }

    @Test
    @DisplayName("concurrent IGNORE persists one decision and rejects the loser as invalid state")
    void concurrentIgnorePersistsOneDecisionAndAudit() {
        User firstAdmin = admin("ignore-first");
        User secondAdmin = admin("ignore-second");
        PaymentSettlement settlement = settlementRepository.saveAndFlush(PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(PaymentProviderType.TOSS)
                .status(PaymentSettlementStatus.MISMATCHED)
                .deduplicationKey("wi067-mysql-ignore-dedup")
                .importBatchKey("wi067-mysql-ignore-batch")
                .orderId("ORDER-WI067-MYSQL-IGNORE")
                .grossAmount(BigDecimal.valueOf(9900))
                .netSettlementAmount(BigDecimal.valueOf(9900))
                .settlementBaseDate(LocalDate.of(2026, 8, 13))
                .build());

        MysqlRaceTestSupport.RacePair<Void> race = MysqlRaceTestSupport.runPair(
                () -> ignore(settlement.getId(), firstAdmin, "first MySQL decision"),
                () -> ignore(settlement.getId(), secondAdmin, "second MySQL decision"));

        assertThat(race.outcomes().stream().filter(MysqlRaceTestSupport.RaceOutcome::succeeded))
                .singleElement();
        assertThat(race.outcomes().stream().filter(outcome -> !outcome.succeeded()))
                .singleElement()
                .satisfies(outcome -> assertThat(MysqlRaceTestSupport.exactBusinessError(outcome))
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));

        PaymentSettlement retained = settlementRepository.findWithGraphById(settlement.getId()).orElseThrow();
        List<PaymentOperationAuditLog> audits = ignoreAudits(settlement.getId());
        String expectedNote = retained.getIgnoredBy().getId().equals(firstAdmin.getId())
                ? "first MySQL decision"
                : "second MySQL decision";
        assertThat(retained.getStatus()).isEqualTo(PaymentSettlementStatus.IGNORED);
        assertThat(retained.getIgnoredBy().getId()).isIn(firstAdmin.getId(), secondAdmin.getId());
        assertThat(retained.getOperatorNote()).isEqualTo(expectedNote);
        assertThat(retained.getIgnoredAt()).isNotNull();
        assertThat(audits).singleElement().satisfies(audit -> {
            assertThat(audit.getBeforeStatus()).isEqualTo(PaymentSettlementStatus.MISMATCHED.name());
            assertThat(audit.getAfterStatus()).isEqualTo(PaymentSettlementStatus.IGNORED.name());
            assertThat(audit.getNote()).isEqualTo(expectedNote);
        });
    }

    private AdminPaymentSettlementImportResponse importOne(
            CustomUserDetails actor,
            String orderID,
            String operationKey) {
        return service.importSettlements(actor, csv(orderID), "MySQL concurrency proof", operationKey)
                .getData();
    }

    private AdminPaymentSettlementImportResponse importSameKey(
            CustomUserDetails actor,
            String orderID,
            String operationKey,
            CountDownLatch loserObserved) {
        try {
            return importOne(actor, orderID, operationKey);
        } catch (BusinessException exception) {
            loserObserved.countDown();
            throw exception;
        }
    }

    private Void ignore(Long settlementID, User admin, String note) {
        service.ignoreSettlement(
                settlementID,
                actor(admin),
                new AdminPaymentSettlementIgnoreRequest(note));
        return null;
    }

    private void assertConserved(AdminPaymentSettlementImportResponse response) {
        assertThat(response.totalRows()).isEqualTo(
                response.importedRows() + response.skippedDuplicateRows() + response.failedRows());
        assertThat(response.statusCounts().values().stream().mapToInt(Integer::intValue).sum())
                .isEqualTo(response.importedRows());
    }

    private User admin(String label) {
        return userRepository.saveAndFlush(User.builder()
                .nickname("wi067-" + label)
                .email("wi067-" + label + "@example.invalid")
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

    private MockMultipartFile csv(String orderID) {
        String content = "provider,provider_settlement_id,order_id,gross_amount,"
                + "net_settlement_amount,settlement_base_date\n"
                + "TOSS,WI067-MYSQL-SHARED," + orderID + ",9900,9900,2026-08-13\n";
        return new MockMultipartFile(
                "file",
                "wi067-mysql-settlement.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private List<PaymentOperationAuditLog> importAudits() {
        return auditLogRepository.findAll().stream()
                .filter(audit -> audit.getAction() == PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED)
                .toList();
    }

    private List<PaymentOperationAuditLog> ignoreAudits(Long settlementID) {
        return auditLogRepository.findAll().stream()
                .filter(audit -> audit.getAction() == PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED)
                .filter(audit -> settlementID.equals(audit.getTargetId()))
                .toList();
    }
}
