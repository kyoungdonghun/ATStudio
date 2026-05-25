# Evidence Pack: WI-20260525-ATS-005

## Summary
- Implemented admin refund ledger, Toss cancel provider integration, idempotency, and audit logging.

## Scope / DoD Check
- [x] `payment_refunds` entity/repository/schema exists.
- [x] Admin refund preview/request/approve/execute/list/detail APIs exist.
- [x] Toss cancel uses Basic auth and `Idempotency-Key`.
- [x] Refund requests are persisted before provider execution.
- [x] Refund workflow writes payment operation audit rows.
- [x] Provider cancel payload is sanitized before persistence.
- [x] Entitlement is not automatically mutated by refund execution.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java` — refund ledger entity.
- `src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java` — refund queries and pessimistic lock.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java` — admin refund workflow.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java` — refund admin endpoints.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/refund/` — refund provider contract.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java` — Toss cancel implementation.
- `src/main/resources/schema.sql` — `payment_refunds` table and audit enum changes.

## Verification
- `.\gradlew.bat test` passed.

## Rollback
- Remove refund entity/repository/service/controller DTO/provider additions, schema table/enum changes, and docs/deliverables for REQ-20260525-ATS-004.
