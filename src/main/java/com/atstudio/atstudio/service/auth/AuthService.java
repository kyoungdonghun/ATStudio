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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final OAuth2Service oAuth2Service;
    private final PasswordLoginPolicy passwordLoginPolicy;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        passwordLoginPolicy.ensureEnabled();

        // 1. Spring Security 인증 (BadCredentialsException 자동 발생)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        ensureVerifiedForPasswordSession(user);

        // 2. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails.getId(), userDetails.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails.getId());

        // 3. Refresh Token DB 저장 (SHA-256 해시 — BCrypt 72바이트 제한 회피)
        user.updateRefreshToken(sha256(refreshToken));

        return new AuthResponse(accessToken, refreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiration() / 1000);
    }

    @Transactional
    public SocialAuthResponse socialLogin(SocialProvider provider, String authorizationCode, String codeVerifier) {
        User user = oAuth2Service.processSocialLogin(provider, authorizationCode, codeVerifier);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(sha256(refreshToken));

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

        User user = userRepository.findByIdForUpdate(userID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        // A stale token must not revoke a newer single-session refresh capability.
        if (user.getRefreshToken() == null
                || !sha256(requestToken).equals(user.getRefreshToken())) {
            throw new BusinessException(BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        }

        // SEC-08: 탈퇴 계정 차단
        if (user.isDeleted()) {
            throw new BusinessException(BUSINESS_ERROR.ACCOUNT_DEACTIVATED);
        }
        ensureVerifiedForPasswordSession(user);

        // 토큰 재발급 (rotation)
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        user.updateRefreshToken(sha256(newRefreshToken));

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer",
                jwtTokenProvider.getAccessTokenExpiration() / 1000);
    }

    @Transactional
    public void logout(Long userID) {
        userRepository.findByIdForUpdate(userID)
                .ifPresent(User::clearRefreshToken);
    }

    private void ensureVerifiedForPasswordSession(User user) {
        if (!user.isVerified()) {
            throw new BusinessException(BUSINESS_ERROR.EMAIL_VERIFICATION_REQUIRED);
        }
    }

    /** SHA-256 해시 (refresh token용 — BCrypt 72바이트 제한 회피) */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
