[WI HEADER]
WI ID: WI-20260809-ATS-056
REQ: REQ-20260809-ATS-001
Agent: se
Depends On: WI-20260809-ATS-041
Blocks: WI-20260809-ATS-067

[WI SUMMARY]

Approval Addendum (2026-08-12): `APPROVED_WITH_REVISIONS`

Resume WI-20260809-ATS-056 under this revised minimal contract:

1. Add a dedicated CSV-import attempt ledger only.
2. Use standard Idempotency-Key with canonical lowercase UUIDv4. Never persist or log the raw key; store an owner-scoped opaque digest/hash using an existing suitable key factory/pattern where possible.
3. Add ADMIN-only attempt list, numeric detail, and operation-key recovery outcome. Prefer recovery key in the Idempotency-Key header, not URL/query.
4. Never retain file bytes, raw CSV rows, raw provider payloads, secrets, or credentials. Per-row validation errors remain response-only.
5. Fresh-only DB baseline: update entity/schema.sql/current docs later; no historical backfill or retained-DB migration and no destructive DB action.
6. Reconciliation gets no new operation-key/attempt recovery API in this WI. It only receives atomic duplicate handling where applicable, orderless/unusable classification, and total = imported + duplicate + failed/status count conservation. Date/range/ceiling/retry policy remains WI-067.
7. Same-key POST must never process the file again. It may return explicit conflict/in-progress/completed recovery guidance; no request fingerprint/file normalization policy is approved. UI must recover by the same key through read-only GET and create a new key only for a new explicit operator action.
8. No Ultra unless parent explicitly says so.

Why:

- Close canonical root `CR-031-117` / `WI-029/F-INTEG-029-B07`: Settlement
  import performs a separate existence check and insert. Concurrent requests can
  both pass the check, leaving the database unique constraint to reject one
  transaction as an unclassified failure. All-duplicate files also leave no
  durable file-level attempt evidence.
- Close canonical root `CR-031-119` / `WI-029/F-INTEG-029-B09`: reconciliation
  includes orderless finalized payments in `totalRows` but silently excludes
  them from imported, duplicate, failed, error, and durable review evidence.
- Preserve the WI-041 mixed-result and IGNORE contract while making every
  accepted row outcome deterministic and every examined reconciliation row
  attributable to exactly one response counter.

Scope In:

- Settlement import/reconciliation transaction boundaries, duplicate
  classification, existing audit-ledger integration, result aggregation, and
  focused backend/frontend contract tests.
- Database-constraint-backed atomic duplicate handling. Concurrent attempts for
  one deduplication key must produce exactly one durable Settlement and
  deterministic imported-versus-duplicate outcomes rather than an unclassified
  constraint failure or whole-file rollback.
- Durable file-attempt evidence for successful, partial, and all-duplicate
  imports, using an existing cleanly queryable persistence/audit contract if one
  can satisfy the canonical finding without semantic overloading.
- Reconciliation classification of every selected local payment, including an
  orderless payment, so `totalRows == importedRows + skippedDuplicateRows +
  failedRows` for every response in this WI's current outcome model.
- Bounded error detail for unusable reconciliation rows through the existing
  response error shape; do not invent a new row ceiling or parser policy.
- Synthetic CSV, H2 transaction/concurrency evidence, current row/audit
  invariants, and regression coverage for WI-041 partial-result and IGNORE
  behavior.
- Current Settlement audit/result documentation only after behavior is proven.
- Mandatory independent QA-INTEG review, Korean user summary, and agent evidence
  pack.

Scope Out:

- `CR-031-115`, `CR-031-116`, and `CR-031-118`: filename, MIME, byte size,
  encoding, CSV dialect/grammar/header/row-width rules, canonical financial or
  provider field bounds, date-span limits, reconciliation row ceilings,
  batching, cursoring, and progress policy. These remain owned by
  WI-20260809-ATS-067.
- Changes to WI-041 mixed-result presentation, retained correction context,
  full-success reset timing, IGNORE note/authority/immutability, or Settlement
  danger confirmation except regression coverage required by this WI.
- Real Provider, payment, refund, subscription, billing-agreement, receipt,
  mail, production import, retained external data, or deployment effects.
- A new table, column, index, migration, API idempotency header, client operation
  identity, file fingerprint policy, dependency, or architectural component
  selected without escalation. First inspect whether the existing
  `payment_settlements` unique constraint and `payment_operation_audit_logs`
  model can meet the complete durable contract cleanly. If they cannot, stop
  before schema/API architecture edits and return a decision packet containing
  the minimal proposed contract, DDL/API diff, alternatives, migration impact,
  rollback, and tests.
