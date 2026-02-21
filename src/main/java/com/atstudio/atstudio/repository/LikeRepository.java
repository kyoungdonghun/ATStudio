package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.LikeId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @EntityGraph(attributePaths = "track")
    List<Like> findAllByUser(User user);

    Optional<Like> findByUserAndTrack_Id(User user, Long trackId);
}
