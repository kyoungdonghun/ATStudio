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
-- NOTE: Admin password must be set via the application's register API
-- because BCrypt hashes generated outside Spring may not match.
-- Steps: 1) Start the app  2) POST /api/users with desired password
--        3) UPDATE users SET role='ADMIN', is_verified=1 WHERE email='admin@atstudio.com';
INSERT IGNORE INTO users (nickname, email, password, is_verified, role, user_type)
VALUES ('admin', 'admin@atstudio.com', '$2a$10$placeholder_register_via_api', 1, 'ADMIN', 'INDIVIDUAL');

-- ─────────────────────────────────────────────
-- 4. Sample Tracks (10 tracks, admin user_id=1)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO tracks (id, title, thumbnail, bpm, tonality, description, audio_file, preview_file, duration, user_id, is_active, play_count)
VALUES
    (1, 'Sunny Piano',       NULL, 120, 'C',  '밝고 경쾌한 피아노 멜로디',           'uploads/tracks/audio/track01_piano_c.wav',      NULL,  30, 1, 1, 152),
    (2, 'Synth Wave',        NULL, 128, 'E',  '신스 사운드의 몽환적 분위기',           'uploads/tracks/audio/track02_synth_high.wav',   NULL,  45, 1, 1, 87),
    (3, 'Deep Bass',         NULL,  90, 'Am', '깊은 베이스 라인의 힙합 비트',          'uploads/tracks/audio/track03_bass_low.wav',     NULL,  60, 1, 1, 234),
    (4, 'Ambient Garden',    NULL,  85, 'G',  '자연 속 잔잔한 앰비언트',              'uploads/tracks/audio/track04_ambient_g.wav',    NULL,  90, 1, 1, 56),
    (5, 'Lofi Study',        NULL,  75, 'C',  '집중할 때 듣기 좋은 로파이 비트',       'uploads/tracks/audio/track05_lofi_mid.wav',     NULL, 120, 1, 1, 412),
    (6, 'EDM Drop',          NULL, 140, 'D',  '강렬한 EDM 드롭',                    'uploads/tracks/audio/track06_edm_d.wav',       NULL,  30, 1, 1, 198),
    (7, 'Acoustic Morning',  NULL, 100, 'F',  '아침에 어울리는 어쿠스틱 기타',         'uploads/tracks/audio/track07_acoustic_f.wav',   NULL,  45, 1, 1, 321),
    (8, 'Cinematic Epic',    NULL, 110, 'Bb', '영화 같은 시네마틱 사운드트랙',          'uploads/tracks/audio/track08_cinematic_bb.wav', NULL, 180, 1, 1, 145),
    (9, 'HipHop Groove',     NULL,  95, 'D',  '힙합 그루브 비트',                    'uploads/tracks/audio/track09_hiphop_d.wav',    NULL,  60, 1, 1, 267),
   (10, 'R&B Smooth',        NULL, 105, 'Ab', '부드러운 R&B 멜로디',                 'uploads/tracks/audio/track10_rnb_ab.wav',      NULL, 150, 1, 1, 89);

-- ─────────────────────────────────────────────
-- 5. Track-Tag mappings
-- ─────────────────────────────────────────────
-- Tag IDs reference: 1-8 MOOD, 9-17 GENRE, 18-23 INSTRUMENT
INSERT IGNORE INTO track_tags (track_id, tag_id) VALUES
    -- Track 1: Sunny Piano → 밝은, 팝, 피아노
    (1, 1), (1, 9), (1, 18),
    -- Track 2: Synth Wave → 몽환적, EDM, 신스
    (2, 8), (2, 11), (2, 21),
    -- Track 3: Deep Bass → 어두운, 힙합, 베이스
    (3, 2), (3, 10), (3, 23),
    -- Track 4: Ambient Garden → 잔잔한, 로파이, 피아노
    (4, 4), (4, 16), (4, 18),
    -- Track 5: Lofi Study → 잔잔한, 로파이, 피아노
    (5, 4), (5, 16), (5, 18),
    -- Track 6: EDM Drop → 신나는, EDM, 신스, 드럼
    (6, 3), (6, 11), (6, 21), (6, 20),
    -- Track 7: Acoustic Morning → 밝은, 어쿠스틱, 기타
    (7, 1), (7, 17), (7, 19),
    -- Track 8: Cinematic Epic → 긴장감, 클래식, 바이올린
    (8, 5), (8, 15), (8, 22),
    -- Track 9: HipHop Groove → 신나는, 힙합, 드럼, 베이스
    (9, 3), (9, 10), (9, 20), (9, 23),
    -- Track 10: R&B Smooth → 로맨틱, R&B, 피아노
   (10, 6), (10, 12), (10, 18);

-- ─────────────────────────────────────────────
-- 6. Sample Albums (3 albums)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO albums (id, title, description, thumbnail, created_by, is_active)
VALUES
    (1, 'Chill Vibes',       '잔잔하고 편안한 음악 모음',     NULL, 1, 1),
    (2, 'Energy Boost',      '에너지 넘치는 비트 모음',      NULL, 1, 1),
    (3, 'Cinematic Collection', '영화 같은 사운드트랙 모음', NULL, 1, 1);

INSERT IGNORE INTO album_tracks (album_id, track_id, track_order) VALUES
    -- Chill Vibes: Lofi Study, Ambient Garden, Acoustic Morning
    (1, 5, 1), (1, 4, 2), (1, 7, 3),
    -- Energy Boost: EDM Drop, HipHop Groove, Deep Bass
    (2, 6, 1), (2, 9, 2), (2, 3, 3),
    -- Cinematic Collection: Cinematic Epic, Synth Wave, R&B Smooth
    (3, 8, 1), (3, 2, 2), (3, 10, 3);

-- ─────────────────────────────────────────────
-- 7. Sample Notices (3 notices)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO notices (id, user_id, title, content, is_pinned)
VALUES
    (1, 1, 'ATStudio 오픈 안내', 'ATStudio가 정식 오픈되었습니다! 다양한 쇼츠용 음악을 만나보세요.', 1),
    (2, 1, '새로운 음원 업로드 안내', '이번 주 새로운 음원 10곡이 추가되었습니다. 지금 바로 확인해보세요!', 0),
    (3, 1, '구독 플랜 업데이트', '기업용 구독 플랜이 새롭게 개편되었습니다. 자세한 내용은 구독 페이지를 확인해주세요.', 0);

-- =============================================================================
-- END OF SEED DATA
-- =============================================================================
