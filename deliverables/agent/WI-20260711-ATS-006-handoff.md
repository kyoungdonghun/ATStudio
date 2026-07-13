[WI HEADER]
WI ID: WI-20260711-ATS-006
REQ: REQ-20260711-ATS-001
Agent: qa-integ
Depends On: WI-20260711-ATS-001, WI-20260711-ATS-002, WI-20260711-ATS-003, WI-20260711-ATS-004, WI-20260711-ATS-005
Blocks: WI-20260711-ATS-009, WI-20260711-ATS-016

[WI SUMMARY]
Why: Reconcile payment/subscription design intent, current backend/frontend behavior, schema/migrations, and current documentation into one 3-way verdict.
Scope (in/out): Cover checkout, billing agreements, first charge, upgrade/downgrade/cycle change, cancellation/reactivation, renewal, reconciliation, refund, entitlement correction, settlement, account withdrawal with active billing, admin payment UI, and operations docs. Read-only except WI outputs.
DoD: Produce a contract matrix, de-duplicated confirmed findings, severity decisions, policy ambiguities, and focused verification inputs.
Constraints/Forbidden: No provider calls, payment/refund/admin mutation, SQL, code/doc edits, or secret output. Re-check evidence; do not copy phase-1 conclusions blindly.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Map each payment capability across design, API/code, DB, frontend, and operational/client docs.
- [ ] Reconcile overlapping findings BE/FE/PG/INT and state one final disposition per issue.
- [ ] Verify account withdrawal, provider/local failure boundaries, concurrency/idempotency, and schema enum findings.
- [ ] List exact regression tests and MySQL/provider-safe verification needed.
Performance:
- [ ] Assess renewal/reconciliation batch bounds and high-value index assumptions without speculative claims.
Quality:
- [ ] Every row has exact pointers and one of: aligned, defect, policy ambiguity, deferred by design, external verification required.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2:
- docs/design/payment-integration-design.md
- docs/design/payment-operations-runbook.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-settlement-import-design.md
- docs/design/usecase/user-subscription.md
- docs/design/usecase/user-info.md
- docs/payment/
- docs/SR/SR-93.md
- docs/client/2-full-feature-checklist.md
- docs/client/3-admin-checklist.md

REQ/Context Docs:
- deliverables/user/REQ-20260711-ATS-001.md
- deliverables/agent/WI-20260711-ATS-001-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-002-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-003-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260711-ATS-005-evidence-pack.md

Files:
- src/main/java/com/atstudio/atstudio/controller/PaymentController.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/payment/
- src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java
- src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java
- src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java
- src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx
- frontend/src/pages/subscriber/SubscriptionManagePage.tsx
- frontend/src/pages/admin/PaymentReadOnlyPage.tsx

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260711-ATS-006-summary.md : Korean payment 3-way verdict
Agent-facing -> deliverables/agent/WI-20260711-ATS-006-evidence-pack.md : matrix, de-duplicated findings, evidence, tests, follow-ups
Handoff Packet -> deliverables/agent/WI-20260711-ATS-006-handoff.md : this packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required
Tests: Specify non-destructive focused tests; do not invoke live provider/DB mutations
Rollback: Remove only this WI's two owned outputs if explicitly requested
