package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Payment database integrity contract tests")
class PaymentDatabaseIntegrityContractTest {

    private static final Path FRESH_SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path MANUAL_PATCH = Path.of(
            "src/main/resources/db/manual/20260714_payment_db_integrity.sql");
    private static final Path RECONCILIATION_INDEX_PATCH = Path.of(
            "src/main/resources/db/manual/20260716_payment_reconciliation_indexes.sql");

    @Test
    @DisplayName("PaymentOrder exposes the command lifecycle mapping")
    void paymentOrderMapsCommandLifecycle() throws NoSuchFieldException {
        assertThat(PaymentOrderStatus.values()).containsExactly(
                PaymentOrderStatus.READY,
                PaymentOrderStatus.IN_PROGRESS,
                PaymentOrderStatus.PROCESSING,
                PaymentOrderStatus.PROVIDER_SUCCEEDED,
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                PaymentOrderStatus.DONE,
                PaymentOrderStatus.FAILED,
                PaymentOrderStatus.CANCELLED,
                PaymentOrderStatus.EXPIRED);

        assertColumn(PaymentOrder.class, "commandKey", "command_key", 191, true);
        assertColumn(PaymentOrder.class, "billingPeriodStart", "billing_period_start", 255, true);
        assertColumn(PaymentOrder.class, "providerAttempt", "provider_attempt", 255, false);
        assertColumn(PaymentOrder.class, "providerIdempotencyKey", "provider_idempotency_key", 100, true);
        assertColumn(PaymentOrder.class, "processingStartedAt", "processing_started_at", 255, true);
        assertColumn(
                PaymentOrder.class,
                "upgradeTargetBillingCycle",
                "upgrade_target_billing_cycle",
                10,
                true);

        Table table = PaymentOrder.class.getAnnotation(Table.class);
        assertUniqueConstraint(table, "uq_payment_orders_command_key", "command_key");
        assertUniqueConstraint(
                table,
                "uq_payment_orders_provider_attempt_key",
                "provider",
                "provider_idempotency_key");
        assertUniqueConstraint(
                table,
                "uq_payment_orders_renewal_period",
                "billing_agreement_id",
                "user_subscription_id",
                "purpose",
                "billing_period_start");
        assertIndex(table, "idx_payment_orders_status_processing", "status,processing_started_at");
        assertIndex(
                table,
                "idx_payment_orders_local_reconciliation",
                "status,id,purpose");
    }

    @Test
    @DisplayName("Package A entities expose retry, cleanup, upgrade, and refund lease mappings")
    void packageAEntitiesMapAdditiveContract() throws NoSuchFieldException {
        assertThat(BillingKeyCleanupStatus.values()).containsExactly(
                BillingKeyCleanupStatus.NONE,
                BillingKeyCleanupStatus.REQUIRED,
                BillingKeyCleanupStatus.PROCESSING,
                BillingKeyCleanupStatus.PENDING_PROVIDER_CONFIRMATION,
                BillingKeyCleanupStatus.FAILED);

        assertColumn(BillingAgreement.class, "renewalRetryAt", "renewal_retry_at", 255, true);
        assertColumn(
                BillingAgreement.class,
                "billingKeyCleanupStatus",
                "billing_key_cleanup_status",
                40,
                false);
        assertColumn(
                BillingAgreement.class,
                "billingKeyCleanupStartedAt",
                "billing_key_cleanup_started_at",
                255,
                true);
        Table agreementTable = BillingAgreement.class.getAnnotation(Table.class);
        assertIndex(
                agreementTable,
                "idx_billing_agreements_renewal_retry",
                "status,renewal_retry_at,id");
        assertIndex(
                agreementTable,
                "idx_billing_agreements_cleanup",
                "billing_key_cleanup_status,billing_key_cleanup_started_at,id");
        assertIndex(
                agreementTable,
                "idx_billing_agreements_local_reconciliation",
                "status,id");

        assertColumn(PaymentRefund.class, "processingStartedAt", "processing_started_at", 255, true);
        Table refundTable = PaymentRefund.class.getAnnotation(Table.class);
        assertIndex(
                refundTable,
                "idx_payment_refunds_status_processing",
                "status,processing_started_at,id");
    }

