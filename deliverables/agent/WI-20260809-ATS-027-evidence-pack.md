# Evidence Pack: WI-20260809-ATS-027

## Summary

- Closed WI-027 as a documentation-only integration-audit evidence deliverable.
- Handoff baseline: `codex/v1-release-rehearsal-fixes@e343c20`; this closeout did not re-run git verification.
- All seven assigned matrix rows are `FAIL` because each contains at least one confirmed contract defect. Passing sublanes are preserved separately.
- Findings: `11` total: `P0 candidate=1`, `P1=3`, `P2=6`, `P3=1`.
- Test results below were supplied from main execution and were not rerun by closeout. Main performed the read-only anonymous guard recheck and restoration recorded below, and the final documentation quality checks (Prettier write/check, documentation validation, and `git diff --check`) were actually run. No product code, authenticated runtime, DB, configuration, secrets, Provider, git-state mutation, or preserved-ZIP operation was performed.

## Scope / DoD Check

| Scope item         | Outcome                                | Evidence basis                                                                                                                                                                                                                                                                               |
| ------------------ | -------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `G-PAY`            | `FAIL`                                 | Anonymous guards pass, but malformed/defaulted financial parameters and the frontend/server purpose contract fail. F01, F07, F08; matrix `:123`.                                                                                                                                             |
| `PUB-07`           | `FAIL`                                 | Cross-audience first-match routing, incomplete error/empty/retry behavior, missing latest-response fencing, and semantics/copy defects. F02, F05, F10; matrix `:139`.                                                                                                                        |
| `MEM-10`           | `FAIL`                                 | Authoritative-purpose mismatch, StrictMode duplicate prepare orders, malformed/defaulted cycle handling, and terminal/copy defects. F01, F03, F07, F08, F10; matrix `:167`.                                                                                                                  |
| `MEM-10S`          | `FAIL`                                 | Backend callback/replay safety passes, but UI purpose is not authoritative and canonical state is not established before success presentation. F01, F04, F10; matrix `:168`.                                                                                                                 |
| `MEM-10F`          | `FAIL`                                 | Raw/blank failure copy, misleading terminal prepare state, and incomplete retry recovery. F08; matrix `:169`.                                                                                                                                                                                |
| `MEM-11`           | `FAIL`                                 | Unknown-outcome UI, Billing Agreement/preview error conflation, one-click reactivation, and semantics/copy defects. F04, F06, F09, F10; matrix `:170`.                                                                                                                                       |
| `INV-SUB`          | `FAIL`                                 | Plan identity, purpose/amount, prepare idempotency, canonical reload, Billing Agreement projection, and mutation-state mismatches cross frontend/backend boundaries. F01-F04, F06, F09; matrix `:261`.                                                                                       |
| Scheduler boundary | `PASS` with `PARTIAL/BLOCKED` ordering | Source/test evidence passes due selection, state fencing, retry/grace/expiry, pending application, and idempotency. The three independent crons establish nominal start order but no demonstrated completion fence. Live Provider and durable state remain `BLOCKED`; matrix `:562,566-569`. |

The seven row results are `FAIL` because each has a confirmed defect; they do not assert that every behavior in the row failed.

## Evidence Index

