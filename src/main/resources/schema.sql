-- =============================================================================
-- ATStudio V1 Fresh Database Baseline
-- =============================================================================
-- Source  : Current JPA entity model after WI-20260717-ATS-002
-- Engine  : InnoDB
-- Charset : utf8mb4 / utf8mb4_unicode_ci
-- DB      : atstudio  (see application.yml)
--
-- NOTE: This is a fresh-only, fail-closed baseline.
--       Apply it exactly once to an empty atstudio database, then apply seed.sql.
--       Existing databases require a separately reviewed migration path.
--
-- Usage:
--   mysql -u root -p atstudio < src/main/resources/schema.sql
-- =============================================================================

-- =============================================================================
-- SECTION 1. INDEPENDENT TABLES (no FK dependencies)
-- =============================================================================

-- ─────────────────────────────────────────────
-- 1.1  users
-- ─────────────────────────────────────────────
CREATE TABLE users
(
    id             BIGINT                                     NOT NULL AUTO_INCREMENT,
    nickname       VARCHAR(20)                                NOT NULL,
    email          VARCHAR(100)                               NOT NULL,
    password       VARCHAR(255)                               NULL                                COMMENT 'BCrypt hash. NULL for social-login-only accounts.',
    phone_company  VARCHAR(20)                                NULL                                COMMENT 'For business members.',
    phone_personal VARCHAR(20)                                NULL                                COMMENT 'NULL on social login; filled during profile completion.',
    is_verified    TINYINT(1)                                 NOT NULL DEFAULT 0                  COMMENT 'Email or phone verification flag.',
    role           ENUM ('USER', 'ADMIN')                     NOT NULL DEFAULT 'USER',
    job            ENUM ('EDITOR', 'ARTIST', 'FREELANCER')    NULL     DEFAULT NULL               COMMENT 'NULL on social login; filled during profile completion.',
    company_name   VARCHAR(100)                               NULL                                COMMENT 'Company name for BUSINESS members.',
    user_type      ENUM ('INDIVIDUAL', 'BUSINESS')            NOT NULL DEFAULT 'INDIVIDUAL',
    is_deleted     TINYINT(1)                                 NOT NULL DEFAULT 0                  COMMENT 'Soft delete flag.',
    refresh_token  VARCHAR(512)                               NULL                                COMMENT 'SHA-256 hashed refresh token. NULL when logged out.',
    created_at     DATETIME                                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME                                   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_nickname (nickname),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 1.2  subscriptions
-- ─────────────────────────────────────────────
CREATE TABLE subscriptions
(
    id                     BIGINT                          NOT NULL AUTO_INCREMENT,
    name                   VARCHAR(30)                     NOT NULL                    COMMENT 'STANDARD / DELUXE / PREMIUM',
    description            TEXT                            NULL,
    user_type              ENUM ('INDIVIDUAL', 'BUSINESS') NOT NULL,
    price_monthly          DECIMAL(10, 2)                  NOT NULL,
    price_yearly           DECIMAL(10, 2)                  NOT NULL,
    download_per_day       INT                             NOT NULL                    COMMENT '-1 = unlimited.',
    max_whitelist_channels INT                             NOT NULL,
    max_playlists          INT                             NOT NULL DEFAULT 3          COMMENT 'Max playlists per subscriber.',
    is_active              TINYINT(1)                      NOT NULL DEFAULT 1          COMMENT 'Whether selectable by users.',
    created_at             DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subscriptions_name_type (name, user_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 1.3  tags
-- ─────────────────────────────────────────────
CREATE TABLE tags
(
    id         BIGINT                                 NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)                            NOT NULL,
    type       ENUM ('MOOD', 'GENRE', 'INSTRUMENT', 'USAGE') NOT NULL,
    created_at DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tags_name (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- SECTION 2. TABLES DEPENDENT ON SECTION 1
-- =============================================================================

-- ─────────────────────────────────────────────
-- 2.1  social_accounts  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE social_accounts
(
    id          BIGINT                              NOT NULL AUTO_INCREMENT,
    user_id     BIGINT                              NOT NULL,
    provider    ENUM ('GOOGLE', 'KAKAO', 'NAVER')   NOT NULL,
    provider_id VARCHAR(255)                        NOT NULL   COMMENT 'User ID assigned by the social provider.',
    created_at  DATETIME                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_social_accounts_provider_id (provider, provider_id),
    CONSTRAINT fk_social_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.2  user_subscriptions  (→ users, subscriptions)
-- ─────────────────────────────────────────────
CREATE TABLE user_subscriptions
(
    id              BIGINT                                  NOT NULL AUTO_INCREMENT,
    user_id         BIGINT                                  NOT NULL,
    subscription_id BIGINT                                  NOT NULL,
    billing_cycle           ENUM ('MONTHLY', 'YEARLY')              NOT NULL,
    status                  ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    started_at              DATE                                    NOT NULL COMMENT 'Current period start date.',
    expires_at              DATE                                    NOT NULL COMMENT 'Current period end date. Next billing date = expires_at + 1 day.',
    pending_subscription_id BIGINT                                  NULL     COMMENT 'Downgrade scheduled plan (applied at next billing).',
    pending_billing_cycle   ENUM ('MONTHLY', 'YEARLY')              NULL     COMMENT 'MONTHLY or YEARLY for pending change.',
    created_at              DATETIME                                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_subscriptions_user (user_id)         COMMENT 'One active subscription per user.',
    CONSTRAINT fk_user_subscriptions_user            FOREIGN KEY (user_id)                  REFERENCES users         (id),
    CONSTRAINT fk_user_subscriptions_subscription    FOREIGN KEY (subscription_id)          REFERENCES subscriptions (id),
    CONSTRAINT fk_user_subscriptions_pending_sub     FOREIGN KEY (pending_subscription_id)  REFERENCES subscriptions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.3  company_certifications  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE company_certifications
(
    id                 BIGINT                                                          NOT NULL AUTO_INCREMENT,
    version            BIGINT                                                          NOT NULL DEFAULT 0,
    user_id            BIGINT                                                          NOT NULL,
    status             ENUM ('PENDING', 'APPROVED', 'REVISION_REQUESTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_note         TEXT                                                            NULL     COMMENT 'Reason for revision request, etc.',
    document_path      VARCHAR(500)                                                    NOT NULL COMMENT 'Per-user dedicated folder path (e.g. /uploads/company-docs/{user_id}/).',
    certification_code VARCHAR(50)                                                     NULL     COMMENT 'UUID-based. Issued upon approval.',
    approved_at        DATETIME                                                        NULL,
    created_at         DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_company_certification_code (certification_code),
    KEY idx_company_certifications_user_status_id (user_id, status, id),
    CONSTRAINT fk_company_certifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE company_certification_documents
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    certification_id  BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original client filename for admin review display.',
    stored_path       VARCHAR(500) NOT NULL COMMENT 'StorageService relative path. Do not expose directly to users.',
    content_type      VARCHAR(100) NULL,
    size_bytes        BIGINT       NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_company_certification_documents_certification_id (certification_id),
    CONSTRAINT fk_company_certification_documents_certification
        FOREIGN KEY (certification_id) REFERENCES company_certifications (id)
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE company_certification_audit_logs
(
    id               BIGINT                                      NOT NULL AUTO_INCREMENT,
    certification_id BIGINT                                      NOT NULL,
    actor_user_id    BIGINT                                      NOT NULL,
    action           ENUM ('REVIEWED', 'DOCUMENT_ACCESS_GRANTED') NOT NULL,
    document_id      BIGINT                                      NULL COMMENT 'Opaque document ID only; no document path or content is stored.',
    from_status      VARCHAR(30)                                 NULL,
    to_status        VARCHAR(30)                                 NULL,
    created_at       DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME                                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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

-- ─────────────────────────────────────────────
-- 2.4  tracks  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE tracks
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100) NOT NULL,
    thumbnail    VARCHAR(255) NULL,
    bpm          INT          NOT NULL,
    tonality     VARCHAR(10)  NOT NULL COMMENT 'e.g. C, Am, F#m',
    description  TEXT         NULL,
    audio_file   VARCHAR(255) NOT NULL COMMENT 'Original storage path for entitled download; never expose through the public static route.',
    duration     INT          NOT NULL DEFAULT 0 COMMENT 'Duration in seconds, auto-extracted from audio file.',
    waveform_data TEXT        NULL     COMMENT 'Waveform peak data extracted from the uploaded audio file.',
    user_id      BIGINT       NOT NULL COMMENT 'Copyright holder (currently admin/artist only).',
    is_active    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Published after admin review.',
    play_count     BIGINT       NOT NULL DEFAULT 0,
    like_count     BIGINT       NOT NULL DEFAULT 0,
    download_count BIGINT       NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_tracks_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.5  playlists  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE playlists
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(50)  NOT NULL,
    description TEXT         NULL,
    thumbnail   VARCHAR(255) NULL,
    user_id     BIGINT       NOT NULL,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_playlists_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.6  whitelist_channels  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE whitelist_channels
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    version              BIGINT       NOT NULL DEFAULT 0,
    user_id              BIGINT       NOT NULL,
    channel_url          VARCHAR(255) NOT NULL COMMENT 'YouTube channel URL.',
    channel_name         VARCHAR(100) NOT NULL,
    youtube_handle       VARCHAR(100) NULL COMMENT 'YouTube handle, e.g. @channel.',
    youtube_channel_id   VARCHAR(100) NULL COMMENT 'YouTube canonical channel ID, e.g. UC...',
    status               ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NOT NULL DEFAULT 'DRAFT',
    is_primary           TINYINT(1)   NOT NULL DEFAULT 0,
    requested_at         DATETIME     NULL,
    exported_at          DATETIME     NULL,
    processed_at         DATETIME     NULL,
    removal_requested_at DATETIME     NULL,
    admin_note           VARCHAR(500) NULL,
    processed_by         BIGINT       NULL,
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_whitelist_channels_user_status (user_id, status),
    KEY idx_whitelist_channels_status_requested (status, requested_at, id),
    CONSTRAINT fk_whitelist_channels_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_whitelist_channels_processed_by FOREIGN KEY (processed_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE whitelist_export_batches
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    file_name   VARCHAR(255) NOT NULL,
    item_count  INT          NOT NULL,
    exported_by BIGINT       NULL,
    note        VARCHAR(500) NULL,
    status_filter ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NULL,
    keyword_filter VARCHAR(100) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_whitelist_export_batches_exported_by FOREIGN KEY (exported_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE whitelist_export_items
(
    id                          BIGINT       NOT NULL AUTO_INCREMENT,
    batch_id                    BIGINT       NOT NULL,
    whitelist_channel_id        BIGINT       NULL,
    status_at_export            ENUM ('DRAFT', 'PENDING', 'EXPORTED', 'REGISTERED', 'REVISION_REQUESTED', 'REJECTED', 'CANCELLED', 'REMOVAL_REQUESTED') NOT NULL,
    item_order                   INT          NULL,
    channel_id_snapshot         BIGINT       NULL,
    user_email_snapshot         VARCHAR(100) NOT NULL,
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

-- ─────────────────────────────────────────────
-- 2.7  questions  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE questions
(
    id         BIGINT                                                       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT                                                       NOT NULL,
    title      VARCHAR(200)                                                 NOT NULL,
    content    TEXT                                                         NOT NULL,
    category   ENUM ('DOWNLOAD', 'PAYMENT', 'COPYRIGHT', 'PRODUCTION', 'OTHER') NOT NULL,
    is_public  TINYINT(1)                                                   NOT NULL DEFAULT 0,
    status     ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')          NOT NULL DEFAULT 'OPEN',
    created_at DATETIME                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_questions_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.8  notices  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE notices
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL COMMENT 'ADMIN author.',
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    is_pinned   TINYINT(1)   NOT NULL DEFAULT 0,
    view_count  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notices_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- =============================================================================
-- SECTION 3. TABLES DEPENDENT ON SECTION 2
-- =============================================================================

-- ─────────────────────────────────────────────
-- 3.1  track_tags  (→ tracks, tags)
-- ─────────────────────────────────────────────
CREATE TABLE track_tags
(
    track_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (track_id, tag_id),
    CONSTRAINT fk_track_tags_track FOREIGN KEY (track_id) REFERENCES tracks (id),
    CONSTRAINT fk_track_tags_tag   FOREIGN KEY (tag_id)   REFERENCES tags   (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.2  playlist_tracks  (→ playlists, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE playlist_tracks
(
    playlist_id BIGINT NOT NULL,
    track_id    BIGINT NOT NULL,
    track_order INT    NOT NULL,
    PRIMARY KEY (playlist_id, track_id),
    CONSTRAINT fk_playlist_tracks_playlist FOREIGN KEY (playlist_id) REFERENCES playlists (id),
    CONSTRAINT fk_playlist_tracks_track    FOREIGN KEY (track_id)    REFERENCES tracks    (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.3  track_downloads  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE track_downloads
(
    id            BIGINT   NOT NULL AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    track_id      BIGINT   NOT NULL,
    downloaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_track_downloads_user_date (user_id, downloaded_at)  COMMENT 'Optimizes daily COUNT query: WHERE user_id=? AND DATE(downloaded_at)=CURDATE()',
    CONSTRAINT fk_track_downloads_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_track_downloads_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.5  likes  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE likes
(
    user_id    BIGINT   NOT NULL,
    track_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, track_id),
    CONSTRAINT fk_likes_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_likes_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.7  licenses  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE licenses
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    track_id     BIGINT      NOT NULL,
    license_code VARCHAR(50) NOT NULL COMMENT 'UUID-based. Proof of copyright.',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Mapped as issuedAt in API response.',
    updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_licenses_code       (license_code),
    UNIQUE KEY uq_licenses_user_track (user_id, track_id) COMMENT 'One license per user per track.',
    CONSTRAINT fk_licenses_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_licenses_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.8  billing_agreements / payment_orders / subscription_payments / payment_settlements / payment_refunds / payment_entitlement_corrections / payment_reconciliation_incidents / payment_receipts / payment_operation_audit_logs
-- ─────────────────────────────────────────────
CREATE TABLE billing_agreements
(
    id                      BIGINT                                                         NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT                                                         NOT NULL,
    provider                ENUM ('TOSS')                              NOT NULL,
    status                  ENUM ('READY', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED')   NOT NULL DEFAULT 'READY',
    provider_customer_key   VARCHAR(300)                                                    NOT NULL,
    billing_key_ciphertext  VARCHAR(1000)                                                   NULL,
    billing_key_fingerprint VARCHAR(128)                                                    NULL,
    pay_method              VARCHAR(50)                                                     NULL,
    masked_method           VARCHAR(100)                                                    NULL,
    next_billing_at         DATE                                                            NULL,
    renewal_retry_at        DATE                                                            NULL,
    last_charged_at         DATETIME                                                        NULL,
    failure_count           INT                                                             NOT NULL DEFAULT 0,
    cancelled_at            DATETIME                                                        NULL,
    billing_key_cleanup_status ENUM (
        'NONE',
        'REQUIRED',
        'PROCESSING',
        'PENDING_PROVIDER_CONFIRMATION',
        'FAILED'
    ) NOT NULL DEFAULT 'NONE',
    billing_key_cleanup_started_at DATETIME                                                 NULL,
    created_at              DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_billing_agreements_user_provider (user_id, provider),
    UNIQUE KEY uq_billing_agreements_provider_customer (provider, provider_customer_key),
    KEY idx_billing_agreements_status_next (status, next_billing_at),
    KEY idx_billing_agreements_renewal_retry (status, renewal_retry_at, id),
    KEY idx_billing_agreements_cleanup (billing_key_cleanup_status, billing_key_cleanup_started_at, id),
    KEY idx_billing_agreements_local_reconciliation (status, id),
    CONSTRAINT fk_billing_agreements_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_orders
(
    id                       BIGINT                                                               NOT NULL AUTO_INCREMENT,
    order_id                 VARCHAR(64)                                                          NOT NULL,
    command_key              VARCHAR(191)                                                         NULL,
    user_id                  BIGINT                                                               NOT NULL,
    purpose                  ENUM ('SUBSCRIBE', 'UPGRADE', 'RENEWAL', 'BILLING_AGREEMENT')         NOT NULL,
    provider                 ENUM ('TOSS')                     NOT NULL,
    status                   ENUM ('READY', 'IN_PROGRESS', 'PROCESSING', 'PROVIDER_SUCCEEDED', 'PENDING_PROVIDER_CONFIRMATION', 'DONE', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'READY',
    subscription_id          BIGINT                                                               NOT NULL,
    user_subscription_id     BIGINT                                                               NULL,
    billing_agreement_id     BIGINT                                                               NULL,
    billing_cycle            ENUM ('MONTHLY', 'YEARLY')                                           NOT NULL,
    upgrade_target_billing_cycle ENUM ('MONTHLY', 'YEARLY')                                       NULL,
    billing_period_start     DATE                                                                 NULL,
    provider_attempt         INT                                                                  NOT NULL DEFAULT 0,
    provider_idempotency_key VARCHAR(100)                                                         NULL,
    processing_started_at    DATETIME                                                             NULL,
    amount                   DECIMAL(10, 2)                                                       NOT NULL,
    currency                 VARCHAR(3)                                                           NOT NULL DEFAULT 'KRW',
    pg_transaction_id        VARCHAR(200)                                                         NULL,
    provider_payload         TEXT                                                                 NULL,
    failure_code             VARCHAR(100)                                                         NULL,
    failure_message          VARCHAR(500)                                                         NULL,
    expires_at               DATETIME                                                             NOT NULL,
    confirmed_at             DATETIME                                                             NULL,
    created_at               DATETIME                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_orders_order_id (order_id),
    UNIQUE KEY uq_payment_orders_command_key (command_key),
    UNIQUE KEY uq_payment_orders_provider_attempt_key (provider, provider_idempotency_key),
    UNIQUE KEY uq_payment_orders_renewal_period (billing_agreement_id, user_subscription_id, purpose, billing_period_start),
    KEY idx_payment_orders_user_status (user_id, status),
    KEY idx_payment_orders_status_processing (status, processing_started_at),
    KEY idx_payment_orders_local_reconciliation (status, id, purpose),
    CONSTRAINT fk_payment_orders_user              FOREIGN KEY (user_id)              REFERENCES users              (id),
    CONSTRAINT fk_payment_orders_subscription      FOREIGN KEY (subscription_id)      REFERENCES subscriptions      (id),
    CONSTRAINT fk_payment_orders_user_subscription FOREIGN KEY (user_subscription_id) REFERENCES user_subscriptions (id),
    CONSTRAINT fk_payment_orders_billing_agreement FOREIGN KEY (billing_agreement_id) REFERENCES billing_agreements (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE subscription_payments
(
    id                   BIGINT                          NOT NULL AUTO_INCREMENT,
    user_id              BIGINT                          NOT NULL,
    user_subscription_id BIGINT                          NOT NULL COMMENT 'Links payment to a specific subscription session.',
    subscription_id      BIGINT                          NOT NULL,
    payment_order_id     BIGINT                          NULL,
    billing_agreement_id BIGINT                          NULL,
    billing_cycle        ENUM ('MONTHLY', 'YEARLY')      NOT NULL,
    provider             ENUM ('TOSS') NOT NULL,
    amount               DECIMAL(10, 2)                  NOT NULL COMMENT 'Prorated amount for upgrades.',
    payment_status       ENUM ('READY', 'DONE', 'REFUND') NOT NULL DEFAULT 'READY',
    pg_transaction_id    VARCHAR(200)                    NULL     COMMENT 'PG provider transaction ID.',
    created_at           DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subscription_payments_order (payment_order_id),
    UNIQUE KEY uq_subscription_payments_provider_transaction (provider, pg_transaction_id),
    CONSTRAINT fk_subscription_payments_user         FOREIGN KEY (user_id)              REFERENCES users              (id),
    CONSTRAINT fk_subscription_payments_user_sub     FOREIGN KEY (user_subscription_id) REFERENCES user_subscriptions (id),
    CONSTRAINT fk_subscription_payments_subscription FOREIGN KEY (subscription_id)      REFERENCES subscriptions      (id),
    CONSTRAINT fk_subscription_payments_order        FOREIGN KEY (payment_order_id)     REFERENCES payment_orders     (id),
    CONSTRAINT fk_subscription_payments_agreement    FOREIGN KEY (billing_agreement_id) REFERENCES billing_agreements (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_settlements
(
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    source                  ENUM ('CSV_MANUAL', 'TOSS_API', 'SYSTEM_RECONCILIATION') NOT NULL,
    provider                ENUM ('TOSS') NOT NULL,
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

CREATE TABLE payment_refunds
(
    id                             BIGINT NOT NULL AUTO_INCREMENT,
    subscription_payment_id        BIGINT NOT NULL,
    payment_order_id               BIGINT NOT NULL,
    user_id                        BIGINT NOT NULL,
    provider                       ENUM ('TOSS') NOT NULL,
    status                         ENUM (
        'REQUESTED',
        'APPROVED',
        'PROCESSING',
        'SUCCEEDED',
        'FAILED',
        'PENDING_PROVIDER_CONFIRMATION',
        'CANCELLED'
    ) NOT NULL DEFAULT 'REQUESTED',
    amount                         DECIMAL(10, 2) NOT NULL,
    currency                       VARCHAR(3) NOT NULL DEFAULT 'KRW',
    reason_code                    ENUM (
        'CUSTOMER_REQUEST',
        'DUPLICATE_PAYMENT',
        'PAYMENT_ERROR',
        'SERVICE_ISSUE',
        'ADMIN_ADJUSTMENT',
        'OTHER'
    ) NOT NULL,
    reason_note                    VARCHAR(500) NULL,
    idempotency_key                VARCHAR(100) NOT NULL,
    provider_payment_key           VARCHAR(200) NOT NULL,
    provider_refund_transaction_id VARCHAR(200) NULL,
    provider_payload               TEXT NULL COMMENT 'Sanitized provider cancel response only.',
    failure_code                   VARCHAR(100) NULL,
    failure_message                VARCHAR(500) NULL,
    requested_by                   BIGINT NULL,
    approved_by                    BIGINT NULL,
    executed_by                    BIGINT NULL,
    approved_at                    DATETIME NULL,
    processing_started_at          DATETIME NULL,
    executed_at                    DATETIME NULL,
    created_at                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_refunds_idempotency (idempotency_key),
    KEY idx_payment_refunds_status_created (status, created_at),
    KEY idx_payment_refunds_payment (subscription_payment_id),
    KEY idx_payment_refunds_user_created (user_id, created_at),
    KEY idx_payment_refunds_status_processing (status, processing_started_at, id),
    CONSTRAINT fk_payment_refunds_subscription_payment
        FOREIGN KEY (subscription_payment_id) REFERENCES subscription_payments (id),
    CONSTRAINT fk_payment_refunds_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_refunds_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_refunds_requested_by
        FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT fk_payment_refunds_approved_by
        FOREIGN KEY (approved_by) REFERENCES users (id),
    CONSTRAINT fk_payment_refunds_executed_by
        FOREIGN KEY (executed_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_entitlement_corrections
(
    id                              BIGINT NOT NULL AUTO_INCREMENT,
    payment_refund_id               BIGINT NOT NULL,
    subscription_payment_id         BIGINT NOT NULL,
    payment_order_id                BIGINT NOT NULL,
    user_subscription_id            BIGINT NOT NULL,
    user_id                         BIGINT NOT NULL,
    provider                        ENUM ('TOSS') NOT NULL,
    status                          ENUM ('REQUESTED', 'APPROVED', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'REQUESTED',
    action                          ENUM ('SET_SUBSCRIPTION_STATE') NOT NULL DEFAULT 'SET_SUBSCRIPTION_STATE',
    before_subscription_id          BIGINT NOT NULL,
    before_billing_cycle            ENUM ('MONTHLY', 'YEARLY') NOT NULL,
    before_status                   ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED') NOT NULL,
    before_expires_at               DATE NOT NULL,
    before_pending_subscription_id  BIGINT NULL,
    before_pending_billing_cycle    ENUM ('MONTHLY', 'YEARLY') NULL,
    target_subscription_id          BIGINT NOT NULL,
    target_billing_cycle            ENUM ('MONTHLY', 'YEARLY') NOT NULL,
    target_status                   ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED') NOT NULL,
    target_expires_at               DATE NOT NULL,
    clear_pending_change            BOOLEAN NOT NULL DEFAULT FALSE,
    cancel_billing_agreement        BOOLEAN NOT NULL DEFAULT FALSE,
    before_billing_agreement_status ENUM ('READY', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED') NULL,
    after_billing_agreement_status  ENUM ('READY', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED') NULL,
    reason_note                     VARCHAR(500) NULL,
    failure_code                    VARCHAR(100) NULL,
    failure_message                 VARCHAR(500) NULL,
    requested_by                    BIGINT NULL,
    approved_by                     BIGINT NULL,
    executed_by                     BIGINT NULL,
    approved_at                     DATETIME NULL,
    executed_at                     DATETIME NULL,
    created_at                      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_payment_entitlement_corrections_status_created (status, created_at),
    KEY idx_payment_entitlement_corrections_refund (payment_refund_id),
    KEY idx_payment_entitlement_corrections_user_created (user_id, created_at),
    CONSTRAINT fk_payment_entitlement_corrections_refund
        FOREIGN KEY (payment_refund_id) REFERENCES payment_refunds (id),
    CONSTRAINT fk_payment_entitlement_corrections_subscription_payment
        FOREIGN KEY (subscription_payment_id) REFERENCES subscription_payments (id),
    CONSTRAINT fk_payment_entitlement_corrections_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_entitlement_corrections_user_subscription
        FOREIGN KEY (user_subscription_id) REFERENCES user_subscriptions (id),
    CONSTRAINT fk_payment_entitlement_corrections_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_entitlement_corrections_before_subscription
        FOREIGN KEY (before_subscription_id) REFERENCES subscriptions (id),
    CONSTRAINT fk_payment_entitlement_corrections_before_pending_subscription
        FOREIGN KEY (before_pending_subscription_id) REFERENCES subscriptions (id),
    CONSTRAINT fk_payment_entitlement_corrections_target_subscription
        FOREIGN KEY (target_subscription_id) REFERENCES subscriptions (id),
    CONSTRAINT fk_payment_entitlement_corrections_requested_by
        FOREIGN KEY (requested_by) REFERENCES users (id),
    CONSTRAINT fk_payment_entitlement_corrections_approved_by
        FOREIGN KEY (approved_by) REFERENCES users (id),
    CONSTRAINT fk_payment_entitlement_corrections_executed_by
        FOREIGN KEY (executed_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_reconciliation_incidents
(
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    dedupe_key              VARCHAR(255) NOT NULL,
    issue_type              ENUM (
        'DONE_ORDER_WITHOUT_PAYMENT',
        'ACTIVE_AGREEMENT_WITHOUT_SUBSCRIPTION',
        'PROVIDER_DONE_LOCAL_NOT_FINALIZED',
        'LOCAL_DONE_PROVIDER_NOT_FOUND',
        'LOCAL_DONE_PROVIDER_NOT_DONE',
        'AMOUNT_MISMATCH',
        'PROVIDER_LOOKUP_FAILED'
    ) NOT NULL,
    status                  ENUM ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'IGNORED') NOT NULL DEFAULT 'OPEN',
    severity                ENUM ('WARNING', 'CRITICAL') NOT NULL DEFAULT 'WARNING',
    payment_order_id        BIGINT NULL,
    billing_agreement_id    BIGINT NULL,
    user_id                 BIGINT NULL,
    order_id                VARCHAR(64) NULL,
    provider                ENUM ('TOSS') NULL,
    purpose                 ENUM ('SUBSCRIBE', 'UPGRADE', 'RENEWAL', 'BILLING_AGREEMENT') NULL,
    local_status            VARCHAR(50) NULL,
    provider_status         VARCHAR(50) NULL,
    local_amount            DECIMAL(10, 2) NULL,
    provider_amount         DECIMAL(10, 2) NULL,
    provider_transaction_id VARCHAR(200) NULL,
    failure_code            VARCHAR(100) NULL,
    failure_message         VARCHAR(500) NULL,
    occurrence_count        INT NOT NULL DEFAULT 1,
    first_detected_at       DATETIME NOT NULL,
    last_detected_at        DATETIME NOT NULL,
    notified_at             DATETIME NULL,
    resolved_at             DATETIME NULL,
    resolution_note         VARCHAR(500) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_reconciliation_incidents_dedupe (dedupe_key),
    KEY idx_payment_reconciliation_incidents_status_last (status, last_detected_at),
    KEY idx_payment_reconciliation_incidents_order (order_id),
    CONSTRAINT fk_payment_reconciliation_incidents_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_reconciliation_incidents_agreement
        FOREIGN KEY (billing_agreement_id) REFERENCES billing_agreements (id),
    CONSTRAINT fk_payment_reconciliation_incidents_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_receipts
(
    id                      BIGINT NOT NULL AUTO_INCREMENT,
    payment_order_id        BIGINT NOT NULL,
    subscription_payment_id BIGINT NOT NULL,
    user_id                 BIGINT NOT NULL,
    provider                ENUM ('TOSS') NOT NULL,
    type                    ENUM ('PAYMENT_RECEIPT', 'CASH_RECEIPT') NOT NULL,
    status                  ENUM ('ISSUED', 'CANCELLED', 'PARTIAL_CANCELLED', 'FAILED') NOT NULL DEFAULT 'ISSUED',
    provider_payment_key    VARCHAR(200) NULL,
    receipt_key             VARCHAR(200) NULL,
    receipt_url             VARCHAR(1000) NULL,
    issued_at               DATETIME NULL,
    cancelled_at            DATETIME NULL,
    evidence_payload        TEXT NULL COMMENT 'Sanitized receipt evidence only. No billing/auth/customer keys or raw card data.',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_receipts_order_type (payment_order_id, type),
    KEY idx_payment_receipts_user_created (user_id, created_at),
    KEY idx_payment_receipts_provider_payment_key (provider_payment_key),
    CONSTRAINT fk_payment_receipts_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_receipts_subscription_payment
        FOREIGN KEY (subscription_payment_id) REFERENCES subscription_payments (id),
    CONSTRAINT fk_payment_receipts_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE payment_operation_audit_logs
(
    id                         BIGINT NOT NULL AUTO_INCREMENT,
    action                     ENUM (
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
    target_type                ENUM ('RECONCILIATION_INCIDENT', 'PAYMENT_RECEIPT', 'PAYMENT_REFUND', 'PAYMENT_ENTITLEMENT_CORRECTION', 'PAYMENT_SETTLEMENT') NOT NULL,
    target_id                  BIGINT NULL,
    actor_user_id              BIGINT NULL COMMENT 'Admin actor. NULL for system-generated audit entries.',
    target_user_id             BIGINT NULL COMMENT 'Payment owner when resolvable.',
    payment_order_id           BIGINT NULL,
    subscription_payment_id    BIGINT NULL,
    reconciliation_incident_id BIGINT NULL,
    provider                   ENUM ('TOSS') NULL,
    order_id                   VARCHAR(64) NULL,
    provider_transaction_id    VARCHAR(200) NULL,
    before_status              VARCHAR(60) NULL,
    after_status               VARCHAR(60) NULL,
    reason_code                VARCHAR(100) NULL,
    note                       VARCHAR(500) NULL,
    created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_payment_operation_audit_logs_target (target_type, target_id),
    KEY idx_payment_operation_audit_logs_order (order_id),
    KEY idx_payment_operation_audit_logs_actor_created (actor_user_id, created_at),
    CONSTRAINT fk_payment_operation_audit_logs_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_operation_audit_logs_target_user
        FOREIGN KEY (target_user_id) REFERENCES users (id),
    CONSTRAINT fk_payment_operation_audit_logs_order
        FOREIGN KEY (payment_order_id) REFERENCES payment_orders (id),
    CONSTRAINT fk_payment_operation_audit_logs_subscription_payment
        FOREIGN KEY (subscription_payment_id) REFERENCES subscription_payments (id),
    CONSTRAINT fk_payment_operation_audit_logs_incident
        FOREIGN KEY (reconciliation_incident_id) REFERENCES payment_reconciliation_incidents (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.9  answers  (→ questions, users)
-- ─────────────────────────────────────────────
CREATE TABLE answers
(
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    question_id BIGINT   NOT NULL,
    user_id     BIGINT   NOT NULL COMMENT 'Inquiry author or admin. Role distinguished by users.role.',
    content     TEXT     NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT fk_answers_user     FOREIGN KEY (user_id)     REFERENCES users     (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.10  question_attachments  (→ questions)
-- ─────────────────────────────────────────────
CREATE TABLE question_attachments
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    question_id   BIGINT       NOT NULL,
    original_name VARCHAR(255) NOT NULL COMMENT 'Original filename at upload.',
    file_path     VARCHAR(500) NOT NULL COMMENT 'Server storage path.',
    file_size     BIGINT       NOT NULL COMMENT 'In bytes.',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_attachments_question FOREIGN KEY (question_id) REFERENCES questions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────
-- 3.11  notice_attachments  (→ notices)
-- ─────────────────────────────────────────────
CREATE TABLE notice_attachments
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    notice_id     BIGINT       NOT NULL,
    original_name VARCHAR(255) NOT NULL COMMENT 'Original filename at upload.',
    file_path     VARCHAR(500) NOT NULL COMMENT 'Server storage path.',
    file_size     BIGINT       NOT NULL COMMENT 'In bytes.',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notice_attachments_notice FOREIGN KEY (notice_id) REFERENCES notices (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────
-- 3.12  albums  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE albums
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    thumbnail   VARCHAR(500) NULL     COMMENT 'Album cover image URL.',
    created_by  BIGINT       NOT NULL COMMENT 'ADMIN user who created this album.',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Soft delete flag.',
    like_count  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_albums_user FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.12  album_tracks  (→ albums, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE album_tracks
(
    album_id    BIGINT NOT NULL,
    track_id    BIGINT NOT NULL,
    track_order INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (album_id, track_id),
    CONSTRAINT fk_album_tracks_album FOREIGN KEY (album_id) REFERENCES albums (id),
    CONSTRAINT fk_album_tracks_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────
-- 3.12b  album_likes  (→ users, albums)
-- ─────────────────────────────────────────────
CREATE TABLE album_likes
(
    user_id    BIGINT   NOT NULL,
    album_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, album_id),
    CONSTRAINT fk_album_likes_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_album_likes_album FOREIGN KEY (album_id) REFERENCES albums (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.13  email_verification_tokens  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE email_verification_tokens
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at DATETIME     NOT NULL COMMENT 'Registration time + 24 hours.',
    used       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Single-use token.',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_email_verification_tokens_token (token),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.14  password_reset_tokens  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE password_reset_tokens
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at DATETIME     NOT NULL COMMENT 'Request time + 1 hour.',
    used       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'Single-use token.',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_password_reset_tokens_token (token),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ─────────────────────────────────────────────
-- 1.x  site_settings
-- ─────────────────────────────────────────────
CREATE TABLE site_settings
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value TEXT         NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_site_settings_key (setting_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Durable file/DB lifecycle journal. This table contains opaque generated keys only.
CREATE TABLE storage_mutations
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


-- =============================================================================
-- END OF SCHEMA
-- Total: 39 tables
-- =============================================================================
