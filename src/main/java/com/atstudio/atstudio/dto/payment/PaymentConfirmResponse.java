package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.dto.subscription.UserSubscriptionResponse;
import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

public record PaymentConfirmResponse(
        String orderId,
        PaymentOrderStatus status,
        PaymentPurpose purpose,
        UserSubscriptionResponse subscription
) {}
