package com.atstudio.atstudio.config;

import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Acceptance startup guard")
class AcceptanceStartupGuardTest {

    private static final String PUBLIC_BASE_URL = "https://acceptance.example.test";

    @Test
    @DisplayName("public base URL derives every approved callback and public host")
    void publicBaseUrlDerivesApprovedCallbacks() {
        AcceptancePublicUrls urls = AcceptancePublicUrls.from(PUBLIC_BASE_URL);

        assertThat(urls.publicHost()).isEqualTo("acceptance.example.test");
        assertThat(urls.mailBaseUrl()).isEqualTo(PUBLIC_BASE_URL);
        assertThat(urls.googleRedirectUri()).isEqualTo(PUBLIC_BASE_URL + "/social-login/google");
        assertThat(urls.kakaoRedirectUri()).isEqualTo(PUBLIC_BASE_URL + "/social-login/kakao");
        assertThat(urls.naverRedirectUri()).isEqualTo(PUBLIC_BASE_URL + "/social-login/naver");
        assertThat(urls.tossBillingAuthSuccessUrl())
                .isEqualTo(PUBLIC_BASE_URL + "/subscriptions/checkout/success");
        assertThat(urls.tossBillingAuthFailUrl())
                .isEqualTo(PUBLIC_BASE_URL + "/subscriptions/checkout/fail");
    }

    @Test
    @DisplayName("public base URL rejects non-HTTPS and non-root forms")
    void publicBaseUrlRejectsUnsafeForms() {
        List<String> invalidUrls = List.of(
                "http://acceptance.example.test",
                "https://user@acceptance.example.test",
                "https://acceptance.example.test/",
                "https://acceptance.example.test/nested",
                "https://acceptance.example.test?mode=test",
                "https://acceptance.example.test#fragment"
        );

        for (String invalidUrl : invalidUrls) {
            assertThatThrownBy(() -> AcceptancePublicUrls.from(invalidUrl))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("APP_PUBLIC_BASE_URL")
                    .hasMessageNotContaining(invalidUrl);
        }
    }

    @Test
    @DisplayName("acceptance profile accepts external core configuration and derived callbacks")
    void acceptanceProfileAcceptsDerivedConfiguration() {
        GuardFixture fixture = validAcceptanceFixture();

        assertThatCode(fixture.guard()::validate).doesNotThrowAnyException();
        assertThat(fixture.guard().getOrder()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("local profile permits bootstrap only with external password and core secrets")
    void localProfileAcceptsExternallyConfiguredBootstrap() {
        MockEnvironment environment = coreEnvironment("local");
        AcceptanceProperties acceptance = new AcceptanceProperties();
        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        bootstrap.setEnabled(true);
        bootstrap.setDefaultPassword("fixture-bootstrap-password");

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                environment,
                acceptance,
                bootstrap,
                validPaymentProperties());

        assertThatCode(guard::validate).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("default profile refuses acceptance flag")
    void defaultProfileRefusesAcceptanceFlag() {
        AcceptanceProperties acceptance = new AcceptanceProperties();
        acceptance.setEnabled(true);

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                new MockEnvironment(),
                acceptance,
                new TestUserBootstrapProperties(),
                validPaymentProperties());

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("explicit acceptance profile");
    }

    @Test
    @DisplayName("default profile refuses test-user bootstrap")
    void defaultProfileRefusesBootstrap() {
        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        bootstrap.setEnabled(true);
        bootstrap.setDefaultPassword("fixture-bootstrap-password");

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                new MockEnvironment(),
                new AcceptanceProperties(),
                bootstrap,
                new PaymentProperties());

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("explicit non-production profile");
    }

