# WI-20260307-ATS-011 Evidence Pack

> **WI**: WI-20260307-ATS-011
> **Date**: 2026-03-07
> **Task**: Cross-validation of api-spec §8/§9/§13/§14, usecase files, front-list, modal-list, screen-flow
> **Agent**: docops
> **Status**: Complete — no document modifications performed (read-only task)

---

## Source Files Read

| File | Sections Checked |
|------|-----------------|
| `docs/design/api-spec.md` | §8 (lines 1004–1143), §9 (lines 1146–1229), §13 (lines 1394–1481), §14 (lines 1484–1618), v3→v4 history (line 30) |
| `docs/design/usecase/company-certification.md` | CC-001 through CC-005 (lines 1–136) |
| `docs/design/usecase/user-question.md` | QUESTION-001 through QUESTION-007 (lines 1–191) |
| `docs/design/usecase/user-notice.md` | ANNOUNCE-001 through ANNOUNCE-005 (lines 1–113) |
| `docs/design/usecase/business-license.md` | Full (line 1–3: redirect stub to company-certification.md) |
| `docs/check/atstudio-front-list.md` | I-1, I-2, Screen 13~15, 20~22, K-1~K-7, Screen 18 (lines 95–151) |
| `docs/check/modal-list.md` | M-15~M-20, M-25, M-28 (lines 84–97) |
| `docs/check/screen-flow.md` | §8 I-1/I-2 (lines 223–237), §9 문의/공지 (lines 241–261), §10 관리자 (lines 265–297) |

---

## Detailed Findings

---

### [CONFLICT] CRITICAL C-01: CC-001 reapplication preconditions vs screen-flow I-2 policy

**File A**: `docs/design/usecase/company-certification.md` — CC-001, Preconditions field
**File B**: `docs/check/screen-flow.md` — §8, I-2 block

**File A text** (company-certification.md, CC-001 Preconditions):
> "Reapplication allowed after REJECTED or REVISION_REQUESTED."

**File B text** (screen-flow.md, §8, I-2):
> "REVISION_REQUESTED / REJECTED: → 관리자가 1:1 문의 또는 이메일로 직접 컨택 (UI 재신청 흐름 없음)"
> "추가 안내는 등록하신 이메일 또는 1:1 문의를 확인해주세요 안내 표시"

And the policy note immediately below I-2:
> "기업인증 핵심 정책: REVISION_REQUESTED/REJECTED 케이스는 관리자 직접 컨택으로 처리 (1:1 문의 전 초벌 필터 역할)."

**Conflict**: CC-001 preconditions allow UI reapplication after REJECTED/REVISION_REQUESTED. screen-flow explicitly states no UI reapplication path exists and redirects to admin contact only.

**Authority**: screen-flow.md is the confirmed design decision document (v1.1 confirmed 2026-03-07). CC-001 preconditions must be updated to reflect the confirmed policy.

**Required fix**: In CC-001, update the Preconditions to remove "Reapplication allowed after REJECTED or REVISION_REQUESTED" and replace with the screen-flow-aligned text: "On REVISION_REQUESTED or REJECTED status, no self-service reapplication UI exists. Admin contacts member directly. PENDING and APPROVED statuses block new applications (409 Conflict)."

---

### [CONFLICT] MAJOR M-01: modal-list M-20 cites wrong API section number and HTTP method

**File A**: `docs/check/modal-list.md` — M-20, API column
**File B**: `docs/design/api-spec.md` — §8

**File A text** (modal-list.md, line 89):
```
| M-20 | QUESTION-006 | Screen 15 (문의 보기) | "문의 삭제" 클릭 | "문의를 삭제하시겠습니까?" | ConfirmModal | `8.4 DELETE /api/questions/{id}` |
```

**File B text** (api-spec.md):
- `8.4` = `GET /api/questions/{questionId}` (Inquiry Detail — line 1078)
- `8.7` = `DELETE /api/questions/{questionId}` (Delete Inquiry — line 1136)

**Conflict**: M-20 cites `8.4 DELETE` but §8.4 is GET, not DELETE. The delete operation is §8.7. Both the section number and HTTP method are wrong.

**Required fix**: Update modal-list M-20 API column from `8.4 DELETE /api/questions/{id}` to `8.7 DELETE /api/questions/{questionId}`.

**Secondary note**: screen-flow.md §9 (line 250) also references M-20 for question delete on Screen 15 — this is consistent with the intended behavior (delete from detail screen), so no fix needed there once M-20 is corrected.

---

### [CONFLICT] MAJOR M-02: modal-list M-17 URL has /review suffix absent from api-spec

**File A**: `docs/check/modal-list.md` — M-17, API column
**File B**: `docs/design/api-spec.md` — §13.5

