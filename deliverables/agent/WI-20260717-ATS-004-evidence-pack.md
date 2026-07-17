# Evidence Pack: WI-20260717-ATS-004

## Summary

Established and proved the fail-closed V1 backend database/configuration/payment-provider baseline, recreated the approved loopback local database from that baseline, retired the nine gated manual SQL patches, and corrected the final six stale test-fixture failures without changing production behavior.

## Scope / DoD Check

- [x] `TOSS` is the sole persisted V1 payment provider; recurring, lookup, and refund provider interfaces remain.
- [x] Legacy provider enum/config meanings, one-time payment surface references, and billing-key V1 crypto/property paths have no active backend/config references.
- [x] Billing-key storage remains V2 key-ID AES-GCM with fail-closed key-ring validation.
- [x] Base configuration no longer imports the ignored root local file automatically; local loading is explicit and acceptance cannot inherit it.
- [x] QA bootstrap is disabled by default and forbidden in production profiles.
- [x] `schema.sql` is a 39-table fresh-only baseline; first apply passes, second apply fails, and the structural/data manifest passes.
- [x] `seed.sql` is the sole deterministic owner of six subscription plans and contains no demo users/tracks/albums/tags/notices.
- [x] Disposable MySQL proof, Hibernate validation, payment race proof, cleanup, local recreation, and final local manifest pass.
- [x] The nine manual migration SQL files were removed only after disposable proof passed.
- [x] The four affected test classes, full backend suite, `jacocoTestReport`, Gradle build, diff check, exact residual searches, and secret scan pass.
- [x] No frontend source, active documentation, Git ref, or ignored local-secret value was modified or exposed by WI-004 completion.

## Reference Documents (Tier 0-2)

**Injected Context** from `deliverables/agent/WI-20260717-ATS-004-handoff.md`:

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | System and ATStudio invariants |
| 0 | `docs/standards/development-standards.md` | Software Engineer implementation/testing standards |
| 1 | `docs/policies/security-policy.md` | Secret-safe config, logging, and payment safeguards |
| 1 | `docs/policies/quality-gates.md` | Required completion verification |
| 2 | `docs/design/db-schema.md` | Database baseline and retained-database context |
| 2 | `docs/design/payment-integration-design.md` | Payment-provider and billing-key context |
| 2 | `docs/design/api-spec.md` | Current backend contract boundary |
| Decision | `deliverables/user/REQ-20260716-ATS-004.md` | Approved V1 cleanup scope |
| Decision | `deliverables/agent/WI-20260717-ATS-001-evidence-pack.md` | Approved disposition and safeguards |
| Decision | `deliverables/agent/WI-20260716-ATS-038-evidence-pack.md` | Integrated residual ledger |
| Decision | `deliverables/agent/WI-20260716-ATS-036-evidence-pack.md` | Prior DB/config findings |
| Decision | `deliverables/agent/WI-20260717-ATS-002-evidence-pack.md` | Backend cleanup dependency |

**Injection Rules Applied**:

- Rule sources: `AGENTS.md`, `.claude/config/context-injection-rules.json`, and the WI handoff.
- Assignee: `se`.
- Task type: bounded backend/config/database implementation and verification.
- Destructive scope was limited to the user-approved loopback `atstudio` recreation and gated manual-SQL retirement.

## Decision Traceability

| Decision | Implementation and proof |
|---|---|
| `INT-P01` | `BillingKeyCrypto` accepts/writes only V2 key-ID envelopes; `PaymentProperties` exposes only the key-ring configuration. Fresh DB proof established no retained V1 data dependency. |
| `INT-P02` | `src/main/resources/schema.sql` is fresh-only and fail-closed. Disposable first apply passed, second apply failed as required, and the 39-table manifest passed. |
| `INT-P03` | `src/main/resources/seed.sql` owns exactly six plans. `AcceptanceSubscriptionPlanBootstrapRunner` validates the baseline instead of writing duplicate plan data. |
| `INT-P04` | Base auto-import and legacy callback/crypto properties were removed; acceptance/local profiles use `ddl-auto=validate`; bootstrap/startup guards remain fail-closed. |
| `INT-P05` | `V1MysqlProofManager.java` and `run-v1-mysql-proof.ps1` provide guarded loopback-only disposable and approved-local proof with redacted outputs. |
| `INT-R06` | Inert Thymeleaf settings were removed from the active base configuration; SPA forwarding behavior was not changed. |
| `INT-R12` | Disposable mode required all nine manual SQL files and passed at 02:09; local-recreation mode required zero files and passed at 02:13. All nine paths are deleted in the working diff. |
| `INT-V07` | `PaymentProviderType` contains only `TOSS`; `SubscriptionPayment.provider` is non-null; provider interfaces and production mismatch logic remain for future multi-PG extension. |
| `INT-V08` | The ignored local file was not edited or value-inspected. Proof tooling consumed credentials internally, enforced loopback/exact DB identity, and emitted redacted metadata only. |

