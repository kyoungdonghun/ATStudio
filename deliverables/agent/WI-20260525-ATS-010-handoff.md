[WI HEADER]
WI ID: WI-20260525-ATS-010
REQ: REQ-20260525-ATS-005
Agent: se
Depends On: WI-20260525-ATS-009
Blocks: WI-20260525-ATS-012, WI-20260525-ATS-013

[WI SUMMARY]
Why: Operators need a safe backend path to correct local subscription entitlement after a support-approved refund.
Scope (in/out): Implement entitlement correction ledger, admin APIs, local subscription/billing agreement mutation, and audit logging. Exclude admin UI and provider billing-key deletion.
DoD: Admin can preview, request, approve, execute, list, and detail entitlement correction records; execution applies explicit target state only.
Constraints/Forbidden: Do not infer previous plan. Do not call provider cancel/delete. Do not auto-execute from refund success.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `payment_entitlement_corrections` entity/repository/service exists.
- [ ] Admin preview/request/approve/execute/read APIs exist.
- [ ] Execution updates `user_subscriptions` according to explicit target fields.
- [ ] Optional local billing agreement cancel does not call provider APIs.
- [ ] Audit log records request/approval/execution transitions.
Performance:
- [ ] List endpoint is pageable.
Quality:
- [ ] Java tests pass.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Context):
- deliverables/user/REQ-20260525-ATS-005.md
- docs/design/payment-refund-receipt-settlement-policy.md
- docs/design/api-spec.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java
- src/main/java/com/atstudio/atstudio/entity/UserSubscription.java
- src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java
- src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java
- src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java
- src/main/resources/schema.sql

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260525-ATS-010-summary.md
Agent-facing -> deliverables/agent/WI-20260525-ATS-010-evidence-pack.md
Handoff Packet -> deliverables/agent/WI-20260525-ATS-010-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Required
Rollback: Document table/API/entity removal and enum rollback
