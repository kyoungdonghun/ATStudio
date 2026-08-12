[WI HEADER]
WI ID: WI-20260809-ATS-038
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-031
Blocks: WI-20260809-ATS-049, WI-20260809-ATS-070

[WI SUMMARY]
Why: `CR-031-055` proves that Album Edit constructs one-based reorder values while the backend validates every current album member exactly once with unique contiguous zero-based orders.
Scope (in/out):
- In: Submit exact zero-based contiguous Album reorder payloads from the existing up/down controls.
- In: Keep optimistic order, successful authoritative refetch, and rejected-request recovery aligned with the current backend contract.
- In: Add a dedicated Album Edit test suite or equivalent exact assertions proving payload, boundary no-op, pending ownership, success projection, and rejection recovery.
- In: Update current Album use-case/API documentation only when verified wording is stale.
- Out: Album search/modal/thumbnail/loading defects owned by WI-049, six-page test inventory owned by WI-070, upload/storage behavior, schema/data mutation, and UI redesign.
DoD:
- A valid move sends every visible album member once with `order` values `0..n-1`.
- Top-up and bottom-down boundary actions send no request.
- Successful reorder performs one authoritative refetch and displays canonical order.
- Rejection shows bounded existing error feedback and performs one authoritative refetch; no duplicate mutation is sent.
- Focused and adjacent tests, frontend full tests/coverage, typecheck, ESLint, Prettier check, build, docs validation, and diff check pass.
- Evidence Pack and Korean user summary record exact evidence, rollback, and follow-up chain.
Constraints/Forbidden:
- Do not inspect or alter ignored secrets or protected output artifacts.
- Do not access DB, external services, or a live backend; use deterministic mocks/fixtures only.
- Do not alter backend reorder semantics, schema, dependencies, Album workflow policy, or unrelated Album state.
- Keep changes to Album Edit reorder ownership, dedicated tests, directly stale docs, and WI deliverables.

[ACCEPTANCE CRITERIA]
Functional:
- [x] Moving a Track up or down sends all current Track IDs once with contiguous zero-based orders.
- [x] Invalid boundary moves issue no reorder request.
- [x] The UI uses optimistic order while pending and then adopts the authoritative refetched response.
- [x] A rejected reorder issues no retry, reports the existing bounded error, and performs one authoritative refetch.
- [x] Public Album detail order and backend exact-membership validation remain unchanged.
Performance:
- [x] Payload construction remains O(n) with one mutation and at most one refetch per gesture.
- [x] No polling, timer, or retry loop is introduced.
Quality:
- [x] Dedicated exact Album reorder tests pass and replace false-positive coverage.
- [x] Relevant Album API/public projection tests pass.
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
- docs/design/usecase/sound-album.md:205
- docs/design/api-spec.md

REQ/Context Docs:
- deliverables/user/REQ-20260809-ATS-001.md
- deliverables/agent/WI-20260809-ATS-025-evidence-pack.md
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:619
- deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:967

Files:
- frontend/src/pages/creator/AlbumEditPage.tsx
- frontend/src/test/coverage/publicAuthShell.coverage.test.tsx
- frontend/src/api/albums.ts
- frontend/src/api/domainApis.test.ts
- src/main/java/com/atstudio/atstudio/dto/album/AlbumTrackOrderItem.java
- src/main/java/com/atstudio/atstudio/service/AlbumService.java
- src/test/java/com/atstudio/atstudio/service/AlbumServiceTest.java

Repro/Logs:
- Use the existing Album Edit fixture plus exact `reorderAlbumTracks` assertions.
- `cd frontend; npm test -- --run <dedicated Album Edit test> src/api/domainApis.test.ts`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260809-ATS-038-summary.md:
- Korean summary, corrected behavior, validation, risks, and follow-up.
Agent-facing -> deliverables/agent/WI-20260809-ATS-038-evidence-pack.md:
- Evidence pointers, red/green reproduction, exact tests, patch notes, rollback, and next WI chain.
Handoff Packet -> deliverables/agent/WI-20260809-ATS-038-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Record focused and full frontend commands with exact counts/results.
Rollback: Revert the Album order mapping, dedicated assertions, directly stale docs, and WI deliverables as one scoped patch; no data rollback is required.
