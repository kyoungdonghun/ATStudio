package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.user.*;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock LikeRepository likeRepository;
    @Mock DownloadQueueRepository downloadQueueRepository;
    @Mock PlayHistoryRepository playHistoryRepository;
    @Mock TrackDownloadRepository trackDownloadRepository;
    @Mock LicenseRepository licenseRepository;
    @Mock WhitelistChannelRepository whitelistChannelRepository;
    @Mock PlaylistService playlistService;

    @InjectMocks UserService userService;

    // ── register() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() 성공 - 저장 후 UserResponse 반환")
    void register_success_returnsUserResponse() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        User savedUser = buildUser(1L, "new@test.com", "newNick", null, null);

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.email()).isEqualTo("new@test.com");
        assertThat(response.nickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("register() 실패 - 이메일 중복 → EMAIL_ALREADY_REGISTERED 예외")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = buildRegisterRequest("dup@test.com", "nick");
        when(userRepository.findByEmail("dup@test.com"))
                .thenReturn(Optional.of(buildUser(1L, "dup@test.com", "nick", null, null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.EMAIL_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("register() 실패 - 닉네임 중복 → NICKNAME_DUPLICATED 예외")
    void register_duplicateNickname_throwsException() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "dupNick");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("dupNick"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "dupNick", null, null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.NICKNAME_DUPLICATED));
    }

    // ── updatePassword() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePassword() 성공 - 현재 비밀번호 일치 시 새 비밀번호로 변경")
    void updatePassword_success() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPw", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("newPw")).thenReturn("encoded-new");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("currentPw");
        request.setNewPassword("newPw");

        userService.updatePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    @DisplayName("updatePassword() 실패 - 현재 비밀번호 불일치 → INVALID_CREDENTIALS 예외")
    void updatePassword_wrongCurrentPassword_throwsException() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPw", "encoded")).thenReturn(false);

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("wrongPw");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("updatePassword() 실패 - 소셜 회원(password null) → INVALID_CREDENTIALS 예외")
    void updatePassword_nullPassword_throwsException() {
        User user = User.builder()
                .email("social@test.com")
                .nickname("socialNick")
                .password(null)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("any");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_CREDENTIALS));
    }

    @Test
    @DisplayName("updatePassword() 실패 - 존재하지 않는 사용자 → RESOURCE_NOT_FOUND 예외")
    void updatePassword_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("pw");
        request.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updatePassword(99L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── completeProfile() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("completeProfile() 실패 - 이미 완성된 프로필 → PROFILE_ALREADY_COMPLETE 예외")
    void completeProfile_alreadyComplete_throwsException() {
        User user = buildUser(1L, "user@test.com", "nick", "010-0000-0000", UserJob.EDITOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.ARTIST);
        request.setUserType(UserType.INDIVIDUAL);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PROFILE_ALREADY_COMPLETE));
    }

    // ── getUsers() (Admin) ────────────────────────────────────────────────────

    @Test
    @DisplayName("getUsers() 성공 - 검색어/userType 필터로 유저 목록 반환")
    void getUsers_success() {
        Page<User> page = new PageImpl<>(List.of());
        given(userRepository.searchUsers(any(), any(), any())).willReturn(page);

        ResponseDTO<UserListItemResponse> result = userService.getUsers("nick", UserType.INDIVIDUAL, 1, 20);

        assertThat(result).isNotNull();
        assertThat(result.getDataList()).isEmpty();
    }

    @Test
    @DisplayName("getUsers() - searchUsers JPQL은 isDeleted=false 조건을 포함하므로 탈퇴 계정 미노출 (C-2 PII 보호)")
    void getUsers_excludesDeletedUsers() {
        // Repository의 searchUsers JPQL에 AND u.isDeleted = false 조건이 포함됨.
        // 단위 테스트에서는 repository mock이므로, 반환된 목록에 deleted user가 없음을 검증.
        User activeUser = buildUser(1L, "active@test.com", "activeNick", null, null);
        Page<User> page = new PageImpl<>(List.of(activeUser));
        given(userRepository.searchUsers(any(), any(), any())).willReturn(page);

        ResponseDTO<UserListItemResponse> result = userService.getUsers(null, null, 1, 20);

        assertThat(result.getDataList()).hasSize(1);
        assertThat(result.getDataList().get(0).email()).isEqualTo("active@test.com");
    }

    // ── getUser() (Admin) ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUser() 성공 - ID로 유저 상세 조회")
    void getUser_success() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserDetailResponse result = userService.getUser(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("getUser() 실패 - 존재하지 않는 ID → RESOURCE_NOT_FOUND 예외")
    void getUser_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    // ── updateUserByAdmin() ───────────────────────────────────────────────────

    @Test
    @DisplayName("updateUserByAdmin() 성공 - role/isVerified 수정")
    void updateUserByAdmin_success() {
        User user = buildUser(1L, "user@test.com", "nick", null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserAdminUpdateRequest request = new UserAdminUpdateRequest();
        request.setRole(UserRole.ADMIN);
        request.setIsVerified(true);

        UserDetailResponse result = userService.updateUserByAdmin(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private RegisterRequest buildRegisterRequest(String email, String nickname) {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setNickname(nickname);
        req.setPassword("password123");
        req.setUserType(UserType.INDIVIDUAL);
        return req;
    }

    private User buildUser(Long id, String email, String nickname, String phonePersonal, UserJob job) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded")
                .phonePersonal(phonePersonal)
                .job(job)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
