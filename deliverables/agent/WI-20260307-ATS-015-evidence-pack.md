# WI-20260307-ATS-015 Evidence Pack

**WI ID**: WI-20260307-ATS-015
**REQ**: REQ-20260307-ATS-008
**Date**: 2026-03-07
**Agent**: docops
**Status**: DONE

---

## Acceptance Criteria Verification

### [atstudio-front-list.md]

| # | Criterion | File:Line | Result |
|---|-----------|-----------|--------|
| 1 | B-1 API에 `4.1 POST /api/play-histories` 추가 | `docs/check/atstudio-front-list.md:27` | PASS |
| 2 | Screen 1 API에 `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` 추가 | `docs/check/atstudio-front-list.md:25` | PASS |
| 3 | Screen 3 API에 `10.1 POST /api/likes/{trackId}`, `11.1 POST /api/download-queue` 추가 | `docs/check/atstudio-front-list.md:26` | PASS |
| 4 | Screen 15 API에 `8.7 DELETE /api/questions/{id}` 추가 | `docs/check/atstudio-front-list.md:110` | PASS |
| 5 | K-7 API에 `1.8 GET /api/tracks/admin` 추가 (기존 `1.2 GET /api/tracks` 교체) | `docs/check/atstudio-front-list.md:136` | PASS |
| 6 | 버전 v3 → v4, 날짜 2026-03-07 | `docs/check/atstudio-front-list.md:3` | PASS |

### [modal-list.md]

| # | Criterion | File:Line | Result |
|---|-----------|-----------|--------|
| 7 | Component Classification에 ReviewModal 행 추가 | `docs/check/modal-list.md:52` | PASS |
| 8 | M-17 API: `{id}/review` suffix 제거 → `{certificationId}` | `docs/check/modal-list.md:86` | PASS |
| 9 | M-17 컴포넌트: StatusModal → ReviewModal | `docs/check/modal-list.md:86` | PASS |
| 10 | M-19 발생 화면: "Screen 21/22" → "Screen 22" | `docs/check/modal-list.md:88` | PASS |
| 11 | M-20 API: `8.4 DELETE` → `8.7 DELETE /api/questions/{questionId}` | `docs/check/modal-list.md:89` | PASS |
| 12 | M-22 API: `{id}` → `{trackId}` | `docs/check/modal-list.md:91` | PASS |
| 13 | Flow 4 성공 응답: `200 OK` → `204 No Content` | `docs/check/modal-list.md:213` | PASS |
| 14 | frontmatter version: 1.1 → 1.2 | `docs/check/modal-list.md:2` | PASS |
| 15 | frontmatter dependencies atstudio-front-list.md version: v3 → v4 | `docs/check/modal-list.md:10` | PASS |
| 16 | 헤더 텍스트 v3 → v4 | `docs/check/modal-list.md:38` | PASS |
| 17 | 하단 요약 v1.2 2026-03-07 | `docs/check/modal-list.md:261` | PASS |
| (bonus) | Flow 2 `pendingSubscriptionId TODO T-3` → `pendingSubscriptionId` | `docs/check/modal-list.md:158` | PASS |

### [screen-flow.md]

| # | Criterion | File:Line | Result |
|---|-----------|-----------|--------|
| 18 | §1 관리자 GNB에 앨범관리 항목 추가 | `docs/check/screen-flow.md:30` | PASS |
| 19 | §2 로그아웃 방식 명시 | `docs/check/screen-flow.md:53` | PASS |
| 20 | §7 `pendingSubscriptionId TODO T-3` 레이블 제거 | Already absent in screen-flow.md; applied to modal-list.md:158 where text resided | PASS |
| 21 | §11 404/500 에러 → ERR-1/ERR-2 레이블 포함 | `docs/check/screen-flow.md:311-312` | PASS |
| 22 | 헤더 의존 문서: v4 / v1.2 | `docs/check/screen-flow.md:19` | PASS |
| 23 | 버전 v1.1 → v1.2, 날짜 2026-03-07 | `docs/check/screen-flow.md:2,19,357` | PASS |

---

## Change Details

### docs/check/atstudio-front-list.md

**Before → After (version header)**
```
> API Spec v5 기준 | v3 2026-03-07
→
> API Spec v5 기준 | v4 2026-03-07
```

**Screen 1 (line 25)**
```
Before: `1.2 GET /api/tracks` `2.2 GET /api/tags`
After:  `1.2 GET /api/tracks` `2.2 GET /api/tags` `10.1 POST /api/likes/{trackId}` `11.1 POST /api/download-queue`
```

