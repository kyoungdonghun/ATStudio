package com.atstudio.atstudio.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.whitelist")
public class WhitelistChannelProperties {

    @Min(1)
    @Max(1000)
    private int maxSavedChannels = 100;
}
