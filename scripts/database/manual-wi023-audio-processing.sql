-- WI-20260909-ATS-023 / REQ-20260909-ATS-005
-- Additive retained-data migration SOURCE ONLY. Never run automatically.
-- Apply once to an explicitly approved backed-up database while its writer is stopped.
-- Preserve the database + public/private storage tuple. No media or historical rows are rewritten.
-- Existing rows default READY/direct playback, with no queued backfill.
ALTER TABLE tracks
    ADD COLUMN stream_audio_file VARCHAR(255) NULL,
    ADD COLUMN stream_required TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN audio_processing_state VARCHAR(16) NOT NULL DEFAULT 'READY',
    ADD COLUMN audio_generation BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN pending_audio_file VARCHAR(255) NULL,
    ADD COLUMN claimed_audio_file VARCHAR(255) NULL,
    ADD COLUMN audio_claim_token VARCHAR(36) NULL,
    ADD COLUMN audio_attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN audio_error_code VARCHAR(64) NULL,
    ADD INDEX idx_tracks_audio_queue (audio_processing_state, id);
-- Before readiness, independently validate the new schema and strict storage integrity.
-- Rollback is forward-preserving: retain added columns and media; never DROP data to revert code.
-- Old binaries cannot safely serve newly created stream-required WAVs. Do not deploy them over
-- such rows without a separately approved data-aware rollback procedure.
