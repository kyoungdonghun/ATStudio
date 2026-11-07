package com.atstudio.atstudio.support;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.service.ListeningCatalogService;
import com.atstudio.atstudio.service.AdminOperationAuditService;
import com.atstudio.atstudio.service.storage.StorageContinuityGuard;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Explicit ordinary-mode dependencies for slices; listening/continuity suites use their real configured guards. */
@TestConfiguration(proxyBeanMethods = false)
@Import({ListeningCatalogService.class, AdminOperationAuditService.class})
public class OrdinaryModeTestConfiguration {
    @Bean ListeningProperties listeningProperties() { return new ListeningProperties(); }
    @Bean StorageContinuityGuard storageContinuityGuard() { return StorageContinuityGuard.disabled(); }
}
