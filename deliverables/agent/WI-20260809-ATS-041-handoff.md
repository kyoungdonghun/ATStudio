[WI HEADER]
WI ID: WI-20260809-ATS-041
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-035
Blocks: WI-20260809-ATS-056, WI-20260809-ATS-067, WI-20260809-ATS-054

[WI SUMMARY]

Why:

- Close canonical root `CR-031-113` / `WI-029/F-INTEG-029-B03`: a mixed
  valid/invalid Settlement import returns HTTP 200 with durable valid rows and
  row errors, but the current UI presents full success, clears React file
  context, and truncates the correction evidence.
- Close canonical root `CR-031-114` / `WI-029/F-INTEG-029-B04`: the normal UI
  requires an IGNORE note, but direct HTTP and service callers can omit it, and
  a repeated IGNORE can overwrite the original actor, time, and note while
  appending duplicate audit evidence.
- Preserve the existing Settlement CSV, financial-field, reconciliation, and
  duplicate/count contracts. This WI corrects clear integrity defects without
  selecting any held product or evidence-envelope policy.

Scope In:

- Settlement-only portions of
  `frontend/src/pages/admin/PaymentOperationsPage.tsx` and focused frontend
  tests.
- Mixed valid/invalid import-result presentation, retained selected file and
  note, complete returned row-error presentation, exactly one durable
  Settlement-list reload, and coordinated DOM plus React file reset only after
  a fully successful import decision.
- Existing frontend note and typed-confirmation requirements for IGNORE; these
  remain defense in depth and must not be weakened.
- `AdminPaymentController` Settlement IGNORE request validation,
  `AdminPaymentSettlementIgnoreRequest`,
  `AdminPaymentSettlementService.ignoreSettlement`, the bounded
  `PaymentSettlement.ignore` domain mutation, Settlement audit recording, and
  focused controller/service/H2 tests.
- A trimmed, nonblank note of at most 500 characters enforced independently at
  both HTTP DTO and service boundaries before any state or audit mutation.
- Repeated `IGNORED` handling that preserves the first actor, time, normalized
  note, status, and audit row. Prefer a same-decision idempotent no-op when the
  current model can identify the same durable decision without schema,
  dependency, or policy change. Otherwise return an explicit existing-style
  conflict/illegal-transition response with zero mutation and zero new audit.
- Adjacent read/regression coverage for Settlement import, reconciliation,
  list/filter tabs, immutable audit evidence, and parser boundaries.
- Current Settlement operation/audit documentation updates only when needed to
  describe verified implemented behavior. Do not use documentation to decide a
  held policy.
- Mandatory independent PG and QA-INTEG reviews, a Korean user summary, and an
  agent evidence pack.

Scope Out:

- `CR-031-115`, `CR-031-116`, and `CR-031-118`: CSV filename, MIME, bytes,
  encoding, dialect, grammar, headers, row width, canonical financial/provider
  field bounds, reconciliation date/range limits, and row ceilings. These
  remain held for WI-20260809-ATS-067 and the required later decisions.
- `CR-031-117` and `CR-031-119`: atomic/concurrent duplicate handling,
  file-level audit, unusable-row accounting, and aggregate count conservation.
  These remain owned by WI-20260809-ATS-056; preserve current behavior and
  regression coverage only.
- Any new import transaction model, file/batch audit contract, idempotency-key
  schema, database constraint, migration, table/column/index change, parser
  replacement, or reconciliation contract.
- Refund, entitlement-correction, receipt, Incident, subscription, payment
  order, billing agreement, Provider, or shared raw-modal behavior. Preserve
  WI-035 recovery behavior and leave shared modal ownership to WI-054.
- Architecture, dependency, product-policy, API-route, response-shape, or
  external integration changes. Stop and escalate if the bounded correction
  cannot be completed without one.

DoD:

- [ ] A mixed valid/invalid Settlement import returned with HTTP 200 is never
      presented as full success. The result is visibly partial, and all row
      errors supplied by the current response are available for correction.
- [ ] The partial result preserves the exact selected `File`, the DOM file
      input context, and the operator note. It performs exactly one durable
      Settlement-list reload and does not submit a second import.
- [ ] A fully successful import (`failedRows == 0`) performs exactly one list
      reload and then clears both React selected-file state and the DOM file
      input. Transport/server failure preserves correction context and does not
      claim durable success.
- [ ] Import result handling does not change parser rules, duplicate semantics,
      response fields, or aggregate conservation definitions owned by WI-056
      and WI-067.
