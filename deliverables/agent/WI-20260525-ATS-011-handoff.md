[WI HEADER]
WI ID: WI-20260525-ATS-011
REQ: REQ-20260525-ATS-005
Agent: re/qa-integ
Depends On: WI-20260525-ATS-009
Blocks: WI-20260525-ATS-013

[WI SUMMARY]
Why: Entitlement correction directly affects paid access and renewal state, so focused tests must cover the risky transitions.
Scope (in/out): Define and implement backend tests for correction preview/request/approval/execution and non-provider mutation boundaries. Exclude frontend tests unless frontend code changes.
DoD: Tests cover approval gate, explicit target mutation, billing agreement local cancel, audit events, and no provider call.
Constraints/Forbidden: Do not rely on external Toss/live systems.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Preview does not mutate.
- [ ] Unapproved correction cannot execute.
- [ ] Execution applies target state and clears pending when requested.
- [ ] Local billing agreement cancel is optional and provider-free.
- [ ] Audit event writer is invoked.
Performance:
- [ ] Unit tests are deterministic.
Quality:
- [ ] Full backend tests pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 (Context):
- deliverables/user/REQ-20260525-ATS-005.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java
- src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-011-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-011-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Required
Rollback: Document test removal if implementation rolls back
