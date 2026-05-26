# Evidence Pack: WI-20260526-ATS-002

## Summary

- Implemented backend settlement ledger, CSV import, reconciliation, ignore workflow, admin APIs, and unit tests.

## Scope / DoD Check

- [x] Settlement ledger persists imported rows.
- [x] Duplicate rows are skipped by deterministic deduplication key.
- [x] Reconciliation links imported rows to local order/payment/refund records where possible.
- [x] Mismatch statuses are queryable by admin.
- [x] Provider APIs are not called.
- [x] Subscription/payment/refund state is not automatically mutated.
- [x] Missing-provider generation skips orders that already have imported provider settlement evidence.

## Reference Documents

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Constitution |
| 0 | docs/standards/development-standards.md | Java/Spring implementation standards |
| 1 | docs/policies/security-policy.md | Sensitive data boundary |
| 2 | docs/design/payment-settlement-import-design.md | Settlement implementation design |
| 2 | docs/design/payment-refund-receipt-settlement-policy.md | Payment operations policy |
| 2 | deliverables/user/REQ-20260526-ATS-001.md | Approved scope |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java` - settlement ledger entity.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java` - CSV import, matching, missing-provider issue generation, ignore workflow.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java` - admin settlement endpoints.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementResponse.java` - support-safe admin response.
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java` - settlement audit event recording.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java` - matched, mismatched, duplicate import, and existing-provider-evidence skip tests.

## Commands & Outputs

- `gradlew.bat compileJava` -> passed.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest"` -> passed.
- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest"` -> passed.

## Risks / Rollback

- Risk: first CSV parser expects stable header names; actual Toss exports may need an adapter mapping patch.
- Risk: fee/VAT/net mismatch formula is strict and may need contract-specific tuning after live settlement samples.
- Rollback: revert settlement entity/enums/repository/service/controller/DTO/test additions and audit enum additions.

## Follow-ups

- WI-20260526-ATS-004: Admin settlement UI.
- WI-20260526-ATS-005: docs/API/DB/UI sync and full validation.
