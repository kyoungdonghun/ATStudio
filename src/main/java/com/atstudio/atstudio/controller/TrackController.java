package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.DownloadService;
import com.atstudio.atstudio.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(trackService.getTracksForAdmin(isActive, keyword, page, size));
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
    public ResponseEntity<ResourceRegion> streamTrack(
            @PathVariable Long trackId,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        TrackService.StreamResource streamResource = trackService.getStreamResource(trackId);
        Resource resource = streamResource.resource();
        long publicLength = streamResource.publicLength();
        MediaType contentType = resolveAudioContentType(resource);

        if (publicLength <= 0) {
            return rangeNotSatisfiable(publicLength);
        }

        if (rangeHeader == null) {
            ResourceRegion region = new ResourceRegion(resource, 0, publicLength);
            return ResponseEntity.ok()
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentLength(publicLength)
                    .contentType(contentType)
                    .body(region);
        }

        try {
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.size() != 1) {
                return rangeNotSatisfiable(publicLength);
            }

            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(publicLength);
            long end = range.getRangeEnd(publicLength);
            if (start < 0 || start >= publicLength || end < start) {
                return rangeNotSatisfiable(publicLength);
            }

            long contentLength = end - start + 1;
            ResourceRegion region = new ResourceRegion(resource, start, contentLength);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE,
                            "bytes " + start + "-" + end + "/" + publicLength)
                    .contentLength(contentLength)
                    .contentType(contentType)
                    .body(region);
        } catch (IllegalArgumentException e) {
            return rangeNotSatisfiable(publicLength);
        }
    }

    private MediaType resolveAudioContentType(Resource resource) {
        String filename = resource.getFilename();
        if (filename != null && filename.toLowerCase().endsWith(".wav")) {
            return MediaType.parseMediaType("audio/wav");
        }
        return MediaType.parseMediaType("audio/mpeg");
    }

    private ResponseEntity<ResourceRegion> rangeNotSatisfiable(long publicLength) {
        return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + Math.max(0, publicLength))
                .build();
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
