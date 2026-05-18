---
wi_id: WI-20260517-ATS-009
req_id: REQ-20260517-ATS-002
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-009 Handoff: Recurring Payment Provider and Toss Billing Adapter

[WI HEADER]
WI ID: WI-20260517-ATS-009
REQ: REQ-20260517-ATS-002
Agent: se
Depends On: WI-20260517-ATS-005, WI-20260517-ATS-006, WI-20260517-ATS-007, WI-20260517-ATS-008
Blocks: WI-20260517-ATS-010, WI-20260517-ATS-011

[WI SUMMARY]
Why: ATStudio needs a provider-neutral recurring billing adapter so Toss billing can be implemented without coupling provider HTTP calls to subscription mutation.
Scope (in/out): Add `RecurringPaymentProvider`, command/result DTOs, `TossBillingProvider`, config URLs/timeouts, sanitized response mapping, and focused provider tests. Exclude controller endpoints and scheduler.
DoD: Toss billing-key issue, recurring charge, and billing-key delete requests are represented by a tested provider that returns result objects and never mutates `UserSubscription`.
Constraints/Forbidden: Do not call `/v1/payments/confirm` for recurring charge. Do not log or persist raw Toss billing key outside encrypted storage handled by WI-008.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Add provider interface for prepare/confirm agreement, charge, and cancel/delete.
- [ ] Implement Toss billing-key issue request: `POST /v1/billing/authorizations/issue`.
- [ ] Implement Toss recurring charge request: `POST /v1/billing/{billingKey}`.
- [ ] Implement optional billing-key delete request: `DELETE /v1/billing/{billingKey}`.
- [ ] Use Basic auth server-side only.
- [ ] Use provider read timeout of at least 60000 ms for recurring charge.
- [ ] Use idempotency key header for charge requests when available.
- [ ] Sanitize provider responses before returning/persisting payload metadata.
Quality:
- [ ] Local HTTP-server tests cover request body, headers, timeout config, success, and error mapping.
- [ ] No frontend code receives secret key or billing key.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- deliverables/agent/WI-20260517-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-007-evidence-pack.md
- docs/design/payment-integration-design.md

Files:
- src/main/java/com/atstudio/atstudio/service/payment/provider/
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/test/java/com/atstudio/atstudio/service/payment/provider/

External References:
- https://docs.tosspayments.com/guides/v2/billing/integration
- https://docs.tosspayments.com/reference
- https://docs.tosspayments.com/en/api-guide

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-009-summary.md :
- Summary, changed files, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-009-evidence-pack.md :
- Evidence pointers, API mapping, tests, rollback, downstream notes
Handoff Packet -> deliverables/agent/WI-20260517-ATS-009-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required, including official Toss source URLs
Tests: Include focused provider tests
Rollback: Document provider/config files to revert
