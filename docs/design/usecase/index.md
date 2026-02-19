# 유스케이스 명세서 인덱스

> **버전**: v4 (Confirmed)
> **확정일**: 2026-02-20
> **기준 문서**: `docs/design/db-schema.md` (v4), `docs/design/api-spec.md` (v4)
> **원본**: `docs/check/유스케이스명세서 csv/`

---

## 파일 목록

| 파일 | 분류 | UC 수 |
|------|------|-------|
| `sound-track.md` | 음원 (생성/조회/수정/삭제/재생/다운로드) | 7 |
| `sound-tag.md` | 태그 (생성/조회/수정/삭제) | 4 |
| `sound-playlist.md` | 플레이리스트 (생성/조회/수정/삭제/음원 추가·제거) | 7 |
| `sound-playhistory.md` | 재생 기록 (저장/조회/삭제) | 3 |
| `user-info.md` | 회원 정보 (회원가입/로그인/소셜 로그인/소셜 프로필 완성/조회/수정/탈퇴) | 10 |
| `user-subscription.md` | 구독 (신청/조회/변경/취소/관리자 관리) | 10 |
| `user-license.md` | 음원 사용 라이센스 (조회) | 4 |
| `user-question.md` | 문의 (생성/조회/답변/삭제/첨부파일/관리자 상태변경) | 7 |
| `user-notice.md` | 공지 (생성/조회/수정/삭제) | 5 |
| `likes.md` | 즐겨찾기 (추가/조회/해제) | 3 |
| `download-queue.md` | 다운로드 대기 목록 (추가/조회/제거) | 3 |
| `whitelist.md` | 화이트리스트 채널 (등록/조회/수정/삭제) | 4 |
| `business-license.md` | 기업 라이센스 심사 (신청/현황 조회/관리자 관리) | 5 |
| `util.md` | 유틸리티 (중복 확인/토큰/구독 상태/다운로드 횟수 등) | 7 |

**총 UC 수: 79개** (v3 대비 순증 2개: +3 추가, -1 제거)

---

## 전체 UC 코드 목록

### Sound

| 코드 | 제목 | 파일 |
|------|------|------|
| SOUND-001 | 음원 생성 | `sound-track.md` |
| SOUND-002 | 플레이리스트 생성 | `sound-playlist.md` |
| SOUND-003 | 태그 생성 | `sound-tag.md` |
| SOUND-004 | 재생 기록 저장 | `sound-playhistory.md` |
| SOUND-005 | 음원 목록 조회 | `sound-track.md` |
| SOUND-006 | 음원 한개 조회 | `sound-track.md` |
| SOUND-007 | 플레이리스트 목록 조회 | `sound-playlist.md` |
| SOUND-008 | 플레이리스트 한개 조회 | `sound-playlist.md` |
| SOUND-009 | 재생 기록 조회 | `sound-playhistory.md` |
| SOUND-010 | 음원 재생 | `sound-track.md` |
| SOUND-011 | 음원 다운로드 | `sound-track.md` |
| SOUND-012 | 음원 수정 | `sound-track.md` |
| SOUND-013 | 플레이리스트 수정 | `sound-playlist.md` |
| SOUND-014 | 태그 수정 | `sound-tag.md` |
| SOUND-015 | 재생 기록 삭제 | `sound-playhistory.md` |
| SOUND-016 | 음원 삭제 | `sound-track.md` |
| SOUND-017 | 플레이리스트 삭제 | `sound-playlist.md` |
| SOUND-018 | 태그 삭제 | `sound-tag.md` |
| SOUND-019 | 플레이리스트 음원 추가 ⭐ | `sound-playlist.md` |
| SOUND-020 | 플레이리스트 음원 제거 ⭐ | `sound-playlist.md` |

### User Info

