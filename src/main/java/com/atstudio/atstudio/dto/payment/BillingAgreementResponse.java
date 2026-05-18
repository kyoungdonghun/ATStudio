package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BillingAgreementResponse(
        PaymentProviderType provider,
        BillingAgreementStatus status,
        String customerKey,
        String payMethod,
        String maskedMethod,
        LocalDate nextBillingAt,
        LocalDateTime lastChargedAt,
        LocalDateTime cancelledAt,
        UserSubscriptionResponse subscription
) {
}
