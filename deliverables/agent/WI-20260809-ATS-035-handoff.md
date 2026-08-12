[WI HEADER]
WI ID: WI-20260809-ATS-035
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-034
Blocks: WI-20260809-ATS-041, WI-20260809-ATS-054, WI-20260809-ATS-064, WI-20260809-ATS-074 -> WI-20260809-ATS-075 -> WI-20260809-ATS-076..079 -> WI-20260809-ATS-080

[WI SUMMARY]

Why:

- Close canonical root `CR-031-092` / `WI-028/F-01`: ADMIN refund and
  entitlement-correction execute paths currently report any rejected response
  as a simple failure even when the durable operation may already have
  committed.
- Recover an ambiguous execute through an authoritative ADMIN detail read
  before any retry, using the existing refund and entitlement-correction
  ledgers and their audit evidence.
- Reuse WI-034's `COMMITTED`, `FAILED`, `RELOAD_FAILED`, and `UNKNOWN`
  vocabulary while preserving separate ADMIN intents, identifiers, domain
  statuses, and UI state for Provider refund and local entitlement correction.

Frozen shared outcome vocabulary and ADMIN mapping:

- `COMMITTED`: an authoritative detail read proves the exact refund or
  correction intent is `SUCCEEDED`. For refund, this means the persisted
  Provider-refund ledger result; for correction, this means the persisted local
  entitlement-correction result. Never infer one intent from the other.
- `FAILED`: an authoritative execute response or detail read proves the exact
  intent is terminal `FAILED` or `CANCELLED`. Transport loss, no response, 5xx,
  or a failed reload is not proof of `FAILED`.
- `RELOAD_FAILED`: execute returned an authoritative terminal success, but the
  required detail, audit, tab, or list refresh failed. Preserve the successful
  intent and status; do not relabel the mutation as failed.
- `UNKNOWN`: execute may have reached the server or Provider, but terminal
  success or failure cannot be proved. `PROCESSING` and refund
  `PENDING_PROVIDER_CONFIRMATION` are in-flight `UNKNOWN`; a failed or stale
  recovery read also remains `UNKNOWN`.
- `REQUESTED` and `APPROVED` remain their exact pre-execution domain statuses.
  After ambiguous execute delivery they do not authorize automatic replay.
  Any later explicit execute requires a fresh authoritative read, current
  eligibility, the existing typed confirmation, and a new operator action.

Scope In:

- `PaymentOperationsPage.tsx` refund and entitlement-correction execute,
  recovery, feedback, pending, and reload behavior only.
- Separate operation state keyed by domain and durable ID so refund and
  correction intents, notes, statuses, errors, and stale responses cannot
  overwrite each other.
- Frontend detail wrappers for the existing ADMIN reads:
  `GET /api/admin/payments/refunds/{refundId}` and
  `GET /api/admin/payments/entitlement-corrections/{correctionId}`.
- A bounded detail read after execute network/no-response/5xx failure, plus a
  read-only `status again` action for `UNKNOWN` and `RELOAD_FAILED`.
- Existing operation-audit and list reads as supporting evidence and screen
  refresh; the exact detail status remains the primary recovery authority.
- Minimal request/approve/preview adjustments only where necessary to prove
  execute response-loss behavior, retain exact intent ownership, or block a
  conflicting mutation.
- Focused frontend and H2/Test-Provider backend tests, verified payment/API/UI
  documentation, PG/QA-INTEG/RE independent review, and two-set deliverables.

Scope Out:

- Refund policy, amount, eligibility, reason policy, Provider selection,
  correction policy, entitlement targets, billing-cycle policy, or approval
  workflow changes.
- A new endpoint, response schema, persistence schema, migration, command key,
  polling system, background reconciliation path, or dependency without
  concrete evidence that the existing detail/audit contracts cannot safely
  satisfy this WI. Stop and escalate before such a change.
- Settlement behavior in the shared page, owned by WI-041, and shared ADMIN
  modal ownership/typed correction confirmation, owned by WI-054.
