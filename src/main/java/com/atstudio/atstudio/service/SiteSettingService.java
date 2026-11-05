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

    private void checkKey(String key) {
        ReservedSettingPolicy.requireGenericKey(key);
        if (siteSettingRepository.countReservedAlias(key) != 0) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Reserved setting alias");
        }
    }

    private String checkedValue(SiteSetting setting) {
        checkKey(setting.getSettingKey());
        return setting.getSettingValue();
    }

    @Transactional(readOnly = true)
    public String getValue(String key) {
        checkKey(key);
        return siteSettingRepository.findBySettingKey(key)
                .map(this::checkedValue)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public String getValue(String key, String defaultValue) {
        checkKey(key);
        return siteSettingRepository.findBySettingKey(key)
                .map(this::checkedValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void setValue(String key, String value) {
        checkKey(key);
        SiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElse(null);

        if (setting != null) {
            checkedValue(setting);
            setting.updateValue(value);
        } else {
            siteSettingRepository.save(new SiteSetting(key, value));
        }
    }
}
