package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackId> {
}
