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
    @Test
    @DisplayName("fresh baseline aligns with versioned certification audit evidence")
    void freshBaselineAlignsWithCertificationContract() throws Exception {
        String freshSchema = Files.readString(FRESH_SCHEMA);
        Field version = CompanyCertification.class.getDeclaredField("version");

        assertThat(version.isAnnotationPresent(Version.class)).isTrue();
        assertThat(freshSchema).contains("version            BIGINT");
        assertThat(freshSchema).contains(
                "idx_company_certifications_user_status_id (user_id, status, id)");
        assertThat(freshSchema).contains("CREATE TABLE company_certification_audit_logs");
        assertThat(freshSchema).contains("ENUM ('REVIEWED', 'DOCUMENT_ACCESS_GRANTED')");
        assertThat(freshSchema).contains("document_id      BIGINT");
        assertThat(CompanyCertificationAuditLog.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("documentPath", "storedPath", "originalFilename", "note", "email", "phone");
    }
}
