# WI-20260711-ATS-016 Summary

## Verdict

Release remains **BLOCKED** for security and payment readiness.

This adjudication retained **19 canonical P0/P1 rows** after de-duplication:

- **P0 confirmed:** ATS016-SEC-01, ATS016-SEC-02, ATS016-PAY-01
- **P1 confirmed:** ATS016-SEC-03, ATS016-SEC-04, ATS016-SEC-05, ATS016-SEC-06, ATS016-SEC-07, ATS016-SEC-08, ATS016-PAY-02, ATS016-PAY-03, ATS016-PAY-04, ATS016-PAY-05, ATS016-PAY-06, ATS016-PAY-07, ATS016-PAY-08
- **P1 conditional:** ATS016-SEC-09, ATS016-SEC-10, ATS016-SEC-11

Count basis: **16 CONFIRMED rows** (3 P0 plus 13 P1) and **3 CONDITIONAL rows** in the canonical evidence table. Of those, **15 rows are immediate release blockers**: ATS016-SEC-01 through ATS016-SEC-07 plus ATS016-PAY-01 through ATS016-PAY-08. ATS016-SEC-08 is retained as confirmed P1 but is not a standalone release blocker. ATS016-SEC-09 through ATS016-SEC-11 are conditional blockers requiring environment verification.

## Immediate Release Blockers

| Canonical ID | Status | Retained aliases | Blocker rationale |
|---|---|---|---|
| ATS016-SEC-01 | CONFIRMED P0 | PG-004-01, ATS008-01, BE-001, CR-A-009 | Public track APIs expose the original audio storage key and `/uploads/**` serves it outside subscriber download, quota, license, and ledger controls. |
| ATS016-SEC-02 | CONFIRMED P0 | PG-004-03, ATS008-02, BE-006 | SMTP failure logs full verification/reset email bodies, including live capability URLs and recipient PII. |
| ATS016-PAY-01 | CONFIRMED P0 | PAY-006-01, BE-002 | Account withdrawal does not stop active billing agreements, and renewal can still charge a withdrawn user's active agreement. |
| ATS016-SEC-03 | CONFIRMED P1 / CONDITIONAL P0 max impact | PG-004-02, ATS008-03 | Subscriber uploads can store unchecked active content under public uploads. Token-theft maximum impact requires same-origin executable delivery not proven by current repo. |
| ATS016-SEC-04 | CONFIRMED P1 | PG-004-04 | Company certification documents rely on extension/size/client content type before admin review. |
| ATS016-SEC-05 | CONFIRMED P1 | PG-004-05, ATS008-07 | Registration and identity availability endpoints are public and outside current auth rate-limit coverage. |
| ATS016-SEC-06 | CONFIRMED P1 | PG-004-07, ATS008-04 | Logout, password reset, and password change do not revoke existing refresh capability. |
| ATS016-SEC-07 | CONFIRMED P1 | PG-004-08, ATS008-06, BE-007 | File storage and DB transactions lack consistent rollback/commit compensation, causing orphan, stale, or broken file references. |
| ATS016-PAY-02 | CONFIRMED P1 | PAY-006-02, INT-005-01 | Payment settlement audit enum values exist in Java/service calls but not executable MySQL DDL. |
| ATS016-PAY-03 | CONFIRMED P1 | PAY-006-03, BE-003 | Billing confirm failure mutations can roll back with the thrown `BusinessException`. |
| ATS016-PAY-04 | CONFIRMED P1 | PAY-006-04, BE-004, INT-005-02 | Payment confirm/upgrade/renewal commands lack serialization/idempotency and can duplicate provider/local finalization. |
| ATS016-PAY-05 | CONFIRMED P1 | PAY-006-05 | Renewal can reuse stale failed orders across periods/subscriptions. |
| ATS016-PAY-06 | CONFIRMED P1 | PAY-006-06, BE-004, INT-005-04 | Renewal batch performs provider calls inside one transaction, risking rollback after external success. |
| ATS016-PAY-07 | CONFIRMED P1 | PAY-006-07, BE-005 | Refund request creation reads aggregate reserved amount without locking the source payment. |
| ATS016-PAY-08 | CONFIRMED P1 | PAY-006-08, FE-002 | ADMIN can enter member checkout paths and backend prepare lacks an explicit ADMIN rejection. |

