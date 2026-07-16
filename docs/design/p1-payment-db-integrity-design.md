---
version: 1.1
last_updated: 2026-07-15
project: ATS
owner: SA
category: design
status: archived
archived_date: 2026-07-15
archive_reason: "Superseded by the implemented P1 payment-integrity remediation design"
replacement_path: p1-payment-integrity-remediation-design.md
dependencies:
  - path: ../standards/core-principles.md
    reason: Financial traceability and approval rules
  - path: ../standards/development-standards.md
    reason: Spring transaction, JPA, and test standards
  - path: payment-integration-design.md
    reason: Existing recurring-payment contract
  - path: payment-refund-receipt-settlement-policy.md
    reason: Refund and financial-evidence policy
  - path: ../../deliverables/user/REQ-20260714-ATS-001.md
    reason: Approved P1 remediation scope
---

# P1 Payment and Database Integrity Design

> Purpose: Define the implementation contract for `ATS020-P1-05` through
> `ATS020-P1-10` and the payment/database portion of `ATS020-X-01` without
> assuming that Toss participates in an ATStudio database transaction.

> **Archived reference:** This document preserves the 2026-07-14 payment/DB
> baseline, original gaps, approval points, and migration cautions. It is not the
> current implementation SoT. Use
> [P1 Payment Integrity Remediation Design](p1-payment-integrity-remediation-design.md)
> for the implemented contract and
> [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md)
> for current code/test evidence. Retained-database applicability remains open.

## TL;DR

- A payment command has one `payment_orders` row, one persisted command key,
  and at most one finalized `subscription_payments` row.
- Provider calls run with no local transaction. Short `REQUIRES_NEW`
  transactions claim work, persist provider outcome, and finalize local state.
- Initial confirm, upgrade, and renewal use the same command lifecycle:
  `IN_PROGRESS -> PROCESSING -> PROVIDER_SUCCEEDED -> DONE`, with durable
  `FAILED` and `PENDING_PROVIDER_CONFIRMATION` exits.
- Renewal identity is `(billing_agreement_id, user_subscription_id,
  billing_period_start)` and never "latest non-DONE order".
- Refund reservation locks the source `subscription_payments` row before
  summing reserved refunds and inserting a new request.
- Fresh DDL and one ordered existing-DB patch must expand Java/MySQL ENUMs and
  add command/finalization constraints. Actual DB application remains a
  separate approval gate.

## 1. Scope and Traceability

| Finding | Required invariant | Primary implementation boundary |
|---|---|---|
| `ATS020-P1-05` | Every Java audit action/target persists under strict MySQL mode. | Java ENUMs, `schema.sql`, ordered manual patch, MySQL proof |
| `ATS020-P1-06` | A reported provider/confirm failure remains committed after the API throws. | Non-transactional orchestrator plus `REQUIRES_NEW` outcome writer |
| `ATS020-P1-07` | Concurrent confirm/upgrade/renewal calls converge on one command and one local finalization. | Command key, row claim, order/payment unique constraints |
| `ATS020-P1-08` | Renewal orders are scoped to one agreement, subscription row, and billing period. | `billing_period_start`, exact repository lookup, unique key |
| `ATS020-P1-09` | One agreement failure cannot roll back another agreement's local result. | Keyset candidate scan and per-agreement command transactions |
| `ATS020-P1-10` | Reserved refunds never exceed the source payment amount. | Source-payment pessimistic lock plus aggregate check |
| `ATS020-X-01` | Fresh and retained MySQL paths are distinguishable and reproducible. | Fresh DDL, preflight inventory, ordered patch, copied-DB rehearsal |

Out of scope: distributed locks, multiple scheduler owners, multi-PG behavior,
Testcontainers, live Toss calls, production DB changes, and automatic deletion
or rewriting of ambiguous legacy ledger rows.

## 2. Confirmed Current-State Gaps

The following are current source observations, not assumptions:

- `BillingAgreementApplicationService.confirmBillingAgreement()` mutates the
  order/agreement and throws `BusinessException` in one default transaction.
  The mutation can roll back with the error.
- `UserSubscriptionService.changeSubscription()` uses
  `noRollbackFor = BusinessException.class`, but still holds one transaction
  across the provider call and creates a random upgrade order per request.
