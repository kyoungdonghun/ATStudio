# Evidence Pack: WI-20260715-ATS-006

## Summary (one-liner)

- Completed Package F with non-transactional scheduled reconciliation, strict provider-DONE evidence, Incident-backed finalize-only recovery for all three payment purposes, and explicit no-charge proof.

## Scope / DoD Check

- [x] Lookup evidence includes exact provider, order ID, amount, currency, status, authoritative transaction ID, and sanitized adapter evidence.
- [x] Scheduled orchestration and provider lookup own no local transaction; candidate, claim, Incident, provider-success, finalizer, and resolution phases use short independent units.
- [x] Only stale `PROCESSING` or `PENDING_PROVIDER_CONFIRMATION` can transition to `PROVIDER_SUCCEEDED`; `PROVIDER_SUCCEEDED` is finalize-only.
- [x] Exact provider `DONE` finalizes `SUBSCRIBE`, `UPGRADE`, and `RENEWAL` once through B-owned finalizers without calling charge.
- [x] Missing or conflicting provider/order/status/amount/currency/transaction evidence is Incident-only with no local financial mutation.
- [x] Finalizer failure preserves `PROVIDER_SUCCEEDED`; a later reconciliation resumes finalize-only and resolves matching Incidents.
- [x] Transaction-observing lookup, strict evidence, no-charge, Incident, adapter, focused integration, and impacted regression tests pass.
- [x] Java compilation and whitespace checks pass.
- [x] No live Toss call, provider mutation, retained database, DDL, preview server, or Package C/D/B-owned file mutation occurred.

## Reference Documents (Tier 0-2)

**Injected Context** (from WI Handoff Packet):

| Tier | Document | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Financial traceability, approval, and transparent execution baseline |
| 0 | `docs/standards/development-standards.md` | Java/Spring transactions, testing, and evidence standards |
| 0 | `docs/standards/documentation-standards.md` | Completion artifact and pointer conventions |
| 0 | `docs/standards/glossary.md` | Canonical WI and subscription terminology |
| 1 | `docs/policies/security-policy.md` | Secret, provider evidence, logging, and retained-environment boundaries |
| 1 | `docs/policies/quality-gates.md` | High-criticality validation and rollback expectations |
| Context | `deliverables/user/REQ-20260714-ATS-001.md` | Approved P1 remediation scope and forbidden live/retained operations |
| Context | `docs/design/p1-payment-integrity-remediation-design.md` | F-02/F-04 transaction, evidence, finalization, Incident, and test contract |
| Evidence | `deliverables/agent/WI-20260714-ATS-036-evidence-pack.md` | Package A-G ownership and design completion baseline |
| Evidence | `deliverables/agent/WI-20260715-ATS-002-evidence-pack.md` | B-owned reconciliation-safe provider-success and purpose finalizer APIs |

**Injection Rules Applied**:

- Handoff: `deliverables/agent/WI-20260715-ATS-006-handoff.md`
- Assignee: `se`
- Task type: payment-integrity implementation
- Ownership: Package F reconciliation/Incident/lookup adapter production files, focused tests, and WI006 completion artifacts only

## Evidence Gate Matrix

| Evidence | Required result | Mutation authority |
|---|---|---|
| Provider | Exact local provider | Mismatch opens/retains Incident only |
| Order | Exact local `orderId` | Mismatch opens/retains Incident only |
| Status | Exactly `DONE` | Missing/non-DONE remains Incident-only |
| Amount | Non-null and numerically equal | Mismatch uses critical `AMOUNT_MISMATCH` Incident |
| Currency | Exact local currency (`KRW` in scope) | Missing/mismatch remains Incident-only |
| Transaction | Nonblank authoritative provider payment key | Missing/conflicting ownership remains Incident-only |
| Local state | Stale `PROCESSING`, `PENDING_PROVIDER_CONFIRMATION`, or same-transaction `PROVIDER_SUCCEEDED` | First two may persist success; last is finalize-only |
| Local relationships | Purpose, command, agreement, subscription, period, target cycle, and billing-key preconditions revalidated | Contradiction remains Incident-only; B finalizer performs locked authoritative validation |

## Evidence Pointers

Production:

- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:39-42` - scheduler has no broad transaction and records local issues separately.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:56-117` - bounded provider candidate orchestration and result accounting.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:124-198` - exact evidence Incident-before-mutation, provider-success persistence, finalization retry behavior, and resolution handling.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationService.java:238-251` - explicit `SUBSCRIBE`/`UPGRADE`/`RENEWAL` finalizer dispatch only.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:47-84` - short read-only local ledger phase.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:87-136` - candidate IDs and canonical agreement/subscription/order claim revalidation.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:139-190` - strict provider/order/status/amount/currency/transaction evidence gate.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionService.java:193-216` - defensive exact-evidence check and B-owned provider-success entry point consumption.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:70-117` - short local/provider Incident open, finalization-failure, and matching resolution phases.
- `src/main/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentService.java:306-321` - sanitized evidence audit note with old/new local state and amount/currency/transaction fields.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/ProviderPaymentLookupResult.java:7-43` - richer immutable lookup evidence including currency.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/PaymentStatusLookupProvider.java:15-16` - provider lookup `Propagation.NEVER` contract.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:207-242` - no-transaction Toss lookup adapter.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProvider.java:428-439`, `502-516` - exact currency/payment-key parsing and lookup-only sanitized payload; charge payload behavior is unchanged.

Focused tests:

- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:119-147` - all three purposes finalize once; lookup transaction observations are false; charge count is zero; Incidents resolve.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:153-187` - amount mismatch is Incident-only, then exact evidence converges and resolves both Incidents.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:193-230` - local finalizer failure retains `PROVIDER_SUCCEEDED`; retry remains finalize-only with no charge.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:236-269` - stale `PROCESSING` recovers while fresh `PROCESSING` is not looked up or mutated.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationRecoveryIntegrationTest.java:455-524` - transaction-observing dual-interface fake and fail-fast `charge()` assertion.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationTransactionServiceTest.java:50-153` - exact pass plus provider/order/status/currency/missing/conflicting transaction rejection.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationServiceTest.java:72-139` - purpose dispatch and Incident-only mismatch orchestration.
- `src/test/java/com/atstudio/atstudio/service/PaymentReconciliationIncidentServiceTest.java:242-277` - matching Incident resolution and audit assertion.
- `src/test/java/com/atstudio/atstudio/service/payment/provider/recurring/TossBillingProviderTest.java:310-346` - Toss lookup currency and sanitized payload evidence.

## Commands & Outputs

- `gradlew.bat test --tests <10 focused and impacted classes>`
  - Result: PASS, 10 suites, 50 tests, 0 failures, 0 errors, 0 skipped, `BUILD SUCCESSFUL`.
  - Included Package F service/transaction/Incident/adapter tests plus provider-success, renewal, upgrade, admin Incident, and audit regressions.
- `gradlew.bat compileJava`
  - Result: PASS, `BUILD SUCCESSFUL`.
- `git diff --check`
  - Result: PASS with no whitespace diagnostics; only repository LF-to-CRLF working-copy notices were emitted.
- `git diff --no-index --check -- NUL <new WI006 file>` for each new production/test/completion file
  - Result: PASS with no whitespace diagnostics; exit code `1` is the expected no-index content-difference result and was normalized by the verification wrapper.

## Test Evidence

- Strict gate: provider, order, status, amount, currency, blank transaction, and conflicting persisted transaction cases covered.
- Transaction boundary: the lookup fake records `TransactionSynchronizationManager.isActualTransactionActive()` and all observations are `false`.
- No charge: the same fake implements `RecurringPaymentProvider`; `charge()` fails immediately if called, and the asserted call count is `0` in all recovery scenarios.
- Purpose recovery: `SUBSCRIBE`, `UPGRADE`, and `RENEWAL` each produce one `DONE` order and one payment effect.
- Idempotency: mismatch leaves local state unchanged; finalizer failure preserves `PROVIDER_SUCCEEDED`; retry creates no second payment or charge and resolves the Incident.
- Fresh ownership: fresh `PROCESSING` produces no lookup, Incident, payment, or local state change.

## Forbidden Operations Check

- Live Toss/provider mutation: not called.
- Retained/copied/local MySQL schema or data: not accessed or changed.
- DDL/schema files: not changed.
- Preview/public/Cloudflare server: not accessed or changed.
- B-owned payment command/entity/repository files: read and consumed only.
- Package C/D files and unrelated runtime logs/deliverables: not reverted or edited by Package F.

## Risks / Rollback

Risks:

- Toss recovery requires exact `currency` and authoritative `paymentKey`; older or incomplete provider responses remain detect-only.
- Provider lookup availability is required for automatic recovery. Missing configuration or lookup failures retain an Incident and perform no financial mutation.
- H2 validates orchestration and persistence behavior but is not InnoDB lock proof. Package G must still run disposable MySQL 8 races for F-05.
- The design assumes single-server scheduling. Multi-server ownership still requires a separate lease/coordination design.
- A transient failure after successful local finalization but before Incident resolution leaves the financial result final and reports an unresolved Incident-resolution issue for operator follow-up.

Rollback:

- Pause reconciliation scheduling before application rollback.
- Revert only the Package F production/test files and WI006 completion artifacts listed in this Evidence Pack.
- Preserve payment rows, command keys, provider transaction evidence, audit logs, and Incident rows; do not contract schema or delete financial evidence.
- No provider or database rollback is required for this work session because no live provider mutation, retained DB mutation, or DDL execution occurred.

## Follow-ups

1. WI-20260715-ATS-007 is unblocked by Package F completion.
2. Package G must provide disposable MySQL 8/InnoDB race proof for reconciliation versus normal finalization and concurrent finalizers.
3. Independent payment/integration review remains required before final P1 closure.
