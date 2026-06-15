-- =============================================================================
-- ATStudio Manual DB Patch: payment settlement + usage tag + whitelist workflow
-- =============================================================================
-- Purpose:
--   Align an existing MySQL database with the current application schema when
--   the server runs with spring.jpa.hibernate.ddl-auto=validate.
--
-- Source of truth:
--   - docs/design/db-schema.md
--   - src/main/resources/schema.sql
--
-- Scope:
--   - tags.type supports USAGE guide tags.
--   - payment_settlements table exists.
--   - whitelist_channels supports draft/request/status/primary workflow.
--   - whitelist_export_batches and whitelist_export_items exist.
--
-- Important:
--   - This file is NOT auto-run by Spring Boot.
--   - MySQL DDL usually causes implicit commits; take a backup first.
--   - Run on a copied local/staging DB before applying to a shared DB.
--   - If your DB is older than the payment operations baseline, apply the
--     earlier payment table migrations first or rebuild from schema.sql.
-- =============================================================================

-- 1) Track usage guide tags.
ALTER TABLE tags
    MODIFY COLUMN type ENUM ('MOOD', 'GENRE', 'INSTRUMENT', 'USAGE') NOT NULL;

-- 2) Whitelist channel workflow columns.
DELIMITER //

DROP PROCEDURE IF EXISTS ats_add_column_if_missing//
CREATE PROCEDURE ats_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_alter_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND column_name = p_column_name
    ) THEN
        SET @ats_sql = p_alter_sql;
        PREPARE ats_stmt FROM @ats_sql;
        EXECUTE ats_stmt;
        DEALLOCATE PREPARE ats_stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS ats_add_index_if_missing//
CREATE PROCEDURE ats_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_alter_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND index_name = p_index_name
    ) THEN
        SET @ats_sql = p_alter_sql;
        PREPARE ats_stmt FROM @ats_sql;
        EXECUTE ats_stmt;
        DEALLOCATE PREPARE ats_stmt;
    END IF;
END//

DROP PROCEDURE IF EXISTS ats_add_fk_if_missing//
CREATE PROCEDURE ats_add_fk_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_fk_name VARCHAR(64),
    IN p_alter_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_schema = DATABASE()
          AND table_name = p_table_name
          AND constraint_name = p_fk_name
          AND constraint_type = 'FOREIGN KEY'
    ) THEN
        SET @ats_sql = p_alter_sql;
        PREPARE ats_stmt FROM @ats_sql;
        EXECUTE ats_stmt;
        DEALLOCATE PREPARE ats_stmt;
    END IF;
END//

DELIMITER ;

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'youtube_handle',
    "ALTER TABLE whitelist_channels ADD COLUMN youtube_handle VARCHAR(100) NULL COMMENT 'YouTube handle, e.g. @channel.' AFTER channel_name"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'youtube_channel_id',
    "ALTER TABLE whitelist_channels ADD COLUMN youtube_channel_id VARCHAR(100) NULL COMMENT 'YouTube canonical channel ID, e.g. UC...' AFTER youtube_handle"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'status',
    "ALTER TABLE whitelist_channels ADD COLUMN status ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NOT NULL DEFAULT 'DRAFT' AFTER youtube_channel_id"
);

ALTER TABLE whitelist_channels
    MODIFY COLUMN status ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NOT NULL DEFAULT 'DRAFT';

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'is_primary',
    "ALTER TABLE whitelist_channels ADD COLUMN is_primary TINYINT(1) NOT NULL DEFAULT 0 AFTER status"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'requested_at',
    "ALTER TABLE whitelist_channels ADD COLUMN requested_at DATETIME NULL AFTER is_primary"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'exported_at',
    "ALTER TABLE whitelist_channels ADD COLUMN exported_at DATETIME NULL AFTER requested_at"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'processed_at',
    "ALTER TABLE whitelist_channels ADD COLUMN processed_at DATETIME NULL AFTER exported_at"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'removal_requested_at',
    "ALTER TABLE whitelist_channels ADD COLUMN removal_requested_at DATETIME NULL AFTER processed_at"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'admin_note',
    "ALTER TABLE whitelist_channels ADD COLUMN admin_note VARCHAR(500) NULL AFTER removal_requested_at"
);

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'processed_by',
    "ALTER TABLE whitelist_channels ADD COLUMN processed_by BIGINT NULL AFTER admin_note"
);

CALL ats_add_index_if_missing(
    'whitelist_channels',
    'idx_whitelist_channels_user_status',
    'ALTER TABLE whitelist_channels ADD INDEX idx_whitelist_channels_user_status (user_id, status)'
);

CALL ats_add_index_if_missing(
    'whitelist_channels',
    'idx_whitelist_channels_status_requested',
    'ALTER TABLE whitelist_channels ADD INDEX idx_whitelist_channels_status_requested (status, requested_at)'
);

CALL ats_add_fk_if_missing(
    'whitelist_channels',
    'fk_whitelist_channels_processed_by',
    'ALTER TABLE whitelist_channels ADD CONSTRAINT fk_whitelist_channels_processed_by FOREIGN KEY (processed_by) REFERENCES users (id)'
);

-- Existing rows become saved drafts. The first channel per user becomes primary
-- only if that user has no existing primary channel.
DROP TEMPORARY TABLE IF EXISTS ats_whitelist_primary_seed;
CREATE TEMPORARY TABLE ats_whitelist_primary_seed AS
SELECT wc.user_id, MIN(wc.id) AS first_id
FROM whitelist_channels wc
GROUP BY wc.user_id
HAVING SUM(CASE WHEN wc.is_primary = 1 THEN 1 ELSE 0 END) = 0;

