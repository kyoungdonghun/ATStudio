package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

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

    @GetMapping("/{trackId}")
    public ResponseEntity<ResponseDTO<TrackResponse>> getTrack(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .data(trackService.getTrack(trackId))
                .build());
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
