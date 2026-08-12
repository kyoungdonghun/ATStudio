[WI HEADER]
WI ID: WI-20260809-ATS-025
REQ: REQ-20260809-ATS-001
Agent: qa-fe
Depends On: WI-20260809-ATS-020; completed WI-20260809-ATS-021~024 findings/evidence as cross-entry evidence
Blocks: WI-20260809-ATS-030

[WI SUMMARY]
Why: Audit the creator/admin catalog-management surfaces and their public projections against the approved acceptance matrix, with explicit evidence for guards, UI behavior, API invocation, server/provider response, and durable state boundaries.

Scope (in/out):

In:

- Execute and/or source-audit these matrix rows: `CRT-01 /admin/tracks/upload`, `CRT-02 /admin/tracks/:trackId/edit`, `CRT-03 /admin/albums`, `CRT-04 /admin/albums/new`, `CRT-05 /admin/albums/:albumId/edit`, `ADM-07 /admin/tags`, `ADM-08 /admin/track-manage`, `ADM-12 /admin/notices/new`, and `ADM-13 /admin/notices/:noticeId/edit`.
- Audit direct public projections for created or edited Track, Album, Tag, and Notice content, including public list/detail/playback/order/filter/download boundaries where the matrix assigns them.
- Audit shared file upload, file preservation/removal/addition, reorder, validation, error, confirmation, and dialog boundaries used by these workflows.
- Review the relevant React pages, API clients, router/guards, components, backend controllers/services/DTOs/tests, and referenced design/UI documents. Identify tests that encode an invalid or stale contract and cite the exact test and contract owner.

Out:

- No product code, test, configuration, schema, fixture, documentation baseline, or runtime implementation changes.
- No durable mutation, upload, delete, download, database, storage, provider, mail, payment, secret, or environment inspection without separate approval and an explicitly provided fixture/session.
- No real external side effect. Authenticated ADMIN browser coverage and all mutation paths may be marked `BLOCKED` when no authorized session or approved fixture exists.

DoD:

- Every in-scope matrix row has an outcome and evidence pointer, or an explicit `BLOCKED`/`NOT RUN` reason with the missing session, fixture, or approval named.
- Findings distinguish UI observation, frontend API invocation, backend/provider response, and canonical/durable state. No layer is inferred from another layer.
- All required state, role, viewport, keyboard/focus, recovery, race, and projection checks below are either evidenced or explicitly bounded.
- Findings are written to `deliverables/agent/WI-20260809-ATS-025-findings.md`; the evidence pack, user summary, and screenshot root follow the output contract below.
- Commands and results are recorded, the browser is restored to a neutral end state, the intentional demo ZIP is preserved, and no stage or commit operation is performed.

Constraints/Forbidden:

- Treat `WI-20260809-ATS-020-acceptance-matrix.md` as the executable test design. It records baseline commit `e343c20` on `codex/v1-release-rehearsal-fixes`; verify the actual current runtime before relying on browser results.
- Preserve `output/client-demo-screenshots-20260716-140514.zip` byte-for-byte. Do not delete, replace, inspect as a test fixture, or move it.
- Do not inspect ignored secret files or print JWT, database, provider, mail, storage, or environment values. Sanitize request and response evidence.
- Read-only anonymous browser checks may proceed. Do not invent authenticated ADMIN results; use `BLOCKED` when an authorized session or fixture is unavailable.
- Do not stage, commit, branch, reset, restart services, or modify unrelated untracked work.

[ACCEPTANCE CRITERIA]
Functional:

- [ ] For every in-scope admin route, verify anonymous and wrong-role `ADMIN` guard behavior, including the redirect destination, encoded/safe return target, and absence of an open redirect or sensitive route leakage.
- [ ] Cover loading, legitimate empty, populated, invalid or missing ID, denied/not-found, validation, API/server error, retry/recovery, stale response, rapid repeat/duplicate submit, keyboard/focus, and responsive behavior at `1440x900`, `1024x768`, `390x844`, and `360x800`.
- [ ] `CRT-01` and `CRT-02`: inspect Track audio, thumbnail, tag, title/metadata, validation, replacement/preservation, activation/inactivation, analysis metadata, multipart boundaries, multi-upload partial success/retry behavior, and public list/detail/playback projection. Confirm whether each behavior is UI-only, requested from an API, returned by the server/provider, or durable.
- [ ] `CRT-03`, `CRT-04`, and `CRT-05`: verify modal and route create/edit consistency; title, description, and thumbnail preservation and explicit clearing; add/remove/reorder exact API payload and sequencing contract; partial-save and retry behavior; and public Album list/detail order after reload or source-confirmed persistence.
- [ ] `ADM-07`: verify Tag taxonomy selection, canonicalization, global duplicate detection, server-race conflict handling, local modal error placement, input/filter preservation, and delete dependency behavior without claiming a delete occurred.
- [ ] `ADM-08`: verify Track search/filter/page state, loading and empty boundaries, image fallback, edit navigation, delete confirmation/dependency response, list refresh, and public active-track projection.
- [ ] `ADM-12` and `ADM-13`: verify Notice title/content/pin/attachments create/edit/delete contracts; attachment preserve/remove/add behavior; validation and retry; confirmation semantics; and public Notice detail/list/download projection. Download invocation remains blocked unless separately approved with a safe fixture.
- [ ] Verify the direct public projections for Track, Album, Tag, and Notice content from the relevant public routes and shared player/filter/detail surfaces. Report projection mismatch separately from admin save or API-request behavior.
- [ ] Review existing frontend and backend tests for false-positive contracts, including wrong request shapes, missing state transitions, one-based versus zero-based order, incomplete attachment semantics, or tests that assert UI text without proving the corresponding API/server/durable outcome.
- [ ] Record each browser or source/test observation with route, role/fixture, viewport, state, expected result, actual result, and evidence path. Do not collapse `PASS`, `FAIL`, `BLOCKED`, and `NOT RUN`.

