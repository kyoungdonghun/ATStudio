package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackService 단위 테스트")
class TrackServiceTest {

    @Mock TrackRepository trackRepository;
    @Mock TrackTagRepository trackTagRepository;
    @Mock TagRepository tagRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock CustomUserDetails userDetails;

    @InjectMocks TrackService trackService;

    // ── createTrack() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTrack() 성공 - audioFile만, thumbnail·tags 없음")
    void createTrack_success_audioFileOnly() {
        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        given(storageService.store(any(), eq("tracks/audio"))).willReturn("tracks/audio/test.mp3");
        given(trackRepository.save(any(Track.class))).willAnswer(inv -> {
            Track t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L);
            return t;
        });

        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Test Track");
        request.setBpm(120);
        request.setTonality("C");
        request.setAudioFile(mockMultipartFile("test.mp3"));

        TrackResponse response = trackService.createTrack(request, userDetails);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Test Track");
        assertThat(response.audioFile()).isEqualTo("tracks/audio/test.mp3");
        assertThat(response.thumbnail()).isNull();
        assertThat(response.tags()).isEmpty();
        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("createTrack() 성공 - thumbnail + tagIds 포함")
    void createTrack_success_withThumbnailAndTags() {
        Tag tag = buildTag(10L, "Happy", TagType.MOOD);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        given(storageService.store(any(), eq("tracks/audio"))).willReturn("tracks/audio/test.mp3");
        given(storageService.store(any(), eq("tracks/thumbnail"))).willReturn("tracks/thumbnail/cover.jpg");
        given(trackRepository.save(any(Track.class))).willAnswer(inv -> {
            Track t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L);
            return t;
        });
        given(tagRepository.findById(10L)).willReturn(Optional.of(tag));
        given(trackTagRepository.saveAll(any())).willReturn(List.of());

        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Test Track");
        request.setBpm(120);
        request.setTonality("C");
        request.setAudioFile(mockMultipartFile("test.mp3"));
        request.setThumbnail(mockMultipartFile("cover.jpg"));
        request.setTagIds(List.of(10L));

        TrackResponse response = trackService.createTrack(request, userDetails);

