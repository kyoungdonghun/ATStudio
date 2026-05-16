package com.atstudio.atstudio.service.payment.provider;

import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    PaymentProviderPrepareResult prepare(PaymentOrder order);

    PaymentProviderConfirmResult confirm(PaymentOrder order, PaymentConfirmRequest request);
}
