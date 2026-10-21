package com.atstudio.atstudio.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.storage.mutation-admission")
public class StorageMutationAdmissionProperties {

    private boolean enabled = true;
    @Min(1)
    private int maxConcurrent = 2;
    @Min(1)
    private int maxConcurrentPerIp = 1;
    @Min(1)
    private int retryAfterSeconds = 2;
}
