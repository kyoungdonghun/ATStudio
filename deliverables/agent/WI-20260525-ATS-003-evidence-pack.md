# Evidence Pack: WI-20260525-ATS-003

## Summary (one-liner)
- Implemented payment receipt evidence persistence, payment operation audit logs, admin read-only APIs, and documentation synchronization for P2-A.

## Scope / DoD Check
- [x] `payment_receipts` entity/repository/service exists and records safe receipt evidence.
- [x] `payment_operation_audit_logs` entity/repository/service exists and records admin/system payment operation changes.
- [x] Initial subscription, upgrade charge, and recurring renewal successful charge paths publish receipt evidence events.
- [x] Toss sanitized charge payload includes safe receipt/cash receipt fields when present.
- [x] Reconciliation incident status update records actor, before/after status, note, and target linkage.
- [x] Admin read-only APIs list payment receipts and operation audit logs with pagination.
- [x] API spec, DB schema, runbook, SR-93, payment policy, UI/API inventory, and acceptance checklist are synchronized.
- [x] Backend tests, docs validation, and diff check pass.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution and execution gate |
| 0 | docs/standards/development-standards.md | Java/Spring implementation standards |
| 0 | docs/standards/documentation-standards.md | Documentation consistency |
| 0 | docs/standards/glossary.md | Domain terminology |
| 1 | docs/policies/security-policy.md | Sensitive payment data boundary |
| 1 | docs/policies/quality-gates.md | Verification gate |
| 2 | docs/design/payment-refund-receipt-settlement-policy.md | P2-A implementation order and policy boundary |
| 2 | docs/design/payment-operations-runbook.md | Operations visibility and incident response |
| 2 | docs/design/payment-integration-design.md | Payment architecture baseline |
| 2 | docs/design/api-spec.md | Admin API contract |
| 2 | docs/design/db-schema.md | DB contract |
| 2 | docs/SR/SR-93.md | Production readiness tracker |
| 2 | docs/index.md | Root documentation index |
| 2 | docs/design/index.md | Design index |
| 2 | deliverables/user/REQ-20260525-ATS-003.md | Approved requirement |
| 2 | deliverables/agent/WI-20260525-ATS-003-handoff.md | WI scope and output contract |

**Injection Rules Applied**:
- Rule source: `.claude/config/context-injection-rules.json`
- Assignee: se/docops
- Task type: payment operations audit foundation, backend implementation, documentation sync

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/entity/PaymentReceipt.java:46` — new receipt evidence entity.
- `src/main/java/com/atstudio/atstudio/entity/PaymentOperationAuditLog.java:38` — new payment operation audit entity.
- `src/main/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceService.java:42` — successful charge evidence event publisher.
- `src/main/java/com/atstudio/atstudio/service/PaymentReceiptEvidenceService.java:57` — after-commit receipt evidence listener.
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java:27` — reconciliation incident status audit log writer.
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java:53` — receipt evidence creation audit log writer.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:233` — initial subscription charge publishes receipt evidence.
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:196` — upgrade charge publishes receipt evidence.
- `src/main/java/com/atstudio/atstudio/service/RecurringRenewalService.java:224` — renewal charge publishes receipt evidence.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:63` — admin receipt list API.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:71` — admin operation audit log list API.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:386` — Toss sanitized charge payload includes safe receipt evidence.
- `src/main/resources/schema.sql` — manual DB schema updated to 33 tables.
- `docs/design/api-spec.md:1369` — new admin receipt/audit APIs listed.
- `docs/design/db-schema.md:445` — `payment_receipts` DB contract.
- `docs/design/db-schema.md:470` — `payment_operation_audit_logs` DB contract.
- `docs/SR/SR-93.md:196` — P2-A completed items.
- `deliverables/user/PAYMENT-FINAL-ACCEPTANCE-CHECKLIST-20260525.md:84` — final acceptance checklist includes receipt/audit endpoints.

## Commands & Outputs

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentReceiptEvidenceServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentIncidentServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`
  - Result: passed.
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest"`
  - Result: passed.
- `.\gradlew.bat test`
  - First run failed because `PaymentReceiptEvidenceService` required an `ObjectMapper` bean not guaranteed by the current app context.
  - Fix: switched to a service-local `ObjectMapper`, matching the existing Toss provider pattern.
  - Second run result: passed.
- `python .agents\skills\validate-docs\scripts\validate_docs.py`
  - Result: all validations passed.
- `git diff --check`
  - Result: no whitespace errors; only Windows LF-to-CRLF warnings.

## Tests

- `PaymentReceiptEvidenceServiceTest`
  - Stores payment receipt and cash receipt evidence without raw card details.
  - Publishes after-commit evidence events.
  - Skips duplicate receipt types.
- `AdminPaymentIncidentServiceTest`
  - Verifies incident status changes record audit log calls with before/after status.
- `PaymentOperationAuditLogServiceTest`
  - Verifies actor, target, reason, and status transition fields.
- Existing billing/upgrade/renewal tests
  - Verify receipt evidence publishing on successful charge paths.
- `TossBillingProviderTest`
  - Verifies safe receipt/cash receipt fields are preserved in sanitized charge payload.

## Risks / Rollback

- Risks:
  - Receipt evidence storage is best-effort after commit; if the after-commit listener fails, the payment remains successful and operators must rely on reconciliation/provider dashboard until evidence is recovered.
  - Cash receipt issue/cancel is not implemented; current support is evidence capture only when provider returns fields.
  - Admin UI tabs for receipt/audit logs may need a follow-up UI polish if operators require first-class tab views beyond API access.
- Rollback:
  - Revert this WI commit.
  - Drop `payment_receipts` and `payment_operation_audit_logs` if already applied to a local/stage DB.
  - Restore API spec, DB schema, SR-93, runbook, payment policy, UI inventory, checklist, and deliverables to the previous state.

## Follow-ups

- P2-B: refund request/approval/execution workflow with Toss cancel API and idempotency.
- P2-C: entitlement correction workflow linked to refund decisions.
- P2-D: settlement import/reconciliation.
- P2-E: tax invoice request/admin tracking.
- P2-F: admin UI tabs for receipts and operation audit logs if needed.
