# Evidence Pack - WI-20260715-ATS-011

## 1. Work Item

- **ID:** WI-20260715-ATS-011
- **Role:** Software Engineer (`se`)
- **Status:** Implemented and locally verified; independent review pending
- **Handoff:** `deliverables/agent/WI-20260715-ATS-011-handoff.md`
- **Approved requirement:** `deliverables/user/REQ-20260714-ATS-001.md`

## 2. Scope Executed

Implemented only the four confirmed gaps assigned by the handoff:

1. Refund provider calls reject proxy entry from an active transaction with `Propagation.NEVER`.
2. SUBSCRIBE reconciliation and finalization fail closed unless all initial-subscription eligibility predicates remain true.
3. Renewal retry dates are consumed on claim of an eligible FAILED retry, before the out-of-transaction provider call.
4. Exact payment keys are removed from serialized lookup evidence and masked in reconciliation Incident/audit evidence.

No schema, MySQL proof, WI-009/010 review artifact, preview artifact, runtime log, or unrelated path was edited. No database mutation outside H2 tests and no real provider call occurred.

## 3. Evidence Inputs Loaded

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`
- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`
- `docs/design/p1-payment-integrity-remediation-design.md`
- `deliverables/user/REQ-20260714-ATS-001.md`
- `deliverables/agent/WI-20260715-ATS-009-evidence-pack.md`
- `deliverables/agent/WI-20260715-ATS-010-evidence-pack.md`
- `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md`
- `deliverables/agent/WI-20260715-ATS-003-evidence-pack.md`
- `deliverables/agent/WI-20260715-ATS-006-evidence-pack.md`

## 4. Implementation Evidence

| Finding | Production evidence | Focused test evidence | Review mapping |
|---|---|---|---|
| Refund isolation | `AdminPaymentRefundService.java:149`, `AdminPaymentRefundService.java:157` | `PaymentRefundResilienceIntegrationTest.java:120`, `PaymentRefundResilienceIntegrationTest.java:133` | WI-009 P1-01; WI-010 P1-EXEC-01 |
| SUBSCRIBE state gate | `BillingAgreement.java:318`, `PaymentReconciliationTransactionService.java:231`, `PaymentCommandTransactionService.java:523`, `PaymentCommandTransactionService.java:1261` | `PaymentReconciliationRecoveryIntegrationTest.java:163`, `PaymentReconciliationRecoveryIntegrationTest.java:203`, `PaymentReconciliationRecoveryIntegrationTest.java:234` | WI-009 P1-02; WI-010 P1-EXEC-02 |
| Retry-date consumption | `BillingAgreement.java:232`, `PaymentCommandTransactionService.java:303`, `PaymentCommandTransactionService.java:323` | `RecurringRenewalCommandIntegrationTest.java:233` | WI-010 P2-EXEC-02 |
| Payment-key minimization | `PaymentReconciliationIncidentService.java:221`, `PaymentReconciliationIncidentService.java:326`, `TossBillingProvider.java:502` | `PaymentReconciliationIncidentServiceTest.java:101`, `TossBillingProviderTest.java:342`, `PaymentReconciliationRecoveryIntegrationTest.java:163` | WI-009 P2-01; WI-010 P2-SEC-03 |

### Behavioral assertions

- Both public refund execution methods reject an active caller transaction at the Spring proxy boundary and record zero provider calls.
- A cancelled agreement with cleanup claimed remains Incident-only and creates no subscription, payment, or refund mutation.
- A cancellation committed after lookup is observed by the locked reconciliation transaction before provider success is persisted.
- The initial-charge finalizer independently rejects ineligible SUBSCRIBE state after provider-success persistence.
- A FAILED renewal retry clears its due date when claimed; an ambiguous provider result leaves it null and prevents an unintended next-day call.
- Structured lookup result ownership retains the exact transaction identifier while serialized payload, Incident identifier, and audit note exclude the raw key.

## 5. Verification Evidence

### Compilation

```powershell
.\gradlew.bat compileJava compileTestJava
```

Result: **PASS** (`BUILD SUCCESSFUL`, 2026-07-15).

### Focused tests

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest" --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.RecurringRenewalCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationRecoveryIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationTransactionServiceTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationIncidentServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"
```

Result: **PASS** - 7 classes, 67 tests, 0 failures, 0 errors, 0 skipped.

### Impacted tests

```powershell
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.service.RecurringRenewalServiceTest" --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentProviderSuccessRecoveryIntegrationTest" --tests "com.atstudio.atstudio.repository.BillingAgreementRepositoryTest" --tests "com.atstudio.atstudio.service.SubscriptionUpgradeCommandIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentReconciliationServiceTest" --tests "com.atstudio.atstudio.service.PaymentOperationAuditLogServiceTest"
```

Result: **PASS** - 7 classes, 33 tests, 0 failures, 0 errors, 0 skipped (`BUILD SUCCESSFUL` in 37 seconds).

### Diff integrity

```powershell
git diff --check
```

Result: **PASS** - exit code 0 and no whitespace errors. Git emitted only LF-to-CRLF working-copy warnings.

## 6. Changed Paths

### Production

- `src/main/java/com/atstudio/atstudio/entity/BillingAgreement.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java`
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java`

### Tests

- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/RecurringRenewalCommandIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java`

### WI-011 outputs

- `deliverables/agent/WI-20260715-ATS-011-evidence-pack.md`
- `deliverables/user/WI-20260715-ATS-011-summary.md`

## 7. Definition of Done

- [x] Both refund entry points use `Propagation.NEVER`.
- [x] Active-transaction refund proxy tests prove rejection before provider invocation.
- [x] SUBSCRIBE reconciliation requires READY, cleanup NONE, not cancelled, no subscription, and retained key material.
- [x] State changes after provider lookup are revalidated under the transaction boundary.
- [x] The initial SUBSCRIBE finalizer independently fails closed.
- [x] Eligible FAILED renewal retries consume their retry date when claimed.
- [x] Deterministic failure may schedule the next retry; ambiguity leaves the date null.
- [x] Raw payment keys are absent from serialized lookup payloads and Incident/audit notes.
- [x] Focused and impacted tests pass.
- [x] `git diff --check` passes.
- [x] WI-011 summary and Evidence Pack are present.

## 8. Risk and Independent Review

- The change does not alter schema, repository SQL, lock ordering, or database concurrency primitives. A WI-007 MySQL proof rerun is therefore not technically required for this implementation pass and was not performed.
- Independent review must confirm the four finding mappings and decide closure; this Evidence Pack does not claim final closure.
- Existing unrelated untracked tunnel/Vite runtime logs and the provided WI-011 handoff were preserved untouched.

## 9. Rollback

Revert only the production/test paths and WI-011 outputs listed in Section 6. No schema or data rollback is required.
