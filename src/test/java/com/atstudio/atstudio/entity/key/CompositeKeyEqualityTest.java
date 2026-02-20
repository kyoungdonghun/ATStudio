package com.atstudio.atstudio.entity.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("복합 키 equals/hashCode 검증")
class CompositeKeyEqualityTest {

    @Test
    @DisplayName("TrackTagId: 동일 값이면 equal")
    void trackTagId_sameValues_areEqual() {
        TrackTagId a = new TrackTagId(1L, 2L);
        TrackTagId b = new TrackTagId(1L, 2L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("TrackTagId: 다른 값이면 not equal")
    void trackTagId_differentValues_areNotEqual() {
        TrackTagId a = new TrackTagId(1L, 2L);
        TrackTagId b = new TrackTagId(1L, 3L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("PlaylistTrackId: 동일 값이면 equal")
    void playlistTrackId_sameValues_areEqual() {
        PlaylistTrackId a = new PlaylistTrackId(10L, 20L);
        PlaylistTrackId b = new PlaylistTrackId(10L, 20L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("LikeId: 동일 값이면 equal")
    void likeId_sameValues_areEqual() {
        LikeId a = new LikeId(5L, 7L);
        LikeId b = new LikeId(5L, 7L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("LikeId: 다른 userId이면 not equal")
    void likeId_differentUserId_areNotEqual() {
        LikeId a = new LikeId(5L, 7L);
        LikeId b = new LikeId(6L, 7L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("DownloadQueueId: 동일 값이면 equal")
    void downloadQueueId_sameValues_areEqual() {
        DownloadQueueId a = new DownloadQueueId(3L, 4L);
        DownloadQueueId b = new DownloadQueueId(3L, 4L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("서로 다른 타입의 복합 키는 not equal")
    void differentKeyTypes_areNotEqual() {
        LikeId likeId = new LikeId(1L, 2L);
        DownloadQueueId downloadQueueId = new DownloadQueueId(1L, 2L);

        assertThat(likeId).isNotEqualTo(downloadQueueId);
    }
}
