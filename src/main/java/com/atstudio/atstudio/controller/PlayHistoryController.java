package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryDeleteRequest;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryListItemResponse;
import com.atstudio.atstudio.dto.playhistory.PlayHistorySaveRequest;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.PlayHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/play-histories")
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryService playHistoryService;

    @PostMapping
    public ResponseEntity<Void> save(
            @Valid @RequestBody PlayHistorySaveRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playHistoryService.savePlayHistory(request.trackId(), userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PlayHistoryListItemResponse>> getMyHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(playHistoryService.getMyHistory(userDetails, page, size));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @Valid @RequestBody PlayHistoryDeleteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playHistoryService.deleteHistory(request, userDetails);
        return ResponseEntity.noContent().build();
    }
}
