---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: closure
status: complete
related_wi: WI-20260716-ATS-021
---

# WI-20260716-ATS-021 Summary

## Result

`CLOSED`

## Change

- `AdminPaymentReconciliationResponse.ProviderLedger` now returns the ADMIN-safe nested `ProviderIssue` record instead of the service-internal provider issue record.
- The mapper preserves existing safe issue fields and aggregate/truncation metadata while converting the raw `providerTransactionId` through `ProviderSupportReference.from(...)` into deterministic `providerReference` values.
- `PaymentReconciliationService.ProviderReconciliationIssue`, Incident persistence, provider operations, and the reconciliation algorithm were not changed by WI-021.
- Existing API and operations documents already define `providerReference` as authoritative, so no design-document correction was required.

## Verification

- Focused backend command: `gradlew.bat test --tests "com.atstudio.atstudio.dto.payment.AdminProviderIdentifierContractTest" --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest"`
- Result: `PASS` - 33 tests, 0 failures, 0 errors, 0 skipped.
- DTO and controller sentinel tests prove that serialized JSON contains `providerReference` and contains neither the `providerTransactionId` field name nor the sentinel raw value.
- `git diff --check`: exit 0 with no whitespace errors; the cumulative worktree emitted line-ending conversion warnings only.

## Readiness Boundary

F-020-01 is closed at the repository response-contract boundary. Final development-branch release-readiness re-verification remains the next blocked activity.
