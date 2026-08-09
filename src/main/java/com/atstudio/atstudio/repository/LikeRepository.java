package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.LikeId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {

    @EntityGraph(attributePaths = {"track", "track.user"})
    List<Like> findAllByUser(User user);

    @EntityGraph(attributePaths = {"track", "track.user"})
    @Query("""
            SELECT l FROM Like l
            WHERE l.user = :user
              AND l.track.isActive = true
            """)
    List<Like> findAllActiveByUser(@Param("user") User user);

    Optional<Like> findByUserAndTrack_Id(User user, Long trackId);

    void deleteAllByTrack(Track track);

    void deleteAllByUser(User user);
}
