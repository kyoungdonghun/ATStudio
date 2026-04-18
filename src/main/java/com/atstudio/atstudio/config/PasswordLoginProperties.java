package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth.password-login")
public class PasswordLoginProperties {

    /**
     * Controls whether local email/password auth flows are available.
     */
    private boolean enabled = true;
}
