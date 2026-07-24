# Evidence Pack: WI-20260724-ATS-004

## Summary

- Proved the full backend, JaCoCo, build, acceptance lifecycle, and demo seed
  tooling gates at the shared WI-001 through WI-003 snapshot without reading
  secrets or modifying product source.

## Scope / DoD Check

- [x] Full Gradle test suite passed.
- [x] JaCoCo report and configured verification gates passed.
- [x] Backend build and executable Spring Boot JAR assembly passed.
- [x] Acceptance backend-environment contract passed.
- [x] Acceptance dry-run lifecycle contract passed.
- [x] Demo seed focused contract test passed.
- [x] Exact test, skip, coverage, warning, command, and build evidence recorded.
- [x] No live Provider, API, DB, storage, credential, or secret operation ran.
- [x] No product source was modified by WI-004.

## Verification Snapshot

| Field | Value |
| --- | --- |
| Branch | `codex/p1-acceptance-hardening` |
| HEAD | `4b00e99f2293e290d92b1fc56412a90743588c80` |
| Tree | Shared final working tree containing uncommitted WI-001 through WI-003 changes |
| Agent | `qa` |
| REQ | `REQ-20260724-ATS-001` |
| WI | `WI-20260724-ATS-004` |

The snapshot was intentionally not a clean checkout. Existing WI-001 through
WI-003 changes and unrelated pre-existing untracked artifacts were preserved.

## Governing Context

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Execution, traceability, and secret boundaries |
| 0 | `docs/standards/development-standards.md` | Backend test and coverage thresholds |
| 1 | `docs/policies/quality-gates.md` | Operational QA gate |
| REQ | `deliverables/user/REQ-20260724-ATS-001.md` | Approved scope |
| WI | `deliverables/agent/WI-20260724-ATS-004-handoff.md` | Mandatory output and forbidden actions |
| Evidence | `deliverables/agent/WI-20260724-ATS-001-evidence-pack.md` | Acceptance environment contract |
| Evidence | `deliverables/agent/WI-20260724-ATS-002-evidence-pack.md` | Demo seed CLI contract |
| Evidence | `deliverables/agent/WI-20260724-ATS-003-evidence-pack.md` | Portable PDF provenance result |

## Commands and Results

### 1. Full backend tests and coverage

```powershell
.\gradlew.bat clean test jacocoTestReport jacocoTestCoverageVerification --warning-mode all
```

- Result: PASS, `BUILD SUCCESSFUL`
- Duration: 2m 4s
- Gradle tasks: 8 executed
- Test suites: 158
- Tests: 1,208
- Passed: 1,199
- Failures: 0
- Errors: 0
- Skipped: 9
- Test report duration: 1m 26.64s
- Reports:
  - `build/reports/tests/test/index.html`
  - `build/reports/jacoco/test/jacocoTestReport.xml`
  - `build/reports/jacoco/test/html/index.html`
  - `build/reports/problems/problems-report.html`

### 2. Backend build

```powershell
.\gradlew.bat build --warning-mode all
```

- Result: PASS, `BUILD SUCCESSFUL`
- Duration: 3s
- Gradle tasks: 10 actionable, 3 executed, 7 up-to-date
- Artifacts:
  - `build/libs/ATStudio-0.0.1-SNAPSHOT.jar`: 69,737,749 bytes
  - `build/libs/ATStudio-0.0.1-SNAPSHOT-plain.jar`: 963,873 bytes

The test and coverage tasks were up-to-date in this second command because the
first clean command had completed them immediately beforehand.

### 3. Acceptance backend environment contract

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-backend-environment.ps1
```

- Result: PASS
- Named checks: 9
- Checks:
  - `external-bundle-validation`
  - `required-and-allowlisted-names`
  - `current-v2-and-scheduler-name-acceptance`
  - `obsolete-payment-name-rejection`
  - `safe-validation-errors`
  - `child-process-environment-isolation`
  - `backend-environment-restoration`
  - `tunnel-before-bundle-load-order`
  - `temporary-fixture-cleanup`

### 4. Acceptance dry-run contract

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\acceptance\test-dry-run.ps1
```

- Result: PASS
- Named checks: 10
- Analyzer: `not-installed`
- Checks:
  - `parser`
  - `quick-tunnel-url-parser`
  - `public-base-url-validation`
  - `dry-run-contract`
  - `status-no-manifest`
  - `stop-no-manifest`
  - `readiness-http-status-contract`
  - `abnormal-start-cleanup-contract`
  - `start-finally-structure`
  - `secret-free-dry-run-output`

