package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
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
public class BillingAgreementCleanupProviderExecutor {

    private static final String ALREADY_REMOVED_BILLING_KEY = "ALREADY_REMOVED_BILLING_KEY";
    private static final String PROVIDER_NOT_CONFIGURED = "BILLING_PROVIDER_NOT_CONFIGURED";
    private static final String DECRYPTION_FAILED = "BILLING_KEY_DECRYPTION_FAILED";
    private static final String DELETE_EXCEPTION = "BILLING_KEY_DELETE_EXCEPTION";
    private static final String EMPTY_PROVIDER_RESULT = "BILLING_KEY_DELETE_EMPTY_RESULT";

    private final BillingKeyCrypto billingKeyCrypto;
    private final Map<PaymentProviderType, RecurringPaymentProvider> recurringProviders;

    public BillingAgreementCleanupProviderExecutor(
            BillingKeyCrypto billingKeyCrypto,
            List<RecurringPaymentProvider> recurringProviders) {
        this.billingKeyCrypto = billingKeyCrypto;
        this.recurringProviders = recurringProviders.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RecurringPaymentProvider::getProviderType,
                        Function.identity()));
    }

    @Transactional(propagation = Propagation.NEVER)
    public CleanupProviderResult deleteBillingKey(
            PaymentProviderType providerType,
            String billingKeyCiphertext) {
        RecurringPaymentProvider provider = recurringProviders.get(providerType);
        if (provider == null) {
            return CleanupProviderResult.failed(
                    PROVIDER_NOT_CONFIGURED,
                    "Recurring payment provider is not configured.");
        }

        String billingKey;
        try {
            billingKey = billingKeyCrypto.decrypt(billingKeyCiphertext);
        } catch (RuntimeException exception) {
            return CleanupProviderResult.failed(
                    DECRYPTION_FAILED,
                    exception.getClass().getSimpleName());
        }

        BillingAgreementCancelResult cancelResult;
        try {
            cancelResult = provider.cancelAgreement(new BillingAgreementCancelCommand(billingKey));
        } catch (RuntimeException exception) {
            return CleanupProviderResult.pending(
                    DELETE_EXCEPTION,
                    exception.getClass().getSimpleName());
        }

        if (cancelResult == null) {
            return CleanupProviderResult.pending(EMPTY_PROVIDER_RESULT, "EmptyProviderResult");
        }
        if (cancelResult.success()
                || ALREADY_REMOVED_BILLING_KEY.equals(cancelResult.failureCode())) {
            return CleanupProviderResult.succeeded();
        }
        return CleanupProviderResult.failed(
                cancelResult.failureCode(),
                cancelResult.failureMessage());
    }

    public enum CleanupDisposition {
        SUCCEEDED,
        FAILED,
        PENDING_PROVIDER_CONFIRMATION
    }

    public record CleanupProviderResult(
            CleanupDisposition disposition,
            String failureCode,
            String failureMessage) {

        public static CleanupProviderResult succeeded() {
            return new CleanupProviderResult(CleanupDisposition.SUCCEEDED, null, null);
        }

        public static CleanupProviderResult failed(String failureCode, String failureMessage) {
            return new CleanupProviderResult(
                    CleanupDisposition.FAILED,
                    failureCode,
                    failureMessage);
        }

        public static CleanupProviderResult pending(String failureCode, String failureMessage) {
            return new CleanupProviderResult(
                    CleanupDisposition.PENDING_PROVIDER_CONFIRMATION,
                    failureCode,
                    failureMessage);
        }
    }
}
