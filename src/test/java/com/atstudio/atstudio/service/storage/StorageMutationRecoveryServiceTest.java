package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageMutationRecoveryServiceTest {

    @Mock StorageMutationJournalService journalService;
    @Mock StorageCleanupService cleanupService;
    @Mock StorageReferenceChecker referenceChecker;

    @InjectMocks StorageMutationRecoveryService recoveryService;

    @BeforeEach
    void configureRecovery() {
        ReflectionTestUtils.setField(recoveryService, "batchSize", 25);
        ReflectionTestUtils.setField(recoveryService, "maxAttempts", 8);
        ReflectionTestUtils.setField(recoveryService, "staleSeconds", 300L);
        ReflectionTestUtils.setField(recoveryService, "claimSeconds", 120L);
    }

    @Test
    void staleCreateWithoutReferenceDeletesNewFileIdempotently() {
        StorageMutationSnapshot snapshot = snapshot(
                1L,
                StorageMutationType.CREATE,
                "notices/attachments/new.pdf",
                null,
                1);
        given(journalService.claimBatch(any(), any(), any(), eq(25), eq(8)))
                .willReturn(List.of(snapshot));
        given(referenceChecker.isReferenced(StorageDomain.NOTICE, snapshot.newKey()))
                .willReturn(false);
        given(cleanupService.cleanupNew(
                StorageRoot.PUBLIC,
                snapshot.operationId(),
                snapshot.newKey()))
                .willReturn(StorageCleanupService.CleanupOutcome.DONE);

        StorageMutationRecoveryService.RecoveryResult result = recoveryService.recoverBatch();

        assertThat(result.completed()).isEqualTo(1);
        verify(journalService).transition(1L, StorageMutationState.DONE, "RECOVERED_NEW_DELETE");
    }

    @Test
    void committedReplacementDeletesOldFileAndRetainsSharedReference() {
        StorageMutationSnapshot snapshot = snapshot(
                2L,
                StorageMutationType.REPLACE,
                "albums/thumbnails/new.jpg",
                "albums/thumbnails/shared.jpg",
                2);
        given(journalService.claimBatch(any(), any(), any(), eq(25), eq(8)))
                .willReturn(List.of(snapshot));
        given(referenceChecker.isReferenced(StorageDomain.NOTICE, snapshot.newKey()))
                .willReturn(true);
        given(cleanupService.cleanupOld(
                StorageDomain.NOTICE,
                StorageRoot.PUBLIC,
                snapshot.oldKey()))
                .willReturn(StorageCleanupService.CleanupOutcome.SHARED_REFERENCE);

        StorageMutationRecoveryService.RecoveryResult result = recoveryService.recoverBatch();

        assertThat(result.retained()).isEqualTo(1);
        verify(journalService).transition(
                2L,
                StorageMutationState.DONE,
                "SHARED_REFERENCE_RETAINED");
    }

    @Test
    void failedCleanupSchedulesBoundedBackoff() {
        StorageMutationSnapshot snapshot = snapshot(
                3L,
                StorageMutationType.DELETE,
                null,
                "notices/attachments/old.pdf",
                3);
        given(journalService.claimBatch(any(), any(), any(), eq(25), eq(8)))
                .willReturn(List.of(snapshot));
        given(referenceChecker.isReferenced(StorageDomain.NOTICE, snapshot.oldKey()))
                .willReturn(false);
        given(cleanupService.cleanupOld(
                StorageDomain.NOTICE,
                StorageRoot.PUBLIC,
                snapshot.oldKey()))
                .willReturn(StorageCleanupService.CleanupOutcome.FAILED);

        StorageMutationRecoveryService.RecoveryResult result = recoveryService.recoverBatch();

        assertThat(result.retried()).isEqualTo(1);
        verify(journalService).retryClaimed(
                eq(3L),
                eq("RECOVERY_OLD_DELETE_FAILED"),
                any(LocalDateTime.class),
                eq(8));
    }

    @Test
    void deleteStillReferencedIsTreatedAsRolledBack() {
        StorageMutationSnapshot snapshot = snapshot(
                4L,
                StorageMutationType.DELETE,
                null,
                "questions/attachments/retained.pdf",
                1);
        given(journalService.claimBatch(any(), any(), any(), eq(25), eq(8)))
                .willReturn(List.of(snapshot));
        given(referenceChecker.isReferenced(StorageDomain.NOTICE, snapshot.oldKey()))
                .willReturn(true);

        StorageMutationRecoveryService.RecoveryResult result = recoveryService.recoverBatch();

        assertThat(result.retained()).isEqualTo(1);
        verify(journalService).transition(4L, StorageMutationState.DONE, "RECOVERED_DELETE_ROLLBACK");
    }

    private StorageMutationSnapshot snapshot(
            Long id,
            StorageMutationType type,
            String newKey,
            String oldKey,
            int attempts) {
        return new StorageMutationSnapshot(
                id,
                "10000000-0000-0000-0000-000000000000",
                StorageDomain.NOTICE,
                type,
                StorageRoot.PUBLIC,
                newKey,
                oldKey,
                StorageMutationState.RETRY,
                attempts);
    }
}
