package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.dto.payment.AdminPaymentReconciliationIncidentResponse;
import com.atstudio.atstudio.entity.PaymentReconciliationIncident;
import com.atstudio.atstudio.entity.enums.PaymentReconciliationIncidentStatus;
import com.atstudio.atstudio.repository.PaymentReconciliationIncidentRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPaymentIncidentService {

    private final PaymentReconciliationIncidentRepository incidentRepository;
    private final PaymentReconciliationIncidentService incidentService;
    private final PaymentOperationAuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentReconciliationIncidentResponse> listIncidents(
            PaymentReconciliationIncidentStatus status,
            int page,
            int size) {
        Pageable pageable = pageable(page, size);
        Page<AdminPaymentReconciliationIncidentResponse> result = findIncidents(status, pageable)
                .map(AdminPaymentReconciliationIncidentResponse::from);
        return ResponseDTO.<AdminPaymentReconciliationIncidentResponse>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentReconciliationIncidentResponse> updateIncidentStatus(
            Long incidentId,
            CustomUserDetails actorDetails,
            PaymentReconciliationIncidentStatus status,
            String note) {
        PaymentReconciliationIncident existing = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new com.atstudio.atstudio.common.exception.BusinessException(
                        com.atstudio.atstudio.common.exception.BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        PaymentReconciliationIncidentStatus beforeStatus = existing.getStatus();
        PaymentReconciliationIncident incident = incidentService.changeStatus(incidentId, status, note);
        auditLogService.recordReconciliationIncidentStatusUpdate(
                actorDetails,
                incident,
                beforeStatus,
                incident.getStatus(),
                note);
        return ResponseDTO.<AdminPaymentReconciliationIncidentResponse>builder()
                .data(AdminPaymentReconciliationIncidentResponse.from(incident))
                .build();
    }

    private Page<PaymentReconciliationIncident> findIncidents(
            PaymentReconciliationIncidentStatus status,
            Pageable pageable) {
        if (status == null) {
            return incidentRepository.findAllByOrderByLastDetectedAtDesc(pageable);
        }
        return incidentRepository.findByStatusOrderByLastDetectedAtDesc(status, pageable);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
    }
}
