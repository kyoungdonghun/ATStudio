package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserJob;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(min = NICKNAME_MIN, max = NICKNAME_MAX)
    @Pattern(regexp = NICKNAME_PATTERN)
    private String nickname;

    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phonePersonal;

    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phoneCompany;

    private UserJob job;
}
