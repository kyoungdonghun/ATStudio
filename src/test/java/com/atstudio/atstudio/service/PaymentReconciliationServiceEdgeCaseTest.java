package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.CompletedProviderLookupClaim;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentStatusLookupProvider;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationService failure classification")
class PaymentReconciliationServiceEdgeCaseTest {

    @Mock PaymentReconciliationTransactionService transactions;
    @Mock PaymentStatusLookupProvider provider;
    @Mock PaymentReconciliationIncidentService incidents;
    @Mock PaymentCommandTransactionService paymentCommands;

    private PaymentProperties properties;
    private PaymentReconciliationService service;

    @BeforeEach
    void setUp() {
        properties = new PaymentProperties();
        properties.getOperations().getReconciliation().setBatchSize(100);
        properties.getOperations().getReconciliation().setIssueDetailLimit(20);
        service = serviceWith(List.of(provider));
    }

    @AfterEach
    void clearTransactionMarker() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    @DisplayName("diagnostics distinguish an evaporated claim, provider not-found, and provider failure")
    void diagnoseProviderLedger_classifiesReadOnlyFailures() {
        ProviderLookupClaim notFound = claim(2L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, "tx-2");
        ProviderLookupClaim failed = claim(3L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, "tx-3");
        given(transactions.findProviderCandidateIDs(any(), eq(0L), eq(100)))
                .willReturn(List.of(1L, 2L, 3L));
        given(transactions.loadProviderLookup(eq(1L), any())).willReturn(Optional.empty());
        given(transactions.loadProviderLookup(eq(2L), any())).willReturn(Optional.of(notFound));
        given(transactions.loadProviderLookup(eq(3L), any())).willReturn(Optional.of(failed));
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of());
        given(provider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        given(provider.isLookupConfigured()).willReturn(true);
        ProviderPaymentLookupResult notFoundResult = ProviderPaymentLookupResult.notFound(
                PaymentProviderType.TOSS, notFound.orderID(), "NOT_FOUND_PAYMENT", "not found");
        ProviderPaymentLookupResult failedResult = ProviderPaymentLookupResult.failure(
                PaymentProviderType.TOSS, failed.orderID(), "PROVIDER_TIMEOUT", "timeout");
        given(provider.findPaymentByOrderId(notFound.orderID())).willReturn(notFoundResult);
        given(provider.findPaymentByOrderId(failed.orderID())).willReturn(failedResult);
        given(transactions.assessProviderEvidence(notFound, notFoundResult)).willReturn(
                EvidenceAssessment.mismatch(
                        PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                        "NOT_FOUND_PAYMENT",
                        "not found"));
        given(transactions.assessProviderEvidence(failed, failedResult)).willReturn(
                EvidenceAssessment.mismatch(
                        PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                        "PROVIDER_TIMEOUT",
                        "timeout"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(2);
        assertThat(result.skippedOrders()).isEqualTo(1);
        assertThat(result.providerNotFound()).isEqualTo(1);
        assertThat(result.lookupFailures()).isEqualTo(1);
        assertThat(result.totalIssues()).isEqualTo(2);
        verify(incidents, never()).recordProviderRecoveryIssue(any());
    }

    @Test
    @DisplayName("missing provider configuration is surfaced without attempting an external lookup")
    void diagnoseProviderLedger_missingProvider_isLookupFailure() {
        ProviderLookupClaim claim = claim(4L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        service = serviceWith(List.of());
        given(transactions.findProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(transactions.loadProviderLookup(eq(claim.paymentOrderID()), any()))
                .willReturn(Optional.of(claim));
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of());
        given(transactions.assessProviderEvidence(eq(claim), any())).willReturn(
                EvidenceAssessment.mismatch(
                        PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                        "PROVIDER_LOOKUP_NOT_CONFIGURED",
                        "not configured"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.lookupFailures()).isOne();
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::failureCode)
                .isEqualTo("PROVIDER_LOOKUP_NOT_CONFIGURED");
        verify(provider, never()).findPaymentByOrderId(any());
    }

    @Test
    @DisplayName("provider exceptions and transaction-boundary violations become bounded lookup failures")
    void diagnoseProviderLedger_providerException_isLookupFailure() {
        ProviderLookupClaim claim = claim(5L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        given(transactions.findProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(transactions.loadProviderLookup(eq(claim.paymentOrderID()), any()))
                .willReturn(Optional.of(claim));
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of());
        given(provider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        given(provider.isLookupConfigured()).willReturn(true);
        TransactionSynchronizationManager.setActualTransactionActive(true);
        given(transactions.assessProviderEvidence(eq(claim), any())).willReturn(
                EvidenceAssessment.mismatch(
                        PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                        "PROVIDER_LOOKUP_EXCEPTION",
                        "IllegalStateException"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.lookupFailures()).isOne();
        verify(provider, never()).findPaymentByOrderId(any());
    }

    @Test
    @DisplayName("completed DONE diagnostics fail closed for every identity and transaction mismatch")
    void diagnoseCompletedOrders_rejectsIncompleteIdentityEvidence() {
        List<CompletedProviderLookupClaim> claims = List.of(
                completed(11L, "tx-11"),
                completed(12L, "tx-12"),
                completed(13L, "tx-13"),
                completed(14L, "tx-14"),
                completed(15L, "tx-15"),
                completed(16L, "tx-16"));
        given(transactions.findProviderCandidateIDs(any(), eq(0L), anyInt())).willReturn(List.of());
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(claims.stream().map(c -> c.claim().paymentOrderID()).toList());
        for (CompletedProviderLookupClaim claim : claims) {
            given(transactions.loadCompletedProviderLookup(eq(claim.claim().paymentOrderID()), any()))
                    .willReturn(Optional.of(claim));
        }
        given(provider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        given(provider.isLookupConfigured()).willReturn(true);
        given(provider.findPaymentByOrderId("ORDER-11")).willReturn(null);
        given(provider.findPaymentByOrderId("ORDER-12")).willReturn(ProviderPaymentLookupResult.found(
                null, "ORDER-12", "tx-12", "DONE", BigDecimal.valueOf(9_900), "KRW", "{}"));
        given(provider.findPaymentByOrderId("ORDER-13")).willReturn(ProviderPaymentLookupResult.found(
                PaymentProviderType.TOSS, "OTHER", "tx-13", "DONE", BigDecimal.valueOf(9_900), "KRW", "{}"));
        given(provider.findPaymentByOrderId("ORDER-14")).willReturn(ProviderPaymentLookupResult.found(
                PaymentProviderType.TOSS, "ORDER-14", "tx-14", "DONE", BigDecimal.valueOf(9_900), "USD", "{}"));
        given(provider.findPaymentByOrderId("ORDER-15")).willReturn(ProviderPaymentLookupResult.found(
                PaymentProviderType.TOSS, "ORDER-15", " ", "DONE", BigDecimal.valueOf(9_900), "KRW", "{}"));
        given(provider.findPaymentByOrderId("ORDER-16")).willReturn(ProviderPaymentLookupResult.found(
                PaymentProviderType.TOSS, "ORDER-16", "other-tx", "DONE", BigDecimal.valueOf(9_900), "KRW", "{}"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.checkedOrders()).isEqualTo(6);
        assertThat(result.lookupFailures()).isEqualTo(1);
        assertThat(result.localDoneButProviderNotDone()).isEqualTo(5);
        assertThat(result.totalIssues()).isEqualTo(6);
    }

    @Test
    @DisplayName("provider success followed by local finalization failure remains an unresolved Incident")
    void reconcileProviderLedger_finalizationFailure_isRetained() {
        ProviderLookupClaim claim = claim(21L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult exact = exact(claim, "tx-21");
        stubPendingClaim(claim, exact);
        given(transactions.applyExactProviderSuccess(eq(claim), eq(exact), any()))
                .willThrow(new IllegalStateException("local finalization failed"));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.finalizedOrders()).isZero();
        assertThat(result.providerDoneWithoutLocalFinalization()).isOne();
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::failureCode)
                .isEqualTo("LOCAL_FINALIZATION_FAILED");
        verify(incidents).recordProviderFinalizationFailure(any());
    }

    @Test
    @DisplayName("successful local finalization with Incident-resolution failure reports finalized-with-issue")
    void reconcileProviderLedger_incidentResolutionFailure_reportsFinalizedIssue() {
        ProviderLookupClaim claim = claim(22L, PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult exact = exact(claim, "tx-22");
        stubPendingClaim(claim, exact);
        PaymentCommandTransactionService.ReconciliationFinalizationTarget target =
                new PaymentCommandTransactionService.ReconciliationFinalizationTarget(
                        PaymentPurpose.RENEWAL,
                        claim.userID(),
                        claim.billingAgreementID(),
                        claim.orderID());
        given(transactions.applyExactProviderSuccess(eq(claim), eq(exact), any())).willReturn(target);
        willThrow(new IllegalStateException("incident store unavailable"))
                .given(incidents).resolveProviderRecoveryIncidents(claim.orderID());

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.finalizedOrders()).isOne();
        assertThat(result.totalIssues()).isOne();
        assertThat(result.issues()).singleElement()
                .extracting(PaymentReconciliationService.ProviderReconciliationIssue::failureCode)
                .isEqualTo("INCIDENT_RESOLUTION_FAILED");
        verify(paymentCommands).finalizeRenewal(claim.billingAgreementID(), claim.orderID());
    }

    @Test
    @DisplayName("completed orders are skipped when no configured provider exists")
    void reconcileCompletedOrder_missingProvider_isSkipped() {
        CompletedProviderLookupClaim completed = completed(31L, "tx-31");
        service = serviceWith(List.of());
        given(transactions.findProviderCandidateIDs(any(), eq(0L), anyInt())).willReturn(List.of());
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(31L));
        given(transactions.loadCompletedProviderLookup(eq(31L), any()))
                .willReturn(Optional.of(completed));

        PaymentReconciliationService.ProviderReconciliationResult result = service.reconcileProviderLedger();

        assertThat(result.checkedOrders()).isOne();
        assertThat(result.skippedOrders()).isOne();
        assertThat(result.totalIssues()).isZero();
    }

    @Test
    @DisplayName("configured bounds clamp invalid batch, detail, lookback, and completed-order limits")
    void reconciliationConfiguration_invalidBoundsFailSafe() {
        properties.getOperations().getReconciliation().setBatchSize(0);
        properties.getOperations().getReconciliation().setIssueDetailLimit(-5);
        properties.getOperations().getReconciliation().setCompletedOrderLookbackDays(0);
        properties.getOperations().getReconciliation().setCompletedOrderMaxPerRun(0);
        given(transactions.findProviderCandidateIDs(any(), eq(0L), eq(1))).willReturn(List.of());
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), eq(1))).willReturn(List.of());

        PaymentReconciliationService.ProviderReconciliationResult result = service.diagnoseProviderLedger();

        assertThat(result.checkedOrders()).isZero();
        verify(transactions).findProviderCandidateIDs(any(), eq(0L), eq(1));
        verify(transactions).findCompletedProviderCandidateIDs(any(), eq(0L), eq(1));
    }

    private void stubPendingClaim(ProviderLookupClaim claim, ProviderPaymentLookupResult result) {
        given(transactions.findProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of(claim.paymentOrderID()));
        given(transactions.claimProviderLookup(eq(claim.paymentOrderID()), any()))
                .willReturn(Optional.of(claim));
        given(transactions.findCompletedProviderCandidateIDs(any(), eq(0L), anyInt()))
                .willReturn(List.of());
        given(provider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        given(provider.isLookupConfigured()).willReturn(true);
        given(provider.findPaymentByOrderId(claim.orderID())).willReturn(result);
        given(transactions.assessProviderEvidence(claim, result))
                .willReturn(new EvidenceAssessment(true, null, null, null));
    }

    private PaymentReconciliationService serviceWith(List<PaymentStatusLookupProvider> providers) {
        return new PaymentReconciliationService(transactions, providers, incidents, paymentCommands, properties);
    }

    private ProviderLookupClaim claim(Long id, PaymentOrderStatus status, String transactionID) {
        return new ProviderLookupClaim(
                id,
                7L,
                11L,
                13L,
                "ORDER-" + id,
                "COMMAND-" + id,
                PaymentProviderType.TOSS,
                PaymentPurpose.RENEWAL,
                status,
                BigDecimal.valueOf(9_900),
                "KRW",
                transactionID,
                true,
                null);
    }

    private CompletedProviderLookupClaim completed(Long id, String transactionID) {
        return new CompletedProviderLookupClaim(claim(id, PaymentOrderStatus.DONE, transactionID), false);
    }

    private ProviderPaymentLookupResult exact(ProviderLookupClaim claim, String transactionID) {
        return ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                transactionID,
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");
    }
}
