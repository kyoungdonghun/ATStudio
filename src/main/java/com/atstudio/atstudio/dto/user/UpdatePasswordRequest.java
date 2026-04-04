package com.atstudio.atstudio.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.atstudio.atstudio.common.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = PASSWORD_MIN, max = PASSWORD_MAX)
    private String newPassword;
}