| ID  | Severity     | Rows                                    | Confirmed cause                                                                                                                             |
| --- | ------------ | --------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| F01 | P0 candidate | `G-PAY`, `MEM-10`, `MEM-10S`, `INV-SUB` | UI-only `purpose=BILLING_AGREEMENT` can show zero payment while a non-subscriber server request creates a full-price `SUBSCRIBE` order.     |
| F02 | P1           | `PUB-07`, `MEM-10`, `INV-SUB`           | Name-only first match can send an INDIVIDUAL plan ID for a BUSINESS user; backend rejects before side effects.                              |
| F03 | P1           | `MEM-10`, `INV-SUB`                     | StrictMode duplicate prepare requests reuse the READY agreement but create distinct payment orders.                                         |
| F04 | P1           | `MEM-10S`, `MEM-11`, `INV-SUB`          | Backend callback/upgrade retries are fenced, but the UI cannot distinguish committed success, failure, reload failure, and unknown outcome. |
| F05 | P2           | `PUB-07`                                | Plans lack deliberate retry/empty handling and latest-response fencing; subscription-read errors are swallowed.                             |
| F06 | P2           | `MEM-11`, `INV-SUB`                     | Manage maps every Billing Agreement read error to absence and silently removes failed previews.                                             |
| F07 | P2           | `G-PAY`, `MEM-10`                       | Missing cycle defaults to MONTHLY and malformed cycle can reach prepare instead of failing before invocation.                               |
| F08 | P2           | `MEM-10`, `MEM-10F`                     | Raw/blank fail copy, terminal `PREPARING` state, no retry, and initial-charge CTA wording are inconsistent.                                 |
| F09 | P2           | `MEM-11`, `INV-SUB`                     | Reactivation restores automatic renewal with one click and no confirmation.                                                                 |
| F10 | P3           | `PUB-07`, `MEM-10`, `MEM-11`            | Audience/cycle/status semantics, live announcements, localization, and copy are inconsistent.                                               |
| F11 | P2           | Documentation / operator baseline       | Schema has `41` tables while one document says `39`; official-branch declarations are ambiguous against the audit baseline.                 |

Detailed four-lane evidence, impact, and bounded follow-up: `deliverables/agent/WI-20260809-ATS-027-findings.md`.

## Four-Lane Row Matrix

| Row       | UI copy/state/control                                                                                           | Frontend request invocation                                                                                          | Server / provider-test                                                                                                                     | Durable state / reload                                                                          |
| --------- | --------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| `G-PAY`   | Anonymous guards `PASS`; malformed/defaulted intent presentation `FAIL`.                                        | Missing cycle can invoke MONTHLY prepare; query purpose is not sent. `FAIL`.                                         | Server derives purpose from subscription state. Live Provider `BLOCKED`.                                                                   | Prepared-order rows not inspected live. `BLOCKED`.                                              |
| `PUB-07`  | Public 1280x720 page rendered without horizontal overflow; error/empty/retry/current-marker semantics `FAIL`.   | Unfiltered plan fetch plus first name match can select wrong audience; no latest-response fence. `FAIL`.             | BUSINESS mismatch rejects before provider/order side effects by source. Live authenticated response `BLOCKED`.                             | Current subscription truth was not inspected live. `BLOCKED`.                                   |
| `MEM-10`  | Amount/purpose and terminal-state copy `FAIL`.                                                                  | StrictMode can issue duplicate prepare POSTs; financial parameters are defaulted or omitted. `FAIL`.                 | Agreement reuse plus new order per prepare is source-confirmed; live Provider `BLOCKED`.                                                   | Two live rows were not queried. `BLOCKED`.                                                      |
| `MEM-10S` | Success navigation does not first prove canonical Subscription/Billing Agreement; purpose can disagree. `FAIL`. | Callback confirmation invocation exists; malformed amount is locally rejected.                                       | Callback validation, concurrent/replay fence, and finalize-only recovery `PASS` in source/targeted backend tests. Live Provider `BLOCKED`. | Test-managed persistence converges to one payment/subscription; production/live rows `BLOCKED`. |
| `MEM-10F` | Raw or blank provider message and misleading terminal state `FAIL`.                                             | Fail callback does not confirm canonical server state and has no complete retry path. `FAIL`.                        | Live provider cancel/error provenance `BLOCKED`.                                                                                           | Order/agreement/entitlement state after abandon/fail `BLOCKED`.                                 |
| `MEM-11`  | Unknown-outcome, hidden read/preview failure, one-click reactivation, and semantics `FAIL`.                     | Mutation then reload cannot represent `mutation succeeded; reload failed`. `FAIL`.                                   | Upgrade callback/idempotency `PASS`; cancel/reactivate source re-calls are no-charge no-ops. Live Provider `BLOCKED`.                      | Test-managed upgrade persistence passes; production/live rows and reload agreement `BLOCKED`.   |
| `INV-SUB` | Multiple projections can disagree. `FAIL`.                                                                      | Plan, purpose, prepare, confirm, manage, and reload contracts do not share one authoritative identity/state. `FAIL`. | Selected backend safety fences pass, but cross-layer contract remains inconsistent.                                                        | Production ledger/order/subscription/agreement consistency `BLOCKED`.                           |

