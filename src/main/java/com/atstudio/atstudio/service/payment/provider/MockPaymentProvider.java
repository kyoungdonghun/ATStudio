package com.atstudio.atstudio.service.payment.provider;

import com.atstudio.atstudio.dto.payment.PaymentConfirmRequest;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.MOCK;
    }

    @Override
    public PaymentProviderPrepareResult prepare(PaymentOrder order) {
        String token = tokenFor(order);
        return new PaymentProviderPrepareResult("MOCK", token, "mockToken=" + token);
    }

    @Override
    public PaymentProviderConfirmResult confirm(PaymentOrder order, PaymentConfirmRequest request) {
        if (!tokenFor(order).equals(request.providerToken())) {
            return PaymentProviderConfirmResult.failure("MOCK_TOKEN_INVALID", "Mock token is invalid.");
        }
        return PaymentProviderConfirmResult.success("MOCK-" + order.getOrderId(), "mockConfirmed=true");
    }

    private String tokenFor(PaymentOrder order) {
        return "mock-" + order.getOrderId();
    }
}
