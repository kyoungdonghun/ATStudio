package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentRefundTransactionService {

    private static final int MAX_FAILURE_CODE_LENGTH = 100;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 500;

    private final PaymentRefundRepository paymentRefundRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefundExecutionClaim claimExecution(
            Long refundId,
            CustomUserDetails actorDetails,
            String note) {
        PaymentRefund refund = findRefundForUpdate(refundId);
        if (refund.getStatus() != PaymentRefundStatus.APPROVED
                && refund.getStatus() != PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        PaymentRefundStatus beforeProcessing = refund.getStatus();
        PaymentOperationAuditAction processingAction = refund.markProcessing(resolveActor(actorDetails));
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                processingAction,
                beforeProcessing,
                refund.getStatus(),
                note);

        return new RefundExecutionClaim(
                refund.getId(),
                refund.getProvider(),
                refund.getProviderPaymentKey(),
                refund.getPaymentOrder().getOrderId(),
                refund.getAmount(),
                refund.getReasonCode().name(),
                refund.getIdempotencyKey());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminPaymentRefundResponse recordExecutionResult(
            Long refundId,
            CustomUserDetails actorDetails,
            PaymentRefundProviderResult providerResult) {
        if (providerResult == null) {
            return recordPending(
                    refundId,
                    actorDetails,
                    "REFUND_PROVIDER_EMPTY_RESULT",
                    "Provider returned no refund result.",
                    null);
        }
        if (providerResult.success()) {
            if (isBlank(providerResult.providerRefundTransactionId())) {
                return recordPending(
                        refundId,
                        actorDetails,
                        "REFUND_PROVIDER_TRANSACTION_MISSING",
                        "Provider success did not include a refund transaction ID.",
                        providerResult.providerPayload());
            }
            return recordSuccess(refundId, actorDetails, providerResult);
        }
        if (providerResult.pendingConfirmation()) {
            return recordPending(
                    refundId,
                    actorDetails,
                    providerResult.failureCode(),
                    providerResult.failureMessage(),
                    providerResult.providerPayload());
        }
        return recordFailure(refundId, actorDetails, providerResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminPaymentRefundResponse recordExecutionException(
            Long refundId,
            CustomUserDetails actorDetails,
            RuntimeException exception) {
        return recordPending(
                refundId,
                actorDetails,
                "REFUND_PROVIDER_EXCEPTION",
                exception.getClass().getSimpleName(),
                null);
    }

    private AdminPaymentRefundResponse recordSuccess(
            Long refundId,
            CustomUserDetails actorDetails,
            PaymentRefundProviderResult providerResult) {
        PaymentRefund refund = findProcessingRefund(refundId);
        PaymentRefundStatus beforeResult = refund.getStatus();
        PaymentOperationAuditAction action = refund.markSucceeded(
                providerResult.providerRefundTransactionId(),
                providerResult.providerPayload());
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeResult,
                refund.getStatus(),
                providerResult.failureMessage());
        return AdminPaymentRefundResponse.from(refund);
    }

    private AdminPaymentRefundResponse recordFailure(
            Long refundId,
            CustomUserDetails actorDetails,
            PaymentRefundProviderResult providerResult) {
        PaymentRefund refund = findProcessingRefund(refundId);
        PaymentRefundStatus beforeResult = refund.getStatus();
        PaymentOperationAuditAction action = refund.markFailed(
                safeCode(providerResult.failureCode()),
                safeMessage(providerResult.failureMessage()),
                providerResult.providerPayload());
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeResult,
                refund.getStatus(),
                providerResult.failureMessage());
        return AdminPaymentRefundResponse.from(refund);
    }

    private AdminPaymentRefundResponse recordPending(
            Long refundId,
            CustomUserDetails actorDetails,
            String failureCode,
            String failureMessage,
            String providerPayload) {
        PaymentRefund refund = findProcessingRefund(refundId);
        PaymentRefundStatus beforeResult = refund.getStatus();
        String safeMessage = safeMessage(failureMessage);
        PaymentOperationAuditAction action = refund.markPendingProviderConfirmation(
                safeCode(failureCode),
                safeMessage,
                providerPayload);
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeResult,
                refund.getStatus(),
                safeMessage);
        return AdminPaymentRefundResponse.from(refund);
    }

    private PaymentRefund findProcessingRefund(Long refundId) {
        PaymentRefund refund = findRefundForUpdate(refundId);
        if (refund.getStatus() != PaymentRefundStatus.PROCESSING) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        return refund;
    }

    private PaymentRefund findRefundForUpdate(Long refundId) {
        return paymentRefundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private User resolveActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            return null;
        }
        return userRepository.findById(actorDetails.getId()).orElse(null);
    }

    private String safeCode(String value) {
        return truncate(isBlank(value) ? "REFUND_PROVIDER_FAILURE" : value, MAX_FAILURE_CODE_LENGTH);
    }

    private String safeMessage(String value) {
        return truncate(isBlank(value) ? "Refund provider result requires review." : value,
                MAX_FAILURE_MESSAGE_LENGTH);
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record RefundExecutionClaim(
            Long refundId,
            PaymentProviderType provider,
            String providerPaymentKey,
            String orderId,
            BigDecimal amount,
            String reason,
            String idempotencyKey) {
    }
}
