package com.atstudio.atstudio.service.payment.provider.refund;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;

public interface PaymentRefundProvider {

    PaymentProviderType getProviderType();

    PaymentRefundProviderResult cancelPayment(PaymentRefundProviderCommand command);
}
