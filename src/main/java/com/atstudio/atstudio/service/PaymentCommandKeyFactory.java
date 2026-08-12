package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class PaymentCommandKeyFactory {

    private static final Pattern CANONICAL_UUID_V4 = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    public String billingAgreementPrepare(Long userID, String rawKey) {
        long ownerID = requiredID(userID, "userID");
        String canonicalKey = requireCanonicalBillingAgreementPrepareKey(rawKey);
        String digestInput = "billing-prepare\u0000v1\u0000" + ownerID + "\u0000" + canonicalKey;
        return "BILLING_PREPARE:v1:" + sha256(digestInput);
    }

    public String settlementImportDigest(Long userID, String rawKey) {
        long ownerID = requiredID(userID, "userID");
        String canonicalKey = requireCanonicalSettlementImportKey(rawKey);
        return sha256("settlement-csv-import\u0000v1\u0000" + ownerID + "\u0000" + canonicalKey);
    }

    public String requireCanonicalBillingAgreementPrepareKey(String rawKey) {
        if (rawKey == null || !CANONICAL_UUID_V4.matcher(rawKey).matches()) {
            throw new IllegalArgumentException("Idempotency-Key must be a canonical UUID v4 value.");
        }
        return rawKey;
    }

    public String requireCanonicalSettlementImportKey(String rawKey) {
        if (rawKey == null || !CANONICAL_UUID_V4.matcher(rawKey).matches()) {
            throw new IllegalArgumentException("Idempotency-Key must be a canonical UUID v4 value.");
        }
        return rawKey;
    }

    public String billingConfirm(String orderID) {
        return "BILLING_CONFIRM:" + requiredText(orderID, "orderID");
    }

    public String upgrade(
            Long userSubscriptionID,
            LocalDate startedAt,
            LocalDate expiresAt,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle) {
        return "UPGRADE:%d:%s:%s:%d:%s".formatted(
                requiredID(userSubscriptionID, "userSubscriptionID"),
                Objects.requireNonNull(startedAt, "startedAt"),
                Objects.requireNonNull(expiresAt, "expiresAt"),
                requiredID(targetSubscriptionID, "targetSubscriptionID"),
                Objects.requireNonNull(targetBillingCycle, "targetBillingCycle").name());
    }

    public String renewal(
            Long billingAgreementID,
            Long userSubscriptionID,
            LocalDate billingPeriodStart) {
        return "RENEWAL:%d:%d:%s".formatted(
                requiredID(billingAgreementID, "billingAgreementID"),
                requiredID(userSubscriptionID, "userSubscriptionID"),
                Objects.requireNonNull(billingPeriodStart, "billingPeriodStart"));
    }

    public String billingInitialAttempt(String orderID, int providerAttempt) {
        return "billing-initial-%s-attempt-%d".formatted(
                requiredText(orderID, "orderID"),
                requiredAttempt(providerAttempt));
    }

    public String upgradeAttempt(String orderID, int providerAttempt) {
        return "subscription-upgrade-%s-attempt-%d".formatted(
                requiredText(orderID, "orderID"),
                requiredAttempt(providerAttempt));
    }

    public String renewalAttempt(String orderID, int providerAttempt) {
        return "renewal-%s-attempt-%d".formatted(
                requiredText(orderID, "orderID"),
                requiredAttempt(providerAttempt));
    }

    private String requiredText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value;
    }

    private long requiredID(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(field + " must be positive.");
        }
        return value;
    }

    private int requiredAttempt(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("providerAttempt must be positive.");
        }
        return value;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
