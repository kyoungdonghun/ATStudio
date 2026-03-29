package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.like.AlbumLikeResponse;
import com.atstudio.atstudio.dto.like.LikeResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.AlbumLikeService;
import com.atstudio.atstudio.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final AlbumLikeService albumLikeService;

    // ── Track Likes ──────────────────────────────────────────────────────────

    @PostMapping("/{trackId}")
    public ResponseEntity<ResponseDTO<Void>> addLike(
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        likeService.addLike(trackId, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<Void>withMessage()
                        .message("Like added")
                        .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<LikeResponse>> getMyLikes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<LikeResponse> likes = likeService.getMyLikes(userDetails);
        return ResponseEntity.ok(ResponseDTO.<LikeResponse>withAll()
                .message("Likes retrieved")
                .dataList(likes)
                .build());
    }

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable Long trackId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        likeService.removeLike(trackId, userDetails);
        return ResponseEntity.noContent().build();
    }

    // ── Album Likes ──────────────────────────────────────────────────────────

    @PostMapping("/albums/{albumId}")
    public ResponseEntity<ResponseDTO<Void>> addAlbumLike(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        albumLikeService.addAlbumLike(albumId, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<Void>withMessage()
                        .message("Album like added")
                        .build());
    }

    @GetMapping("/albums")
    public ResponseEntity<ResponseDTO<AlbumLikeResponse>> getMyAlbumLikes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AlbumLikeResponse> likes = albumLikeService.getMyAlbumLikes(userDetails);
        return ResponseEntity.ok(ResponseDTO.<AlbumLikeResponse>withAll()
                .message("Album likes retrieved")
                .dataList(likes)
                .build());
    }

    @DeleteMapping("/albums/{albumId}")
    public ResponseEntity<Void> removeAlbumLike(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        albumLikeService.removeAlbumLike(albumId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
