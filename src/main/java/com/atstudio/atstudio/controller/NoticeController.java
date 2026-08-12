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
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<NoticeResponse>> createNotice(
            @Valid @ModelAttribute NoticeCreateRequest request,
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return ResponseEntity.ok(noticeService.getNotices(page, size, sort));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseDTO<NoticeResponse>> getNotice(@PathVariable Long noticeId) {
        NoticeResponse response = noticeService.getNotice(noticeId);
        return ResponseEntity.ok(ResponseDTO.<NoticeResponse>withSingleData()
                .message("Notice retrieved")
                .data(response)
                .build());
    }

    @PutMapping(value = "/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<NoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @ModelAttribute NoticeUpdateRequest request,
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

    // ── Attachment download (public — notice attachments are public) ──────

    @GetMapping("/{noticeId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long noticeId,
            @PathVariable Long attachmentId) {
        Resource resource = noticeService.downloadAttachment(noticeId, attachmentId);
        String filename = UriUtils.encode(
                resource.getFilename() != null ? resource.getFilename() : "attachment",
                StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header("Cross-Origin-Resource-Policy", "same-origin")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
