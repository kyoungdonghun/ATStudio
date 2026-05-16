---
wi_id: WI-20260517-ATS-002
req_id: REQ-20260517-ATS-001
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-002 Handoff: Frontend Toss Checkout

[WI HEADER]
WI ID: WI-20260517-ATS-002
REQ: REQ-20260517-ATS-001
Agent: se
Depends On: WI-20260517-ATS-001
Blocks: WI-20260517-ATS-003, WI-20260517-ATS-004

[WI SUMMARY]
Why: Let subscription purchase and upgrade flows handle Toss checkout in addition to Mock checkout.
Scope (in/out): Add Toss checkout metadata types, SDK script loading, success/fail redirect handling, and tests. Exclude custom payment-method UI and recurring billing UX.
DoD: Mock tests remain green; Toss success URL confirms payment through backend; Toss fail URL closes order without subscription mutation.
Constraints/Forbidden: Do not put secret keys in frontend. Do not assume Toss SDK is available during tests.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Payment page branches by `checkout.type`.
- [ ] Toss checkout uses provider-returned client key, order ID, order name, amount, success URL, and fail URL.
- [ ] Toss success redirect calls backend confirm with `paymentKey`.
- [ ] Toss fail redirect calls backend cancel/fail path.
- [ ] Upgrade flow rejects unsupported direct Toss confirm without redirect metadata.
Performance:
- [ ] SDK script is loaded once and reused.
Quality:
- [ ] Focused frontend tests pass.
- [ ] Typecheck passes.
- [ ] Touched files pass Prettier.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 2 (Tech Stack):
- docs/standards/frontend-standards.md
- .agents/skills/react-best-practices/AGENTS.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-001.md
- docs/design/payment-integration-design.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md

Files:
- frontend/src/api/payments.ts
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/subscriber/*Payment*.test.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-002-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-002-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260517-ATS-002-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include command and result
Rollback: Document files to revert
