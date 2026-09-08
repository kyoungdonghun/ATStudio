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
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
    @DisplayName("resetPassword() 성공 - 사용자 행 잠금 후 비밀번호와 refresh session을 함께 변경")
    void resetPassword_success_revokesRefreshSession() {
        User user = User.builder()
                .email("reset@test.com")
                .nickname("reset-user")
                .password("encoded-old")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.updateRefreshToken("stored-refresh-hash");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token("reset-fixture")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(resetTokenRepository.findByToken("reset-fixture")).thenReturn(Optional.of(resetToken));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new");

        emailService.resetPassword("reset-fixture", "new-password");

        assertThat(resetToken.isUsed()).isTrue();
        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(user.getRefreshToken()).isNull();
        verify(userRepository).findByIdForUpdate(1L);
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
        assertThat(message.getSubject()).isEqualTo("[AT.M] 이메일 인증을 완료해주세요");
        String secretToken = tokenCaptor.getValue().getToken();
        String secretUrl = BASE_URL + "/email-verify?token=" + secretToken;
        assertThat(output.getAll())
                .contains("outcome=SUCCESS")
                .containsPattern(DELIVERY_ID_PATTERN)
                .doesNotContain(recipient, message.getSubject(), nickname, secretToken, secretUrl);
    }

    @Test
    @DisplayName("verification email escapes an adversarial nickname and preserves its callback URL")
    void sendVerificationEmail_escapesNicknameAndPreservesCallbackUrl() throws Exception {
        String nickname = "<img src=x onerror=\"alert('verify')\"> & user";
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn("verification@atstudio.test");
        when(user.getNickname()).thenReturn(nickname);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendVerificationEmail(user);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(emailTokenRepository).save(tokenCaptor.capture());
        String verifyUrl = BASE_URL + "/email-verify?token=" + tokenCaptor.getValue().getToken();
        assertThat(bodyText(message))
                .contains("&lt;img src=x onerror=&quot;alert(&#39;verify&#39;)&quot;&gt; &amp; user")
                .contains("href=\"" + verifyUrl + "\"")
                .contains("<br/>" + verifyUrl)
                .doesNotContain(nickname);
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
        assertThat(message.getSubject()).isEqualTo("[AT.M] 비밀번호 재설정 안내");
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

    @Test
    @DisplayName("password reset email escapes an adversarial nickname and preserves its callback URL")
    void sendPasswordResetEmail_escapesNicknameAndPreservesCallbackUrl() throws Exception {
        String recipient = "reset@atstudio.test";
        String nickname = "\"><svg onload='reset()'> & user";
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn(recipient);
        when(user.getNickname()).thenReturn(nickname);
        when(userRepository.findByEmail(recipient)).thenReturn(Optional.of(user));
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendPasswordResetEmail(recipient);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenRepository).save(tokenCaptor.capture());
        String resetUrl = BASE_URL + "/password-reset?token=" + tokenCaptor.getValue().getToken();
        assertThat(bodyText(message))
                .contains("&quot;&gt;&lt;svg onload=&#39;reset()&#39;&gt; &amp; user")
                .contains("href=\"" + resetUrl + "\"")
                .doesNotContain(nickname);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    @DisplayName("subscription payment email preserves Korean UTF-8 subject, greeting and fallbacks")
    void sendSubscriptionPaymentFailureEmail_usesKoreanFallbacks(String blank) throws Exception {
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn("subscriber@atstudio.test");
        when(user.getNickname()).thenReturn(blank);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendSubscriptionPaymentFailureEmail(user, blank, blank);

        verify(mailSender).send(message);
        MimeMessage delivered = mimeRoundTrip(message);
        assertThat(delivered.getSubject()).isEqualTo("[AT.M] 구독 결제 안내");
        assertThat(bodyText(delivered))
                .contains("구독 결제 안내", "안녕하세요, <strong>회원</strong>님!")
                .contains("구독 갱신 결제를 완료하지 못했습니다.")
                .contains("내 구독에서 결제 상태와 등록된 결제 수단을 확인해 주세요.")
                .doesNotContain("Subscription payment notice", "AT.M user", "billing keys", "auth keys");
    }

    @Test
    @DisplayName("subscription payment email escapes every adversarial dynamic field")
    void sendSubscriptionPaymentFailureEmail_escapesAllDynamicFields() throws Exception {
        String nickname = "<b onclick=\"nickname()\">회원 & 사용자</b>";
        String failureSummary = "결제 실패 </p><script>alert('summary')</script>";
        String retryGuide = "<a href=\"https://evil.invalid\">재시도 & 결제</a>";
        User user = mock(User.class);
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(user.getEmail()).thenReturn("subscriber@atstudio.test");
        when(user.getNickname()).thenReturn(nickname);
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendSubscriptionPaymentFailureEmail(user, failureSummary, retryGuide);

        String body = bodyText(mimeRoundTrip(message));
        assertThat(body)
                .contains("&lt;b onclick=&quot;nickname()&quot;&gt;회원 &amp; 사용자&lt;/b&gt;")
                .contains("결제 실패 &lt;/p&gt;&lt;script&gt;alert(&#39;summary&#39;)&lt;/script&gt;")
                .contains("&lt;a href=&quot;https://evil.invalid&quot;&gt;재시도 &amp; 결제&lt;/a&gt;")
                .doesNotContain(nickname, failureSummary, retryGuide);
    }

    @Test
    @DisplayName("payment reconciliation alert preserves Korean UTF-8 labels and escaping")
    void sendPaymentReconciliationIncidentAlert_preservesAtMBrandAndEscaping() throws Exception {
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(mailSender.createMimeMessage()).thenReturn(message);

        String summary = "<strong>결제 점검 이슈 & 상세</strong>";
        String details = "상태=OPEN \"><script>alert('incident')</script>";
        emailService.sendPaymentReconciliationIncidentAlert(
                "operator@atstudio.test",
                summary,
                details);

        verify(mailSender).send(message);
        MimeMessage delivered = mimeRoundTrip(message);
        assertThat(delivered.getSubject()).isEqualTo("[AT.M] 결제 점검 이슈");
        assertThat(bodyText(delivered))
                .contains("결제 점검 이슈", "민감한 정보는 이메일로 공유하지 마세요.")
                .contains("&lt;strong&gt;결제 점검 이슈 &amp; 상세&lt;/strong&gt;")
                .contains("상태=OPEN &quot;&gt;&lt;script&gt;alert(&#39;incident&#39;)&lt;/script&gt;")
                .doesNotContain(summary, details);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    @DisplayName("payment reconciliation alert uses Korean fallback text")
    void sendPaymentReconciliationIncidentAlert_usesKoreanFallbacks(String blank) throws Exception {
        MimeMessage message = newMimeMessage();
        setMailProperties();
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendPaymentReconciliationIncidentAlert("operator@atstudio.test", blank, blank);

        verify(mailSender).send(message);
        assertThat(bodyText(mimeRoundTrip(message)))
                .contains("결제 점검 이슈가 감지되었습니다.", ">-</pre>")
                .doesNotContain("Payment reconciliation incident", "This message never includes");
    }

    private MimeMessage mimeRoundTrip(MimeMessage message) throws Exception {
        message.saveChanges();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        assertThat(output.toString(StandardCharsets.US_ASCII)).contains("charset=UTF-8");
        return new MimeMessage(
                Session.getInstance(new Properties()),
                new ByteArrayInputStream(output.toByteArray()));
    }

    private void setMailProperties() {
        ReflectionTestUtils.setField(emailService, "fromAddress", FROM_ADDRESS);
        ReflectionTestUtils.setField(emailService, "baseUrl", BASE_URL);
    }

    private MimeMessage newMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private String bodyText(Part part) throws Exception {
        Object content = part.getContent();
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof Multipart multipart) {
            StringBuilder body = new StringBuilder();
            for (int index = 0; index < multipart.getCount(); index++) {
                body.append(bodyText(multipart.getBodyPart(index)));
            }
            return body.toString();
        }
        return "";
    }
}
