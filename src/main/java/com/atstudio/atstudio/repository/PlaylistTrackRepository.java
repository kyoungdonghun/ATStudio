package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {

    @EntityGraph(attributePaths = {"track", "track.user"})
    @Query("""
            SELECT pt FROM PlaylistTrack pt
            WHERE pt.id.playlistId = :playlistId
            ORDER BY pt.trackOrder ASC, pt.id.trackId ASC
            """)
    List<PlaylistTrack> findAllByIdPlaylistIdOrderByTrackOrderAsc(
            @Param("playlistId") Long playlistId);

    @EntityGraph(attributePaths = {"track", "track.user"})
    @Query("""
            SELECT pt FROM PlaylistTrack pt
            WHERE pt.id.playlistId = :playlistId
              AND pt.track.isActive = true
            ORDER BY pt.trackOrder ASC, pt.id.trackId ASC
            """)
    List<PlaylistTrack> findAllPlayableByPlaylistIdOrderByTrackOrderAsc(
            @Param("playlistId") Long playlistId);

    long countByIdPlaylistId(Long playlistId);

    @Query("""
            SELECT COUNT(pt) FROM PlaylistTrack pt
            WHERE pt.id.playlistId = :playlistId
              AND pt.track.isActive = true
            """)
    long countActiveByPlaylistId(@Param("playlistId") Long playlistId);

    @Query("""
            SELECT pt.id.playlistId, COUNT(pt) FROM PlaylistTrack pt
            WHERE pt.id.playlistId IN :playlistIds
              AND pt.track.isActive = true
            GROUP BY pt.id.playlistId
            """)
    List<Object[]> countActiveByPlaylistIdIn(@Param("playlistIds") List<Long> playlistIds);

    void deleteAllByIdPlaylistId(Long playlistId);

    void deleteAllByIdTrackId(Long trackId);
}
