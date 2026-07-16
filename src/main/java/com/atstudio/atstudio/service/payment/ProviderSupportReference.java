package com.atstudio.atstudio.service.payment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProviderSupportReference {

    private static final int REFERENCE_HEX_LENGTH = 12;
    private static final Pattern SUPPORT_REFERENCE = Pattern.compile("REF-[0-9A-F]{12}");
    private static final Pattern PROVIDER_IDENTIFIER_IN_FREE_TEXT = Pattern.compile(
            "(?i)\\b(transactionId|providerTransactionId|paymentKey|refundKey|settlementKey|receiptKey|orderId)"
                    + "(\\s*[:=]\\s*)([^\\s,;]+)");

    private ProviderSupportReference() {
    }

    public static String from(String providerIdentifier) {
        if (providerIdentifier == null || providerIdentifier.isBlank()) {
            return null;
        }
        String normalized = providerIdentifier.trim();
        if (SUPPORT_REFERENCE.matcher(normalized).matches()) {
            return normalized;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return "REF-" + HexFormat.of().withUpperCase().formatHex(digest)
                    .substring(0, REFERENCE_HEX_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", e);
        }
    }

    public static String sanitizeFreeText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        Matcher matcher = PROVIDER_IDENTIFIER_IN_FREE_TEXT.matcher(value);
        StringBuffer sanitized = new StringBuffer();
        while (matcher.find()) {
            String identifier = matcher.group(3);
            String replacement = SUPPORT_REFERENCE.matcher(identifier).matches()
                    ? matcher.group()
                    : matcher.group(1) + matcher.group(2) + from(identifier);
            matcher.appendReplacement(sanitized, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sanitized);
        return sanitized.toString();
    }
}
