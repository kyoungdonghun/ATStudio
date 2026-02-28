package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.subscription.*;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.CompanyCertificationStatus;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.payment.PaymentService;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CompanyCertificationRepository companyCertificationRepository;
    private final PaymentService paymentService;

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
        userSubscriptionRepository.findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
                .ifPresent(us -> {
                    throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_ALREADY_EXISTS);
                });

        Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        LocalDate startedAt = LocalDate.now();
        LocalDate expiresAt = request.billingCycle() == BillingCycle.MONTHLY
                ? startedAt.plusMonths(1) : startedAt.plusYears(1);

        UserSubscription userSubscription = userSubscriptionRepository.save(
                UserSubscription.builder()
                        .user(user)
                        .subscription(subscription)
                        .billingCycle(request.billingCycle())
                        .startedAt(startedAt)
                        .expiresAt(expiresAt)
                        .build());

        BigDecimal amount = request.billingCycle() == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly() : subscription.getPriceYearly();

        paymentService.processPayment(user, userSubscription, subscription,
                request.billingCycle(), amount);

        return UserSubscriptionResponse.from(userSubscription);
    }

    // -- 6.4 GET /api/user-subscriptions/me ----------------------------------

    public UserSubscriptionResponse getMySubscription(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        UserSubscription userSubscription = userSubscriptionRepository
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
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
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        Subscription newPlan = subscriptionRepository.findById(request.subscriptionId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        // prorated amount 계산
        LocalDate today = LocalDate.now();
        long remainingDays = ChronoUnit.DAYS.between(today, current.getExpiresAt());
        long totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt());

        BigDecimal currentPrice = current.getBillingCycle() == BillingCycle.MONTHLY
                ? current.getSubscription().getPriceMonthly()
                : current.getSubscription().getPriceYearly();

        BigDecimal refundAmount = totalDays > 0
                ? currentPrice.multiply(BigDecimal.valueOf(remainingDays))
                    .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal newPrice = request.billingCycle() == BillingCycle.MONTHLY
                ? newPlan.getPriceMonthly() : newPlan.getPriceYearly();

        BigDecimal proratedAmount = newPrice.subtract(refundAmount);

        LocalDate newExpiresAt = request.billingCycle() == BillingCycle.MONTHLY
                ? today.plusMonths(1) : today.plusYears(1);

        current.upgrade(newPlan, request.billingCycle(), newExpiresAt);

        // 양수 = 추가 결제, 음수 = 환불
        paymentService.processPayment(user, current, newPlan,
                request.billingCycle(), proratedAmount);

        return new ChangeSubscriptionResponse(
                SubscriptionResponse.from(newPlan),
                request.billingCycle().name(),
                current.getStatus().name(),
                proratedAmount,
                current.getStartedAt(),
                current.getExpiresAt()
        );
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
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        userSubscription.cancel();
    }
}
