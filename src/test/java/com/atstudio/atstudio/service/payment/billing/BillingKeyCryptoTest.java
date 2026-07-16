package com.atstudio.atstudio.service.payment.billing;

import com.atstudio.atstudio.config.PaymentProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BillingKeyCrypto unit tests")
class BillingKeyCryptoTest {

    private static final String LEGACY_SECRET = "legacy-test-billing-key-secret-32-bytes-minimum";
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
    @DisplayName("legacy v1 ciphertext remains decryptable")
    void legacyCiphertextRemainsDecryptable() throws Exception {
        BillingKeyCrypto crypto = new BillingKeyCrypto(properties("key-a", key("key-a", KEY_A_SECRET)));
        String rawBillingKey = "billing_legacy_123";

        String legacyEnvelope = legacyEnvelope(rawBillingKey, LEGACY_SECRET);

        assertThat(legacyEnvelope).startsWith("v1:");
        assertThat(crypto.decrypt(legacyEnvelope)).isEqualTo(rawBillingKey);
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
    @DisplayName("startup validation checks legacy secret, active ID, and every key ring entry")
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
        properties.getBilling().setEncryptionSecret(LEGACY_SECRET);
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

    private String legacyEnvelope(String rawBillingKey, String secret) throws Exception {
        byte[] nonce = new byte[12];
        for (int index = 0; index < nonce.length; index++) {
            nonce[index] = (byte) (index + 1);
        }
        byte[] encryptionKey = MessageDigest.getInstance("SHA-256").digest(
                ("ATStudio:encryption:" + secret).getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(encryptionKey, "AES"),
                new GCMParameterSpec(128, nonce));
        cipher.updateAAD("v1".getBytes(StandardCharsets.UTF_8));
        byte[] ciphertext = cipher.doFinal(rawBillingKey.getBytes(StandardCharsets.UTF_8));
        return "v1:"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce)
                + ":"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
    }
}
