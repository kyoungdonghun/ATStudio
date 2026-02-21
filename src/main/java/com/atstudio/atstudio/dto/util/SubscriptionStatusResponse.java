package com.atstudio.atstudio.dto.util;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionStatusResponse(
        boolean hasSubscription,
        String planName,
        String userType,
        Integer downloadPerDay,
        Integer maxWhitelistChannels
) {
    public static SubscriptionStatusResponse noSubscription() {
        return new SubscriptionStatusResponse(false, null, null, null, null);
    }
}