**File A text** (modal-list.md, line 86):
```
| M-17 | CC-005 | K-5 (기업인증 심사) | 심사결과 처리 클릭 | ... | StatusModal | `13.5 PUT /api/company-certifications/{id}/review` |
```

**File B text** (api-spec.md, §13.5 line 1460):
```
| **URL** | `PUT /api/company-certifications/{certificationId}` |
```

**Conflict**: modal-list appends `/review` to the URL. api-spec does not include `/review`. Only one can be correct; they cannot both be implemented simultaneously.

**Resolution path**: Check the implemented `CompanyCertificationController.java` endpoint URL. Correct the non-matching document to align with the implementation. (File inspection is out of scope for this WI.)

**Note**: front-list K-5 references only "13.5 PUT" without the full URL, so it is not affected either way.

---

### [CONFLICT] MAJOR M-03: modal-list M-17 assigns StatusModal to a multi-option form interaction

**File A**: `docs/check/modal-list.md` — M-17 Component column and modal-list Component Classification table
**File B**: `docs/check/modal-list.md` — Component Classification (lines 44–52)

**Component Classification definition** (modal-list.md, lines 44–52):
> `StatusModal` = "상태 안내/확인 (안내 텍스트 + [확인])" — 1-button modal, text-only.

**M-17 content description** (modal-list.md, line 86):
> "APPROVED / REVISION_REQUESTED / REJECTED 선택 + adminNote 입력"

**Conflict**: The content requires (a) selecting one of three enum values and (b) entering free-text adminNote. A 1-button, text-only StatusModal cannot support dropdown/radio selection or text input fields.

**Required fix**: Replace M-17 Component from `StatusModal` to an appropriate type. Options: a new `FormModal` component type (not currently defined), or `InputModal` extended with a status selector. The Component Classification table may need a new entry if a FormModal pattern is introduced.

---

### [CONFLICT] MINOR Mi-01: modal-list M-19 trigger screen includes Screen 21 incorrectly

**File A**: `docs/check/modal-list.md` — M-19, 발생 화면 column
**File B**: `docs/check/screen-flow.md` — §9 공지 흐름
**File C**: `docs/check/atstudio-front-list.md` — Screen 21 definition

**File A text** (modal-list.md, line 88):
```
| M-19 | ANNOUNCE-005 | Screen 21/22 (공지 조회) | "공지 삭제" 클릭 | ...
```

**File B text** (screen-flow.md, §9, lines 253–254):
```
[22 공지 조회]
     [ADMIN] "삭제" → [M-19 ConfirmModal] → [20 목록]
```

**File C text** (atstudio-front-list.md, line 119):
```
| 21 | 공지 작성 (관리자 전용) | `9.1 POST /api/notices` | [ADMIN] |
```

**Conflict**: M-19 lists trigger screen as "Screen 21/22". Screen 21 is the notice creation screen; the delete action has no logical context there. screen-flow correctly limits the trigger to Screen 22 only. The parenthetical label "(공지 조회)" in M-19 already implies Screen 22 is the intended screen.

**Required fix**: Update M-19 발생 화면 column from "Screen 21/22 (공지 조회)" to "Screen 22 (공지 조회)".

---

### [GAP] MINOR Mi-02: front-list Screen 15 omits 8.7 DELETE from API reference column

**File A**: `docs/check/atstudio-front-list.md` — Screen 15 row
**File B**: `docs/check/screen-flow.md` — §9, Screen 15 flow
**File C**: `docs/check/modal-list.md` — M-20

**File A text** (atstudio-front-list.md, line 110):
```
| 15 | 문의글 보기 | `8.4 GET /api/questions/{id}` `8.2 POST (답변 작성)` `8.5 GET (첨부파일)` | auth required |
```

**File B text** (screen-flow.md, §9, line 250):
```
└── "삭제" → [M-20 ConfirmModal] → [13 목록]
```

**File C text** (modal-list.md, M-20): Delete modal triggered from Screen 15, calling `8.7 DELETE /api/questions/{questionId}` (after C-01 fix is applied).

**Gap**: Front-list Screen 15 does not list `8.7 DELETE` in the related API column even though the delete action is a confirmed interaction on this screen.

**Required fix**: Add `8.7 DELETE /api/questions/{id}` to Screen 15 API column in atstudio-front-list.md.

---

### [SUGGESTION] S-01: api-spec v3→v4 change history references stale UC/API numbering

**File**: `docs/design/api-spec.md` — v3→v4 Change History, item 7 (line 30)

**Text**:
> "Add inquiry status change API UC — Added (confirmed) — QUESTION-008 (admin only, corresponds to existing 8.7 API)"

**Current state**:
- UC code is QUESTION-007 (user-question.md)
- API section is 8.6 (api-spec.md §8.6)

**Issue**: The history entry was written when numbering was different. QUESTION-008 and "8.7 API" in the history do not match current documents. Anyone reading the history to trace the change will find inconsistent numbers.

