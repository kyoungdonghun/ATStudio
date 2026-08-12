package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.ValidationConstants;
import com.atstudio.atstudio.dto.album.*;
import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.entity.key.AlbumTrackId;
import com.atstudio.atstudio.repository.AlbumRepository;
import com.atstudio.atstudio.repository.AlbumTrackRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.image.CanonicalImageService;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlbumService 단위 테스트")
class AlbumServiceTest {

    @Mock AlbumRepository albumRepository;
    @Mock AlbumTrackRepository albumTrackRepository;
    @Mock TrackRepository trackRepository;
    @Mock UserRepository userRepository;
    @Mock StorageMutationCoordinator storageMutationCoordinator;
    @Mock CanonicalImageService canonicalImageService;

    @InjectMocks AlbumService albumService;

    // -- createAlbum() --------------------------------------------------------

    @Test
    @DisplayName("createAlbum() 성공 - 앨범 생성")
    void createAlbum_success() {
        User user = buildUser(1L);
        Album saved = buildAlbum(1L, user, "Test Album");
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("Test Album");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(albumRepository.save(any(Album.class))).willReturn(saved);

        AlbumResponse result = albumService.createAlbum(request, null, buildAdminDetails(1L));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Test Album");
        assertThat(result.trackCount()).isZero();
    }

    @Test
    @DisplayName("createAlbum stores only the canonical JPEG thumbnail")
    void createAlbum_polyglotThumbnail_storesOnlyCanonicalJpeg() throws Exception {
        User user = buildUser(1L);
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("Canonical Album");
        byte[] inputBytes = append(
                pngBytes(24, 16),
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnailFile", "cover.svg", "image/png", inputBytes);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(storageMutationCoordinator.store(
                eq(StorageDomain.ALBUM),
                eq(StorageRoot.PUBLIC),
                any(MultipartFile.class),
                eq("albums/thumbnails")))
                .willReturn("albums/thumbnails/generated.jpg");
        given(albumRepository.save(any(Album.class))).willAnswer(invocation -> {
            Album album = invocation.getArgument(0);
            ReflectionTestUtils.setField(album, "id", 10L);
            return album;
        });
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        AlbumResponse result = albumService.createAlbum(request, thumbnail, buildAdminDetails(1L));

