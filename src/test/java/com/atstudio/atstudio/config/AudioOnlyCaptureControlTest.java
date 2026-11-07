package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.audio.*;
import com.atstudio.atstudio.service.storage.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.MessageDigest;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AudioOnlyCaptureControlTest {
    @TempDir Path root;
    private final ObjectMapper json = new ObjectMapper();

    private ApplicationContextRunner base() {
        return new ApplicationContextRunner().withInitializer(context -> {
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
            context.getEnvironment().getPropertySources().remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
        }).withUserConfiguration(BackgroundTasksConfiguration.class, CaptureControlOnlyConfiguration.class);
    }
    private ApplicationContextRunner audioMode() {
        return base().withPropertyValues("app.background-tasks.enabled=true", "app.background-tasks.audio-only=true",
                "app.background-tasks.capture-control-only=false", "app.audio.worker-enabled=true",
                "app.storage.capture-control-enabled=true", "app.storage.capture-control-poll-ms=100");
    }
    private ApplicationContextRunner withWorker(StorageContinuityGuard guard, StorageCaptureMailbox mailbox) {
        StorageService storage = mock(StorageService.class);
        when(storage.enterMutation()).thenAnswer(call -> guard.enter());
        return audioMode().withUserConfiguration(TrackAudioProcessingWorker.class, com.atstudio.atstudio.support.WorkerCatalogTestConfiguration.class)
                .withBean(StorageCaptureMailbox.class, () -> mailbox)
                .withBean(StorageService.class, () -> storage)
                .withBean(TrackAudioProcessingTransactions.class, () -> mock(TrackAudioProcessingTransactions.class))
                .withBean(StorageMutationCoordinator.class, () -> mock(StorageMutationCoordinator.class))
                .withBean(AudioAnalysisService.class, AudioAnalysisService::new)
                .withBean(FfmpegAudioEncoder.class, () -> mock(FfmpegAudioEncoder.class));
    }
    private void ready(org.springframework.context.ConfigurableApplicationContext context) {
        context.publishEvent(new ApplicationReadyEvent(new SpringApplication(Object.class), new String[0], context, Duration.ZERO));
    }
    private void writePrivate(Path path, String body) throws Exception {
        Files.writeString(path, body);
        if (path.getFileSystem().supportedFileAttributeViews().contains("posix"))
            Files.setPosixFilePermissions(path, PosixFilePermissions.fromString("rw-------"));
    }
    private String request(String action, String capture, String extra) throws Exception {
        String id = action.equals("FREEZE") ? capture : UUID.randomUUID().toString();
        String target = action.equals("FREEZE") ? "" : ",\"capture_id\":\"" + capture + "\"";
        writePrivate(root.resolve("control/requests/" + id + ".json"), "{\"version\":1,\"protocol\":\"ats-online-v1\",\"request_id\":\""
                + id + "\",\"action\":\"" + action + "\"" + target + extra + "}");
        return id;
    }
    private com.fasterxml.jackson.databind.JsonNode response(String id) throws Exception {
        Path path = root.resolve("control/responses/" + id + ".json");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!Files.exists(path) && System.nanoTime() < deadline) Thread.sleep(10);
        assertThat(path).exists();
        return json.readTree(Files.readAllBytes(path));
    }

    @Test
    void actualAudioModeConsumesFreezeOnceSurvivesRestartAndRequiresReceiptForAbort() throws Exception {
        String capture = UUID.randomUUID().toString();
        AtomicInteger snapshots = new AtomicInteger();
        CaptureReferenceSource references = () -> { snapshots.incrementAndGet(); return List.of(); };
        byte[] frozenResponse;
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = new StorageCaptureMailbox(root.resolve("control"), guard, references, root.resolve("public"), root.resolve("private"));
            request("FREEZE", capture, ",\"timeout_seconds\":2");
            withWorker(guard, mailbox).run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(TrackAudioProcessingWorker.class)
                        .hasSingleBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class)
                        .doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
                ready(context.getSourceApplicationContext());
                // Dedicated poller ready handling must be idempotent, independently of worker events.
                context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class).start();
                assertThat(response(capture).path("status").asText()).isEqualTo("FROZEN");
                assertThat(guard.retainedCaptureId()).isEqualTo(capture);
                assertThatThrownBy(guard::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
                var poller = context.getBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
                context.close();
                assertThat(poller.terminated()).isTrue();
            });
            frozenResponse = Files.readAllBytes(root.resolve("control/responses/" + capture + ".json"));
        }
        // A retained FREEZE request/claim/response remains on disk across a new guard and application context.
        try (var guard = new StorageContinuityGuard(root.resolve("state").toString())) {
            var mailbox = new StorageCaptureMailbox(root.resolve("control"), guard, references, root.resolve("public"), root.resolve("private"));
            withWorker(guard, mailbox).run(context -> {
                assertThat(context).hasNotFailed();
                ready(context.getSourceApplicationContext());
                assertThat(response(request("STATUS", capture, "")).path("status").asText()).isEqualTo("FROZEN");
                assertThat(Files.readAllBytes(root.resolve("control/responses/" + capture + ".json"))).isEqualTo(frozenResponse);
                assertThat(snapshots.get()).isEqualTo(1);
                response(request("ABORT", capture, ",\"receipt_sha256\":\"" + "0".repeat(64) + "\""));
                assertThat(guard.retainedCaptureId()).isEqualTo(capture);
                String receipt = "{\"version\":1,\"protocol\":\"ats-online-v1\",\"status\":\"ABANDONED\",\"capture_id\":\"" + capture
                        + "\",\"run_id\":\"" + "a".repeat(32) + "\",\"helpers_ended\":true,\"readers_ended\":true,\"reconciled_at\":\"" + Instant.now() + "\"}";
                Path receiptPath = root.resolve("control/captures/" + capture + "/abandon-receipt.json");
                writePrivate(receiptPath, receipt);
                String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(receiptPath)));
                assertThat(response(request("ABORT", capture, ",\"receipt_sha256\":\"" + digest + "\"")).path("status").asText()).isEqualTo("ABORTED");
                try (var lease = guard.enter()) { assertThat(lease).isNotNull(); }
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"app.background-tasks.enabled=false", "app.background-tasks.audio-only=false", "app.storage.capture-control-enabled=false"})
    void excludedModesNeverCreateDedicatedPoller(String override) {
        audioMode().withPropertyValues(override).run(context -> {
            assertThat(context).hasNotFailed().doesNotHaveBean(CaptureControlOnlyConfiguration.CaptureControlPoller.class);
            if (override.equals("app.background-tasks.audio-only=false"))
                assertThat(context).hasSingleBean(ScheduledAnnotationBeanPostProcessor.class);
            else assertThat(context).doesNotHaveBean(ScheduledAnnotationBeanPostProcessor.class);
        });
    }
    @ParameterizedTest
    @ValueSource(strings = {"app.background-tasks.enabled=invalid", "app.background-tasks.audio-only= true ",
            "app.storage.capture-control-enabled=invalid", "app.background-tasks.capture-control-only=invalid"})
    void malformedFlagsFailBeforeOrdinaryBeanCreation(String override) {
        Runnable sentinel = mock(Runnable.class);
        audioMode().withInitializer(context -> context.getEnvironment().getPropertySources().addFirst(
                new org.springframework.core.env.MapPropertySource("exact-invalid-flag",
                        Map.of(override.substring(0, override.indexOf('=')), override.substring(override.indexOf('=') + 1)))))
                .withBean("sentinel", Object.class, () -> { sentinel.run(); return new Object(); })
                .run(context -> { assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("must be true or false");
                    verifyNoInteractions(sentinel); });
    }
}
