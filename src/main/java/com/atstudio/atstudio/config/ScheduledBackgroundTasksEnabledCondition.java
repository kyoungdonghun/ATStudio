package com.atstudio.atstudio.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ScheduledBackgroundTasksEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return BackgroundTasksEnabledCondition.isScheduledEnabled(context.getEnvironment());
    }
}
