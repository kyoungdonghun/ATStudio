package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import jakarta.validation.constraints.NotNull;

public record PaymentPrepareRequest(
        @NotNull PaymentPurpose purpose,
        @NotNull Long subscriptionId,
        @NotNull BillingCycle billingCycle
) {}
