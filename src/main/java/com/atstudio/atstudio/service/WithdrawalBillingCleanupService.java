package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WithdrawalBillingCleanupService {

    private static final String PROVIDER_NOT_CONFIGURED = "BILLING_PROVIDER_NOT_CONFIGURED";
    private static final String DELETE_EXCEPTION = "BILLING_KEY_DELETE_EXCEPTION";
    private static final String ALREADY_REMOVED_BILLING_KEY = "ALREADY_REMOVED_BILLING_KEY";

    private final BillingAgreementRepository billingAgreementRepository;
    private final BillingKeyCrypto billingKeyCrypto;
    private final PaymentReconciliationIncidentService incidentService;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public WithdrawalBillingCleanupService(
            BillingAgreementRepository billingAgreementRepository,
            BillingKeyCrypto billingKeyCrypto,
            PaymentReconciliationIncidentService incidentService,
            List<RecurringPaymentProvider> recurringProviders) {
        this.billingAgreementRepository = billingAgreementRepository;
        this.billingKeyCrypto = billingKeyCrypto;
        this.incidentService = incidentService;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(RecurringPaymentProvider::getProviderType, Function.identity()));
    }

    public List<Long> findRetryCandidateIDs() {
        return billingAgreementRepository.findWithdrawalCleanupCandidateIDs();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CleanupOutcome cleanup(Long billingAgreementID) {
        BillingAgreement billingAgreement = billingAgreementRepository.findById(billingAgreementID).orElse(null);
        if (!isEligible(billingAgreement)) {
            return CleanupOutcome.SKIPPED;
        }

        RecurringPaymentProvider provider = recurringProviders.get(billingAgreement.getProvider());
        if (provider == null) {
            incidentService.recordBillingCleanupFailure(
                    billingAgreement,
                    PROVIDER_NOT_CONFIGURED,
                    "Recurring payment provider is not configured.");
            return CleanupOutcome.FAILED;
        }

        BillingAgreementCancelResult cancelResult;
        try {
            String billingKey = billingKeyCrypto.decrypt(billingAgreement.getBillingKeyCiphertext());
            cancelResult = provider.cancelAgreement(new BillingAgreementCancelCommand(billingKey));
        } catch (RuntimeException exception) {
            incidentService.recordBillingCleanupFailure(
                    billingAgreement,
                    DELETE_EXCEPTION,
                    exception.getClass().getSimpleName());
            return CleanupOutcome.FAILED;
        }

        if (cancelResult == null) {
            incidentService.recordBillingCleanupFailure(
                    billingAgreement,
                    DELETE_EXCEPTION,
                    "EmptyProviderResult");
            return CleanupOutcome.FAILED;
        }

        if (!isProviderCleanupComplete(cancelResult)) {
            incidentService.recordBillingCleanupFailure(
                    billingAgreement,
                    cancelResult.failureCode(),
                    cancelResult.failureMessage());
            return CleanupOutcome.FAILED;
        }

        billingAgreement.clearIssuedKey();
        incidentService.resolveBillingCleanupIncident(billingAgreement);
        return CleanupOutcome.SUCCEEDED;
    }

    private boolean isProviderCleanupComplete(BillingAgreementCancelResult cancelResult) {
        return cancelResult.success()
                || ALREADY_REMOVED_BILLING_KEY.equals(cancelResult.failureCode());
    }

    private boolean isEligible(BillingAgreement billingAgreement) {
        return billingAgreement != null
                && billingAgreement.getUser().isDeleted()
                && billingAgreement.getStatus() == BillingAgreementStatus.CANCELLED
                && billingAgreement.getBillingKeyCiphertext() != null
                && !billingAgreement.getBillingKeyCiphertext().isBlank();
    }

    public enum CleanupOutcome {
        SUCCEEDED,
        FAILED,
        SKIPPED
    }
}
