package com.atstudio.atstudio.service;

import com.atstudio.atstudio.repository.BillingAgreementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WithdrawalBillingCleanupService {

    private static final int CLEANUP_BATCH_SIZE = 100;
    private static final long FIRST_CANDIDATE_ID = 0L;

    private final BillingAgreementRepository billingAgreementRepository;
    private final BillingAgreementCleanupTransactionService cleanupTransactionService;
    private final BillingAgreementCleanupProviderExecutor cleanupProviderExecutor;

    public WithdrawalBillingCleanupService(
            BillingAgreementRepository billingAgreementRepository,
            BillingAgreementCleanupTransactionService cleanupTransactionService,
            BillingAgreementCleanupProviderExecutor cleanupProviderExecutor) {
        this.billingAgreementRepository = billingAgreementRepository;
        this.cleanupTransactionService = cleanupTransactionService;
        this.cleanupProviderExecutor = cleanupProviderExecutor;
    }

    public List<Long> findRetryCandidateIDs() {
        return billingAgreementRepository.findWithdrawalCleanupCandidateIDs(
                FIRST_CANDIDATE_ID,
                PageRequest.of(0, CLEANUP_BATCH_SIZE));
    }

    public int detectStaleCleanupClaims() {
        LocalDateTime staleBefore = LocalDateTime.now()
                .minus(BillingAgreementCleanupTransactionService.CLEANUP_LEASE);
        List<Long> staleCandidateIDs =
                billingAgreementRepository.findStaleBillingKeyCleanupCandidateIDs(
                        staleBefore,
                        FIRST_CANDIDATE_ID,
                        PageRequest.of(0, CLEANUP_BATCH_SIZE));
        int markedPending = 0;
        for (Long agreementID : staleCandidateIDs) {
            if (cleanupTransactionService.markStaleCleanupPending(agreementID, staleBefore)) {
                markedPending++;
            }
        }
        return markedPending;
    }

    @Transactional(propagation = Propagation.NEVER)
    public CleanupOutcome cleanup(Long billingAgreementID) {
        BillingAgreementCleanupTransactionService.WithdrawalCleanupClaim claim =
                cleanupTransactionService.claimWithdrawalCleanup(
                        billingAgreementID,
                        LocalDateTime.now());
        switch (claim.action()) {
            case SKIPPED -> {
                return CleanupOutcome.SKIPPED;
            }
            case IN_PROGRESS -> {
                return CleanupOutcome.IN_PROGRESS;
            }
            case PENDING_PROVIDER_CONFIRMATION -> {
                return CleanupOutcome.PENDING_PROVIDER_CONFIRMATION;
            }
            case STABLE_FAILURE -> {
                return CleanupOutcome.FAILED;
            }
            case COMPLETED -> {
                return CleanupOutcome.SUCCEEDED;
            }
            case CALL_PROVIDER -> {
                // Continue below after the durable claim commits.
            }
        }

        BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult =
                cleanupProviderExecutor.deleteBillingKey(
                        claim.provider(),
                        claim.billingKeyCiphertext());
        cleanupTransactionService.recordWithdrawalCleanupResult(claim, providerResult);
        return switch (providerResult.disposition()) {
            case SUCCEEDED -> CleanupOutcome.SUCCEEDED;
            case FAILED -> CleanupOutcome.FAILED;
            case PENDING_PROVIDER_CONFIRMATION -> CleanupOutcome.PENDING_PROVIDER_CONFIRMATION;
        };
    }

    public enum CleanupOutcome {
        SUCCEEDED,
        FAILED,
        PENDING_PROVIDER_CONFIRMATION,
        IN_PROGRESS,
        SKIPPED
    }
}
