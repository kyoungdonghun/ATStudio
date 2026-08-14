---
version: 1.0
last_updated: 2026-08-14
project: ATS
owner: pg
category: wi-security-review-result
status: completed
result: PASS
wi: WI-20260809-ATS-055
review_round: 2
---

# Privacy and Security R2 Result: WI-20260809-ATS-055

## Result

**PASS**. Both PG-owned P2 findings are closed. No open P0-P3 security or
privacy finding and no bounded remediation regression was identified.

## Finding Disposition

### PG-055-001 - CLOSED

- `frontend/src/api/downloads.ts:25` defines the Unicode `C` category guard.
  `frontend/src/api/downloads.ts:62-75` rejects the complete candidate name
  before filename-character normalization, so it does not strip a format
  control and retain the attacker-shaped remainder.
- RFC 5987 decoding occurs before this filename validation at
  `frontend/src/api/downloads.ts:78-99`. Rejected response names select the
  deterministic fallback at `frontend/src/api/downloads.ts:140-148`.
- `frontend/src/api/downloads.test.ts:69-85` samples all five Unicode `C`
  subcategories in fallback names. `frontend/src/api/downloads.test.ts:124-161`
  directly proves decoded right-to-left override and zero-width format values
  produce the exact safe fallback in both the normalized result and browser
  download attribute, with no attacker name or `.exe` suffix retained.

### PG-055-002 - CLOSED

- PlayerBar claims ref ownership before any await and releases only the same
  owner at `frontend/src/layouts/PlayerBar.tsx:189-217`. Its direct rapid
  activation and failure/success release test is
  `frontend/src/layouts/PlayerBar.test.tsx:385-413`.
- Track detail applies the same pre-await claim and guarded release at
  `frontend/src/pages/public/TrackDetailPage.tsx:102-136`. Its direct test is
  `frontend/src/pages/public/TrackDetailPage.test.tsx:277-300`.
- Download History single re-download claims `{readKey, trackID}` ownership
  before `downloadTrack` and compares the exact owner and controller in
  `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:253-306`. This blocks
  same-identity repetition while allowing a different identity to replace and
  cancel the old request; an old `finally` cannot clear the newer owner. The
  direct cancellation/success retry test is
  `frontend/src/pages/subscriber/DownloadHistoryPage.test.tsx:272-300`.

## Regression Review

- Question attachment access still executes `checkReadAccess` before PRIVATE
  Resource resolution at `QuestionService.java:154-163` and `:208-214`.
- Company Certification remains ADMIN-only at
  `CompanyCertificationController.java:98-108`; PRIVATE Resource resolution and
  `DOCUMENT_ACCESS_GRANTED` audit timing remain at
  `CompanyCertificationService.java:217-236`.
- Both private controllers still close the input stream and retain attachment,
  `application/octet-stream`, `no-store, private`, pragma, nosniff, sandbox CSP,
  and disabled Range headers at `QuestionController.java:88-116` and
  `CompanyCertificationController.java:98-123`.
- The audit remains proof of authorization and resource access, not completed
  byte delivery. Durable completion success, bulk ceiling, and route-lifetime
  policy remain held and were not decided by this remediation.

## Evidence Boundary

- The R2 handoff records remediation GREEN at 57/57 focused tests, 260/260
  expanded tests, and 1,361/1,361 full frontend tests, with typecheck, scoped
  ESLint, scoped Prettier, and diff check passing. This review inspected the
  direct negative assertions and current source but did not rerun those gates.
- The Unicode response tests exercise the two owned `Cf` attack forms while the
  implementation-wide `\p{C}` guard and fallback matrix cover the broader
  category. There is no separate direct different-Track stale-finally test;
  exact owner-object comparison provides the inspected control.
- No protected output, secret, private/user file, browser download, network,
  provider, database, or other external effect was accessed or executed.
