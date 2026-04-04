# WI-20260307-ATS-011 Cross-Validation Summary

> **WI**: WI-20260307-ATS-011
> **Date**: 2026-03-07
> **Scope**: docs/design/api-spec.md §8/§9/§13/§14, usecase files (company-certification, user-question, user-notice, business-license), docs/ui/atstudio-front-list.md, docs/ui/modal-list.md, docs/ui/screen-flow.md
> **Result**: 7 findings (1 CRITICAL, 3 MAJOR, 2 MINOR, 1 SUGGESTION)

---

## Finding Count by Severity

| Severity | Count |
|----------|-------|
| CRITICAL | 1 |
| MAJOR | 3 |
| MINOR | 2 |
| SUGGESTION | 1 |
| **Total** | **7** |

---

## Finding Summary

### CRITICAL

**C-01: CC-001 reapplication preconditions contradict screen-flow I-2 policy**

`company-certification.md` (CC-001 preconditions) states "Reapplication allowed after REJECTED or REVISION_REQUESTED." `screen-flow.md` (§8, I-2) explicitly states "UI 재신청 흐름 없음" and directs users to contact admin via email/1:1 inquiry. These are directly contradictory. The screen-flow policy is the confirmed design decision; the usecase has not been updated to reflect it.

Impact: If a frontend engineer reads CC-001, they will build a reapplication UI that contradicts the confirmed design.

---

### MAJOR

**M-01: modal-list M-20 references wrong API number for inquiry delete**

`modal-list.md` M-20 cites `8.4 DELETE /api/questions/{id}`. api-spec §8.4 is `GET /api/questions/{questionId}` (inquiry detail). The delete API is `8.7 DELETE /api/questions/{questionId}`. The section number and HTTP method in M-20 are both wrong.

**M-02: modal-list M-17 URL includes /review suffix not present in api-spec**

`modal-list.md` M-17 cites `13.5 PUT /api/company-certifications/{id}/review`. api-spec §13.5 URL is `PUT /api/company-certifications/{certificationId}` (no `/review` suffix). The URL in M-17 does not match the implemented endpoint.

**M-03: modal-list M-17 uses StatusModal component for a 3-option form interaction**

`modal-list.md` M-17 assigns ComponentType = `StatusModal` (defined as "안내 텍스트 + [확인] 1-button"). But M-17 content requires selecting one of three review outcomes (APPROVED / REVISION_REQUESTED / REJECTED) and entering free-text adminNote. A 1-button info modal cannot support this interaction.

---

### MINOR

**Mi-01: modal-list M-19 trigger screen includes Screen 21 incorrectly**

`modal-list.md` M-19 (notice delete) lists trigger screen as "Screen 21/22 (공지 조회)". Screen 21 is "공지 작성" (create), not a context where delete applies. `screen-flow.md` §9 correctly limits the delete trigger to Screen 22 only. The M-19 screen reference is inconsistent with screen-flow.

**Mi-02: front-list Screen 15 omits delete API reference**

`atstudio-front-list.md` Screen 15 (문의글 보기) lists APIs `8.4 GET`, `8.2 POST`, `8.5 GET` but does not list `8.7 DELETE`. `screen-flow.md` §9 and `modal-list.md` M-20 both confirm delete is triggered from Screen 15. The front-list API column is incomplete for this screen.

---

### SUGGESTION

**S-01: api-spec v3→v4 change history references stale UC/API numbers**

api-spec v3→v4 history item 7 states "Added QUESTION-008 (admin only, corresponds to existing 8.7 API)". Current state: UC is QUESTION-007, API is 8.6. The history entry uses numbering from a previous version and may confuse readers tracing the change log.

---

## Pre-existing Known Issue (Confirmed)

**C-01 (WI-008)**: K-7 in front-list states `1.2 GET /api/tracks (비활성 포함)` but api-spec 1.2 explicitly returns only `is_active=1` tracks. No additional endpoint for inactive track listing exists. This was already flagged as CRITICAL C-01 in WI-008 and is confirmed to remain unresolved.

---

## Approval Points

The following require decisions before frontend implementation proceeds:

1. **C-01 (Reapplication policy)**: Confirm whether REVISION_REQUESTED/REJECTED triggers a UI reapplication path (CC-001) or admin-contact-only path (screen-flow I-2). One document must be corrected.
2. **M-01 (M-20 API ref)**: Correct M-20 to reference `8.7 DELETE`. No behavior change — documentation fix only.
3. **M-02 (M-17 URL)**: Confirm whether the implemented endpoint is `/api/company-certifications/{id}` or `/api/company-certifications/{id}/review`. Correct the non-matching document.
4. **M-03 (M-17 component)**: Replace StatusModal with an appropriate component type for the 3-option + note input interaction in M-17.