### 5. Demo seed focused contract

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\demo\test-seed-client-demo.ps1
```

- Result: PASS
- Named checks: 14
- Checks:
  - `no-personal-default-path`
  - `node-syntax`
  - `direct-seed-dry-run`
  - `direct-cleanup-dry-run`
  - `direct-non-dry-run-fail-closed`
  - `direct-explicit-runtime-input-forwarding`
  - `direct-verify-dry-run-rejected`
  - `wrapper-seed-dry-run`
  - `wrapper-cleanup-dry-run`
  - `wrapper-non-dry-run-fail-closed`
  - `wrapper-explicit-runtime-input-forwarding`
  - `wrapper-verify-dry-run-rejected`
  - `secret-safe-output`
  - `temporary-fixture-cleanup`

## JaCoCo Coverage

| Metric | Covered | Total | Coverage | Threshold | Result |
| --- | ---: | ---: | ---: | ---: | --- |
| Instructions | 38,778 | 45,263 | 85.673% | 80% standard | PASS |
| Branches | 2,830 | 3,948 | 71.682% | 70% | PASS |
| Lines | 8,528 | 9,948 | 85.726% | 80% | PASS |
| Methods | 1,511 | 1,822 | 82.931% | 80% | PASS |
| Classes | 345 | 369 | 93.496% | Informational | PASS |

The build configuration directly enforces line, method, and branch bundle
thresholds. Instruction coverage is reported against the project standard.

### Critical security classes

All seven configured classes had zero missed lines and zero missed methods:

| Class | Covered lines | Covered methods |
| --- | ---: | ---: |
| `JwtConfig` | 10 | 2 |
| `AuthRateLimitFilter` | 95 | 17 |
| `CustomUserDetailsService` | 10 | 4 |
| `JwtAuthenticationFilter` | 22 | 2 |
| `JwtTokenProvider` | 36 | 9 |
| `AuthService` | 44 | 6 |
| `BillingKeyCrypto` | 97 | 17 |
| **Total** | **314** | **57** |

Result: 100% line and method coverage for all configured critical classes.

## Skip Classification

| Count | Test area | Reason | Classification |
| ---: | --- | --- | --- |
| 7 | `PaymentMysqlConcurrencyIntegrationTest` | Requires `ATSTUDIO_MYSQL_PROOF_ENABLED=true` and a disposable MySQL proof environment | Expected environment-dependent skip |
| 1 | `PaymentMysqlSchemaValidationTest` | Requires the same disposable MySQL proof environment | Expected environment-dependent skip |
| 1 | `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks` | Symbolic links unavailable in the current Windows environment | JUnit assumption abort |

No skipped test concealed a failure. Enabling the first eight would require DB
provisioning and mutation, which the WI explicitly forbids.

## Warning and Notice Inventory

| Count | Source | Detail | Impact |
| ---: | --- | --- | --- |
| 2 | Gradle Problems Report | `ADVICE` diagnostics for unchecked/unsafe test compilation in `OAuth2ServiceTest.java`; second diagnostic recommends `-Xlint:unchecked` | Non-blocking; no `WARNING` or `ERROR` severity |
| 1 | Test JVM | Class data sharing is limited because the bootstrap classpath was appended | Non-blocking test-runtime warning |
| 1 | Acceptance dry-run | Optional `PSScriptAnalyzer` was not installed | Parser and all 10 contract checks still passed |
| 0 | Gradle deprecation | No deprecation warning under `--warning-mode all` | None |

Gradle also printed the informational recommendation to enable configuration
cache. It is not a correctness warning and was not counted above.

## Safety Evidence

- No external backend environment bundle was supplied to any command.
- No credential file or secret value was read, printed, or mutated.
- No live Provider or network API call ran.
- No live demo seed, verify, or cleanup ran.
- No DB or storage mutation ran.
- PowerShell tests used synthetic or nonexistent temporary inputs and cleaned
  their temporary fixtures.
- WI-004 wrote only:
  - `deliverables/user/WI-20260724-ATS-004-summary.md`
  - `deliverables/agent/WI-20260724-ATS-004-evidence-pack.md`

## Gate Decision

**PASS**

- WI-004 acceptance criteria are satisfied.
- No P0/P1 backend or acceptance lifecycle defect was found.
- The three warning categories are classified as non-blocking.
- This evidence unblocks WI-007 after WI-005 and WI-006 also complete.
