package com.atstudio.atstudio.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderReceiptUrlPolicyTest {

    @Test
    void normalizesValidAbsoluteHttpsUrl() {
        assertThat(ProviderReceiptUrlPolicy.normalizeOrNull(
                "  https://receipts.example.com/a/../receipt/1  "))
                .isEqualTo("https://receipts.example.com/receipt/1");
    }

    @Test
    void acceptsExplicitHttpsPort443() {
        assertThat(ProviderReceiptUrlPolicy.normalizeOrNull(
                "https://receipts.example.com:443/receipt/1"))
                .isEqualTo("https://receipts.example.com:443/receipt/1");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "data:text/html,test",
            "file:///tmp/receipt",
            "ftp://receipts.example.com/r/1",
            "//receipts.example.com/r/1",
            "https://user:password@receipts.example.com/r/1",
            "https://receipts.example.com:8443/r/1",
            "https://",
            "not a url"
    })
    void rejectsUnsafeOrMalformedUrl(String value) {
        assertThat(ProviderReceiptUrlPolicy.normalizeOrNull(value)).isNull();
    }
}