- `RecurringRenewalService.processDueRenewals()` processes the whole candidate
  list in one transaction. Its current reuse predicate accepts every non-DONE
  renewal order, even when the period differs.
- `BillingAgreementRepository.findByIDForRenewal()` already has a pessimistic
  agreement lock. The lock is currently retained by the batch transaction and
  therefore does not provide per-agreement isolation.
- `AdminPaymentRefundService.createRefund()` sums reserved rows and inserts a
  request without locking the source `SubscriptionPayment`.
- `subscription_payments.payment_order_id` is nullable and non-unique.
- Java includes settlement audit actions and `PAYMENT_SETTLEMENT`; executable
  MySQL DDL omits those values.
- Existing-DB history is incomplete: the 2026-06-15 patch requires earlier
  payment migrations that are not present in this repository.

## 3. Non-Negotiable Invariants

### 3.1 Command and ledger invariants

1. Every provider money-movement attempt is represented by exactly one
   `PaymentOrder` before the provider call.
2. The provider idempotency key is persisted before use and is never generated
   from a mutable counter at call time.
3. A command may have multiple intentional attempts, but one attempt number has
   one persisted provider idempotency key.
4. Provider success is committed as `PROVIDER_SUCCEEDED` before local
   subscription finalization starts.
5. At most one `SubscriptionPayment` references a `PaymentOrder`.
6. The same `(provider, pg_transaction_id)` cannot finalize two local payments.
7. A retry of `PROVIDER_SUCCEEDED` performs local finalization only; it does not
   call the provider again.
8. `FAILED`, `PENDING_PROVIDER_CONFIRMATION`, and `PROVIDER_SUCCEEDED` are
   durable operational outcomes, not transient in-memory states.

### 3.2 Lock invariants

All locks are row-level and held only inside short local transactions. The
global acquisition order is:

1. `BillingAgreement`
2. `UserSubscription`
3. `PaymentOrder`
4. `SubscriptionPayment`
5. `PaymentRefund`

Initial confirm is owned by the agreement linked from the order; upgrade and
renewal are owned by their agreement; refund reservation is owned by the source
payment. Implementations must revalidate all IDs after obtaining locks.

### 3.3 External-call invariant

No method that calls `RecurringPaymentProvider`, `PaymentProvider`, or
`PaymentRefundProvider` may have an active local transaction. Provider calls
occur between committed transaction phases.

## 4. Payment Command Model

### 4.1 Required `PaymentOrderStatus` values

Add these values to `PaymentOrderStatus` and both fresh and upgrade DDL:

| State | Meaning | Retry rule |
|---|---|---|
| `PROCESSING` | One persisted attempt has been claimed. | Duplicate caller returns in-progress; it does not call Provider. |
| `PROVIDER_SUCCEEDED` | Provider success evidence is durable; local finalization remains. | Finalize locally without another Provider call. |
| `PENDING_PROVIDER_CONFIRMATION` | Provider result is ambiguous or a claimed process became stale. | Reconcile by order ID where supported; never create a new charge blindly. |

Existing terminal states remain `DONE`, `FAILED`, `CANCELLED`, and `EXPIRED`.
`FAILED` is terminal for initial confirm. Upgrade may open the next persisted
attempt only on a subsequent explicit API call. Renewal may open the next
attempt only when `next_billing_at` makes the retry due.

### 4.2 Required `PaymentOrder` fields

| Java field | Column | Contract |
|---|---|---|
| `commandKey` | `command_key VARCHAR(191) NULL` | Unique business command identity for all new provider commands. Nullable only for retained legacy rows. |
| `billingPeriodStart` | `billing_period_start DATE NULL` | Required for `RENEWAL`; null otherwise. |
| `providerAttempt` | `provider_attempt INT NOT NULL DEFAULT 0` | Incremented in the claim transaction. |
| `providerIdempotencyKey` | `provider_idempotency_key VARCHAR(100) NULL` | Set before a charge/refund-capable provider call. |
| `processingStartedAt` | `processing_started_at DATETIME NULL` | Supports stale-command detection without a distributed lease. |

Required entity transitions:

