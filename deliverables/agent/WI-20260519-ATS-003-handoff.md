---
wi_id: WI-20260519-ATS-003
req_id: REQ-20260519-ATS-001
agent: se
status: ready
created_at: 2026-05-19
---

# WI-20260519-ATS-003 Handoff: Frontend Plan Change Flow

[WI HEADER]
WI ID: WI-20260519-ATS-003
REQ: REQ-20260519-ATS-001
Agent: se
Depends On: WI-20260519-ATS-001
Blocks: WI-20260519-ATS-005

[WI SUMMARY]
Why: The UI must stop sending upgrades to the one-time checkout page.
Scope (in/out): Update subscription manage page behavior, preview copy, direct upgrade payment route guard, and frontend tests. Exclude broad visual redesign.
DoD: Upgrade confirm calls `PUT /api/user-subscriptions/me`; downgrade still schedules pending change.
Constraints/Forbidden: Do not render one-time Toss Widget for subscription upgrade.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Upgrade confirm calls `changeMySubscription`.
- [ ] Upgrade success message reflects immediate prorated charge and next billing date preservation.
- [ ] Downgrade pending message remains clear.
- [ ] Direct `/subscriptions/payment?purpose=UPGRADE` does not prepare one-time payment.
Quality:
- [ ] Focused frontend tests and typecheck pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260519-ATS-001.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

Files:
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260519-ATS-003-summary.md
Agent-facing -> deliverables/agent/WI-20260519-ATS-003-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260519-ATS-003-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused frontend test/typecheck results
Rollback: Revert changed frontend files and tests
