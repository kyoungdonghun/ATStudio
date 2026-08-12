[WI HEADER]
WI ID: WI-20260809-ATS-033
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-032
Blocks: WI-20260809-ATS-034

[WI SUMMARY]

Why:

- Close canonical root `CR-031-083` / `ATS-027-F03` by making billing-agreement
  prepare idempotent for one exact checkout prepare attempt.
- Require an HTTP `Idempotency-Key` for checkout prepare and preserve one key
  across every replay of the same attempt.
- Duplicate sequential or concurrent requests for that key and authoritative
  tuple must reuse one valid, unexpired prepare order instead of creating
  distinct orders.
- Preserve the charge, pricing, subscription, and WI-032 fail-closed policies.

Approved prepare-attempt contract:

- The frontend generates one opaque `Idempotency-Key` per checkout prepare
  attempt and sends it in the required HTTP header.
- Session-scoped client storage preserves the key across React StrictMode
  remount, reload, network retry, and an explicit same-attempt retry.
- The frontend generates a new key only after an expired or terminal result and
  only when the user invokes an explicit new-attempt action.
- The server binds the attempt to authenticated user identity,
  server-authoritative purpose, exact `planId`, validated plan audience, and
  exact `billingCycle`.
- Client key or tuple values are never authoritative by themselves. Reuse is
  eligible only after every WI-032 authentication, plan, audience, cycle,
  subscription-state, and purpose check succeeds.
- Reusing one key with any changed authoritative tuple dimension returns a
  stable HTTP 409 conflict before mutation or Provider descriptor invocation.

Known current basis:

- WI-032 establishes exact authenticated user + `planId` + audience validation
  - `billingCycle` + purpose identity.
- `payment_orders` already has nullable unique `command_key` support and
  repository locking primitives.
- No `User`-first lock is permitted even though `UserRepository` has
  `findByIdForUpdate`.
- The canonical lock order remains `BillingAgreement -> UserSubscription ->
PaymentOrder -> SubscriptionPayment -> PaymentRefund`.
- `RecurringPaymentProvider.prepareAgreement` is an explicitly pure,
  deterministic, side-effect-free descriptor contract. Provider mutation
  starts only at confirm/charge.
- No schema migration is intended. Use the existing command-key and locking
  capabilities; if implementation proves a schema change unavoidable, stop
  before editing schema and escalate.

Approval record:

- The user explicitly approved this complete contract on 2026-08-10.
- That approval includes the required HTTP header/public API change, frontend
  key lifecycle, backend claim/retry behavior, pure Provider descriptor
  contract, canonical lock order, and disposable MySQL 8/InnoDB verification.
- This approval supersedes the earlier WI-033 exclusions for frontend/public
  API changes and H2-only concurrency proof.

Scope In:

- Frontend session-scoped `Idempotency-Key` lifecycle/helper, checkout prepare
  API wrapper/header, relevant subscription/payment pages, and focused tests.
- Backend controller/header validation before DB or Provider work.
- Versioned, opaque, owner-scoped command digest, authoritative tuple binding,
  reuse lookup, validity/expiry/terminal-usability checks, and response
  reconstruction.
- Existing `PaymentOrder.command_key` claim path, committed claim,
  commit-and-retry handling for unique-key races, and canonical aggregate lock
  order without a `User`-first lock.
- `RecurringPaymentProvider.prepareAgreement` contract and test/V1
  implementations required to make descriptor purity explicit.
- Focused controller, service, repository/concurrency, Provider-contract, and
  frontend tests for all required key lifecycle and replay paths.
- Supplemental H2 coverage plus a fresh disposable MySQL 8/InnoDB schema for
  lock order, race convergence, winner-commit visibility, and loser reread.
- PG and QA-INTEG pre-review, SE TDD implementation, RE independent review,
  and DocOps/evidence closeout.
- HTTP prepare, payment command/idempotency, Provider descriptor, and current
  schema documentation after verified behavior.

Scope Out:

- Charge execution, billing-key authorization, refund, cancellation, mail, or
  any other real external effect.
- WI-034 callback response-loss, unknown-outcome, reload, and recovery work.
- Pricing, amount derivation, subscription eligibility, purpose derivation,
  audience, or charge policy changes.