- `claimProviderAttempt(String idempotencyKey, LocalDateTime now)`
- `markProviderSucceeded(String transactionID, String providerPayload)`
- `markProviderFailure(String code, String message)`
- `markProviderOutcomeUnknown(String code, String message)`
- `markDone(UserSubscription userSubscription)`

`markDone()` must not erase `pgTransactionId`; it may transition only from
`PROVIDER_SUCCEEDED`, except zero-amount `BILLING_AGREEMENT` completion where no
charge exists.

### 4.3 Command keys

The exact canonical forms are:

```text
BILLING_CONFIRM:{orderId}
UPGRADE:{userSubscriptionId}:{startedAt}:{expiresAt}:{targetSubscriptionId}:{targetBillingCycle}
RENEWAL:{billingAgreementId}:{userSubscriptionId}:{billingPeriodStart}
```

Dates use ISO `yyyy-MM-dd`; enum names use their Java `name()`. Command keys
must be built by one new `PaymentCommandKeyFactory` class, not duplicated in
services.

Provider attempt keys are persisted with these forms:

```text
billing-initial-{orderId}-attempt-{providerAttempt}
subscription-upgrade-{orderId}-attempt-{providerAttempt}
renewal-{orderId}-attempt-{providerAttempt}
```

The billing-key issue call itself currently has no provider idempotency
parameter. Its duplicate protection therefore depends on the local agreement
claim and requires the stale-processing rule in Section 8.

## 5. Spring Transaction Boundaries

Introduce `PaymentCommandTransactionService` as a separate Spring bean so
`REQUIRES_NEW` is applied through the proxy and not bypassed by self-invocation.

| Symbol | Propagation | Responsibility |
|---|---|---|
| `claimBillingConfirm(...)` | `REQUIRES_NEW` | Lock agreement then order; validate and set `PROCESSING`. |
| `storeIssuedBillingKey(...)` | `REQUIRES_NEW` | Lock agreement/order; persist protected key material after successful issue. |
| `claimUpgrade(...)` | `REQUIRES_NEW` | Lock agreement/subscription; find or create one command and claim one attempt. |
| `claimRenewal(...)` | `REQUIRES_NEW` | Lock agreement/subscription; find exact period order and claim due attempt. |
| `recordProviderSuccess(...)` | `REQUIRES_NEW` | Lock in global order; persist sanitized provider evidence and `PROVIDER_SUCCEEDED`. |
| `recordProviderFailure(...)` | `REQUIRES_NEW` | Persist `FAILED` or `PENDING_PROVIDER_CONFIRMATION` plus agreement retry state. |
| `finalizeInitialCharge(...)` | `REQUIRES_NEW` | Idempotently create subscription/payment and activate agreement. |
| `finalizeUpgrade(...)` | `REQUIRES_NEW` | Idempotently create payment and apply one plan transition. |
| `finalizeRenewal(...)` | `REQUIRES_NEW` | Idempotently create payment, advance one period, and reset agreement failure state. |

The orchestrators below must run with `Propagation.NOT_SUPPORTED` or have no
`@Transactional` annotation:

- `BillingAgreementApplicationService.confirmBillingAgreement()`
- the immediate-charge branch of `UserSubscriptionService.changeSubscription()`
- `RecurringRenewalService.processDueRenewals()` and per-agreement provider call
- `AdminPaymentRefundService.executeRefund()` provider call
- `SubscriptionScheduler.processRecurringRenewals()`

Read-only endpoints remain read-only transactions. Zero-amount plan changes
remain one ordinary local transaction because no external side effect exists.

## 6. Ordered Command Flows

### 6.1 Initial billing confirm

1. `claimBillingConfirm()` locks agreement then order. `DONE` returns the
   existing response; `PROCESSING` returns an in-progress error; terminal
   failure returns the existing failure. A valid command becomes `PROCESSING`.
2. Outside a transaction, call `confirmAgreement()` once.
3. On issue failure, `recordProviderFailure()` commits `FAILED`, then the API
   throws `BILLING_AGREEMENT_CONFIRM_FAILED`.
4. On issue success, encrypt in memory and commit the ciphertext/fingerprint
   with `storeIssuedBillingKey()` before any initial charge.
