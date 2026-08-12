# WI-20260809-ATS-027 Summary

## Outcome

WI-027 is closed as a documentation-only evidence deliverable against the handoff baseline `codex/v1-release-rehearsal-fixes@e343c20`. The baseline was not re-verified with git during this closeout.

All seven assigned rows are `FAIL` because each contains at least one confirmed defect. Passing guard, backend-idempotency, and scheduler sublanes remain recorded separately; a row-level `FAIL` does not mean every behavior failed.

| Row       | Result | Confirmed basis                                                                                                                          |
| --------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------- |
| `G-PAY`   | `FAIL` | Anonymous guards pass, but malformed/defaulted financial parameters and the frontend/server purpose contract fail.                       |
| `PUB-07`  | `FAIL` | Cross-audience first-match routing, missing deliberate error/empty/retry handling, no latest-response fence, and semantics/copy defects. |
| `MEM-10`  | `FAIL` | Authoritative-purpose mismatch, StrictMode duplicate prepare orders, malformed/defaulted cycle handling, and terminal/copy defects.      |
| `MEM-10S` | `FAIL` | Backend callback/replay safety passes, but UI purpose and canonical-state handling fail the row contract.                                |
| `MEM-10F` | `FAIL` | Failure copy can be raw/blank, terminal prepare still appears in progress, and retry recovery is incomplete.                             |
| `MEM-11`  | `FAIL` | Unknown-outcome UI, Billing Agreement/preview error conflation, one-click reactivation, and semantics/copy defects.                      |
| `INV-SUB` | `FAIL` | Plan identity, purpose/amount, prepare idempotency, reload, and state projections disagree across layers.                                |

Finding count: `11` total: `P0 candidate=1`, `P1=3`, `P2=6`, `P3=1`.

## Highest-Risk Findings

- **F01, P0 candidate:** query-only re-registration purpose can display a zero-payment message while a non-subscriber server request creates a full-price `SUBSCRIBE` order. Live Provider charge was not attempted or observed.
- **F02, P1:** BUSINESS checkout can select the first same-name INDIVIDUAL plan ID and then be rejected by backend audience validation before side effects.
- **F03, P1:** duplicate StrictMode prepare POSTs reuse the agreement but create separate payment orders.
- **F04, P1:** callback and upgrade backend retries have confirmed idempotency/finalize-only fences, and cancel/reactivate re-calls are source-confirmed no-charge no-ops. The UI still cannot distinguish committed success, ordinary failure, reload failure, and unknown outcome.
- **F11, P2:** schema contains `41` tables while `docs/payment/known-limits-and-next-steps.md:44` says `39`. Multiple documents call `codex/p1-acceptance-hardening` official while the audit baseline is `codex/v1-release-rehearsal-fixes@e343c20`, without distinguishing release history from current operator instruction.

## Four-Lane Boundary

| Lane                 | Confirmed evidence                                                                                                               | Boundary                                                                                             |
| -------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| UI/control           | Anonymous guards pass; public Plans rendered at 1280x720; F01-F10 identify UI contract defects.                                  | Authenticated UI and mutation states `BLOCKED`.                                                      |
| Frontend invocation  | Plan/prepare/confirm/manage request shapes and duplicate/default/error behavior are source/test-confirmed.                       | No authenticated mutation was invoked live.                                                          |
| Server/provider-test | Callback and upgrade validation/idempotency pass targeted backend tests; cancel/reactivate re-call behavior is source-confirmed. | Live Provider operations `BLOCKED`.                                                                  |
| Durable state/reload | Test-managed callback/upgrade persistence converges safely.                                                                      | Production/live payment order, agreement, payment, subscription, ledger, and reload state `BLOCKED`. |

## Scheduler Boundary

- `processRecurringRenewals()` at 00:00 delegates due work to `RecurringRenewalService`.
- `processExpiredPaymentOrders()` at 00:10 expires stale READY/IN_PROGRESS orders.
- `processExpiredSubscriptions()` at 00:30 expires elapsed subscriptions and clears pending changes without applying them for free.
- Due selection, state fencing, deterministic retry, three-day grace/expiry, successful pending plan/cycle application, ambiguous-outcome blocking, concurrent worker convergence, and idempotency are `PASS` from source/existing targeted test evidence.
- Completion ordering is `PARTIAL/BLOCKED`: independent cron start offsets are present, but no completion fence was demonstrated.
- Live Provider and production/live durable state remain `BLOCKED`.

Scheduler PASS evidence is a boundary result, not an additional defect and not part of the 11-finding count.

## Quality Evidence

