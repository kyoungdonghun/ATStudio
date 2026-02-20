package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.user.CompleteProfileRequest;
import com.atstudio.atstudio.dto.user.RegisterRequest;
import com.atstudio.atstudio.dto.user.UserResponse;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

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

    // ── completeProfile() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("completeProfile() 실패 - 이미 완성된 프로필 → PROFILE_ALREADY_COMPLETE 예외")
    void completeProfile_alreadyComplete_throwsException() {
        // phonePersonal + job 모두 있으면 isProfileComplete() = true
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
