-- =============================================================================
-- ATStudio Database Schema v4
-- =============================================================================
-- Source  : docs/design/db-schema.md (v4 Confirmed, 2026-02-20)
-- Engine  : InnoDB
-- Charset : utf8mb4 / utf8mb4_unicode_ci
-- DB      : atstudio  (see application.yml)
--
-- NOTE: Base application.yml uses ddl-auto=validate.
--       Local development may override this via application-local.yml.
--       This file is for MANUAL setup / reference only.
--       Spring Boot does NOT auto-execute this for external DBs by default.
--
-- Usage:
--   mysql -u root -p atstudio < src/main/resources/schema.sql
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- SECTION 1. INDEPENDENT TABLES (no FK dependencies)
-- =============================================================================

-- ─────────────────────────────────────────────
-- 1.1  users
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users
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
CREATE TABLE IF NOT EXISTS subscriptions
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
CREATE TABLE IF NOT EXISTS tags
(
    id         BIGINT                                 NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)                            NOT NULL,
    type       ENUM ('MOOD', 'GENRE', 'INSTRUMENT')   NOT NULL,
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
CREATE TABLE IF NOT EXISTS social_accounts
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
CREATE TABLE IF NOT EXISTS user_subscriptions
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
CREATE TABLE IF NOT EXISTS company_certifications
(
    id                 BIGINT                                                          NOT NULL AUTO_INCREMENT,
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
    CONSTRAINT fk_company_certifications_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.4  tracks  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tracks
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(100) NOT NULL,
    thumbnail    VARCHAR(255) NULL,
    bpm          INT          NOT NULL,
    tonality     VARCHAR(10)  NOT NULL COMMENT 'e.g. C, Am, F#m',
    description  TEXT         NULL,
    audio_file   VARCHAR(255) NOT NULL COMMENT 'Original file path (for download).',
    preview_file VARCHAR(255) NULL     COMMENT 'Low-quality converted file (for streaming). Falls back to audio_file if NULL.',
    duration     INT          NOT NULL DEFAULT 0 COMMENT 'Duration in seconds, auto-extracted from audio file.',
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
CREATE TABLE IF NOT EXISTS playlists
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
CREATE TABLE IF NOT EXISTS whitelist_channels
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    channel_url  VARCHAR(255) NOT NULL COMMENT 'YouTube channel URL.',
    channel_name VARCHAR(100) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_whitelist_channels_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 2.7  questions  (→ users)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS questions
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
CREATE TABLE IF NOT EXISTS notices
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
CREATE TABLE IF NOT EXISTS track_tags
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
CREATE TABLE IF NOT EXISTS playlist_tracks
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
CREATE TABLE IF NOT EXISTS track_downloads
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
-- 3.4  play_histories  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS play_histories
(
    id        BIGINT   NOT NULL AUTO_INCREMENT,
    user_id   BIGINT   NOT NULL,
    track_id  BIGINT   NOT NULL,
    played_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_play_histories_user_date (user_id, played_at),
    CONSTRAINT fk_play_histories_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_play_histories_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.5  likes  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS likes
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
-- 3.6  download_queue  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS download_queue
(
    user_id    BIGINT   NOT NULL,
    track_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, track_id),
    CONSTRAINT fk_download_queue_user  FOREIGN KEY (user_id)  REFERENCES users  (id),
    CONSTRAINT fk_download_queue_track FOREIGN KEY (track_id) REFERENCES tracks (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────
-- 3.7  licenses  (→ users, tracks)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS licenses
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
-- 3.8  billing_agreements / payment_orders / subscription_payments / payment_reconciliation_incidents
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS billing_agreements
(
    id                      BIGINT                                                         NOT NULL AUTO_INCREMENT,
    user_id                 BIGINT                                                         NOT NULL,
    provider                ENUM ('TOSS_BILLING', 'KAKAOPAY')                              NOT NULL,
    status                  ENUM ('READY', 'ACTIVE', 'SUSPENDED', 'CANCELLED', 'EXPIRED')   NOT NULL DEFAULT 'READY',
    provider_customer_key   VARCHAR(300)                                                    NOT NULL,
    billing_key_ciphertext  VARCHAR(1000)                                                   NULL,
    billing_key_fingerprint VARCHAR(128)                                                    NULL,
    pay_method              VARCHAR(50)                                                     NULL,
    masked_method           VARCHAR(100)                                                    NULL,
    next_billing_at         DATE                                                            NULL,
    last_charged_at         DATETIME                                                        NULL,
    failure_count           INT                                                             NOT NULL DEFAULT 0,
    cancelled_at            DATETIME                                                        NULL,
    created_at              DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME                                                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_billing_agreements_user_provider (user_id, provider),
    UNIQUE KEY uq_billing_agreements_provider_customer (provider, provider_customer_key),
    KEY idx_billing_agreements_status_next (status, next_billing_at),
    CONSTRAINT fk_billing_agreements_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_orders
(
    id                   BIGINT                                                               NOT NULL AUTO_INCREMENT,
    order_id             VARCHAR(64)                                                          NOT NULL,
    user_id              BIGINT                                                               NOT NULL,
    purpose              ENUM ('SUBSCRIBE', 'UPGRADE', 'RENEWAL', 'BILLING_AGREEMENT')         NOT NULL,
    provider             ENUM ('MOCK', 'TOSS', 'TOSS_BILLING', 'KAKAOPAY')                     NOT NULL,
    status               ENUM ('READY', 'IN_PROGRESS', 'DONE', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'READY',
    subscription_id      BIGINT                                                               NOT NULL,
    user_subscription_id BIGINT                                                               NULL,
    billing_agreement_id BIGINT                                                               NULL,
    billing_cycle        ENUM ('MONTHLY', 'YEARLY')                                           NOT NULL,
    amount               DECIMAL(10, 2)                                                       NOT NULL,
    currency             VARCHAR(3)                                                           NOT NULL DEFAULT 'KRW',
    pg_transaction_id    VARCHAR(200)                                                         NULL,
    provider_payload     TEXT                                                                 NULL,
    failure_code         VARCHAR(100)                                                         NULL,
    failure_message      VARCHAR(500)                                                         NULL,
    expires_at           DATETIME                                                             NOT NULL,
    confirmed_at         DATETIME                                                             NULL,
    created_at           DATETIME                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                                                             NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_orders_order_id (order_id),
    KEY idx_payment_orders_user_status (user_id, status),
    CONSTRAINT fk_payment_orders_user              FOREIGN KEY (user_id)              REFERENCES users              (id),
    CONSTRAINT fk_payment_orders_subscription      FOREIGN KEY (subscription_id)      REFERENCES subscriptions      (id),
    CONSTRAINT fk_payment_orders_user_subscription FOREIGN KEY (user_subscription_id) REFERENCES user_subscriptions (id),
    CONSTRAINT fk_payment_orders_billing_agreement FOREIGN KEY (billing_agreement_id) REFERENCES billing_agreements (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS subscription_payments
(
    id                   BIGINT                          NOT NULL AUTO_INCREMENT,
    user_id              BIGINT                          NOT NULL,
    user_subscription_id BIGINT                          NOT NULL COMMENT 'Links payment to a specific subscription session.',
    subscription_id      BIGINT                          NOT NULL,
    payment_order_id     BIGINT                          NULL,
    billing_agreement_id BIGINT                          NULL,
    billing_cycle        ENUM ('MONTHLY', 'YEARLY')      NOT NULL,
    provider             ENUM ('MOCK', 'TOSS', 'TOSS_BILLING', 'KAKAOPAY') NULL,
    amount               DECIMAL(10, 2)                  NOT NULL COMMENT 'Prorated amount for upgrades.',
    payment_status       ENUM ('READY', 'DONE', 'REFUND') NOT NULL DEFAULT 'READY',
    pg_transaction_id    VARCHAR(100)                    NULL     COMMENT 'PG provider transaction ID.',
    created_at           DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME                        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_subscription_payments_user         FOREIGN KEY (user_id)              REFERENCES users              (id),
    CONSTRAINT fk_subscription_payments_user_sub     FOREIGN KEY (user_subscription_id) REFERENCES user_subscriptions (id),
    CONSTRAINT fk_subscription_payments_subscription FOREIGN KEY (subscription_id)      REFERENCES subscriptions      (id),
    CONSTRAINT fk_subscription_payments_order        FOREIGN KEY (payment_order_id)     REFERENCES payment_orders     (id),
    CONSTRAINT fk_subscription_payments_agreement    FOREIGN KEY (billing_agreement_id) REFERENCES billing_agreements (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_reconciliation_incidents
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
    provider                ENUM ('MOCK', 'TOSS', 'TOSS_BILLING', 'KAKAOPAY') NULL,
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

-- ─────────────────────────────────────────────
-- 3.9  answers  (→ questions, users)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS answers
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
CREATE TABLE IF NOT EXISTS question_attachments
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
CREATE TABLE IF NOT EXISTS notice_attachments
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
CREATE TABLE IF NOT EXISTS albums
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
CREATE TABLE IF NOT EXISTS album_tracks
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
CREATE TABLE IF NOT EXISTS album_likes
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
CREATE TABLE IF NOT EXISTS email_verification_tokens
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
CREATE TABLE IF NOT EXISTS password_reset_tokens
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
CREATE TABLE IF NOT EXISTS site_settings
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


SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- END OF SCHEMA
-- Total: 26 tables
-- =============================================================================