- [ ] Missing, null, empty, whitespace-only, or trimmed-over-500 IGNORE notes
      are rejected at HTTP validation with no service invocation or mutation.
- [ ] Direct service calls independently reject a null request, null/blank
      note, and trimmed-over-500 note before entity or audit mutation.
- [ ] A valid IGNORE persists and audits one normalized trimmed note while the
      existing frontend still requires the note and typed confirmation.
- [ ] Repeated IGNORE calls cannot overwrite the original ignored actor,
      timestamp, note, or status and cannot duplicate the original audit row.
- [ ] Same-decision idempotent no-op is used only if sameness is reliably
      derivable from current durable fields and authenticated actor context.
      Otherwise repeated IGNORE is an explicit conflict with zero mutation and
      zero audit. No schema or product-policy choice is invented.
- [ ] Import, reconcile, list/filter, and audit regressions pass; no Settlement
      action changes payment, refund, subscription, billing, or Provider state.
- [ ] PG and QA-INTEG independently approve the final bounded diff and record
      the note-validation, authorization, immutable-evidence, no-external-
      effect, and cross-layer results.
- [ ] `Q-ALL` from the approved portfolio passes with exact evidence, current
      documentation is aligned where necessary, and both required closeout
      deliverables are complete.

Constraints / Forbidden:

- Use H2, test doubles/Test Provider, and safe synthetic CSV only. Expected
  Provider and other real external-effect invocation count is zero.
- Do not run a production/staging import, use real provider exports, call Toss
  or another external service, send mail, deploy, or create retained external
  effects.
- Do not inspect, print, copy, or log secrets, credentials, tokens, raw Provider
  identifiers/payloads, ignored local configuration, unrelated PII, or real
  financial evidence.
- Do not access, list, open, read, hash, extract, copy, or modify `output/` or
  any intentional untracked ZIP artifact.
- Do not delete files, alter schema, destroy or clean database data, add a
  dependency, change architecture, or select product policy.
- Do not stage, commit, push, branch, merge, rebase, tag, stash, or perform any
  other Git mutation. Read-only diff/status commands required for evidence are
  allowed.
- Preserve all unrelated shared-worktree changes. Edit only the Settlement
  blocks and focused tests/docs needed for `CR-031-113` and `CR-031-114`.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] A focused frontend test supplies one mixed result with at least one
      imported row and at least one failed row, then proves partial feedback,
      the complete returned error list, retained file/note/DOM context, one
      import call, and exactly one Settlement-list reload.
- [ ] The mixed-result test does not claim new all-row count conservation or
      change duplicate classification; it asserts only the known synthetic
      valid/invalid rows under the current response contract.
- [ ] A fully successful import test proves full-success feedback, one reload,
      React file state cleared, and the DOM file input cleared so reselecting
      the same file is reliable.
- [ ] Import rejection preserves selected file, DOM input, and note, shows
      failure rather than success/partial completion, and performs no success
      reload.
- [ ] All row errors in the current result are rendered without the existing
      five-row truncation and without another API request or polling loop.
- [ ] Reconciliation result and Settlement list/filter behavior retain their
      current ownership and do not accidentally clear import correction
      context or cross-write another payment tab.
- [ ] Valid IGNORE input is trimmed once, sent once after the existing typed
      confirmation, stored once, and audited once.
- [ ] HTTP tests reject `{}`, `{"note":null}`, empty, whitespace-only, and
      trimmed-over-500 notes with a client error and zero service calls; a
      padded valid note reaches the service under the normalized contract.
- [ ] Service tests reject null request/null note/blank/trimmed-over-500 input
      with zero entity mutation and zero audit calls, independent of controller
      validation.
- [ ] Focused H2 evidence proves the first valid IGNORE actor, time, normalized
      note, status, and one audit row remain unchanged after repeated same and
      conflicting requests.
- [ ] The selected repeated-call path is explicit: supported same-decision
      no-op returns the existing durable result with no write/audit, or every
      repeated call returns an existing-style conflict with no write/audit.

Security / Integration:

- [ ] All Settlement routes remain ADMIN-only; direct non-ADMIN access is
      rejected before controller/service mutation.
- [ ] UI feedback, frontend invocation, HTTP validation/status, service
      decision, entity state, audit count/content, and list reload are evidenced
      as separate lanes for mixed import and IGNORE retry scenarios.
- [ ] Audit evidence retains the first authenticated actor and exact decision
      time and stores only the bounded normalized note allowed by the current
      contract. It contains no raw Provider payload or secret.
