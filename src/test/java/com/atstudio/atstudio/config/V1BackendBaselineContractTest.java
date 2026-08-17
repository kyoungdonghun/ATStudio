package com.atstudio.atstudio.config;

import com.atstudio.atstudio.entity.AdminOperationAuditLog;
import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.UserConsent;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("V1 backend baseline contracts")
class V1BackendBaselineContractTest {

    private static final Path SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path SEED = Path.of("src/main/resources/seed.sql");
    private static final Path LOCAL_EXAMPLE = Path.of("application-local.example.yml");
    private static final Path ACCEPTANCE_LIFECYCLE =
            Path.of("scripts/acceptance/AcceptanceLifecycle.psm1");

    @Test
    @DisplayName("Toss is the sole V1 provider while multi-PG interfaces remain")
    void providerIdentityIsTossOnly() throws Exception {
        assertThat(PaymentProviderType.values()).containsExactly(PaymentProviderType.TOSS);
        assertThat(RecurringPaymentProvider.class.isInterface()).isTrue();
        assertThat(PaymentStatusLookupProvider.class.isInterface()).isTrue();
        assertThat(PaymentRefundProvider.class.isInterface()).isTrue();

        Column provider = SubscriptionPayment.class
                .getDeclaredField("provider")
                .getAnnotation(Column.class);
        assertThat(provider.nullable()).isFalse();
    }

