package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.PaymentEntitlementCorrection;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionAction;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminPaymentEntitlementCorrectionResponse(
        Long id,
        Long paymentRefundId,
        Long subscriptionPaymentId,
        Long paymentOrderId,
        String orderId,
        Long userSubscriptionId,
        Long userId,
        String userNickname,
        PaymentProviderType provider,
        PaymentEntitlementCorrectionStatus status,
        PaymentEntitlementCorrectionAction action,
        Long beforeSubscriptionId,
        String beforePlanName,
        BillingCycle beforeBillingCycle,
        SubscriptionStatus beforeStatus,
        LocalDate beforeExpiresAt,
        Long beforePendingSubscriptionId,
        String beforePendingPlanName,
        BillingCycle beforePendingBillingCycle,
        Long targetSubscriptionId,
        String targetPlanName,
        BillingCycle targetBillingCycle,
        SubscriptionStatus targetStatus,
        LocalDate targetExpiresAt,
        boolean clearPendingChange,
        boolean cancelBillingAgreement,
        BillingAgreementStatus beforeBillingAgreementStatus,
        BillingAgreementStatus afterBillingAgreementStatus,
        String reasonNote,
        String failureCode,
        String failureMessage,
        Long requestedById,
        String requestedByEmail,
        Long approvedById,
        String approvedByEmail,
        Long executedById,
        String executedByEmail,
        LocalDateTime approvedAt,
        LocalDateTime executedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminPaymentEntitlementCorrectionResponse from(PaymentEntitlementCorrection correction) {
        return new AdminPaymentEntitlementCorrectionResponse(
                correction.getId(),
                correction.getPaymentRefund().getId(),
                correction.getSubscriptionPayment().getId(),
                correction.getPaymentOrder().getId(),
                correction.getPaymentOrder().getOrderId(),
                correction.getUserSubscription().getId(),
                correction.getUser().getId(),
                correction.getUser().getNickname(),
                correction.getProvider(),
                correction.getStatus(),
                correction.getAction(),
                correction.getBeforeSubscription().getId(),
                correction.getBeforeSubscription().getName(),
                correction.getBeforeBillingCycle(),
                correction.getBeforeStatus(),
                correction.getBeforeExpiresAt(),
                correction.getBeforePendingSubscription() == null
                        ? null : correction.getBeforePendingSubscription().getId(),
                correction.getBeforePendingSubscription() == null
                        ? null : correction.getBeforePendingSubscription().getName(),
                correction.getBeforePendingBillingCycle(),
                correction.getTargetSubscription().getId(),
                correction.getTargetSubscription().getName(),
                correction.getTargetBillingCycle(),
                correction.getTargetStatus(),
                correction.getTargetExpiresAt(),
                correction.isClearPendingChange(),
                correction.isCancelBillingAgreement(),
                correction.getBeforeBillingAgreementStatus(),
                correction.getAfterBillingAgreementStatus(),
                correction.getReasonNote(),
                correction.getFailureCode(),
                correction.getFailureMessage(),
                correction.getRequestedBy() == null ? null : correction.getRequestedBy().getId(),
                correction.getRequestedBy() == null ? null : correction.getRequestedBy().getEmail(),
                correction.getApprovedBy() == null ? null : correction.getApprovedBy().getId(),
                correction.getApprovedBy() == null ? null : correction.getApprovedBy().getEmail(),
                correction.getExecutedBy() == null ? null : correction.getExecutedBy().getId(),
                correction.getExecutedBy() == null ? null : correction.getExecutedBy().getEmail(),
                correction.getApprovedAt(),
                correction.getExecutedAt(),
                correction.getCreatedAt(),
                correction.getUpdatedAt());
    }
}
