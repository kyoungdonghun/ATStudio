package com.atstudio.atstudio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawalBillingCleanupCoordinator unit tests")
class WithdrawalBillingCleanupCoordinatorTest {

    @Mock WithdrawalBillingCleanupService cleanupService;

    @Test
    @DisplayName("cleanup event carries only agreement ID and is handled after commit")
    void cleanupAfterCommit_usesIDOnlyEvent() throws Exception {
        assertThat(WithdrawalBillingCleanupRequestedEvent.class.getRecordComponents())
                .extracting(component -> component.getName())
                .containsExactly("billingAgreementID");
        Method listenerMethod = WithdrawalBillingCleanupCoordinator.class.getMethod(
                "cleanupAfterCommit",
                WithdrawalBillingCleanupRequestedEvent.class);
        TransactionalEventListener listener = listenerMethod.getAnnotation(TransactionalEventListener.class);
        assertThat(listener.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);

        WithdrawalBillingCleanupCoordinator coordinator = new WithdrawalBillingCleanupCoordinator(cleanupService);
        coordinator.cleanupAfterCommit(new WithdrawalBillingCleanupRequestedEvent(11L));

        verify(cleanupService).cleanup(11L);
    }

    @Test
    @DisplayName("daily retry processes only repository-selected candidates and continues after one error")
    void retryFailedCleanups_continuesAfterError() throws Exception {
        Method retryMethod = WithdrawalBillingCleanupCoordinator.class.getMethod("retryFailedCleanups");
        Scheduled scheduled = retryMethod.getAnnotation(Scheduled.class);
        assertThat(scheduled.cron()).isEqualTo("0 15 1 * * *");

        given(cleanupService.findRetryCandidateIDs()).willReturn(List.of(11L, 12L, 13L));
        given(cleanupService.cleanup(11L))
                .willReturn(WithdrawalBillingCleanupService.CleanupOutcome.SUCCEEDED);
        given(cleanupService.cleanup(12L)).willThrow(new IllegalStateException("database unavailable"));
        given(cleanupService.cleanup(13L))
                .willReturn(WithdrawalBillingCleanupService.CleanupOutcome.FAILED);
        WithdrawalBillingCleanupCoordinator coordinator = new WithdrawalBillingCleanupCoordinator(cleanupService);

        WithdrawalBillingCleanupCoordinator.RetryRunResult result = coordinator.retryFailedCleanups();

        assertThat(result).isEqualTo(new WithdrawalBillingCleanupCoordinator.RetryRunResult(3, 1, 1, 0, 1));
        verify(cleanupService).cleanup(11L);
        verify(cleanupService).cleanup(12L);
        verify(cleanupService).cleanup(13L);
    }
}
