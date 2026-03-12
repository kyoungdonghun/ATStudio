package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackDownload;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TrackDownloadRepository extends JpaRepository<TrackDownload, Long> {

    long countByUserAndDownloadedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    void deleteAllByTrack(Track track);

    void deleteAllByUser(User user);
}
