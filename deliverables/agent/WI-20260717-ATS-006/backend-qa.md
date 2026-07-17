---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa
category: audit
status: blocked
dependencies:
  - ../WI-20260717-ATS-006-handoff.md
  - ../WI-20260717-ATS-001-evidence-pack.md
  - ../WI-20260717-ATS-002-evidence-pack.md
  - ../WI-20260717-ATS-004-evidence-pack.md
---

# WI-20260717-ATS-006 Backend QA

## Verdict

Independent backend QA found **0 P1, 1 P2, and 1 P3** issue. The backend full test suite and clean build pass, and no behavioral regression was found in the reviewed high-risk paths. WI-006 is **blocked by the P2 coverage quality-gate failure** until coverage is repaired or an explicit approved exception changes the requirement.

Verification snapshot: branch `codex/p1-acceptance-hardening`, HEAD `a96d2e0c5d24`, including the current shared working-tree changes. This report did not modify product, documentation, configuration, database, or Git state.

## Findings

### P2 - Repository coverage quality gate is not met

- **Requirement:** `docs/standards/development-standards.md:580-585` requires 80% lines, 70% branches, and 80% functions. The QA gate repeats the 80% line and 70% branch requirements at `docs/standards/development-standards.md:819-823`.
- **Observed:** the fresh JaCoCo report records 78.83% lines (7,842 covered / 2,106 missed), 59.65% branches (2,355 / 1,593), and 79.09% methods (1,441 / 381). Lines and branches are below their explicit thresholds; methods, the available JaCoCo proxy for functions, are also below 80%.
- **Why the build remains green:** `build.gradle:76-83` generates XML and HTML reports but defines no `jacocoTestCoverageVerification` threshold enforcement.
- **Reproduction:** run `./gradlew.bat test --rerun-tasks --console=plain`, then `./gradlew.bat jacocoTestReport --console=plain`; inspect the root counters in `build/reports/jacoco/test/jacocoTestReport.xml` or the generated HTML report.
- **Impact:** the approved WI completion gate cannot be satisfied despite passing tests and build.
- **Repair:** add focused tests, prioritizing the low-coverage payment, download, and track paths, then rerun the full suite and JaCoCo report. Add Gradle threshold enforcement in a separately approved WI so a green build cannot mask future gate failures.

### P3 - Provider-mismatch reconciliation branch lacks a direct regression test

- **Location:** `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:231-233` emits the strict `PROVIDER_MISMATCH` incident. `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/ProviderPaymentLookupResult.java:20-38` permits constructing a found result with a null provider.
- **Observed:** `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java:103-197` tests exact evidence, currency, order ID, status, missing transaction, and persisted transaction mismatch, but contains no `PROVIDER_MISMATCH` assertion. JaCoCo reports the condition at line 231 as partially covered (`mb=1`, `cb=1`).
- **Reproduction:** `rg -n "PROVIDER_MISMATCH" src/main/java src/test/java` returns the production branch and no test reference. The JaCoCo XML line counter for line 231 is `mi=0 ci=5 mb=1 cb=1`.
- **Impact:** provider identity remains fail-closed in implementation, but its incident contract can regress without a focused test.
- **Repair:** add a test using a found lookup result whose provider differs from the claim, including the currently constructible null-provider case, and assert strict incident-only behavior.

## Independent Execution

| Check | Result |
|---|---|
| Safe test preflight | PASS. MySQL-proof and Spring profile/config override environment variables were unset. Only `src/test/resources/application.yml` H2 `create-drop` fixtures were used. |
| `./gradlew.bat test --rerun-tasks --console=plain` | PASS in 154.7 s. 147 suites, 1,074 tests, 0 failures, 0 errors, 9 skipped. |
| `./gradlew.bat jacocoTestReport --console=plain` | PASS as a report-generation task; coverage fails the documented thresholds as described above. |
| `./gradlew.bat build --rerun-tasks --console=plain` | PASS in 146.1 s, including compilation, tests, and packaging. Only existing unchecked-operation and JVM CDS warnings were emitted. |
| Backend-scoped `git diff --check` | PASS for `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`, and `application-local.example.yml`. |

Eight skipped tests are the environment-gated MySQL DDL/concurrency checks (seven concurrency cases and one schema-validation case). One additional skip is in `LocalStorageServiceTest`. The MySQL tests were not forced because this QA assignment prohibits database mutation outside normal H2/test fixtures.

## Exact Residual Checks

