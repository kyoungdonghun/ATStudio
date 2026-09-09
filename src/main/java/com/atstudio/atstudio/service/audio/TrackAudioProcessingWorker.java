package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.common.validation.ValidationConstants;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.atstudio.atstudio.service.audio.AudioTranscodeException.Code.*;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.audio", name = "worker-enabled", havingValue = "true", matchIfMissing = true)
public class TrackAudioProcessingWorker {
    private final TrackAudioProcessingTransactions transactions;
    private final StorageService storage;
    private final StorageMutationCoordinator mutations;
    private final AudioAnalysisService analysis;
    private final FfmpegAudioEncoder encoder;
    // Not a Spring scheduler/executor bean: unrelated @Scheduled jobs keep their existing scheduler.
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "audio-processing");
        thread.setDaemon(true);
        return thread;
    });
    @Value("${app.audio.poll-interval-ms:5000}")
    private long pollIntervalMs = 5000;
    private boolean ready;

    public TrackAudioProcessingWorker(TrackAudioProcessingTransactions transactions, StorageService storage,
            StorageMutationCoordinator mutations, AudioAnalysisService analysis, FfmpegAudioEncoder encoder) {
        this.transactions = transactions;
        this.storage = storage;
        this.mutations = mutations;
        this.analysis = analysis;
        this.encoder = encoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        start();
        executor.scheduleWithFixedDelay(() -> {
            try {
                processNext();
            } catch (RuntimeException exception) {
                log.warn("Audio worker iteration failed; persisted work is retained.");
            }
        }, 0, Math.max(1000, pollIntervalMs), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.error("Audio worker did not terminate within its shutdown bound.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void start() {
        if (ready) return;
        while (transactions.recoverInterruptedBatch() > 0) { /* short, bounded transaction per batch */ }
        ready = true;
    }

    public synchronized void processNext() {
        if (!ready) return;
        TrackAudioProcessingTransactions.Claim claim = transactions.claimNext();
        if (claim == null) return;
        try {
            Path input = resolveInput(claim.sourceKey());
            AudioAnalysisResult source = analysis.analyze("source.wav", new FileSystemResource(input));
            try (var generated = mutations.generate(StorageDomain.TRACK, StorageRoot.PUBLIC,
                    "tracks/audio", "stream.mp3", output -> {
                        encoder.encode(input, output, source);
                        validateOutput(output, source);
                    })) {
                transactions.complete(claim, generated, source);
            }
        } catch (AudioTranscodeException exception) {
            if (exception.code() == AUDIO_PROCESS_TERMINATION_FAILED) {
                ready = false;
                log.error("Audio worker halted because its child did not terminate. trackId={}", claim.trackId());
                return;
            }
            transactions.fail(claim, exception.code());
        } catch (AudioAnalysisException exception) {
            transactions.fail(claim, AUDIO_INPUT_UNAVAILABLE);
        } catch (RuntimeException exception) {
            // No file keys, command, media metadata, or native diagnostics enter admin errors/logs.
            log.warn("Audio processing deferred/failed. trackId={}, generation={}", claim.trackId(), claim.generation());
            transactions.fail(claim, AUDIO_STORAGE_FAILED);
        }
    }

    private Path resolveInput(String key) {
        try {
            Path input = storage.loadAsResource(StorageRoot.PUBLIC, key).getFile().toPath();
            long size = Files.size(input);
            if (size < 1 || size > ValidationConstants.AUDIO_MAX_SIZE_BYTES) {
                throw new AudioTranscodeException(AUDIO_INPUT_UNAVAILABLE);
            }
            return input;
        } catch (IOException | RuntimeException exception) {
            throw new AudioTranscodeException(AUDIO_INPUT_UNAVAILABLE);
        }
    }

    private void validateOutput(Path output, AudioAnalysisResult source) {
        try {
            if (!Files.isRegularFile(output) || Files.size(output) < 1) {
                throw new AudioTranscodeException(AUDIO_OUTPUT_INVALID);
            }
            AudioAnalysisResult result = analysis.analyze("stream.mp3", new FileSystemResource(output));
            FfmpegAudioEncoder.OutputFormat expected = FfmpegAudioEncoder.outputFormat(source);
            double inputSeconds = source.decodedFrameCount() / (double) source.sampleRateHz();
            double outputSeconds = result.decodedFrameCount() / (double) result.sampleRateHz();
            Object bitrate = AudioSystem.getAudioFileFormat(output.toFile()).properties().get("mp3.bitrate.nominal.bps");
            if (!(bitrate instanceof Number number) || number.intValue() != 128000
                    || result.sampleRateHz() != expected.sampleRateHz()
                    || result.channelCount() != expected.channelCount()
                    || Math.abs(inputSeconds - outputSeconds) > 0.25) {
                throw new AudioTranscodeException(AUDIO_OUTPUT_INVALID);
            }
        } catch (Exception exception) {
            throw new AudioTranscodeException(AUDIO_OUTPUT_INVALID);
        }
    }
}
