---
wi_id: WI-20260517-ATS-008
req_id: REQ-20260517-ATS-002
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-008 Handoff: Billing Agreement Entity and Storage

[WI HEADER]
WI ID: WI-20260517-ATS-008
REQ: REQ-20260517-ATS-002
Agent: se
Depends On: WI-20260517-ATS-005, WI-20260517-ATS-006
Blocks: WI-20260517-ATS-009, WI-20260517-ATS-010, WI-20260517-ATS-011

[WI SUMMARY]
Why: Phase C needs persistent billing agreements before Toss billing-key issue, charge, cancellation, and renewal scheduling can be implemented.
Scope (in/out): Add billing agreement domain model, enum, repository, schema entries, encrypted billing key utility, and config placeholders. Exclude Toss HTTP integration and public APIs.
DoD: Billing agreements can be created in `READY`, activated with encrypted billing key metadata, suspended/cancelled/expired, and queried by user/provider/status without storing billing keys in plain text.
Constraints/Forbidden: Do not expose billing keys to DTOs. Do not store raw billing keys, Toss secret keys, or encryption secrets in repo files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Add `BillingAgreement` entity mapped to `billing_agreements`.
- [ ] Add `BillingAgreementStatus` enum.
- [ ] Add `BillingAgreementRepository` with lookup methods needed by agreement confirm and renewal job.
- [ ] Add encrypted billing key value handling using `PAYMENT_BILLING_KEY_ENCRYPTION_SECRET`.
- [ ] Add random provider customer key generation support; do not use predictable `user.id` or email.
- [ ] Add optional `billingAgreement` link to `SubscriptionPayment` and/or `PaymentOrder` if needed for renewal traceability.
Quality:
- [ ] Unit tests cover encryption/decryption, fingerprint determinism, and no raw key persistence in entity fields.
- [ ] Existing payment tests still pass.

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
- docs/design/payment-integration-design.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/entity/
- src/main/java/com/atstudio/atstudio/entity/enums/
- src/main/java/com/atstudio/atstudio/repository/
- src/main/java/com/atstudio/atstudio/config/PaymentProperties.java
- src/main/resources/schema.sql
- application-local.example.yml
- src/test/java/com/atstudio/atstudio/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-008-summary.md :
- Summary, changed files, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-008-evidence-pack.md :
- Evidence pointers, tests, rollback, downstream notes
Handoff Packet -> deliverables/agent/WI-20260517-ATS-008-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused unit tests and any Gradle test result
Rollback: Document entity/schema/config files to revert
