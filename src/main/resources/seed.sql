-- =============================================================================
-- ATStudio Seed Data
-- =============================================================================
-- Usage:
--   mysql -u root -p atstudio < src/main/resources/seed.sql
--
-- NOTE: Run AFTER schema.sql. Uses INSERT IGNORE to skip duplicates.
-- =============================================================================

-- ─────────────────────────────────────────────
-- 1. Subscription Plans (5 plans)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO subscriptions (name, description, user_type, price_monthly, price_yearly, download_per_day, max_whitelist_channels, is_active)
VALUES
    ('STANDARD', '개인 기본 플랜', 'INDIVIDUAL', 9900.00, 99000.00, 5, 1, 1),
    ('DELUXE',   '개인 디럭스 플랜', 'INDIVIDUAL', 19900.00, 199000.00, 20, 2, 1),
    ('PREMIUM',  '개인 프리미엄 플랜', 'INDIVIDUAL', 29900.00, 299000.00, -1, 2, 1),
    ('DELUXE',   '기업 디럭스 플랜', 'BUSINESS', 49900.00, 499000.00, 50, 2, 1),
    ('PREMIUM',  '기업 프리미엄 플랜', 'BUSINESS', 99900.00, 999000.00, -1, 2, 1);

-- ─────────────────────────────────────────────
-- 2. Default Tags (sample)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO tags (name, type) VALUES
    -- MOOD
    ('밝은', 'MOOD'),
    ('어두운', 'MOOD'),
    ('신나는', 'MOOD'),
    ('잔잔한', 'MOOD'),
    ('긴장감', 'MOOD'),
    ('로맨틱', 'MOOD'),
    ('슬픈', 'MOOD'),
    ('몽환적', 'MOOD'),
    -- GENRE
    ('팝', 'GENRE'),
    ('힙합', 'GENRE'),
    ('EDM', 'GENRE'),
    ('R&B', 'GENRE'),
    ('록', 'GENRE'),
    ('재즈', 'GENRE'),
    ('클래식', 'GENRE'),
    ('로파이', 'GENRE'),
    ('어쿠스틱', 'GENRE'),
    -- INSTRUMENT
    ('피아노', 'INSTRUMENT'),
    ('기타', 'INSTRUMENT'),
    ('드럼', 'INSTRUMENT'),
    ('신스', 'INSTRUMENT'),
    ('바이올린', 'INSTRUMENT'),
    ('베이스', 'INSTRUMENT');

-- ─────────────────────────────────────────────
-- 3. Admin User (development only)
-- ─────────────────────────────────────────────
-- password: admin123 (BCrypt hash)
INSERT IGNORE INTO users (nickname, email, password, is_verified, role, user_type)
VALUES ('admin', 'admin@atstudio.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 'ADMIN', 'INDIVIDUAL');

-- =============================================================================
-- END OF SEED DATA
-- =============================================================================
