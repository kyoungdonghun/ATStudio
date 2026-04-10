package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlayHistory;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @EntityGraph(attributePaths = {"track", "track.user"})
    Page<PlayHistory> findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable);

    Optional<PlayHistory> findByUserAndTrack(User user, Track track);

    void deleteByIdInAndUser(List<Long> ids, User user);

    void deleteAllByUser(User user);

    void deleteAllByTrack(Track track);
}
