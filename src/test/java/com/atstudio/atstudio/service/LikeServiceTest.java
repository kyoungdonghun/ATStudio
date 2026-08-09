package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.like.LikeResponse;
import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.key.LikeId;
import com.atstudio.atstudio.repository.LikeRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService 단위 테스트")
class LikeServiceTest {

    @Mock LikeRepository likeRepository;
    @Mock UserRepository userRepository;
    @Mock TrackRepository trackRepository;

    @InjectMocks LikeService likeService;

    // ── addLike() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addLike() 성공 - 좋아요 추가")
    void addLike_success() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(likeRepository.existsById(any(LikeId.class))).willReturn(false);

        likeService.addLike(2L, buildUserDetails(1L));

        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("addLike() 실패 - 이미 좋아요 → RESOURCE_DUPLICATE 예외(409)")
    void addLike_duplicate() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(likeRepository.existsById(any(LikeId.class))).willReturn(true);

        assertThatThrownBy(() -> likeService.addLike(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
    }

    @Test
    @DisplayName("addLike() 실패 - 트랙 없음/비활성 → TRACK_NOT_FOUND 예외")
    void addLike_trackNotFound() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.addLike(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    // ── getMyLikes() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyLikes() 성공 - 좋아요 목록 반환")
    void getMyLikes_success() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(likeRepository.findAllActiveByUser(user)).willReturn(List.of());

        List<LikeResponse> result = likeService.getMyLikes(buildUserDetails(1L));

        assertThat(result).isEmpty();
    }

    // ── removeLike() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeLike() 성공 - 좋아요 취소")
    void removeLike_success() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        Like like = Like.builder().id(new LikeId(1L, 2L)).user(user).track(track).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(likeRepository.findByUserAndTrack_Id(user, 2L)).willReturn(Optional.of(like));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));

        likeService.removeLike(2L, buildUserDetails(1L));

        verify(likeRepository).delete(like);
    }

    @Test
    @DisplayName("removeLike() 실패 - 좋아요 없음 → RESOURCE_NOT_FOUND 예외")
    void removeLike_notFound() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(likeRepository.findByUserAndTrack_Id(user, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.removeLike(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private User buildUser(Long id) {
        User user = User.builder()
                .email("test@test.com").nickname("nick").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.USER).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Track buildTrack(Long id, boolean active) {
        Track track = Track.builder()
                .title("Test Track").bpm(120).tonality("C").audioFile("audio.mp3")
                .user(User.builder().nickname("Artist " + id).email("artist" + id + "@test.com").build())
                .isActive(active).build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id).email("test@test.com").password("pw")
                .role(UserRole.USER).isDeleted(false).isProfileComplete(true)
                .build();
    }
}
