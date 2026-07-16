package com.atstudio.atstudio.common.validation;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provider-neutral safety contract for receipt links retained from payment evidence.
 */
public final class ProviderReceiptUrlPolicy {

    private ProviderReceiptUrlPolicy() {
    }

    public static String normalizeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(value.trim()).normalize();
            if (!uri.isAbsolute()
                    || !"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getHost().isBlank()
                    || uri.getRawUserInfo() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)) {
                return null;
            }
            return uri.toASCIIString();
        } catch (URISyntaxException exception) {
            return null;
        }
    }
}
