package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.util.CheckResponse;
import com.atstudio.atstudio.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utils")
@RequiredArgsConstructor
public class UtilController {

    private final UserService userService;

    @GetMapping("/check-email")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isEmailAvailable(email)))
                .build());
    }

    @GetMapping("/check-phone")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkPhone(
            @RequestParam String phone) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isPhoneAvailable(phone)))
                .build());
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<ResponseDTO<CheckResponse>> checkNickname(
            @RequestParam String nickname) {
        return ResponseEntity.ok(ResponseDTO.<CheckResponse>withSingleData()
                .data(new CheckResponse(userService.isNicknameAvailable(nickname)))
                .build());
    }
}
