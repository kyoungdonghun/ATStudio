package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.repository.EmailVerificationTokenRepository;
import com.atstudio.atstudio.repository.PasswordResetTokenRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService 단위 테스트")
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock EmailVerificationTokenRepository emailTokenRepository;
    @Mock PasswordResetTokenRepository resetTokenRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock PasswordLoginPolicy passwordLoginPolicy;

    @InjectMocks EmailService emailService;

    @Test
    @DisplayName("sendPasswordResetEmail() 실패 - 이메일 로그인 비활성화 시 PASSWORD_LOGIN_DISABLED 예외")
    void sendPasswordResetEmail_disabled_throwsPasswordLoginDisabled() {
        doThrow(new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED))
                .when(passwordLoginPolicy).ensureEnabled();

        assertThatThrownBy(() -> emailService.sendPasswordResetEmail("user@test.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED));
    }

    @Test
    @DisplayName("resetPassword() 실패 - 이메일 로그인 비활성화 시 PASSWORD_LOGIN_DISABLED 예외")
    void resetPassword_disabled_throwsPasswordLoginDisabled() {
        doThrow(new BusinessException(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED))
                .when(passwordLoginPolicy).ensureEnabled();

        assertThatThrownBy(() -> emailService.resetPassword("token", "new-password"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.PASSWORD_LOGIN_DISABLED));
    }
}
