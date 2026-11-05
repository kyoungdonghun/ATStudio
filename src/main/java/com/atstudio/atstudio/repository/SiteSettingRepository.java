package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, Long> {

    Optional<SiteSetting> findBySettingKey(String settingKey);

    @org.springframework.data.jpa.repository.QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from SiteSetting s where s.settingKey = :key")
    Optional<SiteSetting> findByKeyForUpdate(@org.springframework.data.repository.query.Param("key") String key);

    @org.springframework.data.jpa.repository.QueryHints(@jakarta.persistence.QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_READ)
    @org.springframework.data.jpa.repository.Query("select s from SiteSetting s where s.settingKey = :key")
    Optional<SiteSetting> findByKeyForSnapshot(@org.springframework.data.repository.query.Param("key") String key);

    // Database collation, not Java normalization, decides whether the requested key aliases a reserved row.
    @org.springframework.data.jpa.repository.Query("select count(s) from SiteSetting s where s.settingKey = :key "
            + "and s.settingKey like 'listening.%'")
    long countReservedAlias(@org.springframework.data.repository.query.Param("key") String key);
}