## Source / API / Backend Crosswalk

| Flow                            | Frontend pointer                                                                                                      | Backend pointer                                                                                                      | Test pointer / result boundary                                                                                             |
| ------------------------------- | --------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Plans and audience              | `frontend/src/pages/public/SubscriptionPlanPage.tsx:127-209,291-378`; `frontend/src/api/subscriptions.ts:20-25`       | `SubscriptionService.java:23-38`; `SubscriptionRepository.java:14-17`                                                | Frontend targeted suite supplied `PASS`; cross-audience duplicate-name case remains a confirmed gap.                       |
| Prepare                         | `SubscriptionPaymentPage.tsx:20-30,110-160`; `frontend/src/api/payments.ts:30-33,75-82`                               | `BillingAgreementApplicationService.java:108-157,355-375,437-440,478-484`; `BillingAgreementPrepareRequest.java:6-9` | Existing source/test evidence confirms agreement reuse plus distinct orders; live rows/Provider `BLOCKED`.                 |
| Confirm/replay                  | `SubscriptionPaymentPage.tsx:43-108`; `SubscriptionPaymentReplay.test.tsx:23-51`                                      | `PaymentCommandTransactionService.java:168-228,518-600`; `BillingAgreementApplicationService.java:160-317`           | Callback concurrency, provider-success/local-failure, and finalize-only recovery are covered by the supplied backend runs. |
| Manage change/cancel/reactivate | `SubscriptionManagePage.tsx:219-302,355-445,481-506,695-858`; `frontend/src/api/userSubscriptions.ts:154-175,287-296` | `UserSubscriptionService.java:105-217,241-258,324-398`; `PaymentCommandTransactionService.java:69-163,603-664`       | Upgrade ambiguity/concurrency/retry paths pass targeted tests; cancel/reactivate re-call behavior is source-confirmed.     |
| Durable payment projection      | Frontend reload APIs above                                                                                            | `PaymentOrder`, `BillingAgreement`, `UserSubscription`, `SubscriptionPayment` and repositories listed by the handoff | Test-managed persistence evidence only; production/live DB `BLOCKED`.                                                      |

## Scheduler Boundary

| Scheduled method                | Nominal schedule               | Delegation / action                                                                 | Outcome                                                                                                                                    |
| ------------------------------- | ------------------------------ | ----------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| `processRecurringRenewals()`    | 00:00, configured payment zone | `RecurringRenewalService.processDueRenewals()`                                      | `PASS` for delegation and due processing. `SubscriptionScheduler.java:58-61`; `SubscriptionSchedulerTest.java:50-78`.                      |
| `processExpiredPaymentOrders()` | 00:10                          | Direct repository selection of stale READY/IN_PROGRESS orders, then `markExpired()` | `PASS` from source/test. `SubscriptionScheduler.java:63-77`; `SubscriptionSchedulerTest.java:81-103`.                                      |
| `processExpiredSubscriptions()` | 00:30                          | Direct expired-subscription selection, then `expire()`                              | `PASS`; pending change is cleared without free application. `SubscriptionScheduler.java:84-100`; `SubscriptionSchedulerTest.java:105-159`. |