| 코드 | 제목 | 파일 |
|------|------|------|
| INFO-001 | 회원가입 | `user-info.md` |
| INFO-002 | 내 정보 보기 | `user-info.md` |
| INFO-003 | 회원 목록 조회 | `user-info.md` |
| INFO-004 | 특정 회원 정보 보기 | `user-info.md` |
| INFO-005 | 내 정보 수정 | `user-info.md` |
| INFO-006 | 회원 정보 수정 (관리자) | `user-info.md` |
| INFO-007 | 회원탈퇴 (본인) | `user-info.md` |
| INFO-008 | 로그인 | `user-info.md` |
| INFO-009 | 내 라이센스 목록 보기 | `user-license.md` |
| INFO-010 | 회원의 라이센스 목록 보기 (관리자) | `user-license.md` |
| INFO-011 | 내 라이센스 상세 조회 | `user-license.md` |
| INFO-012 | 회원의 라이센스 상세 조회 (관리자) | `user-license.md` |
| INFO-013 | 소셜 로그인 ⭐ | `user-info.md` |
| INFO-014 | 소셜 회원 프로필 완성 ⭐⭐ | `user-info.md` |

### Payment / Subscription

| 코드 | 제목 | 파일 |
|------|------|------|
| PAYMENT-001 | 구독 신청 | `user-subscription.md` |
| PAYMENT-002 | 구독 플랜 목록 조회 | `user-subscription.md` |
| PAYMENT-003 | 구독 플랜 상세 조회 | `user-subscription.md` |
| PAYMENT-004 | 회원 구독 목록 조회 (관리자) | `user-subscription.md` |
| PAYMENT-005 | 회원 구독 상세 조회 (관리자) | `user-subscription.md` |
| PAYMENT-006 | 내 구독 정보 보기 | `user-subscription.md` |
| PAYMENT-007 | 본인 구독 변경 | `user-subscription.md` |
| PAYMENT-008 | 회원 구독 수정 (관리자) | `user-subscription.md` |
| PAYMENT-009 | 회원 구독 삭제/취소 (관리자) | `user-subscription.md` |
| PAYMENT-010 | 본인 구독 취소 ⭐⭐ | `user-subscription.md` |

### Question / Notice

| 코드 | 제목 | 파일 |
|------|------|------|
| QUESTION-001 | 문의 생성 | `user-question.md` |
| QUESTION-002 | 답변 작성 | `user-question.md` |
| QUESTION-003 | 문의 목록 조회 | `user-question.md` |
| QUESTION-004 | 문의 상세 조회 | `user-question.md` |
| QUESTION-005 | 첨부파일 다운로드 | `user-question.md` |
| QUESTION-006 | 문의 삭제 | `user-question.md` |
| QUESTION-007 | 문의 상태 변경 (관리자) ⭐⭐ | `user-question.md` |
| ANNOUNCE-001 | 공지 생성 | `user-notice.md` |
| ANNOUNCE-002 | 공지 목록 조회 | `user-notice.md` |
| ANNOUNCE-003 | 공지 상세 조회 | `user-notice.md` |
| ANNOUNCE-004 | 공지 수정 | `user-notice.md` |
| ANNOUNCE-005 | 공지 삭제 | `user-notice.md` |

### Likes / Download Queue / Whitelist

| 코드 | 제목 | 파일 |
|------|------|------|
| LIKE-001 | 즐겨찾기 추가 ⭐ | `likes.md` |
| LIKE-002 | 즐겨찾기 목록 조회 ⭐ | `likes.md` |
| LIKE-003 | 즐겨찾기 해제 ⭐ | `likes.md` |
| DLQ-001 | 대기 목록 추가 ⭐ | `download-queue.md` |
| DLQ-002 | 대기 목록 조회 ⭐ | `download-queue.md` |
| DLQ-003 | 대기 목록 제거 ⭐ | `download-queue.md` |
| WL-001 | 채널 등록 ⭐ | `whitelist.md` |
| WL-002 | 내 채널 목록 조회 ⭐ | `whitelist.md` |
| WL-003 | 채널 수정 ⭐ | `whitelist.md` |
| WL-004 | 채널 삭제 ⭐ | `whitelist.md` |