## Final Failure Diagnosis

The prior full-suite run had six stale-fixture failures, not production defects:

| Test | Failures | Root cause | Final correction |
|---|---:|---|---|
| `PaymentControllerTest` | 1 | JSON expectation still required `TOSS_BILLING` while the DTO correctly serialized `TOSS`. | Expect `TOSS`. |
| `AdminPaymentSettlementServiceTest` | 3 | Three CSV rows used the removed enum value, so import rejected each row before reconciliation. | Use `TOSS` in all three rows. |
| `AdminPaymentRefundServiceTest` | 1 | The invalid-provider case supplied valid `TOSS`, continued past validation, and reached an unstubbed save result. | Use `null`, a genuinely invalid V1 provider state. |
| `PaymentReconciliationTransactionServiceTest` | 1 | The former `MOCK` mismatch was changed to `TOSS`; with a one-value enum it became exact evidence and returned no failure code. | Remove the unrepresentable case and assert order/status/transaction incidents. |

Production refund validation and the `PROVIDER_MISMATCH` reconciliation branch were preserved unchanged.

## Evidence Pointers

### Key Production and Configuration Locations

- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java:4` - sole V1 provider.
- `src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java:55` - non-null persisted provider.
- `src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java:25` - V2 key-ID envelope.
- `src/main/java/com/atstudio/atstudio/config/PaymentProperties.java` - provider-neutral recurring/key-ring properties.
- `src/main/java/com/atstudio/atstudio/config/AcceptanceStartupGuard.java:48` - production/bootstrap/payment startup refusal checks.
- `src/main/java/com/atstudio/atstudio/bootstrap/AcceptanceSubscriptionPlanBootstrapRunner.java:56` - baseline validation.
- `src/main/resources/application.yml:15` - default `ddl-auto=validate`.
- `src/main/resources/application-acceptance.yml:11` - acceptance `ddl-auto=validate`.
- `application-local.example.yml:2` - explicit local loading instruction.
- `src/main/resources/schema.sql:1` - 39-table V1 fresh baseline.
- `src/main/resources/seed.sql:5` - six-plan-only seed owner.

### Final Test-Fixture Corrections

- `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java:84`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:96`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:138`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:158`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:216`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java:143`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java:176`
- `src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java` - provider/config/schema/seed contract guard.

### Other WI-004 Production/Test Scope

- Provider/config consumers: `AdminPaymentEntitlementCorrectionService`, `AdminPaymentRefundService`, `BillingAgreementApplicationService`, `BillingAgreementCleanupTransactionService`, `PaymentCommandTransactionService`, `PaymentRefundTransactionService`, `RecurringRenewalService`, `SubscriptionUpgradePaymentExecutor`, `UserService`, `UserSubscriptionService`, and `TossBillingProvider`.
- Focused contract updates under `src/test/java/com/atstudio/atstudio/bootstrap/`, `config/`, `controller/`, `dto/payment/`, `entity/`, `repository/`, `service/`, and `service/payment/` remain listed in the prior partial pack history and are covered by the final full-suite result.
- `src/test/resources/application.yml:43` - explicit test-only V2 key material fixture; classified as a non-runtime test fixture by the secret scan.

### Deleted Manual SQL Paths

- `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`
- `src/main/resources/db/manual/20260618_company_certification_documents.sql`
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql`
- `src/main/resources/db/manual/20260714_storage_mutations_journal.sql`
- `src/main/resources/db/manual/20260715_track_waveform_data.sql`
- `src/main/resources/db/manual/20260716_company_certification_integrity_and_audit.sql`
- `src/main/resources/db/manual/20260716_download_atomicity.sql`
- `src/main/resources/db/manual/20260716_payment_reconciliation_indexes.sql`
- `src/main/resources/db/manual/20260716_whitelist_integrity_and_exports.sql`

## MySQL Proof

### Disposable Baseline

