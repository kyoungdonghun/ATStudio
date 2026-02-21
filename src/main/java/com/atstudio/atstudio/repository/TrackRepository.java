package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {

    @Modifying
    @Query("UPDATE Track t SET t.playCount = t.playCount + 1 WHERE t.id = :trackId")
    void incrementPlayCount(@Param("trackId") Long trackId);
}
