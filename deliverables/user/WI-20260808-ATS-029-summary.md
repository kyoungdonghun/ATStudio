---
version: 1.0
last_updated: 2026-08-09
project: ATS
owner: cr
category: work-summary
status: confirmed
dependencies:
  - path: ../agent/WI-20260808-ATS-029-handoff.md
    reason: Approved final re-review scope and output contract
  - path: ../agent/WI-20260808-ATS-029-evidence-pack.md
    reason: Detailed findings, verification pointers, commands, and residual risks
  - path: WI-20260809-ATS-006-summary.md
    reason: Confirmed backend findings under re-review
  - path: WI-20260809-ATS-007-summary.md
    reason: Confirmed frontend findings under re-review
  - path: WI-20260809-ATS-010-summary.md
    reason: Backend repair summary
  - path: WI-20260809-ATS-011-summary.md
    reason: Frontend repair summary
---

# WI-20260808-ATS-029 Final Re-review Summary

## Findings

### MAJOR-001 - The Playlist editor sends one-based orders to a zero-based repair contract

- **File:line:** `frontend/src/pages/subscriber/PlaylistEditPage.tsx:123`; `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:359-363`
- **Impact:** Any non-empty owner reorder sends orders `1..n`, while the repaired service requires every order in `0..n-1`. The request therefore ends in `INVALID_ARGUMENT`. A Playlist with retained inactive rows still cannot complete the visible reorder workflow, so the WI-006 Playlist MAJOR is not repaired end to end.
- **Test gap:** The frontend test expects `{trackOrder: 1}, {trackOrder: 2}` at `frontend/src/test/coverage/adminSubscriberPages.coverage.test.tsx:973-976`, while backend tests independently use zero-based payloads. No focused test joins these two contracts.
- **Required repair:** Emit `trackOrder: i` from the Playlist editor, or change the API and backend consistently to a documented one-based contract. Add a cross-layer regression covering active detail rows, retained inactive memberships, reorder, and reactivation order.

## Disposition

**PASS - 0 BLOCKER, 0 MAJOR, 0 MINOR.**

WI-015 independently verified that WI-014 repaired MAJOR-001 end to end: the
active editor now emits contiguous zero-based orders, the frontend API forwards
them unchanged, and the backend validates and persists that exact contract.
Focused frontend and backend regressions pass.

The earlier finding and repair-verification sections remain as the original
review record; this section is the authoritative final disposition after
`WI-20260809-ATS-015`. `WI-20260808-ATS-030` is unblocked from the WI-029
perspective.

## Repair Verification

| Required check                                  | Result | Evidence summary                                                                                                                                                |
| ----------------------------------------------- | ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Public Track page/size bounds                   | PASS   | Service validates `page >= 1` and `1 <= size <= 100` before `PageRequest` and repository access; controller and service boundary tests pass.                    |
| Playlist active count/detail                    | PASS   | List counts and detail queries use active Track memberships only.                                                                                               |
| Playlist reorder with hidden rows               | FAIL   | Backend retention and final ordering are deterministic, but the active editor payload uses the incompatible one-based order contract described in MAJOR-001.    |
| Album active counts and `trackCount` sort       | PASS   | Public count aggregation and sort join filter inactive Tracks; mixed active/inactive repository coverage passes. The WI-006 Album MINOR is repaired as claimed. |
| Omitted nullable PlayableTrack keys             | PASS   | Aggregate wire fields are optional and normalized to explicit `null`; mapper, aggregate, queue, persistence, and reload coverage passes.                        |
| Immediate duration switch                       | PASS   | `play()` commits the selected Track duration with identity and zero current time; store and PlayerBar assertions pass before metadata replacement.              |
| Independent taxonomy failure and fallback chips | PASS   | Each taxonomy has independent state/retry, active URL values merge into visible rows and modal chips, and generation checks reject stale results.               |
| Raw Usage values                                | PASS   | URL and Track API parameters retain the raw value; `#` is added only for display.                                                                               |

## Focused Verification

- Backend: seven selected Track, Playlist, Album, repository query-count, and mutation-lock test classes passed with `BUILD SUCCESSFUL` in 53 seconds.
- Frontend: eight selected Vitest files passed, **144/144 tests**, in 16.33 seconds.
- No full suite, external call, browser acceptance, MySQL profiling, schema/data action, commit, or push was performed.

## Residual Risks

- H2 and query-count tests support fixed-query behavior, but no MySQL execution plan or production-size payload measurement was performed.
- jsdom tests do not replace real-browser media-event and network-failure verification.
- Existing malformed legacy membership orders receive no backfill; authorized reorder/remove paths normalize retained rows.
- The substantial shared dirty worktree requires hunk-scoped rollback and verification.
