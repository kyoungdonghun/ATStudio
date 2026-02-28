package com.atstudio.atstudio.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
