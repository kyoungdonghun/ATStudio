package com.atstudio.atstudio.service.payment.provider.refund;

import java.math.BigDecimal;

public record PaymentRefundProviderCommand(
        String providerPaymentKey,
        String orderId,
        BigDecimal amount,
        String reason,
        String idempotencyKey
) {
}
