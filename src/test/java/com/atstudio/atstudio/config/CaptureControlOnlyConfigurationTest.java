package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.audio.TrackAudioProcessingWorker;
import com.atstudio.atstudio.service.storage.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaptureControlOnlyConfigurationTest {
    @TempDir Path root;

    private ApplicationContextRunner base() {
        return new ApplicationContextRunner().withInitializer(context -> {
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        }).withUserConfiguration(BackgroundTasksConfiguration.class, CaptureControlOnlyConfiguration.class);
    }

    private ApplicationContextRunner enabled() {
        return base().withPropertyValues("app.background-tasks.capture-control-only=true",
                "app.background-tasks.enabled=false", "app.background-tasks.audio-only=false",
                "app.audio.worker-enabled=false", "app.storage.capture-control-enabled=true",
                "app.storage.continuity-state-path=" + root.resolve("state"),
                "app.storage.capture-control-path=" + root.resolve("control"),
                "spring.sql.init.mode=never", "spring.jpa.hibernate.ddl-auto=validate",
                "app.storage.capture-control-poll-ms=100");
    }

    @Test
    void realContextRunsOnePrivatePollerWithoutAudioRecoveryOrGeneralScheduling() throws Exception {
        StorageCaptureMailbox mailbox = mock(StorageCaptureMailbox.class);
        AtomicInteger active = new AtomicInteger(), peak = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1), finish = new CountDownLatch(1);
        doAnswer(invocation -> {
            peak.accumulateAndGet(active.incrementAndGet(), Math::max);
            entered.countDown();
            try { finish.await(3, TimeUnit.SECONDS); } finally { active.decrementAndGet(); }
            return null;
        }).when(mailbox).processOnce();
        StorageMutationJournalService journal = mock(StorageMutationJournalService.class);
        AtomicReference<CaptureControlOnlyConfiguration.CaptureControlPoller> poller = new AtomicReference<>();
        enabled().withBean(StorageCaptureMailbox.class, () -> mailbox)
                .withUserConfiguration(StorageMutationRecoveryService.class, TrackAudioProcessingWorker.class)
                .withBean(StorageContinuityGuard.class, StorageContinuityGuard::disabled)
                .withBean(StorageMutationJournalService.class, () -> journal)
                .withBean(StorageCleanupService.class, () -> mock(StorageCleanupService.class))
                .withBean(StorageReferenceChecker.class, () -> mock(StorageReferenceChecker.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    clearInvocations(journal); // Ignore the existing guard setter injection on the mock.
                    assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                    assertThat(context).doesNotHaveBean(TrackAudioProcessingWorker.class);
                    poller.set(context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class));
                    verifyNoInteractions(mailbox);
                    var ready = new ApplicationReadyEvent(new SpringApplication(Object.class), new String[0],
                            context.getSourceApplicationContext(), Duration.ZERO);
                    context.publishEvent(ready);
                    context.publishEvent(ready);
                    assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
                    verify(mailbox, times(1)).processOnce();
                    verifyNoInteractions(journal);
                    assertThat(peak.get()).isEqualTo(1);
                    finish.countDown();
                });
        assertThat(poller.get().terminated()).isTrue();
    }

    @Test
    void shutdownInterruptsBlockedCaptureWithoutReleasingDurableFreeze() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            StorageCaptureMailbox mailbox = new StorageCaptureMailbox(root.resolve("control"), guard, () -> {
                entered.countDown();
                try { new CountDownLatch(1).await(); }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Synthetic interrupted inventory");
                }
                return List.of();
            }, root.resolve("public"), root.resolve("private"));
            String capture = UUID.randomUUID().toString();
            Path request = root.resolve("control/requests/" + capture + ".json");
            Files.writeString(request, "{\"version\":1,\"protocol\":\"ats-online-v1\",\"request_id\":\"" + capture
                    + "\",\"action\":\"FREEZE\",\"timeout_seconds\":0}");
            if (request.getFileSystem().supportedFileAttributeViews().contains("posix"))
                Files.setPosixFilePermissions(request, PosixFilePermissions.fromString("rw-------"));
            enabled().withBean(StorageCaptureMailbox.class, () -> mailbox).run(context -> {
                assertThat(context).hasNotFailed();
                context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class), new String[0],
                        context.getSourceApplicationContext(), Duration.ZERO));
                assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
                assertThat(guard.retainedCaptureId()).isEqualTo(capture);
            });
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
        }
        try (var reopened = new StorageContinuityGuard(root.resolve("state").toString())) {
            assertThat(reopened.retainedMode()).isEqualTo("FROZEN");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"app.background-tasks.enabled=true", "app.background-tasks.audio-only=true",
            "app.audio.worker-enabled=true", "app.storage.capture-control-enabled=false",
            "app.bootstrap.test-users.enabled=true", "app.acceptance.enabled=true",
            "spring.sql.init.mode=always", "spring.jpa.hibernate.ddl-auto=update",
            "spring.mail.test-connection=true", "app.storage.continuity-state-path=",
            "app.storage.capture-control-path=", "app.background-tasks.capture-control-only=invalid"})
    void conflictFailsBeforeOrdinaryBeanCreation(String property) {
        Runnable sentinel = mock(Runnable.class);
        enabled().withPropertyValues(property).withBean("sentinel", Object.class, () -> {
            sentinel.run(); return new Object();
        }).run(context -> {
            assertThat(context).hasFailed();
            verifyNoInteractions(sentinel);
        });
    }

    @Test
    void acceptanceProfileIsRejectedEvenWhenFlagsAreFalse() {
        enabled().withInitializer(context -> context.getEnvironment().setActiveProfiles("acceptance"))
                .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 99, 60_001})
    void invalidPollIntervalCannotStart(long interval) {
        StorageCaptureMailbox mailbox = mock(StorageCaptureMailbox.class);
        enabled().withPropertyValues("app.storage.capture-control-poll-ms=" + interval)
                .withBean(StorageCaptureMailbox.class, () -> mailbox).run(context -> {
                    assertThat(context).hasFailed();
                    verifyNoInteractions(mailbox);
                });
    }
}
