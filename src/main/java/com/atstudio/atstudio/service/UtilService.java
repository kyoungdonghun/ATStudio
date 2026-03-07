package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.SubscriptionChangePreviewResponse;
import com.atstudio.atstudio.dto.util.SubscriptionStatusResponse;
import com.atstudio.atstudio.dto.util.UserTypeResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UtilService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TrackDownloadRepository trackDownloadRepository;

    public SubscriptionStatusResponse getSubscriptionStatus(CustomUserDetails userDetails) {
        User user = findUser(userDetails.getId());
        Optional<UserSubscription> subscriptionOpt = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now());

        if (subscriptionOpt.isEmpty()) {
            return SubscriptionStatusResponse.noSubscription();
        }

        UserSubscription userSubscription = subscriptionOpt.get();
        Subscription plan = userSubscription.getSubscription();

        return new SubscriptionStatusResponse(
                true,
                plan.getName(),
                plan.getUserType().name(),
                plan.getDownloadPerDay(),
                plan.getMaxWhitelistChannels()
        );
    }

    public DownloadCountResponse getDownloadCount(CustomUserDetails userDetails) {
        User user = findUser(userDetails.getId());
        Optional<UserSubscription> subscriptionOpt = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayDownloads = trackDownloadRepository.countByUserAndDownloadedAtBetween(user, todayStart, todayEnd);

        LocalDateTime nextResetAt = LocalDate.now().plusDays(1).atStartOfDay();

        if (subscriptionOpt.isEmpty()) {
            return new DownloadCountResponse(todayDownloads, 0, 0, nextResetAt);
        }

        int dailyLimit = subscriptionOpt.get().getSubscription().getDownloadPerDay();

        long remaining;
        if (dailyLimit == -1) {
            remaining = -1;
        } else {
            remaining = Math.max(0, dailyLimit - todayDownloads);
        }

        return new DownloadCountResponse(todayDownloads, dailyLimit, remaining, nextResetAt);
    }

    public UserTypeResponse getUserType(CustomUserDetails userDetails) {
        User user = findUser(userDetails.getId());
        return new UserTypeResponse(
                user.getUserType().name(),
                user.getJob() != null ? user.getJob().name() : null
        );
    }

    public SubscriptionChangePreviewResponse previewSubscriptionChange(
            CustomUserDetails userDetails, Long subscriptionId, String billingCycleStr) {

        BillingCycle billingCycle;
        try {
            billingCycle = BillingCycle.valueOf(billingCycleStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        User user = findUser(userDetails.getId());

        UserSubscription current = userSubscriptionRepository
                .findActiveByUser(user, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));

        Subscription newPlan = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_NOT_FOUND));

        BigDecimal currentPriceMonthly = current.getSubscription().getPriceMonthly();
        BigDecimal newPriceMonthly = newPlan.getPriceMonthly();

        boolean isUpgrade = newPriceMonthly.compareTo(currentPriceMonthly) > 0;

        if (isUpgrade) {
            // UPGRADE: prorated 계산 (UserSubscriptionService:150-167 로직 참조)
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

            BigDecimal newPrice = billingCycle == BillingCycle.MONTHLY
                    ? newPlan.getPriceMonthly() : newPlan.getPriceYearly();

            BigDecimal proratedAmount = newPrice.subtract(refundAmount);

            return new SubscriptionChangePreviewResponse(
                    "UPGRADE",
                    proratedAmount,
                    today,
                    newPlan.getName(),
                    billingCycle.name()
            );
        } else {
            // DOWNGRADE: proratedAmount = 0, effectiveDate = current expiresAt
            return new SubscriptionChangePreviewResponse(
                    "DOWNGRADE",
                    BigDecimal.ZERO,
                    current.getExpiresAt(),
                    newPlan.getName(),
                    billingCycle.name()
            );
        }
    }

    private User findUser(Long userID) {
        return userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }
}
