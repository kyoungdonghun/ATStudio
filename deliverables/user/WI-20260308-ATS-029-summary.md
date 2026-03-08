# WI-20260308-ATS-029 — 변경 요약

**WI ID**: WI-20260308-ATS-029
**REQ**: REQ-20260307-ATS-009
**완료일**: 2026-03-08
**담당**: docops

---

## 변경 개요

Phase 2 MAJOR 문서 수정 4건이 완료되었습니다. 유스케이스 2건 신규 추가, 화면 목록/흐름도 URL 및 컴포넌트명 2건 수정, 인덱스 카운트 갱신이 포함됩니다.

---

## 변경 내역

### 1. sound-track.md — SOUND-021 (Admin 트랙 목록) UC 추가

- 신규 UC: `SOUND-021: List Tracks (Admin)`
- API: `GET /api/tracks/admin` [ADMIN]
- 내용: Admin이 활성/비활성 전체 트랙 목록을 조회. isActive 파라미터로 필터링 가능.
- 응답: AdminTrackListItemResponse (id, title, bpm, tonality, thumbnail, playCount, isActive, tags, createdAt)

### 2. user-info.md — INFO-015 (비밀번호 변경) UC 추가

- 신규 UC: `INFO-015: Change Password`
- API: `PUT /api/users/me/password` [Auth]
- 내용: 현재 비밀번호 확인 후 새 비밀번호로 변경 → 204 No Content
- 예외: 현재 비밀번호 불일치 → 400 Bad Request

### 3. atstudio-front-list.md — download-queue URL 수정 (2건)

| 화면 | 변경 전 | 변경 후 |
|------|---------|---------|
| 1 (메인화면) | `POST /api/download-queue` | `POST /api/download-queue/{trackId}` |
| 3 (음원 목록) | `POST /api/download-queue` | `POST /api/download-queue/{trackId}` |

### 4. screen-flow.md — URL 수정 + M-17 컴포넌트명 수정

| 위치 | 변경 전 | 변경 후 |
|------|---------|---------|
| 3. 음원 탐색 흐름 (B-1 장바구니 담기) | `POST /api/download-queue` | `POST /api/download-queue/{trackId}` |
| 10. 관리자 페이지 흐름 (K-5) | `[M-17 StatusModal]` | `[M-17 ReviewModal]` |

### 5. usecase/index.md — UC 카운트 갱신

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| sound-track.md UC 수 | 7 | 8 |
| user-info.md UC 수 | 10 | 11 |
| 전체 UC 수 | 87 | 89 |
| 인덱스 버전 | v4 | v6 |

---

## 승인 포인트

- SOUND-021 UC 내용이 `GET /api/tracks/admin` API spec 의도와 일치하는지 확인 바랍니다.
- INFO-015 UC에서 소셜 로그인 전용 계정(password=NULL) 예외 처리는 현재 Preconditions에 명시("Account was registered via email/password")되었으나, 백엔드 구현 시 별도 분기 확인이 필요합니다.
