package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrackRepository extends JpaRepository<Track, Long>, JpaSpecificationExecutor<Track> {
}
