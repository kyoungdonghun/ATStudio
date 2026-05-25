# Evidence Pack: WI-20260525-ATS-010

## Summary
- Implemented the entitlement correction ledger, admin service, controller endpoints, DTOs, schema, and audit log integration.

## Scope / DoD Check
- [x] Added `payment_entitlement_corrections`.
- [x] Added preview/list/detail/request/approve/execute admin APIs.
- [x] Added explicit target-state mutation to `UserSubscription`.
- [x] Added local-only billing agreement cancellation option.
- [x] Added audit target/action coverage.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java`
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java`
- `src/main/java/com/atstudio/atstudio/entity/PaymentEntitlementCorrection.java`
- `src/main/resources/schema.sql`
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java`

## Verification
- `gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentEntitlementCorrectionServiceTest"` passed.
- `gradlew.bat test` passed.

## Rollback
- Revert the new service/controller/DTO/entity/repository/schema/audit changes for entitlement correction.
