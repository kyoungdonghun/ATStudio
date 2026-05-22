[WI HEADER]
WI ID: WI-20260521-ATS-005
REQ: REQ-20260521-ATS-001
Agent: se
Depends On: WI-20260521-ATS-001
Blocks: WI-20260521-ATS-009, WI-20260521-ATS-010

[WI SUMMARY]
Why: Move subscription purchase UX to a recurring checkout/callback flow and remove one-time subscription checkout UI paths.
Scope (in/out): In scope: frontend routing, subscription plan navigation, payment page behavior, missing billing agreement guidance. Out of scope: broad visual redesign.
DoD: New subscriptions enter recurring checkout and one-time Toss widget is not rendered for subscription purchase/change.
Constraints/Forbidden: Do not expose authKey/customerKey raw values in screen copy.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Subscription plan page routes new purchase to recurring checkout.
- [ ] Billing callback success/fail routes are handled.
- [ ] One-time Toss widget branch is not reachable for subscription scope.
- [ ] Missing billing agreement upgrade case guides to payment method registration.
Quality:
- [ ] Frontend tests pass.
- [ ] Typecheck and ESLint pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 2:
- .agents/skills/react-best-practices/AGENTS.md
- docs/standards/frontend-standards.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

Files:
- frontend/src/router/index.tsx
- frontend/src/pages/public/SubscriptionPlanPage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/api/payments.ts

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-005-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused Vitest commands and results
Rollback: Document route/UI changes
