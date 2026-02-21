package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.key.TrackTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackTagRepository extends JpaRepository<TrackTag, TrackTagId> {

    void deleteAllByTrack(Track track);

    void deleteAllByTag(Tag tag);

    @Query("SELECT tt FROM TrackTag tt JOIN FETCH tt.tag WHERE tt.track = :track")
    List<TrackTag> findAllWithTagByTrack(@Param("track") Track track);

    @Query("SELECT tt FROM TrackTag tt JOIN FETCH tt.tag WHERE tt.id.trackId IN :trackIds")
    List<TrackTag> findAllWithTagByTrackIdIn(@Param("trackIds") List<Long> trackIds);
}
