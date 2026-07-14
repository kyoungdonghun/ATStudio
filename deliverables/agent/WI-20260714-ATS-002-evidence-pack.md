# Evidence Pack: WI-20260714-ATS-002

## Summary

- Produced an implementation-ready payment/database integrity contract for
  `ATS020-P1-05` through `ATS020-P1-10` and payment scope of `ATS020-X-01`.

## Scope / DoD Check

- [x] Mapped P1-05 through P1-10 to concrete invariants and boundaries.
- [x] Defined ordered initial-confirm, upgrade, renewal, and refund flows.
- [x] Defined `NOT_SUPPORTED`/no-transaction provider orchestration and short
  `REQUIRES_NEW` persistence phases.
- [x] Defined bounded row locks, command keys, renewal identity, and unique
  finalization constraints.
- [x] Defined exact fresh-DDL changes and future manual-patch order.
- [x] Recorded legacy/backfill assumptions, stop conditions, rollback, tests,
  observability, approval points, and residual uncertainty.
- [x] Proposed disposable MySQL proof without creating a DB or dependency.
- [x] Modified only the three WI-owned outputs.

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability, approval, and language policy |
| 0 | `docs/standards/development-standards.md` | Spring/JPA/test standards |
| 0 | `docs/standards/documentation-standards.md` | Metadata, structure, links, terminology |
| 0 | `docs/standards/glossary.md` | Canonical WI/subscription terminology |
| 1 | `docs/architecture/system-design.md` | Dual-output and evidence rules |
| 1 | `docs/policies/quality-gates.md` | HIGH-impact rollback/review requirements |
| 1 | `docs/adr/ADR-20251230-001-reuse-first-registry-traceability.md` | Alternatives, risk, rollback traceability |
| 2 | `docs/design/payment-integration-design.md` | Existing order/agreement/renewal contract |
| 2 | `docs/design/payment-refund-receipt-settlement-policy.md` | Refund idempotency and entitlement separation |
| 2 | `docs/design/payment-operations-runbook.md` | Provider-success/local-failure operations |
| 2 | `docs/design/payment-settlement-import-design.md` | Settlement audit requirements |
| 2 | `docs/design/usecase/user-subscription.md` | Upgrade/renewal/cancellation behavior |
| 2 | `docs/payment/` | Current payment operations and acceptance claims |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 scope and separate approval gates |
| Context | `docs/audit/full-system-audit-20260713.md` | Canonical findings and exit boundary |

`docs/audit/p1-remediation-trace-matrix-20260714.md` was not available during
inspection. The handoff explicitly made it non-blocking; no claim depends on it.

## Evidence Pointers

### Owned outputs

- `docs/design/p1-payment-db-integrity-design.md`: architecture contract,
  exact DDL, transaction propagation, flows, tests, rollback, approvals.
- `deliverables/user/WI-20260714-ATS-002-summary.md`: Korean decision summary,
  risks, approval points, and WI-chain triggers.
- `deliverables/agent/WI-20260714-ATS-002-evidence-pack.md`: this evidence pack.

### Current transaction and concurrency evidence

- `BillingAgreementApplicationService.java:161-245`: initial billing issue,
  charge, local finalization, failure mutation, and thrown business error share
  one default transaction.
- `UserSubscriptionService.java:118-215`: charged upgrade uses
  `noRollbackFor`, creates a random order, calls Provider, and mutates local
  subscription in one method.
- `RecurringRenewalService.java:84-118`: all due agreements are processed by
  one transactional method.
- `RecurringRenewalService.java:177-207`: latest non-DONE order reuse is not
  bounded to the current period.
- `BillingAgreementRepository.java:30-40`: candidate IDs are unpaged and the
  agreement row lock is held by the caller's transaction.
- `SubscriptionScheduler.java:32-36`: scheduled renewal entry point also owns
  a transaction.
- `AdminPaymentRefundService.java:89-111,248-262`: reservation aggregate and
  insert occur without a source-payment lock.
- `PaymentRefundRepository.java:45-57`: refund-row execution lock exists, but
  aggregate reservation has no source lock.

### Current schema evidence

- `PaymentOrder.java:47-106`: no command key, period identity, persisted attempt
  key, processing time, or provider-success intermediate state.
- `SubscriptionPayment.java:36-42`: payment-order relation is non-unique.
- `schema.sql:485-538`: order ID is unique, but command/period/finalization
  constraints are absent.
- `PaymentOperationAuditAction.java:17-19`: Java includes three settlement
  actions.
- `PaymentOperationAuditTargetType.java:8`: Java includes
  `PAYMENT_SETTLEMENT`.
- `schema.sql:797-815`: executable MySQL audit ENUMs omit those values.
- `20260615_align_payment_whitelist_schema.sql:18-23`: retained DB patch
  requires an earlier payment baseline not present in the repository.
