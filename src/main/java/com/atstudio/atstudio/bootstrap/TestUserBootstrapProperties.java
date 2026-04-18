package com.atstudio.atstudio.bootstrap;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.bootstrap.test-users")
public class TestUserBootstrapProperties {

    /**
     * Disabled by default. Intended only for local/stage environments.
     */
    private boolean enabled = false;

    /**
     * Shared password for the bootstrap QA accounts.
     */
    private String defaultPassword = "Test1234!";
}
