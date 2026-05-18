package com.atstudio.atstudio.service.payment.billing;

import com.atstudio.atstudio.config.PaymentProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BillingKeyCrypto unit tests")
class BillingKeyCryptoTest {

    private static final String SECRET = "local-test-billing-key-secret-32-bytes-minimum";

    @Test
    @DisplayName("encrypt/decrypt roundtrip hides raw billing key")
    void encryptDecryptRoundtrip() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties(SECRET));
        String rawBillingKey = "billing_test_1234567890";

        BillingKeyCrypto.ProtectedBillingKey protectedKey = crypto.encrypt(rawBillingKey);

        assertThat(protectedKey.ciphertext()).startsWith("v1:");
        assertThat(protectedKey.ciphertext()).doesNotContain(rawBillingKey);
        assertThat(crypto.decrypt(protectedKey.ciphertext())).isEqualTo(rawBillingKey);
    }

    @Test
    @DisplayName("fingerprint is deterministic and non-reversible")
    void fingerprintDeterministic() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties(SECRET));

        String first = crypto.fingerprint("billing_test_123");
        String second = crypto.fingerprint("billing_test_123");
        String different = crypto.fingerprint("billing_test_456");

        assertThat(first).hasSize(64);
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(different);
        assertThat(first).doesNotContain("billing_test_123");
    }

    @Test
    @DisplayName("missing encryption secret blocks crypto use")
    void missingSecretThrows() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties(""));

        assertThatThrownBy(() -> crypto.encrypt("billing_test_123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_BILLING_KEY_ENCRYPTION_SECRET");
    }

    private PaymentProperties properties(String secret) {
        PaymentProperties properties = new PaymentProperties();
        properties.getBilling().setEncryptionSecret(secret);
        return properties;
    }
}