- Automatic or interceptor-driven mutation retry, Provider status mutation,
  live refund, real Toss access, mail, retained database action, deployment,
  production data, or secret/config inspection.

DoD:

- [ ] Refund and correction execute each retain a distinct immutable intent
      containing domain, durable ID, operation generation, and current outcome.
- [ ] Network/no-response/5xx execute ambiguity performs one bounded existing
      detail GET and maps the fresh durable status without repeating execute.
- [ ] Refund and correction each prove committed response loss separately:
      `SUCCEEDED` converges to `COMMITTED`; `FAILED`/`CANCELLED` converges to
      `FAILED`; in-flight or unreadable state remains `UNKNOWN`.
- [ ] Execute success followed by detail/audit/tab/list refresh failure remains
      `RELOAD_FAILED`, preserves success context, and exposes read-only status
      recovery.
- [ ] While an intent is `UNKNOWN` or `RELOAD_FAILED`, the same execute and
      every mutation that conflicts with that durable refund/correction target
      are blocked; status recovery performs reads only.
- [ ] Provider refund automatic mutation retry count is zero. Entitlement
      correction automatic mutation retry count is zero.
- [ ] Rapid/repeated clicks are fenced, and stale execute/detail/list responses
      cannot overwrite a newer authoritative result or another intent.
- [ ] Backend detail and audit responses are independently reviewed by PG and
      QA-INTEG for ADMIN authorization, minimum necessary fields, safe support
      references, sanitized errors, and absence of secrets or raw Provider
      payloads. Any material contract gap is escalated before endpoint/schema
      expansion.
- [ ] Focused, adjacent, full, coverage, typecheck, lint, format, build,
      documentation, and diff gates pass with exact evidence.
- [ ] PG, QA-INTEG, and RE independently approve the bounded final diff; current
      docs and both user-facing and agent-facing deliverables are complete.
- [ ] Main immediately triggers WI-041 after WI-035 closeout and records the
      remaining WI-054/WI-064/final-audit dependencies without skipping them.

Constraints / Forbidden:

- Use the existing detail GETs first. Do not create an endpoint or schema
  because a wrapper or caller is currently absent.
- Keep Provider refund and local entitlement correction as separate intents
  and state machines. Never treat correction success as refund success, or a
  refund audit row as proof of correction completion.
- Do not infer `COMMITTED` from a toast, HTTP 2xx alone, stale list row, modal
  close, missing error, or another tab's successful load.
- Do not infer `FAILED` from Axios rejection, timeout, no response, 5xx, detail
  read failure, audit/list reload failure, or `PROCESSING`/
  `PENDING_PROVIDER_CONFIRMATION`.
- Do not automatically repeat refund execute or correction execute from catch,
  effect, interceptor, reload helper, polling loop, or status-recovery action.
- Do not broaden approve/request/preview or alter policy merely to simplify
  tests. Keep changes to the minimum needed for execute response-loss proof.
- Do not expose or log raw Provider payloads, payment keys, refund transaction
  IDs beyond the established safe support-reference contract, idempotency
  secrets, credentials, tokens, email addresses beyond approved ADMIN need, or
  unrelated PII.
- Use Test Provider and synthetic H2 data only. Do not invoke real Toss,
  refund, cancellation, mail, retained DB, deployment, or production services.
- Preserve all shared-worktree changes. Do not stage, commit, push, branch,
  merge, delete, inspect ignored secrets, or open/read/hash/touch ZIP or output
  artifacts, including `output/client-demo-screenshots-20260716-140514.zip`.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] Refund execute response loss + detail `SUCCEEDED` -> `COMMITTED`, one
      success result, zero additional execute calls, and exact refund intent
      retained.
- [ ] Correction execute response loss + detail `SUCCEEDED` -> `COMMITTED`, one
      success result, zero additional execute calls, and exact correction
      intent retained independently from refund state.