- `application.yml:16-20`: normal startup defaults to Hibernate `validate`.
- `src/test/resources/application.yml:1-7`: ordinary tests disable MySQL DDL
  and use H2 `create-drop`.
- `build.gradle:29,35,37,42`: existing Spring JPA/MySQL/H2 stack; no
  Testcontainers dependency was found.

## Decisions and Alternatives

| Decision | Chosen | Rejected/Deferred |
|---|---|---|
| External-call boundary | Provider call outside transaction; claim/outcome/finalize in `REQUIRES_NEW` | One long transaction; `noRollbackFor` as durability mechanism |
| Local idempotency | Canonical command key plus row claim and DB unique keys | Provider header alone; random order per duplicate request |
| Renewal identity | Agreement + user subscription + explicit period start | Latest non-DONE renewal order |
| Batch isolation | Keyset IDs and per-agreement phases | One transaction/list for all due agreements |
| Refund reservation | Lock source payment before aggregate and insert | Lock only the refund row after creation |
| MySQL proof | User-provisioned disposable MySQL 8 with existing JDBC stack | H2 as ENUM/lock evidence; new Testcontainers dependency |
| Legacy conflicts | Abort and require approved row-specific disposition | Automatic ledger deletion or silent link nulling |

## Exact Future Impact Surface

- Entities/ENUMs: `PaymentOrder`, `PaymentOrderStatus`, `SubscriptionPayment`.
- Repositories: `PaymentOrderRepository`, `BillingAgreementRepository`,
  `SubscriptionPaymentRepository`.
- New transaction helpers: `PaymentCommandKeyFactory`,
  `PaymentCommandTransactionService`, `PaymentRefundTransactionService`.
- Orchestrators: `BillingAgreementApplicationService.confirmBillingAgreement`,
  `UserSubscriptionService.changeSubscription`,
  `RecurringRenewalService.processDueRenewals`,
  `SubscriptionScheduler.processRecurringRenewals`,
  `AdminPaymentRefundService.createRefund/executeRefund`.
- DDL: `src/main/resources/schema.sql` and future
  `src/main/resources/db/manual/20260714_payment_db_integrity.sql`.
- Focused tests are named in the design document Section 9.

## Commands and Results

- Source inspection commands included handoff reproduction searches for
  transactions, locks, order IDs, audit ENUMs, schema constraints, and manual
  patches.
- `git diff --check -- docs/design/p1-payment-db-integrity-design.md deliverables/user/WI-20260714-ATS-002-summary.md deliverables/agent/WI-20260714-ATS-002-evidence-pack.md`
  - Result: PASS. Because the owned files are new/untracked, each file was also
    checked with `git diff --no-index --check -- NUL <file>`; all passed. Git
    emitted only the repository's LF-to-CRLF conversion warning.

No application tests, DB commands, provider calls, builds, runtime log writes,
or dependency changes were executed because this WI is design-only.

## Risks / Rollback

### Risks and blockers

- Retained DB baseline/history is incomplete; patch applicability is unproven.
- Existing duplicate finalizations/periods are unknown without DB inventory.
- Billing-key issue has no current Provider lookup operation, so stale issue
  recovery cannot be fully automated safely.
- The 15-minute stale threshold and failed-initial-charge key-retention policy
  require explicit approval.
- Refund maker-checker threshold remains a production policy decision; it does
  not block the P1 source-payment lock.

### Rollback

- This WI rollback is deletion/reversion of only the three owned documents.
- Future implementation rollback is application-first; retain expanded ENUMs,
  command columns, constraints, and all ledger/audit rows.
- Never contract ENUMs or delete ledger evidence as an emergency rollback.
- MySQL patch rehearsals must use a disposable/copy database restorable from
  backup because MySQL DDL implicitly commits.

## Approval Points

- `PAYDB-AP-01`: command state/column/constraint model.
- `PAYDB-AP-02`: 15-minute stale-processing and no-blind-replay policy.
- `PAYDB-AP-03`: retained encrypted billing key until tracked cleanup succeeds.
- `PAYDB-AP-04`: retained DB baseline and duplicate-row disposition.
- `PAYDB-AP-05`: disposable MySQL provisioning without Testcontainers.
- `PAYDB-AP-06`: production refund maker-checker threshold.

## WI-Chain Triggers

Completion of WI-002 must immediately trigger handoff/delegation for:

- `WI-20260714-ATS-004` through `WI-20260714-ATS-008`
- `WI-20260714-ATS-015`
- `WI-20260714-ATS-018`
- `WI-20260714-ATS-021`
- `WI-20260714-ATS-023`
- `WI-20260714-ATS-025`

Implementation must not begin until the relevant approval points above are
resolved. WI-021 specifically owns disposable/copy MySQL evidence and must not
infer retained-DB compatibility from this design alone.