5. Outside a transaction, call `charge()` with the persisted attempt key.
6. Commit charge result. Success becomes `PROVIDER_SUCCEEDED`; deterministic
   failure becomes `FAILED`; ambiguous exception becomes
   `PENDING_PROVIDER_CONFIRMATION`.
7. On success, `finalizeInitialCharge()` creates/updates the subscription,
   inserts one `SubscriptionPayment`, marks the order `DONE`, and activates the
   agreement in one transaction.
8. Receipt evidence remains an `AFTER_COMMIT` operation.
9. If finalization fails, the order remains `PROVIDER_SUCCEEDED`; a retry or
   reconciliation finalizes locally without another charge.

Billing-key cleanup after an initial-charge failure must run after the failed
outcome commits. Provider cleanup success clears local key material in a new
transaction. Cleanup failure retains encrypted material and creates a
deduplicated Incident; it must not erase the failed payment command.

### 6.2 Upgrade

1. `claimUpgrade()` locks agreement then current subscription and recomputes
   the prorated amount from locked state.
2. Zero amount completes as a local-only transition.
3. Positive amount uses the canonical upgrade command key. The transaction
   creates or reuses one order, increments `providerAttempt`, persists the
   provider key, and commits `PROCESSING`.
4. Call Provider outside a transaction.
5. Commit provider outcome, then finalize the plan/payment in a separate
   transaction on success.
6. A concurrent caller seeing `PROCESSING` does not charge. A caller seeing
   `PROVIDER_SUCCEEDED` runs local finalization only. A subsequent explicit
   request after `FAILED` may claim the next attempt on the same order.

### 6.3 Renewal

1. Scan due agreement IDs in ascending-ID keyset pages; do not use an offset
   over a set whose `next_billing_at` changes during processing.
2. For each ID, `claimRenewal()` locks only that agreement, its active
   subscription, and its exact-period order.
3. The order lookup is exact by agreement, user subscription, purpose, and
   `billingPeriodStart = agreement.nextBillingAt`.
4. A missing order is created with the canonical renewal command key. An order
   from another period or subscription is never reused.
5. Commit the due attempt, call Provider outside a transaction, commit the
   outcome, then finalize success in another transaction.
6. Deterministic failure commits order failure, agreement failure count, next
   retry date, and grace-period mutation together.
7. The outer loop catches one agreement's exception, records only safe IDs and
   exception type, increments the failed count, and continues.

`SubscriptionScheduler.processRecurringRenewals()` must not own a transaction.
This guarantees that a later agreement cannot roll back an earlier committed
provider outcome or local finalization.

### 6.4 Refund reservation and execution

Create `SubscriptionPaymentRepository.findWithGraphByIdForUpdate(Long id)` with
`PESSIMISTIC_WRITE`. `createRefund()` must:

1. Lock the source payment.
2. Revalidate `DONE`, provider, provider payment key, and amount.
3. Sum `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, and
   `PENDING_PROVIDER_CONFIRMATION` rows while the source lock is held.
4. Reject when `reserved + requested > payment.amount`.
5. Insert the request and audit row in the same transaction.

`previewRefund()` remains advisory and unlocked. Only `createRefund()` is an
authorization decision.

Provider execution must use a new `PaymentRefundTransactionService`:

1. `claimExecution()` locks the refund, commits `PROCESSING`, and preserves the
   existing unique `idempotency_key`.
2. Call `PaymentRefundProvider` outside a transaction.
3. `recordExecutionResult()` commits `SUCCEEDED`, `FAILED`, or
   `PENDING_PROVIDER_CONFIRMATION` and its audit row.
4. A retry reuses the same refund row and idempotency key. It never creates a
   replacement refund request to bypass ambiguity.

## 7. Exact Database Changes

### 7.1 Fresh schema and JPA mapping

Apply equivalent JPA annotations and the following DDL contract:

```sql
ALTER TABLE payment_orders
    MODIFY COLUMN status ENUM (
        'READY', 'IN_PROGRESS', 'PROCESSING', 'PROVIDER_SUCCEEDED',
        'PENDING_PROVIDER_CONFIRMATION', 'DONE', 'FAILED', 'CANCELLED', 'EXPIRED'
    ) NOT NULL DEFAULT 'READY',
    ADD COLUMN command_key VARCHAR(191) NULL AFTER order_id,
    ADD COLUMN billing_period_start DATE NULL AFTER billing_cycle,
    ADD COLUMN provider_attempt INT NOT NULL DEFAULT 0 AFTER billing_period_start,
    ADD COLUMN provider_idempotency_key VARCHAR(100) NULL AFTER provider_attempt,
    ADD COLUMN processing_started_at DATETIME NULL AFTER provider_idempotency_key,
    ADD UNIQUE KEY uq_payment_orders_command_key (command_key),
    ADD UNIQUE KEY uq_payment_orders_provider_attempt_key
        (provider, provider_idempotency_key),
    ADD UNIQUE KEY uq_payment_orders_renewal_period
        (billing_agreement_id, user_subscription_id, purpose, billing_period_start),
    ADD KEY idx_payment_orders_status_processing
        (status, processing_started_at);

