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
    private Billing billing = new Billing();
    private Operations operations = new Operations();

    @Getter
    @Setter
    public static class Toss {
        private String clientKey = "";
        private String secretKey = "";
        private String successUrl = "";
        private String failUrl = "";
        private String confirmUrl = "https://api.tosspayments.com/v1/payments/confirm";
        private String cancelUrl = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";
        private int connectTimeoutMillis = 3000;
        private int readTimeoutMillis = 10000;
    }

    @Getter
    @Setter
    public static class Billing {
        private String encryptionSecret = "";
        private String authSuccessUrl = "";
        private String authFailUrl = "";
        private String issueUrl = "https://api.tosspayments.com/v1/billing/authorizations/issue";
        private String chargeUrl = "https://api.tosspayments.com/v1/billing/{billingKey}";
        private String deleteUrl = "https://api.tosspayments.com/v1/billing/{billingKey}";
        private String paymentLookupByOrderIdUrl = "https://api.tosspayments.com/v1/payments/orders/{orderId}";
        private int connectTimeoutMillis = 3000;
        private int readTimeoutMillis = 60000;
    }

    @Getter
    @Setter
    public static class Operations {
        private boolean reconciliationNotificationEnabled = false;
        private String operatorEmail = "";
    }
}
