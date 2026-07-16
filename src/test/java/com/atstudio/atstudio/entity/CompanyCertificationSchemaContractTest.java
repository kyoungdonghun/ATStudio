package com.atstudio.atstudio.entity;

import jakarta.persistence.Version;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Company certification schema source contracts")
class CompanyCertificationSchemaContractTest {

    private static final Path FRESH_SCHEMA = Path.of("src/main/resources/schema.sql");
    private static final Path RETAINED_DB_PATCH = Path.of(
            "src/main/resources/db/manual/20260716_company_certification_integrity_and_audit.sql");

    @Test
    @DisplayName("fresh and retained schema sources align with versioned certification audit evidence")
    void freshAndRetainedSchemaSourcesAlignWithCertificationContract() throws Exception {
        String freshSchema = Files.readString(FRESH_SCHEMA);
        String retainedPatch = Files.readString(RETAINED_DB_PATCH);
        Field version = CompanyCertification.class.getDeclaredField("version");

        assertThat(version.isAnnotationPresent(Version.class)).isTrue();
        assertThat(freshSchema).contains("version            BIGINT");
        assertThat(freshSchema).contains(
                "idx_company_certifications_user_status_id (user_id, status, id)");
        assertThat(freshSchema).contains("CREATE TABLE IF NOT EXISTS company_certification_audit_logs");
        assertThat(freshSchema).contains("ENUM ('REVIEWED', 'DOCUMENT_ACCESS_GRANTED')");
        assertThat(freshSchema).contains("document_id      BIGINT");
        assertThat(retainedPatch).contains("information_schema.columns");
        assertThat(retainedPatch).contains("PREPARE company_certification_version_statement");
        assertThat(retainedPatch).contains("information_schema.statistics");
        assertThat(retainedPatch).contains("PREPARE company_certification_user_status_index_statement");
        assertThat(retainedPatch).doesNotContain("ADD COLUMN IF NOT EXISTS");
        assertThat(retainedPatch).doesNotContain("ADD INDEX IF NOT EXISTS");
        assertThat(retainedPatch).contains("CREATE TABLE IF NOT EXISTS company_certification_audit_logs");
        assertThat(retainedPatch).doesNotContain("DELETE FROM");
        assertThat(retainedPatch).doesNotContain("DROP TABLE");
        assertThat(CompanyCertificationAuditLog.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("documentPath", "storedPath", "originalFilename", "note", "email", "phone");
    }
}