- Due selection: `RecurringRenewalService.java:81-123` and `BillingAgreementRepository.java:23-42` select bounded ACTIVE candidates, eligible FAILED retries, and provider-success finalize-only work. `PASS` from source/test.
- Retry/grace/expiry: `PaymentCommandTransactionService.java:231-338,469-515,1088-1104` applies a three-day grace and bounded attempts, schedules deterministic retries, suspends after final failure, and expires after grace. `PASS` from source/test.
- Pending plan/cycle: `PaymentCommandTransactionService.java:667-719,763-807,1069-1079` prices the pending target and applies it only during successful renewal finalization; `UserSubscription.java:94-124` clears pending on success or expiry. Source behavior `PASS`; a direct successful-pending-application test was not separately identified in the bounded findings.
- Idempotency: `PaymentOrder.java:44-58` unique constraints plus command/status locks prevent duplicate period/order/provider-attempt ownership. Existing targeted tests cover deterministic retry, ambiguous outcome, concurrent workers, and finalize-only recovery. `PASS`.
- Completion ordering: cron offsets provide nominal `00:00 -> 00:10 -> 00:30` start order, but the independent methods have no demonstrated completion fence. Runtime completion ordering is `PARTIAL/BLOCKED`.
- Live Provider calls and production/live durable agreement/order/payment/subscription rows are `BLOCKED`.

## Anonymous Guard Evidence

Main execution observed exact percent-encoded preservation of each original path in `returnTo`:

| Route                                                            | Observed safe redirect                                                                           | Result |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ | ------ |
| `/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY&from=audit` | `/login?returnTo=%2Fsubscriptions%2Fcheckout%3Fplan%3DSTANDARD%26cycle%3DMONTHLY%26from%3Daudit` | `PASS` |
| `/subscriptions/checkout/success?from=audit`                     | `/login?returnTo=%2Fsubscriptions%2Fcheckout%2Fsuccess%3Ffrom%3Daudit`                           | `PASS` |
| `/subscriptions/checkout/fail?from=audit`                        | `/login?returnTo=%2Fsubscriptions%2Fcheckout%2Ffail%3Ffrom%3Daudit`                              | `PASS` |
| `/subscriptions/manage?from=audit`                               | `/login?returnTo=%2Fsubscriptions%2Fmanage%3Ffrom%3Daudit`                                       | `PASS` |

Only anonymous navigation was observed. Exact USER, BUSINESS, and ADMIN runtime variants are not inferred from these results; authenticated runtime is `BLOCKED`.

## Public Browser and API Evidence

- Route: public `/subscriptions`.
- Viewport: `1280x720`.
- Evidence: GET, DOM, and screenshot observation supplied by main execution.
- Horizontal overflow: none observed; document width `1265`, viewport width `1280`.
- Screenshot: `output/ui-ux-audit/20260809/WI-027/PUB-07-business-yearly-1280x720-observed.png`.
- Screenshot classification: audit output, not a product artifact.
- Live plan API ordering: unfiltered IDs `1-3` were INDIVIDUAL followed by IDs `4-6` BUSINESS; BUSINESS-filtered response returned IDs `4-6`.
- Responsive live checks at `1024x768`, `390x844`, and `360x800`: `NOT RUN / BLOCKED` because current browser control could not resize. Static CSS source was inspected; it is not substituted for live responsive evidence.
- Browser restoration: `OBSERVED`. Main restored the browser to Home at `http://127.0.0.1:5173/` after the read-only guard recheck.

## Test and Quality Evidence

Test-suite, typecheck, and ESLint results in this section were supplied from main execution and were not rerun by closeout. The final documentation quality checks below were actually run.

| Check                                         | Result    | Exact supplied evidence                                                                                                                     |
| --------------------------------------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Targeted frontend Vitest                      | `PASS`    | `9` files / `65` tests; all passed; duration `3.65s`.                                                                                       |
| `npm run typecheck`                           | `PASS`    | Main supplied a passing result.                                                                                                             |
| Targeted ESLint                               | `PASS`    | Main supplied a passing targeted result; exact invocation was not present in the bounded closeout inputs.                                   |
| Backend run A                                 | `PASS`    | `--rerun-tasks`; `15` XML suites / `107` tests; failures `0`, errors `0`, skipped `0`; wall `54.9s`.                                        |
| Backend run B                                 | `PASS`    | `7` suites / `39` tests; failures `0`, errors `0`, skipped `0`; wall `42.3s`. Exact command was not present in the bounded closeout inputs. |
| Combined backend distinct targeted executions | `PASS`    | `146` tests.                                                                                                                                |
| Test-suite closeout rerun                     | `NOT RUN` | No frontend/backend test suite, typecheck, ESLint, or build was rerun by closeout.                                                          |

