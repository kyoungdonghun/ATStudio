[WI HEADER]
WI ID: WI-20260715-ATS-001
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-036
Blocks: WI-20260715-ATS-002, WI-20260715-ATS-005

[WI SUMMARY]
Why: Establish the additive entity and database foundation required to remediate payment-integrity findings F-01 through F-05 without touching retained databases.
Scope (in): Implement design package A only: `BillingAgreement`, `PaymentOrder`, and `PaymentRefund` fields/transitions; new `BillingKeyCleanupStatus`; fresh `schema.sql`; ordered idempotent manual patch; focused static/entity tests and copied/disposable-DB-safe validation artifacts.
Scope (out): Payment orchestration, provider calls, renewal behavior, cancellation/withdrawal flow, upgrade flow, refund orchestration, reconciliation, retained/local/production DB mutation, and live Toss.
DoD: Entity and DDL contracts match Section 9; the manual patch performs preflight/abort, column creation, exact repair/backfill, index creation, and post-validation in that order; focused tests pass; two-set deliverables are written.
Constraints/Forbidden: Do not execute the patch against any retained/local/production DB. Do not change provider/service orchestration. Do not expose secrets or DB credentials. Preserve unrelated work and the four untracked runtime logs. You are not alone in the codebase; do not revert others' changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `renewal_retry_at`, cleanup state/lease, upgrade target cycle, and refund processing lease are represented consistently in JPA and fresh DDL.
- [ ] Entity transitions enforce immutable renewal-period identity, cleanup state validity, persisted upgrade cycle, and refund lease clearing/fencing prerequisites without implementing downstream orchestration.
- [ ] The existing-DB patch never references a new column before creating it and aborts ambiguous legacy rows before mutation.
- [ ] New indexes and enum values exactly match the approved design.
Performance:
- [ ] New candidate indexes support the exact predicates defined in the design without broad table-scan fallbacks in the schema contract.
Quality:
- [ ] Focused entity/DDL contract tests pass.
- [ ] Java compile passes for changed production files.
- [ ] `git diff --check` passes for owned files.

[INPUT POINTERS]
Tier 0 (Constitution - Required):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-integrity-remediation-design.md
- deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
- docs/design/p1-payment-db-integrity-design.md

Files:
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/entity/enums/BillingKeyCleanupStatus.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/20260714_payment_db_integrity.sql
- src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260715-ATS-001-summary.md:
- Korean summary, behavior impact, risks, and downstream dependencies.
Agent-facing -> deliverables/agent/WI-20260715-ATS-001-evidence-pack.md:
- Exact evidence pointers, patch ordering proof, test commands/results, rollback, and follow-up WIs.
Handoff Packet -> deliverables/agent/WI-20260715-ATS-001-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Record exact focused commands and results. A test must not mutate a retained database.
Rollback: Document application rollback separately from additive schema preservation; never recommend destructive schema contraction during an incident.
