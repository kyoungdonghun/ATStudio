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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminPaymentSettlementService {

    private static final long MAX_IMPORT_FILE_BYTES = 5_242_880L;
    private static final int MAX_OPERATOR_NOTE_LENGTH = 500;
    private static final int MAX_ORDER_ID_LENGTH = 64;
    private static final int MAX_PROVIDER_IDENTIFIER_LENGTH = 200;
    private static final int MAX_PROVIDER_STATUS_LENGTH = 100;
    private static final int MAX_RECONCILIATION_DAYS = 90;
    private static final int MAX_RECONCILIATION_PAYMENTS = 5000;
    private static final int MAX_RECONCILIATION_ERRORS = 200;
    private static final Set<String> ACCEPTED_CSV_CONTENT_TYPES = Set.of(
            "text/csv",
            "application/csv",
            "text/comma-separated-values",
            "application/vnd.ms-excel");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d+(?:\\.\\d{1,2})?");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter STRICT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private final PaymentSettlementRepository paymentSettlementRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;
    private final AdminPaymentSettlementAttemptTransactionService attemptTransactionService;
    private final AdminPaymentSettlementRowTransactionService rowTransactionService;
    private final PaymentCommandKeyFactory paymentCommandKeyFactory;
    private final PaymentSettlementCsvParser csvParser;

    public ResponseDTO<AdminPaymentSettlementImportResponse> importSettlements(
            CustomUserDetails actorDetails,
            MultipartFile file,
            String note,
            String idempotencyKey) {
        ValidatedImportFile importFile = validateEnvelope(file);
        Long actorID = requireAdminPrincipalID(actorDetails);
        String keyDigest = settlementImportDigest(actorID, idempotencyKey);
        String normalizedNote = normalizeOptionalNote(note);
        AdminPaymentSettlementAttemptTransactionService.CreatedAttempt attempt = claimAttempt(
                actorID,
                keyDigest,
                normalizedNote);

        try {
            return processImport(actorDetails, importFile, note, attempt);
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
            ValidatedImportFile importFile,
            String note,
            AdminPaymentSettlementAttemptTransactionService.CreatedAttempt attempt) {
        List<PaymentSettlementCsvParser.Row> rows = readCsv(importFile.bytes());
        List<AdminPaymentSettlementImportErrorResponse> errors = new ArrayList<>();
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        int importedRows = 0;
        int skippedDuplicateRows = 0;

        for (PaymentSettlementCsvParser.Row row : rows) {
            if (row.errorMessage() != null) {
                errors.add(new AdminPaymentSettlementImportErrorResponse(
                        row.rowNumber(),
                        row.errorMessage()));
                continue;
            }
            PaymentSettlement settlement;
            try {
                settlement = toSettlement(
                        row,
                        importFile.originalFilename(),
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
                errors,
                0);
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
        LocalDate today = LocalDate.now();
        LocalDate baseDateFrom = request == null || request.baseDateFrom() == null
                ? today.minusDays(29)
                : request.baseDateFrom();
        LocalDate baseDateTo = request == null || request.baseDateTo() == null
                ? today
                : request.baseDateTo();
        long inclusiveDays = ChronoUnit.DAYS.between(baseDateFrom, baseDateTo) + 1;
        if (baseDateTo.isBefore(baseDateFrom) || inclusiveDays > MAX_RECONCILIATION_DAYS) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        LocalDateTime from = baseDateFrom.atStartOfDay();
        LocalDateTime to = baseDateTo.atTime(LocalTime.MAX);
        List<SubscriptionPayment> payments =
                subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetweenOrderByIdAsc(
                        PaymentStatus.DONE,
                        from,
                        to,
                        PageRequest.of(0, MAX_RECONCILIATION_PAYMENTS + 1));
        if (payments.size() > MAX_RECONCILIATION_PAYMENTS) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        String batchKey = "ATS-SETTLE-MISS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        int importedRows = 0;
        int skippedDuplicateRows = 0;
        int failedRows = 0;
        List<AdminPaymentSettlementImportErrorResponse> errors = new ArrayList<>();

        for (int index = 0; index < payments.size(); index++) {
            SubscriptionPayment payment = payments.get(index);
            if (payment.getPaymentOrder() == null) {
                failedRows++;
                addReconciliationError(
                        errors,
                        index,
                        "Local payment has no payment order.");
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
                    addReconciliationError(
                            errors,
                            index,
                            "Settlement reconciliation row could not be persisted.");
                }
            } catch (RuntimeException exception) {
                failedRows++;
                addReconciliationError(
                        errors,
                        index,
                        "Settlement reconciliation row could not be persisted.");
            }
        }

        int omittedErrorCount = Math.max(0, failedRows - errors.size());
        return ResponseDTO.<AdminPaymentSettlementImportResponse>builder()
                .data(new AdminPaymentSettlementImportResponse(
                        batchKey,
                        payments.size(),
                        importedRows,
                        skippedDuplicateRows,
                        failedRows,
                        statusCounts,
                        errors,
                        omittedErrorCount))
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

    private ValidatedImportFile validateEnvelope(MultipartFile file) {
        if (file == null) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || originalFilename.isBlank()
                || originalFilename.length() > 255
                || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        String contentType = file.getContentType();
        if (contentType != null
                && !contentType.isBlank()
                && !ACCEPTED_CSV_CONTENT_TYPES.contains(contentType.trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        long declaredSize = file.getSize();
        if (declaredSize <= 0 || declaredSize > MAX_IMPORT_FILE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }

        byte[] bytes;
        try (InputStream inputStream = file.getInputStream()) {
            bytes = inputStream.readNBytes((int) MAX_IMPORT_FILE_BYTES + 1);
        } catch (IOException exception) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT, exception);
        }
        if (bytes.length == 0 || bytes.length > MAX_IMPORT_FILE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        return new ValidatedImportFile(originalFilename, bytes);
    }

    private PaymentSettlement toSettlement(
            PaymentSettlementCsvParser.Row row,
            String sourceFileName,
            String batchKey,
            String note) {
        Map<String, String> values = row.values();
        PaymentProviderType provider = parseProvider(values.get("provider"));
        String orderId = requiredEvidenceValue(values, "order_id", MAX_ORDER_ID_LENGTH);
        BigDecimal grossAmount = amount(required(values, "gross_amount"), "gross_amount");
        BigDecimal refundAmount = amount(defaultValue(values, "refund_amount", "0"), "refund_amount");
        BigDecimal feeAmount = amount(defaultValue(values, "fee_amount", "0"), "fee_amount");
        BigDecimal vatAmount = amount(defaultValue(values, "vat_amount", "0"), "vat_amount");
        BigDecimal netAmount = amount(required(values, "net_settlement_amount"), "net_settlement_amount");
        LocalDate baseDate = date(required(values, "settlement_base_date"), "settlement_base_date");
        String payoutDateValue = values.get("settlement_payout_date");
        LocalDate payoutDate = payoutDateValue == null || payoutDateValue.isEmpty()
                ? null
                : date(payoutDateValue, "settlement_payout_date");
        if (payoutDate != null && payoutDate.isBefore(baseDate)) {
            throw new IllegalArgumentException(
                    "settlement_payout_date must not precede settlement_base_date.");
        }
        String providerPaymentKey = optionalEvidenceValue(
                values,
                "provider_payment_key",
                MAX_PROVIDER_IDENTIFIER_LENGTH);
        String providerSettlementId = optionalEvidenceValue(
                values,
                "provider_settlement_id",
                MAX_PROVIDER_IDENTIFIER_LENGTH);
        String providerStatus = optionalEvidenceValue(
                values,
                "provider_status",
                MAX_PROVIDER_STATUS_LENGTH);
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
                .sourceFileName(sourceFileName)
                .sourceRowNumber(row.rowNumber())
                .providerSettlementId(providerSettlementId)
                .providerPaymentKey(providerPaymentKey)
                .orderId(orderId)
                .grossAmount(grossAmount)
                .refundAmount(refundAmount)
                .feeAmount(feeAmount)
                .vatAmount(vatAmount)
                .netSettlementAmount(netAmount)
                .currency(currency(defaultValue(values, "currency", "KRW")))
                .settlementBaseDate(baseDate)
                .settlementPayoutDate(payoutDate)
                .providerStatus(providerStatus)
                .operatorNote(truncate(hasText(note) ? note : blankToNull(values.get("note")), 500))
                .build();
    }

    private List<PaymentSettlementCsvParser.Row> readCsv(byte[] bytes) {
        try {
            return csvParser.parse(bytes);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT, exception);
        }
    }

    private PaymentProviderType parseProvider(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("provider is required.");
        }
        rejectProhibitedEvidenceCharacters(value, "provider");
        rejectEdgeWhitespace(value, "provider");
        if (!PaymentProviderType.TOSS.name().equals(value)) {
            throw new IllegalArgumentException("provider is invalid.");
        }
        return PaymentProviderType.TOSS;
    }

    private BigDecimal amount(String value, String fieldName) {
        rejectEdgeWhitespace(value, fieldName);
        if (!AMOUNT_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    fieldName + " must be a plain nonnegative decimal with at most 2 fraction digits.");
        }
        int decimalIndex = value.indexOf('.');
        String integerPart = decimalIndex < 0 ? value : value.substring(0, decimalIndex);
        int firstSignificantDigit = 0;
        while (firstSignificantDigit < integerPart.length() - 1
                && integerPart.charAt(firstSignificantDigit) == '0') {
            firstSignificantDigit++;
        }
        String canonicalIntegerPart = integerPart.substring(firstSignificantDigit);
        if (canonicalIntegerPart.length() > 13) {
            throw new IllegalArgumentException(fieldName + " must fit DECIMAL(15,2).");
        }
        try {
            String canonical = decimalIndex < 0
                    ? canonicalIntegerPart
                    : canonicalIntegerPart + value.substring(decimalIndex);
            return new BigDecimal(canonical).setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid decimal.");
        }
    }

    private LocalDate date(String value, String fieldName) {
        rejectEdgeWhitespace(value, fieldName);
        if (!DATE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(fieldName + " must be yyyy-MM-dd.");
        }
        try {
            return LocalDate.parse(value, STRICT_DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " must be yyyy-MM-dd.");
        }
    }

    private String currency(String value) {
        rejectEdgeWhitespace(value, "currency");
        if (!"KRW".equals(value)) {
            throw new IllegalArgumentException("currency must be KRW.");
        }
        return value;
    }

    private String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return value;
    }

    private String requiredEvidenceValue(Map<String, String> values, String key, int maxLength) {
        String value = values.get(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return validateEvidenceValue(value, key, maxLength);
    }

    private String optionalEvidenceValue(Map<String, String> values, String key, int maxLength) {
        String value = values.get(key);
        if (value == null) {
            return null;
        }
        if (value.isEmpty()) {
            return null;
        }
        return validateEvidenceValue(value, key, maxLength);
    }

    private String validateEvidenceValue(String value, String fieldName, int maxLength) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must be at most " + maxLength + " characters.");
        }
        rejectProhibitedEvidenceCharacters(value, fieldName);
        rejectEdgeWhitespace(value, fieldName);
        return value;
    }

    private void rejectProhibitedEvidenceCharacters(String value, String fieldName) {
        if (value.codePoints().anyMatch(this::isProhibitedEvidenceCharacter)) {
            throw new IllegalArgumentException(
                    fieldName + " must not contain control characters or newline separators.");
        }
    }

    private boolean isProhibitedEvidenceCharacter(int codePoint) {
        int characterType = Character.getType(codePoint);
        return Character.isISOControl(codePoint)
                || characterType == Character.LINE_SEPARATOR
                || characterType == Character.PARAGRAPH_SEPARATOR;
    }

    private String defaultValue(Map<String, String> values, String key, String defaultValue) {
        String value = values.get(key);
        return value == null || value.isEmpty() ? defaultValue : value;
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

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private void rejectEdgeWhitespace(String value, String fieldName) {
        if (!value.isEmpty()
                && (isWhitespace(value.codePointAt(0))
                || isWhitespace(value.codePointBefore(value.length())))) {
            throw new IllegalArgumentException(
                    fieldName + " must not have leading or trailing whitespace.");
        }
    }

    private boolean isWhitespace(int codePoint) {
        return Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint);
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

    private void addReconciliationError(
            List<AdminPaymentSettlementImportErrorResponse> errors,
            int index,
            String message) {
        if (errors.size() >= MAX_RECONCILIATION_ERRORS) {
            return;
        }
        errors.add(new AdminPaymentSettlementImportErrorResponse(index + 1, message));
    }

    private record ValidatedImportFile(String originalFilename, byte[] bytes) {
    }

    private static final class SettlementRowPersistenceException extends RuntimeException {
    }
}
