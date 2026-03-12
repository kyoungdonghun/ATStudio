package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.License;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByUserAndTrack(User user, Track track);

    Optional<License> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = "track")
    Page<License> findAllByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = "track")
    Page<License> findAllByUser_Id(Long userId, Pageable pageable);

    void deleteAllByTrack(Track track);

    void deleteAllByUser(User user);
}