    @Test
    @DisplayName("renewal failure preserves period identity and cleanup transitions require the active lease")
    void billingAgreementEnforcesPackageAStateTransitions() {
        LocalDate billingPeriodStart = LocalDate.of(2026, 7, 15);
        LocalDate retryAt = billingPeriodStart.plusDays(1);
        LocalDateTime cleanupStartedAt = LocalDateTime.of(2026, 7, 15, 12, 0, 0, 123_000_000);
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();
        agreement.activate("ciphertext", "fingerprint", "CARD", "masked", billingPeriodStart);
        agreement.recordSuccessfulCharge(billingPeriodStart);

        agreement.recordFailedCharge(retryAt);

        assertThat(agreement.getNextBillingAt()).isEqualTo(billingPeriodStart);
        assertThat(agreement.getRenewalRetryAt()).isEqualTo(retryAt);
        assertThat(agreement.getFailureCount()).isEqualTo(1);

        agreement.cancel();
        agreement.claimBillingKeyCleanup(cleanupStartedAt);

        LocalDateTime persistedLease = cleanupStartedAt.withNano(0);
        assertThat(agreement.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.PROCESSING);
        assertThat(agreement.getBillingKeyCleanupStartedAt()).isEqualTo(persistedLease);
        assertThat(agreement.isBillingKeyCleanupProcessingStale(persistedLease)).isTrue();
        assertThatThrownBy(() -> agreement.markBillingKeyCleanupFailed(persistedLease.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class);

        agreement.markBillingKeyCleanupPendingProviderConfirmation(persistedLease);
        agreement.markBillingKeyCleanupRequired();
        agreement.claimBillingKeyCleanup(cleanupStartedAt.plusMinutes(16));
        agreement.markBillingKeyCleanupSucceeded(cleanupStartedAt.plusMinutes(16));

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(agreement.getBillingKeyCleanupStatus()).isEqualTo(BillingKeyCleanupStatus.NONE);
        assertThat(agreement.getBillingKeyCleanupStartedAt()).isNull();
        assertThat(agreement.getBillingKeyCiphertext()).isNull();
        assertThat(agreement.getNextBillingAt()).isEqualTo(billingPeriodStart);
        assertThat(agreement.getLastChargedAt()).isNotNull();
    }

    @Test
    @DisplayName("upgrade claims require a target cycle and reconciliation accepts only stale processing")
    void paymentOrderEnforcesUpgradeAndReconciliationContract() {
        LocalDateTime claimedAt = LocalDateTime.of(2026, 7, 15, 12, 0);
        PaymentOrder missingTarget = PaymentOrder.builder()
                .purpose(PaymentPurpose.UPGRADE)
                .build();
        assertThatThrownBy(() -> missingTarget.claimProviderAttempt("upgrade-command", "attempt-1", claimedAt))
                .isInstanceOf(IllegalStateException.class);

        PaymentOrder order = PaymentOrder.builder()
                .purpose(PaymentPurpose.UPGRADE)
                .upgradeTargetBillingCycle(BillingCycle.YEARLY)
                .build();
        order.claimProviderAttempt("upgrade-command", "attempt-1", claimedAt);

        assertThatThrownBy(() -> order.markProviderSucceededFromReconciliation(
                "transaction-1",
                "sanitized",
                claimedAt.minusSeconds(1)))
                .isInstanceOf(IllegalStateException.class);

        order.markProviderSucceededFromReconciliation(
                "transaction-1",
                "sanitized",
                claimedAt);

        assertThat(order.getStatus()).isEqualTo(PaymentOrderStatus.PROVIDER_SUCCEEDED);
        assertThat(order.getUpgradeTargetBillingCycle()).isEqualTo(BillingCycle.YEARLY);
        assertThat(order.getPgTransactionId()).isEqualTo("transaction-1");
        assertThat(order.getProcessingStartedAt()).isNull();
    }

    @Test
    @DisplayName("refund result writers reject an old lease and clear the current lease")
    void paymentRefundFencesResultWriters() {
        LocalDateTime firstClaim = LocalDateTime.of(2026, 7, 15, 12, 0, 0, 987_000_000);
        LocalDateTime secondClaim = firstClaim.plusMinutes(16);
        PaymentRefund refund = PaymentRefund.builder()
                .status(PaymentRefundStatus.APPROVED)
                .build();

        refund.markProcessing(null, firstClaim);
        LocalDateTime firstLease = firstClaim.withNano(0);
        refund.reclaimProcessing(null, firstLease, secondClaim);

        assertThatThrownBy(() -> refund.markSucceeded("refund-transaction", "sanitized", firstLease))
                .isInstanceOf(IllegalStateException.class);

        refund.markSucceeded("refund-transaction", "sanitized", secondClaim);

        assertThat(refund.getStatus()).isEqualTo(PaymentRefundStatus.SUCCEEDED);
        assertThat(refund.getProcessingStartedAt()).isNull();
        assertThat(refund.getProviderRefundTransactionId()).isEqualTo("refund-transaction");
    }

    @Test
    @DisplayName("SubscriptionPayment maps one local finalization per order and provider transaction")
    void subscriptionPaymentMapsFinalizationUniqueness() throws NoSuchFieldException {
        Table table = SubscriptionPayment.class.getAnnotation(Table.class);

        assertUniqueConstraint(table, "uq_subscription_payments_order", "payment_order_id");
        assertUniqueConstraint(
                table,
                "uq_subscription_payments_provider_transaction",
                "provider",
                "pg_transaction_id");
        assertColumn(SubscriptionPayment.class, "pgTransactionId", "pg_transaction_id", 200, true);
    }

    @Test
    @DisplayName("fresh schema matches payment command, finalization, and audit ENUM contracts")
    void freshSchemaMatchesJavaContract() throws IOException {
        String schema = Files.readString(FRESH_SCHEMA);
        String paymentOrders = tableDefinition(schema, "payment_orders");
        String billingAgreements = tableDefinition(schema, "billing_agreements");
        String subscriptionPayments = tableDefinition(schema, "subscription_payments");
        String paymentRefunds = tableDefinition(schema, "payment_refunds");
        String auditLogs = tableDefinition(schema, "payment_operation_audit_logs");
        String normalizedPaymentOrders = normalizeSql(paymentOrders);
        String normalizedBillingAgreements = normalizeSql(billingAgreements);
        String normalizedSubscriptionPayments = normalizeSql(subscriptionPayments);
        String normalizedPaymentRefunds = normalizeSql(paymentRefunds);

        assertThat(normalizedPaymentOrders)
                .contains("command_key VARCHAR(191)"
                        , "billing_period_start DATE"
                        , "provider_attempt INT"
                        , "provider_idempotency_key VARCHAR(100)"
                        , "processing_started_at DATETIME"
                        , "upgrade_target_billing_cycle ENUM ('MONTHLY', 'YEARLY')"
                        , "UNIQUE KEY uq_payment_orders_command_key (command_key)"
                        , "UNIQUE KEY uq_payment_orders_provider_attempt_key (provider, provider_idempotency_key)"
                        , "UNIQUE KEY uq_payment_orders_renewal_period (billing_agreement_id, user_subscription_id, purpose, billing_period_start)"
                        , "KEY idx_payment_orders_status_processing (status, processing_started_at)"
                        , "KEY idx_payment_orders_local_reconciliation (status, id, purpose)");
        assertThat(paymentOrders)
                .contains("'PROCESSING'", "'PROVIDER_SUCCEEDED'", "'PENDING_PROVIDER_CONFIRMATION'");

        assertThat(normalizedBillingAgreements)
                .contains("renewal_retry_at DATE"
                        , "billing_key_cleanup_status ENUM ( 'NONE', 'REQUIRED', 'PROCESSING', 'PENDING_PROVIDER_CONFIRMATION', 'FAILED' ) NOT NULL DEFAULT 'NONE'"
                        , "billing_key_cleanup_started_at DATETIME"
                        , "KEY idx_billing_agreements_renewal_retry (status, renewal_retry_at, id)"
                        , "KEY idx_billing_agreements_cleanup (billing_key_cleanup_status, billing_key_cleanup_started_at, id)"
                        , "KEY idx_billing_agreements_local_reconciliation (status, id)");

        assertThat(normalizedSubscriptionPayments)
                .contains("pg_transaction_id VARCHAR(200)"
                        , "UNIQUE KEY uq_subscription_payments_order (payment_order_id)"
                        , "UNIQUE KEY uq_subscription_payments_provider_transaction (provider, pg_transaction_id)");

        assertThat(normalizedPaymentRefunds)
                .contains("processing_started_at DATETIME"
                        , "KEY idx_payment_refunds_status_processing (status, processing_started_at, id)");

        assertAuditEnumValues(auditLogs);
    }

    @Test
    @DisplayName("manual patch is guarded, ordered, non-destructive, and audit aligned")
    void manualPatchIsGuardedAndOrdered() throws IOException {
        String patch = Files.readString(MANUAL_PATCH);

        assertThat(patch)
                .contains("SIGNAL SQLSTATE '45000'"
                        , "information_schema.columns"
                        , "information_schema.statistics"
                        , "DATE_SUB(DATE(expires_at), INTERVAL 3 DAY)"
                        , "'RENEWAL:'"
                        , "CONCAT('LEGACY:', order_id)"
                        , "agreement.renewal_retry_at = agreement.next_billing_at"
                        , "agreement.next_billing_at = failed_order.billing_period_start"
                        , "SET processing_started_at = updated_at"
                        , "billing_key_cleanup_status ENUM (''NONE'', ''REQUIRED'', ''PROCESSING'', ''PENDING_PROVIDER_CONFIRMATION'', ''FAILED'')"
                        , "upgrade_target_billing_cycle ENUM (''MONTHLY'', ''YEARLY'')"
                        , "ADD COLUMN processing_started_at DATETIME NULL AFTER approved_at"
                        , "idx_billing_agreements_renewal_retry"
                        , "idx_billing_agreements_cleanup"
                        , "idx_payment_refunds_status_processing");
        assertThat(patch).doesNotContain("DELETE FROM", "TRUNCATE TABLE", "DROP TABLE");
        assertThat(patch).doesNotContain(
                "UPDATE subscription_payments",
                "UPDATE payment_operation_audit_logs",
                "SET upgrade_target_billing_cycle");

        int columnSection = patch.indexOf("-- 2. Add command columns and expand ENUMs");
        int packageAPreflight = patch.indexOf("-- 4. Preflight Package A legacy state");
        int packageAColumns = patch.indexOf("-- 5. Add Package A columns");
        String preColumnSection = patch.substring(0, columnSection);
        String packageAPreflightSql = patch.substring(packageAPreflight, packageAColumns);
        assertThat(preColumnSection).doesNotContain(
                "renewal_retry_at",
                "billing_key_cleanup_status",
                "billing_key_cleanup_started_at",
                "upgrade_target_billing_cycle",
                "processing_started_at");
        assertThat(packageAPreflightSql).doesNotContain(
                "renewal_retry_at",
                "billing_key_cleanup_status",
                "billing_key_cleanup_started_at",
                "upgrade_target_billing_cycle",
                "processing_started_at");
        assertThat(patch.indexOf("SET billing_period_start = DATE_SUB"))
                .isLessThan(patch.indexOf("CALL ats_check_legacy_renewal_retry_repair()"));
        assertThat(patch.indexOf("CALL ats_check_legacy_renewal_retry_repair()"))
                .isLessThan(patch.indexOf("ADD COLUMN renewal_retry_at"));
        assertThat(patch.indexOf("FROM payment_refunds"))
                .isLessThan(patch.indexOf("SET billing_period_start = DATE_SUB"));
        assertThat(patch.indexOf("ADD COLUMN renewal_retry_at"))
                .isLessThan(patch.indexOf("agreement.renewal_retry_at = agreement.next_billing_at"));
        assertThat(patch.indexOf("agreement.next_billing_at = failed_order.billing_period_start"))
                .isLessThan(patch.indexOf("ADD KEY idx_billing_agreements_renewal_retry"));
        assertThat(patch.indexOf("ADD COLUMN processing_started_at DATETIME NULL AFTER approved_at"))
                .isLessThan(patch.indexOf("SET processing_started_at = updated_at"));
        assertThat(patch.indexOf("SET processing_started_at = updated_at"))
                .isLessThan(patch.indexOf("ADD KEY idx_payment_refunds_status_processing"));

        assertSectionsInOrder(
                patch,
                "-- 1. Preflight inventory and blocking checks",
                "-- 2. Add command columns and expand ENUMs",
                "-- 3. Backfill legacy command identity",
                "-- 4. Preflight Package A legacy state",
                "-- 5. Add Package A columns",
                "-- 6. Repair and backfill Package A state",
                "-- 7. Block ambiguous renewal groups",
                "-- 8. Add final constraints and indexes",
                "-- 9. Post-apply contract comparison");
        assertAuditEnumValues(patch);
    }

    @Test
    @DisplayName("reconciliation index patch is additive, guarded, and includes reproducible EXPLAIN queries")
    void reconciliationIndexPatchIsAdditiveAndReproducible() throws IOException {
        String patch = Files.readString(RECONCILIATION_INDEX_PATCH);

        assertThat(patch)
                .contains(
                        "SIGNAL SQLSTATE '45000'",
                        "information_schema.statistics",
                        "idx_payment_orders_local_reconciliation (status, id, purpose)",
                        "idx_billing_agreements_local_reconciliation (status, id)",
                        "EXPLAIN FORMAT=JSON",
                        "payment_order.id > @ats_last_seen_id",
                        "agreement.id > @ats_last_seen_id")
                .doesNotContain("DELETE FROM", "UPDATE payment_orders", "UPDATE billing_agreements", "DROP TABLE");
    }

    private static void assertColumn(
            Class<?> entityType,
            String fieldName,
            String columnName,
            int length,
            boolean nullable) throws NoSuchFieldException {
        Field field = entityType.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);

        assertThat(column).as("@Column on %s.%s", entityType.getSimpleName(), fieldName).isNotNull();
        assertThat(column.name()).isEqualTo(columnName);
        assertThat(column.length()).isEqualTo(length);
        assertThat(column.nullable()).isEqualTo(nullable);
    }

