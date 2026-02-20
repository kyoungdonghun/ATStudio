package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.key.TrackTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackTagRepository extends JpaRepository<TrackTag, TrackTagId> {
}
