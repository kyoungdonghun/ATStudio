# Evidence Pack: WI-20260521-ATS-003

## Summary
- Blocked one-time subscription `SUBSCRIBE` and `UPGRADE` prepare/confirm paths in the backend.

## Scope / DoD Check
- [x] `POST /api/payments/subscriptions/prepare` does not create subscription one-time orders.
- [x] `POST /api/payments/confirm` rejects legacy one-time `SUBSCRIBE`/`UPGRADE` orders before provider confirmation.
- [x] Already-`DONE` legacy orders remain idempotent.

## Evidence Pointers
- `src/main/java/com/atstudio/atstudio/service/PaymentApplicationService.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentApplicationServiceTest.java`
- `docs/design/api-spec.md`

## Validation
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentApplicationServiceTest"`: passed as part of the focused backend test run.

## Risks / Rollback
- Stale clients using one-time subscription checkout now receive an explicit business error.
- Rollback by reverting `PaymentApplicationService` and the corresponding tests/docs.
