package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.PaymentReconciliationService;

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
                        provider.issues()));
    }

    public record LocalLedger(
            int checkedOrders,
            int checkedBillingAgreements,
            int doneOrdersWithoutPayment,
            int activeAgreementsWithoutSubscription,
            boolean hasMismatch,
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
            List<PaymentReconciliationService.ProviderReconciliationIssue> issues) {
    }
}
