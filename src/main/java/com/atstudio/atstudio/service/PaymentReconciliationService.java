package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private static final int PROVIDER_CANDIDATE_PAGE_SIZE = 100;
    private static final int STALE_PROCESSING_MINUTES = 15;

    private final PaymentReconciliationTransactionService reconciliationTransactions;
    private final List<PaymentStatusLookupProvider> paymentStatusLookupProviders;
    private final PaymentReconciliationIncidentService incidentService;
    private final PaymentCommandTransactionService paymentCommandTransactions;

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(propagation = Propagation.NEVER)
    public void reconcilePaymentLedgersOnSchedule() {
        ReconciliationResult local = reconcileLocalLedger();
        incidentService.recordLocalIssues(local);
        reconcileProviderLedger();
    }

    public ReconciliationResult reconcileLocalLedger() {
        ReconciliationResult result = reconciliationTransactions.reconcileLocalLedger();
        if (result.hasMismatch()) {
            log.warn("Payment reconciliation completed with mismatches: {}", result);
        } else {
            log.info("Payment reconciliation completed: {}", result);
        }
        return result;
    }

    @Transactional(propagation = Propagation.NEVER)
    public ProviderReconciliationResult reconcileProviderLedger() {
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(STALE_PROCESSING_MINUTES);
        long lastSeenID = 0L;
        int skipped = 0;
        int checked = 0;
        int providerNotFound = 0;
        int lookupFailures = 0;
        int providerDoneWithoutLocalFinalization = 0;
        int localDoneButProviderNotDone = 0;
        int amountMismatches = 0;
        int finalizedOrders = 0;
        List<ProviderReconciliationIssue> issues = new ArrayList<>();

        while (true) {
            List<Long> candidateIDs = reconciliationTransactions.findProviderCandidateIDs(
                    staleBefore,
                    lastSeenID,
                    PROVIDER_CANDIDATE_PAGE_SIZE);
            if (candidateIDs.isEmpty()) {
                break;
            }

            for (Long candidateID : candidateIDs) {
                lastSeenID = candidateID;
                Optional<ProviderLookupClaim> claim =
                        reconciliationTransactions.claimProviderLookup(candidateID, staleBefore);
                if (claim.isEmpty()) {
                    skipped++;
                    continue;
                }

                checked++;
                OrderReconciliationOutcome outcome = reconcileProviderOrder(claim.get(), staleBefore);
                skipped += outcome.skipped() ? 1 : 0;
                providerNotFound += outcome.providerNotFound() ? 1 : 0;
                lookupFailures += outcome.lookupFailure() ? 1 : 0;
                providerDoneWithoutLocalFinalization += outcome.providerDoneWithoutLocalFinalization() ? 1 : 0;
                localDoneButProviderNotDone += outcome.localDoneButProviderNotDone() ? 1 : 0;
                amountMismatches += outcome.amountMismatch() ? 1 : 0;
                finalizedOrders += outcome.finalized() ? 1 : 0;
                if (outcome.issue() != null) {
                    issues.add(outcome.issue());
                }
            }

            if (candidateIDs.size() < PROVIDER_CANDIDATE_PAGE_SIZE) {
                break;
            }
        }

        ProviderReconciliationResult result = new ProviderReconciliationResult(
                checked,
                skipped,
                providerNotFound,
                lookupFailures,
                providerDoneWithoutLocalFinalization,
                localDoneButProviderNotDone,
                amountMismatches,
                finalizedOrders,
                List.copyOf(issues));
        if (result.hasMismatch()) {
            log.warn("Payment provider reconciliation completed with unresolved mismatches: {}", result);
        } else {
            log.info("Payment provider reconciliation completed: {}", result);
        }
        return result;
    }

    private OrderReconciliationOutcome reconcileProviderOrder(
            ProviderLookupClaim claim,
            LocalDateTime staleBefore) {
        Optional<PaymentStatusLookupProvider> provider = lookupProvider(claim.provider());
        if (provider.isEmpty()) {
            ProviderPaymentLookupResult unavailable = ProviderPaymentLookupResult.failure(
                    claim.provider(),
                    claim.orderID(),
                    "PROVIDER_LOOKUP_NOT_CONFIGURED",
                    "Provider lookup is not configured.");
            return recordMismatch(claim, unavailable, true, false);
        }

        ProviderPaymentLookupResult providerResult;
        try {
            assertNoTransactionAtProviderBoundary();
            providerResult = provider.get().findPaymentByOrderId(claim.orderID());
        } catch (RuntimeException exception) {
            providerResult = ProviderPaymentLookupResult.failure(
                    claim.provider(),
                    claim.orderID(),
                    "PROVIDER_LOOKUP_EXCEPTION",
                    exception.getClass().getSimpleName());
        }

        EvidenceAssessment assessment = reconciliationTransactions.assessProviderEvidence(claim, providerResult);
        if (!assessment.exactDone()) {
            return recordMismatch(claim, providerResult, false, false, assessment);
        }

        ProviderReconciliationIssue detectedIssue = issue(
                claim,
                providerResult,
                PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                null,
                null);
        incidentService.recordProviderRecoveryIssue(detectedIssue);
        try {
            PaymentCommandTransactionService.ReconciliationFinalizationTarget target =
                    reconciliationTransactions.applyExactProviderSuccess(claim, providerResult, staleBefore);
            finalizeByPurpose(target);
        } catch (RuntimeException exception) {
            ProviderReconciliationIssue failureIssue = issue(
                    claim,
                    providerResult,
                    PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                    "LOCAL_FINALIZATION_FAILED",
                    exception.getClass().getSimpleName());
            incidentService.recordProviderFinalizationFailure(failureIssue);
            return OrderReconciliationOutcome.unresolved(
                    failureIssue,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false);
        }

        try {
            incidentService.resolveProviderRecoveryIncidents(claim.orderID());
            return OrderReconciliationOutcome.success();
        } catch (RuntimeException exception) {
            log.error(
                    "Payment reconciliation finalized but Incident resolution failed. orderId={}, exceptionClass={}",
                    claim.orderID(),
                    exception.getClass().getSimpleName());
            ProviderReconciliationIssue resolutionIssue = issue(
                    claim,
                    providerResult,
                    PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                    "INCIDENT_RESOLUTION_FAILED",
                    exception.getClass().getSimpleName());
            return OrderReconciliationOutcome.finalizedWithIssue(resolutionIssue);
        }
    }

    private OrderReconciliationOutcome recordMismatch(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult providerResult,
            boolean skipped,
            boolean localDoneButProviderNotDone) {
        EvidenceAssessment assessment = reconciliationTransactions.assessProviderEvidence(claim, providerResult);
        return recordMismatch(claim, providerResult, skipped, localDoneButProviderNotDone, assessment);
    }

    private OrderReconciliationOutcome recordMismatch(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult providerResult,
            boolean skipped,
            boolean localDoneButProviderNotDone,
            EvidenceAssessment assessment) {
        ProviderReconciliationIssue mismatch = issue(
                claim,
                providerResult,
                assessment.issueType(),
                assessment.failureCode(),
                assessment.failureMessage());
        incidentService.recordProviderRecoveryIssue(mismatch);
        boolean providerNotFound = providerResult != null
                && !providerResult.found()
                && !providerResult.lookupFailure();
        boolean lookupFailure = providerResult == null || providerResult.lookupFailure();
        boolean providerDone = providerResult != null && providerResult.providerDone();
        return OrderReconciliationOutcome.unresolved(
                mismatch,
                skipped,
                providerNotFound,
                lookupFailure,
                providerDone,
                localDoneButProviderNotDone,
                assessment.issueType() == PaymentReconciliationIssueType.AMOUNT_MISMATCH);
    }

    private void finalizeByPurpose(PaymentCommandTransactionService.ReconciliationFinalizationTarget target) {
        switch (target.purpose()) {
            case SUBSCRIBE -> paymentCommandTransactions.finalizeInitialCharge(
                    target.userID(),
                    target.agreementID(),
                    target.orderID());
            case UPGRADE -> paymentCommandTransactions.finalizeUpgrade(
                    target.userID(),
                    target.agreementID(),
                    target.orderID());
            case RENEWAL -> paymentCommandTransactions.finalizeRenewal(
                    target.agreementID(),
                    target.orderID());
            default -> throw new IllegalStateException(
                    "Unsupported reconciliation purpose: " + target.purpose());
        }
    }

    private Optional<PaymentStatusLookupProvider> lookupProvider(PaymentProviderType providerType) {
        return paymentStatusLookupProviders.stream()
                .filter(provider -> provider.getProviderType() == providerType)
                .filter(PaymentStatusLookupProvider::isLookupConfigured)
                .findFirst();
    }

    private ProviderReconciliationIssue issue(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult providerResult,
            PaymentReconciliationIssueType issueType,
            String failureCode,
            String failureMessage) {
        return new ProviderReconciliationIssue(
                issueType,
                claim.paymentOrderID(),
                claim.userID(),
                claim.billingAgreementID(),
                claim.orderID(),
                claim.provider(),
                claim.purpose(),
                claim.localStatus().name(),
                providerResult == null ? null : providerResult.status(),
                claim.amount(),
                providerResult == null ? null : providerResult.totalAmount(),
                claim.currency(),
                providerResult == null ? null : providerResult.currency(),
                providerResult == null ? null : providerResult.transactionId(),
                failureCode,
                failureMessage);
    }

    private void assertNoTransactionAtProviderBoundary() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Provider lookup cannot run inside a local transaction.");
        }
    }

    public record ReconciliationResult(
            int checkedOrders,
            int checkedBillingAgreements,
            int doneOrdersWithoutPayment,
            int activeAgreementsWithoutSubscription,
            List<LocalReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return doneOrdersWithoutPayment > 0 || activeAgreementsWithoutSubscription > 0;
        }
    }

    public record ProviderReconciliationResult(
            int checkedOrders,
            int skippedOrders,
            int providerNotFound,
            int lookupFailures,
            int providerDoneWithoutLocalFinalization,
            int localDoneButProviderNotDone,
            int amountMismatches,
            int finalizedOrders,
            List<ProviderReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return !issues.isEmpty();
        }
    }

    public record LocalReconciliationIssue(
            PaymentReconciliationIssueType issueType,
            Long paymentOrderId,
            Long userId,
            Long billingAgreementId,
            String orderId,
            PaymentProviderType provider,
            PaymentPurpose purpose,
            String localStatus,
            BigDecimal localAmount) {
    }

    public record ProviderReconciliationIssue(
            PaymentReconciliationIssueType issueType,
            Long paymentOrderId,
            Long userId,
            Long billingAgreementId,
            String orderId,
            PaymentProviderType provider,
            PaymentPurpose purpose,
            String localStatus,
            String providerStatus,
            BigDecimal localAmount,
            BigDecimal providerAmount,
            String localCurrency,
            String providerCurrency,
            String providerTransactionId,
            String failureCode,
            String failureMessage) {
    }

    private record OrderReconciliationOutcome(
            boolean skipped,
            boolean providerNotFound,
            boolean lookupFailure,
            boolean providerDoneWithoutLocalFinalization,
            boolean localDoneButProviderNotDone,
            boolean amountMismatch,
            boolean finalized,
            ProviderReconciliationIssue issue) {

        static OrderReconciliationOutcome success() {
            return new OrderReconciliationOutcome(false, false, false, true, false, false, true, null);
        }

        static OrderReconciliationOutcome finalizedWithIssue(ProviderReconciliationIssue issue) {
            return new OrderReconciliationOutcome(false, false, false, true, false, false, true, issue);
        }

        static OrderReconciliationOutcome unresolved(
                ProviderReconciliationIssue issue,
                boolean skipped,
                boolean providerNotFound,
                boolean lookupFailure,
                boolean providerDoneWithoutLocalFinalization,
                boolean localDoneButProviderNotDone,
                boolean amountMismatch) {
            return new OrderReconciliationOutcome(
                    skipped,
                    providerNotFound,
                    lookupFailure,
                    providerDoneWithoutLocalFinalization,
                    localDoneButProviderNotDone,
                    amountMismatch,
                    false,
                    issue);
        }
    }
}
