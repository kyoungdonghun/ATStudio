package com.atstudio.atstudio.service.payment.billing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BillingCustomerKeyGenerator unit tests")
class BillingCustomerKeyGeneratorTest {

    @Test
    @DisplayName("generated key is random-looking and Toss customerKey compatible")
    void generatedKeyIsCompatible() {
        BillingCustomerKeyGenerator generator = new BillingCustomerKeyGenerator();

        String first = generator.generate();
        String second = generator.generate();

        assertThat(first).startsWith("ats_billing_");
        assertThat(first).matches("[A-Za-z0-9\\-_.=@]+");
        assertThat(first).hasSizeLessThanOrEqualTo(300);
        assertThat(first).isNotEqualTo(second);
    }
}
