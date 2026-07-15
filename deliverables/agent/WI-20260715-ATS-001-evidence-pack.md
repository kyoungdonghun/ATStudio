# Evidence Pack: WI-20260715-ATS-001

## Summary (one-liner)

- Completed Package A's additive payment entity/schema foundation and corrected the manual patch so Package A ambiguity checks abort before Package A columns or data repair are applied.

## Scope / DoD Check

- [x] `BillingAgreement` represents immutable renewal-period retry scheduling and billing-key cleanup state/lease transitions.
- [x] `PaymentOrder` persists the upgrade target billing cycle and exposes a reconciliation-safe provider-success transition.
- [x] `PaymentRefund` persists a second-precision processing lease, fences lease-bearing result transitions, and clears the lease on terminal/pending outcomes.
- [x] Fresh `schema.sql` contains the exact Package A columns, ENUM values, and candidate indexes from design Section 9.1.
- [x] The manual patch lists and aborts ambiguous legacy renewal rows before Package A column creation and repair, then adds indexes only after repair/backfill.
- [x] The manual patch does not backfill historical terminal upgrade targets and does not delete, merge, or finalize payment/refund rows.
- [x] Focused entity/DDL contract tests pass (8 tests, 0 failures/errors/skips).
- [x] Production Java compilation passes.
- [x] `git diff --check` passes for tracked changes; no-index whitespace checks pass for new owned files.
- [x] No retained, local, production, copied, or disposable database was connected to or mutated. No preview server was started or changed.

## Reference Documents (Tier 0-2)

**Injected Context** (from the WI handoff packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Platform integrity, approval, traceability, and financial auditability |
| 0 | `docs/standards/development-standards.md` | Java/JPA, MySQL, testing, and Evidence Pack standards |
| 0 | `docs/standards/documentation-standards.md` | Documentation structure and canonical pointer rules |
| 0 | `docs/standards/glossary.md` | Canonical ATStudio and WI terminology |
| 1 | `docs/policies/security-policy.md` | Secret, billing-key, DB credential, and logging boundaries |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation, rollback, and evidence requirements |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved payment-integrity remediation scope and DB restrictions |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | Package A entity contract, Section 9 patch order, and Section 12 ownership |
| Context | `docs/design/p1-payment-db-integrity-design.md` | Existing payment command and manual patch baseline |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | Approved remediation design and A-G dependency chain |
| Evidence | `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md` | Source findings F-01 through F-05 |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-001-handoff.md`
- Assignee context: `se` (interrupted implementation takeover)
- Task type: payment-integrity implementation and focused review
- Required context: Tier 0 first, then policy/context/evidence pointers from the approved handoff

## Review Corrections

- Split the existing command-column/backfill prerequisite from the Package A phase in `20260714_payment_db_integrity.sql`.
- Moved the exact legacy retry candidate listing and abort procedure ahead of every Package A column addition and Package A data repair.
- Kept the preflight free of Package A column references, while allowing the existing patch to create/backfill `billing_period_start`, which the exact legacy retry predicate requires.
- Tightened the exact candidate predicate so both the payment order and linked user subscription belong to the billing agreement's user.
- Extended the static contract test to prove the baseline-preparation, Package A preflight, A-column, repair/backfill, index, and post-validation order.

## Evidence Pointers (required)

- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:91-113` - retry and cleanup mappings.
- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:199-302` - immutable due-period retry and cleanup state/lease transitions.
- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:318-333` - retained-key and lease fencing prerequisites.
- `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:103-109` - persisted upgrade target cycle.
- `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:164-237` - purpose validation and reconciliation provider-success transition.
- `src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java:293-300` - upgrade-only target-cycle invariant.
- `src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java:117-124` - refund processing lease mapping.
- `src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java:133-247` - second-precision claim/reclaim, result fencing, and lease clearing.
- `src/main/java/com/atstudio/atstudio/entity/enums/BillingKeyCleanupStatus.java:1-9` - exact cleanup enum values.
- `src/main/resources/schema.sql:469-491` - fresh billing agreement columns and indexes.
- `src/main/resources/schema.sql:507-510` - fresh upgrade target cycle.
- `src/main/resources/schema.sql:648-660` - fresh refund lease and candidate index.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:20-178` - baseline inventory, stop conditions, retained-key abort, and refund review list.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:180-324` - idempotent prerequisite command columns and legacy command identity.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:326-437` - exact Package A legacy retry list and pre-mutation abort.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:439-497` - Package A columns followed by exact repair/refund backfill.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:499-638` - final ambiguity checks and post-repair indexes.
- `src/main/resources/db/manual/20260714_payment_db_integrity.sql:640-675` - post-apply column type and index-order inventory.
- `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java:88-226` - entity, state, lease, and mapping contracts.
- `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java:286-357` - fresh/manual DDL safety and ordering contract.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest"`
  - Result: PASS; `BUILD SUCCESSFUL in 4s`.
  - Report: `build/test-results/test/TEST-com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest.xml` records 8 tests, 0 failures, 0 errors, 0 skipped.
- `.\gradlew.bat compileJava`
  - Result: PASS; `BUILD SUCCESSFUL in 1s` (`compileJava` up-to-date after the focused test compilation).
- `git diff --check`
  - Result: PASS; no whitespace diagnostics. Git emitted only the repository's LF-to-CRLF working-copy notices.
- `git diff --no-index --check -- NUL <new-owned-file>` for the enum and both completion documents
  - Result: PASS; no whitespace diagnostics. Exit code 1 is expected for new no-index content.

## Tests

- Focused contract: PASS, 8/8.
- Covered: JPA fields/indexes, exact enum values, immutable renewal date behavior, cleanup lease transitions, upgrade target invariant, reconciliation transition, refund stale-result fencing, fresh DDL, and manual patch order/non-destructiveness.
- Not run by scope: full Gradle suite, MySQL/Hibernate validation, copied-DB rehearsal, live Toss, retained/local/production DB operations, or preview/public server smoke tests.

## Risks / Rollback

Risks:

- Package A is an additive foundation, not closure of F-01 through F-05. Downstream command, cleanup, refund, and reconciliation services must consume these fields and lease-bearing transitions before release approval.
- Existing command columns and `billing_period_start` backfill remain prerequisites inside the idempotent manual patch; Package A ambiguity aborts occur before Package A-specific DDL/data mutation, not before that retained baseline preparation.
- The manual patch has static contract proof only in this WI. Applicability and Hibernate validation still require a separately approved copied/disposable MySQL 8 environment.
- Historical terminal `UPGRADE` rows intentionally retain `upgrade_target_billing_cycle = NULL`; they are not recovery candidates.

Rollback:

- No database patch was executed, so this WI has no database state to roll back.
- Before downstream packages integrate, application changes can be reverted by reversing only the Package A entity/enum/schema/manual-patch/test files listed in the handoff.
- If additive DDL is later applied in an approved environment, roll back application behavior first and preserve added columns, indexes, ENUM members, command identity, audit rows, and repair evidence.
- Do not drop or contract payment schema during an incident. Restore/discard an approved copied database after a failed rehearsal instead.

## Follow-ups

- Package A now unblocks `WI-20260715-ATS-002` and `WI-20260715-ATS-005` per the handoff chain.
- Downstream package owners must set `upgradeTargetBillingCycle` for new upgrade orders and pass the persisted refund `leaseStartedAt` into every result writer.
- Copied/disposable MySQL 8 patch rehearsal and Hibernate `ddl-auto=validate` remain separately approved proof work; they must not target retained/local/production data.
