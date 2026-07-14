package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageMutationCoordinatorTest {

    @Mock StorageService storageService;
    @Mock StorageMutationJournalService journalService;
    @Mock StorageCleanupService cleanupService;

    @InjectMocks StorageMutationCoordinator coordinator;

    @BeforeEach
    void initializeSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void removesNewFileWhenBusinessTransactionRollsBack() {
        MockMultipartFile file = file("track.mp3");
        given(storageService.generateKey("tracks/audio", "track.mp3"))
                .willReturn("tracks/audio/generated.mp3");
        given(journalService.prepare(anyList())).willReturn(List.of(11L));
        given(cleanupService.cleanupNew(
                eq(StorageRoot.PUBLIC), anyString(), eq("tracks/audio/generated.mp3")))
                .willReturn(StorageCleanupService.CleanupOutcome.DONE);

        coordinator.store(StorageDomain.TRACK, StorageRoot.PUBLIC, file, "tracks/audio");
        synchronization().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(journalService).transition(
                11L,
                StorageMutationState.ROLLBACK_CLEANUP,
                "NEW_DELETE_PENDING");
        verify(cleanupService).cleanupNew(
                eq(StorageRoot.PUBLIC), anyString(), eq("tracks/audio/generated.mp3"));
        verify(journalService).transition(11L, StorageMutationState.DONE, "NEW_DELETE_DONE");
    }

    @Test
    void deletesReplacedFileOnlyAfterCommitAndRetainsSharedReference() {
        MockMultipartFile file = file("new.jpg");
        given(storageService.generateKey("albums/thumbnails", "new.jpg"))
                .willReturn("albums/thumbnails/generated.jpg");
        given(journalService.prepare(anyList())).willReturn(List.of(21L));
        given(cleanupService.cleanupOld(
                StorageDomain.ALBUM,
                StorageRoot.PUBLIC,
                "albums/thumbnails/shared.jpg"))
                .willReturn(StorageCleanupService.CleanupOutcome.SHARED_REFERENCE);

        coordinator.replace(
                StorageDomain.ALBUM,
                StorageRoot.PUBLIC,
                file,
                "albums/thumbnails",
                "albums/thumbnails/shared.jpg");

        verify(cleanupService, never()).cleanupOld(any(), any(), anyString());
        synchronization().afterCommit();

        verify(journalService).transition(
                21L,
                StorageMutationState.AFTER_COMMIT_DELETE,
                "OLD_DELETE_PENDING");
        verify(journalService).transition(
                21L,
                StorageMutationState.DONE,
                "SHARED_REFERENCE_RETAINED");
    }

    @Test
    void recordsRetryWhenAfterCommitDeleteFails() {
        given(journalService.prepare(anyList())).willReturn(List.of(31L));
        given(cleanupService.cleanupOld(
                StorageDomain.NOTICE,
                StorageRoot.PUBLIC,
                "notices/attachments/old.pdf"))
                .willReturn(StorageCleanupService.CleanupOutcome.FAILED);

        coordinator.deleteAfterCommit(
                StorageDomain.NOTICE,
                StorageRoot.PUBLIC,
                "notices/attachments/old.pdf");
        synchronization().afterCommit();

        verify(journalService).recordCleanupFailure(
                eq(31L),
                eq("OLD_DELETE_FAILED"),
                any(),
                eq(8));
    }

    @Test
    void journalsAndCleansFirstStageWhenSecondStageFails() {
        MockMultipartFile first = file("first.pdf");
        MockMultipartFile second = file("second.pdf");
        given(storageService.generateKey("questions/attachments", "first.pdf"))
                .willReturn("questions/attachments/first-generated.pdf");
        given(storageService.generateKey("questions/attachments", "second.pdf"))
                .willReturn("questions/attachments/second-generated.pdf");
        org.mockito.Mockito.doAnswer(invocation -> {
                    if (invocation.getArgument(3) == second) {
                        throw new IllegalStateException("stage failed");
                    }
                    return null;
                })
                .when(storageService)
                .stage(
                        eq(StorageRoot.PUBLIC),
                        anyString(),
                        anyString(),
                        any());
        given(journalService.prepare(anyList())).willReturn(List.of(41L));
        given(cleanupService.cleanupNew(
                eq(StorageRoot.PUBLIC),
                anyString(),
                eq("questions/attachments/first-generated.pdf")))
                .willReturn(StorageCleanupService.CleanupOutcome.DONE);

        assertThatThrownBy(() -> coordinator.storeAll(
                StorageDomain.QUESTION,
                StorageRoot.PUBLIC,
                List.of(first, second),
                "questions/attachments"))
                .isInstanceOf(IllegalStateException.class);

        ArgumentCaptor<List<StorageMutationDraft>> drafts = ArgumentCaptor.forClass(List.class);
        verify(journalService).prepare(drafts.capture());
        assertThat(drafts.getValue()).hasSize(1);
        verify(journalService).transition(41L, StorageMutationState.DONE, "NEW_DELETE_DONE");
        assertThat(TransactionSynchronizationManager.getSynchronizations()).isEmpty();
    }

    @Test
    void deleteRollbackNeverDeletesOldFile() {
        given(journalService.prepare(anyList())).willReturn(List.of(51L));

        coordinator.deleteAfterCommit(
                StorageDomain.PLAYLIST,
                StorageRoot.PUBLIC,
                "playlists/thumbnails/old.jpg");
        synchronization().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(cleanupService, never()).cleanupOld(any(), any(), anyString());
        verify(journalService).transition(
                51L,
                StorageMutationState.DONE,
                "TX_ROLLED_BACK_NO_DELETE");
    }

    @Test
    void refusesMutationWithoutTransactionSynchronization() {
        TransactionSynchronizationManager.clearSynchronization();

        assertThatThrownBy(() -> coordinator.store(
                StorageDomain.TRACK,
                StorageRoot.PUBLIC,
                file("track.mp3"),
                "tracks/audio"))
                .isInstanceOf(IllegalStateException.class);
    }

    private TransactionSynchronization synchronization() {
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertThat(synchronizations).hasSize(1);
        return synchronizations.get(0);
    }

    private MockMultipartFile file(String filename) {
        return new MockMultipartFile("file", filename, "application/octet-stream", new byte[]{1, 2, 3});
    }
}
