package com.atstudio.atstudio.dto.setting;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingUpdateRequest {

    @NotNull
    private String value;
}
