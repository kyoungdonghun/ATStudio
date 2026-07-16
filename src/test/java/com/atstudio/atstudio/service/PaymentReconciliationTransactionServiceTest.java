package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.EvidenceAssessment;
import com.atstudio.atstudio.service.PaymentReconciliationTransactionService.ProviderLookupClaim;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationTransactionService evidence gate tests")
class PaymentReconciliationTransactionServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentCommandTransactionService paymentCommandTransactionService;

    PaymentReconciliationTransactionService service;

    @BeforeEach
    void setUp() {
        service = new PaymentReconciliationTransactionService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                paymentRefundRepository,
                userSubscriptionRepository,
                paymentCommandTransactionService);
    }

    @Test
    @DisplayName("DONE order batch uses an ID keyset and reports only missing finalization rows")
    void doneOrderBatchUsesKeyset() {
        PaymentOrder missingPayment = paymentOrder(5L);
        PaymentOrder finalizedPayment = mock(PaymentOrder.class);
        given(finalizedPayment.getId()).willReturn(9L);
        given(paymentOrderRepository.findLocalReconciliationCandidates(
                eq(PaymentOrderStatus.DONE),
                any(),
                eq(0L),
                any()))
                .willReturn(List.of(missingPayment, finalizedPayment));
        given(subscriptionPaymentRepository.existsByPaymentOrder(missingPayment)).willReturn(false);
        given(subscriptionPaymentRepository.existsByPaymentOrder(finalizedPayment)).willReturn(true);

        PaymentReconciliationTransactionService.LocalReconciliationBatch batch =
                service.reconcileDoneOrderBatch(0L, 2);

        assertThat(batch.checked()).isEqualTo(2);
        assertThat(batch.lastSeenID()).isEqualTo(9L);
        assertThat(batch.exhausted()).isFalse();
        assertThat(batch.issues()).singleElement()
                .extracting(PaymentReconciliationService.LocalReconciliationIssue::paymentOrderId)
                .isEqualTo(5L);
    }

    @Test
    @DisplayName("ACTIVE agreement batch ends on an empty page without moving the cursor")
    void activeAgreementBatchHandlesEmptyPage() {
        given(billingAgreementRepository.findLocalReconciliationCandidates(
                eq(BillingAgreementStatus.ACTIVE),
                eq(17L),
                any()))
                .willReturn(List.of());

        PaymentReconciliationTransactionService.LocalReconciliationBatch batch =
                service.reconcileActiveAgreementBatch(17L, 100, LocalDate.of(2026, 7, 16));

        assertThat(batch.checked()).isZero();
        assertThat(batch.lastSeenID()).isEqualTo(17L);
        assertThat(batch.exhausted()).isTrue();
        assertThat(batch.issues()).isEmpty();
    }

    @Test
    @DisplayName("only exact provider, order, status, amount, currency, and transaction evidence passes")
    void exactDoneEvidence_passes() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-exact",
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isTrue();
    }

    @Test
    @DisplayName("currency mismatch is a strict Incident-only result")
    void currencyMismatch_isIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-exact",
                "DONE",
                claim.amount(),
                "USD",
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isFalse();
        assertThat(assessment.issueType())
                .isEqualTo(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED);
        assertThat(assessment.failureCode()).isEqualTo("CURRENCY_MISMATCH");
    }

    @Test
    @DisplayName("provider order and status conflicts all fail the strict gate")
    void providerOrderAndStatusMismatches_areIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION, null);
        List<ProviderPaymentLookupResult> mismatches = List.of(
                ProviderPaymentLookupResult.found(
                        PaymentProviderType.MOCK,
                        claim.orderID(),
                        "tx-exact",
                        "DONE",
                        claim.amount(),
                        claim.currency(),
                        "{}"),
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        "OTHER-ORDER",
                        "tx-exact",
                        "DONE",
                        claim.amount(),
                        claim.currency(),
                        "{}"),
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        claim.orderID(),
                        "tx-exact",
                        "CANCELED",
                        claim.amount(),
                        claim.currency(),
                        "{}"),
                ProviderPaymentLookupResult.found(
                        claim.provider(),
                        claim.orderID(),
                        " ",
                        "DONE",
                        claim.amount(),
                        claim.currency(),
                        "{}"));

        assertThat(mismatches)
                .map(result -> service.assessProviderEvidence(claim, result))
                .extracting(EvidenceAssessment::failureCode)
                .containsExactly(
                        "PROVIDER_MISMATCH",
                        "ORDER_ID_MISMATCH",
                        "PROVIDER_STATUS_MISMATCH",
                        "PROVIDER_TRANSACTION_MISSING");
    }

    @Test
    @DisplayName("persisted provider success requires the same authoritative transaction ID")
    void providerSucceededTransactionMismatch_isIncidentOnly() {
        ProviderLookupClaim claim = claim(PaymentOrderStatus.PROVIDER_SUCCEEDED, "tx-retained");
        ProviderPaymentLookupResult result = ProviderPaymentLookupResult.found(
                claim.provider(),
                claim.orderID(),
                "tx-conflict",
                "DONE",
                claim.amount(),
                claim.currency(),
                "{}");

        EvidenceAssessment assessment = service.assessProviderEvidence(claim, result);

        assertThat(assessment.exactDone()).isFalse();
        assertThat(assessment.failureCode()).isEqualTo("PROVIDER_TRANSACTION_MISMATCH");
    }

    private ProviderLookupClaim claim(
            PaymentOrderStatus status,
            String providerTransactionID) {
        return new ProviderLookupClaim(
                1L,
                2L,
                3L,
                4L,
                "ORDER-1",
                "RENEWAL:3:4:2026-08-17",
                PaymentProviderType.TOSS_BILLING,
                PaymentPurpose.RENEWAL,
                status,
                BigDecimal.valueOf(9900),
                "KRW",
                providerTransactionID,
                true,
                null);
    }

    private PaymentOrder paymentOrder(Long id) {
        User user = mock(User.class);
        given(user.getId()).willReturn(7L);
        PaymentOrder order = mock(PaymentOrder.class);
        given(order.getId()).willReturn(id);
        given(order.getUser()).willReturn(user);
        given(order.getBillingAgreement()).willReturn(null);
        given(order.getOrderId()).willReturn("ORDER-" + id);
        given(order.getProvider()).willReturn(PaymentProviderType.TOSS_BILLING);
        given(order.getPurpose()).willReturn(PaymentPurpose.RENEWAL);
        given(order.getStatus()).willReturn(PaymentOrderStatus.DONE);
        given(order.getAmount()).willReturn(BigDecimal.valueOf(9900));
        return order;
    }
}
