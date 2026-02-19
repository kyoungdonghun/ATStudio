# ATStudio DB Schema Definition v4 (Confirmed)

> **Status**: 4차 확정본 — 상호 검토 반영
> **Base**: v3 + 상호 검토 8항목 확정
> **Date**: 2026-02-20

---

## v3 → v4 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | `users.password` | **NULL 허용** — 소셜 로그인 전용 계정은 NULL. 2단계 가입 지원. |
| 2 | `users.phone_personal` | **NULL 허용** — 소셜 로그인 가입 시 NULL. 프로필 완성 단계에서 입력. |
| 3 | `users.job` | **NULL 허용 + DEFAULT NULL** — 소셜 로그인 가입 시 NULL. 프로필 완성 단계에서 입력. |
| 4 | `whitelist_channels.is_active` | **컬럼 제거** — 삭제는 물리 삭제로 확정. is_active 사용 시나리오 없음. |
| 5 | `licenses.issued_at` | **컬럼 제거** — created_at과 의미 중복. API 응답에서 created_at → issuedAt 매핑. |
| 6 | `subscription_payments.user_subscription_id` | **FK 추가** — 결제 기록을 특정 구독 세션과 연결. |
| 7 | 플레이리스트 구독 전용 | **구독(ACTIVE) 보유 회원만** 플레이리스트 기능 사용 가능. DB 변경 없음 (앱 레벨 권한 체크). |
| 8 | `users.userType` DEFAULT 명시 | **DEFAULT 'INDIVIDUAL'** 추가 확인 |

---

## v2 → v3 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | `tracks.preview_file` | **추가 확정** — 업로드 후 비동기 저품질 파일 생성. NULL이면 audio_file로 스트리밍 fallback |

---

## v1 → v2 변경 이력

| # | 항목 | 결정 |
|---|------|------|
| 1 | `users.download_remain` | **제거 확정** — COUNT 쿼리 방식으로 대체 |
| 2 | 구독 플랜 구조 | **하나의 레코드에 월/연 가격 포함** |
| 3 | 장바구니 용도 | **"다운로드 대기 목록"으로 재정의** |
| 4 | `tracks.is_active` DEFAULT | **0 (검토 후 공개)** |
| 5 | `tracks.play_count` | **추가 확정** |
| 6 | 소셜 로그인 제공자 | **Google/Kakao/Naver 3개로 확정** |
| 7 | 기업 서류 파일 관리 | **단일 경로 컬럼** + 앱 레벨에서 회원별 폴더 분리 |
| 8 | 일반 사용자 라이센스 | **별도 `licenses` 테이블** 추가 |
| 9 | `track_purchases` | **제거 확정** |
| 10 | 문의 비밀번호 | **컬럼 제거** — 비공개 문의는 작성자 본인+ADMIN만 열람 (앱 레벨 권한) |

---

## 공통 규칙

### Base Columns (모든 테이블 공통)

| 설명 | 컬럼명 | 타입 | NULL | DEFAULT |
|------|--------|------|------|---------|
| 생성일 | `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP |
| 수정일 | `updated_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP |

### 타입 규칙

- PK: `BIGINT AUTO_INCREMENT`
- FK: `BIGINT` (PK와 동일 타입)
- Boolean: `TINYINT(1)` (0/1)
- 금액: `DECIMAL(10,2)`
- 날짜: `DATETIME` (시간 포함) 또는 `DATE` (날짜만)

---

# 1. 사용자와 권한

