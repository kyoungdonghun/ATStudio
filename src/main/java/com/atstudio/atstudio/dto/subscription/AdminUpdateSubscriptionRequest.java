package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;

import java.time.LocalDate;

public record AdminUpdateSubscriptionRequest(
        SubscriptionStatus status,
        BillingCycle billingCycle,
        LocalDate expiresAt
) {}
