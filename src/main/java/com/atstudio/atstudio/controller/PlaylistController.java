package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.playlist.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // ── 3.1 POST /api/playlists ──────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<PlaylistResponse>> createPlaylist(
            @Valid @ModelAttribute PlaylistCreateRequest request,
            @RequestPart(required = false) MultipartFile thumbnail,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PlaylistResponse response = playlistService.createPlaylist(request, thumbnail, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<PlaylistResponse>withSingleData()
                        .message("Playlist created")
                        .data(response)
                        .build());
    }

    // ── 3.2 GET /api/playlists ───────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ResponseDTO<PlaylistListItemResponse>> getMyPlaylists(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PlaylistListItemResponse> playlists = playlistService.getMyPlaylists(userDetails);
        return ResponseEntity.ok(ResponseDTO.<PlaylistListItemResponse>builder()
                .message("Playlists retrieved")
                .dataList(playlists)
                .build());
    }

    // ── 3.3 GET /api/playlists/{id} ──────────────────────────────────────────

    @GetMapping("/{playlistId}")
    public ResponseEntity<ResponseDTO<PlaylistDetailResponse>> getPlaylistDetail(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PlaylistDetailResponse response = playlistService.getPlaylistDetail(playlistId, userDetails);
        return ResponseEntity.ok(ResponseDTO.<PlaylistDetailResponse>withSingleData()
                .message("Playlist retrieved")
                .data(response)
                .build());
    }

    // ── 3.4 POST /api/playlists/{id}/tracks ──────────────────────────────────

    @PostMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> addTrack(
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistAddTrackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playlistService.addTrack(playlistId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ── 3.4b POST /api/playlists/{id}/tracks/batch ─────────────────────────

    @PostMapping("/{playlistId}/tracks/batch")
    public ResponseEntity<ResponseDTO<Integer>> addTracksBatch(
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistBatchAddTrackRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        int added = playlistService.addTracksBatch(playlistId, request, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<Integer>withSingleData()
                        .message(added + " tracks added")
                        .data(added)
                        .build());
    }

    // ── 3.5 PUT /api/playlists/{id} ──────────────────────────────────────────

    @PutMapping(value = "/{playlistId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<PlaylistResponse>> updatePlaylist(
            @PathVariable Long playlistId,
            @Valid @ModelAttribute PlaylistUpdateRequest request,
            @RequestPart(required = false) MultipartFile thumbnail,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PlaylistResponse response = playlistService.updatePlaylist(
                playlistId, request, thumbnail, userDetails);
        return ResponseEntity.ok(ResponseDTO.<PlaylistResponse>withSingleData()
                .message("Playlist updated")
                .data(response)
                .build());
    }

    // ── 3.6 PUT /api/playlists/{id}/tracks ───────────────────────────────────

    @PutMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> reorderTracks(
            @PathVariable Long playlistId,
            @Valid @RequestBody PlaylistReorderRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playlistService.reorderTracks(playlistId, request, userDetails);
        return ResponseEntity.ok().build();
    }

    // ── 3.7 DELETE /api/playlists/{id}/tracks/{trackId} ──────────────────────

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<Void> removeTrack(
            @PathVariable Long playlistId,
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playlistService.removeTrack(playlistId, trackId, userDetails);
        return ResponseEntity.noContent().build();
    }

    // ── 3.8 DELETE /api/playlists/{id} ───────────────────────────────────────

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable Long playlistId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        playlistService.deletePlaylist(playlistId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
