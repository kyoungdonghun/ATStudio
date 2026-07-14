-- =============================================================================
-- ATStudio Manual DB Patch: payment command and audit integrity
-- =============================================================================
-- Apply only after separate operator approval to a backed-up copied database.
-- Required order:
--   1. Restore or approve the missing payment-operations baseline.
--   2. Apply 20260615_align_payment_whitelist_schema.sql.
--   3. Apply 20260618_company_certification_documents.sql.
--   4. Apply this file.
--   5. Start the application with spring.jpa.hibernate.ddl-auto=validate.
--
-- Stop conditions:
--   - Do not use a client option that continues after SQL errors.
--   - SQLSTATE 45000 means STOP. Inspect the emitted rows, obtain a
--     row-specific disposition, restore the copy if needed, and rerun.
--   - This patch never deletes or automatically reconciles ambiguous ledger
--     rows. MySQL DDL implicitly commits, so application rollback comes first.
-- =============================================================================

-- 1. Preflight inventory and blocking checks
SELECT table_name, column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN (
      'billing_agreements',
      'user_subscriptions',
      'payment_orders',
      'subscription_payments',
      'payment_operation_audit_logs'
  )
ORDER BY table_name, ordinal_position;

DELIMITER //

DROP PROCEDURE IF EXISTS ats_payment_integrity_preflight//
CREATE PROCEDURE ats_payment_integrity_preflight()
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN (
          'billing_agreements',
          'user_subscriptions',
          'payment_orders',
          'subscription_payments',
          'payment_operation_audit_logs'
      );
    IF v_count <> 5 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Missing payment baseline table; stop before applying this patch.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND (
          (table_name = 'payment_orders' AND column_name IN (
              'id', 'order_id', 'user_id', 'purpose', 'provider', 'status',
              'subscription_id', 'user_subscription_id',
              'billing_agreement_id', 'billing_cycle', 'expires_at'
          ))
          OR (table_name = 'subscription_payments' AND column_name IN (
              'payment_order_id', 'provider', 'pg_transaction_id'
          ))
          OR (table_name = 'payment_operation_audit_logs' AND column_name IN (
              'action', 'target_type'
          ))
      );
    IF v_count <> 16 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Missing payment baseline column; stop before applying this patch.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM payment_orders
    WHERE status NOT IN ('DONE', 'FAILED', 'CANCELLED', 'EXPIRED');
    IF v_count > 0 THEN
        SELECT id, order_id, purpose, status
        FROM payment_orders
        WHERE status NOT IN ('DONE', 'FAILED', 'CANCELLED', 'EXPIRED')
        ORDER BY id;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Nonterminal payment orders exist; stop and drain or disposition them.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM (
        SELECT payment_order_id
        FROM subscription_payments
        WHERE payment_order_id IS NOT NULL
        GROUP BY payment_order_id
        HAVING COUNT(*) > 1
    ) duplicate_orders;
    IF v_count > 0 THEN
        SELECT payment_order_id, COUNT(*) AS duplicate_count
        FROM subscription_payments
        WHERE payment_order_id IS NOT NULL
        GROUP BY payment_order_id
        HAVING COUNT(*) > 1;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate subscription payment order finalizations exist.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM (
        SELECT provider, pg_transaction_id
        FROM subscription_payments
        WHERE provider IS NOT NULL
          AND pg_transaction_id IS NOT NULL
        GROUP BY provider, pg_transaction_id
        HAVING COUNT(*) > 1
    ) duplicate_transactions;
    IF v_count > 0 THEN
        SELECT provider, pg_transaction_id, COUNT(*) AS duplicate_count
        FROM subscription_payments
        WHERE provider IS NOT NULL
          AND pg_transaction_id IS NOT NULL
        GROUP BY provider, pg_transaction_id
        HAVING COUNT(*) > 1;
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate provider transaction finalizations exist.';
    END IF;
END//

DELIMITER ;

CALL ats_payment_integrity_preflight();
DROP PROCEDURE IF EXISTS ats_payment_integrity_preflight;

-- 2. Add command columns and expand ENUMs
DELIMITER //

DROP PROCEDURE IF EXISTS ats_add_payment_column_if_missing//
CREATE PROCEDURE ats_add_payment_column_if_missing(
    IN p_column_name VARCHAR(64),
    IN p_alter_sql TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'payment_orders'
          AND column_name = p_column_name
    ) THEN
        SET @ats_sql = p_alter_sql;
        PREPARE ats_stmt FROM @ats_sql;
        EXECUTE ats_stmt;
        DEALLOCATE PREPARE ats_stmt;
    END IF;
END//

DELIMITER ;

