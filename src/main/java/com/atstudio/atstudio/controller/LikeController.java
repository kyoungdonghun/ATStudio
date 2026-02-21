package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.like.LikeResponse;
import com.atstudio.atstudio.security.CustomUserDetails;
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
}
