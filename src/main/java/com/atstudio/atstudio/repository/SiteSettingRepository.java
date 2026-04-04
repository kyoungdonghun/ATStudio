package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {

    Optional<SiteSetting> findBySettingKey(String settingKey);
}
