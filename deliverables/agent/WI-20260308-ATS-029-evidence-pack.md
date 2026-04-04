# WI-20260308-ATS-029 — Evidence Pack

**WI ID**: WI-20260308-ATS-029
**REQ**: REQ-20260307-ATS-009
**Agent**: docops
**Completed**: 2026-03-08
**Depends On**: WI-023~027 (Phase 1 검증 완료)
**Blocks**: -

---

## Acceptance Criteria Verification

| Criterion | Status | Evidence |
|-----------|--------|----------|
| sound-track.md — SOUND-021 UC 추가 (Actor: Admin, API: GET /api/tracks/admin) | PASS | sound-track.md:245 |
| user-info.md — INFO-015 UC 추가 (Actor: Member, API: PUT /api/users/me/password) | PASS | user-info.md:281 |
| atstudio-front-list.md — `POST /api/download-queue` → `POST /api/download-queue/{trackId}` 수정 | PASS | atstudio-front-list.md:25, :26 |
| screen-flow.md — 동일 URL 수정 | PASS | screen-flow.md:80 |
| screen-flow.md — `[M-17 StatusModal]` → `[M-17 ReviewModal]` 수정 | PASS | screen-flow.md:282 |
| index.md — sound-track UC 카운트 갱신 (7 → 8) | PASS | index.md:14 |
| index.md — user-info UC 카운트 갱신 (10 → 11) | PASS | index.md:18 |
| index.md — 전체 UC 카운트 갱신 (87 → 89) | PASS | index.md:30 |
| 추가된 UC에 올바른 UC ID 부여 (기존 최대 번호 + 1) | PASS | SOUND-021 (prev max: SOUND-020), INFO-015 (prev max: INFO-014) |
| UC 형식이 기존 파일 패턴과 일치 | PASS | 기존 UC 구조(table + main flow + exception + postconditions) 준수 |

---

## File Change Pointers

### docs/design/usecase/sound-track.md

| Change | Line Range | Description |
|--------|------------|-------------|
| SOUND-021 UC 추가 | 245-298 | Admin track list UC. `GET /api/tracks/admin` [ADMIN]. Query params: page(default=1), size(default=20), isActive(optional). Response: AdminTrackListItemResponse. |

**Before** (EOF after line 241):
```
- Track excluded from track list queries (SOUND-005).
```

**After** (appended):
```markdown
## SOUND-021: List Tracks (Admin)
| Code | SOUND-021 |
| Version | 26-03-08 |
...
```

### docs/design/usecase/user-info.md

| Change | Line Range | Description |
|--------|------------|-------------|
| INFO-015 UC 추가 | 281-315 | Change password UC. `PUT /api/users/me/password` [Auth]. currentPassword 검증 → newPassword BCrypt 저장 → 204. Exception: 400 on mismatch. |

**Before** (EOF after line 277):
```
- users.is_deleted=1 updated. Login with this account is no longer possible.
```

**After** (appended):
```markdown
## INFO-015: Change Password
...
```

### docs/design/usecase/index.md

| Change | Line | Before | After |
|--------|------|--------|-------|
| Version 헤더 | 2 | `v4 (Confirmed)` / `2026-02-20` | `v6 (Confirmed)` / `2026-03-08` |
| sound-track.md UC 수 | 14 | `7` | `8` |
| user-info.md UC 수 | 18 | `10` | `11` |
| 전체 UC 수 | 30 | `87` | `89` |
| Sound UC 목록 | (SOUND-020 다음 행) | (없음) | `SOUND-021 \| List tracks (Admin) \| sound-track.md` |
| User Info UC 목록 | (INFO-014 다음 행) | (없음) | `INFO-015 \| Change password \| user-info.md` |
| Change History | (v5 섹션 앞) | (없음) | v6 change history 블록 추가 |

### docs/ui/atstudio-front-list.md

| Change | Line | Before | After |
|--------|------|--------|-------|
| Screen 1 메인화면 API | 25 | `11.1 POST /api/download-queue` | `11.1 POST /api/download-queue/{trackId}` |
| Screen 3 음원목록 API | 26 | `11.1 POST /api/download-queue` | `11.1 POST /api/download-queue/{trackId}` |

**Scope note**: Line 73 (`11.1~11.3 /api/download-queue`) — range notation, not a path-param error. Per WI constraint, not modified.

### docs/ui/screen-flow.md

| Change | Line | Before | After |
|--------|------|--------|-------|
| B-1 장바구니 담기 API | 80 | `11.1 POST /api/download-queue` | `11.1 POST /api/download-queue/{trackId}` |
| K-5 기업인증 심사 컴포넌트명 | 282 | `[M-17 StatusModal]` | `[M-17 ReviewModal]` |

---

## Constraint Compliance

| Constraint | Verified |
|------------|----------|
| 기존 UC 번호/내용 변경 없음 | PASS — SOUND-001~SOUND-020, INFO-001~INFO-014 미변경 |
| MINOR 항목(path parameter 단축 기재 등) 미수정 | PASS — screen-flow.md:73 range notation 미수정 |
| api-spec.md 수정 없음 | PASS — 해당 파일 미접근 |
| 백엔드 코드 파일 수정 없음 | PASS — src/ 미접근 |

---

## Drift Detection Notes

- atstudio-front-list.md line 73: `11.1~11.3 /api/download-queue` — range notation, path param 미포함. MINOR 범주로 WI scope-out. 별도 WI 필요 여부는 MA 판단.
- INFO-015 Preconditions: 소셜 로그인 계정(password=NULL)의 비밀번호 변경 처리 정책이 백엔드 구현 단계에서 명확히 다루어져야 함. 현재 UC 수준에서는 Preconditions로 제한 명시.

---

## Tier 0 Compliance

| Standard | Check |
|----------|-------|
| core-principles.md — Language Policy: UC 문서 English | PASS |
| documentation-standards.md — UC 형식 일관성 유지 | PASS |
| glossary.md — track, download-queue canonical terms 사용 | PASS |
