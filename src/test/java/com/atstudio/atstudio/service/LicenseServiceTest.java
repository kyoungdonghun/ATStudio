package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.license.LicenseListItemResponse;
import com.atstudio.atstudio.dto.license.LicenseResponse;
import com.atstudio.atstudio.entity.License;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.LicenseRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("LicenseService 단위 테스트")
class LicenseServiceTest {

    @Mock LicenseRepository licenseRepository;
    @Mock UserRepository userRepository;
    @Mock CustomUserDetails userDetails;

    @InjectMocks LicenseService licenseService;

    // ── getMyLicenses() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyLicenses() - 라이선스 목록 반환")
    void getMyLicenses_success() {
        User user = buildUser(1L);
        License license = buildLicense(1L, user, buildTrack(10L));
        Page<License> page = new PageImpl<>(List.of(license), PageRequest.of(0, 20), 1);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(licenseRepository.findAllByUser(eq(user), any())).willReturn(page);

        ResponseDTO<LicenseListItemResponse> result = licenseService.getMyLicenses(userDetails, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).licenseCode()).isEqualTo("test-code");
        assertThat(result.getPageInfo().getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMyLicenses() - 빈 목록")
    void getMyLicenses_empty() {
        User user = buildUser(1L);
        Page<License> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        given(userDetails.getId()).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(licenseRepository.findAllByUser(eq(user), any())).willReturn(emptyPage);

        ResponseDTO<LicenseListItemResponse> result = licenseService.getMyLicenses(userDetails, 1, 20);

        assertThat(result.getDataList()).isEmpty();
        assertThat(result.getPageInfo().getTotal()).isZero();
    }

    // ── getMyLicense() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyLicense() 성공 - 소유자 조회")
    void getMyLicense_success() {
        User user = buildUser(1L);
        License license = buildLicense(1L, user, buildTrack(10L));

        given(userDetails.getId()).willReturn(1L);
        given(licenseRepository.findByIdAndUser_Id(1L, 1L)).willReturn(Optional.of(license));

        LicenseResponse result = licenseService.getMyLicense(1L, userDetails);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.licenseCode()).isEqualTo("test-code");
        assertThat(result.track().id()).isEqualTo(10L);
        assertThat(result.user().id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getMyLicense() 실패 - 타인 라이선스 → RESOURCE_NOT_FOUND")
    void getMyLicense_fail_notOwner() {
        given(userDetails.getId()).willReturn(2L);
        given(licenseRepository.findByIdAndUser_Id(1L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> licenseService.getMyLicense(1L, userDetails))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── getUserLicenses() / getUserLicense() ──────────────────────────────────

    @Test
    @DisplayName("getUserLicenses() - 관리자 목록 조회")
    void getUserLicenses_success() {
        User user = buildUser(1L);
        License license = buildLicense(1L, user, buildTrack(10L));
        Page<License> page = new PageImpl<>(List.of(license), PageRequest.of(0, 20), 1);

        given(licenseRepository.findAllByUser_Id(eq(1L), any())).willReturn(page);

        ResponseDTO<LicenseListItemResponse> result = licenseService.getUserLicenses(1L, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getPageInfo().getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("getUserLicense() 성공 - 관리자 상세 조회")
    void getUserLicense_success() {
        User user = buildUser(1L);
        License license = buildLicense(1L, user, buildTrack(10L));

        given(licenseRepository.findByIdAndUser_Id(1L, 1L)).willReturn(Optional.of(license));

        LicenseResponse result = licenseService.getUserLicense(1L, 1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.user().nickname()).isEqualTo("artist");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User buildUser(Long id) {
        User user = User.builder().nickname("artist").email("artist@test.com").build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Track buildTrack(Long id) {
        Track track = Track.builder()
                .title("Test Track").bpm(120).tonality("C")
                .audioFile("tracks/audio/test.mp3")
                .user(buildUser(1L)).build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private License buildLicense(Long id, User user, Track track) {
        License license = License.builder()
                .user(user).track(track).licenseCode("test-code").build();
        ReflectionTestUtils.setField(license, "id", id);
        return license;
    }
}
