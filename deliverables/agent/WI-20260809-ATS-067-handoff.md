[WI HEADER]
WI ID: WI-20260809-ATS-067
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-056
Blocks: -
Execution State: DECISION-HELD

[WI SUMMARY]

Approval Status:

- Parent REQ `REQ-20260809-ATS-001` is approved.
- The policy and API/test decisions listed in this packet are not approved.
- Implementation remains `HELD`. No code, schema, API, test, tooling, or
  general-document change may begin until every applicable decision has exact
  user-approval evidence.

Why:

- `CR-031-115` / `F-INTEG-029-B05` found that lenient decoding and CSV grammar
  can silently alter financial evidence.
- `CR-031-116` / `F-INTEG-029-B06` found that Settlement financial and Provider
  fields lack canonical bounds matching their durable representation.
- `CR-031-118` / `F-INTEG-029-B08` found that reconciliation has no approved
  date-span or row ceiling and no bounded batch/retry contract.
- WI-056 intentionally deferred these policy choices to WI-067 while closing
  atomic duplicate handling, durable import-attempt recovery, and aggregate
  count conservation.

Scope (in):

- Phase A, decision only: produce a concrete decision register for every gate
  in `[DECISION GATES]`, including alternatives, security/integration impact,
  backward-compatibility impact, test consequences, and a recommended option.
  Obtain explicit user approval without answering a policy question on the
  user's behalf.
- Phase B, implementation only after Phase A approval: implement exactly the
  approved file envelope, decoder, CSV grammar, field bounds, financial/date
  validation, import/reconciliation ceilings, and approved execution/evidence
  behavior.
- Add focused backend and frontend contract tests, independent PG and QA-INTEG
  review, current English documentation updates, a Korean user summary, and an
  agent Evidence Pack only after the implementation gate opens.
- Treat the operator-note transport choice and disposable MySQL rehearsal as
  separate gates. Approval of parser policy does not approve either gate.

Scope (out):

- Any implementation, general-document update, test fixture change, dependency
  addition, or API/schema/tooling change before recorded user approval.
- Replacing or weakening the WI-056 attempt ledger, same-key no-reprocess rule,
  header-only owner-scoped recovery, global ADMIN audit list/numeric detail,
  exact duplicate-constraint classification, or count-conservation contract.
- Extending the CSV attempt ledger or Idempotency-Key contract to reconciliation
  unless the user explicitly approves that API/schema design in DG-067-07.
- Retained-data migration/backfill, production/staging import, real Toss or
  Provider calls, real payment/refund/subscription/billing-agreement mutation,
  mail, deployment, or production data.
- Any access to ignored secrets, ignored local configuration, `output/`, or an
  intentional ZIP artifact; any Git mutation; any unrelated file change.

DoD:

- [ ] Every applicable decision gate has one explicit approved disposition,
      exact values or rules, approval date/source, and named affected layers.
- [ ] PG and QA-INTEG review the proposed security, evidence, parser, API,
      persistence, batching, retry, and operational consequences before Phase B.
- [ ] No Phase B edit predates the recorded user approval.
- [ ] Approved validation fails closed before altered evidence can be persisted
      or audited and returns stable, bounded, non-sensitive errors.
- [ ] WI-056 invariants and no-external-effect boundaries remain proven.
- [ ] Focused, adjacent-regression, full quality, documentation, and approved
      MySQL gates pass; both closeout deliverables are complete.

[DECISION GATES]

Gate rule:

- `UNANSWERED` means `HELD`; there is no inferred default.
- Approval must select exact values and behaviors, not only say "harden CSV".
- If a selected option needs a new dependency, schema, or architectural
  component, stop for the repository's separate library/architecture approval.

DG-067-01 - Filename, Extension, MIME, and Byte Envelope:

- Decide accepted filename shape, filename length, extension list and case
  handling, including missing or suspicious original filenames.
- Decide accepted multipart content types and behavior for missing, generic, or
  conflicting MIME values. Browser `accept` remains advisory only.
- Decide one server-side byte ceiling, where it is enforced, and whether an
  over-limit/nonconforming nonempty request is rejected before attempt claim or
  recorded as a claimed `FAILED` attempt.
- No filename, extension, MIME, or byte value is approved by this handoff.

