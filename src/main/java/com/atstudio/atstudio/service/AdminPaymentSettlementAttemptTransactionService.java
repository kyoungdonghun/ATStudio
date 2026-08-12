package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportAttemptResponse;
import com.atstudio.atstudio.entity.PaymentSettlementImportAttempt;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.PaymentSettlementImportAttemptRepository;
import com.atstudio.atstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminPaymentSettlementAttemptTransactionService {

    private final PaymentSettlementImportAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreatedAttempt create(Long actorID, String keyDigest, String operatorNote) {
        User actor = userRepository.findById(actorID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (actor.isDeleted() || actor.getRole() != UserRole.ADMIN) {
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        PaymentSettlementImportAttempt attempt = attemptRepository.saveAndFlush(
                PaymentSettlementImportAttempt.builder()
                        .keyDigest(keyDigest)
                        .actorUser(actor)
                        .operatorNote(operatorNote)
                        .build());
        return new CreatedAttempt(attempt.getId(), attempt.importBatchKey());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(
            Long attemptID,
            int totalRows,
            int importedRows,
            int duplicateRows,
            int failedRows) {
        PaymentSettlementImportAttempt attempt = findForUpdate(attemptID);
        attempt.complete(
                totalRows,
                importedRows,
                duplicateRows,
                failedRows,
                LocalDateTime.now());
        attemptRepository.saveAndFlush(attempt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long attemptID, String failureCode) {
        PaymentSettlementImportAttempt attempt = findForUpdate(attemptID);
        if (attempt.getState() != PaymentSettlementImportAttemptState.PROCESSING) {
            return;
        }
        attempt.fail(failureCode, LocalDateTime.now());
        attemptRepository.saveAndFlush(attempt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<AttemptState> findStateByDigest(String keyDigest) {
        return attemptRepository.findByKeyDigest(keyDigest)
                .map(attempt -> new AttemptState(attempt.getId(), attempt.getState()));
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> list(int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        Page<AdminPaymentSettlementImportAttemptResponse> result = attemptRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(safePage - 1, safeSize))
                .map(AdminPaymentSettlementImportAttemptResponse::from);
        return ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(safePage, safeSize, (int) result.getTotalElements(), 10))
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> detail(Long attemptID) {
        PaymentSettlementImportAttempt attempt = attemptRepository.findWithActorById(attemptID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder()
                .data(AdminPaymentSettlementImportAttemptResponse.from(attempt))
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> recover(String keyDigest) {
        PaymentSettlementImportAttempt attempt = attemptRepository.findByKeyDigest(keyDigest)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        return ResponseDTO.<AdminPaymentSettlementImportAttemptResponse>builder()
                .data(AdminPaymentSettlementImportAttemptResponse.from(attempt))
                .build();
    }

    private PaymentSettlementImportAttempt findForUpdate(Long attemptID) {
        return attemptRepository.findByIdForUpdate(attemptID)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
    }

    public record CreatedAttempt(Long id, String importBatchKey) {
    }

    public record AttemptState(Long id, PaymentSettlementImportAttemptState state) {
    }
}
