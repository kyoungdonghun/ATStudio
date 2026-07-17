package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingKeyCleanupStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class BillingAgreementCleanupTransactionService {

    static final Duration CLEANUP_LEASE = Duration.ofMinutes(15);
    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;
    private static final String STALE_CLEANUP = "STALE_BILLING_KEY_CLEANUP";
    private static final String STALE_CLEANUP_MESSAGE =
            "Billing key cleanup outcome is unknown after its lease expired.";

    private final UserRepository userRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentReconciliationIncidentService incidentService;
    private final PaymentReconciliationIncidentRepository incidentRepository;
    private final PaymentOperationAuditLogService auditLogService;

    public BillingAgreementCleanupTransactionService(
            UserRepository userRepository,
            BillingAgreementRepository billingAgreementRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentReconciliationIncidentService incidentService,
            PaymentReconciliationIncidentRepository incidentRepository,
            PaymentOperationAuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.billingAgreementRepository = billingAgreementRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.incidentService = incidentService;
        this.incidentRepository = incidentRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserCancellationClaim claimUserCancellation(Long userID, LocalDateTime now) {
        requireNow(now);
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        Long agreementID = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .map(BillingAgreement::getId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        BillingAgreement agreement = lockAgreement(agreementID);
        requireOwnerAndProvider(agreement, userID);
        if (agreement.getStatus() == BillingAgreementStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }

        UserSubscription subscription = userSubscriptionRepository.findActiveByUserForUpdate(
                        agreement.getUser(),
                        now.toLocalDate())
                .orElse(null);
        if (agreement.getStatus() != BillingAgreementStatus.CANCELLED) {
            agreement.cancel();
            if (subscription != null) {
                subscription.cancel();
            }
        }

        if (isBlank(agreement.getBillingKeyCiphertext())) {
            agreement.clearIssuedKey();
            resolveCleanupIncident(agreement);
            return UserCancellationClaim.completed(toResponse(agreement, subscription));
        }

        CleanupAction action = claimOrClassify(agreement, now);
        if (action != CleanupAction.CALL_PROVIDER) {
            return UserCancellationClaim.withoutProvider(action, agreement.getId());
        }
        return UserCancellationClaim.callProvider(
                agreement.getId(),
                agreement.getProvider(),
                agreement.getBillingKeyCiphertext(),
                agreement.getBillingKeyCleanupStartedAt());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WithdrawalCleanupClaim claimWithdrawalCleanup(Long agreementID, LocalDateTime now) {
        requireNow(now);
        BillingAgreement agreement = billingAgreementRepository.findByIDForUpdate(agreementID).orElse(null);
        if (!isWithdrawalCleanupEligible(agreement)) {
            return WithdrawalCleanupClaim.withoutProvider(CleanupAction.SKIPPED, agreementID);
        }

        CleanupAction action = claimOrClassify(agreement, now);
        if (action != CleanupAction.CALL_PROVIDER) {
            return WithdrawalCleanupClaim.withoutProvider(action, agreementID);
        }
        return WithdrawalCleanupClaim.callProvider(
                agreement.getId(),
                agreement.getProvider(),
                agreement.getBillingKeyCiphertext(),
                agreement.getBillingKeyCleanupStartedAt());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingAgreementResponse recordUserCancellationResult(
            Long userID,
            UserCancellationClaim claim,
            BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult) {
        BillingAgreement agreement = lockAgreement(claim.agreementID());
        requireOwnerAndProvider(agreement, userID);
        applyProviderResult(agreement, claim.leaseStartedAt(), providerResult);
        UserSubscription subscription = userSubscriptionRepository.findActiveByUserForUpdate(
                        agreement.getUser(),
                        LocalDateTime.now().toLocalDate())
                .orElse(null);
        return toResponse(agreement, subscription);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordWithdrawalCleanupResult(
            WithdrawalCleanupClaim claim,
            BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult) {
        BillingAgreement agreement = lockAgreement(claim.agreementID());
        applyProviderResult(agreement, claim.leaseStartedAt(), providerResult);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markStaleCleanupPending(Long agreementID, LocalDateTime staleBefore) {
        BillingAgreement agreement = billingAgreementRepository.findByIDForUpdate(agreementID).orElse(null);
        if (agreement == null || !agreement.isBillingKeyCleanupProcessingStale(staleBefore)) {
            return false;
        }

        agreement.markBillingKeyCleanupPendingProviderConfirmation(
                agreement.getBillingKeyCleanupStartedAt());
        recordCleanupFailure(
                agreement,
                STALE_CLEANUP,
                STALE_CLEANUP_MESSAGE);
        return true;
    }

    private CleanupAction claimOrClassify(BillingAgreement agreement, LocalDateTime now) {
        return switch (agreement.getBillingKeyCleanupStatus()) {
            case NONE, REQUIRED -> {
                agreement.claimBillingKeyCleanup(now);
                yield CleanupAction.CALL_PROVIDER;
            }
            case PROCESSING -> {
                if (agreement.isBillingKeyCleanupProcessingStale(now.minus(CLEANUP_LEASE))) {
                    agreement.markBillingKeyCleanupPendingProviderConfirmation(
                            agreement.getBillingKeyCleanupStartedAt());
                    recordCleanupFailure(
                            agreement,
                            STALE_CLEANUP,
                            STALE_CLEANUP_MESSAGE);
                    yield CleanupAction.PENDING_PROVIDER_CONFIRMATION;
                }
                yield CleanupAction.IN_PROGRESS;
            }
            case PENDING_PROVIDER_CONFIRMATION, FAILED -> CleanupAction.STABLE_FAILURE;
        };
    }

    private void applyProviderResult(
            BillingAgreement agreement,
            LocalDateTime leaseStartedAt,
            BillingAgreementCleanupProviderExecutor.CleanupProviderResult providerResult) {
        requireActiveLease(agreement, leaseStartedAt);
        switch (providerResult.disposition()) {
            case SUCCEEDED -> {
                agreement.markBillingKeyCleanupSucceeded(leaseStartedAt);
                resolveCleanupIncident(agreement);
            }
            case FAILED -> {
                agreement.markBillingKeyCleanupFailed(leaseStartedAt);
                recordCleanupFailure(
                        agreement,
                        providerResult.failureCode(),
                        providerResult.failureMessage());
            }
            case PENDING_PROVIDER_CONFIRMATION -> {
                agreement.markBillingKeyCleanupPendingProviderConfirmation(leaseStartedAt);
                recordCleanupFailure(
                        agreement,
                        providerResult.failureCode(),
                        providerResult.failureMessage());
            }
        }
    }

    private void recordCleanupFailure(
            BillingAgreement agreement,
            String failureCode,
            String failureMessage) {
        PaymentReconciliationIncidentStatus beforeStatus = findCleanupIncident(agreement)
                .map(PaymentReconciliationIncident::getStatus)
                .orElse(null);
        incidentService.recordBillingCleanupFailure(agreement, failureCode, failureMessage);
        PaymentReconciliationIncident incident = findCleanupIncident(agreement).orElseThrow();
        auditLogService.recordReconciliationIncidentStatusUpdate(
                null,
                incident,
                beforeStatus,
                incident.getStatus(),
                "Billing key cleanup requires recovery; failureCode=" + safeFailureCode(failureCode));
    }

    private void resolveCleanupIncident(BillingAgreement agreement) {
        PaymentReconciliationIncident existingIncident = findCleanupIncident(agreement).orElse(null);
        if (existingIncident == null
                || existingIncident.getStatus() == PaymentReconciliationIncidentStatus.RESOLVED) {
            incidentService.resolveBillingCleanupIncident(agreement);
            return;
        }

        PaymentReconciliationIncidentStatus beforeStatus = existingIncident.getStatus();
        incidentService.resolveBillingCleanupIncident(agreement);
        PaymentReconciliationIncident resolvedIncident = findCleanupIncident(agreement).orElseThrow();
        auditLogService.recordReconciliationIncidentStatusUpdate(
                null,
                resolvedIncident,
                beforeStatus,
                resolvedIncident.getStatus(),
                "Provider billing key cleanup completed.");
    }

    private Optional<PaymentReconciliationIncident> findCleanupIncident(
            BillingAgreement agreement) {
        String dedupeKey = PaymentReconciliationIssueType.LOCAL_DONE_PROVIDER_NOT_DONE
                + ":billingAgreement:"
                + agreement.getId();
        return incidentRepository.findByDedupeKey(dedupeKey);
    }

    private String safeFailureCode(String failureCode) {
        return isBlank(failureCode) ? "BILLING_KEY_DELETE_FAILED" : failureCode;
    }

    private BillingAgreement lockAgreement(Long agreementID) {
        return billingAgreementRepository.findByIDForUpdate(agreementID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
    }

    private void requireOwnerAndProvider(BillingAgreement agreement, Long userID) {
        if (!Objects.equals(agreement.getUser().getId(), userID)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (agreement.getProvider() != RECURRING_PROVIDER) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private void requireActiveLease(BillingAgreement agreement, LocalDateTime leaseStartedAt) {
        LocalDateTime normalizedLease = leaseStartedAt == null ? null : leaseStartedAt.withNano(0);
        if (agreement.getBillingKeyCleanupStatus() != BillingKeyCleanupStatus.PROCESSING
                || !Objects.equals(agreement.getBillingKeyCleanupStartedAt(), normalizedLease)) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private boolean isWithdrawalCleanupEligible(BillingAgreement agreement) {
        return agreement != null
                && agreement.getUser().isDeleted()
                && agreement.getStatus() == BillingAgreementStatus.CANCELLED
                && !isBlank(agreement.getBillingKeyCiphertext());
    }

    private BillingAgreementResponse toResponse(
            BillingAgreement agreement,
            UserSubscription subscription) {
        return new BillingAgreementResponse(
                agreement.getProvider(),
                agreement.getStatus(),
                agreement.getPayMethod(),
                agreement.getMaskedMethod(),
                agreement.getNextBillingAt(),
                agreement.getLastChargedAt(),
                agreement.getCancelledAt(),
                subscription == null ? null : UserSubscriptionResponse.from(subscription));
    }

    private void requireNow(LocalDateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("Cleanup claim time is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public enum CleanupAction {
        CALL_PROVIDER,
        COMPLETED,
        IN_PROGRESS,
        PENDING_PROVIDER_CONFIRMATION,
        STABLE_FAILURE,
        SKIPPED
    }

    public record UserCancellationClaim(
            CleanupAction action,
            Long agreementID,
            PaymentProviderType provider,
            String billingKeyCiphertext,
            LocalDateTime leaseStartedAt,
            BillingAgreementResponse response) {

        static UserCancellationClaim callProvider(
                Long agreementID,
                PaymentProviderType provider,
                String billingKeyCiphertext,
                LocalDateTime leaseStartedAt) {
            return new UserCancellationClaim(
                    CleanupAction.CALL_PROVIDER,
                    agreementID,
                    provider,
                    billingKeyCiphertext,
                    leaseStartedAt,
                    null);
        }

        static UserCancellationClaim completed(BillingAgreementResponse response) {
            return new UserCancellationClaim(
                    CleanupAction.COMPLETED,
                    null,
                    null,
                    null,
                    null,
                    response);
        }

        static UserCancellationClaim withoutProvider(CleanupAction action, Long agreementID) {
            return new UserCancellationClaim(action, agreementID, null, null, null, null);
        }

        @Override
        public String toString() {
            return "UserCancellationClaim[action=%s, agreementID=%s, provider=%s]"
                    .formatted(action, agreementID, provider);
        }
    }

    public record WithdrawalCleanupClaim(
            CleanupAction action,
            Long agreementID,
            PaymentProviderType provider,
            String billingKeyCiphertext,
            LocalDateTime leaseStartedAt) {

        static WithdrawalCleanupClaim callProvider(
                Long agreementID,
                PaymentProviderType provider,
                String billingKeyCiphertext,
                LocalDateTime leaseStartedAt) {
            return new WithdrawalCleanupClaim(
                    CleanupAction.CALL_PROVIDER,
                    agreementID,
                    provider,
                    billingKeyCiphertext,
                    leaseStartedAt);
        }

        static WithdrawalCleanupClaim withoutProvider(CleanupAction action, Long agreementID) {
            return new WithdrawalCleanupClaim(action, agreementID, null, null, null);
        }

        @Override
        public String toString() {
            return "WithdrawalCleanupClaim[action=%s, agreementID=%s, provider=%s]"
                    .formatted(action, agreementID, provider);
        }
    }
}
