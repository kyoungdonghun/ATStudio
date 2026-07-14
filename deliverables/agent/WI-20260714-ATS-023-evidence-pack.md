# Evidence Pack: WI-20260714-ATS-023

## Summary (one-liner)

- Independent review found three open High payment-integrity defects, two open Medium defects, and one High provider-adapter defect resolved in WI-023; downstream payment approval remains blocked.

## Review Decision

- Decision: **COMPLETE WITH BLOCKING FINDINGS / NOT APPROVED**
- Critical findings: 0
- High findings: 4 total; 3 open and explicitly deferred, 1 resolved in WI-023
- Medium findings: 2 open
- Downstream: WI-20260714-ATS-025, WI-20260714-ATS-026, and WI-20260714-ATS-034 remain blocked on remediation and independent verification.

## Severity-Ordered Findings

### F-01 - HIGH - Renewal retry changes command identity (Open / Blocking)

Evidence:

- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:254-255` uses `agreement.nextBillingAt` as `billingPeriodStart` and renewal lookup identity.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:289-295` derives the command and provider-attempt keys from that period/order.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:409-414` records a retry date after deterministic provider failure.
- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java:189-192` overwrites `nextBillingAt` with that retry date.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:651-695` performs exact-period lookup and creates a new order when the period changed.
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java:125-155` asserts `nextBillingAt = due + 1 day` but does not invoke a next-day retry.

Impact: the next scheduler run can create a second `PaymentOrder` with a new `orderId`, `commandKey`, and provider idempotency key for one logical renewal. `uq_payment_orders_renewal_period` cannot converge rows whose period value changed.

Deferral rationale: a safe fix requires a precise retryable-order selection contract that does not regress the WI-018 prohibition on reusing an arbitrary latest non-DONE order. Add a two-day test proving same order/command, incremented attempt, and bounded grace.

Recommended ownership: `se`; independent verification by `re`.

### F-02 - HIGH - External provider calls remain within broad transaction lifetimes (Open / Blocking)

Evidence:

- Billing cancellation: `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:320-347` is transactional and calls `cancelAgreement` at lines 332-335.
- Withdrawal cleanup: `src/main/java/com/atstudio/atstudio/service/WithdrawalBillingCleanupService.java:49-68` starts `REQUIRES_NEW` and calls the provider in that transaction.
- Scheduled reconciliation: `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:48-53` starts a transaction and self-invokes provider reconciliation; the lookup call is at lines 107-132.
- Charged upgrade: `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java:120-160` opens the outer transaction, reaches the provider at lines 337-360, and `src/main/java/com/atstudio/atstudio/service/SubscriptionUpgradePaymentExecutor.java:26-28` only suspends that existing transaction during the call.

Impact: a transaction/lock lifetime spans provider latency; provider success followed by commit failure can leave provider/local split state. `NOT_SUPPORTED` at the nested upgrade executor proves no active transaction on the network thread, but it does not remove the suspended broad transaction lifetime.

Deferral rationale: these paths require claim/provider/result orchestration splits and purpose-specific failure persistence. An adapter-only change would not close the defect.

Recommended ownership: `se`; review by `cr`, tests by `re`.

### F-03 - HIGH - Refund PROCESSING claim has no stale recovery (Open / Blocking)

Evidence:

- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:33-61` commits a `PROCESSING` claim and its audit event before the provider call.
- The retry gate at lines 38-41 accepts only `APPROVED` or `PENDING_PROVIDER_CONFIRMATION`.
- `src/main/java/com/atstudio/atstudio/entity/PaymentRefund.java:126-129` marks processing without a dedicated claim/lease timestamp.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:148-174` correctly moves the provider call outside the claim/result transactions, but a process stop or result-write failure between those phases leaves `PROCESSING` stranded.
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:101-135` covers a provider exception that is immediately recorded as pending; it does not simulate provider success followed by process/result persistence failure.

Impact: the provider may have refunded while the local ledger and terminal audit state remain incomplete, and subsequent execution is permanently rejected.

Deferral rationale: safe recovery needs a stale claim lease, same-key replay/reconciliation semantics, and a crash-boundary test.

Recommended ownership: `sa`/`se`; independent verification by `re`.

### F-04 - MEDIUM - Reconciliation is detect-only, not finalize-capable (Open)

Evidence:

- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:154-164` detects provider DONE with a non-DONE local order and records an issue.
- No transition to `PROVIDER_SUCCEEDED` or purpose-specific finalize-only dispatch occurs in this path.

