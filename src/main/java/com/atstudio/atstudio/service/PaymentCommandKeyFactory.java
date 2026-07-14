package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class PaymentCommandKeyFactory {

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
}
