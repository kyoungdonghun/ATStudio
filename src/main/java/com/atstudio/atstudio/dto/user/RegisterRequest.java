package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.common.validation.ValidRegisterProfile;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
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
@ValidRegisterProfile
public class RegisterRequest {

    @NotBlank
    @Size(min = NICKNAME_MIN, max = NICKNAME_MAX)
    @Pattern(regexp = NICKNAME_PATTERN)
    private String nickname;

    public void setNickname(String nickname) {
        this.nickname = normalizeNickname(nickname);
    }

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = PASSWORD_MIN, max = PASSWORD_MAX)
    private String password;

    @NotBlank
    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phonePersonal;

    @Size(max = PHONE_MAX)
    @Pattern(regexp = PHONE_PATTERN)
    private String phoneCompany;

    private UserJob job;

    @Size(max = 100)
    private String companyName;

    @NotNull
    private UserType userType;

    @NotNull
    @AssertTrue
    private Boolean termsAgreed;

    @NotNull
    @AssertTrue
    private Boolean privacyAgreed;

    private Boolean marketingAgreed;
}
