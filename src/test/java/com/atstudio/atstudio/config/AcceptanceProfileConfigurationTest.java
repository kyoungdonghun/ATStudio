package com.atstudio.atstudio.config;

import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Acceptance profile configuration")
class AcceptanceProfileConfigurationTest {

    @Test
    @DisplayName("profile uses external placeholders and one public base URL for callbacks")
    void profileUsesExternalPlaceholdersAndDerivedCallbacks() throws IOException {
        ClassPathResource resource = new ClassPathResource("application-acceptance.yml");
        PropertySource<?> source = new YamlPropertySourceLoader().load("acceptance", resource).get(0);

        assertThat(source.getProperty("spring.config.activate.on-profile")).isEqualTo("acceptance");
        assertThat(source.getProperty("server.address")).isEqualTo("127.0.0.1");
        assertThat(source.getProperty("spring.datasource.url")).isEqualTo("${SPRING_DATASOURCE_URL}");
        assertThat(source.getProperty("spring.datasource.username")).isEqualTo("${SPRING_DATASOURCE_USERNAME}");
        assertThat(source.getProperty("spring.datasource.password")).isEqualTo("${SPRING_DATASOURCE_PASSWORD}");
        assertThat(source.getProperty("jwt.secret")).isEqualTo("${JWT_SECRET}");
        assertThat(source.getProperty("app.bootstrap.test-users.default-password"))
                .isEqualTo("${APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD:}");
        assertThat(source.getProperty("app.acceptance.public-base-url")).isEqualTo("${APP_PUBLIC_BASE_URL}");
        assertThat(source.getProperty("app.mail.base-url")).isEqualTo("${APP_PUBLIC_BASE_URL}");
        assertThat(source.getProperty("oauth2.google.redirect-uri"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/social-login/google");
        assertThat(source.getProperty("oauth2.kakao.redirect-uri"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/social-login/kakao");
        assertThat(source.getProperty("oauth2.naver.redirect-uri"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/social-login/naver");
        assertThat(source.getProperty("app.payment.toss.success-url"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/subscriptions/payment/success");
        assertThat(source.getProperty("app.payment.toss.fail-url"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/subscriptions/payment/fail");
        assertThat(source.getProperty("app.payment.billing.auth-success-url"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/subscriptions/checkout/success");
        assertThat(source.getProperty("app.payment.billing.auth-fail-url"))
                .isEqualTo("${APP_PUBLIC_BASE_URL}/subscriptions/checkout/fail");

        String contents = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(contents).doesNotContain("Test1234!");
    }

    @Test
    @DisplayName("bootstrap and payment callback Java defaults contain no fallback values")
    void javaDefaultsContainNoFallbackValues() {
        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        PaymentProperties payment = new PaymentProperties();

        assertThat(bootstrap.getDefaultPassword()).isEmpty();
        assertThat(payment.getToss().getSuccessUrl()).isEmpty();
        assertThat(payment.getToss().getFailUrl()).isEmpty();
        assertThat(payment.getBilling().getAuthSuccessUrl()).isEmpty();
        assertThat(payment.getBilling().getAuthFailUrl()).isEmpty();
    }
}
