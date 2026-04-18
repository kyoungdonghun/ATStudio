package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.playlist.*;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.BillingCycle;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.key.PlaylistTrackId;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaylistService 단위 테스트")
class PlaylistServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserSubscriptionRepository userSubscriptionRepository;
    @Mock PlaylistRepository playlistRepository;
    @Mock PlaylistTrackRepository playlistTrackRepository;
    @Mock TrackRepository trackRepository;
    @Mock StorageService storageService;

    @InjectMocks PlaylistService playlistService;

    // ── createPlaylist() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createPlaylist() 성공 - 구독자 플레이리스트 생성")
    void createPlaylist_success() {
        User user = buildUser(1L);
        Playlist saved = buildPlaylist(1L, user, "Test Playlist");
        PlaylistCreateRequest request = new PlaylistCreateRequest();
        request.setTitle("Test Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.save(any(Playlist.class))).willReturn(saved);

        PlaylistResponse result = playlistService.createPlaylist(request, null, buildUserDetails(1L));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Playlist");
    }

    @Test
    @DisplayName("createPlaylist() 실패 - 활성 플레이리스트 3개 보유 시 PLAYLIST_LIMIT_EXCEEDED 예외")
    void createPlaylist_limitExceeded_throws() {
        // given
        User user = buildUser(1L);
        PlaylistCreateRequest request = new PlaylistCreateRequest();
        request.setTitle("Fourth Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.countByUserAndIsActiveTrue(user)).willReturn(3);

        // when & then
        assertThatThrownBy(() -> playlistService.createPlaylist(request, null, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("createPlaylist() 성공 - 활성 플레이리스트 2개 보유 시 정상 생성")
    void createPlaylist_atLimit_succeeds() {
        // given
        User user = buildUser(1L);
        Playlist saved = buildPlaylist(2L, user, "Third Playlist");
        PlaylistCreateRequest request = new PlaylistCreateRequest();
        request.setTitle("Third Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.countByUserAndIsActiveTrue(user)).willReturn(2);
        given(playlistRepository.save(any(Playlist.class))).willReturn(saved);

        // when
        PlaylistResponse result = playlistService.createPlaylist(request, null, buildUserDetails(1L));

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.title()).isEqualTo("Third Playlist");
    }

    @Test
    @DisplayName("createPlaylist() 실패 - 현재 플랜 maxPlaylists를 초과하면 제한한다")
    void createPlaylist_respectsTierMaxPlaylists() {
        User user = buildUser(1L);
        PlaylistCreateRequest request = new PlaylistCreateRequest();
        request.setTitle("Third Playlist");

        setupSubscriberMocks(user, 2);
        given(playlistRepository.countByUserAndIsActiveTrue(user)).willReturn(2);

        assertThatThrownBy(() -> playlistService.createPlaylist(request, null, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PLAYLIST_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("createPlaylist() 실패 - 비구독자 → NO_ACTIVE_SUBSCRIPTION 예외")
    void createPlaylist_notSubscribed() {
        User user = buildUser(1L);
        PlaylistCreateRequest request = new PlaylistCreateRequest();
        request.setTitle("Test Playlist");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(User.class), any(LocalDate.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.createPlaylist(request, null, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.NO_ACTIVE_SUBSCRIPTION));
    }

    // ── getMyPlaylists() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getMyPlaylists() 성공 - 배치 count 쿼리로 트랙 수 조회")
    void getMyPlaylists_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(user))
                .willReturn(List.of(playlist));
        List<Object[]> countResult = new ArrayList<>();
        countResult.add(new Object[]{1L, 3L});
        given(playlistTrackRepository.countByPlaylistIdIn(List.of(1L)))
                .willReturn(countResult);

        List<PlaylistListItemResponse> result = playlistService.getMyPlaylists(buildUserDetails(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).trackCount()).isEqualTo(3);
        verify(playlistTrackRepository).countByPlaylistIdIn(List.of(1L));
    }

    @Test
    @DisplayName("getMyPlaylists() 성공 - 다수 플레이리스트 배치 count 매핑 정확성")
    void getMyPlaylists_multiplePlaylists_batchCount() {
        User user = buildUser(1L);
        Playlist playlist1 = buildPlaylist(1L, user, "Playlist A");
        Playlist playlist2 = buildPlaylist(2L, user, "Playlist B");
        Playlist playlist3 = buildPlaylist(3L, user, "Playlist C");

        setupSubscriberMocks(user);
        given(playlistRepository.findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(user))
                .willReturn(List.of(playlist1, playlist2, playlist3));
        List<Object[]> countResult = new ArrayList<>();
        countResult.add(new Object[]{1L, 5L});
        countResult.add(new Object[]{3L, 2L});
        given(playlistTrackRepository.countByPlaylistIdIn(List.of(1L, 2L, 3L)))
                .willReturn(countResult);

        List<PlaylistListItemResponse> result = playlistService.getMyPlaylists(buildUserDetails(1L));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).trackCount()).isEqualTo(5);
        assertThat(result.get(1).trackCount()).isEqualTo(0);
        assertThat(result.get(2).trackCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getMyPlaylists() 성공 - 빈 플레이리스트 목록 시 배치 쿼리 미호출")
    void getMyPlaylists_empty() {
        User user = buildUser(1L);

        setupSubscriberMocks(user);
        given(playlistRepository.findAllByUserAndIsActiveTrueOrderByCreatedAtDesc(user))
                .willReturn(List.of());

        List<PlaylistListItemResponse> result = playlistService.getMyPlaylists(buildUserDetails(1L));

        assertThat(result).isEmpty();
        verify(playlistTrackRepository, org.mockito.Mockito.never()).countByPlaylistIdIn(anyList());
    }

    // ── getPlaylistDetail() ───────────────────────────────────────────────────

    @Test
    @DisplayName("getPlaylistDetail() 성공 - 플레이리스트 상세 조회")
    void getPlaylistDetail_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));
        given(playlistTrackRepository.findAllByIdPlaylistIdOrderByTrackOrderAsc(1L))
                .willReturn(List.of());

        PlaylistDetailResponse result = playlistService.getPlaylistDetail(1L, buildUserDetails(1L));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.tracks()).isEmpty();
    }

    @Test
    @DisplayName("getPlaylistDetail() 실패 - 소유자 아님 → RESOURCE_NOT_ACCESS 예외")
    void getPlaylistDetail_notOwner() {
        User owner = buildUser(2L);
        Playlist playlist = buildPlaylist(1L, owner, "Other's Playlist");

        setupSubscriberMocks(buildUser(1L));
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        assertThatThrownBy(() -> playlistService.getPlaylistDetail(1L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
    }

    @Test
    @DisplayName("getPlaylistDetail() 실패 - 없는 플레이리스트 → RESOURCE_NOT_FOUND 예외")
    void getPlaylistDetail_notFound() {
        setupSubscriberMocks(buildUser(1L));
        given(playlistRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> playlistService.getPlaylistDetail(99L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── addTrack() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addTrack() 성공 - 트랙 플레이리스트에 추가")
    void addTrack_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");
        Track track = buildTrack(5L, true);
        PlaylistAddTrackRequest request = new PlaylistAddTrackRequest(5L);

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));
        given(trackRepository.findById(5L)).willReturn(Optional.of(track));
        given(playlistTrackRepository.existsById(any(PlaylistTrackId.class))).willReturn(false);
        given(playlistTrackRepository.countByIdPlaylistId(1L)).willReturn(2L);

        playlistService.addTrack(1L, request, buildUserDetails(1L));

        verify(playlistTrackRepository).save(any(PlaylistTrack.class));
    }

    @Test
    @DisplayName("addTrack() 실패 - 중복 트랙 → RESOURCE_DUPLICATE 예외")
    void addTrack_duplicate() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");
        Track track = buildTrack(5L, true);
        PlaylistAddTrackRequest request = new PlaylistAddTrackRequest(5L);

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));
        given(trackRepository.findById(5L)).willReturn(Optional.of(track));
        given(playlistTrackRepository.existsById(any(PlaylistTrackId.class))).willReturn(true);

        assertThatThrownBy(() -> playlistService.addTrack(1L, request, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
    }

    // ── updatePlaylist() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePlaylist() 성공 - 제목/설명 수정")
    void updatePlaylist_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "Old Title");
        PlaylistUpdateRequest request = new PlaylistUpdateRequest();
        request.setTitle("New Title");

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));
        given(playlistTrackRepository.countByIdPlaylistId(1L)).willReturn(0L);

        PlaylistResponse result = playlistService.updatePlaylist(1L, request, null, buildUserDetails(1L));

        assertThat(result.title()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("updatePlaylist() 실패 - 소유자 아님 → RESOURCE_NOT_ACCESS 예외")
    void updatePlaylist_notOwner() {
        User owner = buildUser(2L);
        Playlist playlist = buildPlaylist(1L, owner, "Other's Playlist");
        PlaylistUpdateRequest request = new PlaylistUpdateRequest();
        request.setTitle("Hacked Title");

        setupSubscriberMocks(buildUser(1L));
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        assertThatThrownBy(() -> playlistService.updatePlaylist(1L, request, null, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
    }

    // ── reorderTracks() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("reorderTracks() 성공 - 트랙 순서 변경")
    void reorderTracks_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");
        PlaylistReorderRequest request = new PlaylistReorderRequest(List.of());

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        playlistService.reorderTracks(1L, request, buildUserDetails(1L));

        verify(playlistTrackRepository).deleteAllByIdPlaylistId(1L);
        verify(playlistTrackRepository).saveAll(anyList());
    }

    // ── removeTrack() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeTrack() 성공 - 플레이리스트에서 트랙 제거")
    void removeTrack_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");
        PlaylistTrackId ptId = new PlaylistTrackId(1L, 5L);
        PlaylistTrack pt = PlaylistTrack.builder().id(ptId).playlist(playlist)
                .track(buildTrack(5L, true)).trackOrder(0).build();

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));
        given(playlistTrackRepository.findById(any(PlaylistTrackId.class))).willReturn(Optional.of(pt));

        playlistService.removeTrack(1L, 5L, buildUserDetails(1L));

        verify(playlistTrackRepository).delete(pt);
    }

    // ── deletePlaylist() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("deletePlaylist() 성공 - 플레이리스트 소프트 삭제 (isActive=false)")
    void deletePlaylist_success() {
        User user = buildUser(1L);
        Playlist playlist = buildPlaylist(1L, user, "My Playlist");

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        playlistService.deletePlaylist(1L, buildUserDetails(1L));

        assertThat(playlist.isActive()).isFalse();
        verify(playlistTrackRepository).deleteAllByIdPlaylistId(1L);
    }

    @Test
    @DisplayName("deletePlaylist() - playlist_tracks 레코드를 deactivate 전에 삭제")
    void deletePlaylist_deletesPlaylistTracksBeforeDeactivate() {
        User user = buildUser(1L);
        Playlist playlist = spy(buildPlaylist(1L, user, "My Playlist"));

        setupSubscriberMocks(user);
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        playlistService.deletePlaylist(1L, buildUserDetails(1L));

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(playlistTrackRepository, playlist);
        inOrder.verify(playlistTrackRepository).deleteAllByIdPlaylistId(1L);
        inOrder.verify(playlist).deactivate();
    }

    @Test
    @DisplayName("deletePlaylist() 실패 - 소유자 아님 → RESOURCE_NOT_ACCESS 예외")
    void deletePlaylist_notOwner() {
        User owner = buildUser(2L);
        Playlist playlist = buildPlaylist(1L, owner, "Other's Playlist");

        setupSubscriberMocks(buildUser(1L));
        given(playlistRepository.findById(1L)).willReturn(Optional.of(playlist));

        assertThatThrownBy(() -> playlistService.deletePlaylist(1L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void setupSubscriberMocks(User user) {
        UserSubscription sub = buildSubscription(user);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(User.class), any(LocalDate.class)))
                .willReturn(Optional.of(sub));
    }

    private void setupSubscriberMocks(User user, int maxPlaylists) {
        UserSubscription sub = buildSubscription(user, maxPlaylists);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userSubscriptionRepository.findActiveByUser(any(User.class), any(LocalDate.class)))
                .willReturn(Optional.of(sub));
    }

    private User buildUser(Long id) {
        User user = User.builder()
                .email("test@test.com").nickname("nick").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.USER).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserSubscription buildSubscription(User user) {
        return buildSubscription(user, 3);
    }

    private UserSubscription buildSubscription(User user, int maxPlaylists) {
        Subscription plan = Subscription.builder()
                .name("Basic").userType(UserType.INDIVIDUAL)
                .priceMonthly(new BigDecimal("9.99")).priceYearly(new BigDecimal("99.99"))
                .downloadPerDay(10).maxWhitelistChannels(3)
                .maxPlaylists(maxPlaylists).build();
        return UserSubscription.builder()
                .user(user).subscription(plan).billingCycle(BillingCycle.MONTHLY)
                .startedAt(LocalDate.now()).expiresAt(LocalDate.now().plusMonths(1))
                .build();
    }

    private Playlist buildPlaylist(Long id, User user, String title) {
        Playlist p = Playlist.builder().title(title).user(user).build();
        ReflectionTestUtils.setField(p, "id", id);
        return p;
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
