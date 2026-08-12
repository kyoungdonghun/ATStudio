# Handoff Packet: WI-20260809-ATS-024

## WI Header

WI ID: WI-20260809-ATS-024
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-023
Blocks: WI-20260809-ATS-025

## WI Summary

Why: Audit the authenticated member/subscriber and question paths, plus the shared history and playlist dialogs, against the approved UI, browser, source, and API contracts.

Scope (in/out):

- In: G-AUTH, G-SUB, and G-QUESTION as applicable; MEM-01, MEM-02, MEM-03, MEM-05, MEM-06, MEM-07, MEM-08, MEM-09, MEM-15, MEM-16, MEM-17; SH-03 HistoryModal, SH-04 PlaylistDrawer, and SH-05 AddToPlaylistModal.
- Audit anonymous guards first. Audit authenticated USER and subscriber paths only when an approved login fixture is present; record fixture absence as an evidence boundary, not a product failure.
- Verify 1440x900, 1024x768, 390x844, and 360x800; keyboard and accessibility behavior; loading, empty, error, missing, ownership, subscription, and entitlement guards; latest-request-wins behavior; the shared PlayableTrack projection; and dialog reset/focus behavior.
- Out: MEM-04, already audited in WI-022; payment/subscription flows assigned to WI-027; whitelist/company certification assigned to WI-026; creator/admin pages assigned to WI-025/028/029; and global cross-entry retest assigned to WI-030.

Definition of Done:

- Each applicable matrix row is classified PASS, FAIL, BLOCKED, or N/A with a short reason.
- Anonymous guard behavior is checked before authenticated flows, and safe internal return navigation is recorded where applicable.
- Browser observations are correlated with source, request/response, and canonical API evidence where available. No claim is upgraded from BLOCKED merely because a fixture is absent.
- Findings are written with severity, classification, exact file/line or route evidence, observed result, expected contract, reproducibility, and bounded follow-up.
- Required outputs are produced using the output contract below, with sanitized evidence and no preserved archive altered.

## Constraints / Forbidden

- Read-only audit. No destructive action, database or test-data mutation, upload, download, test-data creation/deletion, provider/mail/payment operation, secret inspection, or browser storage inspection without a separate explicit approval and approved fixture.
- Browser-local reversible history/player/modal state is allowed only for the audit and must be restored before completion. Do not expose tokens, credentials, storage values, secrets, or private file paths.
- Do not treat fixture absence as a product defect. Mark the exact scenario BLOCKED or N/A and state the missing fixture.
- Preserve all output archives, including the intentional demo ZIP. Do not delete, overwrite, or move unrelated files.
- Carry forward relevant WI-023 items: `B-UI-023-001` frozen fixture/effect boundary, `F-UI-023-009` stale-response risk where the next route/page request can overwrite newer state, `F-UI-023-010` stale playback context where shared Player state is involved, and `R-UI-023-001` persisted progress as a static risk. Keep WI-023 catalog-specific findings in scope only when they directly affect this WI's shared Player/PlayableTrack behavior.
- Do not audit or report payment, whitelist/company certification, creator/admin, MEM-04, or global cross-entry behavior as this WI's result.

## Acceptance Criteria

Functional:

- [ ] Verify G-AUTH, G-SUB, and G-QUESTION anonymous routing and safe internal return behavior before authenticated checks; verify USER/subscriber/ownership/entitlement variants only with approved fixtures.
- [ ] Exercise MEM-01/02/03 playlist list, detail, edit, create/delete/remove/reorder/play paths as applicable, including plan limits, owner/non-owner/missing records, stale-request fencing, optimistic reorder reconciliation, and dialog reset.
- [ ] Exercise MEM-05 likes, MEM-06 local history hydration and cleanup, MEM-07/08 license list/detail, and MEM-09 download history/count/replay paths as applicable, without download or storage inspection unless separately approved.
- [ ] Exercise MEM-15/16/17 question list/create/detail paths as applicable, including latest-request-wins, validation, rejection preserving input, ownership/status guards, confirmation, and attachment boundary without downloading private files.
- [ ] Audit SH-03, SH-04, and SH-05 for empty/error/loading states, duplicate or one-request mutations, close/reopen reset, focus restoration, keyboard alternatives, and consistent PlayableTrack projection.
- [ ] Verify responsive layout and non-overlap at 1440x900, 1024x768, 390x844, and 360x800, including dialog and fixed Player surfaces.

Performance:

- [ ] No new performance target is introduced. Record observable request duplication, stale updates, unbounded loading, or UI lockup as a quality finding with reproduction evidence.

Quality:

