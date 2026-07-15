package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationService unit tests")
class PaymentReconciliationServiceTest {

    @Mock PaymentReconciliationTransactionService reconciliationTransactions;
    @Mock PaymentStatusLookupProvider paymentStatusLookupProvider;
    @Mock PaymentReconciliationIncidentService incidentService;
    @Mock PaymentCommandTransactionService paymentCommandTransactions;

    PaymentReconciliationService service;

    @BeforeEach
    void setUp() {
        service = new PaymentReconciliationService(
                reconciliationTransactions,
                List.of(paymentStatusLookupProvider),
                incidentService,
                paymentCommandTransactions);
    }

    @Test
    @DisplayName("scheduled reconciliation records local issues and runs provider orchestration without an outer unit")
    void reconcilePaymentLedgersOnSchedule_recordsLocalIssues() {
        PaymentReconciliationService.ReconciliationResult localResult =
                new PaymentReconciliationService.ReconciliationResult(0, 0, 0, 0, List.of());
        given(reconciliationTransactions.reconcileLocalLedger()).willReturn(localResult);
        given(reconciliationTransactions.findProviderCandidateIDs(any(), anyLong(), anyInt()))
                .willReturn(List.of());

        service.reconcilePaymentLedgersOnSchedule();

        verify(incidentService).recordLocalIssues(localResult);
        verify(reconciliationTransactions).findProviderCandidateIDs(any(), anyLong(), anyInt());
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
