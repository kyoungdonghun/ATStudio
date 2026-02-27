package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;

public record UserSubscriptionRequest(
        @NotNull Long subscriptionId,
        @NotNull BillingCycle billingCycle
) {}
