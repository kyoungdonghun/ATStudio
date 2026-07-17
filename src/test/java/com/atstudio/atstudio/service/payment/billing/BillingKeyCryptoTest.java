package com.atstudio.atstudio.service.payment.billing;

import com.atstudio.atstudio.config.PaymentProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@DisplayName("BillingKeyCrypto unit tests")
class BillingKeyCryptoTest {

    private static final String KEY_A_SECRET = "rotation-key-a-secret-material-32-bytes-minimum";
    private static final String KEY_B_SECRET = "rotation-key-b-secret-material-32-bytes-minimum";

    @Test
    @DisplayName("v2 key-ID envelope roundtrip hides raw billing key")
    void keyIDEnvelopeRoundtrip() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));
        String rawBillingKey = "billing_test_1234567890";

        BillingKeyCrypto.ProtectedBillingKey protectedKey = crypto.encrypt(rawBillingKey);

        assertThat(protectedKey.ciphertext()).startsWith("v2:key-a:");
        assertThat(protectedKey.ciphertext()).doesNotContain(rawBillingKey);
        assertThat(crypto.decrypt(protectedKey.ciphertext())).isEqualTo(rawBillingKey);
    }

    @Test
    @DisplayName("fingerprint is deterministic and non-reversible")
    void fingerprintDeterministic() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        String first = crypto.fingerprint("billing_test_123");
        String second = crypto.fingerprint("billing_test_123");
        String different = crypto.fingerprint("billing_test_456");

        assertThat(first).hasSize(64);
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(different);
        assertThat(first).doesNotContain("billing_test_123");
    }

    @Test
    @DisplayName("legacy v1 ciphertext is rejected")
    void legacyCiphertextIsRejected() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        assertThatThrownBy(() -> crypto.decrypt("v1:nonce:ciphertext"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported billing key ciphertext format")
                .hasMessageNotContaining(KEY_A_SECRET);
    }

    @Test
    @DisplayName("active key rotation decrypts prior v2 ciphertext and writes with the new key ID")
    void activeKeyRotationIsCompatible() {
        PaymentProperties.EncryptionKey keyA = key("key-a", KEY_A_SECRET);
        PaymentProperties.EncryptionKey keyB = key("key-b", KEY_B_SECRET);
        BillingKeyCrypto beforeRotation = new BillingKeyCrypto(properties("key-a", keyA, keyB));
        BillingKeyCrypto afterRotation = new BillingKeyCrypto(properties("key-b", keyA, keyB));
        String oldCiphertext = beforeRotation.encrypt("billing_before_rotation").ciphertext();

        assertThat(afterRotation.decrypt(oldCiphertext)).isEqualTo("billing_before_rotation");
        assertThat(afterRotation.encrypt("billing_after_rotation").ciphertext())
                .startsWith("v2:key-b:");
    }

    @Test
    @DisplayName("unknown or removed key ID fails without exposing key material")
    void removedKeyFailsClosed() {
        BillingKeyCrypto writer = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));
        BillingKeyCrypto reader = new BillingKeyCrypto(properties("key-b", key("key-b", KEY_B_SECRET)));
        String ciphertext = writer.encrypt("billing_removed_key").ciphertext();

        assertThatThrownBy(() -> reader.decrypt(ciphertext))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable key ID")
                .hasMessageNotContaining(KEY_A_SECRET)
                .hasMessageNotContaining(KEY_B_SECRET)
                .hasMessageNotContaining("billing_removed_key");
    }

    @Test
    @DisplayName("startup validation checks the active ID and every V2 key ring entry")
    void startupValidationChecksEntireKeyRing() {
        PaymentProperties valid = properties(
                "key-b",
                key("key-a", KEY_A_SECRET),
                key("key-b", KEY_B_SECRET));

        assertThatCode(() -> BillingKeyCrypto.validateConfiguration(valid)).doesNotThrowAnyException();

        PaymentProperties missingActive = properties("key-missing", key("key-a", KEY_A_SECRET));
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(missingActive))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not present")
                .hasMessageNotContaining(KEY_A_SECRET);

        PaymentProperties placeholder = properties(
                "key-a",
                key("key-a", "REPLACE_WITH_REAL_SECRET"));
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(placeholder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured")
                .hasMessageNotContaining("REPLACE_WITH_REAL_SECRET");
    }

    @Test
    @DisplayName("missing active key configuration blocks new encryption")
    void missingActiveKeyThrows() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("", key("key-a", KEY_A_SECRET)));

        assertThatThrownBy(() -> crypto.encrypt("billing_test_123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_BILLING_KEY_ACTIVE_KEY_ID")
                .hasMessageNotContaining(KEY_A_SECRET);
    }

    @Test
    @DisplayName("blank plaintext, ciphertext, and fingerprint inputs fail closed")
    void blankInputsFailClosed() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        assertThatThrownBy(() -> crypto.encrypt(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Billing key is required.");
        assertThatThrownBy(() -> crypto.decrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Billing key ciphertext is required.");
        assertThatThrownBy(() -> crypto.fingerprint(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Billing key is required.");
    }

    @Test
    @DisplayName("malformed v2 envelopes and invalid nonces are rejected")
    void malformedEnvelopeFailsClosed() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));
        String shortNonce = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[11]);

        assertThatThrownBy(() -> crypto.decrypt("v2:key-a:incomplete"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported billing key ciphertext format");
        assertThatThrownBy(() -> crypto.decrypt("v2:key-a:" + shortNonce + ":AA"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to decrypt billing key.")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("authenticated encryption rejects a structurally valid but forged ciphertext")
    void forgedCiphertextFailsAuthentication() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));
        String[] parts = crypto.encrypt("billing_authentic").ciphertext().split(":", 4);
        String forgedCiphertext = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[16]);
        String forgedEnvelope = String.join(":", parts[0], parts[1], parts[2], forgedCiphertext);

        assertThatThrownBy(() -> crypto.decrypt(forgedEnvelope))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to decrypt billing key.")
                .hasMessageNotContaining(KEY_A_SECRET)
                .hasMessageNotContaining("billing_authentic");
    }

    @Test
    @DisplayName("missing and malformed key-ring entries block cryptographic use")
    void malformedKeyRingFailsClosed() {
        PaymentProperties unavailableActive = properties("key-b", key("key-a", KEY_A_SECRET));
        assertThatThrownBy(() -> new BillingKeyCrypto(unavailableActive).encrypt("billing_test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not present");

        PaymentProperties emptyRing = new PaymentProperties();
        emptyRing.getBilling().setActiveKeyId("key-a");
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(emptyRing))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key ring is not configured");

        PaymentProperties nullEntry = new PaymentProperties();
        nullEntry.getBilling().setActiveKeyId("key-a");
        nullEntry.getBilling().setEncryptionKeys(Collections.singletonList(null));
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(nullEntry))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entry is missing");

        PaymentProperties duplicateIDs = properties(
                "key-a",
                key("key-a", KEY_A_SECRET),
                key("key-a", KEY_B_SECRET));
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(duplicateIDs))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("IDs must be unique")
                .hasMessageNotContaining(KEY_A_SECRET)
                .hasMessageNotContaining(KEY_B_SECRET);
    }

    @Test
    @DisplayName("null key IDs and secrets are rejected as missing configuration")
    void nullConfigurationValuesFailClosed() {
        PaymentProperties nullActiveID = properties(null, key("key-a", KEY_A_SECRET));
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(nullActiveID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("valid key ID")
                .hasMessageNotContaining(KEY_A_SECRET);

        PaymentProperties.EncryptionKey nullSecretKey = key("key-a", null);
        PaymentProperties nullSecret = properties("key-a", nullSecretKey);
        assertThatThrownBy(() -> BillingKeyCrypto.validateConfiguration(nullSecret))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("secret is not configured")
                .hasMessageNotContaining("key-a");
    }

    @Test
    @DisplayName("cipher provider failure is translated without exposing plaintext")
    void cipherProviderFailureFailsClosed() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        try (var ciphers = mockStatic(Cipher.class)) {
            ciphers.when(() -> Cipher.getInstance("AES/GCM/NoPadding"))
                    .thenThrow(new NoSuchAlgorithmException("AES/GCM unavailable"));

            assertThatThrownBy(() -> crypto.encrypt("billing_private"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to encrypt billing key.")
                    .hasMessageNotContaining("billing_private")
                    .hasMessageNotContaining(KEY_A_SECRET)
                    .hasCauseInstanceOf(NoSuchAlgorithmException.class);
        }
    }

    @Test
    @DisplayName("MAC provider failure is translated without exposing plaintext")
    void macProviderFailureFailsClosed() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        try (var macs = mockStatic(Mac.class)) {
            macs.when(() -> Mac.getInstance("HmacSHA256"))
                    .thenThrow(new NoSuchAlgorithmException("HMAC unavailable"));

            assertThatThrownBy(() -> crypto.fingerprint("billing_private"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to fingerprint billing key.")
                    .hasMessageNotContaining("billing_private")
                    .hasMessageNotContaining(KEY_A_SECRET)
                    .hasCauseInstanceOf(NoSuchAlgorithmException.class);
        }
    }

    @Test
    @DisplayName("digest provider failure is translated without exposing key material")
    void digestProviderFailureFailsClosed() {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));

        try (var messageDigests = mockStatic(MessageDigest.class, CALLS_REAL_METHODS)) {
            messageDigests.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("SHA-256 unavailable"));

            assertThatThrownBy(() -> crypto.encrypt("billing_private"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Failed to derive billing key material.")
                    .hasMessageNotContaining("billing_private")
                    .hasMessageNotContaining(KEY_A_SECRET)
                    .hasCauseInstanceOf(NoSuchAlgorithmException.class);
        }
    }

    @Test
    @DisplayName("Spring environment binding accepts indexed billing encryption key entries")
    void environmentBindingAcceptsIndexedKeyRing() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        Map.of(
                                "APP_PAYMENT_BILLING_ENCRYPTIONKEYS_0_ID", "key-a",
                                "APP_PAYMENT_BILLING_ENCRYPTIONKEYS_0_SECRET", KEY_A_SECRET,
                                "APP_PAYMENT_BILLING_ENCRYPTIONKEYS_1_ID", "key-b",
                                "APP_PAYMENT_BILLING_ENCRYPTIONKEYS_1_SECRET", KEY_B_SECRET)));

        PaymentProperties bound = Binder.get(environment)
                .bind("app.payment", Bindable.of(PaymentProperties.class))
                .orElseThrow(() -> new AssertionError("PaymentProperties did not bind."));

        assertThat(bound.getBilling().getEncryptionKeys())
                .extracting(PaymentProperties.EncryptionKey::getId)
                .containsExactly("key-a", "key-b");
    }

    private PaymentProperties properties(
            String activeKeyID,
            PaymentProperties.EncryptionKey... encryptionKeys) {
        PaymentProperties properties = new PaymentProperties();
        properties.getBilling().setActiveKeyId(activeKeyID);
        properties.getBilling().setEncryptionKeys(List.of(encryptionKeys));
        return properties;
    }

    private PaymentProperties.EncryptionKey key(String id, String secret) {
        PaymentProperties.EncryptionKey key = new PaymentProperties.EncryptionKey();
        key.setId(id);
        key.setSecret(secret);
        return key;
    }

}
