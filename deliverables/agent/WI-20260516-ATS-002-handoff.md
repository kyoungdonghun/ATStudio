[WI HEADER]
WI ID: WI-20260516-ATS-002
REQ: REQ-20260516-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260516-ATS-004

[WI SUMMARY]
Why: Replace the direct subscribe UI call with a provider-neutral mock checkout flow.
Scope (in): Add frontend payment API client functions, update subscription payment page to prepare and confirm mock payments, and expose success/failure/cancel actions.
Scope (out): Toss widget SDK, billing key UI, admin screens, profile subscription tab redesign.
DoD: The subscription payment page no longer calls `subscribe()` for user-facing purchase flow and can exercise mock success/failure/cancel states.
Constraints/Forbidden: Do not remove admin/user subscription management API functions. Do not introduce real payment SDKs in this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Frontend has typed payment prepare, confirm, and cancel/fail API functions.
- [ ] `SubscriptionPaymentPage` prepares a payment order before enabling mock confirmation.
- [ ] Mock success confirms payment and navigates to `/subscriptions/manage`.
- [ ] Mock failure/cancel shows recoverable UI and does not navigate as success.
- [ ] User-facing payment flow does not call `subscribe()`.
Performance:
- [ ] No blocking external SDK load is added.
Quality:
- [ ] TypeScript typecheck passes.
- [ ] Relevant frontend tests pass or are added.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 2 (Tech Stack):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/SKILL.md

Tier 2 (Design/Context):
- docs/design/payment-integration-design.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

REQ/Context Docs:
- deliverables/user/REQ-20260516-ATS-001.md

Files:
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.module.css
- frontend/src/api/userSubscriptions.ts
- frontend/src/api/subscriptions.ts
- frontend/src/router/index.tsx

Repro/Logs:
- `npm test -- SubscriptionPaymentPage.test.tsx`
- `npm run typecheck`

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260516-ATS-002-summary.md:
- Summary, UX behavior, risks, approval points.
Agent-facing -> deliverables/agent/WI-20260516-ATS-002-evidence-pack.md:
- Evidence pointers, patch notes, repro and test results, rollback notes.
Handoff Packet -> deliverables/agent/WI-20260516-ATS-002-handoff.md:
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required.
Tests: Include frontend typecheck/test command and result.
Rollback: Document how to revert payment page/API client changes.
