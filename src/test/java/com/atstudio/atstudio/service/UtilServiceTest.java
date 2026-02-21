package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.SubscriptionStatusResponse;
import com.atstudio.atstudio.dto.util.UserTypeResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilService 단위 테스트")
class UtilServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;

    @InjectMocks UtilService utilService;

    // ── getSubscriptionStatus() ───────────────────────────────────────────────

    @Test
    @DisplayName("getSubscriptionStatus() 구독 있음 - 구독 정보 반환")
    void getSubscriptionStatus_withSubscription() {
        User user = buildUser(1L);
        UserSubscription userSubscription = buildUserSubscription(user, 10);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(eq(user), eq(SubscriptionStatus.ACTIVE), any()))
                .willReturn(Optional.of(userSubscription));

        SubscriptionStatusResponse result = utilService.getSubscriptionStatus(buildUserDetails(1L));

        assertThat(result.hasSubscription()).isTrue();
        assertThat(result.downloadPerDay()).isEqualTo(10);
    }

    @Test
    @DisplayName("getSubscriptionStatus() 구독 없음 - noSubscription 응답 반환")
    void getSubscriptionStatus_noSubscription() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(), any()))
                .willReturn(Optional.empty());

        SubscriptionStatusResponse result = utilService.getSubscriptionStatus(buildUserDetails(1L));

        assertThat(result.hasSubscription()).isFalse();
    }

    // ── getDownloadCount() ────────────────────────────────────────────────────

    @Test
    @DisplayName("getDownloadCount() 구독 있음(유한) - 잔여 다운로드 계산")
    void getDownloadCount_withFiniteSubscription() {
        User user = buildUser(1L);
        UserSubscription userSubscription = buildUserSubscription(user, 10);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(), any()))
                .willReturn(Optional.of(userSubscription));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(3L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.todayDownloads()).isEqualTo(3L);
        assertThat(result.dailyLimit()).isEqualTo(10);
        assertThat(result.remaining()).isEqualTo(7);
    }

    @Test
    @DisplayName("getDownloadCount() 무제한 플랜 - remaining=-1 반환")
    void getDownloadCount_unlimitedSubscription() {
        User user = buildUser(1L);
        UserSubscription userSubscription = buildUserSubscription(user, -1);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(), any()))
                .willReturn(Optional.of(userSubscription));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(5L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.dailyLimit()).isEqualTo(-1);
        assertThat(result.remaining()).isEqualTo(-1);
    }

    @Test
    @DisplayName("getDownloadCount() 구독 없음 - limit=0, remaining=0 반환")
    void getDownloadCount_noSubscription() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(), any()))
                .willReturn(Optional.empty());
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(0L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.dailyLimit()).isEqualTo(0);
        assertThat(result.remaining()).isEqualTo(0);
    }

    // ── getUserType() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserType() 성공 - userType과 job 반환")
    void getUserType_success() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserTypeResponse result = utilService.getUserType(buildUserDetails(1L));

        assertThat(result.userType()).isEqualTo("INDIVIDUAL");
        assertThat(result.job()).isNull();
    }

    @Test
    @DisplayName("getUserType() 성공 - job이 있는 경우 job 반환")
    void getUserType_withJob() {
        User user = User.builder()
                .email("test@test.com").nickname("nick").password("pw")
                .userType(UserType.BUSINESS).job(UserJob.EDITOR)
                .role(UserRole.USER).build();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserTypeResponse result = utilService.getUserType(buildUserDetails(1L));

        assertThat(result.userType()).isEqualTo("BUSINESS");
        assertThat(result.job()).isEqualTo("EDITOR");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private User buildUser(Long id) {
        User user = User.builder()
                .email("test@test.com").nickname("nick").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.USER).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserSubscription buildUserSubscription(User user, int downloadPerDay) {
        Subscription plan = Subscription.builder()
                .name("Pro").userType(UserType.INDIVIDUAL)
                .downloadPerDay(downloadPerDay).maxWhitelistChannels(5)
                .priceMonthly(BigDecimal.TEN).priceYearly(BigDecimal.valueOf(100))
                .build();
        return UserSubscription.builder()
                .user(user).subscription(plan)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now()).expiresAt(LocalDate.now().plusMonths(1))
                .build();
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id).email("test@test.com").password("pw")
                .role(UserRole.USER).isDeleted(false).isProfileComplete(true)
                .build();
    }
}
