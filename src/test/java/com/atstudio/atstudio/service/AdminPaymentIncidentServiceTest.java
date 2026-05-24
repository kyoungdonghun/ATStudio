package com.atstudio.atstudio.service;

import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationIncidentResponse;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentSeverity;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIssueType;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPaymentIncidentService unit tests")
class AdminPaymentIncidentServiceTest {

    @Mock PaymentReconciliationIncidentRepository incidentRepository;
    @Mock PaymentReconciliationIncidentService incidentService;
    @Mock PaymentOperationAuditLogService auditLogService;

    AdminPaymentIncidentService service;

    @BeforeEach
    void setUp() {
        service = new AdminPaymentIncidentService(incidentRepository, incidentService, auditLogService);
    }

    @Test
    @DisplayName("updateIncidentStatus records admin audit log with before and after status")
    void updateIncidentStatus_recordsAuditLog() {
        PaymentReconciliationIncident incident = incident();
        CustomUserDetails admin = CustomUserDetails.builder()
                .id(99L)
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .build();

        given(incidentRepository.findById(1L)).willReturn(Optional.of(incident));
        given(incidentService.changeStatus(
                1L,
                PaymentReconciliationIncidentStatus.RESOLVED,
                "provider and local ledger matched"))
                .willAnswer(invocation -> {
                    incident.changeStatus(
                            PaymentReconciliationIncidentStatus.RESOLVED,
                            "provider and local ledger matched",
                            LocalDateTime.of(2026, 5, 25, 12, 0));
                    return incident;
                });

        AdminPaymentReconciliationIncidentResponse response = service.updateIncidentStatus(
                1L,
                admin,
                PaymentReconciliationIncidentStatus.RESOLVED,
                "provider and local ledger matched").getData();

        assertThat(response.status()).isEqualTo(PaymentReconciliationIncidentStatus.RESOLVED);
        verify(auditLogService).recordReconciliationIncidentStatusUpdate(
                eq(admin),
                eq(incident),
                eq(PaymentReconciliationIncidentStatus.OPEN),
                eq(PaymentReconciliationIncidentStatus.RESOLVED),
                eq("provider and local ledger matched"));
    }

    private PaymentReconciliationIncident incident() {
        User user = User.builder()
                .id(1L)
                .nickname("buyer")
                .email("buyer@test.com")
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        PaymentReconciliationIncident incident = PaymentReconciliationIncident.builder()
                .dedupeKey("PROVIDER_DONE_LOCAL_NOT_FINALIZED:order:ORDER-1")
                .issueType(PaymentReconciliationIssueType.PROVIDER_DONE_LOCAL_NOT_FINALIZED)
                .status(PaymentReconciliationIncidentStatus.OPEN)
                .severity(PaymentReconciliationIncidentSeverity.CRITICAL)
                .user(user)
                .orderId("ORDER-1")
                .firstDetectedAt(LocalDateTime.of(2026, 5, 25, 10, 0))
                .lastDetectedAt(LocalDateTime.of(2026, 5, 25, 10, 0))
                .build();
        ReflectionTestUtils.setField(incident, "id", 1L);
        return incident;
    }
}
