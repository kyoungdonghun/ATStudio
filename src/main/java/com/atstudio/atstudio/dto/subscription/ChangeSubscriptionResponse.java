package com.atstudio.atstudio.dto.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChangeSubscriptionResponse(
        SubscriptionResponse subscription,
        String billingCycle,
        String status,
        String changeType,
        BigDecimal proratedAmount,
        LocalDate startedAt,
        LocalDate expiresAt
) {}
