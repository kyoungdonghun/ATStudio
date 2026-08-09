package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.track.*;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.repository.spec.TrackSpecification;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.audio.AudioAnalysisException;
import com.atstudio.atstudio.service.audio.AudioAnalysisFormat;
import com.atstudio.atstudio.service.audio.AudioAnalysisResult;
import com.atstudio.atstudio.service.audio.AudioAnalysisService;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import com.atstudio.atstudio.service.storage.StorageWriteRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
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
    @Mock AudioAnalysisService audioAnalysisService;
    @Mock CanonicalImageService canonicalImageService;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;
    @Mock CustomUserDetails userDetails;
    @Mock LikeRepository likeRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock LicenseRepository licenseRepository;
    @Mock PlaylistTrackRepository playlistTrackRepository;
    @Mock AlbumTrackRepository albumTrackRepository;

    @InjectMocks TrackService trackService;

    // ── createTrack() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createTrack() 성공 - audioFile만, thumbnail·tags 없음")
    void createTrack_success_audioFileOnly() {
        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        given(storageMutationCoordinator.writeAll(
                eq(StorageDomain.TRACK), eq(StorageRoot.PUBLIC), anyList()))
                .willReturn(List.of("tracks/audio/test.mp3"));
        given(trackRepository.save(any(Track.class))).willAnswer(inv -> {
            Track t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L);
            return t;
        });

        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Test Track");
        request.setBpm(120);
        request.setTonality("C");
        MultipartFile audioFile = mockMultipartFile("test.mp3");
        given(audioAnalysisService.analyze(audioFile)).willReturn(analysis(7, "[0.100]"));

        TrackResponse response = trackService.createTrack(request, audioFile, null, userDetails);

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
        given(storageMutationCoordinator.writeAll(
                eq(StorageDomain.TRACK), eq(StorageRoot.PUBLIC), anyList()))
                .willReturn(List.of("tracks/audio/test.mp3", "tracks/thumbnail/cover.jpg"));
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
        request.setTagIds(List.of(10L));
        MultipartFile audioFile = mockMultipartFile("test.mp3");
        MultipartFile thumbnail = mockMultipartFile("cover.jpg");
        MultipartFile canonicalThumbnail = mockMultipartFile("thumbnail.jpg");
        given(audioAnalysisService.analyze(audioFile)).willReturn(analysis(7, "[0.100]"));
        given(canonicalImageService.canonicalizeSquareTrackThumbnail(thumbnail))
                .willReturn(canonicalThumbnail);

        TrackResponse response = trackService.createTrack(request, audioFile, thumbnail, userDetails);

        assertThat(response.thumbnail()).isEqualTo("tracks/thumbnail/cover.jpg");
        assertThat(response.tags()).hasSize(1);
        assertThat(response.tags().get(0).name()).isEqualTo("Happy");
        verify(storageMutationCoordinator).writeAll(
                eq(StorageDomain.TRACK),
                eq(StorageRoot.PUBLIC),
                argThat(writes -> containsCanonicalThumbnail(writes, canonicalThumbnail)));
    }

    @Test
    @DisplayName("createTrack() 비정사각형 thumbnail 거절 - 저장과 엔티티 생성 없음")
    void createTrack_nonSquareThumbnailRejectedBeforeStorage() {
        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        MultipartFile audioFile = mockMultipartFile("test.mp3");
        MultipartFile thumbnail = mockMultipartFile("wide.png");
        given(canonicalImageService.canonicalizeSquareTrackThumbnail(thumbnail))
                .willThrow(new BusinessException(BUSINESS_ERROR.TRACK_THUMBNAIL_NOT_SQUARE));

        TrackCreateRequest request = new TrackCreateRequest();
        request.setTitle("Test Track");
        request.setBpm(120);
        request.setTonality("C");

        assertThatThrownBy(() -> trackService.createTrack(request, audioFile, thumbnail, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_THUMBNAIL_NOT_SQUARE));

        verifyNoInteractions(audioAnalysisService, storageMutationCoordinator);
        verify(trackRepository, never()).save(any(Track.class));
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
        MultipartFile audioFile = mockMultipartFile("test.mp3");

        assertThatThrownBy(() -> trackService.createTrack(request, audioFile, null, userDetails))
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

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("getTracks() rejects non-positive pages before repository access")
    void getTracks_rejectsNonPositivePageBeforeRepository(int page) {
        TrackSearchRequest request = new TrackSearchRequest();
        request.setPage(page);

        assertThatThrownBy(() -> trackService.getTracks(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(trackRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101})
    @DisplayName("getTracks() rejects sizes outside 1..100 before repository access")
    void getTracks_rejectsInvalidSizeBeforeRepository(int size) {
        TrackSearchRequest request = new TrackSearchRequest();
        request.setSize(size);

        assertThatThrownBy(() -> trackService.getTracks(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(trackRepository);
    }

    @Test
    @DisplayName("getTracks() accepts size 100 and preserves 1-based pageInfo")
    @SuppressWarnings("unchecked")
    void getTracks_acceptsMaximumSize() {
        TrackSearchRequest request = new TrackSearchRequest();
        request.setPage(2);
        request.setSize(100);
        Page<Track> emptyPage = new PageImpl<>(List.of(), PageRequest.of(1, 100), 0);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

        ResponseDTO<TrackListItemResponse> result = trackService.getTracks(request);

        assertThat(result.getPageInfo().getPage()).isEqualTo(2);
        assertThat(result.getPageInfo().getSize()).isEqualTo(100);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(trackRepository).findAll(
                any(Specification.class),
                pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
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

    @Test
    @DisplayName("parseMultiTagSpec() - NFC/공백 정규화 후 중복 제거, 쉼표/# 이름은 원자값 유지")
    void parseMultiTagSpec_canonicalizesBeforeDistinctAndKeepsSpecialCharactersAtomic() {
        List<String> canonicalNames = List.of("Caf\u00E9 Beat", "Piano, Synth", "808 #Kit");
        Specification<Track> expected = mock(Specification.class);

        try (MockedStatic<TrackSpecification> specifications = mockStatic(TrackSpecification.class)) {
            specifications.when(() -> TrackSpecification.hasAllTagsWithType(
                    canonicalNames, "INSTRUMENT")).thenReturn(expected);

            Specification<Track> result = ReflectionTestUtils.invokeMethod(
                    trackService,
                    "parseMultiTagSpec",
                    List.of(
                            "  Cafe\u0301\u00A0\u00A0Beat  ",
                            "Caf\u00E9 Beat",
                            " Piano,  Synth ",
                            "808 #Kit"),
                    "INSTRUMENT");

            assertThat(result).isSameAs(expected);
            specifications.verify(() -> TrackSpecification.hasAllTagsWithType(
                    canonicalNames, "INSTRUMENT"));
        }
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

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(null, null, 1, 20);

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

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(true, null, 1, 20);

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

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(false, null, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("getTracksForAdmin() - 결과 없으면 빈 dataList 반환")
    @SuppressWarnings("unchecked")
    void getTracksForAdmin_emptyResult() {
        Page<Track> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        given(trackRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(emptyPage);

        ResponseDTO<AdminTrackListItemResponse> result = trackService.getTracksForAdmin(null, null, 1, 20);

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
        assertThat(response.audioFile()).isNull();
        assertThat(response.tags()).isEmpty();
        verify(trackRepository).findByIdWithTags(1L);
    }

    @Test
    @DisplayName("getTrackForAdmin() 성공 - 원본 오디오 저장 키 유지")
    void getTrackForAdmin_retainsOriginalAudioKey() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findByIdWithTags(1L)).willReturn(Optional.of(track));

        TrackResponse response = trackService.getTrackForAdmin(1L);

        assertThat(response.audioFile()).isEqualTo("tracks/audio/test.mp3");
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

    // ── getStreamResource() ──────────────────────────────────────────────────

    @Test
    @DisplayName("getStreamResource() - 재생시간과 무관하게 원본 리소스 전체 길이 제공")
    void getStreamResource_usesFullOriginalLengthRegardlessOfDuration() {
        Track track = buildTrack(1L, true);
        ReflectionTestUtils.setField(track, "duration", 120);
        ByteArrayResource resource = new ByteArrayResource(new byte[1_200]);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(storageService.loadAsResource(StorageRoot.PUBLIC, "tracks/audio/test.mp3"))
                .willReturn(resource);

        TrackService.StreamResource result = trackService.getStreamResource(1L);

        assertThat(result.resource()).isSameAs(resource);
        assertThat(result.publicLength()).isEqualTo(1_200L);
    }

    @Test
    @DisplayName("getStreamResource() - 1바이트 원본도 전체 길이 제공")
    void getStreamResource_oneByteOriginalUsesFullLength() {
        Track track = buildTrack(1L, true);
        ByteArrayResource resource = new ByteArrayResource(new byte[1]);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(storageService.loadAsResource(StorageRoot.PUBLIC, "tracks/audio/test.mp3"))
                .willReturn(resource);

        TrackService.StreamResource result = trackService.getStreamResource(1L);

        assertThat(result.publicLength()).isOne();
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

        TrackResponse response = trackService.updateTrack(1L, request, null, null);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.bpm()).isEqualTo(140);
        assertThat(response.duration()).isEqualTo(120);
        assertThat(response.waveformData()).isEqualTo("[0.500]");
        verifyNoInteractions(storageMutationCoordinator);
        verifyNoInteractions(audioAnalysisService);
    }

    @Test
    @DisplayName("updateTrack() 성공 - 새 audioFile 교체: 기존 삭제 + 신규 저장")
    void updateTrack_success_withNewAudioFile() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(storageMutationCoordinator.replace(
                eq(StorageDomain.TRACK),
                eq(StorageRoot.PUBLIC),
                any(),
                eq("tracks/audio"),
                eq("tracks/audio/test.mp3")))
                .willReturn("tracks/audio/new.mp3");
        given(trackTagRepository.findAllWithTagByTrack(any(Track.class))).willReturn(List.of());

        TrackUpdateRequest request = new TrackUpdateRequest();
        MultipartFile newAudioFile = mockMultipartFile("new.mp3");
        given(audioAnalysisService.analyze(newAudioFile)).willReturn(analysis(7, "[0.900]"));

        TrackResponse response = trackService.updateTrack(1L, request, newAudioFile, null);

        assertThat(response.audioFile()).isEqualTo("tracks/audio/new.mp3");
        assertThat(response.duration()).isEqualTo(7);
        assertThat(response.waveformData()).isEqualTo("[0.900]");
        verify(storageMutationCoordinator).replace(
                eq(StorageDomain.TRACK),
                eq(StorageRoot.PUBLIC),
                any(),
                eq("tracks/audio"),
                eq("tracks/audio/test.mp3"));
    }

    @Test
    @DisplayName("updateTrack() 분석 실패 - 기존 파일·duration·waveform 보존")
    void updateTrack_analysisFailurePreservesExistingAudioMetadata() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        MultipartFile newAudioFile = mockMultipartFile("broken.mp3");
        given(audioAnalysisService.analyze(newAudioFile)).willThrow(new AudioAnalysisException(
                AudioAnalysisException.Reason.INVALID_AUDIO,
                AudioAnalysisFormat.MP3));

        assertThatThrownBy(() -> trackService.updateTrack(
                1L,
                new TrackUpdateRequest(),
                newAudioFile,
                null))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.AUDIO_ANALYSIS_FAILED));

        assertThat(track.getAudioFile()).isEqualTo("tracks/audio/test.mp3");
        assertThat(track.getDuration()).isEqualTo(120);
        assertThat(track.getWaveformData()).isEqualTo("[0.500]");
        verifyNoInteractions(storageMutationCoordinator);
    }

    @Test
    @DisplayName("updateTrack() 성공 - canonical thumbnail로 기존 파일 교체")
    void updateTrack_success_withCanonicalThumbnail() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(trackTagRepository.findAllWithTagByTrack(track)).willReturn(List.of());
        MultipartFile thumbnail = mockMultipartFile("square.png");
        MultipartFile canonicalThumbnail = mockMultipartFile("thumbnail.jpg");
        given(canonicalImageService.canonicalizeSquareTrackThumbnail(thumbnail))
                .willReturn(canonicalThumbnail);
        given(storageMutationCoordinator.replace(
                StorageDomain.TRACK,
                StorageRoot.PUBLIC,
                canonicalThumbnail,
                "tracks/thumbnail",
                "tracks/thumbnail/original.jpg"))
                .willReturn("tracks/thumbnail/replacement.jpg");

        TrackResponse response = trackService.updateTrack(
                1L,
                new TrackUpdateRequest(),
                null,
                thumbnail);

        assertThat(response.thumbnail()).isEqualTo("tracks/thumbnail/replacement.jpg");
        verify(storageMutationCoordinator).replace(
                StorageDomain.TRACK,
                StorageRoot.PUBLIC,
                canonicalThumbnail,
                "tracks/thumbnail",
                "tracks/thumbnail/original.jpg");
    }

    @Test
    @DisplayName("updateTrack() 비정사각형 thumbnail 거절 - 저장·필드·태그 변경 없음")
    void updateTrack_nonSquareThumbnailRejectedWithoutPartialMutation() {
        Track track = buildTrack(1L, true);
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        MultipartFile audioFile = mockMultipartFile("replacement.mp3");
        MultipartFile thumbnail = mockMultipartFile("wide.png");
        given(canonicalImageService.canonicalizeSquareTrackThumbnail(thumbnail))
                .willThrow(new BusinessException(BUSINESS_ERROR.TRACK_THUMBNAIL_NOT_SQUARE));
        TrackUpdateRequest request = new TrackUpdateRequest();
        request.setTitle("Rejected Title");
        request.setBpm(140);
        request.setIsActive(false);
        request.setTagIds(List.of(5L));

        assertThatThrownBy(() -> trackService.updateTrack(1L, request, audioFile, thumbnail))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_THUMBNAIL_NOT_SQUARE));

        assertThat(track.getTitle()).isEqualTo("Test Track");
        assertThat(track.getBpm()).isEqualTo(120);
        assertThat(track.getThumbnail()).isEqualTo("tracks/thumbnail/original.jpg");
        assertThat(track.isActive()).isTrue();
        verifyNoInteractions(audioAnalysisService, storageMutationCoordinator, trackTagRepository);
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

        TrackResponse response = trackService.updateTrack(1L, request, null, null);

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
        verifyNoInteractions(storageMutationCoordinator);
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
                .thumbnail("tracks/thumbnail/original.jpg")
                .duration(120)
                .waveformData("[0.500]")
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

    private boolean containsCanonicalThumbnail(
            List<StorageWriteRequest> writes,
            MultipartFile canonicalThumbnail) {
        return writes.size() == 2
                && writes.get(0).directory().equals("tracks/audio")
                && writes.get(1).directory().equals("tracks/thumbnail")
                && writes.get(1).file() == canonicalThumbnail;
    }

    private AudioAnalysisResult analysis(int duration, String waveform) {
        return new AudioAnalysisResult(
                duration,
                waveform,
                AudioAnalysisFormat.MP3,
                duration * 44_100L,
                44_100,
                2);
    }
}