- Any use of the client key as authority for user, purpose, plan, audience,
  cycle, price, amount, subscription state, or financial outcome.
- A schema migration, backfill, destructive cleanup, retained-database action,
  new dependency, deployment, or secret/configuration inspection.
- Callback/confirm/charge recovery semantics owned by WI-034. Prepare-attempt
  replay must remain distinct from financial callback/outcome recovery.
- Support for a future Provider whose prepare step mutates external state. Such
  a Provider requires a separately approved flow and must fail closed until
  then.

DoD:

- [ ] PG and QA-INTEG pre-review artifacts approve the bounded identity,
      transaction/locking order, Provider boundary, and cross-layer evidence
      matrix before product implementation starts.
- [ ] Frontend tests prove one attempt key survives StrictMode remount, reload,
      network retry, and same-attempt retry, and rotates only through the
      explicit new-attempt action after expired/terminal state.
- [ ] Missing, blank, malformed, oversized, or control-character header values
      fail before DB access or Provider descriptor invocation.
- [ ] Only a versioned, opaque, owner-scoped digest is stored in
      `payment_orders.command_key`; the claim row is fully bound to the
      authoritative user/purpose/plan/audience/cycle tuple.
- [ ] Repeated valid prepare with the same key and exact authoritative tuple
      returns the same `orderId` and server-authoritative response without a
      second durable order.
- [ ] The same key with a changed tuple returns a stable HTTP 409 with zero
      mutation and zero Provider descriptor call.
- [ ] Concurrent test-Provider requests converge to one durable order in both
      supplemental H2 and fresh disposable MySQL 8/InnoDB evidence.
- [ ] Unique-key losers retry only after winner commit, reread the committed
      claim, and fully validate owner, tuple, and lifecycle before reuse.
- [ ] Expired or terminally unusable orders are not reused; one fresh bounded
      attempt can be created only with a new key from the explicit frontend
      action under existing lifecycle semantics.
- [ ] WI-032 rejected and mismatch paths remain fail-closed before reusable
      order selection, order mutation, or Provider descriptor invocation.
- [ ] `RecurringPaymentProvider.prepareAgreement` is pure, deterministic,
      side-effect-free, and called outside the local transaction; Provider
      mutation begins at confirm/charge only.
- [ ] Canonical aggregate lock order is preserved with no `User`-first lock and
      no `command_key` rewrite.
- [ ] Legacy null-command-key rows are ignored and never rewritten, backfilled,
      or deleted.
- [ ] Focused, adjacent, full, coverage, static, build, and documentation gates
      pass with exact command/result evidence.
- [ ] RE independently reviews the final bounded diff and tests.
- [ ] The two-set deliverables and payment idempotency documentation are
      complete, and main immediately triggers WI-034.

Constraints / Forbidden:

- STOP before product edits if either mandatory pre-review is missing or has a
  blocker.
- The user-approved HTTP header, frontend key lifecycle, pure Provider
  descriptor, claim/retry, lock order, and MySQL verification are authorized
  WI-033 contract changes and are not escalation items by themselves.
- STOP and escalate to main/user before editing schema or if implementation
  requires any additional architecture, policy, security, dependency, or
  Provider-flow decision beyond this frozen contract.
- The `Idempotency-Key` header is required. Do not add an optional fallback,
  body duplicate, query-parameter substitute, or server-generated replacement.
- PG pre-review must freeze the accepted key grammar and maximum size from
  authoritative current constraints; if a new security/policy choice is
  unavoidable, stop and escalate rather than guess.
- Do not change charge, price, amount, subscription, audience, billing-cycle,
  or purpose policy.
- Do not weaken WI-032 authentication, exact-plan, audience, cycle, purpose, or
  response validation.
- Validate missing, blank, malformed, oversized, and control-character keys
  before all DB/repository and Provider work.
- Persist only a versioned opaque digest in `command_key`. Never persist or log
  the raw key, PII, `customerKey`, `billingKey`, `authKey`, card data, callback
  material, credentials, or secrets there or in evidence.
- The owner-scoped key digest claim plus existing authoritative PaymentOrder
  fields must bind authenticated user, purpose, exact plan ID/audience, and
  billing cycle. Same key + changed tuple must be HTTP 409, not a new claim.