- [ ] Refund/correction detail `FAILED` or `CANCELLED` -> authoritative
      `FAILED`; `PROCESSING` and refund `PENDING_PROVIDER_CONFIRMATION` ->
      in-flight `UNKNOWN` with read-only `status again`.
- [ ] Execute response success + any required detail/audit/tab/list refresh
      failure -> `RELOAD_FAILED`, with the successful result and immutable
      target preserved.
- [ ] Detail read failure after response loss -> `UNKNOWN`; retrying status
      performs GET/read calls only and cannot invoke approve or execute.
- [ ] Fresh `REQUESTED`/`APPROVED` remains visibly distinct from terminal and
      in-flight states; any later explicit execute preserves current eligibility
      and typed-confirmation controls and is never automatic.
- [ ] Rapid double click, repeated status click, tab switch, page reload, and
      old/new deferred response tests prove one mutation owner and latest valid
      read ownership per intent.
- [ ] A refund recovery result cannot alter correction feedback/controls, and a
      correction recovery result cannot alter refund feedback/controls.
- [ ] All payment tabs retain their own latest load result; an unrelated tab or
      list failure cannot downgrade a proven execute outcome.

Security / Integration:

- [ ] Existing detail GETs remain ADMIN-only and return the exact requested
      durable record; absent/invalid IDs use the current safe error contract.
- [ ] PG and QA-INTEG record whether detail/audit DTOs are minimum, safe, and
      permission-appropriate for this recovery UI, including actor PII,
      idempotency/support references, and failure fields.
- [ ] UI -> execute request -> server/Test Provider result -> refund/correction
      durable status -> detail/audit/list reload is evidenced as separate lanes
      for every response-loss scenario.
- [ ] Provider refund mutation retries: `0`. Entitlement correction mutation
      retries: `0`. Recovery GETs perform no mutation and no Provider call.
- [ ] Only synthetic Test-Provider/H2 fixtures are used; no live side effect or
      retained external state is created.

Performance:

- [ ] No polling or unbounded retry is added. One ambiguous execute causes at
      most one immediate bounded detail read; further checks require an
      explicit read-only operator action.
- [ ] Repeated recovery clicks are disabled, deduplicated, or generation-fenced
      while a current detail read is pending.

Quality:

- [ ] Focused frontend tests cover committed response loss for refund and
      correction separately, terminal/in-flight/read failure, rapid/repeated
      clicks, tab/list reload failure, and stale responses.
- [ ] Focused backend controller/service/integration tests prove detail/audit
      authorization, response mapping, H2 durability, and zero Provider calls
      from recovery reads.
- [ ] Full backend tests, JaCoCo review/gates, and backend build pass.
- [ ] Full frontend tests/coverage, typecheck, ESLint, Prettier, and build pass.
- [ ] Verified payment operations runbook, API, payment integration, ADMIN
      operations, and screen-flow docs describe the implemented behavior only.
- [ ] Documentation validator and `git diff --check` pass.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policies - Inferred):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`
- `docs/standards/evidence-pack-standard.md` (explicit two-set evidence-contract
  exception for this ATS WI)

Tier 2 (Frontend / Domain):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/payment-integration-design.md`
- `docs/design/payment-operations-runbook.md`
- `docs/payment/admin-operations-guide.md`
- `docs/ui/screen-flow.md`

REQ / Audit / Dependency Evidence:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-028-findings.md` (`F-01`)
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md`
  (`CR-031-092`, WI-035 row, dependency and final-audit chain)
- `deliverables/agent/WI-20260809-ATS-034-handoff.md`
- `deliverables/agent/WI-20260809-ATS-034-evidence-pack.md`

Primary Product / Contract Pointers:

