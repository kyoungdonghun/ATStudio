package com.atstudio.atstudio.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

@Configuration(proxyBeanMethods = false)
public class BackgroundTasksConfiguration {

    @Bean
    static BeanFactoryPostProcessor backgroundTasksSchedulingGuard(Environment environment) {
        boolean enabled = BackgroundTasksEnabledCondition.isScheduledEnabled(environment);
        return beanFactory -> {
            // Refuse accidental scheduling imports before post-processors can register jobs.
            if (!enabled && beanFactory.getBeanNamesForType(
                    ScheduledAnnotationBeanPostProcessor.class, true, false).length > 0) {
                throw new IllegalStateException(
                        "Scheduling must not be registered when background tasks are disabled or audio-only mode is enabled");
            }
        };
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(ScheduledBackgroundTasksEnabledCondition.class)
    @EnableScheduling
    static class EnabledScheduling {
    }
}
