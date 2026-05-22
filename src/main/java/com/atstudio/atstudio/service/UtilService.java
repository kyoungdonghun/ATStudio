package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.PublicCapabilitiesResponse;
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
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UtilService {

    private static final String UNCONFIGURED = "UNCONFIGURED";
    private static final String LOCAL_SMTP = "LOCAL_SMTP";
    private static final String REMOTE_SMTP = "REMOTE_SMTP";
    private static final String CHANGE_TYPE_UPGRADE = "UPGRADE";
    private static final String CHANGE_TYPE_SCHEDULED_CHANGE = "SCHEDULED_CHANGE";
    private static final String CHANGE_TYPE_NO_CHANGE = "NO_CHANGE";

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TrackDownloadRepository trackDownloadRepository;
    private final TestUserBootstrapProperties testUserBootstrapProperties;
    private final PasswordLoginPolicy passwordLoginPolicy;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Value("${oauth2.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${oauth2.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${oauth2.kakao.redirect-uri:}")
    private String kakaoRedirectUri;

    @Value("${oauth2.naver.client-id:}")
    private String naverClientId;

    @Value("${oauth2.naver.client-secret:}")
    private String naverClientSecret;

    @Value("${oauth2.naver.redirect-uri:}")
    private String naverRedirectUri;

    public PublicCapabilitiesResponse getPublicCapabilities() {
        boolean passwordLoginEnabled = passwordLoginPolicy.isEnabled();
        String mailDeliveryMode = resolveMailDeliveryMode();
        PublicCapabilitiesResponse.MailCapability mailCapability =
                new PublicCapabilitiesResponse.MailCapability(
                        passwordLoginEnabled && !UNCONFIGURED.equals(mailDeliveryMode),
                        mailDeliveryMode
                );

        return new PublicCapabilitiesResponse(
                passwordLoginEnabled,
                mailCapability,
                mailCapability,
                new PublicCapabilitiesResponse.SocialLoginCapability(
                        buildProviderCapability(googleClientId, googleClientSecret, googleRedirectUri),
                        buildProviderCapability(kakaoClientId, kakaoClientSecret, kakaoRedirectUri),
                        buildProviderCapability(naverClientId, naverClientSecret, naverRedirectUri)
                ),
                testUserBootstrapProperties.isEnabled()
        );
    }

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

        if (newPlan.getUserType() != user.getUserType()) {
            throw new BusinessException(BUSINESS_ERROR.SUBSCRIPTION_USER_TYPE_MISMATCH);
        }

        BigDecimal currentPriceMonthly = current.getSubscription().getPriceMonthly();
        BigDecimal newPriceMonthly = newPlan.getPriceMonthly();

        if (current.getSubscription().getId().equals(newPlan.getId())
                && current.getBillingCycle() == billingCycle) {
            return new SubscriptionChangePreviewResponse(
                    CHANGE_TYPE_NO_CHANGE,
                    BigDecimal.ZERO,
                    LocalDate.now(),
                    current.getExpiresAt(),
                    priceFor(current.getSubscription(), current.getBillingCycle()),
                    current.getSubscription().getName(),
                    current.getBillingCycle().name()
            );
        }

        boolean isUpgrade = newPriceMonthly.compareTo(currentPriceMonthly) > 0;

        if (isUpgrade) {
            LocalDate today = LocalDate.now();
            long remainingDays = Math.max(0, ChronoUnit.DAYS.between(today, current.getExpiresAt()));
            long totalDays = ChronoUnit.DAYS.between(current.getStartedAt(), current.getExpiresAt());

            BigDecimal currentPrice = current.getBillingCycle() == BillingCycle.MONTHLY
                    ? current.getSubscription().getPriceMonthly()
                    : current.getSubscription().getPriceYearly();

            BigDecimal newPrice = current.getBillingCycle() == BillingCycle.MONTHLY
                    ? newPlan.getPriceMonthly() : newPlan.getPriceYearly();

            BigDecimal priceDifference = newPrice.subtract(currentPrice);
            BigDecimal proratedAmount = totalDays > 0 && remainingDays > 0 && priceDifference.signum() > 0
                    ? priceDifference.multiply(BigDecimal.valueOf(remainingDays))
                        .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                        .setScale(0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            return new SubscriptionChangePreviewResponse(
                    CHANGE_TYPE_UPGRADE,
                    proratedAmount,
                    today,
                    current.getExpiresAt(),
                    priceFor(newPlan, billingCycle),
                    newPlan.getName(),
                    billingCycle.name()
            );
        } else {
            // Deferred changes are payment-free until the next renewal.
            return new SubscriptionChangePreviewResponse(
                    CHANGE_TYPE_SCHEDULED_CHANGE,
                    BigDecimal.ZERO,
                    current.getExpiresAt(),
                    current.getExpiresAt(),
                    priceFor(newPlan, billingCycle),
                    newPlan.getName(),
                    billingCycle.name()
            );
        }
    }

    private BigDecimal priceFor(Subscription subscription, BillingCycle billingCycle) {
        return billingCycle == BillingCycle.MONTHLY
                ? subscription.getPriceMonthly()
                : subscription.getPriceYearly();
    }

    private User findUser(Long userID) {
        return userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    private PublicCapabilitiesResponse.ProviderCapability buildProviderCapability(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
        if (!StringUtils.hasText(clientId)
                || !StringUtils.hasText(clientSecret)
                || !StringUtils.hasText(redirectUri)) {
            return PublicCapabilitiesResponse.ProviderCapability.disabled();
        }

        return PublicCapabilitiesResponse.ProviderCapability.enabled(clientId, redirectUri);
    }

    private String resolveMailDeliveryMode() {
        if (!StringUtils.hasText(mailHost)) {
            return UNCONFIGURED;
        }

        String normalizedHost = mailHost.trim().toLowerCase(Locale.ROOT);
        if (normalizedHost.equals("localhost")
                || normalizedHost.equals("127.0.0.1")
                || normalizedHost.equals("::1")) {
            return LOCAL_SMTP;
        }

        return REMOTE_SMTP;
    }
}
