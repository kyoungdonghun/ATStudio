package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelRequest;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.WhitelistChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whitelist-channels")
@RequiredArgsConstructor
public class WhitelistChannelController {

    private final WhitelistChannelService whitelistChannelService;

    // ── 12.1 POST /api/whitelist-channels ────────────────────────────────────

    @PostMapping
    public ResponseEntity<ResponseDTO<WhitelistChannelResponse>> registerChannel(
            @Valid @RequestBody WhitelistChannelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WhitelistChannelResponse response = whitelistChannelService.registerChannel(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<WhitelistChannelResponse>withSingleData()
                        .message("Channel registered")
                        .data(response)
                        .build());
    }

    // ── 12.2 GET /api/whitelist-channels ─────────────────────────────────────

    @GetMapping
    public ResponseEntity<ResponseDTO<WhitelistChannelResponse>> getMyChannels(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WhitelistChannelResponse> channels = whitelistChannelService.getMyChannels(userDetails);
        return ResponseEntity.ok(ResponseDTO.<WhitelistChannelResponse>builder()
                .dataList(channels)
                .message("Channels retrieved")
                .build());
    }

    // ── 12.3 PUT /api/whitelist-channels/{channelId} ─────────────────────────

    @PutMapping("/{channelId}")
    public ResponseEntity<ResponseDTO<WhitelistChannelResponse>> updateChannel(
            @PathVariable Long channelId,
            @Valid @RequestBody WhitelistChannelRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WhitelistChannelResponse response = whitelistChannelService.updateChannel(userDetails, channelId, request);
        return ResponseEntity.ok(ResponseDTO.<WhitelistChannelResponse>withSingleData()
                .message("Channel updated")
                .data(response)
                .build());
    }

    // ── 12.4 DELETE /api/whitelist-channels/{channelId} ──────────────────────

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long channelId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        whitelistChannelService.deleteChannel(userDetails, channelId);
        return ResponseEntity.noContent().build();
    }
}
