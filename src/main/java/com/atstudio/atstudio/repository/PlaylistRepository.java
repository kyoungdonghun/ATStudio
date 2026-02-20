package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