    @Test
    @DisplayName("schema is a fail-closed 43-table fresh baseline")
    void schemaIsFreshOnlyAndEntityAligned() throws Exception {
        String schema = Files.readString(SCHEMA);
        long tableCount = Pattern.compile("(?m)^CREATE TABLE ")
                .matcher(schema)
                .results()
                .count();
        List<String> oversizedIdentifiers = Pattern
                .compile("(?m)^\\s*(?:CONSTRAINT|(?:UNIQUE )?KEY)\\s+([A-Za-z0-9_]+)")
                .matcher(schema)
                .results()
                .map(result -> result.group(1))
                .filter(identifier -> identifier.length() > 64)
                .toList();

        assertThat(tableCount).isEqualTo(43);
        assertThat(oversizedIdentifiers).isEmpty();
        assertThat(schema)
                .doesNotContain(
                        "CREATE TABLE IF NOT EXISTS",
                        "FOREIGN_KEY_CHECKS",
                        "play_histories",
                        "download_queue",
                        "preview_file",
                        "user_id_snapshot",
                        "user_nickname_snapshot",
                        "MOCK",
                        "TOSS_BILLING",
                        "KAKAOPAY")
                .contains(
                        "provider             ENUM ('TOSS') NOT NULL",
                        "CREATE TABLE admin_operation_audit_logs",
                        "CREATE TABLE admin_subscription_corrections",
                        "CREATE TABLE payment_settlement_import_attempts",
                        "CREATE TABLE user_consents",
                        "UNIQUE KEY uq_user_consents_user_type_version (user_id, consent_type, policy_version)",
                        "KEY idx_admin_operation_audit_logs_actor_created (actor_user_id, created_at)",
                        "KEY idx_admin_operation_audit_logs_target (target_type, target_id)",
                        "KEY idx_admin_operation_audit_logs_action_created (action, created_at)");

        int importAttemptStart = schema.indexOf("CREATE TABLE payment_settlement_import_attempts");
        int importAttemptEnd = schema.indexOf(") ENGINE = InnoDB", importAttemptStart);
        String importAttemptTable = schema.substring(importAttemptStart, importAttemptEnd);
        assertThat(importAttemptTable)
                .contains(
                        "key_digest     VARCHAR(64) NOT NULL",
                        "UNIQUE KEY uq_payment_settlement_import_attempts_key_digest (key_digest)",
                        "KEY idx_payment_settlement_import_attempts_actor_created (actor_user_id, created_at)",
                        "KEY idx_payment_settlement_import_attempts_state_created (state, created_at)",
                        "CONSTRAINT fk_payment_settlement_import_attempts_actor",
                        "FOREIGN KEY (actor_user_id) REFERENCES users (id)",
                        "CONSTRAINT chk_payment_settlement_import_attempts_completed_counts",
                        "OR total_rows = imported_rows + duplicate_rows + failed_rows")
                .doesNotContain("idempotency_key", "operation_key");

        assertThat(PaymentSettlementImportAttempt.class.getAnnotation(Table.class).name())
                .isEqualTo("payment_settlement_import_attempts");
        assertThat(PaymentSettlementImportAttempt.class.getDeclaredField("keyDigest")
                .getAnnotation(Column.class))
                .satisfies(column -> {
                    assertThat(column.name()).isEqualTo("key_digest");
                    assertThat(column.nullable()).isFalse();
                    assertThat(column.length()).isEqualTo(64);
                });

        int auditStart = schema.indexOf("CREATE TABLE admin_operation_audit_logs");
        int auditEnd = schema.indexOf(") ENGINE = InnoDB", auditStart);
        assertThat(schema.substring(auditStart, auditEnd))
                .contains(
                        "before_state  TEXT         NULL",
                        "after_state   TEXT         NULL",
                        "reason_code   VARCHAR(100) NULL")
                .doesNotContain("FOREIGN KEY", "CONSTRAINT");

        assertThat(AdminOperationAuditLog.class.getDeclaredField("beforeState")
                .getAnnotation(Column.class).nullable()).isTrue();
        assertThat(AdminOperationAuditLog.class.getDeclaredField("afterState")
                .getAnnotation(Column.class).nullable()).isTrue();
        assertThat(AdminOperationAuditLog.class.getDeclaredField("reasonCode")
                .getAnnotation(Column.class).nullable()).isTrue();

        int paymentCorrectionStart = schema.indexOf("CREATE TABLE payment_entitlement_corrections");
        int paymentCorrectionEnd = schema.indexOf(") ENGINE = InnoDB", paymentCorrectionStart);
        assertThat(schema.substring(paymentCorrectionStart, paymentCorrectionEnd))
                .contains(
                        "payment_refund_id               BIGINT NOT NULL",
                        "subscription_payment_id         BIGINT NOT NULL",
                        "payment_order_id                BIGINT NOT NULL",
                        "provider                        ENUM ('TOSS') NOT NULL")
                .doesNotContain("source_type");

        int adminCorrectionStart = schema.indexOf("CREATE TABLE admin_subscription_corrections");
        int adminCorrectionEnd = schema.indexOf(") ENGINE = InnoDB", adminCorrectionStart);
        assertThat(schema.substring(adminCorrectionStart, adminCorrectionEnd))
                .contains(
                        "reason_note                     VARCHAR(500) NOT NULL",
                        "approval_note                   VARCHAR(500) NULL",
                        "execution_note                  VARCHAR(500) NULL",
                        "KEY idx_asc_status_created (status, created_at)",
                        "KEY idx_asc_subscription_created (user_subscription_id, created_at)")
                .doesNotContain(
                        "payment_refund_id",
                        "subscription_payment_id",
                        "payment_order_id",
                        "provider_transaction_id");

        assertThat(AdminSubscriptionCorrection.class.getAnnotation(Table.class).name())
                .isEqualTo("admin_subscription_corrections");
        assertThat(AdminSubscriptionCorrection.class.getDeclaredField("reasonNote")
                .getAnnotation(Column.class).nullable()).isFalse();
        assertThat(AdminSubscriptionCorrection.class.getDeclaredField("approvalNote")
                .getAnnotation(Column.class))
                .satisfies(column -> {
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.length()).isEqualTo(500);
                });
        assertThat(AdminSubscriptionCorrection.class.getDeclaredField("executionNote")
                .getAnnotation(Column.class))
                .satisfies(column -> {
                    assertThat(column.nullable()).isTrue();
                    assertThat(column.length()).isEqualTo(500);
                });
    }

    @Test
    @DisplayName("local subscription correction has no provider, refund, HTTP, or payment-audit dependency")
    void localSubscriptionCorrectionHasNoExternalPaymentDependency() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/atstudio/atstudio/service/AdminSubscriptionCorrectionService.java"));

        assertThat(source).doesNotContain(
                "TossBillingProvider",
                "PaymentRefundProvider",
                "PaymentRefundRepository",
                "RecurringPaymentProvider",
                "PaymentStatusLookupProvider",
                "BillingKeyCrypto",
                "PaymentOperationAuditLogService",
                "ApplicationEventPublisher",
                "RestClient",
                "WebClient",
                "HttpClient");
    }

    @Test
    @DisplayName("entity unique keys exactly match the fresh schema")
    void entityUniqueKeysMatchFreshSchema() throws Exception {
        String schema = Files.readString(SCHEMA);
        Table settlementTable = PaymentSettlement.class.getAnnotation(Table.class);
        Table socialAccountTable = SocialAccount.class.getAnnotation(Table.class);
        Table userConsentTable = UserConsent.class.getAnnotation(Table.class);
        Column deduplicationKey = PaymentSettlement.class
                .getDeclaredField("deduplicationKey")
                .getAnnotation(Column.class);

        assertSingleUniqueConstraint(
                settlementTable,
                "uq_payment_settlements_deduplication_key",
                "deduplication_key");
        assertThat(settlementTable.indexes())
                .noneMatch(index -> index.unique());
        assertThat(deduplicationKey.unique()).isFalse();

        assertSingleUniqueConstraint(
                socialAccountTable,
                "uq_social_accounts_provider_id",
                "provider",
                "provider_id");
        assertSingleUniqueConstraint(
                userConsentTable,
                "uq_user_consents_user_type_version",
                "user_id",
                "consent_type",
                "policy_version");

        assertThat(schema)
                .contains(
                        "UNIQUE KEY uq_payment_settlements_deduplication_key (deduplication_key)",
                        "UNIQUE KEY uq_social_accounts_provider_id (provider, provider_id)",
                        "UNIQUE KEY uq_user_consents_user_type_version (user_id, consent_type, policy_version)")
                .doesNotContain(
                        "idx_payment_settlements_dedup",
                        "uq_social_accounts_provider_provider_id");
    }

    @Test
    @DisplayName("seed owns exactly six deterministic plans and no demo data")
    void seedOwnsSixPlansOnly() throws Exception {
        String seed = Files.readString(SEED);
        long planRows = Pattern.compile("(?m)^\\s*\\('(STANDARD|DELUXE|PREMIUM)'")
                .matcher(seed)
                .results()
                .count();

        assertThat(planRows).isEqualTo(6);
        assertThat(seed)
                .contains("INSERT INTO subscriptions")
                .doesNotContain(
                        "INSERT IGNORE",
                        "INSERT INTO users",
                        "INSERT INTO tracks",
                        "DELETE FROM",
                        "UPDATE ",
                        "TRUNCATE ");
    }

    @Test
    @DisplayName("local and acceptance config contracts are explicit and validate-only")
    void runtimeConfigurationIsExplicitAndValidateOnly() throws Exception {
        String application = Files.readString(Path.of("src/main/resources/application.yml"));
        String acceptance = Files.readString(Path.of("src/main/resources/application-acceptance.yml"));
        String localExample = Files.readString(LOCAL_EXAMPLE);
        String acceptanceLifecycle = Files.readString(ACCEPTANCE_LIFECYCLE);

        assertThat(application)
                .doesNotContain(
                        "optional:file:./application-local.yml",
                        "APP_PAYMENT_PROVIDER",
                        "PAYMENT_BILLING_KEY_ENCRYPTION_SECRET",
                        "APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_WINDOW_SECONDS",
                        "APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_WINDOW_SECONDS",
                        "APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_WINDOW_SECONDS",
                        "TOSS_SUCCESS_URL",
                        "TOSS_FAIL_URL",
                        "TOSS_CONFIRM_URL",
                        "thymeleaf:")
                .contains(
                        "ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}",
                        "APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_CLIENT_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_EMAIL_AVAILABILITY_CLIENT_WINDOW_SECONDS",
                        "APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_CLIENT_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_PHONE_AVAILABILITY_CLIENT_WINDOW_SECONDS",
                        "APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_CLIENT_LIMIT",
                        "APP_SECURITY_RATE_LIMIT_NICKNAME_AVAILABILITY_CLIENT_WINDOW_SECONDS");
        assertThat(acceptance)
                .contains("ddl-auto: validate")
                .doesNotContain("subscriptions/payment/success", "subscriptions/payment/fail");
        assertThat(localExample)
                .contains(
                        "SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:./application-local.yml",
                        "ddl-auto: validate",
                        "enabled: false")
                .doesNotContain("createDatabaseIfNotExist", "ddl-auto: update", "provider: MOCK");
        assertThat(acceptanceLifecycle)
                .contains(
                        "\"PAYMENT_BILLING_KEY_ACTIVE_KEY_ID\"",
                        "\"PAYMENT_BILLING_KEY_0_ID\"",
                        "\"PAYMENT_BILLING_KEY_0_SECRET\"",
                        "\"APP_PAYMENT_SCHEDULER_ZONE\"")
                .doesNotContain(
                        "\"APP_PAYMENT_PROVIDER\"",
                        "\"TOSS_CONFIRM_URL\"",
                        "\"PAYMENT_BILLING_KEY_ENCRYPTION_SECRET\"");
    }

    @Test
    @DisplayName("local example credential fields are reference-only")
    void localExampleCredentialFieldsAreReferenceOnly() throws Exception {
        PropertySource<?> source = new YamlPropertySourceLoader()
                .load("local-example", new FileSystemResource(LOCAL_EXAMPLE))
                .get(0);

        assertReferenceOnly(source, "spring.datasource.password", "SPRING_DATASOURCE_PASSWORD");
        assertReferenceOnly(source, "jwt.secret", "JWT_SECRET");
        assertReferenceOnly(
                source,
                "app.payment.billing.encryption-keys[0].secret",
                "PAYMENT_BILLING_KEY_0_SECRET");
    }

    private static void assertReferenceOnly(
            PropertySource<?> source,
            String field,
            String environmentVariable) {
        String expected = "${" + environmentVariable + "}";
        boolean referenceOnly = expected.equals(source.getProperty(field));

        assertThat(referenceOnly)
                .as("%s must reference only %s without a fallback", field, environmentVariable)
                .isTrue();
    }

    private static void assertSingleUniqueConstraint(
            Table table,
            String expectedName,
            String... expectedColumns) {
        assertThat(table.uniqueConstraints())
                .singleElement()
                .satisfies(constraint -> {
                    assertThat(constraint.name()).isEqualTo(expectedName);
                    assertThat(constraint.columnNames()).containsExactly(expectedColumns);
                });
    }
}
