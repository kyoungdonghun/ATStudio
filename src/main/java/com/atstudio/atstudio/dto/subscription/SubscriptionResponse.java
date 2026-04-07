package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.Subscription;

import java.math.BigDecimal;

public record SubscriptionResponse(
        Long id,
        String name,
        String description,
        String userType,
        BigDecimal priceMonthly,
        BigDecimal priceYearly,
        int downloadPerDay,
        int maxWhitelistChannels,
        int maxPlaylists,
        boolean isActive
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getName(),
                subscription.getDescription(),
                subscription.getUserType().name(),
                subscription.getPriceMonthly(),
                subscription.getPriceYearly(),
                subscription.getDownloadPerDay(),
                subscription.getMaxWhitelistChannels(),
                subscription.getMaxPlaylists(),
                subscription.isActive()
        );
    }
}
