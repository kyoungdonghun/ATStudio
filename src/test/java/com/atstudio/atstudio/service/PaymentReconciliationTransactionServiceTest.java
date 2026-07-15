package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentReconciliationTransactionService evidence gate tests")
class PaymentReconciliationTransactionServiceTest {

    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PaymentCommandTransactionService paymentCommandTransactionService;

    PaymentReconciliationTransactionService service;

    @BeforeEach
    void setUp() {
        service = new PaymentReconciliationTransactionService(
                paymentOrderRepository,
                billingAgreementRepository,
                subscriptionPaymentRepository,
                userSubscriptionRepository,
                paymentCommandTransactionService);
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
}
