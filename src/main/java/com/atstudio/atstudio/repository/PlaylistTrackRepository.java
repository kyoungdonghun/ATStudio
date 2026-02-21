package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {

    @EntityGraph(attributePaths = "track")
    List<PlaylistTrack> findAllByIdPlaylistIdOrderByTrackOrderAsc(Long playlistId);

    long countByIdPlaylistId(Long playlistId);

    void deleteAllByIdPlaylistId(Long playlistId);
}