- Never rewrite `command_key`, including during reuse, expiry, terminal state,
  conflict handling, or callback processing.
- Ignore legacy null-command-key rows. Never rewrite, backfill, delete, or use
  them as replay claims.
- Do not acquire a `User` lock first. Preserve `BillingAgreement ->
UserSubscription -> PaymentOrder -> SubscriptionPayment -> PaymentRefund`.
- Handle uniqueness races through committed claim plus bounded
  commit-and-retry. The loser rereads only after winner commit and validates
  full owner/tuple/lifecycle state before returning.
- Invoke the pure Provider descriptor outside the local transaction. Do not
  move Provider mutation into prepare.
- A future Provider that cannot implement pure deterministic prepare must fail
  closed and use a separately approved flow; do not weaken the interface.
- Do not define new expiry or terminal-state policy. Use existing persisted
  lifecycle semantics; ambiguity is an escalation.
- Do not use wall-clock sleeps as expiry or concurrency proof. Use deterministic
  time control and test synchronization with independent transactions.
- Do not call a real Provider/SDK, charge/refund/cancel, send real mail, use a
  retained database, read/output secrets, deploy, inspect/touch ignored local
  files, or inspect/touch the preserved ZIP.
- Preserve unrelated shared-worktree changes and Git state.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] Checkout prepare without `Idempotency-Key`, or with a blank, malformed,
      oversized, or control-character key, is rejected before DB/repository or
      Provider descriptor interaction.
- [ ] The frontend creates one opaque key per prepare attempt, stores it in
      session-scoped client storage, and sends it through the payment API
      wrapper as the required HTTP header.
- [ ] React StrictMode remount, browser reload, network retry, and same-attempt
      retry reuse the stored key and do not generate a replacement.
- [ ] Only an explicit new-attempt action after an expired or terminal result
      clears/rotates the stored attempt and generates a new key.
- [ ] Two sequential requests with the same key, authenticated user,
      authoritative purpose, exact `planId`, validated audience, and
      `billingCycle`, while the first order is valid, return an equal
      authoritative response and the same `orderId`.
- [ ] Sequential replay leaves exactly one matching durable `payment_orders`
      row and never rewrites `command_key` or financial intent.
- [ ] Reusing the same key while changing exactly one of user scope, purpose,
      plan ID/audience, or cycle returns the stable documented HTTP 409 and
      performs zero mutation and zero Provider descriptor invocation.
- [ ] The persisted command key is versioned and opaque. Inspection proves it
      contains no raw key, PII, customer/billing/auth key, card/callback data,
      credential, or secret.
- [ ] An expired matching order is not reused; a new valid bounded intent
      requires the explicit new-attempt action/new key and returns a different
      `orderId` while preserving the old row as history.
- [ ] A matching order that is terminally unusable under existing lifecycle
      semantics is not reused and does not block one explicit fresh attempt.
- [ ] Legacy nullable-command-key rows are handled compatibly without
      migration, backfill, deletion, or accidental cross-intent reuse.
- [ ] WI-032 invalid audience, exact-plan mismatch, cycle mismatch, and both
      purpose mismatch directions remain rejected with no reusable-order or
      Provider descriptor interaction.
- [ ] `prepareAgreement` returns a pure deterministic descriptor and performs
      no Provider mutation; confirm/charge remains the first mutation boundary.
- [ ] Prepare-attempt replay is documented and tested separately from WI-034
      financial callback, unknown-outcome, reload, and recovery behavior.

Concurrency / Performance:

- [ ] Supplemental H2 and mandatory fresh disposable MySQL 8/InnoDB tests start
      concurrent same-key/same-tuple requests in independent transactions,
      synchronize the race, and prove all successful responses identify one
      durable order.
- [ ] MySQL evidence proves canonical lock order, one committed claim, winner
      commit visibility, bounded loser retry/reread, and exactly one matching
      durable row without leaked constraint exception, deadlock, or generic
      5xx.
- [ ] A same-key/changed-tuple concurrency case converges on stable HTTP 409 for
      the conflicting request with zero mutation and Provider descriptor call.
- [ ] Provider test-double evidence proves `prepareAgreement` is pure,
      deterministic, side-effect-free, and invoked outside local transactions.
