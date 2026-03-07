package com.atstudio.atstudio.dto.util;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionChangePreviewResponse(
        String changeType,
        BigDecimal proratedAmount,
        LocalDate effectiveDate,
        String newPlanName,
        String newBillingCycle
) {
}
