package com.atstudio.atstudio.dto.auth;

public record SocialAuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean isProfileComplete
) {}
