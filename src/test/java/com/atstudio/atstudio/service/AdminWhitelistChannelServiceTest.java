package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.entity.Subscription;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.UserSubscription;
import com.atstudio.atstudio.entity.WhitelistChannel;
import com.atstudio.atstudio.entity.WhitelistExportBatch;
import com.atstudio.atstudio.entity.WhitelistExportItem;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.SubscriptionStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.enums.WhitelistChannelStatus;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.UserSubscriptionRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.repository.WhitelistExportBatchRepository;
import com.atstudio.atstudio.repository.WhitelistExportItemRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminWhitelistChannelService unit tests")
class AdminWhitelistChannelServiceTest {

    @Mock WhitelistChannelRepository whitelistChannelRepository;
    @Mock WhitelistExportBatchRepository whitelistExportBatchRepository;
    @Mock WhitelistExportItemRepository whitelistExportItemRepository;
    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;

    AdminWhitelistChannelService service;

    @BeforeEach
    void setUp() {
        service = new AdminWhitelistChannelService(
                whitelistChannelRepository,
                whitelistExportBatchRepository,
                whitelistExportItemRepository,
                userRepository,
                userSubscriptionRepository);
    }

    @Test
    @DisplayName("exportChannels writes userEmail CSV rows and marks channels exported")
    void exportChannelsIncludesUserEmailAndMarksExported() {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        User user = user(1L, "user@test.com", UserRole.USER);
        Subscription plan = subscription(10L, "DELUXE");
        UserSubscription activeSubscription = userSubscription(user, plan);
        WhitelistChannel channel = channel(7L, user);
        channel.requestRegistration();

        given(whitelistChannelRepository.findByStatusOrderByRequestedAtAsc(WhitelistChannelStatus.PENDING))
                .willReturn(List.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(whitelistExportBatchRepository.save(any(WhitelistExportBatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.of(activeSubscription));
        AtomicReference<List<WhitelistExportItem>> savedItems = new AtomicReference<>();
        given(whitelistExportItemRepository.saveAll(any()))
                .willAnswer(invocation -> {
                    Iterable<WhitelistExportItem> items = invocation.getArgument(0);
                    List<WhitelistExportItem> list = StreamSupport.stream(items.spliterator(), false).toList();
                    savedItems.set(list);
                    return list;
                });

        AdminWhitelistExportFile file = service.exportChannels(
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.PENDING,
                "agency export");

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(file.fileName()).startsWith("whitelist-channels-").endsWith(".csv");
        assertThat(csv).contains("requestId,userId,userEmail");
        assertThat(csv).contains("\"user@test.com\"");
        assertThat(csv).contains("\"DELUXE\"");
        assertThat(channel.getStatus()).isEqualTo(WhitelistChannelStatus.EXPORTED);
        assertThat(channel.getExportedAt()).isNotNull();

        verify(whitelistExportItemRepository).saveAll(any());
        WhitelistExportItem item = savedItems.get().get(0);
        assertThat(item.getUserEmailSnapshot()).isEqualTo("user@test.com");
        assertThat(item.getStatusAtExport()).isEqualTo(WhitelistChannelStatus.PENDING);
        assertThat(item.getExportedAtSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("exportChannels neutralizes formula-leading user cells before CSV quoting")
    void exportChannelsNeutralizesFormulaLeadingUserCellsBeforeQuoting() {
        User directUser = user(2L, "=2+3", "+nickname", UserRole.USER);
        WhitelistChannel directChannel = channel(
                8L,
                directUser,
                "-legitimate",
                "@shorts",
                " =HYPERLINK(\"https://example.com\")",
                "\t=channel");
        directChannel.requestRegistration();

        User controlUser = user(3L, "\uFEFF=hidden", "  @spaced", UserRole.USER);
        WhitelistChannel controlChannel = channel(
                9L,
                controlUser,
                "\r=carriage",
                "\n=line-feed",
                "\tplain-control",
                "-UC123");
        controlChannel.requestRegistration();

        AtomicReference<List<WhitelistExportItem>> savedItems = stubPendingExport(
                List.of(directChannel, controlChannel));

        AdminWhitelistExportFile file = service.exportChannels(
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.PENDING,
                null);

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith(
                "\uFEFFrequestId,userId,userEmail,userNickname,channelName,youtubeHandle,channelUrl,"
                        + "youtubeChannelId,requestedAt,planName,billingCycle,exportedAt\n");
        assertThat(csv).contains(
                "\"'=2+3\",\"'+nickname\",\"'-legitimate\",\"'@shorts\","
                        + "\"' =HYPERLINK(\"\"https://example.com\"\")\",\"'\t=channel\"");
        assertThat(csv).contains(
                "\"'\uFEFF=hidden\",\"'  @spaced\",\"'\r=carriage\",\"'\n=line-feed\","
                        + "\"'\tplain-control\",\"'-UC123\"");

        List<WhitelistExportItem> items = savedItems.get();
        assertThat(items.get(0).getUserEmailSnapshot()).isEqualTo("=2+3");
        assertThat(items.get(0).getUserNicknameSnapshot()).isEqualTo("+nickname");
        assertThat(items.get(0).getChannelNameSnapshot()).isEqualTo("-legitimate");
        assertThat(items.get(0).getYoutubeHandleSnapshot()).isEqualTo("@shorts");
        assertThat(items.get(0).getChannelUrlSnapshot())
                .isEqualTo(" =HYPERLINK(\"https://example.com\")");
        assertThat(items.get(0).getYoutubeChannelIdSnapshot()).isEqualTo("\t=channel");
        assertThat(items.get(1).getUserEmailSnapshot()).isEqualTo("\uFEFF=hidden");
        assertThat(items.get(1).getUserNicknameSnapshot()).isEqualTo("  @spaced");
        assertThat(items.get(1).getChannelNameSnapshot()).isEqualTo("\r=carriage");
        assertThat(items.get(1).getYoutubeHandleSnapshot()).isEqualTo("\n=line-feed");
        assertThat(items.get(1).getChannelUrlSnapshot()).isEqualTo("\tplain-control");
        assertThat(items.get(1).getYoutubeChannelIdSnapshot()).isEqualTo("-UC123");

        assertThat(directUser.getEmail()).isEqualTo("=2+3");
        assertThat(directChannel.getChannelUrl()).isEqualTo(" =HYPERLINK(\"https://example.com\")");
        assertThat(controlUser.getNickname()).isEqualTo("  @spaced");
        assertThat(controlChannel.getYoutubeHandle()).isEqualTo("\n=line-feed");
    }

    @Test
    @DisplayName("exportChannels preserves quoted, empty, null, apostrophe, and Korean user values")
    void exportChannelsPreservesNonFormulaUserValuesAndSnapshots() {
        User user = user(4L, "normal@test.com", "평범한 사용자", UserRole.USER);
        WhitelistChannel channel = channel(
                10L,
                user,
                "일반 \"채널\"\n두번째 줄",
                "'@already-safe",
                "",
                null);
        channel.requestRegistration();

        AtomicReference<List<WhitelistExportItem>> savedItems = stubPendingExport(List.of(channel));

        AdminWhitelistExportFile file = service.exportChannels(
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.PENDING,
                null);

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(csv).contains(
                "\"normal@test.com\",\"평범한 사용자\",\"일반 \"\"채널\"\"\n두번째 줄\","
                        + "\"'@already-safe\",\"\",,");
        assertThat(csv).doesNotContain("\"''@already-safe\"");

        WhitelistExportItem item = savedItems.get().get(0);
        assertThat(item.getUserEmailSnapshot()).isEqualTo("normal@test.com");
        assertThat(item.getUserNicknameSnapshot()).isEqualTo("평범한 사용자");
        assertThat(item.getChannelNameSnapshot()).isEqualTo("일반 \"채널\"\n두번째 줄");
        assertThat(item.getYoutubeHandleSnapshot()).isEqualTo("'@already-safe");
        assertThat(item.getChannelUrlSnapshot()).isEmpty();
        assertThat(item.getYoutubeChannelIdSnapshot()).isNull();

        assertThat(user.getNickname()).isEqualTo("평범한 사용자");
        assertThat(channel.getChannelName()).isEqualTo("일반 \"채널\"\n두번째 줄");
        assertThat(channel.getYoutubeHandle()).isEqualTo("'@already-safe");
        assertThat(channel.getChannelUrl()).isEmpty();
        assertThat(channel.getYoutubeChannelId()).isNull();
    }

    @Test
    @DisplayName("exportChannels does not overwrite non-pending workflow status")
    void exportChannelsKeepsNonPendingStatus() {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        User user = user(1L, "user@test.com", UserRole.USER);
        WhitelistChannel channel = channel(7L, user);
        channel.requestRemoval();

        given(whitelistChannelRepository.findByStatusOrderByRequestedAtAsc(WhitelistChannelStatus.REMOVAL_REQUESTED))
                .willReturn(List.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(whitelistExportBatchRepository.save(any(WhitelistExportBatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(whitelistExportItemRepository.saveAll(any())).willAnswer(invocation -> invocation.getArgument(0));

        service.exportChannels(
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.REMOVAL_REQUESTED,
                "removal export");

        assertThat(channel.getStatus()).isEqualTo(WhitelistChannelStatus.REMOVAL_REQUESTED);
        assertThat(channel.getExportedAt()).isNull();
    }

    @Test
    @DisplayName("updateStatus rejects non-admin-workflow statuses")
    void updateStatusRejectsInvalidWorkflowStatus() {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        User user = user(1L, "user@test.com", UserRole.USER);
        WhitelistChannel channel = channel(7L, user);

        given(whitelistChannelRepository.findById(7L)).willReturn(Optional.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateStatus(
                7L,
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.DRAFT,
                "invalid"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
    }

    private AtomicReference<List<WhitelistExportItem>> stubPendingExport(List<WhitelistChannel> channels) {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        given(whitelistChannelRepository.findByStatusOrderByRequestedAtAsc(WhitelistChannelStatus.PENDING))
                .willReturn(channels);
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(whitelistExportBatchRepository.save(any(WhitelistExportBatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userSubscriptionRepository.findActiveByUser(any(User.class), any(LocalDate.class)))
                .willReturn(Optional.empty());
        AtomicReference<List<WhitelistExportItem>> savedItems = new AtomicReference<>();
        given(whitelistExportItemRepository.saveAll(any()))
                .willAnswer(invocation -> {
                    Iterable<WhitelistExportItem> items = invocation.getArgument(0);
                    List<WhitelistExportItem> list = StreamSupport.stream(items.spliterator(), false).toList();
                    savedItems.set(list);
                    return list;
                });
        return savedItems;
    }

    private User user(Long id, String email, UserRole role) {
        return user(id, email, "user" + id, role);
    }

    private User user(Long id, String email, String nickname, UserRole role) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("pw")
                .userType(UserType.INDIVIDUAL)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Subscription subscription(Long id, String name) {
        Subscription subscription = Subscription.builder()
                .name(name)
                .userType(UserType.INDIVIDUAL)
                .maxWhitelistChannels(3)
                .build();
        ReflectionTestUtils.setField(subscription, "id", id);
        return subscription;
    }

    private UserSubscription userSubscription(User user, Subscription subscription) {
        return UserSubscription.builder()
                .user(user)
                .subscription(subscription)
                .billingCycle(BillingCycle.MONTHLY)
                .status(SubscriptionStatus.ACTIVE)
                .startedAt(LocalDate.now())
                .expiresAt(LocalDate.now().plusMonths(1))
                .build();
    }

    private WhitelistChannel channel(Long id, User user) {
        return channel(
                id,
                user,
                "Shorts Channel",
                "@shorts",
                "https://youtube.com/@shorts",
                "UC123");
    }

    private WhitelistChannel channel(
            Long id,
            User user,
            String channelName,
            String youtubeHandle,
            String channelUrl,
            String youtubeChannelId
    ) {
        WhitelistChannel channel = WhitelistChannel.builder()
                .user(user)
                .channelName(channelName)
                .channelUrl(channelUrl)
                .youtubeHandle(youtubeHandle)
                .youtubeChannelId(youtubeChannelId)
                .primary(true)
                .build();
        ReflectionTestUtils.setField(channel, "id", id);
        return channel;
    }

    private CustomUserDetails actor(Long id, UserRole role) {
        return CustomUserDetails.builder()
                .id(id)
                .email("actor@test.com")
                .password("pw")
                .role(role)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}
