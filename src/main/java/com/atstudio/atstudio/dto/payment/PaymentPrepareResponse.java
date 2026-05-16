package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentPrepareResponse(
        String orderId,
        PaymentProviderType provider,
        PaymentPurpose purpose,
        BigDecimal amount,
        String currency,
        LocalDateTime expiresAt,
        PaymentCheckoutResponse checkout
) {}
