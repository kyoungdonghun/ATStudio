---
wi_id: WI-20260519-ATS-001
req_id: REQ-20260519-ATS-001
agent: sa
status: ready
created_at: 2026-05-19
---

# WI-20260519-ATS-001 Handoff: Recurring Subscription Change Policy

[WI HEADER]
WI ID: WI-20260519-ATS-001
REQ: REQ-20260519-ATS-001
Agent: sa
Depends On: -
Blocks: WI-20260519-ATS-002, WI-20260519-ATS-003, WI-20260519-ATS-004

[WI SUMMARY]
Why: Align subscription plan changes with the recurring billing policy.
Scope (in/out): Confirm the target behavior for upgrade, downgrade, one-time checkout removal from user-facing subscription flows, and SR-92 retirement. Exclude implementation details beyond design boundaries.
DoD: Backend, frontend, and documentation work can proceed from one policy: upgrade charges prorated difference through an active billing agreement; downgrade is pending until next renewal.
Constraints/Forbidden: Do not reintroduce one-time Toss Widget as the user-facing upgrade path.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Upgrade uses an existing billing agreement and immediate recurring charge.
- [ ] Upgrade preserves the current next billing date.
- [ ] Downgrade is scheduled with no immediate charge.
- [ ] Plan change UI remains preview-first and confirm-driven.
Quality:
- [ ] Current findings are traceable to code and docs.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260519-ATS-001.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-subscription.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/SR/SR-92.md

Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260519-ATS-001-summary.md
Agent-facing -> deliverables/agent/WI-20260519-ATS-001-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260519-ATS-001-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Not required for design-only WI
Rollback: Revert this WI's documentation/status notes if policy changes
