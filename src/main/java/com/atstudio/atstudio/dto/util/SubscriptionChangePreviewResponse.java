package com.atstudio.atstudio.dto.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionChangePreviewResponse(
        String changeType,
        BigDecimal proratedAmount,
        LocalDate effectiveDate,
        LocalDate nextBillingDate,
        BigDecimal nextBillingAmount,
        String newPlanName,
        String newBillingCycle
) {
}
