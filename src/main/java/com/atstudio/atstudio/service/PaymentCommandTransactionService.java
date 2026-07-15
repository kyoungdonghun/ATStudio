package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.BillingAgreementConfirmResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCommandTransactionService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final int STALE_PROCESSING_MINUTES = 15;
    private static final int RENEWAL_GRACE_DAYS = 3;
    private static final int RENEWAL_MAX_RETRY_COUNT = 3;
    private static final int MAX_FAILURE_CODE_LENGTH = 100;
    private static final int MAX_FAILURE_MESSAGE_LENGTH = 500;
    private static final int PAYMENT_EXPIRY_MINUTES = 10;
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String CHANGE_TYPE_UPGRADE = "UPGRADE";

    private final BillingAgreementRepository billingAgreementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PlaylistService playlistService;
    private final PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    private final PaymentReconciliationIncidentService incidentService;
    private final PaymentCommandKeyFactory keyFactory;

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            noRollbackFor = BusinessException.class)
    public UpgradeClaim claimUpgrade(
            Long userID,
            Long currentSubscriptionID,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle,
            LocalDateTime claimedAt) {
        BillingAgreement agreement = billingAgreementRepository
                .findByUserAndProvider(
                        userSubscriptionRepository.findById(currentSubscriptionID)
                                .map(UserSubscription::getUser)
                                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION)),
                        RECURRING_PROVIDER)
                .map(existing -> lockAgreement(existing.getId()))
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        UserSubscription current = userSubscriptionRepository.findByIdForUpdate(currentSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        com.atstudio.atstudio.entity.Subscription target = subscriptionRepository.findById(targetSubscriptionID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        validateUpgradeOwner(userID, current, agreement);
        validateReusableBillingAgreement(current, agreement);
        validateUpgradeTarget(current, target, targetBillingCycle);

        BigDecimal proratedAmount = calculateProratedUpgradeAmount(current, target);
        if (proratedAmount.signum() <= 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_AMOUNT_MISMATCH);
        }

        String commandKey = keyFactory.upgrade(
                current.getId(),
                current.getStartedAt(),
                current.getExpiresAt(),
                target.getId(),
                targetBillingCycle);
        PaymentOrder order = findOrCreateUpgradeOrder(
                current,
                target,
                agreement,
                commandKey,
                proratedAmount,
                targetBillingCycle,
                claimedAt);

        validateUpgradeOrder(
                order,
                current,
                target,
                agreement,
                proratedAmount,
                targetBillingCycle);

        if (order.getStatus() == PaymentOrderStatus.DONE
                || order.getStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            return UpgradeClaim.finalizeOnly(
                    agreement.getId(),
                    order.getOrderId(),
                    order.getUpgradeTargetBillingCycle());
        }
        if (order.getStatus() == PaymentOrderStatus.PROCESSING) {
            if (order.isProcessingStale(claimedAt.minusMinutes(STALE_PROCESSING_MINUTES))) {
                order.markProviderOutcomeUnknown(
                        "STALE_SUBSCRIPTION_UPGRADE",
                        "Subscription upgrade claim became stale and requires reconciliation.");
            }
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.getStatus() == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION
                || order.getStatus() == PaymentOrderStatus.CANCELLED
                || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.isExpired(claimedAt)) {
            order.markExpired();
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_EXPIRED);
        }

        int providerAttempt = order.getProviderAttempt() + 1;
        String providerIdempotencyKey = keyFactory.upgradeAttempt(order.getOrderId(), providerAttempt);
        order.claimProviderAttempt(commandKey, providerIdempotencyKey, claimedAt);

        return UpgradeClaim.callProvider(
                agreement.getId(),
                order.getOrderId(),
                agreement.getBillingKeyCiphertext(),
                agreement.getProviderCustomerKey(),
                "ATStudio " + target.getName() + " Upgrade",
                order.getAmount(),
                current.getUser().getEmail(),
                current.getUser().getNickname(),
                providerIdempotencyKey,
                order.getUpgradeTargetBillingCycle());
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            noRollbackFor = BusinessException.class)
    public BillingConfirmClaim claimBillingConfirm(
            Long userID,
            String orderID,
            String customerKey,
            BigDecimal amount,
            LocalDateTime claimedAt) {
        Long agreementID = paymentOrderRepository.findBillingAgreementIDByOrderId(orderID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
        LockedBillingCommand command = lockBillingCommand(agreementID, orderID);
        PaymentOrder order = command.order();
        BillingAgreement agreement = command.agreement();

        validateCommandOwner(userID, order, agreement);
        validateBillingOrder(order);
        validateRequest(order, agreement, customerKey, amount);

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return BillingConfirmClaim.completed(agreementID, orderID, toConfirmResponse(order, agreement));
        }
        if (order.getStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            return BillingConfirmClaim.finalizeOnly(agreementID, orderID);
        }
        if (order.getStatus() == PaymentOrderStatus.PROCESSING) {
            if (order.isProcessingStale(claimedAt.minusMinutes(STALE_PROCESSING_MINUTES))) {
                order.markProviderOutcomeUnknown(
                        "STALE_BILLING_CONFIRM",
                        "Billing confirm claim became stale and requires reconciliation.");
            }
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.getStatus() == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION
                || order.getStatus() == PaymentOrderStatus.FAILED
                || order.getStatus() == PaymentOrderStatus.CANCELLED
                || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (agreement.getStatus() != BillingAgreementStatus.READY) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        if (order.isExpired(claimedAt)) {
            order.markExpired();
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_EXPIRED);
        }

        validateInitialSubscriptionState(order);

        int providerAttempt = order.getProviderAttempt() + 1;
        String commandKey = keyFactory.billingConfirm(orderID);
        String providerIdempotencyKey = keyFactory.billingInitialAttempt(orderID, providerAttempt);
        order.claimProviderAttempt(commandKey, providerIdempotencyKey, claimedAt);

        return BillingConfirmClaim.callProvider(
                agreementID,
                orderID,
                order.getPurpose(),
                agreement.getProviderCustomerKey(),
                order.getSubscription().getName() + " recurring subscription",
                order.getAmount(),
                order.getUser().getEmail(),
                order.getUser().getNickname(),
                providerIdempotencyKey);
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            noRollbackFor = BusinessException.class)
    public RenewalClaim claimRenewal(
            Long billingAgreementID,
            LocalDate today,
            LocalDateTime claimedAt) {
        BillingAgreement agreement = lockAgreement(billingAgreementID);
        if (agreement.getProvider() != RECURRING_PROVIDER || !agreement.isChargeableOn(today)) {
            return RenewalClaim.skipped(billingAgreementID);
        }
        if (agreement.getUser().isDeleted()) {
            agreement.cancel();
            return RenewalClaim.skipped(billingAgreementID);
        }
        if (isBlank(agreement.getBillingKeyCiphertext())) {
            agreement.suspend();
            return RenewalClaim.skipped(billingAgreementID);
        }

        UserSubscription current = userSubscriptionRepository
                .findActiveByUserForUpdate(agreement.getUser(), today)
                .orElse(null);
        if (current == null) {
            suspendAgreementWithoutActiveSubscription(agreement, today);
            return RenewalClaim.skipped(billingAgreementID);
        }
        if (current.getStatus() == SubscriptionStatus.CANCELLED) {
            agreement.cancel();
            return RenewalClaim.skipped(billingAgreementID);
        }

        LocalDate billingPeriodStart = agreement.getNextBillingAt();
        PaymentOrder order = paymentOrderRepository.findRenewalPeriodForUpdate(
                        agreement,
                        current,
                        PaymentPurpose.RENEWAL,
                        billingPeriodStart)
                .orElse(null);
        if (order == null && agreement.getRenewalRetryAt() != null) {
            return RenewalClaim.skipped(billingAgreementID);
        }
        if (order == null) {
            order = createRenewalOrder(agreement, current, billingPeriodStart);
        }
        validateRenewalOrder(order, agreement, current, billingPeriodStart);

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return RenewalClaim.skipped(billingAgreementID);
        }
        if (order.getStatus() == PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            return RenewalClaim.finalizeOnly(billingAgreementID, order.getOrderId());
        }
        if (order.getStatus() == PaymentOrderStatus.PROCESSING) {
            if (order.isProcessingStale(claimedAt.minusMinutes(STALE_PROCESSING_MINUTES))) {
                order.markProviderOutcomeUnknown(
                        "STALE_RENEWAL",
                        "Renewal claim became stale and requires provider reconciliation.");
            }
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.getStatus() == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION
                || order.getStatus() == PaymentOrderStatus.CANCELLED
                || order.getStatus() == PaymentOrderStatus.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (order.getStatus() == PaymentOrderStatus.FAILED
                && (agreement.getRenewalRetryAt() == null
                || agreement.getRenewalRetryAt().isAfter(today)
                || order.getProviderAttempt() >= RENEWAL_MAX_RETRY_COUNT)) {
            return RenewalClaim.skipped(billingAgreementID);
        }

        LocalDate graceEndsAt = renewalGraceEndsAt(order);
        if (today.isAfter(graceEndsAt)) {
            finalizeRenewalFailure(agreement, current, graceEndsAt, today);
            return RenewalClaim.failedWithoutAttempt(
                    billingAgreementID,
                    order.getOrderId(),
                    agreement.getUser(),
                    graceEndsAt,
                    true);
        }

        int providerAttempt = order.getProviderAttempt() + 1;
        String commandKey = keyFactory.renewal(
                agreement.getId(),
                current.getId(),
                billingPeriodStart);
        String providerIdempotencyKey = keyFactory.renewalAttempt(order.getOrderId(), providerAttempt);
        order.claimProviderAttempt(commandKey, providerIdempotencyKey, claimedAt);

        return RenewalClaim.callProvider(
                billingAgreementID,
                order.getOrderId(),
                agreement.getBillingKeyCiphertext(),
                agreement.getProviderCustomerKey(),
                order.getSubscription().getName() + " recurring renewal",
                order.getAmount(),
                agreement.getUser().getEmail(),
                agreement.getUser().getNickname(),
                providerIdempotencyKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeIssuedBillingKey(
            Long agreementID,
            String orderID,
            String billingKeyCiphertext,
            String billingKeyFingerprint,
            String payMethod,
            String maskedMethod) {
        LockedBillingCommand command = lockBillingCommand(agreementID, orderID);
        if (command.order().getStatus() != PaymentOrderStatus.PROCESSING) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        command.agreement().storeIssuedKey(
                billingKeyCiphertext,
                billingKeyFingerprint,
                payMethod,
                maskedMethod);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProviderSuccess(
            Long agreementID,
            String orderID,
            String providerTransactionID,
            String providerPayload,
            String payMethod,
            String maskedMethod) {
        LockedBillingCommand command = lockBillingCommand(agreementID, orderID);
        BillingAgreement agreement = command.agreement();
        PaymentOrder order = command.order();

        if (!isBlank(payMethod) || !isBlank(maskedMethod)) {
            agreement.storeIssuedKey(
                    agreement.getBillingKeyCiphertext(),
                    agreement.getBillingKeyFingerprint(),
                    firstPresent(payMethod, agreement.getPayMethod()),
                    firstPresent(maskedMethod, agreement.getMaskedMethod()));
        }
        lockProviderTransactionOwner(order, providerTransactionID);
        order.markProviderSucceeded(providerTransactionID, providerPayload);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReconciliationFinalizationTarget recordProviderSuccessFromReconciliation(
            Long agreementID,
            String orderID,
            String providerTransactionID,
            String providerPayload,
            LocalDateTime staleBefore) {
        PaymentOrderRepository.CommandLockProjection projection = commandLockProjection(orderID);
        validateProjectedAgreement(agreementID, projection);

        BillingAgreement agreement = lockAgreement(agreementID);
        UserSubscription subscription = lockProjectedSubscription(projection);
        PaymentOrder order = lockOrder(orderID);
        validateCommandLockProjection(projection, agreement, subscription, order);
        validateReconciliationFinalizationTarget(order, agreement, subscription);
        lockProviderTransactionOwner(order, providerTransactionID);
        order.markProviderSucceededFromReconciliation(
                providerTransactionID,
                providerPayload,
                staleBefore);
        return new ReconciliationFinalizationTarget(
                order.getPurpose(),
                order.getUser().getId(),
                agreement.getId(),
                order.getOrderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordProviderFailure(
            Long agreementID,
            String orderID,
            String failureCode,
            String failureMessage,
            ProviderFailureDisposition disposition,
            boolean recordFailedCharge) {
        LockedBillingCommand command = lockBillingCommand(agreementID, orderID);
        PaymentOrder order = command.order();

        if (isSameFailureState(order.getStatus(), disposition)) {
            return;
        }
        if (order.getStatus() != PaymentOrderStatus.PROCESSING) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }

        String safeCode = truncate(defaultText(failureCode, "PROVIDER_FAILURE"), MAX_FAILURE_CODE_LENGTH);
        String safeMessage = truncate(defaultText(failureMessage, "Provider command failed."), MAX_FAILURE_MESSAGE_LENGTH);
        if (disposition == ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION) {
            order.markProviderOutcomeUnknown(safeCode, safeMessage);
        } else {
            order.markFailed(safeCode, safeMessage);
        }
        if (recordFailedCharge) {
            command.agreement().recordFailedCharge();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RenewalFailureResult recordRenewalProviderFailure(
            Long agreementID,
            String orderID,
            String failureCode,
            String failureMessage,
            ProviderFailureDisposition disposition,
            LocalDate today) {
        PaymentOrderRepository.CommandLockProjection projection = commandLockProjection(orderID);
        validateProjectedAgreement(agreementID, projection);
        BillingAgreement agreement = lockAgreement(agreementID);
        UserSubscription subscription = lockProjectedSubscription(projection);
        if (subscription == null) {
            throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
        }
        PaymentOrder order = lockOrder(orderID);
        validateCommandLockProjection(projection, agreement, subscription, order);
        validateRenewalOrder(order, agreement, subscription, agreement.getNextBillingAt());

        if (isSameFailureState(order.getStatus(), disposition)) {
            return new RenewalFailureResult(order.getOrderId(), agreement.getUser(), renewalGraceEndsAt(order), false);
        }
        if (order.getStatus() != PaymentOrderStatus.PROCESSING) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }

        String safeCode = truncate(defaultText(failureCode, "PROVIDER_FAILURE"), MAX_FAILURE_CODE_LENGTH);
        String safeMessage = truncate(defaultText(failureMessage, "Provider command failed."), MAX_FAILURE_MESSAGE_LENGTH);
        if (disposition == ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION) {
            order.markProviderOutcomeUnknown(safeCode, safeMessage);
            return new RenewalFailureResult(order.getOrderId(), agreement.getUser(), renewalGraceEndsAt(order), false);
        }

        order.markFailed(safeCode, safeMessage);
        LocalDate graceEndsAt = renewalGraceEndsAt(order);
        LocalDate nextRetryAt = today.plusDays(1).isAfter(graceEndsAt)
                ? graceEndsAt
                : today.plusDays(1);
        agreement.recordFailedCharge(nextRetryAt);
        if (subscription.getExpiresAt().isBefore(graceEndsAt)) {
            subscription.adminUpdate(null, null, graceEndsAt);
        }
        boolean finalFailure = agreement.getFailureCount() >= RENEWAL_MAX_RETRY_COUNT
                || !today.isBefore(graceEndsAt);
        if (finalFailure) {
            finalizeRenewalFailure(agreement, subscription, graceEndsAt, today);
        }
        return new RenewalFailureResult(order.getOrderId(), agreement.getUser(), graceEndsAt, finalFailure);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BillingAgreementConfirmResponse finalizeInitialCharge(
            Long userID,
            Long agreementID,
            String orderID) {
        BillingAgreement agreement = lockAgreement(agreementID);
        UserSubscription lockedSubscription = userSubscriptionRepository.findByUserIDForUpdate(userID).orElse(null);
        PaymentOrder order = lockOrder(orderID);
        validateLockedCommand(agreementID, orderID, agreement, order);
        validateCommandOwner(userID, order, agreement);

        if (order.getStatus() == PaymentOrderStatus.DONE) {
            if (order.getPurpose() == PaymentPurpose.SUBSCRIBE) {
                lockExistingPaymentForFinalization(order);
            }
            return toConfirmResponse(order, agreement);
        }
        if (order.getStatus() != PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        if (isBlank(agreement.getBillingKeyCiphertext())
                || isBlank(agreement.getBillingKeyFingerprint())) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_REAUTH_REQUIRED);
        }

        if (order.getPurpose() == PaymentPurpose.BILLING_AGREEMENT) {
            return finalizeRegistrationOnly(order, agreement, lockedSubscription);
        }
        if (order.getPurpose() != PaymentPurpose.SUBSCRIBE) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }

        SubscriptionPayment existingPayment = lockExistingPaymentForFinalization(order);
        if (existingPayment != null) {
            UserSubscription existingSubscription = existingPayment.getUserSubscription();
            order.markDone(order.getPgTransactionId(), existingSubscription, order.getProviderPayload());
            activateAgreement(agreement, existingSubscription);
            return toConfirmResponse(order, agreement);
        }

        LocalDate startedAt = LocalDate.now();
        LocalDate expiresAt = order.getBillingCycle() == com.atstudio.atstudio.entity.enums.BillingCycle.MONTHLY
                ? startedAt.plusMonths(1)
                : startedAt.plusYears(1);
        UserSubscription userSubscription = lockedSubscription;
        if (userSubscription == null) {
            userSubscription = userSubscriptionRepository.save(UserSubscription.builder()
                    .user(order.getUser())
                    .subscription(order.getSubscription())
                    .billingCycle(order.getBillingCycle())
                    .startedAt(startedAt)
                    .expiresAt(expiresAt)
                    .build());
        } else {
            userSubscription.startNewSubscription(
                    order.getSubscription(),
                    order.getBillingCycle(),
                    startedAt,
                    expiresAt);
        }

        playlistService.createDefaultPlaylist(order.getUser());
        SubscriptionPayment subscriptionPayment = subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                .paymentOrder(order)
                .billingAgreement(agreement)
                .provider(order.getProvider())
                .user(order.getUser())
                .userSubscription(userSubscription)
                .subscription(order.getSubscription())
                .billingCycle(order.getBillingCycle())
                .amount(order.getAmount())
                .paymentStatus(PaymentStatus.DONE)
                .pgTransactionId(order.getPgTransactionId())
                .build());

        order.markDone(order.getPgTransactionId(), userSubscription, order.getProviderPayload());
        activateAgreement(agreement, userSubscription);
        paymentReceiptEvidenceService.publishSuccessfulChargeEvidence(
                order,
                subscriptionPayment,
                order.getProviderPayload());
        return toConfirmResponse(order, agreement);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse finalizeUpgrade(
            Long userID,
            Long agreementID,
            String orderID) {
        return finalizeUpgradeLocked(userID, agreementID, orderID);
    }

    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse finalizeUpgrade(
            Long userID,
            Long agreementID,
            String orderID,
            BillingCycle ignoredCallerTargetBillingCycle) {
        return finalizeUpgradeLocked(userID, agreementID, orderID);
    }

    private com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse finalizeUpgradeLocked(
            Long userID,
            Long agreementID,
            String orderID) {
        PaymentOrderRepository.CommandLockProjection projection = commandLockProjection(orderID);
        validateProjectedAgreement(agreementID, projection);
        BillingAgreement agreement = lockAgreement(agreementID);
        UserSubscription current = lockProjectedSubscription(projection);
        if (current == null) {
            throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
        }
        PaymentOrder order = lockOrder(orderID);
        validateCommandLockProjection(projection, agreement, current, order);
        validateCommandOwner(userID, order, agreement);
        validateUpgradeFinalizationOrder(order, current, agreement);
        if (!Objects.equals(current.getUser().getId(), userID)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }

        BillingCycle targetBillingCycle = order.getUpgradeTargetBillingCycle();
        if (order.getStatus() != PaymentOrderStatus.DONE
                && order.getStatus() != PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        SubscriptionPayment existingPayment = lockExistingPaymentForFinalization(order);
        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return toUpgradeResponse(order, current, targetBillingCycle);
        }

        if (existingPayment == null) {
            existingPayment = subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                    .paymentOrder(order)
                    .billingAgreement(agreement)
                    .provider(order.getProvider())
                    .user(order.getUser())
                    .userSubscription(current)
                    .subscription(order.getSubscription())
                    .billingCycle(order.getBillingCycle())
                    .amount(order.getAmount())
                    .paymentStatus(PaymentStatus.DONE)
                    .pgTransactionId(order.getPgTransactionId())
                    .build());
        }

        reactivateIfCancelled(current, agreement);
        current.upgradeKeepingPeriod(order.getSubscription(), targetBillingCycle);
        order.markDone(order.getPgTransactionId(), current, order.getProviderPayload());
        agreement.recordSuccessfulCharge(agreement.getNextBillingAt());
        paymentReceiptEvidenceService.publishSuccessfulChargeEvidence(
                order,
                existingPayment,
                order.getProviderPayload());
        return toUpgradeResponse(order, current, targetBillingCycle);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeRenewal(Long agreementID, String orderID) {
        PaymentOrderRepository.CommandLockProjection projection = commandLockProjection(orderID);
        validateProjectedAgreement(agreementID, projection);
        BillingAgreement agreement = lockAgreement(agreementID);
        UserSubscription current = lockProjectedSubscription(projection);
        if (current == null) {
            throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
        }
        PaymentOrder order = lockOrder(orderID);
        validateCommandLockProjection(projection, agreement, current, order);
        validateRenewalOrder(order, agreement, current, agreement.getNextBillingAt());

        if (order.getStatus() != PaymentOrderStatus.DONE
                && order.getStatus() != PaymentOrderStatus.PROVIDER_SUCCEEDED) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        SubscriptionPayment existingPayment = lockExistingPaymentForFinalization(order);
        if (order.getStatus() == PaymentOrderStatus.DONE) {
            return;
        }

        if (existingPayment == null) {
            existingPayment = subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                    .paymentOrder(order)
                    .billingAgreement(agreement)
                    .provider(order.getProvider())
                    .user(order.getUser())
                    .userSubscription(current)
                    .subscription(order.getSubscription())
                    .billingCycle(order.getBillingCycle())
                    .amount(order.getAmount())
                    .paymentStatus(PaymentStatus.DONE)
                    .pgTransactionId(order.getPgTransactionId())
                    .build());
        }

        LocalDate periodStart = order.getBillingPeriodStart();
        LocalDate newExpiresAt = expiresAt(periodStart, order.getBillingCycle());
        current.startNewSubscription(
                order.getSubscription(),
                order.getBillingCycle(),
                periodStart,
                newExpiresAt);
        order.markDone(order.getPgTransactionId(), current, order.getProviderPayload());
        agreement.recordSuccessfulCharge(newExpiresAt);
        paymentReceiptEvidenceService.publishSuccessfulChargeEvidence(
                order,
                existingPayment,
                order.getProviderPayload());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireIssuedBillingKeyAfterProviderRemoval(Long agreementID) {
        BillingAgreement agreement = lockAgreement(agreementID);
        agreement.expireIssuedKey();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearIssuedBillingKeyAfterCleanup(Long agreementID) {
        BillingAgreement agreement = lockAgreement(agreementID);
        agreement.clearIssuedKey();
        incidentService.resolveBillingCleanupIncident(agreement);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordBillingCleanupFailure(
            Long agreementID,
            String failureCode,
            String failureMessage) {
        BillingAgreement agreement = lockAgreement(agreementID);
        incidentService.recordBillingCleanupFailure(agreement, failureCode, failureMessage);
    }

    private BillingAgreementConfirmResponse finalizeRegistrationOnly(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription lockedSubscription) {
        UserSubscription activeSubscription = userSubscriptionRepository.findActiveByUser(
                        order.getUser(),
                        LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        if (lockedSubscription == null
                || !Objects.equals(lockedSubscription.getId(), activeSubscription.getId())
                || !Objects.equals(activeSubscription.getSubscription().getId(), order.getSubscription().getId())
                || activeSubscription.getBillingCycle() != order.getBillingCycle()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
        }

        order.markDone(order.getPgTransactionId(), activeSubscription, order.getProviderPayload());
        activateAgreement(agreement, activeSubscription);
        return toConfirmResponse(order, agreement);
    }

    private PaymentOrder createRenewalOrder(
            BillingAgreement agreement,
            UserSubscription current,
            LocalDate billingPeriodStart) {
        String commandKey = keyFactory.renewal(
                agreement.getId(),
                current.getId(),
                billingPeriodStart);
        PaymentOrder existing = paymentOrderRepository.findRenewalPeriodForUpdate(
                        agreement,
                        current,
                        PaymentPurpose.RENEWAL,
                        billingPeriodStart)
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        Subscription renewalSubscription = renewalSubscription(current);
        BillingCycle renewalBillingCycle = renewalBillingCycle(current);
        PaymentOrder order = PaymentOrder.builder()
                .orderId(generateRenewalOrderID(billingPeriodStart))
                .commandKey(commandKey)
                .user(agreement.getUser())
                .purpose(PaymentPurpose.RENEWAL)
                .provider(RECURRING_PROVIDER)
                .subscription(renewalSubscription)
                .userSubscription(current)
                .billingAgreement(agreement)
                .billingCycle(renewalBillingCycle)
                .billingPeriodStart(billingPeriodStart)
                .amount(priceFor(renewalSubscription, renewalBillingCycle))
                .currency("KRW")
                .expiresAt(billingPeriodStart.plusDays(RENEWAL_GRACE_DAYS).atTime(java.time.LocalTime.MAX))
                .build();
        try {
            return paymentOrderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException exception) {
            return paymentOrderRepository.findRenewalPeriodForUpdate(
                            agreement,
                            current,
                            PaymentPurpose.RENEWAL,
                            billingPeriodStart)
                    .orElseThrow(() -> exception);
        }
    }

    private PaymentOrder findOrCreateUpgradeOrder(
            UserSubscription current,
            com.atstudio.atstudio.entity.Subscription target,
            BillingAgreement agreement,
            String commandKey,
            BigDecimal proratedAmount,
            BillingCycle targetBillingCycle,
            LocalDateTime claimedAt) {
        PaymentOrder existing = paymentOrderRepository.findByCommandKeyForUpdate(commandKey).orElse(null);
        if (existing != null) {
            return existing;
        }

        PaymentOrder order = PaymentOrder.builder()
                .orderId(generateUpgradeOrderID())
                .commandKey(commandKey)
                .user(current.getUser())
                .purpose(PaymentPurpose.UPGRADE)
                .provider(RECURRING_PROVIDER)
                .subscription(target)
                .userSubscription(current)
                .billingAgreement(agreement)
                .billingCycle(current.getBillingCycle())
                .upgradeTargetBillingCycle(targetBillingCycle)
                .amount(proratedAmount)
                .currency("KRW")
                .expiresAt(claimedAt.plusMinutes(PAYMENT_EXPIRY_MINUTES))
                .build();
        try {
            return paymentOrderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException exception) {
            return paymentOrderRepository.findByCommandKeyForUpdate(commandKey)
                    .orElseThrow(() -> exception);
        }
    }

    private void validateRenewalOrder(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription current,
            LocalDate billingPeriodStart) {
        validateRenewalPaymentOrder(order);
        Subscription expectedSubscription = renewalSubscription(current);
        BillingCycle expectedBillingCycle = renewalBillingCycle(current);
        BigDecimal expectedAmount = priceFor(expectedSubscription, expectedBillingCycle);
        String expectedCommandKey = keyFactory.renewal(
                agreement.getId(),
                current.getId(),
                billingPeriodStart);
        if (!Objects.equals(order.getUser().getId(), current.getUser().getId())
                || !Objects.equals(order.getUserSubscription().getId(), current.getId())
                || order.getBillingAgreement() == null
                || !Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                || !Objects.equals(order.getBillingPeriodStart(), billingPeriodStart)
                || !Objects.equals(order.getSubscription().getId(), expectedSubscription.getId())
                || order.getBillingCycle() != expectedBillingCycle
                || !Objects.equals(order.getCommandKey(), expectedCommandKey)
                || order.getAmount().compareTo(expectedAmount) != 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateRenewalPaymentOrder(PaymentOrder order) {
        if (order.getProvider() != RECURRING_PROVIDER
                || order.getPurpose() != PaymentPurpose.RENEWAL
                || order.getUserSubscription() == null
                || order.getBillingAgreement() == null
                || order.getBillingPeriodStart() == null) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateUpgradeOwner(
            Long userID,
            UserSubscription current,
            BillingAgreement agreement) {
        if (userID == null
                || current.getUser() == null
                || !Objects.equals(current.getUser().getId(), userID)
                || !agreement.isOwnedBy(current.getUser())) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
    }

    private void validateReusableBillingAgreement(
            UserSubscription current,
            BillingAgreement agreement) {
        if (isBlank(agreement.getBillingKeyCiphertext())) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        if (agreement.getStatus() == BillingAgreementStatus.ACTIVE) {
            return;
        }
        if (current.getStatus() == SubscriptionStatus.CANCELLED
                && agreement.getStatus() == BillingAgreementStatus.CANCELLED) {
            return;
        }
        throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
    }

    private void validateUpgradeTarget(
            UserSubscription current,
            com.atstudio.atstudio.entity.Subscription target,
            BillingCycle targetBillingCycle) {
        if (current.getStatus() != SubscriptionStatus.ACTIVE
                && current.getStatus() != SubscriptionStatus.CANCELLED) {
            throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
        }
        if (current.getExpiresAt().isBefore(LocalDate.now())) {
            throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
        }
        if (target.getUserType() != current.getUser().getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }
        if (targetBillingCycle == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        if (target.getPriceMonthly().compareTo(current.getSubscription().getPriceMonthly()) <= 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateUpgradeOrder(
            PaymentOrder order,
            UserSubscription current,
            com.atstudio.atstudio.entity.Subscription target,
            BillingAgreement agreement,
            BigDecimal proratedAmount,
            BillingCycle targetBillingCycle) {
        validateUpgradePaymentOrder(order);
        String expectedCommandKey = keyFactory.upgrade(
                current.getId(),
                current.getStartedAt(),
                current.getExpiresAt(),
                target.getId(),
                targetBillingCycle);
        if (!Objects.equals(order.getUser().getId(), current.getUser().getId())
                || !Objects.equals(order.getUserSubscription().getId(), current.getId())
                || !Objects.equals(order.getSubscription().getId(), target.getId())
                || order.getBillingAgreement() == null
                || !Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                || order.getBillingCycle() != current.getBillingCycle()
                || order.getUpgradeTargetBillingCycle() != targetBillingCycle
                || !Objects.equals(order.getCommandKey(), expectedCommandKey)
                || order.getAmount().compareTo(proratedAmount) != 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateUpgradePaymentOrder(PaymentOrder order) {
        if (order.getProvider() != RECURRING_PROVIDER
                || order.getPurpose() != PaymentPurpose.UPGRADE
                || order.getUserSubscription() == null
                || order.getBillingAgreement() == null
                || order.getUpgradeTargetBillingCycle() == null) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateUpgradeFinalizationOrder(
            PaymentOrder order,
            UserSubscription current,
            BillingAgreement agreement) {
        validateUpgradePaymentOrder(order);
        if (!Objects.equals(order.getUser().getId(), current.getUser().getId())
                || !Objects.equals(order.getUserSubscription().getId(), current.getId())
                || !Objects.equals(order.getBillingAgreement().getId(), agreement.getId())
                || order.getAmount() == null
                || order.getAmount().signum() <= 0
                || !"KRW".equals(order.getCurrency())
                || isBlank(order.getCommandKey())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void reactivateIfCancelled(UserSubscription current, BillingAgreement agreement) {
        if (current.getStatus() != SubscriptionStatus.CANCELLED) {
            return;
        }
        if (agreement.getStatus() != BillingAgreementStatus.CANCELLED) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        try {
            agreement.resume(current.getExpiresAt());
            current.reactivate();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
    }

    private BigDecimal calculateProratedUpgradeAmount(
            UserSubscription current,
            com.atstudio.atstudio.entity.Subscription target) {
        LocalDate today = LocalDate.now();
        long remainingDays = Math.max(0, ChronoUnit.DAYS.between(today, current.getExpiresAt()));
        long totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt());
        if (remainingDays == 0 || totalDays <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal currentPrice = priceFor(current.getSubscription(), current.getBillingCycle());
        BigDecimal targetPrice = priceFor(target, current.getBillingCycle());
        BigDecimal difference = targetPrice.subtract(currentPrice);
        if (difference.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return difference.multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal priceFor(
            com.atstudio.atstudio.entity.Subscription subscription,
            BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
    }

    private Subscription renewalSubscription(UserSubscription subscription) {
        return subscription.getPendingSubscription() == null
                ? subscription.getSubscription()
                : subscription.getPendingSubscription();
    }

    private BillingCycle renewalBillingCycle(UserSubscription subscription) {
        return subscription.getPendingBillingCycle() == null
                ? subscription.getBillingCycle()
                : subscription.getPendingBillingCycle();
    }

    private LocalDate expiresAt(LocalDate startedAt, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1)
                : startedAt.plusYears(1);
    }

    private LocalDate renewalGraceEndsAt(PaymentOrder order) {
        return order.getBillingPeriodStart().plusDays(RENEWAL_GRACE_DAYS);
    }

    private void finalizeRenewalFailure(
            BillingAgreement agreement,
            UserSubscription subscription,
            LocalDate graceEndsAt,
            LocalDate today) {
        agreement.suspend();
        if (subscription.getExpiresAt().isBefore(graceEndsAt)) {
            subscription.adminUpdate(null, null, graceEndsAt);
        }
        if (today.isAfter(graceEndsAt)) {
            subscription.expire();
        }
    }

    private void suspendAgreementWithoutActiveSubscription(BillingAgreement agreement, LocalDate today) {
        agreement.suspend();
        userSubscriptionRepository.findByUserForUpdate(agreement.getUser())
                .filter(subscription -> subscription.getExpiresAt().isBefore(today))
                .ifPresent(UserSubscription::expire);
    }

    private com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse toUpgradeResponse(
            PaymentOrder order,
            UserSubscription current,
            BillingCycle targetBillingCycle) {
        return new com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse(
                com.atstudio.atstudio.dto.subscription.SubscriptionResponse.from(order.getSubscription()),
                targetBillingCycle.name(),
                current.getStatus().name(),
                CHANGE_TYPE_UPGRADE,
                order.getAmount(),
                current.getStartedAt(),
                current.getExpiresAt());
    }

    private String generateUpgradeOrderID() {
        return "ATS-UPG-" + LocalDate.now().format(ORDER_DATE) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateRenewalOrderID(LocalDate billingPeriodStart) {
        return "ATS-REN-" + billingPeriodStart.format(ORDER_DATE) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void activateAgreement(BillingAgreement agreement, UserSubscription userSubscription) {
        agreement.activate(
                agreement.getBillingKeyCiphertext(),
                agreement.getBillingKeyFingerprint(),
                agreement.getPayMethod(),
                agreement.getMaskedMethod(),
                userSubscription.getExpiresAt());
        agreement.recordSuccessfulCharge(userSubscription.getExpiresAt());
    }

    private PaymentOrderRepository.CommandLockProjection commandLockProjection(String orderID) {
        return paymentOrderRepository.findCommandLockProjectionByOrderId(orderID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
    }

    private void validateProjectedAgreement(
            Long agreementID,
            PaymentOrderRepository.CommandLockProjection projection) {
        if (!Objects.equals(projection.getBillingAgreementID(), agreementID)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private UserSubscription lockProjectedSubscription(
            PaymentOrderRepository.CommandLockProjection projection) {
        if (projection.getUserSubscriptionID() == null) {
            return null;
        }
        return userSubscriptionRepository.findByIdForUpdate(projection.getUserSubscriptionID())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
    }

    private void validateCommandLockProjection(
            PaymentOrderRepository.CommandLockProjection projection,
            BillingAgreement agreement,
            UserSubscription subscription,
            PaymentOrder order) {
        Long lockedSubscriptionID = subscription == null ? null : subscription.getId();
        Long orderSubscriptionID = order.getUserSubscription() == null
                ? null
                : order.getUserSubscription().getId();
        if (!Objects.equals(projection.getBillingAgreementID(), agreement.getId())
                || !Objects.equals(projection.getUserSubscriptionID(), lockedSubscriptionID)
                || !Objects.equals(projection.getUserSubscriptionID(), orderSubscriptionID)
                || !Objects.equals(projection.getUserID(), order.getUser().getId())
                || projection.getPurpose() != order.getPurpose()) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        validateLockedCommand(agreement.getId(), order.getOrderId(), agreement, order);
    }

    private SubscriptionPayment lockExistingPaymentForFinalization(PaymentOrder order) {
        if (isBlank(order.getPgTransactionId())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        SubscriptionPayment existing = subscriptionPaymentRepository
                .findByPaymentOrderForUpdate(order)
                .orElse(null);
        SubscriptionPayment transactionOwner = subscriptionPaymentRepository
                .findByProviderAndPgTransactionIdForUpdate(
                        order.getProvider(),
                        order.getPgTransactionId())
                .orElse(null);
        if (existing == null) {
            if (transactionOwner != null) {
                throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
            }
            return null;
        }
        if (transactionOwner == null || !Objects.equals(transactionOwner.getId(), existing.getId())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        validateExistingPayment(order, existing);
        return existing;
    }

    private void lockProviderTransactionOwner(PaymentOrder order, String providerTransactionID) {
        if (isBlank(providerTransactionID)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        subscriptionPaymentRepository.findByProviderAndPgTransactionIdForUpdate(
                        order.getProvider(),
                        providerTransactionID)
                .ifPresent(payment -> {
                    if (payment.getPaymentOrder() == null
                            || !Objects.equals(payment.getPaymentOrder().getId(), order.getId())) {
                        throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
                    }
                });
    }

    private void validateExistingPayment(PaymentOrder order, SubscriptionPayment payment) {
        Long orderSubscriptionID = order.getUserSubscription() == null
                ? null
                : order.getUserSubscription().getId();
        if (payment.getPaymentOrder() == null
                || !Objects.equals(payment.getPaymentOrder().getId(), order.getId())
                || payment.getBillingAgreement() == null
                || !Objects.equals(payment.getBillingAgreement().getId(), order.getBillingAgreement().getId())
                || payment.getProvider() != order.getProvider()
                || !Objects.equals(payment.getPgTransactionId(), order.getPgTransactionId())
                || payment.getPaymentStatus() != PaymentStatus.DONE
                || payment.getAmount().compareTo(order.getAmount()) != 0
                || !Objects.equals(payment.getUser().getId(), order.getUser().getId())
                || (orderSubscriptionID != null
                && !Objects.equals(payment.getUserSubscription().getId(), orderSubscriptionID))) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateReconciliationFinalizationTarget(
            PaymentOrder order,
            BillingAgreement agreement,
            UserSubscription subscription) {
        if (isBlank(order.getCommandKey())) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
        switch (order.getPurpose()) {
            case SUBSCRIBE -> {
                validateBillingOrder(order);
                if (subscription != null
                        || isBlank(agreement.getBillingKeyCiphertext())
                        || isBlank(agreement.getBillingKeyFingerprint())) {
                    throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
                }
            }
            case UPGRADE -> {
                if (subscription == null) {
                    throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
                }
                validateUpgradeFinalizationOrder(order, subscription, agreement);
            }
            case RENEWAL -> {
                if (subscription == null) {
                    throw new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
                }
                validateRenewalOrder(order, agreement, subscription, agreement.getNextBillingAt());
            }
            default -> throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private LockedBillingCommand lockBillingCommand(Long agreementID, String orderID) {
        BillingAgreement agreement = lockAgreement(agreementID);
        PaymentOrder order = lockOrder(orderID);
        validateLockedCommand(agreementID, orderID, agreement, order);
        return new LockedBillingCommand(agreement, order);
    }

    private BillingAgreement lockAgreement(Long agreementID) {
        return billingAgreementRepository.findByIDForUpdate(agreementID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
    }

    private PaymentOrder lockOrder(String orderID) {
        return paymentOrderRepository.findByOrderIdForUpdate(orderID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_NOT_FOUND));
    }

    private void validateLockedCommand(
            Long agreementID,
            String orderID,
            BillingAgreement agreement,
            PaymentOrder order) {
        if (!Objects.equals(agreement.getId(), agreementID)
                || !Objects.equals(order.getOrderId(), orderID)
                || order.getBillingAgreement() == null
                || !Objects.equals(order.getBillingAgreement().getId(), agreementID)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateCommandOwner(Long userID, PaymentOrder order, BillingAgreement agreement) {
        if (userID == null
                || order.getUser() == null
                || !Objects.equals(order.getUser().getId(), userID)
                || !agreement.isOwnedBy(order.getUser())) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
    }

    private void validateBillingOrder(PaymentOrder order) {
        if (order.getProvider() != RECURRING_PROVIDER
                || (order.getPurpose() != PaymentPurpose.SUBSCRIBE
                && order.getPurpose() != PaymentPurpose.BILLING_AGREEMENT)) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_ORDER_INVALID_STATE);
        }
    }

    private void validateRequest(
            PaymentOrder order,
            BillingAgreement agreement,
            String customerKey,
            BigDecimal amount) {
        if (!Objects.equals(agreement.getProviderCustomerKey(), customerKey)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        if (amount == null || order.getAmount().compareTo(amount) != 0) {
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateInitialSubscriptionState(PaymentOrder order) {
        User user = order.getUser();
        if (order.getSubscription().getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }

        if (order.getPurpose() == PaymentPurpose.SUBSCRIBE) {
            if (user.getUserType() == UserType.BUSINESS
                    && !companyCertificationRepository.existsByUserAndStatusIn(
                    user,
                    List.of(CompanyCertificationStatus.APPROVED))) {
                throw new BusinessException(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED);
            }
            userSubscriptionRepository.findActiveByUser(user, LocalDate.now()).ifPresent(subscription -> {
                throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
            });
            return;
        }

        UserSubscription activeSubscription = userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        if (!Objects.equals(activeSubscription.getSubscription().getId(), order.getSubscription().getId())
                || activeSubscription.getBillingCycle() != order.getBillingCycle()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
        }
    }

    private BillingAgreementConfirmResponse toConfirmResponse(
            PaymentOrder order,
            BillingAgreement agreement) {
        UserSubscriptionResponse subscription = order.getUserSubscription() == null
                ? null
                : UserSubscriptionResponse.from(order.getUserSubscription());
        return new BillingAgreementConfirmResponse(
                order.getOrderId(),
                order.getStatus(),
                agreement.getProvider(),
                agreement.getStatus(),
                agreement.getNextBillingAt(),
                subscription);
    }

    private boolean isSameFailureState(
            PaymentOrderStatus status,
            ProviderFailureDisposition disposition) {
        return status == PaymentOrderStatus.FAILED
                && disposition == ProviderFailureDisposition.FAILED
                || status == PaymentOrderStatus.PENDING_PROVIDER_CONFIRMATION
                && disposition == ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION;
    }

    private String defaultText(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String firstPresent(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public enum BillingConfirmAction {
        CALL_PROVIDER,
        FINALIZE_ONLY,
        COMPLETED
    }

    public enum ProviderFailureDisposition {
        FAILED,
        PENDING_PROVIDER_CONFIRMATION
    }

    public enum UpgradeAction {
        CALL_PROVIDER,
        FINALIZE_ONLY
    }

    public enum RenewalAction {
        CALL_PROVIDER,
        FINALIZE_ONLY,
        FAILED_WITHOUT_ATTEMPT,
        SKIPPED
    }

    public record RenewalFailureResult(
            String orderID,
            User user,
            LocalDate graceEndsAt,
            boolean finalFailure) {
    }

    public record ReconciliationFinalizationTarget(
            PaymentPurpose purpose,
            Long userID,
            Long agreementID,
            String orderID) {
    }

    public record RenewalClaim(
            RenewalAction action,
            Long agreementID,
            String orderID,
            User user,
            String billingKeyCiphertext,
            String providerCustomerKey,
            String orderName,
            BigDecimal amount,
            String userEmail,
            String userNickname,
            String providerIdempotencyKey,
            LocalDate graceEndsAt,
            boolean finalFailure) {

        private static RenewalClaim callProvider(
                Long agreementID,
                String orderID,
                String billingKeyCiphertext,
                String providerCustomerKey,
                String orderName,
                BigDecimal amount,
                String userEmail,
                String userNickname,
                String providerIdempotencyKey) {
            return new RenewalClaim(
                    RenewalAction.CALL_PROVIDER,
                    agreementID,
                    orderID,
                    null,
                    billingKeyCiphertext,
                    providerCustomerKey,
                    orderName,
                    amount,
                    userEmail,
                    userNickname,
                    providerIdempotencyKey,
                    null,
                    false);
        }

        private static RenewalClaim finalizeOnly(Long agreementID, String orderID) {
            return new RenewalClaim(
                    RenewalAction.FINALIZE_ONLY,
                    agreementID,
                    orderID,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false);
        }

        private static RenewalClaim failedWithoutAttempt(
                Long agreementID,
                String orderID,
                User user,
                LocalDate graceEndsAt,
                boolean finalFailure) {
            return new RenewalClaim(
                    RenewalAction.FAILED_WITHOUT_ATTEMPT,
                    agreementID,
                    orderID,
                    user,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    graceEndsAt,
                    finalFailure);
        }

        private static RenewalClaim skipped(Long agreementID) {
            return new RenewalClaim(
                    RenewalAction.SKIPPED,
                    agreementID,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    false);
        }
    }

    public record UpgradeClaim(
            UpgradeAction action,
            Long agreementID,
            String orderID,
            String billingKeyCiphertext,
            String providerCustomerKey,
            String orderName,
            BigDecimal amount,
            String userEmail,
            String userNickname,
            String providerIdempotencyKey,
            BillingCycle targetBillingCycle) {

        private static UpgradeClaim callProvider(
                Long agreementID,
                String orderID,
                String billingKeyCiphertext,
                String providerCustomerKey,
                String orderName,
                BigDecimal amount,
                String userEmail,
                String userNickname,
                String providerIdempotencyKey,
                BillingCycle targetBillingCycle) {
            return new UpgradeClaim(
                    UpgradeAction.CALL_PROVIDER,
                    agreementID,
                    orderID,
                    billingKeyCiphertext,
                    providerCustomerKey,
                    orderName,
                    amount,
                    userEmail,
                    userNickname,
                    providerIdempotencyKey,
                    targetBillingCycle);
        }

        private static UpgradeClaim finalizeOnly(
                Long agreementID,
                String orderID,
                BillingCycle targetBillingCycle) {
            return new UpgradeClaim(
                    UpgradeAction.FINALIZE_ONLY,
                    agreementID,
                    orderID,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    targetBillingCycle);
        }
    }

    public record BillingConfirmClaim(
            BillingConfirmAction action,
            Long agreementID,
            String orderID,
            PaymentPurpose purpose,
            String providerCustomerKey,
            String orderName,
            BigDecimal amount,
            String userEmail,
            String userNickname,
            String providerIdempotencyKey,
            BillingAgreementConfirmResponse response) {

        private static BillingConfirmClaim callProvider(
                Long agreementID,
                String orderID,
                PaymentPurpose purpose,
                String providerCustomerKey,
                String orderName,
                BigDecimal amount,
                String userEmail,
                String userNickname,
                String providerIdempotencyKey) {
            return new BillingConfirmClaim(
                    BillingConfirmAction.CALL_PROVIDER,
                    agreementID,
                    orderID,
                    purpose,
                    providerCustomerKey,
                    orderName,
                    amount,
                    userEmail,
                    userNickname,
                    providerIdempotencyKey,
                    null);
        }

        private static BillingConfirmClaim finalizeOnly(Long agreementID, String orderID) {
            return new BillingConfirmClaim(
                    BillingConfirmAction.FINALIZE_ONLY,
                    agreementID,
                    orderID,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        private static BillingConfirmClaim completed(
                Long agreementID,
                String orderID,
                BillingAgreementConfirmResponse response) {
            return new BillingConfirmClaim(
                    BillingConfirmAction.COMPLETED,
                    agreementID,
                    orderID,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    response);
        }
    }

    private record LockedBillingCommand(BillingAgreement agreement, PaymentOrder order) {
    }
}
