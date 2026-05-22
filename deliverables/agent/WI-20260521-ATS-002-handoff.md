[WI HEADER]
WI ID: WI-20260521-ATS-002
REQ: REQ-20260521-ATS-001
Agent: pg
Depends On: -
Blocks: WI-20260521-ATS-003, WI-20260521-ATS-004, WI-20260521-ATS-006, WI-20260521-ATS-007

[WI SUMMARY]
Why: Review payment hardening work for secret, billing-key, authKey, customerKey, email, and read-only admin boundaries.
Scope (in/out): In scope: sensitive data exposure, log/doc/test-fixture boundaries, email content, admin read-only safety. Out of scope: implementation.
DoD: Security constraints are actionable for implementation WIs.
Constraints/Forbidden: No raw provider payloads, billing keys, authKey, customerKey, or secrets in user screens, logs, docs, or fixtures.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Sensitive values have explicit masking or exclusion rules.
- [ ] Email notifications do not include secrets or raw provider payloads.
- [ ] Admin payment view is read-only and sanitized.
Quality:
- [ ] Security notes map to REQ constraints.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
Tier 1 (Policies - Inferred):
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260521-ATS-001.md
- docs/SR/SR-93.md
- docs/design/payment-integration-design.md

Files:
- src/main/java/com/atstudio/atstudio/service/payment/billing/BillingKeyCrypto.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/entity/PaymentOrder.java
- src/main/resources/application.yml

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260521-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260521-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260521-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Not required for review-only WI
Rollback: Document policy changes if any