**Screen 3 (line 26)**
```
Before: `1.2 GET /api/tracks`
After:  `1.2 GET /api/tracks` `10.1 POST /api/likes/{trackId}` `11.1 POST /api/download-queue`
```

**B-1 (line 27)**
```
Before: `1.3 GET /api/tracks/{trackId}` `1.4 GET /api/tracks/{trackId}/stream`
After:  `1.3 GET /api/tracks/{trackId}` `1.4 GET /api/tracks/{trackId}/stream` `4.1 POST /api/play-histories`
```

**Screen 15 (line 110)**
```
Before: `8.4 GET /api/questions/{id}` `8.2 POST (답변 작성)` `8.5 GET (첨부파일)`
After:  `8.4 GET /api/questions/{id}` `8.2 POST (답변 작성)` `8.5 GET (첨부파일)` `8.7 DELETE /api/questions/{id}`
```

**K-7 (line 136)**
```
Before: `1.2 GET /api/tracks` (비활성 포함) `1.6 PUT /api/tracks/{id}` `1.7 DELETE /api/tracks/{id}`
After:  `1.8 GET /api/tracks/admin` `1.6 PUT /api/tracks/{id}` `1.7 DELETE /api/tracks/{id}`
```

### docs/check/modal-list.md

**Component Classification — ReviewModal 추가 (line 52)**
```
| **ReviewModal** | 관리자 심사 처리 (상태 선택 + 메모 입력) | 상태 드롭다운 + adminNote 텍스트입력 + `[취소]` `[처리]` 2-button |
```

**M-17 (line 86)**
```
Before: StatusModal | `13.5 PUT /api/company-certifications/{id}/review`
After:  ReviewModal | `13.5 PUT /api/company-certifications/{certificationId}`
```

**M-19 (line 88)**
```
Before: Screen 21/22 (공지 조회)
After:  Screen 22 (공지 조회)
```

**M-20 (line 89)**
```
Before: `8.4 DELETE /api/questions/{id}`
After:  `8.7 DELETE /api/questions/{questionId}`
```

**M-22 (line 91)**
```
Before: `11.3 DELETE /api/download-queue/{id}`
After:  `11.3 DELETE /api/download-queue/{trackId}`
```

**Flow 4 success response (line 213)**
```
Before: 200 OK → 완료 토스트
After:  204 No Content → 완료 토스트
```

**Flow 2 다운그레이드 경로 (line 158)**
```
Before: → PUT 6.7 /api/user-subscriptions/me (pendingSubscriptionId TODO T-3)
After:  → PUT 6.7 /api/user-subscriptions/me (pendingSubscriptionId)
```

### docs/check/screen-flow.md

**§1 관리자 GNB (line 30)**
```
Before: [관리자]      GNB: 메인 / 앨범 / 음원관리 / 관리자대시보드 / 로그아웃
After:  [관리자]      GNB: 메인 / 앨범 / 앨범관리 / 음원관리 / 관리자대시보드 / 로그아웃
```

**§2 로그아웃 (line 53)**
```
Before: [로그아웃]  어느 화면에서든 → [1 메인] (비로그인 상태)
After:  [로그아웃]  클라이언트 측 토큰 삭제 (서버 무효화 엔드포인트 없음 — 초기 버전) → [1 메인] (비로그인 상태)
```

**§11 에러 패턴 (lines 311-312)**
```
Before: API 에러 404 | [404 에러 페이지]
        API 에러 500 | [500 에러 페이지]
After:  API 에러 404 | [ERR-1 404 Not Found 에러 페이지]
        API 에러 500 | [ERR-2 500 Server Error 에러 페이지]
```

**헤더 (line 19)**
```
Before: atstudio-front-list.md v3 / modal-list.md v1 기준 | v1.1 2026-03-07 확정
After:  atstudio-front-list.md v4 / modal-list.md v1.2 기준 | v1.2 2026-03-07 확정
```

---

## Notes

- Change #20 (screen-flow.md §7 `pendingSubscriptionId TODO T-3`): Text was already absent from screen-flow.md §7. The `TODO T-3` text resided in `modal-list.md` Section 3 Flow 2 (line 158) and was corrected there. screen-flow.md §7 (line 189) already reads `PUT 6.7 → 화면 갱신 (pending 표시)` without the TODO label.
- Scope respected: Only 3 target files modified. api-spec.md and usecase files untouched.
- Backend code: No changes.

---

## Traceability

- REQ: `deliverables/user/REQ-20260307-ATS-008.md`
- Handoff: `deliverables/agent/WI-20260307-ATS-015-handoff.md`
- Summary: `deliverables/user/WI-20260307-ATS-015-summary.md`
