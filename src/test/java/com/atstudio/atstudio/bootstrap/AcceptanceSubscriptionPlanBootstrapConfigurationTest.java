package com.atstudio.atstudio.bootstrap;

import com.atstudio.atstudio.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("Acceptance subscription plan bootstrap configuration")
class AcceptanceSubscriptionPlanBootstrapConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BootstrapConfiguration.class);

    @Test
    @DisplayName("Runner activates only with the acceptance profile and both flags")
    void runner_requiresAcceptanceProfileAndBothFlags() {
        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("acceptance"))
                .withPropertyValues(
                        "app.acceptance.enabled=true",
                        "app.bootstrap.test-users.enabled=true"
                )
                .run(context -> assertThat(context.getBeansOfType(
                        AcceptanceSubscriptionPlanBootstrapRunner.class
                )).hasSize(1));

        contextRunner
                .withPropertyValues(
                        "app.acceptance.enabled=true",
                        "app.bootstrap.test-users.enabled=true"
                )
                .run(context -> assertThat(context.getBeansOfType(
                        AcceptanceSubscriptionPlanBootstrapRunner.class
                )).isEmpty());

        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("acceptance"))
                .withPropertyValues("app.acceptance.enabled=true")
                .run(context -> assertThat(context.getBeansOfType(
                        AcceptanceSubscriptionPlanBootstrapRunner.class
                )).isEmpty());

        contextRunner
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("acceptance"))
                .withPropertyValues("app.bootstrap.test-users.enabled=true")
                .run(context -> assertThat(context.getBeansOfType(
                        AcceptanceSubscriptionPlanBootstrapRunner.class
                )).isEmpty());
    }

    @Configuration(proxyBeanMethods = false)
    @Import(AcceptanceSubscriptionPlanBootstrapRunner.class)
    static class BootstrapConfiguration {

        @Bean
        SubscriptionRepository subscriptionRepository() {
            return mock(SubscriptionRepository.class);
        }
    }
}
