package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.audio.*;
import com.atstudio.atstudio.service.storage.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListeningPreparationTest extends ListeningDatabaseCatalogTest {
    @Autowired StorageMutationCoordinator mutations;
    @TempDir Path staging;
    AudioAnalysisService analyzer;
    StorageService stagingStorage;
    StorageMutationJournalService journal;
    StorageCleanupService cleanup;
    StorageMutationCoordinator actual;
    AudioAnalysisResult result = new AudioAnalysisResult(1, "[0]", AudioAnalysisFormat.MP3, 44100, 44100, 1);
    volatile CountDownLatch stageEntered;
    volatile CountDownLatch stageRelease;

    @BeforeEach void prepareStagingFixture() {
        TrackService target = AopTestUtils.getTargetObject(trackService);
        analyzer = (AudioAnalysisService) ReflectionTestUtils.getField(target, "audioAnalysisService");
        reset(analyzer, mutations);
        when(analyzer.analyze(any(org.springframework.web.multipart.MultipartFile.class))).thenAnswer(call -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse(); return result;
        });
        stagingStorage = mock(StorageService.class); journal = mock(StorageMutationJournalService.class);
        cleanup = mock(StorageCleanupService.class); actual = new StorageMutationCoordinator(stagingStorage, journal, cleanup);
        AtomicLong sequence = new AtomicLong();
        when(stagingStorage.generateKey(anyString(), any())).thenAnswer(call -> call.getArgument(0) + "/prepared-" + sequence.incrementAndGet() + ".mp3");
        when(journal.prepare(anyList())).thenAnswer(call -> List.of(sequence.incrementAndGet()));
        when(cleanup.cleanupNew(any(), anyString(), anyString())).thenReturn(StorageCleanupService.CleanupOutcome.DONE);
        when(cleanup.cleanupOld(any(), any(), anyString())).thenReturn(StorageCleanupService.CleanupOutcome.DONE);
        doAnswer(call -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            if (stageEntered != null) { stageEntered.countDown(); await(stageRelease); }
            Consumer<Path> generator = call.getArgument(3);
            generator.accept(staging.resolve("bytes-" + sequence.incrementAndGet()));
            return null;
        }).when(stagingStorage).stageGenerated(any(), anyString(), anyString(), any());
        when(mutations.generate(any(), any(), anyString(), any(), any())).thenAnswer(call ->
                actual.generate(call.getArgument(0), call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));
        doAnswer(call -> { actual.deleteAfterCommit(call.getArgument(0), call.getArgument(1), (String) call.getArgument(2)); return null; })
                .when(mutations).deleteAfterCommit(any(), any(), anyString());
    }

    MockMultipartFile upload() { return new MockMultipartFile("audioFile", "fixture.mp3", "audio/mpeg", new byte[]{1,2,3}); }
    TrackUpdateRequest edit() { var r = new TrackUpdateRequest(); r.setTitle("Prepared replacement"); return r; }
    long otherSelected() {
        long id = tx.execute(s -> tracks.saveAndFlush(Track.builder().title("Other approved").tonality("C")
                .bpm(100).audioFile("tracks/audio/other.mp3").isActive(true).user(users.findAll().get(0)).build()).getId());
        catalog.replace(update(0, List.of(id)), 1L); return id;
    }

    @Test void preparationRejectsOuterTransactionWhenGuardUnconfigured() {
        Object directory = ReflectionTestUtils.getField(guard, "directory");
        try {
            ReflectionTestUtils.setField(guard, "directory", null);
            assertThatThrownBy(() -> tx.executeWithoutResult(status -> trackService.updateTrack(trackId, edit(), upload(), null)))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("preparation must precede");
            verifyNoInteractions(analyzer);
        } finally { ReflectionTestUtils.setField(guard, "directory", directory); }
    }

    @Test void preparationRejectsAlreadyAdmittedOuterCatalogTransaction() {
        try (var admission = guard.enter()) {
            assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
                catalog.lockForWrite();
                trackService.updateTrack(trackId, edit(), upload(), null);
            })).isInstanceOf(IllegalStateException.class).hasMessageContaining("preparation must precede");
            verifyNoInteractions(analyzer);
        }
    }

    @Test void preparationPausedAnalysisDoesNotBlockUnrelatedStream() throws Exception {
        catalog.replace(update(0, List.of(trackId)), 1L);
        var entered = new CountDownLatch(1); var release = new CountDownLatch(1);
        when(analyzer.analyze(any(org.springframework.web.multipart.MultipartFile.class))).thenAnswer(call -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            entered.countDown(); await(release); return result;
        });
        var request = new TrackCreateRequest(); request.setTitle("New upload"); request.setBpm(100); request.setTonality("C");
        var actor = CustomUserDetails.from(users.findAll().get(0)); var pool = Executors.newFixedThreadPool(2);
        try {
            var create = pool.submit(() -> trackService.createTrack(request, upload(), null, actor));
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(pool.submit(() -> trackService.getStreamResource(trackId)).get(1, TimeUnit.SECONDS).publicLength()).isEqualTo(1);
            release.countDown(); assertThat(create.get(5, TimeUnit.SECONDS).isActive()).isFalse();
        } finally { release.countDown(); pool.shutdownNow(); }
    }

    @Test void preparationPausedStagingAllowsReadAndConcurrentPublicationRejectsFinalAttach() throws Exception {
        long other = otherSelected(); stageEntered = new CountDownLatch(1); stageRelease = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var update = pool.submit(() -> trackService.updateTrack(trackId, edit(), upload(), null));
            assertThat(stageEntered.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(pool.submit(() -> trackService.getStreamResource(other)).get(1, TimeUnit.SECONDS).publicLength()).isEqualTo(1);
            catalog.replace(update(1, List.of(trackId, other)), 1L);
            stageRelease.countDown();
            assertThatThrownBy(() -> update.get(5, TimeUnit.SECONDS)).hasCauseInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            assertThat(tracks.findById(trackId).orElseThrow().getAudioFile()).isEqualTo("tracks/audio/old.mp3");
            verify(cleanup).cleanupNew(any(), anyString(), anyString());
            verify(cleanup, never()).cleanupOld(any(), any(), anyString());
            verify(journal).releaseInFlight(anyString());
        } finally { stageRelease.countDown(); pool.shutdownNow(); }
    }

    @Test void preparationFreezeWaitsThroughFailedCleanupAndPreservesRetryEvidence() throws Exception {
        long other = otherSelected(); stageEntered = new CountDownLatch(1); stageRelease = new CountDownLatch(1);
        var cleanupEntered = new CountDownLatch(1); var cleanupRelease = new CountDownLatch(1);
        when(cleanup.cleanupNew(any(), anyString(), anyString())).thenAnswer(call -> {
            cleanupEntered.countDown(); await(cleanupRelease); return StorageCleanupService.CleanupOutcome.FAILED;
        });
        var pool = Executors.newFixedThreadPool(2); String capture = null;
        try {
            var update = pool.submit(() -> trackService.updateTrack(trackId, edit(), upload(), null));
            assertThat(stageEntered.await(5, TimeUnit.SECONDS)).isTrue();
            catalog.replace(update(1, List.of(trackId, other)), 1L);
            var freeze = pool.submit(() -> guard.freeze(java.time.Duration.ofSeconds(8)));
            stageRelease.countDown(); assertThat(cleanupEntered.await(5, TimeUnit.SECONDS)).isTrue();
            assertThat(freeze.isDone()).isFalse(); cleanupRelease.countDown();
            assertThatThrownBy(() -> update.get(5, TimeUnit.SECONDS)).hasCauseInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            capture = freeze.get(5, TimeUnit.SECONDS);
            verify(journal).recordCleanupFailure(anyLong(), eq("NEW_DELETE_FAILED"), any(), anyInt());
            verify(journal, never()).transition(anyLong(), eq(StorageMutationState.DONE), eq("NEW_DELETE_DONE"));
            assertThatThrownBy(() -> trackService.updateTrack(trackId, edit(), upload(), null)).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            assertThat(trackService.getStreamResource(other).publicLength()).isEqualTo(1);
        } finally {
            stageRelease.countDown(); cleanupRelease.countDown(); pool.shutdownNow();
            if (capture != null) guard.releaseReconciled(capture);
        }
    }

    @Test void preparationFinalRollbackCleansNewBytesWithoutDeletingOldKey() {
        var request = edit(); request.setReplaceTags(true); request.setTagIds(List.of(999999L));
        assertThatThrownBy(() -> trackService.updateTrack(trackId, request, upload(), null))
                .isInstanceOf(com.atstudio.atstudio.common.exception.BusinessException.class);
        assertThat(tracks.findById(trackId).orElseThrow().getAudioFile()).isEqualTo("tracks/audio/old.mp3");
        verify(cleanup).cleanupNew(any(), anyString(), anyString());
        verify(cleanup, never()).cleanupOld(any(), any(), anyString());
    }
}
