package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.AlbumLike;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.AlbumLikeId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumLikeRepository extends JpaRepository<AlbumLike, AlbumLikeId> {

    @EntityGraph(attributePaths = "album")
    List<AlbumLike> findAllByUser(User user);

    Optional<AlbumLike> findByUserAndAlbum_Id(User user, Long albumId);

    void deleteAllByUser(User user);
}