DG-067-02 - Encoding, BOM, and Malformed Bytes:

- Decide the accepted encoding set, whether UTF-8 BOM is accepted/removed, and
  whether BOM-less input is required or merely allowed.
- Decide strict malformed/unmappable-byte behavior and the file-level error
  contract. Silent replacement characters are not an implicit policy.
- Decide whether any legacy encoding is accepted and, if so, how it is
  unambiguously selected without heuristic evidence conversion.

DG-067-03 - CSV Dialect, Quoting, Newlines, and Header/Width Rules:

- Decide delimiter, quote and escape rules, embedded comma and quoted-newline
  support, accepted CRLF/LF forms, trailing newline, blank-line behavior, and
  malformed/unbalanced-quote rejection.
- Decide header case/whitespace normalization, required-header matching,
  duplicate-header rejection, header uniqueness after normalization, unknown
  header acceptance/rejection, and deterministic header order requirements.
- Decide exact row-width behavior for missing, extra, and trailing empty cells.
- Do not select a parser library or silently claim RFC conformance without an
  approved dialect and executable conformance tests.

DG-067-04 - Canonical Provider, Order, and Payment Identifiers:

- Decide accepted Provider values/case and canonicalization.
- Decide maximum and minimum lengths plus allowed characters for `order_id`,
  `provider_payment_key`, `provider_settlement_id`, Provider status, and every
  other identifier included in persisted evidence or the deduplication basis.
- Decide trim/case/Unicode/control-character behavior and reject-versus-normalize
  semantics. Silent truncation must not remain an accidental identity policy.
- Ensure the validated identity, deduplication identity, persisted identity,
  ADMIN-visible identity, and audit identity cannot diverge.

DG-067-05 - Money, Currency, and Settlement Dates:

- Decide accepted decimal notation, grouping separators, sign, exponent use,
  precision, scale, maximum value, and reject-versus-round behavior for every
  amount. The durable `DECIMAL(15,2)` shape is evidence, not approval to round.
- Decide a currency allowlist and case normalization; "any three characters" is
  not an approved currency policy.
- Decide date format, settlement-base/payout ordering, future-date handling,
  oldest accepted date, and any per-row date-range relationship.
- Validation must occur before reconciliation, deduplication, persistence, and
  audit use of a value.

DG-067-06 - Import Rows and Reconciliation Date/Row Ceilings:

- Decide the maximum CSV data-row count and whether header, blank, malformed,
  duplicate, and rejected rows count toward it. The current 1,000 nonblank-row
  guard is an observed implementation value, not an approved policy.
- Decide reconciliation default date behavior, inclusive boundaries, maximum
  span, oldest/future boundary, maximum selected rows, and over-limit response.
  The current omitted-date default of 30 days is also an observation.
- Define deterministic boundary tests for at-limit and one-over-limit cases.

DG-067-07 - Batching, Cursor, Progress, Retry, and Error Retention:

- Decide stable ordering and cursor shape, batch size, transaction boundary,
  concurrency rule, progress states, interruption behavior, and retry semantics
  for bounded reconciliation and any batched import behavior.
- Decide whether reconciliation gains an operation identity, durable progress,
  read recovery, or a new ledger/API. WI-056 approved none of those; any such
  addition requires explicit API/schema approval and must remain distinct from
  the CSV import-attempt ledger unless expressly approved.
- Decide maximum returned error-detail count, truncation/summary signal, and
  which details are response-only versus durably retained. Preserve the current
  prohibition on raw CSV rows, file bytes, Provider payloads, credentials, and
  secrets unless a later separately approved policy says otherwise.
- Retry policy must distinguish definite rejection, durable completion,
  in-progress work, and unknown transport outcome without duplicate mutation.

DG-067-08 - Operator Note Transport and Query Redaction (Separate API Gate):

- Current evidence: the SPA sends `note` through Axios `params`, and the backend
  accepts it with `@RequestParam`; request-target/query logging can therefore
  capture optional free text even though application logging omits it.
- Decide whether WI-067 changes the note to an approved multipart-body/request
  contract or leaves the API unchanged and treats request-target/query
  omission/redaction as an operator infrastructure requirement.
- Define compatibility, frontend/backend tests, OpenAPI/docs updates, and
  infrastructure verification for the chosen disposition.
