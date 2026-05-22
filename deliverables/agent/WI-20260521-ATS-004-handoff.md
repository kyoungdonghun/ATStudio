[WI HEADER]
WI ID: WI-20260521-ATS-004
REQ: REQ-20260521-ATS-001
Agent: se
Depends On: WI-20260521-ATS-001, WI-20260521-ATS-002
Blocks: WI-20260521-ATS-008, WI-20260521-ATS-010

[WI SUMMARY]
Why: Add payment order expiration cleanup and reconciliation scaffolding for provider/local ledger mismatch detection.
Scope (in/out): In scope: scheduler/service/repository support and tests. Out of scope: live Toss settlement automation and refund mutation.
DoD: Expired orders can be closed safely and reconciliation can report mismatches without mutating provider state.
Constraints/Forbidden: Single-server scheduler assumption; no distributed lock in this REQ.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Expired READY/IN_PROGRESS payment orders become EXPIRED by scheduler.
- [ ] Reconciliation job produces sanitized mismatch records or logs.
- [ ] Done/cancelled/failed orders are not incorrectly changed.
Quality:
- [ ] Scheduler/service tests pass.
- [ ] Idempotent repeated run behavior is covered.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/SR/SR-93.md
- docs/design/payment-integration-design.md

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-004-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-004-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-004-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused scheduler tests
Rollback: Document scheduler/repository changes
