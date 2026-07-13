# Evidence Pack: WI-20260711-ATS-015

## Summary (one-liner)

- Verified that neither Java nor frontend coverage is currently measurable with repository-configured tooling, recorded the absence as an instrumentation gap rather than `0%`, and mapped prior P0/P1 findings to focused test gaps.

## Scope / DoD Check

- [x] Verified Java coverage capability from the active Gradle configuration and report locations.
- [x] Verified frontend coverage capability from npm scripts, dependencies, installed packages, Vitest configuration, and report locations.
- [x] Mapped high-risk paths from WI-002, WI-003, and WI-004 to missing focused tests.
- [x] Recorded existing regression baselines from dependency WIs WI-009 and WI-010 without presenting them as coverage.
- [x] Recorded why no coverage command was run and why elapsed time is not applicable.
- [x] Reported absent tooling as an instrumentation gap; no percentage was fabricated.
- [x] Added no dependency, plugin, test, source change, or generated coverage artifact.
- [x] Created only the two output files owned by WI-015.

## Baseline and Constraints

| Field | Value |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Working tree | Dirty before this WI; concurrent and user changes were retained and not reverted. |
| Allowed writes | `deliverables/user/WI-20260711-ATS-015-summary.md`; `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md` |
| Forbidden work | Adding coverage dependencies/plugins/configuration, adding tests, changing source, or inventing metrics |
| Audit posture | Static configuration and artifact inspection; existing coverage commands only if configured |

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Transparency, platform integrity, payment auditability, and non-destructive execution |
| 1 | `docs/policies/quality-gates.md` | Traceability, regression evidence, risk recording, and Evidence Pack gate |
| 2 | `.agents/skills/test-coverage/SKILL.md` | JaCoCo and Vitest/Jest coverage detection workflow and expected metrics |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Evidence Pack structure and reproducibility requirements |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope and WI-015 phase assignment |
| WI | `deliverables/agent/WI-20260711-ATS-015-handoff.md` | Scope, acceptance criteria, constraints, dependencies, and output contract |
| Prior audit | `deliverables/agent/WI-20260711-ATS-002-evidence-pack.md` | Backend defects and focused test gaps |
| Prior audit | `deliverables/agent/WI-20260711-ATS-003-evidence-pack.md` | Frontend findings and focused test gaps |
| Prior audit | `deliverables/agent/WI-20260711-ATS-004-evidence-pack.md` | Security findings and security test gaps |
| Dependency | `deliverables/agent/WI-20260711-ATS-009-evidence-pack.md` | Fresh backend regression baseline |
| Dependency | `deliverables/agent/WI-20260711-ATS-010-evidence-pack.md` | Frontend regression baseline |

Context was loaded in handoff order: Tier 0, Tier 1, Tier 2, REQ/prior evidence, then the current repository snapshot.

## Coverage Capability

| Area | Configured test runner | Coverage plugin/provider | Coverage command | Existing report | Result |
|---|---|---|---|---|---|
| Java | JUnit Platform via Gradle | No JaCoCo plugin or configuration | None | No `build/reports/jacoco/` | **Not measurable** |
| Frontend | Vitest via `npm test` | No declared or installed `@vitest/coverage-v8` or `@vitest/coverage-istanbul` | None | No `frontend/coverage/` | **Not measurable** |

### Java evidence

- `build.gradle:1-5` applies Java, Spring Boot, and dependency-management plugins only; it does not apply `jacoco`.
- `build.gradle:66-68` configures only `test { useJUnitPlatform() }`.
- No `jacocoTestReport` or `jacocoTestCoverageVerification` block exists.
- `build/reports/jacoco/`, `build/reports/jacoco/test/html/index.html`, and `build/reports/jacoco/test/jacocoTestReport.xml` were absent at inspection time.
- Consequently, `gradlew.bat test jacocoTestReport` is not a repository-configured command and was not run.

### Frontend evidence

- `frontend/package.json:10-16` defines `test: vitest run` but no coverage script.
- `frontend/package.json:18-37` declares Vitest and Testing Library dependencies but no Vitest coverage provider, `c8`, or `nyc`.
- `frontend/vite.config.ts:20-25` configures the jsdom environment, setup file, and CSS only; no `test.coverage` configuration exists.
- `frontend/node_modules/@vitest/coverage-v8/package.json` and `frontend/node_modules/@vitest/coverage-istanbul/package.json` were absent.
- `frontend/coverage/` was absent at inspection time.
- `frontend/package-lock.json:4795-4824` mentions Vitest coverage packages only as optional peer dependencies; neither is a top-level declared or installed provider.
- Consequently, `npx vitest run --coverage` could request an unapproved package installation and was not run.