## 1.1 사용자 (`users`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 닉네임 | `nickname` | VARCHAR(20) | NOT NULL | UNIQUE | | |
| 이메일 | `email` | VARCHAR(100) | NOT NULL | UNIQUE | | |
| 비밀번호 | `password` | VARCHAR(255) | NULL | | | BCrypt 해시 저장. 소셜 로그인 전용 계정은 NULL. |
| 회사 전화번호 | `phone_company` | VARCHAR(20) | NULL | | | 기업회원용 |
| 개인 전화번호 | `phone_personal` | VARCHAR(20) | NULL | | | 소셜 로그인 가입 시 NULL. 프로필 완성 단계에서 입력. |
| 계정 인증 여부 | `is_verified` | TINYINT(1) | NOT NULL | | 0 | 이메일 or 전화번호 인증 |
| 권한 | `role` | ENUM('USER','ADMIN') | NOT NULL | | 'USER' | |
| 직업 | `job` | ENUM('EDITOR','ARTIST','FREELANCER') | NULL | | NULL | 소셜 로그인 가입 시 NULL. 프로필 완성 단계에서 입력. |
| 회원 구분 | `user_type` | ENUM('INDIVIDUAL','BUSINESS') | NOT NULL | | 'INDIVIDUAL' | 일반회원/기업회원 |
| 논리적 삭제 여부 | `is_deleted` | TINYINT(1) | NOT NULL | | 0 | 탈퇴 처리 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**소셜 로그인 2단계 가입 처리:**
- 소셜 로그인 최초 가입 시: email(OAuth 제공), nickname(자동생성 또는 입력), userType=INDIVIDUAL(default)으로 최소 레코드 생성
- `password`, `phone_personal`, `job` = NULL → 프로필 미완성 상태
- 프론트엔드가 `isProfileComplete` 플래그(=phone_personal IS NOT NULL AND job IS NOT NULL)를 확인 후 프로필 완성 화면으로 이동
- 프로필 완성 후 모든 기능 정상 이용 가능

**일일 다운로드 제한 처리:**
- `download_remain` 컬럼 없음
- `track_downloads` 테이블에서 `WHERE user_id = ? AND DATE(downloaded_at) = CURDATE()` COUNT 쿼리로 계산
- 구독 플랜의 `download_per_day`와 비교하여 제한

## 1.2 소셜 로그인 (`social_accounts`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 소셜 제공자 | `provider` | ENUM('GOOGLE','KAKAO','NAVER') | NOT NULL | | | |
| 소셜 고유 ID | `provider_id` | VARCHAR(255) | NOT NULL | | | 소셜 서비스에서 부여하는 사용자 ID |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- UNIQUE 제약: (`provider`, `provider_id`)
- 한 사용자가 여러 소셜 계정 연결 가능 (1:N)

---

# 2. 구독제

## 2.1 구독 플랜 (`subscriptions`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 플랜 이름 | `name` | VARCHAR(30) | NOT NULL | | | STANDARD/DELUXE/PREMIUM |
| 설명 | `description` | TEXT | NULL | | | |
| 대상 회원 유형 | `user_type` | ENUM('INDIVIDUAL','BUSINESS') | NOT NULL | | | 개인/기업 구분 |
| 월간 가격 | `price_monthly` | DECIMAL(10,2) | NOT NULL | | | |
| 연간 가격 | `price_yearly` | DECIMAL(10,2) | NOT NULL | | | |
| 일일 다운로드 허용 수 | `download_per_day` | INT | NOT NULL | | | -1 = 무제한 |
| 화이트리스트 채널 수 | `max_whitelist_channels` | INT | NOT NULL | | | 등급별 채널 수 제한 |
| 활성 여부 | `is_active` | TINYINT(1) | NOT NULL | | 1 | 사용자가 선택 가능 여부 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- UNIQUE 제약: (`name`, `user_type`) — 같은 이름+유형 조합 중복 방지

### 구독 플랜 초기 데이터

| name | user_type | price_monthly | price_yearly | download_per_day | max_whitelist_channels |
|------|-----------|---------------|--------------|------------------|----------------------|
| STANDARD | INDIVIDUAL | [미정] | [미정] | 5 | 1 |
| DELUXE | INDIVIDUAL | [미정] | [미정] | 20 | 2 |
| PREMIUM | INDIVIDUAL | [미정] | [미정] | -1 | 2 |
| DELUXE | BUSINESS | [미정] | [미정] | 50 | 2 |
| PREMIUM | BUSINESS | [미정] | [미정] | -1 | 2 |

## 2.2 사용자 구독 상태 (`user_subscriptions`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id), UNIQUE | | 1인 1구독 |
| 구독 플랜 | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | |
| 결제 주기 | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| 구독 상태 | `status` | ENUM('ACTIVE','CANCELLED','EXPIRED') | NOT NULL | | 'ACTIVE' | |
| 현재 구독 시작일 | `started_at` | DATE | NOT NULL | | | |
| 현재 구독 만료일 | `expires_at` | DATE | NOT NULL | | | 다음 결제일 = 만료일 + 1일 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**구독 업그레이드 처리:**
- 변경 시 즉시 적용 (바로 사용 가능)
- 결제 금액 = 새 플랜 가격 - 현재 플랜 잔여 기간 비례 금액
- 상세 계산 로직은 애플리케이션 레벨에서 처리

---

# 3. 기업 라이센스 심사

## 3.1 기업 라이센스 신청 (`business_license_requests`)

