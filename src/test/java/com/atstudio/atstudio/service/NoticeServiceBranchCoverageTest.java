package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.notice.NoticeCreateRequest;
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
import com.atstudio.atstudio.service.storage.StorageMutationCoordinator;
import com.atstudio.atstudio.service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceBranchCoverageTest {

    @Mock NoticeRepository noticeRepository;
    @Mock NoticeAttachmentRepository attachmentRepository;
    @Mock UserRepository userRepository;
    @Mock StorageService storageService;
    @Mock StorageMutationCoordinator storageMutationCoordinator;

    @InjectMocks NoticeService noticeService;

    @Test
    void viewsSortClampsInvalidPageAndSizeAtTheServiceBoundary() {
        given(noticeRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

        noticeService.getNotices(-4, 0, "views");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(noticeRepository).findAll(captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(1);
        assertThat(pageable.getSort().getOrderFor("isPinned").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getSort().getOrderFor("viewCount").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNull();
    }

    @Test
    void createPersistsOnlyNonEmptyAttachments() {
        User author = user(11L);
        Notice notice = notice(21L, author);
        MultipartFile empty = org.mockito.Mockito.mock(MultipartFile.class);
        MultipartFile upload = org.mockito.Mockito.mock(MultipartFile.class);
        given(empty.isEmpty()).willReturn(true);
        given(upload.isEmpty()).willReturn(false);
        given(upload.getOriginalFilename()).willReturn("guide.pdf");
        given(upload.getSize()).willReturn(512L);
        given(userRepository.findById(11L)).willReturn(Optional.of(author));
        given(noticeRepository.save(any(Notice.class))).willReturn(notice);
        given(storageMutationCoordinator.storeAll(any(), any(), anyList(), any()))
                .willReturn(List.of("notices/attachments/stored-guide.pdf"));
        given(attachmentRepository.save(any(NoticeAttachment.class)))
                .willAnswer(invocation -> {
                    NoticeAttachment saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 31L);
                    return saved;
                });

        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("Maintenance");
        request.setContent("Scheduled work");
        request.setIsPinned(false);
        request.setAttachments(Arrays.asList(null, empty, upload));

        var response = noticeService.createNotice(request, details(11L, UserRole.ADMIN));

        ArgumentCaptor<List<MultipartFile>> files = ArgumentCaptor.forClass(List.class);
        verify(storageMutationCoordinator).storeAll(any(), any(), files.capture(), any());
        assertThat(files.getValue()).containsExactly(upload);
        assertThat(response.attachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.originalName()).isEqualTo("guide.pdf");
            assertThat(attachment.fileSize()).isEqualTo(512L);
        });
    }

    @Test
    void emptyAttachmentListSkipsStorageMutation() {
        User author = user(11L);
        Notice notice = notice(21L, author);
        given(userRepository.findById(11L)).willReturn(Optional.of(author));
        given(noticeRepository.save(any(Notice.class))).willReturn(notice);

        NoticeCreateRequest request = new NoticeCreateRequest();
        request.setTitle("Maintenance");
        request.setContent("Scheduled work");
        request.setIsPinned(false);
        request.setAttachments(List.of());

        noticeService.createNotice(request, details(11L, UserRole.ADMIN));

        verify(storageMutationCoordinator, never()).storeAll(any(), any(), anyList(), any());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void ownerUpdateDeletesOnlyExplicitlySelectedAttachments() {
        User author = user(11L);
        Notice notice = notice(21L, author);
        NoticeAttachment removed = attachment(31L, notice, "notices/old.pdf");
        NoticeAttachment retained = attachment(32L, notice, "notices/keep.pdf");
        given(noticeRepository.findById(21L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findAllByNoticeId(21L)).willReturn(List.of(removed, retained));

        NoticeUpdateRequest request = new NoticeUpdateRequest();
        request.setTitle("Updated");
        request.setContent("Updated content");
        request.setIsPinned(true);
        request.setDeleteAttachmentIds(List.of(31L, 999L));
        request.setNewAttachments(List.of());

        var response = noticeService.updateNotice(21L, request, details(11L, UserRole.USER));

        assertThat(response.title()).isEqualTo("Updated");
        verify(storageMutationCoordinator).deleteAfterCommit(any(), any(),
                org.mockito.ArgumentMatchers.eq(List.of("notices/old.pdf")));
        verify(attachmentRepository).delete(removed);
        verify(attachmentRepository, never()).delete(retained);
    }

    @Test
    void attachmentLookupCannotCrossNoticeBoundary() {
        User author = user(11L);
        given(noticeRepository.findById(21L)).willReturn(Optional.of(notice(21L, author)));
        given(attachmentRepository.findByIdAndNoticeId(31L, 21L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.downloadAttachment(21L, 31L))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        verify(storageService, never()).loadAsResource(any(), any());
    }

    @Test
    void attachmentDownloadUsesTheValidatedStoredPath() {
        User author = user(11L);
        Notice notice = notice(21L, author);
        NoticeAttachment attachment = attachment(31L, notice, "notices/guide.pdf");
        Resource resource = org.mockito.Mockito.mock(Resource.class);
        given(noticeRepository.findById(21L)).willReturn(Optional.of(notice));
        given(attachmentRepository.findByIdAndNoticeId(31L, 21L)).willReturn(Optional.of(attachment));
        given(storageService.loadAsResource(any(), any())).willReturn(resource);

        assertThat(noticeService.downloadAttachment(21L, 31L)).isSameAs(resource);
        verify(storageService).loadAsResource(any(), org.mockito.ArgumentMatchers.eq("notices/guide.pdf"));
    }

    private User user(Long id) {
        User user = User.builder()
                .email("notice-author@test.com")
                .nickname("notice-author")
                .password("encoded")
                .userType(UserType.INDIVIDUAL)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Notice notice(Long id, User author) {
        Notice notice = Notice.builder()
                .user(author)
                .title("Original")
                .content("Original content")
                .isPinned(false)
                .build();
        ReflectionTestUtils.setField(notice, "id", id);
        return notice;
    }

    private NoticeAttachment attachment(Long id, Notice notice, String path) {
        NoticeAttachment attachment = NoticeAttachment.builder()
                .notice(notice)
                .originalName("attachment.pdf")
                .filePath(path)
                .fileSize(128L)
                .build();
        ReflectionTestUtils.setField(attachment, "id", id);
        return attachment;
    }

    private CustomUserDetails details(Long id, UserRole role) {
        return CustomUserDetails.builder()
                .id(id)
                .email("notice-user@test.com")
                .password("encoded")
                .role(role)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();
    }
}
