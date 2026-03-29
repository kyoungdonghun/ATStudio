package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.like.AlbumLikeResponse;
import com.atstudio.atstudio.entity.Album;
import com.atstudio.atstudio.entity.AlbumLike;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.AlbumLikeId;
import com.atstudio.atstudio.repository.AlbumLikeRepository;
import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.AlbumTrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlbumLikeService {

    private final AlbumLikeRepository albumLikeRepository;
    private final AlbumRepository albumRepository;
    private final AlbumTrackRepository albumTrackRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addAlbumLike(Long albumId, CustomUserDetails userDetails) {
        User user = getUser(userDetails);

        Album album = albumRepository.findById(albumId)
                .filter(Album::isActive)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.ALBUM_NOT_FOUND));

        AlbumLikeId likeId = new AlbumLikeId(user.getId(), album.getId());
        if (albumLikeRepository.existsById(likeId)) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_DUPLICATE);
        }

        AlbumLike albumLike = AlbumLike.builder()
                .id(likeId)
                .user(user)
                .album(album)
                .build();
        albumLikeRepository.save(albumLike);
        album.incrementLikeCount();
    }

    public List<AlbumLikeResponse> getMyAlbumLikes(CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        List<AlbumLike> albumLikes = albumLikeRepository.findAllByUser(user);
        List<Album> albums = albumLikes.stream().map(AlbumLike::getAlbum).toList();
        Map<Long, Integer> countMap = albumTrackRepository.countMapByAlbums(albums);
        return albumLikes.stream()
                .map(al -> AlbumLikeResponse.from(al, countMap.getOrDefault(al.getAlbum().getId(), 0)))
                .toList();
    }

    @Transactional
    public void removeAlbumLike(Long albumId, CustomUserDetails userDetails) {
        User user = getUser(userDetails);
        AlbumLike albumLike = albumLikeRepository.findByUserAndAlbum_Id(user, albumId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        albumLikeRepository.delete(albumLike);

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.ALBUM_NOT_FOUND));
        album.decrementLikeCount();
    }

    private User getUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }
}
