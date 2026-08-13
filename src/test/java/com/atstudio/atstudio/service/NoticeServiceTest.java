package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.notice.NoticeAdminResponse;
import com.atstudio.atstudio.dto.notice.NoticeCreateRequest;
import com.atstudio.atstudio.dto.notice.NoticeListItemResponse;
import com.atstudio.atstudio.dto.notice.NoticeResponse;
import com.atstudio.atstudio.dto.notice.NoticeUpdateRequest;
import com.atstudio.atstudio.entity.Notice;
import com.atstudio.atstudio.entity.NoticeAttachment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.NoticeAttachmentRepository;
import com.atstudio.atstudio.repository.NoticeRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.storage.StorageDomain;
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageRoot;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService 단위 테스트")
class NoticeServiceTest {

    @Mock NoticeRepository noticeRepository;
    @Mock NoticeAttachmentRepository attachmentRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;

    @InjectMocks NoticeService noticeService;

    // ── createNotice() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createNotice() 성공 - 공지사항 생성 후 응답 반환")
    void createNotice_success() {
        User user = buildUser(1L);
        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("제목");
        request.setContent("내용");
        request.setIsPinned(false);
        Notice saved = buildNotice(1L, user, "제목", "내용", false);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.save(any(Notice.class))).willReturn(saved);

