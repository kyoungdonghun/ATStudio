package com.atstudio.atstudio.dto.tag;

import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.enums.TagType;

public record TagDeletionImpactResponse(
        Long id,
        String name,
        TagType type,
        long trackAssociationCount
) {
    public static TagDeletionImpactResponse from(Tag tag, long trackAssociationCount) {
        return new TagDeletionImpactResponse(
                tag.getId(),
                tag.getName(),
                tag.getType(),
                trackAssociationCount
        );
    }
}
