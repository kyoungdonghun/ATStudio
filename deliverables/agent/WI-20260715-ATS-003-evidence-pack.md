# Evidence Pack: WI-20260715-ATS-003

## Summary (one-liner)

- Implemented Package E's 15-minute refund lease, atomic stale reclaim, exact same-key replay ceiling, lookup-only Incident fallback, immutable claim validation, and lease-fenced result persistence.

## Scope / DoD Check

- [x] Fresh `PROCESSING` rejects a competing execution.
- [x] A lease at least 15 minutes old is reclaimed atomically on the same refund row.
- [x] Claim evidence contains refund/provider/payment/order/amount/currency/reason/idempotency/lease fields.
- [x] Provider mutation is preceded by exact persisted snapshot and lease validation.
- [x] Success, failure, pending, exception, empty-result, and lookup-only writers reject an old lease.
- [x] Recovery inside the 24-hour ceiling reuses the same row, key, and provider command.
- [x] Elapsed-ceiling recovery makes no provider mutation and persists pending refund plus OPEN Incident evidence.
- [x] Provider invocation occurs with no active local transaction.
- [x] The stale candidate projection follows `(status, processing_started_at, id)` order and accepts `Pageable` bounds.
- [x] Both completion deliverables exist.

## Reference Documents (Tier 0-2)

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approval, platform integrity, financial traceability |
| 0 | `docs/standards/development-standards.md` | Java/JPA, transactions, testing, evidence pointers |
| 0 | `docs/standards/documentation-standards.md` | Completion-document structure |
| 0 | `docs/standards/glossary.md` | Canonical project terminology |
| 1 | `docs/policies/security-policy.md` | No secrets, retained DB, or live-provider access |
| 1 | `docs/policies/quality-gates.md` | Focused regression, rollback, and high-criticality evidence |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved remediation scope and forbidden environment access |
| Context | `docs/design/p1-payment-integrity-remediation-design.md:275` | Lease, replay, crash tests, ownership, rollback |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | F-03 closure and Package E dependency contract |
| Evidence | `deliverables/agent/WI-20260714-ATS-023-evidence-pack.md` | Original stranded-`PROCESSING` finding and test gap |
| Evidence | `deliverables/agent/WI-20260715-ATS-001-evidence-pack.md` | Package A entity, lease, schema, and index foundation |

Handoff: `deliverables/agent/WI-20260715-ATS-003-handoff.md` (`se`, depends on WI-001, blocks WI-007).

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/repository/PaymentRefundRepository.java:49-62` - bounded stale processing ID projection ordered by lease and ID.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:148-207` - non-transactional orchestration, lookup-only branch, pre-provider validation, same-command invocation, and lease-bearing result calls.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:34-105` - 15-minute/24-hour constants, locked first claim, stale reclaim, audit marker, and complete claim snapshot.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:108-193` - exact claim validation and fenced result/lookup-only entry points.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:196-289` - success/failure/pending lease fencing and complete snapshot equality.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:296-368` - stale boundary, replay mode, stale audit note, and OPEN Incident persistence.
- `src/main/java/com/atstudio/atstudio/service/PaymentRefundTransactionService.java:407-423` - immutable claim and explicit provider-mutation/lookup-only modes.

Focused tests:

| Design 6.4 case | Evidence |
|---|---|
| 1. Crash after claim, before provider | `PaymentRefundResilienceIntegrationTest.java:114-153` |
| 2. Provider success, result not persisted | `PaymentRefundResilienceIntegrationTest.java:155-186` |
| 3. Delayed old result after reclaim | `PaymentRefundResilienceIntegrationTest.java:188-242` |
| 4. Two stale reclaimers race | `PaymentRefundResilienceIntegrationTest.java:245-285` |
| 5. Same key with changed command | `PaymentRefundResilienceIntegrationTest.java:287-305` |
| 6. Ceiling elapsed, no exact lookup | `PaymentRefundResilienceIntegrationTest.java:308-338` |

Additional evidence:

