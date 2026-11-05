package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.atstudio.atstudio.service.audio.AudioTranscodeException.Code.AUDIO_PROCESS_TERMINATION_FAILED;

class AudioContinuityIndependentTest {
    @TempDir Path temporary;

    @Test
    void terminalNativeChildFailureCannotBeReactivatedByTheNextPoll() throws Exception {
        var transactions = mock(TrackAudioProcessingTransactions.class);
        var storage = mock(StorageService.class);
        var mutations = mock(StorageMutationCoordinator.class);
        var analysis = mock(AudioAnalysisService.class);
        var encoder = mock(FfmpegAudioEncoder.class);
        var worker = new TrackAudioProcessingWorker(transactions, storage, mutations, analysis, encoder);
        var guard = new StorageContinuityGuard(temporary.resolve("state").toString());
        when(storage.enterMutation()).thenAnswer(invocation -> guard.enter());
        doAnswer(invocation -> { guard.haltForUncertainWriter(); return null; }).when(storage).haltForUncertainWriter();
        var claim = new TrackAudioProcessingTransactions.Claim(1L, 1L, "synthetic-claim", "tracks/source.wav");
        var input = Files.write(temporary.resolve("source.wav"), new byte[]{1});
        when(transactions.claimNext()).thenReturn(claim);
        when(storage.loadAsResource(StorageRoot.PUBLIC, claim.sourceKey())).thenReturn(new FileSystemResource(input));
        when(analysis.analyze(eq("source.wav"), any())).thenReturn(new AudioAnalysisResult(
                10, "[0.0]", AudioAnalysisFormat.WAV, 441000, 44100, 2));
        when(mutations.generate(any(), any(), anyString(), anyString(), any()))
                .thenThrow(new AudioTranscodeException(AUDIO_PROCESS_TERMINATION_FAILED));
        try {
            worker.start();
            worker.processNext();
            clearInvocations(transactions, mutations, analysis, encoder);
            worker.processNext();
            verifyNoInteractions(transactions, mutations, analysis, encoder);
            assertThatThrownBy(() -> guard.freeze(Duration.ZERO)).isInstanceOf(IllegalStateException.class);
            String retained = guard.retainedCaptureId();
            guard.close();
            try (var reopened = new StorageContinuityGuard(temporary.resolve("state").toString())) {
                assertThat(reopened.retainedCaptureId()).isEqualTo(retained);
                assertThatThrownBy(() -> reopened.freeze(Duration.ZERO)).isInstanceOf(IllegalStateException.class);
                assertThatThrownBy(reopened::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            }
        } finally { worker.stop(); guard.close(); }
    }

    @Test
    void frozenAdmissionDoesNotClaimOrFailAnAudioJob() {
        var transactions = mock(TrackAudioProcessingTransactions.class);
        var storage = mock(StorageService.class);
        var mutations = mock(StorageMutationCoordinator.class);
        var analysis = mock(AudioAnalysisService.class);
        var encoder = mock(FfmpegAudioEncoder.class);
        var worker = new TrackAudioProcessingWorker(transactions, storage, mutations, analysis, encoder);
        try {
            worker.start();
            clearInvocations(transactions);
            when(storage.enterMutation()).thenThrow(new StorageContinuityGuard.AdmissionClosedException());
            worker.processNext();
            verifyNoInteractions(transactions, mutations, analysis, encoder);
        } finally { worker.stop(); }
    }
}
