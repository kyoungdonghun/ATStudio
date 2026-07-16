package com.atstudio.atstudio.service.storage;

import com.atstudio.atstudio.entity.StorageMutation;
import com.atstudio.atstudio.repository.StorageMutationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageMutationJournalServiceTest {

    @Mock StorageMutationRepository mutationRepository;

    StorageMutationJournalService service;

    @BeforeEach
    void setUp() {
        service = new StorageMutationJournalService(mutationRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void preparePersistsReplayableMutationEvidence() {
        StorageMutation saved = mutation(11L, StorageMutationState.PREPARED, 0);
        given(mutationRepository.saveAllAndFlush(any())).willReturn(List.of(saved));
        StorageMutationDraft draft = new StorageMutationDraft(
                "operation-1",
                StorageDomain.TRACK,
                StorageMutationType.REPLACE,
                StorageRoot.PUBLIC,
                "tracks/new.mp3",
                "tracks/old.mp3");

        List<Long> ids = service.prepare(List.of(draft));

        assertThat(ids).containsExactly(11L);
        ArgumentCaptor<List<StorageMutation>> captor = ArgumentCaptor.forClass(List.class);
        verify(mutationRepository).saveAllAndFlush(captor.capture());
        StorageMutation prepared = captor.getValue().get(0);
        assertThat(prepared.getState()).isEqualTo(StorageMutationState.PREPARED);
        assertThat(prepared.getNewKey()).isEqualTo("tracks/new.mp3");
        assertThat(prepared.getOldKey()).isEqualTo("tracks/old.mp3");
    }

    @Test
    void recordCleanupFailureSchedulesRetryThenStopsAtAttemptLimit() {
        LocalDateTime retryAt = LocalDateTime.now().plusSeconds(30);
        StorageMutation retryable = mutation(21L, StorageMutationState.ROLLBACK_CLEANUP, 0);
        given(mutationRepository.findById(21L)).willReturn(Optional.of(retryable));

        service.recordCleanupFailure(21L, "NEW_DELETE_FAILED", retryAt, 2);

        assertThat(retryable.getState()).isEqualTo(StorageMutationState.RETRY);
        assertThat(retryable.getAttemptCount()).isEqualTo(1);
        assertThat(retryable.getNextAttemptAt()).isEqualTo(retryAt);

        service.recordCleanupFailure(21L, "NEW_DELETE_FAILED", retryAt.plusMinutes(1), 2);

        assertThat(retryable.getState()).isEqualTo(StorageMutationState.FAILED);
        assertThat(retryable.getNextAttemptAt()).isNull();
    }

    @Test
    void claimBatchFencesCandidatesAndReturnsTheClaimedSnapshot() {
        StorageMutation candidate = mutation(31L, StorageMutationState.RETRY, 2);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime claimUntil = now.plusMinutes(2);
        given(mutationRepository.findRecoveryCandidates(
                any(), any(), any(), any(), anyInt(), any()))
                .willReturn(List.of(candidate));

        List<StorageMutationSnapshot> claimed = service.claimBatch(
                now, now.minusMinutes(5), claimUntil, 50, 8);

        assertThat(candidate.getAttemptCount()).isEqualTo(3);
        assertThat(candidate.getNextAttemptAt()).isEqualTo(claimUntil);
        assertThat(claimed).singleElement().satisfies(snapshot -> {
            assertThat(snapshot.id()).isEqualTo(31L);
            assertThat(snapshot.attemptCount()).isEqualTo(3);
            assertThat(snapshot.state()).isEqualTo(StorageMutationState.RETRY);
        });
    }

    @Test
    void retryClaimedIsIdempotentForMissingRowsAndFailsAtBound() {
        StorageMutation exhausted = mutation(41L, StorageMutationState.RETRY, 8);
        given(mutationRepository.findById(40L)).willReturn(Optional.empty());
        given(mutationRepository.findById(41L)).willReturn(Optional.of(exhausted));

        service.retryClaimed(40L, "RECOVERY_FAILED", LocalDateTime.now(), 8);
        service.retryClaimed(41L, "RECOVERY_FAILED", LocalDateTime.now(), 8);

        assertThat(exhausted.getState()).isEqualTo(StorageMutationState.FAILED);
        assertThat(exhausted.getNextAttemptAt()).isNull();
    }

    private StorageMutation mutation(Long id, StorageMutationState state, int attemptCount) {
        StorageMutation mutation = StorageMutation.prepared(
                "operation-1",
                StorageDomain.TRACK,
                StorageMutationType.REPLACE,
                StorageRoot.PUBLIC,
                "tracks/new.mp3",
                "tracks/old.mp3");
        ReflectionTestUtils.setField(mutation, "id", id);
        ReflectionTestUtils.setField(mutation, "state", state);
        ReflectionTestUtils.setField(mutation, "attemptCount", attemptCount);
        return mutation;
    }
}
