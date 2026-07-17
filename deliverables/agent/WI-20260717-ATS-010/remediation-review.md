---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa-integ / cr
category: remediation-verification
status: passed
dependencies:
  - ../WI-20260717-ATS-010-handoff.md
  - ../WI-20260717-ATS-008/backend-qa.md
  - ../WI-20260717-ATS-008/frontend-qa.md
  - ../WI-20260717-ATS-008/integration-review.md
  - ../WI-20260717-ATS-009/backend-remediation.md
  - ../WI-20260717-ATS-009/frontend-remediation.md
---

# WI-20260717-ATS-010 Remediation Review

> Purpose: Independently verify the WI-009 backend and frontend remediation,
> test quality, generated test/coverage evidence, and closure of every WI-008
> P1/P2/P3 product finding assigned to this track.

## 1. Verdict

**PASS.** All five WI-008 product findings reviewed by this track are
**CLOSED**. None is reopened. The seven governed backend classes are exactly
100% line and method covered and are protected by a fail-closed JaCoCo class
rule. The global backend 80% line / 80% method / 70% branch rules and frontend
80% statements / 80% lines / 80% functions / 70% branches rules remain in
force and pass on the current generated reports.

The current evidence accounts for 1,206 backend tests and 468 frontend tests.
Focused frontend remediation tests independently pass 32/32. Review of the
added tests found no assertion-only test, production implementation mock, or
behavior-masking assertion. No new P1, P2, or P3 finding was identified.

This is the Track A remediation verdict only. WI-008 integration `GATE-01`,
`GATE-02`, and `GATE-03` are repository-readiness evidence observations rather
than P1/P2/P3 product findings; they remain owned by the companion Track B
report and are not adjudicated here.

## 2. Finding Closure Matrix

| WI-008 finding | Severity | Status | Independent closure evidence |
|---|---:|---|---|
| Backend: critical security-path coverage below 100% and unenforced (`WI-20260717-ATS-008/backend-qa.md:34-74`) | P2 | **CLOSED** | `build.gradle:76-84` names exactly seven classes; `:120-134` enforces `CLASS` line and method ratios of `1.00`; `:100-118` retains global 80/80/70; `:138-140` wires verification into `check`. Current JaCoCo XML independently yields 100% line/method for all seven classes, and `gradlew.bat jacocoTestCoverageVerification --console=plain` succeeds with all seven dependent tasks current. |
| Frontend `P3-NEW-01`: refreshed credentials could be used without durable persistence (`WI-20260717-ATS-008/frontend-qa.md:57-72`) | P3 | **CLOSED** | `frontend/src/api/client.ts:95-119` persists the access token and any rotated refresh token before Zustand update, queue release, or retry; failure rejects the queue, removes tokens, clears the session, emits the toast, and navigates to login. `frontend/src/api/client.test.ts:138-220` proves successful synchronization and independent access-token and refresh-token write failures, including no adapter retry and no auth-store update. |
| Frontend `P3-NEW-02`: unnamed Download History controls (`WI-20260717-ATS-008/frontend-qa.md:74-87`) | P3 | **CLOSED** | `frontend/src/pages/subscriber/DownloadHistoryPage.tsx:339-360` gives search/sort stable accessible names; `:465-474` gives the row action a Track-specific name and `type="button"`. `DownloadHistoryPage.test.tsx:181-207` locates the controls by role/name, exercises playback, and verifies the row button type. |
| Frontend `P3-NEW-03`: payment status type narrower than backend (`WI-20260717-ATS-008/frontend-qa.md:89-103`) | P3 | **CLOSED** | `frontend/src/api/payments.ts:5-17` contains all nine values and derives the union from the runtime tuple. The tuple exactly matches `PaymentOrderStatus.java:3-13`, which is serialized by `BillingAgreementConfirmResponse.java:10-16`. `frontend/src/api/domainApis.test.ts:32-42,263-268` verifies runtime tuple equality and exact type equality. |
| Integration `P3-01`: ADMIN route comment said 9 (`WI-20260717-ATS-008/integration-review.md:43-57`) | P3 | **CLOSED** | `frontend/src/router/index.tsx:222-236` says `Admin (14 routes)` and contains exactly 14 following ADMIN route declarations. `docs/ui/atstudio-front-list.md:39-47` independently records 14 Admin operation UIs. |