Impact: blind retries are prevented, but ambiguous orders have no automated convergence path and can remain pending indefinitely.

Recommended ownership: `sa`/`se`.

### F-05 - MEDIUM - Lock order and concurrency proof are incomplete (Open)

Evidence:

- Upgrade finalization locks agreement -> order -> subscription at `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:507-521`.
- Renewal finalization uses the same order at lines 560-569; renewal failure uses agreement -> order -> subscription at lines 380-393 and 962-966.
- This differs from the design lock order: BillingAgreement -> UserSubscription -> PaymentOrder -> SubscriptionPayment -> PaymentRefund.
- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java:46-60` is an H2-backed `@DataJpaTest`, not MySQL/InnoDB proof.
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:138-180` treats any exception as the expected losing reservation, allowing an unrelated timeout/deadlock/error to satisfy the test.

Impact: the documented deadlock-avoidance invariant is not consistently implemented, and the tests do not prove production-engine convergence or the specific loser reason.

Recommended ownership: `se` for lock order, `re`/`qa-integ` for MySQL concurrency proof.

### F-06 - HIGH - Ambiguous Toss outcomes and refund transaction evidence (Resolved)

Original defect: the adapter converted charge/billing-key transport errors, interrupts, 5xx responses, and successful-response mismatches to deterministic failures. Refund 5xx was also deterministic, and refund success selected the first cancel transaction or fell back to `paymentKey`. This could authorize a fresh payment attempt after an unknown outcome and could persist the wrong refund ledger identity.

Resolution evidence:

- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:92-132` throws `PaymentProviderOutcomeUnknownException` for billing-key/charge I/O and interruption.
- Server errors and invalid successful evidence are unknown at lines 320-374.
- Refund 5xx remains pending and success requires matching order/refund evidence at lines 438-472.
- Sanitized audit payload includes `lastTransactionKey`, and success requires that authoritative field at lines 498-538; `paymentKey` and an unordered cancellation-history entry are not used as current refund transaction evidence.
- Toss's Payment contract defines `lastTransactionKey` as the latest approval/cancel transaction and `Cancel.transactionKey` as the identifier for each cancellation: [Toss Core API reference](https://docs.tosspayments.com/reference).

Tests:

- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:135` billing-key 5xx unknown.
- Lines 217 and 247 cover successful-evidence mismatch and charge 5xx.
- Line 293 covers billing-key deletion 5xx.
- Lines 348, 405, 436, and 460 cover exact refund header/success evidence, latest transaction, 5xx pending, and missing transaction evidence.

Ownership: WI-20260714-ATS-023 (`cr`).

## Verified Contracts Without Findings

### WI-018 refund split and Idempotency-Key forwarding

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:148-174` uses `NOT_SUPPORTED` around claim -> provider -> result phases.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:33-64` and lines 99-109 persist claim/result/exception in separate `REQUIRES_NEW` transactions.
- The persisted claim key is passed at `AdminPaymentRefundService.java:153-164` and forwarded as the HTTP `Idempotency-Key` at `TossBillingProvider.java:306`.
- `PaymentRefundResilienceIntegrationTest.java:101-135` proves immediate provider-exception retry reuses the same key; `TossBillingProviderTest.java:348-393` proves the exact header.

### Unique constraints and schema alignment

