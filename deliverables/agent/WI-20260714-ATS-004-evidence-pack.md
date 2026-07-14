# Evidence Pack: WI-20260714-ATS-004

## Summary (one-liner)

- Aligned payment command/finalization JPA mappings, fresh MySQL DDL, audit ENUMs, and a guarded retained-DB manual patch.

## Scope / DoD Check

- [x] Added `PROCESSING`, `PROVIDER_SUCCEEDED`, and `PENDING_PROVIDER_CONFIRMATION`.
- [x] Mapped command key, billing period start, Provider attempt/idempotency key, and processing start time.
- [x] Added one-payment-per-order and one-payment-per-non-null-provider-transaction constraints.
- [x] Added every current Java payment audit action and target to fresh and manual DDL.
- [x] Added an ordered manual patch with explicit preflight aborts and no destructive ledger cleanup.
- [x] Passed focused contract tests, `compileJava`, and owned-file whitespace checks.
- [x] Did not connect to or apply changes to a database and did not edit payment orchestration services.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability and approval boundary |
| 0 | `docs/standards/development-standards.md` | Java/JPA/MySQL and test standards |
| 0 | `docs/standards/documentation-standards.md` | Deliverable structure and language policy |
| 0 | `docs/standards/glossary.md` | Canonical WI and payment terminology |
| 1 | `docs/policies/quality-gates.md` | Validation and rollback evidence |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and approval boundaries |
| 2 | `docs/audit/p1-remediation-trace-matrix-20260714.md` | P1-05 through P1-09 traceability |
| 2 | `docs/design/p1-payment-db-integrity-design.md` | Exact model, DDL, patch, and rollback contract |
| 2 | `src/main/resources/schema.sql` | Fresh-schema baseline |
| 2 | `src/main/resources/db/manual/` | Existing ordered manual patches |
| 2 | `PaymentOrder.java`, `SubscriptionPayment.java`, payment ENUM files | Current Java mapping baseline |

**Injection rules applied:** assignee `se`; implementation task; Tier 0 first, then policy and WI context.

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:33` - named command/finalization constraints and processing index.
- `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:72` - persisted command fields.
- `src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java:13` - finalization uniqueness mappings and 200-character Provider transaction ID.
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java:3` - expanded command lifecycle states.
- `src/main/resources/schema.sql:485` - fresh payment command and finalization DDL.
- `src/main/resources/schema.sql:808` - Java-aligned payment audit action/target ENUMs.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:20` - retained-DB preflight and abort checks.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:214` - bounded legacy backfill and duplicate-period blocker.
- `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java:28` - focused mapping/schema/manual-patch contract tests.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest"`
  - Final result: PASS, 4 tests, `BUILD SUCCESSFUL`.
  - Red evidence: initial compile failed on the three missing status constants; the first Green run exposed two whitespace-sensitive SQL assertions, which were narrowed to semantic whitespace-normalized checks.
- `.\gradlew.bat compileJava`
  - PASS, `BUILD SUCCESSFUL`.
- `git diff --check -- <owned tracked files>`
  - PASS; no whitespace errors. Git emitted only LF-to-CRLF working-copy warnings.
- `git diff --no-index --check -- NUL <owned untracked file>`
  - No whitespace diagnostics for the manual patch and focused test. Exit code 1 is the expected no-index difference result.

## Risks / Rollback

- Risks:
  - The manual SQL was statically verified only; MySQL 8 execution and Hibernate validation remain deferred to `WI-20260714-ATS-021`.
  - Existing nonterminal orders, duplicate finalizations, renewal-period duplicates, or disagreement with the retained three-day grace assumption intentionally block the patch.
  - MySQL DDL implicitly commits; application rollback must precede any separately approved forward DB remediation.
- Rollback:
  - Revert application mappings first.
  - Retain expanded ENUM values, additive columns/indexes, payment ledgers, and audit rows on any database where the patch was separately applied.
  - Never contract ENUMs or delete ambiguous financial rows during incident rollback.

## Approvals

- Used: approved `REQ-20260714-ATS-001` and existing WI handoff.
- Not used: database connection/application, disposable DB provisioning, row disposition, Testcontainers/new dependency, or production access.
- Separate approval remains required for applying the patch and resolving any blocking retained rows.

## WI Chain Trigger

- Immediately generate handoffs with `/create-wi-handoff-packet` and delegate `WI-20260714-ATS-005`, `WI-20260714-ATS-006`, and `WI-20260714-ATS-007`.
- Release only the dependency edge for `WI-20260714-ATS-018` and `WI-20260714-ATS-021`; trigger them after their remaining prerequisites complete.
