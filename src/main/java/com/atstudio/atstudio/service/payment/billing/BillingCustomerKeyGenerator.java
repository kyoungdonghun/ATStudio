package com.atstudio.atstudio.service.payment.billing;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class BillingCustomerKeyGenerator {

    private static final String PREFIX = "ats_billing_";
    private static final int RANDOM_BYTES = 24;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] random = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(random);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }
}