> 기업 회원(100명 초과)이 구독제 구매 전 서류 제출 → 관리자 검토 → 승인/반려

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 신청자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | 기업회원 |
| 심사 상태 | `status` | ENUM('PENDING','APPROVED','REVISION_REQUESTED','REJECTED') | NOT NULL | | 'PENDING' | 승인 대기/승인/보완요청/반려 |
| 관리자 메모 | `admin_note` | TEXT | NULL | | | 보완 요청 사유 등 |
| 제출 파일 경로 | `document_path` | VARCHAR(500) | NOT NULL | | | 회원별 전용 폴더에 저장 |
| 라이센스 코드 | `license_code` | VARCHAR(50) | NULL | UNIQUE | | 승인 시 UUID 기반 발급 |
| 승인일 | `approved_at` | DATETIME | NULL | | | 승인 완료 시각 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**파일 저장 정책:**
- 앱 레벨에서 회원별 전용 디렉토리 생성 (예: `/uploads/business-docs/{user_id}/`)
- `document_path`에는 해당 디렉토리 경로 저장
- 디렉토리 내 파일은 파일시스템에서 직접 관리

**프로세스:**
1. 기업 회원 → 구독제 선택 → 서류 제출 페이지
2. 파일 업로드 + 라이센스 요청 전송 (status: PENDING)
3. 관리자 검토 → 보완 요청(REVISION_REQUESTED) 또는 승인(APPROVED)
4. 승인 완료 → `license_code` 발급 → 구독제 결제 가능
5. 결제 후 관리자가 기업에게 세금계산서/계약서 등 제공 (오프라인/별도 처리)

---

# 4. 음원과 태그

