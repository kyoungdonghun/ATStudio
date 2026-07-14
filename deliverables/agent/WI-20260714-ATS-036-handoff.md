[WI HEADER]
WI ID: WI-20260714-ATS-036
REQ: REQ-20260714-ATS-001
Agent: sa
Depends On: WI-20260714-ATS-007, WI-20260714-ATS-008, WI-20260714-ATS-018, WI-20260714-ATS-023
Blocks: WI-20260714-ATS-025, WI-20260714-ATS-026, WI-20260714-ATS-028, WI-20260714-ATS-034

[WI SUMMARY]
Why: Resolve the blocking payment-integrity findings from independent review with one precise transaction/state/locking design before implementation.
Scope: Design remediation contracts for WI-023 F-01 through F-05: stable renewal-period identity versus retry scheduling, provider calls outside broad local transactions for cancellation/withdrawal/reconciliation/charged upgrade, refund PROCESSING lease/stale recovery, provider-DONE finalize-only reconciliation, and canonical lock order/MySQL proof.
Out: Implementation, live provider calls, existing/production DB changes, new payment features, policy redesign, maker-checker expansion, or multi-server scheduling.
DoD: A code-addressable design specifies states/fields/indexes, transaction phases, retry/idempotency semantics, crash boundaries, lock order, purpose-specific reconciliation actions, tests, migration/rollback, and implementation WI split; no finding is hand-waved.
Constraints: Preserve card-only recurring subscription policy, single-server assumption, stable provider idempotency keys, no blind replay, and no real DB apply. Prefer existing entities/services and minimal additive schema. Explicitly distinguish detect-only incidents from mutation-capable recovery.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] One logical renewal period retains one command/order across deterministic retries while retry scheduling remains bounded by grace.
- [ ] Cancellation, withdrawal cleanup, provider reconciliation, and charged upgrade have no suspended broad transaction across network latency.
- [ ] Refund crash after PROCESSING claim converges using a dedicated lease and the same idempotency key without duplicate refund.
- [ ] Provider-DONE/local-not-finalized recovery has safe purpose-specific finalize-only preconditions and audit/incident behavior.
- [ ] Canonical lock order is implementable across upgrade, renewal, refund, cancellation, and reconciliation.
Quality:
- [ ] Every F-01~F-05 maps to exact proposed files/tests and a closure criterion.
- [ ] Required DDL/manual patch is additive, separately applied, and disposable-MySQL testable.
- [ ] H2 limitations and required MySQL/InnoDB concurrency cases are explicit.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-db-integrity-design.md
- deliverables/agent/WI-20260714-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-018-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-023-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-035-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java
- src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/20260714_payment_db_integrity.sql

[OUTPUT CONTRACT]
Design -> docs/design/p1-payment-integrity-remediation-design.md
User-facing -> deliverables/user/WI-20260714-ATS-036-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-036-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-036-handoff.md

[TRACEABILITY REQUIREMENTS]
Finding-to-contract matrix, state diagrams/tables, transaction boundaries, lock order, schema/index deltas, crash/retry examples, test matrix, implementation WI split, migration/rollback, and residual risks are required.