- [ ] Evidence is sanitized and separates UI, console/network, API response, and persisted/canonical state claims.
- [ ] Run applicable frontend tests and report exact commands/results; use existing tests as contract references and add no files in this audit WI.
- [ ] Complete keyboard/a11y checks: names/labels, focus visibility/order, Escape, focus containment/restoration, status/error announcement, and no keyboard-only dead end.
- [ ] Produce findings, an agent-facing evidence pack, and a user-facing WI-024 summary.

## Input Pointers

Tier 0 (Constitution and standards - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies):

- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (React/UI and domain context):

- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md
- docs/ui/index.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/design/usecase/sound-playlist.md
- docs/design/usecase/likes.md
- docs/design/usecase/sound-playhistory.md
- docs/design/usecase/user-license.md
- docs/design/usecase/download-queue.md
- docs/design/usecase/user-question.md
- docs/design/usecase/user-subscription.md
- docs/design/api-spec.md

REQ/Matrix and prior WI evidence:

- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md
- deliverables/agent/WI-20260809-ATS-023-handoff.md
- deliverables/agent/WI-20260809-ATS-023-findings.md
- deliverables/agent/WI-20260809-ATS-023-evidence-pack.md

Frontend pages, components, APIs, stores, router, and tests:

- frontend/src/router/ProtectedRoute.tsx
- frontend/src/router/SubscriberRoute.tsx
- frontend/src/router/index.tsx
- frontend/src/store/authStore.ts
- frontend/src/store/playerStore.ts
- frontend/src/store/likeStore.ts
- frontend/src/store/albumLikeStore.ts
- frontend/src/api/playlists.ts
- frontend/src/api/likes.ts
- frontend/src/api/licenses.ts
- frontend/src/api/downloads.ts
- frontend/src/api/questions.ts
- frontend/src/api/tracks.ts
- frontend/src/components/player/HistoryModal.tsx
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/playlist/AddToPlaylistModal.tsx
- frontend/src/pages/subscriber/PlaylistListPage.tsx
- frontend/src/pages/subscriber/PlaylistDetailPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/LikeListPage.tsx
- frontend/src/pages/subscriber/PlayHistoryPage.tsx
- frontend/src/pages/subscriber/LicenseListPage.tsx
- frontend/src/pages/subscriber/LicenseDetailPage.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.tsx
- frontend/src/pages/subscriber/QuestionListPage.tsx
- frontend/src/pages/subscriber/QuestionCreatePage.tsx
- frontend/src/pages/subscriber/QuestionDetailPage.tsx
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/components/playlist/AddToPlaylistModal.test.tsx
- frontend/src/pages/subscriber/PlaylistListPage.test.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.test.tsx
- frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx
- frontend/src/store/playerStore.test.ts
- frontend/src/store/playerPersistence.test.ts

Repro/Logs:

- Record route, fixture alias, viewport, browser, sanitized request/response status, console result, and screenshot/log path in the evidence pack.
- Use the approved browser session/fixture supplied for the WI; do not create credentials or fixtures.
- Restore browser-local history/player/modal state and record the restored end state.

## Output Contract

User-facing -> deliverables/user/WI-20260809-ATS-024-summary.md:

- Scope/result summary, PASS/FAIL/BLOCKED/N/A counts, key findings, risks, fixture boundaries, and approval points.

Agent-facing -> deliverables/agent/WI-20260809-ATS-024-evidence-pack.md:

- Findings and severity, evidence pointers, UI/source/API/canonical-state separation, sanitized repro steps, screenshots/logs, test commands/results, fixture limitations, rollback/restoration state, and follow-up WI traceability.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-024-handoff.md:

- This packet for traceability.

## Traceability Requirements

- Map every finding and test case to the applicable WI-020 matrix row, state pack (`L`, `G`, `C`, `F`, `M`, `X`, `P`, `B`, `R`, `K`, `V`), fixture alias, viewport, and evidence pointer.
- For authenticated or subscription-dependent scenarios, record the approved fixture alias and sanitized user identifier only. For unavailable fixtures, record BLOCKED/N/A with the exact missing prerequisite.
- For every mutation-like control observed, separate confirmation copy, request invocation, provider/external boundary, and persisted/canonical result; this WI must stop before any prohibited external or durable mutation.
- Include tests where applicable: `cd frontend; npm run test -- --run`, `npm run typecheck`, `npm run lint`, `npm run format:check`, and `npm run build` if available in the package scripts. Report unavailable scripts rather than inventing results.
- Rollback/restoration: revert only browser-local reversible audit state to its pre-audit condition; do not revert repository files or alter archives. No code patch is authorized by this WI.
