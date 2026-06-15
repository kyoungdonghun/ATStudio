package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelRequest;
import com.atstudio.atstudio.dto.whitelist.WhitelistChannelResponse;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WhitelistChannelService 단위 테스트")
class WhitelistChannelServiceTest {

    @Mock WhitelistChannelRepository whitelistChannelRepository;
    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;

    @InjectMocks WhitelistChannelService whitelistChannelService;

    // ── 12.1 registerChannel ─────────────────────────────────────────────────

    @Nested
    @DisplayName("registerChannel()")
    class RegisterChannel {

        @Test
        @DisplayName("성공 - 정상 채널 등록")
        void success() {
            User user = buildUser(1L, UserRole.USER);
            Subscription plan = buildSubscription(5);
            UserSubscription sub = buildUserSubscription(user, plan);
            WhitelistChannel saved = buildChannel(1L, user,
                    "https://youtube.com/@channel1", "채널1");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(whitelistChannelRepository.countByUser(user)).willReturn(0L);
            given(whitelistChannelRepository.save(any(WhitelistChannel.class))).willReturn(saved);

            WhitelistChannelResponse result = whitelistChannelService.registerChannel(
                    buildUserDetails(1L, UserRole.USER),
                    new WhitelistChannelRequest("https://youtube.com/@channel1", "채널1"));

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.channelUrl()).isEqualTo("https://youtube.com/@channel1");
            assertThat(result.channelName()).isEqualTo("채널1");
            assertThat(result.status()).isEqualTo(WhitelistChannelStatus.DRAFT);
        }