## Inventory and Regression Baseline

| Area | Production files | Test files | Existing suite result | Coverage interpretation |
|---|---:|---:|---|---|
| Java | 363 `.java` files | 71 test files | WI-009: 745 passed, 0 failed/errors/skipped | Passing regression only; percentage unknown |
| Frontend | 111 production `.ts`/`.tsx` files | 14 test files | WI-010: 51 passed, 0 failed/skipped | Passing regression only; percentage unknown |

- Java counts were collected with `rg --files src/main/java -g '*.java'` and `rg --files src/test -g '*Test.java' -g '*Tests.java'`.
- Frontend counts were collected with `rg --files frontend/src -g '*.ts' -g '*.tsx'`, excluding `*.test.*`, `*.spec.*`, and the shared `src/test/` setup directory from the production count.
- File counts describe test structure, not executable line/branch/method coverage.

## High-Risk Test-Gap Map

`Not proven covered` below means the prior audit identified a missing focused test and this WI found no instrumentation capable of measuring the path. It does not assert numeric `0%` coverage.

| Priority | Path not proven covered | Prior evidence | Required focused test |
|---|---|---|---|
| P0 | Anonymous original-audio retrieval bypasses subscription, quota, ledger, and license checks | BE-001 at `WI-002-evidence-pack.md:98-117`; PG-004-01 at `WI-004-evidence-pack.md:72,91-101` | Security/resource integration test denying `/uploads/tracks/audio/**` while allowing intended public media |
| P0 | Withdrawn account remains eligible for recurring renewal | BE-002 at `WI-002-evidence-pack.md:118-131` | Transaction/integration flow `withdraw -> due scheduler`, asserting zero provider calls and non-renewable state |
| P0 | Playlist thumbnail accepts active content on the SPA origin | PG-004-02 at `WI-004-evidence-pack.md:73,103-115` | Reject HTML, SVG, MIME mismatch, and non-image bytes; verify upload-origin isolation/security headers |
| P1 | Initial billing failure state can roll back with the API error | BE-003 at `WI-002-evidence-pack.md:132-144` | Real Spring transaction test that reloads order/agreement after provider decline |
| P1 | Renewal batch can mix multiple external charges in one transaction | BE-004 at `WI-002-evidence-pack.md:145-158` | Two-agreement failure-isolation test plus duplicate-run concurrency test |
| P1 | Concurrent refunds can over-reserve one payment | BE-005 at `WI-002-evidence-pack.md:159-169` | Real-DB concurrent reservation test with aggregate invariant assertion |
| P1 | Mail failure logs live token URLs, recipient PII, and body | BE-006 at `WI-002-evidence-pack.md:170-180`; PG-004-03 at `WI-004-evidence-pack.md:74,117-124` | Log-capture test asserting token, URL, body, recipient, and nickname are absent |
| P1 | File and DB mutations have inconsistent rollback/commit compensation | BE-007 at `WI-002-evidence-pack.md:181-196` | Real transaction matrix for create/replace/delete, rollback, partial store failure, and cleanup retry |
| P1 | Company-document validation trusts weak file indicators before admin review | PG-004-04 at `WI-004-evidence-pack.md:75,126-136` | MIME/magic/parser mismatch rejection, quarantine, and safe-download behavior |
| P1 | Social login calls `/users/me` without the newly returned token | FE-001 at `WI-003-evidence-pack.md:118-136` | Empty-storage callback test asserting returned-token authorization and atomic user/token commit |
| P1 | ADMIN can reach member checkout and prepare recurring billing state | FE-002 at `WI-003-evidence-pack.md:137-157` | Role matrix across direct URLs, catalog CTA, prepare API, and no-order persistence |
| P1 | Refresh sessions survive logout or credential change | PG-004-07 at `WI-004-evidence-pack.md:78`; security gap 8 at `WI-004-evidence-pack.md:198` | Multi-session revocation tests for logout, password reset, and password change |

Additional P2/P3 gaps remain listed in `WI-002-evidence-pack.md:335-350`, `WI-003-evidence-pack.md:384-404`, and `WI-004-evidence-pack.md:190-203`. This WI prioritizes the P0/P1 set for downstream review.

