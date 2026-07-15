package com.atstudio.atstudio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalBillingCleanupCoordinator {

    private final WithdrawalBillingCleanupService cleanupService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cleanupAfterCommit(WithdrawalBillingCleanupRequestedEvent event) {
        try {
            cleanupService.cleanup(event.billingAgreementID());
        } catch (RuntimeException exception) {
            log.warn(
                    "Withdrawal billing cleanup failed after commit. billingAgreementID={}, failureType={}",
                    event.billingAgreementID(),
                    exception.getClass().getSimpleName());
        }
    }

    @Scheduled(cron = "0 15 1 * * *")
    public RetryRunResult retryFailedCleanups() {
        int staleMarkedPending = cleanupService.detectStaleCleanupClaims();
        List<Long> candidateIDs = cleanupService.findRetryCandidateIDs();
        int succeeded = 0;
        int failed = 0;
        int skipped = 0;
        int errors = 0;

        for (Long billingAgreementID : candidateIDs) {
            try {
                WithdrawalBillingCleanupService.CleanupOutcome outcome = cleanupService.cleanup(billingAgreementID);
                switch (outcome) {
                    case SUCCEEDED -> succeeded++;
                    case FAILED, PENDING_PROVIDER_CONFIRMATION -> failed++;
                    case IN_PROGRESS, SKIPPED -> skipped++;
                }
            } catch (RuntimeException exception) {
                errors++;
                log.warn(
                        "Withdrawal billing cleanup retry failed. billingAgreementID={}, failureType={}",
                        billingAgreementID,
                        exception.getClass().getSimpleName());
            }
        }

        if (staleMarkedPending > 0 || !candidateIDs.isEmpty()) {
            log.info(
                    "Withdrawal billing cleanup retry processed: staleMarkedPending={}, candidates={}, succeeded={}, failed={}, skipped={}, errors={}",
                    staleMarkedPending,
                    candidateIDs.size(),
                    succeeded,
                    failed,
                    skipped,
                    errors);
        }
        return new RetryRunResult(candidateIDs.size(), succeeded, failed, skipped, errors);
    }

    public record RetryRunResult(int candidates, int succeeded, int failed, int skipped, int errors) {
    }
}
