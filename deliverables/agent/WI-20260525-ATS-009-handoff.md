[WI HEADER]
WI ID: WI-20260525-ATS-009
REQ: REQ-20260525-ATS-005
Agent: sa/pg
Depends On: -
Blocks: WI-20260525-ATS-010, WI-20260525-ATS-011

[WI SUMMARY]
Why: Refund execution now exists, but local subscription entitlement correction remains manual and unsafe if done through ad hoc DB edits.
Scope (in/out): Define a safe admin-only entitlement correction model linked to refund records. Exclude user refund request UI, admin UI tab, provider billing-key deletion, settlement, tax invoice, and cash receipt work.
DoD: Correction states, explicit target-state policy, audit boundaries, and tests are clear enough for implementation.
Constraints/Forbidden: Do not infer a previous plan from incomplete historical data. Do not automatically execute entitlement correction after refund. Do not expose secrets or raw provider/card data.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Correction requires a linked refund and explicit target subscription state.
- [ ] Preview shows before/after state without mutation.
- [ ] Request/approve/execute boundaries are defined.
- [ ] Local billing agreement cancel is separate from provider billing-key deletion.
Performance:
- [ ] Admin read APIs remain pageable.
Quality:
- [ ] Design is traceable to REQ-20260525-ATS-005.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md

Tier 2 (Context):
- deliverables/user/REQ-20260525-ATS-005.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/SR/SR-93.md

Files:
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-009-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260525-ATS-009-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260525-ATS-009-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: If applicable, include test command and results
Rollback: Document how to revert design/docs changes
