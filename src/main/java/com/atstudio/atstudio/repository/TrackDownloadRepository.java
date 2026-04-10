package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackDownload;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrackDownloadRepository extends JpaRepository<TrackDownload, Long> {

    long countByUserAndDownloadedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    void deleteAllByTrack(Track track);

    void deleteAllByUser(User user);

    /**
     * Paged download history for current user, with optional keyword filter on
     * track title or tag name. Only includes active tracks. One row per
     * download event (duplicates allowed if user downloaded the same track
     * multiple times — sort/pagination is based on downloaded_at).
     */
    @Query("""
            SELECT td FROM TrackDownload td
            JOIN td.track t
            WHERE td.user = :user
              AND t.isActive = true
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR EXISTS (
                        SELECT 1 FROM TrackTag tt
                        WHERE tt.track = t
                          AND LOWER(tt.tag.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   ))
            """)
    Page<TrackDownload> findMyDownloadHistory(
            @Param("user") User user,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Returns distinct track IDs (ordered by most recent download) matching the
     * current user's filtered download history — used by "전체 재다운로드" to
     * build the full re-download list without paging through every row.
     */
    @Query("""
            SELECT DISTINCT t.id FROM TrackDownload td
            JOIN td.track t
            WHERE td.user = :user
              AND t.isActive = true
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR EXISTS (
                        SELECT 1 FROM TrackTag tt
                        WHERE tt.track = t
                          AND LOWER(tt.tag.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   ))
            """)
    List<Long> findMyDownloadHistoryTrackIds(
            @Param("user") User user,
            @Param("keyword") String keyword
    );
}