        NoticeResponse result = noticeService.createNotice(request, buildAdminDetails(1L));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("제목");
    }

    // ── getNotices() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getNotices() 성공 - 페이지네이션된 공지 목록 반환")
    void getNotices_success() {
        Page<Notice> page = new PageImpl<>(List.of());
        given(noticeRepository.findAll(any(org.springframework.data.domain.Pageable.class))).willReturn(page);

        ResponseDTO<NoticeListItemResponse> result = noticeService.getNotices(1, 20, "latest");

        assertThat(result).isNotNull();
        assertThat(result.getDataList()).isEmpty();
    }

    // ── getNotice() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getNotice() 성공 - ID로 공지 상세 조회")
    void getNotice_success() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        NoticeResponse result = noticeService.getNotice(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("getNotice() 실패 - 존재하지 않는 ID → RESOURCE_NOT_FOUND 예외")
    void getNotice_notFound() {
        given(noticeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNotice(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── updateNotice() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateNotice() 성공 - ADMIN이 공지사항 제목/내용/핀 수정")
    void updateNotice_success() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "구제목", "구내용", false);
        NoticeUpdateRequest request = buildUpdateRequest("새제목", "새내용", true);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        NoticeResponse result = noticeService.updateNotice(1L, request, buildAdminDetails(1L));

        assertThat(result.title()).isEqualTo("새제목");
        assertThat(result.isPinned()).isTrue();
    }

    @Test
    @DisplayName("updateNotice() 성공 - ADMIN이 타인 공지사항 수정 허용")
    void updateNotice_adminCanUpdateOtherNotice() {
        User author = buildUser(2L);
        Notice notice = buildNotice(1L, author, "구제목", "구내용", false);
        NoticeUpdateRequest request = buildUpdateRequest("새제목", "새내용", true);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        NoticeResponse result = noticeService.updateNotice(1L, request, buildAdminDetails(1L));

        assertThat(result.title()).isEqualTo("새제목");
    }

    @Test
    @DisplayName("updateNotice() 실패 - 비ADMIN 타인 공지사항 수정 → RESOURCE_NOT_ACCESS 예외")
    void updateNotice_nonAdminCannotUpdateOtherNotice() {
        User author = buildUser(2L);
        Notice notice = buildNotice(1L, author, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        assertThatThrownBy(() -> noticeService.updateNotice(1L,
                buildUpdateRequest("t", "c", false), buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
    }

    @Test
    @DisplayName("updateNotice() 실패 - 존재하지 않는 ID → RESOURCE_NOT_FOUND 예외")
    void updateNotice_notFound() {
        given(noticeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.updateNotice(99L,
                buildUpdateRequest("t", "c", false), buildAdminDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── deleteNotice() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteNotice() 성공 - ADMIN이 공지사항 삭제")
    void deleteNotice_success() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        noticeService.deleteNotice(1L, buildAdminDetails(1L));

        verify(attachmentRepository).deleteAllByNotice(notice);
        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("deleteNotice() 성공 - ADMIN이 타인 공지사항 삭제 허용")
    void deleteNotice_adminCanDeleteOtherNotice() {
        User author = buildUser(2L);
        Notice notice = buildNotice(1L, author, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        noticeService.deleteNotice(1L, buildAdminDetails(1L));

        verify(attachmentRepository).deleteAllByNotice(notice);
        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("deleteNotice() 실패 - 비ADMIN 타인 공지사항 삭제 → RESOURCE_NOT_ACCESS 예외")
    void deleteNotice_nonAdminCannotDeleteOtherNotice() {
        User author = buildUser(2L);
        Notice notice = buildNotice(1L, author, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        assertThatThrownBy(() -> noticeService.deleteNotice(1L, buildUserDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_ACCESS));
    }

    @Test
    @DisplayName("deleteNotice() 실패 - 존재하지 않는 ID → RESOURCE_NOT_FOUND 예외")
    void deleteNotice_notFound() {
        given(noticeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.deleteNotice(99L, buildAdminDetails(1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createNotice stores accepted active content only in PRIVATE storage")
    void createNotice_activeContentAttachment_storedOnlyInPrivateRoot() {
        User user = buildUser(1L);
        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("Notice");
        request.setContent("Content");
        request.setIsPinned(false);
        byte[] payload = "<html><script>alert(1)</script>".getBytes(
                java.nio.charset.StandardCharsets.UTF_8);
        MockMultipartFile attachment = new MockMultipartFile(
                "attachments",
                "announcement.html",
                "text/html",
                payload);
        request.setAttachments(List.of(attachment));
        Notice saved = buildNotice(1L, user, "Notice", "Content", false);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.save(any(Notice.class))).willReturn(saved);
        given(storageMutationCoordinator.storeAll(
                StorageDomain.NOTICE,
                StorageRoot.PRIVATE,
                List.of(attachment),
                "notices/attachments"))
                .willReturn(List.of("notices/attachments/generated.html"));
        given(attachmentRepository.save(any(NoticeAttachment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        NoticeResponse result = noticeService.createNotice(request, buildAdminDetails(1L));

        assertThat(result.attachments()).singleElement()
                .satisfies(savedAttachment -> {
                    assertThat(savedAttachment.originalName()).isEqualTo("announcement.html");
                    assertThat(savedAttachment.fileSize()).isEqualTo((long) payload.length);
                });
        verify(storageMutationCoordinator).storeAll(
                StorageDomain.NOTICE,
                StorageRoot.PRIVATE,
                List.of(attachment),
                "notices/attachments");
        verify(storageMutationCoordinator, never()).storeAll(
                eq(StorageDomain.NOTICE),
                eq(StorageRoot.PUBLIC),
                anyList(),
                anyString());
    }

    @Test
    @DisplayName("updateNotice deletes selected attachments from PRIVATE storage after commit")
    void updateNotice_attachmentDeletion_usesPrivateRoot() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "Old", "Content", false);
        NoticeAttachment attachment = buildAttachment(
                7L,
                notice,
                "announcement.html",
                "notices/attachments/private.html");
        NoticeUpdateRequest request = buildUpdateRequest("New", "Content", false);
        request.setDeleteAttachmentIds(List.of(7L));

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L))
                .willReturn(List.of(attachment), List.of());

        noticeService.updateNotice(1L, request, buildAdminDetails(1L));

        verify(storageMutationCoordinator).deleteAfterCommit(
                StorageDomain.NOTICE,
                StorageRoot.PRIVATE,
                List.of("notices/attachments/private.html"));
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    @DisplayName("deleteNotice schedules every attachment delete against PRIVATE storage")
    void deleteNotice_withAttachments_usesPrivateRoot() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "Notice", "Content", false);
        NoticeAttachment attachment = buildAttachment(
                7L,
                notice,
                "announcement.html",
                "notices/attachments/private.html");
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of(attachment));

        noticeService.deleteNotice(1L, buildAdminDetails(1L));

        verify(storageMutationCoordinator).deleteAfterCommit(
                StorageDomain.NOTICE,
                StorageRoot.PRIVATE,
                List.of("notices/attachments/private.html"));
    }

    @Test
    @DisplayName("downloadAttachment resolves the DB-owned key only from PRIVATE storage")
    void downloadAttachment_usesPrivateRootOnly() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "Notice", "Content", false);
        NoticeAttachment attachment = buildAttachment(
                7L,
                notice,
                "announcement.html",
                "notices/attachments/private.html");
        Resource resource = new ByteArrayResource(new byte[] {1, 2, 3});

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findByIdAndNoticeId(7L, 1L)).willReturn(Optional.of(attachment));
        given(storageService.loadAsResource(
                StorageRoot.PRIVATE,
                "notices/attachments/private.html"))
                .willReturn(resource);

        assertThat(noticeService.downloadAttachment(1L, 7L)).isSameAs(resource);
        verify(storageService).loadAsResource(
                StorageRoot.PRIVATE,
                "notices/attachments/private.html");
        verify(storageService, never()).loadAsResource(
                StorageRoot.PUBLIC,
                "notices/attachments/private.html");
    }

    @Test
    @DisplayName("Public detail increments viewCount exactly once")
    void getNotice_incrementsViewCountExactlyOnce() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "Notice", "Content", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(1L)).willReturn(List.of());

        NoticeResponse result = noticeService.getNotice(1L);

        assertThat(result.viewCount()).isEqualTo(1L);
        assertThat(notice.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ADMIN detail returns the edit projection without incrementing viewCount")
    void getNoticeForAdmin_doesNotIncrementViewCount() {
        User user = buildUser(1L);
        Notice notice = buildNotice(1L, user, "Notice", "Content", true);
        NoticeRepository.AdminEditRow row = org.mockito.Mockito.mock(NoticeRepository.AdminEditRow.class);
        given(row.getTitle()).willReturn("Notice");
        given(row.getContent()).willReturn("Content");
        given(row.getIsPinned()).willReturn(true);
        given(row.getAttachmentId()).willReturn(7L);
        given(row.getAttachmentOriginalName()).willReturn("guide.pdf");
        given(row.getAttachmentFileSize()).willReturn(3L);
        given(noticeRepository.findAdminEditRowsById(1L)).willReturn(List.of(row));

        NoticeAdminResponse result = noticeService.getNoticeForAdmin(1L);

        assertThat(result.title()).isEqualTo("Notice");
        assertThat(result.content()).isEqualTo("Content");
        assertThat(result.isPinned()).isTrue();
        assertThat(result.attachments()).singleElement()
                .satisfies(item -> assertThat(item.originalName()).isEqualTo("guide.pdf"));
        assertThat(notice.getViewCount()).isZero();
        verify(noticeRepository).findAdminEditRowsById(1L);
        verify(noticeRepository, never()).findById(1L);
        verify(attachmentRepository, never()).findAllByNoticeId(1L);
    }

    @Test
    @DisplayName("ADMIN detail returns RESOURCE_NOT_FOUND when the projection is empty")
    void getNoticeForAdmin_missingProjection_throwsNotFound() {
        given(noticeRepository.findAdminEditRowsById(99L)).willReturn(List.of());

        assertThatThrownBy(() -> noticeService.getNoticeForAdmin(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        verify(attachmentRepository, never()).findAllByNoticeId(99L);
    }

    private User buildUser(Long id) {
        User user = User.builder()
                .email("admin@test.com").nickname("admin").password("pw")
                .userType(UserType.INDIVIDUAL).role(UserRole.ADMIN).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Notice buildNotice(Long id, User user, String title, String content, boolean isPinned) {
        Notice notice = Notice.builder().user(user).title(title).content(content).isPinned(isPinned).build();
        ReflectionTestUtils.setField(notice, "id", id);
        return notice;
    }

    private NoticeAttachment buildAttachment(
            Long id,
            Notice notice,
            String originalName,
            String filePath) {
        NoticeAttachment attachment = NoticeAttachment.builder()
                .notice(notice)
                .originalName(originalName)
                .filePath(filePath)
                .fileSize(3L)
                .build();
        ReflectionTestUtils.setField(attachment, "id", id);
        return attachment;
    }

    private CustomUserDetails buildAdminDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id).email("admin@test.com").password("pw")
                .role(UserRole.ADMIN).isDeleted(false).isProfileComplete(true)
                .build();
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id).email("user@test.com").password("pw")
                .role(UserRole.USER).isDeleted(false).isProfileComplete(true)
                .build();
    }

    private NoticeUpdateRequest buildUpdateRequest(String title, String content, boolean isPinned) {
        NoticeUpdateRequest req = new NoticeUpdateRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setIsPinned(isPinned);
        return req;
    }
}
