package com.atstudio.atstudio.dto.payment;

public record PaymentCheckoutResponse(
        String type,
        String confirmToken
) {}
