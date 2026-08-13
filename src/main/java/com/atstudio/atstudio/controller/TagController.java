package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.tag.TagCreateRequest;
import com.atstudio.atstudio.dto.tag.TagDeletionImpactResponse;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TagResponse>> createTag(
            @Valid @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.<TagResponse>withSingleData()
                        .message("Tag created")
                        .data(response)
                        .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ResponseDTO<TagResponse>> getAvailableTags(
            @RequestParam(required = false) List<String> genre,
            @RequestParam(required = false) List<String> mood,
            @RequestParam(required = false) List<String> instrument,
            @RequestParam(required = false) List<String> usage,
            @RequestParam(required = false) Integer bpmMin,
            @RequestParam(required = false) Integer bpmMax) {
        List<TagResponse> tags = tagService.getAvailableTags(
                genre, mood, instrument, usage, bpmMin, bpmMax);
        return ResponseEntity.ok(ResponseDTO.<TagResponse>builder()
                .message("Available tags retrieved")
                .dataList(tags)
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<TagResponse>> getAllTags(
            @RequestParam(required = false) TagType type) {
        List<TagResponse> tags = tagService.getAllTags(type);
        return ResponseEntity.ok(ResponseDTO.<TagResponse>builder()
                .message("Tag list retrieved")
                .dataList(tags)
                .build());
    }

    @GetMapping("/{tagId}/deletion-impact")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TagDeletionImpactResponse>> getDeletionImpact(
            @PathVariable Long tagId) {
        return ResponseEntity.ok(ResponseDTO.<TagDeletionImpactResponse>withSingleData()
                .message("Tag deletion impact retrieved")
                .data(tagService.getDeletionImpact(tagId))
                .build());
    }

    @PutMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<TagResponse>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagCreateRequest request) {
        TagResponse response = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(ResponseDTO.<TagResponse>withSingleData()
                .message("Tag updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
