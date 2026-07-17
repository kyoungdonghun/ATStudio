package com.atstudio.atstudio.config;

import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.SocialAccount;
import com.atstudio.atstudio.entity.SubscriptionPayment;
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
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("V1 backend baseline contracts")
class V1BackendBaselineContractTest {

    private static final Path SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path SEED = Path.of("src/main/resources/seed.sql");
    private static final Path LOCAL_EXAMPLE = Path.of("application-local.example.yml");

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
    @DisplayName("schema is a fail-closed 39-table fresh baseline")
    void schemaIsFreshOnlyAndEntityAligned() throws Exception {
        String schema = Files.readString(SCHEMA);
        long tableCount = Pattern.compile("(?m)^CREATE TABLE ")
                .matcher(schema)
                .results()
                .count();

        assertThat(tableCount).isEqualTo(39);
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
                .contains("provider             ENUM ('TOSS') NOT NULL");
    }

    @Test
    @DisplayName("entity unique keys exactly match the fresh schema")
    void entityUniqueKeysMatchFreshSchema() throws Exception {
        String schema = Files.readString(SCHEMA);
        Table settlementTable = PaymentSettlement.class.getAnnotation(Table.class);
        Table socialAccountTable = SocialAccount.class.getAnnotation(Table.class);
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

        assertThat(schema)
                .contains(
                        "UNIQUE KEY uq_payment_settlements_deduplication_key (deduplication_key)",
                        "UNIQUE KEY uq_social_accounts_provider_id (provider, provider_id)")
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
    @DisplayName("local config loading is explicit and all runtime profiles validate schema")
    void runtimeConfigurationIsExplicitAndValidateOnly() throws Exception {
        String application = Files.readString(Path.of("src/main/resources/application.yml"));
        String acceptance = Files.readString(Path.of("src/main/resources/application-acceptance.yml"));
        String localExample = Files.readString(LOCAL_EXAMPLE);

        assertThat(application)
                .doesNotContain(
                        "optional:file:./application-local.yml",
                        "APP_PAYMENT_PROVIDER",
                        "PAYMENT_BILLING_KEY_ENCRYPTION_SECRET",
                        "TOSS_SUCCESS_URL",
                        "TOSS_FAIL_URL",
                        "TOSS_CONFIRM_URL",
                        "thymeleaf:")
                .contains("ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}");
        assertThat(acceptance)
                .contains("ddl-auto: validate")
                .doesNotContain("subscriptions/payment/success", "subscriptions/payment/fail");
        assertThat(localExample)
                .contains(
                        "SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:./application-local.yml",
                        "ddl-auto: validate",
                        "enabled: false")
                .doesNotContain("createDatabaseIfNotExist", "ddl-auto: update", "provider: MOCK");
    }

    @Test
    @DisplayName("local example credential fields are reference-only")
    void localExampleCredentialFieldsAreReferenceOnly() throws Exception {
        PropertySource<?> source = new YamlPropertySourceLoader()
                .load("local-example", new FileSystemResource(LOCAL_EXAMPLE))
                .get(0);

        assertReferenceOnly(source, "spring.datasource.password", "DB_PASSWORD");
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
