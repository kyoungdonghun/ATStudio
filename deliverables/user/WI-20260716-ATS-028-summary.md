---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: se
category: implementation
status: complete
related_wi: WI-20260716-ATS-028
---

# WI-20260716-ATS-028 Summary

## Decision

**IMPLEMENTED; FOCUSED VERIFICATION PASSED.** F-025-01 through F-025-05 are remediated in the owned backend, test, and payment/security documentation surfaces. No schema change was introduced. The retained-MySQL concurrency run and live Provider verification remain environment-conditional because this WI explicitly prohibited DB and Provider mutation.

The WI-029 integration hold for F-027-04 is also closed on the WI-028-owned surface: the payment operations runbook now distinguishes local amount/currency from Provider amount/currency in diagnostics and evidence collection. No WI-029-owned API specification or frontend file was touched.

The full-suite follow-up ApplicationContext failure is closed without a production workaround. `RecurringRenewalCommandIntegrationTest` now imports a test-only `PaymentProperties` bean with deterministic `Asia/Seoul` scheduler configuration, allowing the revised production constructor to remain unchanged.

## Remediation Results

| Finding | Result | Main protection |
|---|---|---|
| F-025-01 | Closed in source and focused tests | Withdrawal uses agreement-before-subscription locking and rejects Provider-outcome-pending charge orders. Renewal rechecks cancellation/deletion immediately before the Provider call, while recording Provider success, and before entitlement finalization. |
| F-025-02 | Closed in source and focused tests | ADMIN GET reconciliation uses a dedicated observation-only path with non-claiming reads and no Incident, payment, agreement, or subscription mutation. Scheduled reconciliation remains the explicit recovery path. Runbook diagnostics now pair each local/Provider amount with its currency, closing the WI-028-owned part of F-027-04. |
| F-025-03 | Closed in source and focused tests | Entitlement-correction create/execute lock agreement before subscription, reject non-terminal charge results, and compare both agreement and subscription before-state snapshots. |
| F-025-04 | Closed in source and focused tests | Payment jobs derive dates and times from an injectable `Clock` created from `app.payment.scheduler-zone`; deterministic Seoul and Los Angeles midnight cases pass. |
| F-025-05 | Closed in source and focused tests | Support-reference generation is centralized in the payment service package. Incidents persist deterministic `REF-*` values, new free text omits Provider IDs, and retained labelled legacy text is sanitized without exposing raw fragments. |

## State and Lock Reasoning

- Withdrawal validates credentials before billing mutation, then locks billing agreement, subscription, and user in canonical order. A `SUBSCRIBE`, `UPGRADE`, or `RENEWAL` order in `PROCESSING`, `PROVIDER_SUCCEEDED`, or `PENDING_PROVIDER_CONFIRMATION` rejects withdrawal with `PAYMENT_ORDER_INVALID_STATE`.
- Renewal authorization is repeated in a short `REQUIRES_NEW` transaction immediately before key decryption and Provider invocation. A cancellation/deletion that wins before this boundary prevents the charge.
- If Provider success arrives after cancellation, the order retains `PROVIDER_SUCCEEDED` evidence for reconciliation, but the specialized cancellation fence prevents entitlement finalization and cannot reactivate the subscription.
- Entitlement correction locks the agreement before the subscription on both create and execute, compares stored agreement/subscription before state, and rejects the same Provider-outcome-pending order states.

## Verification

The final focused Gradle command compiled main and test sources and ran 17 selected test classes (26 JUnit suites including nested suites):

- **156 tests**
- **0 failures**
- **0 errors**
- **0 skipped**
- **BUILD SUCCESSFUL** in 1m 1s

Coverage included withdrawal, renewal cancellation/finalization, read-only ADMIN reconciliation, reconciliation state handling, entitlement correction, scheduler business dates, Provider identifier persistence/serialization, related payment controllers, billing agreement flows, subscription flows, operation audit logging, and withdrawal cleanup coordination.

`git diff --check` passed for the owned tracked files. Git emitted only the existing LF-to-CRLF working-copy warnings.

### Full-suite Follow-up

The full suite exposed one repeated root cause: all six `RecurringRenewalCommandIntegrationTest` methods failed while creating the sliced `@DataJpaTest` ApplicationContext because `RecurringRenewalService` required `PaymentProperties` and the slice did not supply it.

- Class-only rerun: **6 tests**, 0 failures/errors/skips, `BUILD SUCCESSFUL in 34s`.
- Related focused rerun: **48 tests** across renewal integration, renewal service, cancellation fences, scheduler, and reconciliation clock/state tests; 0 failures/errors/skips, `BUILD SUCCESSFUL in 35s`.
- Resolution: nested test-only `PaymentConfiguration` creates `PaymentProperties` with `schedulerZone=Asia/Seoul`; no production bean, constructor, or fallback was added.

## Scope Preservation

- Branch remained `codex/p1-acceptance-hardening`.
- No frontend, WI-029-owned file, client worktree/runtime, schema, migration, live/retained DB, or Provider state was changed.
- No Git index, commit, branch, remote, or push operation was performed.
- The focused controller tests used their configured in-process test application contexts/data sources; no retained or external database was used.
- Concurrent and unrelated worktree edits were preserved.
- The follow-up changed only the owned integration test and these two WI-028 deliverables; the cumulative WI-028 candidate list is now 38 files.

## Remaining Environment Evidence

- Run the withdrawal/correction-versus-renewal interleavings on an approved retained MySQL 8 test copy to establish database lock/deadlock behavior.
- Run approved Toss sandbox charge/lookup/reconciliation scenarios to establish live Provider timing and response behavior.
- Rerun the complete backend acceptance suite after this test-slice correction; this follow-up reran the formerly failing class and its related focused tests, not the entire suite.

The complete changed-file list, exact command, test-class list, rollback, and traceability mapping are in `deliverables/agent/WI-20260716-ATS-028-evidence-pack.md`.