- [ ] The lock trace proves there is no `User`-first lock and preserves
      `BillingAgreement -> UserSubscription -> PaymentOrder ->
SubscriptionPayment -> PaymentRefund` whenever those aggregates are
      acquired.
- [ ] Existing unique command-key lookup/locking is used without an unbounded
      table scan. No latency or throughput SLA is invented in this WI.

Quality:

- [ ] PG pre-review covers command-key data content, authenticated ownership,
      accepted header grammar/size, cross-user isolation, 409 conflict,
      replay/reuse ordering, Provider purity, and fail-closed paths.
- [ ] QA-INTEG pre-review defines the UI -> API -> Provider -> durable-state
      matrix and key-lifecycle, sequential, conflict, expiry, H2, and MySQL
      assertions.
- [ ] Frontend/backend unit tests are red before implementation and green
      afterward; H2 is supplemental and MySQL 8/InnoDB is the mandatory lock,
      convergence, and commit-visibility proof.
- [ ] The disposable MySQL schema and data are created for this test run only,
      torn down afterward, and never reuse a retained/local/production schema.
- [ ] Existing WI-032 focused backend and frontend regressions remain green.
- [ ] `Q-ALL` passes: focused and adjacent tests, full backend/frontend suites,
      coverage review, typecheck, ESLint, Prettier, both builds, documentation
      validation, and `git diff --check`.
- [ ] RE records an independent ACCEPTED/BLOCKED decision and residual risks.

[EXECUTION GATES]

1. PG pre-review, no product edits:
   - Produce `deliverables/agent/WI-20260809-ATS-033-pg-review.md`.
   - Freeze required-header grammar and maximum size; confirm blank, malformed,
     oversized, and control-character values fail before DB/Provider work.
   - Confirm owner-scoped digest and PaymentOrder tuple binding contain no raw
     key, PII, customer/billing/auth key, card/callback data, credential, or
     secret and cannot cross authenticated ownership.
   - Confirm same-key/changed-tuple HTTP 409, legacy-null isolation, immutable
     `command_key`, pure Provider descriptor, and fail-closed future Provider
     behavior.
2. QA-INTEG pre-review, no product edits:
   - Produce
     `deliverables/agent/WI-20260809-ATS-033-qa-integ-review.md`.
   - Freeze the exact StrictMode/remount/reload/network/same-attempt lifecycle,
     explicit rotation, header validation, sequential replay, 409 conflict,
     expiry, terminal, H2, MySQL, Provider descriptor, and durable-row matrix.
   - Confirm fresh disposable MySQL 8/InnoDB plus supplemental H2, test Provider
     only, canonical lock order, commit visibility, and no retained-state
     operation.
3. SE TDD implementation:
   - Add frontend key-lifecycle/API tests, backend validation/service/provider
     tests, supplemental H2 tests, and disposable MySQL tests first; capture the
     expected red results.
   - Implement the smallest frontend and backend changes that satisfy the
     approved reviews using existing schema and primitives.
   - Validate the HTTP key before DB/Provider work, persist only the versioned
     opaque owner-scoped digest, and bind the claim row to the full
     authoritative tuple.
   - Preserve canonical aggregate lock order without a `User`-first lock.
   - Commit the winning claim, then handle a unique-key loser through bounded
     commit-and-retry and a post-commit reread with full tuple/lifecycle
     validation. Never rewrite `command_key`.
   - Invoke the pure deterministic Provider descriptor outside the local
     transaction. If a Provider cannot comply, fail closed and escalate for a
     separately approved flow.
   - Keep prepare replay distinct from WI-034 financial outcome recovery.
4. RE independent review:
   - Produce `deliverables/agent/WI-20260809-ATS-033-re-review.md` after SE's
     green evidence.
   - Inspect frontend key lifetime, header/API contract, digest safety, HTTP
     409, canonical lock order, commit-and-retry, descriptor purity, H2/MySQL
     evidence, regression scope, and rollback.
5. Documentation and evidence closeout:
   - Update only verified payment command/idempotency contracts.
   - Produce the agent Evidence Pack and Korean user summary.
   - Run all required gates and record exact results and limits.
6. WI chain:
   - On WI-033 completion, main immediately creates the standard WI-034 handoff
     with `/create-wi-handoff-packet` and delegates it. Do not absorb WI-034.

