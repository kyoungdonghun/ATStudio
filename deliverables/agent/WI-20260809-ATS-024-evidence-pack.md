# Evidence Pack: WI-20260809-ATS-024

## Summary

- Completed the authenticated/member-route and shared-dialog audit as a documentation-only closeout.
- Row outcome: `PASS 1`, `FAIL 15`, `BLOCKED 1`, `N/A 0`.
- A `FAIL` row has at least one confirmed defect in a required state; it does not mean every core behavior failed.
- No product, test, configuration, database, fixture, archive, or Git state was changed.

## Scope and DoD Check

- [x] Classified `G-AUTH`, `G-SUB`, `G-QUESTION`, all listed `MEM` rows, and `SH-03`, `SH-04`, `SH-05`.
- [x] Separated browser/UI observations, source/API contract evidence, and canonical/durable-state boundaries.
- [x] Recorded the anonymous guard routes, return-target behavior, duplicate-toast result, responsive viewport checks, and dialog behavior.
- [x] Recorded exact source pointers for confirmed findings and the drawer test contract defect.
- [x] Preserved `output/client-demo-screenshots-20260716-140514.zip` and all existing audit evidence.
- [x] Did not execute authenticated live browser coverage or prohibited durable/external operations.

## Row Outcome

| Row group | Rows                                                                                                         | Result    |
| --------- | ------------------------------------------------------------------------------------------------------------ | --------- |
| Guards    | `G-AUTH`                                                                                                     | `PASS`    |
| Guards    | `G-SUB`                                                                                                      | `FAIL`    |
| Guards    | `G-QUESTION`                                                                                                 | `BLOCKED` |
| Member    | `MEM-01`, `MEM-02`, `MEM-03`, `MEM-05`, `MEM-06`, `MEM-07`, `MEM-08`, `MEM-09`, `MEM-15`, `MEM-16`, `MEM-17` | `FAIL`    |
| Shared    | `SH-03`, `SH-04`, `SH-05`                                                                                    | `FAIL`    |

Totals: `PASS 1`, `FAIL 15`, `BLOCKED 1`, `N/A 0`.

## Evidence Pointers

### Browser Guard and Toast Results

| Scenario     | Result    | Evidence                                                                                                                                                                                                                                                                                                                 |
| ------------ | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `G-AUTH`     | `PASS`    | `ProtectedRoute` preserved encoded `returnTo` for `/likes`, `/play-history`, `/licenses`, `/licenses/999`, `/questions`, `/questions/new`, and `/questions/999`; source `frontend/src/router/ProtectedRoute.tsx:42-56`. One warning was emitted.                                                                         |
| `G-SUB`      | `FAIL`    | `/playlists`, `/playlists/999`, `/playlists/999/edit`, and `/downloads` redirected to bare `/login`; source `frontend/src/router/SubscriberRoute.tsx:85-90`; screenshot `output/ui-ux-audit/20260809/WI-024/G-SUB_VM390_anonymous-login.png`. Fresh navigation and wait reproduced duplicate `SubscriberRoute` warnings. |
| `G-QUESTION` | `BLOCKED` | Authenticated and admin variants were not run because no authenticated QA browser session was available.                                                                                                                                                                                                                 |

### Shared Dialog and Viewport Results

| Scenario                   | Result | Evidence                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| -------------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `SH-03` HistoryModal       | `FAIL` | Escape closed, focus returned, and the mobile visual result was healthy; accessibility labels remain defective. Screenshot `output/ui-ux-audit/20260809/WI-024/SH-03_VM360_history-modal.png`. No `SH-03_VD1440` file is cited or assumed.                                                                                                                                                                                                                                                       |
| `SH-04` PlaylistDrawer     | `FAIL` | Audited at `1440x900`, `1024x768`, `390x844`, and `360x800`; visual layout was healthy, while reorder, dialog semantics, focus, destructive action, stale-response, and label defects were confirmed. Screenshots: `output/ui-ux-audit/20260809/WI-024/SH-04_VD1440_guest-drawer.png`, `output/ui-ux-audit/20260809/WI-024/SH-04_VT1024_guest-drawer.png`, `output/ui-ux-audit/20260809/WI-024/SH-04_VM390_guest-drawer.png`, `output/ui-ux-audit/20260809/WI-024/SH-04_VM360_guest-drawer.png`. |
| `SH-05` AddToPlaylistModal | `FAIL` | Loading returns no rendered status, load failure has no retry, and subscription-required close can be silent; source `frontend/src/components/playlist/AddToPlaylistModal.tsx:44-74,120-126`.                                                                                                                                                                                                                                                                                                    |

