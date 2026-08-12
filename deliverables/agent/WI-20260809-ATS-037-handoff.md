[WI HEADER]
WI ID: WI-20260809-ATS-037
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-045, WI-20260809-ATS-046, WI-20260809-ATS-058

[WI SUMMARY]
Why: `CR-031-039` proves that Playlist Drawer drag-and-drop constructs one-based `trackOrder` values while the backend contract requires every visible active Track exactly once with unique contiguous zero-based orders.
Scope (in/out):
- In: Send exact zero-based contiguous Drawer reorder payloads and keep the optimistic local order consistent with the canonical API contract.
- In: On reorder rejection, restore authoritative detail without leaving a misleading optimistic order; preserve drag, touch, selection, playback, active/inactive membership, and dedicated edit-page behavior.
- In: Correct focused tests so they assert the actual backend contract rather than the prior one-based defect; add rejection/reload proof where absent.
- In: Update current Playlist contract documentation only if implementation verification exposes stale wording.
- Out: Playlist metadata/reorder atomicity policy (`CR-031-050`), delete confirmation, broader Drawer accessibility, add-to-playlist lifecycle, schema/data changes, and unrelated Playlist/Player refactoring.
DoD:
- Drawer emits `0..n-1` exactly once for all visible active playlist tracks after drag/drop or touch reorder.
- Optimistic state uses the same zero-based order values sent to the backend.
- A rejected reorder reloads and presents the authoritative playlist detail without a second mutation.
- Focused tests prove exact payload, optimistic ordering, rejection recovery, and unchanged no-op behavior.
- Relevant frontend tests, typecheck, ESLint, Prettier check, build, docs validation, and diff check pass.
- Evidence Pack and Korean user summary capture exact evidence, rollback, and follow-up chain.
Constraints/Forbidden:
- Do not inspect or alter ignored secrets or protected output artifacts.
- Do not mutate DB data or call a live backend; browser/API evidence must use controlled mocks/fixtures only.
- Do not decide or implement metadata-plus-reorder atomicity, delete behavior, accessibility redesign, or product policy.
- Do not change backend reorder semantics, API shape, dependencies, or schema.
- Keep the patch to Playlist Drawer reorder ownership, focused tests, directly stale docs, and WI deliverables.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Reordering two or more visible tracks sends every track ID once with contiguous `trackOrder` values starting at zero.
- [x] The optimistic selected-playlist state matches the submitted zero-based order.
- [x] Dropping on the original position and invalid drag state produce no reorder request.
- [x] Reorder rejection performs one authoritative detail reload and replaces the optimistic state with that response.
- [x] Existing Playlist Edit-page reorder and backend active/inactive membership contracts remain unchanged.
Performance:
- [x] Reorder construction remains O(n) and creates one API request per completed reorder gesture.
- [x] Recovery performs at most one detail reload per failed request; no polling or retry loop is added.
Quality:
- [x] Focused Playlist Drawer tests pass with exact zero-based assertions.
- [x] Relevant Playlist API/component/page tests pass.
- [x] Frontend full tests and configured coverage thresholds pass.
- [x] Typecheck, ESLint, Prettier check, and production build pass.
- [x] Documentation validation and `git diff --check` pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/frontend-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

Tier 2 (Current contracts):
- docs/design/usecase/sound-playlist.md:123
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-024-evidence-pack.md:55
- deliverables/agent/WI-20260809-ATS-024-evidence-pack.md:63
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:603
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:966

Files:
- frontend/src/components/player/PlaylistDrawer.tsx
- frontend/src/components/player/playerComponents.test.tsx
- frontend/src/api/playlists.ts
- frontend/src/api/domainApis.test.ts
- frontend/src/pages/subscriber/PlaylistEditPage.tsx
- frontend/src/pages/subscriber/PlaylistEditPage.test.tsx
- src/main/java/com/atstudio/atstudio/dto/playlist/PlaylistTrackOrderItem.java
- src/main/java/com/atstudio/atstudio/service/PlaylistService.java
- src/test/java/com/atstudio/atstudio/service/PlaylistServiceTest.java

Repro/Logs:
- `cd frontend; npm test -- --run src/components/player/playerComponents.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/api/domainApis.test.ts`
- Use mocked API adapters and deterministic playlist fixtures only.

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-037-summary.md:
- Korean summary, corrected behavior, validation, risks, and follow-up.
Agent-facing -> deliverables/agent/WI-20260809-ATS-037-evidence-pack.md:
- Evidence pointers, red/green reproduction, exact tests, patch notes, rollback, and next WI chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-037-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record focused and full frontend commands with exact counts/results.
Rollback: Revert the Drawer order mapping, focused assertions, directly stale docs, and WI deliverables as one scoped patch; no data rollback is required.