[INPUT POINTERS]

Tier 0 (Constitution and SE standards):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/standards/evidence-pack-standard.md`

Tier 1 (Payment identity, security, quality, and current schema):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`
- `docs/design/db-schema.md`

Tier 2 (Current payment/API/UI contracts):

- `docs/design/payment-integration-design.md`
- `docs/design/api-spec.md`
- `docs/ui/screen-flow.md`
- `.agents/skills/react-best-practices/AGENTS.md` (auto-injected React stack
  pointer; regression/read-only unless an escalation changes scope)
- `docs/standards/frontend-standards.md` (auto-injected React stack pointer;
  regression/read-only)

REQ / Context:

- [Approved REQ](../user/REQ-20260809-ATS-001.md)
- [Canonical findings and Phase 1 owner row](WI-20260809-ATS-031-consolidated-findings.md)
- [WI-032 Evidence Pack](WI-20260809-ATS-032-evidence-pack.md)
- `CR-031-083` / `ATS-027-F03`

Primary source and test pointers:

- `src/main/java/com/atstudio/atstudio/service/BillingAgreementApplicationService.java`
- `src/main/java/com/atstudio/atstudio/**/PaymentOrder*.java`
- `src/main/java/com/atstudio/atstudio/**/UserRepository.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/BillingAgreementPrepareRequest.java`
- `src/test/java/com/atstudio/atstudio/service/BillingAgreementApplicationServiceTest.java`
- `src/test/java/com/atstudio/atstudio/controller/PaymentControllerTest.java`
- `src/test/java/com/atstudio/atstudio/**/*BillingAgreement*Test.java`

Required review inputs before SE product edits:

- `deliverables/agent/WI-20260809-ATS-033-pg-review.md`
- `deliverables/agent/WI-20260809-ATS-033-qa-integ-review.md`

[TEST AND QUALITY COMMANDS]

Run from the repository root unless a different working directory is stated.
Record exit code, test counts, failures/skips, and any rerun separately; do not
replace a failed full run with only an isolated rerun.

Focused backend TDD and concurrency:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.service.BillingAgreementApplicationServiceTest" --tests "*BillingAgreementPrepareIdempotency*Test" --console=plain
```

Adjacent billing-agreement regression:

```powershell
.\gradlew.bat test --tests "com.atstudio.atstudio.controller.PaymentControllerTest" --tests "*BillingAgreement*Test" --console=plain
```

Full backend, coverage, and build:

```powershell
.\gradlew.bat test --console=plain
.\gradlew.bat jacocoTestReport jacocoTestCoverageVerification --console=plain
.\gradlew.bat build --console=plain
```

WI-032 frontend regression and complete frontend quality (`frontend/` working
directory):

```powershell
npm test -- src/api/domainApis.test.ts src/pages/public/SubscriptionPlanPage.test.tsx src/pages/subscriber/SubscriptionManagePage.test.tsx src/pages/subscriber/SubscriptionPaymentPage.test.tsx src/pages/subscriber/SubscriptionPaymentReplay.test.tsx
npm test
npm test -- --coverage
npm run typecheck
npm run lint
npm run format
npm run build
```

Documentation and diff checks:

```powershell
python .claude/scripts/validate_docs.py
Set-Location frontend
npx prettier --check "../docs/design/payment-integration-design.md" "../docs/design/api-spec.md" "../deliverables/agent/WI-20260809-ATS-033-*.md" "../deliverables/user/WI-20260809-ATS-033-summary.md"
Set-Location ..
git diff --check
```

[EVIDENCE SEPARATION]

UI / control evidence:

- Report the approved checkout prepare control-contract change: require
  `Idempotency-Key`, preserve it in session-scoped storage across StrictMode
  remount, reload, network retry, and same-attempt retry, rotate it only through
  an explicit new-attempt action, and provide no silent fallback.
- Re-run WI-032 frontend prepare-response and SDK non-invocation tests.
- Report automated UI/control evidence separately from browser, real SDK, and
  payment acceptance evidence.

API / server evidence:

- Capture exact authenticated request identity and response for first prepare,
  sequential reuse, each changed identity dimension, expiry, terminal
  unusability, and concurrent requests.
- Prove equal authoritative response and same `orderId` for reusable requests.
- Prove WI-032 mismatch/rejection precedes idempotency selection and effects.

Provider evidence:

- Use only a test Provider/double and record invocation count and ordering for
  first creation, sequential reuse, concurrency, expiry, and rejection.
- Prove Toss satisfies the interface-level pure, deterministic,
  side-effect-free descriptor contract; do not assume current Toss behavior for
  future PGs, and require any non-compliant future Provider to fail closed
  pending a separately approved flow.
- Produce no real Toss SDK, Provider, authorization, charge, cancellation, or
  refund evidence.

Durable-state evidence:

- Use H2 only as supplemental evidence; a fresh disposable MySQL 8/InnoDB schema is mandatory for lock-order, convergence, unique-race, and commit-visibility proof, and no retained, local, or production database may be inspected or mutated.
- Record durable row count, `orderId`, bounded command-key match, status,
  validity/expiry basis, and post-commit read for each required case.
- Separate "one durable order" from Provider invocation evidence; neither one
  substitutes for the other.
- Do not inspect or mutate a real retained database.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-033-summary.md`:

