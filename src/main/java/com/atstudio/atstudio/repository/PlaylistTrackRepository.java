package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {

    @EntityGraph(attributePaths = "track")
    List<PlaylistTrack> findAllByIdPlaylistIdOrderByTrackOrderAsc(Long playlistId);

    long countByIdPlaylistId(Long playlistId);

    @Query("SELECT pt.id.playlistId, COUNT(pt) FROM PlaylistTrack pt WHERE pt.id.playlistId IN :playlistIds GROUP BY pt.id.playlistId")
    List<Object[]> countByPlaylistIdIn(@Param("playlistIds") List<Long> playlistIds);

    void deleteAllByIdPlaylistId(Long playlistId);
}
