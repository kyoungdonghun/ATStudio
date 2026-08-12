# WI-20260809-ATS-033 Independent Reliability Re-Review

- **Reviewer role:** Independent Reliability Engineer (RE)
- **Review date:** 2026-08-12
- **Branch:** `codex/v1-release-rehearsal-fixes`
- **Reviewed HEAD:** `e343c2085fbc82c66b44fb8e5edde35bf920980f` plus the preserved shared-worktree changes
- **Implementation decision:** **APPROVE**

## Findings

### RE-L01 - LOW - DocOps closeout remains stale but does not block implementation approval

No BLOCKER, MAJOR, or implementation-blocking reliability finding remains. The
three currently modified design documents still describe WI-033 prepare
idempotency as future or out of scope, so they do not yet document the verified
implementation:

- `docs/design/api-spec.md:209-243,279-282`
- `docs/design/payment-integration-design.md:66-107,156-164,243-248`
- `docs/ui/screen-flow.md:64-98`

The follow-up DocOps change must update exactly those documents to record:

1. Required header-only `Idempotency-Key` transport and lowercase canonical
   UUIDv4 validation, including the stable invalid-key response.
2. Session-scoped key persistence across StrictMode remount, reload, network
   retry, and same-attempt retry; explicit replacement only for local corrupt
   state, invalid key, expiry, or safe terminal history.
3. Owner-scoped opaque `BILLING_PREPARE:v1:<sha256>` storage, exact tuple
   binding, same-owner mismatch `409`, cross-owner isolation, immutable
   non-null `command_key`, and legacy-null prepare isolation.
4. Separate agreement/order claim transactions, bounded named-unique loser
   retry, canonical aggregate lock order, and the pure prepare descriptor
   outside local transactions.
5. Distinction between WI-033 prepare replay and WI-034 callback response-loss,
   unknown-outcome, and financial recovery.

This is a documentation closeout finding only. The user explicitly stated that
DocOps will close it later, so it is not used to downgrade the verified product
implementation to BLOCK.

## Prior Block Resolution

### RE-B01 - RESOLVED - Prepare `command_key` survives confirmation

`claimBillingConfirm` now reuses every non-null order command key and derives
`BILLING_CONFIRM:<orderID>` only for a selected legacy null-key order
(`src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java:214-220`).
The real prepare-to-confirm integration proves byte-for-byte digest preservation
and a separate provider-attempt fence
(`src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareToConfirmIntegrationTest.java:113-153,155-200`).

**Status:** resolved.

### RE-B02 - RESOLVED - Deterministic disposable MySQL proof exists

The race test now synchronizes both empty non-locking probes, holds the winning
flush, observes the duplicate insert path, captures the named
`uq_billing_agreements_user_provider` loser, and proves a fresh-transaction
reread of the committed winner ID
(`src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareMysqlConcurrencyIntegrationTest.java:211-267,569-740`).

Main-run evidence supplied for this re-review:

- MySQL `8.0.45`, InnoDB, `REPEATABLE_READ`.
- Fresh schema/seed manifest: `41` tables, `493` columns, `168` indexes,
  `89` foreign keys, `6` plans; manifest **PASS**.
- `BillingAgreementPrepareMysqlConcurrencyIntegrationTest`: `3` tests,
  `0` skipped, `0` failures, `0` errors.
- `WI033_MYSQL_PROOF=PASS`; guarded drop **PASS**.
- Residual temporary directories: `0`; WI-033 `mysqld` processes: `0`.

The retained test result inspected during review also reported MySQL `8.0.45`,
`REPEATABLE_READ`, InnoDB, the three passing race cases, and the exact named
duplicate-key event. Per instruction, the deleted disposable runner was not
recreated or rerun.

**Status:** resolved.

### RE-M01 - RESOLVED - Owner-scoped contract is authoritative and isolated

The remediation addendum supersedes the earlier global cross-owner `409`
wording. The digest namespaces the raw UUID by authenticated owner
(`src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java:17-31`).
Two owners receive distinct claims and orders, while a second owner cannot claim
the first owner's order during confirm
(`src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareIdempotencyIntegrationTest.java:413-433`;
`src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareToConfirmIntegrationTest.java:202-243`).

**Status:** resolved.

### RE-M02 - RESOLVED - Frontend lowercase, corrupt-state, and replacement lifecycle aligns

