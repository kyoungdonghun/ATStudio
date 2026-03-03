package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, AlbumTrackId> {

    void deleteAllByAlbum(Album album);

    @EntityGraph(attributePaths = {"track", "track.user"})
    List<AlbumTrack> findAllByAlbumOrderByTrackOrder(Album album);

    boolean existsByAlbumAndTrack(Album album, Track track);

    long countByAlbum(Album album);
}
