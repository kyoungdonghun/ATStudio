package com.atstudio.atstudio.service.payment.provider.recurring;

public class PaymentProviderOutcomeUnknownException extends RuntimeException {

    public PaymentProviderOutcomeUnknownException(String message) {
        super(message);
    }

    public PaymentProviderOutcomeUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
