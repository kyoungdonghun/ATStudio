---
version: 1.0
last_updated: 2026-07-15
project: ATS
owner: docops
category: audit
status: stable
dependencies:
  - path: ../design/p1-payment-integrity-remediation-design.md
    reason: Implemented payment-integrity contract and Package A-G ownership
  - path: p1-remediation-trace-matrix-20260714.md
    reason: Historical ATS020-P1 baseline and current payment-row appendix
  - path: ../../deliverables/agent/WI-20260715-ATS-012-evidence-pack.md
    reason: Final independent review of the WI-009 and WI-010 follow-up findings
---

# P1 Payment Integrity Closure - 2026-07-15

> Purpose: Record the current code-and-test closure of payment-integrity findings F-01 through F-05 and the WI-009/WI-010 follow-up findings without changing historical FAIL evidence or closing production readiness.

## 1. Decision

- **Payment-integrity code and test gate: PASS.** F-01 through F-05 are closed at repository snapshot `14053e6` by Packages A-G, the WI-008 convergence correction, the WI-011 follow-up correction, and WI-012 independent review.
- **Historical reviews remain valid.** WI-009 and WI-010 correctly reported FAIL at snapshot `830c8dd`; their records are not rewritten. WI-011 implemented the four confirmed follow-ups in commit `46edd88`, and WI-012 independently returned PASS in commit `14053e6`.
- **Production readiness remains OPEN.** This report does not verify live Toss, a retained or production database, deployment configuration, production rollout, client acceptance, or non-payment P1 gates.

## 2. Finding Closure

| Finding | Current closure | WI and commit evidence | Reproducible proof |
|---|---|---|---|
| `F-01` renewal identity | One unresolved renewal period keeps one order and command; deterministic retries use a new attempt key on the same command. The retry scheduling gate is consumed when the retry is claimed. | Package B: `WI-20260715-ATS-002`, `77c2ebd`; follow-up: `WI-20260715-ATS-011`, `46edd88`; independent decision: `WI-20260715-ATS-012`, `14053e6` | `RecurringRenewalCommandIntegrationTest`; MySQL races 1 and 2 in `PaymentMysqlConcurrencyIntegrationTest` |
| `F-02` provider transaction boundaries | Cancellation, withdrawal cleanup, charged upgrade, reconciliation, and refund provider mutation use committed claim/result phases and fail when entered from an active transaction. | Packages C/D/F: `WI-20260715-ATS-004` (`49e8774`), `WI-20260715-ATS-005` (`45daf18`), `WI-20260715-ATS-006` (`d0bc21b`); refund correction: `WI-20260715-ATS-011` (`46edd88`) | `BillingAgreementCancellationTransactionIntegrationTest`, `WithdrawalBillingCleanupTransactionIntegrationTest`, `SubscriptionUpgradeCommandIntegrationTest`, `PaymentReconciliationRecoveryIntegrationTest`, `PaymentRefundResilienceIntegrationTest` |
| `F-03` refund crash recovery | One refund row and idempotency key are protected by a 15-minute lease, stale-claim fencing, exact same-key replay limits, and Incident-backed lookup-only fallback. | Package E: `WI-20260715-ATS-003`, `f5bbd7b`; strict boundary correction and review: `WI-20260715-ATS-011`/`WI-20260715-ATS-012`, `46edd88`/`14053e6` | Six crash/reclaim cases in `PaymentRefundResilienceIntegrationTest`; MySQL races 5 and 6 |
| `F-04` finalize-only reconciliation | Exact provider/order/status/amount/currency/transaction evidence may persist `PROVIDER_SUCCEEDED` and dispatch the existing purpose finalizer; mismatches remain Incident-only. Completed renewal convergence and cancelled-SUBSCRIBE state are fail-closed. | Package F: `WI-20260715-ATS-006`, `d0bc21b`; convergence correction: `WI-20260715-ATS-008`, `1ecfe5c`; state/redaction correction: `WI-20260715-ATS-011`, `46edd88`; review: `WI-20260715-ATS-012`, `14053e6` | `PaymentReconciliationRecoveryIntegrationTest`, `PaymentCommandIndependentVerificationIntegrationTest`; MySQL races 4 and 7 |
| `F-05` lock order and engine proof | Multi-row payment phases use the canonical lock order and the seven required races converge with exact business losers; deadlock, timeout, connection failure, and arbitrary exception are not accepted. | Packages B-F plus Package G: `WI-20260715-ATS-002` through `WI-20260715-ATS-007`; final proof commit `830c8dd` | `deliverables/agent/WI-20260715-ATS-007/run-summary.log`: schema PASS, Hibernate validate PASS, races PASS, cleanup count `0`; generated suite: 7 tests, 0 failures/errors/skips |

