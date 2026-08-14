---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: ma
category: wi-remediation-handoff
status: active
dependencies:
  - path: WI-20260809-ATS-055-qa-integ-result.md
    reason: Independent QA-INTEG FAIL with one P2 and one P3
---

# Integration Remediation Handoff: WI-20260809-ATS-055

## Assignment

- **Agent:** `se`
- **Purpose:** close both QA-INTEG findings without changing held download
  policies or existing durable semantics.

## Required Corrections

### QA-INTEG-055-001 - Single/bulk cross-entry duplicate Track request

- Replace the Download History single-only claim with one ref-backed registry
  keyed by current `readKey` plus Track ID and shared by single and bulk paths.
- A single request must not start a Track currently claimed by bulk.
- A bulk run must skip a Track currently claimed by single or another current
  bulk invocation, while still processing distinct unclaimed Track IDs.
- Every acquired claim must release in owner-safe `finally` cleanup after
  success, failure, or cancellation. Route/unmount cleanup must retire the
  registry without allowing stale cleanup to release a newer owner.
- Preserve the existing single replacement behavior for a different Track,
  bulk controller/generation, selected-ID de-duplication, counts, and held
  completion/bulk-ceiling policy. A skipped in-flight identity is neither a new
  success nor a new failure; its existing owner remains responsible for UI
  feedback.
- Add deferred tests for:
  - single then selected bulk with the same plus a distinct Track;
  - single then confirmed-all with the same plus a distinct Track;
  - bulk then single for the same Track;
  - retry after settlement/cancellation where applicable.
- Assert exactly one request for the shared identity and normal progress for the
  distinct identity.

### QA-INTEG-055-002 - Actual AxiosHeaders branch untested

- Add a `normalizeBinaryDownload` test using the installed Axios
  `AxiosHeaders` class, RFC 5987 Content-Disposition, and Content-Type.
- Assert exact decoded filename/content type and retain the plain-record matrix.

## Write Scope

- `frontend/src/api/downloads.test.ts`
- `frontend/src/pages/subscriber/DownloadHistoryPage.tsx`
- `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx`

## Verification

- Focused RED/GREEN for these three files.
- Expanded prior 17-file scope and full frontend test.
- Typecheck, scoped ESLint/Prettier, diff check.

## Constraints

- Touch only the three files above.
- Do not alter backend, docs, PG/QA records, authorization, entitlement/history,
  bulk ceiling, byte-completion or route-lifetime policy, dependencies,
  schema/data, protected outputs, secrets, external effects, branches, commit,
  or push.