### Final Documentation Validation

- Prettier write on handoff/findings/evidence/summary: exit `0`; handoff unchanged `60ms`, findings `59ms`, evidence `48ms`, summary `11ms`.
- Prettier check on all four WI-027 documents: exit `0`; `All matched files use Prettier code style.`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit `0`; Tier 0, links, `539` traceability IDs, document index, and all validation passed.
- `git diff --check`: exit `0`; no output.

## Commands and Outputs

- Targeted frontend command recorded by the handoff:
  - `cd frontend; npm run test -- src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/api/domainApis.test.ts src/utils/tossPayments.test.ts src/router/index.test.tsx src/router/ProtectedRoute.test.tsx src/router/SubscriberRoute.test.tsx`
  - Main-supplied outcome: `9` files / `65` tests passed in `3.65s`.
- Typecheck command: `npm run typecheck`.
  - Main-supplied outcome: `PASS`.
- Targeted backend selection command recorded by the handoff:
  - `gradlew.bat test --tests "com.atstudio.atstudio.controller.SubscriptionControllerTest" --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "com.atstudio.atstudio.controller.UserSubscriptionControllerTest" --tests "com.atstudio.atstudio.entity.BillingAgreementStateMachineTest" --tests "com.atstudio.atstudio.entity.PaymentOrderStateMachineTest" --tests "com.atstudio.atstudio.entity.UserSubscriptionStateMachineTest" --tests "com.atstudio.atstudio.service.SubscriptionSchedulerTest" --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "com.atstudio.atstudio.service.UserSubscriptionServiceTest" --tests "com.atstudio.atstudio.service.payment.provider.recurring.TossBillingProviderTest"`
  - Run A additionally used the exact flag `--rerun-tasks`.
  - Main-supplied outcome: `15` XML suites / `107` tests, all zero failure/error/skipped, wall `54.9s`.
- Backend run B exact command: not present in the bounded closeout inputs.
  - Main-supplied outcome: `7` suites / `39` tests, all zero failure/error/skipped, wall `42.3s`.
- Targeted ESLint exact command: not present in the bounded closeout inputs; result supplied as `PASS`.
- Prettier write command:
  - `npx prettier --write ../deliverables/agent/WI-20260809-ATS-027-handoff.md ../deliverables/agent/WI-20260809-ATS-027-findings.md ../deliverables/agent/WI-20260809-ATS-027-evidence-pack.md ../deliverables/user/WI-20260809-ATS-027-summary.md`
  - Exit `0`; handoff `60ms (unchanged)`, findings `59ms`, evidence pack `48ms`, summary `11ms`.
- Prettier check command:
  - `npx prettier --check ../deliverables/agent/WI-20260809-ATS-027-handoff.md ../deliverables/agent/WI-20260809-ATS-027-findings.md ../deliverables/agent/WI-20260809-ATS-027-evidence-pack.md ../deliverables/user/WI-20260809-ATS-027-summary.md`
  - Exit `0`; `All matched files use Prettier code style.`
- Documentation validator command: `python .agents/skills/validate-docs/scripts/validate_docs.py`.
  - Exit `0`; Tier 0, links, `539` traceability IDs, document index, and all validation passed.
- Diff check command: `git diff --check`.
  - Exit `0`; no output.

No exit code, warning, command string, or validation result absent from the supplied evidence is invented here.

## Documentation Drift Evidence

- `src/main/resources/schema.sql`: `41` actual `CREATE TABLE` statements, as counted by main execution.
- `docs/payment/feature-inventory.md:150-152`, `docs/design/index.md:29`, and `docs/registry/project-registry.md:41-43`: `41` tables.
- `docs/payment/known-limits-and-next-steps.md:44`: stale count `39`.
- `docs/payment/index.md`, `docs/payment/acceptance-test-checklist.md`, `docs/payment/feature-inventory.md`, `docs/payment/known-limits-and-next-steps.md`, and `docs/registry/project-registry.md` identify `codex/p1-acceptance-hardening` as official.
- Audit handoff baseline: `codex/v1-release-rehearsal-fixes@e343c20`.
- Whether the former branch label is historical release provenance or current operator instruction is not distinguished. F11 records this as P2 documentation drift.

