package com.atstudio.atstudio.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

public record AcceptancePublicUrls(String baseUrl, String publicHost) {

    private static final String INVALID_BASE_URL_MESSAGE =
            "Acceptance startup refused: APP_PUBLIC_BASE_URL must be an absolute HTTPS origin without a trailing slash, user info, query, or fragment.";

    public static AcceptancePublicUrls from(String value) {
        if (value == null || value.isBlank() || !value.equals(value.trim())) {
            throw new IllegalStateException(INVALID_BASE_URL_MESSAGE);
        }

        URI uri;
        try {
            uri = new URI(value);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(INVALID_BASE_URL_MESSAGE, e);
        }

        if (!uri.isAbsolute()
                || !"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getHost().isBlank()
                || uri.getRawUserInfo() != null
                || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            throw new IllegalStateException(INVALID_BASE_URL_MESSAGE);
        }

        return new AcceptancePublicUrls(value, uri.getHost().toLowerCase(Locale.ROOT));
    }

    public String mailBaseUrl() {
        return baseUrl;
    }

    public String googleRedirectUri() {
        return resolve("/social-login/google");
    }

    public String kakaoRedirectUri() {
        return resolve("/social-login/kakao");
    }

    public String naverRedirectUri() {
        return resolve("/social-login/naver");
    }

    public String tossBillingAuthSuccessUrl() {
        return resolve("/subscriptions/checkout/success");
    }

    public String tossBillingAuthFailUrl() {
        return resolve("/subscriptions/checkout/fail");
    }

    private String resolve(String path) {
        return baseUrl + path;
    }
}
