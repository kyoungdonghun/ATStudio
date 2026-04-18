package com.atstudio.atstudio.dto.util;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PublicCapabilitiesResponse(
        boolean passwordLoginEnabled,
        MailCapability emailVerification,
        MailCapability passwordReset,
        SocialLoginCapability socialLogin,
        boolean testUsersEnabled
) {

    public record MailCapability(
            boolean enabled,
            String deliveryMode
    ) {
    }

    public record SocialLoginCapability(
            ProviderCapability google,
            ProviderCapability kakao,
            ProviderCapability naver
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ProviderCapability(
            boolean enabled,
            String clientId,
            String redirectUri
    ) {
        public static ProviderCapability disabled() {
            return new ProviderCapability(false, null, null);
        }

        public static ProviderCapability enabled(String clientId, String redirectUri) {
            return new ProviderCapability(true, clientId, redirectUri);
        }
    }
}