UPDATE whitelist_channels wc
JOIN ats_whitelist_primary_seed seed
    ON seed.first_id = wc.id
SET wc.is_primary = 1;

DROP TEMPORARY TABLE IF EXISTS ats_whitelist_primary_seed;

-- 3) Whitelist export ledger.
CREATE TABLE IF NOT EXISTS whitelist_export_batches
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    file_name   VARCHAR(255) NOT NULL,
    item_count  INT          NOT NULL,
    exported_by BIGINT       NULL,
    note        VARCHAR(500) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_whitelist_export_batches_exported_by FOREIGN KEY (exported_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS whitelist_export_items
(
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    batch_id                    BIGINT       NOT NULL,
    whitelist_channel_id        BIGINT       NULL,
    status_at_export            ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NOT NULL,
    user_id_snapshot            BIGINT       NOT NULL,
    user_email_snapshot         VARCHAR(100) NOT NULL,
    user_nickname_snapshot      VARCHAR(20)  NOT NULL,
    channel_name_snapshot       VARCHAR(100) NOT NULL,
    youtube_handle_snapshot     VARCHAR(100) NULL,
    channel_url_snapshot        VARCHAR(255) NOT NULL,
    youtube_channel_id_snapshot VARCHAR(100) NULL,
    plan_name_snapshot          VARCHAR(30)  NULL,
    billing_cycle_snapshot      ENUM ('MONTHLY', 'YEARLY') NULL,
    requested_at_snapshot       DATETIME     NULL,
    exported_at_snapshot        DATETIME     NOT NULL,
    created_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_whitelist_export_items_batch (batch_id),
    CONSTRAINT fk_whitelist_export_items_batch FOREIGN KEY (batch_id) REFERENCES whitelist_export_batches (id),
    CONSTRAINT fk_whitelist_export_items_channel FOREIGN KEY (whitelist_channel_id) REFERENCES whitelist_channels (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 4) Payment settlement ledger.
CREATE TABLE IF NOT EXISTS payment_settlements
(
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    source                  ENUM ('CSV_MANUAL', 'TOSS_API', 'SYSTEM_RECONCILIATION') NOT NULL,
    provider                ENUM ('MOCK', 'TOSS', 'TOSS_BILLING', 'KAKAOPAY') NOT NULL,
    status                  ENUM ('IMPORTED', 'MATCHED', 'MISMATCHED', 'LOCAL_PAYMENT_NOT_FOUND', 'PROVIDER_SETTLEMENT_NOT_FOUND', 'IGNORED') NOT NULL DEFAULT 'IMPORTED',
    deduplication_key       VARCHAR(64) NOT NULL,
    import_batch_key        VARCHAR(64) NOT NULL,
    source_file_name        VARCHAR(255) NULL,
    source_row_number       INT NULL,
    provider_settlement_id  VARCHAR(200) NULL,
    provider_payment_key    VARCHAR(200) NULL,
    order_id                VARCHAR(64) NOT NULL,
    payment_order_id        BIGINT NULL,
    subscription_payment_id BIGINT NULL,
    user_id                 BIGINT NULL,
    gross_amount            DECIMAL(15, 2) NOT NULL,
    refund_amount           DECIMAL(15, 2) NOT NULL DEFAULT 0,
    fee_amount              DECIMAL(15, 2) NOT NULL DEFAULT 0,
    vat_amount              DECIMAL(15, 2) NOT NULL DEFAULT 0,
    net_settlement_amount   DECIMAL(15, 2) NOT NULL,
    currency                VARCHAR(3) NOT NULL DEFAULT 'KRW',
    settlement_base_date    DATE NOT NULL,
    settlement_payout_date  DATE NULL,
    provider_status         VARCHAR(100) NULL,
    mismatch_reason         VARCHAR(500) NULL,
    operator_note           VARCHAR(500) NULL,
    source_payload          TEXT NULL,
    reconciled_at           DATETIME NULL,
    ignored_by              BIGINT NULL,
    ignored_at              DATETIME NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_settlements_deduplication_key (deduplication_key),
    KEY idx_payment_settlements_status_created (status, created_at),
    KEY idx_payment_settlements_order_id (order_id),
    KEY idx_payment_settlements_payment_key (provider_payment_key),
    KEY idx_payment_settlements_base_date (settlement_base_date),
    CONSTRAINT fk_payment_settlements_order FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_settlements_subscription_payment FOREIGN KEY (subscription_payment_id) REFERENCES subscription_payments (id),
    CONSTRAINT fk_payment_settlements_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_settlements_ignored_by FOREIGN KEY (ignored_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS ats_add_column_if_missing;
DROP PROCEDURE IF EXISTS ats_add_index_if_missing;
DROP PROCEDURE IF EXISTS ats_add_fk_if_missing;

-- 5) Post-apply validation queries.
SELECT COUNT(*) AS table_count
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'payment_settlements',
      'whitelist_export_batches',
      'whitelist_export_items'
  );

SELECT table_name, column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'tags' AND column_name = 'type')
      OR (table_name = 'whitelist_channels'
          AND column_name IN (
              'youtube_handle',
              'youtube_channel_id',
              'status',
              'is_primary',
              'requested_at',
              'exported_at',
              'processed_at',
              'removal_requested_at',
              'admin_note',
              'processed_by'
          ))
  )
ORDER BY table_name, ordinal_position;
