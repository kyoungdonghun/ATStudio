[WI HEADER]
WI ID: WI-20260518-ATS-013
REQ: REQ-20260518-ATS-001
Agent: uv
Depends On: -
Blocks: WI-20260518-ATS-016

[WI SUMMARY]
Why: Define the user-facing payment UX scenario and state transitions now that one-time Toss payment and Toss billing-key recurring payment both exist.
Scope (in/out): In scope: subscription checkout entry, Toss one-time payment, Toss billing authorization, success/fail/cancel/return/retry states, page-fixed versus separated checkout recommendation. Out of scope: code implementation, refund, settlement, receipt automation, multi-PG implementation.
DoD: A recommended UX flow, fallback paths, and state labels are ready to be reflected into UI documentation.
Constraints/Forbidden: Do not change implementation files. Do not expose billingKey, authKey, customerKey, secret key, or raw PG payloads in user-facing copy.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] User checkout flow covers start, preparing, external PG interaction, success, failure, cancel, return, expired order, and retry.
- [ ] One-time payment and recurring billing authorization are distinguished without fragmenting the user journey.
- [ ] Recommendation is explicit: popup/modal/dedicated route/page-fixed tradeoff.
Performance:
- [ ] No runtime performance requirement; this is design-only.
Quality:
- [ ] Output can be linked from `docs/ui/screen-flow.md`.
- [ ] Follow-up SR candidates are separated from immediate UX design.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 2 (Tech Stack / UX):
- .agents/skills/react-best-practices/SKILL.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/SR/SR-92.md
- docs/design/payment-integration-design.md

REQ/Context Docs:
- deliverables/user/REQ-20260518-ATS-001.md
- deliverables/user/REQ-20260517-ATS-002.md

Files:
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/public/SubscriptionPlanPage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-013-summary.md :
- UX recommendation, state map summary, and open UX risks.
Agent-facing -> deliverables/agent/WI-20260518-ATS-013-evidence-pack.md :
- Evidence pointers, design decisions, affected docs, and follow-up WI.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-013-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Design-only; note documentation validation command if run.
Rollback (if needed): Revert documentation changes tied to this WI.
