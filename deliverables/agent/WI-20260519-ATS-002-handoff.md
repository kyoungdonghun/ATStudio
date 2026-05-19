---
wi_id: WI-20260519-ATS-002
req_id: REQ-20260519-ATS-001
agent: se
status: ready
created_at: 2026-05-19
---

# WI-20260519-ATS-002 Handoff: Backend Recurring Upgrade Charge

[WI HEADER]
WI ID: WI-20260519-ATS-002
REQ: REQ-20260519-ATS-001
Agent: se
Depends On: WI-20260519-ATS-001
Blocks: WI-20260519-ATS-005

[WI SUMMARY]
Why: Upgrades must no longer route through one-time payment and must charge the existing billing key.
Scope (in/out): Update subscription change service, prorated calculation, payment order/payment ledger creation, and tests. Exclude live-key testing and refund automation.
DoD: Upgrade succeeds only after `RecurringPaymentProvider.charge()` succeeds; failure leaves subscription unchanged.
Constraints/Forbidden: Do not expose raw billing keys. Do not mutate subscription before provider charge success.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Active billing agreement is required for upgrade.
- [ ] Upgrade amount is calculated from remaining period difference.
- [ ] Upgrade payment order uses `PaymentPurpose.UPGRADE` and `PaymentProviderType.TOSS_BILLING`.
- [ ] Upgrade preserves current `expiresAt` and uses the requested billing cycle for future renewal.
- [ ] Downgrade behavior remains pending-only.
Quality:
- [ ] Focused backend tests pass.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

REQ/Context Docs:
- deliverables/user/REQ-20260519-ATS-001.md
- docs/design/payment-integration-design.md
- docs/design/api-spec.md

Files:
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/UtilService.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/test/java/com/atstudio/atstudio/service/UserSubscriptionServiceTest.java
- src/test/java/com/atstudio/atstudio/service/UtilServiceTest.java

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260519-ATS-002-summary.md
Agent-facing -> deliverables/agent/WI-20260519-ATS-002-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260519-ATS-002-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused backend test results
Rollback: Revert changed backend files and tests
