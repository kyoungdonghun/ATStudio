[WI HEADER]
WI ID: WI-20260724-ATS-015
REQ: REQ-20260724-ATS-002
Agent: qa-integ
Depends On: WI-20260724-ATS-014
Blocks: WI-20260724-ATS-017

[WI SUMMARY]
Why: Validate the real Toss integration boundary with test-only transactions.
Scope (in/out): After a secret-safe key classification proves Toss test configuration, use dedicated QA accounts and the isolated DB to verify the executable recurring-payment checklist: billing auth/card registration, first charge, upgrade, pending downgrade/cycle change, cancellation/reactivation, an explicitly prepared renewal, refund request/approve/execute, receipt/audit evidence, reconciliation, and billing-key cleanup where safely reproducible.
DoD: Every executed flow records provider/local parity; non-automatable or unsafe scenarios are classified rather than simulated as success.
Constraints/Forbidden: Fail closed on unknown/live keys. Refund only a payment created in this rehearsal. Never print provider keys, auth/customer keys, billing keys, raw card data, secret values, or exact provider identifiers. No production/live money.

[ACCEPTANCE CRITERIA]
- [ ] Test-only key gate passes before Provider mutation.
- [ ] Initial recurring subscription charge and at least one plan-change path succeed.
- [ ] Cancellation/reactivation and pending-change behavior match policy.
- [ ] Test refund persists idempotent ledger and Toss cancellation parity.
- [ ] Receipt/audit/reconciliation/admin evidence is support-safe.
- [ ] Scheduler/cleanup scenarios are executed only with deterministic preparation; otherwise explicitly deferred.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/payment/acceptance-test-checklist.md
- docs/payment/user-flows.md
- docs/payment/admin-operations-guide.md
- docs/design/payment-operations-runbook.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- deliverables/agent/WI-20260724-ATS-014-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-015-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-015-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record support-safe references, state transitions, amounts as test evidence, executed/deferred matrix, failure recovery, and cleanup ownership without Provider secrets.
