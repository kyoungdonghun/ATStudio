package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportAttemptResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportErrorResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementReconcileRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementResponse;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentSettlementImportAttemptState;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPaymentSettlementService {

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final int MAX_OPERATOR_NOTE_LENGTH = 500;
    private static final List<String> REQUIRED_HEADERS = List.of(
            "provider",
            "order_id",
            "gross_amount",
            "net_settlement_amount",
            "settlement_base_date");
    private final PaymentSettlementRepository paymentSettlementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;
    private final AdminPaymentSettlementAttemptTransactionService attemptTransactionService;
    private final AdminPaymentSettlementRowTransactionService rowTransactionService;
    private final PaymentCommandKeyFactory paymentCommandKeyFactory;

    public ResponseDTO<AdminPaymentSettlementImportResponse> importSettlements(
            CustomUserDetails actorDetails,
            MultipartFile file,
            String note,
            String idempotencyKey) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        Long actorID = requireAdminPrincipalID(actorDetails);
        String keyDigest = settlementImportDigest(actorID, idempotencyKey);
        String normalizedNote = normalizeOptionalNote(note);
        AdminPaymentSettlementAttemptTransactionService.CreatedAttempt attempt = claimAttempt(
                actorID,
                keyDigest,
                normalizedNote);

        try {
            return processImport(actorDetails, file, note, attempt);
        } catch (BusinessException exception) {
            attemptTransactionService.fail(attempt.id(), "CSV_READ_FAILED");
            throw exception;
        } catch (SettlementRowPersistenceException exception) {
            attemptTransactionService.fail(attempt.id(), "ROW_PERSISTENCE_FAILED");
            throw new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED);
        } catch (RuntimeException exception) {
            attemptTransactionService.fail(attempt.id(), "IMPORT_ORCHESTRATION_FAILED");
            throw new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED);
        }
    }

    private ResponseDTO<AdminPaymentSettlementImportResponse> processImport(
            CustomUserDetails actorDetails,
            MultipartFile file,
            String note,
            AdminPaymentSettlementAttemptTransactionService.CreatedAttempt attempt) {
        List<CsvRow> rows = readCsv(file);
        List<AdminPaymentSettlementImportErrorResponse> errors = new ArrayList<>();
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        int importedRows = 0;
        int skippedDuplicateRows = 0;

        for (CsvRow row : rows) {
            PaymentSettlement settlement;
            try {
                settlement = toSettlement(
                        row,
                        file.getOriginalFilename(),
                        attempt.importBatchKey(),
                        note);
            } catch (IllegalArgumentException exception) {
                errors.add(new AdminPaymentSettlementImportErrorResponse(
                        row.rowNumber(),
                        exception.getMessage()));
                continue;
            }

            try {
                PaymentSettlementStatus status = rowTransactionService.persistImported(actorDetails, settlement);
                importedRows++;
                increment(statusCounts, status.name());
            } catch (DataIntegrityViolationException exception) {
                if (PaymentSettlementConstraintTranslator.isDeduplicationUniqueViolation(exception)
                        && rowTransactionService.exactDeduplicationWinnerExists(
                        settlement.getDeduplicationKey())) {
                    skippedDuplicateRows++;
                    continue;
                }
                throw new SettlementRowPersistenceException();
            }
        }

        attemptTransactionService.complete(
                attempt.id(),
                rows.size(),
                importedRows,
                skippedDuplicateRows,
                errors.size());
        AdminPaymentSettlementImportResponse response = new AdminPaymentSettlementImportResponse(
                attempt.importBatchKey(),
                rows.size(),
                importedRows,
                skippedDuplicateRows,
                errors.size(),
                statusCounts,
                errors);
        return ResponseDTO.<AdminPaymentSettlementImportResponse>builder()
                .data(response)
                .build();
    }

    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> listImportAttempts(int page, int size) {
        return attemptTransactionService.list(page, size);
    }

    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> getImportAttempt(Long attemptID) {
        return attemptTransactionService.detail(attemptID);
    }

    public ResponseDTO<AdminPaymentSettlementImportAttemptResponse> recoverImportAttempt(
            CustomUserDetails actorDetails,
            String idempotencyKey) {
        Long actorID = requireAdminPrincipalID(actorDetails);
        return attemptTransactionService.recover(settlementImportDigest(actorID, idempotencyKey));
    }

    @Transactional(readOnly = true)
    public ResponseDTO<AdminPaymentSettlementResponse> listSettlements(
            PaymentSettlementStatus status,
            PaymentSettlementSource source,
            LocalDate baseDateFrom,
            LocalDate baseDateTo,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size));
        Page<AdminPaymentSettlementResponse> result =
                paymentSettlementRepository.search(status, source, baseDateFrom, baseDateTo, pageable)
                        .map(AdminPaymentSettlementResponse::from);
        return ResponseDTO.<AdminPaymentSettlementResponse>builder()
                .dataList(result.getContent())
                .pageInfo(PageInfo.of(page, size, (int) result.getTotalElements(), 10))
                .build();
    }

    public ResponseDTO<AdminPaymentSettlementImportResponse> reconcileMissingProviderSettlements(
            CustomUserDetails actorDetails,
            AdminPaymentSettlementReconcileRequest request) {
        LocalDate baseDateFrom = request == null || request.baseDateFrom() == null
                ? LocalDate.now().minusDays(30)
                : request.baseDateFrom();
        LocalDate baseDateTo = request == null || request.baseDateTo() == null
                ? LocalDate.now()
                : request.baseDateTo();
        if (baseDateTo.isBefore(baseDateFrom)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        LocalDateTime from = baseDateFrom.atStartOfDay();
        LocalDateTime to = baseDateTo.plusDays(1).atStartOfDay().minusNanos(1);
        String batchKey = "ATS-SETTLE-MISS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        int importedRows = 0;
        int skippedDuplicateRows = 0;
        int failedRows = 0;
        List<AdminPaymentSettlementImportErrorResponse> errors = new ArrayList<>();

        List<SubscriptionPayment> payments =
                subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(PaymentStatus.DONE, from, to);
        for (int index = 0; index < payments.size(); index++) {
            SubscriptionPayment payment = payments.get(index);
            if (payment.getPaymentOrder() == null) {
                failedRows++;
                errors.add(new AdminPaymentSettlementImportErrorResponse(
                        index + 1,
                        "Local payment has no payment order."));
                continue;
            }
            String orderID = payment.getPaymentOrder().getOrderId();
            String deduplicationKey = sha256("MISSING|" + payment.getId() + "|" + orderID);
            try {
                AdminPaymentSettlementRowTransactionService.ReconciliationRowResult result =
                        rowTransactionService.persistMissing(
                                actorDetails,
                                payment.getId(),
                                deduplicationKey,
                                batchKey,
                                baseDateTo);
                if (result.imported()) {
                    importedRows++;
                    increment(statusCounts, result.status().name());
                } else {
                        skippedDuplicateRows++;
                }
            } catch (DataIntegrityViolationException exception) {
                if (PaymentSettlementConstraintTranslator.isDeduplicationUniqueViolation(exception)
                        && rowTransactionService.exactDeduplicationWinnerExists(deduplicationKey)) {
                    skippedDuplicateRows++;
                } else {
                    failedRows++;
                    errors.add(reconciliationPersistenceError(index));
                }
            } catch (RuntimeException exception) {
                failedRows++;
                errors.add(reconciliationPersistenceError(index));
            }
        }

        return ResponseDTO.<AdminPaymentSettlementImportResponse>builder()
                .data(new AdminPaymentSettlementImportResponse(
                        batchKey,
                        payments.size(),
                        importedRows,
                        skippedDuplicateRows,
                        failedRows,
                        statusCounts,
                        errors))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentSettlementResponse> ignoreSettlement(
            Long settlementId,
            CustomUserDetails actorDetails,
            AdminPaymentSettlementIgnoreRequest request) {
        String note = normalizeRequiredIgnoreNote(request);
        User actor = resolveRequiredAdminActor(actorDetails);
        PaymentSettlement settlement = paymentSettlementRepository.findByIdForUpdate(settlementId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (settlement.getStatus() == PaymentSettlementStatus.IGNORED) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_STATE_TRANSITION);
        }
        PaymentSettlementStatus beforeStatus = settlement.getStatus();
        settlement.ignore(actor, note);
        auditLogService.recordPaymentSettlementEvent(
                actorDetails,
                settlement,
                PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED,
                beforeStatus,
                settlement.getStatus(),
                note);
        return ResponseDTO.<AdminPaymentSettlementResponse>builder()
                .data(AdminPaymentSettlementResponse.from(settlement))
                .build();
    }

    private String normalizeRequiredIgnoreNote(AdminPaymentSettlementIgnoreRequest request) {
        if (request == null || request.note() == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        String note = request.note().trim();
        if (note.isBlank() || note.length() > MAX_OPERATOR_NOTE_LENGTH) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        return note;
    }

    private PaymentSettlement toSettlement(
            CsvRow row,
            String sourceFileName,
            String batchKey,
            String note) {
        Map<String, String> values = row.values();
        PaymentProviderType provider = parseProvider(required(values, "provider"));
        String orderId = required(values, "order_id");
        if (orderId.length() > 64) {
            throw new IllegalArgumentException("order_id must be at most 64 characters.");
        }
        BigDecimal grossAmount = amount(required(values, "gross_amount"), "gross_amount");
        BigDecimal refundAmount = amount(defaultValue(values, "refund_amount", "0"), "refund_amount");
        BigDecimal feeAmount = amount(defaultValue(values, "fee_amount", "0"), "fee_amount");
        BigDecimal vatAmount = amount(defaultValue(values, "vat_amount", "0"), "vat_amount");
        BigDecimal netAmount = amount(required(values, "net_settlement_amount"), "net_settlement_amount");
        LocalDate baseDate = date(required(values, "settlement_base_date"), "settlement_base_date");
        LocalDate payoutDate = blankToNull(values.get("settlement_payout_date")) == null
                ? null
                : date(values.get("settlement_payout_date"), "settlement_payout_date");
        String providerPaymentKey = blankToNull(values.get("provider_payment_key"));
        String providerSettlementId = blankToNull(values.get("provider_settlement_id"));
        String dedupKey = deduplicationKey(
                provider,
                providerSettlementId,
                orderId,
                providerPaymentKey,
                grossAmount,
                refundAmount,
                feeAmount,
                vatAmount,
                netAmount,
                baseDate,
                payoutDate);
        return PaymentSettlement.builder()
                .source(PaymentSettlementSource.CSV_MANUAL)
                .provider(provider)
                .deduplicationKey(dedupKey)
                .importBatchKey(batchKey)
                .sourceFileName(truncate(sourceFileName, 255))
                .sourceRowNumber(row.rowNumber())
                .providerSettlementId(truncate(providerSettlementId, 200))
                .providerPaymentKey(truncate(providerPaymentKey, 200))
                .orderId(orderId)
                .grossAmount(grossAmount)
                .refundAmount(refundAmount)
                .feeAmount(feeAmount)
                .vatAmount(vatAmount)
                .netSettlementAmount(netAmount)
                .currency(currency(defaultValue(values, "currency", "KRW")))
                .settlementBaseDate(baseDate)
                .settlementPayoutDate(payoutDate)
                .providerStatus(truncate(blankToNull(values.get("provider_status")), 100))
                .operatorNote(truncate(hasText(note) ? note : blankToNull(values.get("note")), 500))
                .build();
    }

    private List<CsvRow> readCsv(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
            }
            List<String> headers = parseLine(stripBom(headerLine));
            validateHeaders(headers);
            List<CsvRow> rows = new ArrayList<>();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (!hasText(line)) {
                    continue;
                }
                if (rows.size() >= MAX_IMPORT_ROWS) {
                    throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
                }
                List<String> cells = parseLine(line);
                Map<String, String> values = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    values.put(headers.get(i), i < cells.size() ? cells.get(i).trim() : "");
                }
                rows.add(new CsvRow(rowNumber, values));
            }
            return rows;
        } catch (IOException ex) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT, ex);
        }
    }

    private void validateHeaders(List<String> headers) {
        if (!headers.containsAll(REQUIRED_HEADERS)) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private PaymentProviderType parseProvider(String value) {
        try {
            return PaymentProviderType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("provider is invalid.");
        }
    }

    private BigDecimal amount(String value, String fieldName) {
        try {
            BigDecimal amount = new BigDecimal(value.replace(",", "").trim());
            if (amount.signum() < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be numeric.");
        }
    }

    private LocalDate date(String value, String fieldName) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must be yyyy-MM-dd.");
        }
    }

    private String currency(String value) {
        String currency = value.trim().toUpperCase(Locale.ROOT);
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO code.");
        }
        return currency;
    }

    private String required(Map<String, String> values, String key) {
        String value = blankToNull(values.get(key));
        if (value == null) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return value;
    }

    private String defaultValue(Map<String, String> values, String key, String defaultValue) {
        String value = blankToNull(values.get(key));
        return value == null ? defaultValue : value;
    }

    private String deduplicationKey(
            PaymentProviderType provider,
            String providerSettlementId,
            String orderId,
            String providerPaymentKey,
            BigDecimal grossAmount,
            BigDecimal refundAmount,
            BigDecimal feeAmount,
            BigDecimal vatAmount,
            BigDecimal netAmount,
            LocalDate baseDate,
            LocalDate payoutDate) {
        String basis = hasText(providerSettlementId)
                ? "SETTLEMENT|" + provider + "|" + providerSettlementId
                : "ROW|" + provider + "|" + orderId + "|" + nullSafe(providerPaymentKey) + "|"
                + grossAmount + "|" + refundAmount + "|" + feeAmount + "|" + vatAmount + "|"
                + netAmount + "|" + baseDate + "|" + nullSafe(payoutDate);
        return sha256(basis);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    private User resolveRequiredAdminActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (actorDetails.getRole() != UserRole.ADMIN) {
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        User actor = userRepository.findByIdForUpdate(actorDetails.getId())
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        if (actor.isDeleted() || actor.getRole() != UserRole.ADMIN) {
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        return actor;
    }

    private String stripBom(String value) {
        if (value != null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }
        return value;
    }

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }

    private void increment(Map<String, Integer> counts, String key) {
        counts.merge(key, 1, Integer::sum);
    }

    private AdminPaymentSettlementAttemptTransactionService.CreatedAttempt claimAttempt(
            Long actorID,
            String keyDigest,
            String operatorNote) {
        try {
            return attemptTransactionService.create(actorID, keyDigest, operatorNote);
        } catch (DataIntegrityViolationException exception) {
            if (!PaymentSettlementConstraintTranslator.isAttemptKeyDigestUniqueViolation(exception)) {
                throw new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED);
            }
            AdminPaymentSettlementAttemptTransactionService.AttemptState existing =
                    attemptTransactionService.findStateByDigest(keyDigest)
                            .orElseThrow(() -> new BusinessException(
                                    BUSINESS_ERROR.SETTLEMENT_IMPORT_ORCHESTRATION_FAILED));
            throw attemptConflict(existing.state());
        }
    }

    private BusinessException attemptConflict(PaymentSettlementImportAttemptState state) {
        return switch (state) {
            case PROCESSING -> new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_IN_PROGRESS);
            case COMPLETED -> new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_COMPLETED);
            case FAILED -> new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_ATTEMPT_FAILED);
        };
    }

    private Long requireAdminPrincipalID(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            throw new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_ACCESS);
        }
        if (actorDetails.getRole() != UserRole.ADMIN) {
            throw new BusinessException(BUSINESS_ERROR.ADMIN_ROLE_REQUIRED);
        }
        return actorDetails.getId();
    }

    private String settlementImportDigest(Long actorID, String idempotencyKey) {
        try {
            return paymentCommandKeyFactory.settlementImportDigest(actorID, idempotencyKey);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.SETTLEMENT_IMPORT_IDEMPOTENCY_KEY_INVALID);
        }
    }

    private String normalizeOptionalNote(String note) {
        String normalized = blankToNull(note);
        return truncate(normalized, MAX_OPERATOR_NOTE_LENGTH);
    }

    private AdminPaymentSettlementImportErrorResponse reconciliationPersistenceError(int index) {
        return new AdminPaymentSettlementImportErrorResponse(
                index + 1,
                "Settlement reconciliation row could not be persisted.");
    }

    private record CsvRow(int rowNumber, Map<String, String> values) {
    }

    private static final class SettlementRowPersistenceException extends RuntimeException {
    }
}