    @Test
    @DisplayName("production-like profile refuses acceptance and bootstrap flags")
    void productionProfileRefusesAcceptanceAndBootstrap() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production-eu");
        AcceptanceProperties acceptance = new AcceptanceProperties();
        acceptance.setEnabled(true);
        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        bootstrap.setEnabled(true);

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                environment,
                acceptance,
                bootstrap,
                new PaymentProperties());

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("forbidden in production profiles");
    }

    @Test
    @DisplayName("bootstrap refuses a missing external password")
    void bootstrapRefusesMissingExternalPassword() {
        MockEnvironment environment = coreEnvironment("local");
        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        bootstrap.setEnabled(true);

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                environment,
                new AcceptanceProperties(),
                bootstrap,
                validPaymentProperties());

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD");
    }

    @Test
    @DisplayName("acceptance refuses missing external DB or JWT configuration")
    void acceptanceRefusesMissingCoreExternalConfiguration() {
        GuardFixture fixture = validAcceptanceFixture();
        fixture.environment().withProperty("spring.datasource.password", "");

        assertThatThrownBy(fixture.guard()::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SPRING_DATASOURCE_PASSWORD");
    }

    @Test
    @DisplayName("acceptance rejects an independently overridden social callback without exposing values")
    void acceptanceRefusesIndependentSocialCallbackWithoutValues() {
        GuardFixture fixture = validAcceptanceFixture();
        fixture.environment().withProperty(
                "oauth2.google.redirect-uri",
                "https://independent.example.test/social-login/google");

        assertThatThrownBy(fixture.guard()::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oauth2.google.redirect-uri")
                .hasMessageNotContaining("independent.example.test")
                .hasMessageNotContaining("fixture-db-password")
                .hasMessageNotContaining("fixture-jwt-secret");
    }

    @Test
    @DisplayName("acceptance rejects an independently overridden Toss callback")
    void acceptanceRefusesIndependentTossCallback() {
        GuardFixture fixture = validAcceptanceFixture();
        fixture.payment().getBilling().setAuthFailUrl(PUBLIC_BASE_URL + "/wrong-path");

        assertThatThrownBy(fixture.guard()::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.payment.billing.auth-fail-url")
                .hasMessageNotContaining("wrong-path");
    }

    @Test
    @DisplayName("Toss acceptance mode refuses missing externally supplied credentials")
    void tossAcceptanceRefusesMissingExternalCredentials() {
        GuardFixture fixture = validAcceptanceFixture();
        fixture.payment().getToss().setClientKey("");

        assertThatThrownBy(fixture.guard()::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TOSS_CLIENT_KEY");
    }

    @Test
    @DisplayName("Toss recurring billing refuses an invalid key ring in every profile")
    void tossRecurringRefusesInvalidKeyRingOutsideAcceptance() {
        PaymentProperties payment = new PaymentProperties();
        payment.getBilling().setActiveKeyId("active-key");
        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                new MockEnvironment(),
                new AcceptanceProperties(),
                new TestUserBootstrapProperties(),
                payment);

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key ring");
    }

    @Test
    @DisplayName("Missing V2 key configuration refuses the recurring provider")
    void missingV2KeyRingRefusesRecurringProvider() {
        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                new MockEnvironment(),
                new AcceptanceProperties(),
                new TestUserBootstrapProperties(),
                new PaymentProperties());

        assertThatThrownBy(guard::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_BILLING_KEY_ACTIVE_KEY_ID");
    }

    private GuardFixture validAcceptanceFixture() {
        MockEnvironment environment = coreEnvironment("acceptance")
                .withProperty("app.mail.base-url", PUBLIC_BASE_URL)
                .withProperty("oauth2.google.redirect-uri", PUBLIC_BASE_URL + "/social-login/google")
                .withProperty("oauth2.kakao.redirect-uri", PUBLIC_BASE_URL + "/social-login/kakao")
                .withProperty("oauth2.naver.redirect-uri", PUBLIC_BASE_URL + "/social-login/naver");

        AcceptanceProperties acceptance = new AcceptanceProperties();
        acceptance.setEnabled(true);
        acceptance.setPublicBaseUrl(PUBLIC_BASE_URL);

        TestUserBootstrapProperties bootstrap = new TestUserBootstrapProperties();
        PaymentProperties payment = validPaymentProperties();
        AcceptancePublicUrls urls = AcceptancePublicUrls.from(PUBLIC_BASE_URL);
        payment.getBilling().setAuthSuccessUrl(urls.tossBillingAuthSuccessUrl());
        payment.getBilling().setAuthFailUrl(urls.tossBillingAuthFailUrl());

        AcceptanceStartupGuard guard = new AcceptanceStartupGuard(
                environment,
                acceptance,
                bootstrap,
                payment);
        return new GuardFixture(environment, payment, guard);
    }

    private PaymentProperties validPaymentProperties() {
        PaymentProperties payment = new PaymentProperties();
        payment.getToss().setClientKey("fixture-toss-client-key");
        payment.getToss().setSecretKey("fixture-toss-secret-key");
        payment.getBilling().setActiveKeyId("fixture-v2");
        PaymentProperties.EncryptionKey key = new PaymentProperties.EncryptionKey();
        key.setId("fixture-v2");
        key.setSecret("fixture-v2-key-material-not-for-runtime");
        payment.getBilling().setEncryptionKeys(List.of(key));
        return payment;
    }

    private MockEnvironment coreEnvironment(String profile) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://fixture-db/atstudio")
                .withProperty("spring.datasource.username", "fixture-db-user")
                .withProperty("spring.datasource.password", "fixture-db-password")
                .withProperty("jwt.secret", "fixture-jwt-secret");
        environment.setActiveProfiles(profile);
        return environment;
    }

    private record GuardFixture(
            MockEnvironment environment,
            PaymentProperties payment,
            AcceptanceStartupGuard guard
    ) {
    }
}
