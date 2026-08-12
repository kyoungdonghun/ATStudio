package com.atstudio.atstudio.service;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
final class PaymentSettlementCsvParser {

    private static final int MAX_DATA_ROWS = 1000;
    private static final int MAX_RETAINED_RECORDS = MAX_DATA_ROWS + 1;
    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "provider",
            "order_id",
            "gross_amount",
            "net_settlement_amount",
            "settlement_base_date");
    private static final Set<String> ALLOWED_HEADERS = Set.of(
            "provider",
            "provider_payment_key",
            "provider_settlement_id",
            "order_id",
            "gross_amount",
            "refund_amount",
            "fee_amount",
            "vat_amount",
            "net_settlement_amount",
            "currency",
            "settlement_base_date",
            "settlement_payout_date",
            "provider_status",
            "note");

    PaymentSettlementCsvParser() {
    }

    List<Row> parse(byte[] bytes) {
        String decoded = decode(bytes);
        if (decoded.startsWith("\uFEFF")) {
            decoded = decoded.substring(1);
        }

        ParsedRecords parsedRecords = parseRecords(decoded);
        List<LogicalRecord> records = parsedRecords.records();
        if (records.isEmpty()) {
            throw new CsvFileException("CSV header is required.");
        }

        List<String> headers = normalizeAndValidateHeaders(records.get(0).cells());
        if (parsedRecords.dataRowLimitExceeded()) {
            throw new CsvFileException("CSV must not contain more than 1000 nonblank data rows.");
        }

        List<Row> rows = new ArrayList<>();
        for (int index = 1; index < records.size(); index++) {
            LogicalRecord record = records.get(index);
            if (record.cells().size() != headers.size()) {
                rows.add(Row.invalid(
                        record.startLine(),
                        "CSV row width must exactly match the header."));
                continue;
            }

            Map<String, String> values = new HashMap<>();
            for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
                values.put(headers.get(cellIndex), record.cells().get(cellIndex));
            }
            rows.add(Row.valid(record.startLine(), values));
        }
        return rows;
    }

    private static String decode(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new CsvFileException("CSV must contain valid UTF-8.", exception);
        }
    }

    private static ParsedRecords parseRecords(String input) {
        List<LogicalRecord> records = new ArrayList<>(MAX_RETAINED_RECORDS);
        List<String> cells = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        boolean afterClosingQuote = false;
        boolean recordStarted = false;
        int line = 1;
        int recordStartLine = 1;

        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (inQuotes) {
                if (current == '"') {
                    if (index + 1 < input.length() && input.charAt(index + 1) == '"') {
                        cell.append('"');
                        index++;
                    } else {
                        inQuotes = false;
                        afterClosingQuote = true;
                    }
                } else if (current == '\r') {
                    if (index + 1 >= input.length() || input.charAt(index + 1) != '\n') {
                        throw new CsvFileException("CSV record separator must be CRLF or LF.");
                    }
                    cell.append("\r\n");
                    index++;
                    line++;
                } else if (current == '\n') {
                    cell.append('\n');
                    line++;
                } else {
                    cell.append(current);
                }
                continue;
            }

            if (afterClosingQuote) {
                if (current == ',') {
                    cells.add(cell.toString());
                    cell.setLength(0);
                    afterClosingQuote = false;
                    recordStarted = true;
                } else if (current == '\r' || current == '\n') {
                    FinishedRecord finishedRecord = finishRecord(
                            records,
                            cells,
                            cell,
                            recordStartLine,
                            input,
                            index,
                            current);
                    if (finishedRecord.dataRowLimitExceeded()) {
                        return new ParsedRecords(records, true);
                    }
                    index += finishedRecord.consumedCharacters();
                    line++;
                    recordStartLine = line;
                    afterClosingQuote = false;
                    recordStarted = false;
                } else {
                    throw new CsvFileException("CSV contains a malformed quote.");
                }
                continue;
            }

            if (current == '"') {
                if (cell.length() != 0) {
                    throw new CsvFileException("CSV contains a malformed quote.");
                }
                inQuotes = true;
                recordStarted = true;
            } else if (current == ',') {
                cells.add(cell.toString());
                cell.setLength(0);
                recordStarted = true;
            } else if (current == '\r' || current == '\n') {
                FinishedRecord finishedRecord = finishRecord(
                        records,
                        cells,
                        cell,
                        recordStartLine,
                        input,
                        index,
                        current);
                if (finishedRecord.dataRowLimitExceeded()) {
                    return new ParsedRecords(records, true);
                }
                index += finishedRecord.consumedCharacters();
                line++;
                recordStartLine = line;
                recordStarted = false;
            } else {
                cell.append(current);
                recordStarted = true;
            }
        }

        if (inQuotes) {
            throw new CsvFileException("CSV contains an unbalanced quote.");
        }
        if (recordStarted || afterClosingQuote || !cells.isEmpty() || cell.length() > 0) {
            cells.add(cell.toString());
            if (addNonBlankRecord(records, cells, recordStartLine)) {
                return new ParsedRecords(records, true);
            }
        }
        return new ParsedRecords(records, false);
    }

    private static FinishedRecord finishRecord(
            List<LogicalRecord> records,
            List<String> cells,
            StringBuilder cell,
            int recordStartLine,
            String input,
            int index,
            char newline) {
        int consumed = 0;
        if (newline == '\r') {
            if (index + 1 >= input.length() || input.charAt(index + 1) != '\n') {
                throw new CsvFileException("CSV record separator must be CRLF or LF.");
            }
            consumed = 1;
        }
        cells.add(cell.toString());
        boolean limitExceeded = addNonBlankRecord(records, cells, recordStartLine);
        cells.clear();
        cell.setLength(0);
        return new FinishedRecord(consumed, limitExceeded);
    }

    private static boolean addNonBlankRecord(
            List<LogicalRecord> records,
            List<String> cells,
            int recordStartLine) {
        if (isBlank(cells)) {
            return false;
        }
        if (records.size() >= MAX_RETAINED_RECORDS) {
            return true;
        }
        records.add(new LogicalRecord(recordStartLine, List.copyOf(cells)));
        return false;
    }

    private static List<String> normalizeAndValidateHeaders(List<String> rawHeaders) {
        List<String> headers = rawHeaders.stream()
                .map(header -> header.trim().toLowerCase(Locale.ROOT))
                .toList();
        Set<String> uniqueHeaders = new HashSet<>();
        for (String header : headers) {
            if (!uniqueHeaders.add(header)) {
                throw new CsvFileException("CSV contains a duplicate header.");
            }
            if (!ALLOWED_HEADERS.contains(header)) {
                throw new CsvFileException("CSV contains an unknown header.");
            }
        }
        if (!uniqueHeaders.containsAll(REQUIRED_HEADERS)) {
            throw new CsvFileException("CSV is missing a required header.");
        }
        return headers;
    }

    private static boolean isBlank(List<String> cells) {
        return cells.stream().allMatch(String::isBlank);
    }

    record Row(int rowNumber, Map<String, String> values, String errorMessage) {

        private static Row valid(int rowNumber, Map<String, String> values) {
            return new Row(rowNumber, Map.copyOf(values), null);
        }

        private static Row invalid(int rowNumber, String errorMessage) {
            return new Row(rowNumber, Map.of(), errorMessage);
        }
    }

    private record LogicalRecord(int startLine, List<String> cells) {
    }

    private record ParsedRecords(List<LogicalRecord> records, boolean dataRowLimitExceeded) {
    }

    private record FinishedRecord(int consumedCharacters, boolean dataRowLimitExceeded) {
    }

    static final class CsvFileException extends IllegalArgumentException {

        private CsvFileException(String message) {
            super(message);
        }

        private CsvFileException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
