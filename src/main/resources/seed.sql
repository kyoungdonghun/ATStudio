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
    -- MOOD (1~12)
    ('밝은', 'MOOD'),
    ('어두운', 'MOOD'),
    ('신나는', 'MOOD'),
    ('잔잔한', 'MOOD'),
    ('긴장감', 'MOOD'),
    ('로맨틱', 'MOOD'),
    ('슬픈', 'MOOD'),
    ('몽환적', 'MOOD'),
    ('코믹', 'MOOD'),
    ('파워풀', 'MOOD'),
    ('청량한', 'MOOD'),
    ('감성적', 'MOOD'),
    -- GENRE (13~25)
    ('팝', 'GENRE'),
    ('힙합', 'GENRE'),
    ('EDM', 'GENRE'),
    ('R&B', 'GENRE'),
    ('록', 'GENRE'),
    ('재즈', 'GENRE'),
    ('클래식', 'GENRE'),
    ('로파이', 'GENRE'),
    ('어쿠스틱', 'GENRE'),
    ('트랩', 'GENRE'),
    ('하우스', 'GENRE'),
    ('펑크', 'GENRE'),
    ('앰비언트', 'GENRE'),
    -- INSTRUMENT (26~35)
    ('피아노', 'INSTRUMENT'),
    ('기타', 'INSTRUMENT'),
    ('드럼', 'INSTRUMENT'),
    ('신스', 'INSTRUMENT'),
    ('바이올린', 'INSTRUMENT'),
    ('베이스', 'INSTRUMENT'),
    ('플루트', 'INSTRUMENT'),
    ('트럼펫', 'INSTRUMENT'),
    ('첼로', 'INSTRUMENT'),
    ('우쿨렐레', 'INSTRUMENT');

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
   (10, 'R&B Smooth',        NULL, 105, 'Ab', '부드러운 R&B 멜로디',                 'uploads/tracks/audio/track10_rnb_ab.wav',      NULL, 150, 1, 1, 89),
   (11, 'Trap Kingdom',      NULL, 145, 'Cm', '강렬한 트랩 비트',                   'uploads/tracks/audio/track11_trap_cm.wav',     NULL,  45, 1, 1, 178),
   (12, 'Jazz Café',         NULL,  95, 'Bb', '카페에서 듣기 좋은 재즈',             'uploads/tracks/audio/track12_jazz_bb.wav',     NULL, 120, 1, 1, 203),
   (13, 'Future Bass',       NULL, 150, 'F',  '미래적 느낌의 베이스 뮤직',            'uploads/tracks/audio/track13_future_f.wav',    NULL,  60, 1, 1, 156),
   (14, 'Rainy Day',         NULL,  70, 'Em', '비 오는 날의 감성 피아노',             'uploads/tracks/audio/track14_rainy_em.wav',    NULL,  90, 1, 1, 289),
   (15, 'Punk Energy',       NULL, 160, 'A',  '펑크 에너지 넘치는 기타 사운드',        'uploads/tracks/audio/track15_punk_a.wav',      NULL,  30, 1, 1, 134),
   (16, 'Dreamy Flute',      NULL,  80, 'G',  '꿈결 같은 플루트 선율',               'uploads/tracks/audio/track16_flute_g.wav',     NULL,  60, 1, 1, 97),
   (17, 'House Party',       NULL, 126, 'C',  '파티용 하우스 뮤직',                  'uploads/tracks/audio/track17_house_c.wav',     NULL,  45, 1, 1, 312),
   (18, 'Cello Serenade',    NULL,  88, 'D',  '첼로의 세레나데',                     'uploads/tracks/audio/track18_cello_d.wav',     NULL, 120, 1, 1, 167),
   (19, 'Ukulele Summer',    NULL, 110, 'C',  '여름 느낌 우쿨렐레',                  'uploads/tracks/audio/track19_uku_c.wav',       NULL,  30, 1, 1, 245),
   (20, 'Dark Ambient',      NULL,  65, 'Fm', '어둡고 신비로운 앰비언트',              'uploads/tracks/audio/track20_dark_fm.wav',     NULL, 180, 1, 1, 78),
   (21, 'Trumpet Fanfare',   NULL, 130, 'Bb', '트럼펫 팡파르',                       'uploads/tracks/audio/track21_trumpet_bb.wav',  NULL,  30, 1, 1, 112),
   (22, 'Pop Dance',         NULL, 118, 'G',  '댄스 팝 비트',                       'uploads/tracks/audio/track22_pop_g.wav',       NULL,  60, 1, 1, 398),
   (23, 'Meditation Bell',   NULL,  60, 'C',  '명상용 벨 사운드',                    'uploads/tracks/audio/track23_meditation_c.wav',NULL, 300, 1, 1, 56),
   (24, 'Rock Anthem',       NULL, 135, 'E',  '록 앤썸',                            'uploads/tracks/audio/track24_rock_e.wav',      NULL,  60, 1, 1, 276),
   (25, 'Lofi Rain',         NULL,  72, 'Am', '비 소리가 섞인 로파이',                'uploads/tracks/audio/track25_lofi_am.wav',     NULL, 120, 1, 1, 445),
   (26, 'Funky Groove',      NULL, 108, 'Dm', '펑키한 그루브 베이스',                 'uploads/tracks/audio/track26_funky_dm.wav',    NULL,  45, 1, 1, 189),
   (27, 'Violin Romance',    NULL,  92, 'F',  '바이올린 로맨스',                     'uploads/tracks/audio/track27_violin_f.wav',    NULL,  90, 1, 1, 234),
   (28, 'Chiptune Quest',    NULL, 140, 'C',  '레트로 칩튠 게임 사운드',              'uploads/tracks/audio/track28_chiptune_c.wav',  NULL,  30, 1, 1, 167),
   (29, 'Blues Highway',     NULL,  82, 'G',  '블루스 하이웨이 기타',                 'uploads/tracks/audio/track29_blues_g.wav',     NULL, 120, 1, 1, 143),
   (30, 'K-Pop Beat',        NULL, 125, 'D',  'K-Pop 스타일 비트',                  'uploads/tracks/audio/track30_kpop_d.wav',     NULL,  45, 1, 1, 521);

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
   (10, 6), (10, 12), (10, 18),
    -- Track 11: Trap Kingdom → 파워풀, 트랩, 드럼, 베이스
   (11, 25), (11, 28), (11, 20), (11, 23),
    -- Track 12: Jazz Café → 잔잔한, 재즈, 피아노
   (12, 4), (12, 14), (12, 18),
    -- Track 13: Future Bass → 신나는, EDM, 신스, 베이스
   (13, 3), (13, 11), (13, 21), (13, 23),
    -- Track 14: Rainy Day → 슬픈, 감성적, 팝, 피아노
   (14, 7), (14, 27), (14, 9), (14, 18),
    -- Track 15: Punk Energy → 파워풀, 펑크, 기타, 드럼
   (15, 25), (15, 30), (15, 19), (15, 20),
    -- Track 16: Dreamy Flute → 몽환적, 앰비언트, 플루트
   (16, 8), (16, 31), (16, 32),
    -- Track 17: House Party → 신나는, 하우스, 신스, 드럼
   (17, 3), (17, 29), (17, 21), (17, 20),
    -- Track 18: Cello Serenade → 로맨틱, 클래식, 첼로
   (18, 6), (18, 15), (18, 34),
    -- Track 19: Ukulele Summer → 밝은, 청량한, 어쿠스틱, 우쿨렐레
   (19, 1), (19, 26), (19, 17), (19, 35),
    -- Track 20: Dark Ambient → 어두운, 긴장감, 앰비언트, 신스
   (20, 2), (20, 5), (20, 31), (20, 21),
    -- Track 21: Trumpet Fanfare → 밝은, 클래식, 트럼펫
   (21, 1), (21, 15), (21, 33),
    -- Track 22: Pop Dance → 신나는, 팝, 신스
   (22, 3), (22, 9), (22, 21),
    -- Track 23: Meditation Bell → 잔잔한, 앰비언트, 피아노
   (23, 4), (23, 31), (23, 18),
    -- Track 24: Rock Anthem → 파워풀, 록, 기타, 드럼
   (24, 25), (24, 13), (24, 19), (24, 20),
    -- Track 25: Lofi Rain → 잔잔한, 로파이, 피아노
   (25, 4), (25, 16), (25, 18),
    -- Track 26: Funky Groove → 신나는, R&B, 베이스, 기타
   (26, 3), (26, 12), (26, 23), (26, 19),
    -- Track 27: Violin Romance → 로맨틱, 클래식, 바이올린
   (27, 6), (27, 15), (27, 22),
    -- Track 28: Chiptune Quest → 코믹, EDM, 신스
   (28, 24), (28, 11), (28, 21),
    -- Track 29: Blues Highway → 감성적, 재즈, 기타
   (29, 27), (29, 14), (29, 19),
    -- Track 30: K-Pop Beat → 신나는, 팝, 신스, 드럼
   (30, 3), (30, 9), (30, 21), (30, 20);

