package com.atstudio.atstudio.config;

import com.atstudio.atstudio.common.validation.ValidationConstants;
import com.atstudio.atstudio.service.audio.*;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.FileSystemResource;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AudioProcessingConfigTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TrackAudioProcessingWorker.class, FfmpegAudioEncoder.class)
            .withBean(TrackAudioProcessingTransactions.class, () -> mock(TrackAudioProcessingTransactions.class))
            .withBean(StorageMutationCoordinator.class, () -> mock(StorageMutationCoordinator.class))
            .withBean(StorageService.class, () -> mock(StorageService.class))
            .withBean(AudioAnalysisService.class, AudioAnalysisService::new);

    @Test
    void defaultIsOneDedicatedWorkerAndExplicitDisableDoesNotCreateIt() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(TrackAudioProcessingWorker.class);
            assertThat(context).doesNotHaveBean(ScheduledExecutorService.class);
            assertThat(context).doesNotHaveBean(org.springframework.scheduling.TaskScheduler.class);
        });
        runner.withPropertyValues("app.audio.worker-enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(TrackAudioProcessingWorker.class);
            assertThat(context).doesNotHaveBean(ScheduledExecutorService.class);
        });
    }

    @Test
    void audioCapAndMultipartHeadroomDoNotRelaxOtherDomains() throws Exception {
        var properties = new YamlPropertySourceLoader().load("application", new FileSystemResource("src/main/resources/application.yml")).get(0);
        assertThat(properties.getProperty("spring.servlet.multipart.max-file-size")).isEqualTo("100MB");
        assertThat(properties.getProperty("spring.servlet.multipart.max-request-size")).isEqualTo("120MB");
        assertThat(ValidationConstants.AUDIO_MAX_SIZE_BYTES).isEqualTo(104857600);
        assertThat(ValidationConstants.IMAGE_MAX_SIZE_BYTES).isEqualTo(10485760);
        assertThat(ValidationConstants.ATTACHMENT_MAX_SIZE_BYTES).isEqualTo(20971520);
        assertThat(ValidationConstants.CERT_DOC_MAX_AGGREGATE_SIZE_BYTES).isEqualTo(52428800);
    }
}
