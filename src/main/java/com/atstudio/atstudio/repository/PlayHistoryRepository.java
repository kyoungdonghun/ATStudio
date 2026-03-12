package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.PlayHistory;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayHistoryRepository extends JpaRepository<PlayHistory, Long> {

    @EntityGraph(attributePaths = "track")
    Page<PlayHistory> findAllByUserOrderByPlayedAtDesc(User user, Pageable pageable);

    void deleteByIdInAndUser(List<Long> ids, User user);

    void deleteAllByUser(User user);

    void deleteAllByTrack(Track track);
}
