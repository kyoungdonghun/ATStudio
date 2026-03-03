package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.playlist.*;
import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final StorageService storageService;

    // ── 3.1 POST /api/playlists ──────────────────────────────────────────────

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistCreateRequest request,
                                           MultipartFile thumbnailFile,
                                           CustomUserDetails userDetails) {
        User user = validateSubscriber(userDetails);

        String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? storageService.store(thumbnailFile, "playlists/thumbnails")
                : null;

        Playlist playlist = Playlist.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(thumbnailUrl)
                .user(user)
                .build();

        playlist = playlistRepository.save(playlist);
        return PlaylistResponse.from(playlist, 0);
    }

    // ── 3.2 GET /api/playlists ───────────────────────────────────────────────

    public List<PlaylistListItemResponse> getMyPlaylists(CustomUserDetails userDetails) {
        User user = validateSubscriber(userDetails);

        List<Playlist> playlists = playlistRepository
                .findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(user);

        List<Long> playlistIds = playlists.stream().map(Playlist::getId).toList();

        Map<Long, Long> countMap = playlistIds.isEmpty()
                ? Collections.emptyMap()
                : playlistTrackRepository.countByPlaylistIdIn(playlistIds)
                        .stream()
                        .collect(Collectors.toMap(
                                row -> (Long) row[0],
                                row -> (Long) row[1]
                        ));

        return playlists.stream()
                .map(p -> PlaylistListItemResponse.from(p, countMap.getOrDefault(p.getId(), 0L).intValue()))
                .toList();
    }

    // ── 3.3 GET /api/playlists/{id} ──────────────────────────────────────────

    public PlaylistDetailResponse getPlaylistDetail(Long playlistId,
                                                     CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());

        List<PlaylistTrackItemResponse> tracks = playlistTrackRepository
                .findAllByIdPlaylistIdOrderByTrackOrderAsc(playlistId)
                .stream()
                .map(PlaylistTrackItemResponse::from)
                .toList();

        return PlaylistDetailResponse.from(playlist, tracks);
    }

    // ── 3.4 POST /api/playlists/{id}/tracks ──────────────────────────────────

    @Transactional
    public void addTrack(Long playlistId,
                         PlaylistAddTrackRequest request,
                         CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());

        Track track = trackRepository.findById(request.trackId())
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        PlaylistTrackId id = new PlaylistTrackId(playlistId, request.trackId());
        if (playlistTrackRepository.existsById(id)) {
            throw new BusinessException(BUSINESS_ERROR.DATA_INTEGRITY_VIOLATION);
        }

        int nextOrder = (int) playlistTrackRepository.countByIdPlaylistId(playlistId);

        PlaylistTrack playlistTrack = PlaylistTrack.builder()
                .id(id)
                .playlist(playlist)
                .track(track)
                .trackOrder(nextOrder)
                .build();

        playlistTrackRepository.save(playlistTrack);
    }

    // ── 3.5 PUT /api/playlists/{id} ──────────────────────────────────────────

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId,
                                           PlaylistUpdateRequest request,
                                           MultipartFile thumbnailFile,
                                           CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());

        String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? storageService.store(thumbnailFile, "playlists/thumbnails")
                : playlist.getThumbnail();

        playlist.update(request.getTitle(), request.getDescription(), thumbnailUrl);

        int trackCount = (int) playlistTrackRepository.countByIdPlaylistId(playlistId);
        return PlaylistResponse.from(playlist, trackCount);
    }

    // ── 3.6 PUT /api/playlists/{id}/tracks ───────────────────────────────────

    @Transactional
    public void reorderTracks(Long playlistId,
                              PlaylistReorderRequest request,
                              CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());

        playlistTrackRepository.deleteAllByIdPlaylistId(playlistId);
        playlistTrackRepository.flush();

        List<PlaylistTrack> reordered = request.tracks().stream()
                .map(item -> PlaylistTrack.builder()
                        .id(new PlaylistTrackId(playlistId, item.trackId()))
                        .playlist(playlist)
                        .track(trackRepository.getReferenceById(item.trackId()))
                        .trackOrder(item.trackOrder())
                        .build())
                .toList();

        playlistTrackRepository.saveAll(reordered);
    }

    // ── 3.7 DELETE /api/playlists/{id}/tracks/{trackId} ──────────────────────

    @Transactional
    public void removeTrack(Long playlistId, Long trackId,
                            CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        getOwnedPlaylist(playlistId, userDetails.getId());

        PlaylistTrackId id = new PlaylistTrackId(playlistId, trackId);
        PlaylistTrack playlistTrack = playlistTrackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        playlistTrackRepository.delete(playlistTrack);
    }

    // ── 3.8 DELETE /api/playlists/{id} ───────────────────────────────────────

    @Transactional
    public void deletePlaylist(Long playlistId, CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylist(playlistId, userDetails.getId());
        playlistTrackRepository.deleteAllByIdPlaylistId(playlistId);
        playlist.deactivate();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User validateSubscriber(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        userSubscriptionRepository.findActiveByUser(user, SubscriptionStatus.ACTIVE, LocalDate.now())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        return user;
    }

    private Playlist getOwnedPlaylist(Long playlistId, Long userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (!playlist.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }
        return playlist;
    }
}
