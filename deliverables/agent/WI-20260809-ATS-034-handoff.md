[WI HEADER]
WI ID: WI-20260809-ATS-034
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-032, WI-20260809-ATS-033
Blocks: WI-20260809-ATS-035, WI-20260809-ATS-052

[WI SUMMARY]

Why:

- Close canonical root `CR-031-084` / `ATS-027-F04`: payment and
  subscription mutations currently present only binary success or failure even
  when the request may have committed before its response was lost, or when
  the mutation succeeded but the following canonical reload failed.
- Preserve the command, locking, and Provider idempotency guarantees completed
  by WI-032 and WI-033. This WI is a recovery/read-model and user-state
  correction, not a pricing, charge, plan-change, or Provider-policy change.
- Prevent ordinary error copy or an automatic retry from causing a user to
  repeat a potentially completed financial action.

Frozen outcome vocabulary:

- `COMMITTED`: an authoritative server result and canonical user-owned reads
  prove the requested durable state. A success toast or redirect is permitted
  only after this proof.
- `FAILED`: an authoritative business result or terminal command state proves
  the requested operation did not commit and is not still ambiguous.
- `RELOAD_FAILED`: the mutation response explicitly reported success, but one
  or more canonical reads required to refresh the screen failed. Preserve the
  successful mutation result and operation context; never relabel this as a
  mutation failure.
- `UNKNOWN`: the mutation may have reached the server or Provider, but neither
  committed nor failed can yet be proved. Do not automatically repeat the
  financial mutation. Preserve the operation context and expose a read-only
  `check status again` action.

Known current basis:

- Billing confirmation and charged upgrade already use persisted command/status
  fences. `DONE` replays return an existing result, `PROVIDER_SUCCEEDED` replays
  finalize locally, and `PROCESSING` or
  `PENDING_PROVIDER_CONFIRMATION` is not charged blindly.
- Initial billing confirmation already has exact callback `orderId`; however,
  there is no owner-scoped user API that reads the current outcome of that
  order without mutation.
- Charged upgrade already uses a deterministic command key bound to the current
  subscription period, exact target plan, and target billing cycle. The
  frontend does not receive an operation identifier if the mutation response
  is lost.
- Cancel and reactivate are local, no-charge state transitions whose repeated
  calls converge through existing state checks. They still must not be
  automatically replayed by the UI.
- `SubscriptionPaymentPage` shows success and redirects immediately after the
  confirm response. `SubscriptionManagePage.load()` catches its own error, so
  callers cannot distinguish mutation success from post-mutation reload
  failure.

Scope In:

- Callback/recovery behavior in `SubscriptionPaymentPage.tsx` and focused API
  helpers/tests.
- Change, cancel, and reactivate recovery behavior in
  `SubscriptionManagePage.tsx` and focused API helpers/tests.
- A minimal authenticated, owner-scoped, read-only payment command outcome API
  and DTO/service/repository support when required to prove callback or charged
  upgrade state after response loss.
- Exact-intent charged-upgrade lookup may use the existing deterministic
  command identity and current authoritative subscription period. It must not
  use an unconstrained "latest payment" guess.
- Canonical reload of the current Subscription and Billing Agreement before a
  final success claim.
- Explicit UI states and retry-read controls for `RELOAD_FAILED` and `UNKNOWN`.
- H2/Test-Provider backend integration evidence and frontend tests for response
  loss, post-commit 5xx simulation, canonical reload failure, terminal failure,
  owner isolation, and no duplicate mutation.
- PG, QA-INTEG, and RE independent review after implementation, followed by
  verified payment/API/screen-flow documentation and two-set deliverables.

Scope Out:

- Pricing, proration, billing-cycle, plan ranking, upgrade/downgrade timing,
  cancellation, reactivation, or subscription eligibility policy changes.
- Any new Provider call, real Toss charge, billing-key issue/deletion, refund,
  cancellation, mail, retained DB action, production data, or secret/config
  inspection.
- Schema migration, backfill, new dependency, architecture replacement, broad
  event sourcing, polling infrastructure, or background reconciliation change.
- Automatic retry of confirm, upgrade, cancel, or reactivate after an ambiguous
  transport/5xx result.
- ADMIN refund/correction recovery owned by WI-035.
- General Plan/Payment/Manage loading, malformed-query, error-copy, and
  confirmation improvements owned by WI-052 except where directly required to
  represent this WI's four outcome states.

