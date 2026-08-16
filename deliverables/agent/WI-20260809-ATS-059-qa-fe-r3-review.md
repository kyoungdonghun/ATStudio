---
version: 1.0
last_updated: 2026-08-16
project: ATS
owner: qa-fe
category: qa-fe-review
status: findings-open
wi_id: WI-20260809-ATS-059
req_id: REQ-20260809-ATS-001
dependencies:
  - path: WI-20260809-ATS-059-qa-fe-r2-review.md
    reason: Prior P3 keyboard-test finding
  - path: WI-059-keyboard-remediation-result.md
    reason: Claimed R3 keyboard remediation
---

# QA-FE Re-Review: WI-20260809-ATS-059 (R3)

## Review Boundary

- Read the injected handoff, prior QA-FE reviews, keyboard remediation result,
  and required standards/policy before inspecting the WI-059 diff and tests.
- Inspected only the current WI-059 frontend source/test changes and ran focused
  local Vitest plus a path-scoped diff check.
- Did not modify source or tests, inspect protected `output/` artifacts, stage,
  commit, push, or execute browser, API, player, authentication, payment, mail,
  download, database, network, or other external effects.

## Verdict

**FAIL - open P1 and P3 findings.** The prior P2 direct-route mutation remains
closed, and image/heading changes pass. Playlist pointer behavior regressed,
and the R3 keyboard tests still do not prove native single activation.

## Findings

### P1 - Playlist overlays block pointer navigation and delete

- **Evidence:** The full-card navigation button is `position: absolute`,
  `inset: 0`, and `z-index: 1` in
  `frontend/src/pages/subscriber/PlaylistListPage.module.css:123-131`.
  `plPlayOverlay` is also full-card (`inset: 0`) but uses `z-index: 2` and has
  normal pointer handling at lines 234-242. It is rendered after both the
  card-link button and the delete layer at
  `frontend/src/pages/subscriber/PlaylistListPage.tsx:446-471`. The delete
  layer also uses `z-index: 2` at CSS lines 222-230, so the later full-card
  overlay paints above it.
- **Impact:** Pointer events land on the no-op play overlay/button rather than
  the detail-route button, and the same overlay covers the delete button. The
  existing route and nested delete action are unavailable to pointer users.
  The test targets the hidden semantic buttons directly at
  `PlaylistListPage.test.tsx:359-375`; JSDOM does not perform CSS hit testing,
  so the passing test cannot detect this regression.
- **Required remediation:** Make the decorative/no-op play overlay ignore
  pointer events and keep the delete control above non-interactive decoration,
  or restructure the layers so the card-link owns the intended pointer surface
  while delete remains independently operable. Do not add route or player
  behavior to the `Play` control without separately approved scope. Recheck
  pointer card navigation and delete isolation in native-browser acceptance.

### P3 - Keyboard tests do not exercise native activation or detect duplicates

- **Evidence:** Each new `activateWithKeyboard` helper only calls
  `fireEvent.keyDown` and `fireEvent.keyUp`: `AlbumCard.test.tsx:14-18`,
  `TrackRow.test.tsx:23-27`, `PlaylistListPage.test.tsx:70-74`,
  `QuestionListPage.test.tsx:50-54`, and
  `QuestionManagePage.test.tsx:52-56`. The keyboard cases do not append a
  synthetic `fireEvent.click`, which closes the narrow R2 complaint, but
  `fireEvent` also does not generate the browser's native activation click.
  Source controls retain `onClick` while custom key handlers invoke the same
  command directly, for example `AlbumCard.tsx:49-54`, `TrackRow.tsx:66-72`,
  `PlaylistListPage.tsx:446-461`, and `QuestionManagePage.tsx:300-309`.
- **Impact:** Callback counts show only the custom handler path. Navigation
  assertions check the final pathname, which remains identical if the same
  navigation runs twice, so they do not detect duplicate history entries.
  The create-card at `PlaylistListPage.tsx:475-479` has no focused keyboard
  activation case. R3 therefore does not prove the DoD requirement of one
  command/navigation per native Enter or Space activation.
- **Required remediation:** Prefer native semantics: remove custom Enter/Space
  handlers from native buttons and drive the existing `onClick`; let the
  anchor's native Enter activation drive its `onClick`/`href` behavior. Do not
  add non-native Space activation to the anchor unless it is an explicit
  requirement. Replace the helpers with keyboard-only `userEvent` interactions
  that model native activation without an appended `fireEvent.click`. Assert
  exactly one callback or history entry per key activation, including Album
  card/like, Track play, Playlist card/delete/create, subscriber Question link
  Enter, and admin Question button Enter/Space. For navigation, verify one Back
  operation returns to the source route or spy on the navigation command count.

## P0-P3 Closure

| Severity | Result | Assessment |
| --- | --- | --- |
| P0 | PASS | No P0 issue found in the reviewed scope. |
| P1 | OPEN | Playlist overlay blocks pointer route and delete ownership. |
| P2 | CLOSED | The Playlist `Play` button has no direct route, player, or API handler; the prior P2 mutation remains removed. |
| P3 | OPEN | Keyboard-only events exist, but native click and duplicate dispatch are not tested; create-card coverage is missing. |

## Acceptance Assessment

| Area | Result | Evidence |
| --- | --- | --- |
| Nested controls | PARTIAL | Album like, Playlist card/delete/play, and admin title/status controls are sibling controls rather than invalid nested interactive elements. Playlist CSS layering breaks pointer isolation. |
| Routes | FAIL | Destinations remain unchanged in source, but Playlist pointer navigation is blocked. Keyboard navigation tests do not detect duplicate history entries. |
| API | PASS | No reviewed API module, request shape, or invocation changed. |
| Player | PASS | `TrackRow` still delegates to the supplied `onPlay(track)` path; public detail player commands are unchanged. Playlist `Play` remains a no-op. |
| Image fallback | PASS | `CatalogImage` swaps a failed nonempty source for the bounded labelled music-note fallback; Album/Track list and public detail usage preserves meaningful labels. |
| Heading scope | PASS | Public Album and Track titles are `h1` elements at `AlbumDetailPage.tsx:173` and `TrackDetailPage.tsx:247`; no competing loaded-state `h1` was found. |

## Verification

| Command | Result |
| --- | --- |
| `npm exec vitest -- run src/components/album/AlbumCard.test.tsx src/components/track/TrackRow.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/QuestionListPage.test.tsx src/pages/admin/QuestionManagePage.test.tsx src/pages/public/AlbumDetailPage.test.tsx src/pages/public/TrackDetailPage.test.tsx` | PASS: 7 files, 30 tests |
| `git diff --check -- [reviewed WI-059 source/test paths]` | PASS: no output |

Native-browser keyboard and pointer evidence remains owned by
`WI-20260809-ATS-076`; it was not executed in this review.
