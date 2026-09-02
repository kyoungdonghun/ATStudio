package com.atstudio.atstudio.config;

import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.service.storage.StorageIntegrityService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class StorageIntegrityStartupGuardTest {

    @Test
    void doesNotAuditWhenStartupAuditIsDisabled() {
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        StorageIntegrityService service = mock(StorageIntegrityService.class);
        StorageIntegrityStartupGuard guard = new StorageIntegrityStartupGuard(
                properties, service, new MockEnvironment());

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
        verifyNoInteractions(service);
    }

    @Test
    void strictStartupRefusesMissingReferences() {
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        properties.setAuditOnStartup(true);
        properties.setStrictOnStartup(true);
        StorageIntegrityService service = mock(StorageIntegrityService.class);
        given(service.inspect()).willReturn(report(1));
        StorageIntegrityStartupGuard guard = new StorageIntegrityStartupGuard(
                properties, service, new MockEnvironment());

        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missingReferences=1");
    }

    @Test
    void nonStrictStartupLogsButPermitsMissingReferences() {
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        properties.setAuditOnStartup(true);
        StorageIntegrityService service = mock(StorageIntegrityService.class);
        given(service.inspect()).willReturn(report(1));
        StorageIntegrityStartupGuard guard = new StorageIntegrityStartupGuard(
                properties, service, new MockEnvironment());

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
    }

    @Test
    void productionProfileRequiresStrictStartupAudit() {
        StorageIntegrityProperties properties = new StorageIntegrityProperties();
        StorageIntegrityService service = mock(StorageIntegrityService.class);
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        StorageIntegrityStartupGuard guard = new StorageIntegrityStartupGuard(properties, service, environment);

        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("strict startup integrity audit");
        verifyNoInteractions(service);
    }

    private StorageIntegrityReportResponse report(int missing) {
        return new StorageIntegrityReportResponse(Instant.now(), missing, 0, missing, false, List.of());
    }
}
