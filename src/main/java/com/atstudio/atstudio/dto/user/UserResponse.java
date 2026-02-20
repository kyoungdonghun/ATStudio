package com.atstudio.atstudio.dto.user;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String nickname,
        String email,
        String phonePersonal,
        String phoneCompany,
        String job,
        String userType,
        String role,
        boolean isVerified,
        LocalDateTime createdAt
) {
}