- Do not change note transport, its 500-character bound, ADMIN visibility, or
  free-text/DLP claims without explicit user approval of this gate. Deferring it
  to a separate API WI is an acceptable disposition only when explicitly chosen.

DG-067-09 - Disposable MySQL Baseline and Rehearsal (Separate Destructive/Test Gate):

- Current source is 42 tables/42 JPA entities. `DisposableMysqlBootstrap` still
  enforces the predecessor 41-table manifest and predecessor hash; neither is
  current proof.
- Decision A: separately approve or reject code/tooling work that replaces the
  predecessor expectations with values derived from the current 42-table
  source. Do not invent current column/index/foreign-key counts or a hash.
- Decision B: separately approve or reject a real fresh-MySQL manifest/hash and
  transaction/concurrency rehearsal. Approval A does not imply approval B.
- Before Decision B execution, report the exact uniquely named disposable
  database, verified-empty/non-production server, create/apply/validate/drop
  steps, expected residual state, secret-safe command method, and rollback.
  Obtain explicit destructive/test approval immediately before execution.
- Never use a retained, acceptance, staging, or production database. Never use
  real payment/Provider data. Do not print connection secrets. If approved,
  record the actual manifest/hash and distinguish it from H2 and synthetic
  driver-exception evidence.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] A user-approved decision matrix resolves DG-067-01 through DG-067-07 and
      separately records DG-067-08 and both approvals in DG-067-09.
- [ ] Strict decoder/parser tests cover accepted BOM/input, malformed bytes,
      quoting, embedded newlines, escaped quotes, blank lines, duplicate and
      unknown headers, and missing/extra/trailing cells according to approval.
- [ ] File-envelope tests cover every approved filename, extension, MIME, and
      byte boundary at the HTTP and service lanes.
- [ ] Identifier, amount, currency, and date tests cover minimum, maximum,
      one-over, malformed, normalization, and durable-representation cases.
- [ ] Import and reconciliation tests cover row/date ceilings, stable ordering,
      batching, interruption, retry, progress, and error-detail limits exactly
      where approved.
- [ ] If DG-067-08 approves an API change, frontend and backend contract tests
      prove note absence from the request target/query and preserve bounded
      optional note behavior. If deferred, the residual operational control is
      reported without claiming repository verification.

WI-056 Preservation:

- [ ] One explicit import action still claims one durable attempt; same-key POST
      never parses or processes the file again.
- [ ] Recovery remains header-only and owner-scoped; ADMIN list and numeric
      detail remain global actor-attributed audit views.
- [ ] Exact attempt and Settlement duplicate constraints remain the only
      accepted replay/duplicate classifications, with winner confirmation and
      unrelated integrity failures failing closed.
- [ ] Every normal import/reconciliation result still satisfies
      `totalRows == importedRows + skippedDuplicateRows + failedRows`, and
      `sum(statusCounts) == importedRows`.
- [ ] Partial-result context, all returned approved error details, full-success
      reset timing, and IGNORE authority/note/immutability remain intact.
- [ ] Import, reconciliation, list, detail, recovery, and IGNORE produce zero
      payment, refund, subscription, billing-agreement, receipt, mail, or
      Provider mutation/invocation.

Performance:

- [ ] No byte, row, date, batch, cursor, progress, retry, retention, memory, or
      latency threshold is invented before user approval.
- [ ] Approved bounds are enforced server-side with deterministic at-limit and
      over-limit behavior and no unbounded in-memory/error-detail growth.

Quality:

- [ ] Focused backend unit/controller/H2 integration tests pass with exact
      command, test count, and zero failures.
- [ ] Relevant frontend contract/page tests pass when UI/API behavior changes.
- [ ] Backend full test/build/JaCoCo and frontend test/coverage, typecheck,
      ESLint, Prettier, and build gates pass after implementation.
- [ ] PG and QA-INTEG independently approve security, HTTP, parser, transaction,
      persistence, count, UI, and no-external-effect lanes.
- [ ] `validate_docs.py` and `git diff --check` pass.
- [ ] MySQL is reported as `NOT RUN - NOT APPROVED`, `APPROVED BUT BLOCKED`, or
      `RUN` with exact manifest/hash evidence; H2 is never presented as MySQL.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`

Tier 0 (Standards - Assignee and Documentation):

- `docs/standards/development-standards.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`

Tier 1 (Policy - Inferred):

- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`
- `docs/policies/quality-gates.md`

