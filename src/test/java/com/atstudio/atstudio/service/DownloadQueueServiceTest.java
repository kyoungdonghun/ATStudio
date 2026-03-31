package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.downloadqueue.DownloadQueueResponse;
import com.atstudio.atstudio.entity.DownloadQueue;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.key.DownloadQueueId;
import com.atstudio.atstudio.repository.DownloadQueueRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadQueueService 단위 테스트")
class DownloadQueueServiceTest {

    @Mock DownloadQueueRepository downloadQueueRepository;
    @Mock UserRepository userRepository;
    @Mock TrackRepository trackRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks DownloadQueueService downloadQueueService;

    // ── @Transactional 검증 ───────────────────────────────────────────────────

    @Test
    @DisplayName("DownloadQueueService 클래스 레벨 @Transactional(readOnly=true) 존재 확인")
    void classLevel_transactionalReadOnly() {
        Transactional annotation = DownloadQueueService.class.getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }

    @Test
    @DisplayName("addToQueue() 메서드 레벨 @Transactional override 존재 확인")
    void addToQueue_hasTransactional() throws NoSuchMethodException {
        Transactional annotation = DownloadQueueService.class
                .getMethod("addToQueue", Long.class, CustomUserDetails.class)
                .getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isFalse();
    }

    @Test
    @DisplayName("removeFromQueue() 메서드 레벨 @Transactional override 존재 확인")
    void removeFromQueue_hasTransactional() throws NoSuchMethodException {
        Transactional annotation = DownloadQueueService.class
                .getMethod("removeFromQueue", Long.class, CustomUserDetails.class)
                .getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isFalse();
    }

    // ── addToQueue() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("addToQueue() 성공 - 다운로드 큐에 추가")
    void addToQueue_success() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.of(mock(com.atstudio.atstudio.entity.UserSubscription.class)));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(downloadQueueRepository.existsById(any(DownloadQueueId.class))).willReturn(false);

        downloadQueueService.addToQueue(2L, buildUserDetails(1L));

        verify(downloadQueueRepository).save(any(DownloadQueue.class));
    }

    @Test
    @DisplayName("addToQueue() 실패 - 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
    void addToQueue_noSubscription() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadQueueService.addToQueue(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
    }

    @Test
    @DisplayName("addToQueue() 성공 - ADMIN은 구독 없이도 추가 가능")
    void addToQueue_adminBypass() {
        User admin = buildUser(1L);
        ReflectionTestUtils.setField(admin, "role", UserRole.ADMIN);
        Track track = buildTrack(2L, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(admin));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(downloadQueueRepository.existsById(any(DownloadQueueId.class))).willReturn(false);

        CustomUserDetails adminDetails = CustomUserDetails.builder()
                .id(1L).email("admin@test.com").password("pw")
                .role(UserRole.ADMIN).isDeleted(false).isProfileComplete(true)
                .build();
        downloadQueueService.addToQueue(2L, adminDetails);

        verify(downloadQueueRepository).save(any(DownloadQueue.class));
    }

    @Test
    @DisplayName("addToQueue() 실패 - 이미 큐에 있음 → RESOURCE_DUPLICATE 예외(409)")
    void addToQueue_duplicate() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.of(mock(com.atstudio.atstudio.entity.UserSubscription.class)));
        given(trackRepository.findById(2L)).willReturn(Optional.of(track));
        given(downloadQueueRepository.existsById(any(DownloadQueueId.class))).willReturn(true);

        assertThatThrownBy(() -> downloadQueueService.addToQueue(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
    }

    @Test
    @DisplayName("addToQueue() 실패 - 트랙 없음/비활성 → TRACK_NOT_FOUND 예외")
    void addToQueue_trackNotFound() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.of(mock(com.atstudio.atstudio.entity.UserSubscription.class)));
        given(trackRepository.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadQueueService.addToQueue(2L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    // ── getMyQueue() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyQueue() 성공 - 다운로드 큐 목록 반환")
    void getMyQueue_success() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(downloadQueueRepository.findAllByUser(user)).willReturn(List.of());

        List<DownloadQueueResponse> result = downloadQueueService.getMyQueue(buildUserDetails(1L));

        assertThat(result).isEmpty();
    }

    // ── removeFromQueue() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("removeFromQueue() 성공 - 큐에서 삭제")
    void removeFromQueue_success() {
        User user = buildUser(1L);
        Track track = buildTrack(2L, true);
        DownloadQueue queue = DownloadQueue.builder()
                .id(new DownloadQueueId(1L, 2L)).user(user).track(track).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(downloadQueueRepository.findByUserAndTrack_Id(user, 2L)).willReturn(Optional.of(queue));

        downloadQueueService.removeFromQueue(2L, buildUserDetails(1L));

        verify(downloadQueueRepository).delete(queue);
    }

    @Test
    @DisplayName("removeFromQueue() 실패 - 큐에 없음 → RESOURCE_NOT_FOUND 예외")
    void removeFromQueue_notFound() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(downloadQueueRepository.findByUserAndTrack_Id(user, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadQueueService.removeFromQueue(2L, buildUserDetails(1L)))
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
