package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.storage.StorageIntegrityReportResponse;
import com.atstudio.atstudio.service.storage.StorageIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/storage-integrity")
@RequiredArgsConstructor
public class AdminStorageIntegrityController {

    private final StorageIntegrityService storageIntegrityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<StorageIntegrityReportResponse>> inspect() {
        return ResponseEntity.ok(ResponseDTO.<StorageIntegrityReportResponse>builder()
                .message("Storage integrity inspection completed")
                .data(storageIntegrityService.inspect())
                .build());
    }
}
