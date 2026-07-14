-- WI-20260714-ATS-012
-- Ordered manual patch for retained MySQL 8 databases.
-- Do not run without the separate database-application approval.
-- This patch creates journal structure only. It does not migrate or delete legacy data/files.

CREATE TABLE IF NOT EXISTS storage_mutations
(
    id              BIGINT                                                                                              NOT NULL AUTO_INCREMENT,
    operation_id    CHAR(36)                                                                                            NOT NULL,
    domain          ENUM ('TRACK', 'PLAYLIST', 'ALBUM', 'COMPANY_CERTIFICATION', 'NOTICE', 'QUESTION')                 NOT NULL,
    mutation_type   ENUM ('CREATE', 'REPLACE', 'DELETE')                                                               NOT NULL,
    storage_root    ENUM ('PUBLIC', 'PRIVATE')                                                                         NOT NULL,
    new_key         VARCHAR(500)                                                                                        NULL,
    old_key         VARCHAR(500)                                                                                        NULL,
    state           ENUM ('PREPARED', 'COMMITTED', 'ROLLBACK_CLEANUP', 'AFTER_COMMIT_DELETE', 'RETRY', 'DONE', 'FAILED') NOT NULL,
    attempt_count   INT                                                                                                 NOT NULL DEFAULT 0,
    next_attempt_at DATETIME                                                                                            NULL,
    reason_code     VARCHAR(64)                                                                                         NULL,
    created_at      DATETIME                                                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME                                                                                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_storage_mutations_recovery (state, next_attempt_at, id),
    KEY idx_storage_mutations_operation_id (operation_id),
    CONSTRAINT chk_storage_mutations_keys CHECK (
        (mutation_type = 'CREATE' AND new_key IS NOT NULL AND old_key IS NULL)
        OR (mutation_type = 'REPLACE' AND new_key IS NOT NULL AND old_key IS NOT NULL)
        OR (mutation_type = 'DELETE' AND new_key IS NULL AND old_key IS NOT NULL)
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Operator verification only. This query does not mutate rows.
SELECT table_name, engine, table_collation
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'storage_mutations';