- `PaymentRefundResilienceIntegrationTest.java:341-360` - fresh lease loser is exactly `INVALID_STATE_TRANSITION`.
- `PaymentRefundResilienceIntegrationTest.java:363-381` - stale projection ordering and one-row page bound.
- `PaymentRefundResilienceIntegrationTest.java:388-423` - pending retry reuses the same idempotency key.
- `PaymentRefundResilienceIntegrationTest.java:425-462` - reservation race accepts only the exact business loser; arbitrary exceptions fail the test.
- `PaymentRefundResilienceIntegrationTest.java:733-737` - fake provider throws if a local transaction is active.
- `AdminPaymentRefundServiceTest.java:259-367` - provider command, validation order, lookup-only no-call behavior, and unapproved rejection.

## Commands & Outputs

1. `.\gradlew.bat compileJava compileTestJava`
   - PASS: `BUILD SUCCESSFUL in 6s`.
   - Only existing deprecation notes from `UserSubscriptionService` and its test were emitted.
2. `.\gradlew.bat test --tests "com.atstudio.atstudio.service.AdminPaymentRefundServiceTest" --tests "com.atstudio.atstudio.service.PaymentRefundResilienceIntegrationTest"`
   - PASS: `BUILD SUCCESSFUL in 16s`.
   - XML: `AdminPaymentRefundServiceTest` 11 tests and `PaymentRefundResilienceIntegrationTest` 12 tests; 23 total, 0 failures/errors/skips.
3. `git diff --check`
   - PASS: no whitespace errors; only repository LF-to-CRLF working-copy notices.
4. Scope diff/status inspection
   - Package E changed only its three production files, two focused test files, and two WI003 completion documents.
   - Concurrent Package B changes remained present and were not reverted, edited, or claimed.

Transient verification note:

- Early compile attempts observed Package B's in-progress method/signature gaps in `RecurringRenewalService`, `BillingAgreementRepository`, and reconciliation code. Package E did not edit those files. The same full compile command passed after Package B completed the corresponding edits.

## Test Coverage Details

- Lease boundary uses second precision and treats exactly 15 minutes as stale.
- The stale race catches only `BusinessException` and asserts `INVALID_STATE_TRANSITION`; timeout, deadlock, connection failure, or arbitrary exception fails the future/test.
- Every old-lease result path is invoked after a replacement claim and rejected before state mutation.
- Same-key replay executes the identical `PaymentRefundProviderCommand` twice and converges to one `SUCCEEDED` refund and one provider transaction ID.
- Ceiling fallback asserts zero provider calls, one retained refund row/key, pending failure evidence, and an OPEN `PROVIDER_LOOKUP_FAILED` Incident.
- The fake provider checks `TransactionSynchronizationManager.isActualTransactionActive()` and throws if true.

## Risks / Rollback

Risks:

- The current refund provider contract has no exact refund lookup operation. Therefore elapsed-ceiling recovery is intentionally pending/Incident-backed rather than mutation-capable.
- The 24-hour local ceiling assumes it remains within the verified Toss idempotency-retention contract. If that assumption cannot be maintained, set recovery to lookup-only until the contract is re-verified.
- The existing `PROVIDER_LOOKUP_FAILED` Incident type is reused because Package E does not own reconciliation enum/schema expansion.
- H2 proves application transaction behavior but not MySQL/InnoDB lock scheduling; Package G retains that proof obligation.
- The lease follows the approved single-server model and is not a distributed ownership token.

Rollback:

- Pause refund execution first.
- Revert only the five Package E code/test files and the two WI003 completion documents listed above.
- Preserve Package A's additive lease column/index and all refund, audit, and Incident rows.
- Any refund left `PROCESSING` requires operator review; do not create a replacement refund row or idempotency key.
- No database patch or external mutation was performed by this WI, so there is no environment rollback.

## Environment Boundaries

- Used only Gradle compilation and ephemeral H2 focused tests.
- Did not connect to or mutate retained, local, copied, disposable MySQL, stage, or production databases.
- Did not call live or test Toss endpoints; the provider was an in-process fake.
- Did not start, stop, or modify preview/public servers or Cloudflare tunnels.
