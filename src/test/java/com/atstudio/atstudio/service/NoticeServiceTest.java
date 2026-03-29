package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.notice.NoticeCreateRequest;
import com.atstudio.atstudio.dto.notice.NoticeListItemResponse;
import com.atstudio.atstudio.dto.notice.NoticeResponse;
import com.atstudio.atstudio.dto.notice.NoticeUpdateRequest;
import com.atstudio.atstudio.entity.Notice;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.NoticeRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService 단위 테스트")
class NoticeServiceTest {

    @Mock NoticeRepository noticeRepository;
    @Mock UserRepository userRepository;

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
        given(noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc(any())).willReturn(page);

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

        noticeService.deleteNotice(1L, buildAdminDetails(1L));

        verify(noticeRepository).delete(notice);
    }

    @Test
    @DisplayName("deleteNotice() 성공 - ADMIN이 타인 공지사항 삭제 허용")
    void deleteNotice_adminCanDeleteOtherNotice() {
        User author = buildUser(2L);
        Notice notice = buildNotice(1L, author, "제목", "내용", false);
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        noticeService.deleteNotice(1L, buildAdminDetails(1L));

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
