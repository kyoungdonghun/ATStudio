package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentOrderStatus;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

public record PaymentOrderResponse(
        String orderId,
        PaymentOrderStatus status,
        PaymentPurpose purpose
) {}
