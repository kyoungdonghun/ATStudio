---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: qa-fe
category: qa-fe-review
status: findings-open
wi_id: WI-20260809-ATS-059
req_id: REQ-20260809-ATS-001
dependencies:
  - path: WI-20260809-ATS-059-qa-fe-review.md
    reason: Prior P2 and P3 findings
  - path: WI-059-remediation-result.md
    reason: Claimed remediation coverage
---

# QA-FE Re-Review: WI-20260809-ATS-059 (R2)

## Review Boundary

- Read the WI-059 QA-FE review and remediation result only.
- Inspected only the current remediation diff for the Playlist Play overlay and
  the focused keyboard-activation test changes.
- Ran the focused local Vitest command recorded by the remediation result.
- Did not modify source or test code, inspect protected `output/` artifacts,
  stage, commit, or execute browser, API, player, authentication, payment,
  download, database, or other external effects.

## Verdict

**PARTIAL PASS - P2 is closed; P3 remains open.**

## Finding Closure

### P2 - Closed

The Playlist card's detail-route command is now the separate `cardLink`
button. The adjacent `Play` overlay button has no event handler in the
remediation diff. Therefore the Play overlay itself adds no route navigation,
player command, or API invocation. The prior route/action mutation is removed.

### P3 - Open

The focused test files add an `activateWithKeyboard` helper, but each helper
dispatches `keyDown` and `keyUp` and then directly dispatches
`fireEvent.click(..., { detail: 0 })`. The assertions therefore demonstrate
the synthetic click, not that `Enter` or `Space` activates the native control.
This affects the new Album card, Playlist card/delete, subscriber Question
title link, and ADMIN Question title command coverage.

The required keyboard-activation evidence is not independently demonstrated.
Replace the helper with an interaction that asserts the browser-equivalent
keyboard behavior without injecting a click, then retain the route/action
isolation assertions.

## Verification

| Command | Result |
| --- | --- |
| `npm exec vitest -- run src/components/album/AlbumCard.test.tsx src/components/track/TrackRow.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/QuestionListPage.test.tsx src/pages/admin/QuestionManagePage.test.tsx` | PASS: 5 files, 18 tests |
| `git diff --check -- frontend/src/pages/subscriber/PlaylistListPage.tsx frontend/src/pages/subscriber/PlaylistListPage.test.tsx frontend/src/components/album/AlbumCard.test.tsx frontend/src/pages/subscriber/QuestionListPage.test.tsx frontend/src/pages/admin/QuestionManagePage.test.tsx` | PASS: no output |

Native-browser keyboard acceptance remains owned by `WI-20260809-ATS-076`.
It does not substitute for the focused regression evidence required by this
WI.
