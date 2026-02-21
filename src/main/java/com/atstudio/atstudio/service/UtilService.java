package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.SubscriptionStatusResponse;
import com.atstudio.atstudio.dto.util.UserTypeResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UtilService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final TrackDownloadRepository trackDownloadRepository;

    public SubscriptionStatusResponse getSubscriptionStatus(CustomUserDetails userDetails) {
        User user = findUser(userDetails.getId());
        Optional<UserSubscription> subscriptionOpt = userSubscriptionRepository
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now());

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
                .findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now());

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayDownloads = trackDownloadRepository.countByUserAndDownloadedAtBetween(user, todayStart, todayEnd);

        if (subscriptionOpt.isEmpty()) {
            return new DownloadCountResponse(todayDownloads, 0, 0);
        }

        int dailyLimit = subscriptionOpt.get().getSubscription().getDownloadPerDay();

        long remaining;
        if (dailyLimit == -1) {
            remaining = -1;
        } else {
            remaining = Math.max(0, dailyLimit - todayDownloads);
        }

        return new DownloadCountResponse(todayDownloads, dailyLimit, remaining);
    }

    public UserTypeResponse getUserType(CustomUserDetails userDetails) {
        User user = findUser(userDetails.getId());
        return new UserTypeResponse(
                user.getUserType().name(),
                user.getJob() != null ? user.getJob().name() : null
        );
    }

    private User findUser(Long userID) {
        return userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }
}
