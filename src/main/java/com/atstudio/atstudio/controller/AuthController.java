package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.auth.*;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ResponseDTO.<AuthResponse>withSingleData()
                .message("Login successful")
                .data(authService.login(request))
                .build());
    }

    @PostMapping("/social/{provider}")
    public ResponseEntity<ResponseDTO<SocialAuthResponse>> socialLogin(
            @PathVariable SocialProvider provider,
            @Valid @RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(ResponseDTO.<SocialAuthResponse>withSingleData()
                .message("Social login successful")
                .data(authService.socialLogin(provider, request.getAuthorizationCode()))
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ResponseDTO.<AuthResponse>withSingleData()
                .message("Token refreshed")
                .data(authService.refresh(request))
                .build());
    }
}
