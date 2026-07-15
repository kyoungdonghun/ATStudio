# Evidence Pack: WI-20260715-ATS-012

## Summary

- Independent review of commit `46edd882cc088b7f4f475f7fca32b01bc847c810`
  closes exactly four WI-009/WI-010 findings. Verdict: **PASS**, with no P0/P1
  remaining and one non-blocking P3 test gap.

## Scope / DoD Check

- [x] Reviewed only the WI-012 handoff, WI-009/010/011 Evidence Packs,
  remediation design, and production/tests changed by `46edd88`.
- [x] Verified active-transaction refund rejection before provider invocation.
- [x] Verified SUBSCRIBE state gates at claim, locked result, and finalizer.
- [x] Verified day-two retry-date consumption and ambiguous-result behavior.
- [x] Verified payment-key minimization in lookup, Incident/audit, and cancel log.
- [x] Reproduced the focused 7-class test command successfully.
- [x] Performed no code, schema, existing artifact, provider, server, preview,
  or retained-database mutation.
- [x] Created only the WI-012 summary and this Evidence Pack.

## Reference Documents

| Tier | Document | Review use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability and review boundaries |
| 0 | `docs/standards/development-standards.md` | Transaction and test standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/security-policy.md` | Sensitive identifier minimization |
| 1 | `docs/policies/quality-gates.md` | P0/P1 PASS gate |
| 2 | `docs/design/p1-payment-integrity-remediation-design.md` | Approved remediation contract |
| Handoff | `deliverables/agent/WI-20260715-ATS-012-handoff.md` | Scope and output contract |
| Evidence | `deliverables/agent/WI-20260715-ATS-009-evidence-pack.md` | CR findings |
| Evidence | `deliverables/agent/WI-20260715-ATS-010-evidence-pack.md` | Integration findings |
| Evidence | `deliverables/agent/WI-20260715-ATS-011-evidence-pack.md` | Implementation/test claims |

## Finding Closure Evidence

| Finding | Corrected code | Test evidence | Decision |
|---|---|---|---|
| Refund `NEVER` | `AdminPaymentRefundService.java:149,157` | `PaymentRefundResilienceIntegrationTest.java:120-145` rejects both proxy entry points and asserts zero calls | **CLOSED** |
| SUBSCRIBE fail-closed | `BillingAgreement.java:318-324`; `PaymentReconciliationTransactionService.java:229-231`; `PaymentCommandTransactionService.java:523,1261,1279-1284` | `PaymentReconciliationRecoveryIntegrationTest.java:163-258` covers cancellation before result, after lookup, and before finalizer | **CLOSED** |
| Retry-date consumption | `BillingAgreement.java:232-238`; `PaymentCommandTransactionService.java:303,323-325` | `RecurringRenewalCommandIntegrationTest.java:233-260` proves ambiguous attempt 2 leaves retry null and prevents attempt 3 | **CLOSED** |
| Payment-key minimization | `TossBillingProvider.java:197,502-514`; `PaymentReconciliationIncidentService.java:221,320-335` | `TossBillingProviderTest.java:342-346`; `PaymentReconciliationIncidentServiceTest.java:101-145`; integration assertions at `PaymentReconciliationRecoveryIntegrationTest.java:188-197` | **CLOSED** |

### Behavioral Assessment

- Refund annotations are enforced by Spring proxy tests, not only by checking
  transaction state inside a fake provider.
- SUBSCRIBE eligibility is centralized in
  `BillingAgreement.isInitialSubscriptionFinalizationEligible()` and combined
  with subscription absence at all three mutation boundaries.
- Retry consumption occurs in the same `REQUIRES_NEW` claim transaction as the
  `FAILED -> PROCESSING` transition. A crash or ambiguous outcome therefore
  belongs to reconciliation and cannot reopen the automatic retry gate.
- Exact lookup transaction identity remains available for ownership matching;
  duplicated operational evidence is removed or masked.

## Commands and Results

### Commit inspection

- `git show --no-ext-diff --format=fuller --name-status 46edd88`
  - Confirmed 6 production files and 5 test files in the remediation commit,
    plus WI-011 artifacts.
- Focused per-file `git diff 46edd88^ 46edd88 -- <approved paths>`
  - Confirmed the four corrections and corresponding assertions without
    expanding to unrelated code.

### Focused tests

```powershell
.\gradlew.bat test --rerun-tasks \
  --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" \
  --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" \
  --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" \
  --tests "com.atstudio.atstudio.service.PaymentReconciliationRecoveryIntegrationTest" \
  --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest" \
  --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" \
  --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"
```

- Result: **PASS**, `BUILD SUCCESSFUL in 33s`, 5 Gradle tasks executed.
- Runtime boundary: in-process H2 and fake providers only; no MySQL or real
  provider invocation.
- WI-011 records 7 classes, 67 focused tests, and 7 classes/33 impacted tests
  passing. The impacted suite was not rerun in WI-012 per its narrow review
  scope; WI-011's passing 33-test evidence was reviewed.

## Residual Risk and MySQL Decision

- **P3 test gap:** the unknown-cancel log no longer includes the raw key, but
  no dedicated test appender asserts the rendered log content. Static diff and
  source inspection confirm the direct interpolation was removed.
- No P0/P1 remains within the four reviewed findings.
- MySQL rerun is not needed because `46edd88` changes no schema, repository SQL,
  lock acquisition order, isolation behavior, or concurrency primitive. The
  existing WI-007 engine proof is unaffected.

## Documentation Update Pointers

- Update WI-002, WI-003, and WI-006 Evidence Packs only through a separately
  authorized documentation WI; do not rewrite WI-009/WI-010 historical results.
- Update the remediation design status and completed slice table when the final
  quality gate is formally closed.

## Verdict / Rollback

- **PASS: no P0/P1 finding remains.** The residual P3 test gap is non-blocking.
- Rollback affects only
  `deliverables/user/WI-20260715-ATS-012-summary.md` and this file. No code,
  schema, database, provider, server, preview, or commit rollback is required.
