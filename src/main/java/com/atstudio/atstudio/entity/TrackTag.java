package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.key.TrackTagId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "track_tags")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackTag {

    @EmbeddedId
    private TrackTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