- Korean summary of behavior, acceptance results, risks, evidence limits,
  rollback, and WI-034 trigger.
- Separate UI, API/server, Provider, and durable-state outcomes.
- State clearly that no real Provider/SDK/charge/refund/mail, retained DB,
  deployment, schema, policy, or secret action occurred.

Agent-facing ->
`deliverables/agent/WI-20260809-ATS-033-evidence-pack.md`:

- Root-to-change traceability for `CR-031-083` / `ATS-027-F03`.
- PG and QA-INTEG pre-review decisions and incorporated conditions.
- Red/green TDD evidence, exact patch inventory, focused/adjacent/full command
  results, concurrency method, Provider invocation evidence, durable-state
  evidence, residual risks, and rollback.
- RE independent review decision.
- Explicit follow-up chain to WI-034.

Review artifacts:

- `deliverables/agent/WI-20260809-ATS-033-pg-review.md`
- `deliverables/agent/WI-20260809-ATS-033-qa-integ-review.md`
- `deliverables/agent/WI-20260809-ATS-033-re-review.md`

Handoff Packet ->
`deliverables/agent/WI-20260809-ATS-033-handoff.md`:

- Preserve this packet as the approved execution contract and audit pointer.

[TRACEABILITY REQUIREMENTS]

- Every changed product, test, and documentation file must map to
  `CR-031-083` / `ATS-027-F03` and one acceptance criterion.
- Evidence pointers must include repository-relative file paths, tight line
  references, exact commands, exit codes, test counts, and result limitations.
- Record the canonical bounded-intent fields and the existing lifecycle
  predicate used for reusable, expired, and terminally unusable orders.
- Record transaction/lock order, command-key collision behavior, independent
  transaction setup, synchronization mechanism, and post-commit durable query.
- Record first-create versus reuse Provider invocation counts. Do not infer
  Provider safety from durable row count.
- Distinguish UI behavior, API invocation/server result, Provider boundary, and
  durable persisted state in every conclusion.
- Preserve unrelated worktree changes and do not stage, commit, push, branch,
  merge, delete, deploy, or touch the preserved ZIP as part of this WI unless a
  later explicit instruction separately authorizes Git work.

[ROLLBACK]

- Revert only WI-033 product, test, and verified documentation hunks listed in
  the Evidence Pack; preserve unrelated shared-worktree changes and all
  handoff/review/evidence records as audit history.
- Because no schema change, backfill, retained DB, or real Provider action is
  allowed, rollback requires no migration reversal, data deletion, Provider
  cancellation, charge reversal, or refund.
- Test-managed H2 state and Provider-double state must be isolated and removed
  by test teardown only.
- Existing legacy `payment_orders` rows, including nullable `command_key`
  values, must not be rewritten or deleted for rollback.
- If a schema or larger contract change proves unavoidable, stop before making
  it and escalate; do not create a rollback obligation outside this WI.

[WI CHAIN]

- WI-033 completion immediately triggers WI-20260809-ATS-034.
- Main must run `/create-wi-handoff-packet` for WI-034 and delegate the next
  owner before unrelated closeout work.
- WI-034 remains the sole owner of callback response-loss, unknown-outcome,
  reload, and recovery behavior.
