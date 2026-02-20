package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompleteProfileRequest {

    @NotBlank
    @Size(max = 20)
    private String nickname;

    @NotBlank
    @Size(max = 20)
    private String phonePersonal;

    @Size(max = 20)
    private String phoneCompany;

    @NotNull
    private UserJob job;

    @NotNull
    private UserType userType;
}
