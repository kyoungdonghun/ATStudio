---
version: 1.0
last_updated: 2026-08-13
project: ATS
owner: se
category: evidence-pack
status: active
related_wi: WI-20260809-ATS-037
dependencies:
  - path: WI-20260809-ATS-037-handoff.md
    reason: Approved scope, acceptance criteria, and output contract
  - path: ../../docs/design/usecase/sound-playlist.md
    reason: Current Playlist reorder use case
  - path: ../../docs/design/api-spec.md
    reason: Current zero-based active Track reorder contract
---

# Evidence Pack: WI-20260809-ATS-037

## Summary

- Corrected Playlist Drawer reorder payloads and optimistic Track state from
  one-based values to exact contiguous zero-based values, with bounded recovery
  to the last confirmed detail and one authoritative reload after rejection.

## Scope / DoD Check

- [x] Drag and touch reorders submit every visible Track ID once with orders
      `0..n-1`.
- [x] Optimistic Drawer order and submitted `trackOrder` values use the same
      zero-based mapping.
- [x] Invalid drag state and same-position drop issue no mutation request.
- [x] Reorder rejection restores the last confirmed detail immediately and
      performs one authoritative detail reload.
- [x] A successful reload replaces the optimistic state; a failed reload keeps
      the last confirmed state without retry or polling.
- [x] Dedicated Playlist Edit-page zero-based behavior and backend
      active/inactive membership implementation were not changed.
- [x] Main verified the full frontend suite and configured coverage thresholds,
      typecheck, ESLint, Prettier, production build, documentation validation,
      and final diff gates.

