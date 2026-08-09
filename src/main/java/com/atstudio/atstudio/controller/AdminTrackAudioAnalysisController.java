package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.track.AdminTrackAudioAnalysisDryRunItemResponse;
import com.atstudio.atstudio.service.AdminTrackAudioAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tracks/audio-analysis")
@RequiredArgsConstructor
public class AdminTrackAudioAnalysisController {

    private final AdminTrackAudioAnalysisService audioAnalysisService;

    @GetMapping("/dry-run")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminTrackAudioAnalysisDryRunItemResponse>> dryRun(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(audioAnalysisService.dryRun(page, size));
    }
}