The WI-008 frontend report also left prior coverage finding `P2-06` deferred
because executable evidence had not been run (`frontend-qa.md:49,105-124`).
That evidence gap is **CLOSED** for this track by the current 468-test coverage
report, enforced thresholds, current test collection, and focused rerun below.

## 3. Backend Coverage Verification

### 3.1 Governed Class Rule

Source: `build.gradle:76-84,96-140`.

- Governed list size: 7 exact class names.
- Per-class rule: line `COVEREDRATIO >= 1.00`; method `COVEREDRATIO >= 1.00`.
- Global rule: line `>= 0.80`; method `>= 0.80`; branch `>= 0.70`.
- Task graph: `jacocoTestCoverageVerification` depends on the report, and
  `check` depends on verification.
- Production exclusions: none added by WI-009.

Independent extraction from
`build/reports/jacoco/test/jacocoTestReport.xml`:

| Governed class | Lines | Methods | Branches |
|---|---:|---:|---:|
| `config.JwtConfig` | 10/10 | 2/2 | 5/6 |
| `security.AuthRateLimitFilter` | 95/95 | 17/17 | 36/40 |
| `security.CustomUserDetailsService` | 10/10 | 4/4 | 4/4 |
| `security.JwtAuthenticationFilter` | 22/22 | 2/2 | 10/10 |
| `security.JwtTokenProvider` | 36/36 | 9/9 | n/a |
| `service.auth.AuthService` | 44/44 | 6/6 | 10/10 |
| `service.payment.billing.BillingKeyCrypto` | 97/97 | 17/17 | 45/50 |

The acceptance contract requires 100% executable line and method coverage for
these classes. Branch coverage remains governed by the unchanged global 70%
rule.

### 3.2 Global Counters and Test Totals

| Counter | Covered | Total | Coverage | Gate |
|---|---:|---:|---:|---:|
| Lines | 8,528 | 9,948 | 85.73% | 80% PASS |
| Methods | 1,511 | 1,822 | 82.93% | 80% PASS |
| Branches | 2,830 | 3,948 | 71.68% | 70% PASS |
| Instructions | 38,778 | 45,263 | 85.67% | informational |

Aggregation of all 158 generated
`build/test-results/test/TEST-*.xml` suite headers gives **1,206 tests, 0
failures, 0 errors, and 9 skipped**. The skips are confined to seven guarded
MySQL concurrency tests, one guarded MySQL schema test, and one Windows
symbolic-link test. No WI-009 remediation test is skipped.

Focused non-heavy verification:

```powershell
.\gradlew.bat jacocoTestCoverageVerification --console=plain
```

Result: **PASS** in 3 seconds; `test`, `jacocoTestReport`, and
`jacocoTestCoverageVerification` were all `UP-TO-DATE`. The fresh 1,206-test
suite was not rerun because its XML, test results, source timestamps, and
configuration are internally consistent.

## 4. Backend Test Quality Review

The reviewed tests invoke production behavior and assert observable outputs or
side effects:

- `JwtConfigTest.java:15-64`: missing, malformed, short, and valid signing keys.
- `CustomUserDetailsServiceTest.java:28-91`: active, absent, and deactivated
  users by both email and ID.
- `JwtAuthenticationFilterTest.java:41-103`: bypass, authentication, expired
  clearing/header behavior, invalid clearing, and filter-chain continuation.
- `JwtTokenProviderTest.java:38-111`: token claims, validation classes,
  expiry-tolerant subject extraction, configured lifetime, and PII absence.
