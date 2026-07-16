package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.payment.provider.recurring.ProviderPaymentLookupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentReconciliationTransactionService {

    private static final EnumSet<PaymentPurpose> FINAL_PAYMENT_PURPOSES = EnumSet.of(
            PaymentPurpose.SUBSCRIBE,
            PaymentPurpose.UPGRADE,
            PaymentPurpose.RENEWAL
    );

    private final PaymentOrderRepository paymentOrderRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentCommandTransactionService paymentCommandTransactionService;

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public LocalReconciliationBatch reconcileDoneOrderBatch(
            Long lastSeenID,
            int pageSize) {
        List<PaymentOrder> orders = paymentOrderRepository.findLocalReconciliationCandidates(
                PaymentOrderStatus.DONE,
                FINAL_PAYMENT_PURPOSES,
                lastSeenID,
                PageRequest.of(0, pageSize));
        List<PaymentReconciliationService.LocalReconciliationIssue> issues = new ArrayList<>();

        for (PaymentOrder order : orders) {
            if (!subscriptionPaymentRepository.existsByPaymentOrder(order)) {
                issues.add(localIssue(order, PaymentReconciliationIssueType.DONE_ORDER_WITHOUT_PAYMENT));
            }
        }

        return localBatch(lastSeenID, pageSize, orders, issues);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public LocalReconciliationBatch reconcileActiveAgreementBatch(
            Long lastSeenID,
            int pageSize,
            LocalDate today) {
        List<BillingAgreement> agreements = billingAgreementRepository.findLocalReconciliationCandidates(
                BillingAgreementStatus.ACTIVE,
                lastSeenID,
                PageRequest.of(0, pageSize));
        List<PaymentReconciliationService.LocalReconciliationIssue> issues = new ArrayList<>();
        for (BillingAgreement agreement : agreements) {
            boolean hasActiveSubscription = userSubscriptionRepository
                    .findActiveByUser(agreement.getUser(), today)
                    .isPresent();
            if (!hasActiveSubscription) {
                issues.add(localIssue(agreement));
            }
        }

        return localBatch(lastSeenID, pageSize, agreements, issues);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Long> findProviderCandidateIDs(
            LocalDateTime staleBefore,
            Long lastSeenID,
            int pageSize) {
        return paymentOrderRepository.findReconciliationCandidateIDs(
                staleBefore,
                lastSeenID,
                PageRequest.of(0, pageSize));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public List<Long> findCompletedProviderCandidateIDs(
            LocalDateTime createdAfter,
            Long lastSeenID,
            int pageSize) {
        return paymentOrderRepository.findCompletedProviderReconciliationCandidateIDs(
                createdAfter,
                lastSeenID,
                PageRequest.of(0, pageSize));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<CompletedProviderLookupClaim> loadCompletedProviderLookup(
            Long paymentOrderID,
            LocalDateTime createdAfter) {
        return paymentOrderRepository.findById(paymentOrderID)
                .filter(order -> order.getStatus() == PaymentOrderStatus.DONE)
                .filter(order -> FINAL_PAYMENT_PURPOSES.contains(order.getPurpose()))
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(createdAfter))
                .map(order -> new CompletedProviderLookupClaim(
                        toClaim(order, true, null),
                        paymentRefundRepository.existsByPaymentOrder_IdAndStatus(
                                order.getId(), PaymentRefundStatus.SUCCEEDED)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<ProviderLookupClaim> claimProviderLookup(
            Long paymentOrderID,
            LocalDateTime staleBefore) {
        PaymentOrder seed = paymentOrderRepository.findById(paymentOrderID).orElse(null);
        if (seed == null) {
            return Optional.empty();
        }

        PaymentOrderRepository.CommandLockProjection projection = paymentOrderRepository
                .findCommandLockProjectionByOrderId(seed.getOrderId())
                .orElse(null);
        if (projection == null || projection.getBillingAgreementID() == null) {
            return Optional.of(toClaim(seed, false, "LOCAL_RELATIONSHIP_MISMATCH"));
        }

        BillingAgreement agreement = billingAgreementRepository
                .findByIDForUpdate(projection.getBillingAgreementID())
                .orElse(null);
        UserSubscription subscription = projection.getUserSubscriptionID() == null
                ? null
                : userSubscriptionRepository.findByIdForUpdate(projection.getUserSubscriptionID()).orElse(null);
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(seed.getOrderId()).orElse(null);
        if (agreement == null || order == null || !isCandidateState(order, staleBefore)) {
            return Optional.empty();
        }

        boolean validRelationships = Objects.equals(order.getId(), paymentOrderID)
                && order.getBillingAgreement() != null
                && Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                && Objects.equals(order.getUser().getId(), projection.getUserID())
                && order.getPurpose() == projection.getPurpose()
                && Objects.equals(entityID(order.getUserSubscription()), projection.getUserSubscriptionID())
                && Objects.equals(entityID(subscription), projection.getUserSubscriptionID());
        boolean validPurposeState = validPurposeState(order, agreement, subscription);
        boolean mutationEligible = validRelationships
                && validPurposeState
                && !isBlank(order.getCommandKey());
        String failureCode = mutationEligible ? null : "LOCAL_EVIDENCE_INVALID";
        return Optional.of(toClaim(order, mutationEligible, failureCode));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<ProviderLookupClaim> loadProviderLookup(
            Long paymentOrderID,
            LocalDateTime staleBefore) {
        PaymentOrder order = paymentOrderRepository.findById(paymentOrderID).orElse(null);
        if (order == null || !isCandidateState(order, staleBefore)) {
            return Optional.empty();
        }

        PaymentOrderRepository.CommandLockProjection projection = paymentOrderRepository
                .findCommandLockProjectionByOrderId(order.getOrderId())
                .orElse(null);
        if (projection == null || projection.getBillingAgreementID() == null) {
            return Optional.of(toClaim(order, false, "LOCAL_RELATIONSHIP_MISMATCH"));
        }

        BillingAgreement agreement = billingAgreementRepository
                .findById(projection.getBillingAgreementID())
                .orElse(null);
        UserSubscription subscription = projection.getUserSubscriptionID() == null
                ? null
                : userSubscriptionRepository.findById(projection.getUserSubscriptionID()).orElse(null);
        boolean validRelationships = agreement != null
                && Objects.equals(order.getId(), paymentOrderID)
                && order.getBillingAgreement() != null
                && Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                && Objects.equals(order.getUser().getId(), projection.getUserID())
                && order.getPurpose() == projection.getPurpose()
                && Objects.equals(entityID(order.getUserSubscription()), projection.getUserSubscriptionID())
                && Objects.equals(entityID(subscription), projection.getUserSubscriptionID());
        boolean validPurposeState = agreement != null
                && validPurposeState(order, agreement, subscription);
        boolean mutationEligible = validRelationships
                && validPurposeState
                && !isBlank(order.getCommandKey());
        return Optional.of(toClaim(
                order,
                mutationEligible,
                mutationEligible ? null : "LOCAL_EVIDENCE_INVALID"));
    }

    public EvidenceAssessment assessProviderEvidence(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult result) {
        if (!claim.mutationEligible()) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                    claim.localEligibilityFailure(),
                    "Local reconciliation evidence is incomplete or contradictory.");
        }
        if (result == null) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                    "PROVIDER_LOOKUP_RESULT_MISSING",
                    "Provider lookup returned no evidence.");
        }
        if (!result.found()) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.PROVIDER_LOOKUP_FAILED,
                    safeCode(result.failureCode(), "PROVIDER_PAYMENT_NOT_FOUND"),
                    "Provider lookup did not return authoritative payment evidence.");
        }
        if (result.provider() != claim.provider()) {
            return strictMismatch("PROVIDER_MISMATCH", "Provider type does not match the local command.");
        }
        if (!Objects.equals(result.orderId(), claim.orderID())) {
            return strictMismatch("ORDER_ID_MISMATCH", "Provider order ID does not match the local command.");
        }
        if (!"DONE".equals(result.status())) {
            return strictMismatch("PROVIDER_STATUS_MISMATCH", "Provider payment is not exactly DONE.");
        }
        if (result.totalAmount() == null || claim.amount().compareTo(result.totalAmount()) != 0) {
            return EvidenceAssessment.mismatch(
                    PaymentReconciliationIssueType.AMOUNT_MISMATCH,
                    "AMOUNT_MISMATCH",
                    "Provider amount does not match the local command.");
        }
        if (!Objects.equals(result.currency(), claim.currency())) {
            return strictMismatch("CURRENCY_MISMATCH", "Provider currency does not match the local command.");
        }
        if (isBlank(result.transactionId())) {
            return strictMismatch(
                    "PROVIDER_TRANSACTION_MISSING",
                    "Provider transaction ID is missing.");
        }
        if (claim.localStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED
                && !Objects.equals(claim.providerTransactionID(), result.transactionId())) {
            return strictMismatch(
                    "PROVIDER_TRANSACTION_MISMATCH",
                    "Provider transaction ID conflicts with the persisted success evidence.");
        }
        return EvidenceAssessment.exact();
    }

    @Transactional(propagation = Propagation.NEVER)
    public PaymentCommandTransactionService.ReconciliationFinalizationTarget applyExactProviderSuccess(
            ProviderLookupClaim claim,
            ProviderPaymentLookupResult result,
            LocalDateTime staleBefore) {
        if (!assessProviderEvidence(claim, result).exactDone()) {
            throw new IllegalArgumentException("Exact provider DONE evidence is required for reconciliation.");
        }
        if (claim.localStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            return new PaymentCommandTransactionService.ReconciliationFinalizationTarget(
                    claim.purpose(),
                    claim.userID(),
                    claim.billingAgreementID(),
                    claim.orderID());
        }
        if (claim.localStatus() != PaymentOrderStatus.PROCESSING
                && claim.localStatus() != PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION) {
            throw new IllegalArgumentException("Payment order is not eligible for reconciled provider success.");
        }
        return paymentCommandTransactionService.recordProviderSuccessFromReconciliation(
                claim.billingAgreementID(),
                claim.orderID(),
                result.transactionId(),
                result.providerPayload(),
                staleBefore);
    }

    private boolean isCandidateState(PaymentOrder order, LocalDateTime staleBefore) {
        return order.getStatus() == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION
                || order.getStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED
                || order.isProcessingStale(staleBefore);
    }

    private boolean validPurposeState(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription subscription) {
        return switch (order.getPurpose()) {
            case SUBSCRIBE -> subscription == null
                    && agreement.isInitialSubscriptionFinalizationEligible();
            case UPGRADE -> subscription != null
                    && order.getUpgradeTargetBillingCycle() != null;
            case RENEWAL -> subscription != null
                    && order.getBillingPeriodStart() != null
                    && Objects.equals(order.getBillingPeriodStart(), agreement.getNextBillingAt());
            default -> false;
        };
    }

    private ProviderLookupClaim toClaim(
            PaymentOrder order,
            boolean mutationEligible,
            String localEligibilityFailure) {
        return new ProviderLookupClaim(
                order.getId(),
                order.getUser().getId(),
                order.getBillingAgreement() == null ? null : order.getBillingAgreement().getId(),
                order.getUserSubscription() == null ? null : order.getUserSubscription().getId(),
                order.getOrderId(),
                order.getCommandKey(),
                order.getProvider(),
                order.getPurpose(),
                order.getStatus(),
                order.getAmount(),
                order.getCurrency(),
                order.getPgTransactionId(),
                mutationEligible,
                localEligibilityFailure);
    }

    private PaymentReconciliationService.LocalReconciliationIssue localIssue(
            PaymentOrder order,
            PaymentReconciliationIssueType issueType) {
        return new PaymentReconciliationService.LocalReconciliationIssue(
                issueType,
                order.getId(),
                order.getUser().getId(),
                order.getBillingAgreement() == null ? null : order.getBillingAgreement().getId(),
                order.getOrderId(),
                order.getProvider(),
                order.getPurpose(),
                order.getStatus().name(),
                order.getAmount());
    }

    private PaymentReconciliationService.LocalReconciliationIssue localIssue(BillingAgreement agreement) {
        return new PaymentReconciliationService.LocalReconciliationIssue(
                PaymentReconciliationIssueType.ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION,
                null,
                agreement.getUser().getId(),
                agreement.getId(),
                null,
                agreement.getProvider(),
                null,
                agreement.getStatus().name(),
                null);
    }

    private LocalReconciliationBatch localBatch(
            Long previousLastSeenID,
            int pageSize,
            List<?> candidates,
            List<PaymentReconciliationService.LocalReconciliationIssue> issues) {
        long lastSeenID = candidates.isEmpty()
                ? previousLastSeenID
                : entityID(candidates.get(candidates.size() - 1));
        return new LocalReconciliationBatch(
                candidates.size(),
                lastSeenID,
                candidates.size() < pageSize,
                List.copyOf(issues));
    }

    private long entityID(Object candidate) {
        if (candidate instanceof PaymentOrder paymentOrder) {
            return paymentOrder.getId();
        }
        if (candidate instanceof BillingAgreement billingAgreement) {
            return billingAgreement.getId();
        }
        throw new IllegalArgumentException("Unsupported reconciliation candidate type.");
    }

    private EvidenceAssessment strictMismatch(String failureCode, String failureMessage) {
        return EvidenceAssessment.mismatch(
                PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED,
                failureCode,
                failureMessage);
    }

    private Long entityID(UserSubscription subscription) {
        return subscription == null ? null : subscription.getId();
    }

    private String safeCode(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record ProviderLookupClaim(
            Long paymentOrderID,
            Long userID,
            Long billingAgreementID,
            Long userSubscriptionID,
            String orderID,
            String commandKey,
            com.atstudio.atstudio.entity.enums.PaymentProviderType provider,
            PaymentPurpose purpose,
            PaymentOrderStatus localStatus,
            BigDecimal amount,
            String currency,
            String providerTransactionID,
            boolean mutationEligible,
            String localEligibilityFailure) {
    }

    public record CompletedProviderLookupClaim(
            ProviderLookupClaim claim,
            boolean locallyRefundedOrCancelled) {
    }

    public record LocalReconciliationBatch(
            int checked,
            long lastSeenID,
            boolean exhausted,
            List<PaymentReconciliationService.LocalReconciliationIssue> issues) {
    }

    public record EvidenceAssessment(
            boolean exactDone,
            PaymentReconciliationIssueType issueType,
            String failureCode,
            String failureMessage) {

        static EvidenceAssessment exact() {
            return new EvidenceAssessment(true, null, null, null);
        }

        static EvidenceAssessment mismatch(
                PaymentReconciliationIssueType issueType,
                String failureCode,
                String failureMessage) {
            return new EvidenceAssessment(false, issueType, failureCode, failureMessage);
        }
    }
}