### Source and Contract Evidence

- Reorder: `PlaylistDrawer` maps orders as `i + 1` at `frontend/src/components/player/PlaylistDrawer.tsx:184-199`; backend validation requires the complete exact `0..n-1` set at `src/main/java/com/atstudio/atstudio/service/PlaylistService.java:334-363`; the drawer test asserts the wrong `1,2` payload at `frontend/src/components/player/playerComponents.test.tsx:223-237`; `PlaylistEditPage` is correct at `frontend/src/pages/subscriber/PlaylistEditPage.tsx:115-124`.
- Subscriber guard: render-time toast and bare login redirect are at `frontend/src/router/SubscriberRoute.tsx:85-90`; inactive subscription redirect is at `frontend/src/router/SubscriberRoute.tsx:132-137`; `ProtectedRoute` performs the guarded effect and return-target encoding at `frontend/src/router/ProtectedRoute.tsx:42-56`.
- Question owner delete: UI renders the owner action for every status at `frontend/src/pages/subscriber/QuestionDetailPage.tsx:239-246`; backend allows non-admin owners only for `OPEN` at `src/main/java/com/atstudio/atstudio/service/QuestionService.java:179-188`.
- Invalid IDs: playlist detail/edit return before fetch while loading starts true at `frontend/src/pages/subscriber/PlaylistDetailPage.tsx:50-62` and `frontend/src/pages/subscriber/PlaylistEditPage.tsx:53-71`; license detail has the same early return at `frontend/src/pages/subscriber/LicenseDetailPage.tsx:17-40`.
- Partial save: `PlaylistEditPage` calls metadata update and reorder separately at `frontend/src/pages/subscriber/PlaylistEditPage.tsx:97-132`.
- Guest origin: PlayerBar hardcodes `/login` for Like and Add-to-Playlist at `frontend/src/layouts/PlayerBar.tsx:652-678`.
- Attachment handling: Question detail invokes `downloadAttachment` without awaiting or pending/error handling at `frontend/src/pages/subscriber/QuestionDetailPage.tsx:162-175`.

## Findings Index

