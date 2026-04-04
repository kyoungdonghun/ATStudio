package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class CompleteProfileRequest {

    @NotBlank
    @Size(min = NICKNAME_MIN, max = NICKNAME_MAX)
    @Pattern(regexp = NICKNAME_PATTERN)
    private String nickname;

    @NotBlank
    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phonePersonal;

    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phoneCompany;

    @NotNull
    private UserJob job;

    @NotNull
    private UserType userType;
}
