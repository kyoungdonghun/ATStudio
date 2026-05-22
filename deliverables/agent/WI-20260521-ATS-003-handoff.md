[WI HEADER]
WI ID: WI-20260521-ATS-003
REQ: REQ-20260521-ATS-001
Agent: se
Depends On: WI-20260521-ATS-001, WI-20260521-ATS-002
Blocks: WI-20260521-ATS-008, WI-20260521-ATS-010

[WI SUMMARY]
Why: Remove or block one-time subscription payment paths and guide missing billing-agreement upgrades to payment-method registration.
Scope (in/out): In scope: backend payment service/controller behavior, tests, API contract adjustments. Out of scope: frontend UX and admin payment screens.
DoD: User-facing subscription payment cannot proceed through MOCK/TOSS one-time subscription checkout.
Constraints/Forbidden: Keep recurring billing provider abstraction intact. Do not delete unrelated provider classes unless tests and docs prove they are unused outside subscription scope.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend rejects one-time subscription UPGRADE.
- [ ] User-facing one-time SUBSCRIBE is removed or explicitly invalid.
- [ ] Missing billing agreement for upgrade returns a clear recoverable error.
Quality:
- [ ] Backend focused tests pass.
- [ ] No secret or raw billing data is exposed.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/usecase/user-subscription.md

Files:
- src/main/java/com/atstudio/atstudio/controller/PaymentController.java
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/test/java/com/atstudio/atstudio/service/PaymentApplicationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused backend test commands and results
Rollback: Document files to revert