## 4.1 음원 (`tracks`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 곡 제목 | `title` | VARCHAR(100) | NOT NULL | | | |
| 썸네일 파일명 | `thumbnail` | VARCHAR(255) | NULL | | | |
| BPM | `bpm` | INT | NOT NULL | | | |
| 조성 | `tonality` | VARCHAR(10) | NOT NULL | | | ex) C, Am, F#m |
| 설명 | `description` | TEXT | NULL | | | |
| 오디오 파일 경로 | `audio_file` | VARCHAR(255) | NOT NULL | | | 원본 파일 (다운로드용) |
| 미리듣기 파일 경로 | `preview_file` | VARCHAR(255) | NULL | | | 저품질 변환본 (스트리밍용). NULL이면 audio_file로 fallback |
| 저작권자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | 현재 단일 관리자(아티스트)만 사용 |
| 활성 여부 | `is_active` | TINYINT(1) | NOT NULL | | 0 | 검토 후 공개 (관리자가 활성화) |
| 재생 수 | `play_count` | BIGINT | NOT NULL | | 0 | 인기순 정렬 등에 활용 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 4.2 태그 (`tags`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 태그 이름 | `name` | VARCHAR(50) | NOT NULL | UNIQUE | | |
| 태그 타입 | `type` | ENUM('MOOD','GENRE','INSTRUMENT') | NOT NULL | | | 분위기/장르/악기 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 4.3 음원-태그 매핑 (`track_tags`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| 음원 | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| 태그 | `tag_id` | BIGINT | NOT NULL | PK, FK(tags.id) | | |

- 복합 PK: (`track_id`, `tag_id`)
- 태그 변경 시 `tracks.updated_at` 함께 갱신

---

# 5. 플레이리스트

## 5.1 플레이리스트 (`playlists`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 제목 | `title` | VARCHAR(50) | NOT NULL | | | |
| 설명 | `description` | TEXT | NULL | | | |
| 썸네일 | `thumbnail` | VARCHAR(255) | NULL | | | |
| 생성자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 활성 여부 | `is_active` | TINYINT(1) | NOT NULL | | 1 | |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

## 5.2 플레이리스트-음원 매핑 (`playlist_tracks`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| 플레이리스트 | `playlist_id` | BIGINT | NOT NULL | PK, FK(playlists.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| 재생 순서 | `track_order` | INT | NOT NULL | | | |

- 복합 PK: (`playlist_id`, `track_id`)
- 수정 시 `playlists.updated_at` 함께 갱신

---

# 6. 기록

## 6.1 음원 다운로드 기록 (`track_downloads`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| 다운로드 일시 | `downloaded_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 같은 곡 여러 번 다운로드 가능
- 일일 다운로드 제한: `COUNT(*) WHERE user_id = ? AND DATE(downloaded_at) = CURDATE()`
- INDEX: (`user_id`, `downloaded_at`) — 일일 카운트 쿼리 성능 확보

## 6.2 재생 기록 (`play_histories`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| 재생 일시 | `played_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 같은 곡 반복 재생 시 매번 기록 (히스토리)
- Que bar에서 재생 시 기록 생성
- `tracks.play_count` 증가와 연동 (앱 레벨)

## 6.3 구독 결제 기록 (`subscription_payments`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 구독 레코드 | `user_subscription_id` | BIGINT | NOT NULL | FK(user_subscriptions.id) | | 어떤 구독 세션에 대한 결제인지 연결 |
| 구독 플랜 | `subscription_id` | BIGINT | NOT NULL | FK(subscriptions.id) | | |
| 결제 주기 | `billing_cycle` | ENUM('MONTHLY','YEARLY') | NOT NULL | | | |
| 결제 금액 | `amount` | DECIMAL(10,2) | NOT NULL | | | 업그레이드 시 차등 금액 |
| 결제 상태 | `payment_status` | ENUM('READY','DONE','REFUND') | NOT NULL | | 'READY' | |
| PG 거래 ID | `pg_transaction_id` | VARCHAR(100) | NULL | | | PG사 연동 시 사용 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

---

# 7. 즐겨찾기

## 7.1 즐겨찾기 (`likes`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| 사용자 | `user_id` | BIGINT | NOT NULL | PK, FK(users.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 복합 PK: (`user_id`, `track_id`)

---

# 8. 다운로드 대기 목록

> 구매 개념이 없으므로 "장바구니" → **"다운로드 대기 목록"**으로 재정의.
> 여러 곡을 모아 일괄 다운로드하는 용도.

## 8.1 다운로드 대기 목록 (`download_queue`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| 사용자 | `user_id` | BIGINT | NOT NULL | PK, FK(users.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | PK, FK(tracks.id) | | |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 복합 PK: (`user_id`, `track_id`)
- 테이블명 `cart_items` → `download_queue`로 변경 (용도에 맞는 명칭)

---

# 9. 화이트리스트 채널

## 9.1 화이트리스트 채널 (`whitelist_channels`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 채널 URL | `channel_url` | VARCHAR(255) | NOT NULL | | | 유튜브 채널 URL |
| 채널 이름 | `channel_name` | VARCHAR(100) | NOT NULL | | | 표시용 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 등록 가능 채널 수: `subscriptions.max_whitelist_channels`로 제한
- 앱에서 등록 시 현재 활성 채널 수 체크

---

# 10. 게시판 (문의/답변)

## 10.1 문의 (`questions`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 문의자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 제목 | `title` | VARCHAR(200) | NOT NULL | | | |
| 내용 | `content` | TEXT | NOT NULL | | | |
| 문의 유형 | `category` | ENUM('DOWNLOAD','PAYMENT','COPYRIGHT','PRODUCTION','OTHER') | NOT NULL | | | 다운로드/결제/저작권/음원제작/기타 |
| 공개 여부 | `is_public` | TINYINT(1) | NOT NULL | | 0 | |
| 문의 상태 | `status` | ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') | NOT NULL | | 'OPEN' | |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

**비공개 문의 접근 정책:**
- `password` 컬럼 없음
- `is_public = 0`인 문의: **작성자 본인 + ADMIN만** 열람 가능
- 애플리케이션 레벨에서 권한 체크 (Spring Security)

**상태 흐름:**
- OPEN → IN_PROGRESS (관리자 첫 답변 시) → RESOLVED (해결) → CLOSED (종료)
- OPEN → CLOSED (답변 없이 관리자가 닫는 경우)

## 10.2 문의 답변 (`answers`)

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 문의 | `question_id` | BIGINT | NOT NULL | FK(questions.id) | | |
| 작성자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | 문의자 or 관리자 |
| 내용 | `content` | TEXT | NOT NULL | | | |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 문의자와 관리자가 주고받는 대화형 구조 (1:N)
- `user_id`의 role(USER/ADMIN)로 작성자 구분
- 새 답변 작성 시 `questions.updated_at` 갱신

---

# 11. 라이센스 (일반 사용자용)

## 11.1 음원 사용 라이센스 (`licenses`)

> 다운로드한 음원에 대해 UUID 기반 라이센스 코드 발급

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 사용자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | |
| 음원 | `track_id` | BIGINT | NOT NULL | FK(tracks.id) | | |
| 라이센스 코드 | `license_code` | VARCHAR(50) | NOT NULL | UNIQUE | | UUID 기반 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | API 응답에서 issuedAt으로 매핑 |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 다운로드 시 자동 발급 (같은 곡 재다운로드 시 기존 라이센스 유지, 중복 발급 방지)
- UNIQUE 제약: (`user_id`, `track_id`) — 1인 1곡당 1라이센스
- 저작권 증빙용 식별코드 역할
- 상세 법적 포맷은 추후 가이드 시 구현

---

# 12. 공지사항

## 12.1 공지사항 (`notices`)

> API 명세서 반영으로 추가

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 작성자 | `user_id` | BIGINT | NOT NULL | FK(users.id) | | ADMIN |
| 제목 | `title` | VARCHAR(200) | NOT NULL | | | |
| 내용 | `content` | TEXT | NOT NULL | | | |
| 고정 여부 | `is_pinned` | TINYINT(1) | NOT NULL | | 0 | 상단 고정 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |
| 수정일 | `updated_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

---

# 13. 문의 첨부파일

## 13.1 문의 첨부파일 (`question_attachments`)

> API 명세서 반영으로 추가

| 설명 | 컬럼명 | 타입 | NULL | 제약조건 | DEFAULT | 비고 |
|------|--------|------|------|----------|---------|------|
| ID | `id` | BIGINT | NOT NULL | PK, AUTO_INCREMENT | | |
| 문의 | `question_id` | BIGINT | NOT NULL | FK(questions.id) | | |
| 원본 파일명 | `original_name` | VARCHAR(255) | NOT NULL | | | 업로드 시 원래 파일명 |
| 저장 파일 경로 | `file_path` | VARCHAR(500) | NOT NULL | | | 서버 저장 경로 |
| 파일 크기 | `file_size` | BIGINT | NOT NULL | | | 바이트 단위 |
| 생성일 | `created_at` | DATETIME | NOT NULL | | CURRENT_TIMESTAMP | |

- 하나의 문의에 여러 첨부파일 가능 (1:N)

---

# 테이블 관계도

```
users ─┬─< social_accounts
       ├─< user_subscriptions ──> subscriptions
       ├─< subscription_payments ──> subscriptions
       ├─< business_license_requests
       ├─< track_downloads ──> tracks
       ├─< play_histories ──> tracks
       ├─< likes ──> tracks
       ├─< download_queue ──> tracks
       ├─< whitelist_channels
       ├─< licenses ──> tracks
       ├─< playlists ─< playlist_tracks ──> tracks
       ├─< questions ─┬─< answers
       │              └─< question_attachments
       └─< notices (ADMIN only)

tracks ─< track_tags ──> tags
```

---

# 전체 테이블 목록 (17개)

| # | 테이블명 | 설명 | 유형 |
|---|----------|------|------|
| 1 | `users` | 사용자 | 마스터 |
| 2 | `social_accounts` | 소셜 로그인 | 마스터 |
| 3 | `subscriptions` | 구독 플랜 정의 | 마스터 |
| 4 | `user_subscriptions` | 사용자 구독 상태 | 트랜잭션 |
| 5 | `business_license_requests` | 기업 라이센스 심사 | 트랜잭션 |
| 6 | `tracks` | 음원 | 마스터 |
| 7 | `tags` | 태그 | 마스터 |
| 8 | `track_tags` | 음원-태그 매핑 | 매핑 |
| 9 | `playlists` | 플레이리스트 | 마스터 |
| 10 | `playlist_tracks` | 플레이리스트-음원 매핑 | 매핑 |
| 11 | `track_downloads` | 다운로드 기록 | 로그 |
| 12 | `play_histories` | 재생 기록 | 로그 |
| 13 | `subscription_payments` | 구독 결제 기록 | 트랜잭션 |
| 14 | `likes` | 즐겨찾기 | 매핑 |
| 15 | `download_queue` | 다운로드 대기 목록 | 매핑 |
| 16 | `whitelist_channels` | 화이트리스트 채널 | 마스터 |
| 17 | `questions` | 문의 | 트랜잭션 |
| 18 | `answers` | 문의 답변 | 트랜잭션 |
| 19 | `licenses` | 음원 사용 라이센스 | 트랜잭션 |
| 20 | `notices` | 공지사항 | 마스터 |
| 21 | `question_attachments` | 문의 첨부파일 | 트랜잭션 |

총 **21개 테이블**