ALTER TABLE subscription_payments
    MODIFY COLUMN pg_transaction_id VARCHAR(200) NULL,
    ADD UNIQUE KEY uq_subscription_payments_order (payment_order_id),
    ADD UNIQUE KEY uq_subscription_payments_provider_transaction
        (provider, pg_transaction_id);

ALTER TABLE payment_operation_audit_logs
    MODIFY COLUMN action ENUM (
        'RECONCILIATION_INCIDENT_STATUS_UPDATE',
        'RECEIPT_EVIDENCE_CREATED',
        'PAYMENT_REFUND_REQUESTED',
        'PAYMENT_REFUND_APPROVED',
        'PAYMENT_REFUND_PROCESSING',
        'PAYMENT_REFUND_SUCCEEDED',
        'PAYMENT_REFUND_FAILED',
        'PAYMENT_REFUND_PENDING_PROVIDER_CONFIRMATION',
        'PAYMENT_ENTITLEMENT_CORRECTION_REQUESTED',
        'PAYMENT_ENTITLEMENT_CORRECTION_APPROVED',
        'PAYMENT_ENTITLEMENT_CORRECTION_PROCESSING',
        'PAYMENT_ENTITLEMENT_CORRECTION_SUCCEEDED',
        'PAYMENT_ENTITLEMENT_CORRECTION_FAILED',
        'PAYMENT_SETTLEMENT_IMPORTED',
        'PAYMENT_SETTLEMENT_RECONCILED',
        'PAYMENT_SETTLEMENT_IGNORED'
    ) NOT NULL,
    MODIFY COLUMN target_type ENUM (
        'RECONCILIATION_INCIDENT', 'PAYMENT_RECEIPT', 'PAYMENT_REFUND',
        'PAYMENT_ENTITLEMENT_CORRECTION', 'PAYMENT_SETTLEMENT'
    ) NOT NULL;
