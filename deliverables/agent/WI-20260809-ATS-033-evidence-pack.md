---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-033
dependencies:
  - path: WI-20260809-ATS-033-handoff.md
    reason: Approved WI contract and DoD
  - path: WI-20260809-ATS-033-remediation-addendum.md
    reason: Authoritative remediation contract
  - path: WI-20260809-ATS-033-re-review.md
    reason: Independent final implementation approval
---

# Evidence Pack: WI-20260809-ATS-033

## Summary

- Closed `CR-031-083` / `ATS-027-F03` with owner-scoped, header-only billing
  prepare idempotency, deterministic concurrent claiming, and explicit SPA
  attempt-key lifecycle while preserving the separate WI-034 recovery boundary.

## Scope / DoD Check

- [x] Required lowercase canonical UUIDv4 `Idempotency-Key` is header-only and
      invalid input has a stable pre-effect error.
- [x] One session-scoped key survives StrictMode remount, reload, network retry,
      and same-attempt retry; replacement is explicit and narrowly classified.
- [x] Only `BILLING_PREPARE:v1:<sha256>` is persisted; raw keys are not stored
      or logged, exact replay reuses one order, same-owner tuple mismatch is 409,
      and other owners are isolated.
- [x] Non-null prepare `command_key` remains immutable through confirm; only a
      selected legacy null-key order receives `BILLING_CONFIRM:<orderID>`.
- [x] First-agreement and order unique losers use named constraints, bounded
      fresh-transaction retry/reread, canonical aggregate locks, and a pure
      Provider descriptor outside local transactions.
- [x] Supplemental H2, disposable MySQL 8/InnoDB concurrency, focused/full
      backend and frontend, coverage, static, build, and regression gates pass.
- [x] Independent RE decision is `APPROVE`; all six prior BLOCK findings are
      resolved. The remaining LOW DocOps finding is closed by this five-file set.
- [x] No schema, real external effect, retained database, deployment, secret, or
      output ZIP action occurred.

## Reference Documents (Tier 0-2)

| Tier | Document | Reason |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Constitution, payment integrity, and traceability |
| 0 | `docs/standards/documentation-standards.md` | Metadata, language, links, and structure |
| 0 | `docs/standards/development-standards.md` | Test, evidence, and rollback standards |
| 0 | `docs/standards/glossary.md` | Canonical project terms |
| 0 | `docs/standards/evidence-pack-standard.md` | Evidence Pack requirements |
| 1 | `docs/policies/security-policy.md` | No-secret and payment isolation boundary |
| 1 | `docs/policies/access-control-policy.md` | Authenticated owner scope |
| 1 | `docs/policies/quality-gates.md` | Completion gates |
| 1 | `docs/design/db-schema.md` | Existing unique constraints and nullable command key |
| 2 | `docs/design/payment-integration-design.md` | Payment command and Provider boundary |
| 2 | `docs/design/api-spec.md` | Required prepare API contract |
| 2 | `docs/ui/screen-flow.md` | SPA attempt lifecycle |

Injection source is the approved handoff's `[INPUT POINTERS]`; assignee is
DocOps for documentation closeout after SE implementation and independent RE.

## Reviewer Decisions

| Reviewer | Decision | Incorporated result |
| --- | --- | --- |
| PG | `APPROVE WITH CONDITIONS` | Header validation, owner-scoped digest, immutable key, isolation, lock order, pure Provider, and no-real-effects conditions are implemented and tested. |
| QA-INTEG | `APPROVE WITH CONDITIONS` | UI, API/server, Provider, and durable-state evidence lanes are separated; MySQL is authoritative for InnoDB concurrency. |
| SA remediation | Approved contract | Cross-owner namespace, lifecycle error matrix, confirm fence, and first-agreement claim algorithm supersede conflicting earlier clauses. |
| RE final | `APPROVE` | All six prior BLOCK findings resolved; only this DocOps closeout remained LOW. |

## Evidence Pointers

### Contract And Product

- `frontend/src/utils/checkoutPrepareAttempt.ts`: canonical key validation,
  context-scoped session storage, explicit replacement set, and no automatic
  repair/rotation.
