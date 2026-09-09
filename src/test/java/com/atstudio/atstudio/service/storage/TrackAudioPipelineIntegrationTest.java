package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.controller.TrackController;
import com.atstudio.atstudio.dto.track.TrackCreateRequest;
import com.atstudio.atstudio.dto.track.TrackUpdateRequest;
import com.atstudio.atstudio.entity.License;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.AudioProcessingState;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.PlayableTrackService;
import com.atstudio.atstudio.service.TrackService;
import com.atstudio.atstudio.service.audio.*;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.testfixture.SyntheticAudioFixtures;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataJpaTest(showSql = false, properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never",
        "spring.jpa.show-sql=false"})
@Import({JpaConfig.class, TrackService.class, DownloadService.class, TrackAudioProcessingTransactions.class,
        AudioAnalysisService.class, CanonicalImageService.class, StorageMutationCoordinator.class,
        StorageMutationJournalService.class, StorageCleanupService.class, StorageReferenceChecker.class,
        StorageIntegrityService.class, TrackAudioPipelineIntegrationTest.Config.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TrackAudioPipelineIntegrationTest {
    @TempDir static Path root;
    @Autowired TrackService tracks;
    @Autowired DownloadService downloads;
    @Autowired TrackRepository repository;
    @Autowired UserRepository users;
    @Autowired LicenseRepository licenses;
    @Autowired TrackAudioProcessingTransactions jobs;
    @Autowired AudioAnalysisService analysis;
    @Autowired StorageMutationCoordinator mutations;
    @Autowired StorageIntegrityService integrity;
    @Autowired StorageReferenceChecker references;
    @Autowired StorageMutationRepository journal;
    @MockitoSpyBean LocalStorageService storage;
    @Autowired PlatformTransactionManager manager;
    @Autowired EntityManager entityManager;
    private User user;
    private CustomUserDetails principal;
    private byte[] originalBytes;

    @BeforeEach
    void setup() {
        user = users.save(User.builder().email(UUID.randomUUID() + "@test.example")
                .nickname("audio").role(UserRole.USER).build());
        principal = CustomUserDetails.builder().id(user.getId()).role(UserRole.USER).build();
        originalBytes = SyntheticAudioFixtures.wav16(44100, 1, 88200);
    }

    @AfterEach
    void cleanH2Only() {
        tx().executeWithoutResult(status -> {
            for (String entity : List.of("License", "TrackDownload", "TrackTag", "Track", "User", "StorageMutation")) {
                entityManager.createQuery("DELETE FROM " + entity).executeUpdate();
            }
        });
    }

    @Test
    void wavRemainsInactiveUntilVerifiedThenStreamsDerivativeAndDownloadsOriginal() throws Exception {
        long id = createWav();
        Track before = track(id);
        assertThat(before.getAudioProcessingState()).isEqualTo(AudioProcessingState.PENDING);
        assertThat(before.isStreamReady()).isFalse();
        assertThat(before.isActive()).isFalse();
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
        assertThatThrownBy(() -> activate(id)).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(BUSINESS_ERROR.AUDIO_STREAM_NOT_READY));
        assertThatThrownBy(() -> tracks.getStreamResource(id)).isInstanceOf(BusinessException.class);
        worker(fakeEncoder()).processNext();
        assertReadyAndOriginal(id);
        activate(id);
        byte[] stream = tracks.getStreamResource(id).resource().getContentAsByteArray();
        assertThat(stream).isEqualTo(SyntheticAudioFixtures.mp3Cbr(128, 77));
        assertThatThrownBy(() -> downloads.download(id, principal)).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        licenses.save(License.builder().user(user).track(track(id)).licenseCode(UUID.randomUUID().toString()).build());
        assertThat(downloads.download(id, principal).getContentAsByteArray()).isEqualTo(originalBytes);
        var mvc = MockMvcBuilders.standaloneSetup(new TrackController(tracks, mock(PlayableTrackService.class), downloads)).build();
        mvc.perform(get("/api/tracks/" + id + "/stream").header("Range", "bytes=10-99"))
                .andExpect(status().isPartialContent()).andExpect(header().string("Content-Range", "bytes 10-99/" + stream.length))
                .andExpect(content().bytes(java.util.Arrays.copyOfRange(stream, 10, 100)));
        assertThat(tracks.getTrack(id).audioFile()).isNull();
        assertThat(tracks.getTrack(id).audioProcessing()).isNull();
    }

    @Test
    void missingEncoderFailsClosedAndRetryIsGenerationFenced() {
        long id = createWav();
        worker(new FfmpegAudioEncoder(root.resolve("not-installed.exe").toString(), 1)).processNext();
        Track failed = track(id);
        assertThat(failed.getAudioProcessingState()).isEqualTo(AudioProcessingState.FAILED);
        assertThat(failed.getAudioErrorCode()).isEqualTo("FFMPEG_NOT_FOUND");
        assertThat(failed.canRetryAudio()).isTrue();
        assertThat(failed.isStreamReady()).isFalse();
        assertThat(storage.exists(StorageRoot.PUBLIC, failed.getPendingAudioFile())).isTrue();
        long generation = failed.getAudioGeneration();
        tracks.retryAudioProcessing(id, generation);
        assertThatThrownBy(() -> tracks.retryAudioProcessing(id, generation)).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(BUSINESS_ERROR.AUDIO_PROCESSING_CONFLICT));
        worker(fakeEncoder()).processNext();
        assertReadyAndOriginal(id);
        assertThat(track(id).getAudioAttemptCount()).isEqualTo(2);
    }

    @Test
    void interruptedClaimRetainsInputAndRequiresExplicitRetry() {
        long id = createWav();
        var abandoned = jobs.claimNext();
        assertThat(references.isReferenced(StorageDomain.TRACK, abandoned.sourceKey())).isTrue();
        assertThat(jobs.recoverInterruptedBatch()).isEqualTo(1);
        Track failed = track(id);
        assertThat(failed.getAudioErrorCode()).isEqualTo("AUDIO_PROCESSING_INTERRUPTED");
        assertThat(failed.getAudioClaimToken()).isNull();
        assertThat(failed.canRetryAudio()).isTrue();
        tracks.retryAudioProcessing(id, failed.getAudioGeneration());
        try (var output = generated()) {
            assertThat(jobs.complete(abandoned, output, wavAnalysis())).isFalse();
        }
        assertThat(track(id).getAudioProcessingState()).isEqualTo(AudioProcessingState.PENDING);
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
    }

    @Test
    void replacementKeepsPairMetadataAndClaimedInputUntilNewestCompletion() {
        long id = createWav();
        worker(fakeEncoder()).processNext();
        activate(id);
        Track ready = track(id);
        String original = ready.getAudioFile();
        String stream = ready.getStreamAudioFile();
        replaceWav(id);
        var oldClaim = jobs.claimNext();
        replaceWav(id);
        Track newest = track(id);
        assertThat(newest.getAudioFile()).isEqualTo(original);
        assertThat(newest.getStreamAudioFile()).isEqualTo(stream);
        assertThat(newest.getDuration()).isEqualTo(ready.getDuration());
        assertThat(newest.getWaveformData()).isEqualTo(ready.getWaveformData());
        assertThat(storage.exists(StorageRoot.PUBLIC, oldClaim.sourceKey())).isTrue();
        assertThat(references.isReferenced(StorageDomain.TRACK, oldClaim.sourceKey())).isTrue();
        try (var output = generated()) {
            assertThat(jobs.complete(oldClaim, output, wavAnalysis())).isFalse();
        }
        assertThat(storage.exists(StorageRoot.PUBLIC, oldClaim.sourceKey())).isFalse();
        assertThat(track(id).getPendingAudioFile()).isEqualTo(newest.getPendingAudioFile());
        var claim = jobs.claimNext();
        TrackUpdateRequest metadata = new TrackUpdateRequest();
        metadata.setTitle("Edited while encoding");
        tracks.updateTrack(id, metadata, null, null);
        try (var output = generated()) {
            assertThat(jobs.complete(claim, output, wavAnalysis())).isTrue();
        }
        assertThat(track(id).getTitle()).isEqualTo("Edited while encoding");
        assertThat(track(id).isActive()).isTrue();
        assertThat(storage.exists(StorageRoot.PUBLIC, original)).isFalse();
        assertThat(storage.exists(StorageRoot.PUBLIC, stream)).isFalse();
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
    }

    @Test
    void deletionCancelsClaimWithoutResurrectionOrDeletingLicensedOriginal() {
        long id = createWav();
        worker(fakeEncoder()).processNext();
        activate(id);
        Track ready = track(id);
        licenses.save(License.builder().user(user).track(ready).licenseCode(UUID.randomUUID().toString()).build());
        replaceWav(id);
        var claim = jobs.claimNext();
        tracks.deleteTrack(id);
        try (var output = generated()) {
            assertThat(jobs.complete(claim, output, wavAnalysis())).isFalse();
        }
        Track cancelled = track(id);
        assertThat(cancelled.isActive()).isFalse();
        assertThat(cancelled.getAudioProcessingState()).isEqualTo(AudioProcessingState.CANCELLED);
        assertThat(cancelled.getAudioFile()).isEqualTo(ready.getAudioFile());
        assertThat(storage.exists(StorageRoot.PUBLIC, ready.getAudioFile())).isTrue();
        assertThat(storage.exists(StorageRoot.PUBLIC, ready.getStreamAudioFile())).isTrue();
        assertThat(storage.exists(StorageRoot.PUBLIC, claim.sourceKey())).isFalse();
        assertThat(licenses.count()).isEqualTo(1);
    }

    @Test
    void failedFinalizeRollsBackFilePairAndOnlyRemovesGeneratedOutput() {
        long id = createWav();
        worker(fakeEncoder()).processNext();
        Track previous = track(id);
        replaceWav(id);
        var claim = jobs.claimNext();
        String outputKey;
        try (var output = generated()) {
            outputKey = output.key();
            tx().executeWithoutResult(status -> {
                assertThat(jobs.complete(claim, output, wavAnalysis())).isTrue();
                status.setRollbackOnly();
            });
        }
        Track rolledBack = track(id);
        assertThat(rolledBack.getAudioFile()).isEqualTo(previous.getAudioFile());
        assertThat(rolledBack.getStreamAudioFile()).isEqualTo(previous.getStreamAudioFile());
        assertThat(storage.exists(StorageRoot.PUBLIC, outputKey)).isFalse();
        assertThat(storage.exists(StorageRoot.PUBLIC, claim.sourceKey())).isTrue();
        assertThat(storage.exists(StorageRoot.PUBLIC, previous.getAudioFile())).isTrue();
        assertThat(storage.exists(StorageRoot.PUBLIC, previous.getStreamAudioFile())).isTrue();
        jobs.fail(claim, AudioTranscodeException.Code.AUDIO_STORAGE_FAILED);
        assertThat(track(id).canRetryAudio()).isTrue();
    }

    @Test
    void encoderPartialWriteFailureHasJournalOwnerAndRetainsOriginal() throws Exception {
        long id = createWav();
        FfmpegAudioEncoder failedEncoder = mock(FfmpegAudioEncoder.class);
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat(journal.findAll()).anyMatch(m -> m.getState() == StorageMutationState.PREPARED);
            Files.write(invocation.getArgument(1, Path.class), new byte[]{1, 2, 3});
            throw new AudioTranscodeException(AudioTranscodeException.Code.AUDIO_TRANSCODE_FAILED);
        }).when(failedEncoder).encode(any(), any(), any());
        worker(failedEncoder).processNext();
        assertThat(track(id).getAudioErrorCode()).isEqualTo("AUDIO_TRANSCODE_FAILED");
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
        assertThat(journal.findAll()).allMatch(m -> m.getState() == StorageMutationState.DONE);
    }

    @Test
    void invalidGeneratedBytesNeverCommitAndRemainRetryable() throws Exception {
        long id = createWav();
        FfmpegAudioEncoder bad = mock(FfmpegAudioEncoder.class);
        doAnswer(i -> { Files.write(i.getArgument(1, Path.class), new byte[]{0, 1, 2}); return null; })
                .when(bad).encode(any(), any(), any());
        worker(bad).processNext();
        assertThat(track(id).getAudioErrorCode()).isEqualTo("AUDIO_OUTPUT_INVALID");
        assertThat(track(id).canRetryAudio()).isTrue();
        assertThat(track(id).getStreamAudioFile()).isNull();
    }

    @Test
    void generatedDiskFailureRetainsPendingOriginalAndJournalRecovery() {
        long id = createWav();
        doThrow(new IllegalStateException("synthetic disk failure")).when(storage)
                .stageGenerated(any(), any(), any(), any());
        worker(fakeEncoder()).processNext();
        assertThat(track(id).getAudioErrorCode()).isEqualTo("AUDIO_STORAGE_FAILED");
        assertThat(track(id).canRetryAudio()).isTrue();
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
    }

    @Test
    void explicitDeactivationFencesReplacementAndNeverReactivatesOnCompletion() {
        long id = createWav();
        worker(fakeEncoder()).processNext();
        activate(id);
        Track old = track(id);
        replaceWav(id);
        var claim = jobs.claimNext();
        TrackUpdateRequest deactivate = new TrackUpdateRequest();
        deactivate.setIsActive(false);
        tracks.updateTrack(id, deactivate, null, null);
        try (var output = generated()) { assertThat(jobs.complete(claim, output, wavAnalysis())).isFalse(); }
        assertThat(track(id).isActive()).isFalse();
        assertThat(track(id).getAudioFile()).isEqualTo(old.getAudioFile());
        assertThat(track(id).getStreamAudioFile()).isEqualTo(old.getStreamAudioFile());
        assertThat(track(id).getAudioProcessingState()).isEqualTo(AudioProcessingState.CANCELLED);
    }

    @ParameterizedTest
    @CsvSource({"16000,1,16000,1", "22050,2,22050,2", "24000,1,24000,1",
            "32000,2,32000,2", "44100,1,44100,1", "48000,2,48000,2",
            "96000,2,48000,2", "44100,6,44100,2", "96000,6,48000,2", "8000,1,16000,1"})
    @EnabledIfEnvironmentVariable(named = "ATS_TEST_FFMPEG_PATH", matches = ".+")
    void realConfiguredEncoderRunsApplicationPipelineInTemporaryStorage(int rate, int channels,
            int expectedRate, int expectedChannels) throws Exception {
        originalBytes = SyntheticAudioFixtures.wav16(rate, channels, rate * 2);
        long id = createWav();
        byte[] before = MessageDigest.getInstance("SHA-256").digest(originalBytes);
        worker(new FfmpegAudioEncoder(System.getenv("ATS_TEST_FFMPEG_PATH"), 30)).processNext();
        assertReadyAndOriginal(id);
        Track ready = track(id);
        byte[] retained = storage.loadAsResource(StorageRoot.PUBLIC, ready.getAudioFile()).getContentAsByteArray();
        assertThat(MessageDigest.getInstance("SHA-256").digest(retained)).isEqualTo(before);
        var stream = storage.loadAsResource(StorageRoot.PUBLIC, ready.getStreamAudioFile());
        AudioAnalysisResult output = analysis.analyze("stream.mp3", stream);
        assertThat(output.sampleRateHz()).isEqualTo(expectedRate);
        assertThat(output.channelCount()).isEqualTo(expectedChannels);
        assertThat(output.decodedFrameCount() / (double) output.sampleRateHz())
                .isCloseTo(2.0, within(0.25));
        assertThat(javax.sound.sampled.AudioSystem.getAudioFileFormat(stream.getFile()).properties()
                .get("mp3.bitrate.nominal.bps")).isEqualTo(128000);
        assertThat(stream.contentLength()).isPositive().isLessThan(97536);
    }

    private long createWav() {
        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Full length"); request.setBpm(120); request.setTonality("C");
        return tracks.createTrack(request, wav(), null, principal).id();
    }
    private void replaceWav(long id) { tracks.updateTrack(id, new TrackUpdateRequest(), wav(), null); }
    private MockMultipartFile wav() { return new MockMultipartFile("audioFile", "source.wav", "audio/wav", originalBytes); }
    private AudioAnalysisResult wavAnalysis() { return analysis.analyze(wav()); }
    private Track track(long id) { return repository.findById(id).orElseThrow(); }
    private void activate(long id) {
        TrackUpdateRequest request = new TrackUpdateRequest(); request.setIsActive(true);
        tracks.updateTrack(id, request, null, null);
    }
    private void assertReadyAndOriginal(long id) {
        Track ready = track(id);
        assertThat(ready.getAudioProcessingState()).isEqualTo(AudioProcessingState.READY);
        assertThat(ready.isActive()).isFalse();
        assertThat(ready.isStreamReady()).isTrue();
        assertThat(ready.getStreamAudioFile()).isNotEqualTo(ready.getAudioFile());
        assertThat(ready.getPendingAudioFile()).isNull();
        assertThat(ready.getAudioClaimToken()).isNull();
        assertThat(integrity.inspect().missingReferenceCount()).isZero();
    }
    private TrackAudioProcessingWorker worker(FfmpegAudioEncoder encoder) {
        TrackAudioProcessingWorker worker = new TrackAudioProcessingWorker(jobs, storage, mutations, analysis, encoder);
        worker.start();
        return worker;
    }
    private FfmpegAudioEncoder fakeEncoder() {
        FfmpegAudioEncoder encoder = mock(FfmpegAudioEncoder.class);
        doAnswer(i -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            Files.write(i.getArgument(1, Path.class), SyntheticAudioFixtures.mp3Cbr(128, 77));
            return null;
        }).when(encoder).encode(any(), any(), any());
        return encoder;
    }
    private StorageMutationCoordinator.GeneratedWrite generated() {
        return mutations.generate(StorageDomain.TRACK, StorageRoot.PUBLIC, "tracks/audio", "stream.mp3", path -> {
            try { Files.write(path, SyntheticAudioFixtures.mp3Cbr(128, 77)); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        });
    }
    private TransactionTemplate tx() { return new TransactionTemplate(manager); }
    @TestConfiguration
    static class Config {
        @Bean LocalStorageService storageService() {
            return new LocalStorageService(root.resolve("public").toString(), root.resolve("private").toString());
        }
    }
}
