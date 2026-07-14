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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordLoginPolicy passwordLoginPolicy;

    @Value("${app.mail.from:noreply@atstudio.com}")
    private String fromAddress;

    @Value("${app.mail.base-url:http://localhost:5173}")
    private String baseUrl;

    // ── 이메일 인증 ──────────────────────────────────────────────

    @Transactional
    public void sendVerificationEmail(User user) {
        passwordLoginPolicy.ensureEnabled();

        // 기존 토큰 삭제 (재발송 대응)
        emailTokenRepository.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        emailTokenRepository.save(verificationToken);

        String verifyUrl = baseUrl + "/email-verify?token=" + token;
        String subject = "[ATStudio] 이메일 인증을 완료해주세요";
        String body = buildVerificationEmailBody(user.getNickname(), verifyUrl);

        sendEmail(user.getEmail(), subject, body);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.INVALID_TOKEN));

        if (verificationToken.isUsed()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_TOKEN);
        }
        if (verificationToken.isExpired()) {
            throw new BusinessException(BUSINESS_ERROR.TOKEN_EXPIRED);
        }

        verificationToken.markUsed();

        User user = verificationToken.getUser();
        user.verify();
    }

    // ── 비밀번호 재설정 ──────────────────────────────────────────

    @Transactional
    public void sendPasswordResetEmail(String email) {
        passwordLoginPolicy.ensureEnabled();

        // 존재하지 않는 이메일도 동일 응답 (계정 탐색 방지)
        userRepository.findByEmail(email).ifPresent(user -> {
            resetTokenRepository.deleteAllByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            resetTokenRepository.save(resetToken);

            String resetUrl = baseUrl + "/password-reset?token=" + token;
            String subject = "[ATStudio] 비밀번호 재설정 안내";
            String body = buildResetEmailBody(user.getNickname(), resetUrl);

            sendEmail(user.getEmail(), subject, body);
        });
    }

    public void sendSubscriptionPaymentFailureEmail(
            User user,
            String failureSummary,
            String retryGuide) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String subject = "[ATStudio] Subscription payment notice";
        String body = buildSubscriptionPaymentFailureEmailBody(
                user.getNickname(),
                failureSummary,
                retryGuide);
        sendEmail(user.getEmail(), subject, body);
    }

    public void sendPaymentReconciliationIncidentAlert(
            String operatorEmail,
            String summary,
            String details) {
        if (operatorEmail == null || operatorEmail.isBlank()) {
            return;
        }

        String subject = "[ATStudio] Payment reconciliation incident";
        String body = buildPaymentReconciliationIncidentEmailBody(summary, details);
        sendEmail(operatorEmail, subject, body);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        passwordLoginPolicy.ensureEnabled();

        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.INVALID_TOKEN));

        if (resetToken.isUsed()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_TOKEN);
        }
        if (resetToken.isExpired()) {
            throw new BusinessException(BUSINESS_ERROR.TOKEN_EXPIRED);
        }

        User user = userRepository.findByIdForUpdate(resetToken.getUser().getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));

        resetToken.markUsed();
        user.updatePassword(passwordEncoder.encode(newPassword));
        user.clearRefreshToken();
    }

    // ── 이메일 발송 ──────────────────────────────────────────────

    private void sendEmail(String to, String subject, String htmlBody) {
        String deliveryId = UUID.randomUUID().toString();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email delivery completed. deliveryId={}, outcome=SUCCESS", deliveryId);
        } catch (Exception e) {
            log.warn(
                    "Email delivery failed. deliveryId={}, outcome=FAILURE, exceptionClass={}",
                    deliveryId,
                    e.getClass().getName());
        }
    }

    // ── HTML 템플릿 ──────────────────────────────────────────────

    private String buildVerificationEmailBody(String nickname, String verifyUrl) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Apple SD Gothic Neo',sans-serif;">
                  <h2 style="color:#333;">이메일 인증</h2>
                  <p>안녕하세요, <strong>%s</strong>님!</p>
                  <p>아래 버튼을 클릭하여 이메일 인증을 완료해주세요.</p>
                  <a href="%s"
                     style="display:inline-block;padding:12px 24px;background:#6366f1;color:#fff;
                            border-radius:6px;text-decoration:none;font-weight:600;margin:16px 0;">
                    이메일 인증하기
                  </a>
                  <p style="color:#888;font-size:13px;">이 링크는 24시간 후 만료됩니다.</p>
                  <p style="color:#888;font-size:13px;">버튼이 작동하지 않으면 아래 URL을 브라우저에 복사해주세요:<br/>%s</p>
                </div>
                """.formatted(nickname, verifyUrl, verifyUrl);
    }

    private String buildResetEmailBody(String nickname, String resetUrl) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Apple SD Gothic Neo',sans-serif;">
                  <h2 style="color:#333;">비밀번호 재설정</h2>
                  <p>안녕하세요, <strong>%s</strong>님!</p>
                  <p>아래 버튼을 클릭하여 비밀번호를 재설정해주세요.</p>
                  <a href="%s"
                     style="display:inline-block;padding:12px 24px;background:#6366f1;color:#fff;
                            border-radius:6px;text-decoration:none;font-weight:600;margin:16px 0;">
                    비밀번호 재설정
                  </a>
                  <p style="color:#888;font-size:13px;">이 링크는 1시간 후 만료됩니다.</p>
                  <p style="color:#888;font-size:13px;">본인이 요청하지 않은 경우 이 이메일을 무시해주세요.</p>
                </div>
                """.formatted(nickname, resetUrl, resetUrl);
    }

    private String buildSubscriptionPaymentFailureEmailBody(
            String nickname,
            String failureSummary,
            String retryGuide) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Apple SD Gothic Neo',sans-serif;">
                  <h2 style="color:#333;">Subscription payment notice</h2>
                  <p>Hello <strong>%s</strong>,</p>
                  <p>%s</p>
                  <p>%s</p>
                  <p style="color:#888;font-size:13px;">
                    This message never includes card numbers, billing keys, auth keys, or provider secrets.
                  </p>
                </div>
                """.formatted(
                defaultText(nickname, "ATStudio user"),
                defaultText(failureSummary, "Your subscription renewal payment could not be completed."),
                defaultText(retryGuide, "Please check your payment method and try again from My Subscription."));
    }

    private String buildPaymentReconciliationIncidentEmailBody(String summary, String details) {
        return """
                <div style="max-width:640px;margin:0 auto;font-family:'Apple SD Gothic Neo',sans-serif;">
                  <h2 style="color:#333;">Payment reconciliation incident</h2>
                  <p>%s</p>
                  <pre style="white-space:pre-wrap;background:#f6f8fa;border:1px solid #d0d7de;
                              border-radius:6px;padding:12px;color:#24292f;">%s</pre>
                  <p style="color:#888;font-size:13px;">
                    This message never includes card numbers, billing keys, auth keys, customer keys, or provider secrets.
                  </p>
                </div>
                """.formatted(
                escapeHtml(defaultText(summary, "A payment reconciliation incident was detected.")),
                escapeHtml(defaultText(details, "-")));
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
