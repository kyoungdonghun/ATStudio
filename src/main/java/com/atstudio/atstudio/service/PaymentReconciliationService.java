package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.CompletedProviderLookupClaim;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.LocalReconciliationBatch;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class PaymentReconciliationService {

    private static final int MAX_RECONCILIATION_BATCH_SIZE = 1000;
    private static final int MAX_ISSUE_DETAIL_LIMIT = 500;
    private static final int MAX_COMPLETED_ORDER_LOOKBACK_DAYS = 365;
    private static final int MAX_COMPLETED_ORDER_CHECKS_PER_RUN = 5000;
    private static final int STALE_PROCESSING_MINUTES = 15;
    private static final long FIRST_CANDIDATE_ID = 0L;

    private final PaymentReconciliationTransactionService reconciliationTransactions;
    private final List<PaymentStatusLookupProvider> paymentStatusLookupProviders;
    private final PaymentReconciliationIncidentService incidentService;
    private final PaymentCommandTransactionService paymentCommandTransactions;
    private final PaymentProperties paymentProperties;
    private final Clock paymentClock;

    @Autowired
    public PaymentReconciliationService(
            PaymentReconciliationTransactionService reconciliationTransactions,
            List<PaymentStatusLookupProvider> paymentStatusLookupProviders,
            PaymentReconciliationIncidentService incidentService,
            PaymentCommandTransactionService paymentCommandTransactions,
            PaymentProperties paymentProperties) {
        this(
                reconciliationTransactions,
                paymentStatusLookupProviders,
                incidentService,
                paymentCommandTransactions,
                paymentProperties,
                Clock.system(paymentProperties.schedulerZoneId()));
    }

    PaymentReconciliationService(
            PaymentReconciliationTransactionService reconciliationTransactions,
            List<PaymentStatusLookupProvider> paymentStatusLookupProviders,
            PaymentReconciliationIncidentService incidentService,
            PaymentCommandTransactionService paymentCommandTransactions,
            PaymentProperties paymentProperties,
            Clock paymentClock) {
        this.reconciliationTransactions = reconciliationTransactions;
        this.paymentStatusLookupProviders = paymentStatusLookupProviders;
        this.incidentService = incidentService;
        this.paymentCommandTransactions = paymentCommandTransactions;
        this.paymentProperties = paymentProperties;
        this.paymentClock = paymentClock;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "${app.payment.scheduler-zone:Asia/Seoul}")
    @Transactional(propagation = Propagation.NEVER)
    public void reconcilePaymentLedgersOnSchedule() {
        reconcileLocalLedger(true);
        reconcileProviderLedger();
    }

    public ReconciliationResult reconcileLocalLedger() {
        return reconcileLocalLedger(false);
    }

    private ReconciliationResult reconcileLocalLedger(boolean persistIncidents) {
        int batchSize = reconciliationBatchSize();
        int issueDetailLimit = issueDetailLimit();
        int checkedOrders = 0;
        int checkedAgreements = 0;
        int doneOrdersWithoutPayment = 0;
        int activeAgreementsWithoutSubscription = 0;
        List<LocalReconciliationIssue> issueDetails = new ArrayList<>();

        long lastSeenOrderID = FIRST_CANDIDATE_ID;
        while (true) {
            LocalReconciliationBatch batch = reconciliationTransactions.reconcileDoneOrderBatch(
                    lastSeenOrderID,
                    batchSize);
            checkedOrders += batch.checked();
            doneOrdersWithoutPayment += batch.issues().size();
            persistLocalIssues(persistIncidents, batch.issues());
            appendIssueDetails(issueDetails, batch.issues(), issueDetailLimit);
            if (batch.exhausted()) {
                break;
            }
            lastSeenOrderID = batch.lastSeenID();
        }

        long lastSeenAgreementID = FIRST_CANDIDATE_ID;
        LocalDate today = LocalDate.now(paymentClock);
        while (true) {
            LocalReconciliationBatch batch = reconciliationTransactions.reconcileActiveAgreementBatch(
                    lastSeenAgreementID,
                    batchSize,
                    today);
            checkedAgreements += batch.checked();
            activeAgreementsWithoutSubscription += batch.issues().size();
            persistLocalIssues(persistIncidents, batch.issues());
            appendIssueDetails(issueDetails, batch.issues(), issueDetailLimit);
            if (batch.exhausted()) {
                break;
            }
            lastSeenAgreementID = batch.lastSeenID();
        }

        int totalIssues = doneOrdersWithoutPayment + activeAgreementsWithoutSubscription;
        ReconciliationResult result = new ReconciliationResult(
                checkedOrders,
                checkedAgreements,
                doneOrdersWithoutPayment,
                activeAgreementsWithoutSubscription,
                totalIssues,
                totalIssues > issueDetails.size(),
                List.copyOf(issueDetails));
        if (result.hasMismatch()) {
            log.warn(
                    "Payment reconciliation completed with mismatches. checkedOrders={}, checkedBillingAgreements={}, doneOrdersWithoutPayment={}, activeAgreementsWithoutSubscription={}, totalIssues={}, issueDetailsTruncated={}",
                    result.checkedOrders(),
                    result.checkedBillingAgreements(),
                    result.doneOrdersWithoutPayment(),
                    result.activeAgreementsWithoutSubscription(),
                    result.totalIssues(),
                    result.issueDetailsTruncated());
        } else {
            log.info(
                    "Payment reconciliation completed. checkedOrders={}, checkedBillingAgreements={}, totalIssues={}",
                    result.checkedOrders(),
                    result.checkedBillingAgreements(),
                    result.totalIssues());
        }
        return result;
    }

    @Transactional(propagation = Propagation.NEVER)
    public ProviderReconciliationResult diagnoseProviderLedger() {
        LocalDateTime staleBefore = LocalDateTime.now(paymentClock).minusMinutes(STALE_PROCESSING_MINUTES);
        int batchSize = reconciliationBatchSize();
        int issueDetailLimit = issueDetailLimit();
        long lastSeenID = FIRST_CANDIDATE_ID;
        int skipped = 0;
        int checked = 0;
        int providerNotFound = 0;
        int lookupFailures = 0;
        int providerDoneWithoutLocalFinalization = 0;
        int localDoneButProviderNotDone = 0;
        int amountMismatches = 0;
        int totalIssues = 0;
        List<ProviderReconciliationIssue> issues = new ArrayList<>();
        Set<Long> providerOrdersCheckedThisRun = new HashSet<>();

        while (true) {
            List<Long> candidateIDs = reconciliationTransactions.findProviderCandidateIDs(
                    staleBefore,
                    lastSeenID,
                    batchSize);
            if (candidateIDs.isEmpty()) {
                break;
            }

            for (Long candidateID : candidateIDs) {
                lastSeenID = candidateID;
                Optional<ProviderLookupClaim> claim =
                        reconciliationTransactions.loadProviderLookup(candidateID, staleBefore);
                if (claim.isEmpty()) {
                    skipped++;
                    continue;
                }

                checked++;
                providerOrdersCheckedThisRun.add(candidateID);
                OrderReconciliationOutcome outcome = diagnoseProviderOrder(claim.get());
                skipped += outcome.skipped() ? 1 : 0;
                providerNotFound += outcome.providerNotFound() ? 1 : 0;
                lookupFailures += outcome.lookupFailure() ? 1 : 0;
                providerDoneWithoutLocalFinalization += outcome.providerDoneWithoutLocalFinalization() ? 1 : 0;
                localDoneButProviderNotDone += outcome.localDoneButProviderNotDone() ? 1 : 0;
                amountMismatches += outcome.amountMismatch() ? 1 : 0;
                if (outcome.issue() != null) {
                    totalIssues++;
                    appendIssueDetail(issues, outcome.issue(), issueDetailLimit);
                }
            }

            if (candidateIDs.size() < batchSize) {
                break;
            }
        }

        LocalDateTime completedCreatedAfter = LocalDateTime.now(paymentClock)
                .minusDays(completedOrderLookbackDays());
        int completedRemaining = completedOrderMaxPerRun();
        lastSeenID = FIRST_CANDIDATE_ID;
        while (completedRemaining > 0) {
            int pageSize = Math.min(batchSize, completedRemaining);
            List<Long> candidateIDs = reconciliationTransactions.findCompletedProviderCandidateIDs(
                    completedCreatedAfter,
                    lastSeenID,
                    pageSize);
            if (candidateIDs.isEmpty()) {
                break;
            }

            for (Long candidateID : candidateIDs) {
                lastSeenID = candidateID;
                completedRemaining--;
                if (providerOrdersCheckedThisRun.contains(candidateID)) {
                    skipped++;
                    continue;
                }
                Optional<CompletedProviderLookupClaim> completedClaim =
                        reconciliationTransactions.loadCompletedProviderLookup(
                                candidateID, completedCreatedAfter);
                if (completedClaim.isEmpty() || completedClaim.get().locallyRefundedOrCancelled()) {
                    skipped++;
                    continue;
                }

                checked++;
                OrderReconciliationOutcome outcome = diagnoseCompletedProviderOrder(completedClaim.get());
                skipped += outcome.skipped() ? 1 : 0;
                providerNotFound += outcome.providerNotFound() ? 1 : 0;
                lookupFailures += outcome.lookupFailure() ? 1 : 0;
                localDoneButProviderNotDone += outcome.localDoneButProviderNotDone() ? 1 : 0;
                amountMismatches += outcome.amountMismatch() ? 1 : 0;
                if (outcome.issue() != null) {
                    totalIssues++;
                    appendIssueDetail(issues, outcome.issue(), issueDetailLimit);
                }
            }

            if (candidateIDs.size() < pageSize) {
                break;
            }
        }

        return new ProviderReconciliationResult(
                checked,
                skipped,
                providerNotFound,
                lookupFailures,
                providerDoneWithoutLocalFinalization,
                localDoneButProviderNotDone,
                amountMismatches,
                0,
                totalIssues,
                totalIssues > issues.size(),
                List.copyOf(issues));
    }

    @Transactional(propagation = Propagation.NEVER)
    public ProviderReconciliationResult reconcileProviderLedger() {
        LocalDateTime staleBefore = LocalDateTime.now(paymentClock).minusMinutes(STALE_PROCESSING_MINUTES);
        int batchSize = reconciliationBatchSize();
        int issueDetailLimit = issueDetailLimit();
        long lastSeenID = 0L;
        int skipped = 0;
        int checked = 0;
        int providerNotFound = 0;
        int lookupFailures = 0;
        int providerDoneWithoutLocalFinalization = 0;
        int localDoneButProviderNotDone = 0;
        int amountMismatches = 0;
        int finalizedOrders = 0;
        int totalIssues = 0;
        List<ProviderReconciliationIssue> issues = new ArrayList<>();
        Set<Long> providerOrdersCheckedThisRun = new HashSet<>();

        while (true) {
            List<Long> candidateIDs = reconciliationTransactions.findProviderCandidateIDs(
                    staleBefore,
                    lastSeenID,
                    batchSize);
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
                providerOrdersCheckedThisRun.add(candidateID);
                OrderReconciliationOutcome outcome = reconcileProviderOrder(claim.get(), staleBefore);
                skipped += outcome.skipped() ? 1 : 0;
                providerNotFound += outcome.providerNotFound() ? 1 : 0;
                lookupFailures += outcome.lookupFailure() ? 1 : 0;
                providerDoneWithoutLocalFinalization += outcome.providerDoneWithoutLocalFinalization() ? 1 : 0;
                localDoneButProviderNotDone += outcome.localDoneButProviderNotDone() ? 1 : 0;
                amountMismatches += outcome.amountMismatch() ? 1 : 0;
                finalizedOrders += outcome.finalized() ? 1 : 0;
                if (outcome.issue() != null) {
                    totalIssues++;
                    appendIssueDetail(issues, outcome.issue(), issueDetailLimit);
                }
            }

            if (candidateIDs.size() < batchSize) {
                break;
            }
        }

        LocalDateTime completedCreatedAfter = LocalDateTime.now(paymentClock)
                .minusDays(completedOrderLookbackDays());
        int completedRemaining = completedOrderMaxPerRun();
        lastSeenID = FIRST_CANDIDATE_ID;
        while (completedRemaining > 0) {
            int pageSize = Math.min(batchSize, completedRemaining);
            List<Long> candidateIDs = reconciliationTransactions.findCompletedProviderCandidateIDs(
                    completedCreatedAfter,
                    lastSeenID,
                    pageSize);
            if (candidateIDs.isEmpty()) {
                break;
            }

            for (Long candidateID : candidateIDs) {
                lastSeenID = candidateID;
                completedRemaining--;
                if (providerOrdersCheckedThisRun.contains(candidateID)) {
                    skipped++;
                    continue;
                }
                Optional<CompletedProviderLookupClaim> completedClaim =
                        reconciliationTransactions.loadCompletedProviderLookup(
                                candidateID, completedCreatedAfter);
                if (completedClaim.isEmpty() || completedClaim.get().locallyRefundedOrCancelled()) {
                    skipped++;
                    continue;
                }

                checked++;
                OrderReconciliationOutcome outcome = reconcileCompletedProviderOrder(completedClaim.get());
                skipped += outcome.skipped() ? 1 : 0;
                providerNotFound += outcome.providerNotFound() ? 1 : 0;
                lookupFailures += outcome.lookupFailure() ? 1 : 0;
                localDoneButProviderNotDone += outcome.localDoneButProviderNotDone() ? 1 : 0;
                amountMismatches += outcome.amountMismatch() ? 1 : 0;
                if (outcome.issue() != null) {
                    totalIssues++;
                    appendIssueDetail(issues, outcome.issue(), issueDetailLimit);
                }
            }

            if (candidateIDs.size() < pageSize) {
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
                totalIssues,
                totalIssues > issues.size(),
                List.copyOf(issues));
        if (result.hasMismatch()) {
            log.warn(
                    "Payment provider reconciliation completed with unresolved mismatches. checkedOrders={}, skippedOrders={}, providerNotFound={}, lookupFailures={}, providerDoneWithoutLocalFinalization={}, localDoneButProviderNotDone={}, amountMismatches={}, finalizedOrders={}, totalIssues={}, issueDetailsTruncated={}",
                    result.checkedOrders(),
                    result.skippedOrders(),
                    result.providerNotFound(),
                    result.lookupFailures(),
                    result.providerDoneWithoutLocalFinalization(),
                    result.localDoneButProviderNotDone(),
                    result.amountMismatches(),
                    result.finalizedOrders(),
                    result.totalIssues(),
                    result.issueDetailsTruncated());
        } else {
            log.info(
                    "Payment provider reconciliation completed. checkedOrders={}, skippedOrders={}, finalizedOrders={}, totalIssues={}",
                    result.checkedOrders(),
                    result.skippedOrders(),
                    result.finalizedOrders(),
                    result.totalIssues());
        }
        return result;
    }

    private void persistLocalIssues(
            boolean persistIncidents,
            List<LocalReconciliationIssue> issues) {
        if (persistIncidents && !issues.isEmpty()) {
            incidentService.recordLocalIssues(issues);
        }
    }

    private <T> void appendIssueDetails(List<T> target, List<T> source, int limit) {
        int remaining = limit - target.size();
        if (remaining <= 0 || source.isEmpty()) {
            return;
        }
        target.addAll(source.subList(0, Math.min(remaining, source.size())));
    }

    private <T> void appendIssueDetail(List<T> target, T issue, int limit) {
        if (target.size() < limit) {
            target.add(issue);
        }
    }

    private int reconciliationBatchSize() {
        int configured = paymentProperties.getOperations().getReconciliation().getBatchSize();
        return Math.max(1, Math.min(configured, MAX_RECONCILIATION_BATCH_SIZE));
    }

    private int issueDetailLimit() {
        int configured = paymentProperties.getOperations().getReconciliation().getIssueDetailLimit();
        return Math.max(0, Math.min(configured, MAX_ISSUE_DETAIL_LIMIT));
    }

    private int completedOrderLookbackDays() {
        int configured = paymentProperties.getOperations().getReconciliation()
                .getCompletedOrderLookbackDays();
        return Math.max(1, Math.min(configured, MAX_COMPLETED_ORDER_LOOKBACK_DAYS));
    }

    private int completedOrderMaxPerRun() {
        int configured = paymentProperties.getOperations().getReconciliation()
                .getCompletedOrderMaxPerRun();
        return Math.max(1, Math.min(configured, MAX_COMPLETED_ORDER_CHECKS_PER_RUN));
    }

    private OrderReconciliationOutcome diagnoseProviderOrder(ProviderLookupClaim claim) {
        Optional<PaymentStatusLookupProvider> provider = lookupProvider(claim.provider());
        ProviderPaymentLookupResult providerResult;
        if (provider.isEmpty()) {
            providerResult = ProviderPaymentLookupResult.failure(
                    claim.provider(),
                    claim.orderID(),
                    "PROVIDER_LOOKUP_NOT_CONFIGURED",
                    "Provider lookup is not configured.");
        } else {
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
        }

        EvidenceAssessment assessment = reconciliationTransactions.assessProviderEvidence(claim, providerResult);
        if (assessment.exactDone()) {
            ProviderReconciliationIssue detectedIssue = issue(
                    claim,
                    providerResult,
                    PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                    null,
                    null);
            return OrderReconciliationOutcome.unresolved(
                    detectedIssue,
                    false,
                    false,
                    false,
                    true,
                    false,
                    false);
        }
        return diagnosticMismatch(claim, providerResult, false, false, assessment);
    }

    private OrderReconciliationOutcome diagnoseCompletedProviderOrder(
            CompletedProviderLookupClaim completedClaim) {
        ProviderLookupClaim claim = completedClaim.claim();
        Optional<PaymentStatusLookupProvider> provider = lookupProvider(claim.provider());
        if (provider.isEmpty()) {
            return OrderReconciliationOutcome.skippedWithoutLookup();
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
        EvidenceAssessment assessment = assessCompletedProviderEvidence(claim, providerResult);
        return assessment.exactDone()
                ? OrderReconciliationOutcome.verifiedCompleted()
                : diagnosticMismatch(
                        claim,
                        providerResult,
                        false,
                        assessment.issueType() == PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                        assessment);
    }

    private OrderReconciliationOutcome diagnosticMismatch(
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

    private OrderReconciliationOutcome reconcileCompletedProviderOrder(
            CompletedProviderLookupClaim completedClaim) {
        ProviderLookupClaim claim = completedClaim.claim();
        Optional<PaymentStatusLookupProvider> provider = lookupProvider(claim.provider());
        if (provider.isEmpty()) {
            return OrderReconciliationOutcome.skippedWithoutLookup();
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

        EvidenceAssessment assessment = assessCompletedProviderEvidence(claim, providerResult);
        if (!assessment.exactDone()) {
            return recordMismatch(
                    claim,
                    providerResult,
                    false,
                    assessment.issueType() == PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                    assessment);
        }

        incidentService.resolveProviderRecoveryIncidents(claim.orderID());
        return OrderReconciliationOutcome.verifiedCompleted();
    }

    private EvidenceAssessment assessCompletedProviderEvidence(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult result) {
        if (result == null || result.lookupFailure()) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                    result == null ? "PROVIDER_LOOKUP_RESULT_MISSING" : result.failureCode(),
                    result == null ? "Provider lookup returned no evidence." : result.failureMessage());
        }
        if (!result.found()) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_FOUND,
                    result.failureCode(),
                    result.failureMessage());
        }
        if (result.provider() != claim.provider()
                || !Objects.equals(result.orderId(), claim.orderID())
                || !Objects.equals(result.currency(), claim.currency())
                || result.transactionId() == null
                || result.transactionId().isBlank()
                || (claim.providerTransactionID() != null
                && !Objects.equals(result.transactionId(), claim.providerTransactionID()))) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                    "PROVIDER_EVIDENCE_MISMATCH",
                    "Provider identity, order, currency, or transaction evidence does not match local DONE state.");
        }
        if (!"DONE".equals(result.status())) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                    "PROVIDER_STATUS_MISMATCH",
                    "Provider payment is not DONE while the local order is DONE.");
        }
        if (result.totalAmount() == null || claim.amount().compareTo(result.totalAmount()) != 0) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.AMOUNT_MISMATCH,
                    "AMOUNT_MISMATCH",
                    "Provider amount does not match the local DONE order.");
        }
        return EvidenceAssessment.exact();
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
            int totalIssues,
            boolean issueDetailsTruncated,
            List<LocalReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return totalIssues > 0;
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
            int totalIssues,
            boolean issueDetailsTruncated,
            List<ProviderReconciliationIssue> issues) {
        public boolean hasMismatch() {
            return totalIssues > 0;
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

        static OrderReconciliationOutcome verifiedCompleted() {
            return new OrderReconciliationOutcome(false, false, false, false, false, false, false, null);
        }

        static OrderReconciliationOutcome skippedWithoutLookup() {
            return new OrderReconciliationOutcome(true, false, false, false, false, false, false, null);
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
