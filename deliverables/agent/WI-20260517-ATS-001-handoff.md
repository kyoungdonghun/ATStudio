---
wi_id: WI-20260517-ATS-001
req_id: REQ-20260517-ATS-001
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-001 Handoff: Backend Toss Provider

[WI HEADER]
WI ID: WI-20260517-ATS-001
REQ: REQ-20260517-ATS-001
Agent: se
Depends On: -
Blocks: WI-20260517-ATS-002, WI-20260517-ATS-003, WI-20260517-ATS-004

[WI SUMMARY]
Why: Enable Toss one-time payment confirmation behind the existing payment provider contract.
Scope (in/out): Implement backend config, provider selection, checkout metadata, and Toss confirm API call. Exclude billing-key recurring payments, webhooks, refunds, and live-key provisioning.
DoD: Provider can be selected with configuration; MOCK remains default; TOSS prepare/confirm compiles and is test-covered without real network calls.
Constraints/Forbidden: Do not hardcode secret keys. Do not expose Toss secret key to frontend. Do not mutate subscriptions before provider confirmation succeeds.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `app.payment.provider` controls prepare provider with `MOCK` default.
- [ ] Toss prepare response returns client-safe checkout metadata.
- [ ] Toss confirm sends `paymentKey`, `orderId`, and server-side amount to Toss confirm API.
- [ ] Toss failure marks payment order failed and does not apply subscription.
Performance:
- [ ] Toss HTTP client timeout is explicit.
Quality:
- [ ] Backend tests pass.
- [ ] No committed secret values.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-001.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/
- src/main/java/com/atstudio/atstudio/dto/payment/
- src/main/resources/application.yml
- src/test/java/com/atstudio/atstudio/

External References:
- https://docs.tosspayments.com/guides/v2/get-started/llms-quick-reference
- https://docs.tosspayments.com/reference

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-001-summary.md :
- Summary, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-001-evidence-pack.md :
- Evidence pointers, patch notes, repro & tests, follow-up WI
Handoff Packet -> deliverables/agent/WI-20260517-ATS-001-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include command and result
Rollback: Document files to revert
