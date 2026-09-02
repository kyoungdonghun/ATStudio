package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage.integrity")
public class StorageIntegrityProperties {

    private boolean auditOnStartup = false;
    private boolean strictOnStartup = false;
}
