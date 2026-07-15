# Evidence Pack: WI-20260715-ATS-009

## Summary

- Independent review result: **FAIL**. Two P1 defects and one P2 defect remain in
  commit range `103fdf4..830c8dd`; PASS is not issued.

## Scope and Boundaries

- Role: independent Code Reviewer (`cr`).
- Reviewed range: `103fdf4..830c8dd` (7 commits, 82 changed files).
- Governing inputs: the WI-009 handoff, payment-integrity remediation design,
  required standards/policies, and WI-001 through WI-008 Evidence Packs.
- Review remained limited to files changed by the range and supplied evidence.
- No production code, test, schema, runner, log, or existing WI artifact was
  edited. Only the WI-009 summary and this Evidence Pack were created.
- No build/test suite, database, provider, preview, or server operation was run.

## Severity-Ordered Findings

### P1-01 - Refund provider boundary permits transaction suspension

Evidence:

- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:149-186`
  - both refund entry points declare `Propagation.NOT_SUPPORTED`;
  - `executeRefundAt()` invokes `PaymentRefundProvider.cancelPayment()`.
- `docs/design/p1-payment-integrity-remediation-design.md:79-87`
  - every `PaymentRefundProvider` invocation must use `NEVER` or an equivalent
    runtime assertion;
  - a provider call must not be reached by suspending an outer transaction.
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:82`
  - the integration test also runs with `NOT_SUPPORTED`.
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java:734`
  - the fake rejects only an *active* transaction and cannot detect suspension.
- `deliverables/agent/WI-20260715-ATS-003-evidence-pack.md:16`, `:95`
  - the claimed proof is only that no transaction is active at invocation.

Impact:

- A future transactional caller is accepted and suspended rather than rejected.
  The refund provider mutation and `REQUIRES_NEW` claim/result phases can commit
  while the resumed caller transaction later rolls back. This violates the
  approved fail-closed transaction contract for a money-moving command.

Required correction:

- Use `Propagation.NEVER` at the refund orchestration/provider boundary or add an
  equivalent runtime assertion.
- Add a test that invokes refund execution inside an active transaction and
  requires `IllegalTransactionStateException` before any provider call.

### P1-02 - SUBSCRIBE reconciliation omits expected agreement/cleanup state

Evidence:

- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:229-238`
  - `SUBSCRIBE` mutation eligibility checks only null subscription plus retained
    ciphertext/fingerprint.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:1246-1273`
  - locked reconciliation revalidation does not require the agreement to remain
    in its expected initial state or cleanup to remain unclaimed.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:68-99`
  - cancellation can commit before cleanup and retain key material for a provider
    cleanup claim.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementCleanupTransactionService.java:182-205`
  - failed or ambiguous cleanup preserves a non-success state and records an
    Incident rather than clearing the key.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:378-405`
  - exact reconciliation evidence can persist provider success and dispatch a
    purpose finalization target.
- `docs/design/p1-payment-integrity-remediation-design.md:361-386`
  - SUBSCRIBE requires expected initial state; contradictory local evidence must
    remain Incident-only without financial mutation.

Impact:

- After an ambiguous initial charge, a user cancellation plus failed/unknown key
  cleanup can leave `CANCELLED` state and retained key material. Reconciliation
  can still authorize `PROVIDER_SUCCEEDED` and initial finalization. This can
  reverse cancellation intent or, at minimum, mutate the order before the
  contradictory state is rejected.

Required correction:

- Revalidate the locked agreement status, user/subscription absence, and cleanup
  state before provider-success persistence and again in finalization.
- Add an Incident-only regression for exact provider `DONE` after cancellation
  with retained key material; assert no order, subscription, agreement, payment,
  refund, or entitlement mutation.

### P2-01 - Audit note does not sanitize the provider transaction ID

Evidence:

- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:306-323`
  writes the complete provider transaction ID into an audit note.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:428-439`
  maps Toss `paymentKey` to that transaction ID.
