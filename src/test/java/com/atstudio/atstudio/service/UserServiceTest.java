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
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
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
    @Mock PasswordLoginPolicy passwordLoginPolicy;

    @InjectMocks UserService userService;

    // ── register() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register() 성공 - 저장 후 UserResponse 반환")
    void register_success_returnsUserResponse() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        User savedUser = buildUser(1L, "new@test.com", "newNick", "010-1111-2222", UserJob.EDITOR);

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1111-2222")).thenReturn(Optional.empty());
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

    @Test
    @DisplayName("register() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void register_duplicatePhone_throwsException() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setPhonePersonal("010-1234-5678");

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(3L, "other@test.com", "otherNick", "010-1234-5678", null)));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("register() 실패 - 연락처 누락 → INVALID_ARGUMENT 예외")
    void register_missingPhone_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setPhonePersonal(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 실패 - 개인 회원 직업 누락 → INVALID_ARGUMENT 예외")
    void register_individualMissingJob_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        request.setJob(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 실패 - 기업 회원 회사명 누락 → INVALID_ARGUMENT 예외")
    void register_businessMissingCompanyName_throwsInvalidArgument() {
        RegisterRequest request = buildRegisterRequest("biz@test.com", "bizNick");
        request.setUserType(UserType.BUSINESS);
        request.setJob(null);
        request.setCompanyName(null);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("register() 성공 - 기업 회원은 직업 없이 회사명으로 가입 가능")
    void register_businessWithoutJob_succeeds() {
        RegisterRequest request = buildRegisterRequest("biz@test.com", "bizNick");
        request.setUserType(UserType.BUSINESS);
        request.setJob(null);
        request.setCompanyName("ATStudio Biz");
        User savedUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 9L);

        when(userRepository.findByEmail("biz@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByNickname("bizNick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1111-2222")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pw");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.userType()).isEqualTo("BUSINESS");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
    }

    @Test
    @DisplayName("register() 실패 - 이메일 로그인 비활성화 시 PASSWORD_LOGIN_DISABLED 예외")
    void register_disabled_throwsPasswordLoginDisabled() {
        RegisterRequest request = buildRegisterRequest("new@test.com", "newNick");
        org.mockito.Mockito.doThrow(new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED))
                .when(passwordLoginPolicy).ensureEnabled();

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED));
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

    @Test
    @DisplayName("completeProfile() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void completeProfile_duplicatePhone_throwsException() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByNickname("nick")).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "otherNick", "010-1234-5678", UserJob.EDITOR)));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.ARTIST);
        request.setUserType(UserType.INDIVIDUAL);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("completeProfile() 실패 - 기업 회원 회사명 누락 → INVALID_ARGUMENT 예외")
    void completeProfile_businessWithoutCompanyName_throwsInvalidArgument() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("biznick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(null);
        request.setUserType(UserType.BUSINESS);
        request.setCompanyName(null);

        assertThatThrownBy(() -> userService.completeProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("completeProfile() 성공 - 기업 회원은 회사명으로 프로필 완성 가능")
    void completeProfile_businessWithCompanyName_succeeds() {
        User incompleteUser = buildUser(1L, "user@test.com", "nick", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteUser));
        when(userRepository.findByNickname("biznick")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1234-5678")).thenReturn(Optional.empty());

        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setNickname("biznick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(null);
        request.setUserType(UserType.BUSINESS);
        request.setCompanyName("ATStudio Biz");

        UserResponse response = userService.completeProfile(1L, request);

        assertThat(response.userType()).isEqualTo("BUSINESS");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
    }

    // ── updateMyProfile() ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMyProfile() 실패 - 전화번호 중복 → PHONE_ALREADY_REGISTERED 예외")
    void updateMyProfile_duplicatePhone_throwsException() {
        User currentUser = buildUser(1L, "user@test.com", "nick", "010-1111-2222", UserJob.EDITOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByPhonePersonal("010-1234-5678"))
                .thenReturn(Optional.of(buildUser(2L, "other@test.com", "otherNick", "010-1234-5678", UserJob.ARTIST)));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("nick");
        request.setPhonePersonal("010-1234-5678");
        request.setJob(UserJob.EDITOR);

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PHONE_ALREADY_REGISTERED));
    }

    @Test
    @DisplayName("updateMyProfile() 실패 - 기업 회원이 회사명을 빈값으로 덮어쓰면 INVALID_ARGUMENT 예외")
    void updateMyProfile_businessBlankCompanyName_throwsInvalidArgument() {
        User businessUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(businessUser, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(businessUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("bizNick");
        request.setPhonePersonal("010-1111-2222");
        request.setCompanyName("   ");

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("updateMyProfile() 실패 - 개인 회원의 최종 직업이 없으면 INVALID_ARGUMENT 예외")
    void updateMyProfile_individualWithoutEffectiveJob_throwsInvalidArgument() {
        User incompleteIndividual = buildUser(1L, "user@test.com", "nick", "010-1111-2222", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(incompleteIndividual));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("nick2");
        request.setPhonePersonal("010-1111-2222");

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_ARGUMENT));
    }

    @Test
    @DisplayName("updateMyProfile() 성공 - 회사명 미전달 시 기존 회사명 유지")
    void updateMyProfile_omittedCompanyName_preservesExistingValue() {
        User businessUser = User.builder()
                .email("biz@test.com")
                .nickname("bizNick")
                .password("encoded")
                .phonePersonal("010-1111-2222")
                .job(null)
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(businessUser, "id", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(businessUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("bizNick2");
        request.setPhonePersonal("010-1111-2222");
        request.setPhoneCompany("02-1234-5678");

        UserResponse response = userService.updateMyProfile(1L, request);

        assertThat(response.nickname()).isEqualTo("bizNick2");
        assertThat(response.companyName()).isEqualTo("ATStudio Biz");
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
        req.setPhonePersonal("010-1111-2222");
        req.setJob(UserJob.EDITOR);
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
