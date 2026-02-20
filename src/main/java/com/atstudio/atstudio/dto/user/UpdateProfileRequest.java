package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserJob;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 20)
    private String nickname;

    @Size(max = 20)
    private String phonePersonal;

    @Size(max = 20)
    private String phoneCompany;

    private UserJob job;
}