    private static void assertUniqueConstraint(Table table, String name, String... columnNames) {
        Map<String, UniqueConstraint> constraints = Arrays.stream(table.uniqueConstraints())
                .collect(Collectors.toMap(UniqueConstraint::name, Function.identity()));

        assertThat(constraints).containsKey(name);
        assertThat(constraints.get(name).columnNames()).containsExactly(columnNames);
    }

    private static void assertIndex(Table table, String name, String columnList) {
        Map<String, Index> indexes = Arrays.stream(table.indexes())
                .collect(Collectors.toMap(Index::name, Function.identity()));

        assertThat(indexes).containsKey(name);
        assertThat(indexes.get(name).columnList()).isEqualTo(columnList);
    }

    private static String tableDefinition(String schema, String tableName) {
        Pattern pattern = Pattern.compile(
                "CREATE TABLE IF NOT EXISTS " + Pattern.quote(tableName)
                        + "\\s*\\((.*?)\\) ENGINE = InnoDB",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(schema);

        assertThat(matcher.find()).as("table %s exists", tableName).isTrue();
        return matcher.group(1);
    }

    private static String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static void assertAuditEnumValues(String sql) {
        Set<String> expectedValues = Arrays.stream(PaymentOperationAuditAction.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        expectedValues.addAll(Arrays.stream(PaymentOperationAuditTargetType.values())
                .map(Enum::name)
                .collect(Collectors.toSet()));

        assertThat(sql).contains(expectedValues.stream()
                .map(value -> "'" + value + "'")
                .toArray(String[]::new));
    }

    private static void assertSectionsInOrder(String sql, String... sections) {
        int previousIndex = -1;
        for (String section : sections) {
            int currentIndex = sql.indexOf(section);
            assertThat(currentIndex).as("section %s exists", section).isGreaterThan(previousIndex);
            previousIndex = currentIndex;
        }
    }
}