- `frontend/src/api/payments.ts`: required `Idempotency-Key` header transport;
  request body remains free of the key.
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`: stored-attempt
  reuse, same-attempt retry, and explicit new-attempt control.
- `src/main/java/com/atstudio/atstudio/controller/PaymentController.java`:
  invalid-key rejection before application-service invocation.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java`:
  lowercase UUIDv4 grammar and owner-scoped `BILLING_PREPARE:v1:` digest.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java`:
  bounded named-unique retries and non-transactional Provider boundary.
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementPrepareTransactionService.java`:
  non-locking agreement probe, claim/replay validation, lifecycle matrix, and
  canonical aggregate locking.
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java`:
  non-null command-key preservation and legacy null confirm fallback.
- `src/main/java/com/atstudio/atstudio/service/payment/provider/recurring/RecurringPaymentProvider.java`:
  pure deterministic prepare capability with fail-closed default.

### Test And Closeout

- `src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareIdempotencyIntegrationTest.java`:
  sequential replay, conflicts, expiry/terminal/in-flight state, legacy null,
  owner isolation, H2 concurrency, and Provider transaction boundary.
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareToConfirmIntegrationTest.java`:
  immutable prepare digest, separate Provider fence, and legacy fallback.
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementPrepareMysqlConcurrencyIntegrationTest.java`:
  three deterministic InnoDB races, named unique loser, and post-commit reread.
- `frontend/src/utils/checkoutPrepareAttempt.test.ts` and
  `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`: canonical
  storage, StrictMode/reload/network reuse, and exact replacement classification.
- `docs/design/api-spec.md`, `docs/design/payment-integration-design.md`, and
  `docs/ui/screen-flow.md`: current WI-033 contracts and WI-034 boundary.

The implementation followed TDD and all final green gates are recorded below.
A standalone transcript of the initial red phase was not retained in the
closeout inputs, so this pack does not invent one.

## Cross-Layer Results

| Lane | Verified result | Limit |
| --- | --- | --- |
| UI/control | One key survives remount/reload/retry; only local corrupt, invalid-key, expired, and terminal signals expose explicit replacement. | Automated React evidence, not browser acceptance. |
| API/server | Header-only canonical UUIDv4, owner digest, exact replay, stable tuple conflict, independent owner namespace, and immutable confirm command fence. | Source and automated tests, not deployed traffic. |
| Provider | Prepare descriptor is deterministic, side-effect-free, and outside local transactions; non-attesting Providers fail closed. | Test doubles and V1 contract tests; no real Toss/SDK call. |
| Durable state | One agreement/order under races, named unique loser, winner-commit visibility, fresh reread, immutable history, and no schema change. | H2 is supplemental; disposable MySQL is the InnoDB proof. |

## Commands And Outputs

The product results below are implementation/main evidence accepted by RE;
DocOps did not rerun product suites during this closeout.

| Verification lane | Recorded result |
| --- | --- |
| Focused backend | 10 suites, 113 tests PASS; 0 skipped/failures/errors. |
| Focused frontend | 6 files, 107 tests PASS; typecheck PASS. |
| Related thumbnail regression | 5 loops x 9 tests = 45 PASS after the minimal `useLayoutEffect` cached-load/effect race correction. |
| Full backend | 174 suites, 1,445 tests; 16 conditional skips; 0 failures/errors. JaCoCo line 86.517%, method 83.895%, branch 71.353%; build PASS. |
| Full frontend | 72 files, 660 tests PASS. Statements 87.60%, branches 78.33%, functions 87.15%, lines 89.69%; typecheck, ESLint, Prettier, and build PASS. |
| Full-gate context remediation | Two missing integration-context `@MockitoBean` wirings were corrected; all 6/6 affected checks passed again. |
| Disposable MySQL | MySQL 8.0.45, InnoDB, `REPEATABLE_READ`; fresh manifest 41 tables, 493 columns, 168 indexes, 89 FKs, 6 plans PASS. Three concurrency tests; 0 skipped/failures/errors. `WI033_MYSQL_PROOF=PASS`; guarded drop PASS; residual temp directories/processes 0. |
| Diff hygiene | `git diff --check` PASS; line-ending warnings only, whitespace errors 0. |

DocOps closeout commands:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check -- docs/design/api-spec.md docs/design/payment-integration-design.md docs/ui/screen-flow.md deliverables/agent/WI-20260809-ATS-033-evidence-pack.md deliverables/user/WI-20260809-ATS-033-summary.md
rg -n -i "WI-033.*(future|out.of.scope)|duplicate.prepare.*(future|out.of.scope)|does not claim duplicate-prepare|remains assigned to.*WI-20260809-ATS-033|does not add duplicate-prepare" docs/design/api-spec.md docs/design/payment-integration-design.md docs/ui/screen-flow.md
```

Final DocOps outcomes are recorded after executing these commands: documentation
validation PASS; scoped diff check PASS with line-ending warnings only and zero
whitespace errors; stale-expression search 0 matches after semantic review.

## Risks / Rollback

- Residual risk: automated and disposable-environment evidence does not prove
  live Toss, SDK, production runtime, or production database behavior.
- Residual risk: callback response loss, unknown financial outcome, post-confirm
  reload recovery, and reconciliation remain intentionally unresolved until
  WI-034.
- Rollback only WI-033 product/test hunks and these five closeout files while
  preserving unrelated shared-worktree changes and audit records.
- No migration or retained data changed, so rollback requires no schema reversal,
  deletion, Provider cancellation, charge reversal, or refund.

## Follow-Ups

- Immediately trigger `WI-20260809-ATS-034`; it exclusively owns callback
  response-loss, unknown-outcome, reload recovery, and reconciliation behavior.

## Related Documents

- [WI-033 User Summary](../user/WI-20260809-ATS-033-summary.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [API Specification](../../docs/design/api-spec.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
