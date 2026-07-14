package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.ProviderFailureDisposition;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.RenewalAction;
import com.atstudio.atstudio.service.PaymentCommandTransactionService.RenewalClaim;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurringRenewalService unit tests")
class RecurringRenewalServiceTest {

    @Mock BillingAgreementRepository billingAgreementRepository;
    @Mock PaymentCommandTransactionService paymentCommandTransactions;
    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock EmailService emailService;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    RecurringRenewalService service;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS_BILLING);
        service = new RecurringRenewalService(
                billingAgreementRepository,
                paymentCommandTransactions,
                billingKeyCrypto,
                emailService,
                List.of(recurringPaymentProvider));
    }

    @Test
    @DisplayName("due agreements are scanned with bounded ascending keyset pages")
    void processDueRenewals_usesBoundedKeysetPages() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        List<Long> firstPage = LongStream.rangeClosed(1, 100).boxed().toList();
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                0L,
                PageRequest.of(0, 100))).willReturn(firstPage);
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                100L,
                PageRequest.of(0, 100))).willReturn(List.of(101L));
        given(paymentCommandTransactions.claimRenewal(any(), eq(today), any()))
                .willAnswer(invocation -> skippedClaim(invocation.getArgument(0)));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.skipped()).isEqualTo(101);
        verify(paymentCommandTransactions, org.mockito.Mockito.times(101))
                .claimRenewal(any(), eq(today), any());
    }

    @Test
    @DisplayName("provider call uses the persisted renewal attempt key and then finalizes locally")
    void processDueRenewals_successfulClaimChargesAndFinalizes() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        givenSingleDueAgreement(7L, today);
        given(paymentCommandTransactions.claimRenewal(eq(7L), eq(today), any()))
                .willReturn(callProviderClaim());
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.success("tx_renewal", "CARD", "1234", "{}"));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.succeeded()).isEqualTo(1);
        ArgumentCaptor<BillingChargeCommand> chargeCaptor = ArgumentCaptor.forClass(BillingChargeCommand.class);
        verify(recurringPaymentProvider).charge(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().idempotencyKey()).isEqualTo("renewal-ORDER-7-attempt-1");
        verify(paymentCommandTransactions).recordProviderSuccess(7L, "ORDER-7", "tx_renewal", "{}", null, null);
        verify(paymentCommandTransactions).finalizeRenewal(7L, "ORDER-7");
    }

    @Test
    @DisplayName("stale processing claim rejection does not blind replay the provider charge")
    void processDueRenewals_staleProcessingNeverBlindReplays() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        givenSingleDueAgreement(7L, today);
        given(paymentCommandTransactions.claimRenewal(eq(7L), eq(today), any()))
                .willThrow(new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.failed()).isEqualTo(1);
        verify(recurringPaymentProvider, never()).charge(any());
        verify(paymentCommandTransactions, never()).recordProviderSuccess(any(), any(), any(), any(), any(), any());
        verify(paymentCommandTransactions, never()).finalizeRenewal(any(), any());
    }

    @Test
    @DisplayName("contract-level catch continues after finalize-only local failure")
    void processDueRenewals_continuesAfterFinalizeOnlyFailure() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                0L,
                PageRequest.of(0, 100))).willReturn(List.of(7L, 8L));
        given(paymentCommandTransactions.claimRenewal(eq(7L), eq(today), any()))
                .willReturn(finalizeOnlyClaim(7L, "ORDER-7"));
        given(paymentCommandTransactions.claimRenewal(eq(8L), eq(today), any()))
                .willReturn(skippedClaim(8L));
        org.mockito.Mockito.doThrow(new IllegalStateException("local finalize failed"))
                .when(paymentCommandTransactions)
                .finalizeRenewal(7L, "ORDER-7");

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        verify(paymentCommandTransactions).claimRenewal(eq(8L), eq(today), any());
        verify(recurringPaymentProvider, never()).charge(any());
    }

    @Test
    @DisplayName("null provider result is persisted as pending confirmation and the batch continues")
    void processDueRenewals_nullProviderResultBecomesPendingAndContinues() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        givenTwoDueAgreements(today);
        given(paymentCommandTransactions.claimRenewal(eq(7L), eq(today), any()))
                .willReturn(callProviderClaim());
        given(paymentCommandTransactions.claimRenewal(eq(8L), eq(today), any()))
                .willReturn(skippedClaim(8L));
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any())).willReturn(null);

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.failed()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        verify(paymentCommandTransactions).recordRenewalProviderFailure(
                7L,
                "ORDER-7",
                "PROVIDER_NULL_RESULT",
                "Provider returned null charge result.",
                ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                today);
        verify(paymentCommandTransactions).claimRenewal(eq(8L), eq(today), any());
    }

    @Test
    @DisplayName("provider success with blank transaction ID is persisted as pending confirmation")
    void processDueRenewals_blankSuccessTransactionBecomesPending() {
        LocalDate today = LocalDate.of(2026, 5, 17);
        givenSingleDueAgreement(7L, today);
        given(paymentCommandTransactions.claimRenewal(eq(7L), eq(today), any()))
                .willReturn(callProviderClaim());
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("billing_raw_key");
        given(recurringPaymentProvider.charge(any()))
                .willReturn(BillingChargeResult.success(" ", "CARD", "1234", "{}"));

        RecurringRenewalService.RenewalRunResult result = service.processDueRenewals(today);

        assertThat(result.failed()).isEqualTo(1);
        verify(paymentCommandTransactions).recordRenewalProviderFailure(
                7L,
                "ORDER-7",
                "PROVIDER_SUCCESS_MISSING_TRANSACTION_ID",
                "Provider success result did not include a transaction ID.",
                ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                today);
        verify(paymentCommandTransactions, never()).recordProviderSuccess(any(), any(), any(), any(), any(), any());
    }

    private void givenSingleDueAgreement(Long agreementID, LocalDate today) {
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                0L,
                PageRequest.of(0, 100))).willReturn(List.of(agreementID));
    }

    private void givenTwoDueAgreements(LocalDate today) {
        given(billingAgreementRepository.findDueRenewalCandidateIDs(
                BillingAgreementStatus.ACTIVE,
                today,
                0L,
                PageRequest.of(0, 100))).willReturn(List.of(7L, 8L));
    }

    private RenewalClaim skippedClaim(Long agreementID) {
        return new RenewalClaim(
                RenewalAction.SKIPPED,
                agreementID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
    }

    private RenewalClaim callProviderClaim() {
        return new RenewalClaim(
                RenewalAction.CALL_PROVIDER,
                7L,
                "ORDER-7",
                null,
                "encrypted-key",
                "customer-7",
                "Basic recurring renewal",
                BigDecimal.valueOf(9900),
                "user@test.com",
                "user",
                "renewal-ORDER-7-attempt-1",
                null,
                false);
    }

    private RenewalClaim finalizeOnlyClaim(Long agreementID, String orderID) {
        return new RenewalClaim(
                RenewalAction.FINALIZE_ONLY,
                agreementID,
                orderID,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
    }
}
