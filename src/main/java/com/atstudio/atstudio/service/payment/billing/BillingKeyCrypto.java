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

@Component
@RequiredArgsConstructor
public class BillingKeyCrypto {

    private static final String VERSION = "v1";
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_NONCE_BYTES = 12;

    private final PaymentProperties paymentProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public ProtectedBillingKey encrypt(String rawBillingKey) {
        if (isBlank(rawBillingKey)) {
            throw new IllegalArgumentException("Billing key is required.");
        }

        try {
            byte[] nonce = new byte[GCM_NONCE_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(VERSION.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(rawBillingKey.getBytes(StandardCharsets.UTF_8));

            String envelope = VERSION + ":"
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(nonce) + ":"
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
            return new ProtectedBillingKey(envelope, fingerprint(rawBillingKey));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to encrypt billing key.", e);
        }
    }

    public String decrypt(String envelope) {
        if (isBlank(envelope)) {
            throw new IllegalArgumentException("Billing key ciphertext is required.");
        }

        String[] parts = envelope.split(":", 3);
        if (parts.length != 3 || !VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported billing key ciphertext format.");
        }

        try {
            byte[] nonce = Base64.getUrlDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getUrlDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), new GCMParameterSpec(GCM_TAG_BITS, nonce));
            cipher.updateAAD(VERSION.getBytes(StandardCharsets.UTF_8));
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

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(deriveKey("fingerprint"), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(rawBillingKey.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to fingerprint billing key.", e);
        }
    }

    private SecretKeySpec encryptionKey() {
        return new SecretKeySpec(deriveKey("encryption"), "AES");
    }

    private byte[] deriveKey(String purpose) {
        String secret = encryptionSecret();
        try {
            return MessageDigest.getInstance("SHA-256").digest(
                    ("ATStudio:" + purpose + ":" + secret).getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to derive billing key material.", e);
        }
    }

    private String encryptionSecret() {
        String secret = paymentProperties.getBilling().getEncryptionSecret();
        if (isBlank(secret) || secret.startsWith("REPLACE_WITH_")) {
            throw new IllegalStateException("PAYMENT_BILLING_KEY_ENCRYPTION_SECRET is not configured.");
        }
        return secret;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ProtectedBillingKey(String ciphertext, String fingerprint) {
    }
}