## Reference Documents

The handoff supplied these references. This closeout read the handoff, findings, relevant WI-020 matrix rows, the attached evidence-pack skill, and WI-026 formatting precedent; it did not re-open product or Tier documents.

| Tier       | References                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Reason                                                                                     |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| 0          | `docs/standards/core-principles.md`; `docs/standards/development-standards.md`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    | Constitution and development standards for `qa-integ`.                                     |
| 1          | `docs/policies/security-policy.md`; `docs/policies/access-control-policy.md`; `docs/policies/quality-gates.md`; `docs/architecture/system-design.md`                                                                                                                                                                                                                                                                                                                                                                                                                                              | Security, access, quality, and architecture boundaries.                                    |
| 2          | `docs/standards/frontend-standards.md`; `.agents/skills/react-best-practices/AGENTS.md`; `docs/design/api-spec.md`; `docs/design/db-schema.md`; `docs/design/payment-integration-design.md`; `docs/design/p1-payment-db-integrity-design.md`; `docs/design/p1-payment-integrity-remediation-design.md`; `docs/design/usecase/user-subscription.md`; `docs/payment/system-overview.md`; `docs/payment/user-flows.md`; `docs/payment/acceptance-test-checklist.md`; `docs/payment/feature-inventory.md`; `docs/payment/known-limits-and-next-steps.md`; `docs/design/payment-operations-runbook.md` | Frontend, subscription, payment, persistence, and operator contracts named by the handoff. |
| WI context | `deliverables/agent/WI-20260809-ATS-027-handoff.md`; `deliverables/agent/WI-20260809-ATS-027-findings.md`; `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`                                                                                                                                                                                                                                                                                                                                                                                                                          | Scope, detailed findings, and executable row contracts.                                    |

**Injection rules applied:** assignee `qa-integ`; task type read-only subscription/payment integration audit; Tier 0-2 references retained from the handoff. No new context expansion was performed during closeout.

## Limitations and Frozen Boundary

- Authenticated runtime, Provider prepare/auth/confirm/charge/cancel operations, and production/live durable DB rows are `BLOCKED`.
- Responsive live checks at 1024/390/360 are `NOT RUN / BLOCKED`; static CSS is source evidence only.
- No product source, test source, runtime, DB, schema, configuration, fixture, secret, browser, Provider, or git mutation was performed by this closeout.
- No stage or commit was created.
- The audit screenshot is retained at the declared audit-output path.
- Intentional `output/client-demo-screenshots-20260716-140514.zip` was preserved and uninspected. It was not opened, read, hashed, metadata-probed, moved, replaced, or used as a fixture.

## Files Changed

- `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md` - agent-facing closure evidence.
- `deliverables/user/WI-20260809-ATS-027-summary.md` - user-facing closure summary.

## Risks / Rollback

- Highest risk: F01 can present a zero-payment operation while the server prepares a full-price subscription order.
- P1 risks: BUSINESS checkout blocking, duplicate prepare orders, and UI ambiguity after financial mutation response/reload loss.
- F11 can misdirect schema verification or operator branch selection.
- Live Provider and production durable behavior remain unverified and must not be inferred from source, mocks, tests, or UI copy.
- No product or runtime rollback is required. This WI is documentation-only evidence.

## Follow-ups

- Resolve F01 before charge-bearing acceptance; establish one authoritative server-bound purpose/amount contract.
- Carry immutable plan identity/audience and add prepare idempotency for F02/F03.
- Add explicit unknown-outcome/canonical-reload UI for F04 and retain the confirmed backend fences.
- Address F05-F10 through separately approved product WIs.
- Reconcile F11 table count and separate historical release branch metadata from current operator instructions.
- `WI-20260809-ATS-030` is blocked by WI-027 according to the handoff; this pack provides closure evidence but does not start that WI.
