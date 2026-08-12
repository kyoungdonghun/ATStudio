package com.atstudio.atstudio.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PaymentSettlementCsvParser strict CSV contracts")
class PaymentSettlementCsvParserTest {

    private static final String REQUIRED_HEADER =
            "provider,order_id,gross_amount,net_settlement_amount,settlement_base_date";

    private final PaymentSettlementCsvParser parser = new PaymentSettlementCsvParser();

    @Test
    void parsesOneBomNormalizedHeadersQuotedNewlinesEscapedQuotesAndCrLfOrLfRecords() {
        String csv = "\uFEFF\" Provider \", ORDER_ID,gross_amount,net_settlement_amount,"
                + "settlement_base_date,note\r\n"
                + "TOSS,Order-AbC,10.00,10,2026-08-01,\"line 1\r\nline 2, \"\"quoted\"\"\"\r\n"
                + "TOSS,Order-Def,20,20.0,2026-08-02,plain\n";

        List<PaymentSettlementCsvParser.Row> rows = parse(csv);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).rowNumber()).isEqualTo(2);
        assertThat(rows.get(0).values())
                .containsEntry("provider", "TOSS")
                .containsEntry("order_id", "Order-AbC")
                .containsEntry("note", "line 1\r\nline 2, \"quoted\"");
        assertThat(rows.get(1).rowNumber()).isEqualTo(4);
        assertThat(rows.get(1).values()).containsEntry("order_id", "Order-Def");
    }

    @Test
    void removesOnlyOneLeadingBom() {
        assertThat(parse("\uFEFF" + REQUIRED_HEADER + "\n")).isEmpty();
        assertThatThrownBy(() -> parse("\uFEFF\uFEFF" + REQUIRED_HEADER + "\n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMalformedUtf8WithoutReplacement() {
        byte[] malformed = {(byte) 0xC3, (byte) 0x28};

        assertThatThrownBy(() -> parser.parse(malformed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UTF-8");
    }

    @Test
    void rejectsDuplicateAndUnknownHeadersAfterNormalization() {
        assertThatThrownBy(() -> parse(
                REQUIRED_HEADER + ", ORDER_ID \n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate header");
        assertThatThrownBy(() -> parse(
                REQUIRED_HEADER + ",unexpected\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown header");
    }

    @Test
    void reportsMissingExtraAndTrailingCellsAsRejectedRows() {
        assertThat(parse(REQUIRED_HEADER + "\nTOSS,ORDER-1,10,10\n"))
                .singleElement()
                .extracting(PaymentSettlementCsvParser.Row::errorMessage)
                .asString()
                .contains("row width");
        assertThat(parse(REQUIRED_HEADER + "\nTOSS,ORDER-1,10,10,2026-08-01,extra\n"))
                .singleElement()
                .extracting(PaymentSettlementCsvParser.Row::errorMessage)
                .asString()
                .contains("row width");
        assertThat(parse(REQUIRED_HEADER + "\nTOSS,ORDER-1,10,10,2026-08-01,\n"))
                .singleElement()
                .extracting(PaymentSettlementCsvParser.Row::errorMessage)
                .asString()
                .contains("row width");
    }

    @Test
    void rejectsMalformedUnbalancedQuotesAndBareCarriageReturns() {
        assertThatThrownBy(() -> parse(REQUIRED_HEADER + "\nTO\"SS,ORDER-1,10,10,2026-08-01\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("malformed quote");
        assertThatThrownBy(() -> parse(REQUIRED_HEADER + "\n\"TOSS,ORDER-1,10,10,2026-08-01\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unbalanced quote");
        assertThatThrownBy(() -> parse(REQUIRED_HEADER + "\rTOSS,ORDER-1,10,10,2026-08-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("record separator");
    }

    @Test
    void ignoresBlankPhysicalAndLogicalRecords() {
        String csv = "\n\"\"\r\n  \t  \n"
                + REQUIRED_HEADER + "\n"
                + ",,,,\r\n"
                + "TOSS,ORDER-1,10,10,2026-08-01\n"
                + "\" \n \"\n";

        List<PaymentSettlementCsvParser.Row> rows = parse(csv);

        assertThat(rows).singleElement()
                .satisfies(row -> {
                    assertThat(row.rowNumber()).isEqualTo(6);
                    assertThat(row.values()).containsEntry("order_id", "ORDER-1");
                });
    }

    @Test
    void dropsNearFiveMebibytesOfLfBlankRecordsWithoutChangingTheFollowingLineNumber() {
        String dataRow = "TOSS,ORDER-1,10,10,2026-08-01";
        int maxFileBytes = 5 * 1024 * 1024;
        int blankLineCount = maxFileBytes - REQUIRED_HEADER.length() - 1 - dataRow.length();
        String csv = REQUIRED_HEADER + "\n" + "\n".repeat(blankLineCount) + dataRow;
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        assertThat(bytes).hasSize(maxFileBytes);
        assertThat(parser.parse(bytes)).singleElement().satisfies(row -> {
            assertThat(row.rowNumber()).isEqualTo(blankLineCount + 2);
            assertThat(row.values()).containsEntry("order_id", "ORDER-1");
        });
    }

    @Test
    void acceptsOneThousandNonblankLogicalRowsIncludingDuplicateInvalidValues() {
        StringBuilder csv = new StringBuilder(REQUIRED_HEADER).append('\n');
        for (int index = 0; index < 1000; index++) {
            csv.append("UNKNOWN,DUPLICATE,10,10,2026-08-01\n");
        }

        assertThat(parse(csv.toString()))
                .hasSize(1000)
                .last()
                .extracting(PaymentSettlementCsvParser.Row::rowNumber)
                .isEqualTo(1001);
    }

    @Test
    void rejectsTheOneThousandFirstNonblankLogicalRow() {
        StringBuilder csv = new StringBuilder(REQUIRED_HEADER).append('\n');
        for (int index = 0; index < 1001; index++) {
            csv.append("TOSS,ORDER-").append(index).append(",10,10,2026-08-01\n");
        }

        assertThatThrownBy(() -> parse(csv.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSV must not contain more than 1000 nonblank data rows.");
    }

    @Test
    void stopsAtTheOneThousandFirstRecordWithoutParsingTheDenseSuffix() {
        int denseRecordCount = 500_000;
        String csv = REQUIRED_HEADER + "\n"
                + "x\n".repeat(denseRecordCount)
                + "TO\"SS,ORDER-LATE,10,10,2026-08-01\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        // Reaching the malformed final record would change the error and expose a full scan.
        assertThat(bytes.length).isLessThanOrEqualTo(5 * 1024 * 1024);
        assertThatThrownBy(() -> parser.parse(bytes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CSV must not contain more than 1000 nonblank data rows.");
    }

    @Test
    void keepsMalformedRecordAndHeaderErrorsAheadOfTheRowLimit() {
        String thousandRows = "x\n".repeat(1000);

        assertThatThrownBy(() -> parse(REQUIRED_HEADER + "\n" + thousandRows + "x\"\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("malformed quote");
        assertThatThrownBy(() -> parse(REQUIRED_HEADER + ",unexpected\n" + thousandRows + "x\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown header");
    }

    private List<PaymentSettlementCsvParser.Row> parse(String csv) {
        return parser.parse(csv.getBytes(StandardCharsets.UTF_8));
    }
}
