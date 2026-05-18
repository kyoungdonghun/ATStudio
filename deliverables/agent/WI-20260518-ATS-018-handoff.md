[WI HEADER]
WI ID: WI-20260518-ATS-018
REQ: REQ-20260518-ATS-001
Agent: qa-fe
Depends On: WI-20260518-ATS-016
Blocks: WI-20260518-ATS-020

[WI SUMMARY]
Why: Identify frontend implementation impact and future test coverage needed for the payment UX stabilization design.
Scope (in/out): In scope: route impact, payment page/modal/dedicated route candidates, subscription management state display, frontend tests. Out of scope: implementation.
DoD: Future frontend work can be estimated and tested without rediscovering the current code structure.
Constraints/Forbidden: Do not edit frontend implementation files in this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Frontend affected files and routes are listed.
- [ ] Test candidates cover recurring auth, one-time payment, failure callback, cancel/return, and manage-page billing state.
- [ ] Current page-fixed debug behavior is documented as temporary, not removed.
Performance:
- [ ] No runtime performance requirement; this is design QA.
Quality:
- [ ] Existing frontend validation commands remain identified.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 2 (React):
- .agents/skills/react-best-practices/SKILL.md
- docs/standards/frontend-standards.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- deliverables/user/REQ-20260518-ATS-001.md

Files:
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx
- frontend/src/router/index.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260518-ATS-018-summary.md :
- Frontend impact and test candidate summary.
Agent-facing -> deliverables/agent/WI-20260518-ATS-018-evidence-pack.md :
- Affected files, risk notes, and future validation commands.
Handoff Packet -> deliverables/agent/WI-20260518-ATS-018-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Design-only; list future `npm test`, `npm run typecheck`, and `npm run lint` scope.
Rollback (if needed): Revert docs tied to this WI.
