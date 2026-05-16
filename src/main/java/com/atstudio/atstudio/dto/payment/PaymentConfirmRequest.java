package com.atstudio.atstudio.dto.payment;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentConfirmRequest(
        @NotBlank String orderId,
        @NotNull BigDecimal amount,
        @NotNull PaymentProviderType provider,
        String providerToken,
        String paymentKey
) {
    public PaymentConfirmRequest(
            String orderId,
            BigDecimal amount,
            PaymentProviderType provider,
            String providerToken) {
        this(orderId, amount, provider, providerToken, null);
    }
}
