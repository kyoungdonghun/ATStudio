package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.dto.album.AlbumTrackItemResponse;
import com.atstudio.atstudio.dto.download.DownloadHistoryItemResponse;
import com.atstudio.atstudio.dto.like.LikeResponse;
import com.atstudio.atstudio.dto.playlist.PlaylistTrackItemResponse;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackDownload;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import com.atstudio.atstudio.entity.key.LikeId;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import com.atstudio.atstudio.service.PlayableTrackService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({JpaConfig.class, PlayableTrackService.class})
class PlayableTrackQueryCountTest {

    private int userSequence;

    @Autowired private AlbumRepository albumRepository;
    @Autowired private AlbumTrackRepository albumTrackRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private PlaylistRepository playlistRepository;
    @Autowired private PlaylistTrackRepository playlistTrackRepository;
    @Autowired private TrackDownloadRepository trackDownloadRepository;
    @Autowired private TrackRepository trackRepository;
    @Autowired private TrackTagRepository trackTagRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayableTrackService playableTrackService;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Test
    void batchHydrationUsesTwoQueriesForOneOrManyTracks() {
        User user = saveUser();
        List<Long> trackIds = saveTracks(user, 12);
        flushAndClear();

        MeasuredResult<Integer> one = measure(() -> playableTrackService
                .hydrate(trackIds.subList(0, 1)).size());
        MeasuredResult<Integer> many = measure(() -> playableTrackService
                .hydrate(trackIds).size());

        assertThat(one.value()).isEqualTo(1);
        assertThat(many.value()).isEqualTo(12);
        assertThat(one.queryCount()).isEqualTo(2);
        assertThat(many.queryCount()).isEqualTo(one.queryCount());
    }

