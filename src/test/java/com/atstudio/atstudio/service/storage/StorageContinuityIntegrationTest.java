package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.repository.StorageMutationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StorageContinuityIntegrationTest {
    @TempDir Path temporary;

    private LocalStorageService storage(StorageContinuityGuard guard) {
        var storage = new LocalStorageService(temporary.resolve("public").toString(), temporary.resolve("private").toString());
        storage.setContinuity(guard);
        storage.init();
        return storage;
    }

    @Test
    void pinnedFinalAndStagedFilesRemainUntilExplicitRelease() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            String operation = UUID.randomUUID().toString();
            storage.stage(StorageRoot.PUBLIC, operation, "tracks/a.wav", new MockMultipartFile("file", new byte[]{1, 2}));
            storage.promote(StorageRoot.PUBLIC, operation, "tracks/a.wav");
            storage.stage(StorageRoot.PRIVATE, operation, "attachments/b.txt", new MockMultipartFile("file", new byte[]{3}));
            String capture = guard.freeze(Duration.ZERO);
            guard.seal(capture, Set.of(new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/a.wav"),
                    new StorageContinuityGuard.FileRef(StorageRoot.PRIVATE, ".staging/" + operation + "/attachments/b.txt")));
            assertThat(storage.delete(StorageRoot.PUBLIC, "tracks/a.wav")).isEqualTo(StorageDeleteResult.DEFERRED);
            assertThat(storage.deleteStaged(StorageRoot.PRIVATE, operation, "attachments/b.txt")).isEqualTo(StorageDeleteResult.DEFERRED);
            assertThatThrownBy(() -> storage.promote(StorageRoot.PRIVATE, operation, "attachments/b.txt")).isInstanceOf(RuntimeException.class);
            assertThat(storage.exists(StorageRoot.PUBLIC, "tracks/a.wav")).isTrue();
            guard.releaseReconciled(capture);
            assertThat(storage.delete(StorageRoot.PUBLIC, "tracks/a.wav")).isEqualTo(StorageDeleteResult.DELETED);
            assertThat(storage.deleteStaged(StorageRoot.PRIVATE, operation, "attachments/b.txt")).isEqualTo(StorageDeleteResult.DELETED);
        }
    }

    @Test
    void configuredRootsCannotSilentlyRestartWithoutGuardOrWithAnotherStateDirectory() {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) { storage(guard); }
        try (var disabled = StorageContinuityGuard.disabled()) {
            assertThatThrownBy(() -> storage(disabled)).isInstanceOf(IllegalStateException.class);
        }
        try (var changed = new StorageContinuityGuard(temporary.resolve("other-state").toString())) {
            assertThatThrownBy(() -> storage(changed)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void stateInsidePublicStorageIsRejected() {
        try (var guard = new StorageContinuityGuard(temporary.resolve("public/state").toString())) {
            assertThatThrownBy(() -> storage(guard)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void promotionDoesNotOverwriteExistingFinalBytes() throws Exception {
        try (var guard = StorageContinuityGuard.disabled()) {
            var storage = storage(guard);
            String first = UUID.randomUUID().toString(), second = UUID.randomUUID().toString();
            storage.stage(StorageRoot.PUBLIC, first, "tracks/a.wav", new MockMultipartFile("file", new byte[]{1}));
            storage.promote(StorageRoot.PUBLIC, first, "tracks/a.wav");
            storage.stage(StorageRoot.PUBLIC, second, "tracks/a.wav", new MockMultipartFile("file", new byte[]{2}));
            assertThatThrownBy(() -> storage.promote(StorageRoot.PUBLIC, second, "tracks/a.wav")).isInstanceOf(RuntimeException.class);
            assertThat(Files.readAllBytes(temporary.resolve("public/tracks/a.wav"))).containsExactly((byte) 1);
        }
    }

    @Test
    void coordinatorLeaseLivesThroughRollbackCleanupAndTransactionCompletion() throws Exception {
        try (var guard = new StorageContinuityGuard(temporary.resolve("state").toString())) {
            var storage = storage(guard);
            var journal = spy(new StorageMutationJournalService(mock(StorageMutationRepository.class)));
            journal.setContinuity(guard);
            doReturn(List.of(1L)).when(journal).prepare(anyList());
            var coordinator = new StorageMutationCoordinator(storage, journal,
                    new StorageCleanupService(storage, mock(StorageReferenceChecker.class)));
            var executor = Executors.newSingleThreadExecutor();
            TransactionSynchronizationManager.initSynchronization();
            try {
                String key = coordinator.store(StorageDomain.TRACK, StorageRoot.PUBLIC,
                        new MockMultipartFile("file", "a.wav", "audio/wav", new byte[]{1}), "tracks");
                var failed = executor.submit(() -> guard.freeze(Duration.ZERO));
                assertThatThrownBy(() -> failed.get(2, TimeUnit.SECONDS)).hasCauseInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
                TransactionSynchronization callback = TransactionSynchronizationManager.getSynchronizations().get(0);
                callback.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
                assertThat(storage.exists(StorageRoot.PUBLIC, key)).isFalse();
                String capture = executor.submit(() -> guard.freeze(Duration.ofSeconds(1))).get(2, TimeUnit.SECONDS);
                guard.releaseReconciled(capture);
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
                executor.shutdownNow();
            }
        }
    }

    @Test
    void validLookingTruncationOrByteChangeCannotDropPersistedPins() throws Exception {
        Path state = temporary.resolve("state");
        try (var guard = new StorageContinuityGuard(state.toString())) {
            String capture = guard.freeze(Duration.ZERO);
            guard.seal(capture, Set.of(new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/a.wav")));
        }
        Path manifest = state.resolve("capture.state");
        String contents = Files.readString(manifest);
        Files.writeString(manifest, contents.replace("PUBLIC:", "PRIVAT:"));
        assertThatThrownBy(() -> new StorageContinuityGuard(state.toString())).isInstanceOf(IllegalStateException.class);
    }
}
