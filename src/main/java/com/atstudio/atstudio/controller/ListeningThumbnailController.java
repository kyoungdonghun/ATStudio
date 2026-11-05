package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.listening.enabled", havingValue = "true")
public class ListeningThumbnailController {
    private final TrackService tracks;

    @GetMapping("/uploads/tracks/thumbnail/{filename}")
    public ResponseEntity<Resource> thumbnail(@PathVariable String filename) {
        Resource resource = tracks.getPublicThumbnail(filename);
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM)).body(resource);
    }
}