```

`payment_refunds.idempotency_key` is already unique; no new refund-table
constraint is required for P1-10.

### 7.2 Existing-DB manual patches

The payment command patch and the later reconciliation index patch are:

- `src/main/resources/db/manual/20260714_payment_db_integrity.sql`
- `src/main/resources/db/manual/20260716_payment_reconciliation_indexes.sql`

Existing-DB order is:

1. Restore or explicitly approve the missing payment-operations baseline.
2. `20260615_align_payment_whitelist_schema.sql`
3. `20260618_company_certification_documents.sql`
4. `20260714_payment_db_integrity.sql`
5. `20260716_payment_reconciliation_indexes.sql`
6. Start with `ddl-auto=validate`.

The 2026-07-14 patch must execute in this internal order:

1. Preflight table/column/ENUM inventory and abort on missing baseline objects.
2. Abort if any existing nonterminal order is present during cutover.
3. Abort on duplicate non-null `payment_order_id` or
   `(provider, pg_transaction_id)` in `subscription_payments`.
4. Add nullable command columns and expand ENUMs.
5. Backfill `billing_period_start` for legacy renewal rows as
   `DATE(expires_at) - INTERVAL 3 DAY`; report every row for which this does not
   match the retained 3-day-grace assumption.
6. Backfill one canonical non-duplicate renewal command as
   `RENEWAL:{agreement}:{userSubscription}:{period}`. Backfill all other legacy
   rows as `LEGACY:{order_id}`.
7. Abort on duplicate renewal period groups. Do not delete or rewrite a ledger
   row automatically.
8. Add unique keys and query indexes.
9. Compare `information_schema` column types/indexes with this contract.

The three-day backfill formula is a current-code compatibility assumption, not
proof of historical production policy. A mismatch or duplicate is a migration
blocker requiring an approved, row-specific disposition.

The 2026-07-16 patch is additive and data-preserving. It adds
`idx_payment_orders_local_reconciliation (status, id, purpose)` and
`idx_billing_agreements_local_reconciliation (status, id)`, then provides
`EXPLAIN FORMAT=JSON` statements for the two keyset scans. Run those statements
only on an approved copied/disposable MySQL 8 database with representative row
counts. Source and static contract tests do not close retained-database
compatibility or production query-plan evidence; `ATS020-X-01` remains
environment-conditional.

## 8. Failure Recovery and Observability

- A `PROCESSING` order older than 15 minutes is stale by proposed default.
- For charge commands, reconcile by `orderId` before any replay. Provider DONE
  becomes `PROVIDER_SUCCEEDED`; Provider not-found may become `FAILED`; lookup
  failure remains `PENDING_PROVIDER_CONFIRMATION`.
- Billing-key issue has no current provider lookup interface. A stale initial
  issue must not be replayed automatically; require re-authentication and keep
  an Incident until cleanup state is known.
- Logs may contain order ID, agreement ID, purpose, attempt number, state,
  sanitized failure code, and exception class. They must not contain billing
  keys, auth keys, customer keys, raw provider payloads, card data, or secrets.
- Count command claims, duplicate/in-progress responses, provider outcomes,
  finalization retries, renewal per-agreement failures, refund lock rejections,
  and stale commands.

## 9. Focused Test Contract

### Unit and Spring transaction tests

- `BillingAgreementFailurePersistenceIntegrationTest` reloads the order in a
  new transaction after a declined initial charge and verifies durable failure.
- `PaymentProviderSuccessRecoveryIntegrationTest` forces local finalization
  failure after provider success, verifies `PROVIDER_SUCCEEDED`, retries, and
  proves no second provider call.
- `PaymentCommandConcurrencyMySqlTest` covers concurrent initial confirm and
  upgrade; one command key and one finalized payment must remain.
- `RecurringRenewalServiceTest` proves an old FAILED period and a replacement
  subscription cannot supply the current renewal order.
- `RecurringRenewalConcurrencyMySqlTest` proves one agreement/period
  finalization under concurrent workers.
- `RecurringRenewalBatchIntegrationTest` proves agreement B failure does not
  roll back agreement A.
- `AdminPaymentRefundConcurrencyMySqlTest` proves concurrent reservations do
  not exceed the source amount.
- `AdminPaymentRefundExecutionIntegrationTest` proves provider execution result
  is durable and retry reuses the original idempotency key.

### Disposable MySQL 8 proof

Use the existing MySQL JDBC driver and Spring test stack; do not add
Testcontainers. After separate approval provisions an empty disposable schema:

1. Load corrected `schema.sql`; start with `ddl-auto=validate`.
2. Persist every `PaymentOperationAuditAction` and
   `PaymentOperationAuditTargetType`, including settlement values, and flush.
3. Run concurrency tests against InnoDB, not H2.
4. Load an approved pre-patch fixture, apply the ordered manual patches, and
   repeat Hibernate validation and ENUM flush tests.
5. Run negative fixtures proving duplicate finalization and duplicate renewal
   periods stop the patch before constraints are applied.

H2 remains useful for unit behavior but is not evidence for MySQL ENUM,
implicit-commit migration behavior, or pessimistic-lock concurrency.

## 10. Rollout and Rollback

1. Complete code and tests against fresh disposable MySQL.
2. Rehearse the retained-DB patch only on a copied database after approval.
3. Apply the additive schema expansion before deploying code that writes new
   states/columns.
4. Deploy application code and run Hibernate validation plus focused smoke.
5. Keep the new nullable columns and expanded ENUM values during code rollback.

Rollback is application-first. Do not contract ENUMs, drop unique keys, or
delete payment/refund/audit rows during an incident. MySQL DDL implicitly
commits; a failed rehearsal is restored from the disposable copy. Any shared-DB
rollback requires backup restore or a separately reviewed forward patch.

## 11. Exact Impacted Files and Symbols

| File | Required symbols/change |
|---|---|
| `entity/PaymentOrder.java` | New command fields and state transition methods |
| `entity/enums/PaymentOrderStatus.java` | Three new states |
| `entity/SubscriptionPayment.java` | Unique order/provider transaction mappings and 200-char provider ID |
| `repository/PaymentOrderRepository.java` | Lock lookup, command-key lookup, exact renewal-period lookup, stale query |
| `repository/BillingAgreementRepository.java` | Keyset due-ID query; retain agreement lock |
| `repository/SubscriptionPaymentRepository.java` | Source-payment lock and finalization lookup |
| `service/PaymentCommandKeyFactory.java` | Canonical command/idempotency key generation |
| `service/PaymentCommandTransactionService.java` | All short `REQUIRES_NEW` command phases |
| `service/BillingAgreementApplicationService.java` | Non-transactional initial-confirm orchestration |
| `service/UserSubscriptionService.java` | Split charged upgrade from local-only changes |
| `service/RecurringRenewalService.java` | Keyset scan and per-agreement orchestration |
| `service/SubscriptionScheduler.java` | Remove transaction from recurring-renewal entry point |
| `service/PaymentRefundTransactionService.java` | Refund execution claim/result transactions |
| `service/AdminPaymentRefundService.java` | Source lock on create and non-transactional provider execution |
| `schema.sql` | Command columns/statuses, unique keys, audit ENUM alignment |
| `db/manual/20260714_payment_db_integrity.sql` | Preflight, backfill, DDL, post-validation |

`PaymentApplicationService.confirmPayment()` remains blocked for subscription
`SUBSCRIBE`/`UPGRADE`. It must recognize the expanded terminal/recovery statuses
but must not become a second subscription-payment implementation.

## 12. Approval Points and Blockers

| ID | Decision required | Recommended default | Blocks |
|---|---|---|---|
| `PAYDB-AP-01` | Approve command states/columns and additive unique constraints. | Approve this model. | WI-004, WI-006, WI-007 |
| `PAYDB-AP-02` | Approve 15-minute stale `PROCESSING` threshold and no blind replay. | Approve; re-auth billing-key issue when lookup is unavailable. | WI-005, WI-006, WI-007 |
| `PAYDB-AP-03` | Initial-charge failure key cleanup policy. | Retain encrypted key until tracked cleanup succeeds; never clear it on ambiguous failure. | WI-005, WI-006 |
| `PAYDB-AP-04` | Retained DB baseline and duplicate-row disposition. | Inventory copied DB; block rather than auto-edit ambiguous ledgers. | WI-004, WI-021 |
| `PAYDB-AP-05` | Disposable MySQL provisioning method. | User-provisioned MySQL 8 schema with existing JDBC driver; no new dependency. | WI-018, WI-021 |
| `PAYDB-AP-06` | Maker-checker threshold for large refunds. | Keep as a production-policy blocker; implement P1 source lock independently. | Production refund approval, not WI-008 locking |

Known uncertainty: no retained DB inventory or complete earlier payment migration
chain was available to this WI. The patch contract is exact, but its applicability
to a specific retained database is intentionally unconfirmed until WI-021.

## Related Documents

### Required References

- [P1 Payment Integrity Remediation Design](p1-payment-integrity-remediation-design.md)
- [Payment Integration Design](payment-integration-design.md)
- [Payment Refund, Receipt, Settlement, and Tax Invoice Policy](payment-refund-receipt-settlement-policy.md)
- [Payment Operations Runbook](payment-operations-runbook.md)
- [Approved P1 Remediation REQ](../../deliverables/user/REQ-20260714-ATS-001.md)
- [P1 Payment Integrity Closure](../audit/p1-payment-integrity-closure-20260715.md)

### Follow-up Work Items

- `WI-20260714-ATS-004` through `WI-20260714-ATS-008`
- `WI-20260714-ATS-015`
- `WI-20260714-ATS-018`
- `WI-20260714-ATS-021`
- `WI-20260714-ATS-023`
- `WI-20260714-ATS-025`
