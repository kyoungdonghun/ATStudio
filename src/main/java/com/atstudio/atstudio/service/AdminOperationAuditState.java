package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

final class AdminOperationAuditState {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AdminOperationAuditState() {
    }

    static String user(UserRole role, boolean isDeleted) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("role", role.name());
        state.put("isDeleted", isDeleted);
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize minimal administrator audit state", exception);
        }
    }

    static String userSubscription(
            Long subscriptionID,
            BillingCycle billingCycle,
            SubscriptionStatus status,
            String expiresAt,
            Long pendingSubscriptionID,
            BillingCycle pendingBillingCycle,
            BillingAgreementStatus billingAgreementStatus) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("subscriptionId", subscriptionID);
        state.put("billingCycle", billingCycle == null ? null : billingCycle.name());
        state.put("status", status == null ? null : status.name());
        state.put("expiresAt", expiresAt);
        state.put("pendingSubscriptionId", pendingSubscriptionID);
        state.put("pendingBillingCycle", pendingBillingCycle == null ? null : pendingBillingCycle.name());
        state.put("billingAgreementStatus",
                billingAgreementStatus == null ? null : billingAgreementStatus.name());
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize minimal subscription audit state", exception);
        }
    }

    static String userSubscriptionCorrection(
            Long userSubscriptionID,
            AdminSubscriptionCorrectionStatus correctionStatus) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("userSubscriptionId", userSubscriptionID);
        state.put("correctionStatus", correctionStatus == null ? null : correctionStatus.name());
        try {
            return OBJECT_MAPPER.writeValueAsString(state);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize minimal correction audit state", exception);
        }
    }
}
