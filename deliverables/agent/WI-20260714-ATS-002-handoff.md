[WI HEADER]
WI ID: WI-20260714-ATS-002
REQ: REQ-20260714-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260714-ATS-004 through WI-20260714-ATS-008, WI-20260714-ATS-015, WI-20260714-ATS-018, WI-20260714-ATS-021, WI-20260714-ATS-023, WI-20260714-ATS-025

[WI SUMMARY]
Why: Define implementable invariants for payment ledger durability, command idempotency, renewal isolation, refund serialization, and executable MySQL compatibility.
Scope (in/out):
- In: ATS020-P1-05 through ATS020-P1-10 and the payment/DB portion of ATS020-X-01.
- In: Current transaction boundaries, provider-call ordering, failure persistence, order uniqueness, billing-period identity, locking, retry semantics, refund reservation, ENUM/DDL alignment, and disposable-MySQL proof strategy.
- In: Exact schema/index changes, service boundaries, repository locking/query changes, migration/backfill assumptions, and test seams.
- Out: Implementation edits, live Toss calls, real DB changes, disposable DB creation, and new dependencies.
- Out: Multi-server locking, multi-PG expansion, and unrelated payment features.
DoD:
- Design defines one durable final outcome per payment command without assuming an external provider participates in the local transaction.
- Initial confirm, upgrade, renewal, and refund each have an explicit state machine, idempotency key, lock owner, transaction boundary, and failure recovery path.
- Renewal cannot reuse stale orders across periods or subscriptions and one agreement failure cannot roll back another agreement's local result.
- MySQL ENUM/index/constraint changes and manual-patch ordering are listed exactly, with legacy/backfill and rollback considerations.
- The design names all decisions requiring user approval before implementation.
Constraints/Forbidden:
- Read-only inspection except for the three WI deliverables listed in the output contract.
- Do not edit application code, schemas, tests, existing docs, or runtime logs.
- Do not introduce a library, Testcontainers, distributed lock, or new provider without approval.
- Do not expose or use live credentials; fake providers and disposable MySQL are design targets only.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] ATS020-P1-05 through P1-10 each map to a concrete invariant and implementation boundary.
- [ ] Confirm, upgrade, renewal, and refund include sequence diagrams or equivalent ordered steps.
- [ ] Transaction propagation and post-provider failure persistence are explicit and testable.
- [ ] Locking and unique constraints prevent duplicate finalization and refund over-reservation.
- [ ] DDL changes align Java ENUM values and identify ordered migration/manual-patch updates.
- [ ] Disposable MySQL validation method is proposed without executing or creating a DB.
Performance:
- [ ] Lock scope is bounded to a command, agreement, order, or source payment and avoids table-wide serialization.
- [ ] Renewal design preserves per-agreement progress and does not hold one transaction across all provider calls.
Quality:
- [ ] Design follows Spring service transaction and DTO/entity boundaries.
- [ ] Existing data compatibility, rollback, retry, and observability are included.
- [ ] `git diff --check` passes for the WI deliverables.

[INPUT POINTERS]
Tier 0 (Constitution and development standards):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Architecture and quality):
- docs/architecture/system-design.md
- docs/policies/quality-gates.md
- docs/adr/

Tier 2 (Payment contracts):
- docs/design/payment-integration-design.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-settlement-import-design.md
- docs/design/usecase/user-subscription.md
- docs/payment/

REQ/Context Docs:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/audit/full-system-audit-20260713.md
- docs/audit/p1-remediation-trace-matrix-20260714.md (consume if available; do not block on parallel WI)

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOperationAuditLog.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditAction.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOperationAuditTargetType.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/test/java/com/atstudio/atstudio/service/

Repro/Logs:
- `rg -n "@Transactional|REQUIRES_NEW|find.*ForUpdate|orderId|failureCode|PaymentOperationAudit" src/main/java/com/atstudio/atstudio`
- `rg -n "payment_operation_audit_logs|payment_orders|payment_refunds|ENUM|UNIQUE" src/main/resources/schema.sql src/main/resources/db/manual`
- `git diff --check`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-002-summary.md:
- Korean explanation of the chosen payment/DB contracts, risks, and approval points.
Agent-facing -> deliverables/agent/WI-20260714-ATS-002-evidence-pack.md:
- Evidence pointers, alternatives considered, exact impacted symbols/files, test plan, migration implications, and next-WI triggers.
Handoff Packet -> deliverables/agent/WI-20260714-ATS-002-handoff.md:
- This packet.
Additional artifact -> docs/design/p1-payment-db-integrity-design.md:
- English implementation-ready architecture contract for P1-05 through P1-10.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Specify focused unit/integration/concurrency tests and disposable-MySQL proof; do not run live-provider tests.
Rollback: Document reversible code/schema sequencing and retained-ledger behavior; revert only WI-owned deliverables at this stage.