**Suggested fix**: Update the history entry to: "Add inquiry status change API UC — Added (confirmed) — QUESTION-007 (admin only, corresponds to 8.6 PUT /api/questions/{questionId}/status)".

---

## Pre-existing Known Issue Verification

### K-7 (WI-008 CRITICAL C-01) — Confirmed unresolved

**File**: `docs/check/atstudio-front-list.md` — K-7 row (line 136)
**Reference**: WI-008 CRITICAL C-01

**Text**:
```
| K-7 | 트랙 관리 (전체 목록 + 활성화/삭제) | `1.2 GET /api/tracks` (비활성 포함) `1.6 PUT` `1.7 DELETE` | [ADMIN] |
```

**api-spec §1.2 description** (api-spec.md, line 162):
> "Returns only active (is_active=1) tracks"

**Status**: CONFLICT persists. No admin-specific endpoint exists in api-spec for retrieving inactive tracks. WI-008 flagged this; no corrective action has been taken in these documents. Out of scope for this WI — not a new finding.

---

## Cross-Validation Matrix (Full Coverage)

| Check Point | Result | Finding ID |
|-------------|--------|------------|
| CC-001 preconditions vs screen-flow I-2 reapplication policy | CONFLICT | C-01 |
| CC-002 status display vs screen-flow I-2 | OK — consistent | — |
| CC-003/004/005 admin flow vs front-list K-5 | OK — consistent | — |
| M-17 URL vs api-spec 13.5 URL | CONFLICT | M-02 |
| M-17 component type vs interaction requirement | CONFLICT | M-03 |
| M-15 deferred status vs front-list I-1 | OK — both show [보류] | — |
| M-20 API reference vs api-spec §8 | CONFLICT | M-01 |
| M-18 API reference vs api-spec 8.6 | OK | — |
| M-19 trigger screen vs screen-flow §9 | CONFLICT | Mi-01 |
| Screen 15 API column vs screen-flow + M-20 | GAP | Mi-02 |
| QUESTION-001~006 UC vs api-spec §8 structure | OK — aligned | — |
| QUESTION-007 status flow vs api-spec 8.6 status flow | OK — identical | — |
| Inquiry edit policy (UC note vs v3→v4 history) | OK — both confirm no edit | — |
| ANNOUNCE-001~005 UC vs api-spec §9 | OK — aligned | — |
| Screen 20~22 vs api-spec §9 | OK — aligned | — |
| business-license.md | Redirect stub only (→ company-certification.md) | — |
| api-spec v3→v4 history item 7 numbering | Stale reference | S-01 |
| K-7 inactive track gap | Pre-existing (WI-008 C-01) | — |
| K-1 through K-6 vs api-spec | OK | — |
| M-25 vs api-spec 5.8 | OK | — |
| M-28 vs api-spec 2.4 | OK | — |
| screen-flow §10 admin flows vs front-list K-1~K-7 | OK | — |
| screen-flow I-2 policy note | OK — confirmed design decision | — |

---

## Fix Priority Order

| Priority | Finding | File to Fix | Change |
|----------|---------|-------------|--------|
| 1 (CRITICAL) | C-01 | `docs/design/usecase/company-certification.md` | CC-001 Preconditions — remove reapplication allowance, replace with no-UI-reapplication policy |
| 2 (MAJOR) | M-01 | `docs/check/modal-list.md` | M-20 API column: `8.4 DELETE` → `8.7 DELETE` |
| 3 (MAJOR) | M-02 | `docs/check/modal-list.md` OR `docs/design/api-spec.md` | M-17 URL: verify against implementation; correct non-matching doc |
| 4 (MAJOR) | M-03 | `docs/check/modal-list.md` | M-17 Component: replace StatusModal with appropriate component type |
| 5 (MINOR) | Mi-01 | `docs/check/modal-list.md` | M-19 screen: "Screen 21/22" → "Screen 22" |
| 6 (MINOR) | Mi-02 | `docs/check/atstudio-front-list.md` | Screen 15 API column: add `8.7 DELETE /api/questions/{id}` |
| 7 (SUGGESTION) | S-01 | `docs/design/api-spec.md` | v3→v4 history item 7: update UC/API numbers to current |

---

## Notes for Next WI

- M-02 resolution requires checking `CompanyCertificationController.java` endpoint mapping before deciding which document to fix. This is a code-read dependency outside this WI scope.
- M-03 fix may require adding a new component type (`FormModal`) to the Component Classification table in modal-list.md. Coordinate with the screen design owner before adding new component types.
- C-01 fix in CC-001 must also check whether CC-001 "Trigger" line needs updating (currently: "User clicks the 'Apply for Company Certification Review' button" — this may need a note that this trigger is only available when no existing PENDING/APPROVED application exists, and no reapplication is available via UI for REJECTED/REVISION_REQUESTED).
