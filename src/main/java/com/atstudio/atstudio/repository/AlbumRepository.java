package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Album;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {

    @Query(
            value = "SELECT a FROM Album a WHERE a.isActive = true "
                    + "ORDER BY a.createdAt DESC, a.id DESC",
            countQuery = "SELECT COUNT(a) FROM Album a WHERE a.isActive = true")
    Page<Album> findAllActiveOrderByCreatedAt(Pageable pageable);

    @Query(
            value = "SELECT a FROM Album a LEFT JOIN a.albumTracks at ON at.track.isActive = true "
                    + "WHERE a.isActive = true "
                    + "GROUP BY a.id "
                    + "ORDER BY COUNT(at) DESC, a.createdAt DESC, a.id DESC",
            countQuery = "SELECT COUNT(a) FROM Album a WHERE a.isActive = true")
    Page<Album> findAllActiveOrderByTrackCount(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Album a WHERE a.id = :id")
    Optional<Album> findByIdForUpdate(@Param("id") Long id);
}
