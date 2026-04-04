package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.setting.SettingUpdateRequest;
import com.atstudio.atstudio.service.SiteSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class AdminSettingController {

    private final SiteSettingService siteSettingService;

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody SettingUpdateRequest request) {
        siteSettingService.setValue(key, request.getValue());
        return ResponseEntity.ok(ResponseDTO.<Void>withMessage()
                .message("Setting updated")
                .build());
    }
}
