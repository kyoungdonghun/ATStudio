---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: qa-integ
category: closure
status: complete
related_wi: WI-20260716-ATS-020
---

# WI-20260716-ATS-020 Summary

## Judgment

`NEEDS_FOLLOW_UP_WI`

## Repository-Level Blocker

**F-020-01 - P2 Medium - Raw provider transaction identifier is exposed by the ADMIN reconciliation response.**

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:253-256` exposes `GET /api/admin/payments/reconciliation` to ADMIN users.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentReadService.java:73-80` passes the provider reconciliation result to `AdminPaymentReconciliationResponse.from(...)`.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:524-546` copies `providerResult.transactionId()` into each provider issue; the returned record names that field `providerTransactionId` at `PaymentReconciliationService.java:597-613`.
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentReconciliationResponse.java:25-36,50-61` returns the service issue list directly without mapping the raw identifier to a masked support reference.
- This contradicts `docs/policies/security-policy.md:233-237`, which requires ADMIN API responses to expose deterministic masked `REF-*` support references instead of raw provider identifiers.
- It also contradicts `docs/design/api-spec.md:1619,2202-2204,2232-2247`, which defines support-safe issue records and documents `providerReference`, not `providerTransactionId`.
- `src/test/java/com/atstudio/atstudio/dto/payment/AdminProviderIdentifierContractTest.java:19-43` forbids `providerTransactionId` but omits `AdminPaymentReconciliationResponse` from the response types under test.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:224-269` verifies only that raw issue evidence is absent from logs; it does not verify that the ADMIN response suppresses the same raw identifier.

The supplied deterministic gates passed, but they do not cover this response contract. No remediation or client propagation was performed by WI-020.
