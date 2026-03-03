package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.entity.key.AlbumTrackId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "album_tracks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AlbumTrack {

    @EmbeddedId
    private AlbumTrackId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("albumId")
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("trackId")
    @JoinColumn(name = "track_id")
    private Track track;

    @Column(nullable = false)
    private int trackOrder;

    public void updateOrder(int trackOrder) {
        this.trackOrder = trackOrder;
    }
}
