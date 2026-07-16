package com.atstudio.atstudio.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.CompletedProviderLookupClaim;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationService unit tests")
class PaymentReconciliationServiceTest {

    @Mock PaymentReconciliationTransactionService reconciliationTransactions;
    @Mock PaymentStatusLookupProvider paymentStatusLookupProvider;
    @Mock PaymentReconciliationIncidentService incidentService;
    @Mock PaymentCommandTransactionService paymentCommandTransactions;

    PaymentReconciliationService service;
    PaymentProperties paymentProperties;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        paymentProperties.getOperations().getReconciliation().setBatchSize(100);
        paymentProperties.getOperations().getReconciliation().setIssueDetailLimit(3);
        service = new PaymentReconciliationService(
                reconciliationTransactions,
                List.of(paymentStatusLookupProvider),
                incidentService,
                paymentCommandTransactions,
                paymentProperties);
    }

    @Test
    @DisplayName("scheduled reconciliation has an explicit configurable Asia/Seoul zone")
    void reconcilePaymentLedgersOnSchedule_hasExplicitZone() throws Exception {
        Method scheduledMethod = PaymentReconciliationService.class.getMethod(
                "reconcilePaymentLedgersOnSchedule");
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);

        assertThat(scheduled.cron()).isEqualTo("0 0 1 * * *");
        assertThat(scheduled.zone()).isEqualTo("${app.payment.scheduler-zone:Asia/Seoul}");
    }

    @Test
    @DisplayName("scheduled reconciliation persists every local batch and terminates on final pages")
    void reconcilePaymentLedgersOnSchedule_recordsEveryLocalBatch() {
        List<PaymentReconciliationService.LocalReconciliationIssue> orderIssues = issues(
                1,
                PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT);
        List<PaymentReconciliationService.LocalReconciliationIssue> agreementIssues = issues(
                1,
                PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION);
        given(reconciliationTransactions.reconcileDoneOrderBatch(0L, 100))
                .willReturn(batch(1, 7L, true, orderIssues));
        given(reconciliationTransactions.reconcileActiveAgreementBatch(eq(0L), eq(100), any(LocalDate.class)))
                .willReturn(batch(1, 9L, true, agreementIssues));
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of());

        service.reconcilePaymentLedgersOnSchedule();

        verify(incidentService).recordLocalIssues(orderIssues);
        verify(incidentService).recordLocalIssues(agreementIssues);
        verify(reconciliationTransactions).findProviderCandidateIDs(any(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("local reconciliation scans more than 100 rows with keyset cursors and caps response details")
    void reconcileLocalLedger_scansAllKeysetBatchesAndCapsDetails() {
        List<PaymentReconciliationService.LocalReconciliationIssue> firstOrderIssues = issues(
                100,
                PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT);
        List<PaymentReconciliationService.LocalReconciliationIssue> finalOrderIssues = issues(
                1,
                PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT);
        List<PaymentReconciliationService.LocalReconciliationIssue> firstAgreementIssues = issues(
                100,
                PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION);
        List<PaymentReconciliationService.LocalReconciliationIssue> finalAgreementIssues = issues(
                1,
                PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION);
        given(reconciliationTransactions.reconcileDoneOrderBatch(0L, 100))
                .willReturn(batch(100, 100L, false, firstOrderIssues));
        given(reconciliationTransactions.reconcileDoneOrderBatch(100L, 100))
                .willReturn(batch(1, 150L, true, finalOrderIssues));
        given(reconciliationTransactions.reconcileActiveAgreementBatch(eq(0L), eq(100), any(LocalDate.class)))
                .willReturn(batch(100, 200L, false, firstAgreementIssues));
        given(reconciliationTransactions.reconcileActiveAgreementBatch(eq(200L), eq(100), any(LocalDate.class)))
                .willReturn(batch(1, 250L, true, finalAgreementIssues));

        PaymentReconciliationService.ReconciliationResult result = service.reconcileLocalLedger();

        assertThat(result.checkedOrders()).isEqualTo(101);
        assertThat(result.checkedBillingAgreements()).isEqualTo(101);
        assertThat(result.doneOrdersWithoutPayment()).isEqualTo(101);
        assertThat(result.activeAgreementsWithoutSubscription()).isEqualTo(101);
        assertThat(result.totalIssues()).isEqualTo(202);
        assertThat(result.issueDetailsTruncated()).isTrue();
        assertThat(result.issues()).hasSize(3);
        verifyNoInteractions(incidentService);
        InOrder keysetOrder = inOrder(reconciliationTransactions);
        keysetOrder.verify(reconciliationTransactions).reconcileDoneOrderBatch(0L, 100);
        keysetOrder.verify(reconciliationTransactions).reconcileDoneOrderBatch(100L, 100);
        keysetOrder.verify(reconciliationTransactions)
                .reconcileActiveAgreementBatch(eq(0L), eq(100), any(LocalDate.class));
        keysetOrder.verify(reconciliationTransactions)
                .reconcileActiveAgreementBatch(eq(200L), eq(100), any(LocalDate.class));
    }

    @Test
    @DisplayName("read-only provider diagnostics report exact DONE evidence without recovery writes")
    void diagnoseProviderLedger_exactDoneNeverMutatesRecoveryState() {
        ProviderLookupClaim claim = claim(PaymentPurpose.RENEWAL);
        ProviderPaymentLookupResult providerResult = exactResult(claim);
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(reconciliationTransactions.loadProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(claim));
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of());
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(claim.orderID())).willReturn(providerResult);
        given(reconciliationTransactions.assessProviderEvidence(claim, providerResult))
                .willReturn(new EvidenceAssessment(true, null, null, null));

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.finalizedOrders()).isZero();
        assertThat(result.providerDoneWithoutLocalFinalization()).isEqualTo(1);
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::issueType)
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        verify(reconciliationTransactions, never()).claimProviderLookup(anyLong(), any());
        verify(reconciliationTransactions, never()).applyExactProviderSuccess(any(), any(), any());
        verifyNoInteractions(incidentService, paymentCommandTransactions);
    }

    @ParameterizedTest
    @EnumSource(value = PaymentPurpose.class, names = {"SUBSCRIBE", "UPGRADE", "RENEWAL"})
    @DisplayName("exact DONE evidence dispatches the purpose-specific finalizer")
    void exactDoneEvidence_dispatchesPurposeFinalizer(PaymentPurpose purpose) {
        ProviderLookupClaim claim = claim(purpose);
        ProviderPaymentLookupResult providerResult = exactResult(claim);
        PaymentCommandTransactionService.ReconciliationFinalizationTarget target =
                new PaymentCommandTransactionService.ReconciliationFinalizationTarget(
                        purpose,
                        claim.userID(),
                        claim.billingAgreementID(),
                        claim.orderID());
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(reconciliationTransactions.claimProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(claim));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(claim.orderID())).willReturn(providerResult);
        given(reconciliationTransactions.assessProviderEvidence(claim, providerResult))
                .willReturn(new EvidenceAssessment(true, null, null, null));
        given(reconciliationTransactions.applyExactProviderSuccess(eq(claim), eq(providerResult), any()))
                .willReturn(target);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.finalizedOrders()).isEqualTo(1);
        assertThat(result.hasMismatch()).isFalse();
        verify(incidentService).recordProviderRecoveryIssue(any());
        verify(incidentService).resolveProviderRecoveryIncidents(claim.orderID());
        switch (purpose) {
            case SUBSCRIBE -> verify(paymentCommandTransactions).finalizeInitialCharge(7L, 11L, claim.orderID());
            case UPGRADE -> verify(paymentCommandTransactions).finalizeUpgrade(7L, 11L, claim.orderID());
            case RENEWAL -> verify(paymentCommandTransactions).finalizeRenewal(11L, claim.orderID());
            default -> throw new AssertionError("Unexpected purpose: " + purpose);
        }
    }

    @Test
    @DisplayName("mismatched provider evidence remains Incident-only")
    void mismatchedEvidence_doesNotPersistOrFinalize() {
        ProviderLookupClaim claim = claim(PaymentPurpose.RENEWAL);
        ProviderPaymentLookupResult providerResult = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-1",
                "DONE",
                BigDecimal.valueOf(10900),
                "KRW",
                "{}");
        EvidenceAssessment mismatch = new EvidenceAssessment(
                false,
                PaymentReconciliationIssueType.AMOUNT_MISMATCH,
                "AMOUNT_MISMATCH",
                "Provider amount does not match the local command.");
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(reconciliationTransactions.claimProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(claim));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(claim.orderID())).willReturn(providerResult);
        given(reconciliationTransactions.assessProviderEvidence(claim, providerResult)).willReturn(mismatch);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.amountMismatches()).isEqualTo(1);
        assertThat(result.finalizedOrders()).isZero();
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::failureCode)
                .isEqualTo("AMOUNT_MISMATCH");
        verify(incidentService).recordProviderRecoveryIssue(any());
        verify(reconciliationTransactions, never()).applyExactProviderSuccess(any(), any(), any());
        verify(paymentCommandTransactions, never()).finalizeInitialCharge(anyLong(), anyLong(), any());
        verify(paymentCommandTransactions, never()).finalizeUpgrade(anyLong(), anyLong(), any());
        verify(paymentCommandTransactions, never()).finalizeRenewal(anyLong(), any());
    }

    @Test
    @DisplayName("provider reconciliation logs aggregate counts without raw issue evidence")
    void providerReconciliation_logsOnlyAggregateCounts() {
        String rawProviderTransactionID = "provider-transaction-secret-123";
        String rawFailureMessage = "provider URI contained sensitive evidence";
        ProviderLookupClaim claim = claim(PaymentPurpose.RENEWAL);
        ProviderPaymentLookupResult providerResult = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                rawProviderTransactionID,
                "DONE",
                BigDecimal.valueOf(10900),
                "KRW",
                "{}");
        EvidenceAssessment mismatch = new EvidenceAssessment(
                false,
                PaymentReconciliationIssueType.AMOUNT_MISMATCH,
                "AMOUNT_MISMATCH",
                rawFailureMessage);
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(reconciliationTransactions.claimProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(claim));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(claim.orderID())).willReturn(providerResult);
        given(reconciliationTransactions.assessProviderEvidence(claim, providerResult)).willReturn(mismatch);

        Logger logger = (Logger) LoggerFactory.getLogger(PaymentReconciliationService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            service.reconcileProviderLedger();
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        String logged = String.join("\n", appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList());
        assertThat(logged)
                .contains("checkedOrders=1", "amountMismatches=1", "totalIssues=1")
                .doesNotContain(rawProviderTransactionID, rawFailureMessage, "ProviderReconciliationResult", "issues=");
        assertThat(appender.list).allMatch(event -> event.getThrowableProxy() == null);
    }

    @Test
    @DisplayName("recent DONE provider order with exact evidence is verified without local re-finalization")
    void completedDoneOrder_exactEvidenceIsVerified() {
        ProviderLookupClaim claim = completedClaim(false).claim();
        ProviderPaymentLookupResult providerResult = ProviderPaymentLookupResult.found(
                claim.provider(), claim.orderID(), claim.providerTransactionID(), "DONE",
                claim.amount(), claim.currency(), "{}");
        stubCompletedLookup(completedClaim(false), providerResult);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(1);
        assertThat(result.hasMismatch()).isFalse();
        assertThat(result.finalizedOrders()).isZero();
        verify(incidentService).resolveProviderRecoveryIncidents(claim.orderID());
        verify(reconciliationTransactions, never()).applyExactProviderSuccess(any(), any(), any());
    }

    @Test
    @DisplayName("locally succeeded refund excludes a completed order from provider status comparison")
    void completedDoneOrder_localRefundIsSkipped() {
        CompletedProviderLookupClaim completed = completedClaim(true);
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(completed.claim().paymentOrderID()));
        given(reconciliationTransactions.loadCompletedProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(completed));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.skippedOrders()).isEqualTo(1);
        verifyNoInteractions(paymentStatusLookupProvider);
        verifyNoInteractions(incidentService);
    }

    @Test
    @DisplayName("completed DONE provider not-found creates the dedicated deduplicated incident input")
    void completedDoneOrder_providerNotFound() {
        CompletedProviderLookupClaim completed = completedClaim(false);
        ProviderPaymentLookupResult providerResult = ProviderPaymentLookupResult.notFound(
                completed.claim().provider(), completed.claim().orderID(),
                "NOT_FOUND_PAYMENT", "not found");
        stubCompletedLookup(completed, providerResult);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.providerNotFound()).isEqualTo(1);
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::issueType)
                .isEqualTo(PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_FOUND);
        verify(incidentService).recordProviderRecoveryIssue(any());
    }

    @Test
    @DisplayName("completed DONE lookup failure remains distinguishable from provider not-found")
    void completedDoneOrder_lookupFailure() {
        CompletedProviderLookupClaim completed = completedClaim(false);
        ProviderPaymentLookupResult providerResult = ProviderPaymentLookupResult.failure(
                completed.claim().provider(), completed.claim().orderID(),
                "PROVIDER_TIMEOUT", "timeout");
        stubCompletedLookup(completed, providerResult);

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.lookupFailures()).isEqualTo(1);
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::issueType)
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED);
    }

    @Test
    @DisplayName("completed DONE detects provider non-DONE and amount mismatch separately")
    void completedDoneOrder_nonDoneAndAmountMismatch() {
        CompletedProviderLookupClaim first = completedClaim(false);
        CompletedProviderLookupClaim second = new CompletedProviderLookupClaim(
                new ProviderLookupClaim(
                        2L, 7L, 11L, 13L, "ORDER-DONE-2", "COMMAND-DONE-2",
                        PaymentProviderType.TOSS_BILLING, PaymentPurpose.RENEWAL,
                        PaymentOrderStatus.DONE, BigDecimal.valueOf(9900), "KRW", "tx-done-2", true, null),
                false);
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(1L, 2L));
        given(reconciliationTransactions.loadCompletedProviderLookup(eq(1L), any()))
                .willReturn(Optional.of(first));
        given(reconciliationTransactions.loadCompletedProviderLookup(eq(2L), any()))
                .willReturn(Optional.of(second));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(first.claim().orderID()))
                .willReturn(ProviderPaymentLookupResult.found(
                        first.claim().provider(), first.claim().orderID(), first.claim().providerTransactionID(),
                        "CANCELED", first.claim().amount(), "KRW", "{}"));
        given(paymentStatusLookupProvider.findPaymentByOrderId(second.claim().orderID()))
                .willReturn(ProviderPaymentLookupResult.found(
                        second.claim().provider(), second.claim().orderID(), second.claim().providerTransactionID(),
                        "DONE", BigDecimal.valueOf(10900), "KRW", "{}"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.localDoneButProviderNotDone()).isEqualTo(1);
        assertThat(result.amountMismatches()).isEqualTo(1);
        assertThat(result.issues())
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::issueType)
                .containsExactly(
                        PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE,
                        PaymentReconciliationIssueType.AMOUNT_MISMATCH);
    }

    @Test
    @DisplayName("completed provider verification obeys the configured per-run cap")
    void completedDoneOrder_respectsRunCap() {
        paymentProperties.getOperations().getReconciliation().setBatchSize(2);
        paymentProperties.getOperations().getReconciliation().setCompletedOrderMaxPerRun(3);
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), eq(0L), eq(2)))
                .willReturn(List.of(1L, 2L));
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), eq(2L), eq(1)))
                .willReturn(List.of(3L));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.skippedOrders()).isEqualTo(3);
        verify(reconciliationTransactions).findCompletedProviderCandidateIDs(any(), eq(0L), eq(2));
        verify(reconciliationTransactions).findCompletedProviderCandidateIDs(any(), eq(2L), eq(1));
        verify(reconciliationTransactions, never()).findCompletedProviderCandidateIDs(any(), eq(3L), anyInt());
    }

    private ProviderLookupClaim claim(PaymentPurpose purpose) {
        return new ProviderLookupClaim(
                1L,
                7L,
                11L,
                purpose == PaymentPurpose.SUBSCRIBE ? null : 13L,
                "ORDER-" + purpose,
                "COMMAND-" + purpose,
                PaymentProviderType.TOSS_BILLING,
                purpose,
                PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION,
                BigDecimal.valueOf(9900),
                "KRW",
                null,
                true,
                null);
    }

    private CompletedProviderLookupClaim completedClaim(boolean locallyRefunded) {
        return new CompletedProviderLookupClaim(
                new ProviderLookupClaim(
                        1L,
                        7L,
                        11L,
                        13L,
                        "ORDER-DONE-1",
                        "COMMAND-DONE-1",
                        PaymentProviderType.TOSS_BILLING,
                        PaymentPurpose.RENEWAL,
                        PaymentOrderStatus.DONE,
                        BigDecimal.valueOf(9900),
                        "KRW",
                        "tx-done-1",
                        true,
                        null),
                locallyRefunded);
    }

    private void stubCompletedLookup(
            CompletedProviderLookupClaim completed,
            ProviderPaymentLookupResult providerResult) {
        given(reconciliationTransactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(completed.claim().paymentOrderID()));
        given(reconciliationTransactions.loadCompletedProviderLookup(anyLong(), any()))
                .willReturn(Optional.of(completed));
        given(paymentStatusLookupProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(paymentStatusLookupProvider.isLookupConfigured()).willReturn(true);
        given(paymentStatusLookupProvider.findPaymentByOrderId(completed.claim().orderID()))
                .willReturn(providerResult);
    }

    private PaymentReconciliationTransactionService.LocalReconciliationBatch batch(
            int checked,
            long lastSeenID,
            boolean exhausted,
            List<PaymentReconciliationService.LocalReconciliationIssue> issues) {
        return new PaymentReconciliationTransactionService.LocalReconciliationBatch(
                checked,
                lastSeenID,
                exhausted,
                issues);
    }

    private List<PaymentReconciliationService.LocalReconciliationIssue> issues(
            int count,
            PaymentReconciliationIssueType issueType) {
        List<PaymentReconciliationService.LocalReconciliationIssue> issues = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            issues.add(new PaymentReconciliationService.LocalReconciliationIssue(
                    issueType,
                    (long) index,
                    (long) index,
                    (long) index,
                    "ORDER-" + index,
                    PaymentProviderType.TOSS_BILLING,
                    PaymentPurpose.RENEWAL,
                    PaymentOrderStatus.DONE.name(),
                    BigDecimal.valueOf(9900)));
        }
        return List.copyOf(issues);
    }

    private ProviderPaymentLookupResult exactResult(ProviderLookupClaim claim) {
        return ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-" + claim.purpose(),
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");
    }
}
