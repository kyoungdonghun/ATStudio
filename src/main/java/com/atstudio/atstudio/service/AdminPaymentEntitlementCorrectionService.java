package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionPreviewResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentEntitlementCorrectionResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentEntitlementCorrection;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentEntitlementCorrectionStatus;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentEntitlementCorrectionRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPaymentEntitlementCorrectionService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final Set<PaymentEntitlementCorrectionStatus> NON_TERMINAL_CORRECTION_STATUSES = Set.of(
            PaymentEntitlementCorrectionStatus.REQUESTED,
            PaymentEntitlementCorrectionStatus.APPROVED,
            PaymentEntitlementCorrectionStatus.PROCESSING);
    private static final Set<PaymentPurpose> PROVIDER_CHARGE_PURPOSES = Set.of(
            PaymentPurpose.SUBSCRIBE,
            PaymentPurpose.UPGRADE,
            PaymentPurpose.RENEWAL);
    private static final Set<PaymentOrderStatus> PROVIDER_OUTCOME_PENDING_STATUSES = Set.of(
            PaymentOrderStatus.PROCESSING,
            PaymentOrderStatus.PROVIDER_SUCCEEDED,
            PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);

    private final PaymentEntitlementCorrectionRepository correctionRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;

    public ResponseDTO<AdminPaymentEntitlementCorrectionResponse> listCorrections(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminPaymentEntitlementCorrectionResponse> result =
                correctionRepository.findAllByOrderByCreatedAtDesc(pageable)
                        .map(AdminPaymentEntitlementCorrectionResponse::from);
        return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }

    public ResponseDTO<AdminPaymentEntitlementCorrectionResponse> getCorrection(Long correctionId) {
        PaymentEntitlementCorrection correction = correctionRepository.findDetailedById(correctionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                .data(AdminPaymentEntitlementCorrectionResponse.from(correction))
                .build();
    }

    public ResponseDTO<AdminPaymentEntitlementCorrectionPreviewResponse> previewCorrection(
            AdminPaymentEntitlementCorrectionRequest request) {
        PaymentRefund refund = findRefund(request.paymentRefundId());
        Subscription targetSubscription = findTargetSubscription(request.targetSubscriptionId());
        UserSubscription current = refund.getSubscriptionPayment().getUserSubscription();
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(
                        refund.getUser(),
                        RECURRING_PROVIDER)
                .orElse(null);

        String invalidReason = invalidReason(refund, current, targetSubscription, request);
        return ResponseDTO.<AdminPaymentEntitlementCorrectionPreviewResponse>builder()
                .data(AdminPaymentEntitlementCorrectionPreviewResponse.of(
                        refund,
                        current,
                        targetSubscription,
                        request,
                        agreement,
                        invalidReason == null,
                        invalidReason))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentEntitlementCorrectionResponse> createCorrection(
            CustomUserDetails actorDetails,
            AdminPaymentEntitlementCorrectionRequest request) {
        PaymentRefund refund = findRefund(request.paymentRefundId());
        Subscription targetSubscription = findTargetSubscription(request.targetSubscriptionId());
        UserSubscription linkedSubscription = refund.getSubscriptionPayment().getUserSubscription();
        BillingAgreement agreement = lockAgreementForUser(refund.getUser());
        UserSubscription current = userSubscriptionRepository.findByIdForUpdate(linkedSubscription.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        assertNoProviderOutcomePending(agreement);
        if (correctionRepository.existsByPaymentRefund_IdAndUserSubscription_IdAndStatusIn(
                refund.getId(), current.getId(), NON_TERMINAL_CORRECTION_STATUSES)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }
        validateCorrectable(refund, current, targetSubscription, request);
        User actor = resolveActor(actorDetails);

        PaymentEntitlementCorrection correction = correctionRepository.save(PaymentEntitlementCorrection.builder()
                .paymentRefund(refund)
                .subscriptionPayment(refund.getSubscriptionPayment())
                .paymentOrder(refund.getPaymentOrder())
                .userSubscription(current)
                .user(refund.getUser())
                .provider(refund.getProvider())
                .beforeSubscription(current.getSubscription())
                .beforeBillingCycle(current.getBillingCycle())
                .beforeStatus(current.getStatus())
                .beforeExpiresAt(current.getExpiresAt())
                .beforePendingSubscription(current.getPendingSubscription())
                .beforePendingBillingCycle(current.getPendingBillingCycle())
                .targetSubscription(targetSubscription)
                .targetBillingCycle(request.targetBillingCycle())
                .targetStatus(request.targetStatus())
                .targetExpiresAt(request.targetExpiresAt())
                .clearPendingChange(request.clearPendingChange())
                .cancelBillingAgreement(request.cancelBillingAgreement())
                .beforeBillingAgreementStatus(agreement == null ? null : agreement.getStatus())
                .afterBillingAgreementStatus(agreement == null ? null : agreement.getStatus())
                .reasonNote(request.reasonNote())
                .requestedBy(actor)
                .build());
        auditLogService.recordPaymentEntitlementCorrectionEvent(
                actorDetails,
                correction,
                PaymentOperationAuditAction.PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED,
                null,
                correction.getStatus(),
                request.reasonNote());
        return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                .data(AdminPaymentEntitlementCorrectionResponse.from(correction))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentEntitlementCorrectionResponse> approveCorrection(
            Long correctionId,
            CustomUserDetails actorDetails,
            AdminPaymentEntitlementCorrectionApproveRequest request) {
        PaymentEntitlementCorrection correction = findCorrectionForUpdate(correctionId);
        if (correction.getStatus() != PaymentEntitlementCorrectionStatus.REQUESTED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        PaymentEntitlementCorrectionStatus beforeStatus = correction.getStatus();
        PaymentOperationAuditAction action = correction.approve(resolveActor(actorDetails));
        auditLogService.recordPaymentEntitlementCorrectionEvent(
                actorDetails,
                correction,
                action,
                beforeStatus,
                correction.getStatus(),
                request.note());
        return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                .data(AdminPaymentEntitlementCorrectionResponse.from(correction))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentEntitlementCorrectionResponse> executeCorrection(
            Long correctionId,
            CustomUserDetails actorDetails,
            AdminPaymentEntitlementCorrectionExecuteRequest request) {
        PaymentEntitlementCorrection correction = findCorrectionForUpdate(correctionId);
        if (correction.getStatus() == PaymentEntitlementCorrectionStatus.SUCCEEDED) {
            return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                    .data(AdminPaymentEntitlementCorrectionResponse.from(correction))
                    .build();
        }
        if (correction.getStatus() != PaymentEntitlementCorrectionStatus.APPROVED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }

        BillingAgreement agreement = lockAgreementForUser(correction.getUser());
        UserSubscription current = userSubscriptionRepository.findByIdForUpdate(
                        correction.getUserSubscription().getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        if (!matchesBeforeState(current, correction)
                || !matchesBeforeAgreementState(agreement, correction)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        assertNoProviderOutcomePending(agreement);

        PaymentEntitlementCorrectionStatus beforeProcessing = correction.getStatus();
        PaymentOperationAuditAction processingAction = correction.markProcessing(resolveActor(actorDetails));
        auditLogService.recordPaymentEntitlementCorrectionEvent(
                actorDetails,
                correction,
                processingAction,
                beforeProcessing,
                correction.getStatus(),
                request.note());

        PaymentEntitlementCorrectionStatus beforeResult = correction.getStatus();
        current.applyEntitlementCorrection(
                correction.getTargetSubscription(),
                correction.getTargetBillingCycle(),
                correction.getTargetStatus(),
                correction.getTargetExpiresAt(),
                correction.isClearPendingChange());

        BillingAgreementStatus afterAgreementStatus = correction.getBeforeBillingAgreementStatus();
        if (correction.isCancelBillingAgreement()) {
            if (agreement != null
                    && agreement.getStatus() != BillingAgreementStatus.CANCELLED
                    && agreement.getStatus() != BillingAgreementStatus.EXPIRED) {
                agreement.cancel();
            }
            afterAgreementStatus = agreement == null ? null : agreement.getStatus();
        }

        PaymentOperationAuditAction resultAction = correction.markSucceeded(afterAgreementStatus);
        auditLogService.recordPaymentEntitlementCorrectionEvent(
                actorDetails,
                correction,
                resultAction,
                beforeResult,
                correction.getStatus(),
                null);
        return ResponseDTO.<AdminPaymentEntitlementCorrectionResponse>builder()
                .data(AdminPaymentEntitlementCorrectionResponse.from(correction))
                .build();
    }

    private PaymentRefund findRefund(Long refundId) {
        return paymentRefundRepository.findWithGraphById(refundId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private Subscription findTargetSubscription(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private PaymentEntitlementCorrection findCorrectionForUpdate(Long correctionId) {
        return correctionRepository.findByIdForUpdate(correctionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private void validateCorrectable(
            PaymentRefund refund,
            UserSubscription current,
            Subscription targetSubscription,
            AdminPaymentEntitlementCorrectionRequest request) {
        String reason = invalidReason(refund, current, targetSubscription, request);
        if (reason != null) {
            throw new BusinessException(
                    BUSINESS_ERROR.INVALID_ARGUMENT,
                    new IllegalStateException(reason));
        }
    }

    private String invalidReason(
            PaymentRefund refund,
            UserSubscription current,
            Subscription targetSubscription,
            AdminPaymentEntitlementCorrectionRequest request) {
        if (refund.getStatus() != PaymentRefundStatus.SUCCEEDED) {
            return "Only succeeded refund records can receive entitlement correction.";
        }
        if (current == null || current.getUser() == null || current.getSubscription() == null) {
            return "Linked user subscription is missing required state.";
        }
        if (!Objects.equals(refund.getUser().getId(), current.getUser().getId())) {
            return "Refund owner and subscription owner do not match.";
        }
        if (targetSubscription.getUserType() != refund.getUser().getUserType()) {
            return "Target subscription user type does not match refund owner.";
        }
        if (!targetSubscription.isActive()) {
            return "Target subscription is inactive.";
        }
        if (request.targetStatus() == SubscriptionStatus.EXPIRED
                && request.targetExpiresAt().isAfter(LocalDate.now())) {
            return "Expired subscriptions must not have a future expiration date.";
        }
        if (request.targetStatus() != SubscriptionStatus.EXPIRED
                && request.targetExpiresAt().isBefore(LocalDate.now())) {
            return "Active or cancelled subscriptions must not have a past expiration date.";
        }
        if (isNoOp(current, request, targetSubscription)) {
            return "Entitlement correction target is identical to the current subscription state.";
        }
        return null;
    }

    private boolean isNoOp(
            UserSubscription current,
            AdminPaymentEntitlementCorrectionRequest request,
            Subscription targetSubscription) {
        boolean sameState = Objects.equals(current.getSubscription().getId(), targetSubscription.getId())
                && current.getBillingCycle() == request.targetBillingCycle()
                && current.getStatus() == request.targetStatus()
                && Objects.equals(current.getExpiresAt(), request.targetExpiresAt());
        boolean pendingNoOp = !request.clearPendingChange()
                || (current.getPendingSubscription() == null && current.getPendingBillingCycle() == null);
        return sameState && pendingNoOp && !request.cancelBillingAgreement();
    }

    private boolean matchesBeforeState(
            UserSubscription current,
            PaymentEntitlementCorrection correction) {
        return sameEntityId(current.getSubscription(), correction.getBeforeSubscription())
                && current.getBillingCycle() == correction.getBeforeBillingCycle()
                && current.getStatus() == correction.getBeforeStatus()
                && Objects.equals(current.getExpiresAt(), correction.getBeforeExpiresAt())
                && sameEntityId(current.getPendingSubscription(), correction.getBeforePendingSubscription())
                && current.getPendingBillingCycle() == correction.getBeforePendingBillingCycle();
    }

    private boolean matchesBeforeAgreementState(
            BillingAgreement agreement,
            PaymentEntitlementCorrection correction) {
        BillingAgreementStatus expected = correction.getBeforeBillingAgreementStatus();
        if (agreement == null) {
            return expected == null;
        }
        if (expected == null || agreement.getStatus() != expected) {
            return false;
        }

        LocalDateTime correctionCreatedAt = correction.getCreatedAt();
        LocalDateTime agreementUpdatedAt = agreement.getUpdatedAt();
        return correctionCreatedAt != null
                && agreementUpdatedAt != null
                && agreementUpdatedAt.isBefore(correctionCreatedAt);
    }

    private BillingAgreement lockAgreementForUser(User user) {
        return billingAgreementRepository.findByUserIDAndProviderForUpdate(
                        user.getId(),
                        RECURRING_PROVIDER)
                .orElse(null);
    }

    private void assertNoProviderOutcomePending(BillingAgreement agreement) {
        if (agreement != null && paymentOrderRepository
                .existsByBillingAgreementAndPurposeInAndStatusIn(
                        agreement,
                        PROVIDER_CHARGE_PURPOSES,
                        PROVIDER_OUTCOME_PENDING_STATUSES)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private boolean sameEntityId(Subscription left, Subscription right) {
        if (left == null || right == null) {
            return left == right;
        }
        return Objects.equals(left.getId(), right.getId());
    }

    private User resolveActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            return null;
        }
        return userRepository.findById(actorDetails.getId()).orElse(null);
    }
}
