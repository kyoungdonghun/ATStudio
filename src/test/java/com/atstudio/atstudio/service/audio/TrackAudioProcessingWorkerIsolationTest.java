package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.dto.track.TrackCreateRequest;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.AudioProcessingState;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.TrackService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageCleanupService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageMutationDraft;
import com.atstudio.atstudio.service.storage.StorageMutationJournalService;
import com.atstudio.atstudio.service.storage.StorageMutationState;
import com.atstudio.atstudio.service.storage.StorageMutationType;
import com.atstudio.atstudio.service.storage.StorageReferenceChecker;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrackAudioProcessingWorkerIsolationTest {
    private final TrackAudioProcessingTransactions transactions = mock(TrackAudioProcessingTransactions.class);
    private final StorageService storage = mock(StorageService.class);
    private final StorageMutationCoordinator mutations = mock(StorageMutationCoordinator.class);
    private final AudioAnalysisService analysis = mock(AudioAnalysisService.class);
    private final FfmpegAudioEncoder encoder = mock(FfmpegAudioEncoder.class);
    private final TrackAudioProcessingWorker worker = new TrackAudioProcessingWorker(
            transactions, storage, mutations, analysis, encoder);

    @AfterEach
    void stopWorker() {
        worker.stop();
    }

    @Test
    void startupDrainsGlobalInterruptedBatchesBeforeTheFirstClaim() {
        when(transactions.recoverInterruptedBatch()).thenReturn(50, 1, 0);

        worker.processNext();
        verifyNoInteractions(transactions);
        worker.start();
        worker.start();
        worker.processNext();

        var order = inOrder(transactions);
        order.verify(transactions, times(3)).recoverInterruptedBatch();
        order.verify(transactions).claimNext();
        order.verifyNoMoreInteractions();
        verify(storage, atLeastOnce()).enterMutation();
        verifyNoMoreInteractions(storage);
        verifyNoInteractions(mutations, analysis, encoder);
    }

    @Test
    void readyEventRetainsTheDedicatedGlobalQueuePoller() {
        worker.onApplicationReady();

        verify(transactions).recoverInterruptedBatch();
        verify(transactions, timeout(2000)).claimNext();
        verify(storage, atLeastOnce()).enterMutation();
        verifyNoMoreInteractions(storage);
        verifyNoInteractions(mutations, analysis, encoder);
    }

    @Test
    void twoFreshWavSuccessPathsCreateEightJournalsWithoutDeletingOriginals() {
        TrackRepository tracks = mock(TrackRepository.class);
        UserRepository users = mock(UserRepository.class);
        StorageMutationJournalService journal = mock(StorageMutationJournalService.class);
        StorageReferenceChecker references = mock(StorageReferenceChecker.class);
        StorageCleanupService cleanup = new StorageCleanupService(storage, references);
        StorageMutationCoordinator coordinator = new StorageMutationCoordinator(storage, journal, cleanup);
        TrackAudioProcessingTransactions audioTransactions = new TrackAudioProcessingTransactions(tracks, coordinator);
        Map<Long, Track> rows = new LinkedHashMap<>();
        Map<Long, StorageMutationDraft> drafts = new LinkedHashMap<>();
        AudioAnalysisResult decoded = new AudioAnalysisResult(10, "[0.0]", AudioAnalysisFormat.WAV,
                441000, 44100, 2);
        User creator = User.builder().nickname("synthetic-creator").build();
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getId()).thenReturn(1L);
        when(users.findById(1L)).thenReturn(Optional.of(creator));
        when(analysis.analyze(any(MultipartFile.class))).thenReturn(decoded);
        when(storage.generateKey("tracks/audio", "fixture.wav"))
                .thenReturn("tracks/audio/original-1.wav", "tracks/audio/original-2.wav");
        when(storage.generateKey("tracks/audio", "stream.mp3"))
                .thenReturn("tracks/audio/stream-1.mp3", "tracks/audio/stream-2.mp3");
        when(journal.prepare(anyList())).thenAnswer(invocation -> {
            List<StorageMutationDraft> batch = invocation.getArgument(0);
            return batch.stream().map(draft -> {
                long id = drafts.size() + 1L;
                drafts.put(id, draft);
                return id;
            }).toList();
        });
        when(tracks.save(any(Track.class))).thenAnswer(invocation -> {
            Track track = invocation.getArgument(0);
            long id = rows.size() + 1L;
            ReflectionTestUtils.setField(track, "id", id);
            rows.put(id, track);
            return track;
        });
        when(tracks.findByIdForUpdate(anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(rows.get(invocation.getArgument(0, Long.class))));
        when(tracks.findPendingAudioIds(any())).thenAnswer(invocation -> rows.values().stream()
                .filter(track -> track.getAudioProcessingState() == AudioProcessingState.PENDING)
                .map(Track::getId).toList());
        when(references.isReferenced(eq(StorageDomain.TRACK), anyString()))
                .thenAnswer(invocation -> rows.values().stream()
                        .anyMatch(track -> track.getAudioFile().equals(invocation.getArgument(1))));
        TrackService trackService = new TrackService(tracks, mock(TrackTagRepository.class),
                mock(TagRepository.class), users, analysis, mock(CanonicalImageService.class), storage,
                coordinator, mock(LikeRepository.class), mock(TrackDownloadRepository.class),
                mock(LicenseRepository.class), mock(PlaylistTrackRepository.class), mock(AlbumTrackRepository.class));

        for (int index = 1; index <= 2; index++) {
            TrackCreateRequest request = new TrackCreateRequest();
            request.setTitle("Synthetic journal proof " + index);
            request.setBpm(120);
            request.setTonality("C");
            MockMultipartFile upload = new MockMultipartFile("audioFile", "fixture.wav", "audio/wav",
                    new byte[]{1});
            inTransaction(() -> trackService.createTrack(request, upload, null, principal));
            TrackAudioProcessingTransactions.Claim claim = inTransaction(audioTransactions::claimNext);
            assertThat(claim).isNotNull();
            try (var generated = coordinator.generate(StorageDomain.TRACK, StorageRoot.PUBLIC,
                    "tracks/audio", "stream.mp3", output -> {
                        throw new AssertionError("Native generation is excluded from this journal proof");
                    })) {
                assertThat(inTransaction(() -> audioTransactions.complete(claim, generated, decoded))).isTrue();
            }
        }

        assertThat(drafts).hasSize(8);
        assertThat(drafts.values()).filteredOn(draft -> draft.mutationType() == StorageMutationType.CREATE)
                .hasSize(4);
        assertThat(drafts.values()).filteredOn(draft -> draft.mutationType() == StorageMutationType.DELETE)
                .extracting(StorageMutationDraft::oldKey).containsExactly(
                        "tracks/audio/original-1.wav", "tracks/audio/original-1.wav",
                        "tracks/audio/original-2.wav", "tracks/audio/original-2.wav");
        drafts.forEach((id, draft) -> verify(journal).transition(id, StorageMutationState.DONE,
                draft.mutationType() == StorageMutationType.CREATE ? "CREATE_COMMITTED" : "SHARED_REFERENCE_RETAINED"));
        assertThat(rows.values()).allSatisfy(track -> {
            assertThat(track.getAudioProcessingState()).isEqualTo(AudioProcessingState.READY);
            assertThat(track.getAudioFile()).isEqualTo("tracks/audio/original-" + track.getId() + ".wav");
            assertThat(track.getStreamAudioFile()).isEqualTo("tracks/audio/stream-" + track.getId() + ".mp3");
            assertThat(track.getAudioClaimToken()).isNull();
            assertThat(track.isActive()).isFalse();
        });
        verify(storage, never()).delete(any(), anyString());
        verify(storage, never()).deleteStaged(any(), anyString(), anyString());
        verify(journal, never()).claimBatch(any(), any(), any(), anyInt(), anyInt());
        verifyNoInteractions(encoder, transactions, mutations);
    }

    private static <T> T inTransaction(Supplier<T> action) {
        // Exercise coordinator callbacks without a database or a Spring transaction manager.
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            T result = action.get();
            List<TransactionSynchronization> callbacks = TransactionSynchronizationManager.getSynchronizations();
            callbacks.forEach(TransactionSynchronization::afterCommit);
            callbacks.forEach(callback -> callback.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
            return result;
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