CALL ats_add_payment_column_if_missing(
    'command_key',
    'ALTER TABLE payment_orders ADD COLUMN command_key VARCHAR(191) NULL AFTER order_id'
);
CALL ats_add_payment_column_if_missing(
    'billing_period_start',
    'ALTER TABLE payment_orders ADD COLUMN billing_period_start DATE NULL AFTER billing_cycle'
);
CALL ats_add_payment_column_if_missing(
    'provider_attempt',
    'ALTER TABLE payment_orders ADD COLUMN provider_attempt INT NOT NULL DEFAULT 0 AFTER billing_period_start'
);
CALL ats_add_payment_column_if_missing(
    'provider_idempotency_key',
    'ALTER TABLE payment_orders ADD COLUMN provider_idempotency_key VARCHAR(100) NULL AFTER provider_attempt'
);
CALL ats_add_payment_column_if_missing(
    'processing_started_at',
    'ALTER TABLE payment_orders ADD COLUMN processing_started_at DATETIME NULL AFTER provider_idempotency_key'
);

ALTER TABLE payment_orders
    MODIFY COLUMN status ENUM (
        'READY', 'IN_PROGRESS', 'PROCESSING', 'PROVIDER_SUCCEEDED',
        'PENDING_PROVIDER_CONFIRMATION', 'DONE', 'FAILED', 'CANCELLED', 'EXPIRED'
    ) NOT NULL DEFAULT 'READY';

ALTER TABLE subscription_payments
    MODIFY COLUMN pg_transaction_id VARCHAR(200) NULL;

ALTER TABLE payment_operation_audit_logs
    MODIFY COLUMN action ENUM (
        'RECONCILIATION_INCIDENT_STATUS_UPDATE',
        'RECEIPT_EVIDENCE_CREATED',
        'PAYMENT_REFUND_REQUESTED',
        'PAYMENT_REFUND_APPROVED',
        'PAYMENT_REFUND_PROCESSING',
        'PAYMENT_REFUND_SUCCEEDED',
        'PAYMENT_REFUND_FAILED',
        'PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION',
        'PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED',
        'PAYMENT_ENTITLEMENT_CORRECTION_APPROVED',
        'PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING',
        'PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED',
        'PAYMENT_ENTITLEMENT_CORRECTION_FAILED',
        'PAYMENT_SETTLEMENT_IMPORTED',
        'PAYMENT_SETTLEMENT_RECONCILED',
        'PAYMENT_SETTLEMENT_IGNORED'
    ) NOT NULL,
    MODIFY COLUMN target_type ENUM (
        'RECONCILIATION_INCIDENT',
        'PAYMENT_RECEIPT',
        'PAYMENT_REFUND',
        'PAYMENT_ENTITLEMENT_CORRECTION',
        'PAYMENT_SETTLEMENT'
    ) NOT NULL;

-- 3. Backfill legacy command identity
SELECT id, order_id, billing_period_start,
       DATE_SUB(DATE(expires_at), INTERVAL 3 DAY) AS assumed_billing_period_start
FROM payment_orders
WHERE purpose = 'RENEWAL'
  AND billing_period_start IS NOT NULL
  AND billing_period_start <> DATE_SUB(DATE(expires_at), INTERVAL 3 DAY)
ORDER BY id;

DELIMITER //

DROP PROCEDURE IF EXISTS ats_check_renewal_backfill_assumption//
CREATE PROCEDURE ats_check_renewal_backfill_assumption()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM payment_orders
        WHERE purpose = 'RENEWAL'
          AND billing_period_start IS NOT NULL
          AND billing_period_start <> DATE_SUB(DATE(expires_at), INTERVAL 3 DAY)
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Renewal period conflicts with the retained three-day grace assumption.';
    END IF;
END//

DELIMITER ;

CALL ats_check_renewal_backfill_assumption();
DROP PROCEDURE IF EXISTS ats_check_renewal_backfill_assumption;

UPDATE payment_orders
SET billing_period_start = DATE_SUB(DATE(expires_at), INTERVAL 3 DAY)
WHERE purpose = 'RENEWAL'
  AND billing_period_start IS NULL;

UPDATE payment_orders payment_order
JOIN (
    SELECT MIN(id) AS canonical_id
    FROM payment_orders
    WHERE purpose = 'RENEWAL'
      AND billing_agreement_id IS NOT NULL
      AND user_subscription_id IS NOT NULL
      AND billing_period_start IS NOT NULL
    GROUP BY billing_agreement_id, user_subscription_id, purpose, billing_period_start
) canonical ON canonical.canonical_id = payment_order.id
SET payment_order.command_key = CONCAT(
        'RENEWAL:',
        payment_order.billing_agreement_id, ':',
        payment_order.user_subscription_id, ':',
        DATE_FORMAT(payment_order.billing_period_start, '%Y-%m-%d')
    )
WHERE payment_order.command_key IS NULL;

UPDATE payment_orders
SET command_key = CONCAT('LEGACY:', order_id)
WHERE command_key IS NULL;

-- 4. Block ambiguous renewal groups
SELECT billing_agreement_id, user_subscription_id, purpose,
       billing_period_start, COUNT(*) AS duplicate_count
