package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    private String schedulerZone = "Asia/Seoul";
    private Toss toss = new Toss();
    private Billing billing = new Billing();
    private Operations operations = new Operations();

    public ZoneId schedulerZoneId() {
        return ZoneId.of(schedulerZone);
    }

    @Getter
    @Setter
    public static class Toss {
        private String clientKey = "";
        private String secretKey = "";
        private String cancelUrl = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";
        private int connectTimeoutMillis = 3000;
        private int readTimeoutMillis = 10000;
    }

    @Getter
    @Setter
    public static class Billing {
        private String activeKeyId = "";
        private List<EncryptionKey> encryptionKeys = new ArrayList<>();
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
    public static class EncryptionKey {
        private String id = "";
        private String secret = "";
    }

    @Getter
    @Setter
    public static class Operations {
        private boolean reconciliationNotificationEnabled = false;
        private String operatorEmail = "";
        private Reconciliation reconciliation = new Reconciliation();
    }

    @Getter
    @Setter
    public static class Reconciliation {
        private int batchSize = 100;
        private int issueDetailLimit = 100;
        private int completedOrderLookbackDays = 30;
        private int completedOrderMaxPerRun = 500;
    }
}
