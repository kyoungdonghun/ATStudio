package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionAction;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminSubscriptionCorrectionResponse(
        Long id,
        Long userSubscriptionId,
        Long userId,
        String userNickname,
        Long billingAgreementId,
        AdminSubscriptionCorrectionStatus status,
        AdminSubscriptionCorrectionAction action,
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
        Long approvedById,
        Long executedById,
        String approvalNote,
        String executionNote,
        LocalDateTime approvedAt,
        LocalDateTime executedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminSubscriptionCorrectionResponse from(AdminSubscriptionCorrection correction) {
        return new AdminSubscriptionCorrectionResponse(
                correction.getId(),
                correction.getUserSubscription().getId(),
                correction.getUser().getId(),
                correction.getUser().getNickname(),
                correction.getBillingAgreement() == null ? null : correction.getBillingAgreement().getId(),
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
                correction.getRequestedBy().getId(),
                correction.getApprovedBy() == null ? null : correction.getApprovedBy().getId(),
                correction.getExecutedBy() == null ? null : correction.getExecutedBy().getId(),
                correction.getApprovalNote(),
                correction.getExecutionNote(),
                correction.getApprovedAt(),
                correction.getExecutedAt(),
                correction.getCreatedAt(),
                correction.getUpdatedAt());
    }
}
