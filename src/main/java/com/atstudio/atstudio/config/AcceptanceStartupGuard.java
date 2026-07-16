package com.atstudio.atstudio.config;

import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AcceptanceStartupGuard implements ApplicationRunner, Ordered {

    private static final String ACCEPTANCE_PROFILE = "acceptance";
    private static final Set<String> EXPLICIT_NON_PRODUCTION_PROFILES = Set.of(
            ACCEPTANCE_PROFILE,
            "local",
            "dev",
            "development",
            "test",
            "qa",
            "stage",
            "staging"
    );

    private final Environment environment;
    private final AcceptanceProperties acceptanceProperties;
    private final TestUserBootstrapProperties bootstrapProperties;
    private final PaymentProperties paymentProperties;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void run(ApplicationArguments args) {
        validate();
    }

    void validate() {
        if (paymentProperties.getProvider() == PaymentProviderType.TOSS_BILLING) {
            BillingKeyCrypto.validateConfiguration(paymentProperties);
        }

        Set<String> activeProfiles = activeProfiles();
        boolean acceptanceProfileActive = activeProfiles.contains(ACCEPTANCE_PROFILE);
        boolean acceptanceFlagEnabled = acceptanceProperties.isEnabled();
        boolean bootstrapEnabled = bootstrapProperties.isEnabled();

        if (containsProductionProfile(activeProfiles)
                && (acceptanceProfileActive || acceptanceFlagEnabled || bootstrapEnabled)) {
            refuse("Acceptance and test-user bootstrap are forbidden in production profiles.");
        }
        if (acceptanceFlagEnabled && !acceptanceProfileActive) {
            refuse("Acceptance mode requires the explicit acceptance profile.");
        }
        if (acceptanceProfileActive && !acceptanceFlagEnabled) {
            refuse("The acceptance profile requires app.acceptance.enabled=true.");
        }
        if (bootstrapEnabled && activeProfiles.stream().noneMatch(EXPLICIT_NON_PRODUCTION_PROFILES::contains)) {
            refuse("Test-user bootstrap requires an explicit non-production profile.");
        }
        if (!acceptanceProfileActive && !bootstrapEnabled) {
            return;
        }

        requireResolvedProperty("spring.datasource.url", "SPRING_DATASOURCE_URL");
        requireResolvedProperty("spring.datasource.username", "SPRING_DATASOURCE_USERNAME");
        requireResolvedProperty("spring.datasource.password", "SPRING_DATASOURCE_PASSWORD");
        requireResolvedProperty("jwt.secret", "JWT_SECRET");

        if (bootstrapEnabled) {
            requireResolvedValue(
                    bootstrapProperties.getDefaultPassword(),
                    "APP_BOOTSTRAP_TEST_USERS_DEFAULT_PASSWORD");
        }

        if (acceptanceProfileActive) {
            validateAcceptanceCallbacks();
            validateTossSecretsWhenEnabled();
        }
    }

    private void validateAcceptanceCallbacks() {
        AcceptancePublicUrls expected = AcceptancePublicUrls.from(acceptanceProperties.getPublicBaseUrl());

        requireExpected("app.mail.base-url", environment.getProperty("app.mail.base-url"), expected.mailBaseUrl());
        requireExpected(
                "oauth2.google.redirect-uri",
                environment.getProperty("oauth2.google.redirect-uri"),
                expected.googleRedirectUri());
        requireExpected(
                "oauth2.kakao.redirect-uri",
                environment.getProperty("oauth2.kakao.redirect-uri"),
                expected.kakaoRedirectUri());
        requireExpected(
                "oauth2.naver.redirect-uri",
                environment.getProperty("oauth2.naver.redirect-uri"),
                expected.naverRedirectUri());
        requireExpected(
                "app.payment.toss.success-url",
                paymentProperties.getToss().getSuccessUrl(),
                expected.tossSuccessUrl());
        requireExpected(
                "app.payment.toss.fail-url",
                paymentProperties.getToss().getFailUrl(),
                expected.tossFailUrl());
        requireExpected(
                "app.payment.billing.auth-success-url",
                paymentProperties.getBilling().getAuthSuccessUrl(),
                expected.tossBillingAuthSuccessUrl());
        requireExpected(
                "app.payment.billing.auth-fail-url",
                paymentProperties.getBilling().getAuthFailUrl(),
                expected.tossBillingAuthFailUrl());
    }

    private void validateTossSecretsWhenEnabled() {
        PaymentProviderType provider = paymentProperties.getProvider();
        if (provider != PaymentProviderType.TOSS && provider != PaymentProviderType.TOSS_BILLING) {
            return;
        }

        requireResolvedValue(paymentProperties.getToss().getClientKey(), "TOSS_CLIENT_KEY");
        requireResolvedValue(paymentProperties.getToss().getSecretKey(), "TOSS_SECRET_KEY");
        requireResolvedValue(
                paymentProperties.getBilling().getEncryptionSecret(),
                "PAYMENT_BILLING_KEY_ENCRYPTION_SECRET");
    }

    private Set<String> activeProfiles() {
        return Arrays.stream(environment.getActiveProfiles())
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean containsProductionProfile(Set<String> activeProfiles) {
        return activeProfiles.stream().anyMatch(profile ->
                profile.equals("prod")
                        || profile.equals("production")
                        || profile.startsWith("prod-")
                        || profile.startsWith("production-"));
    }

    private void requireResolvedProperty(String propertyName, String externalName) {
        requireResolvedValue(environment.getProperty(propertyName), externalName);
    }

    private void requireResolvedValue(String value, String externalName) {
        if (value == null || value.isBlank() || isUnresolvedPlaceholder(value)) {
            refuse("Required external configuration is missing: " + externalName + ".");
        }
    }

    private boolean isUnresolvedPlaceholder(String value) {
        String trimmed = value.trim();
        return trimmed.startsWith("${") && trimmed.endsWith("}");
    }

    private void requireExpected(String propertyName, String actual, String expected) {
        if (!expected.equals(actual)) {
            refuse("Configured callback does not match APP_PUBLIC_BASE_URL: " + propertyName + ".");
        }
    }

    private void refuse(String reason) {
        throw new IllegalStateException("Acceptance startup refused: " + reason);
    }
}
