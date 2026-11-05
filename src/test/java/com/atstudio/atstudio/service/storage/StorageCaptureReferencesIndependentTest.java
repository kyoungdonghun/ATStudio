package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Independent WI009 reference-inventory checks using synthetic files, never a database. */
class StorageCaptureReferencesIndependentTest {
    @TempDir Path temporary;
    private final TrackRepository tracks = mock(TrackRepository.class);
    private final AlbumRepository albums = mock(AlbumRepository.class);
    private final PlaylistRepository playlists = mock(PlaylistRepository.class);
    private final CompanyCertificationDocumentRepository documents = mock(CompanyCertificationDocumentRepository.class);
    private final NoticeAttachmentRepository notices = mock(NoticeAttachmentRepository.class);
    private final QuestionAttachmentRepository questions = mock(QuestionAttachmentRepository.class);
    private final StorageMutationRepository mutations = mock(StorageMutationRepository.class);

    private StorageCaptureReferences source(StorageContinuityGuard guard, LocalStorageService storage) {
        when(tracks.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(albums.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(playlists.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(documents.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(notices.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(questions.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(mutations.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        return new StorageCaptureReferences(guard, storage, tracks, albums, playlists,
                documents, notices, questions, mutations);
    }

    private LocalStorageService storage(StorageContinuityGuard guard) {
        var storage = new LocalStorageService(temporary.resolve("public").toString(), temporary.resolve("private").toString());
        storage.setContinuity(guard);
        storage.init();
        return storage;
    }

    private void put(LocalStorageService storage, StorageRoot root, String key) {
        String operation = UUID.randomUUID().toString();
        storage.stage(root, operation, key, new MockMultipartFile("file", new byte[]{1, 2, 3}));
        storage.promote(root, operation, key);
    }

    @Test
    void certificationDirectoryIsNotMisclassifiedAsARequiredFile() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var source = source(guard, storage);
            put(storage, StorageRoot.PRIVATE, "certification/42/document.pdf");
            var certification = CompanyCertification.builder().documentPath("certification/42").build();
            when(documents.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                    CompanyCertificationDocument.builder().certification(certification)
                            .storedPath("certification/42/document.pdf").sizeBytes(3L).build())));
            guard.freeze(Duration.ZERO);
            assertThat(source.snapshot()).containsExactly(new CaptureReferenceSource.Reference(
                    "PRIVATE", "certification/42/document.pdf", true, true));
        }
    }

    @Test
    void inactiveTrackStillPreservesOriginalStreamPendingAndClaimedInput() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var source = source(guard, storage);
            var keys = List.of("tracks/original.wav", "tracks/stream.mp3", "tracks/pending.wav", "tracks/claimed.wav");
            keys.forEach(key -> put(storage, StorageRoot.PUBLIC, key));
            when(tracks.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(Track.builder()
                    .isActive(false).audioFile(keys.get(0)).streamAudioFile(keys.get(1))
                    .pendingAudioFile(keys.get(2)).claimedAudioFile(keys.get(3)).build())));
            guard.freeze(Duration.ZERO);
            assertThat(source.snapshot()).hasSize(4).allMatch(ref -> ref.required() && ref.present());
            assertThat(source.snapshot()).extracting(CaptureReferenceSource.Reference::key).containsExactlyInAnyOrderElementsOf(keys);
        }
    }

    @Test
    void committedMissingFileFailsInsteadOfBecomingAnOptionalRecoveryMember() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var source = source(guard, storage);
            when(tracks.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                    Track.builder().audioFile("tracks/missing.wav").build())));
            guard.freeze(Duration.ZERO);
            assertThatThrownBy(source::snapshot).isInstanceOf(IllegalStateException.class);
            assertThat(guard.retainedMode()).isEqualTo("FROZEN");
        }
    }

    @Test
    void unfinishedJournalClassifiesPresentStagingAndAbsentFinalWithoutInventingBytes() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var source = source(guard, storage);
            String operation = UUID.randomUUID().toString();
            storage.stage(StorageRoot.PUBLIC, operation, "tracks/new.wav", new MockMultipartFile("file", new byte[]{4}));
            when(mutations.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(StorageMutation.prepared(
                    operation, StorageDomain.TRACK, StorageMutationType.CREATE, StorageRoot.PUBLIC, "tracks/new.wav", null))));
            guard.freeze(Duration.ZERO);
            assertThat(source.snapshot()).containsExactlyInAnyOrder(
                    new CaptureReferenceSource.Reference("PUBLIC", "tracks/new.wav", false, false),
                    new CaptureReferenceSource.Reference("PUBLIC", ".staging/" + operation + "/tracks/new.wav", false, true));
        }
    }

    @Test
    void referenceReadWithoutFrozenBoundaryIsRejectedBeforeRepositoriesAreRead() {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var source = source(guard, storage);
            clearInvocations(tracks, albums, playlists, documents, notices, questions, mutations);
            assertThatThrownBy(source::snapshot).isInstanceOf(IllegalStateException.class);
            verifyNoInteractions(tracks, albums, playlists, documents, notices, questions, mutations);
        }
    }
}
