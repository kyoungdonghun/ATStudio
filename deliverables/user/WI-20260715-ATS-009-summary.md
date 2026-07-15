# WI-20260715-ATS-009 Independent Review Summary

## Findings

### P1-01 - Refund provider execution still suspends an outer transaction

`AdminPaymentRefundService.executeRefund()` and `executeRefundAt()` use
`Propagation.NOT_SUPPORTED` while `executeRefundAt()` invokes
`PaymentRefundProvider.cancelPayment()`
(`src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:149-186`).
This permits an accidental caller transaction to be suspended instead of failing
closed. The design requires `NEVER` or an equivalent no-transaction assertion for
every refund-provider invocation
(`docs/design/p1-payment-integrity-remediation-design.md:79-87`).

The refund integration test is itself `NOT_SUPPORTED` and its fake checks only
`TransactionSynchronizationManager.isActualTransactionActive() == false`
(`src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:82`,
`:734`). It therefore cannot distinguish a clean boundary from a suspended outer
transaction. F-02/transaction-boundary closure is incomplete.

### P1-02 - Cancelled initial-subscription state remains mutation-capable

The reconciliation eligibility check for `SUBSCRIBE` requires only a null local
subscription and retained billing-key ciphertext/fingerprint
(`src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:229-238`).
The locked revalidation repeats those conditions without requiring the agreement
to remain in the expected pre-charge state or cleanup to remain unclaimed
(`src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1246-1273`).

Cancellation can commit `CANCELLED` while retaining key material when provider
cleanup fails or is ambiguous
(`src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:68-99`,
`:182-205`). An exact provider `DONE` lookup can then move the order to
`PROVIDER_SUCCEEDED` and dispatch initial finalization despite the intervening
cancellation. This violates the required expected-initial-state gate and the
detect-only rule for contradictory local evidence
(`docs/design/p1-payment-integrity-remediation-design.md:361-386`). F-04 is not
closed.

### P2-01 - Reconciliation audit notes retain the full provider transaction ID

`recoveryEvidenceNote()` writes `issue.providerTransactionId()` directly into the
free-text audit note
(`src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:306-323`).
For Toss lookup this value is the provider `paymentKey`
(`src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:428-439`).
The design requires a sanitized transaction ID in reconciliation audit evidence
(`docs/design/p1-payment-integrity-remediation-design.md:406-417`).

## Verdict

**FAIL.** PASS is not issued because two P1 defects remain.

| Finding | Review result |
|---|---|
| F-01 renewal identity/retry | No new P0/P1 defect found in the reviewed range; same-order retry and immutable-period evidence align. |
| F-02 transaction boundaries | **Open:** refund provider execution uses `NOT_SUPPORTED`, not fail-closed `NEVER`. |
| F-03 refund fencing/idempotency | Lease timestamp fencing, same-row/same-key replay, and lookup-only fallback align; the provider boundary defect above still blocks closure. |
| F-04 provider-DONE recovery | **Open:** cancelled initial-subscription state is not rejected as contradictory evidence. |
| F-05 canonical locks/MySQL proof | No contradiction found in the reviewed lock paths or WI-007 evidence. WI-007 records MySQL 8/InnoDB schema validation and 7/7 race PASS after WI-008. |

## Residual Risks

- The 24-hour same-key Toss refund replay still depends on a verified provider
  idempotency-retention contract.
- The MySQL proof covers the seven designed races, not the two missing scenarios
  above: refund invocation under an outer transaction and initial-subscription
  reconciliation after cancellation/cleanup failure.
- The review did not execute tests, a database, or a provider. It used the
  committed range and the supplied WI-001 through WI-008 evidence.

## Recommendation

Create a corrective WI before the final payment quality gate. Require strict
`NEVER` refund execution with an active-transaction rejection test, require locked
initial-subscription state/cleanup validation before reconciliation mutation, add
the cancellation race regression, and sanitize provider transaction IDs in audit
notes. Re-run the focused payment suite and only the MySQL races affected by the
correction.