## Commands and Outputs

### Configuration and artifact inspection

Commands executed from the repository root:

```powershell
Get-Content -Raw build.gradle
Get-Content -Raw frontend/package.json
Get-Content -Raw frontend/vite.config.ts
rg -n -i 'jacoco|coverage|@vitest/coverage|istanbul|c8|jest' build.gradle settings.gradle gradle.properties frontend/package.json frontend/package-lock.json frontend/vite.config.ts frontend/src src/test
Test-Path build/reports/jacoco
Test-Path build/reports/jacoco/test/html/index.html
Test-Path build/reports/jacoco/test/jacocoTestReport.xml
Test-Path frontend/coverage
Test-Path frontend/node_modules/@vitest/coverage-v8/package.json
Test-Path frontend/node_modules/@vitest/coverage-istanbul/package.json
rg --files src/main/java -g '*.java'
rg --files src/test -g '*Test.java' -g '*Tests.java'
rg --files frontend/src -g '*.ts' -g '*.tsx'
```

Observed capability output:

```json
{"JacocoPlugin":0,"JacocoTasks":0,"FrontendCoverageScript":0,"FrontendCoverageDependency":0,"CoverageProviderInstalled":0}
```

Observed artifact state:

```text
build/reports/jacoco: missing
build/reports/jacoco/test/html/index.html: missing
build/reports/jacoco/test/jacocoTestReport.xml: missing
frontend/coverage: missing
```

Observed inventory:

```text
Java production files: 363
Java test files: 71
Frontend production files: 111
Frontend test files: 14
```

### Coverage command decision

| Candidate command | Executed | Reason |
|---|---|---|
| `gradlew.bat test jacocoTestReport` | No | `jacoco` plugin and `jacocoTestReport` task are not configured. |
| `gradlew.bat jacocoTestReport` | No | No configured JaCoCo task or existing report input. |
| `npx vitest run --coverage` | No | No declared/installed Vitest coverage provider; `npx` could trigger an unapproved install. |
| `npx jest --coverage` | No | Jest is not the configured frontend test runner. |

- Coverage-command elapsed time: **N/A**, because no configured coverage command existed to run.
- No standalone log was written; command outputs were captured directly for this Evidence Pack.

## Tests

- No test suite was rerun by WI-015 because this WI was limited to coverage capability and test-gap analysis.
- Dependency evidence was current at the same HEAD:
  - WI-009: `gradlew.bat test --rerun-tasks --console=plain` passed 745/745 backend tests in 103.942 seconds.
  - WI-010: `npm test` passed 51/51 frontend tests in 11.957 seconds wall-clock time.
- These results are regression baselines only. They do not supply line, branch, method/function, statement, or instruction coverage.

## Files Changed

- `deliverables/user/WI-20260711-ATS-015-summary.md` - concise user-facing verdict, evidence, and priority gaps.
- `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md` - reproducible capability checks, inventory, risk map, commands, and limitations.
- No source, test, dependency, build configuration, coverage report, or unrelated user/concurrent file was modified.

## Risks / Limitations / Rollback

- Current percentage coverage is unknown, not zero. Any threshold comparison would be fabricated until instrumentation exists.
- File and test counts cannot identify which statements or branches execute.
- Passing unit-heavy suites do not verify transaction rollback, row locking, concurrency, resource-handler security, browser role journeys, or external-provider boundaries unless dedicated tests exist.
- The workspace is shared and dirty; later concurrent changes may make this snapshot stale.
- Adding JaCoCo or a Vitest coverage provider requires a separately approved implementation WI because this handoff forbids dependency, plugin, configuration, test, and source changes.
- Rollback, only if explicitly requested: remove only these two files:
  - `deliverables/user/WI-20260711-ATS-015-summary.md`
  - `deliverables/agent/WI-20260711-ATS-015-evidence-pack.md`

## Follow-ups / WI Chain

- WI-016 should consume the P0/P1 security and payment test-gap map without treating passing regression suites as risk closure.
- WI-017 should consume the backend transaction, concurrency, file-lifecycle, and protected-media gaps.
- WI-018 should consume the social-login, role-routing, upload-origin, and frontend test-instrumentation gaps.
- Per the handoff, WI-015 blocks WI-016, WI-017, and WI-018; the MA must perform the chain check and delegate the next WIs after accepting this Evidence Pack.
