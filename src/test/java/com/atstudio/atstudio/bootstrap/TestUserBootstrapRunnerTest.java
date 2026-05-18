package com.atstudio.atstudio.bootstrap;

import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.CompanyCertificationRepository;
import com.atstudio.atstudio.repository.PlaylistRepository;
import com.atstudio.atstudio.repository.SubscriptionRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.service.PlaylistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestUserBootstrapRunner 단위 테스트")
class TestUserBootstrapRunnerTest {

    @Mock UserRepository userRepository;
    @Mock SubscriptionRepository subscriptionRepository;
    @Mock SubscriptionPaymentRepository subscriptionPaymentRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock CompanyCertificationRepository companyCertificationRepository;
    @Mock PlaylistRepository playlistRepository;
    @Mock PlaylistService playlistService;
    @Mock PasswordEncoder passwordEncoder;

    TestUserBootstrapProperties properties = new TestUserBootstrapProperties();

    @InjectMocks TestUserBootstrapRunner runner;

    @Test
    @DisplayName("run() 성공 - 누락된 QA 계정과 구독 fixture를 생성한다")
    void run_createsFixtureAccounts() throws Exception {
        ReflectionTestUtils.setField(runner, "properties", properties);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded");
        given(userRepository.findOneByEmail(any())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (ReflectionTestUtils.getField(user, "id") == null) {
                ReflectionTestUtils.setField(user, "id", System.nanoTime());
            }
            return user;
        });
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("DELUXE"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(10L, "DELUXE", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("STANDARD"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(11L, "STANDARD", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("PREMIUM"), eq(UserType.BUSINESS)))
                .willReturn(Optional.of(buildSubscription(12L, "PREMIUM", UserType.BUSINESS)));
        given(userSubscriptionRepository.findByUser(any())).willReturn(Optional.empty());
        given(userSubscriptionRepository.save(any(UserSubscription.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(playlistRepository.countByUserAndIsActiveTrue(any())).willReturn(0);
        given(companyCertificationRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(userRepository, times(5)).save(any(User.class));
        verify(userSubscriptionRepository, times(3)).save(any(UserSubscription.class));
        verify(companyCertificationRepository, times(1)).save(any());
        verify(playlistService, times(3)).createDefaultPlaylist(any(User.class));
    }

    @Test
    @DisplayName("run() 성공 - 일반 QA 계정에 활성 구독이 있으면 만료 처리한다")
    void run_expiresSubscriptionForBasicUser() throws Exception {
        ReflectionTestUtils.setField(runner, "properties", properties);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded");

        User existingUser = User.builder()
                .nickname("qa_user")
                .email("qa.user@atstudio.local")
                .password("old")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", 2L);

        UserSubscription existingSubscription = UserSubscription.builder()
                .user(existingUser)
                .subscription(buildSubscription(11L, "STANDARD", UserType.INDIVIDUAL))
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(3))
                .expiresAt(LocalDate.now().plusDays(7))
                .build();

        given(userRepository.findOneByEmail(any())).willAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return "qa.user@atstudio.local".equals(email)
                    ? Optional.of(existingUser)
                    : Optional.empty();
        });
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(userSubscriptionRepository.findByUser(any())).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return "qa.user@atstudio.local".equals(user.getEmail())
                    ? Optional.of(existingSubscription)
                    : Optional.empty();
        });
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("DELUXE"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(10L, "DELUXE", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("STANDARD"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(11L, "STANDARD", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("PREMIUM"), eq(UserType.BUSINESS)))
                .willReturn(Optional.of(buildSubscription(12L, "PREMIUM", UserType.BUSINESS)));
        given(userSubscriptionRepository.save(any(UserSubscription.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(playlistRepository.countByUserAndIsActiveTrue(any())).willReturn(0);
        given(companyCertificationRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);

        runner.run(new DefaultApplicationArguments(new String[0]));

        assertThat(existingSubscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        assertThat(existingSubscription.getExpiresAt()).isBefore(LocalDate.now());
    }

    @Test
    @DisplayName("run() 성공 - 결제 이력이 있는 QA 계정의 구독은 보존한다")
    void run_preservesPaymentManagedSubscriptionForBasicUser() throws Exception {
        ReflectionTestUtils.setField(runner, "properties", properties);
        given(passwordEncoder.encode("Test1234!")).willReturn("encoded");

        User existingUser = User.builder()
                .nickname("qa_user")
                .email("qa.user@atstudio.local")
                .password("old")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(existingUser, "id", 2L);

        UserSubscription existingSubscription = UserSubscription.builder()
                .user(existingUser)
                .subscription(buildSubscription(11L, "STANDARD", UserType.INDIVIDUAL))
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now().minusDays(3))
                .expiresAt(LocalDate.now().plusDays(7))
                .build();

        given(userRepository.findOneByEmail(any())).willAnswer(invocation -> {
            String email = invocation.getArgument(0);
            return "qa.user@atstudio.local".equals(email)
                    ? Optional.of(existingUser)
                    : Optional.empty();
        });
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(subscriptionPaymentRepository.existsByUser(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return "qa.user@atstudio.local".equals(user.getEmail());
        });
        given(userSubscriptionRepository.findByUser(any())).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return "qa.user@atstudio.local".equals(user.getEmail())
                    ? Optional.of(existingSubscription)
                    : Optional.empty();
        });
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("DELUXE"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(10L, "DELUXE", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("STANDARD"), eq(UserType.INDIVIDUAL)))
                .willReturn(Optional.of(buildSubscription(11L, "STANDARD", UserType.INDIVIDUAL)));
        given(subscriptionRepository.findByNameAndUserTypeAndIsActiveTrue(eq("PREMIUM"), eq(UserType.BUSINESS)))
                .willReturn(Optional.of(buildSubscription(12L, "PREMIUM", UserType.BUSINESS)));
        given(userSubscriptionRepository.save(any(UserSubscription.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(playlistRepository.countByUserAndIsActiveTrue(any())).willReturn(0);
        given(companyCertificationRepository.existsByUserAndStatusIn(any(), any())).willReturn(false);

        runner.run(new DefaultApplicationArguments(new String[0]));

        assertThat(existingSubscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(existingSubscription.getExpiresAt()).isAfter(LocalDate.now());
    }

    private Subscription buildSubscription(Long id, String name, UserType userType) {
        Subscription subscription = Subscription.builder()
                .name(name)
                .description(name + " plan")
                .userType(userType)
                .priceMonthly(BigDecimal.valueOf(9900))
                .priceYearly(BigDecimal.valueOf(99000))
                .downloadPerDay(10)
                .maxWhitelistChannels(2)
                .maxPlaylists(10)
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(subscription, "id", id);
        return subscription;
    }
}