Tier 2 (Technology / Domain - Inferred):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/standards/dto-standards.md`
- `docs/design/api-spec.md`
- `docs/design/db-schema.md`
- `docs/design/payment-settlement-import-design.md`
- `docs/design/payment-operations-runbook.md`
- `docs/design/payment-refund-receipt-settlement-policy.md`
- `docs/payment/admin-operations-guide.md`
- `docs/ui/screen-flow.md`

REQ / Finding / Predecessor:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:543-546`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:679-682`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:776-778`
- `deliverables/agent/WI-20260809-ATS-029-findings.md:354-404`
- `deliverables/agent/WI-20260809-ATS-029-findings.md:432-455`
- `deliverables/agent/WI-20260809-ATS-056-handoff.md`
- `deliverables/agent/WI-20260809-ATS-056-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-056-qa-integ-review.md`
- `deliverables/agent/WI-20260809-ATS-056-pg-review.md`

Implementation / Test / Tooling Roots:

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:126-169`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:58-68`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:197-268`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:310-462`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementRowTransactionService.java`
- `src/main/java/com/atstudio/atstudio/service/PaymentSettlementConstraintTranslator.java`
- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java`
- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlementImportAttempt.java`
- `src/main/resources/schema.sql:557-624`
- `frontend/src/api/admin.ts:723-770`
- `frontend/src/pages/admin/PaymentOperationsPage.tsx`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementImportIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java`
- `frontend/src/api/adminContracts.test.ts`
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx`
- `scripts/database/DisposableMysqlBootstrap.java:43-53`
- `scripts/database/bootstrap-disposable-mysql.ps1`

Repro / Current Baseline:

- Current parser: UTF-8 `InputStreamReader`, physical-line parsing, required
  header `containsAll`, manual quote handling, and 1,000 nonblank-row guard.
- Current field path: comma removal plus unrestricted nonnegative `BigDecimal`,
  any uppercased three-character currency, 64-character order ID, and silent
  truncation of Provider identifiers to durable lengths.
- Current reconciliation: omitted dates default to 30 days; all matching
  finalized payments load into one list with no approved maximum span/row cap.
- Current note transport: Axios `params` to Spring `@RequestParam`.
- Current DB proof: 42 source tables/42 entities; only a predecessor 41-table
  MySQL manifest/hash exists, and the reusable bootstrap still expects it.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-067-summary.md`:

- Korean decision/implementation summary with approved values, unchanged held
  gates, behavior, risks, MySQL status, and any remaining approval point.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-067-evidence-pack.md`:

- Approval evidence, decision matrix, exact patch pointers, parser/envelope and
  boundary matrices, request/response/persistence/audit/UI lanes, test commands
  and counts, reviewer verdicts, MySQL disposition/evidence, limitations,
  rollback, and next-WI pointer.

Handoff packet -> `deliverables/agent/WI-20260809-ATS-067-handoff.md`:

- This packet. During the current authoring step, this is the only permitted
  file creation; no implementation or general-document file is modified.

[TRACEABILITY REQUIREMENTS]

- Map every decision and implementation change to `CR-031-115`, `CR-031-116`,
  or `CR-031-118` and its source finding.
- Record the exact user-approved option/value, approval date/source, and files,
  layers, tests, and rollback affected by each gate. Silence is not approval.
- Separate UI guidance, multipart/request target, decoder, parser, normalized
  row, service validation, deduplication basis, durable Settlement, import
  attempt, row audit, aggregate response, and external-effect evidence lanes.
- Record exact commands, exit codes, test counts, and file/line pointers. Do not
  infer persistence from UI/HTTP text or MySQL behavior from H2.
- Before approval, remain read-only outside this handoff. After approval, edit
  only the exact approved write set and preserve unrelated shared-worktree work.
- Rollback must be bounded by file/hunk. No schema/data rollback, disposable DB
  action, or retained-data operation is permitted without its separate approval.
- Do not access `output/`, ignored secrets/configuration, or real financial or
  Provider evidence. Do not stage, commit, push, branch, merge, rebase, tag,
  stash, or perform any other Git mutation.
