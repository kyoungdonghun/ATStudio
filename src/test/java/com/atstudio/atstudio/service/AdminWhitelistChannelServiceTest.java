package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.WhitelistExportProperties;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportFile;
import com.atstudio.atstudio.dto.whitelist.AdminWhitelistExportRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminWhitelistChannelService unit tests")
class AdminWhitelistChannelServiceTest {

    @Mock WhitelistChannelRepository whitelistChannelRepository;
    @Mock WhitelistExportBatchRepository whitelistExportBatchRepository;
    @Mock WhitelistExportItemRepository whitelistExportItemRepository;
    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;

    AdminWhitelistChannelService service;
    WhitelistExportProperties properties;

    @BeforeEach
    void setUp() {
        properties = new WhitelistExportProperties();
        service = new AdminWhitelistChannelService(
                whitelistChannelRepository,
                whitelistExportBatchRepository,
                whitelistExportItemRepository,
                userRepository,
                userSubscriptionRepository,
                properties);
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

        given(whitelistChannelRepository.findExportCandidates(
                eq(WhitelistChannelStatus.PENDING), isNull(), any()))
                .willReturn(List.of(channel));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(whitelistChannelRepository.findAllByIdForUpdate(List.of(7L)))
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
                new AdminWhitelistExportRequest(
                        WhitelistChannelStatus.PENDING,
                        null,
                        "agency export"));

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(file.fileName()).startsWith("whitelist-channels-").endsWith(".csv");
        assertThat(csv).contains("requestId,userEmail,channelName");
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
    @DisplayName("exportChannels locks users in ID order before locking selected channels")
    void exportChannelsUsesStableUserThenChannelLockOrder() {
        User laterUser = user(2L, "later@test.com", UserRole.USER);
        User earlierUser = user(1L, "earlier@test.com", UserRole.USER);
        WhitelistChannel laterChannel = channel(8L, laterUser);
        WhitelistChannel earlierChannel = channel(7L, earlierUser);
        laterChannel.requestRegistration();
        earlierChannel.requestRegistration();
        stubPendingExport(List.of(laterChannel, earlierChannel));

        service.exportChannels(
                actor(99L, UserRole.ADMIN),
                new AdminWhitelistExportRequest(WhitelistChannelStatus.PENDING, null, null));

        InOrder lockOrder = inOrder(whitelistChannelRepository, userRepository);
        lockOrder.verify(whitelistChannelRepository).findExportCandidates(
                eq(WhitelistChannelStatus.PENDING), isNull(), any());
        lockOrder.verify(userRepository).findByIdForUpdate(1L);
        lockOrder.verify(userRepository).findByIdForUpdate(2L);
        lockOrder.verify(whitelistChannelRepository).findAllByIdForUpdate(List.of(8L, 7L));
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
                new AdminWhitelistExportRequest(WhitelistChannelStatus.PENDING, null, null));

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(csv).startsWith(
                "\uFEFFrequestId,userEmail,channelName,youtubeHandle,channelUrl,"
                        + "youtubeChannelId,requestedAt,planName,billingCycle,exportedAt\n");
        assertThat(csv).contains(
                "\"'=2+3\",\"'-legitimate\",\"'@shorts\","
                        + "\"' =HYPERLINK(\"\"https://example.com\"\")\",\"'\t=channel\"");
        assertThat(csv).contains(
                "\"'\uFEFF=hidden\",\"'\r=carriage\",\"'\n=line-feed\","
                        + "\"'\tplain-control\",\"'-UC123\"");