Performance:

- [ ] At all four required viewports, confirm stable layout dimensions during loading, validation, error, modal, file, and table/list transitions; record overflow, clipping, occlusion, unexpected layout shift, or an unresponsive interaction.
- [ ] For rapid repeat, filter/page, upload, reorder, and route-departure checks, record request ordering and whether late responses overwrite current UI. No performance SLO is to be invented; report only observed timing/ordering and source-confirmed risk.

Quality:

- [ ] Findings contain source pointers for every confirmed contract or defect, including frontend owner, API method/path, backend controller/service/DTO owner, test owner, and relevant UI/design pointer where available.
- [ ] Evidence separates four lanes: UI observation; API invocation; provider/server response; and canonical/durable state. Mark a lane `NOT INSPECTED` or `BLOCKED` when its required approval or fixture is absent.
- [ ] The evidence pack contains reproducible commands/results, screenshot inventory, browser restoration result, limitations, and rollback instructions.
- [ ] The user summary identifies risks and approval points without implying production readiness or successful mutation where none was authorized.
- [ ] Targeted Prettier for the handoff and `git diff --check` pass after this packet is created; no stage or commit is performed.

[INPUT POINTERS]
Tier 0 (Constitution and standards - required):

- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - required for this audit):

- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Frontend, UI, API, and domain references):

- docs/standards/frontend-standards.md
- docs/ui/screen-flow.md
- docs/ui/atstudio-front-list.md
- docs/ui/modal-list.md
- docs/design/api-spec.md
- docs/design/usecase/sound-track.md
- docs/design/usecase/sound-album.md
- docs/design/usecase/sound-tag.md
- docs/design/usecase/user-notice.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:

- AGENTS.md
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md
- deliverables/agent/WI-20260809-ATS-021-findings.md
- deliverables/agent/WI-20260809-ATS-021-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-022-findings.md
- deliverables/agent/WI-20260809-ATS-022-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-023-findings.md
- deliverables/agent/WI-20260809-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-024-findings.md
- deliverables/agent/WI-20260809-ATS-024-evidence-pack.md
- .claude/config/workspace.json
- .claude/config/context-injection-rules.json

Files:

- frontend/src/router/index.tsx
- frontend/src/router/ProtectedRoute.tsx
- frontend/src/layouts/AdminLayout.tsx
- frontend/src/pages/creator/TrackUploadPage.tsx
- frontend/src/pages/creator/TrackEditPage.tsx
- frontend/src/pages/creator/AlbumManagePage.tsx
- frontend/src/pages/creator/AlbumCreatePage.tsx
- frontend/src/pages/creator/AlbumEditPage.tsx
- frontend/src/pages/admin/TagManagePage.tsx
- frontend/src/pages/admin/TrackManagePage.tsx
- frontend/src/pages/admin/NoticeCreatePage.tsx
- frontend/src/pages/admin/NoticeEditPage.tsx
- frontend/src/pages/public/TrackListPage.tsx
- frontend/src/pages/public/TrackDetailPage.tsx
- frontend/src/pages/public/AlbumListImagePage.tsx
- frontend/src/pages/public/AlbumListPage.tsx
- frontend/src/pages/public/AlbumDetailPage.tsx
- frontend/src/pages/public/NoticeListPage.tsx
- frontend/src/pages/public/NoticeDetailPage.tsx
- frontend/src/api/tracks.ts
- frontend/src/api/albums.ts
- frontend/src/api/tags.ts
- frontend/src/api/notices.ts
- frontend/src/api/admin.ts
- frontend/src/api/client.ts
- frontend/src/components/track/
- frontend/src/components/album/
- frontend/src/components/filter/TagFilterModal.tsx
- frontend/src/components/ui/Tag.tsx
- frontend/src/pages/creator/\*.{test.ts,test.tsx}
- frontend/src/pages/admin/\*.{test.ts,test.tsx}
- frontend/src/pages/public/\*.{test.ts,test.tsx}
- frontend/src/router/\*.{test.ts,test.tsx}
- src/main/java/com/atstudio/atstudio/controller/TrackController.java
- src/main/java/com/atstudio/atstudio/controller/AlbumController.java
- src/main/java/com/atstudio/atstudio/controller/TagController.java
- src/main/java/com/atstudio/atstudio/controller/NoticeController.java
- src/main/java/com/atstudio/atstudio/service/TrackService.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/main/java/com/atstudio/atstudio/service/TagService.java
- src/main/java/com/atstudio/atstudio/service/NoticeService.java
- src/main/java/com/atstudio/atstudio/dto/track/
- src/main/java/com/atstudio/atstudio/dto/album/
- src/main/java/com/atstudio/atstudio/dto/tag/
- src/main/java/com/atstudio/atstudio/dto/notice/
- src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/TagControllerTest.java
- src/test/java/com/atstudio/atstudio/controller/NoticeControllerTest.java
- src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java
- src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java
- src/test/java/com/atstudio/atstudio/service/TagServiceTest.java
- src/test/java/com/atstudio/atstudio/service/NoticeServiceTest.java
- src/main/resources/schema.sql