        ArgumentCaptor<MultipartFile> storedFile = ArgumentCaptor.forClass(MultipartFile.class);
        verify(storageMutationCoordinator).store(
                eq(StorageDomain.ALBUM),
                eq(StorageRoot.PUBLIC),
                storedFile.capture(),
                eq("albums/thumbnails"));
        assertThat(storedFile.getValue().getOriginalFilename()).isEqualTo("thumbnail.jpg");
        assertThat(storedFile.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(storedFile.getValue().getBytes()).startsWith((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
        assertThat(new String(storedFile.getValue().getBytes(), StandardCharsets.ISO_8859_1))
                .doesNotContain("<svg");
        assertThat(result.thumbnailUrl()).isEqualTo("albums/thumbnails/generated.jpg");
    }

    @Test
    @DisplayName("createAlbum rejects HTML and SVG before storage or Album persistence")
    void createAlbum_activeContent_rejectedBeforeStorageAndPersistence() {
        User user = buildUser(1L);
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("Rejected Album");
        List<MockMultipartFile> activeContent = List.of(
                new MockMultipartFile(
                        "thumbnailFile", "cover.jpg", "application/octet-stream",
                        "<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)),
                new MockMultipartFile(
                        "thumbnailFile", "cover.png", "application/octet-stream",
                        "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8)));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        activeContent.forEach(file -> assertThatThrownBy(
                () -> albumService.createAlbum(request, file, buildAdminDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID)));

        verifyNoInteractions(storageMutationCoordinator, albumRepository);
    }

    @Test
    @DisplayName("createAlbum rejects a MIME mismatch before storage or Album persistence")
    void createAlbum_mimeMismatch_rejectedBeforeStorageAndPersistence() throws Exception {
        User user = buildUser(1L);
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("Rejected Album");
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnailFile", "cover.jpg", "image/png", jpegBytes(16, 16));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        assertThatThrownBy(() -> albumService.createAlbum(request, thumbnail, buildAdminDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
        verifyNoInteractions(storageMutationCoordinator, albumRepository);
    }

    @Test
    @DisplayName("createAlbum rejects an oversized thumbnail before storage or Album persistence")
    void createAlbum_oversizedThumbnail_rejectedBeforeStorageAndPersistence() {
        User user = buildUser(1L);
        AlbumCreateRequest request = new AlbumCreateRequest();
        request.setTitle("Rejected Album");
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnailFile",
                "cover.png",
                "image/png",
                new byte[(int) ValidationConstants.IMAGE_MAX_SIZE_BYTES + 1]);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        assertThatThrownBy(() -> albumService.createAlbum(request, thumbnail, buildAdminDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.IO_LARGE));
        verifyNoInteractions(storageMutationCoordinator, albumRepository);
    }

    // -- getAlbums() ----------------------------------------------------------

    @Test
    @DisplayName("getAlbums() 성공 - 활성 앨범만 반환")
    void getAlbumsPaged_trackCountUsesGlobalDatabaseOrdering() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Active Album");

        given(albumRepository.findAllActiveOrderByTrackCount(PageRequest.of(1, 2)))
                .willReturn(new PageImpl<>(List.of(album), PageRequest.of(1, 2), 3));
        given(albumTrackRepository.countActiveMapByAlbums(List.of(album)))
                .willReturn(Map.of(1L, 1));

        var result = albumService.getAlbumsPaged(2, 2, "trackCount");

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).title()).isEqualTo("Active Album");
        assertThat(result.getDataList().get(0).trackCount()).isEqualTo(1);
        verify(albumRepository).findAllActiveOrderByTrackCount(PageRequest.of(1, 2));
        verify(albumTrackRepository).countActiveMapByAlbums(List.of(album));
    }

    @Test
    void getAlbumsPaged_latestUsesDeterministicDatabaseOrdering() {
        User user = buildUser(1L);
        Album album = buildAlbum(2L, user, "Latest Album");

        given(albumRepository.findAllActiveOrderByCreatedAt(PageRequest.of(0, 2)))
                .willReturn(new PageImpl<>(List.of(album), PageRequest.of(0, 2), 1));
        given(albumTrackRepository.countActiveMapByAlbums(List.of(album))).willReturn(Map.of());

        albumService.getAlbumsPaged(1, 2, "latest");

        verify(albumRepository).findAllActiveOrderByCreatedAt(PageRequest.of(0, 2));
    }

    // -- getAlbum() -----------------------------------------------------------

    @Test
    @DisplayName("getAlbum() 실패 - 존재하지 않는 앨범 → RESOURCE_NOT_FOUND")
    void getAlbum_notFound_throws() {
        given(albumRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.getAlbum(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    @Test
    @DisplayName("getAlbum() 실패 - 비활성 앨범 → RESOURCE_NOT_FOUND")
    void getAlbum_inactive_throws() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Inactive Album");
        album.softDelete();

        given(albumRepository.findById(1L)).willReturn(Optional.of(album));

        assertThatThrownBy(() -> albumService.getAlbum(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    @Test
    @DisplayName("getAlbum() returns only the active membership projection")
    void getAlbum_mixedMembershipReturnsOnlyActiveTrack() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Mixed Album");
        Track activeTrack = buildTrack(10L, true);
        Track inactiveTrack = buildTrack(20L, false);
        AlbumTrack activeMembership = AlbumTrack.builder()
                .id(new AlbumTrackId(1L, 10L))
                .album(album)
                .track(activeTrack)
                .trackOrder(0)
                .build();

        given(albumRepository.findById(1L)).willReturn(Optional.of(album));
        given(albumTrackRepository.findAllPlayableByAlbumOrderByTrackOrder(album))
                .willReturn(List.of(activeMembership));

        AlbumDetailResponse result = albumService.getAlbum(1L);

        assertThat(result.tracks())
                .extracting(AlbumTrackItemResponse::trackId)
                .containsExactly(activeTrack.getId())
                .doesNotContain(inactiveTrack.getId());
    }

    // -- updateAlbum() --------------------------------------------------------

    @Test
    @DisplayName("updateAlbum() 성공 - 제목/설명 수정")
    void updateAlbum_success() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Old Title");
        AlbumUpdateRequest request = new AlbumUpdateRequest();
        request.setTitle("New Title");

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(albumTrackRepository.countByAlbum(album))
                .willReturn(0L);

        AlbumResponse result = albumService.updateAlbum(1L, request, null);

        assertThat(result.title()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("updateAlbum canonicalizes a replacement before public storage")
    void updateAlbum_thumbnailReplacement_storesCanonicalJpeg() throws Exception {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Old Title");
        ReflectionTestUtils.setField(album, "thumbnail", "albums/thumbnails/old.jpg");
        AlbumUpdateRequest request = new AlbumUpdateRequest();
        request.setTitle("New Title");
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnailFile", "cover.png", "image/png", pngBytes(18, 12));

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(storageMutationCoordinator.replace(
                eq(StorageDomain.ALBUM),
                eq(StorageRoot.PUBLIC),
                any(MultipartFile.class),
                eq("albums/thumbnails"),
                eq("albums/thumbnails/old.jpg")))
                .willReturn("albums/thumbnails/new.jpg");
        given(albumTrackRepository.countByAlbum(album)).willReturn(0L);
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        AlbumResponse result = albumService.updateAlbum(1L, request, thumbnail);

        ArgumentCaptor<MultipartFile> replacement = ArgumentCaptor.forClass(MultipartFile.class);
        verify(storageMutationCoordinator).replace(
                eq(StorageDomain.ALBUM),
                eq(StorageRoot.PUBLIC),
                replacement.capture(),
                eq("albums/thumbnails"),
                eq("albums/thumbnails/old.jpg"));
        assertThat(replacement.getValue().getOriginalFilename()).isEqualTo("thumbnail.jpg");
        assertThat(replacement.getValue().getContentType()).isEqualTo("image/jpeg");
        assertThat(result.thumbnailUrl()).isEqualTo("albums/thumbnails/new.jpg");
    }

    @Test
    @DisplayName("updateAlbum rejection leaves the Album and storage unchanged")
    void updateAlbum_invalidThumbnail_leavesAlbumAndStorageUnchanged() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "Old Title");
        AlbumUpdateRequest request = new AlbumUpdateRequest();
        request.setTitle("New Title");
        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnailFile",
                "cover.svg",
                "image/svg+xml",
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        ReflectionTestUtils.setField(albumService, "canonicalImageService", new CanonicalImageService());

        assertThatThrownBy(() -> albumService.updateAlbum(1L, request, thumbnail))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
        assertThat(album.getTitle()).isEqualTo("Old Title");
        verify(storageMutationCoordinator, never()).replace(any(), any(), any(), any(), any());
        verify(albumTrackRepository, never()).countByAlbum(any());
    }

    // -- deleteAlbum() --------------------------------------------------------

    @Test
    @DisplayName("deleteAlbum() 성공 - 소프트 삭제 (isActive=false)")
    void deleteAlbum_success() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "To Delete");

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));

        albumService.deleteAlbum(1L);

        assertThat(album.isActive()).isFalse();
    }

    // -- addTrack() -----------------------------------------------------------

    @Test
    @DisplayName("addTrack() 성공 - 앨범에 트랙 추가")
    void addTrack_success() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "My Album");
        Track track = buildTrack(5L, true);

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(albumRepository.findById(1L)).willReturn(Optional.of(album));
        given(trackRepository.findById(5L)).willReturn(Optional.of(track));
        given(albumTrackRepository.existsByAlbumAndTrack(album, track)).willReturn(false);
        given(albumTrackRepository.countByAlbum(album)).willReturn(0L);
        given(albumTrackRepository.findAllPlayableByAlbumOrderByTrackOrder(album))
                .willReturn(List.of());

        albumService.addTrack(1L, new AlbumTrackAddRequest(5L));

        verify(albumTrackRepository).save(any(AlbumTrack.class));
    }

    @Test
    @DisplayName("addTrack() 실패 - 중복 트랙 → RESOURCE_DUPLICATE")
    void addTrack_duplicate_throws() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "My Album");
        Track track = buildTrack(5L, true);

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(trackRepository.findById(5L)).willReturn(Optional.of(track));
        given(albumTrackRepository.existsByAlbumAndTrack(album, track)).willReturn(true);

        assertThatThrownBy(() -> albumService.addTrack(1L, new AlbumTrackAddRequest(5L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_DUPLICATE));
    }

    // -- removeTrack() --------------------------------------------------------

    @Test
    @DisplayName("removeTrack() 성공 - 앨범에서 트랙 제거")
    void removeTrack_success() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "My Album");
        Track track = buildTrack(5L, true);
        AlbumTrackId atId = new AlbumTrackId(1L, 5L);
        AlbumTrack albumTrack = AlbumTrack.builder()
                .id(atId).album(album).track(track).trackOrder(0).build();

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(albumTrackRepository.findById(atId)).willReturn(Optional.of(albumTrack));
        given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album)).willReturn(List.of());

        albumService.removeTrack(1L, 5L);

        verify(albumTrackRepository).delete(albumTrack);
    }

    // -- reorderTracks() ------------------------------------------------------

    @Test
    @DisplayName("reorderTracks() 성공 - 트랙 순서 변경")
    void reorderTracks_success() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "My Album");
        Track track1 = buildTrack(1L, true);
        Track track2 = buildTrack(2L, true);

        AlbumTrackId atId1 = new AlbumTrackId(1L, 1L);
        AlbumTrackId atId2 = new AlbumTrackId(1L, 2L);
        AlbumTrack at1 = AlbumTrack.builder().id(atId1).album(album).track(track1).trackOrder(0).build();
        AlbumTrack at2 = AlbumTrack.builder().id(atId2).album(album).track(track2).trackOrder(1).build();

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album))
                .willReturn(List.of(at1, at2));

        AlbumTrackOrderRequest request = new AlbumTrackOrderRequest(List.of(
                new AlbumTrackOrderItem(1L, 1),
                new AlbumTrackOrderItem(2L, 0)
        ));

        AlbumDetailResponse result = albumService.reorderTracks(1L, request);

        assertThat(at1.getTrackOrder()).isEqualTo(1);
        assertThat(at2.getTrackOrder()).isEqualTo(0);
        assertThat(result.tracks()).hasSize(2);
    }

    @Test
    void reorderTracks_rejectsIncompleteMembership() {
        User user = buildUser(1L);
        Album album = buildAlbum(1L, user, "My Album");
        Track track = buildTrack(1L, true);
        AlbumTrack albumTrack = AlbumTrack.builder()
                .id(new AlbumTrackId(1L, 1L)).album(album).track(track).trackOrder(0).build();

        given(albumRepository.findByIdForUpdate(1L)).willReturn(Optional.of(album));
        given(albumTrackRepository.findAllByAlbumOrderByTrackOrder(album)).willReturn(List.of(albumTrack));

        assertThatThrownBy(() -> albumService.reorderTracks(
                1L,
                new AlbumTrackOrderRequest(List.of(new AlbumTrackOrderItem(2L, 0)))))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    // -- helpers --------------------------------------------------------------

    private User buildUser(Long id) {
        User user = User.builder()
                .email("admin@test.com").nickname("admin").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.ADMIN).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Album buildAlbum(Long id, User user, String title) {
        Album album = Album.builder().title(title).createdBy(user).build();
        ReflectionTestUtils.setField(album, "id", id);
        return album;
    }

    private Track buildTrack(Long id, boolean active) {
        Track track = Track.builder()
                .title("Test Track").bpm(120).tonality("C").audioFile("audio.mp3")
                .user(User.builder().nickname("Artist " + id).email("artist" + id + "@test.com").build())
                .isActive(active).build();
        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }

    private CustomUserDetails buildAdminDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id).email("admin@test.com").password("pw")
                .role(UserRole.ADMIN).isDeleted(false).isProfileComplete(true)
                .build();
    }

    private byte[] pngBytes(int width, int height) throws Exception {
        return imageBytes("png", width, height);
    }

    private byte[] jpegBytes(int width, int height) throws Exception {
        return imageBytes("jpg", width, height);
    }

    private byte[] imageBytes(String format, int width, int height) throws Exception {
        int imageType = "jpg".equals(format)
                ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage image = new BufferedImage(width, height, imageType);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private byte[] append(byte[] base, byte[] suffix) {
        byte[] result = Arrays.copyOf(base, base.length + suffix.length);
        System.arraycopy(suffix, 0, result, base.length, suffix.length);
        return result;
    }
}
