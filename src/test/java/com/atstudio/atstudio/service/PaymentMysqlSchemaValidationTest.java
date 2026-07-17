package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false",
        "spring.datasource.hikari.connection-timeout=5000"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "ATSTUDIO_MYSQL_PROOF_ENABLED", matches = "true")
@DisplayName("WI-20260715-ATS-007 Hibernate validate gate")
class PaymentMysqlSchemaValidationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_wi(?:004|007)_\\d{8}_[a-z0-9]{8}$");

    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Hibernate validates the disposable MySQL schema before race execution")
    void hibernateValidatePassesOnGuardedDisposableDatabase() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        boolean localRecreateProof = "local-atstudio".equals(
                System.getenv("ATSTUDIO_MYSQL_PROOF_TARGET"));
        boolean guardedTarget = localRecreateProof
                ? "atstudio".equals(database)
                : database != null && DISPOSABLE_DATABASE.matcher(database).matches();
        if (!guardedTarget) {
            throw new AssertionError("Datasource target failed the guarded MySQL proof name check.");
        }
        assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        System.out.println("hibernate.schemaValidation=PASS");
    }
}
