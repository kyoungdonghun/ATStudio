package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentRefundTransactionService {

    static final Duration PROCESSING_LEASE = Duration.ofMinutes(15);
    static final Duration TOSS_SAME_KEY_REPLAY_CEILING = Duration.ofHours(24);

    private static final int MAX_FAILURE_CODE_LENGTH = 100;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 500;
    private static final String REPLAY_CEILING_FAILURE_CODE = "REFUND_REPLAY_CEILING_ELAPSED";
    private static final String REPLAY_CEILING_FAILURE_MESSAGE =
            "Exact provider refund lookup is unavailable after the same-key replay ceiling elapsed.";
    private static final String STALE_RECLAIM_AUDIT_PREFIX = "STALE_RECLAIM previousLeaseStartedAt=";
    private static final String REFUND_INCIDENT_DEDUPE_PREFIX = "refund-replay-unavailable:";

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentReconciliationIncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefundExecutionClaim claimExecution(
            Long refundId,
            CustomUserDetails actorDetails,
            String note) {
        return claimExecution(refundId, actorDetails, note, LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RefundExecutionClaim claimExecution(
            Long refundId,
            CustomUserDetails actorDetails,
            String note,
            LocalDateTime now) {
        LocalDateTime claimedAt = requireSecondPrecision(now);
        PaymentRefund refund = findRefundForUpdate(refundId);
        PaymentRefundStatus beforeProcessing = refund.getStatus();
        LocalDateTime previousLeaseStartedAt = refund.getProcessingStartedAt();
        boolean recoveryClaim = beforeProcessing == PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION
                || beforeProcessing == PaymentRefundStatus.PROCESSING;
        boolean staleReclaim = false;

        PaymentOperationAuditAction processingAction;
        if (beforeProcessing == PaymentRefundStatus.APPROVED
                || beforeProcessing == PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION) {
            processingAction = refund.markProcessing(resolveActor(actorDetails), claimedAt);
        } else if (beforeProcessing == PaymentRefundStatus.PROCESSING
                && isStale(previousLeaseStartedAt, claimedAt)) {
            processingAction = refund.reclaimProcessing(
                    resolveActor(actorDetails),
                    previousLeaseStartedAt,
                    claimedAt);
            staleReclaim = true;
        } else {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                processingAction,
                beforeProcessing,
                refund.getStatus(),
                staleReclaim ? staleReclaimNote(previousLeaseStartedAt, note) : note);

        return new RefundExecutionClaim(
                refund.getId(),
                refund.getProvider(),
                refund.getProviderPaymentKey(),
                refund.getPaymentOrder().getOrderId(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getReasonCode().name(),
                refund.getIdempotencyKey(),
                refund.getProcessingStartedAt(),
                recoveryExecutionMode(refund, recoveryClaim, claimedAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validateClaimForExecution(RefundExecutionClaim claim) {
        if (claim == null || claim.refundId() == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        PaymentRefund refund = findRefundForUpdate(claim.refundId());
        if (!matchesActiveClaim(refund, claim)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminPaymentRefundResponse recordExecutionResult(
            Long refundId,
            CustomUserDetails actorDetails,
            LocalDateTime leaseStartedAt,
            PaymentRefundProviderResult providerResult) {
        if (providerResult == null) {
            return recordPending(
                    refundId,
                    actorDetails,
                    leaseStartedAt,
                    "REFUND_PROVIDER_EMPTY_RESULT",
                    "Provider returned no refund result.",
                    null);
        }
        if (providerResult.success()) {
            if (isBlank(providerResult.providerRefundTransactionId())) {
                return recordPending(
                        refundId,
                        actorDetails,
                        leaseStartedAt,
                        "REFUND_PROVIDER_TRANSACTION_MISSING",
                        "Provider success did not include a refund transaction ID.",
                        providerResult.providerPayload());
            }
            return recordSuccess(refundId, actorDetails, leaseStartedAt, providerResult);
        }
        if (providerResult.pendingConfirmation()) {
            return recordPending(
                    refundId,
                    actorDetails,
                    leaseStartedAt,
                    providerResult.failureCode(),
                    providerResult.failureMessage(),
                    providerResult.providerPayload());
        }
        return recordFailure(refundId, actorDetails, leaseStartedAt, providerResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminPaymentRefundResponse recordExecutionException(
            Long refundId,
            CustomUserDetails actorDetails,
            LocalDateTime leaseStartedAt,
            RuntimeException exception) {
        return recordPending(
                refundId,
                actorDetails,
                leaseStartedAt,
                "REFUND_PROVIDER_EXCEPTION",
                exception.getClass().getSimpleName(),
                null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AdminPaymentRefundResponse recordReplayUnavailable(
            Long refundId,
            CustomUserDetails actorDetails,
            LocalDateTime leaseStartedAt) {
        PaymentRefund refund = findProcessingRefund(refundId, leaseStartedAt);
        PaymentRefundStatus beforeResult = refund.getStatus();
        PaymentOperationAuditAction action = refund.markPendingProviderConfirmation(
                REPLAY_CEILING_FAILURE_CODE,
                REPLAY_CEILING_FAILURE_MESSAGE,
                null,
                leaseStartedAt);
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeResult,
                refund.getStatus(),
                REPLAY_CEILING_FAILURE_MESSAGE);
        recordReplayUnavailableIncident(refund, leaseStartedAt);
        return AdminPaymentRefundResponse.from(refund);
    }

    private AdminPaymentRefundResponse recordSuccess(
            Long refundId,
            CustomUserDetails actorDetails,
            LocalDateTime leaseStartedAt,
            PaymentRefundProviderResult providerResult) {
        PaymentRefund refund = findProcessingRefund(refundId, leaseStartedAt);
        PaymentRefundStatus beforeResult = refund.getStatus();
        PaymentOperationAuditAction action = refund.markSucceeded(
                providerResult.providerRefundTransactionId(),
                providerResult.providerPayload(),
                leaseStartedAt);
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
            LocalDateTime leaseStartedAt,
            PaymentRefundProviderResult providerResult) {
        PaymentRefund refund = findProcessingRefund(refundId, leaseStartedAt);
        PaymentRefundStatus beforeResult = refund.getStatus();
        PaymentOperationAuditAction action = refund.markFailed(
                safeCode(providerResult.failureCode()),
                safeMessage(providerResult.failureMessage()),
                providerResult.providerPayload(),
                leaseStartedAt);
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
            LocalDateTime leaseStartedAt,
            String failureCode,
            String failureMessage,
            String providerPayload) {
        PaymentRefund refund = findProcessingRefund(refundId, leaseStartedAt);
        PaymentRefundStatus beforeResult = refund.getStatus();
        String safeMessage = safeMessage(failureMessage);
        PaymentOperationAuditAction action = refund.markPendingProviderConfirmation(
                safeCode(failureCode),
                safeMessage,
                providerPayload,
                leaseStartedAt);
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeResult,
                refund.getStatus(),
                safeMessage);
        return AdminPaymentRefundResponse.from(refund);
    }

    private PaymentRefund findProcessingRefund(Long refundId, LocalDateTime leaseStartedAt) {
        PaymentRefund refund = findRefundForUpdate(refundId);
        if (refund.getStatus() != PaymentRefundStatus.PROCESSING
                || leaseStartedAt == null
                || !Objects.equals(
                        refund.getProcessingStartedAt(),
                        leaseStartedAt.withNano(0))) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        return refund;
    }

    private boolean matchesActiveClaim(PaymentRefund refund, RefundExecutionClaim claim) {
        return refund.getStatus() == PaymentRefundStatus.PROCESSING
                && Objects.equals(refund.getId(), claim.refundId())
                && refund.getProvider() == claim.provider()
                && Objects.equals(refund.getProviderPaymentKey(), claim.providerPaymentKey())
                && Objects.equals(refund.getPaymentOrder().getOrderId(), claim.orderId())
                && sameAmount(refund.getAmount(), claim.amount())
                && Objects.equals(refund.getCurrency(), claim.currency())
                && Objects.equals(refund.getReasonCode().name(), claim.reason())
                && Objects.equals(refund.getIdempotencyKey(), claim.idempotencyKey())
                && claim.leaseStartedAt() != null
                && Objects.equals(
                        refund.getProcessingStartedAt(),
                        claim.leaseStartedAt().withNano(0));
    }

    private boolean sameAmount(BigDecimal persisted, BigDecimal claimed) {
        return persisted != null && claimed != null && persisted.compareTo(claimed) == 0;
    }

    private boolean isStale(LocalDateTime leaseStartedAt, LocalDateTime now) {
        return leaseStartedAt != null
                && !leaseStartedAt.plus(PROCESSING_LEASE).isAfter(now);
    }

    private RefundExecutionMode recoveryExecutionMode(
            PaymentRefund refund,
            boolean recoveryClaim,
            LocalDateTime now) {
        if (!recoveryClaim) {
            return RefundExecutionMode.PROVIDER_MUTATION;
        }
        LocalDateTime createdAt = refund.getCreatedAt();
        boolean sameKeyReplayAllowed = refund.getProvider() == PaymentProviderType.TOSS
                && createdAt != null
                && now.isBefore(createdAt.plus(TOSS_SAME_KEY_REPLAY_CEILING));
        return sameKeyReplayAllowed
                ? RefundExecutionMode.PROVIDER_MUTATION
                : RefundExecutionMode.LOOKUP_ONLY;
    }

    private String staleReclaimNote(LocalDateTime previousLeaseStartedAt, String note) {
        String prefix = STALE_RECLAIM_AUDIT_PREFIX + previousLeaseStartedAt.withNano(0);
        return isBlank(note) ? prefix : prefix + "; " + note;
    }

    private void recordReplayUnavailableIncident(PaymentRefund refund, LocalDateTime detectedAt) {
        String dedupeKey = REFUND_INCIDENT_DEDUPE_PREFIX + refund.getId();
        PaymentReconciliationIncident incident = incidentRepository.findByDedupeKey(dedupeKey)
                .map(existing -> {
                    existing.recordDetection(
                            refund.getPaymentOrder(),
                            refund.getPaymentOrder().getBillingAgreement(),
                            refund.getUser(),
                            refund.getPaymentOrder().getOrderId(),
                            refund.getProvider(),
                            refund.getPaymentOrder().getPurpose(),
                            refund.getStatus().name(),
                            null,
                            refund.getAmount(),
                            null,
                            refund.getProviderRefundTransactionId(),
                            REPLAY_CEILING_FAILURE_CODE,
                            REPLAY_CEILING_FAILURE_MESSAGE,
                            PaymentReconciliationIncidentSeverity.CRITICAL,
                            detectedAt);
                    return existing;
                })
                .orElseGet(() -> incidentRepository.save(PaymentReconciliationIncident.builder()
                        .dedupeKey(dedupeKey)
                        .issueType(PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED)
                        .status(PaymentReconciliationIncidentStatus.OPEN)
                        .severity(PaymentReconciliationIncidentSeverity.CRITICAL)
                        .paymentOrder(refund.getPaymentOrder())
                        .billingAgreement(refund.getPaymentOrder().getBillingAgreement())
                        .user(refund.getUser())
                        .orderId(refund.getPaymentOrder().getOrderId())
                        .provider(refund.getProvider())
                        .purpose(refund.getPaymentOrder().getPurpose())
                        .localStatus(refund.getStatus().name())
                        .localAmount(refund.getAmount())
                        .failureCode(REPLAY_CEILING_FAILURE_CODE)
                        .failureMessage(REPLAY_CEILING_FAILURE_MESSAGE)
                        .occurrenceCount(1)
                        .firstDetectedAt(detectedAt)
                        .lastDetectedAt(detectedAt)
                        .build()));
        if (incident.getStatus() != PaymentReconciliationIncidentStatus.OPEN) {
            incident.changeStatus(
                    PaymentReconciliationIncidentStatus.OPEN,
                    REPLAY_CEILING_FAILURE_MESSAGE,
                    detectedAt);
        }
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

    private LocalDateTime requireSecondPrecision(LocalDateTime value) {
        if (value == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        return value.withNano(0);
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
            String currency,
            String reason,
            String idempotencyKey,
            LocalDateTime leaseStartedAt,
            RefundExecutionMode executionMode) {
    }

    public enum RefundExecutionMode {
        PROVIDER_MUTATION,
        LOOKUP_ONLY
    }
}
