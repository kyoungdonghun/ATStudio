# Evidence Pack: WI-20260525-ATS-006

## Summary
- Added focused backend test coverage for refund service and Toss cancel provider behavior.

## Scope / DoD Check
- [x] Local ledger creation before provider call covered.
- [x] Approval gate before provider execution covered.
- [x] Cumulative over-refund guard covered.
- [x] Provider idempotency key reuse covered.
- [x] Toss cancel request headers/body covered.
- [x] Sanitized provider payload excludes raw card number.

## Evidence Pointers
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java` — refund workflow tests.
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java` — Toss cancel provider test.

## Commands
- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest"` — passed during focused verification.
- `.\gradlew.bat test` — passed during final verification.

## Rollback
- Remove the refund-focused test classes/changes if the refund implementation is rolled back.
