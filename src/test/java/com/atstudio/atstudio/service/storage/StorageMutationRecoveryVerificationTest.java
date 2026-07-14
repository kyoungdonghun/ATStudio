package com.atstudio.atstudio.service.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WI-019 storage recovery independent verification")
class StorageMutationRecoveryVerificationTest {

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
    @DisplayName("stale REPLACE without live DB reference cleans only the new file path")
    void staleReplaceWithoutReferenceCleansNewFileOnly() {
        StorageMutationSnapshot snapshot = new StorageMutationSnapshot(
                9L,
                "10000000-0000-0000-0000-000000000000",
                StorageDomain.COMPANY_CERTIFICATION,
                StorageMutationType.REPLACE,
                StorageRoot.PRIVATE,
                "company-docs/1/new/canonical.jpg",
                "company-docs/1/old/original.pdf",
                StorageMutationState.RETRY,
                1);
        given(journalService.claimBatch(any(), any(), any(), eq(25), eq(8)))
                .willReturn(List.of(snapshot));
        given(referenceChecker.isReferenced(StorageDomain.COMPANY_CERTIFICATION, snapshot.newKey()))
                .willReturn(false);
        given(cleanupService.cleanupNew(
                StorageRoot.PRIVATE,
                snapshot.operationId(),
                snapshot.newKey()))
                .willReturn(StorageCleanupService.CleanupOutcome.DONE);

        StorageMutationRecoveryService.RecoveryResult result = recoveryService.recoverBatch();

        assertThat(result.claimed()).isEqualTo(1);
        assertThat(result.completed()).isEqualTo(1);
        verify(cleanupService).cleanupNew(
                StorageRoot.PRIVATE,
                snapshot.operationId(),
                snapshot.newKey());
        verify(cleanupService, never()).cleanupOld(any(), any(), any());
        verify(journalService).transition(9L, StorageMutationState.DONE, "RECOVERED_NEW_DELETE");
    }
}
