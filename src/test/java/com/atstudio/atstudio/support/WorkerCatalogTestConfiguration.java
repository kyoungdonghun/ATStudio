package com.atstudio.atstudio.support;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.mockito.Mockito.mock;

/** No persistence is exercised by ordinary-mode worker configuration tests. */
@TestConfiguration(proxyBeanMethods = false)
@Import({ListeningCatalogService.class, AdminOperationAuditService.class})
public class WorkerCatalogTestConfiguration {
    @Bean ListeningProperties listeningProperties() { return new ListeningProperties(); }
    @Bean SiteSettingRepository settings() { return mock(SiteSettingRepository.class); }
    @Bean TrackRepository tracks() { return mock(TrackRepository.class); }
    @Bean AdminOperationAuditLogRepository auditLogs() { return mock(AdminOperationAuditLogRepository.class); }
}
