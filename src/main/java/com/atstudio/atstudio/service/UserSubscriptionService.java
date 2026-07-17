package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.AdminUpdateSubscriptionRequest;
import com.atstudio.atstudio.dto.subscription.ChangeSubscriptionRequest;
import com.atstudio.atstudio.dto.subscription.ChangeSubscriptionResponse;
import com.atstudio.atstudio.dto.subscription.SubscriptionResponse;
import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS;
    private static final int PAYMENT_EXPIRY_MINUTES = 10;
    private static final String CHANGE_TYPE_UPGRADE = "UPGRADE";
    private static final String CHANGE_TYPE_SCHEDULED_CHANGE = "SCHEDULED_CHANGE";
    private static final String CHANGE_TYPE_NO_CHANGE = "NO_CHANGE";

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BillingAgreementRepository billingAgreementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final BillingKeyCrypto billingKeyCrypto;
    private final PaymentReceiptEvidenceService paymentReceiptEvidenceService;
    private final TransactionTemplate transactionTemplate;
    private final PaymentCommandTransactionService paymentCommandTransactionService;
    private final SubscriptionUpgradePaymentExecutor subscriptionUpgradePaymentExecutor;
    private final List<RecurringPaymentProvider> recurringProviders;

    // 6.4 GET /api/user-subscriptions/me
    public UserSubscriptionResponse getMySubscription(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        return UserSubscriptionResponse.from(userSubscription);
    }

    // 6.5 GET /api/user-subscriptions
    public ResponseDTO<UserSubscriptionResponse> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserSubscription> result = userSubscriptionRepository.findAll(pageable);
        List<UserSubscriptionResponse> dataList = result.getContent().stream()
                .map(UserSubscriptionResponse::from)
                .toList();
        int total = (int) result.getTotalElements();

        return ResponseDTO.<UserSubscriptionResponse>builder()
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, total, 10))
                .build();
    }

    // 6.7 PUT /api/user-subscriptions/me
    @Transactional(propagation = Propagation.NEVER)
    public ChangeSubscriptionResponse changeSubscription(
            CustomUserDetails userDetails,
            ChangeSubscriptionRequest request) {
        SubscriptionChangePlan plan = transactionTemplate.execute(
                status -> planSubscriptionChange(userDetails, request));
        if (plan == null) {
            throw new IllegalStateException("Subscription change planning returned no result.");
        }
        if (plan.localResponse() != null) {
            return plan.localResponse();
        }
        return processChargedUpgrade(
                plan.userID(),
                plan.currentSubscriptionID(),
                plan.targetSubscriptionID(),
                plan.targetBillingCycle());
    }

    private SubscriptionChangePlan planSubscriptionChange(
            CustomUserDetails userDetails,
            ChangeSubscriptionRequest request) {
        User user = findUser(userDetails);
        UserSubscription current = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        Subscription newPlan = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        if (newPlan.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }

        if (isSamePlanAndCycle(current, newPlan, request.billingCycle())) {
            reactivateIfCancelled(user, current);
            current.clearPendingChange();
            return SubscriptionChangePlan.local(new ChangeSubscriptionResponse(
                    SubscriptionResponse.from(current.getSubscription()),
                    current.getBillingCycle().name(),
                    current.getStatus().name(),
                    CHANGE_TYPE_NO_CHANGE,
                    BigDecimal.ZERO,
                    current.getStartedAt(),
                    current.getExpiresAt()
            ));
        }

        boolean isUpgrade = newPlan.getPriceMonthly().compareTo(
                current.getSubscription().getPriceMonthly()) > 0;

        if (isUpgrade) {
            BigDecimal proratedAmount = calculateProratedUpgradeAmount(current, newPlan);
            if (requiresImmediateCharge(proratedAmount)) {
                return SubscriptionChangePlan.charged(
                        user.getId(),
                        current.getId(),
                        newPlan.getId(),
                        request.billingCycle());
            }

            findReusableBillingAgreement(user, current);
            reactivateIfCancelled(user, current);
            current.upgradeKeepingPeriod(newPlan, request.billingCycle());

            return SubscriptionChangePlan.local(new ChangeSubscriptionResponse(
                    SubscriptionResponse.from(newPlan),
                    request.billingCycle().name(),
                    current.getStatus().name(),
                    CHANGE_TYPE_UPGRADE,
                    proratedAmount,
                    current.getStartedAt(),
                    current.getExpiresAt()
            ));
        }

        reactivateIfCancelled(user, current);
        current.schedulePendingChange(newPlan, request.billingCycle());
        return SubscriptionChangePlan.local(new ChangeSubscriptionResponse(
                SubscriptionResponse.from(newPlan),
                request.billingCycle().name(),
                current.getStatus().name(),
                CHANGE_TYPE_SCHEDULED_CHANGE,
                BigDecimal.ZERO,
                current.getStartedAt(),
                current.getExpiresAt()
        ));
    }

    // 6.8 PUT /api/user-subscriptions/{id}
    @Transactional
    public UserSubscriptionResponse adminUpdate(Long id, AdminUpdateSubscriptionRequest request) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        userSubscription.adminUpdate(request.status(), request.billingCycle(), request.expiresAt());
        return UserSubscriptionResponse.from(userSubscription);
    }

    // 6.9 DELETE /api/user-subscriptions/{id}
    @Transactional
    public void adminCancel(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));
        userSubscription.cancel();
    }

    // 6.10 DELETE /api/user-subscriptions/me
    @Transactional
    public void selfCancel(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        userSubscription.cancel();
        billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .filter(agreement -> agreement.getStatus() != BillingAgreementStatus.CANCELLED
                        && agreement.getStatus() != BillingAgreementStatus.EXPIRED)
                .ifPresent(BillingAgreement::cancel);
    }

    // 6.11 POST /api/user-subscriptions/me/reactivate
    @Transactional
    public UserSubscriptionResponse reactivate(CustomUserDetails userDetails) {
        User user = findUser(userDetails);
        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        reactivateIfCancelled(user, userSubscription);
        return UserSubscriptionResponse.from(userSubscription);
    }

    private User findUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private BillingAgreement findReusableBillingAgreement(User user, UserSubscription subscription) {
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        if (isBlank(agreement.getBillingKeyCiphertext())) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        if (agreement.getStatus() == BillingAgreementStatus.ACTIVE) {
            return agreement;
        }
        if (subscription.getStatus() == SubscriptionStatus.CANCELLED
                && agreement.getStatus() == BillingAgreementStatus.CANCELLED) {
            return agreement;
        }
        throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
    }

    private void reactivateIfCancelled(User user, UserSubscription subscription) {
        if (subscription.getStatus() != SubscriptionStatus.CANCELLED) {
            return;
        }

        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        if (agreement.getStatus() == BillingAgreementStatus.CANCELLED) {
            try {
                agreement.resume(subscription.getExpiresAt());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
            }
        } else if (agreement.getStatus() != BillingAgreementStatus.ACTIVE) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        subscription.reactivate();
    }

    private boolean isSamePlanAndCycle(
            UserSubscription current,
            Subscription newPlan,
            BillingCycle requestedBillingCycle) {
        return current.getSubscription().getId().equals(newPlan.getId())
                && current.getBillingCycle() == requestedBillingCycle;
    }

    private BigDecimal calculateProratedUpgradeAmount(UserSubscription current, Subscription newPlan) {
        LocalDate today = LocalDate.now();
        long remainingDays = Math.max(0, ChronoUnit.DAYS.between(today, current.getExpiresAt()));
        long totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt());
        if (remainingDays == 0 || totalDays <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal currentPrice = priceFor(current.getSubscription(), current.getBillingCycle());
        BigDecimal newPrice = priceFor(newPlan, current.getBillingCycle());
        BigDecimal difference = newPrice.subtract(currentPrice);
        if (difference.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return difference.multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                .setScale(0, RoundingMode.HALF_UP);
    }

    private PaymentOrder createUpgradeOrder(
            User user,
            UserSubscription current,
            Subscription newPlan,
            BillingCycle chargedBillingCycle,
            BigDecimal amount,
            BillingAgreement agreement) {
        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.builder()
                .orderId(generateUpgradeOrderId())
                .user(user)
                .purpose(PaymentPurpose.UPGRADE)
                .provider(RECURRING_PROVIDER)
                .subscription(newPlan)
                .userSubscription(current)
                .billingAgreement(agreement)
                .billingCycle(chargedBillingCycle)
                .amount(amount)
                .currency("KRW")
                .expiresAt(LocalDateTime.now().plusMinutes(PAYMENT_EXPIRY_MINUTES))
                .build());
        order.markInProgress("{\"source\":\"subscription-upgrade\"}");
        return order;
    }

    private BillingChargeResult chargeUpgrade(PaymentOrder order, BillingAgreement agreement) {
        return recurringProvider().charge(new BillingChargeCommand(
                billingKeyCrypto.decrypt(agreement.getBillingKeyCiphertext()),
                agreement.getProviderCustomerKey(),
                order.getOrderId(),
                orderName(order),
                order.getAmount(),
                order.getUser().getEmail(),
                order.getUser().getNickname(),
                "subscription-upgrade-" + order.getOrderId()
        ));
    }

    private ChangeSubscriptionResponse processChargedUpgrade(
            Long userID,
            Long currentSubscriptionID,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle) {
        PaymentCommandTransactionService.UpgradeClaim claim =
                paymentCommandTransactionService.claimUpgrade(
                        userID,
                        currentSubscriptionID,
                        targetSubscriptionID,
                        targetBillingCycle,
                        LocalDateTime.now());
        if (claim.action() == PaymentCommandTransactionService.UpgradeAction.FINALIZE_ONLY) {
            return paymentCommandTransactionService.finalizeUpgrade(
                    userID,
                    claim.agreementID(),
                    claim.orderID());
        }

        BillingChargeResult chargeResult;
        try {
            chargeResult = subscriptionUpgradePaymentExecutor.charge(claim);
        } catch (RuntimeException exception) {
            recordUpgradeProviderFailure(
                    claim,
                    "SUBSCRIPTION_UPGRADE_CHARGE_EXCEPTION",
                    exception.getClass().getSimpleName(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED);
        }
        if (chargeResult == null) {
            recordUpgradeProviderFailure(
                    claim,
                    "SUBSCRIPTION_UPGRADE_CHARGE_EMPTY_RESULT",
                    "Provider returned no upgrade-charge result.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED);
        }
        if (!chargeResult.success()) {
            boolean removedBillingKey = isRemovedBillingKeyFailure(chargeResult);
            recordUpgradeProviderFailure(
                    claim,
                    chargeResult.failureCode(),
                    chargeResult.failureMessage(),
                    PaymentCommandTransactionService.ProviderFailureDisposition.FAILED,
                    !removedBillingKey);
            if (removedBillingKey) {
                paymentCommandTransactionService.expireIssuedBillingKeyAfterProviderRemoval(claim.agreementID());
                throw billingAgreementReauthRequired(chargeResult);
            }
            throw paymentConfirmFailed(chargeResult);
        }
        if (isBlank(chargeResult.transactionId())) {
            recordUpgradeProviderFailure(
                    claim,
                    "PROVIDER_TRANSACTION_MISSING",
                    "Provider success did not include a transaction ID.",
                    PaymentCommandTransactionService.ProviderFailureDisposition.PENDING_PROVIDER_CONFIRMATION,
                    true);
            throw new BusinessException(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED);
        }

        paymentCommandTransactionService.recordProviderSuccess(
                claim.agreementID(),
                claim.orderID(),
                chargeResult.transactionId(),
                chargeResult.providerPayload(),
                chargeResult.payMethod(),
                chargeResult.maskedMethod());
        return paymentCommandTransactionService.finalizeUpgrade(
                userID,
                claim.agreementID(),
                claim.orderID());
    }

    private void recordUpgradeProviderFailure(
            PaymentCommandTransactionService.UpgradeClaim claim,
            String failureCode,
            String failureMessage,
            PaymentCommandTransactionService.ProviderFailureDisposition disposition,
            boolean recordFailedCharge) {
        paymentCommandTransactionService.recordProviderFailure(
                claim.agreementID(),
                claim.orderID(),
                failureCode,
                failureMessage,
                disposition,
                recordFailedCharge);
    }

    private BusinessException paymentConfirmFailed(BillingChargeResult chargeResult) {
        return new BusinessException(
                BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED,
                new IllegalStateException(providerFailureDetail(
                        chargeResult.failureCode(),
                        chargeResult.failureMessage())));
    }

    private BusinessException billingAgreementReauthRequired(BillingChargeResult chargeResult) {
        return new BusinessException(
                BUSINESS_ERROR.BILLING_AGREEMENT_REAUTH_REQUIRED,
                new IllegalStateException(providerFailureDetail(
                        chargeResult.failureCode(),
                        chargeResult.failureMessage())));
    }

    private RecurringPaymentProvider recurringProvider() {
        return recurringProviders.stream()
                .filter(provider -> provider.getProviderType() == RECURRING_PROVIDER)
                .findFirst()
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.PAYMENT_PROVIDER_NOT_CONFIGURED));
    }

    private BigDecimal priceFor(Subscription subscription, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
    }

    private boolean requiresImmediateCharge(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    private String orderName(PaymentOrder order) {
        return "AT.M " + order.getSubscription().getName() + " Upgrade";
    }

    private String generateUpgradeOrderId() {
        return "ATS-UPG-" + LocalDate.now().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String providerFailureDetail(String failureCode, String failureMessage) {
        return "providerFailureCode=" + nullToDash(failureCode)
                + ", providerFailureMessage=" + nullToDash(failureMessage);
    }

    private boolean isRemovedBillingKeyFailure(BillingChargeResult chargeResult) {
        String code = chargeResult.failureCode();
        if (isBlank(code)) {
            return false;
        }
        String normalized = code.toUpperCase(Locale.ROOT);
        return normalized.contains("BILLING_KEY")
                && (normalized.contains("REMOVED")
                || normalized.contains("NOT_FOUND")
                || normalized.contains("INVALID"));
    }

    private void expireRemovedBillingKey(BillingAgreement agreement) {
        agreement.expireIssuedKey();
    }

    private String nullToDash(String value) {
        return isBlank(value) ? "-" : value;
    }

    private record SubscriptionChangePlan(
            Long userID,
            Long currentSubscriptionID,
            Long targetSubscriptionID,
            BillingCycle targetBillingCycle,
            ChangeSubscriptionResponse localResponse) {

        private static SubscriptionChangePlan charged(
                Long userID,
                Long currentSubscriptionID,
                Long targetSubscriptionID,
                BillingCycle targetBillingCycle) {
            return new SubscriptionChangePlan(
                    userID,
                    currentSubscriptionID,
                    targetSubscriptionID,
                    targetBillingCycle,
                    null);
        }

        private static SubscriptionChangePlan local(ChangeSubscriptionResponse response) {
            return new SubscriptionChangePlan(null, null, null, null, response);
        }
    }
}
