[WI HEADER]
WI ID: WI-20260525-ATS-005
REQ: REQ-20260525-ATS-004
Agent: se
Depends On: WI-20260525-ATS-004
Blocks: WI-20260525-ATS-007, WI-20260525-ATS-008

[WI SUMMARY]
Why: Operators need a local audited refund workflow before calling Toss cancel.
Scope (in/out): Implement `payment_refunds`, admin refund APIs, Toss cancel provider call, idempotency, audit logs, and backend tests. Exclude admin UI and entitlement correction.
DoD: Admin can preview, request, approve, execute, list, and detail refund records; provider cancel is invoked only for approved requests.
Constraints/Forbidden: No raw secrets or raw provider payloads; no automatic subscription entitlement mutation.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `payment_refunds` entity/repository/service exists.
- [ ] Admin refund preview/request/approve/execute/read APIs exist.
- [ ] Toss cancel uses Basic auth and `Idempotency-Key`.
- [ ] Partial refund cumulative amount cannot exceed original payment amount.
Quality:
- [ ] Provider and service tests cover success, failure, pending-confirmation, and over-refund guard.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/policies/security-policy.md

Tier 2:
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/payment-operations-runbook.md
- docs/design/api-spec.md
- docs/design/db-schema.md
- src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java
- deliverables/user/REQ-20260525-ATS-004.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-005-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-005-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-005-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, changed files, commands, tests, and rollback notes are required.
