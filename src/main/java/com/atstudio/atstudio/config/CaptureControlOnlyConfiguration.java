package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.storage.StorageCaptureMailbox;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Explicit private control mode; never enables Spring's general scheduled-method processor. */
@Configuration(proxyBeanMethods = false)
@Conditional(CaptureControlOnlyConfiguration.DedicatedCaptureControlCondition.class)
public class CaptureControlOnlyConfiguration {
    @Bean
    CaptureControlPoller captureControlPoller(StorageCaptureMailbox mailbox, Environment environment,
            @Value("${app.storage.capture-control-poll-ms:1000}") long intervalMs) {
        if (!dedicatedPollingEnabled(environment)) {
            throw new IllegalStateException("Private capture mode is not enabled");
        }
        return new CaptureControlPoller(mailbox, intervalMs);
    }

    private static boolean dedicatedPollingEnabled(Environment environment) {
        if (BackgroundTasksEnabledCondition.validateCaptureControlOnly(environment)) return true;
        boolean enabled = BackgroundTasksEnabledCondition.isEnabled(environment);
        boolean audioOnly = BackgroundTasksEnabledCondition.readBoolean(environment,
                "app.background-tasks.audio-only", "false");
        boolean captureEnabled = BackgroundTasksEnabledCondition.readBoolean(environment,
                "app.storage.capture-control-enabled", "false");
        // Normal mode uses the mailbox's existing @Scheduled consumer, never both consumers.
        return enabled && audioOnly && captureEnabled;
    }

    static final class DedicatedCaptureControlCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return dedicatedPollingEnabled(context.getEnvironment());
        }
    }

    static final class CaptureControlPoller implements DisposableBean {
        private final StorageCaptureMailbox mailbox;
        private final long intervalMs;
        private final ScheduledExecutorService executor;
        private boolean started;
        private boolean closed;

        CaptureControlPoller(StorageCaptureMailbox mailbox, long intervalMs) {
            if (intervalMs < 100 || intervalMs > 60_000) {
                throw new IllegalArgumentException("Private capture poll interval must be 100..60000 ms");
            }
            this.mailbox = mailbox;
            this.intervalMs = intervalMs;
            this.executor = Executors.newSingleThreadScheduledExecutor(task -> {
                Thread thread = new Thread(task, "storage-capture-control");
                thread.setDaemon(true);
                return thread;
            });
        }

        @EventListener(ApplicationReadyEvent.class)
        public synchronized void start() {
            if (started || closed) return;
            started = true;
            executor.scheduleWithFixedDelay(() -> {
                try {
                    mailbox.processOnce();
                } catch (RuntimeException exception) {
                    // The mailbox retains uncertain capture state; no automatic release/retry effect.
                    // Keep subsequent STATUS/reconciliation commands available without logging private data.
                }
            }, 0, intervalMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public synchronized void destroy() {
            if (closed) return;
            closed = true;
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Private capture poller did not stop; retained state requires reconciliation");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Private capture shutdown interrupted; retained state requires reconciliation");
            }
        }

        boolean terminated() { return executor.isTerminated(); }
    }
}