- Broad parser refactoring or normalization of evidence fields held by
  WI-20260809-ATS-067.

DoD:

- [ ] Two concurrent imports of the same valid deduplication key persist exactly
      one Settlement. One request classifies the row as imported and the other
      as duplicate; neither returns a generic unique-constraint/transaction
      failure and neither rolls back unrelated valid rows solely because of the
      race.
- [ ] The winning imported row has exactly one row-level
      `PAYMENT_SETTLEMENT_IMPORTED` audit event. The duplicate path creates no
      second row-level import audit.
- [ ] Every accepted file attempt, including an all-duplicate file, has one
      durable, queryable file-level evidence record with operation/batch
      identity, actor, source, total/imported/duplicate/failed counts, and a
      bounded note without raw Provider payload or secret material.
- [ ] Retrying or racing file-attempt evidence cannot fabricate a second
      successful row import or contradictory aggregate evidence.
- [ ] Reconciliation counts every selected payment exactly once. Orderless
      finalized payments are classified as failed/unusable and represented by
      bounded error detail or an equally explicit existing review record.
- [ ] Every import/reconciliation response satisfies
      `totalRows == importedRows + skippedDuplicateRows + failedRows`; tests
      cover all-valid, mixed-invalid, all-duplicate, orderless, and concurrent
      duplicate cases.
- [ ] Status counts describe persisted Settlement rows only and remain
      internally consistent with `importedRows`; duplicate and failed/unusable
      rows do not inflate them.
- [ ] WI-041 UI behavior remains intact: partial results are not full success,
      all returned errors render, correction context remains, and no automatic
      second import occurs.
- [ ] Import/reconcile/list/IGNORE actions do not mutate payment, refund,
      subscription, billing-agreement, or Provider state.
- [ ] QA-INTEG independently approves HTTP/result, transaction, durable-row,
      file-audit, aggregate-count, and no-external-effect lanes.
- [ ] Focused tests and approved `Q-ALL` gates pass, current English docs match
      proven behavior, and both closeout deliverables are complete.

Constraints / Forbidden:

- Use H2, test doubles/Test Provider, and safe synthetic CSV only. Expected real
  Provider and external-effect invocation count is zero.
- Do not run production/staging imports, use real provider exports, call Toss,
  charge/refund/cancel, send mail, deploy, or retain external effects.
- Do not inspect, print, copy, or log secrets, credentials, tokens, raw Provider
  identifiers/payloads, ignored local configuration, unrelated PII, or real
  financial evidence.
- Do not access, list, open, read, hash, extract, copy, or modify `output/` or
  any intentional untracked ZIP artifact.
- Do not delete files, destroy or clean database data, stage, commit, push,
  branch, merge, rebase, tag, stash, or perform any other Git mutation.
- Do not make a schema, dependency, architecture, or new API identity decision
  under this packet. Escalate with exact evidence if one is required.
- Preserve every unrelated shared-worktree change. Edit only the Settlement
  import/reconciliation/audit blocks and focused tests/docs needed for
  `CR-031-117` and `CR-031-119`.

[ACCEPTANCE CRITERIA]

Functional:

- [ ] A deterministic concurrent H2 integration test starts two import
      transactions against the same valid row and proves exactly one durable
      Settlement, one row-level audit, and complementary imported/duplicate
      results with no unclassified exception.
- [ ] A multi-row race proves a duplicate collision does not erase an unrelated
      valid row from the same file attempt.
- [ ] Sequential replay and an all-duplicate file return duplicate counts and
      durable file-level attempt evidence without another Settlement or
      row-level import audit.
- [ ] Full-valid and mixed-valid/invalid imports each satisfy count conservation
      and keep every current returned row error.
- [ ] Reconciliation with imported, duplicate, and orderless finalized payments
      reports each selected row in one and only one counter and includes bounded
      orderless-row error evidence.
- [ ] `statusCounts.values().sum() == importedRows` for synthetic import and
      reconciliation cases in this WI.
- [ ] Existing list, reconcile, import, and IGNORE endpoint response shapes stay
      compatible unless an unavoidable API change is escalated and approved.

Integration / Audit:

- [ ] UI feedback, frontend invocation, HTTP response, service classification,
      Settlement rows, file-level evidence, row-level audits, and aggregate
      counters are evidenced as separate lanes.
- [ ] File-level evidence is queryable after response loss and remains useful
      for an all-duplicate attempt; a merely server-returned transient UUID is
      not presented as durable recovery evidence.
- [ ] Constraint conflicts are handled at a transaction boundary that remains
      valid after the database exception; do not catch and continue inside a
      transaction already marked rollback-only.
- [ ] Actor and operator note are bounded and minimized. No raw file bytes,
      arbitrary row payload, Provider payload, secret, or raw credential is
      copied into audit free text.