-- ─────────────────────────────────────────────
-- 6. Sample Albums (3 albums)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO albums (id, title, description, thumbnail, created_by, is_active)
VALUES
    (1, 'Chill Vibes',          '잔잔하고 편안한 음악 모음',           NULL, 1, 1),
    (2, 'Energy Boost',         '에너지 넘치는 비트 모음',            NULL, 1, 1),
    (3, 'Cinematic Collection', '영화 같은 사운드트랙 모음',          NULL, 1, 1),
    (4, 'Summer Drive',         '여름 드라이브에 어울리는 음악',       NULL, 1, 1),
    (5, 'Midnight Jazz',        '늦은 밤 재즈 클럽의 분위기',         NULL, 1, 1),
    (6, 'Gaming Beats',         '게이밍할 때 듣기 좋은 비트 모음',     NULL, 1, 1),
    (7, 'Classical Moments',    '클래식 악기의 아름다운 순간들',       NULL, 1, 1),
    (8, 'Workout Mix',          '운동할 때 들으면 좋은 비트',          NULL, 1, 1);

INSERT IGNORE INTO album_tracks (album_id, track_id, track_order) VALUES
    -- Chill Vibes: Lofi Study, Ambient Garden, Acoustic Morning
    (1, 5, 1), (1, 4, 2), (1, 7, 3),
    -- Energy Boost: EDM Drop, HipHop Groove, Deep Bass
    (2, 6, 1), (2, 9, 2), (2, 3, 3),
    -- Cinematic Collection: Cinematic Epic, Synth Wave, R&B Smooth
    (3, 8, 1), (3, 2, 2), (3, 10, 3),
    -- Summer Drive: Ukulele Summer, Pop Dance, Funky Groove, Lofi Rain
    (4, 19, 1), (4, 22, 2), (4, 26, 3), (4, 25, 4),
    -- Midnight Jazz: Jazz Café, Blues Highway, Cello Serenade
    (5, 12, 1), (5, 29, 2), (5, 18, 3),
    -- Gaming Beats: Chiptune Quest, Trap Kingdom, Future Bass, House Party
    (6, 28, 1), (6, 11, 2), (6, 13, 3), (6, 17, 4),
    -- Classical Moments: Violin Romance, Cello Serenade, Trumpet Fanfare, Dreamy Flute
    (7, 27, 1), (7, 18, 2), (7, 21, 3), (7, 16, 4),
    -- Workout Mix: Punk Energy, Rock Anthem, K-Pop Beat, EDM Drop
    (8, 15, 1), (8, 24, 2), (8, 30, 3), (8, 6, 4);

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
