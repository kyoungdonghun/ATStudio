package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;

import java.time.LocalDate;

public record AdminSubscriptionCorrectionPreviewResponse(
        Long userSubscriptionId,
        Long userId,
        String userNickname,
        Long currentSubscriptionId,
        String currentPlanName,
        BillingCycle currentBillingCycle,
        SubscriptionStatus currentStatus,
        LocalDate currentExpiresAt,
        Long currentPendingSubscriptionId,
        String currentPendingPlanName,
        BillingCycle currentPendingBillingCycle,
        Long targetSubscriptionId,
        String targetPlanName,
        BillingCycle targetBillingCycle,
        SubscriptionStatus targetStatus,
        LocalDate targetExpiresAt,
        boolean clearPendingChange,
        boolean cancelBillingAgreement,
        BillingAgreementStatus currentBillingAgreementStatus,
        BillingAgreementStatus targetBillingAgreementStatus,
        boolean externalPaymentExecuted,
        boolean executable,
        String reason
) {
    public static AdminSubscriptionCorrectionPreviewResponse of(
            UserSubscription current,
            Subscription targetSubscription,
            AdminSubscriptionCorrectionRequest request,
            BillingAgreement agreement,
            boolean executable,
            String reason) {
        return new AdminSubscriptionCorrectionPreviewResponse(
                current.getId(),
                current.getUser().getId(),
                current.getUser().getNickname(),
                current.getSubscription().getId(),
                current.getSubscription().getName(),
                current.getBillingCycle(),
                current.getStatus(),
                current.getExpiresAt(),
                current.getPendingSubscription() == null ? null : current.getPendingSubscription().getId(),
                current.getPendingSubscription() == null ? null : current.getPendingSubscription().getName(),
                current.getPendingBillingCycle(),
                targetSubscription.getId(),
                targetSubscription.getName(),
                request.targetBillingCycle(),
                request.targetStatus(),
                request.targetExpiresAt(),
                request.clearPendingChange(),
                request.cancelBillingAgreement(),
                agreement == null ? null : agreement.getStatus(),
                targetAgreementStatus(agreement, request.cancelBillingAgreement()),
                false,
                executable,
                reason);
    }

    private static BillingAgreementStatus targetAgreementStatus(
            BillingAgreement agreement,
            boolean cancelBillingAgreement) {
        if (agreement == null) {
            return null;
        }
        if (cancelBillingAgreement
                && agreement.getStatus() != BillingAgreementStatus.CANCELLED
                && agreement.getStatus() != BillingAgreementStatus.EXPIRED) {
            return BillingAgreementStatus.CANCELLED;
        }
        return agreement.getStatus();
    }
}