- [ ] QA-INTEG records counts and durable/audit evidence for first import,
      concurrent duplicate, all-duplicate replay, partial import, orderless
      reconciliation, and required regression paths.

Quality:

- [ ] Focused backend unit/controller/H2 integration tests pass with exact test
      counts and zero failures.
- [ ] Relevant frontend contract/page tests pass if response rendering or types
      are touched; otherwise record why frontend product files required no
      change.
- [ ] Backend full test/build/JaCoCo threshold and frontend test/coverage,
      typecheck, ESLint, Prettier, and build gates pass.
- [ ] Documentation validation and `git diff --check` pass.
- [ ] Evidence distinguishes H2 concurrency proof from unperformed MySQL
      lock/deadlock/isolation rehearsal.

[INPUT POINTERS]

Tier 0 (Constitution - Required):

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`

Tier 1 (Policy - Inferred):

- `docs/policies/quality-gates.md`
- `docs/policies/security-policy.md`
- `docs/policies/access-control-policy.md`

Tier 2 (Technology / Domain - Inferred):

- `.agents/skills/react-best-practices/AGENTS.md`
- `docs/standards/frontend-standards.md`
- `docs/design/api-spec.md`
- `docs/design/payment-operations-runbook.md`
- `docs/design/payment-refund-receipt-settlement-policy.md`
- `docs/payment/admin-operations-guide.md`
- `docs/ui/screen-flow.md`

REQ / Portfolio / Predecessor:

- `deliverables/user/REQ-20260809-ATS-001.md`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:529-547`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:677-683`
- `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:990`
- `deliverables/agent/WI-20260809-ATS-029-findings.md:406-430`
- `deliverables/agent/WI-20260809-ATS-029-findings.md:457-480`
- `deliverables/agent/WI-20260809-ATS-041-handoff.md`
- `deliverables/agent/WI-20260809-ATS-041-evidence-pack.md`
- `deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md`

Implementation / Test Roots:

- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:106-149`
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:80-237`
- `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java:18-45`
- `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:32-88`
- `src/main/java/com/atstudio/atstudio/entity/PaymentOperationAuditLog.java:25-95`
- `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java:134-165`
- `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementImportResponse.java`
- `src/main/resources/schema.sql:559-601`
- `src/main/resources/schema.sql:813-864`
- `frontend/src/api/admin.ts:392-405,687-732`
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:674-713,1586-1660`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java`
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java`
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java`
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx`
- `frontend/src/api/adminContracts.test.ts`

Reproduction Baseline:

- Current sequential gap: `existsByDeduplicationKey` then `save` in one service
  transaction; database unique constraint is the final fence.
- Current all-duplicate gap: response receives a fresh server batch key, but no
  new Settlement row carries it and there is no separate file-attempt record.
- Current count gap: reconciliation `continue`s when `paymentOrder == null`,
  while response `totalRows` uses the complete payment list and `failedRows` is
  fixed to zero.

[OUTPUT CONTRACT]

User-facing -> `deliverables/user/WI-20260809-ATS-056-summary.md`:

- Korean summary of behavior, count invariant, concurrency/audit evidence,
  limitations, risks, and any remaining approval point.

Agent-facing -> `deliverables/agent/WI-20260809-ATS-056-evidence-pack.md`:

- Exact patch pointers, transaction design, before/after matrices, concurrent
  results, durable Settlement/file-audit/row-audit evidence, count-conservation
  evidence, commands/results, reviewer verdict, limitations, rollback, and next
  WI pointer.

Handoff packet -> `deliverables/agent/WI-20260809-ATS-056-handoff.md`:

- This packet.

Escalation packet when required ->
`deliverables/agent/WI-20260809-ATS-056-schema-api-decision.md`:

- Create only if the complete file-level audit/recovery requirement cannot be
  met cleanly with current schema/API. Include minimal alternatives and exact
  consequences; do not implement the held change before approval.

[TRACEABILITY REQUIREMENTS]

- Map every change to `CR-031-117` / `F-INTEG-029-B07` or `CR-031-119` /
  `F-INTEG-029-B09`.
- Record exact file/line pointers and reproducible commands with exit codes and
  test counts.
- Separate request/HTTP, transaction, durable Settlement, file-level evidence,
  row-level audit, aggregate response, UI, and external-effect lanes.
- Prove exact invocation and row counts; do not infer durability from an HTTP
  message or a mocked UI success alone.
- Record rollback by bounded file/hunk and explicitly state that no schema/data
  rollback is permitted unless separately approved.
- Preserve held WI-067 boundaries and identify any residual MySQL-only risk.
