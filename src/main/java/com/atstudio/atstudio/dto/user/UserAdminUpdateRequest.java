package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAdminUpdateRequest {

    private UserRole role;
    private Boolean isVerified;
}
