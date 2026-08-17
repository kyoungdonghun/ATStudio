package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
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
@EnabledIf("isExplicitGuardedDisposableProof")
@DisplayName("WI-20260817-ATS-016 guarded Hibernate validate proof")
class PaymentMysqlSchemaValidationTest {

    private static final Pattern DISPOSABLE_DATABASE =
            Pattern.compile("^ats_disposable_\\d{8}_[a-z0-9]{8}$");
    private static final Pattern GUARDED_LOOPBACK_JDBC_URL = Pattern.compile(
            "^jdbc:mysql://(?:localhost|127\\.0\\.0\\.1|\\[::1])(?::3306)?/"
                    + "ats_disposable_\\d{8}_[a-z0-9]{8}$");

    @Autowired DataSource dataSource;

    static boolean isExplicitGuardedDisposableProof() {
        String proofDatabase = System.getenv("ATSTUDIO_MYSQL_PROOF_DATABASE");
        String jdbcUrl = System.getenv("SPRING_DATASOURCE_URL");
        return "true".equals(System.getenv("ATSTUDIO_MYSQL_PROOF_ENABLED"))
                && proofDatabase != null
                && DISPOSABLE_DATABASE.matcher(proofDatabase).matches()
                && jdbcUrl != null
                && GUARDED_LOOPBACK_JDBC_URL.matcher(jdbcUrl).matches()
                && jdbcUrl.endsWith("/" + proofDatabase);
    }

    @Test
    @DisplayName("Hibernate validates only the explicitly opted-in disposable MySQL schema")
    void hibernateValidatePassesOnGuardedDisposableDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getCatalog())
                    .isEqualTo(System.getenv("ATSTUDIO_MYSQL_PROOF_DATABASE"));
        }
        System.out.println("hibernate.schemaValidation=PASS");
    }
}
