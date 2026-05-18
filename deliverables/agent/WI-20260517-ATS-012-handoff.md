---
wi_id: WI-20260517-ATS-012
req_id: REQ-20260517-ATS-002
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-012 Handoff: Frontend Billing Registration and Management UX

[WI HEADER]
WI ID: WI-20260517-ATS-012
REQ: REQ-20260517-ATS-002
Agent: se
Depends On: WI-20260517-ATS-010, WI-20260517-ATS-011
Blocks: WI-20260517-ATS-013

[WI SUMMARY]
Why: Users need a frontend path to register Toss Billing automatic payment, confirm success redirects, inspect billing agreement state, and cancel automatic renewal.
Scope (in/out): Add frontend API client types/functions, Toss Billing SDK helper, billing success/fail routes, subscription payment page recurring mode, and subscription management billing status/cancel controls. Exclude SR-92 modal checkout separation and live-key/manual production testing.
DoD: Authenticated users can start recurring billing registration from subscription plans, return from Toss billing auth success/fail, confirm backend billing agreement, and cancel automatic renewal from the subscription management page without exposing raw billing keys.
Constraints/Forbidden: Do not expose Toss secret key or raw billing key. Do not remove existing Mock/Toss one-time payment flow. Keep SR-92 checkout UX separation as a later SR.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Add payment API functions for billing agreement prepare, confirm, current state, and cancel.
- [ ] Add Toss SDK helper for `payment({ customerKey }).requestBillingAuth(...)`.
- [ ] Add `/subscriptions/billing/success` and `/subscriptions/billing/fail` routes.
- [ ] Subscription plan CTA can enter recurring billing mode for new subscriptions.
- [ ] Billing success redirect confirms `authKey`, `customerKey`, `orderId`, and amount with backend.
- [ ] Subscription management page shows current billing agreement state and automatic renewal cancel action.
- [ ] Existing one-time payment and mock payment flows continue to work.
Quality:
- [ ] Focused frontend tests cover recurring prepare, billing redirect confirm, and cancel state.
- [ ] `npm test` focused tests pass.
- [ ] `npm run typecheck` passes.
- [ ] `npm run build` passes.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

Tier 2 (Tech Stack):
- .agents/skills/react-best-practices/SKILL.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- deliverables/agent/WI-20260517-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-011-evidence-pack.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/ui/screen-flow.md

Files:
- frontend/src/api/payments.ts
- frontend/src/utils/tossPayments.ts
- frontend/src/pages/public/SubscriptionPlanPage.tsx
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/router/index.tsx
- frontend/src/pages/subscriber/*.test.tsx

External References:
- https://docs.tosspayments.com/guides/v2/billing/integration
- https://docs.tosspayments.com/sdk/v2/js

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-012-summary.md :
- Summary, UI behavior, risks, verification
Agent-facing -> deliverables/agent/WI-20260517-ATS-012-evidence-pack.md :
- Evidence pointers, tests, rollback, downstream notes
Handoff Packet -> deliverables/agent/WI-20260517-ATS-012-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused frontend tests, typecheck, and build
Rollback: Document frontend/API files to revert
