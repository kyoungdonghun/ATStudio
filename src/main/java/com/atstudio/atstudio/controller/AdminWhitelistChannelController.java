package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistChannelResponse;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistChannelStatusRequest;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportRequest;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AdminWhitelistChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/whitelist-channels")
@RequiredArgsConstructor
public class AdminWhitelistChannelController {

    private final AdminWhitelistChannelService adminWhitelistChannelService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminWhitelistChannelResponse>> listChannels(
            @RequestParam(required = false) WhitelistChannelStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminWhitelistChannelService.listChannels(status, keyword, page, size));
    }

    @PutMapping("/{channelId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminWhitelistChannelResponse>> updateStatus(
            @PathVariable Long channelId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminWhitelistChannelStatusRequest request) {
        return ResponseEntity.ok(ResponseDTO.<AdminWhitelistChannelResponse>withSingleData()
                .data(adminWhitelistChannelService.updateStatus(
                        channelId,
                        userDetails,
                        request.status(),
                        request.adminNote()))
                .build());
    }

    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportChannels(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AdminWhitelistExportRequest request) {
        AdminWhitelistExportFile file = adminWhitelistChannelService.exportChannels(userDetails, request);
        return exportResponse(file);
    }

    @GetMapping("/exports/{batchID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadExportBatch(@PathVariable Long batchID) {
        return exportResponse(adminWhitelistChannelService.downloadExportBatch(batchID));
    }

    private ResponseEntity<byte[]> exportResponse(AdminWhitelistExportFile file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .header("X-Whitelist-Export-Batch-Id", String.valueOf(file.batchId()))
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(file.content());
    }
}
