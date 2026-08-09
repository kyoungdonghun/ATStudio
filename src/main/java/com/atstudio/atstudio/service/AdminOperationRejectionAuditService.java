package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.entity.AdminOperationAuditLog;
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
public class AdminOperationRejectionAuditService {

    private final AdminOperationAuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRoleChangeRejected(
            Long actorUserID,
            Long targetUserID,
            UserRole currentRole,
            boolean isDeleted,
            BUSINESS_ERROR reason,
            String reasonNote) {
        String state = AdminOperationAuditState.user(currentRole, isDeleted);
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_ROLE_CHANGE)
                .targetType(AdminOperationAuditTargetType.USER)
                .targetId(targetUserID)
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.REJECTED)
                .beforeState(state)
                .afterState(state)
                .reasonCode(reason.name())
                .reasonNote(reasonNote)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAdminWithdrawalRejected(
            Long userID,
            UserRole currentRole,
            boolean isDeleted,
            BUSINESS_ERROR reason) {
        String state = AdminOperationAuditState.user(currentRole, isDeleted);
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.ADMIN_WITHDRAWAL)
                .targetType(AdminOperationAuditTargetType.USER)
                .targetId(userID)
                .actorUserId(userID)
                .outcome(AdminOperationAuditOutcome.REJECTED)
                .beforeState(state)
                .afterState(state)
                .reasonCode(reason.name())
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUserSubscriptionCorrectionRejected(
            Long actorUserID,
            Long userSubscriptionID,
            String currentState,
            BUSINESS_ERROR reason,
            String reasonNote) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION)
                .targetType(AdminOperationAuditTargetType.USER_SUBSCRIPTION)
                .targetId(userSubscriptionID)
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.REJECTED)
                .beforeState(currentState)
                .afterState(currentState)
                .reasonCode(reason.name())
                .reasonNote(reasonNote)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUserSubscriptionCorrectionRequestRejected(
            Long actorUserID,
            Long userSubscriptionID,
            String currentState,
            BUSINESS_ERROR reason) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION_REQUEST)
                .targetType(AdminOperationAuditTargetType.USER_SUBSCRIPTION)
                .targetId(userSubscriptionID)
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.REJECTED)
                .beforeState(currentState)
                .afterState(currentState)
                .reasonCode(reason.name())
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordUserSubscriptionCorrectionApprovalRejected(
            Long actorUserID,
            Long correctionID,
            String currentState,
            BUSINESS_ERROR reason) {
        auditLogRepository.save(AdminOperationAuditLog.builder()
                .action(AdminOperationAuditAction.USER_SUBSCRIPTION_CORRECTION_APPROVAL)
                .targetType(AdminOperationAuditTargetType.ADMIN_SUBSCRIPTION_CORRECTION)
                .targetId(correctionID)
                .actorUserId(actorUserID)
                .outcome(AdminOperationAuditOutcome.REJECTED)
                .beforeState(currentState)
                .afterState(currentState)
                .reasonCode(reason.name())
                .build());
    }
}
