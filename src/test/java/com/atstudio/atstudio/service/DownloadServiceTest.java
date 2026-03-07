package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadService 단위 테스트")
class DownloadServiceTest {

    @Mock TrackRepository trackRepository;
    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock LicenseRepository licenseRepository;
    @Mock StorageService storageService;
    @Mock CustomUserDetails userDetails;

    @InjectMocks DownloadService downloadService;

    // ── download() 성공 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("download() 성공 - 신규 라이선스 발급")
    void download_success_newLicense() {
        User user = buildUser(1L);
        Track track = buildTrack(1L, true);
        Resource mockResource = mock(Resource.class);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(5)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(2L);
        given(trackDownloadRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(licenseRepository.findByUserAndTrack(user, track)).willReturn(Optional.empty());
        given(licenseRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(storageService.loadAsResource(anyString())).willReturn(mockResource);

        Resource result = downloadService.download(1L, userDetails);

        assertThat(result).isEqualTo(mockResource);
        verify(trackDownloadRepository).save(any(TrackDownload.class));
        verify(licenseRepository).save(any(License.class));
    }

    @Test
    @DisplayName("download() 성공 - 기존 라이선스 있으면 재발급 안 함")
    void download_success_existingLicense() {
        User user = buildUser(1L);
        Track track = buildTrack(1L, true);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(5)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(0L);
        given(trackDownloadRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(licenseRepository.findByUserAndTrack(user, track)).willReturn(Optional.of(buildLicense(user, track)));
        given(storageService.loadAsResource(anyString())).willReturn(mock(Resource.class));

        downloadService.download(1L, userDetails);

        verify(licenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("download() 성공 - 무제한 플랜(downloadPerDay=-1) todayCount=0 → 다운로드 허용")
    void download_success_unlimitedPlan_zeroCount() {
        User user = buildUser(1L);
        Track track = buildTrack(1L, true);
        Resource mockResource = mock(Resource.class);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(-1)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(0L);
        given(trackDownloadRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(licenseRepository.findByUserAndTrack(user, track)).willReturn(Optional.empty());
        given(licenseRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(storageService.loadAsResource(anyString())).willReturn(mockResource);

        Resource result = downloadService.download(1L, userDetails);

        assertThat(result).isEqualTo(mockResource);
        verify(trackDownloadRepository).save(any(TrackDownload.class));
    }

    @Test
    @DisplayName("download() 성공 - 무제한 플랜(downloadPerDay=-1) todayCount=1000 → 다운로드 허용")
    void download_success_unlimitedPlan_highUsage() {
        User user = buildUser(1L);
        Track track = buildTrack(1L, true);
        Resource mockResource = mock(Resource.class);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(-1)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(1000L);
        given(trackDownloadRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(licenseRepository.findByUserAndTrack(user, track)).willReturn(Optional.empty());
        given(licenseRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(storageService.loadAsResource(anyString())).willReturn(mockResource);

        Resource result = downloadService.download(1L, userDetails);

        assertThat(result).isEqualTo(mockResource);
        verify(trackDownloadRepository).save(any(TrackDownload.class));
    }

    @Test
    @DisplayName("download() 성공 - 제한 플랜(downloadPerDay=5) todayCount=4 → 다운로드 허용")
    void download_success_limitedPlan_underLimit() {
        User user = buildUser(1L);
        Track track = buildTrack(1L, true);
        Resource mockResource = mock(Resource.class);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(track));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(5)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(4L);
        given(trackDownloadRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(licenseRepository.findByUserAndTrack(user, track)).willReturn(Optional.empty());
        given(licenseRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(storageService.loadAsResource(anyString())).willReturn(mockResource);

        Resource result = downloadService.download(1L, userDetails);

        assertThat(result).isEqualTo(mockResource);
        verify(trackDownloadRepository).save(any(TrackDownload.class));
    }

    @Test
    @DisplayName("download() 실패 - downloadPerDay=0 플랜 → 즉시 차단 (DOWNLOAD_LIMIT_EXCEEDED)")
    void downloadLimited_zeroPlan_blocks() {
        User user = buildUser(1L);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(buildTrack(1L, true)));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(0)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(0L);

        assertThatThrownBy(() -> downloadService.download(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.DOWNLOAD_LIMIT_EXCEEDED));
    }

    // ── download() 실패 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("download() 실패 - 존재하지 않는 사용자 → RESOURCE_NOT_FOUND")
    void download_fail_userNotFound() {
        given(userDetails.getId()).willReturn(99L);
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadService.download(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    @Test
    @DisplayName("download() 실패 - 비활성 트랙 → TRACK_NOT_FOUND")
    void download_fail_inactiveTrack() {
        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        given(trackRepository.findById(1L)).willReturn(Optional.of(buildTrack(1L, false)));

        assertThatThrownBy(() -> downloadService.download(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.TRACK_NOT_FOUND));
    }

    @Test
    @DisplayName("download() 실패 - 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
    void download_fail_noSubscription() {
        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(buildUser(1L)));
        given(trackRepository.findById(1L)).willReturn(Optional.of(buildTrack(1L, true)));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class))).willReturn(Optional.empty());

        assertThatThrownBy(() -> downloadService.download(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
    }

    @Test
    @DisplayName("download() 실패 - 일일 한도 초과 → DOWNLOAD_LIMIT_EXCEEDED")
    void download_fail_limitExceeded() {
        User user = buildUser(1L);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(trackRepository.findById(1L)).willReturn(Optional.of(buildTrack(1L, true)));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(buildSubscription(5)));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(eq(user), any(), any())).willReturn(5L);

        assertThatThrownBy(() -> downloadService.download(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.DOWNLOAD_LIMIT_EXCEEDED));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User buildUser(Long id) {
        User user = User.builder().nickname("artist").email("artist@test.com").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Track buildTrack(Long id, boolean active) {
        Track track = Track.builder()
                .title("Test Track").bpm(120).tonality("C")
                .audioFile("tracks/audio/test.mp3")
                .user(buildUser(1L)).isActive(active).build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private UserSubscription buildSubscription(int downloadPerDay) {
        Subscription sub = Subscription.builder()
                .name("BASIC")
                .userType(UserType.INDIVIDUAL)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(downloadPerDay)
                .maxWhitelistChannels(1)
                .build();
        return UserSubscription.builder()
                .user(buildUser(1L))
                .subscription(sub)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
    }

    private License buildLicense(User user, Track track) {
        License license = License.builder()
                .user(user).track(track).licenseCode("existing-code").build();
        ReflectionTestUtils.setField(license, "id", 1L);
        return license;
    }
}
