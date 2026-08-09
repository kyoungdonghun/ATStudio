package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class ActiveMembershipRepositoryTest {

    @Autowired private AlbumRepository albumRepository;
    @Autowired private AlbumTrackRepository albumTrackRepository;
    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private PlaylistTrackRepository playlistTrackRepository;
    @Autowired private TrackRepository trackRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void playlistQueriesCountAndReturnOnlyActiveTracksWhilePreservingAllRows() {
        User user = saveUser("playlist-owner@example.com", "PlaylistOwner");
        Playlist playlist = playlistRepository.save(
                Playlist.builder().title("Mixed Playlist").user(user).build());
        Track activeFirst = saveTrack(user, "Active First", true);
        Track inactive = saveTrack(user, "Inactive", false);
        Track activeLast = saveTrack(user, "Active Last", true);

        savePlaylistTrack(playlist, activeFirst, 0);
        savePlaylistTrack(playlist, inactive, 1);
        savePlaylistTrack(playlist, activeLast, 2);
        flushAndClear();

        List<Object[]> activeCounts = playlistTrackRepository
                .countActiveByPlaylistIdIn(List.of(playlist.getId()));
        List<PlaylistTrack> playable = playlistTrackRepository
                .findAllPlayableByPlaylistIdOrderByTrackOrderAsc(playlist.getId());
        List<PlaylistTrack> allMemberships = playlistTrackRepository
                .findAllByIdPlaylistIdOrderByTrackOrderAsc(playlist.getId());

        assertThat(activeCounts).singleElement().satisfies(row -> {
            assertThat(row[0]).isEqualTo(playlist.getId());
            assertThat(row[1]).isEqualTo(2L);
        });
        assertThat(playable)
                .extracting(membership -> membership.getTrack().getId())
                .containsExactly(activeFirst.getId(), activeLast.getId());
        assertThat(allMemberships)
                .extracting(membership -> membership.getTrack().getId())
                .containsExactly(activeFirst.getId(), inactive.getId(), activeLast.getId());
        assertThat(playlistTrackRepository.count()).isEqualTo(3);
    }

    @Test
    void albumPublicCountsAndTrackCountOrderingUseActiveTracksOnly() {
        User user = saveUser("album-owner@example.com", "AlbumOwner");
        Album mostlyHidden = albumRepository.save(
                Album.builder().title("Mostly Hidden").createdBy(user).build());
        Album twoActive = albumRepository.save(
                Album.builder().title("Two Active").createdBy(user).build());
        Track activeOne = saveTrack(user, "Active One", true);
        Track activeTwo = saveTrack(user, "Active Two", true);
        Track inactiveOne = saveTrack(user, "Inactive One", false);
        Track inactiveTwo = saveTrack(user, "Inactive Two", false);

        saveAlbumTrack(mostlyHidden, inactiveOne, 0);
        saveAlbumTrack(mostlyHidden, inactiveTwo, 1);
        saveAlbumTrack(mostlyHidden, activeOne, 2);
        saveAlbumTrack(twoActive, activeOne, 0);
        saveAlbumTrack(twoActive, activeTwo, 1);
        flushAndClear();

        List<Album> orderedAlbums = albumRepository
                .findAllActiveOrderByTrackCount(PageRequest.of(0, 10))
                .getContent();
        Map<Long, Integer> publicCounts = albumTrackRepository
                .countActiveMapByAlbums(orderedAlbums);
        Map<Long, Integer> allMembershipCounts = albumTrackRepository
                .countMapByAlbums(orderedAlbums);
        Album mostlyHiddenReference = entityManager.getReference(Album.class, mostlyHidden.getId());
        List<AlbumTrack> playable = albumTrackRepository
                .findAllPlayableByAlbumOrderByTrackOrder(mostlyHiddenReference);

        assertThat(orderedAlbums)
                .extracting(Album::getId)
                .containsExactly(twoActive.getId(), mostlyHidden.getId());
        assertThat(publicCounts)
                .containsEntry(twoActive.getId(), 2)
                .containsEntry(mostlyHidden.getId(), 1);
        assertThat(allMembershipCounts)
                .containsEntry(twoActive.getId(), 2)
                .containsEntry(mostlyHidden.getId(), 3);
        assertThat(playable)
                .extracting(membership -> membership.getTrack().getId())
                .containsExactly(activeOne.getId());
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.builder()
                .email(email)
                .nickname(nickname)
                .build());
    }

    private Track saveTrack(User user, String title, boolean active) {
        return trackRepository.save(Track.builder()
                .title(title)
                .bpm(120)
                .tonality("C")
                .audioFile("tracks/audio/" + title.replace(' ', '-') + ".mp3")
                .user(user)
                .isActive(active)
                .build());
    }

    private void savePlaylistTrack(Playlist playlist, Track track, int order) {
        playlistTrackRepository.save(PlaylistTrack.builder()
                .id(new PlaylistTrackId(playlist.getId(), track.getId()))
                .playlist(playlist)
                .track(track)
                .trackOrder(order)
                .build());
    }

    private void saveAlbumTrack(Album album, Track track, int order) {
        albumTrackRepository.save(AlbumTrack.builder()
                .id(new AlbumTrackId(album.getId(), track.getId()))
                .album(album)
                .track(track)
                .trackOrder(order)
                .build());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