Repro/Logs:

- `git status --short` before and after the audit; confirm no stage/commit and confirm the demo ZIP remains present and unchanged.
- Browser navigation, viewport, screenshot, DOM/accessibility, console, and sanitized network capture commands used for each row; record exit/status and evidence path in the evidence pack.
- Targeted frontend test, backend test, typecheck, lint, documentation validation, and Prettier commands only when run; record exact command, result, duration/counts, and skipped or blocked reason.
- `git diff --check` after the handoff is written.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-025-summary.md:

- Scope and row outcomes, direct public projection results, confirmed findings, blocked coverage, risks, approval points, and browser end-state restoration.

Agent-facing -> deliverables/agent/WI-20260809-ATS-025-evidence-pack.md:

- Evidence index, source/API/server/durable-state separation, screenshots, sanitized commands/results, test and documentation checks, limitations, cross-entry references to WI-021~024, and rollback.

Findings -> deliverables/agent/WI-20260809-ATS-025-findings.md:

- One finding per independent cause/contract, with severity, affected row, reproduction or source evidence, UI/API/server/durable-state classification, impact, and follow-up recommendation. Preserve `BLOCKED` and `NOT INSPECTED` distinctions.

Screenshot root -> output/ui-ux-audit/20260809/WI-025/

- Use stable names containing row/scenario, viewport, and outcome. Do not overwrite the preserved demo ZIP or prior WI evidence.

Handoff Packet -> deliverables/agent/WI-20260809-ATS-025-handoff.md:

- This packet only.

[TRACEABILITY REQUIREMENTS]
Evidence pointers:

- Link each row and finding to the acceptance-matrix row, route or source file/line, relevant WI-021~024 finding/evidence, and screenshot or log path when available.
- For each mutation-shaped workflow, record four independent statements: what the UI displayed or enabled; what request was invoked; what server/provider response was returned; and what canonical/durable state was observed or remained uninspected.
- For file and reorder behavior, record exact accepted file/type/order constraints and the request payload or source validator. Never infer persisted order or file retention from a successful toast alone.
- For public projections, verify the rendered list/detail/playback/filter/order/download surface separately from the admin form result. If reload, database, storage, or download proof is unavailable, mark the persistence/projection claim accordingly.
- Cite tests that codify an invalid contract as a finding or review note, with the expected contract owner and the reason the test is invalid.

Tests and commands:

- Record exact commands, working directory, exit code, duration, test counts, warnings, and relevant output excerpts. Do not report a command as passed if it was not run in this WI.
- Browser checks may use the anonymous session. Authenticated ADMIN and mutation coverage must be `BLOCKED` unless a separate approved session/fixture is available.
- Restore the browser to the agreed neutral end state: public home or other read-only route, dialogs and player overlays closed, no active form or upload, viewport reset, and no secret/token in screenshots or logs. Record the restoration result.

Rollback:

- This is a documentation-only audit handoff and does not require product/runtime rollback.
- If explicitly approved, remove only untracked WI-025 evidence files and captures, including `deliverables/agent/WI-20260809-ATS-025-*`, `deliverables/user/WI-20260809-ATS-025-summary.md`, and `output/ui-ux-audit/20260809/WI-025/`.
- Do not remove or alter `output/client-demo-screenshots-20260716-140514.zip`, prior WI evidence, product files, runtime data, database state, storage, provider state, mail, payment state, secrets, or environment configuration.
