package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, AlbumTrackId> {

    void deleteAllByAlbum(Album album);

    @EntityGraph(attributePaths = {"track", "track.user"})
    List<AlbumTrack> findAllByAlbumOrderByTrackOrder(Album album);

    boolean existsByAlbumAndTrack(Album album, Track track);

    long countByAlbum(Album album);

    @Query("SELECT at.album.id, COUNT(at) FROM AlbumTrack at WHERE at.album IN :albums GROUP BY at.album.id")
    List<Object[]> countByAlbumIn(List<Album> albums);

    default Map<Long, Integer> countMapByAlbums(List<Album> albums) {
        return countByAlbumIn(albums).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    void deleteAllByTrack(Track track);
}