    @Test
    void batchHydrationDeduplicatesAndKeepsRequestedActiveOrder() {
        User user = saveUser();
        List<Track> activeTracks = saveTrackEntities(user, 2);
        Track inactiveTrack = trackRepository.save(Track.builder()
                .title("Inactive")
                .bpm(90)
                .tonality("Am")
                .audioFile("tracks/audio/inactive.mp3")
                .duration(90)
                .waveformData("[0.4]")
                .user(user)
                .isActive(false)
                .build());
        flushAndClear();

        List<Long> requested = List.of(
                activeTracks.get(1).getId(),
                activeTracks.get(0).getId(),
                activeTracks.get(1).getId(),
                inactiveTrack.getId(),
                999_999L);
        var result = playableTrackService.hydrate(requested);

        assertThat(result).extracting(response -> response.id())
                .containsExactly(activeTracks.get(1).getId(), activeTracks.get(0).getId());
        assertThat(result).allSatisfy(response -> {
            assertThat(response.duration()).isPositive();
            assertThat(response.waveformData()).isNotBlank();
        });

        List<Long> oversized = java.util.stream.LongStream.rangeClosed(1, 101).boxed().toList();
        assertThatThrownBy(() -> playableTrackService.hydrate(oversized))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aggregateMappingsUseOneQueryForOneOrManyRows() {
        User artist = saveUser();
        User singleLiker = saveUser("single-like@example.com");
        User manyLiker = saveUser("many-like@example.com");
        Album singleAlbum = albumRepository.save(
                Album.builder().title("Single album").createdBy(artist).build());
        Album manyAlbum = albumRepository.save(
                Album.builder().title("Many album").createdBy(artist).build());
        Playlist singlePlaylist = playlistRepository.save(
                Playlist.builder().title("Single playlist").user(artist).build());
        Playlist manyPlaylist = playlistRepository.save(
                Playlist.builder().title("Many playlist").user(artist).build());
        List<Track> tracks = saveTrackEntities(artist, 12);
        for (int index = 0; index < tracks.size(); index++) {
            Track track = tracks.get(index);
            albumTrackRepository.save(AlbumTrack.builder()
                    .id(new AlbumTrackId(manyAlbum.getId(), track.getId()))
                    .album(manyAlbum)
                    .track(track)
                    .trackOrder(index + 1)
                    .build());
            playlistTrackRepository.save(PlaylistTrack.builder()
                    .id(new PlaylistTrackId(manyPlaylist.getId(), track.getId()))
                    .playlist(manyPlaylist)
                    .track(track)
                    .trackOrder(index + 1)
                    .build());
            likeRepository.save(Like.builder()
                    .id(new LikeId(manyLiker.getId(), track.getId()))
                    .user(manyLiker)
                    .track(track)
                    .build());
            if (index == 0) {
                albumTrackRepository.save(AlbumTrack.builder()
                        .id(new AlbumTrackId(singleAlbum.getId(), track.getId()))
                        .album(singleAlbum)
                        .track(track)
                        .trackOrder(1)
                        .build());
                playlistTrackRepository.save(PlaylistTrack.builder()
                        .id(new PlaylistTrackId(singlePlaylist.getId(), track.getId()))
                        .playlist(singlePlaylist)
                        .track(track)
                        .trackOrder(1)
                        .build());
                likeRepository.save(Like.builder()
                        .id(new LikeId(singleLiker.getId(), track.getId()))
                        .user(singleLiker)
                        .track(track)
                        .build());
            }
        }
        flushAndClear();

        Album singleAlbumReference = entityManager.getReference(Album.class, singleAlbum.getId());
        Album manyAlbumReference = entityManager.getReference(Album.class, manyAlbum.getId());
        User singleLikerReference = entityManager.getReference(User.class, singleLiker.getId());
        User manyLikerReference = entityManager.getReference(User.class, manyLiker.getId());
        MeasuredResult<Integer> singleAlbumRows = measure(() -> albumTrackRepository
                .findAllPlayableByAlbumOrderByTrackOrder(singleAlbumReference).stream()
                .map(AlbumTrackItemResponse::from)
                .toList()
                .size());
        MeasuredResult<Integer> manyAlbumRows = measure(() -> albumTrackRepository
                .findAllPlayableByAlbumOrderByTrackOrder(manyAlbumReference).stream()
                .map(AlbumTrackItemResponse::from)
                .toList()
                .size());
        MeasuredResult<Integer> singlePlaylistRows = measure(() -> playlistTrackRepository
                .findAllPlayableByPlaylistIdOrderByTrackOrderAsc(singlePlaylist.getId()).stream()
                .map(PlaylistTrackItemResponse::from)
                .toList()
                .size());
        MeasuredResult<Integer> manyPlaylistRows = measure(() -> playlistTrackRepository
                .findAllPlayableByPlaylistIdOrderByTrackOrderAsc(manyPlaylist.getId()).stream()
                .map(PlaylistTrackItemResponse::from)
                .toList()
                .size());
        MeasuredResult<Integer> singleLikeRows = measure(() -> likeRepository
                .findAllActiveByUser(singleLikerReference).stream()
                .map(LikeResponse::from)
                .toList()
                .size());
        MeasuredResult<Integer> manyLikeRows = measure(() -> likeRepository
                .findAllActiveByUser(manyLikerReference).stream()
                .map(LikeResponse::from)
                .toList()
                .size());

        assertThat(singleAlbumRows.value()).isEqualTo(1);
        assertThat(manyAlbumRows.value()).isEqualTo(12);
        assertThat(singleAlbumRows.queryCount()).isEqualTo(1);
        assertThat(manyAlbumRows.queryCount()).isEqualTo(singleAlbumRows.queryCount());
        assertThat(singlePlaylistRows.value()).isEqualTo(1);
        assertThat(manyPlaylistRows.value()).isEqualTo(12);
        assertThat(singlePlaylistRows.queryCount()).isEqualTo(1);
        assertThat(manyPlaylistRows.queryCount()).isEqualTo(singlePlaylistRows.queryCount());
        assertThat(singleLikeRows.value()).isEqualTo(1);
        assertThat(manyLikeRows.value()).isEqualTo(12);
        assertThat(singleLikeRows.queryCount()).isEqualTo(1);
        assertThat(manyLikeRows.queryCount()).isEqualTo(singleLikeRows.queryCount());
    }

    @Test
    void downloadHistoryUsesTwoQueriesForOneOrManyRows() {
        User user = saveUser();
        List<Track> tracks = saveTrackEntities(user, 12);
        trackDownloadRepository.save(TrackDownload.builder().user(user).track(tracks.get(0)).build());
        flushAndClear();

        MeasuredResult<Integer> one = measure(() -> mapDownloadHistory(user.getId()));

        User managedUser = entityManager.getReference(User.class, user.getId());
        for (int index = 1; index < tracks.size(); index++) {
            Track managedTrack = entityManager.getReference(Track.class, tracks.get(index).getId());
            trackDownloadRepository.save(
                    TrackDownload.builder().user(managedUser).track(managedTrack).build());
        }
        flushAndClear();

        MeasuredResult<Integer> many = measure(() -> mapDownloadHistory(user.getId()));

        assertThat(one.value()).isEqualTo(1);
        assertThat(many.value()).isEqualTo(12);
        assertThat(one.queryCount()).isEqualTo(2);
        assertThat(many.queryCount()).isEqualTo(one.queryCount());
    }

    private int mapDownloadHistory(Long userId) {
        User user = entityManager.getReference(User.class, userId);
        List<TrackDownload> downloads = trackDownloadRepository
                .findMyDownloadHistory(user, null, PageRequest.of(0, 100))
                .getContent();
        List<Long> trackIds = downloads.stream()
                .map(download -> download.getTrack().getId())
                .distinct()
                .toList();
        trackTagRepository.findAllWithTagByTrackIdIn(trackIds);
        return downloads.stream()
                .map(download -> DownloadHistoryItemResponse.from(download, List.of()))
                .toList()
                .size();
    }

    private User saveUser() {
        return saveUser("playable@example.com");
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .nickname("PlayableArtist" + ++userSequence)
                .build());
    }

    private List<Long> saveTracks(User user, int count) {
        return saveTrackEntities(user, count).stream().map(Track::getId).toList();
    }

    private List<Track> saveTrackEntities(User user, int count) {
        List<Track> tracks = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            tracks.add(trackRepository.save(Track.builder()
                    .title("Track " + index)
                    .bpm(100 + index)
                    .tonality("C")
                    .audioFile("tracks/audio/" + index + ".mp3")
                    .duration(120 + index)
                    .waveformData("[0.1,0.9]")
                    .user(user)
                    .isActive(true)
                    .build()));
        }
        return tracks;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private <T> MeasuredResult<T> measure(Supplier<T> operation) {
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        T value = operation.get();
        return new MeasuredResult<>(value, statistics.getPrepareStatementCount());
    }

    private record MeasuredResult<T>(T value, long queryCount) {
    }
}
