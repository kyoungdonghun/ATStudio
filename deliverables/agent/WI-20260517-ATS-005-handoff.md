---
wi_id: WI-20260517-ATS-005
req_id: REQ-20260517-ATS-002
agent: sa
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-005 Handoff: Recurring Billing Architecture Design

[WI HEADER]
WI ID: WI-20260517-ATS-005
REQ: REQ-20260517-ATS-002
Agent: sa
Depends On: -
Blocks: WI-20260517-ATS-008, WI-20260517-ATS-009, WI-20260517-ATS-010, WI-20260517-ATS-011

[WI SUMMARY]
Why: REQ-20260517-ATS-002 needs a precise architecture boundary before Phase C implementation starts, because recurring billing touches subscription state, payment orders, billing agreements, renewal scheduling, and failure policy.
Scope (in/out): Define the backend architecture, API boundary, DB state model, state transitions, idempotency rules, and policy decisions for Toss billing-key based recurring billing. Exclude implementation code changes and UI styling decisions.
DoD: Architecture decisions are clear enough for backend, security, frontend, and QA WIs to implement without re-deciding core behavior.
Constraints/Forbidden: Do not introduce a provider-specific subscription mutation path. Provider adapters must return payment/billing results only; application services own subscription mutation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Define how `BillingAgreement`, `PaymentOrder`, `SubscriptionPayment`, and `UserSubscription` relate.
- [ ] Define the initial recurring subscription flow: billing-key registration followed by immediate first charge before activation.
- [ ] Define renewal flow, including `RENEWAL` payment order creation and subscription period extension.
- [ ] Define automatic renewal cancellation behavior: stop future charges while preserving paid access until `expiresAt`.
- [ ] Define failure behavior: 3-day grace period, up to 3 retries, and final state transitions.
- [ ] Define idempotency boundaries for billing agreement confirmation and renewal charge per subscription period.
Quality:
- [ ] Existing `MOCK` and `TOSS` one-time payment flows remain compatible.
- [ ] The design keeps future providers open through provider interfaces and enum extension.
- [ ] Output contains concrete API/DB/service pointers for implementation WIs.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- docs/design/usecase/user-subscription.md
- docs/ui/screen-flow.md
- docs/ui/modal-list.md
- docs/SR/SR-92.md

Files:
- src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentPurpose.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentProviderType.java
- src/main/java/com/atstudio/atstudio/entity/enums/PaymentOrderStatus.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/PaymentProvider.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-005-summary.md :
- Architecture summary, key decisions, remaining risks, and approval points if any
Agent-facing -> deliverables/agent/WI-20260517-ATS-005-evidence-pack.md :
- Evidence pointers, decision notes, affected files, downstream WI instructions
Handoff Packet -> deliverables/agent/WI-20260517-ATS-005-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required. Cite document paths and code files reviewed.
Tests: Not applicable unless a design validation command is run.
Rollback: Document which design decisions downstream WIs should revert if this architecture changes.