## Retained Non-Standalone P1 Finding

| Canonical ID | Status | Retained aliases | Rationale |
|---|---|---|---|
| ATS016-SEC-08 | CONFIRMED P1 / not standalone release blocker | ATS008-05, FE-001 | Social callback can fail local session/profile completion after successful provider auth; retained as security/session correctness, but not a standalone release blocker. |

## Conditional Release Blockers

| Canonical ID | Status | Retained aliases | Conditional blocker rationale |
|---|---|---|---|
| ATS016-SEC-09 | CONDITIONAL P1 | PG-004-06 | Proxy/tunnel topology can turn rate limiting into shared auth lockout if clients collapse to one remote address. |
| ATS016-SEC-10 | CONDITIONAL P1 | PG-004-09 | Any environment that ever used the historical JWT fallback needs rotation/history cleanup. |
| ATS016-SEC-11 | CONDITIONAL P1 | PG-004-10 | Any environment with bootstrap test users enabled outside protected non-production must be blocked. |

## Conditional / Rejected P0-P1 Claims

| Claim | Adjudication | Reason |
|---|---|---|
| PG-004-02 as unconditional P0 same-origin token theft | CONDITIONAL | Unsafe upload/public serving is confirmed, but production same-origin executable delivery is not proven by current repository. |
| PG-004-06 global auth outage behind tunnel/proxy | CONDITIONAL; retained as ATS016-SEC-09 | Rate limit keying uses `request.getRemoteAddr()`, but current repo does not prove production proxy address collapse. |
| PG-004-09 current JWT fallback still accepted | REJECTED for current runtime; CONDITIONAL as ATS016-SEC-10 for historical exposure | Current `application.yml` requires `JWT_SECRET`; historical residue impact depends on whether an old fallback was deployed. |
| PG-004-10 bootstrap ADMIN credential currently exposed | CONDITIONAL; retained as ATS016-SEC-11 | Shared default and property-only enablement exist, but current committed default is disabled. |
| ADMIN satisfying USER routes as broad non-payment P1 | REJECTED | Broad bypass is not proven; retained only as payment checkout role-boundary defect ATS016-PAY-08. |
| Public stream original fallback as the paid-download bypass | REJECTED | Stream fallback is a documented policy behavior; the retained bypass is direct static original retrieval. |
| Unbounded pages/races/accessibility as P1 | REJECTED for P0/P1 | These remain P2 follow-ups unless runtime exhaustion or security impact is proven. |

## First-Wave Remediation Order

1. **Private paid media:** remove original audio keys from public DTOs, move originals outside static roots, and add denial tests for `/uploads/tracks/audio/**`.
2. **Mail/session safety:** redact email failure logging, stop logging bodies/recipients, add server-side refresh revocation for logout/password reset/password change.
3. **Stop billing on withdrawal:** define retention policy, cancel/disable local renewal state during withdrawal, and add a due-renewal regression test.
4. **Payment transaction/idempotency:** fix billing confirm failure persistence, add command locks/idempotency/unique finalization, and isolate renewal work per agreement.
5. **Upload and file lifecycle:** enforce image/document content validation, quarantine or reject mismatches, and introduce transaction-aware file cleanup/retry.
6. **Payment schema/refund/role gates:** repair payment operation audit DDL/migration, lock refund aggregate reservation, and reject ADMIN from member billing APIs and routes.

## Verification

No exploit, HTTP/provider/DB mutation, secret read, build, or test command was run in WI-016. Existing WI-009 backend regression evidence remains 745/745 JUnit tests passing, but WI-015 confirms coverage is not measurable with current repository tooling.

Final git-status verification is recorded in the agent Evidence Pack. The only files written by this WI-016 run were the two owned WI-016 output paths; final status also showed concurrent/unowned untracked WI-018 outputs that were not touched by this run.
