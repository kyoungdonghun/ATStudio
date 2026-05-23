package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillingAgreementPrepareResponse(
        String orderId,
        PaymentProviderType provider,
        PaymentPurpose purpose,
        BillingAgreementStatus agreementStatus,
        Long subscriptionId,
        BillingCycle billingCycle,
        BigDecimal amount,
        String currency,
        LocalDateTime expiresAt,
        BillingAgreementCheckoutResponse checkout
) {
}