The frontend accepts only lowercase canonical UUIDv4, preserves invalid stored
bytes without generation or API transport, validates generated UUIDs before
write, and rotates only through the explicit operation
(`frontend/src/utils/checkoutPrepareAttempt.ts:27-53,56-91,98-131`;
`frontend/src/utils/checkoutPrepareAttempt.test.ts:43-141`). The page exposes
replacement only for local corruption, invalid key, expiry, or terminal history;
tuple conflict, invalid state, arbitrary `409`, Provider errors, and network
errors retain the key (`frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:227-289,496-505`;
`frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx:148-287`).

**Status:** resolved.

### RE-M03 - RESOLVED - Fresh-key, legacy-null, and lifecycle regressions are covered

Focused H2 tests prove fresh-key creation after expiry and `FAILED`/`CANCELLED`
history while preserving old rows and keys; in-flight/unknown states remain
non-replaceable; legacy null history remains null and is ignored by prepare
(`src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareIdempotencyIntegrationTest.java:246-411`).
The prepare-to-confirm suite covers both prepare digest preservation and the
legacy null confirm fallback.

**Status:** resolved.

### RE-L01 (prior review) - RESOLVED FOR IMPLEMENTATION / OPEN FOR DOCOPS

The stale-doc condition remains the LOW finding at the top of this review. It is
separated from implementation acceptance as directed.

## Additional Reliability Review

- **Provider transaction boundary:** `prepareBillingAgreement` is
  `NOT_SUPPORTED`; agreement ensure, order claim, and descriptor finalization are
  separate `REQUIRES_NEW` transactions. `prepareAgreement` runs between claim
  and finalize with no active local transaction
  (`src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java:95-136`;
  `src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java:70-98,100-193`).
- **First-agreement unique race:** the missing-row path uses a non-locking probe
  followed by `saveAndFlush`; only the named agreement unique violation is
  retried, after the failed `REQUIRES_NEW` call returns, through another fresh
  transaction (`BillingAgreementApplicationService.java:139-157`;
  `BillingAgreementPrepareTransactionService.java:70-98`).
- **Same-owner key and tuple:** exact `READY`/`IN_PROGRESS` replay returns the
  same order; tuple mismatch is checked before lifecycle classification and
  returns `PAYMENT_PREPARE_ATTEMPT_CONFLICT` with no Provider call or mutation
  (`BillingAgreementPrepareTransactionService.java:118-152,196-236`).
- **Product/security safety:** raw keys and owner IDs are absent from persisted
  command keys; invalid headers fail before service/DB/Provider work; future
  Providers default to fail closed; no schema, pricing, amount, entitlement, or
  charge policy was broadened.
- **WI-034 boundary:** `PROCESSING`, `PROVIDER_SUCCEEDED`, and
  `PENDING_PROVIDER_CONFIRMATION` remain non-replaceable. No callback
  response-loss, unknown-outcome recovery, or reconciliation behavior was added.
- **External effects:** review tests used H2 and deterministic Provider doubles.
  No real Toss/SDK, charge, refund, cancellation, mail, deployment, retained DB,
  or secret/configuration action was used.

## Verification Evidence

### Focused backend

```powershell
.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.entity.PaymentOrderStateMachineTest" --tests "com.atstudio.atstudio.service.PaymentCommandKeyFactoryTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.BillingAgreementPrepareIdempotencyIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementPrepareToConfirmIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementCancellationTransactionIntegrationTest" --tests "com.atstudio.atstudio.service.BillingAgreementFailurePersistenceIntegrationTest" --tests "com.atstudio.atstudio.service.PaymentCommandIndependentVerificationIntegrationTest" --console=plain
```

- Exit code: `0`; `10` suites; `113` tests; `0` skipped/failures/errors.
- Gradle result: `BUILD SUCCESSFUL` in `55s`.

### Focused frontend

```powershell
npm test -- src/utils/checkoutPrepareAttempt.test.ts src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx
npm run typecheck
```

- Vitest: exit `0`; `6` files; `107` tests passed; duration `5.77s`.
- TypeScript: exit `0`; `tsc --noEmit` passed.

### Not executed in this time-bounded re-review

Full backend/frontend suites, coverage gates, ESLint, Prettier, production
builds, documentation validation, and a new `git diff --check` were not rerun.
They remain closeout gates and are not represented as completed here.

## Final Decision

**APPROVE**

All six prior reliability BLOCK findings are resolved by source, focused
regressions, and the supplied deterministic disposable MySQL proof. No new
implementation blocker, product-safety regression, security breach, or WI-034
scope intrusion was found. DocOps must still close RE-L01 using the exact
three-document list above before WI-033 documentation/evidence closeout.

## Safety Boundary

Only this review file was replaced. Product code, tests, other documents, Git
state, secrets, ignored configuration, retained data, and the prohibited ZIP
were not modified or opened during this review.
