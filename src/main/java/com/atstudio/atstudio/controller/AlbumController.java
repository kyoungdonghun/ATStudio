package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.album.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AlbumService;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // -- 15.1 POST /api/albums ------------------------------------------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<AlbumResponse>> createAlbum(
            @Valid @ModelAttribute AlbumCreateRequest request,
            @RequestPart(required = false) MultipartFile thumbnailFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AlbumResponse response = albumService.createAlbum(request, thumbnailFile, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<AlbumResponse>withSingleData()
                        .message("Album created")
                        .data(response)
                        .build());
    }

    // -- 15.2 GET /api/albums -------------------------------------------------

    @GetMapping
    public ResponseEntity<ResponseDTO<AlbumListItemResponse>> getAlbums() {
        List<AlbumListItemResponse> albums = albumService.getAlbums();
        return ResponseEntity.ok(ResponseDTO.<AlbumListItemResponse>builder()
                .message("Albums retrieved")
                .dataList(albums)
                .build());
    }

    // -- 15.3 GET /api/albums/{id} --------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<AlbumDetailResponse>> getAlbum(
            @PathVariable Long id) {
        AlbumDetailResponse response = albumService.getAlbum(id);
        return ResponseEntity.ok(ResponseDTO.<AlbumDetailResponse>withSingleData()
                .message("Album retrieved")
                .data(response)
                .build());
    }

    // -- 15.4 PUT /api/albums/{id} --------------------------------------------

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDTO<AlbumResponse>> updateAlbum(
            @PathVariable Long id,
            @Valid @ModelAttribute AlbumUpdateRequest request,
            @RequestPart(required = false) MultipartFile thumbnailFile) {
        AlbumResponse response = albumService.updateAlbum(id, request, thumbnailFile);
        return ResponseEntity.ok(ResponseDTO.<AlbumResponse>withSingleData()
                .message("Album updated")
                .data(response)
                .build());
    }

    // -- 15.5 DELETE /api/albums/{id} -----------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    // -- 15.6 POST /api/albums/{id}/tracks ------------------------------------

    @PostMapping("/{id}/tracks")
    public ResponseEntity<ResponseDTO<AlbumDetailResponse>> addTrack(
            @PathVariable Long id,
            @Valid @RequestBody AlbumTrackAddRequest request) {
        AlbumDetailResponse response = albumService.addTrack(id, request);
        return ResponseEntity.ok(ResponseDTO.<AlbumDetailResponse>withSingleData()
                .message("Track added to album")
                .data(response)
                .build());
    }

    // -- 15.7 DELETE /api/albums/{id}/tracks/{trackId} ------------------------

    @DeleteMapping("/{id}/tracks/{trackId}")
    public ResponseEntity<Void> removeTrack(
            @PathVariable Long id,
            @PathVariable Long trackId) {
        albumService.removeTrack(id, trackId);
        return ResponseEntity.noContent().build();
    }

    // -- 15.8 PUT /api/albums/{id}/tracks -------------------------------------

    @PutMapping("/{id}/tracks")
    public ResponseEntity<ResponseDTO<AlbumDetailResponse>> reorderTracks(
            @PathVariable Long id,
            @Valid @RequestBody AlbumTrackOrderRequest request) {
        AlbumDetailResponse response = albumService.reorderTracks(id, request);
        return ResponseEntity.ok(ResponseDTO.<AlbumDetailResponse>withSingleData()
                .message("Album tracks reordered")
                .data(response)
                .build());
    }
}
