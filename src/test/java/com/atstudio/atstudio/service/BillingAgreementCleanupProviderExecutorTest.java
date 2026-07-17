package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingAgreementCancelResult;
import com.atstudio.atstudio.service.payment.provider.recurring.PaymentProviderOutcomeUnknownException;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingAgreementCleanupProviderExecutor unit tests")
class BillingAgreementCleanupProviderExecutorTest {

    @Mock BillingKeyCrypto billingKeyCrypto;
    @Mock RecurringPaymentProvider recurringPaymentProvider;

    BillingAgreementCleanupProviderExecutor executor;

    @BeforeEach
    void setUp() {
        given(recurringPaymentProvider.getProviderType()).willReturn(PaymentProviderType.TOSS);
        executor = new BillingAgreementCleanupProviderExecutor(
                billingKeyCrypto,
                List.of(recurringPaymentProvider));
    }

    @Test
    @DisplayName("provider deletion method declares a strict NEVER transaction boundary")
    void deleteBillingKey_declaresNeverBoundary() throws Exception {
        Method method = BillingAgreementCleanupProviderExecutor.class.getMethod(
                "deleteBillingKey",
                PaymentProviderType.class,
                String.class);

        assertThat(method.getAnnotation(Transactional.class).propagation())
                .isEqualTo(Propagation.NEVER);
    }

    @Test
    @DisplayName("already-removed provider evidence converges to success")
    void deleteBillingKey_alreadyRemovedConverges() {
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willReturn(BillingAgreementCancelResult.failure(
                        "ALREADY_REMOVED_BILLING_KEY",
                        "Already removed."));

        var result = executor.deleteBillingKey(
                PaymentProviderType.TOSS,
                "encrypted-key");

        assertThat(result.disposition())
                .isEqualTo(BillingAgreementCleanupProviderExecutor.CleanupDisposition.SUCCEEDED);
    }

    @Test
    @DisplayName("decryption failure is deterministic and never invokes the provider")
    void deleteBillingKey_decryptionFailureIsDeterministic() {
        given(billingKeyCrypto.decrypt("encrypted-key"))
                .willThrow(new IllegalStateException("secret detail"));

        var result = executor.deleteBillingKey(
                PaymentProviderType.TOSS,
                "encrypted-key");

        assertThat(result.disposition())
                .isEqualTo(BillingAgreementCleanupProviderExecutor.CleanupDisposition.FAILED);
        assertThat(result.failureCode()).isEqualTo("BILLING_KEY_DECRYPTION_FAILED");
        assertThat(result.failureMessage()).isEqualTo("IllegalStateException");
        verify(recurringPaymentProvider, never()).cancelAgreement(any());
    }

    @Test
    @DisplayName("provider exception retains only safe pending evidence")
    void deleteBillingKey_providerExceptionIsPending() {
        given(billingKeyCrypto.decrypt("encrypted-key")).willReturn("raw-key");
        given(recurringPaymentProvider.cancelAgreement(any()))
                .willThrow(new PaymentProviderOutcomeUnknownException("raw transport detail"));

        var result = executor.deleteBillingKey(
                PaymentProviderType.TOSS,
                "encrypted-key");

        assertThat(result.disposition()).isEqualTo(
                BillingAgreementCleanupProviderExecutor.CleanupDisposition.PENDING_PROVIDER_CONFIRMATION);
        assertThat(result.failureMessage()).isEqualTo("PaymentProviderOutcomeUnknownException");
        assertThat(result.toString()).doesNotContain("raw-key", "raw transport detail");
    }
}
