package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.SiteSetting;
import com.atstudio.atstudio.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    @Transactional(readOnly = true)
    public String getValue(String key) {
        return siteSettingRepository.findBySettingKey(key)
                .map(SiteSetting::getSettingValue)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        return siteSettingRepository.findBySettingKey(key)
                .map(SiteSetting::getSettingValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void setValue(String key, String value) {
        SiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElse(null);

        if (setting != null) {
            setting.updateValue(value);
        } else {
            siteSettingRepository.save(new SiteSetting(key, value));
        }
    }
}