### Business License

| 코드 | 제목 | 파일 |
|------|------|------|
| BL-001 | 기업 라이센스 신청 ⭐ | `business-license.md` |
| BL-002 | 내 라이센스 신청 현황 조회 ⭐ | `business-license.md` |
| BL-003 | 라이센스 신청 목록 조회 (관리자) ⭐ | `business-license.md` |
| BL-004 | 라이센스 신청 상세 조회 (관리자) ⭐ | `business-license.md` |
| BL-005 | 라이센스 심사 처리 (관리자) ⭐ | `business-license.md` |

### Util

| 코드 | 제목 | 파일 |
|------|------|------|
| UTIL-002 | 이메일 중복 확인 | `util.md` |
| UTIL-003 | 휴대폰 중복 확인 | `util.md` |
| UTIL-004 | 토큰 재발급 | `util.md` |
| UTIL-005 | 구독 등급 확인 | `util.md` |
| UTIL-006 | 다운로드 횟수 확인 | `util.md` |
| UTIL-007 | 회원 타입 확인 | `util.md` |
| UTIL-012 | 닉네임 중복 확인 ⭐ | `util.md` |

> ⭐ = v3 신규 추가 (원본 대비)
> ⭐⭐ = v4 신규 추가 (상호 검토 반영)

---

## 변경 이력 (v3 → v4)

### UC v4 수정 사항 (상호 검토 확정)

| # | 항목 | 내용 |
|---|------|------|
| 1 | INFO-013 소셜 로그인 | isProfileComplete 파생 필드 추가. 신규 가입 시 2단계 프로필 완성 흐름으로 분기. INFO-014 참조. |
| 2 | INFO-014 소셜 프로필 완성 | **신규** — `PUT /api/users/me/complete-profile`. 소셜 최초 가입자 필수 프로필 입력. |
| 3 | SOUND-010 음원 재생 | stream API에서 play_histories 기록 제거. 프론트엔드가 SOUND-004를 별도 호출하는 구조로 변경. |
| 4 | SOUND-004 재생 기록 저장 | 트리거 변경: "SOUND-010 내부 자동" → "프론트엔드가 QueBar 재생 시작 시 명시적 호출". |
| 5 | SOUND-002/007/008/013/017/019/020 | 사전 조건에 "활성 구독(ACTIVE) 보유" 추가. 플레이리스트 전 기능 구독자 전용 확정. |
| 6 | QUESTION-006 수정 제거 | 문의 수정 기능 제거. 수정 불가 정책 → 삭제 후 재작성 유도. |
| 7 | QUESTION-006 삭제 (재번호) | 기존 QUESTION-007(삭제) → QUESTION-006(삭제)로 재번호. |
| 8 | QUESTION-007 상태 변경 | **신규** — 관리자 문의 상태 변경 UC (기존 API 8.7 → v4 API 8.6 대응). |
| 9 | PAYMENT-010 본인 구독 취소 | **신규** — `DELETE /api/user-subscriptions/me`. 회원 직접 구독 취소. |
| 10 | BL-001 사전 조건 수정 | "PENDING 또는 APPROVED 없음" → "PENDING/APPROVED 없음, REJECTED/REVISION_REQUESTED 후 재신청 가능" |

### 제거된 UC (v3 → v4)

| 원본 코드 | 이유 |
|-----------|------|
| QUESTION-006 (문의 수정) | 문의 수정 기능 제거 결정. 프론트엔드에서 수정 불가 안내 + 삭제 후 재작성 유도. |

---

## 변경 이력 (원본 → v3)

### 원본 대비 주요 수정 사항

