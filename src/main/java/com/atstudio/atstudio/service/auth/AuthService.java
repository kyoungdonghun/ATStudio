package com.atstudio.atstudio.service.auth;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.auth.AuthResponse;
import com.atstudio.atstudio.dto.auth.LoginRequest;
import com.atstudio.atstudio.dto.auth.RefreshRequest;
import com.atstudio.atstudio.dto.auth.SocialAuthResponse;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.JwtTokenProvider;
import com.atstudio.atstudio.security.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2Service oAuth2Service;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Spring Security 인증 (BadCredentialsException 자동 발생)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 2. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getId(), userDetails.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        // 3. Refresh Token DB 저장 (BCrypt 해시)
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        user.updateRefreshToken(passwordEncoder.encode(refreshToken));

        return new AuthResponse(accessToken, refreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiration() / 1000);
    }

    @Transactional
    public SocialAuthResponse socialLogin(SocialProvider provider, String authorizationCode) {
        User user = oAuth2Service.processSocialLogin(provider, authorizationCode);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(passwordEncoder.encode(refreshToken));

        boolean isProfileComplete = user.isProfileComplete();

        return new SocialAuthResponse(accessToken, refreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiration() / 1000, isProfileComplete);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String requestToken = request.getRefreshToken();

        TokenValidationResult result = jwtTokenProvider.validateToken(requestToken);
        if (result == TokenValidationResult.EXPIRED) {
            throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_EXPIRED);
        }
        if (result != TokenValidationResult.VALID) {
            throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        }

        Long userID = jwtTokenProvider.getUserID(requestToken);

        User user = userRepository.findById(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        // SEC-07: DB 불일치 시 refresh token clear
        if (user.getRefreshToken() == null
                || !passwordEncoder.matches(requestToken, user.getRefreshToken())) {
            user.clearRefreshToken();
            throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        }

        // SEC-08: 탈퇴 계정 차단
        if (user.isDeleted()) {
            throw new BusinessException(BUSINESS_ERROR.ACCOUNT_DEACTIVATED);
        }

        // 토큰 재발급 (rotation)
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        user.updateRefreshToken(passwordEncoder.encode(newRefreshToken));

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiration() / 1000);
    }
}