- `frontend/src/pages/admin/PaymentOperationsPage.tsx`
- `frontend/src/api/admin.ts`
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentRefundResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentEntitlementCorrectionResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentOperationAuditLogResponse.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentRefundStatus.java`
- `src/main/java/com/atstudio/atstudio/entity/enums/PaymentEntitlementCorrectionStatus.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java`

Primary Test Pointers:

- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx`
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/PaymentRefundResilienceIntegrationTest.java`

Repro / Quality Gates:

- Focused commands selected from the files changed, with exact command, exit
  code, test count, and limitation recorded in the Evidence Pack.
- `gradlew.bat test`
- `gradlew.bat jacocoTestReport jacocoTestCoverageVerification`
- `gradlew.bat build`
- From `frontend/`: `npm test`, `npm run test:coverage`,
  `npm run typecheck`, `npm run lint`, `npm run format`, `npm run build`.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-035-summary.md`:

- Korean summary of refund and correction recovery behavior, four-state ADMIN
  mapping, operator controls, verification, reviewer decisions, risks, and
  residual acceptance limitations.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-035-evidence-pack.md`:

- Exact patch pointers; separate refund/correction intent and state tables;
  UI/API/server/Provider/durable/audit/reload lane evidence; mutation and read
  invocation counts; focused/full commands, exit codes, test counts, coverage,
  limitations, reviewer decisions, documentation changes, rollback, and
  follow-up WI.

Independent review artifacts:

- `deliverables/agent/WI-20260809-ATS-035-pg-review.md`
- `deliverables/agent/WI-20260809-ATS-035-qa-integ-review.md`
- `deliverables/agent/WI-20260809-ATS-035-re-review.md`

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-035-handoff.md`:

- Preserve this packet as the approved execution contract and traceability
  pointer.

[TRACEABILITY REQUIREMENTS]

- Map every changed product, test, and documentation file to `CR-031-092` /
  `WI-028/F-01` and at least one acceptance criterion.
- Record the exact domain-status predicate for each shared outcome separately:
  refund `COMMITTED`/`FAILED`/`RELOAD_FAILED`/`UNKNOWN`, and correction
  `COMMITTED`/`FAILED`/`RELOAD_FAILED`/`UNKNOWN`.
- For every scenario, report UI copy/control, frontend execute count, frontend
  detail/audit/list read counts, backend result, Provider invocation count,
  durable H2 state, and final reload state as separate evidence lanes.
- Record automatic Provider refund execute retries as zero and automatic
  entitlement-correction execute retries as zero.
- Evidence pointers require repository-relative paths, tight line references,
  exact commands, exit codes, test counts, and explicit limitations.
- PG/QA-INTEG reviews must address ADMIN authorization and every sensitive or
  operational detail/audit response field; RE must independently verify
  response-loss, race, reload, and no-duplicate-mutation assertions.
- Preserve unrelated shared-worktree changes and record that no stage, commit,
  push, branch, merge, deploy, destructive action, ignored-secret access, ZIP,
  or output-artifact access occurred.

[ROLLBACK]

- Revert only WI-035 product, test, and verified documentation hunks listed in
  its Evidence Pack. Preserve WI-032 through WI-034 and all unrelated
  shared-worktree changes.
- Remove added frontend detail wrappers only together with their callers and
  focused tests. Do not remove or alter the existing backend detail GETs,
  durable refund/correction ledgers, audit rows, idempotency controls, typed
  confirmations, or Provider safety fences.
- No Provider reversal, retained-data cleanup, schema rollback, or deployment
  rollback is permitted or required by this WI.

[WI CHAIN]

- WI-035 completion immediately triggers WI-20260809-ATS-041 because WI-041
  next owns only Settlement blocks in the shared `PaymentOperationsPage.tsx`.
- Main must run `/create-wi-handoff-packet` for WI-041 and delegate its owner
  before unrelated closeout work. Do not create that packet inside WI-035.
- WI-035 is also a prerequisite for WI-054 and WI-064. WI-054 still waits for
  WI-041 and WI-053; WI-064 still follows the WI-033 through WI-035 payment
  evidence. Preserve those additional dependencies exactly.
- The applicable completion feeds WI-074 focused/adjacent regression, then
  WI-075 full gates, WI-076 through WI-079 final evidence lanes, and WI-080
  final integrated audit. Do not skip or collapse this final audit chain.
