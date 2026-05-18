[WI HEADER]
WI ID: WI-20260518-ATS-019
REQ: REQ-20260518-ATS-001
Agent: re
Depends On: WI-20260518-ATS-017
Blocks: WI-20260518-ATS-020

[WI SUMMARY]
Why: Identify backend regression risk and future test coverage needed for payment UX and operations stabilization.
Scope (in/out): In scope: payment order recovery, expired order retry, billing agreement failure states, renewal retry/grace, admin read-only operations test candidates. Out of scope: implementation.
DoD: Future backend work has focused test targets and known regression risks.
Constraints/Forbidden: Do not edit backend implementation files in this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Backend risk areas are mapped to current service/entity files.
- [ ] Test candidates cover one-time payment, recurring billing prepare/confirm/cancel, renewal failure, and operator query candidates.
- [ ] Idempotency and duplicate charge prevention remain explicit concerns.
Performance:
- [ ] No runtime performance requirement; this is design QA.
Quality:
- [ ] Existing backend validation command remains identified.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Quality):
- docs/policies/quality-gates.md
- docs/standards/evidence-pack-standard.md

Tier 2 / Context:
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- deliverables/user/REQ-20260518-ATS-001.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java
- src/test/java/com/atstudio/atstudio/service/RecurringRenewalServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-019-summary.md :
- Backend impact and regression test candidate summary.
Agent-facing -> deliverables/agent/WI-20260518-ATS-019-evidence-pack.md :
- Affected files, risk notes, and future validation commands.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-019-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Design-only; list future `./gradlew.bat test` scope.
Rollback (if needed): Revert docs tied to this WI.
