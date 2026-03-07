package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final DownloadService downloadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<TrackResponse>> createTrack(
            @Valid @ModelAttribute TrackCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TrackResponse response = trackService.createTrack(request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<TrackResponse>withSingleData()
                        .message("Track created")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<TrackListItemResponse>> getTracks(
            @ModelAttribute TrackSearchRequest request) {
        return ResponseEntity.ok(trackService.getTracks(request));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminTrackListItemResponse>> getTracksForAdmin(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.getTracksForAdmin(isActive, page, size));
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<ResponseDTO<TrackResponse>> getTrack(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .data(trackService.getTrack(trackId))
                .build());
    }

    @GetMapping("/{trackId}/download")
    public ResponseEntity<Resource> downloadTrack(
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Resource resource = downloadService.download(trackId, userDetails);
        String filename = resource.getFilename() != null ? resource.getFilename() : "track.mp3";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/{trackId}/stream")
    public ResponseEntity<Resource> streamTrack(@PathVariable Long trackId) {
        Resource resource = trackService.getStreamResource(trackId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }

    @PutMapping(value = "/{trackId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<TrackResponse>> updateTrack(
            @PathVariable Long trackId,
            @Valid @ModelAttribute TrackUpdateRequest request) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .message("Track updated")
                .data(trackService.updateTrack(trackId, request))
                .build());
    }

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long trackId) {
        trackService.deleteTrack(trackId);
        return ResponseEntity.noContent().build();
    }
}
