package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.storage.StorageCaptureMailbox;
import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** WI012 independent private polling checks; no app boot, database or outbound clients. */
class CaptureControlOnlyIndependentTest {
    @TempDir Path root;

    private ApplicationContextRunner base() {
        return new ApplicationContextRunner().withInitializer(context -> {
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        }).withUserConfiguration(BackgroundTasksConfiguration.class, CaptureControlOnlyConfiguration.class);
    }

    private ApplicationContextRunner captureMode() {
        return base().withPropertyValues("app.background-tasks.capture-control-only=true",
                "app.background-tasks.enabled=false", "app.background-tasks.audio-only=false",
                "app.audio.worker-enabled=false", "app.storage.capture-control-enabled=true",
                "app.storage.continuity-state-path=" + root.resolve("state"),
                "app.storage.capture-control-path=" + root.resolve("control"),
                "spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=validate",
                "app.storage.capture-control-poll-ms=100");
    }

    @Test
    void defaultOffDoesNotRequireMailboxOrEnableAnyScheduler() {
        base().withPropertyValues("app.background-tasks.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
            assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
            assertThat(context).doesNotHaveBean(StorageCaptureMailbox.class);
        });
    }

    @Test
    void actualMailboxFreezeSurvivesPollerShutdownAndGuardReopen() throws Exception {
        String capture = UUID.randomUUID().toString();
        Path state = root.resolve("state");
        try (var guard = new StorageContinuityGuard(state.toString())) {
            var mailbox = new StorageCaptureMailbox(root.resolve("control"), guard, List::of,
                    root.resolve("public"), root.resolve("private"));
            captureMode().withBean(StorageCaptureMailbox.class, () -> mailbox).run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasSingleBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
                assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                Path request = root.resolve("control/requests/" + capture + ".json");
                String body = "{\"version\":1,\"protocol\":\"ats-online-v1\",\"request_id\":\"" + capture
                        + "\",\"action\":\"FREEZE\",\"timeout_seconds\":0}";
                Files.writeString(request, body);
                if (request.getFileSystem().supportedFileAttributeViews().contains("posix"))
                    Files.setPosixFilePermissions(request, PosixFilePermissions.fromString("rw-------"));
                var event = new ApplicationReadyEvent(new SpringApplication(Object.class), new String[0],
                        context.getSourceApplicationContext(), Duration.ZERO);
                context.publishEvent(event);
                context.publishEvent(event);
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
                while (!Files.exists(root.resolve("control/responses/" + capture + ".json")) && System.nanoTime() < deadline)
                    Thread.sleep(10);
                assertThat(guard.retainedMode()).isEqualTo("FROZEN");
                var poller = context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
                context.close();
                assertThat(poller.terminated()).isTrue();
                assertThat(guard.retainedCaptureId()).isEqualTo(capture);
            });
        }
        try (var reopened = new StorageContinuityGuard(state.toString())) {
            assertThat(reopened.retainedMode()).isEqualTo("FROZEN");
            assertThat(reopened.retainedCaptureId()).isEqualTo(capture);
        }
    }

    @Test
    void duplicateReadyCreatesNoSecondPollAndShutdownInterruptsInFlightPoll() throws Exception {
        var mailbox = mock(StorageCaptureMailbox.class);
        CountDownLatch entered = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        doAnswer(call -> {
            entered.countDown();
            try { new CountDownLatch(1).await(); }
            catch (InterruptedException expected) { interrupted.set(true); Thread.currentThread().interrupt(); }
            return null;
        }).when(mailbox).processOnce();
        captureMode().withBean(StorageCaptureMailbox.class, () -> mailbox).run(context -> {
            assertThat(context).hasNotFailed();
            var event = new ApplicationReadyEvent(new SpringApplication(Object.class), new String[0],
                    context.getSourceApplicationContext(), Duration.ZERO);
            context.publishEvent(event);
            context.publishEvent(event);
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            var poller = context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
            context.close();
            assertThat(interrupted).isTrue();
            assertThat(poller.terminated()).isTrue();
            verify(mailbox, times(1)).processOnce();
        });
    }

    @Test
    void schemaMutationSettingFailsBeforeOrdinaryBeanCreation() {
        AtomicBoolean created = new AtomicBoolean();
        captureMode().withPropertyValues("spring.jpa.hibernate.ddl-auto=update")
                .withBean("sentinel", Object.class, () -> { created.set(true); return new Object(); })
                .run(context -> { assertThat(context).hasFailed(); assertThat(created).isFalse(); });
    }

    @Test
    void accidentalGeneralSchedulingImportFailsClosed() {
        captureMode().withUserConfiguration(AccidentalScheduling.class)
                .withBean(StorageCaptureMailbox.class, () -> mock(StorageCaptureMailbox.class))
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class AccidentalScheduling { }
}
