package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundApproveRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundCreateRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundExecuteRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundPreviewResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentRefundResponse;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProvider;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderCommand;
import com.atstudio.atstudio.service.payment.provider.refund.PaymentRefundProviderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPaymentRefundService {

    private static final Set<PaymentRefundStatus> RESERVED_REFUND_STATUSES = Set.of(
            PaymentRefundStatus.REQUESTED,
            PaymentRefundStatus.APPROVED,
            PaymentRefundStatus.PROCESSING,
            PaymentRefundStatus.SUCCEEDED,
            PaymentRefundStatus.PENDING_PROVIDER_CONFIRMATION
    );

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;
    private final PaymentRefundTransactionService refundTransactionService;
    private final List<PaymentRefundProvider> refundProviders;

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentRefundResponse> listRefunds(int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminPaymentRefundResponse> result = paymentRefundRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminPaymentRefundResponse::from);
        return paged(result, page, size);
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentRefundResponse> getRefund(Long refundId) {
        PaymentRefund refund = paymentRefundRepository.findWithGraphById(refundId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return ResponseDTO.<AdminPaymentRefundResponse>builder()
                .data(AdminPaymentRefundResponse.from(refund))
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentRefundPreviewResponse> previewRefund(Long subscriptionPaymentId) {
        SubscriptionPayment payment = findSubscriptionPayment(subscriptionPaymentId);
        BigDecimal reservedAmount = reservedRefundAmount(payment);
        BigDecimal refundableAmount = payment.getAmount().subtract(reservedAmount).max(BigDecimal.ZERO);
        boolean refundable = canRefund(payment) && refundableAmount.compareTo(BigDecimal.ZERO) > 0;
        return ResponseDTO.<AdminPaymentRefundPreviewResponse>builder()
                .data(AdminPaymentRefundPreviewResponse.of(
                        payment,
                        reservedAmount,
                        refundableAmount,
                        refundable,
                        refundable ? null : nonRefundableReason(payment, refundableAmount)))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentRefundResponse> createRefund(
            CustomUserDetails actorDetails,
            AdminPaymentRefundCreateRequest request) {
        SubscriptionPayment payment = findSubscriptionPaymentForUpdate(request.subscriptionPaymentId());
        validateLockedRefundReservation(payment, request.amount());

        User actor = resolveActor(actorDetails);
        PaymentRefund refund = paymentRefundRepository.save(PaymentRefund.builder()
                .subscriptionPayment(payment)
                .paymentOrder(payment.getPaymentOrder())
                .user(payment.getUser())
                .provider(payment.getProvider())
                .status(PaymentRefundStatus.REQUESTED)
                .amount(request.amount())
                .currency("KRW")
                .reasonCode(request.reasonCode())
                .reasonNote(truncate(request.reasonNote()))
                .idempotencyKey("ATS-REFUND-" + UUID.randomUUID())
                .providerPaymentKey(payment.getPgTransactionId())
                .requestedBy(actor)
                .build());
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                PaymentOperationAuditAction.PAYMENT_REFUND_REQUESTED,
                null,
                refund.getStatus(),
                request.reasonNote());
        return ResponseDTO.<AdminPaymentRefundResponse>builder()
                .data(AdminPaymentRefundResponse.from(refund))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentRefundResponse> approveRefund(
            Long refundId,
            CustomUserDetails actorDetails,
            AdminPaymentRefundApproveRequest request) {
        PaymentRefund refund = findRefundForUpdate(refundId);
        if (refund.getStatus() != PaymentRefundStatus.REQUESTED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        PaymentRefundStatus beforeStatus = refund.getStatus();
        PaymentOperationAuditAction action = refund.approve(resolveActor(actorDetails));
        auditLogService.recordPaymentRefundEvent(
                actorDetails,
                refund,
                action,
                beforeStatus,
                refund.getStatus(),
                request.note());
        return ResponseDTO.<AdminPaymentRefundResponse>builder()
                .data(AdminPaymentRefundResponse.from(refund))
                .build();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResponseDTO<AdminPaymentRefundResponse> executeRefund(
            Long refundId,
            CustomUserDetails actorDetails,
            AdminPaymentRefundExecuteRequest request) {
        return executeRefundAt(refundId, actorDetails, request, LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResponseDTO<AdminPaymentRefundResponse> executeRefundAt(
            Long refundId,
            CustomUserDetails actorDetails,
            AdminPaymentRefundExecuteRequest request,
            LocalDateTime now) {
        PaymentRefundTransactionService.RefundExecutionClaim claim =
                refundTransactionService.claimExecution(refundId, actorDetails, request.note(), now);

        if (claim.executionMode() == PaymentRefundTransactionService.RefundExecutionMode.LOOKUP_ONLY) {
            AdminPaymentRefundResponse response = refundTransactionService.recordReplayUnavailable(
                    refundId,
                    actorDetails,
                    claim.leaseStartedAt());
            return ResponseDTO.<AdminPaymentRefundResponse>builder()
                    .data(response)
                    .build();
        }

        refundTransactionService.validateClaimForExecution(claim);

        PaymentRefundProvider provider = refundProvider(claim.provider());
        PaymentRefundProviderResult providerResult;
        try {
            providerResult = provider.cancelPayment(new PaymentRefundProviderCommand(
                    claim.providerPaymentKey(),
                    claim.orderId(),
                    claim.amount(),
                    claim.reason(),
                    claim.idempotencyKey()));
        } catch (RuntimeException exception) {
            AdminPaymentRefundResponse response =
                    refundTransactionService.recordExecutionException(
                            refundId,
                            actorDetails,
                            claim.leaseStartedAt(),
                            exception);
            return ResponseDTO.<AdminPaymentRefundResponse>builder()
                    .data(response)
                    .build();
        }

        AdminPaymentRefundResponse response =
                refundTransactionService.recordExecutionResult(
                        refundId,
                        actorDetails,
                        claim.leaseStartedAt(),
                        providerResult);
        return ResponseDTO.<AdminPaymentRefundResponse>builder()
                .data(response)
                .build();
    }

    private SubscriptionPayment findSubscriptionPayment(Long subscriptionPaymentId) {
        return subscriptionPaymentRepository.findWithGraphById(subscriptionPaymentId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private SubscriptionPayment findSubscriptionPaymentForUpdate(Long subscriptionPaymentId) {
        return subscriptionPaymentRepository.findWithGraphByIdForUpdate(subscriptionPaymentId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private PaymentRefund findRefundForUpdate(Long refundId) {
        return paymentRefundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private void validateRefundablePayment(SubscriptionPayment payment) {
        if (!canRefund(payment)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
    }

    private boolean canRefund(SubscriptionPayment payment) {
        return payment.getPaymentStatus() == PaymentStatus.DONE
                && payment.getPaymentOrder() != null
                && payment.getProvider() == PaymentProviderType.TOSS_BILLING
                && !isBlank(payment.getPgTransactionId());
    }

    private String nonRefundableReason(SubscriptionPayment payment, BigDecimal refundableAmount) {
        if (payment.getPaymentStatus() != PaymentStatus.DONE) {
            return "Subscription payment is not DONE.";
        }
        if (payment.getPaymentOrder() == null) {
            return "Subscription payment has no linked payment order.";
        }
        if (payment.getProvider() != PaymentProviderType.TOSS_BILLING) {
            return "Only Toss recurring billing payments are refundable in this phase.";
        }
        if (isBlank(payment.getPgTransactionId())) {
            return "Provider payment key is missing.";
        }
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "No refundable amount remains.";
        }
        return "Payment is not refundable.";
    }

    private void validateLockedRefundReservation(SubscriptionPayment payment, BigDecimal amount) {
        validateRefundablePayment(payment);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        BigDecimal reservedAmount = reservedRefundAmount(payment);
        if (reservedAmount.add(amount).compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private BigDecimal reservedRefundAmount(SubscriptionPayment payment) {
        BigDecimal reservedAmount = paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(
                payment,
                RESERVED_REFUND_STATUSES);
        return reservedAmount == null ? BigDecimal.ZERO : reservedAmount;
    }

    private PaymentRefundProvider refundProvider(PaymentProviderType providerType) {
        return refundProviders.stream()
                .filter(provider -> provider.getProviderType() == providerType)
                .findFirst()
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED));
    }

    private User resolveActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            return null;
        }
        return userRepository.findById(actorDetails.getId()).orElse(null);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
    }

    private <T> ResponseDTO<T> paged(Page<T> result, int page, int size) {
        return ResponseDTO.<T>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 500) {
            return value;
        }
        return value.substring(0, 500);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
