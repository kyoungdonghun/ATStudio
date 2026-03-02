package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.notice.NoticeCreateRequest;
import com.atstudio.atstudio.dto.notice.NoticeListItemResponse;
import com.atstudio.atstudio.dto.notice.NoticeResponse;
import com.atstudio.atstudio.dto.notice.NoticeUpdateRequest;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NoticeResponse response = noticeService.createNotice(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<NoticeResponse>withSingleData()
                        .message("Notice created")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<NoticeListItemResponse>> getNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(noticeService.getNotices(page, size));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseDTO<NoticeResponse>> getNotice(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData()
                .message("Notice retrieved")
                .data(response)
                .build());
    }

    @PutMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request, userDetails);
        return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData()
                .message("Notice updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        noticeService.deleteNotice(noticeId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
