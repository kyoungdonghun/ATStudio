package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.download.DownloadHistoryItemResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.DownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SR-79 — 다운로드 기록 (download history) endpoints.
 * <p>
 * Distinct from {@link DownloadQueueController} which handles the legacy
 * "cart"-style download queue. This controller reads from the
 * {@code track_downloads} log table and exposes paginated, searchable history
 * plus a helper for "전체 재다운로드".
 */
@RestController
@RequestMapping("/api/downloads")
@RequiredArgsConstructor
public class DownloadController {

    private final DownloadService downloadService;

    /** Paginated + searchable + sortable download history for the current user. */
    @GetMapping("/history")
    public ResponseEntity<ResponseDTO<DownloadHistoryItemResponse>> getMyDownloadHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                downloadService.getMyDownloadHistory(userDetails, keyword, sort, page, size)
        );
    }

    /** Distinct track IDs matching the current filter — for "전체 재다운로드". */
    @GetMapping("/history/track-ids")
    public ResponseEntity<ResponseDTO<Long>> getMyDownloadHistoryTrackIds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword
    ) {
        List<Long> ids = downloadService.getMyDownloadHistoryTrackIds(userDetails, keyword);
        return ResponseEntity.ok(ResponseDTO.<Long>builder()
                .message("Download history track ids retrieved")
                .dataList(ids)
                .build());
    }
}
