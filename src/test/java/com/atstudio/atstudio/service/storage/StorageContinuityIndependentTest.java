package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.repository.StorageMutationRepository;
import com.atstudio.atstudio.service.audio.AudioTranscodeException;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Independent failure-oriented checks for WI-20261029-ATS-005; synthetic local state only. */
class StorageContinuityIndependentTest {
    @TempDir Path temporary;

    private StorageContinuityGuard open() {
        return new StorageContinuityGuard(temporary.resolve("state").toString());
    }

    @Test
    void frozenCaptureSurvivesReopenAndCannotBeReleasedByAnotherId() throws Exception {
        String capture;
        try (var guard = open()) { capture = guard.freeze(Duration.ZERO); }
        try (var reopened = open()) {
            assertThat(reopened.retainedCaptureId()).isEqualTo(capture);
            assertThatThrownBy(reopened::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            assertThatThrownBy(() -> reopened.releaseReconciled("another-capture")).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(reopened::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
            reopened.releaseReconciled(capture);
            try (var ignored = reopened.enter()) { assertThat(reopened.retainedCaptureId()).isEqualTo("-"); }
        }
    }

    @Test
    void pinnedReferencesRemainRootSpecificAcrossReopen() throws Exception {
        var publicFile = new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/original.wav");
        var privateFile = new StorageContinuityGuard.FileRef(StorageRoot.PRIVATE, "tracks/original.wav");
        String capture;
        try (var guard = open()) {
            capture = guard.freeze(Duration.ZERO);
            guard.seal(capture, Set.of(publicFile));
        }
        AtomicInteger mutations = new AtomicInteger();
        try (var reopened = open()) {
            assertThat(reopened.mutate(publicFile, -1, mutations::incrementAndGet)).isEqualTo(-1);
            assertThat(mutations).hasValue(0);
            assertThat(reopened.mutate(privateFile, -1, mutations::incrementAndGet)).isEqualTo(1);
            reopened.releaseReconciled(capture);
            assertThat(reopened.mutate(publicFile, -1, mutations::incrementAndGet)).isEqualTo(2);
        }
    }

    @Test
    void secondInstanceCannotOwnExistingStateConcurrently() {
        try (var first = open()) {
            assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class);
            try (var ignored = first.enter()) { assertThat(first.retainedCaptureId()).isEqualTo("-"); }
        }
        try (var reopened = open()) { assertThat(reopened.retainedCaptureId()).isEqualTo("-"); }
    }

    @Test
    void missingPreviouslyInitializedStateIsNotRecreatedAsEmpty() throws Exception {
        try (var ignored = open()) { }
        Files.delete(temporary.resolve("state/capture.state"));
        assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class);
        assertThat(temporary.resolve("state/capture.state")).doesNotExist();
    }

    @Test
    void corruptStateRejectsMutationAndRemainsPoisonedAfterExternalRestoration() throws Exception {
        try (var guard = open()) {
            Path state = temporary.resolve("state/capture.state");
            byte[] before = Files.readAllBytes(state);
            Files.writeString(state, "truncated");
            AtomicInteger mutations = new AtomicInteger();
            var file = new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/a.wav");
            assertThatThrownBy(() -> guard.mutate(file, -1, mutations::incrementAndGet)).isInstanceOf(IllegalStateException.class);
            Files.write(state, before);
            assertThatThrownBy(guard::enter).isInstanceOf(IllegalStateException.class);
            assertThat(mutations).hasValue(0);
        }
    }

