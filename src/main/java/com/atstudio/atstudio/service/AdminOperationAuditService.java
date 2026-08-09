package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.AdminOperationAuditLog;
import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditAction;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditOutcome;
import com.atstudio.atstudio.entity.enums.AdminOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.AdminOperationAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOperationAuditService {

    private static final String ROLE_CHANGED = "ROLE_CHANGED";
    private static final String USER_REQUESTED_WITHDRAWAL = "USER_REQUESTED_WITHDRAWAL";

    private final AdminOperationAuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordRoleChangeSuccess(
            Long actorUserID,
            Long targetUserID,
            UserRole beforeRole,
            UserRole afterRole,
            boolean isDeleted,
            String reasonNote) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_ROLE_CHANGE)
                .targetType(AdminOperationAuditTargetType.USER)
                .targetId(targetUserID)
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.SUCCEEDED)
                .beforeState(AdminOperationAuditState.user(beforeRole, isDeleted))
                .afterState(AdminOperationAuditState.user(afterRole, isDeleted))
                .reasonCode(ROLE_CHANGED)
                .reasonNote(reasonNote)
                .build());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordAdminWithdrawalSuccess(Long userID, UserRole role) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.ADMIN_WITHDRAWAL)
                .targetType(AdminOperationAuditTargetType.USER)
                .targetId(userID)
                .actorUserId(userID)
                .outcome(AdminOperationAuditOutcome.SUCCEEDED)
                .beforeState(AdminOperationAuditState.user(role, false))
                .afterState(AdminOperationAuditState.user(role, true))
                .reasonCode(USER_REQUESTED_WITHDRAWAL)
                .build());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordUserSubscriptionCorrectionSuccess(
            Long actorUserID,
            AdminSubscriptionCorrection correction,
            UserSubscription afterState) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION)
                .targetType(AdminOperationAuditTargetType.USER_SUBSCRIPTION)
                .targetId(afterState.getId())
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.SUCCEEDED)
                .beforeState(AdminOperationAuditState.userSubscription(
                        correction.getBeforeSubscription().getId(),
                        correction.getBeforeBillingCycle(),
                        correction.getBeforeStatus(),
                        correction.getBeforeExpiresAt().toString(),
                        correction.getBeforePendingSubscription() == null
                                ? null : correction.getBeforePendingSubscription().getId(),
                        correction.getBeforePendingBillingCycle(),
                        correction.getBeforeBillingAgreementStatus()))
                .afterState(AdminOperationAuditState.userSubscription(
                        afterState.getSubscription().getId(),
                        afterState.getBillingCycle(),
                        afterState.getStatus(),
                        afterState.getExpiresAt().toString(),
                        afterState.getPendingSubscription() == null
                                ? null : afterState.getPendingSubscription().getId(),
                        afterState.getPendingBillingCycle(),
                        correction.getAfterBillingAgreementStatus()))
                .reasonCode(correction.getAction().name())
                .reasonNote(correction.getReasonNote())
                .build());
    }
}