        @Test
        @DisplayName("실패 - 비 YouTube 도메인 → INVALID_ARGUMENT")
        void fail_invalidUrl() {
            assertThatThrownBy(() -> whitelistChannelService.registerChannel(
                    buildUserDetails(1L, UserRole.USER),
                    new WhitelistChannelRequest("https://vimeo.com/@channel1", "채널1")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        }

        @Test
        @DisplayName("실패 - 유사 도메인 (notarealsite-youtube.com) → INVALID_ARGUMENT")
        void fail_spoofedDomain() {
            assertThatThrownBy(() -> whitelistChannelService.registerChannel(
                    buildUserDetails(1L, UserRole.USER),
                    new WhitelistChannelRequest("https://notarealsite-youtube.com/@ch", "채널")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        }

        @Test
        @DisplayName("실패 - 잘못된 URL 형식 → INVALID_ARGUMENT")
        void fail_malformedUrl() {
            assertThatThrownBy(() -> whitelistChannelService.registerChannel(
                    buildUserDetails(1L, UserRole.USER),
                    new WhitelistChannelRequest("not a valid url :::", "채널")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
        }

        @Test
        @DisplayName("성공 - www.youtube.com 서브도메인 허용")
        void success_wwwSubdomain() {
            User user = buildUser(1L, UserRole.USER);
            Subscription plan = buildSubscription(5);
            UserSubscription sub = buildUserSubscription(user, plan);
            WhitelistChannel saved = buildChannel(1L, user,
                    "https://www.youtube.com/channel/xxx", "채널WWW");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(whitelistChannelRepository.countByUser(user)).willReturn(0L);
            given(whitelistChannelRepository.save(any(WhitelistChannel.class))).willReturn(saved);

            WhitelistChannelResponse result = whitelistChannelService.registerChannel(
                    buildUserDetails(1L, UserRole.USER),
                    new WhitelistChannelRequest("https://www.youtube.com/channel/xxx", "채널WWW"));

            assertThat(result.channelUrl()).isEqualTo("https://www.youtube.com/channel/xxx");
        }

        @Test
        @DisplayName("실패 - 등록 요청 시 활성 구독 없음 → NO_ACTIVE_SUBSCRIPTION")
        void fail_noSubscription() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@channel1", "채널1");
            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> whitelistChannelService.requestRegistration(
                    buildUserDetails(1L, UserRole.USER), 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }

        @Test
        @DisplayName("실패 - 등록 요청 시 채널 한도 초과 → WHITELIST_CHANNEL_LIMIT_EXCEEDED")
        void fail_limitExceeded() {
            User user = buildUser(1L, UserRole.USER);
            Subscription plan = buildSubscription(3);
            UserSubscription sub = buildUserSubscription(user, plan);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@channel4", "채널4");

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(sub));
            given(whitelistChannelRepository.countByUserAndStatusIn(eq(user), any()))
                    .willReturn(3L);

            assertThatThrownBy(() -> whitelistChannelService.requestRegistration(
                    buildUserDetails(1L, UserRole.USER), 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.WHITELIST_CHANNEL_LIMIT_EXCEEDED));
        }

        @Test
        @DisplayName("success - revision requested channel can be requested again without consuming a new slot")
        void success_revisionRequestedCanRequestAgain() {
            User user = buildUser(1L, UserRole.USER);
            Subscription plan = buildSubscription(1);
            UserSubscription sub = buildUserSubscription(user, plan);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@channel1", "channel1");
            ReflectionTestUtils.setField(channel, "status", WhitelistChannelStatus.REVISION_REQUESTED);

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(sub));
            given(whitelistChannelRepository.countByUserAndStatusIn(eq(user), any()))
                    .willReturn(1L);

            WhitelistChannelResponse result = whitelistChannelService.requestRegistration(
                    buildUserDetails(1L, UserRole.USER), 10L);

            assertThat(result.status()).isEqualTo(WhitelistChannelStatus.PENDING);
            assertThat(channel.getRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("fail - registered channel cannot be requested again directly")
        void fail_registeredCannotRequestAgainDirectly() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@channel1", "channel1");
            ReflectionTestUtils.setField(channel, "status", WhitelistChannelStatus.REGISTERED);

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));

            assertThatThrownBy(() -> whitelistChannelService.requestRegistration(
                    buildUserDetails(1L, UserRole.USER), 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
        }
    }

    // ── 12.2 getMyChannels ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyChannels()")
    class GetMyChannels {

        @Test
        @DisplayName("성공 - 내 채널 목록 조회")
        void success() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel ch1 = buildChannel(1L, user, "https://youtube.com/@ch1", "채널1");
            WhitelistChannel ch2 = buildChannel(2L, user, "https://youtube.com/@ch2", "채널2");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(whitelistChannelRepository.findByUserOrderByPrimaryDescCreatedAtDesc(user))
                    .willReturn(List.of(ch1, ch2));

            List<WhitelistChannelResponse> result = whitelistChannelService.getMyChannels(
                    buildUserDetails(1L, UserRole.USER));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).channelName()).isEqualTo("채널1");
            assertThat(result.get(1).channelName()).isEqualTo("채널2");
        }
    }

    // ── 12.3 updateChannel ───────────────────────────────────────────────────

    @Nested
    @DisplayName("updateChannel()")
    class UpdateChannel {

        @Test
        @DisplayName("성공 - 정상 수정")
        void success() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@old", "이전채널");

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));

            WhitelistChannelResponse result = whitelistChannelService.updateChannel(
                    buildUserDetails(1L, UserRole.USER), 10L,
                    new WhitelistChannelRequest("https://youtube.com/@new", "새채널"));

