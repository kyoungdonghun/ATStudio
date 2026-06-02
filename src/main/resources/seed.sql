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
INSERT IGNORE INTO subscriptions (name, description, user_type, price_monthly, price_yearly, download_per_day, max_whitelist_channels, max_playlists, is_active)
VALUES
    ('STANDARD', '개인 기본 플랜', 'INDIVIDUAL', 9900.00, 99000.00, 5, 1, 3, 1),
    ('DELUXE',   '개인 디럭스 플랜', 'INDIVIDUAL', 19900.00, 199000.00, 20, 2, 10, 1),
    ('PREMIUM',  '개인 프리미엄 플랜', 'INDIVIDUAL', 29900.00, 299000.00, -1, 2, 10, 1),
    ('STANDARD', '기업 기본 플랜', 'BUSINESS', 19900.00, 199000.00, 10, 1, 3, 1),
    ('DELUXE',   '기업 디럭스 플랜', 'BUSINESS', 49900.00, 499000.00, 50, 2, 10, 1),
    ('PREMIUM',  '기업 프리미엄 플랜', 'BUSINESS', 99900.00, 999000.00, -1, 2, 10, 1);

-- ─────────────────────────────────────────────
-- 2. Default Tags (sample) — explicit IDs for deterministic track_tag references
-- ─────────────────────────────────────────────
DELETE FROM track_tags WHERE track_id <= 90;
DELETE FROM tags WHERE id > 0;
INSERT INTO tags (id, name, type) VALUES
    -- MOOD (1~25)
    (1,  '밝은', 'MOOD'),
    (2,  '어두운', 'MOOD'),
    (3,  '신나는', 'MOOD'),
    (4,  '잔잔한', 'MOOD'),
    (5,  '긴장감', 'MOOD'),
    (6,  '로맨틱', 'MOOD'),
    (7,  '슬픈', 'MOOD'),
    (8,  '몽환적', 'MOOD'),
    (9,  '코믹', 'MOOD'),
    (10, '파워풀', 'MOOD'),
    (11, '청량한', 'MOOD'),
    (12, '감성적', 'MOOD'),
    (36, '우울한', 'MOOD'),
    (37, '평화로운', 'MOOD'),
    (38, '격렬한', 'MOOD'),
    (39, '신비로운', 'MOOD'),
    (40, '따뜻한', 'MOOD'),
    (41, '차가운', 'MOOD'),
    (42, '희망적', 'MOOD'),
    (43, '그리운', 'MOOD'),
    (44, '축제적', 'MOOD'),
    (45, '고요한', 'MOOD'),
    (46, '웅장한', 'MOOD'),
    (47, '달콤한', 'MOOD'),
    (48, '몽글몽글', 'MOOD'),
    -- GENRE (13~25 + 49~68)
    (13, '팝', 'GENRE'),
    (14, '힙합', 'GENRE'),
    (15, 'EDM', 'GENRE'),
    (16, 'R&B', 'GENRE'),
    (17, '록', 'GENRE'),
    (18, '재즈', 'GENRE'),
    (19, '클래식', 'GENRE'),
    (20, '로파이', 'GENRE'),
    (21, '어쿠스틱', 'GENRE'),
    (22, '트랩', 'GENRE'),
    (23, '하우스', 'GENRE'),
    (24, '펑크', 'GENRE'),
    (25, '앰비언트', 'GENRE'),
    (49, '디스코', 'GENRE'),
    (50, '레게', 'GENRE'),
    (51, '보사노바', 'GENRE'),
    (52, '블루스', 'GENRE'),
    (53, '소울', 'GENRE'),
    (54, '테크노', 'GENRE'),
    (55, '드럼앤베이스', 'GENRE'),
    (56, '인디', 'GENRE'),
    (57, '메탈', 'GENRE'),
    (58, '레게톤', 'GENRE'),
    (59, '칩튠', 'GENRE'),
    (60, '뉴에이지', 'GENRE'),
    (61, '월드뮤직', 'GENRE'),
    (62, '사이키델릭', 'GENRE'),
    (63, '가스펠', 'GENRE'),
    (64, '트로피컬', 'GENRE'),
    (65, '시티팝', 'GENRE'),
    (66, '프로그레시브', 'GENRE'),
    (67, '일렉트로닉', 'GENRE'),
    (68, '신스웨이브', 'GENRE'),
    -- INSTRUMENT (26~35)
    (26, '피아노', 'INSTRUMENT'),
    (27, '기타', 'INSTRUMENT'),
    (28, '드럼', 'INSTRUMENT'),
    (29, '신스', 'INSTRUMENT'),
    (30, '바이올린', 'INSTRUMENT'),
    (31, '베이스', 'INSTRUMENT'),
    (32, '플루트', 'INSTRUMENT'),
    (33, '트럼펫', 'INSTRUMENT'),
    (34, '첼로', 'INSTRUMENT'),
    (35, '우쿨렐레', 'INSTRUMENT'),
    -- USAGE (69~74)
    (69, '쇼츠용', 'USAGE'),
    (70, '유튜브용', 'USAGE'),
    (71, '릴스용', 'USAGE'),
    (72, '브이로그용', 'USAGE'),
    (73, '광고용', 'USAGE'),
    (74, '게임용', 'USAGE');

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
INSERT IGNORE INTO tracks (id, title, thumbnail, bpm, tonality, description, audio_file, preview_file, duration, user_id, is_active, play_count, like_count, download_count)
VALUES
    (1, 'Sunny Piano',       NULL, 120, 'C',  '밝고 경쾌한 피아노 멜로디',           'tracks/audio/track01_piano_c.wav',      NULL,  30, 1, 1, 152, 0, 0),
    (2, 'Synth Wave',        NULL, 128, 'E',  '신스 사운드의 몽환적 분위기',           'tracks/audio/track02_synth_high.wav',   NULL,  45, 1, 1, 87, 0, 0),
    (3, 'Deep Bass',         NULL,  90, 'Am', '깊은 베이스 라인의 힙합 비트',          'tracks/audio/track03_bass_low.wav',     NULL,  60, 1, 1, 234, 0, 0),
    (4, 'Ambient Garden',    NULL,  85, 'G',  '자연 속 잔잔한 앰비언트',              'tracks/audio/track04_ambient_g.wav',    NULL,  90, 1, 1, 56, 0, 0),
    (5, 'Lofi Study',        NULL,  75, 'C',  '집중할 때 듣기 좋은 로파이 비트',       'tracks/audio/track05_lofi_mid.wav',     NULL, 120, 1, 1, 412, 0, 0),
    (6, 'EDM Drop',          NULL, 140, 'D',  '강렬한 EDM 드롭',                    'tracks/audio/track06_edm_d.wav',       NULL,  30, 1, 1, 198, 0, 0),
    (7, 'Acoustic Morning',  NULL, 100, 'F',  '아침에 어울리는 어쿠스틱 기타',         'tracks/audio/track07_acoustic_f.wav',   NULL,  45, 1, 1, 321, 0, 0),
    (8, 'Cinematic Epic',    NULL, 110, 'Bb', '영화 같은 시네마틱 사운드트랙',          'tracks/audio/track08_cinematic_bb.wav', NULL, 180, 1, 1, 145, 0, 0),
    (9, 'HipHop Groove',     NULL,  95, 'D',  '힙합 그루브 비트',                    'tracks/audio/track09_hiphop_d.wav',    NULL,  60, 1, 1, 267, 0, 0),
   (10, 'R&B Smooth',        NULL, 105, 'Ab', '부드러운 R&B 멜로디',                 'tracks/audio/track10_rnb_ab.wav',      NULL, 150, 1, 1, 89, 0, 0),
   (11, 'Trap Kingdom',      NULL, 145, 'Cm', '강렬한 트랩 비트',                   'tracks/audio/track01_piano_c.wav',     NULL,  45, 1, 1, 178, 0, 0),
   (12, 'Jazz Café',         NULL,  95, 'Bb', '카페에서 듣기 좋은 재즈',             'tracks/audio/track02_synth_high.wav',  NULL, 120, 1, 1, 203, 0, 0),
   (13, 'Future Bass',       NULL, 150, 'F',  '미래적 느낌의 베이스 뮤직',            'tracks/audio/track03_bass_low.wav',    NULL,  60, 1, 1, 156, 0, 0),
   (14, 'Rainy Day',         NULL,  70, 'Em', '비 오는 날의 감성 피아노',             'tracks/audio/track04_ambient_g.wav',   NULL,  90, 1, 1, 289, 0, 0),
   (15, 'Punk Energy',       NULL, 160, 'A',  '펑크 에너지 넘치는 기타 사운드',        'tracks/audio/track05_lofi_mid.wav',    NULL,  30, 1, 1, 134, 0, 0),
   (16, 'Dreamy Flute',      NULL,  80, 'G',  '꿈결 같은 플루트 선율',               'tracks/audio/track06_edm_d.wav',       NULL,  60, 1, 1, 97, 0, 0),
   (17, 'House Party',       NULL, 126, 'C',  '파티용 하우스 뮤직',                  'tracks/audio/track07_acoustic_f.wav',  NULL,  45, 1, 1, 312, 0, 0),
   (18, 'Cello Serenade',    NULL,  88, 'D',  '첼로의 세레나데',                     'tracks/audio/track08_cinematic_bb.wav', NULL, 120, 1, 1, 167, 0, 0),
   (19, 'Ukulele Summer',    NULL, 110, 'C',  '여름 느낌 우쿨렐레',                  'tracks/audio/track09_hiphop_d.wav',    NULL,  30, 1, 1, 245, 0, 0),
   (20, 'Dark Ambient',      NULL,  65, 'Fm', '어둡고 신비로운 앰비언트',              'tracks/audio/track10_rnb_ab.wav',      NULL, 180, 1, 1, 78, 0, 0),
   (21, 'Trumpet Fanfare',   NULL, 130, 'Bb', '트럼펫 팡파르',                       'tracks/audio/track01_piano_c.wav',     NULL,  30, 1, 1, 112, 0, 0),
   (22, 'Pop Dance',         NULL, 118, 'G',  '댄스 팝 비트',                       'tracks/audio/track02_synth_high.wav',  NULL,  60, 1, 1, 398, 0, 0),
   (23, 'Meditation Bell',   NULL,  60, 'C',  '명상용 벨 사운드',                    'tracks/audio/track03_bass_low.wav',    NULL, 300, 1, 1, 56, 0, 0),
   (24, 'Rock Anthem',       NULL, 135, 'E',  '록 앤썸',                            'tracks/audio/track04_ambient_g.wav',   NULL,  60, 1, 1, 276, 0, 0),
   (25, 'Lofi Rain',         NULL,  72, 'Am', '비 소리가 섞인 로파이',                'tracks/audio/track05_lofi_mid.wav',    NULL, 120, 1, 1, 445, 0, 0),
   (26, 'Funky Groove',      NULL, 108, 'Dm', '펑키한 그루브 베이스',                 'tracks/audio/track06_edm_d.wav',       NULL,  45, 1, 1, 189, 0, 0),
   (27, 'Violin Romance',    NULL,  92, 'F',  '바이올린 로맨스',                     'tracks/audio/track07_acoustic_f.wav',  NULL,  90, 1, 1, 234, 0, 0),
   (28, 'Chiptune Quest',    NULL, 140, 'C',  '레트로 칩튠 게임 사운드',              'tracks/audio/track08_cinematic_bb.wav', NULL,  30, 1, 1, 167, 0, 0),
   (29, 'Blues Highway',     NULL,  82, 'G',  '블루스 하이웨이 기타',                 'tracks/audio/track09_hiphop_d.wav',    NULL, 120, 1, 1, 143, 0, 0),
   (30, 'K-Pop Beat',        NULL, 125, 'D',  'K-Pop 스타일 비트',                  'tracks/audio/track10_rnb_ab.wav',      NULL,  45, 1, 1, 521, 0, 0),
   -- Batch 2: tracks 31-60 (reuse audio files for testing)
   (31, 'Morning Coffee',     NULL,  88, 'C',  '모닝 커피와 함께하는 재즈',           'tracks/audio/track01_piano_c.wav',     NULL,  60, 1, 1, 312, 0, 0),
   (32, 'Neon City',           NULL, 132, 'Em', '네온 도시의 밤거리',                  'tracks/audio/track02_synth_high.wav',  NULL,  45, 1, 1, 189, 0, 0),
   (33, 'Ocean Breeze',        NULL,  78, 'G',  '바다 바람의 여유로움',                 'tracks/audio/track03_bass_low.wav',    NULL,  90, 1, 1, 267, 0, 0),
   (34, 'Retro Disco',         NULL, 115, 'Bb', '레트로 디스코 파티',                  'tracks/audio/track04_ambient_g.wav',   NULL,  45, 1, 1, 198, 0, 0),
   (35, 'Sakura Wind',         NULL,  92, 'D',  '벚꽃이 날리는 봄바람',                'tracks/audio/track05_lofi_mid.wav',    NULL, 120, 1, 1, 423, 0, 0),
   (36, 'Thunder Strike',      NULL, 155, 'Am', '천둥 같은 강렬한 비트',               'tracks/audio/track06_edm_d.wav',      NULL,  30, 1, 1, 156, 0, 0),
   (37, 'Starlight Waltz',     NULL,  76, 'F',  '별빛 아래 왈츠',                     'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 89, 0, 0),
   (38, 'Street Hustle',       NULL, 100, 'Cm', '스트릿 허슬 비트',                   'tracks/audio/track08_cinematic_bb.wav', NULL, 60, 1, 1, 334, 0, 0),
   (39, 'Fireplace',           NULL,  68, 'Ab', '벽난로 앞의 평온한 시간',              'tracks/audio/track09_hiphop_d.wav',   NULL, 120, 1, 1, 145, 0, 0),
   (40, 'Digital Dream',       NULL, 138, 'E',  '디지털 세계의 꿈',                   'tracks/audio/track10_rnb_ab.wav',     NULL,  45, 1, 1, 278, 0, 0),
   (41, 'Bamboo Forest',       NULL,  72, 'G',  '대나무 숲의 고요함',                  'tracks/audio/track01_piano_c.wav',     NULL,  90, 1, 1, 167, 0, 0),
   (42, 'Pixel Adventure',     NULL, 142, 'C',  '픽셀 모험 게임 OST',                'tracks/audio/track02_synth_high.wav',  NULL,  30, 1, 1, 445, 0, 0),
   (43, 'Sunset Boulevard',    NULL,  98, 'D',  '석양의 대로',                        'tracks/audio/track03_bass_low.wav',    NULL,  60, 1, 1, 201, 0, 0),
   (44, 'Midnight Run',        NULL, 128, 'Fm', '자정의 질주',                        'tracks/audio/track04_ambient_g.wav',   NULL,  45, 1, 1, 356, 0, 0),
   (45, 'Cherry Blossom',      NULL,  85, 'Bb', '벚꽃 피는 거리',                     'tracks/audio/track05_lofi_mid.wav',    NULL, 120, 1, 1, 289, 0, 0),
   (46, 'Cosmic Voyage',       NULL, 148, 'A',  '우주 항해의 사운드',                  'tracks/audio/track06_edm_d.wav',      NULL,  60, 1, 1, 178, 0, 0),
   (47, 'Lullaby Moon',        NULL,  62, 'F',  '달빛 아래 자장가',                   'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 134, 0, 0),
   (48, 'Groove Machine',      NULL, 118, 'Dm', '그루브 머신 비트',                   'tracks/audio/track08_cinematic_bb.wav', NULL, 45, 1, 1, 312, 0, 0),
   (49, 'Arctic Wind',         NULL,  70, 'Em', '북극의 차가운 바람',                  'tracks/audio/track09_hiphop_d.wav',   NULL,  90, 1, 1, 98, 0, 0),
   (50, 'Festival Anthem',     NULL, 135, 'G',  '페스티벌 앤썸',                      'tracks/audio/track10_rnb_ab.wav',     NULL,  30, 1, 1, 567, 0, 0),
   (51, 'Rainy Window',        NULL,  74, 'Am', '빗소리가 들리는 창가',                'tracks/audio/track01_piano_c.wav',     NULL, 120, 1, 1, 234, 0, 0),
   (52, 'Cyber Punk',          NULL, 145, 'Cm', '사이버펑크 도시의 소리',               'tracks/audio/track02_synth_high.wav',  NULL,  45, 1, 1, 189, 0, 0),
   (53, 'Garden Party',        NULL, 105, 'C',  '가든 파티의 즐거움',                  'tracks/audio/track03_bass_low.wav',    NULL,  60, 1, 1, 167, 0, 0),
   (54, 'Desert Mirage',       NULL,  90, 'Bb', '사막의 신기루',                      'tracks/audio/track04_ambient_g.wav',   NULL,  90, 1, 1, 123, 0, 0),
   (55, 'Spring Rain',         NULL,  80, 'F',  '봄비 내리는 오후',                   'tracks/audio/track05_lofi_mid.wav',    NULL, 120, 1, 1, 345, 0, 0),
   (56, 'Turbo Boost',         NULL, 160, 'D',  '터보 부스트 레이싱',                  'tracks/audio/track06_edm_d.wav',      NULL,  30, 1, 1, 234, 0, 0),
   (57, 'Candlelight',         NULL,  66, 'G',  '촛불 아래 저녁',                     'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 156, 0, 0),
   (58, 'Soul Train',          NULL, 112, 'Ab', '소울 트레인 펑크',                   'tracks/audio/track08_cinematic_bb.wav', NULL, 60, 1, 1, 278, 0, 0),
   (59, 'Frozen Lake',         NULL,  68, 'Em', '얼어붙은 호수의 고요',                'tracks/audio/track09_hiphop_d.wav',   NULL,  90, 1, 1, 87, 0, 0),
   (60, 'Summer Festival',     NULL, 130, 'C',  '여름 페스티벌',                      'tracks/audio/track10_rnb_ab.wav',     NULL,  45, 1, 1, 478, 0, 0),
   -- Batch 3: tracks 61-90
   (61, 'Velvet Night',        NULL,  95, 'Bb', '벨벳 같은 밤',                      'tracks/audio/track01_piano_c.wav',     NULL, 120, 1, 1, 198, 0, 0),
   (62, 'Electric Storm',      NULL, 152, 'Am', '일렉트릭 폭풍',                     'tracks/audio/track02_synth_high.wav',  NULL,  30, 1, 1, 345, 0, 0),
   (63, 'Misty Mountain',      NULL,  82, 'G',  '안개 낀 산의 아침',                  'tracks/audio/track03_bass_low.wav',    NULL,  90, 1, 1, 167, 0, 0),
   (64, 'Roller Coaster',      NULL, 140, 'D',  '롤러코스터 같은 비트',                'tracks/audio/track04_ambient_g.wav',   NULL,  45, 1, 1, 289, 0, 0),
   (65, 'Autumn Leaves',       NULL,  78, 'F',  '가을 낙엽이 떨어지는 소리',            'tracks/audio/track05_lofi_mid.wav',    NULL, 120, 1, 1, 312, 0, 0),
   (66, 'Gravity Drop',        NULL, 158, 'Cm', '중력 드롭 베이스',                   'tracks/audio/track06_edm_d.wav',      NULL,  30, 1, 1, 234, 0, 0),
   (67, 'Moonlit Sonata',      NULL,  72, 'Ab', '달빛 소나타',                       'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 145, 0, 0),
   (68, 'Underground',         NULL, 108, 'Em', '언더그라운드 씬',                    'tracks/audio/track08_cinematic_bb.wav', NULL, 60, 1, 1, 378, 0, 0),
   (69, 'Snowfall',            NULL,  64, 'C',  '첫눈이 내리는 밤',                   'tracks/audio/track09_hiphop_d.wav',   NULL, 120, 1, 1, 201, 0, 0),
   (70, 'Carnival',            NULL, 125, 'Bb', '카니발의 활기',                      'tracks/audio/track10_rnb_ab.wav',     NULL,  45, 1, 1, 456, 0, 0),
   (71, 'Silk Road',           NULL,  88, 'Dm', '실크로드 여행',                      'tracks/audio/track01_piano_c.wav',     NULL,  90, 1, 1, 178, 0, 0),
   (72, 'Laser Tag',           NULL, 148, 'E',  '레이저 태그 게임',                   'tracks/audio/track02_synth_high.wav',  NULL,  30, 1, 1, 267, 0, 0),
   (73, 'Twilight Zone',       NULL,  76, 'Fm', '황혼의 영역',                       'tracks/audio/track03_bass_low.wav',    NULL, 120, 1, 1, 134, 0, 0),
   (74, 'Speed Racer',         NULL, 165, 'A',  '스피드 레이서',                      'tracks/audio/track04_ambient_g.wav',   NULL,  30, 1, 1, 412, 0, 0),
   (75, 'Petal Dance',         NULL,  84, 'G',  '꽃잎이 춤추는 정원',                  'tracks/audio/track05_lofi_mid.wav',    NULL,  90, 1, 1, 256, 0, 0),
   (76, 'Bass Cannon',         NULL, 155, 'Cm', '베이스 캐논',                       'tracks/audio/track06_edm_d.wav',      NULL,  45, 1, 1, 189, 0, 0),
   (77, 'Gentle Stream',       NULL,  70, 'D',  '잔잔한 시냇물 소리',                  'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 98, 0, 0),
   (78, 'Breakbeat City',      NULL, 120, 'Bb', '브레이크비트 도시',                   'tracks/audio/track08_cinematic_bb.wav', NULL, 60, 1, 1, 345, 0, 0),
   (79, 'Winter Cabin',        NULL,  66, 'F',  '겨울 산장의 밤',                     'tracks/audio/track09_hiphop_d.wav',   NULL, 120, 1, 1, 112, 0, 0),
   (80, 'Rave On',             NULL, 138, 'Am', '레이브 파티',                       'tracks/audio/track10_rnb_ab.wav',     NULL,  30, 1, 1, 534, 0, 0),
   (81, 'Cafe Latte',          NULL,  92, 'C',  '카페 라떼 한 잔',                    'tracks/audio/track01_piano_c.wav',     NULL,  60, 1, 1, 223, 0, 0),
   (82, 'Glitch Hop',          NULL, 115, 'Em', '글리치 홉 비트',                     'tracks/audio/track02_synth_high.wav',  NULL,  45, 1, 1, 167, 0, 0),
   (83, 'Mountain Peak',       NULL,  80, 'Ab', '산 정상의 풍경',                     'tracks/audio/track03_bass_low.wav',    NULL,  90, 1, 1, 289, 0, 0),
   (84, 'Night Drive',         NULL, 110, 'Dm', '밤의 드라이브',                      'tracks/audio/track04_ambient_g.wav',   NULL, 120, 1, 1, 356, 0, 0),
   (85, 'Blossom Garden',      NULL,  86, 'G',  '꽃이 만발한 정원',                   'tracks/audio/track05_lofi_mid.wav',    NULL,  60, 1, 1, 198, 0, 0),
   (86, 'Overdrive',           NULL, 162, 'E',  '오버드라이브 기타',                   'tracks/audio/track06_edm_d.wav',      NULL,  30, 1, 1, 412, 0, 0),
   (87, 'Lakeview',            NULL,  74, 'F',  '호수가 보이는 풍경',                  'tracks/audio/track07_acoustic_f.wav',  NULL, 180, 1, 1, 145, 0, 0),
   (88, 'Dub Step',            NULL, 142, 'Cm', '덥스텝 웨이브',                      'tracks/audio/track08_cinematic_bb.wav', NULL, 45, 1, 1, 267, 0, 0),
   (89, 'Silent Snow',         NULL,  60, 'Bb', '고요한 눈 내리는 밤',                 'tracks/audio/track09_hiphop_d.wav',   NULL, 120, 1, 1, 78, 0, 0),
   (90, 'Bounce Back',         NULL, 128, 'D',  '바운스 백 비트',                     'tracks/audio/track10_rnb_ab.wav',     NULL,  45, 1, 1, 489, 0, 0);

-- ─────────────────────────────────────────────
-- 5. Track-Tag mappings
-- ─────────────────────────────────────────────
-- Tag IDs reference (actual auto-increment order):
--   MOOD:       1=밝은, 2=어두운, 3=신나는, 4=잔잔한, 5=긴장감, 6=로맨틱, 7=슬픈, 8=몽환적, 9=코믹, 10=파워풀, 11=청량한, 12=감성적
--   GENRE:      13=팝, 14=힙합, 15=EDM, 16=R&B, 17=록, 18=재즈, 19=클래식, 20=로파이, 21=어쿠스틱, 22=트랩, 23=하우스, 24=펑크, 25=앰비언트
--   INSTRUMENT: 26=피아노, 27=기타, 28=드럼, 29=신스, 30=바이올린, 31=베이스, 32=플루트, 33=트럼펫, 34=첼로, 35=우쿨렐레
--   USAGE:      69=쇼츠용, 70=유튜브용, 71=릴스용, 72=브이로그용, 73=광고용, 74=게임용
DELETE FROM track_tags WHERE track_id <= 90;
INSERT IGNORE INTO track_tags (track_id, tag_id) VALUES
    -- Track 1: Sunny Piano (7 tags)
    (1, 1), (1, 11), (1, 42), (1, 13), (1, 65), (1, 26), (1, 27),
    -- Track 2: Synth Wave (9 tags)
    (2, 8), (2, 39), (2, 41), (2, 15), (2, 68), (2, 54), (2, 29), (2, 31), (2, 28),
    -- Track 3: Deep Bass (5 tags)
    (3, 2), (3, 38), (3, 14), (3, 22), (3, 31),
    -- Track 4: Ambient Garden (8 tags)
    (4, 4), (4, 37), (4, 45), (4, 20), (4, 25), (4, 60), (4, 26), (4, 32),
    -- Track 5: Lofi Study (4 tags)
    (5, 4), (5, 40), (5, 20), (5, 26),
    -- Track 6: EDM Drop (10 tags)
    (6, 3), (6, 38), (6, 44), (6, 10), (6, 15), (6, 23), (6, 54), (6, 29), (6, 28), (6, 31),
    -- Track 7: Acoustic Morning (3 tags)
    (7, 1), (7, 21), (7, 27),
    -- Track 8: Cinematic Epic (8 tags)
    (8, 5), (8, 46), (8, 2), (8, 19), (8, 25), (8, 30), (8, 34), (8, 28),
    -- Track 9: HipHop Groove (6 tags)
    (9, 3), (9, 10), (9, 14), (9, 22), (9, 28), (9, 31),
    -- Track 10: R&B Smooth (5 tags)
   (10, 6), (10, 40), (10, 16), (10, 53), (10, 26),
    -- Track 11: Trap Kingdom (7 tags)
   (11, 10), (11, 38), (11, 2), (11, 22), (11, 14), (11, 28), (11, 31),
    -- Track 12: Jazz Café (6 tags)
   (12, 4), (12, 37), (12, 12), (12, 18), (12, 51), (12, 26),
    -- Track 13: Future Bass (9 tags)
   (13, 3), (13, 44), (13, 11), (13, 15), (13, 23), (13, 67), (13, 29), (13, 31), (13, 28),
    -- Track 14: Rainy Day (4 tags)
   (14, 7), (14, 43), (14, 13), (14, 26),
    -- Track 15: Punk Energy (8 tags)
   (15, 10), (15, 38), (15, 3), (15, 24), (15, 17), (15, 57), (15, 27), (15, 28),
    -- Track 16: Dreamy Flute (5 tags)
   (16, 8), (16, 39), (16, 25), (16, 60), (16, 32),
    -- Track 17: House Party (7 tags)
   (17, 3), (17, 44), (17, 11), (17, 23), (17, 49), (17, 29), (17, 28),
    -- Track 18: Cello Serenade (4 tags)
   (18, 6), (18, 47), (18, 19), (18, 34),
    -- Track 19: Ukulele Summer (6 tags)
   (19, 1), (19, 11), (19, 42), (19, 21), (19, 64), (19, 35),
    -- Track 20: Dark Ambient (8 tags)
   (20, 2), (20, 41), (20, 39), (20, 5), (20, 25), (20, 62), (20, 29), (20, 34),
    -- Track 21: Trumpet Fanfare (3 tags)
   (21, 1), (21, 19), (21, 33),
    -- Track 22: Pop Dance (7 tags)
   (22, 3), (22, 44), (22, 11), (22, 13), (22, 49), (22, 29), (22, 28),
    -- Track 23: Meditation Bell (6 tags)
   (23, 4), (23, 45), (23, 37), (23, 25), (23, 60), (23, 26),
    -- Track 24: Rock Anthem (10 tags)
   (24, 10), (24, 38), (24, 46), (24, 3), (24, 17), (24, 24), (24, 57), (24, 27), (24, 28), (24, 31),
    -- Track 25: Lofi Rain (5 tags)
   (25, 4), (25, 7), (25, 43), (25, 20), (25, 26),
    -- Track 26: Funky Groove (8 tags)
   (26, 3), (26, 9), (26, 11), (26, 16), (26, 49), (26, 53), (26, 31), (26, 27),
    -- Track 27: Violin Romance (5 tags)
   (27, 6), (27, 47), (27, 12), (27, 19), (27, 30),
    -- Track 28: Chiptune Quest (7 tags)
   (28, 9), (28, 3), (28, 44), (28, 15), (28, 59), (28, 67), (28, 29),
    -- Track 29: Blues Highway (6 tags)
   (29, 12), (29, 43), (29, 18), (29, 52), (29, 27), (29, 26),
    -- Track 30: K-Pop Beat (9 tags)
   (30, 3), (30, 11), (30, 44), (30, 42), (30, 13), (30, 15), (30, 65), (30, 29), (30, 28),
    -- Track 31: Morning Coffee (4 tags)
   (31, 4), (31, 37), (31, 18), (31, 26),
    -- Track 32: Neon City (8 tags)
   (32, 2), (32, 5), (32, 41), (32, 15), (32, 54), (32, 68), (32, 29), (32, 28),
    -- Track 33: Ocean Breeze (5 tags)
   (33, 4), (33, 37), (33, 11), (33, 20), (33, 27),
    -- Track 34: Retro Disco (7 tags)
   (34, 3), (34, 9), (34, 44), (34, 13), (34, 49), (34, 53), (34, 28),
    -- Track 35: Sakura Wind (3 tags)
   (35, 1), (35, 20), (35, 26),
    -- Track 36: Thunder Strike (9 tags)
   (36, 10), (36, 38), (36, 46), (36, 5), (36, 15), (36, 55), (36, 57), (36, 28), (36, 29),
    -- Track 37: Starlight Waltz (6 tags)
   (37, 6), (37, 47), (37, 39), (37, 19), (37, 30), (37, 26),
    -- Track 38: Street Hustle (5 tags)
   (38, 3), (38, 10), (38, 14), (38, 22), (38, 31),
    -- Track 39: Fireplace (7 tags)
   (39, 4), (39, 40), (39, 45), (39, 12), (39, 25), (39, 21), (39, 26),
    -- Track 40: Digital Dream (10 tags)
   (40, 8), (40, 39), (40, 48), (40, 15), (40, 54), (40, 62), (40, 68), (40, 29), (40, 28), (40, 31),
    -- Track 41: Bamboo Forest (4 tags)
   (41, 4), (41, 45), (41, 25), (41, 32),
    -- Track 42: Pixel Adventure (8 tags)
   (42, 9), (42, 3), (42, 44), (42, 15), (42, 59), (42, 67), (42, 29), (42, 28),
    -- Track 43: Sunset Boulevard (6 tags)
   (43, 6), (43, 40), (43, 12), (43, 13), (43, 65), (43, 27),
    -- Track 44: Midnight Run (7 tags)
   (44, 2), (44, 5), (44, 38), (44, 22), (44, 55), (44, 28), (44, 31),
    -- Track 45: Cherry Blossom (3 tags)
   (45, 1), (45, 42), (45, 20),
    -- Track 46: Cosmic Voyage (9 tags)
   (46, 8), (46, 39), (46, 46), (46, 41), (46, 15), (46, 25), (46, 62), (46, 29), (46, 32),
    -- Track 47: Lullaby Moon (5 tags)
   (47, 4), (47, 45), (47, 47), (47, 19), (47, 30),
    -- Track 48: Groove Machine (8 tags)
   (48, 3), (48, 10), (48, 44), (48, 16), (48, 53), (48, 49), (48, 31), (48, 28),
    -- Track 49: Arctic Wind (6 tags)
   (49, 2), (49, 41), (49, 45), (49, 25), (49, 60), (49, 29),
    -- Track 50: Festival Anthem (10 tags)
   (50, 3), (50, 44), (50, 10), (50, 11), (50, 15), (50, 23), (50, 54), (50, 64), (50, 29), (50, 28),
    -- Track 51: Rainy Window (4 tags)
   (51, 7), (51, 43), (51, 20), (51, 26),
    -- Track 52: Cyber Punk (7 tags)
   (52, 10), (52, 2), (52, 38), (52, 15), (52, 54), (52, 29), (52, 31),
    -- Track 53: Garden Party (5 tags)
   (53, 1), (53, 42), (53, 11), (53, 13), (53, 27),
    -- Track 54: Desert Mirage (8 tags)
   (54, 2), (54, 39), (54, 8), (54, 46), (54, 25), (54, 61), (54, 62), (54, 29),
    -- Track 55: Spring Rain (3 tags)
   (55, 4), (55, 37), (55, 20),
    -- Track 56: Turbo Boost (9 tags)
   (56, 3), (56, 10), (56, 38), (56, 15), (56, 55), (56, 54), (56, 67), (56, 29), (56, 28),
    -- Track 57: Candlelight (6 tags)
   (57, 6), (57, 40), (57, 47), (57, 12), (57, 21), (57, 27),
    -- Track 58: Soul Train (7 tags)
   (58, 3), (58, 12), (58, 44), (58, 16), (58, 53), (58, 49), (58, 31),
    -- Track 59: Frozen Lake (5 tags)
   (59, 4), (59, 41), (59, 45), (59, 25), (59, 26),
    -- Track 60: Summer Festival (10 tags)
   (60, 3), (60, 11), (60, 44), (60, 42), (60, 13), (60, 64), (60, 49), (60, 65), (60, 29), (60, 35),
    -- Track 61: Velvet Night (6 tags)
   (61, 6), (61, 8), (61, 47), (61, 16), (61, 53), (61, 26),
    -- Track 62: Electric Storm (8 tags)
   (62, 10), (62, 38), (62, 46), (62, 5), (62, 15), (62, 57), (62, 28), (62, 29),
    -- Track 63: Misty Mountain (4 tags)
   (63, 4), (63, 45), (63, 25), (63, 32),
    -- Track 64: Roller Coaster (7 tags)
   (64, 3), (64, 44), (64, 9), (64, 15), (64, 23), (64, 29), (64, 28),
    -- Track 65: Autumn Leaves (5 tags)
   (65, 7), (65, 43), (65, 12), (65, 20), (65, 26),
    -- Track 66: Gravity Drop (9 tags)
   (66, 10), (66, 38), (66, 2), (66, 22), (66, 55), (66, 14), (66, 31), (66, 28), (66, 29),
    -- Track 67: Moonlit Sonata (6 tags)
   (67, 6), (67, 47), (67, 37), (67, 19), (67, 26), (67, 34),
    -- Track 68: Underground (8 tags)
   (68, 2), (68, 5), (68, 38), (68, 14), (68, 22), (68, 55), (68, 28), (68, 31),
    -- Track 69: Snowfall (3 tags)
   (69, 4), (69, 45), (69, 26),
    -- Track 70: Carnival (10 tags)
   (70, 3), (70, 9), (70, 44), (70, 11), (70, 13), (70, 49), (70, 64), (70, 29), (70, 28), (70, 35),
    -- Track 71: Silk Road (7 tags)
   (71, 8), (71, 39), (71, 12), (71, 18), (71, 61), (71, 27), (71, 32),
    -- Track 72: Laser Tag (5 tags)
   (72, 3), (72, 9), (72, 15), (72, 59), (72, 29),
    -- Track 73: Twilight Zone (8 tags)
   (73, 2), (73, 8), (73, 39), (73, 5), (73, 25), (73, 62), (73, 29), (73, 34),
    -- Track 74: Speed Racer (6 tags)
   (74, 10), (74, 38), (74, 17), (74, 57), (74, 28), (74, 27),
    -- Track 75: Petal Dance (4 tags)
   (75, 1), (75, 42), (75, 20), (75, 26),
    -- Track 76: Bass Cannon (9 tags)
   (76, 10), (76, 38), (76, 2), (76, 46), (76, 22), (76, 55), (76, 15), (76, 31), (76, 28),
    -- Track 77: Gentle Stream (5 tags)
   (77, 4), (77, 37), (77, 40), (77, 21), (77, 27),
    -- Track 78: Breakbeat City (7 tags)
   (78, 3), (78, 10), (78, 44), (78, 23), (78, 55), (78, 28), (78, 31),
    -- Track 79: Winter Cabin (6 tags)
   (79, 4), (79, 45), (79, 40), (79, 19), (79, 30), (79, 26),
    -- Track 80: Rave On (10 tags)
   (80, 3), (80, 10), (80, 44), (80, 38), (80, 15), (80, 23), (80, 54), (80, 67), (80, 29), (80, 28),
    -- Track 81: Cafe Latte (4 tags)
   (81, 4), (81, 37), (81, 18), (81, 26),
    -- Track 82: Glitch Hop (8 tags)
   (82, 3), (82, 9), (82, 8), (82, 15), (82, 59), (82, 55), (82, 29), (82, 28),
    -- Track 83: Mountain Peak (5 tags)
   (83, 4), (83, 45), (83, 37), (83, 25), (83, 27),
    -- Track 84: Night Drive (7 tags)
   (84, 8), (84, 2), (84, 12), (84, 20), (84, 68), (84, 65), (84, 29),
    -- Track 85: Blossom Garden (3 tags)
   (85, 1), (85, 42), (85, 21),
    -- Track 86: Overdrive (9 tags)
   (86, 10), (86, 38), (86, 46), (86, 3), (86, 17), (86, 57), (86, 24), (86, 27), (86, 28),
    -- Track 87: Lakeview (6 tags)
   (87, 4), (87, 37), (87, 40), (87, 11), (87, 21), (87, 26),
    -- Track 88: Dub Step (8 tags)
   (88, 10), (88, 38), (88, 46), (88, 15), (88, 55), (88, 54), (88, 31), (88, 28),
    -- Track 89: Silent Snow (4 tags)
   (89, 4), (89, 45), (89, 41), (89, 26),
    -- Track 90: Bounce Back (7 tags)
   (90, 3), (90, 11), (90, 44), (90, 13), (90, 16), (90, 28), (90, 31),
    -- Representative usage tags for search/display guidance
   (1, 69), (1, 70),
   (5, 70), (5, 72),
   (6, 69), (6, 71),
   (9, 69), (9, 70),
   (12, 70), (12, 73),
   (19, 69), (19, 71),
   (22, 69), (22, 70),
   (28, 74),
   (30, 69), (30, 71),
   (42, 74),
   (50, 69), (50, 71),
   (60, 70), (60, 72);

-- ─────────────────────────────────────────────
-- 6. Sample Albums
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
    (8, 'Workout Mix',          '운동할 때 들으면 좋은 비트',          NULL, 1, 1),
    (9, 'Rainy Afternoon',      '비 오는 오후의 감성 음악',           NULL, 1, 1),
   (10, 'Retro Wave',           '80년대 레트로 감성',               NULL, 1, 1),
   (11, 'Asian Vibes',          '동양적 분위기의 음악 모음',          NULL, 1, 1),
   (12, 'Deep Focus',           '깊은 집중을 위한 BGM',             NULL, 1, 1),
   (13, 'Party All Night',      '밤새 파티 비트 모음',               NULL, 1, 1),
   (14, 'Road Trip',            '로드트립 드라이브 뮤직',             NULL, 1, 1),
   (15, 'Emotional Piano',      '감성 피아노 컬렉션',               NULL, 1, 1),
   (16, 'Bass Culture',         '베이스 중심 음악 모음',              NULL, 1, 1),
   (17, 'Morning Routine',      '아침 루틴에 어울리는 음악',          NULL, 1, 1),
   (18, 'Cyber Dreams',         '사이버 꿈의 세계',                  NULL, 1, 1),
   (19, 'Nature Sounds',        '자연의 소리와 음악의 조화',          NULL, 1, 1),
   (20, 'Best of 2026',         '2026년 인기 음원 모음',             NULL, 1, 1);

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
    (8, 15, 1), (8, 24, 2), (8, 30, 3), (8, 6, 4),
    -- Rainy Afternoon
    (9, 14, 1), (9, 51, 2), (9, 55, 3), (9, 45, 4),
    -- Retro Wave
    (10, 34, 1), (10, 40, 2), (10, 52, 3), (10, 32, 4),
    -- Asian Vibes
    (11, 35, 1), (11, 41, 2), (11, 71, 3), (11, 75, 4),
    -- Deep Focus
    (12, 39, 1), (12, 49, 2), (12, 59, 3), (12, 69, 4), (12, 79, 5),
    -- Party All Night
    (13, 50, 1), (13, 60, 2), (13, 70, 3), (13, 80, 4), (13, 36, 5),
    -- Road Trip
    (14, 43, 1), (14, 53, 2), (14, 84, 3), (14, 33, 4),
    -- Emotional Piano
    (15, 31, 1), (15, 37, 2), (15, 47, 3), (15, 67, 4), (15, 87, 5),
    -- Bass Culture
    (16, 38, 1), (16, 48, 2), (16, 58, 3), (16, 68, 4), (16, 76, 5),
    -- Morning Routine
    (17, 81, 1), (17, 85, 2), (17, 57, 3), (17, 77, 4),
    -- Cyber Dreams
    (18, 42, 1), (18, 52, 2), (18, 62, 3), (18, 72, 4), (18, 82, 5),
    -- Nature Sounds
    (19, 33, 1), (19, 41, 2), (19, 63, 3), (19, 75, 4), (19, 83, 5),
    -- Best of 2026
    (20, 50, 1), (20, 30, 2), (20, 22, 3), (20, 5, 4), (20, 25, 5), (20, 80, 6);

-- ─────────────────────────────────────────────
-- 7. Sample Notices (3 notices)
-- ─────────────────────────────────────────────
INSERT IGNORE INTO notices (id, user_id, title, content, is_pinned)
VALUES
    (1, 1, 'ATStudio 오픈 안내', 'ATStudio가 정식 오픈되었습니다! 다양한 쇼츠용 음악을 만나보세요.', 1),
    (2, 1, '새로운 음원 업로드 안내', '이번 주 새로운 음원 10곡이 추가되었습니다. 지금 바로 확인해보세요!', 0),
    (3, 1, '구독 플랜 업데이트', '기업용 구독 플랜이 새롭게 개편되었습니다. 자세한 내용은 구독 페이지를 확인해주세요.', 0),
    (4, 1, '서비스 점검 안내', '3월 15일(토) 02:00~06:00 서비스 점검이 예정되어 있습니다. 이용에 불편을 드려 죄송합니다.', 0),
    (5, 1, '3월 신규 음원 60곡 추가', '3월 업데이트로 60곡의 신규 음원이 추가되었습니다. 다양한 장르와 분위기의 음원을 확인해보세요!', 0),
    (6, 1, '라이선스 정책 안내', '다운로드한 음원의 라이선스는 구독 해지 후에도 영구적으로 유효합니다. 자세한 내용은 FAQ를 확인해주세요.', 0),
    (7, 1, '기업 인증 서비스 오픈', 'Pro 이상 구독자를 위한 기업 인증 서비스가 오픈되었습니다. 사업자등록증을 제출하여 상업 라이선스를 활성화하세요.', 0),
    (8, 1, '앨범 기능 업데이트', '새롭게 개편된 앨범 페이지를 만나보세요. 이미지 뷰와 리스트 뷰를 지원합니다.', 0),
    (9, 1, '모바일 최적화 완료', 'ATStudio가 모바일 환경에 최적화되었습니다. 스마트폰에서도 편리하게 음원을 검색하고 다운로드하세요.', 0),
   (10, 1, '1:1 문의 기능 추가', '궁금한 점이 있으시면 1:1 문의를 이용해주세요. 72시간 이내에 답변 드리겠습니다.', 0);

-- ─────────────────────────────────────────────
-- 8. Fix NULL/zero dates for seed data
-- ─────────────────────────────────────────────
-- BaseEntity (created_at, updated_at) is NOT NULL but SQL INSERT doesn't trigger JPA @CreatedDate.
-- Spread dates for realistic test data.
UPDATE users SET created_at = '2026-01-01 09:00:00', updated_at = '2026-01-01 09:00:00'
    WHERE created_at IS NULL AND id > 0;

UPDATE tracks SET
    created_at = DATE_SUB(NOW(), INTERVAL (91 - id) DAY),
    updated_at = DATE_SUB(NOW(), INTERVAL (91 - id) DAY)
    WHERE created_at IS NULL AND id > 0;

UPDATE albums SET
    created_at = DATE_SUB(NOW(), INTERVAL (21 - id) * 3 DAY),
    updated_at = DATE_SUB(NOW(), INTERVAL (21 - id) * 3 DAY)
    WHERE created_at IS NULL AND id > 0;

UPDATE notices SET
    created_at = DATE_SUB(NOW(), INTERVAL (11 - id) * 5 DAY),
    updated_at = DATE_SUB(NOW(), INTERVAL (11 - id) * 5 DAY)
    WHERE created_at IS NULL AND id > 0;

-- ─────────────────────────────────────────────
-- PATCH: Fix audio_file for tracks 11-30 (map to existing 10 WAV files)
-- Run this if seed data was already loaded with non-existent file names.
-- ─────────────────────────────────────────────
UPDATE tracks SET audio_file = 'tracks/audio/track01_piano_c.wav'      WHERE id IN (11, 21) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track02_synth_high.wav'   WHERE id IN (12, 22) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track03_bass_low.wav'     WHERE id IN (13, 23) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track04_ambient_g.wav'    WHERE id IN (14, 24) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track05_lofi_mid.wav'     WHERE id IN (15, 25) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track06_edm_d.wav'        WHERE id IN (16, 26) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track07_acoustic_f.wav'   WHERE id IN (17, 27) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track08_cinematic_bb.wav' WHERE id IN (18, 28) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track09_hiphop_d.wav'     WHERE id IN (19, 29) AND audio_file NOT LIKE '%track0%';
UPDATE tracks SET audio_file = 'tracks/audio/track10_rnb_ab.wav'       WHERE id IN (20, 30) AND audio_file NOT LIKE '%track0%';

-- ─────────────────────────────────────────────
-- 9. Site Settings
-- ─────────────────────────────────────────────
INSERT IGNORE INTO site_settings (setting_key, setting_value, created_at, updated_at) VALUES
('COMPANY_CERT_GUIDE', '1. 사업자등록증 사본\n2. 법인인감증명서 또는 사용인감계\n3. 대표자 신분증 사본', NOW(), NOW());

-- =============================================================================
-- END OF SEED DATA
-- =============================================================================