- [ ] PG reviews fail-closed validation, authorization, actor ownership,
      repeated-call integrity, audit minimization, and secret/PII boundaries.
- [ ] QA-INTEG reviews response mapping, call counts, durable H2 state, audit
      counts, one reload, and preservation of import/reconcile/list behavior.
- [ ] Payment, refund, subscription, billing-agreement, and Provider mutation
      counts remain zero for IGNORE and import correction tests except the
      intended local Settlement and audit writes from the first valid action.

Performance:

- [ ] One import decision makes at most one import request and one post-result
      Settlement-list reload. No polling, automatic retry, or unbounded network
      loop is added.
- [ ] Complete returned row-error presentation adds no extra server call and no
      new parser/file ceiling. Any need for a new bound is escalated to WI-067.
- [ ] Repeated IGNORE requests add no duplicate state or audit writes, and
      pending frontend controls continue to fence rapid confirmation clicks.

Quality:

- [ ] Focused frontend tests cover mixed HTTP 200, complete errors, retained
      correction context, full-success DOM/React reset, transport failure, one
      reload, and existing note/typed confirmation.
- [ ] Focused controller tests cover HTTP validation and authorization; focused
      service/entity/audit tests cover independent validation, normalization,
      first-decision immutability, and repeated-call behavior.
- [ ] Focused H2 integration evidence verifies durable Settlement fields and
      exact audit row counts using only synthetic records.
- [ ] Adjacent Settlement import/reconcile/list/API contract tests pass without
      changing held parser, bounds, atomicity, or count-conservation behavior.
- [ ] Full backend/frontend suites, coverage review, typecheck, ESLint,
      Prettier, backend/frontend builds, docs validation, and
      `git diff --check` pass as required by `Q-ALL`.
- [ ] PG and QA-INTEG final review artifacts, the user summary, and the agent
      evidence pack contain reproducible pointers and explicit limitations.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policies - Inferred):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (Frontend / Settlement Domain):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/payment-operations-runbook.md`
- `docs/design/payment-refund-receipt-settlement-policy.md`
- `docs/payment/admin-operations-guide.md`

REQ / Portfolio / Dependency Evidence:

- `deliverables/user/REQ-20260809-ATS-001.md` (approved parent request,
  execution strategy, and quality gates)
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:677-680`
  (`CR-031-113` through `CR-031-116` dispositions and held boundaries)
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:957-970`
  (Phase 1 portfolio and preassigned WI-041 row)
- `deliverables/agent/WI-20260809-ATS-029-findings.md:302-326`
  (`F-INTEG-029-B03`)
- `deliverables/agent/WI-20260809-ATS-029-findings.md:328-352`
  (`F-INTEG-029-B04`)
- `deliverables/agent/WI-20260809-ATS-035-handoff.md`
- `deliverables/agent/WI-20260809-ATS-035-evidence-pack.md`

Primary Frontend Symbols / Tests:

- `frontend/src/pages/admin/PaymentOperationsPage.tsx`
  (`settlementFile`, `settlementNote`, `settlementImportResult`,
  `importSettlementFile`, `ignoreSettlement`, `SettlementOperationPanel`, and
  `SettlementTable`; Settlement blocks only)
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx` (Settlement import,
  reload ownership, file input, and mixed-result tests)
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx`
  (existing Settlement list/filter and IGNORE note/confirmation regressions)
- `frontend/src/api/admin.ts` (`AdminPaymentSettlementImportResult`,
  `importAdminPaymentSettlements`, `fetchAdminPaymentSettlements`,
  `reconcileAdminPaymentSettlements`, and `ignoreAdminPaymentSettlement`;
  contract/regression scope unless a proven bounded correction is required)
- `frontend/src/api/adminContracts.test.ts` (Settlement request-shape
  regression)

Primary Backend Symbols / Tests:

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java`
  (`listSettlements`, `importSettlements`, `reconcileSettlements`, and
  `ignoreSettlement`)
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementIgnoreRequest.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementImportResponse.java`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementImportErrorResponse.java`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java`
  (`importSettlements`, `reconcileMissingProviderSettlements`,
  `ignoreSettlement`, and parser boundaries)
- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java`
  (`ignore`, `ignoredBy`, `ignoredAt`, `operatorNote`, and `status`)
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java`
  (`recordPaymentSettlementEvent`)
- `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java`
  (`findWithGraphById`; read/locking behavior only, with no schema change)
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java`
- Focused H2 Settlement/audit integration test under `src/test/java/` if the
  existing focused tests cannot prove durable first-decision immutability.

