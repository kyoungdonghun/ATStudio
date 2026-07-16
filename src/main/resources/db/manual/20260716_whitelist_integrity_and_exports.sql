-- WI-20260716-ATS-007
-- Additive source patch for retained MySQL 8.x databases.
-- Review and rehearse on a copied database before applying. This file is not auto-executed.

DELIMITER //

DROP PROCEDURE IF EXISTS ats_add_column_if_missing//
CREATE PROCEDURE ats_add_column_if_missing(
    IN target_table VARCHAR(64),
    IN target_column VARCHAR(64),
    IN ddl_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND column_name = target_column
    ) THEN
        SET @ddl = ddl_sql;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

DROP PROCEDURE IF EXISTS ats_add_index_if_missing//
CREATE PROCEDURE ats_add_index_if_missing(
    IN target_table VARCHAR(64),
    IN target_index VARCHAR(64),
    IN ddl_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = target_table
          AND index_name = target_index
    ) THEN
        SET @ddl = ddl_sql;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END//

DELIMITER ;

CALL ats_add_column_if_missing(
    'whitelist_channels',
    'version',
    'ALTER TABLE whitelist_channels ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER id');

CALL ats_add_column_if_missing(
    'whitelist_export_batches',
    'status_filter',
    'ALTER TABLE whitelist_export_batches ADD COLUMN status_filter ENUM (''DRAFT'', ''PENDING'', ''EXPORTED'', ''REGISTERED'', ''REVISION_REQUESTED'', ''REJECTED'', ''CANCELLED'', ''REMOVAL_REQUESTED'') NULL AFTER note');

CALL ats_add_column_if_missing(
    'whitelist_export_batches',
    'keyword_filter',
    'ALTER TABLE whitelist_export_batches ADD COLUMN keyword_filter VARCHAR(100) NULL AFTER status_filter');

CALL ats_add_column_if_missing(
    'whitelist_export_items',
    'item_order',
    'ALTER TABLE whitelist_export_items ADD COLUMN item_order INT NULL AFTER status_at_export');

CALL ats_add_column_if_missing(
    'whitelist_export_items',
    'channel_id_snapshot',
    'ALTER TABLE whitelist_export_items ADD COLUMN channel_id_snapshot BIGINT NULL AFTER item_order');

ALTER TABLE whitelist_export_items
    MODIFY COLUMN user_id_snapshot BIGINT NULL,
    MODIFY COLUMN user_nickname_snapshot VARCHAR(20) NULL;

CALL ats_add_index_if_missing(
    'whitelist_channels',
    'idx_whitelist_channels_export_scope',
    'ALTER TABLE whitelist_channels ADD INDEX idx_whitelist_channels_export_scope (status, requested_at, id)');

DROP PROCEDURE IF EXISTS ats_add_column_if_missing;
DROP PROCEDURE IF EXISTS ats_add_index_if_missing;

-- Source-only validation queries. Running these is ENVIRONMENT-CONDITIONAL.
SELECT table_name, column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'whitelist_channels' AND column_name = 'version')
      OR (table_name = 'whitelist_export_batches'
          AND column_name IN ('status_filter', 'keyword_filter'))
      OR (table_name = 'whitelist_export_items'
          AND column_name IN (
              'item_order',
              'channel_id_snapshot',
              'user_id_snapshot',
              'user_nickname_snapshot'))
  )
ORDER BY table_name, ordinal_position;

SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS indexed_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'whitelist_channels'
  AND index_name = 'idx_whitelist_channels_export_scope'
GROUP BY index_name;