FROM payment_orders
WHERE purpose = 'RENEWAL'
  AND billing_agreement_id IS NOT NULL
  AND user_subscription_id IS NOT NULL
  AND billing_period_start IS NOT NULL
GROUP BY billing_agreement_id, user_subscription_id, purpose, billing_period_start
HAVING COUNT(*) > 1;

DELIMITER //

DROP PROCEDURE IF EXISTS ats_payment_integrity_constraint_preflight//
CREATE PROCEDURE ats_payment_integrity_constraint_preflight()
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM (
        SELECT billing_agreement_id, user_subscription_id, purpose, billing_period_start
        FROM payment_orders
        WHERE purpose = 'RENEWAL'
          AND billing_agreement_id IS NOT NULL
          AND user_subscription_id IS NOT NULL
          AND billing_period_start IS NOT NULL
        GROUP BY billing_agreement_id, user_subscription_id, purpose, billing_period_start
        HAVING COUNT(*) > 1
    ) duplicate_periods;
    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate renewal periods require approved row-specific disposition.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM (
        SELECT command_key
        FROM payment_orders
        WHERE command_key IS NOT NULL
        GROUP BY command_key
        HAVING COUNT(*) > 1
    ) duplicate_commands;
    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate payment command keys require approved disposition.';
    END IF;

    SELECT COUNT(*) INTO v_count
    FROM (
        SELECT provider, provider_idempotency_key
        FROM payment_orders
        WHERE provider_idempotency_key IS NOT NULL
        GROUP BY provider, provider_idempotency_key
        HAVING COUNT(*) > 1
    ) duplicate_attempt_keys;
    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Duplicate provider attempt keys require approved disposition.';
    END IF;
END//

DELIMITER ;

CALL ats_payment_integrity_constraint_preflight();
DROP PROCEDURE IF EXISTS ats_payment_integrity_constraint_preflight;

-- 5. Add final constraints and indexes
DELIMITER //

DROP PROCEDURE IF EXISTS ats_add_payment_index_if_missing//
CREATE PROCEDURE ats_add_payment_index_if_missing(
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

CALL ats_add_payment_index_if_missing(
    'payment_orders',
    'uq_payment_orders_command_key',
    'ALTER TABLE payment_orders ADD UNIQUE KEY uq_payment_orders_command_key (command_key)'
);
CALL ats_add_payment_index_if_missing(
    'payment_orders',
    'uq_payment_orders_provider_attempt_key',
    'ALTER TABLE payment_orders ADD UNIQUE KEY uq_payment_orders_provider_attempt_key (provider, provider_idempotency_key)'
);
CALL ats_add_payment_index_if_missing(
    'payment_orders',
    'uq_payment_orders_renewal_period',
    'ALTER TABLE payment_orders ADD UNIQUE KEY uq_payment_orders_renewal_period (billing_agreement_id, user_subscription_id, purpose, billing_period_start)'
);
CALL ats_add_payment_index_if_missing(
    'payment_orders',
    'idx_payment_orders_status_processing',
    'ALTER TABLE payment_orders ADD KEY idx_payment_orders_status_processing (status, processing_started_at)'
);
CALL ats_add_payment_index_if_missing(
    'subscription_payments',
    'uq_subscription_payments_order',
    'ALTER TABLE subscription_payments ADD UNIQUE KEY uq_subscription_payments_order (payment_order_id)'
);
CALL ats_add_payment_index_if_missing(
    'subscription_payments',
    'uq_subscription_payments_provider_transaction',
    'ALTER TABLE subscription_payments ADD UNIQUE KEY uq_subscription_payments_provider_transaction (provider, pg_transaction_id)'
);

DROP PROCEDURE IF EXISTS ats_add_payment_column_if_missing;
DROP PROCEDURE IF EXISTS ats_add_payment_index_if_missing;

-- 6. Post-apply contract comparison
SELECT table_name, column_name, column_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'payment_orders' AND column_name IN (
          'status', 'command_key', 'billing_period_start', 'provider_attempt',
          'provider_idempotency_key', 'processing_started_at'
      ))
      OR (table_name = 'subscription_payments' AND column_name = 'pg_transaction_id')
      OR (table_name = 'payment_operation_audit_logs'
          AND column_name IN ('action', 'target_type'))
  )
ORDER BY table_name, ordinal_position;

SELECT table_name, index_name, non_unique,
       GROUP_CONCAT(column_name ORDER BY seq_in_index) AS indexed_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name IN (
      'uq_payment_orders_command_key',
      'uq_payment_orders_provider_attempt_key',
      'uq_payment_orders_renewal_period',
      'idx_payment_orders_status_processing',
      'uq_subscription_payments_order',
      'uq_subscription_payments_provider_transaction'
  )
GROUP BY table_name, index_name, non_unique
ORDER BY table_name, index_name;
