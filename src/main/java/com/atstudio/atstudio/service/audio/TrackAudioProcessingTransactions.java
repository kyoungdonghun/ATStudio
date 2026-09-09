package com.atstudio.atstudio.service.audio;

import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.enums.AudioProcessingState;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackAudioProcessingTransactions {
    private final TrackRepository tracks;
    private final StorageMutationCoordinator mutations;

    public record Claim(Long trackId, long generation, String token, String sourceKey) { }

    @Transactional
    public Claim claimNext() {
        for (Long id : tracks.findPendingAudioIds(PageRequest.of(0, 1))) {
            Track track = tracks.findByIdForUpdate(id).orElse(null);
            if (track == null || track.getAudioProcessingState() != AudioProcessingState.PENDING
                    || track.getAudioClaimToken() != null || track.getPendingAudioFile() == null) continue;
            String token = UUID.randomUUID().toString();
            track.claimAudio(token);
            return new Claim(id, track.getAudioGeneration(), token, track.getClaimedAudioFile());
        }
        return null;
    }

    @Transactional
    public boolean complete(Claim claim, StorageMutationCoordinator.GeneratedWrite generated,
            AudioAnalysisResult analysis) {
        Track track = tracks.findByIdForUpdate(claim.trackId()).orElse(null);
        if (track == null) return false;
        boolean current = track.ownsAudioClaim(claim.generation(), claim.token());
        if (current) {
            String oldOriginal = track.getAudioFile();
            String oldStream = track.getStreamAudioFile();
            generated.attachToTransaction();
            // Update audio fields only. Manual activation and concurrent metadata edits survive.
            track.finishAudio(generated.key(), analysis.durationSeconds(), analysis.waveformJson());
            cleanup(oldOriginal);
            cleanup(oldStream);
        }
        releaseClaim(track, claim.token());
        return current;
    }

    @Transactional
    public void fail(Claim claim, AudioTranscodeException.Code code) {
        Track track = tracks.findByIdForUpdate(claim.trackId()).orElse(null);
        if (track == null) return;
        if (track.ownsAudioClaim(claim.generation(), claim.token())) track.failAudio(code.name());
        releaseClaim(track, claim.token());
    }

    @Transactional
    public int recoverInterruptedBatch() {
        var ids = tracks.findClaimedAudioIds(PageRequest.of(0, 50));
        for (Long id : ids) {
            Track track = tracks.findByIdForUpdate(id).orElse(null);
            if (track == null || track.getAudioClaimToken() == null) continue;
            if (track.getAudioProcessingState() == AudioProcessingState.PROCESSING) {
                track.failAudio(AudioTranscodeException.Code.AUDIO_PROCESSING_INTERRUPTED.name());
            }
            releaseClaim(track, track.getAudioClaimToken());
        }
        return ids.size();
    }

    private void releaseClaim(Track track, String token) {
        if (!token.equals(track.getAudioClaimToken())) return;
        String input = track.getClaimedAudioFile();
        track.releaseAudioClaim();
        cleanup(input);
    }

    private void cleanup(String key) {
        if (key != null) mutations.deleteAfterCommit(StorageDomain.TRACK, StorageRoot.PUBLIC, key);
    }
}
