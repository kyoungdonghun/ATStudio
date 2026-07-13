package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.entity.EmailVerificationToken;
import com.atstudio.atstudio.entity.PasswordResetToken;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.EmailVerificationTokenRepository;
import com.atstudio.atstudio.repository.PasswordResetTokenRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@DisplayName("EmailService 단위 테스트")
class EmailServiceTest {

    private static final String FROM_ADDRESS = "noreply@atstudio.test";
    private static final String BASE_URL = "https://atstudio.test";
    private static final String DELIVERY_ID_PATTERN =
            "deliveryId=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

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

    @Test
    @DisplayName("verification email success logs only non-sensitive delivery metadata")
    void sendVerificationEmail_success_logsOnlyNonSensitiveDeliveryMetadata(CapturedOutput output)
            throws Exception {
        String recipient = "verification+private@atstudio.test";
        String nickname = "private-verification-user";
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn(recipient);
        when(user.getNickname()).thenReturn(nickname);
        when(mailSender.createMimeMessage()).thenReturn(message);

        assertThatCode(() -> emailService.sendVerificationEmail(user))
                .doesNotThrowAnyException();

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailTokenRepository).save(tokenCaptor.capture());
        verify(mailSender).send(message);
        String secretToken = tokenCaptor.getValue().getToken();
        String secretUrl = BASE_URL + "/email-verify?token=" + secretToken;
        assertThat(output.getAll())
                .contains("outcome=SUCCESS")
                .containsPattern(DELIVERY_ID_PATTERN)
                .doesNotContain(recipient, message.getSubject(), nickname, secretToken, secretUrl);
    }

    @Test
    @DisplayName("password reset email failure logs correlation metadata without secrets or stack trace")
    void sendPasswordResetEmail_failure_logsCorrelationWithoutSecretsOrStackTrace(CapturedOutput output)
            throws Exception {
        String recipient = "reset+private@atstudio.test";
        String nickname = "private-reset-user";
        String providerMessage = "SMTP provider rejected a secret reset payload";
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn(recipient);
        when(user.getNickname()).thenReturn(nickname);
        when(userRepository.findByEmail(recipient)).thenReturn(Optional.of(user));
        when(mailSender.createMimeMessage()).thenReturn(message);
        doThrow(new MailSendException(providerMessage)).when(mailSender).send(message);

        assertThatCode(() -> emailService.sendPasswordResetEmail(recipient))
                .doesNotThrowAnyException();

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenRepository).save(tokenCaptor.capture());
        verify(mailSender).send(message);
        String secretToken = tokenCaptor.getValue().getToken();
        String secretUrl = BASE_URL + "/password-reset?token=" + secretToken;
        assertThat(output.getAll())
                .contains("outcome=FAILURE")
                .contains("exceptionClass=" + MailSendException.class.getName())
                .containsPattern(DELIVERY_ID_PATTERN)
                .doesNotContain(
                        recipient,
                        message.getSubject(),
                        nickname,
                        secretToken,
                        secretUrl,
                        providerMessage,
                        "\tat ");
    }

    private void setMailProperties() {
        ReflectionTestUtils.setField(emailService, "fromAddress", FROM_ADDRESS);
        ReflectionTestUtils.setField(emailService, "baseUrl", BASE_URL);
    }

    private MimeMessage newMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }
}
