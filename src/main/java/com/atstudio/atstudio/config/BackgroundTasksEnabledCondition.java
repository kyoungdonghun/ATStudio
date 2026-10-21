package com.atstudio.atstudio.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class BackgroundTasksEnabledCondition implements Condition {
    private static final String PROPERTY = "app.background-tasks.enabled";
    private static final String AUDIO_ONLY_PROPERTY = "app.background-tasks.audio-only";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return isEnabled(context.getEnvironment());
    }

    static boolean isEnabled(Environment environment) {
        boolean enabled = readBoolean(environment, PROPERTY, "true");
        // Validate suppression even when the master switch disables every worker.
        readBoolean(environment, AUDIO_ONLY_PROPERTY, "false");
        return enabled;
    }

    static boolean isScheduledEnabled(Environment environment) {
        boolean enabled = isEnabled(environment);
        return enabled && !readBoolean(environment, AUDIO_ONLY_PROPERTY, "false");
    }

    private static boolean readBoolean(Environment environment, String property, String defaultValue) {
        String value = environment.getProperty(property, defaultValue);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalStateException(property + " must be true or false");
    }
}
