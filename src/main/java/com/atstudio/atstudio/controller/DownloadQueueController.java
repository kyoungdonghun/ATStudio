package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.downloadqueue.DownloadQueueResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.DownloadQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/download-queue")
@RequiredArgsConstructor
public class DownloadQueueController {

    private final DownloadQueueService downloadQueueService;

    @PostMapping("/{trackId}")
    public ResponseEntity<ResponseDTO<Void>> addToQueue(
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        downloadQueueService.addToQueue(trackId, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<Void>withMessage()
                        .message("Added to download queue")
                        .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<DownloadQueueResponse>> getMyQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<DownloadQueueResponse> queue = downloadQueueService.getMyQueue(userDetails);
        return ResponseEntity.ok(ResponseDTO.<DownloadQueueResponse>withAll()
                .dataList(queue)
                .build());
    }

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> removeFromQueue(
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        downloadQueueService.removeFromQueue(trackId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
