package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserRole;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAdminUpdateRequest {

    private UserRole role;
    private Boolean isVerified;

    @Size(max = 500)
    private String reason;
}
