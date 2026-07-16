package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Track;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {

    @EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})
    @Override
    Page<Track> findAll(Specification<Track> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"trackTags", "trackTags.tag"})
    @Query("SELECT t FROM Track t WHERE t.id = :id")
    Optional<Track> findByIdWithTags(@Param("id") Long id);

    long countByIsActiveTrue();

    @Modifying
    @Query("UPDATE Track t SET t.playCount = t.playCount + 1 WHERE t.id = :trackId")
    void incrementPlayCount(@Param("trackId") Long trackId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Track t SET t.downloadCount = t.downloadCount + 1 "
            + "WHERE t.id = :trackId AND t.isActive = true")
    int incrementDownloadCountAtomically(@Param("trackId") Long trackId);
}
