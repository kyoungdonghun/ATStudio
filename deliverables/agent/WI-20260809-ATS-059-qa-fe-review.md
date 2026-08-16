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
  - path: WI-20260809-ATS-059-handoff.md
    reason: Review scope and acceptance criteria
  - path: WI-20260809-ATS-059-implementation-result.md
    reason: Claimed implementation coverage
---

# QA-FE Review: WI-20260809-ATS-059

## Review Boundary

- Reviewed only the current unstaged WI-059 frontend source and test changes.
- Did not open, hash, modify, stage, or delete the protected `output/` artifacts.
- Did not execute browser, API, player, authentication, payment, download, database, or other external effects.

## Verdict

**FAIL - open P2 and P3 findings.** The six canonical findings are substantially
addressed, but the Playlist `Play` control changes an existing route/action
contract and the focused tests do not exercise keyboard activation as required.

## Findings

### P2 - Playlist `Play` overlay now navigates despite having no prior action

- **Evidence:** [PlaylistListPage.tsx](../../frontend/src/pages/subscriber/PlaylistListPage.tsx) (line 333) defines `handleCardClick` as detail-route navigation, and the newly changed `Play` control invokes it at [PlaylistListPage.tsx](../../frontend/src/pages/subscriber/PlaylistListPage.tsx) (line 444). In `HEAD`, that control had no `onClick` handler.
- **Impact:** A control labelled `Play` now performs route navigation. This changes a route/action behavior while WI-059 explicitly excludes changes to routing destinations and player command meanings. It also does not enter the existing player path.
- **Required correction:** Restore the previous non-routing behavior for this overlay, or define and implement actual playback through the established player contract in a separately approved scope. Do not make the `Play` label navigate to a detail route.

### P3 - Focused regression tests do not verify keyboard activation

- **Evidence:** The new tests activate the native controls only with `fireEvent.click` in [AlbumCard.test.tsx](../../frontend/src/components/album/AlbumCard.test.tsx) (line 17), [PlaylistListPage.test.tsx](../../frontend/src/pages/subscriber/PlaylistListPage.test.tsx) (line 343), and [QuestionListPage.test.tsx](../../frontend/src/pages/subscriber/QuestionListPage.test.tsx) (line 113). There is no focused ADMIN Question keyboard-activation coverage for the new button at [QuestionManagePage.tsx](../../frontend/src/pages/admin/QuestionManagePage.tsx) (line 286).
- **Impact:** Native controls are the correct primitive, but the handoff explicitly requires focused tests for keyboard activation. The implementation result's keyboard claim is therefore not independently demonstrated by the changed suite. Native-browser acceptance remains WI-076, but it does not replace this focused regression coverage.
- **Required correction:** Add focused `Enter` and `Space` activation tests for the native card/buttons and `Enter` activation coverage for the title link, including the ADMIN Question title command.

## Canonical Finding Assessment

| Finding      | Assessment | Evidence                                                                                                                                                                                                                                                                                   |
| ------------ | ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `CR-031-027` | PASS       | Album card uses a native full-card button; the like action is elevated above it and remains isolated. [AlbumCard.tsx](../../frontend/src/components/album/AlbumCard.tsx) (line 30)                                                                                                                |
| `CR-031-029` | PASS       | `TrackRow` renders the play button outside hover and playing-state selectors; it continues to call the supplied `onPlay` callback. [TrackRow.tsx](../../frontend/src/components/track/TrackRow.tsx) (line 49), [TrackRow.module.css](../../frontend/src/components/track/TrackRow.module.css) (line 84)  |
| `CR-031-044` | PASS       | Playlist navigation and create-card entry now use native buttons; delete remains above the card navigation layer. The separate P2 finding covers the changed `Play` overlay behavior. [PlaylistListPage.tsx](../../frontend/src/pages/subscriber/PlaylistListPage.tsx) (line 423)                 |
| `CR-031-124` | PASS       | Nonempty failed Track and Album images render the bounded labelled music-note fallback through `CatalogImage`. [CatalogImage.tsx](../../frontend/src/components/catalog/CatalogImage.tsx) (line 20)                                                                                               |
| `CR-031-126` | PASS       | Subscriber Question title is a native link isolated from the row click; ADMIN Question title is a native button. [QuestionListPage.tsx](../../frontend/src/pages/subscriber/QuestionListPage.tsx) (line 232), [QuestionManagePage.tsx](../../frontend/src/pages/admin/QuestionManagePage.tsx) (line 286) |
| `CR-031-130` | PASS       | Public Album and Track titles remain semantic `h1` elements. [AlbumDetailPage.tsx](../../frontend/src/pages/public/AlbumDetailPage.tsx) (line 173), [TrackDetailPage.tsx](../../frontend/src/pages/public/TrackDetailPage.tsx) (line 247)                                                                |

## Contract Review

- **API:** PASS. The reviewed diff does not change API modules, request payloads, or server calls.
- **Player:** FAIL. `TrackRow` retains its existing `onPlay` path, but the Playlist control labelled `Play` now routes to detail rather than preserving its prior behavior or calling a player command.
- **Routes:** FAIL. The Playlist `Play` overlay adds a detail-route invocation absent from `HEAD`; other reviewed card/title destinations remain unchanged.
- **Nested actions:** PASS for Album like, Playlist delete, and subscriber Question title isolation. The card-overlay z-index separation prevents the generic navigation button from receiving those pointer actions.

## Verification

| Command                                                                                                                                                                                                                                                                                   | Result                  |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------- |
| `npm exec vitest -- run src/components/album/AlbumCard.test.tsx src/components/track/TrackRow.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/QuestionListPage.test.tsx src/pages/public/AlbumDetailPage.test.tsx src/pages/public/TrackDetailPage.test.tsx` | PASS: 6 files, 28 tests |
| `git diff --check`                                                                                                                                                                                                                                                                        | PASS: no output         |

No source or test file was modified by this review. Native-browser keyboard
evidence remains owned by `WI-20260809-ATS-076`.
