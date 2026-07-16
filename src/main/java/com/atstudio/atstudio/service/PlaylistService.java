package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.playlist.*;
import com.atstudio.atstudio.entity.Playlist;
import com.atstudio.atstudio.entity.PlaylistTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final StorageMutationCoordinator storageMutationCoordinator;
    private final CanonicalImageService canonicalImageService;

    // ── 3.1 POST /api/playlists ──────────────────────────────────────────────

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistCreateRequest request,
                                           MultipartFile thumbnailFile,
                                           CustomUserDetails userDetails) {
        User user = validateSubscriberForPlaylistCreation(userDetails);

        int maxPlaylists = userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
                .map(us -> us.getSubscription().getMaxPlaylists())
                .orElse(3);
        if (playlistRepository.countByUserAndIsActiveTrue(user) >= maxPlaylists) {
            throw new BusinessException(BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED);
        }

        String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? storageMutationCoordinator.store(
                        StorageDomain.PLAYLIST,
                        StorageRoot.PUBLIC,
                        canonicalImageService.canonicalizeThumbnail(thumbnailFile),
                        "playlists/thumbnails")
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
        Playlist playlist = getOwnedPlaylistForUpdate(playlistId, userDetails.getId());

        Track track = trackRepository.findById(request.trackId())
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.TRACK_NOT_FOUND));

        PlaylistTrackId id = new PlaylistTrackId(playlistId, request.trackId());
        if (playlistTrackRepository.existsById(id)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
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

    // ── 3.4b POST /api/playlists/{id}/tracks/batch ─────────────────────────

    @Transactional
    public int addTracksBatch(Long playlistId,
                              PlaylistBatchAddTrackRequest request,
                              CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylistForUpdate(playlistId, userDetails.getId());

        int currentOrder = (int) playlistTrackRepository.countByIdPlaylistId(playlistId);
        int added = 0;
        Set<Long> requestedTrackIds = new HashSet<>();

        for (Long trackId : request.trackIds()) {
            if (!requestedTrackIds.add(trackId)) {
                continue;
            }
            PlaylistTrackId id = new PlaylistTrackId(playlistId, trackId);
            if (playlistTrackRepository.existsById(id)) {
                continue; // skip duplicates silently
            }

            Track track = trackRepository.findById(trackId)
                    .filter(Track::isActive)
                    .orElse(null);
            if (track == null) {
                continue; // skip invalid tracks
            }

            PlaylistTrack playlistTrack = PlaylistTrack.builder()
                    .id(id)
                    .playlist(playlist)
                    .track(track)
                    .trackOrder(currentOrder + added)
                    .build();

            playlistTrackRepository.save(playlistTrack);
            added++;
        }

        return added;
    }

    // ── 3.5 PUT /api/playlists/{id} ──────────────────────────────────────────

    @Transactional
    public PlaylistResponse updatePlaylist(Long playlistId,
                                           PlaylistUpdateRequest request,
                                           MultipartFile thumbnailFile,
                                           CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylistForUpdate(playlistId, userDetails.getId());

        String thumbnailUrl = playlist.getThumbnail();
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailUrl = storageMutationCoordinator.replace(
                    StorageDomain.PLAYLIST,
                    StorageRoot.PUBLIC,
                    canonicalImageService.canonicalizeThumbnail(thumbnailFile),
                    "playlists/thumbnails",
                    playlist.getThumbnail());
        }

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
        getOwnedPlaylistForUpdate(playlistId, userDetails.getId());
        List<PlaylistTrack> playlistTracks = playlistTrackRepository
                .findAllByIdPlaylistIdOrderByTrackOrderAsc(playlistId);
        validatePlaylistReorderRequest(request, playlistTracks);

        Map<Long, PlaylistTrack> tracksById = playlistTracks.stream()
                .collect(Collectors.toMap(track -> track.getTrack().getId(), track -> track));
        for (PlaylistTrackOrderItem item : request.tracks()) {
            tracksById.get(item.trackId()).updateOrder(item.trackOrder());
        }
    }

    // ── 3.7 DELETE /api/playlists/{id}/tracks/{trackId} ──────────────────────

    @Transactional
    public void removeTrack(Long playlistId, Long trackId,
                            CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        getOwnedPlaylistForUpdate(playlistId, userDetails.getId());

        PlaylistTrackId id = new PlaylistTrackId(playlistId, trackId);
        PlaylistTrack playlistTrack = playlistTrackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        playlistTrackRepository.delete(playlistTrack);
        compactTrackOrders(playlistTrackRepository.findAllByIdPlaylistIdOrderByTrackOrderAsc(playlistId), trackId);
    }

    // ── 3.8 DELETE /api/playlists/{id} ───────────────────────────────────────

    @Transactional
    public void deletePlaylist(Long playlistId, CustomUserDetails userDetails) {
        validateSubscriber(userDetails);
        Playlist playlist = getOwnedPlaylistForUpdate(playlistId, userDetails.getId());
        playlistTrackRepository.deleteAllByIdPlaylistId(playlistId);
        playlist.deactivate();
        storageMutationCoordinator.deleteAfterCommit(
                StorageDomain.PLAYLIST,
                StorageRoot.PUBLIC,
                playlist.getThumbnail());
    }

    // ── Default playlist on signup ──────────────────────────────────────────

    /**
     * Creates a default playlist for a newly registered user.
     * Bypasses subscription validation and playlist count limit.
     */
    @Transactional
    public void createDefaultPlaylist(User user) {
        Playlist playlist = Playlist.builder()
                .title("내 재생목록")
                .user(user)
                .build();
        playlistRepository.save(playlist);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User validateSubscriber(CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return requireActiveSubscription(user);
    }

    private User validateSubscriberForPlaylistCreation(CustomUserDetails userDetails) {
        User user = userRepository.findByIdForUpdate(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return requireActiveSubscription(user);
    }

    private User requireActiveSubscription(User user) {
        userSubscriptionRepository.findActiveByUser(user, LocalDate.now())
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

    private Playlist getOwnedPlaylistForUpdate(Long playlistId, Long userId) {
        Playlist playlist = playlistRepository.findByIdForUpdate(playlistId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (!playlist.getUser().getId().equals(userId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (!playlist.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }
        return playlist;
    }

    private void validatePlaylistReorderRequest(PlaylistReorderRequest request, List<PlaylistTrack> playlistTracks) {
        if (request == null || request.tracks() == null || request.tracks().size() != playlistTracks.size()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        Set<Long> requestedTrackIds = new HashSet<>();
        Set<Integer> requestedOrders = new HashSet<>();
        for (PlaylistTrackOrderItem item : request.tracks()) {
            if (item == null || item.trackId() == null || item.trackOrder() == null
                    || item.trackOrder() < 0 || !requestedTrackIds.add(item.trackId())
                    || !requestedOrders.add(item.trackOrder())) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
        }

        Set<Long> existingTrackIds = playlistTracks.stream()
                .map(track -> track.getTrack().getId())
                .collect(Collectors.toSet());
        if (!requestedTrackIds.equals(existingTrackIds)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        for (int order = 0; order < playlistTracks.size(); order++) {
            if (!requestedOrders.contains(order)) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
        }
    }

    private void compactTrackOrders(List<PlaylistTrack> playlistTracks, Long removedTrackId) {
        int order = 0;
        for (PlaylistTrack playlistTrack : playlistTracks) {
            if (!playlistTrack.getTrack().getId().equals(removedTrackId)) {
                playlistTrack.updateOrder(order++);
            }
        }
    }
}
