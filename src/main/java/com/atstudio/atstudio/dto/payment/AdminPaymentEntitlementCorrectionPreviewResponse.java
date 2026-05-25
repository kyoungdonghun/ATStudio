package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;

import java.time.LocalDate;

public record AdminPaymentEntitlementCorrectionPreviewResponse(
        Long paymentRefundId,
        PaymentRefundStatus refundStatus,
        Long userId,
        String userNickname,
        Long userSubscriptionId,
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
        boolean executable,
        String reason
) {
    public static AdminPaymentEntitlementCorrectionPreviewResponse of(
            PaymentRefund refund,
            UserSubscription current,
            Subscription targetSubscription,
            AdminPaymentEntitlementCorrectionRequest request,
            BillingAgreement agreement,
            boolean executable,
            String reason) {
        return new AdminPaymentEntitlementCorrectionPreviewResponse(
                refund.getId(),
                refund.getStatus(),
                current.getUser().getId(),
                current.getUser().getNickname(),
                current.getId(),
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
                request.cancelBillingAgreement() ? BillingAgreementStatus.CANCELLED
                        : agreement == null ? null : agreement.getStatus(),
                executable,
                reason);
    }
}
