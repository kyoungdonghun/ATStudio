package com.atstudio.atstudio.config;

import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private PaymentProviderType provider = PaymentProviderType.MOCK;
    private Toss toss = new Toss();

    @Getter
    @Setter
    public static class Toss {
        private String clientKey = "";
        private String secretKey = "";
        private String successUrl = "http://localhost:5173/subscriptions/payment/success";
        private String failUrl = "http://localhost:5173/subscriptions/payment/fail";
        private String confirmUrl = "https://api.tosspayments.com/v1/payments/confirm";
        private int connectTimeoutMillis = 3000;
        private int readTimeoutMillis = 10000;
    }
}
