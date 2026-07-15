# Evidence Pack: WI-20260715-ATS-007

## Summary (one-liner)

- **PASS:** the final authorized disposable MySQL 8/InnoDB run passed schema creation, Hibernate validation, all seven bounded concurrency races, and guaranteed database cleanup.

## Scope / DoD Check

- [x] Added MySQL-only test/support and a repository-safe disposable runner.
- [x] Enforced the approved disposable database name shape and denied retained/local, preview, stage, and production targets.
- [x] Reused the local credential-source pattern without persisting or printing connection details, credentials, or the generated database name.
- [x] Applied the current schema/manual patch to a newly generated disposable database.
- [x] Passed Hibernate `ddl-auto=validate` before race execution.
- [x] Executed all seven design Section 10.2 races with bounded futures/latches and a 30-second JUnit timeout.
- [x] Proved each exact winner/loser outcome and row-count/state invariant without accepting a deadlock, lock timeout, connection error, arbitrary exception, or assertion timeout.
- [x] Dropped the disposable database in `finally` and independently confirmed post-drop existence count `0`.
- [x] Preserved retained/local, preview, stage, and production databases and the running acceptance preview.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial integrity and execution boundaries |
| 0 | `docs/standards/development-standards.md` | Java, JPA, transaction, and test standards |
| 1 | `docs/policies/quality-gates.md` | High-criticality verification requirements |
| 1 | `docs/policies/security-policy.md` | Credential, database, and evidence boundaries |
| 1 | `docs/standards/evidence-pack-standard.md` | Pointer-based completion evidence |
| 2 | `docs/design/p1-payment-integrity-remediation-design.md` | Section 10.2 production-engine race contract |
| Handoff | `deliverables/agent/WI-20260715-ATS-007-handoff.md` | Approved scope, ownership, and output contract |
| Correction | `deliverables/agent/WI-20260715-ATS-008-evidence-pack.md` | Production correction that closed races 4 and 7 |

## Evidence Pointers

Implementation and runner:

- `src/test/java/com/atstudio/atstudio/service/MysqlRaceTestSupport.java` - strict two-worker race execution, bounded waits, and exact failure classification.
- `src/test/java/com/atstudio/atstudio/service/PaymentMysqlConcurrencyIntegrationTest.java:116-595` - 30-second suite timeout and all seven MySQL/InnoDB races.
- `src/test/java/com/atstudio/atstudio/service/PaymentMysqlSchemaValidationTest.java` - dedicated Hibernate validation gate.
- `src/test/java/com/atstudio/atstudio/service/PackageGMysqlRunnerContractTest.java` - runner safety and redaction contract.
- `deliverables/agent/WI-20260715-ATS-007/run-package-g-mysql-proof.ps1` - schema create, validate, race, diagnostic, and `finally` cleanup orchestration.
- `deliverables/agent/WI-20260715-ATS-007/DisposableMysqlDatabaseManager.java` - constrained disposable database create/drop/absence helper.

Final redacted run evidence:

- `deliverables/agent/WI-20260715-ATS-007/run-summary.log` - authoritative PASS summary: schema, validation, races, cleanup, and no required diagnostics.
- `deliverables/agent/WI-20260715-ATS-007/mysql-races.log` - Gradle race suite completed with `BUILD SUCCESSFUL`.
- `deliverables/agent/WI-20260715-ATS-007/hibernate-validate.log` - Hibernate validation completed with `BUILD SUCCESSFUL`.
- `deliverables/agent/WI-20260715-ATS-007/database-drop.log` - `drop.database=OK` and cleanup existence count `0`.
- `deliverables/agent/WI-20260715-ATS-007/database-absent.log` - independent post-drop absence result `0`.

Historical redacted audit artifacts, retained from the earlier 5/7 failed run and **not** produced or required by the final PASS run:

- `deliverables/agent/WI-20260715-ATS-007/failure-diagnostics.log` - redacted failure diagnostics captured for the earlier races 4 and 7 failure.
- `deliverables/agent/WI-20260715-ATS-007/manager-diagnostics-process.log` - redacted manager-process diagnostics captured for that same earlier failed run.

## Seven-Race Traceability

