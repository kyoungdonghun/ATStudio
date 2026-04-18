package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.bootstrap.TestUserBootstrapProperties;
import com.atstudio.atstudio.dto.util.DownloadCountResponse;
import com.atstudio.atstudio.dto.util.PublicCapabilitiesResponse;
import com.atstudio.atstudio.dto.util.SubscriptionChangePreviewResponse;
import com.atstudio.atstudio.dto.util.SubscriptionStatusResponse;
import com.atstudio.atstudio.dto.util.UserTypeResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UtilService 단위 테스트")
class UtilServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock TestUserBootstrapProperties testUserBootstrapProperties;
    @Mock PasswordLoginPolicy passwordLoginPolicy;

    @InjectMocks UtilService utilService;

    // ── getSubscriptionStatus() ───────────────────────────────────────────────

    @Test
    @DisplayName("getSubscriptionStatus() 구독 있음 - 구독 정보 반환")
    void getSubscriptionStatus_withSubscription() {
        User user = buildUser(1L);
        UserSubscription userSubscription = buildUserSubscription(user, 10);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
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
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
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
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.of(userSubscription));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(3L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.todayDownloads()).isEqualTo(3L);
        assertThat(result.dailyLimit()).isEqualTo(10);
        assertThat(result.remaining()).isEqualTo(7);
        assertThat(result.nextResetAt()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("getDownloadCount() 무제한 플랜 - remaining=-1 반환")
    void getDownloadCount_unlimitedSubscription() {
        User user = buildUser(1L);
        UserSubscription userSubscription = buildUserSubscription(user, -1);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.of(userSubscription));
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(5L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.dailyLimit()).isEqualTo(-1);
        assertThat(result.remaining()).isEqualTo(-1);
        assertThat(result.nextResetAt()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
    }

    @Test
    @DisplayName("getDownloadCount() 구독 없음 - limit=0, remaining=0 반환")
    void getDownloadCount_noSubscription() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(trackDownloadRepository.countByUserAndDownloadedAtBetween(any(), any(), any()))
                .willReturn(0L);

        DownloadCountResponse result = utilService.getDownloadCount(buildUserDetails(1L));

        assertThat(result.dailyLimit()).isEqualTo(0);
        assertThat(result.remaining()).isEqualTo(0);
        assertThat(result.nextResetAt()).isEqualTo(LocalDate.now().plusDays(1).atStartOfDay());
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

    // ── getPublicCapabilities() ──────────────────────────────────────────────

    @Test
    @DisplayName("getPublicCapabilities() 설정된 OAuth/원격 SMTP 기준 capability 반환")
    void getPublicCapabilities_withConfiguredProviders() {
        given(testUserBootstrapProperties.isEnabled()).willReturn(true);
        given(passwordLoginPolicy.isEnabled()).willReturn(true);
        configureCapabilities(
                "smtp.atstudio.com",
                "google-client", "google-secret", "https://app.atstudio.com/social-login/google",
                "", "", "",
                "naver-client", "naver-secret", "https://app.atstudio.com/social-login/naver"
        );

        PublicCapabilitiesResponse result = utilService.getPublicCapabilities();

        assertThat(result.passwordLoginEnabled()).isTrue();
        assertThat(result.emailVerification().enabled()).isTrue();
        assertThat(result.emailVerification().deliveryMode()).isEqualTo("REMOTE_SMTP");
        assertThat(result.passwordReset().enabled()).isTrue();
        assertThat(result.passwordReset().deliveryMode()).isEqualTo("REMOTE_SMTP");
        assertThat(result.socialLogin().google().enabled()).isTrue();
        assertThat(result.socialLogin().google().clientId()).isEqualTo("google-client");
        assertThat(result.socialLogin().google().redirectUri())
                .isEqualTo("https://app.atstudio.com/social-login/google");
        assertThat(result.socialLogin().kakao().enabled()).isFalse();
        assertThat(result.socialLogin().kakao().clientId()).isNull();
        assertThat(result.socialLogin().naver().enabled()).isTrue();
        assertThat(result.testUsersEnabled()).isTrue();
    }

    @Test
    @DisplayName("getPublicCapabilities() 메일 host 미설정 시 password reset 비활성화")
    void getPublicCapabilities_withoutMailHost() {
        given(testUserBootstrapProperties.isEnabled()).willReturn(false);
        given(passwordLoginPolicy.isEnabled()).willReturn(true);
        configureCapabilities(
                " ",
                "", "", "",
                "", "", "",
                "", "", ""
        );

        PublicCapabilitiesResponse result = utilService.getPublicCapabilities();

        assertThat(result.emailVerification().enabled()).isFalse();
        assertThat(result.emailVerification().deliveryMode()).isEqualTo("UNCONFIGURED");
        assertThat(result.passwordReset().enabled()).isFalse();
        assertThat(result.passwordReset().deliveryMode()).isEqualTo("UNCONFIGURED");
        assertThat(result.socialLogin().google().enabled()).isFalse();
        assertThat(result.socialLogin().kakao().enabled()).isFalse();
        assertThat(result.socialLogin().naver().enabled()).isFalse();
        assertThat(result.testUsersEnabled()).isFalse();
    }

    @Test
    @DisplayName("getPublicCapabilities() 이메일 로그인 비활성화 시 password/login mail capability 모두 false")
    void getPublicCapabilities_passwordLoginDisabled() {
        given(testUserBootstrapProperties.isEnabled()).willReturn(true);
        given(passwordLoginPolicy.isEnabled()).willReturn(false);
        configureCapabilities(
                "smtp.atstudio.com",
                "google-client", "google-secret", "https://app.atstudio.com/social-login/google",
                "", "", "",
                "", "", ""
        );

        PublicCapabilitiesResponse result = utilService.getPublicCapabilities();

        assertThat(result.passwordLoginEnabled()).isFalse();
        assertThat(result.emailVerification().enabled()).isFalse();
        assertThat(result.passwordReset().enabled()).isFalse();
        assertThat(result.emailVerification().deliveryMode()).isEqualTo("REMOTE_SMTP");
        assertThat(result.socialLogin().google().enabled()).isTrue();
        assertThat(result.testUsersEnabled()).isTrue();
    }

    // ── previewSubscriptionChange() ────────────────────────────────────────────

    @Test
    @DisplayName("previewSubscriptionChange() UPGRADE - 비싼 플랜 변경 시 proratedAmount > 0, effectiveDate = today")
    void previewSubscriptionChange_upgrade() {
        User user = buildUser(1L);
        Subscription currentPlan = Subscription.builder()
                .name("Basic").userType(UserType.INDIVIDUAL)
                .downloadPerDay(5).maxWhitelistChannels(3)
                .priceMonthly(BigDecimal.valueOf(10000)).priceYearly(BigDecimal.valueOf(100000))
                .build();
        ReflectionTestUtils.setField(currentPlan, "id", 1L);

        UserSubscription currentSub = UserSubscription.builder()
                .user(user).subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now().minusDays(15)).expiresAt(LocalDate.now().plusDays(15))
                .build();

        Subscription newPlan = Subscription.builder()
                .name("Pro").userType(UserType.INDIVIDUAL)
                .downloadPerDay(20).maxWhitelistChannels(10)
                .priceMonthly(BigDecimal.valueOf(30000)).priceYearly(BigDecimal.valueOf(300000))
                .build();
        ReflectionTestUtils.setField(newPlan, "id", 2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(currentSub));
        given(subscriptionRepository.findById(2L)).willReturn(Optional.of(newPlan));

        SubscriptionChangePreviewResponse result = utilService.previewSubscriptionChange(
                buildUserDetails(1L), 2L, "MONTHLY");

        assertThat(result.changeType()).isEqualTo("UPGRADE");
        assertThat(result.proratedAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.effectiveDate()).isEqualTo(LocalDate.now());
        assertThat(result.newPlanName()).isEqualTo("Pro");
        assertThat(result.newBillingCycle()).isEqualTo("MONTHLY");
    }

    @Test
    @DisplayName("previewSubscriptionChange() DOWNGRADE - 저렴한 플랜 변경 시 proratedAmount = 0, effectiveDate = expiresAt")
    void previewSubscriptionChange_downgrade() {
        User user = buildUser(1L);
        Subscription currentPlan = Subscription.builder()
                .name("Pro").userType(UserType.INDIVIDUAL)
                .downloadPerDay(20).maxWhitelistChannels(10)
                .priceMonthly(BigDecimal.valueOf(30000)).priceYearly(BigDecimal.valueOf(300000))
                .build();
        ReflectionTestUtils.setField(currentPlan, "id", 2L);

        LocalDate expiresAt = LocalDate.now().plusDays(15);
        UserSubscription currentSub = UserSubscription.builder()
                .user(user).subscription(currentPlan)
                .billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now().minusDays(15)).expiresAt(expiresAt)
                .build();

        Subscription newPlan = Subscription.builder()
                .name("Basic").userType(UserType.INDIVIDUAL)
                .downloadPerDay(5).maxWhitelistChannels(3)
                .priceMonthly(BigDecimal.valueOf(10000)).priceYearly(BigDecimal.valueOf(100000))
                .build();
        ReflectionTestUtils.setField(newPlan, "id", 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(currentSub));
        given(subscriptionRepository.findById(1L)).willReturn(Optional.of(newPlan));

        SubscriptionChangePreviewResponse result = utilService.previewSubscriptionChange(
                buildUserDetails(1L), 1L, "MONTHLY");

        assertThat(result.changeType()).isEqualTo("DOWNGRADE");
        assertThat(result.proratedAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.effectiveDate()).isEqualTo(expiresAt);
        assertThat(result.newPlanName()).isEqualTo("Basic");
        assertThat(result.newBillingCycle()).isEqualTo("MONTHLY");
    }

    @Test
    @DisplayName("previewSubscriptionChange() 활성 구독 없음 - BusinessException 발생")
    void previewSubscriptionChange_noSubscription() {
        User user = buildUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(), any(LocalDate.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> utilService.previewSubscriptionChange(
                buildUserDetails(1L), 1L, "MONTHLY"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION);
    }

    @Test
    @DisplayName("previewSubscriptionChange() 잘못된 billingCycle - BusinessException 발생")
    void previewSubscriptionChange_invalidBillingCycle() {
        assertThatThrownBy(() -> utilService.previewSubscriptionChange(
                buildUserDetails(1L), 1L, "INVALID"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT);

        verify(userRepository, never()).findById(any());
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

    private void configureCapabilities(
            String mailHost,
            String googleClientId,
            String googleClientSecret,
            String googleRedirectUri,
            String kakaoClientId,
            String kakaoClientSecret,
            String kakaoRedirectUri,
            String naverClientId,
            String naverClientSecret,
            String naverRedirectUri
    ) {
        ReflectionTestUtils.setField(utilService, "mailHost", mailHost);
        ReflectionTestUtils.setField(utilService, "googleClientId", googleClientId);
        ReflectionTestUtils.setField(utilService, "googleClientSecret", googleClientSecret);
        ReflectionTestUtils.setField(utilService, "googleRedirectUri", googleRedirectUri);
        ReflectionTestUtils.setField(utilService, "kakaoClientId", kakaoClientId);
        ReflectionTestUtils.setField(utilService, "kakaoClientSecret", kakaoClientSecret);
        ReflectionTestUtils.setField(utilService, "kakaoRedirectUri", kakaoRedirectUri);
        ReflectionTestUtils.setField(utilService, "naverClientId", naverClientId);
        ReflectionTestUtils.setField(utilService, "naverClientSecret", naverClientSecret);
        ReflectionTestUtils.setField(utilService, "naverRedirectUri", naverRedirectUri);
    }
}
