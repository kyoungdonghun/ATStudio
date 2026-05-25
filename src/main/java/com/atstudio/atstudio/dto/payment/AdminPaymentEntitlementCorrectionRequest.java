package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminPaymentEntitlementCorrectionRequest(
        @NotNull Long paymentRefundId,
        @NotNull Long targetSubscriptionId,
        @NotNull BillingCycle targetBillingCycle,
        @NotNull SubscriptionStatus targetStatus,
        @NotNull LocalDate targetExpiresAt,
        boolean clearPendingChange,
        boolean cancelBillingAgreement,
        @Size(max = 500) String reasonNote
) {
}