- `AuthRateLimitFilterTest.java:61-290`: independent budgets, normalization,
  privacy-preserving keys/logs, cleanup, recovery endpoints, and digest failure.
- `AuthServiceTest.java:53-335`: login/social hashing, digest failure, refresh
  rotation/replay/deactivation, missing users, and logout invalidation.
- `BillingKeyCryptoTest.java:32-306`: authenticated roundtrip, rotation,
  malformed/forged ciphertext, key-ring validation, null/blank/placeholder
  configuration, provider failures, and environment binding.

The static JDK crypto mocks are scoped to provider-unavailability cases and
assert exception type, cause, fail-closed state, and non-disclosure. They do not
replace the production class under test. `BillingKeyCrypto.java:190-200`
reorders two pure short-circuit predicates so null placeholder handling is
reachable; null, blank, placeholder, malformed, and valid outcomes remain
equivalent and are exercised through public behavior. No assertion-only or
behavior-masking backend remediation test was found.

## 5. Frontend Evidence Verification

### 5.1 Generated Coverage

Independent parsing of `frontend/coverage/coverage-summary.json` gives:

| Counter | Covered | Total | Coverage | Gate |
|---|---:|---:|---:|---:|
| Statements | 6,095 | 7,027 | 86.73% | 80% PASS |
| Branches | 3,626 | 4,710 | 76.98% | 70% PASS |
| Functions | 1,616 | 1,892 | 85.41% | 80% PASS |
| Lines | 5,597 | 6,306 | 88.75% | 80% PASS |

`frontend/vite.config.ts:141-157` uses V8, includes production `src` TypeScript
and TSX, excludes declarations/tests/test infrastructure/main entry point, and
enforces the four thresholds above. The summary covers 112 production files.
All relevant WI-009 frontend source/test timestamps precede the generated
summary.

Current non-executing collection command:

```powershell
.\node_modules\.bin\vitest.cmd list
```

Result: **468 collected tests** across 63 test files (62 under `src` plus
`vite.config.test.ts`). A disabled-test search found no `skip`, `todo`, `xit`,
`xtest`, or `xdescribe` case.

### 5.2 Focused Independent Rerun

```powershell
npm run test -- src/api/client.test.ts `
  src/pages/subscriber/DownloadHistoryPage.test.tsx `
  src/api/domainApis.test.ts
```

Result: **PASS**, 3 files and 32 tests, 0 failures; Vitest duration 3.69 seconds.

An AST-based review found 389 static test declarations across the 62 `src`
test files; parameterized cases expand that set to 468. Every declaration has
an observable `expect`/`expectTypeOf`, Testing Library query, `waitFor`, or
promise rejection/resolution assertion. The four broad coverage suites contain
108 such test declarations and no unobserved test body. Targeted manual review
confirmed that WI-009 tests mock dependencies, not the implementation under
test, and assert durable storage, retry suppression, auth state, navigation,
accessible roles/names, button type, runtime tuple values, and TypeScript type
equality.

## 6. New Findings and Residual Risk

| Severity | New findings |
|---|---:|
| P1 | 0 |
| P2 | 0 |
| P3 | 0 |

- The frontend payment-status test uses an explicit backend contract snapshot
  rather than generated cross-language types. The current tuple was independently
  compared with the Java enum and is exact; this is maintenance exposure, not a
  current contract defect.
- Runtime/API/UI smoke, repository-wide secret scanning, and Git/worktree/tag
  cleanup preconditions belong to Track B. This report does not infer their
  outcome.
- Concurrent repository activity after the report invalidates artifact
  freshness and requires rechecking the affected counters/tests.

## 7. Scope and Rollback

This review read source, tests, policies, generated test/coverage reports, and
safe Git status only. It did not read or print `application-local.yml` or any
secret value. It did not modify product code, active documentation, database
state, Git refs, or the index. Only this assigned report was created.

Report-only rollback: remove this file. No product, documentation, database, or
Git rollback applies.

## 8. Final Status

**PASS**
