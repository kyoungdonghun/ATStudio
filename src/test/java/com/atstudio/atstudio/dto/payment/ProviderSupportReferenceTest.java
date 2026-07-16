package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.service.payment.ProviderSupportReference;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderSupportReferenceTest {

    @Test
    void createsStableNonRawSupportReference() {
        String raw = "tgen_20260716_secret_payment_key";

        String reference = ProviderSupportReference.from(raw);

        assertThat(reference)
                .startsWith("REF-")
                .hasSize(16)
                .doesNotContain(raw)
                .isEqualTo(ProviderSupportReference.from(raw));
    }

    @Test
    void returnsNullForMissingIdentifier() {
        assertThat(ProviderSupportReference.from(null)).isNull();
        assertThat(ProviderSupportReference.from("  ")).isNull();
    }

    @Test
    void keepsSupportReferencesStableWithoutDoubleHashing() {
        String reference = ProviderSupportReference.from("provider-payment-key");

        assertThat(ProviderSupportReference.from(reference)).isEqualTo(reference);
    }

    @Test
    void removesLegacyProviderIdentifierFragmentsFromFreeText() {
        String sanitized = ProviderSupportReference.sanitizeFreeText(
                "providerStatus=DONE, transactionId=pay_...cdef. paymentKey=raw-payment-key");

        assertThat(sanitized)
                .containsPattern("transactionId=REF-[0-9A-F]{12}")
                .containsPattern("paymentKey=REF-[0-9A-F]{12}")
                .doesNotContain("pay_")
                .doesNotContain("cdef")
                .doesNotContain("raw-payment-key");
    }

    @Test
    void sanitizesSupportedLabelsWithColonEqualsAndWhitespace() {
        String transactionId = "pay_0123456789_abcdef";
        String paymentKey = "payment-key-raw-prefix-suffix";
        String orderId = "ORDER-RAW-PREFIX-SUFFIX";

        String sanitized = ProviderSupportReference.sanitizeFreeText(
                "transactionId : " + transactionId
                        + ", paymentKey=  " + paymentKey
                        + "; orderId  :" + orderId);

        assertThat(sanitized)
                .containsPattern("transactionId\\s*:\\s*REF-[0-9A-F]{12}")
                .containsPattern("paymentKey=\\s*REF-[0-9A-F]{12}")
                .containsPattern("orderId\\s*:\\s*REF-[0-9A-F]{12}")
                .doesNotContain(transactionId)
                .doesNotContain("pay_0123")
                .doesNotContain("abcdef")
                .doesNotContain(paymentKey)
                .doesNotContain(orderId)
                .doesNotContain("RAW-PREFIX")
                .doesNotContain("SUFFIX");
    }
}
