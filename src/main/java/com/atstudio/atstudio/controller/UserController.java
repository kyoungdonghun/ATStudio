package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.user.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResponseDTO<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<UserResponse>withSingleData()
                        .message("Registration successful")
                        .data(response)
                        .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<UserResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ResponseDTO.<UserResponse>withSingleData()
                .data(userService.getMyProfile(userDetails.getId()))
                .build());
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseDTO<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ResponseDTO.<UserResponse>withSingleData()
                .data(userService.updateMyProfile(userDetails.getId(), request))
                .build());
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WithdrawRequest request) {
        userService.withdraw(userDetails.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/complete-profile")
    public ResponseEntity<ResponseDTO<UserResponse>> completeProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CompleteProfileRequest request) {
        return ResponseEntity.ok(ResponseDTO.<UserResponse>withSingleData()
                .data(userService.completeProfile(userDetails.getId(), request))
                .build());
    }
}
