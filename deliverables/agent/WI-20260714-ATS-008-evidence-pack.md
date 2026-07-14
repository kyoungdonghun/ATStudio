# Evidence Pack: WI-20260714-ATS-008

## Summary

- Added source-payment pessimistic locking and locked refund-reservation validation without changing provider execution, schema, `PaymentOrder`, or maker-checker policy.

## Scope / DoD Check

- [x] `createRefund` locks the source payment before reading reserved totals and inserting the request.
- [x] Locked validation rechecks status, provider, provider payment key, requested amount, and source amount boundary.
- [x] Reserved statuses are `REQUESTED`, `APPROVED`, `PROCESSING`, `SUCCEEDED`, and `PENDING_PROVIDER_CONFIRMATION`.
- [x] Exact-boundary reservations remain allowed; over-reservations are rejected.
- [x] Preview remains advisory and unlocked.
- [x] Existing random request idempotency key creation and no-provider-call-on-create behavior remain compatible.
- [ ] MySQL InnoDB concurrency semantics require the approved disposable MySQL proof in `WI-20260714-ATS-018`.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and traceability |
| 0 | `docs/standards/development-standards.md` | Java, transaction, and test standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence document conventions |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 1 | `docs/policies/quality-gates.md` | Verification and rollback gates |
| REQ | `deliverables/user/REQ-20260714-ATS-001.md` | Approved scope and constraints |
| Audit | `docs/audit/p1-remediation-trace-matrix-20260714.md` | `ATS020-P1-10` ownership and proof target |
| Design | `docs/design/p1-payment-db-integrity-design.md` | Locked refund-reservation contract |

## Evidence Pointers

- `src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java:32` - graph-complete `PESSIMISTIC_WRITE` source lookup.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:90` - lock-first create flow and local request insertion.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:252` - locked validation and aggregate reservation boundary.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:87` - lock, aggregate, insert ordering; reserved statuses; no live Provider call.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:163` - exact boundary and over-reservation behavior.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:186` - locked status, provider, and provider-key revalidation.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:218` - locked source-amount validation.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:241` - advisory preview remains unlocked.
- `src/test/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepositoryLockContractTest.java:18` - repository lock and EntityGraph contract.

## Commands and Results

- `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.repository.SubscriptionPaymentRepositoryLockContractTest"` - PASS, 11 tests, 0 failures, 0 skipped.
- `.\gradlew.bat compileJava` - PASS.
- `git diff --check -- 'src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java' 'src/main/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepository.java' 'src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java'` - PASS.
- `git diff --no-index --check -- NUL 'src/test/java/com/atstudio/atstudio/repository/SubscriptionPaymentRepositoryLockContractTest.java'` - no whitespace errors; exit 1 is the expected new-file difference status.
- `git diff --no-index --check -- NUL 'deliverables/user/WI-20260714-ATS-008-summary.md'` - no whitespace errors; exit 1 is the expected new-file difference status.
- `git diff --no-index --check -- NUL 'deliverables/agent/WI-20260714-ATS-008-evidence-pack.md'` - no whitespace errors; exit 1 is the expected new-file difference status.

## Risks / Rollback

- Residual risk: Mockito and annotation-contract tests prove application ordering and lock declaration only. They do not prove InnoDB wait, serialization, or deadlock behavior.
- Rollback: revert only the owned service, repository, focused tests, summary, and Evidence Pack. This WI performs no data or schema mutation.

## WI-Chain Trigger

- Trigger `WI-20260714-ATS-018` for disposable MySQL refund concurrency proof.
- Provide this Evidence Pack to downstream `WI-20260714-ATS-023` and `WI-20260714-ATS-025` reviews.
