package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BillingAgreementPrepareRequest(
        @NotNull @Positive Long subscriptionId,
        @NotNull BillingCycle billingCycle,
        @NotNull PaymentPurpose purpose
) {
    @AssertTrue(message = "purpose must be SUBSCRIBE or BILLING_AGREEMENT")
    public boolean isSupportedPurpose() {
        return purpose == null
                || purpose == PaymentPurpose.SUBSCRIBE
                || purpose == PaymentPurpose.BILLING_AGREEMENT;
    }
}
