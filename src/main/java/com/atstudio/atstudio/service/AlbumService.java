package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.album.*;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumTrack;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.AlbumTrackRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumTrackRepository albumTrackRepository;
    private final TrackRepository trackRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    // -- 15.1 POST /api/albums ------------------------------------------------

    @Transactional
    public AlbumResponse createAlbum(AlbumCreateRequest request,
                                     MultipartFile thumbnailFile,
                                     CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? storageService.store(thumbnailFile, "albums/thumbnails")
                : null;

        Album album = Album.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnail(thumbnailUrl)
                .createdBy(user)
                .build();

        album = albumRepository.save(album);
        return AlbumResponse.from(album, 0);
    }

    // -- 15.2 GET /api/albums -------------------------------------------------

    public List<AlbumListItemResponse> getAlbums() {
        List<Album> albums = albumRepository.findAllByIsActiveTrueOrderByCreatedAtDesc();
        Map<Long, Integer> countMap = albumTrackRepository.countMapByAlbums(albums);
        return albums.stream()
                .map(album -> AlbumListItemResponse.from(album, countMap.getOrDefault(album.getId(), 0)))
                .toList();
    }

    public ResponseDTO<AlbumListItemResponse> getAlbumsPaged(int page, int size) {
        Page<Album> albumPage = albumRepository.findAllByIsActiveTrueOrderByCreatedAtDesc(
                PageRequest.of(page - 1, size));
        List<Album> albums = albumPage.getContent();
        Map<Long, Integer> countMap = albumTrackRepository.countMapByAlbums(albums);
        List<AlbumListItemResponse> dataList = albums.stream()
                .map(album -> AlbumListItemResponse.from(album, countMap.getOrDefault(album.getId(), 0)))
                .toList();
        return ResponseDTO.<AlbumListItemResponse>builder()
                .message("Albums retrieved")
                .dataList(dataList)
                .pageInfo(PageInfo.of(page, size, (int) albumPage.getTotalElements(), 10))
                .build();
    }

    // -- 15.3 GET /api/albums/{id} --------------------------------------------

    public AlbumDetailResponse getAlbum(Long id) {
        Album album = getActiveAlbum(id);

        List<AlbumTrackItemResponse> tracks = albumTrackRepository
                .findAllByAlbumOrderByTrackOrder(album)
                .stream()
                .map(AlbumTrackItemResponse::from)
                .toList();

        return AlbumDetailResponse.from(album, tracks);
    }

    // -- 15.4 PUT /api/albums/{id} --------------------------------------------

    @Transactional
    public AlbumResponse updateAlbum(Long id,
                                     AlbumUpdateRequest request,
                                     MultipartFile thumbnailFile) {
        Album album = getActiveAlbum(id);

        String thumbnailUrl = (thumbnailFile != null && !thumbnailFile.isEmpty())
                ? storageService.store(thumbnailFile, "albums/thumbnails")
                : null;

        album.update(request.getTitle(), request.getDescription(), thumbnailUrl);

        int trackCount = (int) albumTrackRepository.countByAlbum(album);
        return AlbumResponse.from(album, trackCount);
    }

    // -- 15.5 DELETE /api/albums/{id} -----------------------------------------

    @Transactional
    public void deleteAlbum(Long id) {
        Album album = getActiveAlbum(id);
        album.softDelete();
    }

    // -- 15.6 POST /api/albums/{id}/tracks ------------------------------------

    @Transactional
    public AlbumDetailResponse addTrack(Long albumId, AlbumTrackAddRequest request) {
        Album album = getActiveAlbum(albumId);

        Track track = trackRepository.findById(request.trackId())
                .filter(Track::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        if (albumTrackRepository.existsByAlbumAndTrack(album, track)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        int nextOrder = (int) albumTrackRepository.countByAlbum(album);

        AlbumTrack albumTrack = AlbumTrack.builder()
                .id(new AlbumTrackId(albumId, request.trackId()))
                .album(album)
                .track(track)
                .trackOrder(nextOrder)
                .build();

        albumTrackRepository.save(albumTrack);

        return getAlbum(albumId);
    }

    // -- 15.7 DELETE /api/albums/{id}/tracks/{trackId} ------------------------

    @Transactional
    public void removeTrack(Long albumId, Long trackId) {
        getActiveAlbum(albumId);

        AlbumTrackId id = new AlbumTrackId(albumId, trackId);
        AlbumTrack albumTrack = albumTrackRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        albumTrackRepository.delete(albumTrack);
    }

    // -- 15.8 PUT /api/albums/{id}/tracks -------------------------------------

    @Transactional
    public AlbumDetailResponse reorderTracks(Long albumId, AlbumTrackOrderRequest request) {
        Album album = getActiveAlbum(albumId);

        for (AlbumTrackOrderItem item : request.trackOrders()) {
            AlbumTrackId id = new AlbumTrackId(albumId, item.trackId());
            AlbumTrack albumTrack = albumTrackRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
            albumTrack.updateOrder(item.order());
        }

        List<AlbumTrackItemResponse> tracks = albumTrackRepository
                .findAllByAlbumOrderByTrackOrder(album)
                .stream()
                .map(AlbumTrackItemResponse::from)
                .toList();

        return AlbumDetailResponse.from(album, tracks);
    }

    // -- helpers --------------------------------------------------------------

    private Album getActiveAlbum(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (!album.isActive()) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND);
        }
        return album;
    }
}
