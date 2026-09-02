package com.atstudio.atstudio.config;

import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.service.storage.StorageIntegrityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageIntegrityStartupGuard implements ApplicationRunner, Ordered {

    private final StorageIntegrityProperties properties;
    private final StorageIntegrityService storageIntegrityService;
    private final Environment environment;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (hasProductionProfile() && (!properties.isAuditOnStartup() || !properties.isStrictOnStartup())) {
            throw new IllegalStateException(
                    "Production storage runtime requires a strict startup integrity audit");
        }
        if (!properties.isAuditOnStartup()) {
            return;
        }

        StorageIntegrityReportResponse report = storageIntegrityService.inspect();
        if (report.healthy()) {
            log.info("Storage integrity startup audit passed. checkedReferences={}",
                    report.checkedReferenceCount());
            return;
        }
        if (properties.isStrictOnStartup()) {
            throw new IllegalStateException(
                    "Storage integrity startup audit failed: missingReferences="
                            + report.missingReferenceCount());
        }
        log.warn("Storage integrity startup audit found missing references. checkedReferences={}, missingReferences={}",
                report.checkedReferenceCount(), report.missingReferenceCount());
    }

    private boolean hasProductionProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .map(profile -> profile.toLowerCase(Locale.ROOT))
                .anyMatch(profile -> profile.equals("prod")
                        || profile.equals("production")
                        || profile.startsWith("prod-")
                        || profile.startsWith("production-"));
    }
}
