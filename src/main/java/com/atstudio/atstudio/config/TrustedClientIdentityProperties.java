package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.trusted-client-identity")
public class TrustedClientIdentityProperties {

    private boolean enabled = false;
    private List<String> trustedProxyAddresses = new ArrayList<>(List.of("127.0.0.1", "::1"));
}
