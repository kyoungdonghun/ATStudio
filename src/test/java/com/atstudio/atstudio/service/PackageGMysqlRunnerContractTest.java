package com.atstudio.atstudio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WI-20260715-ATS-007 runner safety contract")
class PackageGMysqlRunnerContractTest {

    private static final Path ARTIFACT_DIR = Path.of(
            "deliverables",
            "agent",
            "WI-20260715-ATS-007");

    @Test
    @DisplayName("runner enforces disposable naming, five minute bounds, redaction, and finally cleanup")
    void runnerHasRepoSafeLifecycleContract() throws Exception {
        String runner = Files.readString(
                ARTIFACT_DIR.resolve("run-package-g-mysql-proof.ps1"),
                StandardCharsets.UTF_8);

        assertThat(runner)
                .contains("^ats_wi007_\\d{8}_[a-z0-9]{8}$")
                .contains("[ValidateRange(1, 300)]")
                .contains("CommandTimeoutSeconds = 300")
                .contains("Package G accepts only a loopback MySQL credential source")
                .contains("Get-RedactedText")
                .contains("[REDACTED_JDBC_URL]")
                .contains("[WI007_DISPOSABLE]")
                .contains("} finally {")
                .contains("-Mode \"drop\"")
                .contains("-Mode \"verify-absent\"")
                .contains("cleanupDatabaseExists = \"0\"")
                .doesNotContain("packageG.databaseName=");
    }

    @Test
    @DisplayName("database manager rejects protected and remote targets without logging the actual name")
    void databaseManagerHasTargetAndEvidenceGuards() throws Exception {
        String manager = Files.readString(
                ARTIFACT_DIR.resolve("DisposableMysqlDatabaseManager.java"),
                StandardCharsets.UTF_8);

        assertThat(manager)
                .contains("^ats_wi007_\\\\d{8}_[a-z0-9]{8}$")
                .contains("PROTECTED_DATABASE_NAMES")
                .contains("atstudio")
                .contains("preview")
                .contains("production")
                .contains("requireLoopbackHost")
                .contains("database.alias=WI007_DISPOSABLE; actual-name-not-printed")
                .contains("DROP DATABASE IF EXISTS")
                .contains("cleanup.database.exists=")
                .doesNotContain("disposable.database: " + " + databaseName");
    }

    @Test
    @DisplayName("MySQL suite carries all seven numbered races and production-engine guards")
    void mysqlSuiteDeclaresAllSevenStrictRaces() throws Exception {
        String suite = Files.readString(
                Path.of(
                        "src",
                        "test",
                        "java",
                        "com",
                        "atstudio",
                        "atstudio",
                        "service",
                        "PaymentMysqlConcurrencyIntegrationTest.java"),
                StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("@DisplayName\\(\"race ([1-7]):").matcher(suite);
        int races = 0;
        while (matcher.find()) {
            races++;
        }

        assertThat(races).isEqualTo(7);
        assertThat(suite)
                .contains("AutoConfigureTestDatabase.Replace.NONE")
                .contains("spring.jpa.hibernate.ddl-auto=validate")
                .contains("ATSTUDIO_MYSQL_PROOF_ENABLED")
                .contains("SELECT VERSION()")
                .contains("SELECT @@transaction_isolation")
                .contains("InnoDB")
                .contains("PAYMENT_ORDER_INVALID_STATE")
                .contains("INVALID_STATE_TRANSITION")
                .contains("INVALID_ARGUMENT");

        String validateGate = Files.readString(
                Path.of(
                        "src",
                        "test",
                        "java",
                        "com",
                        "atstudio",
                        "atstudio",
                        "service",
                        "PaymentMysqlSchemaValidationTest.java"),
                StandardCharsets.UTF_8);
        assertThat(validateGate)
                .contains("AutoConfigureTestDatabase.Replace.NONE")
                .contains("spring.jpa.hibernate.ddl-auto=validate")
                .contains("ATSTUDIO_MYSQL_PROOF_ENABLED")
                .contains("hibernate.schemaValidation=PASS");
    }
}