| # | 항목 | 내용 |
|---|------|------|
| 1 | INFO-001 | 흐름 번호 중복 수정, job/userType 필드 추가, 닉네임 중복 확인 단계 추가 |
| 2 | INFO-005 | 수정 가능 필드 명시(nickname/phonePersonal/phoneCompany/job), email/userType 불변 명시 |
| 3 | INFO-007 | 논리적 삭제(is_deleted=1)로 변경, 비밀번호 재확인 추가 |
| 4 | INFO-009 | 설명 오류 수정("특정 회원의" → "본인의"), 구독 활성 사전 조건 제거 |
| 5 | INFO-011 | 코드 오타 수정 (IFNO-011 → INFO-011) |
| 6 | SOUND-001 | 파일 업로드 흐름 수정 (multipart를 백엔드에 직접 전송), 비동기 preview_file 생성 추가 |
| 7 | SOUND-002 | 액터 수정 (관리자/아티스트 → 사용자(회원)) |
| 8 | SOUND-004/009/015 | "재생 목록(playlog)" → "재생 기록(play_histories)"으로 명칭 수정 |
| 9 | SOUND-010 | play_histories 기록 + play_count 증가 사후 조건 추가 |
| 10 | SOUND-011 | 사전 조건 수정(구독 활성 + COUNT 쿼리 체크), 다운로드 횟수 차감 → COUNT 방식으로 수정, 라이센스 자동 발급 추가 |
| 11 | SOUND-016 | 흐름 번호 수정, track_tags 삭제 명시 |
| 12 | PAYMENT-001 | billingCycle 추가, 기업 라이센스 승인 사전 조건 추가, 음원 사용 라이센스 혼동 제거 |
| 13 | PAYMENT-007 | 사후 조건에서 라이센스 변경 내용 제거 |
| 14 | QUESTION-001 | category/isPublic/첨부파일 필드 추가 |
| 15 | QUESTION-002 | 답변 가능 대상 명시 (회원=본인 문의만, 관리자=전체) |

### 신규 추가 UC (19개)

| 코드 | 제목 | 사유 |
|------|------|------|
| INFO-013 | 소셜 로그인 | API spec 섹션 5.3에 존재하나 원본 누락 |
| SOUND-019 | 플레이리스트 음원 추가 | API spec 섹션 4.5에 존재하나 원본 누락 |
| SOUND-020 | 플레이리스트 음원 제거 | API spec 섹션 4.6에 존재하나 원본 누락 |
| LIKE-001~003 | 즐겨찾기 CRUD | DB likes 테이블 + API 섹션 10 존재하나 원본 누락 |
| DLQ-001~003 | 다운로드 대기 목록 | DB download_queue 테이블 + API 섹션 11 존재하나 원본 누락 |
| WL-001~004 | 화이트리스트 채널 | DB whitelist_channels 테이블 + API 섹션 12 존재하나 원본 누락 |
| BL-001~005 | 기업 라이센스 심사 | DB business_license_requests 테이블 + API 섹션 13 존재하나 원본 누락 |
| UTIL-012 | 닉네임 중복 확인 | INFO-001/005에서 필요하나 원본 누락. API 14.7 추가 확정 |

### 제거된 UC (5개)

| 원본 코드 | 이유 |
|-----------|------|
| UTIL-001 (토큰 발급) | INFO-008(로그인)에 통합 |
| UTIL-008 (라이센스 발급) | SOUND-011(음원 다운로드)에 통합 |
| UTIL-009 (입력값 검증-BE) | Spring Bean Validation 표준 기능 |
| UTIL-010 (입력값 검증-FE) | 프론트엔드 코드 레벨, UC 범위 아님 |
| UTIL-011 (파일 저장) | SOUND-001(음원 생성)에 통합 |

### DB/API 명세 변경 사항 (v2 → v3)

| # | 항목 | 내용 |
|---|------|------|
| 1 | `tracks.preview_file` 컬럼 추가 | 업로드 후 비동기 저품질 파일 생성. NULL이면 audio_file로 스트리밍 fallback |
| 2 | `GET /api/utils/check-nickname` API 추가 | 닉네임 중복 확인 API (UTIL-012) |
| 3 | `track_tags` 조인 테이블 | 음원 논리적 삭제 시 물리적 삭제 확정 |