- `src/main/resources/schema.sql:486-524` contains command key, provider-attempt key, and renewal-period unique constraints.
- `src/main/resources/schema.sql:528-550` contains one-payment-per-order and provider transaction unique constraints.
- `src/main/resources/schema.sql:602-646` aligns refund status/fields and idempotency uniqueness.
- `src/main/resources/schema.sql:809-830` aligns payment audit ENUM actions and target types.
- `src/test/java/com/atstudio/atstudio/entity/PaymentDatabaseIntegrityContractTest.java:85-113` statically asserts the core DDL contract and passed in this WI.
- WI-20260714-ATS-035 evidence reports successful Hibernate validation after the waveform schema fix. WI-023 did not connect to MySQL or any external/persistent database; selected `@DataJpaTest` classes used isolated in-memory H2 contexts.

## Acceptance Criteria

- [ ] Provider calls do not run inside broad local transactions: failed by F-02.
- [ ] Retry/finalize-only/stale ambiguity cannot duplicate charge or ledger effects: failed by F-01 and F-03; adapter ambiguity fixed by F-06.
- [ ] Lock order and unique keys converge upgrade, renewal, and refund: unique keys pass; lock/concurrency proof fails by F-05.
- [x] MySQL schema proof matches JPA/entity/enum contracts: WI-021/WI-035 evidence plus current focused DDL test reviewed; no new DB execution.
- [x] Residual risks and missing tests are explicitly assessed.

## Reference Documents Read

- Tier 0: `docs/standards/core-principles.md`, `docs/standards/development-standards.md`
- Tier 1: `docs/policies/security-policy.md`, `docs/policies/quality-gates.md`
- Context: `deliverables/user/REQ-20260714-ATS-001.md`, `docs/design/p1-payment-db-integrity-design.md`
- Prior evidence: WI-20260714-ATS-004, WI-005, WI-006, WI-007, WI-008, WI-018, WI-021, and WI-035 evidence packs
- Current implementation: payment entities, repositories, services, provider adapters, focused tests, `schema.sql`, and `20260714_payment_db_integrity.sql`

## Files Changed And Ownership

WI-023 owns:

- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/PaymentProviderOutcomeUnknownException.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java`
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java`
- `deliverables/user/WI-20260714-ATS-023-summary.md`
- `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md`

No unrelated worktree changes were reverted or claimed.

## Focused Test Evidence

1. `gradlew.bat test --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`
   - Result: PASS, rerun after final adapter-test additions.
2. `gradlew.bat test --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest"`
   - Result: PASS.

Not run by constraint: full Gradle suite, live Toss, MySQL, or any external/persistent DB mutation. The selected integration tests used ephemeral in-memory H2 only.

## Assumptions, Risks, And Rollback

Assumptions:

- Toss 4xx responses with a parsed provider error are treated as deterministic failures; transport/interrupt/5xx and inconsistent 2xx evidence are unknown.
- WI-021/WI-035 MySQL/Hibernate evidence is accepted as prior proof; this reviewer independently checked current schema/entity/test alignment without DB access.

Residual risks:

- F-01 through F-03 remain release-blocking payment-integrity risks.
- F-04 leaves ambiguous commands operationally stuck even when reconciliation detects provider success.
- F-05 leaves production-engine concurrency and deadlock behavior unproved.
- The focused tests passing does not negate these gaps because their scenarios are absent or assertions are insufficiently specific.

Rollback:

- Revert only the three WI-023 production/test files listed above if the adapter change must be withdrawn.
- Removing the exception behavior would restore deterministic classification of ambiguous payment outcomes and is not recommended without an equivalent pending-outcome mechanism.
- Deliverable files can be removed independently; no schema or data rollback is required.

## Required Follow-up

1. Repair renewal retry identity and add a next-day same-command test.
2. Split cancellation, cleanup, reconciliation, and charged-upgrade outer transaction lifetimes.
3. Add stale refund PROCESSING recovery plus provider-success/result-write-failure proof.
4. Add finalize-capable reconciliation for pending orders.
5. Align lock order and run a disposable MySQL/InnoDB concurrency proof with specific exception assertions.
