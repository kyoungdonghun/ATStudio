package com.atstudio.atstudio.dto.payment;

public record PaymentCheckoutResponse(
        String type,
        String confirmToken,
        String clientKey,
        String customerKey,
        String orderName,
        String successUrl,
        String failUrl
) {
    public PaymentCheckoutResponse(String type, String confirmToken) {
        this(type, confirmToken, null, null, null, null, null);
    }
}
