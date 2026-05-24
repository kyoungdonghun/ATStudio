package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

public interface PaymentStatusLookupProvider {

    PaymentProviderType getProviderType();

    default boolean isLookupConfigured() {
        return true;
    }

    ProviderPaymentLookupResult findPaymentByOrderId(String orderId);
}
