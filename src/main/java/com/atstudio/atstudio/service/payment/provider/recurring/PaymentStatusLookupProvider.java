package com.atstudio.atstudio.service.payment.provider.recurring;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentStatusLookupProvider {

    PaymentProviderType getProviderType();

    default boolean isLookupConfigured() {
        return true;
    }

    @Transactional(propagation = Propagation.NEVER)
    ProviderPaymentLookupResult findPaymentByOrderId(String orderId);
}
