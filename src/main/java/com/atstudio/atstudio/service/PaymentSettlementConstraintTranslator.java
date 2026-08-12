package com.atstudio.atstudio.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

final class PaymentSettlementConstraintTranslator {

    static final String DEDUPLICATION_UNIQUE_CONSTRAINT =
            "uq_payment_settlements_deduplication_key";
    static final String ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT =
            "uq_payment_settlement_import_attempts_key_digest";
    private static final Pattern MYSQL_CONSTRAINT_REFERENCE = Pattern.compile(
            "duplicate entry .*? for (?:key|constraint|index)\\s+[`'\\\"]?(?:[a-z0-9_$]+\\.)?"
                    + DEDUPLICATION_UNIQUE_CONSTRAINT + "(?:[`'\\\"]|\\b)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern H2_DEDUPLICATION_REFERENCE = Pattern.compile(
            "unique (?:index|constraint).*?\\bon\\s+(?:[a-z0-9_$\\\"]+\\.)?"
                    + "[`\\\"]?payment_settlements[`\\\"]?"
                    + "\\s*\\(\\s*[`\\\"]?deduplication_key\\b",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern MYSQL_ATTEMPT_KEY_DIGEST_REFERENCE = Pattern.compile(
            "duplicate entry .*? for (?:key|constraint|index)\\s+[`'\\\"]?(?:[a-z0-9_$]+\\.)?"
                    + ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT + "(?:[`'\\\"]|\\b)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern H2_ATTEMPT_KEY_DIGEST_REFERENCE = Pattern.compile(
            "unique (?:index|constraint)(?: or primary key)? violation.*?"
                    + "[`\\\"]?(?:[a-z0-9_$]+\\.)?"
                    + ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT
                    + "(?:_index_[0-9]+)?[`\\\"]?.*?\\bon\\s+"
                    + "[`\\\"]?(?:[a-z0-9_$]+\\.)?[`\\\"]?"
                    + "payment_settlement_import_attempts[`\\\"]?"
                    + "\\s*\\(\\s*[`\\\"]?key_digest\\b",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private PaymentSettlementConstraintTranslator() {
    }

    static boolean isDeduplicationUniqueViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 12; depth++) {
            if (cause instanceof ConstraintViolationException violation
                    && isExactConstraintName(
                    violation.getConstraintName(),
                    DEDUPLICATION_UNIQUE_CONSTRAINT)) {
                return true;
            }

            String message = cause.getMessage();
            if (message != null
                    && (MYSQL_CONSTRAINT_REFERENCE.matcher(message).find()
                    || isH2DeduplicationViolation(cause, message))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    static boolean isAttemptKeyDigestUniqueViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 12; depth++) {
            if (cause instanceof ConstraintViolationException violation
                    && isExactConstraintName(
                    violation.getConstraintName(),
                    ATTEMPT_KEY_DIGEST_UNIQUE_CONSTRAINT)) {
                return true;
            }
            if (cause instanceof SQLException sqlException
                    && (isMySqlAttemptKeyDigestViolation(sqlException)
                    || isH2AttemptKeyDigestViolation(sqlException))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean isExactConstraintName(String constraintName, String expectedName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace("`", "")
                .replace("\"", "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals(expectedName)
                || normalized.endsWith("." + expectedName);
    }

    private static boolean isH2DeduplicationViolation(Throwable cause, String message) {
        return cause instanceof SQLException sqlException
                && "23505".equals(sqlException.getSQLState())
                && H2_DEDUPLICATION_REFERENCE.matcher(message).find();
    }

    private static boolean isMySqlAttemptKeyDigestViolation(SQLException exception) {
        return "23000".equals(exception.getSQLState())
                && exception.getErrorCode() == 1062
                && MYSQL_ATTEMPT_KEY_DIGEST_REFERENCE.matcher(exception.getMessage()).find();
    }

    private static boolean isH2AttemptKeyDigestViolation(SQLException exception) {
        return "23505".equals(exception.getSQLState())
                && H2_ATTEMPT_KEY_DIGEST_REFERENCE.matcher(exception.getMessage()).find();
    }
}
