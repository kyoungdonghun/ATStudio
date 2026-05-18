---
wi_id: WI-20260517-ATS-006
req_id: REQ-20260517-ATS-002
agent: pg
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-006 Handoff: Billing Key Security Design

[WI HEADER]
WI ID: WI-20260517-ATS-006
REQ: REQ-20260517-ATS-002
Agent: pg
Depends On: -
Blocks: WI-20260517-ATS-008, WI-20260517-ATS-009, WI-20260517-ATS-010, WI-20260517-ATS-011, WI-20260517-ATS-013

[WI SUMMARY]
Why: Toss billing keys are sensitive payment credentials. ATStudio must define encryption, masking, config, logging, and exposure rules before implementing billing agreement storage or recurring charges.
Scope (in/out): Define security controls for billing-key storage, encryption key injection, fingerprinting, provider payload sanitization, logs, API responses, and tests. Exclude production secret provisioning outside the repository.
DoD: Implementation WIs can add billing-key persistence and provider calls without exposing billing keys or secrets in code, DB logs, frontend responses, or committed files.
Constraints/Forbidden: Do not store billing keys in plain text. Do not expose billing keys, Toss secret keys, or encryption secrets to frontend code, logs, docs examples, or test fixtures.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Define encryption-at-rest requirements for `billing_key_ciphertext`.
- [ ] Define non-reversible fingerprint rules for lookup/debugging.
- [ ] Define environment variable policy for `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.
- [ ] Define masking rules for billing key, customer key, card/method metadata, and provider payload.
- [ ] Define allowed frontend response fields for billing agreement status and masked payment method.
- [ ] Define local/test safety rules so live billing cannot be triggered accidentally.
Quality:
- [ ] Security policy is consistent with docs/policies/security-policy.md.
- [ ] Downstream implementation has explicit checks for no secret leakage in diff/logs.
- [ ] Remaining production-hardening risks are clearly separated from Phase C implementation.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 1 (Policies - Based on Assignee):
- docs/policies/security-policy.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- docs/design/payment-integration-design.md
- docs/design/db-schema.md
- docs/design/api-spec.md

Files:
- application-local.example.yml
- src/main/resources/application.yml
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/java/com/atstudio/atstudio/service/payment/provider/TossPaymentProvider.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/java/com/atstudio/atstudio/entity/SubscriptionPayment.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-006-summary.md :
- Security decision summary, required env vars, risks, and approval points if any
Agent-facing -> deliverables/agent/WI-20260517-ATS-006-evidence-pack.md :
- Evidence pointers, security controls, implementation checklist, test expectations
Handoff Packet -> deliverables/agent/WI-20260517-ATS-006-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required. Cite policy sections and target implementation files.
Tests: Not applicable unless a secret scan or validation command is run.
Rollback: Document how to disable recurring billing safely if billing-key storage is suspected to be compromised.
