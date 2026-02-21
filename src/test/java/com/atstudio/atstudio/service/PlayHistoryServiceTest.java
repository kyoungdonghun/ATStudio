package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryDeleteRequest;
import com.atstudio.atstudio.dto.playhistory.PlayHistoryListItemResponse;
import com.atstudio.atstudio.entity.PlayHistory;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PlayHistoryRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayHistoryService 단위 테스트")
class PlayHistoryServiceTest {

    @Mock PlayHistoryRepository playHistoryRepository;
    @Mock TrackRepository trackRepository;
    @Mock UserRepository userRepository;

    @InjectMocks PlayHistoryService playHistoryService;

    // ── savePlayHistory() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("savePlayHistory() 성공 - 히스토리 저장 및 playCount 증가")
    void savePlayHistory_success() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        CustomUserDetails userDetails = buildUserDetails(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(playHistoryRepository.save(any(PlayHistory.class))).willReturn(
                PlayHistory.builder().user(user).track(track).build());

        playHistoryService.savePlayHistory(2L, userDetails);

        verify(playHistoryRepository).save(any(PlayHistory.class));
        verify(trackRepository).incrementPlayCount(2L);
    }

    @Test
    @DisplayName("savePlayHistory() 실패 - 유저 없음 → RESOURCE_NOT_FOUND 예외")
    void savePlayHistory_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> playHistoryService.savePlayHistory(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    @Test
    @DisplayName("savePlayHistory() 실패 - 트랙 없음/비활성 → TRACK_NOT_FOUND 예외")
    void savePlayHistory_trackNotFound() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> playHistoryService.savePlayHistory(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    // ── getMyHistory() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyHistory() 성공 - 페이지네이션된 히스토리 목록 반환")
    void getMyHistory_success() {
        User user = buildUser(1L);
        Page<PlayHistory> page = new PageImpl<>(List.of());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(playHistoryRepository.findAllByUserOrderByPlayedAtDesc(eq(user), any())).willReturn(page);

        ResponseDTO<PlayHistoryListItemResponse> result = playHistoryService.getMyHistory(buildUserDetails(1L), 1, 50);

        assertThat(result).isNotNull();
        assertThat(result.getDataList()).isEmpty();
    }

    // ── deleteHistory() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteHistory() 선택 삭제 - 특정 ID 목록 삭제")
    void deleteHistory_selective() {
        User user = buildUser(1L);
        PlayHistoryDeleteRequest request = new PlayHistoryDeleteRequest(List.of(10L, 20L));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        playHistoryService.deleteHistory(request, buildUserDetails(1L));

        verify(playHistoryRepository).deleteByIdInAndUser(List.of(10L, 20L), user);
    }

    @Test
    @DisplayName("deleteHistory() 전체 삭제 - historyIds 비어있으면 전체 삭제")
    void deleteHistory_all() {
        User user = buildUser(1L);
        PlayHistoryDeleteRequest request = new PlayHistoryDeleteRequest(List.of());
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        playHistoryService.deleteHistory(request, buildUserDetails(1L));

        verify(playHistoryRepository).deleteAllByUser(user);
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
