---
wi_id: WI-20260517-ATS-011
req_id: REQ-20260517-ATS-002
agent: se
status: ready
created_at: 2026-05-17
---

# WI-20260517-ATS-011 Handoff: Recurring Renewal Scheduler and Failure Policy

[WI HEADER]
WI ID: WI-20260517-ATS-011
REQ: REQ-20260517-ATS-002
Agent: se
Depends On: WI-20260517-ATS-005, WI-20260517-ATS-006, WI-20260517-ATS-007, WI-20260517-ATS-008, WI-20260517-ATS-009, WI-20260517-ATS-010
Blocks: WI-20260517-ATS-012

[WI SUMMARY]
Why: Recurring subscriptions need an automated renewal path that charges active billing agreements, extends subscriptions, and handles failures consistently.
Scope (in/out): Add recurring renewal job/service, renewal target queries, idempotency guard, success extension, 3-day grace and 3-retry failure policy. Exclude webhook/refund automation and frontend UI.
DoD: Due recurring agreements create exactly one renewal order per period, successful charges extend access, failures retry within policy, and final failure disables automatic renewal without duplicate charges.
Constraints/Forbidden: Do not charge cancelled agreements. Do not double-charge the same agreement/period. Do not expire a user before applying the approved 3-day grace policy.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Query due `ACTIVE` billing agreements by `nextBillingAt <= today`.
- [ ] Skip cancelled, suspended beyond policy, expired, or missing active subscription cases.
- [ ] Create `RENEWAL` payment orders with idempotency per billing agreement and period.
- [ ] Call `RecurringPaymentProvider.charge`.
- [ ] On success, save `SubscriptionPayment`, extend `UserSubscription.expiresAt`, update `nextBillingAt`, reset failure count.
- [ ] On failure, mark order failed, increment failure count, schedule retry inside 3-day grace.
- [ ] After 3 failures or grace expiry, mark agreement `SUSPENDED` and set subscription to `EXPIRED` only after paid/grace access ends.
- [ ] Existing downgrade/expiry scheduler does not race against recurring renewal.
Quality:
- [ ] Scheduler/service unit tests cover success, duplicate run, transient failure, final failure, cancelled agreement, and due-date boundary.
- [ ] Existing subscription scheduler tests still pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260517-ATS-002.md
- deliverables/agent/WI-20260517-ATS-005-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-006-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-009-evidence-pack.md
- deliverables/agent/WI-20260517-ATS-010-evidence-pack.md
- docs/design/payment-integration-design.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java
- src/main/java/com/atstudio/atstudio/service/
- src/main/java/com/atstudio/atstudio/repository/
- src/main/java/com/atstudio/atstudio/entity/
- src/test/java/com/atstudio/atstudio/service/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260517-ATS-011-summary.md :
- Summary, renewal policy behavior, risks, approval points
Agent-facing -> deliverables/agent/WI-20260517-ATS-011-evidence-pack.md :
- Evidence pointers, tests, rollback, downstream notes
Handoff Packet -> deliverables/agent/WI-20260517-ATS-011-handoff.md :
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Include focused scheduler/service tests and command results
Rollback: Document scheduler/service/entity changes to revert
