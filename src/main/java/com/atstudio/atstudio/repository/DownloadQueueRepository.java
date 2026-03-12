package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.DownloadQueue;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.DownloadQueueId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DownloadQueueRepository extends JpaRepository<DownloadQueue, DownloadQueueId> {

    @EntityGraph(attributePaths = "track")
    List<DownloadQueue> findAllByUser(User user);

    Optional<DownloadQueue> findByUserAndTrack_Id(User user, Long trackId);

    void deleteAllByTrack(Track track);

    void deleteAllByUser(User user);
}