        assertThat(response.thumbnail()).isEqualTo("tracks/thumbnail/cover.jpg");
        assertThat(response.tags()).hasSize(1);
        assertThat(response.tags().get(0).name()).isEqualTo("Happy");
    }

    @Test
    @DisplayName("createTrack() 실패 - 존재하지 않는 사용자 → RESOURCE_NOT_FOUND 예외")
    void createTrack_fail_userNotFound() {
        given(userDetails.getId()).willReturn(99L);
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Track");
        request.setBpm(100);
        request.setTonality("D");
        request.setAudioFile(mockMultipartFile("test.mp3"));

        assertThatThrownBy(() -> trackService.createTrack(request, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── getTracks() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTracks() - 검색 결과 없을 때 빈 dataList, total=0 반환")
    @SuppressWarnings("unchecked")
    void getTracks_emptyResult() {
        Page<Track> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

        ResponseDTO<TrackListItemResponse> result = trackService.getTracks(new TrackSearchRequest());

        assertThat(result.getDataList()).isEmpty();
        assertThat(result.getPageInfo().getTotal()).isZero();
        verify(trackRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("getTracks() - @EntityGraph로 트랙+태그 일괄 로딩 (findAll 호출 검증)")
    @SuppressWarnings("unchecked")
    void getTracks_callsFindAllWithEntityGraph() {
        Track track = buildTrack(1L, true);
        Page<Track> page = new PageImpl<>(List.of(track), PageRequest.of(0, 20), 1);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        ResponseDTO<TrackListItemResponse> result = trackService.getTracks(new TrackSearchRequest());

        assertThat(result.getDataList()).hasSize(1);
        verify(trackRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ── getTracksForAdmin() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getTracksForAdmin() - isActive=null이면 활성+비활성 전체 반환")
    @SuppressWarnings("unchecked")
    void getTracksForAdmin_allTracks() {
        Track active = buildTrack(1L, true);
        Track inactive = buildTrack(2L, false);
        Page<Track> page = new PageImpl<>(List.of(active, inactive), PageRequest.of(0, 20), 2);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(null, 1, 20);

        assertThat(result.getDataList()).hasSize(2);
        assertThat(result.getPageInfo().getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("getTracksForAdmin() - isActive=true이면 활성 트랙만 반환")
    @SuppressWarnings("unchecked")
    void getTracksForAdmin_activeOnly() {
        Track active = buildTrack(1L, true);
        Page<Track> page = new PageImpl<>(List.of(active), PageRequest.of(0, 20), 1);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(true, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("getTracksForAdmin() - isActive=false이면 비활성 트랙만 반환")
    @SuppressWarnings("unchecked")
    void getTracksForAdmin_inactiveOnly() {
        Track inactive = buildTrack(2L, false);
        Page<Track> page = new PageImpl<>(List.of(inactive), PageRequest.of(0, 20), 1);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(false, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("getTracksForAdmin() - 결과 없으면 빈 dataList 반환")
    @SuppressWarnings("unchecked")
    void getTracksForAdmin_emptyResult() {
        Page<Track> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(null, 1, 20);

        assertThat(result.getDataList()).isEmpty();
        assertThat(result.getPageInfo().getTotal()).isZero();
    }

    // ── getTrack() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTrack() 성공 - findByIdWithTags로 @EntityGraph 활용하여 태그 일괄 로딩")
    void getTrack_success() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findByIdWithTags(1L)).willReturn(Optional.of(track));

        TrackResponse response = trackService.getTrack(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.isActive()).isTrue();
        assertThat(response.tags()).isEmpty();
        verify(trackRepository).findByIdWithTags(1L);
    }

    @Test
    @DisplayName("getTrack() 실패 - 존재하지 않는 트랙 → TRACK_NOT_FOUND 예외")
    void getTrack_fail_notFound() {
        given(trackRepository.findByIdWithTags(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> trackService.getTrack(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    @Test
    @DisplayName("getTrack() 실패 - 비활성 트랙(isActive=false) → TRACK_NOT_FOUND 예외")
    void getTrack_fail_inactiveTrack() {
        Track inactiveTrack = buildTrack(1L, false);
        given(trackRepository.findByIdWithTags(1L)).willReturn(Optional.of(inactiveTrack));

        assertThatThrownBy(() -> trackService.getTrack(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    // ── updateTrack() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateTrack() 성공 - 메타데이터만 수정, 파일·태그 변경 없음")
    void updateTrack_success_metadataOnly() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(trackTagRepository.findAllWithTagByTrack(any(Track.class))).willReturn(List.of());

        TrackUpdateRequest request = new TrackUpdateRequest();
        request.setTitle("Updated Title");
        request.setBpm(140);

        TrackResponse response = trackService.updateTrack(1L, request);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.bpm()).isEqualTo(140);
        verify(storageService, never()).delete(anyString());
        verify(storageService, never()).store(any(), anyString());
    }

    @Test
    @DisplayName("updateTrack() 성공 - 새 audioFile 교체: 기존 삭제 + 신규 저장")
    void updateTrack_success_withNewAudioFile() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(storageService.store(any(), eq("tracks/audio"))).willReturn("tracks/audio/new.mp3");
        given(trackTagRepository.findAllWithTagByTrack(any(Track.class))).willReturn(List.of());

        TrackUpdateRequest request = new TrackUpdateRequest();
        request.setAudioFile(mockMultipartFile("new.mp3"));

        TrackResponse response = trackService.updateTrack(1L, request);

        assertThat(response.audioFile()).isEqualTo("tracks/audio/new.mp3");
        verify(storageService).delete("tracks/audio/test.mp3");
        verify(storageService).store(any(), eq("tracks/audio"));
    }

    @Test
    @DisplayName("updateTrack() 성공 - tagIds 교체: 기존 전체 삭제 후 신규 저장")
    void updateTrack_success_withNewTagIds() {
        Track track = buildTrack(1L, true);
        Tag newTag = buildTag(5L, "Chill", TagType.MOOD);

        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(tagRepository.findById(5L)).willReturn(Optional.of(newTag));
        given(trackTagRepository.saveAll(any())).willReturn(List.of());

        TrackUpdateRequest request = new TrackUpdateRequest();
        request.setTagIds(List.of(5L));

        TrackResponse response = trackService.updateTrack(1L, request);

        verify(trackTagRepository).deleteAllByTrack(track);
        verify(trackTagRepository).saveAll(any());
        assertThat(response.tags()).hasSize(1);
        assertThat(response.tags().get(0).name()).isEqualTo("Chill");
    }

    // ── deleteTrack() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteTrack() 성공 - 소프트 삭제 (isActive → false)")
    void deleteTrack_success() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));

        trackService.deleteTrack(1L);

        assertThat(track.isActive()).isFalse();
    }

    @Test
    @DisplayName("deleteTrack() - track_tags 레코드를 deactivate 전에 삭제")
    void deleteTrack_deletesTrackTagsBeforeDeactivate() {
        Track track = spy(buildTrack(1L, true));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));

        trackService.deleteTrack(1L);

        org.mockito.InOrder inOrder = inOrder(trackTagRepository, track);
        inOrder.verify(trackTagRepository).deleteAllByTrack(track);
        inOrder.verify(track).deactivate();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Track buildTrack(Long id, boolean active) {
        Track track = Track.builder()
                .title("Test Track")
                .bpm(120)
                .tonality("C")
                .audioFile("tracks/audio/test.mp3")
                .user(buildUser(1L))
                .isActive(active)
                .build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private User buildUser(Long id) {
        User user = User.builder()
                .nickname("artist")
                .email("artist@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Tag buildTag(Long id, String name, TagType type) {
        Tag tag = Tag.builder().name(name).type(type).build();
        ReflectionTestUtils.setField(tag, "id", id);
        return tag;
    }

    private MultipartFile mockMultipartFile(String filename) {
        return mock(MultipartFile.class);
    }
}