DoD:

- [ ] Callback success is not announced and navigation does not occur until
      canonical Subscription/Billing Agreement state is reloaded and matches
      the authoritative confirm purpose/result.
- [ ] A successful mutation followed by failed canonical reload produces
      `RELOAD_FAILED`, keeps the successful mutation context, and offers only a
      read retry before any new mutation.
- [ ] A lost/5xx callback or charged-upgrade response is reconciled through an
      authenticated owner-scoped read. `DONE` can converge to `COMMITTED`;
      terminal failure can converge to `FAILED`; in-flight/ambiguous or failed
      reads remain `UNKNOWN`.
- [ ] `UNKNOWN` copy states that processing may already have occurred and tells
      the user not to repeat the action. Its primary recovery control performs
      reads only.
- [ ] Cancel/reactivate response-loss tests prove canonical state convergence
      without an automatic second mutation.
- [ ] Manage mutation success messages survive reload failure and are not
      overwritten by a generic page-level mutation error.
- [ ] Read-only outcome endpoints enforce authenticated ownership and disclose
      only the minimum non-secret status/intent fields needed for recovery.
- [ ] Outcome reads never call a Provider, mutate a command, finalize a charge,
      retry a mutation, expose secrets/PII, or rely on client-supplied ownership.
- [ ] WI-032 intent/audience/amount controls and WI-033 prepare-attempt key
      lifecycle remain unchanged and all adjacent regressions pass.
- [ ] Focused, adjacent, full, coverage, typecheck, lint, format, build,
      documentation, and diff gates pass with exact evidence.
- [ ] PG, QA-INTEG, and RE independently approve the final bounded diff; the
      two-set deliverables and verified docs are complete.
- [ ] Main immediately triggers WI-035 after closeout.

Constraints / Forbidden:

- Do not infer `COMMITTED` from a toast, an HTTP 2xx alone, stale component
  state, navigation, or an absent error. Require canonical durable read proof.
- Do not infer `FAILED` merely because Axios timed out, the response was lost,
  the server returned 5xx, or the post-mutation reload failed.
- Do not retry a financial mutation automatically in an effect, interceptor,
  polling loop, reload helper, or unknown-outcome action.
- Do not make GET/read recovery mutate state, acquire a Provider result, or
  finalize commands. If a safe outcome cannot be proven without mutation,
  return an ambiguous/in-flight outcome and leave Provider reconciliation to
  the existing operational path.
- Never expose or log raw `authKey`, `customerKey`, `billingKey`, card data,
  Provider payload, credentials, secrets, or unrelated user data.
- Owner authorization must be server-side. A valid but foreign `orderId` must
  not reveal whether the order exists or disclose its status.
- Preserve the canonical aggregate lock order and all WI-032/WI-033 command
  keys. Do not rewrite command keys or create a second prepare/charge path.
- If exact charged-upgrade recovery requires a schema change, a new client
  mutation key, a Provider status call, or another architecture/policy choice,
  stop before that change and escalate with evidence.
- Preserve all unrelated shared-worktree edits. Do not stage, commit, push,
  branch, merge, deploy, delete, inspect secrets, or touch the preserved demo
  ZIP.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] Confirm response success + canonical reload success -> `COMMITTED`, one
      success announcement, replace navigation, and no second confirm.
- [ ] Confirm response success + canonical reload failure -> `RELOAD_FAILED`,
      no final success navigation, operation context retained, read retry only.
- [ ] Confirm transport/5xx loss + owner outcome `DONE` + canonical reload ->
      `COMMITTED` without a second confirm or Provider call.
- [ ] Confirm transport/5xx loss + terminal failure -> `FAILED`; in-flight,
      ambiguous, foreign, or unavailable status -> `UNKNOWN` without mutation.
- [ ] Upgrade response success + reload failure retains the exact successful
      upgrade/schedule/no-change result while displaying reload recovery.
- [ ] Upgrade response loss + canonical matching state converges to
      `COMMITTED`; ambiguous payment command remains `UNKNOWN`.
- [ ] Cancel and reactivate post-commit response loss reconcile from canonical
      subscription state and never auto-submit a second mutation.
- [ ] Rapid repeated read-recovery clicks are fenced or deduplicated so stale
      reads cannot overwrite a newer authoritative result.

Security / Data:

