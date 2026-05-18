package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BillingAgreement entity tests")
class BillingAgreementTest {

    @Test
    @DisplayName("defaults to READY with zero failures")
    void defaults() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.READY);
        assertThat(agreement.getFailureCount()).isZero();
    }

    @Test
    @DisplayName("activate stores protected billing metadata only")
    void activate() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();

        agreement.activate(
                "v1:nonce:ciphertext",
                "abc123fingerprint",
                "카드",
                "1234-****-****-5678",
                LocalDate.of(2026, 6, 17));

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(agreement.getBillingKeyCiphertext()).isEqualTo("v1:nonce:ciphertext");
        assertThat(agreement.getBillingKeyCiphertext()).doesNotContain("billing_");
        assertThat(agreement.getBillingKeyFingerprint()).isEqualTo("abc123fingerprint");
        assertThat(agreement.getNextBillingAt()).isEqualTo(LocalDate.of(2026, 6, 17));
    }

    @Test
    @DisplayName("activate requires ciphertext and fingerprint")
    void activateRequiresProtectedValues() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();

        assertThatThrownBy(() -> agreement.activate(
                "",
                "fingerprint",
                "카드",
                "masked",
                LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("charge success resets failure count and updates next billing date")
    void recordSuccessfulCharge() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();
        agreement.activate("v1:nonce:ciphertext", "fingerprint", "카드", "masked", LocalDate.now());
        agreement.recordFailedCharge();

        agreement.recordSuccessfulCharge(LocalDate.of(2026, 7, 17));

        assertThat(agreement.getFailureCount()).isZero();
        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.ACTIVE);
        assertThat(agreement.getLastChargedAt()).isNotNull();
        assertThat(agreement.getNextBillingAt()).isEqualTo(LocalDate.of(2026, 7, 17));
    }

    @Test
    @DisplayName("cancel marks agreement non-chargeable")
    void cancel() {
        BillingAgreement agreement = BillingAgreement.builder()
                .provider(PaymentProviderType.TOSS_BILLING)
                .providerCustomerKey("ats_billing_random")
                .build();
        agreement.activate("v1:nonce:ciphertext", "fingerprint", "카드", "masked", LocalDate.now());

        agreement.cancel();

        assertThat(agreement.getStatus()).isEqualTo(BillingAgreementStatus.CANCELLED);
        assertThat(agreement.getCancelledAt()).isNotNull();
        assertThat(agreement.isChargeableOn(LocalDate.now())).isFalse();
    }
}
