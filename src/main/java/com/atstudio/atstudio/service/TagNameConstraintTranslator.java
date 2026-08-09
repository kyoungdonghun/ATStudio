package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

final class TagNameConstraintTranslator {

    private static final String TAG_NAME_UNIQUE_CONSTRAINT = "uq_tags_name";
    private static final Pattern NAMED_CONSTRAINT_REFERENCE = Pattern.compile(
            "(?:key|constraint|index)\\s+[`'\\\"]?(?:[a-z0-9_$]+\\.)?uq_tags_name(?:[`'\\\"]|\\b)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern H2_TAG_NAME_UNIQUE_REFERENCE = Pattern.compile(
            "unique (?:index|constraint).*?\\bon\\s+(?:[a-z0-9_$\\\"]+\\.)?[`\\\"]?tags[`\\\"]?"
                    + "\\s*\\(\\s*[`\\\"]?name\\b",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private TagNameConstraintTranslator() {
    }

    static RuntimeException translate(DataIntegrityViolationException exception) {
        if (isTagNameUniqueViolation(exception)) {
            return new BusinessException(BUSINESS_ERROR.TAG_NAME_DUPLICATED, exception);
        }
        return exception;
    }

    static boolean isTagNameUniqueViolation(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        for (int depth = 0; cause != null && depth < 12; depth++) {
            if (cause instanceof ConstraintViolationException violation
                    && isExactTagNameConstraint(violation.getConstraintName())) {
                return true;
            }

            String message = cause.getMessage();
            if (message != null
                    && (NAMED_CONSTRAINT_REFERENCE.matcher(message).find()
                    || isH2TagNameUniqueViolation(cause, message))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean isExactTagNameConstraint(String constraintName) {
        if (constraintName == null) {
            return false;
        }
        String normalized = constraintName
                .replace("`", "")
                .replace("\"", "")
                .toLowerCase(Locale.ROOT);
        return normalized.equals(TAG_NAME_UNIQUE_CONSTRAINT)
                || normalized.endsWith("." + TAG_NAME_UNIQUE_CONSTRAINT);
    }

    private static boolean isH2TagNameUniqueViolation(Throwable cause, String message) {
        return cause instanceof SQLException sqlException
                && "23505".equals(sqlException.getSQLState())
                && H2_TAG_NAME_UNIQUE_REFERENCE.matcher(message).find();
    }
}
