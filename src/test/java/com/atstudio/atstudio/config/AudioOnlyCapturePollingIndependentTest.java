package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.storage.StorageCaptureMailbox;
import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/** WI012 independent condition and shutdown boundaries; no network, database or native process. */
class AudioOnlyCapturePollingIndependentTest {
    @TempDir Path root;

    private ApplicationContextRunner audioMode() {
        return new ApplicationContextRunner().withInitializer(context -> {
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        }).withUserConfiguration(BackgroundTasksConfiguration.class, CaptureControlOnlyConfiguration.class)
                .withPropertyValues("app.background-tasks.enabled=true", "app.background-tasks.audio-only=true",
                        "app.background-tasks.capture-control-only=false", "app.audio.worker-enabled=true",
                        "app.storage.capture-control-enabled=true", "app.storage.capture-control-poll-ms=100");
    }

    @ParameterizedTest
    @ValueSource(strings = {"app.background-tasks.audio-only", "app.background-tasks.capture-control-only",
            "app.storage.capture-control-enabled"})
    void disabledMasterDoesNotHideMalformedModeFlags(String key) {
        AtomicBoolean created = new AtomicBoolean();
        audioMode().withPropertyValues("app.background-tasks.enabled=false")
                .withInitializer(context -> context.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("malformed", Map.of(key, "invalid"))))
                .withBean("sentinel", Object.class, () -> { created.set(true); return new Object(); })
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(created).isFalse();
                });
    }

    @Test
    void accidentalGeneralSchedulerInAudioOnlyModeFailsBeforeOrdinaryBeans() {
        AtomicBoolean created = new AtomicBoolean();
        audioMode().withUserConfiguration(AccidentalScheduling.class)
                .withBean("sentinel", Object.class, () -> { created.set(true); return new Object(); })
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(created).isFalse();
                });
    }

    @Test
    void closeBeforeReadyCannotConsumeRetainedRequestOrRestartPoller() throws Exception {
        String capture = UUID.randomUUID().toString();
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = new StorageCaptureMailbox(root.resolve("control"), guard, List::of,
                    root.resolve("public"), root.resolve("private"));
            Path request = root.resolve("control/requests/" + capture + ".json");
            String body = "{\"version\":1,\"protocol\":\"ats-online-v1\",\"request_id\":\"" + capture
                    + "\",\"action\":\"FREEZE\",\"timeout_seconds\":0}";
            Files.writeString(request, body);
            if (request.getFileSystem().supportedFileAttributeViews().contains("posix"))
                Files.setPosixFilePermissions(request, PosixFilePermissions.fromString("rw-------"));
            audioMode().withBean(StorageCaptureMailbox.class, () -> mailbox).run(context -> {
                assertThat(context).hasNotFailed()
                        .hasSingleBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class)
                        .doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                var poller = context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
                context.close();
                poller.start();
                assertThat(poller.terminated()).isTrue();
                assertThat(guard.retainedMode()).isEqualTo("EMPTY");
                assertThat(Files.readString(request)).isEqualTo(body);
                assertThat(root.resolve("control/claims/" + capture + ".json")).doesNotExist();
                assertThat(root.resolve("control/responses/" + capture + ".json")).doesNotExist();
            });
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    static class AccidentalScheduling { }
}