| Race | Exact contract proved | Final result |
|---|---|---|
| 1 | One first-renewal claimant received `CALL_PROVIDER`; the loser received exact `PAYMENT_ORDER_INVALID_STATE`; one `PROCESSING` order at provider attempt 1 and zero payment rows remained. | PASS |
| 2 | One day-two retry claimant received `CALL_PROVIDER`; the loser received exact `PAYMENT_ORDER_INVALID_STATE`; the same order and command were reused at provider attempt 2. | PASS |
| 3 | One upgrade claim won and the duplicate received exact `PAYMENT_ORDER_INVALID_STATE`; finalization and cancellation both completed without deadlock, leaving one order, one payment, upgraded entitlement, cancelled agreement/subscription, and one cleanup lease. | PASS |
| 4 | Both provider-success renewal finalizers completed idempotently; one `DONE` order, one payment row, one entitlement transition, and one next-period advance remained. | PASS |
| 5 | One refund reservation won and the loser received exact `INVALID_ARGUMENT`; one requested refund totaling 6,000 KRW, one audit row, and zero provider calls remained. | PASS |
| 6 | One stale-refund reclaimer won and the loser received exact `INVALID_STATE_TRANSITION`; the delayed old result was fenced, leaving one `PROCESSING` refund under the replacement lease and zero provider calls. | PASS |
| 7 | Reconciliation and the normal finalizer both completed; one `DONE` order, one payment row owning the exact provider transaction, and only `RESOLVED` matching Incidents remained. | PASS |

The generated JUnit suite reported **7 tests, 0 failures, 0 errors, 0 skipped**, with suite time **17.051 seconds**. All seven named races above passed.

## Commands & Outputs

- Final runner:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File .\deliverables\agent\WI-20260715-ATS-007\run-package-g-mysql-proof.ps1`
  - Exit code: `0`
  - Wall time: `97.6s`
- Final redacted runner result from `run-summary.log`:

```text
schemaCreate=PASS
hibernateValidate=PASS
mysqlRaces=PASS
diagnostics=NOT_REQUIRED
drop=PASS
cleanupDatabaseExists=0
result=PASS
```

- `diagnostics=NOT_REQUIRED` describes the authoritative final PASS run. The two retained diagnostics files listed above belong only to the earlier 5/7 failed run and remain as historical audit evidence.

- Owned-document validation:
  - `git diff --check -- deliverables/agent/WI-20260715-ATS-007-evidence-pack.md deliverables/user/WI-20260715-ATS-007-summary.md`
  - Result: PASS. Because both WI-007 documents are currently untracked, `git diff --no-index --check` was also run for each file; it reported no whitespace errors and only the repository's existing LF-to-CRLF working-copy notice.

## Audit History

- Early disposable runs reached schema creation, Hibernate validation, and guaranteed cleanup but stopped in test-only preflight code: first because a row callback advanced the `ResultSet` twice, then because Java selected Spring JDBC's `ResultSetExtractor` overload and read before the first row.
- After those harness defects were corrected, a real MySQL run completed all races with 5/7 passing and exposed production idempotency/convergence defects in races 4 and 7.
- `failure-diagnostics.log` and `manager-diagnostics-process.log` were captured and redacted during that earlier 5/7 failed run. Their continued presence is intentional audit retention and does not indicate diagnostics were generated for the final PASS run.
- WI-20260715-ATS-008 corrected completed-renewal idempotency and reconciliation convergence. Its focused and impacted H2 regression suite passed 122/122 before this final MySQL rerun.
- The final authorized rerun then passed all seven unchanged MySQL races. The earlier failures remain audit history only; they are not the current WI status.

## Security and Cleanup

- Repository evidence contains no JDBC URL, username, password, secret value, or generated database name.
- The runner's `finally` path reported `drop=PASS`.
- Independent post-drop inspection reported `cleanupDatabaseExists=0`.
- No retained/local, preview, stage, or production database was targeted.
- No live Toss operation or server/preview mutation occurred.

## Risks / Rollback

Risks:

- This proof is bounded to MySQL 8/InnoDB and the approved single-server topology; it does not claim behavior for another database engine or multi-server scheduler execution.
- It proves the seven specified interleavings and exact invariants, not every possible production schedule.

Rollback:

- WI-007 added test/support, runner, and evidence artifacts only. They can be removed without a production-code or database rollback.
- The separate production correction belongs to WI-20260715-ATS-008 and must be rolled back only with its own evidence and regression scope.

## Follow-up

- WI-20260715-ATS-007 is closed. The next approved chain step is independent payment/integration review; no further disposable MySQL rerun is required for this WI.