    @Test
    void truncatedStateFailsStartupInsteadOfDroppingRetainedCapture() throws Exception {
        try (var guard = open()) { guard.freeze(Duration.ZERO); }
        Files.writeString(temporary.resolve("state/capture.state"), "ATS-CONTINUITY-1\n");
        assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void validLookingPinLineRemovalStillFailsStartupIntegrity() throws Exception {
        try (var guard = open()) {
            String capture = guard.freeze(Duration.ZERO);
            guard.seal(capture, Set.of(new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/retain.wav")));
        }
        Path state = temporary.resolve("state/capture.state");
        String original = Files.readString(state);
        String edited = original.replaceAll("(?m)^PUBLIC:[^\\r\\n]*\\r?\\n", "");
        assertThat(edited).isNotEqualTo(original);
        Files.writeString(state, edited);
        assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freezeDeadlineIncludesWaitingForAnAlreadyRunningFileAction() throws Exception {
        try (var guard = open()) {
            var executor = Executors.newFixedThreadPool(2);
            var actionEntered = new CountDownLatch(1);
            var finish = new CountDownLatch(1);
            try {
                var mutation = executor.submit(() -> guard.mutate(
                        new StorageContinuityGuard.FileRef(StorageRoot.PUBLIC, "tracks/active.wav"), false, () -> {
                            actionEntered.countDown();
                            try {
                                if (!finish.await(5, TimeUnit.SECONDS)) throw new AssertionError("action release timeout");
                            } catch (InterruptedException interrupted) { throw new AssertionError(interrupted); }
                            return true;
                        }));
                assertThat(actionEntered.await(5, TimeUnit.SECONDS)).isTrue();
                var freezer = executor.submit(() -> {
                    assertThatThrownBy(() -> guard.freeze(Duration.ofMillis(50)))
                            .isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
                    return null;
                });
                freezer.get(2, TimeUnit.SECONDS);
                assertThat(mutation).isNotDone();
                finish.countDown();
                assertThat(mutation.get(5, TimeUnit.SECONDS)).isTrue();
            } finally { finish.countDown(); executor.shutdownNow(); }
        }
    }

    @Test
    void timedOutDrainDoesNotConsumeAnActiveLeaseOrPreventLaterCapture() throws Exception {
        try (var guard = open()) {
            var executor = Executors.newSingleThreadExecutor();
            var admitted = new CountDownLatch(1);
            var finish = new CountDownLatch(1);
            try {
                var worker = executor.submit(() -> {
                    try (var ignored = guard.enter()) {
                        admitted.countDown();
                        if (!finish.await(5, TimeUnit.SECONDS)) throw new AssertionError("worker release timeout");
                    }
                    return null;
                });
                assertThat(admitted.await(5, TimeUnit.SECONDS)).isTrue();
                assertThatThrownBy(() -> guard.freeze(Duration.ZERO)).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
                assertThat(guard.retainedCaptureId()).isEqualTo("-");
                assertThatThrownBy(guard::close).isInstanceOf(IllegalStateException.class);
                finish.countDown();
                worker.get(5, TimeUnit.SECONDS);
                String capture = guard.freeze(Duration.ZERO);
                assertThatThrownBy(() -> guard.freeze(Duration.ZERO)).isInstanceOf(IllegalStateException.class);
                assertThat(guard.retainedCaptureId()).isEqualTo(capture);
                guard.releaseReconciled(capture);
            } finally { finish.countDown(); executor.shutdownNow(); }
        }
    }

    @Test
    void duplicateLeaseCloseCannotReleaseAnotherActiveLease() throws Exception {
        try (var guard = open()) {
            var first = guard.enter();
            var second = guard.enter();
            first.close();
            first.close();
            assertThatThrownBy(guard::close).isInstanceOf(IllegalStateException.class);
            second.close();
            String capture = guard.freeze(Duration.ZERO);
            guard.releaseReconciled(capture);
        }
    }

    @Test
    void wrongThreadCannotReleaseTheOwningThreadsLease() throws Exception {
        try (var guard = open()) {
            var lease = guard.enter();
            var executor = Executors.newSingleThreadExecutor();
            try {
                executor.submit(() -> assertThatThrownBy(lease::close).isInstanceOf(IllegalStateException.class))
                        .get(5, TimeUnit.SECONDS);
                assertThatThrownBy(guard::close).isInstanceOf(IllegalStateException.class);
            } finally { lease.close(); executor.shutdownNow(); }
        }
    }

    @Test
    void uncertainGeneratedWriterKeepsItsStagingBytesBeforeCleanup() throws Exception {
        try (var guard = open()) {
            var storage = new LocalStorageService(temporary.resolve("public").toString(), temporary.resolve("private").toString());
            storage.setContinuity(guard);
            storage.init();
            var journal = spy(new StorageMutationJournalService(mock(StorageMutationRepository.class)));
            journal.setContinuity(guard);
            doReturn(List.of(1L)).when(journal).prepare(anyList());
            var coordinator = new StorageMutationCoordinator(storage, journal,
                    new StorageCleanupService(storage, mock(StorageReferenceChecker.class)));
            var generatedPath = new AtomicReference<Path>();
            assertThatThrownBy(() -> coordinator.generate(StorageDomain.TRACK, StorageRoot.PUBLIC,
                    "tracks", "partial.mp3", path -> {
                        generatedPath.set(path);
                        try { Files.write(path, new byte[]{4, 5}); }
                        catch (Exception exception) { throw new AssertionError(exception); }
                        throw new AudioTranscodeException(AudioTranscodeException.Code.AUDIO_PROCESS_TERMINATION_FAILED);
                    })).isInstanceOf(AudioTranscodeException.class);
            assertThat(generatedPath.get()).exists();
            assertThat(Files.readAllBytes(generatedPath.get())).containsExactly((byte) 4, (byte) 5);
            assertThatThrownBy(() -> guard.freeze(Duration.ZERO)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void preservationDeferralsDoNotExhaustCleanupAttemptsOrLosePendingWork() {
        var mutation = StorageMutation.prepared(UUID.randomUUID().toString(), StorageDomain.TRACK,
                StorageMutationType.DELETE, StorageRoot.PUBLIC, null, "tracks/old.wav");
        mutation.transition(StorageMutationState.AFTER_COMMIT_DELETE, "OLD_DELETE_PENDING");
        mutation.deferPreserved(LocalDateTime.now().plusSeconds(30), false);
        for (int count = 0; count < 20; count++) {
            mutation.claim(LocalDateTime.now().plusMinutes(1));
            mutation.deferPreserved(LocalDateTime.now().plusSeconds(30), true);
        }
        assertThat(mutation.getAttemptCount()).isZero();
        assertThat(mutation.getState()).isEqualTo(StorageMutationState.RETRY);
        assertThat(mutation.getReasonCode()).isEqualTo("BACKUP_PRESERVED");
        assertThat(mutation.getNextAttemptAt()).isNotNull();
    }

    @Test
    void actualChildJvmCrashRetainsFrozenCaptureAndReleasesOnlyProcessLock() throws Exception {
        Path ready = temporary.resolve("child-ready.txt");
        String classpath = Path.of(StorageContinuityGuard.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(StorageContinuityIndependentTest.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Process child = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", classpath, CrashCaptureProcess.class.getName(), temporary.resolve("state").toString(), ready.toString())
                .redirectErrorStream(true).redirectOutput(temporary.resolve("child.log").toFile()).start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!Files.exists(ready) && child.isAlive() && System.nanoTime() < deadline) Thread.sleep(10);
            assertThat(ready).exists();
            String capture = Files.readString(ready);
            assertThatThrownBy(this::open).isInstanceOf(IllegalStateException.class);
            var descendants = child.descendants().toList();
            child.destroyForcibly();
            assertThat(child.waitFor(5, TimeUnit.SECONDS)).isTrue();
            assertThat(descendants).noneMatch(ProcessHandle::isAlive);
            // Distinguish native process-lock release from persisted-state validation failures.
            try (var channel = java.nio.channels.FileChannel.open(temporary.resolve("state/owner.lock"),
                    java.nio.file.StandardOpenOption.WRITE)) {
                long unlockStarted = System.nanoTime();
                long unlockDeadline = unlockStarted + TimeUnit.SECONDS.toNanos(2);
                java.nio.channels.FileLock lock = null;
                while (lock == null && System.nanoTime() < unlockDeadline) {
                    lock = channel.tryLock();
                    if (lock == null) Thread.sleep(10);
                }
                assertThat(lock).as("terminated child must release its OS lock within two seconds").isNotNull();
                System.out.println("Observed child OS lock release after waitFor: "
                        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - unlockStarted) + " ms");
                lock.release();
            }
            String state = Files.readString(temporary.resolve("state/capture.state"));
            int digestAt = state.lastIndexOf("SHA256:");
            assertThat(digestAt).isPositive();
            String expectedDigest = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(state.substring(0, digestAt).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            assertThat(state.substring(digestAt)).isEqualTo("SHA256:" + expectedDigest + "\nEND\n");
            try (var reopened = open()) {
                assertThat(reopened.retainedCaptureId()).isEqualTo(capture);
                assertThatThrownBy(reopened::enter).isInstanceOf(StorageContinuityGuard.AdmissionClosedException.class);
                reopened.releaseReconciled(capture);
            }
        } finally {
            if (child.isAlive()) { child.destroyForcibly(); child.waitFor(5, TimeUnit.SECONDS); }
        }
    }

    public static final class CrashCaptureProcess {
        public static void main(String[] args) throws Exception {
            try (var guard = new StorageContinuityGuard(args[0])) {
                Files.writeString(Path.of(args[1]), guard.freeze(Duration.ZERO));
                Thread.sleep(30_000);
            }
        }
    }
}
