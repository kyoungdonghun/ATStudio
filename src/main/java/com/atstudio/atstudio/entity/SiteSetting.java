package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "site_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiteSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Lob
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    public SiteSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public void updateValue(String settingValue) {
        this.settingValue = settingValue;
    }
}
