package com.atstudio.atstudio.service.payment.billing;

import com.atstudio.atstudio.config.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class BillingKeyCrypto {

    private static final String KEY_ID_VERSION = "v2";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_NONCE_BYTES = 12;
    private static final Pattern KEY_ID_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    private final PaymentProperties paymentProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProtectedBillingKey encrypt(String rawBillingKey) {
        if (isBlank(rawBillingKey)) {
            throw new IllegalArgumentException("Billing key is required.");
        }

        KeyMaterial activeKey = activeKey();
        try {
            byte[] nonce = new byte[GCM_NONCE_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    encryptionKey(activeKey.secret()),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(keyIDAAD(activeKey.id()));
            byte[] ciphertext = cipher.doFinal(rawBillingKey.getBytes(StandardCharsets.UTF_8));

            String envelope = KEY_ID_VERSION + ":" + activeKey.id() + ":"
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce) + ":"
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
            return new ProtectedBillingKey(envelope, fingerprint(rawBillingKey, activeKey.secret()));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt billing key.", e);
        }
    }

    public String decrypt(String envelope) {
        if (isBlank(envelope)) {
            throw new IllegalArgumentException("Billing key ciphertext is required.");
        }

        if (envelope.startsWith(KEY_ID_VERSION + ":")) {
            return decryptKeyIDEnvelope(envelope);
        }
        throw new IllegalArgumentException("Unsupported billing key ciphertext format.");
    }

    public static void validateConfiguration(PaymentProperties paymentProperties) {
        PaymentProperties.Billing billing = paymentProperties.getBilling();
        String activeKeyID = requireValidKeyID(
                billing.getActiveKeyId(),
                "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID");
        Map<String, String> keyRing = validatedKeyRing(billing);
        if (!keyRing.containsKey(activeKeyID)) {
            throw new IllegalStateException("Active billing key ID is not present in the configured key ring.");
        }
    }

    private String decryptKeyIDEnvelope(String envelope) {
        String[] parts = envelope.split(":", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Unsupported billing key ciphertext format.");
        }
        String keyID = requireValidKeyID(parts[1], "billing key ciphertext key ID");
        String secret = validatedKeyRing(paymentProperties.getBilling()).get(keyID);
        if (secret == null) {
            throw new IllegalStateException("Billing key ciphertext references an unavailable key ID.");
        }
        return decrypt(parts[2], parts[3], secret, keyIDAAD(keyID));
    }

    private String decrypt(
            String encodedNonce,
            String encodedCiphertext,
            String secret,
            byte[] aad) {

        try {
            byte[] nonce = Base64.getUrlDecoder().decode(encodedNonce);
            if (nonce.length != GCM_NONCE_BYTES) {
                throw new IllegalArgumentException("Invalid billing key ciphertext nonce.");
            }
            byte[] ciphertext = Base64.getUrlDecoder().decode(encodedCiphertext);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    encryptionKey(secret),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(aad);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Failed to decrypt billing key.", e);
        }
    }

    public String fingerprint(String rawBillingKey) {
        if (isBlank(rawBillingKey)) {
            throw new IllegalArgumentException("Billing key is required.");
        }

        return fingerprint(rawBillingKey, activeKey().secret());
    }

    private String fingerprint(String rawBillingKey, String secret) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(deriveKey("fingerprint", secret), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(rawBillingKey.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to fingerprint billing key.", e);
        }
    }

    private SecretKeySpec encryptionKey(String secret) {
        return new SecretKeySpec(deriveKey("encryption", secret), "AES");
    }

    private byte[] deriveKey(String purpose, String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(
                    ("ATStudio:" + purpose + ":" + secret).getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to derive billing key material.", e);
        }
    }

    private KeyMaterial activeKey() {
        PaymentProperties.Billing billing = paymentProperties.getBilling();
        String activeKeyID = requireValidKeyID(
                billing.getActiveKeyId(),
                "PAYMENT_BILLING_KEY_ACTIVE_KEY_ID");
        String secret = validatedKeyRing(billing).get(activeKeyID);
        if (secret == null) {
            throw new IllegalStateException("Active billing key ID is not present in the configured key ring.");
        }
        return new KeyMaterial(activeKeyID, secret);
    }

    private byte[] keyIDAAD(String keyID) {
        return (KEY_ID_VERSION + ":" + keyID).getBytes(StandardCharsets.UTF_8);
    }

    private static Map<String, String> validatedKeyRing(PaymentProperties.Billing billing) {
        Map<String, String> keyRing = new LinkedHashMap<>();
        for (PaymentProperties.EncryptionKey configuredKey : billing.getEncryptionKeys()) {
            if (configuredKey == null) {
                throw new IllegalStateException("Billing key encryption key entry is missing.");
            }
            String keyID = requireValidKeyID(configuredKey.getId(), "billing key encryption key ID");
            String secret = requireConfiguredSecret(
                    configuredKey.getSecret(),
                    "billing key encryption key secret");
            if (keyRing.putIfAbsent(keyID, secret) != null) {
                throw new IllegalStateException("Billing key encryption key IDs must be unique.");
            }
        }
        if (keyRing.isEmpty()) {
            throw new IllegalStateException("Billing key encryption key ring is not configured.");
        }
        return Map.copyOf(keyRing);
    }

    private static String requireValidKeyID(String value, String propertyName) {
        if (isPlaceholder(value) || isBlank(value) || !KEY_ID_PATTERN.matcher(value.trim()).matches()) {
            throw new IllegalStateException(propertyName + " is not configured with a valid key ID.");
        }
        return value.trim();
    }

    private static String requireConfiguredSecret(String value, String propertyName) {
        if (isPlaceholder(value) || isBlank(value)) {
            throw new IllegalStateException(propertyName + " is not configured.");
        }
        return value;
    }

    private static boolean isPlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("${")
                || trimmed.startsWith("REPLACE_WITH_")
                || trimmed.equalsIgnoreCase("CHANGE_ME")
                || trimmed.equalsIgnoreCase("CHANGEME");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record KeyMaterial(String id, String secret) {
    }

    public record ProtectedBillingKey(String ciphertext, String fingerprint) {
    }
}
