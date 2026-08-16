---
version: 1.0
last_updated: 2026-08-16
project: ATS
owner: se
category: evidence-pack
status: confirmed
wi_id: WI-20260809-ATS-059
req_id: REQ-20260809-ATS-001
dependencies:
  - path: WI-20260809-ATS-059-handoff.md
    reason: Approved remediation boundary
  - path: WI-20260809-ATS-059-qa-fe-r3-review.md
    reason: R3 findings addressed by this result
---

# WI-059 R3 Remediation Result

## Scope Result

- Removed direct Enter/Space command handlers from the owned native buttons.
  Their existing `onClick` paths are the single command path.
- Kept the subscriber Question title as a native link. It retains its `href`
  and native Enter behavior; no Space behavior was added.
- Kept Playlist Play behaviorless. The visual marker is now an aria-hidden
  `span`, and its full-card overlay has `pointer-events: none`, allowing card
  navigation and delete controls to receive pointer input.
- Preserved routes, request payloads, player command meaning, authorization,
  Korean source bytes, and external-effect boundaries.

## Exact Files Changed

- `frontend/src/components/album/AlbumCard.tsx`
- `frontend/src/components/album/AlbumCard.test.tsx`
- `frontend/src/components/track/TrackRow.tsx`
- `frontend/src/components/track/TrackRow.test.tsx`
- `frontend/src/pages/subscriber/PlaylistListPage.tsx`
- `frontend/src/pages/subscriber/PlaylistListPage.module.css`
- `frontend/src/pages/subscriber/PlaylistListPage.test.tsx`
- `frontend/src/pages/subscriber/QuestionListPage.tsx`
- `frontend/src/pages/subscriber/QuestionListPage.test.tsx`
- `frontend/src/pages/admin/QuestionManagePage.tsx`
- `frontend/src/pages/admin/QuestionManagePage.test.tsx`
- `deliverables/agent/WI-059-r3-remediation-result.md`

## Test Evidence

| Check | Result |
| --- | --- |
| `npm exec vitest -- run src/components/album/AlbumCard.test.tsx src/components/track/TrackRow.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/pages/subscriber/QuestionListPage.test.tsx src/pages/admin/QuestionManagePage.test.tsx` | PASS: 5 files, 18 tests |
| `npm run typecheck` | PASS |
| Targeted ESLint for the owned TS/TSX files | PASS |
| Targeted Prettier for the owned TS/TSX/CSS files | PASS |
| Targeted `git diff --check` | PASS |

The focused tests assert native button/link semantics and one existing click
command per owned action: Album card/like, Track play, Playlist card/delete/
create, subscriber Question title link, and admin Question title button.
No keyboard `fireEvent` assertion is used. JSDOM click tests do not prove the
browser's physical Enter/Space default activation or CSS hit testing.

## Remaining Native Browser Evidence

Native-browser confirmation of Enter/Space activation and Playlist pointer
hit testing remains owned by `WI-20260809-ATS-076`. The missing
`@testing-library/user-event` dependency was not added because dependency
changes are outside this WI. It does not block the source remediation or the
static and click-path test evidence recorded here.

## External Effects and Rollback

No browser, network, authentication, payment, mail, download, database, or
other external-effect action was executed.

Rollback is source-control reversion of the exact files listed above.
