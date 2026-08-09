package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.PaymentProperties;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionApproveRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionExecuteRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionPreviewResponse;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionRequest;
import com.atstudio.atstudio.dto.subscription.AdminSubscriptionCorrectionResponse;
import com.atstudio.atstudio.entity.AdminSubscriptionCorrection;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.AdminSubscriptionCorrectionStatus;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.AdminSubscriptionCorrectionRepository;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@Slf4j
public class AdminSubscriptionCorrectionService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;
    private static final Set<AdminSubscriptionCorrectionStatus> NON_TERMINAL_STATUSES = Set.of(
            AdminSubscriptionCorrectionStatus.REQUESTED,
            AdminSubscriptionCorrectionStatus.APPROVED,
            AdminSubscriptionCorrectionStatus.PROCESSING);
    private static final Set<PaymentPurpose> PROVIDER_CHARGE_PURPOSES = Set.of(
            PaymentPurpose.SUBSCRIBE,
            PaymentPurpose.UPGRADE,
            PaymentPurpose.RENEWAL);
    private static final Set<PaymentOrderStatus> PROVIDER_OUTCOME_PENDING_STATUSES = Set.of(
            PaymentOrderStatus.PROCESSING,
            PaymentOrderStatus.PROVIDER_SUCCEEDED,
            PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION);

    private final AdminSubscriptionCorrectionRepository correctionRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final AdminOperationAuditService auditService;
    private final AdminOperationRejectionAuditService rejectionAuditService;
    private final Clock businessClock;

    @Autowired
    public AdminSubscriptionCorrectionService(
            AdminSubscriptionCorrectionRepository correctionRepository,
            BillingAgreementRepository billingAgreementRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            UserRepository userRepository,
            AdminOperationAuditService auditService,
            AdminOperationRejectionAuditService rejectionAuditService,
            PaymentProperties paymentProperties) {
        this(
                correctionRepository,
                billingAgreementRepository,
                userSubscriptionRepository,
                subscriptionRepository,
                paymentOrderRepository,
                userRepository,
                auditService,
                rejectionAuditService,
                Clock.system(paymentProperties.schedulerZoneId()));
    }

    AdminSubscriptionCorrectionService(
            AdminSubscriptionCorrectionRepository correctionRepository,
            BillingAgreementRepository billingAgreementRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionRepository subscriptionRepository,
            PaymentOrderRepository paymentOrderRepository,
            UserRepository userRepository,
            AdminOperationAuditService auditService,
            AdminOperationRejectionAuditService rejectionAuditService,
            Clock businessClock) {
        this.correctionRepository = correctionRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.rejectionAuditService = rejectionAuditService;
        this.businessClock = businessClock;
    }

    public ResponseDTO<AdminSubscriptionCorrectionPreviewResponse> previewCorrection(
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrectionRequest request) {
        requireActorID(actorDetails);
        UserSubscription current = findSubscription(request.userSubscriptionId());
        Subscription target = findTargetSubscription(request.targetSubscriptionId());
        BillingAgreement agreement = findAgreement(current.getUser());
        ValidationFailure failure = validateRequest(current, target, agreement, request);
        return ResponseDTO.<AdminSubscriptionCorrectionPreviewResponse>builder()
                .data(AdminSubscriptionCorrectionPreviewResponse.of(
                        current,
                        target,
                        request,
                        agreement,
                        failure == null,
                        failure == null ? null : failure.message()))
                .build();
    }

    public ResponseDTO<AdminSubscriptionCorrectionResponse> listCorrections(
            CustomUserDetails actorDetails,
            int page,
            int size) {
        requireActorID(actorDetails);
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AdminSubscriptionCorrectionResponse> result = correctionRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminSubscriptionCorrectionResponse::from);
        return ResponseDTO.<AdminSubscriptionCorrectionResponse>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }

    public ResponseDTO<AdminSubscriptionCorrectionResponse> getCorrection(
            CustomUserDetails actorDetails,
            Long correctionID) {
        requireActorID(actorDetails);
        AdminSubscriptionCorrection correction = correctionRepository.findDetailedById(correctionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return singleResponse(correction);
    }

    public Optional<ResponseDTO<AdminSubscriptionCorrectionResponse>> getOpenCorrection(
            CustomUserDetails actorDetails,
            Long userSubscriptionID) {
        requireActorID(actorDetails);
        findSubscription(userSubscriptionID);
        return correctionRepository
                .findFirstByUserSubscription_IdAndStatusInOrderByCreatedAtDescIdDesc(
                        userSubscriptionID,
                        NON_TERMINAL_STATUSES)
                .map(this::singleResponse);
    }

    @Transactional
    public ResponseDTO<AdminSubscriptionCorrectionResponse> requestCorrection(
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrectionRequest request) {
        Long actorID = actorDetails == null ? null : actorDetails.getId();
        Long userSubscriptionID = request.userSubscriptionId();
        UserSubscription current = null;
        BillingAgreement agreement = null;
        try {
            actorID = requireActorID(actorDetails);
            UserSubscription observed = findSubscription(userSubscriptionID);
            agreement = lockAgreement(observed.getUser().getId());
            current = lockSubscription(userSubscriptionID);
            assertSameUser(observed, current);
            Subscription target = lockTargetSubscription(request.targetSubscriptionId());

            if (!correctionRepository.findNonTerminalByUserSubscriptionIDForUpdate(
                    current.getId(), NON_TERMINAL_STATUSES).isEmpty()) {
                throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
            }
            ValidationFailure failure = validateRequest(current, target, agreement, request);
            if (failure != null) {
                throw failure.asException();
            }

            User actor = lockAdminActor(actorID);
            AdminSubscriptionCorrection correction = correctionRepository.save(
                    AdminSubscriptionCorrection.builder()
                            .userSubscription(current)
                            .user(current.getUser())
                            .billingAgreement(agreement)
                            .beforeSubscription(current.getSubscription())
                            .beforeBillingCycle(current.getBillingCycle())
                            .beforeStatus(current.getStatus())
                            .beforeExpiresAt(current.getExpiresAt())
                            .beforePendingSubscription(current.getPendingSubscription())
                            .beforePendingBillingCycle(current.getPendingBillingCycle())
                            .targetSubscription(target)
                            .targetBillingCycle(request.targetBillingCycle())
                            .targetStatus(request.targetStatus())
                            .targetExpiresAt(request.targetExpiresAt())
                            .clearPendingChange(request.clearPendingChange())
                            .cancelBillingAgreement(request.cancelBillingAgreement())
                            .beforeBillingAgreementStatus(agreement == null ? null : agreement.getStatus())
                            .afterBillingAgreementStatus(agreement == null ? null : agreement.getStatus())
                            .reasonNote(request.reasonNote().trim())
                            .requestedBy(actor)
                            .build());
            return singleResponse(correction);
        } catch (BusinessException exception) {
            recordRequestRejectionSafely(
                    actorID,
                    userSubscriptionID,
                    current,
                    agreement,
                    exception);
            throw exception;
        }
    }

    @Transactional
    public ResponseDTO<AdminSubscriptionCorrectionResponse> approveCorrection(
            Long correctionID,
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrectionApproveRequest request) {
        Long actorID = actorDetails == null ? null : actorDetails.getId();
        AdminSubscriptionCorrection correction = null;
        try {
            actorID = requireActorID(actorDetails);
            correction = lockCorrection(correctionID);
            if (correction.getStatus() != AdminSubscriptionCorrectionStatus.REQUESTED) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
            }
            String approvalNote = normalizeOptionalNote(request.note());
            User actor = lockAdminActor(actorID);
            correction.approve(actor, approvalNote, LocalDateTime.now(businessClock));
            return singleResponse(correction);
        } catch (BusinessException exception) {
            recordApprovalRejectionSafely(actorID, correctionID, correction, exception);
            throw exception;
        }
    }

    @Transactional
    public ResponseDTO<AdminSubscriptionCorrectionResponse> executeCorrection(
            Long correctionID,
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrectionExecuteRequest request) {
        Long actorID = requireActorID(actorDetails);
        AdminSubscriptionCorrection correction = null;
        UserSubscription current = null;
        BillingAgreement agreement = null;
        try {
            AdminSubscriptionCorrectionRepository.ExecutionLockProjection lockTargets =
                    observeExecutionLockTargets(correctionID);
            agreement = lockAgreement(lockTargets.getUserID());
            current = lockSubscription(lockTargets.getUserSubscriptionID());
            Subscription target = lockTargetSubscription(lockTargets.getTargetSubscriptionID());
            correction = lockCorrection(correctionID);
            assertExecutionLockTargets(lockTargets, correction, agreement, current, target);

            if (correction.getStatus() == AdminSubscriptionCorrectionStatus.SUCCEEDED) {
                return singleResponse(correction);
            }
            if (correction.getStatus() != AdminSubscriptionCorrectionStatus.APPROVED) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
            }

            if (!matchesBeforeState(current, correction)
                    || !matchesBeforeAgreementState(agreement, correction)) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
            }
            assertTargetStillValid(current, target, correction);
            assertNoProviderOutcomePending(agreement);

            String executionNote = normalizeOptionalNote(request.note());
            User actor = lockAdminActor(actorID);
            correction.markProcessing(actor, executionNote);
            current.applyEntitlementCorrection(
                    target,
                    correction.getTargetBillingCycle(),
                    correction.getTargetStatus(),
                    correction.getTargetExpiresAt(),
                    correction.isClearPendingChange());

            if (correction.isCancelBillingAgreement()
                    && agreement != null
                    && agreement.getStatus() != BillingAgreementStatus.CANCELLED
                    && agreement.getStatus() != BillingAgreementStatus.EXPIRED) {
                agreement.cancel();
            }
            BillingAgreementStatus afterAgreementStatus = agreement == null ? null : agreement.getStatus();
            correction.markSucceeded(afterAgreementStatus, LocalDateTime.now(businessClock));
            auditService.recordUserSubscriptionCorrectionSuccess(actor.getId(), correction, current);
            return singleResponse(correction);
        } catch (BusinessException exception) {
            recordExecutionRejectionSafely(
                    actorDetails,
                    correction,
                    current,
                    agreement,
                    exception);
            throw exception;
        }
    }

    private ValidationFailure validateRequest(
            UserSubscription current,
            Subscription target,
            BillingAgreement agreement,
            AdminSubscriptionCorrectionRequest request) {
        if (request.reasonNote() == null || request.reasonNote().isBlank()) {
            return invalid("A nonblank operator reason is required.");
        }
        if (request.reasonNote().length() > 500) {
            return invalid("The operator reason must not exceed 500 characters.");
        }
        ValidationFailure targetFailure = validateTarget(
                current,
                target,
                request.targetStatus(),
                request.targetExpiresAt());
        if (targetFailure != null) {
            return targetFailure;
        }
        if (hasProviderOutcomePending(agreement)) {
            return new ValidationFailure(
                    BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE,
                    "A payment order can still receive a provider outcome.");
        }
        if (isNoOp(current, target, agreement, request)) {
            return invalid("The requested correction does not change local subscription state.");
        }
        return null;
    }

    private ValidationFailure validateTarget(
            UserSubscription current,
            Subscription target,
            SubscriptionStatus targetStatus,
            LocalDate targetExpiresAt) {
        if (!target.isActive()) {
            return invalid("The target subscription plan is inactive.");
        }
        if (target.getUserType() != current.getUser().getUserType()) {
            return invalid("The target subscription plan does not match the user's type.");
        }
        LocalDate today = LocalDate.now(businessClock);
        if (targetStatus == SubscriptionStatus.EXPIRED && targetExpiresAt.isAfter(today)) {
            return invalid("An expired subscription must expire today or earlier.");
        }
        if (targetStatus != SubscriptionStatus.EXPIRED && targetExpiresAt.isBefore(today)) {
            return invalid("An active or cancelled subscription must expire today or later.");
        }
        return null;
    }

    private void assertTargetStillValid(
            UserSubscription current,
            Subscription target,
            AdminSubscriptionCorrection correction) {
        ValidationFailure failure = validateTarget(
                current,
                target,
                correction.getTargetStatus(),
                correction.getTargetExpiresAt());
        if (failure != null) {
            throw failure.asException();
        }
    }

    private boolean isNoOp(
            UserSubscription current,
            Subscription target,
            BillingAgreement agreement,
            AdminSubscriptionCorrectionRequest request) {
        boolean sameState = Objects.equals(current.getSubscription().getId(), target.getId())
                && current.getBillingCycle() == request.targetBillingCycle()
                && current.getStatus() == request.targetStatus()
                && Objects.equals(current.getExpiresAt(), request.targetExpiresAt());
        boolean pendingUnchanged = !request.clearPendingChange() || !current.hasPending();
        boolean agreementUnchanged = !request.cancelBillingAgreement()
                || agreement == null
                || agreement.getStatus() == BillingAgreementStatus.CANCELLED
                || agreement.getStatus() == BillingAgreementStatus.EXPIRED;
        return sameState && pendingUnchanged && agreementUnchanged;
    }

    private boolean matchesBeforeState(
            UserSubscription current,
            AdminSubscriptionCorrection correction) {
        return sameEntityID(current.getSubscription(), correction.getBeforeSubscription())
                && current.getBillingCycle() == correction.getBeforeBillingCycle()
                && current.getStatus() == correction.getBeforeStatus()
                && Objects.equals(current.getExpiresAt(), correction.getBeforeExpiresAt())
                && sameEntityID(current.getPendingSubscription(), correction.getBeforePendingSubscription())
                && current.getPendingBillingCycle() == correction.getBeforePendingBillingCycle();
    }

    private boolean matchesBeforeAgreementState(
            BillingAgreement agreement,
            AdminSubscriptionCorrection correction) {
        BillingAgreement expectedAgreement = correction.getBillingAgreement();
        if (agreement == null || expectedAgreement == null) {
            return agreement == null
                    && expectedAgreement == null
                    && correction.getBeforeBillingAgreementStatus() == null;
        }
        if (!Objects.equals(agreement.getId(), expectedAgreement.getId())
                || agreement.getStatus() != correction.getBeforeBillingAgreementStatus()) {
            return false;
        }
        return agreement.getUpdatedAt() != null
                && correction.getCreatedAt() != null
                && !agreement.getUpdatedAt().isAfter(correction.getCreatedAt());
    }

    private void assertNoProviderOutcomePending(BillingAgreement agreement) {
        if (hasProviderOutcomePending(agreement)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private boolean hasProviderOutcomePending(BillingAgreement agreement) {
        return agreement != null && paymentOrderRepository.existsByBillingAgreementAndPurposeInAndStatusIn(
                agreement,
                PROVIDER_CHARGE_PURPOSES,
                PROVIDER_OUTCOME_PENDING_STATUSES);
    }

    private UserSubscription findSubscription(Long userSubscriptionID) {
        return userSubscriptionRepository.findById(userSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private UserSubscription lockSubscription(Long userSubscriptionID) {
        return userSubscriptionRepository.findByIdForUpdate(userSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private Subscription findTargetSubscription(Long targetSubscriptionID) {
        return subscriptionRepository.findById(targetSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private Subscription lockTargetSubscription(Long targetSubscriptionID) {
        return subscriptionRepository.findByIdForUpdate(targetSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
    }

    private BillingAgreement findAgreement(User user) {
        return billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER).orElse(null);
    }

    private BillingAgreement lockAgreement(Long userID) {
        return billingAgreementRepository.findByUserIDAndProviderForUpdate(userID, RECURRING_PROVIDER)
                .orElse(null);
    }

    private AdminSubscriptionCorrection lockCorrection(Long correctionID) {
        return correctionRepository.findByIDForUpdate(correctionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private AdminSubscriptionCorrectionRepository.ExecutionLockProjection observeExecutionLockTargets(
            Long correctionID) {
        return correctionRepository.findExecutionLockProjectionByID(correctionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private void assertExecutionLockTargets(
            AdminSubscriptionCorrectionRepository.ExecutionLockProjection observed,
            AdminSubscriptionCorrection correction,
            BillingAgreement agreement,
            UserSubscription userSubscription,
            Subscription targetSubscription) {
        Long correctionAgreementID = correction.getBillingAgreement() == null
                ? null : correction.getBillingAgreement().getId();
        Long lockedAgreementID = agreement == null ? null : agreement.getId();
        if (!Objects.equals(observed.getCorrectionID(), correction.getId())
                || !Objects.equals(observed.getUserID(), correction.getUser().getId())
                || !Objects.equals(observed.getUserID(), userSubscription.getUser().getId())
                || !Objects.equals(observed.getUserSubscriptionID(), correction.getUserSubscription().getId())
                || !Objects.equals(observed.getUserSubscriptionID(), userSubscription.getId())
                || !Objects.equals(observed.getTargetSubscriptionID(), correction.getTargetSubscription().getId())
                || !Objects.equals(observed.getTargetSubscriptionID(), targetSubscription.getId())
                || !Objects.equals(observed.getBillingAgreementID(), correctionAgreementID)
                || !Objects.equals(observed.getBillingAgreementID(), lockedAgreementID)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }

    private User lockAdminActor(Long actorID) {
        // 교정 도메인 락 뒤에만 행위자를 잠가 탈퇴 락 순서와 역전되지 않게 한다.
        User actor = userRepository.findByIdForUpdate(actorID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (actor.isDeleted() || actor.getRole() != UserRole.ADMIN) {
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        return actor;
    }

    private Long requireActorID(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        return actorDetails.getId();
    }

    private void assertSameUser(UserSubscription observed, UserSubscription locked) {
        if (!Objects.equals(observed.getUser().getId(), locked.getUser().getId())) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }

    private boolean sameEntityID(Subscription left, Subscription right) {
        if (left == null || right == null) {
            return left == right;
        }
        return Objects.equals(left.getId(), right.getId());
    }

    private void recordRequestRejectionSafely(
            Long actorID,
            Long userSubscriptionID,
            UserSubscription current,
            BillingAgreement agreement,
            BusinessException originalException) {
        if (userSubscriptionID == null) {
            return;
        }
        try {
            String auditState = current == null
                    ? AdminOperationAuditState.userSubscriptionCorrection(userSubscriptionID, null)
                    : userSubscriptionAuditState(current, agreement);
            rejectionAuditService.recordUserSubscriptionCorrectionRequestRejected(
                    actorID,
                    userSubscriptionID,
                    auditState,
                    originalException.getErrorCode());
        } catch (RuntimeException auditException) {
            preserveOriginalRejection(
                    "request",
                    userSubscriptionID,
                    originalException,
                    auditException);
        }
    }

    private void recordApprovalRejectionSafely(
            Long actorID,
            Long correctionID,
            AdminSubscriptionCorrection correction,
            BusinessException originalException) {
        if (correctionID == null) {
            return;
        }
        try {
            Long userSubscriptionID = correction == null || correction.getUserSubscription() == null
                    ? null
                    : correction.getUserSubscription().getId();
            AdminSubscriptionCorrectionStatus status = correction == null ? null : correction.getStatus();
            String auditState = AdminOperationAuditState.userSubscriptionCorrection(
                    userSubscriptionID,
                    status);
            rejectionAuditService.recordUserSubscriptionCorrectionApprovalRejected(
                    actorID,
                    correctionID,
                    auditState,
                    originalException.getErrorCode());
        } catch (RuntimeException auditException) {
            preserveOriginalRejection(
                    "approval",
                    correctionID,
                    originalException,
                    auditException);
        }
    }

    private void preserveOriginalRejection(
            String phase,
            Long targetID,
            BusinessException originalException,
            RuntimeException auditException) {
        originalException.addSuppressed(auditException);
        log.warn(
                "Failed to persist administrator subscription correction rejection audit. "
                        + "phase={}, targetId={}, reason={}, exception={}",
                phase,
                targetID,
                originalException.getErrorCode(),
                auditException.getClass().getSimpleName());
    }

    private void recordExecutionRejectionSafely(
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrection correction,
            UserSubscription current,
            BillingAgreement agreement,
            BusinessException originalException) {
        try {
            recordExecutionRejection(
                    actorDetails,
                    correction,
                    current,
                    agreement,
                    originalException.getErrorCode());
        } catch (RuntimeException auditException) {
            originalException.addSuppressed(auditException);
            log.warn(
                    "Failed to persist administrator subscription correction rejection audit. "
                            + "correctionId={}, reason={}, exception={}",
                    correction == null ? null : correction.getId(),
                    originalException.getErrorCode(),
                    auditException.getClass().getSimpleName());
        }
    }

    private void recordExecutionRejection(
            CustomUserDetails actorDetails,
            AdminSubscriptionCorrection correction,
            UserSubscription current,
            BillingAgreement agreement,
            BUSINESS_ERROR reason) {
        if (correction == null || correction.getUserSubscription() == null) {
            return;
        }
        UserSubscription state = current == null ? correction.getUserSubscription() : current;
        BillingAgreement stateAgreement = agreement == null
                ? correction.getBillingAgreement()
                : agreement;
        String auditState = userSubscriptionAuditState(state, stateAgreement);
        rejectionAuditService.recordUserSubscriptionCorrectionRejected(
                actorDetails == null ? null : actorDetails.getId(),
                correction.getUserSubscription().getId(),
                auditState,
                reason,
                null);
    }

    private String userSubscriptionAuditState(
            UserSubscription state,
            BillingAgreement agreement) {
        return AdminOperationAuditState.userSubscription(
                state.getSubscription().getId(),
                state.getBillingCycle(),
                state.getStatus(),
                state.getExpiresAt().toString(),
                state.getPendingSubscription() == null ? null : state.getPendingSubscription().getId(),
                state.getPendingBillingCycle(),
                agreement == null ? null : agreement.getStatus());
    }

    private String normalizeOptionalNote(String note) {
        if (note == null) {
            return null;
        }
        if (note.length() > 500) {
            throw invalid("The workflow note must not exceed 500 characters.").asException();
        }
        String normalized = note.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private ResponseDTO<AdminSubscriptionCorrectionResponse> singleResponse(
            AdminSubscriptionCorrection correction) {
        return ResponseDTO.<AdminSubscriptionCorrectionResponse>builder()
                .data(AdminSubscriptionCorrectionResponse.from(correction))
                .build();
    }

    private ValidationFailure invalid(String message) {
        return new ValidationFailure(BUSINESS_ERROR.INVALID_ARGUMENT, message);
    }

    private record ValidationFailure(BUSINESS_ERROR error, String message) {
        private BusinessException asException() {
            return new BusinessException(error, new IllegalStateException(message));
        }
    }
}
