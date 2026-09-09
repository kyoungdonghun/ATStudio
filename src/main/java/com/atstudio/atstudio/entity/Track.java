package com.atstudio.atstudio.entity;

import com.atstudio.atstudio.common.entity.BaseEntity;
import com.atstudio.atstudio.entity.enums.AudioProcessingState;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracks", indexes = @Index(name = "idx_tracks_audio_queue", columnList = "audio_processing_state,id"))
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
    private String streamAudioFile;

    @Builder.Default
    @Column(nullable = false)
    private boolean streamRequired = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "VARCHAR(16)")
    private AudioProcessingState audioProcessingState = AudioProcessingState.READY;

    @Builder.Default
    @Column(nullable = false)
    private long audioGeneration = 0;

    @Column(length = 255)
    private String pendingAudioFile;

    // A superseded encoder retains its input until it exits, including across a restart.
    @Column(length = 255)
    private String claimedAudioFile;

    @Column(length = 36)
    private String audioClaimToken;

    @Builder.Default
    @Column(nullable = false)
    private int audioAttemptCount = 0;

    @Column(length = 64)
    private String audioErrorCode;

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

    public void updateAudioAnalysis(String audioFile, int duration, String waveformData) {
        if (audioFile == null || audioFile.isBlank() || duration < 1
                || waveformData == null || waveformData.isBlank()) {
            throw new IllegalArgumentException("Complete audio analysis metadata is required");
        }
        this.audioFile = audioFile;
        this.duration = duration;
        this.waveformData = waveformData;
    }

    public void updateThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public boolean isStreamReady() {
        return audioFile != null && (!streamRequired || streamAudioFile != null);
    }

    public String listeningAudioFile() {
        return streamAudioFile != null ? streamAudioFile : (streamRequired ? null : audioFile);
    }

    public void enqueueAudio(String key, boolean initial) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Audio key is required");
        pendingAudioFile = key;
        audioGeneration++;
        audioAttemptCount = 0;
        audioErrorCode = null;
        audioProcessingState = AudioProcessingState.PENDING;
        if (initial) streamRequired = true;
    }

    public boolean canRetryAudio() {
        return audioProcessingState == AudioProcessingState.FAILED && pendingAudioFile != null
                && audioClaimToken == null;
    }

    public void retryAudio() {
        if (!canRetryAudio()) throw new IllegalStateException("Audio cannot be retried");
        audioGeneration++;
        audioErrorCode = null;
        audioProcessingState = AudioProcessingState.PENDING;
    }

    public void claimAudio(String token) {
        if (audioProcessingState != AudioProcessingState.PENDING || audioClaimToken != null
                || pendingAudioFile == null) throw new IllegalStateException("Audio cannot be claimed");
        audioClaimToken = token;
        claimedAudioFile = pendingAudioFile;
        audioAttemptCount++;
        audioProcessingState = AudioProcessingState.PROCESSING;
    }

    public boolean ownsAudioClaim(long generation, String token) {
        return audioGeneration == generation && token != null && token.equals(audioClaimToken)
                && audioProcessingState == AudioProcessingState.PROCESSING;
    }

    public void finishAudio(String streamKey, int duration, String waveform) {
        updateAudioAnalysis(pendingAudioFile, duration, waveform);
        streamAudioFile = streamKey;
        streamRequired = true;
        pendingAudioFile = null;
        audioProcessingState = AudioProcessingState.READY;
        audioErrorCode = null;
    }

    public void failAudio(String code) {
        audioProcessingState = AudioProcessingState.FAILED;
        audioErrorCode = code;
    }

    public void releaseAudioClaim() {
        claimedAudioFile = null;
        audioClaimToken = null;
    }

    public void cancelAudio() {
        if (pendingAudioFile == null) return;
        audioGeneration++;
        pendingAudioFile = null;
        audioProcessingState = AudioProcessingState.CANCELLED;
        audioErrorCode = null;
    }

    public void useDirectAudio(String key, int duration, String waveform) {
        updateAudioAnalysis(key, duration, waveform);
        audioGeneration++;
        pendingAudioFile = null;
        streamAudioFile = null;
        streamRequired = false;
        audioProcessingState = AudioProcessingState.READY;
        audioErrorCode = null;
        audioAttemptCount = 0;
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
