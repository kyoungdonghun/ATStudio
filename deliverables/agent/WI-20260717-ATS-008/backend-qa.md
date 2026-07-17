---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa / cr
category: audit
status: blocked
dependencies:
  - ../WI-20260717-ATS-008-handoff.md
  - ../WI-20260717-ATS-006/backend-qa.md
  - ../WI-20260717-ATS-006/integration-review.md
  - ../WI-20260717-ATS-007-handoff.md
---

# WI-20260717-ATS-008 Backend QA Re-audit

## Verdict

**BLOCK.** The requested clean backend build, full test suite, JaCoCo report, and
global JaCoCo verification all pass. All four WI-006 findings with a backend,
DB/config, or payment repair owned by WI-007 are closed on current evidence.
However, this independent re-audit found **one new P2 quality-gate defect**:
the repository's explicit 100% coverage requirement for security-sensitive
code is neither met nor enforced. There are **0 P1, 1 P2, and 0 P3** open
findings, so WI-008 cannot issue a backend PASS.

Review target: the current dirty working tree on
`codex/p1-acceptance-hardening`. This audit did not modify product code,
active documentation, configuration, database state, Git refs, or the index.
Only this report was written. No `application-local.yml` file was read.

## Finding

### P2 - Critical security-path coverage is below the mandatory 100% gate and is not enforced

- **Requirement:** `docs/standards/development-standards.md:592-603` requires
  100% coverage for security-sensitive authentication, authorization, and data
  sanitization code. This is separate from the repository-wide 80% line,
  80% function/method, and 70% branch minimums at lines 576-585.
- **Enforcement evidence:** `build.gradle:86-110` adds only one `BUNDLE` rule:
  line 80%, method 80%, and branch 70%. `check` depends on that global rule at
  `build.gradle:112-114`; there is no class/package rule for critical security
  paths.
- **Fresh JaCoCo evidence:** the generated
  `build/reports/jacoco/test/jacocoTestReport.xml` records the following
  security-sensitive classes below 100%:
  - `security/CustomUserDetailsService`: lines 0%, branches 0%, methods 0%; all
    executable lines 19-33 are missed.
  - `security/JwtTokenProvider`: lines 94.44%, methods 88.89%; lines 64 and 86
    are missed.
  - `security/JwtAuthenticationFilter`: lines 95.45%, branches 80%; lines
    45, 50, and 58 contain missed instructions or branches.
  - `security/AuthRateLimitFilter`: lines 94.74%, branches 82.50%; missed or
    partial paths include lines 68, 118-125, 164, 178-179, 252, and 288.
  - `service/auth/AuthService`: lines 79.55%, branches 100%, methods 66.67%;
    missed paths include lines 62-72, 90, and 124-125.
  - `service/payment/billing/BillingKeyCrypto`: lines 82.47%, branches 68%,
    methods 100%; 32 source lines contain missed instructions or branches,
    including validation/failure paths at lines 35-36, 56-63, 85-86,
    104-124, 136-181, and 191-216.
  - `config/JwtConfig` reaches 100% lines and methods but only 83.33% branches;
    line 25 remains partial.
- **Reproduction:** run the full command recorded below, then inspect the class
  counters and missed source lines in
  `build/reports/jacoco/test/jacocoTestReport.xml` or the generated HTML report.
- **Impact:** authentication, token parsing/filtering, rate limiting, and
  billing-key cryptography can lose untested failure behavior while the
  repository still reports a green `build` and coverage verification. This
  violates an explicit High-criticality completion criterion.
- **Smallest safe remediation:** add behavior-focused tests for the uncovered
  success, rejection, malformed-input, and failure paths. After the tests
  genuinely reach the requirement, add fail-closed JaCoCo class/package rules
  for the named security-sensitive production paths at 100%. Do not lower the
  standard, exclude production code, or add assertion-only coverage tests.

## WI-006 Finding Closure Matrix

| Prior finding | Status | Current independent evidence |
|---|---|---|
| WI-006 backend P2: global coverage thresholds were not met or enforced | **CLOSED** | The fresh root counters are lines 85.26%, methods 82.44%, and branches 71.15%. `build.gradle:86-114` enforces 80/80/70 and wires verification into `check`. The full verification command passes. The new critical-path finding above is narrower and does not reopen the global bundle result. |
| WI-006 backend P3: no direct `PROVIDER_MISMATCH` regression | **CLOSED** | `PaymentReconciliationTransactionService.java:231-233` remains fail-closed. `PaymentReconciliationTransactionServiceTest.java:155-172` directly supplies missing provider identity and asserts incident-only `PROVIDER_MISMATCH`. The fresh full suite passes. |
| WI-006 integration P2-04 / `INT-V08`: JWT message implied automatic local import | **CLOSED** | `JwtConfig.java:24-40` now names explicit `SPRING_CONFIG_ADDITIONAL_LOCATION` loading for missing and malformed secrets. `JwtConfigTest.java:14-40` asserts the guidance; its four tests pass. The base `application.yml` diff removes automatic local import. |
| WI-006 integration P3-01 backend portion / `INT-R06`: stale Thymeleaf configuration commentary | **CLOSED** | Current `application.yml` contains no active Thymeleaf settings or comment. `V1BackendBaselineContractTest.java:99` retains a negative contract guard, and the full suite passes. |
| WI-006 integration P2-03 / `INT-R12`: deleted manual-patch instructions | **DEFERRED (documentation subpart)** | The backend/DB runtime side is closed: the current diff deletes all manual SQL files, and current schema evidence is fresh-only. Active-document wording is owned by the WI-008 integration report and is not asserted closed here. |
| WI-006 integration P2-01 / `INT-V07`: frontend legacy provider values | **DEFERRED (cross-layer subpart)** | Backend and schema evidence use the V1 `TOSS` provider identity. Frontend type/fixture closure belongs to frontend/integration QA. |