- [ ] Anonymous and foreign-user outcome reads are rejected without status or
      existence disclosure.
- [ ] Outcome responses contain no raw Provider payload, key material, payment
      method secret, email, nickname, or unrelated identifiers.
- [ ] Test evidence uses only synthetic H2/Test-Provider state and performs no
      live Provider, mail, refund, cancellation, or retained DB action.

Quality:

- [ ] Focused backend controller/service/integration tests pass.
- [ ] Focused frontend payment/manage/replay tests pass, including StrictMode
      and stale-response cases.
- [ ] Full backend tests/build/JaCoCo gates pass.
- [ ] Full frontend tests/coverage/typecheck/ESLint/Prettier/build pass.
- [ ] `python .claude/scripts/validate_docs.py` and `git diff --check` pass.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policies - Inferred):

- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`
- `docs/standards/evidence-pack-standard.md`

Tier 2 (Frontend / Domain):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/payment-integration-design.md`
- `docs/ui/screen-flow.md`
- `docs/payment/user-flows.md`
- `docs/payment/admin-operations-guide.md`

REQ / Audit / Dependency Evidence:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-020-acceptance-matrix.md`
- `deliverables/agent/WI-20260809-ATS-027-findings.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
- `deliverables/agent/WI-20260809-ATS-032-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-033-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-033-re-review.md`

Primary Product / Test Pointers:

- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx`
- `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx`
- `frontend/src/api/payments.ts`
- `frontend/src/api/userSubscriptions.ts`
- `src/main/java/com/atstudio/atstudio/controller/PaymentController.java`
- `src/main/java/com/atstudio/atstudio/controller/UserSubscriptionController.java`
- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandTransactionService.java`
- `src/main/java/com/atstudio/atstudio/service/UserSubscriptionService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentCommandKeyFactory.java`
- `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentProviderSuccessRecoveryIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentCommandIndependentVerificationIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/service/SubscriptionUpgradeCommandIntegrationTest.java`

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-034-summary.md`:

- Korean summary of corrected user behavior, what each outcome means, what was
  verified, and any residual acceptance limitation.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-034-evidence-pack.md`:

- Exact patch pointers, four-state decision table, owner/read security proof,
  UI/API/Provider/durable-state lane separation, commands, exit codes, test
  counts, limitations, reviewer decisions, and rollback.

Review artifacts:

- `deliverables/agent/WI-20260809-ATS-034-pg-review.md`
- `deliverables/agent/WI-20260809-ATS-034-qa-integ-review.md`
- `deliverables/agent/WI-20260809-ATS-034-re-review.md`

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-034-handoff.md`:

- Preserve this packet as the approved execution contract and audit pointer.

[TRACEABILITY REQUIREMENTS]

- Map every changed product, test, and documentation file to `CR-031-084` /
  `ATS-027-F04` and at least one acceptance criterion.
- Record the exact proof predicate for each of `COMMITTED`, `FAILED`,
  `RELOAD_FAILED`, and `UNKNOWN`; do not collapse evidence lanes.
- For each scenario, report UI copy/control, frontend API invocation count,
  backend result, Provider invocation count/boundary, and durable test-managed
  state separately.
- Evidence pointers require repository-relative paths, tight line references,
  exact commands, exit codes, test counts, and stated limitations.
- Record authorization behavior for absent, malformed, foreign, terminal,
  in-flight, and completed order/intent lookups.
- Record that all status-recovery controls are reads and all automatic mutation
  replay counts are zero.
- Preserve unrelated worktree changes and do not stage, commit, push, branch,
  merge, delete, deploy, inspect secrets, or touch the preserved ZIP.

[ROLLBACK]

- Revert only WI-034 product, test, and verified documentation hunks listed in
  the Evidence Pack. Preserve WI-032/WI-033 behavior and unrelated shared
  worktree edits.
- No schema, retained data, or Provider reversal is permitted or required.
- Remove any newly added read-only endpoint/DTO/helper only together with its
  callers and tests; do not remove existing command/idempotency safeguards.

[WI CHAIN]

- WI-034 completion immediately triggers WI-20260809-ATS-035.
- Main must run `/create-wi-handoff-packet` for WI-035 and delegate its owner
  before unrelated closeout work.
- WI-035 alone owns ADMIN refund/correction response-loss recovery. WI-052 owns
  the remaining Plan/Checkout/Manage P2 loading and copy findings.
