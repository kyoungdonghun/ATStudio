---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-055-pg-result.md
    reason: Independent PG FAIL with two open P2 findings
---

# Remediation Handoff: WI-20260809-ATS-055

## Assignment

- **Agent:** `se`
- **Purpose:** close both independent PG P2 findings without expanding policy or
  implementation scope.

## Required Corrections

### PG-055-001 - Unicode format-control filename spoofing

- Reject response or fallback filenames containing any Unicode `C` category
  character before normalization, including `Cf` bidi/zero-width controls.
- Do not silently strip a bidi control and retain the attacker-shaped rest of
  the filename. A response filename containing such a character must fall back
  to the deterministic safe filename.
- Add tests for decoded RFC 5987 right-to-left override and zero-width-format
  values. Assert exact safe fallback and no attacker extension/name reaches the
  browser action.

### PG-055-002 - Retained Track entry points lack synchronous fences

- Add a ref-backed synchronous in-flight ownership fence to:
  - `PlayerBar` download;
  - public Track detail download;
  - Download History single re-download.
- The ref must be claimed before the first awaited operation and released in a
  guarded `finally`, while preserving current visible pending state,
  cancellation/generation ownership, and existing bulk separation.
- Same-render rapid activation of the same Track action must call
  `downloadTrack` once. After success or failure settles, a later activation may
  call it again.
- Add direct tests for all three surfaces. Do not rely only on disabled DOM state.

## Write Scope

- `frontend/src/api/downloads.ts`
- `frontend/src/api/downloads.test.ts`
- `frontend/src/layouts/PlayerBar.tsx`
- `frontend/src/layouts/PlayerBar.test.tsx` or the existing focused coverage
  file that mounts and directly activates PlayerBar.
- `frontend/src/pages/public/TrackDetailPage.tsx`
- Its existing focused test file.
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx`

Expand only to the direct existing test owner when coverage is located in a
different file, and list it explicitly.

## Verification

- Run focused RED/GREEN tests for both findings.
- Run the prior 15-file frontend scope plus any new direct test owner.
- Run full frontend test, typecheck, scoped ESLint/Prettier, and diff check.

## Constraints

- Do not alter backend, docs, initial PG result, authorization, entitlement,
  bulk, route-lifetime policy, dependencies, schema/data, protected output,
  secrets, external effects, branches, commit, or push.
