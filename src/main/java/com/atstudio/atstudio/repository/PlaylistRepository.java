package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    int countByUserAndIsActiveTrue(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findByIdForUpdate(@Param("id") Long id);
}
