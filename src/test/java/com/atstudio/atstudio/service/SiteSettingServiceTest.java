package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.SiteSetting;
import com.atstudio.atstudio.repository.SiteSettingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteSettingService tests")
class SiteSettingServiceTest {

    private static final String KEY = "COMPANY_CERT_GUIDE";

    @Mock
    SiteSettingRepository siteSettingRepository;

    @InjectMocks
    SiteSettingService siteSettingService;

    @Test
    @DisplayName("setValue updates an existing setting without inserting another row")
    void setValue_updatesExistingSetting() {
        SiteSetting existing = new SiteSetting(KEY, "Old guide");
        given(siteSettingRepository.findBySettingKey(KEY)).willReturn(Optional.of(existing));

        siteSettingService.setValue(KEY, "Canonical guide");

        assertThat(existing.getSettingValue()).isEqualTo("Canonical guide");
        verify(siteSettingRepository, never()).save(existing);
    }

    @Test
    @DisplayName("setValue inserts a missing setting with the exact submitted value")
    void setValue_insertsMissingSetting() {
        given(siteSettingRepository.findBySettingKey(KEY)).willReturn(Optional.empty());
        ArgumentCaptor<SiteSetting> settingCaptor = ArgumentCaptor.forClass(SiteSetting.class);

        siteSettingService.setValue(KEY, "Canonical guide");

        verify(siteSettingRepository).save(settingCaptor.capture());
        assertThat(settingCaptor.getValue().getSettingKey()).isEqualTo(KEY);
        assertThat(settingCaptor.getValue().getSettingValue()).isEqualTo("Canonical guide");
    }

    @Test
    @DisplayName("getValue returns the supplied default only when the setting is absent")
    void getValue_returnsDefaultForMissingSetting() {
        given(siteSettingRepository.findBySettingKey(KEY)).willReturn(Optional.empty());

        assertThat(siteSettingService.getValue(KEY, "")).isEmpty();
    }
}
