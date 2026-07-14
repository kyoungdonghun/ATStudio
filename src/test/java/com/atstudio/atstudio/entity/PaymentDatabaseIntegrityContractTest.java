package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Payment database integrity contract tests")
class PaymentDatabaseIntegrityContractTest {

    private static final Path FRESH_SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path MANUAL_PATCH = Path.of(
            "src/main/resources/db/manual/20260714_payment_db_integrity.sql");

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
        String subscriptionPayments = tableDefinition(schema, "subscription_payments");
        String auditLogs = tableDefinition(schema, "payment_operation_audit_logs");
        String normalizedPaymentOrders = normalizeSql(paymentOrders);
        String normalizedSubscriptionPayments = normalizeSql(subscriptionPayments);

        assertThat(normalizedPaymentOrders)
                .contains("command_key VARCHAR(191)"
                        , "billing_period_start DATE"
                        , "provider_attempt INT"
                        , "provider_idempotency_key VARCHAR(100)"
                        , "processing_started_at DATETIME"
                        , "UNIQUE KEY uq_payment_orders_command_key (command_key)"
                        , "UNIQUE KEY uq_payment_orders_provider_attempt_key (provider, provider_idempotency_key)"
                        , "UNIQUE KEY uq_payment_orders_renewal_period (billing_agreement_id, user_subscription_id, purpose, billing_period_start)"
                        , "KEY idx_payment_orders_status_processing (status, processing_started_at)");
        assertThat(paymentOrders)
                .contains("'PROCESSING'", "'PROVIDER_SUCCEEDED'", "'PENDING_PROVIDER_CONFIRMATION'");

        assertThat(normalizedSubscriptionPayments)
                .contains("pg_transaction_id VARCHAR(200)"
                        , "UNIQUE KEY uq_subscription_payments_order (payment_order_id)"
                        , "UNIQUE KEY uq_subscription_payments_provider_transaction (provider, pg_transaction_id)");

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
                        , "CONCAT('LEGACY:', order_id)");
        assertThat(patch).doesNotContain("DELETE FROM", "TRUNCATE TABLE", "DROP TABLE");
        assertThat(patch).doesNotContain("UPDATE subscription_payments", "UPDATE payment_operation_audit_logs");

        assertSectionsInOrder(
                patch,
                "-- 1. Preflight inventory and blocking checks",
                "-- 2. Add command columns and expand ENUMs",
                "-- 3. Backfill legacy command identity",
                "-- 4. Block ambiguous renewal groups",
                "-- 5. Add final constraints and indexes",
                "-- 6. Post-apply contract comparison");
        assertAuditEnumValues(patch);
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
