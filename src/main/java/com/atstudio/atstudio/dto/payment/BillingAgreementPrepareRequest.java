package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;

public record BillingAgreementPrepareRequest(
        @NotNull Long subscriptionId,
        @NotNull BillingCycle billingCycle
) {
}
