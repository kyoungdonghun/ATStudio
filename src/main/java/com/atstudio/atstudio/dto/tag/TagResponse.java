package com.atstudio.atstudio.dto.tag;

import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;

import java.time.LocalDateTime;

public record TagResponse(
        Long id,
        String name,
        TagType type,
        LocalDateTime createdAt
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getType(),
                tag.getCreatedAt()
        );
    }
}