| ID             | Severity | Rows                                            | Finding                                                                                    |
| -------------- | -------- | ----------------------------------------------- | ------------------------------------------------------------------------------------------ |
| `F-UI-024-001` | `P1`     | `MEM-01`, `MEM-03`, `SH-04`                     | PlaylistDrawer sends one-based reorder orders; backend requires zero-based exact sequence. |
| `F-UI-024-002` | `P2`     | `G-SUB`                                         | SubscriberRoute loses return navigation and can duplicate render-time toasts.              |
| `F-UI-024-003` | `P2`     | `SH-04`                                         | PlaylistDrawer lacks complete dialog, keyboard, focus, and accessible-control semantics.   |
| `F-UI-024-004` | `P2`     | `MEM-01`, `MEM-02`, `MEM-03`, `SH-04`           | Drawer destructive actions bypass confirmation and swallow errors.                         |
| `F-UI-024-005` | `P2`     | `MEM-01`, `MEM-05`, `MEM-07`, `MEM-15`, `SH-04` | Stale-response races exist across seven member/shared loads.                               |
| `F-UI-024-006` | `P2`     | `MEM-16`, `MEM-17`                              | Question owner delete remains visible for non-OPEN status.                                 |
| `F-UI-024-007` | `P2`     | `MEM-01`, `MEM-02`, `MEM-03`                    | Playlist cards/create card are mouse-only; Play has no handler and bubbles.                |
| `F-UI-024-008` | `P2`     | `MEM-01`, `MEM-07`, `MEM-08`                    | Invalid/nonpositive IDs can leave three pages loading forever.                             |
| `F-UI-024-009` | `P2`     | `SH-05`, `MEM-01`, `MEM-03`                     | AddToPlaylistModal lacks visible loading, retry, and explicit expiry feedback.             |
| `F-UI-024-010` | `P2`     | `G-AUTH`, `MEM-05`, `SH-04`                     | Guest PlayerBar Like/Add actions hardcode `/login` and lose origin.                        |
| `F-UI-024-011` | `P2`     | `MEM-17`                                        | Question attachment download lacks await/catch/pending handling.                           |
| `F-UI-024-012` | `P2`     | `MEM-01`, `MEM-02`, `SH-04`                     | Playlist plan capacity can default silently or remain stale.                               |
| `F-UI-024-013` | `REVIEW` | `MEM-01`, `MEM-03`                              | Playlist metadata and reorder save calls can partially succeed.                            |
| `F-UI-024-014` | `P3`     | Multiple member/shared rows                     | English/symbol-only labels and inconsistent loading text remain.                           |
| `F-UI-024-015` | `P3`     | `MEM-01`, `MEM-02`                              | Playlist preview object URLs are not revoked.                                              |
| `F-UI-024-016` | `P2`     | Member/shared rows                              | Dedicated page test gaps exist; drawer reorder test protects the wrong payload.            |

Full finding detail is in `deliverables/agent/WI-20260809-ATS-024-findings.md`.

## UI, API, and Durable-State Separation

- UI: guard routing, duplicate warning, healthy HistoryModal/PlaylistDrawer viewport layouts, Escape/focus result, missing labels, immediate drawer mutations, silent errors, loading screens, and stale-response risks were observed or source-confirmed.
- API/source contract: backend reorder validation, question owner deletion policy, separate playlist save calls, and frontend request construction were checked through the cited source pointers.
- Canonical/durable state: no database, storage, provider, payment, mail, download, upload, secret, or durable mutation inspection was performed. The only browser-local effect was a refreshed Track 3 play-history timestamp; it was not a durable application mutation.

## Test and Build Results

- Targeted frontend result recorded from the audit: `7 files, 42 tests passed` covering `ProtectedRoute`, `SubscriberRoute`, `playerComponents`, `AddToPlaylistModal`, `PlaylistListPage`, `PlaylistEditPage`, and `DownloadHistoryPage`.
- Targeted backend result recorded from the audit: `BUILD SUCCESSFUL` for `PlaylistController/Service`, `LikeController/Service`, `LicenseController/Service`, `DownloadService`, and `QuestionController/Service`.
- These targeted frontend/backend results were audit inputs and were not rerun during documentation closeout.
- Documentation closeout validation is recorded below after execution.

## Restoration and Boundaries

- Browser end state: Home, no Track, panels closed, viewport reset.
- Authenticated live browser coverage was unavailable; this blocked authenticated/admin `G-QUESTION` variants.
- No durable mutations, download/upload, provider/mail/payment operation, secret inspection, or storage inspection occurred.
- The intentional archive `output/client-demo-screenshots-20260716-140514.zip` was not touched.

## Documentation Closeout Validation

- Targeted Prettier write and check for the four WI-024 documents: exit `0`; all matched files use Prettier code style.
- `git diff --check`: exit `0`.
- Documentation validator command: `python .agents/skills/validate-docs/scripts/validate_docs.py`.
- Validator result: exit `0`.
- Tier 0 validation passed, no broken internal links were found, `536` traceability IDs matched supported formats, all documents were listed in the index, and all validations passed overall.
