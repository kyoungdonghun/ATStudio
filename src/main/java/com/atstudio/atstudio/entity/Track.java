package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Track extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String thumbnail;

    @Column(nullable = false)
    private int bpm;

    @Column(nullable = false, length = 10)
    private String tonality;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String audioFile;

    @Column(length = 255)
    private String previewFile;

    /** Duration in seconds — auto-extracted from audio file on upload */
    @Builder.Default
    @Column(nullable = false)
    private int duration = 0;

    /** Waveform peak data (JSON array of 200 floats 0.0–1.0) — extracted on upload (SR-90) */
    @Column(columnDefinition = "TEXT")
    private String waveformData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = false;

    @Builder.Default
    @Column(nullable = false)
    private long playCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long likeCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private long downloadCount = 0L;

    @OneToMany(mappedBy = "track", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TrackTag> trackTags = new ArrayList<>();

    public void update(String title, Integer bpm, String tonality, String description) {
        if (title != null) this.title = title;
        if (bpm != null) this.bpm = bpm;
        if (tonality != null) this.tonality = tonality;
        if (description != null) this.description = description;
    }

    public void updateAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public void updateWaveformData(String waveformData) {
        this.waveformData = waveformData;
    }

    public void updateThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

}
