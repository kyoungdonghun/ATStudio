# Evidence Pack: WI-20260716-ATS-013

## Summary (one-liner)

- Independently completed the full backend clean regression, application build, JaCoCo measurement, report/log inspection, policy-invariant review, and residual-risk classification without modifying product or test code.

## Scope / DoD Check

- [x] Verified development worktree and branch: `C:/Users/jm991/Desktop/project/ATStudio`, `codex/p1-acceptance-hardening`.
- [x] Loaded the handoff's Tier 0, Tier 1, REQ, design, and WI-005 through WI-012 evidence pointers.
- [x] Executed a clean full backend suite and generated fresh JUnit and JaCoCo reports.
- [x] Executed the full Gradle build and assembled the Spring Boot artifact.
- [x] Parsed exact suite/test/failure/error/skip/duration totals from JUnit XML.
- [x] Parsed instruction, branch, line, complexity, method, and class counters from JaCoCo XML.
- [x] Reviewed risk-heavy packages/classes, skipped reasons, slow suites, log severity, OOM markers, and product invariants.
- [x] Reviewed fresh-schema/manual-patch contract evidence without executing DDL.
- [x] Confirmed `git diff --check` and `frontend/tsconfig.tsbuildinfo` integrity.
- [x] Created the required user summary and this Evidence Pack only.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and approved domain invariants |
| 0 | `docs/standards/development-standards.md` | Backend testing, coverage, and evidence standards |
| 1 | `docs/policies/quality-gates.md` | Regression and evidence requirements |
| 1 | `docs/policies/security-policy.md` | Security, payment, media, whitelist, and certification boundaries |
| 2 | `deliverables/user/REQ-20260716-ATS-002.md` | Approved development-branch remediation scope |
| 2 | `docs/design/remaining-remediation-design-20260716.md` | P2/X/P3 ownership and unchanged product policy |
| 2 | `deliverables/agent/WI-20260716-ATS-005-evidence-pack.md` | Auth/rate-limit/payment-role evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-006-evidence-pack.md` | Payment reconciliation/crypto/scheduler evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-007-evidence-pack.md` | Whitelist lifecycle/concurrency/export evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-008-evidence-pack.md` | Company certification integrity/audit evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-009-evidence-pack.md` | OAuth/catalog/download evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-010-evidence-pack.md` | Frontend integration and preserved-policy evidence |
| 2 | `deliverables/agent/WI-20260716-ATS-011-evidence-pack.md` | JaCoCo setup and prior baseline |
| 2 | `deliverables/agent/WI-20260716-ATS-012-evidence-pack.md` | Current code/design/document reconciliation |

**Injection Rules Applied**:

- Rule source: `AGENTS.md` and `deliverables/agent/WI-20260716-ATS-013-handoff.md`.
- Assignee: Reliability Engineer (`re`).
- Task type: independent backend regression, coverage observation, and reliability review.
- Forbidden boundaries preserved: product/test code, client branch/runtime, DB/data, live providers/secrets, Git stage/commit/push, and frontend commands.

## Evidence Pointers

### Generated Reports

- JUnit HTML: `build/reports/tests/test/index.html` (40,003 bytes, final run timestamp 2026-07-16 08:30:13 +09:00).
- JUnit XML: `build/test-results/test/TEST-*.xml` (146 files).
- JaCoCo XML: `build/reports/jacoco/test/jacocoTestReport.xml` (1,398,094 bytes, 2026-07-16 08:30:15 +09:00).
- JaCoCo HTML: `build/reports/jacoco/test/html/index.html`.
- Build artifact: `build/libs/ATStudio-0.0.1-SNAPSHOT.jar` (69,805,335 bytes, 2026-07-16 08:30:25 +09:00).

### Test and Contract Sources

- Payment schema/entity/manual-patch contracts: `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java`.
- Company certification schema contract: `src/test/java/com/atstudio/atstudio/entity/CompanyCertificationSchemaContractTest.java`.
- Whitelist lock/schema/version contract: `src/test/java/com/atstudio/atstudio/service/WhitelistConcurrencyContractTest.java`.
- Download user-lock/unique-license/atomic-count contract: `src/test/java/com/atstudio/atstudio/service/DownloadConcurrencyContractTest.java`.
- MySQL-only gates: `src/test/java/com/atstudio/atstudio/service/PaymentMysqlConcurrencyIntegrationTest.java:115`, `src/test/java/com/atstudio/atstudio/service/PaymentMysqlSchemaValidationTest.java:28`.
- Symbolic-link environment abort: `src/test/java/com/atstudio/atstudio/service/storage/LocalStorageServiceTest.java:68-88`.
- Full public stream and authenticated official download: `src/test/java/com/atstudio/atstudio/controller/TrackControllerTest.java`, `src/test/java/com/atstudio/atstudio/service/TrackServiceTest.java`, `src/test/java/com/atstudio/atstudio/service/DownloadServiceTest.java`.
- Report-only JaCoCo and 1 GiB test heap: `build.gradle:67-83`.

## Commands & Outputs

1. `gradlew.bat clean test jacocoTestReport --console=plain`
   - Preliminary attempt: the outer command wrapper reached its 120-second limit after all 146 JUnit XML files and the test HTML were produced, but while JaCoCo XML remained 0 bytes. This attempt was rejected as incomplete evidence.
   - Final rerun with a sufficient command limit: exit 0, `BUILD SUCCESSFUL in 1m 54s`, 7/7 tasks executed, complete JUnit and JaCoCo reports generated.
2. `gradlew.bat build --console=plain`
   - Exit 0, `BUILD SUCCESSFUL in 3s`; 3 tasks executed and 5 up-to-date; Boot JAR assembled.
3. PowerShell JUnit XML aggregation over `build/test-results/test/TEST-*.xml`
   - 146 suites, 1,046 tests, 0 failures, 0 errors, 9 skipped, aggregate suite time 88.671 seconds.
4. PowerShell JaCoCo XML aggregation over `build/reports/jacoco/test/jacocoTestReport.xml`
   - Exact counters recorded below.
5. `git diff --check`
   - Exit 0; no whitespace errors. Output contained only non-failing LF-to-CRLF working-tree warnings.
6. `Get-FileHash -Algorithm SHA256 frontend/tsconfig.tsbuildinfo`
   - `B6A42AD2CD32A5AD04D06C55B8B1B26DD9B1894AFDAF7B508DFF16EF0C60F22A`, unchanged from the pre-WI baseline.
7. Read-only static checks
   - No distributed-lock/ShedLock/leader-election implementation found.
   - No destructive `DROP`, `DELETE`, `TRUNCATE`, `UPDATE`, or `INSERT` statement was applied; no manual SQL was executed.
   - No live Toss/OAuth/provider call, retained-DB connection, secret read, or client-runtime action was performed.

## Tests

### Exact JUnit Result

| Suites | Total | Executed passed | Failures | Errors | Skipped | Aggregate suite time |
|---:|---:|---:|---:|---:|---:|---:|
| 146 | 1,046 | 1,037 | 0 | 0 | 9 | 88.671s |

### Area Representation

Counts below are pattern-based evidence and overlap where one class proves more than one contract.

| Area | Tests | Executed | Skipped | Report classes |
|---|---:|---:|---:|---:|
| Auth/rate-limit/security | 83 | 83 | 0 | 7 |
| Payment/billing/reconciliation/scheduler | 215 | 207 | 8 | 37 |
| Whitelist | 58 | 58 | 0 | 11 |
| Company certification | 73 | 73 | 0 | 12 |
| OAuth/social | 14 | 14 | 0 | 4 |
| Download/license | 46 | 46 | 0 | 7 |
| Playlist/album/catalog | 51 | 51 | 0 | 3 |
| Schema/concurrency contracts | 27 | 19 | 8 | 7 |

### Skipped Tests

- Seven `PaymentMysqlConcurrencyIntegrationTest` races and one `PaymentMysqlSchemaValidationTest` are class-gated by `@EnabledIfEnvironmentVariable(named = "ATSTUDIO_MYSQL_PROOF_ENABLED", matches = "true")`.
- `LocalStorageServiceTest.rejectsDirectoriesAndSymbolicLinks()` aborts only the symbolic-link branch when the host cannot create a temporary symbolic link.
- Conclusion: local unit/H2 regression is clean. Disposable/copied MySQL and symbolic-link-capable-host proof remains environment-conditional and cannot be closed by this WI.

### Runtime/Log Inspection

- No `OutOfMemoryError`, `StackOverflowError`, `AssertionError`, or `BUILD FAILED` marker exists in JUnit XML.
- Three case-sensitive ERROR-level log rows occur only in `GlobalExceptionHandlerTest` and are its expected technical-exception/fallback scenarios.
- 109 case-sensitive WARN-level rows across 29 report files correspond to exercised denial, validation, provider-failure, reconciliation, and other negative paths. JUnit reports no swallowed failure/error.
- The slowest suite is `AtStudioApplicationTests` at 18.836 seconds; the complete suite remains bounded at 88.671 aggregate seconds and 115.2 seconds observed wall time.
- The final run is consistent with WI-011's prior 1,046-test clean baseline. Test order is not randomized, so this proves no observed flake, not exhaustive order independence.

## Coverage

Coverage values are observations. The approved REQ explicitly does not enforce an arbitrary threshold, and `build.gradle` contains no verification rule.

| Counter | Covered | Missed | Total | Percent |
|---|---:|---:|---:|---:|
| Instruction | 34,615 | 10,842 | 45,457 | 76.15% |
| Branch | 2,226 | 1,544 | 3,770 | 59.05% |
| Line | 7,653 | 2,311 | 9,964 | 76.81% |
| Complexity | 2,060 | 1,738 | 3,798 | 54.24% |
| Method | 1,451 | 444 | 1,895 | 76.57% |
| Class | 341 | 48 | 389 | 87.66% |

### Risk-Heavy Class Observations

| Class | Lines | Branches | Interpretation |
|---|---:|---:|---|
| `StorageMutationJournalService` | 11.11% | 0% | No direct report; compensation/cleanup journal behavior needs risk-based tests. |
| `AdminPaymentController` | 0% | n/a | Lower payment services are tested, but aggregate HTTP wiring is not. |
| `AdminPaymentReadService` | 0% | n/a | Admin payment order/agreement/payment/receipt/audit read wiring is not directly tested. |
| `PaymentApplicationService` | 26.97% | 25.00% | Uncovered code is predominantly retained legacy one-time internals; blocked entry and legacy terminal behavior are tested. |
| `PaymentOperationAuditLogService` | 44.55% | 60.00% | Receipt/correction/settlement audit event variants have gaps. |
| `DownloadService` | 54.39% | 41.67% | Entitlement/quota/license mutation is tested; history-read methods are uncovered. |
| `OAuth2Service` | 67.86% | 50.00% | Strict parsing is tested; real Kakao/Naver transport compatibility is not. |
| `PlaylistService` | 76.30% | 58.06% | Core lock/quota/reorder behavior is tested; batch/default creation and error branches remain. |

Selected hardened paths have materially stronger coverage: `AuthRateLimitFilter` 94.74% lines / 82.50% branches, `AcceptanceStartupGuard` 85.37% / 74.07%, `BillingKeyCrypto` 82.24% / 66.67%, `CompanyCertificationService` 92.31% / 66.67%, `WhitelistChannelService` 88.62% / 67.24%, and `SubscriptionScheduler` 100% lines / 75% branches.

## Product Invariant Review

1. **Public full listening:** `TrackControllerTest` covers no-Range complete representation and complete-resource Range handling. `TrackServiceTest` proves the original full resource is used even if a legacy `previewFile` exists and that duration does not truncate the resource.
2. **Gated download:** anonymous `/api/tracks/{id}/download` receives 401; `DownloadServiceTest` covers no subscription, inactive Track, zero/finite/unlimited quota, new license, and licensed re-download; `DownloadConcurrencyContractTest` covers user locking, unique license, and atomic Track counting.
3. **Recurring billing-key card payment:** payment/billing/reconciliation/scheduler classes contribute 207 executed tests. `BillingKeyCrypto`, startup guard, provider failures, renewal, reconciliation, scheduler, and withdrawal billing cleanup are represented. Legacy one-time subscription preparation/confirmation remains blocked.
4. **Single server:** scheduler zones are explicit and no distributed lock dependency was found. Multi-server coordination remains outside the approved architecture.

## Findings by Severity

### P0 / P1

- None found. Compilation, test execution, JaCoCo generation, and build all completed without a correctness failure.

### P2 / Medium

1. **Risk-based coverage debt:** storage compensation and admin payment HTTP/read wiring have no direct coverage; payment audit, legacy payment internals, download read paths, OAuth transport, and selected playlist branches remain thin. Evidence: JaCoCo class rows above.
2. **MySQL proof not executed:** eight explicit MySQL race/schema tests are skipped. This preserves `ATS020-X-01` as `ENVIRONMENT-CONDITIONAL`; source/H2 evidence must not be represented as retained-MySQL proof.

### P3 / Low

1. **Symbolic-link branch not executed:** one local storage test aborts because this host cannot create the test link.
2. **Test resource/timeout sensitivity:** JaCoCo uses a 1 GiB test heap and the complete clean run takes about two minutes. A 120-second external wrapper was insufficient even though tests had completed and report generation was in progress.
3. **Compiler warning:** Gradle reports that some inputs use unchecked or unsafe operations without file-level detail; it does not fail compilation.

## Risks / Rollback

- Environment limits:
  - No retained/fresh MySQL execution, EXPLAIN, migration, backfill, or Hibernate validate proof was performed.
  - No live Toss, Google, Kakao, or Naver call was performed.
  - No deployed JWT secret, trusted proxy, multi-egress identity, CORS, callback, or production scheduler evidence was inspected.
  - Social-only withdrawal remains `POLICY-PENDING` and unimplemented.
- Generated `build/` reports and artifacts are ignored and reproducible.
- Rollback: this WI is verification-only except `deliverables/user/WI-20260716-ATS-013-summary.md` and `deliverables/agent/WI-20260716-ATS-013-evidence-pack.md`. Remove only those two files to roll back this WI; no product, test, data, schema, secret, runtime, or Git-history rollback is required.

## Follow-ups

- WI-015 may proceed with cross-layer/code/design/document reconciliation while carrying the P2/P3 findings above.
- WI-016 should review the uncovered compensation/admin payment surfaces and the environment-only concurrency boundary from a security/concurrency perspective.
- WI-017 should add focused tests for confirmed risk, then rerun the complete backend regression and JaCoCo report. It must not implement social-only withdrawal or apply retained-DB DDL without separate approval.