| Invariant | Independent result |
|---|---|
| Schema/entity parity | PASS: 39 unique `CREATE TABLE` definitions and 39 `@Entity`/`@Table` mappings; no schema-only or entity-only table. |
| Removed persistence remnants | PASS: zero removed tables/columns for play history, download queue, preview file, and legacy user whitelist snapshots. |
| Compatibility DDL | PASS: zero `CREATE TABLE IF NOT EXISTS` and zero `FOREIGN_KEY_CHECKS` bypasses. |
| Provider baseline | PASS: `PaymentProviderType` contains exactly `TOSS`; zero production references to `MOCK`, `TOSS_BILLING`, or `KAKAOPAY`. |
| Provider columns | PASS: nine provider columns, all TOSS-only by schema constraints; seven are non-null and two are intentionally nullable. `SubscriptionPayment.provider` is explicitly non-null. |
| DDL mode | PASS: base, acceptance, and checked-in local example resolve to `ddl-auto: validate`; no automatic local config import was found. |
| Retired configuration | PASS: zero old provider selector, legacy billing-key secret, one-time Toss callback/confirm, DDL update, or mock-provider keys. |
| Seed baseline | PASS: exactly six subscription-plan seed rows and no non-baseline seed writes. |
| Manual SQL and removed Java types | PASS: zero manual SQL files and zero residual deleted one-time-payment, play-history, download-queue, old subscription-request, preview-field, or old whitelist-snapshot types. |

No application-local file was read, and no local value was printed.

## WI-004 MySQL Evidence Verification

The current redacted WI-004 proof artifacts were rechecked without accessing application-local values:

- `proof-validity.log` reports local-value access as false, matching schema/seed hashes, consistent manifests, zero manual SQL, expected deleted migrations, passing disposable/local checks, and stopped proof runtime.
- The disposable proof reports successful first apply, expected second-apply failure, manifest verification, Hibernate `ddl-auto=validate`, MySQL race tests, and cleanup.
- The local recreation proof reports passing preflight, first apply, manifest verification, and Hibernate validation. Redacted preflight/postflight evidence records loopback scope, exact `atstudio` target, zero active sessions, and no disposable database residue.
- Current checked-in schema and seed SHA-256 values match the redacted WI-004 expected hashes. Both disposable and local manifests report 39 tables, 449 columns, 153 indexes, 80 foreign keys, zero forbidden tables/columns, six plans, and nine TOSS-only provider columns with the same manifest hash.
- Secret-pattern scanning of the redacted proof artifacts returned zero matches.

This verifies that the existing redacted MySQL proof remains current for the checked-in schema/seed baseline. It is not a new live MySQL execution; the eight environment-gated MySQL tests remain a residual verification limit.

## High-Risk Review

No additional P1/P2/P3 behavioral finding was identified in these reviewed boundaries:

| Boundary | Reviewed implementation evidence |
|---|---|
| Payment idempotency, claims, locks, and fences | `PaymentCommandTransactionService.java:130-162`, `1219-1251`, and `1333-1347` retain provider-attempt claims, transaction-owner locking, exact existing-payment validation, and pessimistic agreement/order locking. |
| Refund leases and replay | `PaymentRefundTransactionService.java:50-155` and `264-314` retain durable claims, active-lease fencing, stale reclaim audit, same-key replay ceiling, and success/pending/failure recording. |
| Reconciliation and audit | `PaymentReconciliationTransactionService.java:210-260` retains strict provider/order/status/amount/currency/transaction evidence assessment. Payment audit and reconciliation incident persistence paths are unchanged by the reviewed diff. |
| Storage recovery | `StorageMutationRecoveryService.java:36-160` and journal claim/lock paths retain bounded claims, retry/retention handling, and pessimistic recovery ownership. The removed preview-file reference follows the removed schema field. |
| QA bootstrap guards | `AcceptanceStartupGuard.java:44-68` rejects production/default bootstrap combinations and validates the V2 key ring. The acceptance plan runner remains acceptance-profiled, opt-in, validation-only, and fail-closed for missing/inactive/duplicate/mismatched rows. |
| Emergency admin operations | Admin subscription PUT/DELETE operations and ADMIN authorization remain present in controller, service, and security mappings. |
| Public full playback | `TrackController.java:94-152` retains full-body playback without a Range header and strict single-range handling; public DTOs continue to mask storage keys and direct static audio access remains denied. |
| Download and licensing | `DownloadService.java:41-83` retains user-row serialization, license-aware redownload, quota enforcement, license/history creation, and atomic download-count increment. |
| Whitelist export | `AdminWhitelistChannelService.java:145-247` retains candidate locking, bounded/scoped export, immutable replay, and optimistic/pessimistic concurrency contracts. Removed user snapshots match the approved minimized schema. |
| Company certification | `CompanyCertificationService.java:72-276` retains apply/resubmit/review state fencing, audit persistence, private storage usage, and entity versioning. |

The payment-related production diff is limited primarily to the `TOSS_BILLING` to `TOSS` enum migration, removal of legacy V1 billing-key decryption/configuration, non-null provider enforcement, and deletion of a deprecated finalization overload. Core reconciliation, incident, audit, and storage-recovery implementations were not weakened by the reviewed changes.

## Residual Risk And Recovery

- Current confidence is bounded by the eight skipped MySQL-only tests and the verified-but-not-rerun WI-004 MySQL proof.
- The report describes a dirty shared worktree snapshot. Any subsequent concurrent change requires rerunning these checks before relying on the verdict.
- Report-only rollback is removal of this file. Product repair should occur under a follow-up approved WI; after repair, rerun the full backend test, JaCoCo, clean build, exact residual checks, and the authorized MySQL proof workflow.
