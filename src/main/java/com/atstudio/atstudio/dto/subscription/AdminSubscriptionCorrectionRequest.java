package com.atstudio.atstudio.dto.subscription;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminSubscriptionCorrectionRequest(
        @NotNull Long userSubscriptionId,
        @NotNull Long targetSubscriptionId,
        @NotNull BillingCycle targetBillingCycle,
        @NotNull SubscriptionStatus targetStatus,
        @NotNull LocalDate targetExpiresAt,
        boolean clearPendingChange,
        boolean cancelBillingAgreement,
        @NotBlank @Size(max = 500) String reasonNote
) {
}
