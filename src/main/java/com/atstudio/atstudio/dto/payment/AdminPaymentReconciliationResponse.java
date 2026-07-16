package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.service.PaymentReconciliationService;

import java.math.BigDecimal;
import java.util.List;

public record AdminPaymentReconciliationResponse(
        LocalLedger localLedger,
        ProviderLedger providerLedger
) {

    public static AdminPaymentReconciliationResponse from(
            PaymentReconciliationService.ReconciliationResult local,
            PaymentReconciliationService.ProviderReconciliationResult provider) {
        return new AdminPaymentReconciliationResponse(
                new LocalLedger(
                        local.checkedOrders(),
                        local.checkedBillingAgreements(),
                        local.doneOrdersWithoutPayment(),
                        local.activeAgreementsWithoutSubscription(),
                        local.hasMismatch(),
                        local.totalIssues(),
                        local.issueDetailsTruncated(),
                        local.issues()),
                new ProviderLedger(
                        provider.checkedOrders(),
                        provider.skippedOrders(),
                        provider.providerNotFound(),
                        provider.lookupFailures(),
                        provider.providerDoneWithoutLocalFinalization(),
                        provider.localDoneButProviderNotDone(),
                        provider.amountMismatches(),
                        provider.hasMismatch(),
                        provider.totalIssues(),
                        provider.issueDetailsTruncated(),
                        provider.issues().stream()
                                .map(ProviderIssue::from)
                                .toList()));
    }

    public record LocalLedger(
            int checkedOrders,
            int checkedBillingAgreements,
            int doneOrdersWithoutPayment,
            int activeAgreementsWithoutSubscription,
            boolean hasMismatch,
            int totalIssues,
            boolean issueDetailsTruncated,
            List<PaymentReconciliationService.LocalReconciliationIssue> issues) {
    }

    public record ProviderLedger(
            int checkedOrders,
            int skippedOrders,
            int providerNotFound,
            int lookupFailures,
            int providerDoneWithoutLocalFinalization,
            int localDoneButProviderNotDone,
            int amountMismatches,
            boolean hasMismatch,
            int totalIssues,
            boolean issueDetailsTruncated,
            List<ProviderIssue> issues) {
    }

    public record ProviderIssue(
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
            String providerReference,
            String failureCode,
            String failureMessage) {

        private static ProviderIssue from(
                PaymentReconciliationService.ProviderReconciliationIssue issue) {
            return new ProviderIssue(
                    issue.issueType(),
                    issue.paymentOrderId(),
                    issue.userId(),
                    issue.billingAgreementId(),
                    issue.orderId(),
                    issue.provider(),
                    issue.purpose(),
                    issue.localStatus(),
                    issue.providerStatus(),
                    issue.localAmount(),
                    issue.providerAmount(),
                    issue.localCurrency(),
                    issue.providerCurrency(),
                    ProviderSupportReference.from(issue.providerTransactionId()),
                    issue.failureCode(),
                    issue.failureMessage());
        }
    }
}