        List<WhitelistExportItem> items = savedItems.get();
        assertThat(items.get(0).getUserEmailSnapshot()).isEqualTo("=2+3");
        assertThat(items.get(0).getChannelNameSnapshot()).isEqualTo("-legitimate");
        assertThat(items.get(0).getYoutubeHandleSnapshot()).isEqualTo("@shorts");
        assertThat(items.get(0).getChannelUrlSnapshot())
                .isEqualTo(" =HYPERLINK(\"https://example.com\")");
        assertThat(items.get(0).getYoutubeChannelIdSnapshot()).isEqualTo("\t=channel");
        assertThat(items.get(1).getUserEmailSnapshot()).isEqualTo("\uFEFF=hidden");
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
                new AdminWhitelistExportRequest(WhitelistChannelStatus.PENDING, null, null));

        String csv = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(csv).contains(
                "\"normal@test.com\",\"일반 \"\"채널\"\"\n두번째 줄\","
                        + "\"'@already-safe\",\"\",,");
        assertThat(csv).doesNotContain("\"''@already-safe\"");

        WhitelistExportItem item = savedItems.get().get(0);
        assertThat(item.getUserEmailSnapshot()).isEqualTo("normal@test.com");
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

        given(whitelistChannelRepository.findExportCandidates(
                eq(WhitelistChannelStatus.REMOVAL_REQUESTED), isNull(), any()))
                .willReturn(List.of(channel));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(whitelistChannelRepository.findAllByIdForUpdate(List.of(7L)))
                .willReturn(List.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));
        given(whitelistExportBatchRepository.save(any(WhitelistExportBatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userSubscriptionRepository.findActiveByUser(eq(user), any(LocalDate.class)))
                .willReturn(Optional.empty());
        given(whitelistExportItemRepository.saveAll(any())).willAnswer(invocation -> invocation.getArgument(0));

        service.exportChannels(
                actor(99L, UserRole.ADMIN),
                new AdminWhitelistExportRequest(
                        WhitelistChannelStatus.REMOVAL_REQUESTED,
                        null,
                        "removal export"));

        assertThat(channel.getStatus()).isEqualTo(WhitelistChannelStatus.REMOVAL_REQUESTED);
        assertThat(channel.getExportedAt()).isNull();
    }

    @Test
    @DisplayName("updateStatus rejects non-admin-workflow statuses")
    void updateStatusRejectsInvalidWorkflowStatus() {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        User user = user(1L, "user@test.com", UserRole.USER);
        WhitelistChannel channel = channel(7L, user);
        channel.requestRegistration();

        given(whitelistChannelRepository.findById(7L)).willReturn(Optional.of(channel));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(whitelistChannelRepository.findByIdForUpdate(7L)).willReturn(Optional.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.updateStatus(
                7L,
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.CANCELLED,
                "invalid"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_STATE_TRANSITION));
    }

    @Test
    @DisplayName("status transition matrix matches the approved lifecycle for every state pair")
    void statusTransitionMatrixMatchesApprovedLifecycle() {
        Map<WhitelistChannelStatus, Set<WhitelistChannelStatus>> expectedTargets = Map.of(
                WhitelistChannelStatus.DRAFT, Set.of(),
                WhitelistChannelStatus.PENDING, Set.of(
                        WhitelistChannelStatus.REGISTERED,
                        WhitelistChannelStatus.REVISION_REQUESTED,
                        WhitelistChannelStatus.REJECTED),
                WhitelistChannelStatus.EXPORTED, Set.of(
                        WhitelistChannelStatus.REGISTERED,
                        WhitelistChannelStatus.REVISION_REQUESTED,
                        WhitelistChannelStatus.REJECTED,
                        WhitelistChannelStatus.REMOVAL_REQUESTED),
                WhitelistChannelStatus.REGISTERED, Set.of(
                        WhitelistChannelStatus.REVISION_REQUESTED,
                        WhitelistChannelStatus.REMOVAL_REQUESTED),
                WhitelistChannelStatus.REVISION_REQUESTED, Set.of(
                        WhitelistChannelStatus.REGISTERED,
                        WhitelistChannelStatus.REJECTED),
                WhitelistChannelStatus.REJECTED, Set.of(),
                WhitelistChannelStatus.REMOVAL_REQUESTED, Set.of(WhitelistChannelStatus.CANCELLED),
                WhitelistChannelStatus.CANCELLED, Set.of());

        for (WhitelistChannelStatus source : WhitelistChannelStatus.values()) {
            for (WhitelistChannelStatus target : WhitelistChannelStatus.values()) {
                boolean expected = source == target || expectedTargets.get(source).contains(target);
                assertThat(AdminWhitelistChannelService.isStatusTransitionAllowed(source, target))
                        .as("%s -> %s", source, target)
                        .isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("REMOVAL_REQUESTED transitions to terminal CANCELLED idempotently")
    void updateStatusCompletesRemovalIdempotently() {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        User user = user(1L, "user@test.com", UserRole.USER);
        WhitelistChannel channel = channel(7L, user);
        channel.requestRemoval();

        given(whitelistChannelRepository.findById(7L)).willReturn(Optional.of(channel));
        given(userRepository.findByIdForUpdate(1L)).willReturn(Optional.of(user));
        given(whitelistChannelRepository.findByIdForUpdate(7L)).willReturn(Optional.of(channel));
        given(userRepository.findById(99L)).willReturn(Optional.of(admin));

        service.updateStatus(
                7L,
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.CANCELLED,
                "External removal completed");
        var processedAt = channel.getProcessedAt();

        service.updateStatus(
                7L,
                actor(99L, UserRole.ADMIN),
                WhitelistChannelStatus.CANCELLED,
                "Repeated callback");

        assertThat(channel.getStatus()).isEqualTo(WhitelistChannelStatus.CANCELLED);
        assertThat(channel.getProcessedAt()).isEqualTo(processedAt);
        assertThat(channel.getAdminNote()).isEqualTo("External removal completed");
        assertThat(channel.isPrimary()).isFalse();
    }

    @Test
    @DisplayName("exportChannels requires a recorded filter scope")
    void exportChannelsRejectsImplicitUnboundedScope() {
        assertThatThrownBy(() -> service.exportChannels(
                actor(99L, UserRole.ADMIN),
                new AdminWhitelistExportRequest(null, " ", null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(whitelistExportBatchRepository, whitelistExportItemRepository);
    }

    @Test
    @DisplayName("exportChannels rejects oversized scope before creating a partial batch")
    void exportChannelsRejectsOversizedScopeWithoutPartialBatch() {
        User user = user(1L, "user@test.com", UserRole.USER);
        properties.setMaxItems(1);
        given(whitelistChannelRepository.findExportCandidates(
                eq(WhitelistChannelStatus.PENDING), isNull(), any()))
                .willReturn(List.of(channel(1L, user), channel(2L, user)));

        assertThatThrownBy(() -> service.exportChannels(
                actor(99L, UserRole.ADMIN),
                new AdminWhitelistExportRequest(WhitelistChannelStatus.PENDING, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(whitelistExportBatchRepository, whitelistExportItemRepository);
    }

    @Test
    @DisplayName("downloadExportBatch rebuilds byte-stable CSV from immutable items")
    void downloadExportBatchIsByteStableWithoutChannelRequery() {
        User user = user(1L, "user@test.com", UserRole.USER);
        WhitelistChannel channel = channel(7L, user);
        channel.requestRegistration();
        AtomicReference<List<WhitelistExportItem>> savedItems = stubPendingExport(List.of(channel));

        AdminWhitelistExportFile original = service.exportChannels(
                actor(99L, UserRole.ADMIN),
                new AdminWhitelistExportRequest(WhitelistChannelStatus.PENDING, null, null));

        ArgumentCaptor<WhitelistExportBatch> batchCaptor = ArgumentCaptor.forClass(WhitelistExportBatch.class);
        verify(whitelistExportBatchRepository).save(batchCaptor.capture());
        WhitelistExportBatch batch = batchCaptor.getValue();
        ReflectionTestUtils.setField(batch, "id", 77L);
        given(whitelistExportBatchRepository.findById(77L)).willReturn(Optional.of(batch));
        given(whitelistExportItemRepository.findImmutableBatchItems(eq(77L), any()))
                .willReturn(savedItems.get());

        AdminWhitelistExportFile replay = service.downloadExportBatch(77L);

        assertThat(replay.fileName()).isEqualTo(original.fileName());
        assertThat(replay.content()).containsExactly(original.content());
        assertThat(savedItems.get().get(0).getChannelIdSnapshot()).isEqualTo(7L);
    }

    @Test
    @DisplayName("downloadExportBatch rejects legacy batches above the configured bound")
    void downloadExportBatchRejectsOversizedStoredBatchBeforeLoadingItems() {
        properties.setMaxItems(1);
        WhitelistExportBatch batch = WhitelistExportBatch.builder()
                .fileName("legacy.csv")
                .itemCount(2)
                .build();
        given(whitelistExportBatchRepository.findById(77L)).willReturn(Optional.of(batch));

        assertThatThrownBy(() -> service.downloadExportBatch(77L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));

        verifyNoInteractions(whitelistExportItemRepository);
    }

    private AtomicReference<List<WhitelistExportItem>> stubPendingExport(List<WhitelistChannel> channels) {
        User admin = user(99L, "admin@test.com", UserRole.ADMIN);
        given(whitelistChannelRepository.findExportCandidates(
                eq(WhitelistChannelStatus.PENDING), isNull(), any()))
                .willReturn(channels);
        channels.stream()
                .map(WhitelistChannel::getUser)
                .distinct()
                .forEach(user -> given(userRepository.findByIdForUpdate(user.getId()))
                        .willReturn(Optional.of(user)));
        given(whitelistChannelRepository.findAllByIdForUpdate(
                channels.stream().map(WhitelistChannel::getId).toList()))
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
