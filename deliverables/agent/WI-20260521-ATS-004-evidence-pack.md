# Evidence Pack: WI-20260521-ATS-004

## Summary
- Added stale payment-order expiration and local payment-ledger reconciliation.

## Scope / DoD Check
- [x] `READY` and `IN_PROGRESS` payment orders past `expiresAt` are marked `EXPIRED` by scheduler.
- [x] Local reconciliation reports `DONE` payment orders without finalized subscription payment rows.
- [x] Local reconciliation reports active billing agreements without active subscriptions.
- [x] Provider webhook/API-backed reconciliation is explicitly left as remaining production hardening.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/service/SubscriptionScheduler.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java`
- `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/BillingAgreementRepository.java`
- `src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java`
- `src/test/java/com/atstudio/atstudio/service/SubscriptionSchedulerTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java`

## Validation
- Focused backend payment/scheduler/reconciliation test run: passed.

## Risks / Rollback
- Reconciliation currently logs mismatches only; it does not mutate or auto-refund.
- Rollback by reverting scheduler/reconciliation service and repository query additions.
