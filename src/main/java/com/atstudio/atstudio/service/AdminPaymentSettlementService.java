package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.dto.PageInfo;
import com.atstudio.atstudio.common.dto.ResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementIgnoreRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportErrorResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementImportResponse;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementReconcileRequest;
import com.atstudio.atstudio.dto.payment.AdminPaymentSettlementResponse;
import com.atstudio.atstudio.entity.PaymentOrder;
import com.atstudio.atstudio.entity.PaymentSettlement;
import com.atstudio.atstudio.entity.PaymentRefund;
import com.atstudio.atstudio.entity.SubscriptionPayment;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.PaymentOperationAuditAction;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.PaymentRefundStatus;
import com.atstudio.atstudio.entity.enums.PaymentSettlementSource;
import com.atstudio.atstudio.entity.enums.PaymentSettlementStatus;
import com.atstudio.atstudio.entity.enums.PaymentStatus;
import com.atstudio.atstudio.repository.PaymentOrderRepository;
import com.atstudio.atstudio.repository.PaymentRefundRepository;
import com.atstudio.atstudio.repository.PaymentSettlementRepository;
import com.atstudio.atstudio.repository.SubscriptionPaymentRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPaymentSettlementService {

    private static final int MAX_IMPORT_ROWS = 1000;
    private static final List<String> REQUIRED_HEADERS = List.of(
            "provider",
            "order_id",
            "gross_amount",
            "net_settlement_amount",
            "settlement_base_date");
    private static final Collection<PaymentRefundStatus> SETTLED_REFUND_STATUSES =
            List.of(PaymentRefundStatus.SUCCEEDED);

    private final PaymentSettlementRepository paymentSettlementRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final UserRepository userRepository;
    private final PaymentOperationAuditLogService auditLogService;

    @Transactional
    public ResponseDTO<AdminPaymentSettlementImportResponse> importSettlements(
            CustomUserDetails actorDetails,
            MultipartFile file,
            String note) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BUSINESS_ERROR.INVALID_ARGUMENT);
        }
        List<CsvRow> rows = readCsv(file);
        String batchKey = "ATS-SETTLE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<AdminPaymentSettlementImportErrorResponse> errors = new ArrayList<>();
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        int importedRows = 0;
        int skippedDuplicateRows = 0;

        for (CsvRow row : rows) {
            try {
                PaymentSettlement settlement = toSettlement(row, file.getOriginalFilename(), batchKey, note);
                if (paymentSettlementRepository.existsByDeduplicationKey(settlement.getDeduplicationKey())) {
                    skippedDuplicateRows++;
                    continue;
                }
                reconcile(settlement);
                PaymentSettlement saved = paymentSettlementRepository.save(settlement);
                auditLogService.recordPaymentSettlementEvent(
                        actorDetails,
                        saved,
                        PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IMPORTED,
                        null,
                        saved.getStatus(),
                        "Settlement row imported.");
                importedRows++;
                increment(statusCounts, saved.getStatus().name());
            } catch (IllegalArgumentException ex) {
                errors.add(new AdminPaymentSettlementImportErrorResponse(row.rowNumber(), ex.getMessage()));
            }
        }

        AdminPaymentSettlementImportResponse response = new AdminPaymentSettlementImportResponse(
                batchKey,
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

    @Transactional
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

        List<SubscriptionPayment> payments =
                subscriptionPaymentRepository.findByPaymentStatusAndCreatedAtBetween(PaymentStatus.DONE, from, to);
        for (SubscriptionPayment payment : payments) {
            if (payment.getPaymentOrder() == null) {
                continue;
            }
            String orderId = payment.getPaymentOrder().getOrderId();
            if (paymentSettlementRepository.existsByOrderIdAndSourceNot(
                    orderId,
                    PaymentSettlementSource.SYSTEM_RECONCILIATION)) {
                skippedDuplicateRows++;
                continue;
            }
            String dedupKey = sha256("MISSING|" + payment.getId() + "|" + orderId);
            if (paymentSettlementRepository.existsByDeduplicationKey(dedupKey)) {
                skippedDuplicateRows++;
                continue;
            }
            BigDecimal refundAmount = paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(
                    payment,
                    SETTLED_REFUND_STATUSES);
            PaymentProviderType provider = payment.getProvider() == null
                    ? payment.getPaymentOrder().getProvider()
                    : payment.getProvider();
            PaymentSettlement settlement = PaymentSettlement.builder()
                    .source(PaymentSettlementSource.SYSTEM_RECONCILIATION)
                    .provider(provider)
                    .status(PaymentSettlementStatus.PROVIDER_SETTLEMENT_NOT_FOUND)
                    .deduplicationKey(dedupKey)
                    .importBatchKey(batchKey)
                    .sourceFileName("system-reconciliation")
                    .orderId(orderId)
                    .providerPaymentKey(payment.getPgTransactionId())
                    .paymentOrder(payment.getPaymentOrder())
                    .subscriptionPayment(payment)
                    .user(payment.getUser())
                    .grossAmount(payment.getAmount())
                    .refundAmount(refundAmount)
                    .feeAmount(BigDecimal.ZERO)
                    .vatAmount(BigDecimal.ZERO)
                    .netSettlementAmount(payment.getAmount().subtract(refundAmount))
                    .currency("KRW")
                    .settlementBaseDate(baseDateTo)
                    .mismatchReason("No imported provider settlement evidence found for local payment.")
                    .reconciledAt(LocalDateTime.now())
                    .build();
            PaymentSettlement saved = paymentSettlementRepository.save(settlement);
            auditLogService.recordPaymentSettlementEvent(
                    actorDetails,
                    saved,
                    PaymentOperationAuditAction.PAYMENT_SETTLEMENT_RECONCILED,
                    null,
                    saved.getStatus(),
                    "Missing provider settlement evidence generated.");
            importedRows++;
            increment(statusCounts, saved.getStatus().name());
        }

        return ResponseDTO.<AdminPaymentSettlementImportResponse>builder()
                .data(new AdminPaymentSettlementImportResponse(
                        batchKey,
                        payments.size(),
                        importedRows,
                        skippedDuplicateRows,
                        0,
                        statusCounts,
                        List.of()))
                .build();
    }

    @Transactional
    public ResponseDTO<AdminPaymentSettlementResponse> ignoreSettlement(
            Long settlementId,
            CustomUserDetails actorDetails,
            AdminPaymentSettlementIgnoreRequest request) {
        PaymentSettlement settlement = paymentSettlementRepository.findWithGraphById(settlementId)
                .orElseThrow(() -> new BusinessException(BUSINESS_ERROR.RESOURCE_NOT_FOUND));
        PaymentSettlementStatus beforeStatus = settlement.getStatus();
        User actor = resolveActor(actorDetails);
        settlement.ignore(actor, request == null ? null : request.note());
        auditLogService.recordPaymentSettlementEvent(
                actorDetails,
                settlement,
                PaymentOperationAuditAction.PAYMENT_SETTLEMENT_IGNORED,
                beforeStatus,
                settlement.getStatus(),
                request == null ? null : request.note());
        return ResponseDTO.<AdminPaymentSettlementResponse>builder()
                .data(AdminPaymentSettlementResponse.from(settlement))
                .build();
    }

    private void reconcile(PaymentSettlement settlement) {
        Optional<PaymentOrder> order = paymentOrderRepository.findByOrderId(settlement.getOrderId());
        Optional<SubscriptionPayment> payment = Optional.empty();
        if (order.isPresent()) {
            payment = subscriptionPaymentRepository.findByPaymentOrder(order.get());
        }
        if (payment.isEmpty() && hasText(settlement.getProviderPaymentKey())) {
            payment = subscriptionPaymentRepository.findFirstByPgTransactionId(settlement.getProviderPaymentKey());
        }

        if (payment.isEmpty()) {
            settlement.applyReconciliation(
                    PaymentSettlementStatus.LOCAL_PAYMENT_NOT_FOUND,
                    order.orElse(null),
                    null,
                    order.map(PaymentOrder::getUser).orElse(null),
                    "Local finalized subscription payment was not found.");
            return;
        }

        SubscriptionPayment subscriptionPayment = payment.get();
        BigDecimal localRefundAmount = paymentRefundRepository.sumAmountBySubscriptionPaymentAndStatuses(
                subscriptionPayment,
                SETTLED_REFUND_STATUSES);
        List<String> mismatches = new ArrayList<>();
        if (!sameAmount(settlement.getGrossAmount(), subscriptionPayment.getAmount())) {
            mismatches.add("gross_amount local=" + subscriptionPayment.getAmount()
                    + " provider=" + settlement.getGrossAmount());
        }
        if (!sameAmount(settlement.getRefundAmount(), localRefundAmount)) {
            mismatches.add("refund_amount local=" + localRefundAmount
                    + " provider=" + settlement.getRefundAmount());
        }
        BigDecimal expectedNet = settlement.getGrossAmount()
                .subtract(settlement.getRefundAmount())
                .subtract(settlement.getFeeAmount())
                .subtract(settlement.getVatAmount());
        if (!sameAmount(settlement.getNetSettlementAmount(), expectedNet)) {
            mismatches.add("net_settlement_amount expected=" + expectedNet
                    + " provider=" + settlement.getNetSettlementAmount());
        }
        PaymentSettlementStatus status = mismatches.isEmpty()
                ? PaymentSettlementStatus.MATCHED
                : PaymentSettlementStatus.MISMATCHED;
        settlement.applyReconciliation(
                status,
                subscriptionPayment.getPaymentOrder(),
                subscriptionPayment,
                subscriptionPayment.getUser(),
                mismatches.isEmpty() ? null : String.join("; ", mismatches));
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
                .sourcePayload(sanitizedPayload(values))
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

    private String sanitizedPayload(Map<String, String> values) {
        Map<String, String> safe = new LinkedHashMap<>();
        List.of(
                "provider",
                "provider_payment_key",
                "order_id",
                "provider_settlement_id",
                "gross_amount",
                "refund_amount",
                "fee_amount",
                "vat_amount",
                "net_settlement_amount",
                "settlement_base_date",
                "settlement_payout_date",
                "provider_status",
                "currency").forEach(key -> {
            String value = blankToNull(values.get(key));
            if (value != null) {
                safe.put(key, value);
            }
        });
        return safe.toString();
    }

    private User resolveActor(CustomUserDetails actorDetails) {
        if (actorDetails == null || actorDetails.getId() == null) {
            return null;
        }
        return userRepository.findById(actorDetails.getId()).orElse(null);
    }

    private boolean sameAmount(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.compareTo(right) == 0;
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

    private record CsvRow(int rowNumber, Map<String, String> values) {
    }
}