- Summary: `deliverables/agent/WI-20260717-ATS-004/run-20260717-020705-a4047376/Disposable-summary.log`.
- Runtime and datasource preflight: pass; loopback host, exact `atstudio`, zero active sessions, zero prior WI-004 disposable databases.
- First schema/seed apply: pass.
- Second schema apply: expected failure, pass.
- Manifest: 39 tables, 449 columns, 153 indexes, 80 foreign keys, six plans, nine provider columns, nine Toss-only provider columns.
- Hibernate `ddl-auto=validate`: pass.
- MySQL payment race suite: pass.
- Disposable cleanup: pass; post-disposable audit reports zero disposable databases.

### Approved Local Recreation

- Summary: `deliverables/agent/WI-20260717-ATS-004/run-20260717-021207-9953beed/RecreateLocal-summary.log`.
- Runtime/datasource/session/exact-name preflight: pass.
- Manual SQL count gate: zero.
- Local drop/recreate and schema/seed apply: pass.
- Hibernate `ddl-auto=validate`: pass.
- Final local manifest: pass and identical to the recreate manifest.
- Current schema and seed hashes still match the hashes used for local recreation.

### Proof Revalidation

- `deliverables/agent/WI-20260717-ATS-004/verification-20260717-022332-3efc1f35/proof-validity.log`.
- Current runtime: port 5173 listeners `0`; port 8080 listeners `0`; `cloudflared` processes `0`.
- Current manual SQL files: `0`; deleted manual SQL paths in diff: `9`.
- Ignored local values read by revalidation: `false`.

## Commands and Outputs

- Focused four-class test command:
  - `gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest"`
  - Result: 28 tests, 0 failures, 0 errors, 0 skipped.
  - Log: `deliverables/agent/WI-20260717-ATS-004/verification-20260717-022332-3efc1f35/focused-four-tests.log`.
- Full backend test:
  - `gradlew.bat test --rerun-tasks`
  - Result: 147 suites, 1,074 tests, 0 failures, 0 errors, 9 environment-conditional skips.
  - Logs: `full-backend-test.log`, `full-backend-test-aggregate.log` in the final verification directory.
- JaCoCo:
  - `gradlew.bat jacocoTestReport` -> pass; XML and HTML reports generated.
  - Metrics: instructions 78.05%, branches 59.65%, lines 78.83%, methods 79.09%, classes 91.60%.
  - `build.gradle` configures report generation but no `jacocoTestCoverageVerification` threshold task; the WI handoff gate is report generation and passed. The repository-wide metrics remain below the generic documentation targets and were not broadened in this fixture-only completion.
- Build:
  - `gradlew.bat build` -> pass.
- Diff integrity:
  - `git diff --check` and `git diff --cached --check` -> exit 0; line-ending conversion notices only.
- Exact residual searches:
  - Provider enum constants, serialized stale fixtures, production legacy provider values, old provider config, V1 crypto/property paths, old one-time payment types/endpoints, base auto-local import, schema compatibility/obsolete identifiers, seed non-baseline writes: all `0`.
  - Manual SQL files: `0`.
  - Log: `exact-residual-searches.log`.
- Secret scan:
  - Scope: unstaged additions, staged additions, and non-ignored untracked text; ignored local values were not read.
  - High-confidence secret candidates: `0`; one explicit test-only key-material fixture was classified as non-runtime.
  - Log: `secret-scan.log`.

## Risks / Rollback

**Risks**:

- A provider mismatch cannot be constructed while `PaymentProviderType` has one value. Production mismatch logic remains intentionally present and should regain a concrete branch test when a second provider is actually introduced.
- Active documentation still contains references to the retired manual SQL history. It was intentionally not edited because active-doc cleanup belongs to downstream WI-005/WI-006 and was explicitly out of WI-004 scope.
- Repository-wide JaCoCo percentages are recorded above and are below the generic documentation targets; no enforcement task is configured, and increasing broad coverage was outside the user-limited fixture correction.
- This shared worktree contains concurrent WI-002/WI-003 changes. WI-004 completion did not revert or stage them.

**Rollback**:

- Restore the nine manual SQL files from Git history only if returning to a retained-database patch strategy; do not apply them to the recreated V1 baseline.
- Revert only WI-004 provider/config/schema/seed/test/proof changes as one unit; do not revert concurrent WI changes.
- Local DB rollback requires an explicitly approved restore source. The user approved deletion of the prior local data and no data-preservation rollback exists.
- Remove only the WI-004 evidence/proof artifacts when rolling back documentation; preserve other WI deliverables.

## Follow-ups

- `WI-20260717-ATS-005`: active-document/generated-artifact/ignore-policy consequences, including current references to retired manual SQL paths.
- `WI-20260717-ATS-006`: independent full residual, runtime/API/UI, and cross-layer verification.
