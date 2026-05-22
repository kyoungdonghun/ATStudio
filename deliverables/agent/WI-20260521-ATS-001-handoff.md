[WI HEADER]
WI ID: WI-20260521-ATS-001
REQ: REQ-20260521-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260521-ATS-003, WI-20260521-ATS-004, WI-20260521-ATS-005, WI-20260521-ATS-006

[WI SUMMARY]
Why: Confirm the operating hardening architecture and the exact one-time subscription payment removal boundary.
Scope (in/out): In scope: recurring-only subscription policy, checkout/callback target, legacy one-time subscription boundary, scheduler/reconciliation shape. Out of scope: implementation.
DoD: Backend, frontend, and documentation work can proceed from one clear policy.
Constraints/Forbidden: Do not reintroduce user-facing one-time subscription checkout.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] User-facing subscription purchase uses recurring billing only.
- [ ] One-time UPGRADE is blocked at backend boundary.
- [ ] One-time SUBSCRIBE removal or isolation boundary is explicit.
Quality:
- [ ] Architecture notes map to REQ approval points.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/SR/SR-93.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/public/SubscriptionPlanPage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Not required for design-only WI
Rollback: Document impacted docs/files
