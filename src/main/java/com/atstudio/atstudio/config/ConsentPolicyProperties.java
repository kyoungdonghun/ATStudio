package com.atstudio.atstudio.config;

import com.atstudio.atstudio.entity.enums.UserConsentType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.consent.policy")
public class ConsentPolicyProperties {

    private String termsOfServiceVersion = "v1";
    private String privacyCollectionAndUseVersion = "v1";
    private String marketingVersion = "v1";

    public String versionFor(UserConsentType consentType) {
        String version = switch (consentType) {
            case TERMS_OF_SERVICE -> termsOfServiceVersion;
            case PRIVACY_COLLECTION_AND_USE -> privacyCollectionAndUseVersion;
            case MARKETING -> marketingVersion;
        };

        if (version == null || version.isBlank()) {
            throw new IllegalStateException("Consent policy version must be configured");
        }
        return version;
    }
}