            assertThat(result.channelUrl()).isEqualTo("https://youtube.com/@new");
            assertThat(result.channelName()).isEqualTo("새채널");
        }

        @Test
        @DisplayName("success - processed channel update requeues with the existing counted slot")
        void success_processedChannelUpdateRequeuesWithExistingSlot() {
            User user = buildUser(1L, UserRole.USER);
            Subscription plan = buildSubscription(1);
            UserSubscription sub = buildUserSubscription(user, plan);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@old", "old");
            ReflectionTestUtils.setField(channel, "status", WhitelistChannelStatus.REGISTERED);

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.of(sub));
            given(whitelistChannelRepository.countByUserAndStatusIn(eq(user), any()))
                    .willReturn(1L);

            WhitelistChannelResponse result = whitelistChannelService.updateChannel(
                    buildUserDetails(1L, UserRole.USER), 10L,
                    new WhitelistChannelRequest("https://youtube.com/@new", "new"));

            assertThat(result.status()).isEqualTo(WhitelistChannelStatus.PENDING);
            assertThat(channel.getRequestedAt()).isNotNull();
        }

        @Test
        @DisplayName("fail - processed channel update cannot requeue without active subscription")
        void fail_processedChannelUpdateWithoutSubscription() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@old", "old");
            ReflectionTestUtils.setField(channel, "status", WhitelistChannelStatus.REGISTERED);

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));
            given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> whitelistChannelService.updateChannel(
                    buildUserDetails(1L, UserRole.USER), 10L,
                    new WhitelistChannelRequest("https://youtube.com/@new", "new")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
        }

        @Test
        @DisplayName("실패 - 타인 채널 수정 → RESOURCE_NOT_ACCESS")
        void fail_notOwner() {
            User owner = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, owner,
                    "https://youtube.com/@ch", "채널");

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));

            assertThatThrownBy(() -> whitelistChannelService.updateChannel(
                    buildUserDetails(50L, UserRole.USER), 10L,
                    new WhitelistChannelRequest("https://youtube.com/@new", "새채널")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }

        @Test
        @DisplayName("실패 - 채널 미존재 → RESOURCE_NOT_FOUND")
        void fail_notFound() {
            given(whitelistChannelRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> whitelistChannelService.updateChannel(
                    buildUserDetails(1L, UserRole.USER), 99L,
                    new WhitelistChannelRequest("https://youtube.com/@ch", "채널")))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        }
    }

    // ── 12.6 deleteChannel ───────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteChannel()")
    class DeleteChannel {

        @Test
        @DisplayName("성공 - 정상 삭제")
        void success() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, user,
                    "https://youtube.com/@ch", "채널");

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));

            whitelistChannelService.deleteChannel(buildUserDetails(1L, UserRole.USER), 10L);

            verify(whitelistChannelRepository).delete(channel);
        }

        @Test
        @DisplayName("success - deleting the primary channel promotes another channel")
        void success_promotesReplacementPrimaryOnDelete() {
            User user = buildUser(1L, UserRole.USER);
            WhitelistChannel primary = buildChannel(10L, user,
                    "https://youtube.com/@primary", "primary");
            WhitelistChannel replacement = buildChannel(11L, user,
                    "https://youtube.com/@replacement", "replacement");
            primary.setPrimary(true);

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(primary));
            given(whitelistChannelRepository.findByUserOrderByCreatedAtDesc(user))
                    .willReturn(List.of(primary, replacement));

            whitelistChannelService.deleteChannel(buildUserDetails(1L, UserRole.USER), 10L);

            assertThat(replacement.isPrimary()).isTrue();
            verify(whitelistChannelRepository).delete(primary);
        }

        @Test
        @DisplayName("실패 - 타인 채널 삭제 → RESOURCE_NOT_ACCESS")
        void fail_notOwner() {
            User owner = buildUser(1L, UserRole.USER);
            WhitelistChannel channel = buildChannel(10L, owner,
                    "https://youtube.com/@ch", "채널");

            given(whitelistChannelRepository.findById(10L)).willReturn(Optional.of(channel));

            assertThatThrownBy(() -> whitelistChannelService.deleteChannel(
                    buildUserDetails(50L, UserRole.USER), 10L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildUser(Long id, UserRole role) {
        User user = User.builder()
                .email("user" + id + "@test.com").nickname("user" + id).password("pw")
                .userType(UserType.INDIVIDUAL).role(role).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Subscription buildSubscription(int maxChannels) {
        Subscription subscription = Subscription.builder()
                .name("Basic").maxWhitelistChannels(maxChannels).build();
        ReflectionTestUtils.setField(subscription, "id", 1L);
        return subscription;
    }

    private UserSubscription buildUserSubscription(User user, Subscription subscription) {
        return UserSubscription.builder()
                .user(user).subscription(subscription)
                .status(SubscriptionStatus.ACTIVE).build();
    }

    private WhitelistChannel buildChannel(Long id, User user, String url, String name) {
        WhitelistChannel channel = WhitelistChannel.builder()
                .user(user).channelUrl(url).channelName(name).build();
        ReflectionTestUtils.setField(channel, "id", id);
        return channel;
    }

    private CustomUserDetails buildUserDetails(Long id, UserRole role) {
        return CustomUserDetails.builder()
                .id(id).email("user" + id + "@test.com").password("pw")
                .role(role).isDeleted(false).isProfileComplete(true)
                .build();
    }
}
