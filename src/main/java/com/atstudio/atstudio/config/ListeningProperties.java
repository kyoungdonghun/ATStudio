package com.atstudio.atstudio.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/** Explicit opt-in catalog. An empty selection publishes no tracks. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.listening")
public class ListeningProperties {
    private boolean enabled;
    /** Explicit opt-in for credential-authenticated clients on changing networks. */
    private OperatorAccess operatorAccess = OperatorAccess.SOURCE_RESTRICTED;
    public enum OperatorAccess { SOURCE_RESTRICTED, AUTHENTICATED_ADMIN }
    private CatalogSource catalogSource = CatalogSource.STATIC;
    public enum CatalogSource { STATIC, DATABASE }
    private Set<Long> trackIds = new LinkedHashSet<>();
    private Set<String> operatorAddresses = new LinkedHashSet<>();

    public boolean includes(Long id) {
        return !enabled || trackIds.contains(id);
    }
}
