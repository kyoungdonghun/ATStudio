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

    Optional<Playlist> findByIdAndUser_IdAndIsActiveTrue(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Playlist p WHERE p.id = :id AND p.user.id = :userId AND p.isActive = true")
    Optional<Playlist> findOwnedForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findByIdForUpdate(@Param("id") Long id);
}
