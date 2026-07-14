-- =============================================================================
-- ATStudio Manual DB Patch: Track waveform data
-- =============================================================================
-- WI: WI-20260714-ATS-035
-- Purpose:
--   Add the nullable TEXT tracks.waveform_data column required by
--   Track.waveformData to an already-created MySQL 8 database.
--
-- Applicability:
--   - This file is NOT auto-run by Spring Boot.
--   - Apply only after separate operator approval.
--   - Do not apply to a fresh database created from the current schema.sql.
--   - Test first on a backed-up copy because MySQL DDL implicitly commits.
--
-- Safety:
--   - Adds one nullable column only when it is missing.
--   - Performs no row updates, inserts, deletes, or table drops.
--   - Stops when tracks is missing or an existing column has a different
--     type/nullability contract; it does not coerce existing data.
-- =============================================================================

DELIMITER //

DROP PROCEDURE IF EXISTS ats_align_track_waveform_data//
CREATE PROCEDURE ats_align_track_waveform_data()
BEGIN
    DECLARE v_table_count INT DEFAULT 0;
    DECLARE v_column_count INT DEFAULT 0;
    DECLARE v_data_type VARCHAR(64) DEFAULT NULL;
    DECLARE v_is_nullable VARCHAR(3) DEFAULT NULL;

    SELECT COUNT(*) INTO v_table_count
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'tracks';

    IF v_table_count <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Missing tracks table; stop before applying waveform_data patch.';
    END IF;

    SELECT COUNT(*) INTO v_column_count
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'tracks'
      AND column_name = 'waveform_data';

    IF v_column_count = 0 THEN
        ALTER TABLE tracks ADD COLUMN waveform_data TEXT NULL AFTER duration;
    END IF;

    SELECT LOWER(data_type), is_nullable
    INTO v_data_type, v_is_nullable
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'tracks'
      AND column_name = 'waveform_data';

    IF v_data_type <> 'text' OR v_is_nullable <> 'YES' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Existing waveform_data contract is not nullable TEXT; stop without coercion.';
    END IF;
END//

DELIMITER ;

CALL ats_align_track_waveform_data();
DROP PROCEDURE IF EXISTS ats_align_track_waveform_data;

-- Operator verification only. This query does not mutate rows.
SELECT table_name, column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'tracks'
  AND column_name = 'waveform_data';
