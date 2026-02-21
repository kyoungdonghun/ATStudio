package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);
}