No relevant WI-006 backend finding was reopened.

## Fresh Execution Evidence

### Full gate

Command:

```powershell
.\gradlew.bat clean build jacocoTestReport jacocoTestCoverageVerification --console=plain
```

Result: **PASS**, exit code 0, wall time 199.1 seconds. Output contained only
the existing unchecked-operation compiler note and JVM CDS warning.

Test XML aggregation from `build/test-results/test/TEST-*.xml`:

| Suites | Tests | Failures | Errors | Skipped |
|---:|---:|---:|---:|---:|
| 156 | 1,181 | 0 | 0 | 9 |

The skipped set is seven environment-gated MySQL concurrency tests, one MySQL
schema-validation test, and one filesystem symbolic-link case. The MySQL tests
were not forced because this report is prohibited from mutating a database.

### JaCoCo root counters

| Counter | Covered | Missed | Total | Coverage | Required | Result |
|---|---:|---:|---:|---:|---:|---|
| Instructions | 38,546 | 6,717 | 45,263 | 85.16% | 80% | PASS |
| Lines | 8,482 | 1,466 | 9,948 | 85.26% | 80% | PASS |
| Methods | 1,502 | 320 | 1,822 | 82.44% | 80% | PASS |
| Branches | 2,809 | 1,139 | 3,948 | 71.15% | 70% | PASS |
| Classes | 343 | 26 | 369 | 92.95% | n/a | Informational |

## Protected Contract Review

| Boundary | Result | Current evidence |
|---|---|---|
| Payment provider baseline | PASS | Current DDL provider columns use `ENUM ('TOSS')`; payment DTO/entity tests and the full suite pass. No second V1 provider was observed in the inspected backend baseline. |
| Payment idempotency and ownership fences | PASS | Spot review of `PaymentCommandTransactionService.java:123-150` confirms completed/finalize-only handling, stale-processing reconciliation fencing, invalid-state rejection, expiry rejection, and per-attempt provider idempotency claims. Focused command/fence integration suites pass with 10 tests and no skips. |
| Refund/reconciliation recovery | PASS | Fresh focused suites pass: refund resilience 14 tests, reconciliation recovery 9 tests, recurring renewal command 6 tests, and subscription upgrade command 5 tests. The direct provider mismatch test is present and passing. |
| Financial schema constraints | PASS | Current `schema.sql` retains unique order/command/provider-attempt keys (`:515-518`), unique finalized payment order/provider transaction keys (`:546-547`), refund idempotency (`:646`), reconciliation dedupe (`:769`), provider constraints, foreign keys, and operational indexes. |
| Authorization ordering and payment boundary | PASS | `SecurityConfig.java:91-101` keeps `/api/users/me` authenticated matchers before the ADMIN wildcard; `:127-139` keeps `/api/payments/**` USER-only and `/api/admin/**` ADMIN-only. The 23-test security filter-chain suite passes. |
| Direct protected storage | PASS | `SecurityConfig.java:86-87` denies direct Track audio and company-document upload paths before the authenticated catch-all. |
| JWT local-config semantics | PASS | Base automatic import is removed; runtime guidance explicitly requires additional-location loading. No ignored local configuration was inspected. |
| Critical security test completeness | **FAIL** | See the P2 finding: current security and billing-key crypto class coverage violates the separate 100% standard despite passing global thresholds. |

## Commands and Inspected Paths

- Full Gradle gate shown above.
- XML aggregation of `build/test-results/test/TEST-*.xml`.
- Root and class/source-line counter extraction from
  `build/reports/jacoco/test/jacocoTestReport.xml`.
- `git status --short --branch` and backend-scoped `git diff`/`git diff --stat`
  for the dirty review target.
- Current searches and line inspection across `build.gradle`,
  `src/main/java/com/atstudio/atstudio/config/`,
  `src/main/java/com/atstudio/atstudio/security/`, payment services,
  `src/main/resources/application.yml`, `schema.sql`, and backend tests.
- Environment preflight checked only whether profile, datasource, additional
  location, and MySQL-test variables were set; no values were printed.

## Injected Documents

- `AGENTS.md` and `WI-20260717-ATS-008-handoff.md`.
- Tier 0: `core-principles.md`, `development-standards.md`,
  `documentation-standards.md`, and `glossary.md`.
- Tier 1: `quality-gates.md`, `security-policy.md`, and
  `access-control-policy.md`.
- Current SoT: `api-spec.md` and `db-schema.md`.
- Decision/evidence: approved `REQ-20260716-ATS-004`, WI-006 backend and
  integration reports, and `WI-20260717-ATS-007-handoff.md`.
- Execution skills: `build-check`, `test`, and `test-coverage`.

## Residual Risk and Next Gate

- The eight MySQL-only tests remain environment-gated. Existing redacted MySQL
  evidence was not treated as a fresh database execution in this report.
- The symbolic-link filesystem test remains skipped on this Windows host.
- This verdict applies to the dirty-tree snapshot exercised by the 199.1-second
  gate. Concurrent changes require rerunning the gate and class counters.
- WI-009 evidence aggregation and repository cleanup must remain blocked from
  the backend track until the P2 critical security coverage defect is repaired
  and independently rerun.

Report-only rollback: remove this file. No product, document, DB, or Git
rollback is applicable.

## Final Recommendation

**BLOCK**
