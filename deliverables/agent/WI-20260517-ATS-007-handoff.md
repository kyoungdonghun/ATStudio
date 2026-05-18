---
wi_id: WI-20260517-ATS-007
req_id: REQ-20260517-ATS-002
agent: tr
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-007 Handoff: Toss Billing API Research

[WI HEADER]
WI ID: WI-20260517-ATS-007
REQ: REQ-20260517-ATS-002
Agent: tr
Depends On: -
Blocks: WI-20260517-ATS-009, WI-20260517-ATS-010, WI-20260517-ATS-011

[WI SUMMARY]
Why: Phase C implementation must use the current Toss billing-key API correctly, especially around billing auth, billing key issuance, automatic payment approval, customerKey, test/live behavior, and provider error handling.
Scope (in/out): Review official Toss Payments documentation for billing-key registration and automatic payment approval. Produce implementation notes and risks for backend/provider WIs. Exclude KakaoPay/Naver Pay research and production merchant contract setup.
DoD: Downstream implementation WIs have source-backed endpoint names, request/response fields, auth requirements, error handling notes, and test-key safety constraints.
Constraints/Forbidden: Use official Toss documentation as primary source. Do not rely on blog posts or stale snippets for API contract decisions.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Identify the official billing-key issuance flow and required request/response fields.
- [ ] Identify the official automatic payment approval flow and required request/response fields.
- [ ] Confirm how `customerKey`, `authKey`, `billingKey`, `orderId`, `amount`, and `orderName` are used.
- [ ] Confirm test key behavior and local-test safety expectations.
- [ ] Summarize provider error codes/status handling needed by ATStudio.
- [ ] Note any contract or console setup prerequisites for production enablement.
Quality:
- [ ] All external claims include official Toss source links.
- [ ] Ambiguous or provider-contract-dependent items are marked as risks, not asserted as implemented facts.
- [ ] Output separates implementation requirements from Phase D production-hardening recommendations.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java
- frontend/src/utils/tossPayments.ts
- frontend/src/api/payments.ts

External References:
- https://docs.tosspayments.com/guides/v2/billing/integration-api
- https://docs.tosspayments.com/en/api-guide
- https://docs.tosspayments.com/reference

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-007-summary.md :
- Research summary, key API findings, risks, and implementation recommendations
Agent-facing -> deliverables/agent/WI-20260517-ATS-007-evidence-pack.md :
- Source links, endpoint/field matrix, error-handling notes, downstream WI checklist
Handoff Packet -> deliverables/agent/WI-20260517-ATS-007-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required. Include official source URLs and the retrieval date.
Tests: Not applicable unless API examples are locally mocked.
Rollback: Document which implementation assumptions should be revisited if Toss docs change.
