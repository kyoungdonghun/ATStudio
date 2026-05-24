package com.atstudio.atstudio.service;

import com.atstudio.atstudio.entity.PaymentOperationAuditLog;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditTargetType;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentOperationAuditLogRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentOperationAuditLogService unit tests")
class PaymentOperationAuditLogServiceTest {

    @Mock PaymentOperationAuditLogRepository auditLogRepository;
    @Mock UserRepository userRepository;

    PaymentOperationAuditLogService service;

    @BeforeEach
    void setUp() {
        service = new PaymentOperationAuditLogService(auditLogRepository, userRepository);
    }

    @Test
    @DisplayName("recordReconciliationIncidentStatusUpdate stores actor and status transition")
    void recordReconciliationIncidentStatusUpdate() {
        User actor = user(99L, "admin", "admin@test.com", UserRole.ADMIN);
        User target = user(1L, "buyer", "buyer@test.com", UserRole.USER);
        PaymentReconciliationIncident incident = PaymentReconciliationIncident.builder()
                .dedupeKey("PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ORDER-1")
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.RESOLVED)
                .severity(PaymentReconciliationIncidentSeverity.CRITICAL)
                .user(target)
                .orderId("ORDER-1")
                .providerTransactionId("payment_key")
                .firstDetectedAt(LocalDateTime.of(2026, 5, 25, 10, 0))
                .lastDetectedAt(LocalDateTime.of(2026, 5, 25, 10, 0))
                .build();
        ReflectionTestUtils.setField(incident, "id", 1L);
        given(userRepository.findById(99L)).willReturn(Optional.of(actor));
        given(auditLogRepository.save(any(PaymentOperationAuditLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        service.recordReconciliationIncidentStatusUpdate(
                CustomUserDetails.builder()
                        .id(99L)
                        .email("admin@test.com")
                        .role(UserRole.ADMIN)
                        .build(),
                incident,
                PaymentReconciliationIncidentStatus.OPEN,
                PaymentReconciliationIncidentStatus.RESOLVED,
                "checked");

        ArgumentCaptor<PaymentOperationAuditLog> captor =
                ArgumentCaptor.forClass(PaymentOperationAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        PaymentOperationAuditLog log = captor.getValue();
        assertThat(log.getAction()).isEqualTo(PaymentOperationAuditAction.RECONCILIATION_INCIDENT_STATUS_UPDATE);
        assertThat(log.getTargetType()).isEqualTo(PaymentOperationAuditTargetType.RECONCILIATION_INCIDENT);
        assertThat(log.getTargetId()).isEqualTo(1L);
        assertThat(log.getActorUser()).isEqualTo(actor);
        assertThat(log.getTargetUser()).isEqualTo(target);
        assertThat(log.getBeforeStatus()).isEqualTo("OPEN");
        assertThat(log.getAfterStatus()).isEqualTo("RESOLVED");
        assertThat(log.getReasonCode()).isEqualTo("PROVIDER_DONE_LOCAL_NOT_FINALIZED");
    }

    private User user(Long id, String nickname, String email, UserRole role) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .role(role)
                .userType(UserType.INDIVIDUAL)
                .build();
    }
}
