package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.setting.SettingResponse;
import com.atstudio.atstudio.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SiteSettingService siteSettingService;

    @GetMapping("/{key}")
    public ResponseEntity<ResponseDTO<SettingResponse>> getSetting(
            @PathVariable String key) {
        String value = siteSettingService.getValue(key, "");
        return ResponseEntity.ok(ResponseDTO.<SettingResponse>withSingleData()
                .message("Setting retrieved")
                .data(new SettingResponse(key, value))
                .build());
    }
}
