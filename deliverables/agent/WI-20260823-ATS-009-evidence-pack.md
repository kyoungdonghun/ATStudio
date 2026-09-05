# Evidence Pack: WI-20260823-ATS-009

## Summary

- Independently completed the full frontend quality gate and verified every
  required public media response for the exact ten current `AT.M Demo` Tracks.

## Scope / DoD Check

- [x] Full frontend Vitest suite passed.
- [x] Frontend typecheck, ESLint, and production build passed.
- [x] All ten exact scoped Tracks returned a full stream, byte-range stream,
  and thumbnail from the already-running local development runtime.
- [x] `git diff --check` completed without whitespace diagnostics.
- [x] No source, configuration, database, storage, client worktree, or
  process change was made by this WI.

## Reference Documents

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Scope and execution controls. |
| 0 | `docs/standards/development-standards.md` | Verification conventions. |
| 0 | `docs/standards/documentation-standards.md` | Deliverable format and language policy. |
| 0 | `docs/standards/glossary.md` | Canonical project terminology. |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence expectations. |
| 1 | `docs/policies/security-policy.md` | Secret-safe local-runtime verification. |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | React verification guidance. |
| 2 | `docs/standards/frontend-standards.md` | Active frontend quality conventions. |
| REQ | `deliverables/user/REQ-20260823-ATS-001.md` | Approved scope and acceptance-media gate. |
| WI | `deliverables/agent/WI-20260823-ATS-009-handoff.md` | DoD and output contract. |
| Prior WI | `deliverables/agent/WI-20260823-ATS-008-evidence-pack.md` | Exact Track ID, title, and storage-key baseline. |

## Evidence Pointers

- `frontend/package.json:6-15`: Defines the executed Vitest, typecheck,
  ESLint, and build scripts.
- `frontend/src/pages/public/HomePage.test.tsx:68-82`: The repaired
  whitespace-tolerant hero-copy assertion remained part of the passing full
  suite.
- `src/main/java/com/atstudio/atstudio/controller/TrackController.java`: The
  public `GET /api/tracks/{trackId}/stream` endpoint serves full and single
  byte-range representations; `TrackService.getStreamResource` reads the
  active public resource.
- `deliverables/agent/WI-20260823-ATS-008-evidence-pack.md`: Records the
  exact active fixture set and the pre-restoration storage-key baseline used
  for this independent fixed-ID/title/key verification.

## Commands And Results

| Command | Result |
| --- | --- |
| `npm run test` | PASS: 111 files, 1,447 tests, 0 failures; duration 57.29 s. |
| `npm run typecheck` | PASS: `tsc --noEmit` exited 0. |
| `npm run lint` | PASS: `eslint src --ext .ts,.tsx --max-warnings 0` exited 0. |
| `npm run build -- --outDir "$env:TEMP\\atstudio-wi009-build"` | PASS: `tsc -b` and Vite production build exited 0. Output was intentionally outside the workspace. |
| `git diff --check` | PASS: exit 0 with no whitespace diagnostics. Git emitted only existing CRLF-to-LF normalization warnings for unrelated dirty files. |

The Vitest output included jsdom's `Not implemented: navigation to another
Document` diagnostic, but all 111 files and all 1,447 tests passed. It is a
test-environment diagnostic, not a WI-009 quality-gate failure.

## Local Runtime Media Matrix

Verification used the already-running local backend at `127.0.0.1:8080`.
For each fixed expected ID, the read-only check first confirmed the exact title
and thumbnail key from `GET /api/tracks/{id}`, then requested the full stream,
`Range: bytes=0-1023` stream, and the returned thumbnail path. Every full
response was `audio/wav`; every thumbnail response was `image/jpeg`; every
partial response carried `Accept-Ranges: bytes` and the shown `Content-Range`.

| ID | Exact Track title | Full stream | Range stream | Content-Range | Thumbnail |
| ---: | --- | ---: | ---: | --- | ---: |
| 4 | AT.M Demo 01 - Dawn Signal | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 5 | AT.M Demo 02 - City Paper | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 6 | AT.M Demo 03 - Soft Circuit | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 7 | AT.M Demo 04 - Window Light | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 8 | AT.M Demo 05 - Slow Bloom | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 9 | AT.M Demo 06 - Night Marker | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 10 | AT.M Demo 07 - Open Road | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 11 | AT.M Demo 08 - Amber Room | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 12 | AT.M Demo 09 - Blue Outline | 200 | 206 | `bytes 0-1023/112044` | 200 |
| 13 | AT.M Demo 10 - Last Frame | 200 | 206 | `bytes 0-1023/112044` | 200 |

Result: 10/10 exact Tracks passed all three read-only media checks.

## Risks / Rollback

- Residual blocker within WI-009 scope: none.
- Residual condition outside WI-009 scope: the confirmed media files reside
  under Git-ignored local `uploads/` storage. This validates the current local
  development runtime only; production readiness still requires the target
  environment's separate media provisioning and deployment smoke test.
- Rollback: none. This WI made no product, runtime, storage, configuration,
  database, or process change. Remove only these two WI-009 deliverables if
  their documentation must be reverted.
