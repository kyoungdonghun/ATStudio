-- =============================================================================
-- ATStudio Manual DB Patch: company certification integrity and audit evidence
-- =============================================================================
-- Apply only after separate operator approval to a backed-up copied database.
-- Prerequisite: 20260618_company_certification_documents.sql has been applied.
-- This source-only patch is not executed by application startup.
-- Retained DBs require a rehearsal/backfill assessment before validation; this patch
-- neither deletes data nor reconstructs legacy document metadata.
-- MySQL 8.x does not support IF NOT EXISTS on ALTER TABLE ADD COLUMN/ADD INDEX.
-- The metadata checks below conditionally prepare each additive statement; they are
-- source-only operator steps and are not executed by application startup.

SET @company_certification_version_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'company_certifications'
      AND column_name = 'version'
);
SET @company_certification_version_sql := IF(
    @company_certification_version_exists = 0,
    'ALTER TABLE company_certifications ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER id',
    'SELECT 1'
);
PREPARE company_certification_version_statement FROM @company_certification_version_sql;
EXECUTE company_certification_version_statement;
DEALLOCATE PREPARE company_certification_version_statement;

SET @company_certification_user_status_index_exists := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'company_certifications'
      AND index_name = 'idx_company_certifications_user_status_id'
);
SET @company_certification_user_status_index_sql := IF(
    @company_certification_user_status_index_exists = 0,
    'ALTER TABLE company_certifications ADD INDEX idx_company_certifications_user_status_id (user_id, status, id)',
    'SELECT 1'
);
PREPARE company_certification_user_status_index_statement
    FROM @company_certification_user_status_index_sql;
EXECUTE company_certification_user_status_index_statement;
DEALLOCATE PREPARE company_certification_user_status_index_statement;

CREATE TABLE IF NOT EXISTS company_certification_audit_logs
(
    id               BIGINT                                   NOT NULL AUTO_INCREMENT,
    certification_id BIGINT                                   NOT NULL,
    actor_user_id    BIGINT                                   NOT NULL,
    action           ENUM ('REVIEWED', 'DOCUMENT_ACCESS_GRANTED') NOT NULL,
    document_id      BIGINT                                   NULL COMMENT 'Opaque document ID only; no document path or content is stored.',
    from_status      VARCHAR(30)                              NULL,
    to_status        VARCHAR(30)                              NULL,
    created_at       DATETIME                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME                                 NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_company_certification_audit_logs_certification_created (certification_id, created_at),
    KEY idx_company_certification_audit_logs_actor_created (actor_user_id, created_at),
    CONSTRAINT fk_company_certification_audit_logs_certification
        FOREIGN KEY (certification_id) REFERENCES company_certifications (id),
    CONSTRAINT fk_company_certification_audit_logs_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Operator verification query (do not run automatically):
-- SELECT table_name, column_name, column_type
-- FROM information_schema.columns
-- WHERE table_schema = DATABASE()
--   AND ((table_name = 'company_certifications' AND column_name = 'version')
--     OR table_name = 'company_certification_audit_logs')
-- ORDER BY table_name, ordinal_position;
