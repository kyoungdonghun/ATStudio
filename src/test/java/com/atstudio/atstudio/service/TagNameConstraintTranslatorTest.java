package com.atstudio.atstudio.service;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

class TagNameConstraintTranslatorTest {

    @Test
    void translatesMysqlUqTagsNameEvidence() {
        DataIntegrityViolationException violation = mysqlViolation(
                "Duplicate entry 'Hip Hop' for key 'tags.uq_tags_name'");

        RuntimeException translated = TagNameConstraintTranslator.translate(violation);

        assertThat(translated).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) translated).getErrorCode())
                .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
        assertThat(translated).hasCause(violation);
    }

    @Test
    void translatesH2UqTagsNameEvidence() {
        DataIntegrityViolationException violation = h2Violation(
                "Unique index or primary key violation: "
                        + "\"PUBLIC.UQ_TAGS_NAME ON PUBLIC.TAGS(NAME NULLS FIRST) VALUES ('Hip Hop')\"");

        RuntimeException translated = TagNameConstraintTranslator.translate(violation);

        assertThat(translated).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) translated).getErrorCode())
                .isEqualTo(BUSINESS_ERROR.TAG_NAME_DUPLICATED);
    }

    @Test
    void leavesUnrelatedMysqlIntegrityViolationUntouched() {
        DataIntegrityViolationException violation = mysqlViolation(
                "Duplicate entry 'person@example.com' for key 'users.uq_users_email'");

        assertThat(TagNameConstraintTranslator.translate(violation)).isSameAs(violation);
    }

    @Test
    void leavesUnrelatedH2UniqueViolationUntouched() {
        DataIntegrityViolationException violation = h2Violation(
                "Unique index or primary key violation: "
                        + "\"PUBLIC.UQ_TAGS_TYPE ON PUBLIC.TAGS(TYPE NULLS FIRST) VALUES ('GENRE')\"");

        assertThat(TagNameConstraintTranslator.translate(violation)).isSameAs(violation);
    }

    private DataIntegrityViolationException mysqlViolation(String message) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLIntegrityConstraintViolationException(message, "23000", 1062));
    }

    private DataIntegrityViolationException h2Violation(String message) {
        return new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException(message, "23505", 23505));
    }
}
