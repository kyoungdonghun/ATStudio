package com.atstudio.atstudio.service;

import com.atstudio.atstudio.config.ListeningProperties;
import com.atstudio.atstudio.entity.SiteSetting;
import com.atstudio.atstudio.repository.SiteSettingRepository;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListeningReservedSettingTest {
    @Test void databaseAliasProbeAndPersistedIdentityProtectAllGenericOperations() {
        var settings = mock(SiteSettingRepository.class);
        var service = new SiteSettingService(settings);
        when(settings.countReservedAlias("legacy_alias")).thenReturn(1L);
        assertThatThrownBy(() -> service.getValue("legacy_alias")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> service.getValue("legacy_alias", "fallback")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> service.setValue("legacy_alias", "overwrite")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        when(settings.findBySettingKey("ordinary")).thenReturn(java.util.Optional.of(new SiteSetting("listening.catalog.v1", "secret")));
        assertThatThrownBy(() -> service.getValue("ordinary")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        assertThatThrownBy(() -> service.setValue("ordinary", "overwrite")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verify(settings, never()).save(any());
    }

    @Test void sellingAndStaticModesNeverReadOrWriteDatabaseCatalog() {
        var p = new ListeningProperties(); p.setTrackIds(java.util.Set.of(7L));
        var settings = mock(SiteSettingRepository.class);
        var service = new ListeningCatalogService(p, settings, null, null);
        p.setCatalogSource(ListeningProperties.CatalogSource.DATABASE);
        assertThat(service.databaseMode()).isFalse();
        assertThat(service.get().source()).isEqualTo("STATIC");
        p.setEnabled(true); p.setCatalogSource(ListeningProperties.CatalogSource.STATIC);
        assertThat(service.get().trackIds()).containsExactly(7L);
        service.requireUnselected(7L);
        assertThatThrownBy(() -> service.replace(new ListeningCatalogService.Update(0, java.util.List.of(), "reason", true), 1L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verifyNoInteractions(settings);
    }
}