The test-suite, typecheck, and ESLint results were supplied from main execution and were not rerun by this documentation closeout:

- Frontend targeted Vitest: `9` files / `65` tests, all passed, `3.65s`.
- `npm run typecheck`: `PASS`.
- Targeted ESLint: `PASS`.
- Backend run A: `--rerun-tasks`; `15` XML suites / `107` tests; failures `0`, errors `0`, skipped `0`; wall `54.9s`.
- Backend run B: `7` suites / `39` tests; failures `0`, errors `0`, skipped `0`; wall `42.3s`.
- Combined backend distinct targeted executions: `146 PASS`.

The following final documentation quality checks were actually run:

- Prettier write on handoff/findings/evidence/summary: exit `0`; handoff unchanged `60ms`, findings `59ms`, evidence `48ms`, summary `11ms`.
- Prettier check on all four WI-027 documents: exit `0`; `All matched files use Prettier code style.`
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: exit `0`; Tier 0, links, `539` traceability IDs, document index, and all validation passed.
- `git diff --check`: exit `0`; no output.

No frontend/backend test suite, typecheck, ESLint, or build was rerun by closeout. Main did perform the read-only guard recheck described below.

## Guard, Browser, and Screenshot Evidence

- Anonymous checkout, success callback, fail callback, and manage guards preserved each exact percent-encoded internal `returnTo`; no authenticated mutation was invoked.

| Requested route                                                  | Observed redirect                                                                                |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| `/subscriptions/checkout?plan=STANDARD&cycle=MONTHLY&from=audit` | `/login?returnTo=%2Fsubscriptions%2Fcheckout%3Fplan%3DSTANDARD%26cycle%3DMONTHLY%26from%3Daudit` |
| `/subscriptions/checkout/success?from=audit`                     | `/login?returnTo=%2Fsubscriptions%2Fcheckout%2Fsuccess%3Ffrom%3Daudit`                           |
| `/subscriptions/checkout/fail?from=audit`                        | `/login?returnTo=%2Fsubscriptions%2Fcheckout%2Ffail%3Ffrom%3Daudit`                              |
| `/subscriptions/manage?from=audit`                               | `/login?returnTo=%2Fsubscriptions%2Fmanage%3Ffrom%3Daudit`                                       |

- Public `/subscriptions` was observed at `1280x720` through GET, DOM, and screenshot evidence.
- No horizontal overflow was observed: document width `1265`, viewport width `1280`.
- Screenshot: `output/ui-ux-audit/20260809/WI-027/PUB-07-business-yearly-1280x720-observed.png`.
- Live public plan ordering: unfiltered IDs `1-3` INDIVIDUAL then `4-6` BUSINESS; BUSINESS-filtered response IDs `4-6`.
- Responsive live checks at `1024x768`, `390x844`, and `360x800` are `NOT RUN / BLOCKED` because current browser control could not resize. Static CSS source is not live responsive proof.
- Browser restoration: `OBSERVED`. Main restored Home at `http://127.0.0.1:5173/` after the read-only guard recheck.

## Blocked Coverage

- Authenticated USER/BUSINESS/ADMIN runtime.
- Live Provider prepare, auth, confirm, charge, cancel, and failure outcomes.
- Production/live durable PaymentOrder, Billing Agreement, payment, subscription, ledger, and canonical reload rows.
- Responsive 1024/390/360 live behavior.

## Risk and Approval Points

- F01 requires an explicit product/payment contract decision before charge-bearing acceptance: purpose and amount must be authoritative at one server-bound boundary.
- F02/F03 require immutable audience-aware plan identity and prepare idempotency before BUSINESS/payment acceptance.
- F04 requires an explicit unknown-outcome and canonical reload UI even though backend callback/upgrade replay safety is confirmed.
- F05-F10 require separately approved product work; this WI did not change product behavior.
- F11 requires documentation ownership to reconcile the `39`/`41` count and label historical versus current branch authority.

## Change Boundary

- This closeout created only the evidence pack and this summary.
- No product/runtime/DB/schema/configuration/fixture/secret/browser/Provider/git mutation, stage, or commit occurred.
- No product rollback is required.
- The screenshot is intentional audit output.
- Intentional `output/client-demo-screenshots-20260716-140514.zip` was preserved and uninspected; it was not opened, read, hashed, metadata-probed, moved, replaced, or used as a fixture.

## Deliverables

- Agent evidence: `deliverables/agent/WI-20260809-ATS-027-evidence-pack.md`
- Findings: `deliverables/agent/WI-20260809-ATS-027-findings.md`
- User summary: `deliverables/user/WI-20260809-ATS-027-summary.md`
