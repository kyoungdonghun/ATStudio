-- =============================================================================
-- ATStudio Manual DB Patch: bounded payment reconciliation indexes
-- =============================================================================
-- Apply only after separate operator approval to a backed-up copied database.
-- Required order:
--   1. Apply the payment baseline and 20260714_payment_db_integrity.sql.
--   2. Apply this file.
--   3. Run the EXPLAIN statements below with representative copied data.
--   4. Start the application with spring.jpa.hibernate.ddl-auto=validate.
--
-- This patch adds indexes only. It does not update or delete ledger rows.
-- MySQL DDL may implicitly commit. ATS020-X-01 remains environment-conditional
-- until this patch is rehearsed against an approved retained-database copy.
-- =============================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS ats_payment_reconciliation_index_preflight//
CREATE PROCEDURE ats_payment_reconciliation_index_preflight()
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN ('payment_orders', 'billing_agreements');

    IF v_count <> 2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Missing payment reconciliation baseline table; stop before applying this patch.';
    END IF;
END//

CALL ats_payment_reconciliation_index_preflight()//
DROP PROCEDURE IF EXISTS ats_payment_reconciliation_index_preflight//

DROP PROCEDURE IF EXISTS ats_add_payment_reconciliation_index_if_missing//
CREATE PROCEDURE ats_add_payment_reconciliation_index_if_missing(
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

DELIMITER ;

CALL ats_add_payment_reconciliation_index_if_missing(
    'payment_orders',
    'idx_payment_orders_local_reconciliation',
    'ALTER TABLE payment_orders ADD KEY idx_payment_orders_local_reconciliation (status, id, purpose)'
);

CALL ats_add_payment_reconciliation_index_if_missing(
    'billing_agreements',
    'idx_billing_agreements_local_reconciliation',
    'ALTER TABLE billing_agreements ADD KEY idx_billing_agreements_local_reconciliation (status, id)'
);

DROP PROCEDURE IF EXISTS ats_add_payment_reconciliation_index_if_missing;

SELECT table_name, index_name, non_unique,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS indexed_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN (
      'idx_payment_orders_local_reconciliation',
      'idx_billing_agreements_local_reconciliation'
  )
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;

-- Reproduce query-plan evidence only on an approved copied/disposable MySQL 8
-- database with representative row counts. Record key, possible_keys, rows,
-- filtered, and Extra without copying payment payload or key material.
SET @ats_last_seen_id = 0;
SET @ats_batch_size = 100;

EXPLAIN FORMAT=JSON
SELECT payment_order.id
FROM payment_orders payment_order
WHERE payment_order.status = 'DONE'
  AND payment_order.purpose IN ('SUBSCRIBE', 'UPGRADE', 'RENEWAL')
  AND payment_order.id > @ats_last_seen_id
ORDER BY payment_order.id ASC
LIMIT 100;

EXPLAIN FORMAT=JSON
SELECT agreement.id
FROM billing_agreements agreement
WHERE agreement.status = 'ACTIVE'
  AND agreement.id > @ats_last_seen_id
ORDER BY agreement.id ASC
LIMIT 100;