## 3. Package and Review Chain

| Package or review | WI | Commit | Current role |
|---|---|---|---|
| A - Entity/schema/manual-patch foundation | `WI-20260715-ATS-001` | `103fdf4` | Additive fields, ENUMs, patch ordering, and static contract |
| B - Payment command core | `WI-20260715-ATS-002` | `77c2ebd` | Stable command identity, exact retry gate, canonical command locks |
| C - Cancellation and withdrawal cleanup | `WI-20260715-ATS-004` | `49e8774` | Durable local cancellation and fenced cleanup phases |
| D - Charged upgrade | `WI-20260715-ATS-005` | `45daf18` | Strict provider boundary and finalize-only retry |
| E - Refund recovery | `WI-20260715-ATS-003` | `f5bbd7b` | Lease, same-key recovery, and stale-result fencing |
| F - Reconciliation | `WI-20260715-ATS-006` | `d0bc21b` | Exact provider evidence and purpose finalization |
| Completion convergence correction | `WI-20260715-ATS-008` | `1ecfe5c` | Renewal DONE idempotency and reconciliation convergence |
| G - Disposable MySQL proof | `WI-20260715-ATS-007` | `830c8dd` | Fresh disposable MySQL 8/InnoDB schema, validation, seven races, cleanup |
| Independent reviews at the pre-correction snapshot | `WI-20260715-ATS-009`, `WI-20260715-ATS-010` | `3f18fed` | Historical FAIL and four actionable findings |
| Follow-up correction | `WI-20260715-ATS-011` | `46edd88` | Refund `NEVER`, SUBSCRIBE gates, retry-gate consumption, payment-key minimization |
| Independent follow-up review | `WI-20260715-ATS-012` | `14053e6` | PASS; no P0/P1 remains in the four reviewed findings |

## 4. Verification Boundary

Authoritative final MySQL proof:

- Runner: `deliverables/agent/WI-20260715-ATS-007/run-package-g-mysql-proof.ps1`
- Summary: `deliverables/agent/WI-20260715-ATS-007/run-summary.log`
- Race log: `deliverables/agent/WI-20260715-ATS-007/mysql-races.log`
- Hibernate validation: `deliverables/agent/WI-20260715-ATS-007/hibernate-validate.log`
- Cleanup: `deliverables/agent/WI-20260715-ATS-007/database-drop.log` and `database-absent.log`

The earlier 5/7 diagnostics in the same WI directory remain historical evidence. They do not replace or contradict the later final 7/7 PASS run.

## 5. Remaining Open Gates

- Retained-database inventory, row-specific disposition, and copied-database migration rehearsal remain unverified. The disposable fresh-schema proof is not retained-DB proof.
- Live Toss configuration and live money movement were not exercised.
- Production deployment, scheduler ownership, monitoring, secret provisioning, and rollback rehearsal remain open under [SR-93](../SR/SR-93.md).
- Client acceptance and public acceptance-environment checks remain open.
- Non-payment P1 findings and the overall REQ quality/build/documentation gates remain outside this closure.
- WI-012 records one non-blocking P3 test gap: rendered unknown-cancel log output has no dedicated test-appender assertion.
- Refund same-key recovery remains bounded by the verified provider idempotency-retention contract; otherwise recovery stays lookup-only and Incident-backed.

## 6. Rollback Guidance

- Pause payment mutation, refund execution, renewal, cleanup, and reconciliation entry points before application rollback.
- Roll back application behavior by the owning WI/commit; preserve payment orders, payments, refunds, command keys, provider ownership evidence, audit logs, and Incidents.
- Keep additive columns, indexes, and ENUM members. Do not contract payment schema during an incident.
- A failed copied-database rehearsal must be discarded or restored from its approved copy. This report authorizes no retained or production database change.

## Related Documents

- [Current Remediation Design](../design/p1-payment-integrity-remediation-design.md): Implemented Package A-G contract.
- [Historical DB Integrity Design](../design/p1-payment-db-integrity-design.md): Superseded baseline and migration cautions.
- [P1 Trace Matrix](p1-remediation-trace-matrix-20260714.md): Historical baseline plus current payment-row appendix.
- [WI-012 Evidence Pack](../../deliverables/agent/WI-20260715-ATS-012-evidence-pack.md): Independent follow-up PASS.
