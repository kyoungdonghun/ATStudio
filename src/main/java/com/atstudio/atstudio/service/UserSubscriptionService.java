package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.*;
import com.atstudio.atstudio.entity.BillingAgreement;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.BillingAgreementRepository;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.PaymentService;
import com.atstudio.atstudio.service.payment.billing.BillingKeyCrypto;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeCommand;
import com.atstudio.atstudio.service.payment.provider.recurring.BillingChargeResult;
import com.atstudio.atstudio.service.payment.provider.recurring.RecurringPaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserSubscriptionService {

    private static final PaymentProviderType RECURRING_PROVIDER = PaymentProviderType.TOSS_BILLING;
    private static final int PAYMENT_EXPIRY_MINUTES = 10;

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PaymentService paymentService;
    private final PlaylistService playlistService;
    private final BillingAgreementRepository billingAgreementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final BillingKeyCrypto billingKeyCrypto;
    private final List<RecurringPaymentProvider> recurringProviders;

    // -- 6.3 POST /api/user-subscriptions ------------------------------------

    @Transactional
    public UserSubscriptionResponse subscribe(CustomUserDetails userDetails,
                                               UserSubscriptionRequest request) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        // BUSINESS 회원 기업 인증 체크
        if (user.getUserType() == UserType.BUSINESS) {
            boolean approved = companyCertificationRepository.existsByUserAndStatusIn(
                    user, List.of(CompanyCertificationStatus.APPROVED));
            if (!approved) {
                throw new BusinessException(BUSINESS_ERROR.COMPANY_CERTIFICATION_REQUIRED);
            }
        }

        // 중복 구독 체크
        userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .ifPresent(us -> {
                    throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
                });

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }

        LocalDate startedAt = LocalDate.now();
        LocalDate expiresAt = request.billingCycle() == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1) : startedAt.plusYears(1);

        UserSubscription userSubscription = userSubscriptionRepository.findByUser(user)
                .map(existing -> {
                    existing.startNewSubscription(
                            subscription,
                            request.billingCycle(),
                            startedAt,
                            expiresAt);
                    return existing;
                })
                .orElseGet(() -> userSubscriptionRepository.save(
                        UserSubscription.builder()
                                .user(user)
                                .subscription(subscription)
                                .billingCycle(request.billingCycle())
                                .startedAt(startedAt)
                                .expiresAt(expiresAt)
                                .build()));

        BigDecimal amount = request.billingCycle() == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly() : subscription.getPriceYearly();

        paymentService.processPayment(user, userSubscription, subscription,
                request.billingCycle(), amount);

        playlistService.createDefaultPlaylist(user);

        return UserSubscriptionResponse.from(userSubscription);
    }

    // -- 6.4 GET /api/user-subscriptions/me ----------------------------------

    public UserSubscriptionResponse getMySubscription(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        return UserSubscriptionResponse.from(userSubscription);
    }

    // -- 6.5 GET /api/user-subscriptions -------------------------------------

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

    // -- 6.6 GET /api/user-subscriptions/{id} --------------------------------

    public UserSubscriptionResponse getDetail(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        return UserSubscriptionResponse.from(userSubscription);
    }

    // -- 6.7 PUT /api/user-subscriptions/me ----------------------------------

    @Transactional
    public ChangeSubscriptionResponse changeSubscription(CustomUserDetails userDetails,
                                                          ChangeSubscriptionRequest request) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        UserSubscription current = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        Subscription newPlan = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        if (newPlan.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }

        // UPGRADE vs DOWNGRADE 판정: 월간 가격 기준
        boolean isUpgrade = newPlan.getPriceMonthly().compareTo(
                current.getSubscription().getPriceMonthly()) > 0;

        if (isUpgrade) {
            BigDecimal proratedAmount = calculateProratedUpgradeAmount(current, newPlan);
            BillingAgreement agreement = findActiveBillingAgreement(user);
            if (requiresImmediateCharge(proratedAmount)) {
                PaymentOrder order = createUpgradeOrder(
                        user, current, newPlan, current.getBillingCycle(), proratedAmount, agreement);
                BillingChargeResult chargeResult = chargeUpgrade(order, agreement);
                if (!chargeResult.success()) {
                    order.markFailed(chargeResult.failureCode(), chargeResult.failureMessage());
                    throw new BusinessException(BUSINESS_ERROR.PAYMENT_CONFIRM_FAILED);
                }

                subscriptionPaymentRepository.save(SubscriptionPayment.builder()
                        .paymentOrder(order)
                        .billingAgreement(agreement)
                        .provider(order.getProvider())
                        .user(user)
                        .userSubscription(current)
                        .subscription(newPlan)
                        .billingCycle(current.getBillingCycle())
                        .amount(proratedAmount)
                        .paymentStatus(PaymentStatus.DONE)
                        .pgTransactionId(chargeResult.transactionId())
                        .build());
                order.markDone(chargeResult.transactionId(), current, chargeResult.providerPayload());
                agreement.recordSuccessfulCharge(agreement.getNextBillingAt());
            }

            current.upgradeKeepingPeriod(newPlan, request.billingCycle());

            return new ChangeSubscriptionResponse(
                    SubscriptionResponse.from(newPlan),
                    request.billingCycle().name(),
                    current.getStatus().name(),
                    "UPGRADE",
                    proratedAmount,
                    current.getStartedAt(),
                    current.getExpiresAt()
            );
        } else {
            // DOWNGRADE: pending 예약만, 현재 구독 유지, payment 없음
            current.schedulePendingChange(newPlan, request.billingCycle());

            return new ChangeSubscriptionResponse(
                    SubscriptionResponse.from(newPlan),
                    request.billingCycle().name(),
                    current.getStatus().name(),
                    "DOWNGRADE",
                    BigDecimal.ZERO,
                    current.getStartedAt(),
                    current.getExpiresAt()
            );
        }
    }

    private BillingAgreement findActiveBillingAgreement(User user) {
        BillingAgreement agreement = billingAgreementRepository.findByUserAndProvider(user, RECURRING_PROVIDER)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_NOT_FOUND));
        if (agreement.getStatus() != BillingAgreementStatus.ACTIVE || isBlank(agreement.getBillingKeyCiphertext())) {
            throw new BusinessException(BUSINESS_ERROR.BILLING_AGREEMENT_INVALID_STATE);
        }
        return agreement;
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
        return "ATStudio " + order.getSubscription().getName() + " Upgrade";
    }

    private String generateUpgradeOrderId() {
        return "ATS-UPG-" + LocalDate.now().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // -- 6.8 PUT /api/user-subscriptions/{id} --------------------------------

    @Transactional
    public UserSubscriptionResponse adminUpdate(Long id, AdminUpdateSubscriptionRequest request) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        userSubscription.adminUpdate(request.status(), request.billingCycle(), request.expiresAt());

        return UserSubscriptionResponse.from(userSubscription);
    }

    // -- 6.9 DELETE /api/user-subscriptions/{id} -----------------------------

    @Transactional
    public void adminCancel(Long id) {
        UserSubscription userSubscription = userSubscriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        userSubscription.cancel();
    }

    // -- 6.10 DELETE /api/user-subscriptions/me ------------------------------

    @Transactional
    public void selfCancel(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        userSubscription.cancel();
    }
}
