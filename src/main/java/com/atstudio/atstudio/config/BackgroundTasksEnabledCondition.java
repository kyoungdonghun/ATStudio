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
        validateCaptureControlOnly(environment);
        return enabled;
    }

    static boolean validateCaptureControlOnly(Environment environment) {
        boolean captureOnly = readBoolean(environment, "app.background-tasks.capture-control-only", "false");
        if (!captureOnly) return false;
        if (readBoolean(environment, PROPERTY, "true")
                || readBoolean(environment, AUDIO_ONLY_PROPERTY, "false")
                || readBoolean(environment, "app.audio.worker-enabled", "true")
                || !readBoolean(environment, "app.storage.capture-control-enabled", "false")
                || readBoolean(environment, "app.bootstrap.test-users.enabled", "false")
                || readBoolean(environment, "app.acceptance.enabled", "false")
                || readBoolean(environment, "spring.mail.test-connection", "false")) {
            throw new IllegalStateException("Capture-control-only mode requires disabled business workers/bootstrap and enabled private control");
        }
        if (!"never".equals(environment.getProperty("spring.sql.init.mode"))
                || !"validate".equals(environment.getProperty("spring.jpa.hibernate.ddl-auto"))) {
            throw new IllegalStateException("Capture-control-only mode requires SQL init never and Hibernate validate");
        }
        if (java.util.Arrays.stream(environment.getActiveProfiles()).anyMatch("acceptance"::equalsIgnoreCase)
                || environment.getProperty("app.storage.continuity-state-path", "").isBlank()
                || environment.getProperty("app.storage.capture-control-path", "").isBlank()) {
            throw new IllegalStateException("Capture-control-only mode requires private state/control paths and no acceptance profile");
        }
        return true;
    }

    static boolean isScheduledEnabled(Environment environment) {
        boolean enabled = isEnabled(environment);
        return enabled && !readBoolean(environment, AUDIO_ONLY_PROPERTY, "false");
    }

    static boolean readBoolean(Environment environment, String property, String defaultValue) {
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
