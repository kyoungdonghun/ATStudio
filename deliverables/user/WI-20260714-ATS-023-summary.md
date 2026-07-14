# WI-20260714-ATS-023 Summary

## Findings

### HIGH - Renewal retry creates a new command identity

`PaymentCommandTransactionService` derives `billingPeriodStart` from `BillingAgreement.nextBillingAt` (`PaymentCommandTransactionService.java:254-255`), but a failed renewal overwrites that value with tomorrow (`PaymentCommandTransactionService.java:409-414`, `BillingAgreement.java:189-192`). The next scheduler run therefore looks up and creates another renewal order for a different period (`PaymentCommandTransactionService.java:651-695`) instead of incrementing the original order attempt.

Impact: one logical renewal can acquire a new `orderId`, `commandKey`, and provider idempotency key. The unique renewal-period key does not converge these rows because the period itself changed. `RecurringRenewalCommandIntegrationTest.java:125-155` asserts the changed date but never runs the next-day retry.

Status: **Open / blocking**. Deferred because correction requires an explicit retryable-order lookup and scheduling contract, plus a next-day integration test. Recommended owner: `se` with independent `re` verification.

### HIGH - Provider calls remain inside broad transaction lifetimes

- Billing agreement cancellation starts `@Transactional` and calls Toss before local cancellation (`BillingAgreementApplicationService.java:320-347`).
- Withdrawal cleanup starts `REQUIRES_NEW` and calls Toss in that transaction (`WithdrawalBillingCleanupService.java:49-68`).
- Scheduled reconciliation starts `@Transactional`, self-invokes provider reconciliation, and performs provider lookups in the outer transaction (`PaymentReconciliationService.java:48-53`, `PaymentReconciliationService.java:107-132`).
- Charged upgrade starts at `UserSubscriptionService.java:120`, reaches the provider at `UserSubscriptionService.java:337-360`, and only suspends the already-open transaction through `SubscriptionUpgradePaymentExecutor.java:26-28`.

Impact: locks/transactions can span network latency, and provider success followed by local rollback can leave split state.

Status: **Open / blocking**. Deferred because the affected paths need orchestration splits, not an adapter-only patch. Recommended owner: `se`, reviewed by `cr`/`re`.

### HIGH - A committed refund claim can remain permanently PROCESSING

Refund execution commits `PROCESSING` before the provider call (`PaymentRefundTransactionService.java:33-61`), but another execution accepts only `APPROVED` or `PENDING_PROVIDER_CONFIRMATION` (`PaymentRefundTransactionService.java:38-41`). `PaymentRefund.markProcessing` records no lease timestamp (`PaymentRefund.java:126-129`). A process stop after provider success, or a failure while recording the result, leaves the refund neither retryable nor reconcilable.

Status: **Open / blocking**. Deferred because a stale-claim lease and provider reconciliation rule must be designed together. Recommended owner: `sa`/`se`, verified by `re`.

### MEDIUM - Reconciliation detects ambiguity but cannot finalize it

Provider reconciliation identifies `provider DONE / local not finalized` (`PaymentReconciliationService.java:154-164`) but only emits an issue; it does not persist provider success or invoke purpose-specific finalize-only logic. This prevents blind re-charge but leaves `PENDING_PROVIDER_CONFIRMATION` orders without an automated recovery path.

Status: **Open**. Recommended owner: `sa`/`se`.

### MEDIUM - Lock-order and concurrency proof are incomplete

Upgrade and renewal finalizers lock agreement, then order, then subscription (`PaymentCommandTransactionService.java:507-521`, `PaymentCommandTransactionService.java:560-569`), contrary to the documented agreement -> subscription -> order order. The focused concurrency tests use `@DataJpaTest`/H2 (`PaymentCommandIndependentVerificationIntegrationTest.java:46-60`), not MySQL/InnoDB. The refund concurrency helper converts every exception into the expected loser result (`PaymentRefundResilienceIntegrationTest.java:168-180`), so unrelated failures can satisfy the test.

Status: **Open**. MySQL schema proof is aligned, but MySQL concurrency behavior is not proved.

### HIGH - Provider ambiguity and refund evidence defect fixed in WI-023

Toss 5xx, transport interruption, malformed successful charge evidence, and missing billing-key evidence now surface as an unknown outcome rather than deterministic failure (`TossBillingProvider.java:92-132`, `TossBillingProvider.java:320-374`). Refund 5xx remains pending, and a success now requires matching `orderId` plus the authoritative `lastTransactionKey` (`TossBillingProvider.java:438-472`). The adapter never substitutes `paymentKey` or infers the current refund from cancellation history (`TossBillingProvider.java:498-538`).

This matches the Toss Payment contract, where `lastTransactionKey` identifies the latest approval/cancel transaction and each cancel has its own `transactionKey`: https://docs.tosspayments.com/reference

Status: **Resolved by WI-023**.

## Verified Behavior

- WI-018 refund split is present: reservation/result writes use separate `REQUIRES_NEW` transactions and the provider call runs under `NOT_SUPPORTED` (`AdminPaymentRefundService.java:148-174`).
- Refund `Idempotency-Key` forwarding is correct from the persisted claim to the HTTP header (`AdminPaymentRefundService.java:153-164`, `TossBillingProvider.java:306`). Retry reuse is asserted at `PaymentRefundResilienceIntegrationTest.java:101-135`, and exact header forwarding at `TossBillingProviderTest.java:348-393`.
- `schema.sql` contains payment command, renewal-period, provider-transaction, and refund-idempotency unique keys (`schema.sql:486-545`, `schema.sql:602-646`) and aligned audit ENUMs (`schema.sql:809-830`). WI-035's successful Hibernate validation evidence was reviewed. WI-023 did not connect to MySQL or any external/persistent database; selected `@DataJpaTest` classes used isolated in-memory H2 contexts.

## Decision

**Review complete; payment integrity approval is blocked.** WI-025, WI-026, and WI-034 should not consume WI-023 as an approval until the three open HIGH findings are remediated and independently retested. There are no confirmed Critical findings.

## Changes And Ownership

WI-023 owns only:

- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/PaymentProviderOutcomeUnknownException.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java`
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java`
- `deliverables/user/WI-20260714-ATS-023-summary.md`
- `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md`

No unrelated edits were reverted.

## Focused Verification

- PASS: `gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`
- PASS: six explicitly selected payment contract classes covering renewal commands, refund resilience, command concurrency, provider-success recovery, failure persistence, and DDL contract.
- Not run: full Gradle suite, live Toss, MySQL, or any external/persistent DB mutation. Selected integration tests used ephemeral in-memory H2 only.
