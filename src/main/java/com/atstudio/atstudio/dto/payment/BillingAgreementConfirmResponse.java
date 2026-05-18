package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.enums.BillingAgreementStatus;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

import java.time.LocalDate;

public record BillingAgreementConfirmResponse(
        String orderId,
        PaymentOrderStatus orderStatus,
        PaymentProviderType provider,
        BillingAgreementStatus agreementStatus,
        LocalDate nextBillingAt,
        UserSubscriptionResponse subscription
) {
}
