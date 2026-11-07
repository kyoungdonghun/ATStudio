package com.atstudio.atstudio.config;

import com.atstudio.atstudio.service.audio.TrackAudioProcessingWorker;
import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

/** Whole local application/H2 graph with real audio worker and private mailbox, no network listener. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = {
        "app.listening.enabled=true", "app.listening.catalog-source=DATABASE",
        "app.background-tasks.enabled=true", "app.background-tasks.audio-only=true",
        "app.background-tasks.capture-control-only=false", "app.audio.worker-enabled=true",
        "app.storage.capture-control-enabled=true", "app.storage.capture-control-poll-ms=100",
        "app.bootstrap.test-users.enabled=false", "app.acceptance.enabled=false",
        "spring.mail.test-connection=false", "spring.datasource.url=jdbc:h2:mem:wi010-audio;DB_CLOSE_DELAY=-1"
})
@org.springframework.context.annotation.Import(AudioOnlyCaptureApplicationTest.CatalogFixture.class)
@DirtiesContext
class AudioOnlyCaptureApplicationTest {
    static final Path ROOT;
    static { try { ROOT = Files.createTempDirectory(Path.of("build").toAbsolutePath(), "wi010-application-"); }
        catch (java.io.IOException exception) { throw new ExceptionInInitializerError(exception); } }
    @DynamicPropertySource static void isolatedRoots(DynamicPropertyRegistry properties) {
        properties.add("app.storage.public-path", () -> ROOT.resolve("public").toString());
        properties.add("app.storage.private-path", () -> ROOT.resolve("private").toString());
        properties.add("app.storage.continuity-state-path", () -> ROOT.resolve("state").toString());
        properties.add("app.storage.capture-control-path", () -> ROOT.resolve("control").toString());
    }
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods = false)
    static class CatalogFixture {
        @org.springframework.context.annotation.Bean
        org.springframework.boot.ApplicationRunner emptyLocalCatalog(com.atstudio.atstudio.repository.SiteSettingRepository settings) {
            return args -> settings.saveAndFlush(new com.atstudio.atstudio.entity.SiteSetting(
                    com.atstudio.atstudio.service.ReservedSettingPolicy.CATALOG_KEY, "{\"revision\":0,\"trackIds\":[]}"));
        }
    }
    @Autowired ApplicationContext context;
    @Autowired StorageContinuityGuard guard;
    @Test void realApplicationConsumesPrivateFreezeWhileAudioWorkerIsEnabled() throws Exception {
        assertThat(context.getBeansOfType(TrackAudioProcessingWorker.class)).hasSize(1);
        assertThat(context.getBeansOfType(CaptureControlOnlyConfiguration.CaptureControlPoller.class)).hasSize(1);
        assertThat(context.getBeansOfType(ScheduledAnnotationBeanPostProcessor.class)).isEmpty();
        String capture = UUID.randomUUID().toString();
        Path request = ROOT.resolve("control/requests/" + capture + ".json");
        Files.writeString(request, "{\"version\":1,\"protocol\":\"ats-online-v1\",\"request_id\":\"" + capture
                + "\",\"action\":\"FREEZE\",\"timeout_seconds\":2}");
        if (request.getFileSystem().supportedFileAttributeViews().contains("posix"))
            Files.setPosixFilePermissions(request, PosixFilePermissions.fromString("rw-------"));
        Path response = ROOT.resolve("control/responses/" + capture + ".json");
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (!Files.exists(response) && System.nanoTime() < deadline) Thread.sleep(10);
        assertThat(response).exists();
        assertThat(new ObjectMapper().readTree(Files.readAllBytes(response)).path("status").asText()).isEqualTo("FROZEN");
        assertThat(guard.retainedCaptureId()).isEqualTo(capture);
        assertThatThrownBy(guard::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
    }
}
