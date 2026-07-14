package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.question.*;
import com.atstudio.atstudio.entity.enums.QuestionCategory;
import com.atstudio.atstudio.entity.enums.QuestionStatus;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    // ── 8.1 POST /api/questions ─────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<QuestionResponse>> createQuestion(
            @Valid @ModelAttribute QuestionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        QuestionResponse response = questionService.createQuestion(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<QuestionResponse>withSingleData()
                        .message("Question created")
                        .data(response)
                        .build());
    }

    // ── 8.2 POST /api/questions/{questionId}/answers ────────────────────────

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<ResponseDTO<AnswerResponse>> createAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AnswerResponse response = questionService.createAnswer(questionId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<AnswerResponse>withSingleData()
                        .message("Answer created")
                        .data(response)
                        .build());
    }

    // ── 8.3 GET /api/questions ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ResponseDTO<QuestionListItemResponse>> getQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) QuestionCategory category,
            @RequestParam(required = false) QuestionStatus status,
            @RequestParam(required = false) Boolean mine,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                questionService.getQuestions(page, size, category, status, mine, userDetails));
    }

    // ── 8.4 GET /api/questions/{questionId} ─────────────────────────────────

    @GetMapping("/{questionId}")
    public ResponseEntity<ResponseDTO<QuestionResponse>> getQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        QuestionResponse response = questionService.getQuestion(questionId, userDetails);
        return ResponseEntity.ok(ResponseDTO.<QuestionResponse>withSingleData()
                .message("Question retrieved")
                .data(response)
                .build());
    }

    // ── 8.5 GET /api/questions/{questionId}/attachments/{attachmentId} ──────

    @GetMapping("/{questionId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long questionId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        QuestionAttachmentDownload download = questionService.downloadAttachment(
                questionId,
                attachmentId,
                userDetails
        );
        String originalFilename = download.originalFilename();
        String filename = UriUtils.encode(
                originalFilename == null || originalFilename.isBlank() ? "attachment" : originalFilename,
                StandardCharsets.UTF_8
        );
        byte[] body = StreamUtils.copyToByteArray(download.resource().getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .header(HttpHeaders.ACCEPT_RANGES, "none")
                .body(body);
    }

    // ── 8.6 PUT /api/questions/{questionId}/status ──────────────────────────

    @PutMapping("/{questionId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<QuestionResponse>> updateQuestionStatus(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionStatusUpdateRequest request) {
        QuestionResponse response = questionService.updateQuestionStatus(questionId, request);
        return ResponseEntity.ok(ResponseDTO.<QuestionResponse>withSingleData()
                .message("Question status updated")
                .data(response)
                .build());
    }

    // ── 8.7 DELETE /api/questions/{questionId} ──────────────────────────────

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        questionService.deleteQuestion(questionId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
