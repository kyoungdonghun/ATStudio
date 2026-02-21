package com.atstudio.atstudio.dto.user;

import com.atstudio.atstudio.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDetailResponse(
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
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getPhonePersonal(),
                user.getPhoneCompany(),
                user.getJob() != null ? user.getJob().name() : null,
                user.getUserType().name(),
                user.getRole().name(),
                user.isVerified(),
                user.getCreatedAt()
        );
    }
}