## Reference Documents

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Approval, bounded scope, and traceability |
| 0 | `docs/standards/development-standards.md` | TDD, regression evidence, and rollback |
| 0 | `docs/standards/documentation-standards.md` | Deliverable metadata and structure |
| 0 | `docs/standards/glossary.md` | Canonical Playlist, Track, and WI terms |
| 1 | `docs/standards/frontend-standards.md` | Current React and API client patterns |
| 1 | `docs/policies/quality-gates.md` | Focused and main-owned verification gates |
| 2 | `docs/design/usecase/sound-playlist.md:117-146` | Zero-based complete active Track reorder flow |
| 2 | `docs/design/api-spec.md:708-711` | Exact `0..n-1` payload and inactive membership contract |
| REQ | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent request |
| WI | `deliverables/agent/WI-20260809-ATS-024-evidence-pack.md:49-64` | Original Drawer mismatch evidence |
| WI | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:603,966` | Canonical root and bounded remediation portfolio |
| WI | `deliverables/agent/WI-20260809-ATS-037-handoff.md` | Approved implementation contract |

## Evidence Pointers

### Implementation

- `frontend/src/components/player/PlaylistDrawer.tsx` captures the last
  confirmed detail before an optimistic reorder and maps reordered Tracks with
  `trackOrder: i` in one O(n) pass.
- The same reordered array supplies both Drawer state and the one
  `reorderTracks` request, preventing state/payload index drift.
- On rejection, the Drawer restores the captured detail before one
  `fetchPlaylistDetail` call. Reload failure is caught locally, so there is no
  second mutation, retry loop, polling, or misleading optimistic order.
- `frontend/src/api/playlists.ts`, `PlaylistEditPage.tsx`, and all backend files
  remain unchanged.

### Focused Tests

- `frontend/src/components/player/playerComponents.test.tsx` uses zero-based
  fixtures and proves exact drag and touch payloads, optimistic rendered order,
  one mutation per completed gesture, invalid/same-position no-op, successful
  authoritative replacement, and failed-reload fallback.
- `frontend/src/api/domainApis.test.ts` now asserts the canonical zero-based
  reorder body at the domain API boundary.
- `frontend/src/pages/subscriber/PlaylistEditPage.test.tsx` remains unchanged
  and passes its existing exact zero-based reorder assertion.

### Current Documentation

- `docs/design/usecase/sound-playlist.md:134-146` and
  `docs/design/api-spec.md:708-711` already describe the verified zero-based
  contract. No WI-037 contract documentation edit was necessary.
- The pre-existing WI-036 changes in `docs/design/api-spec.md` were not altered.

## Red / Green Reproduction

1. Red command:
   `cd frontend; npm test -- --run src/components/player/playerComponents.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/api/domainApis.test.ts`
   - Before the Drawer implementation change: 3 files, 27 tests; 25 passed and
     2 failed.
   - Both failures received one-based `1,2` values where exact zero-based `0,1`
     values were expected for drag and touch reorder.
2. Final green command: same command.
   - 3 files passed; 28 tests passed; 0 failed; exit 0.

## Commands and Outputs

| Lane | Exact command | Result |
| --- | --- | --- |
| Final focused | `cd frontend; npm test -- --run src/components/player/playerComponents.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/api/domainApis.test.ts` | PASS: 3 files, 28 tests; exit 0 |
| Playlist adjacent, before final assertion | `cd frontend; npm test -- --run src/components/player/playerComponents.test.tsx src/components/playlist/AddToPlaylistModal.test.tsx src/pages/subscriber/PlaylistEditPage.test.tsx src/pages/subscriber/PlaylistListPage.test.tsx src/api/domainApis.test.ts` | PASS: 5 files, 37 tests; exit 0 |
| Main final frontend | `cd frontend; npm test -- --run` | PASS: 73 files, 833 tests |
| Main final coverage | `cd frontend; npm run test:coverage -- --run` | PASS: statements 88.61%, branches 79.73%, functions 88.16%, lines 90.84% |
| Main final typecheck | `cd frontend; npm run typecheck` | PASS |
| Main final ESLint | `cd frontend; npm run lint` | PASS |
| Main final Prettier | `cd frontend; npm run format` | PASS: Prettier check |
| Main final build | `cd frontend; npm run build` | PASS: 274 modules transformed |
| Main final documentation | `python .agents/skills/validate-docs/scripts/validate_docs.py` | PASS: 566 traceability IDs |
| Main final whitespace | `git diff --check` | PASS: exit 0; only the existing `security-policy.md` CRLF warning |

Main supplied and verified all final gate results after the worker's focused
implementation pass. The existing `security-policy.md` CRLF warning is outside
WI-037 and does not change the successful `git diff --check` exit status.

## Risks / Rollback

- Deterministic Vitest mocks prove request/state/recovery behavior without a
  live backend. No DB, external service, or browser fixture was used.
- Reorder recovery intentionally keeps the last confirmed detail if the
  authoritative reload also fails. User-facing failure messaging belongs to
  later Playlist mutation recovery work and is outside WI-037.
- Rollback: revert the Drawer mapping/recovery hunk, focused assertions, API
  contract assertion, handoff checkbox updates, and both WI-037 deliverables as
  one scoped patch. No data or schema rollback is required.

## Side Effects and Git Record

- No DB, external service, ignored secret, protected output artifact,
  dependency, backend API shape, schema, commit, stage, push, or branch state
  was accessed or changed.
- Existing WI-036 files and changes were not modified by WI-037.

## Follow-Up Chain

- Immediate next WI: `WI-20260809-ATS-045` for member load ownership,
  malformed IDs, and capacity state.
- Then `WI-20260809-ATS-046` may revisit Playlist Drawer destructive mutation
  recovery after WI-045.
- `WI-20260809-ATS-058` remains another handoff-blocked follow-up with separate
  semantics ownership.
- Main's final quality verification is complete, so WI-037 releases the listed
  follow-up chain for normal orchestration.

## Related Documents

- [WI-037 Handoff](WI-20260809-ATS-037-handoff.md)
- [WI-037 User Summary](../user/WI-20260809-ATS-037-summary.md)
- [Playlist Use Case](../../docs/design/usecase/sound-playlist.md)
- [API Specification](../../docs/design/api-spec.md)
