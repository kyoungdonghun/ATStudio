package com.atstudio.atstudio.service.payment.provider.recurring;

import java.math.BigDecimal;

public record BillingChargeCommand(
        String billingKey,
        String providerCustomerKey,
        String orderId,
        String orderName,
        BigDecimal amount,
        String customerEmail,
        String customerName,
        String idempotencyKey
) {
}