- `docs/design/p1-payment-integrity-remediation-design.md:406-417` requires a
  sanitized transaction ID in reconciliation audit evidence.
- `docs/policies/security-policy.md:41-45` requires sensitive logging to be
  minimized.

Impact:

- The authoritative provider payment identifier is duplicated into free-text
  audit material, expanding exposure beyond the structured financial evidence.

Required correction:

- Mask the transaction ID in audit notes while retaining exact structured
  ownership evidence where required for idempotency and reconciliation.

## Finding Closure Matrix

| Design finding | Implementation/evidence assessment | Status |
|---|---|---|
| F-01 | Exact-period query, same order/command retry, canonical renewal claims, focused retry tests, and MySQL races 1-2 align. | No P0/P1 found |
| F-02 | Cancellation, withdrawal, charged upgrade, and scheduled reconciliation use split phases, but the refund provider boundary still permits suspension. | **OPEN (P1-01)** |
| F-03 | Same refund row/key, 15-minute lease, delayed-result fencing, replay ceiling, and Incident fallback align. Strict provider-boundary proof is missing. | Partial |
| F-04 | Provider/order/status/amount/currency/transaction checks and finalize-only dispatch exist, but initial-subscription expected state is incomplete. | **OPEN (P1-02)** |
| F-05 | Reviewed lock paths follow the class order; WI-007 reports disposable MySQL 8/InnoDB validation and all seven exact races passing after WI-008. | Supported, subject to residual scope |

## Transaction, Lock, and Idempotency Assessment

- Canonical lock order is preserved in the reviewed payment command paths:
  agreement, subscription, order, payment, then refund where applicable.
- Renewal retries retain one period/order/command and create only a new persisted
  attempt key.
- Completed renewal and reconciliation convergence from WI-008 validate existing
  payment/provider ownership before returning idempotently.
- Refund result writers compare the persisted second-precision lease timestamp,
  fencing delayed claimants and preserving the same refund row/idempotency key.
- Cleanup claims commit before provider deletion and stale cleanup is detect-only.
- The two P1 findings above are the remaining fail-closed gaps.

## MySQL Proof Assessment

- `deliverables/agent/WI-20260715-ATS-007-evidence-pack.md:43-68` records schema
  creation, Hibernate validation, InnoDB verification, and seven exact race
  outcomes.
- `deliverables/agent/WI-20260715-ATS-007-evidence-pack.md:94-100` records the
  earlier 5/7 failure, WI-008 correction, and final unchanged 7/7 PASS rerun.
- `src/test/java/com/atstudio/atstudio/service/MysqlRaceTestSupport.java:26-48`,
  `:113-147` uses bounded two-worker execution and rejects SQL, deadlock, timeout,
  connection, and arbitrary-exception losers.
- The proof was not rerun because WI-009 forbids database execution. No
  contradiction was found in the evidence reviewed before deliverable creation.
- The seven races do not cover refund invocation from an outer transaction or
  cancelled initial-subscription reconciliation.

## Commands and Outputs

- `git status --short --branch`
  - Branch: `codex/p1-acceptance-hardening`.
  - Existing untracked runtime logs and WI-009/WI-010 handoffs were preserved.
- `git log --oneline --reverse 103fdf4..830c8dd`
  - 7 commits: Package B, E, C, D, F, WI-008 correction, and final MySQL proof.
- `git diff --name-status 103fdf4..830c8dd`
  - 82 changed files in the reviewed range.
- No build, test, database, provider, or server command was executed.

## Verdict and Follow-up

- **Verdict: FAIL.** Two P1 defects remain, so the WI-009 PASS condition is not
  satisfied.
- Next WI recommendation: correct P1-01 and P1-02, include the two missing
  regression scenarios, correct P2-01, run focused payment tests, and rerun only
  MySQL races whose production paths change.
- Rollback implication: this review changed no application or environment state.
  Remove only the two WI-009 documents if the review artifacts must be withdrawn.
