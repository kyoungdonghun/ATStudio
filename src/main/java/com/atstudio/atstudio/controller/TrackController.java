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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final DownloadService downloadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TrackResponse>> createTrack(
            @Valid @ModelAttribute TrackCreateRequest request,
            @RequestPart MultipartFile audioFile,
            @RequestPart(required = false) MultipartFile thumbnail,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TrackResponse response = trackService.createTrack(request, audioFile, thumbnail, userDetails);
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

    @GetMapping("/admin/{trackId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TrackResponse>> getTrackForAdmin(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .message("Track retrieved")
                .data(trackService.getTrackForAdmin(trackId))
                .build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<AdminTrackListItemResponse>> getTracksForAdmin(
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.getTracksForAdmin(isActive, page, size));
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<ResponseDTO<TrackResponse>> getTrack(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .message("Track retrieved")
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
    public ResponseEntity<org.springframework.core.io.support.ResourceRegion> streamTrack(
            @PathVariable Long trackId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        Resource resource = trackService.getStreamResource(trackId);
        try {
            long fileLength = resource.contentLength();
            String contentType = "audio/mpeg";
            String filename = resource.getFilename();
            if (filename != null && filename.toLowerCase().endsWith(".wav")) {
                contentType = "audio/wav";
            }

            long start = 0;
            long end = fileLength - 1;
            long chunkSize = Math.min(1024 * 1024, fileLength); // 1MB default chunk

            if (rangeHeader != null) {
                String range = rangeHeader.replace("bytes=", "");
                String[] parts = range.split("-");
                start = Long.parseLong(parts[0]);
                end = (parts.length > 1 && !parts[1].isEmpty())
                        ? Long.parseLong(parts[1])
                        : Math.min(start + chunkSize - 1, fileLength - 1);
                if (end >= fileLength) end = fileLength - 1;
            }

            long contentLength = end - start + 1;
            var region = new org.springframework.core.io.support.ResourceRegion(resource, start, contentLength);

            if (rangeHeader == null) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(region);
            }

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(region);
        } catch (java.io.IOException e) {
            throw new com.atstudio.atstudio.common.exception.BusinessException(
                    com.atstudio.atstudio.common.exception.BUSINESS_ERROR.TRACK_NOT_FOUND);
        }
    }

    @PutMapping(value = "/{trackId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TrackResponse>> updateTrack(
            @PathVariable Long trackId,
            @Valid @ModelAttribute TrackUpdateRequest request,
            @RequestPart(required = false) MultipartFile audioFile,
            @RequestPart(required = false) MultipartFile thumbnail) {
        return ResponseEntity.ok(ResponseDTO.<TrackResponse>withSingleData()
                .message("Track updated")
                .data(trackService.updateTrack(trackId, request, audioFile, thumbnail))
                .build());
    }

    @DeleteMapping("/{trackId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long trackId) {
        trackService.deleteTrack(trackId);
        return ResponseEntity.noContent().build();
    }
}