Repro / Quality Gates:

- Start with focused tests for changed Settlement symbols and record the exact
  command, exit code, test count, and limitation in the Evidence Pack.
- Backend: `gradlew.bat test`,
  `gradlew.bat jacocoTestReport jacocoTestCoverageVerification`, and
  `gradlew.bat build`.
- From `frontend/`: `npm test`, `npm run test:coverage`,
  `npm run typecheck`, `npm run lint`, `npm run format`, and
  `npm run build`.
- `python .agents/skills/validate-docs/scripts/validate_docs.py`
- `git diff --check`

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-041-summary.md`:

- Korean summary of mixed-import partial handling, retained correction context,
  server-side IGNORE validation/retry semantics, reviewer decisions, quality
  results, risks, and the explicitly held WI-056/WI-067 boundaries.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-041-evidence-pack.md`:

- Exact patch pointers; root-to-criterion map; mixed import UI/HTTP/durable/
  reload matrix; IGNORE HTTP/service/entity/audit decision table; before/after
  actor/time/note/status and audit counts; invocation counts; focused/adjacent/
  full commands, exits, test counts, coverage, documentation changes,
  limitations, reviewer verdicts, rollback, and follow-up WI chain.

Independent review artifacts:

- `deliverables/agent/WI-20260809-ATS-041-pg-review.md`
- `deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md`

Handoff Packet -> `deliverables/agent/WI-20260809-ATS-041-handoff.md`:

- Preserve this packet as the approved execution contract and traceability
  pointer.

[TRACEABILITY REQUIREMENTS]

- Map every changed product, test, and documentation hunk to exactly
  `CR-031-113` / `F-INTEG-029-B03` or `CR-031-114` /
  `F-INTEG-029-B04` and at least one acceptance criterion.
- For mixed import, record the presented outcome, file React state, DOM input
  state, note state, number of returned/rendered errors, import request count,
  list reload count, and intended local durable/audit results separately.
- For IGNORE, record HTTP status, service decision, entity fields before/after,
  audit rows before/after, and external/Provider call count for first, repeated
  same-decision, repeated conflicting-decision, null, blank, and over-limit
  cases.
- State why current architecture supports a same-decision no-op, if selected.
  If it cannot establish sameness without a held decision or structural change,
  record the explicit conflict path and escalate rather than invent policy.
- Record that `CR-031-115`, `CR-031-116`, `CR-031-118`, `CR-031-117`, and
  `CR-031-119` were regression-preserved but not implemented or claimed closed.
- Evidence pointers require repository-relative paths, tight line references,
  exact commands, exit codes, test counts, and explicit limitations.
- PG and QA-INTEG reviews must be independent and must cover the final bounded
  diff, not only the proposed design.
- Record that only H2/Test Provider/test doubles and safe synthetic CSV were
  used and that no production import, external effect, secret/config access,
  output/ZIP access, destructive action, or Git mutation occurred.

[ROLLBACK]

- Revert only WI-041 product, test, and verified documentation hunks listed in
  its Evidence Pack. Preserve WI-035 and all unrelated shared-worktree changes.
- Roll back DTO/service/entity/audit repeated-IGNORE behavior and its focused
  tests as one unit so boundary validation and durable semantics cannot diverge.
- Roll back mixed-import UI state/reset behavior and its tests as one unit; do
  not alter parser, response, duplicate, or count contracts during rollback.
- No schema rollback, data deletion, audit-row cleanup, Provider reversal,
  production import compensation, or deployment rollback is permitted or
  required by this WI.

[WI CHAIN]

- WI-041 completion directly unblocks WI-20260809-ATS-056, the next Settlement
  implementation owner for `CR-031-117` and `CR-031-119`. Main must create its
  handoff with `/create-wi-handoff-packet` and delegate it before unrelated
  closeout work.
- WI-20260809-ATS-067 remains blocked until the required WI-041 and WI-056
  evidence is available; it owns the held `CR-031-115`, `CR-031-116`, and
  `CR-031-118` decisions and must not be pulled into this implementation.
- WI-20260809-ATS-054 is also unblocked by WI-041 only after its other approved
  prerequisite, WI-20260809-ATS-053, is complete. It owns shared ADMIN modal and
  typed local-correction execution behavior, not Settlement integrity.
- Preserve all later regression and final-audit gates from the approved REQ and
  consolidated portfolio; do not collapse their ownership into WI-041.
