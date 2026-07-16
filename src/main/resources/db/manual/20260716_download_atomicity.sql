-- WI-20260716-ATS-009
-- Additive source-only patch for retained MySQL 8.x databases.
-- Review and rehearse on a copied database before applying. This file is not auto-executed.
-- Do not apply until the duplicate preflight query returns no rows; MySQL DDL may implicitly commit.

-- Preflight: duplicate user-track pairs must be resolved before the unique invariant is added.
SELECT user_id, track_id, COUNT(*) AS license_count
FROM licenses
GROUP BY user_id, track_id
HAVING COUNT(*) > 1;

DELIMITER //

DROP PROCEDURE IF EXISTS ats_add_license_user_track_unique_if_missing//
CREATE PROCEDURE ats_add_license_user_track_unique_if_missing()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'licenses'
          AND index_name = 'uq_licenses_user_track'
          AND non_unique <> 0
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'uq_licenses_user_track exists but is not unique; resolve before applying patch';
    ELSEIF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'licenses'
          AND index_name = 'uq_licenses_user_track'
          AND non_unique = 0
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM licenses
            GROUP BY user_id, track_id
            HAVING COUNT(*) > 1
        ) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Resolve duplicate licenses before adding uq_licenses_user_track';
        END IF;

        ALTER TABLE licenses
            ADD CONSTRAINT uq_licenses_user_track UNIQUE (user_id, track_id);
    END IF;
END//

DELIMITER ;

CALL ats_add_license_user_track_unique_if_missing();
DROP PROCEDURE IF EXISTS ats_add_license_user_track_unique_if_missing;

-- Source-only validation query. Running it is ENVIRONMENT-CONDITIONAL.
SELECT index_name, non_unique, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS indexed_columns
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'licenses'
  AND index_name = 'uq_licenses_user_track'
GROUP BY index_name, non_unique;
