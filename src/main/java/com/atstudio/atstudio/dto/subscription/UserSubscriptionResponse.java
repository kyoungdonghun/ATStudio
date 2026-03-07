package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.UserSubscription;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserSubscriptionResponse(
        Long id,
        Long userId,
        String userNickname,
        SubscriptionResponse subscription,
        String billingCycle,
        String status,
        LocalDate startedAt,
        LocalDate expiresAt,
        Long pendingSubscriptionId,
        String pendingBillingCycle,
        LocalDateTime createdAt
) {
    public static UserSubscriptionResponse from(UserSubscription us) {
        return new UserSubscriptionResponse(
                us.getId(),
                us.getUser().getId(),
                us.getUser().getNickname(),
                SubscriptionResponse.from(us.getSubscription()),
                us.getBillingCycle().name(),
                us.getStatus().name(),
                us.getStartedAt(),
                us.getExpiresAt(),
                us.getPendingSubscription() != null ? us.getPendingSubscription().getId() : null,
                us.getPendingBillingCycle() != null ? us.getPendingBillingCycle().name() : null,
                us.getCreatedAt()
        );
    }
}
