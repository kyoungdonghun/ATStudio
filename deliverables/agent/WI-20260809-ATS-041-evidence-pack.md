---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-041
dependencies:
  - path: WI-20260809-ATS-041-handoff.md
    reason: Approved scope, acceptance criteria, and output contract
  - path: WI-20260809-ATS-041-pg-review.md
    reason: Final security and privacy verdict
  - path: WI-20260809-ATS-041-qa-integ-review.md
    reason: Final cross-layer integration verdict
---

# Evidence Pack: WI-20260809-ATS-041

## Summary

- Closed `CR-031-113` / `F-INTEG-029-B03` and `CR-031-114` /
  `F-INTEG-029-B04` with honest mixed-import presentation, retained correction
  context, independent IGNORE validation and ADMIN authority checks, and an
  immutable first decision plus one audit row.

## Scope / DoD Check

- [x] Mixed HTTP-200 import with `failedRows > 0` is visibly partial and every
      returned row error is rendered.
- [x] Partial import retains the exact React `File`, DOM file input, and note;
      one confirmation produces one import and one Settlement-list reload.
- [x] Full success clears React and DOM file state only after the one required
      reload succeeds; transport and reload failure retain correction context.
- [x] Missing, null, blank, and trimmed-over-500 IGNORE notes fail at HTTP and
      service boundaries before Settlement mutation or audit.
- [x] A valid direct caller must supply an authenticated ADMIN principal whose
      locked authoritative User row is still active and ADMIN before the
      Settlement lock or mutation.
- [x] The first actor, time, normalized note, `IGNORED` status, and audit row
      remain immutable; every otherwise-valid repeat returns
      `INVALID_STATE_TRANSITION` with zero new mutation or audit.
- [x] Settlement operations add no payment, refund, subscription,
      billing-agreement, Provider, receipt, or mail mutation path.
- [x] Existing note and danger confirmation remain; no typed phrase was added.
- [x] PG and QA-INTEG final verdicts are `APPROVE`.
- [x] Five current English documents and both closeout deliverables are
      complete without DocOps product/test edits.
- [x] Full recorded backend/frontend gates and final DocOps document gates pass.

## Reference Documents (Tier 0-2)

The handoff supplied the implementation context below. DocOps directly read the
Tier 0 documentation context, approved handoff, final reviews, current
Settlement implementation/tests/UI, and current domain documents.

| Tier | Document | Use |
| --- | --- | --- |
| 0 | `docs/standards/core-principles.md` | Language, approval, traceability, and financial integrity |
| 0 | `docs/standards/development-standards.md` | Pointer-first evidence, test evidence, JPA auditing, and H2/MySQL limits |
| 0 | `docs/standards/documentation-standards.md` | Metadata, versioning, links, and English documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical ADMIN, Subscription, and WI terminology |
| 1 | `docs/policies/security-policy.md` | ADMIN payment and Provider-data boundary |
| 1 | `docs/policies/access-control-policy.md` | Least privilege and default deny |
| 1 | `docs/policies/quality-gates.md` | Handoff quality-policy pointer |
| 2 | `.agents/skills/react-best-practices/AGENTS.md` | Handoff frontend review pointer |
| 2 | `docs/standards/frontend-standards.md` | Handoff React/TypeScript pointer |
| 2 | `docs/design/api-spec.md` | Current Settlement API contract |
| 2 | `docs/design/payment-operations-runbook.md` | Operator sequence and external-effect boundary |
| 2 | `docs/design/payment-refund-receipt-settlement-policy.md` | Settlement and audit policy |
| 2 | `docs/payment/admin-operations-guide.md` | ADMIN screen procedure |
| WI | `deliverables/user/REQ-20260809-ATS-001.md:1-5` | Approved parent request |
| WI | `deliverables/user/REQ-20260809-ATS-001.md:31-41` | Parent DoD |
| WI | `deliverables/user/REQ-20260809-ATS-001.md:92-101` | Parent quality gates |
| WI | `deliverables/agent/WI-20260809-ATS-041-handoff.md:1-107` | Approved bounded contract and DoD |

**Injection rules applied:** the approved handoff identifies assignee `se`,
task type implementation/review, Tier 0 core/development context, Tier 1
security/access/quality context, and Tier 2 frontend/Settlement context. This
DocOps closeout additionally applied the attached `create-wi-evidence-pack`
skill and the parent REQ's documentation context at
`deliverables/user/REQ-20260809-ATS-001.md:64-75`.

## Repository Root and Canonical Mapping

| Item | Exact value / pointer |
| --- | --- |
| Repository root | `C:\Users\jm991\Desktop\project\ATStudio` |
| Approved WI packet | `deliverables/agent/WI-20260809-ATS-041-handoff.md` |
| Root registry | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:677-683` |
| Portfolio owner row | `deliverables/agent/WI-20260809-ATS-031-consolidated-findings.md:957-970` |
| Source finding, mixed import | `deliverables/agent/WI-20260809-ATS-029-findings.md:302-326` |
| Source finding, IGNORE | `deliverables/agent/WI-20260809-ATS-029-findings.md:328-352` |

| Canonical root | Implemented behavior | Primary product/test roots | Current documentation |
| --- | --- | --- | --- |
| `CR-031-113` / `F-INTEG-029-B03` | Partial result, all errors, retained correction context, one import/reload, delayed full-success reset | `frontend/src/pages/admin/PaymentOperationsPage.tsx:674-713`, `frontend/src/pages/admin/PaymentOperationsPage.tsx:1586-1660`, `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:614-715`, `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:80-129`, `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:272-313` | `docs/design/api-spec.md:134-176`, `docs/design/payment-operations-runbook.md:324-356`, `docs/payment/admin-operations-guide.md:158-220`, `docs/ui/screen-flow.md:149-169` |
| `CR-031-114` / `F-INTEG-029-B04` | Required bounded note, authenticated and authoritative ADMIN, locks, immutable first decision/audit, explicit repeat failure | `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementIgnoreRequest.java:3-13`, `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:239-273`, `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:556-568`, `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:166-174`, `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java:57-197` | `docs/design/api-spec.md:151-176`, `docs/design/payment-refund-receipt-settlement-policy.md:425-451`, `docs/payment/admin-operations-guide.md:204-220`, `docs/ui/screen-flow.md:159-169` |

## Mixed Import Matrix

| Scenario | UI state and feedback | Frontend invocations | HTTP / service | Durable / audit lane |
| --- | --- | --- | --- | --- |
| Mixed HTTP `200`, `failedRows > 0` | Warning plus visible partial status; all returned errors; exact `File`, same DOM input, and original note retained | Import `1`; post-result Settlement reload `1`; no second import | Existing result fields are unchanged; request receives trimmed note | Valid rows and their row audits persist; invalid rows return errors. Synthetic service proof: imported `1`, failed `1`, Settlement save `1`, import audit `1` |
| Full success, `failedRows == 0` | Success only after reload; React selected file becomes null and keyed DOM input is replaced empty | Import `1`; post-result Settlement reload `1` | Resolved zero-failure result | Current imported-row writes/audits remain unchanged; no new count policy is claimed |
| Import transport/server failure | Error; exact file, DOM input, and note retained; no success or partial claim | Import attempt `1`; post-result reload `0` | No usable result | UI does not infer durable outcome; operator retains context for verification |
| Required reload failure | Reload-specific error; exact file, DOM input, note, and received result retained; no full-success claim | Import `1`; reload attempt `1` | Import result was received; list confirmation failed | Received result may represent durable rows, but the current list is unconfirmed; no automatic resubmit |

Pointers:

- State and import decision: `frontend/src/pages/admin/PaymentOperationsPage.tsx:248-253,674-713`.
- Active-tab list reload: `frontend/src/pages/admin/PaymentOperationsPage.tsx:413-423,468-482,548-562`.
- DOM reset and complete error rendering:
  `frontend/src/pages/admin/PaymentOperationsPage.tsx:1617-1626,1639-1660`.
- Full, mixed, transport, and reload tests:
  `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:614-715`.
- Current row-level durable behavior:
  `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:80-129`.
- Mixed service proof:
  `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:272-313`.

## IGNORE Boundary Matrix

| Case | HTTP / service decision | Repository order | Settlement fields | Audit / external calls |
| --- | --- | --- | --- | --- |
| `{}`, null, empty, blank, trimmed 501-char note at HTTP | HTTP `400`; service calls `0` | None | Mutation `0` | Audit `0`; external `0` |
| Null request, null/blank note, trimmed-over-500 direct service input | `INVALID_ARGUMENT` before actor access | None | Mutation `0` | Audit `0`; external `0` |
| Null or ID-less principal | `RESOURCE_NOT_ACCESS` | None | Mutation `0` | Audit `0`; external `0` |
| Token principal is not ADMIN | `ADMIN_ROLE_REQUIRED` | None | Mutation `0` | Audit `0`; external `0` |
| Authoritative user missing | `RESOURCE_NOT_FOUND` | Locked User lookup only | Mutation `0` | Audit `0`; external `0` |
| Authoritative user role drift or deleted | `ADMIN_ROLE_REQUIRED` | Locked User lookup only | Mutation `0` | Audit `0`; external `0` |
| First valid IGNORE | Success; normalized note | Locked User, then locked Settlement, then audit | `MISMATCHED` to `IGNORED`; authoritative actor, first time, normalized note | One `PAYMENT_SETTLEMENT_IGNORED` audit; external `0` |
| Same-note valid repeat | `INVALID_STATE_TRANSITION` | Locked User, then locked Settlement | First status/actor/time/note unchanged | New audit `0`; external `0` |
| Conflicting-note valid repeat | `INVALID_STATE_TRANSITION` | Locked User, then locked Settlement | First status/actor/time/note unchanged | New audit `0`; external `0` |

Pointers:

- ADMIN routes and validated request:
  `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:106-149`;
  `/api/admin/**` rule at
  `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:128-138`.
- Trimmed HTTP DTO: `src/main/java/com/atstudio/atstudio/dto/payment/AdminPaymentSettlementIgnoreRequest.java:3-13`.
- Note, actor, lock, mutation, and audit order:
  `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:239-273,556-568`.
- Pessimistic lock queries:
  `src/main/java/com/atstudio/atstudio/repository/UserRepository.java:18-20` and
  `src/main/java/com/atstudio/atstudio/repository/PaymentSettlementRepository.java:27-29`.
- Entity repeat guard:
  `src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:166-174`.
- Audit projection:
  `src/main/java/com/atstudio/atstudio/service/PaymentOperationAuditLogService.java:134-158`.
- HTTP cases: `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:232-294`.
- Direct service cases and call order:
  `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:460-685`.
- Entity immutability:
  `src/test/java/com/atstudio/atstudio/entity/PaymentSettlementTest.java:23-53`.

## Durable and Audit Evidence

| Point | Status | Actor | Time | Note | Audit rows |
| --- | --- | --- | --- | --- | ---: |
| Before first IGNORE | `MISMATCHED` | null | null | null | 0 |
| After first valid IGNORE | `IGNORED` | first authoritative ADMIN | first decision time | `first durable note` | 1 |
| After same-note repeat | unchanged | first actor | first time | first note | 1, same ID and creation time |
| After conflicting actor/note repeat | unchanged | first actor | first time | first note | 1, same ID and creation time |
| Concurrent first decisions | `IGNORED` | exactly one winner | winner time | winner note | 1; audit actor/note match durable winner |

- Sequential H2 proof:
  `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java:57-123`.
- Concurrent H2 proof:
  `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java:125-197`.
- H2 exercises the JPA transaction and pessimistic-lock path. It does not
  replace a MySQL lock-wait or deadlock rehearsal.

## Side-Effect Matrix

| State family | Import / reconcile / IGNORE behavior |
| --- | --- |
| `payment_settlements` | Intended local row create/reconcile/first IGNORE mutation |
| `payment_operation_audit_logs` | Intended row audit per imported/generated row and one first valid IGNORE audit |
| Payment order / finalized payment | Read for reconciliation only; no status mutation |
| Refund | Read succeeded refund aggregate only; no refund mutation |
| Subscription / entitlement | Read linkage only; no access mutation |
| Billing agreement | No mutation or Provider billing-key operation |
| Provider / receipt / mail | Invocation count `0` for changed paths |
| UI confirmation | Existing note plus danger confirmation; typed phrase count added by WI-041 is `0` |

Service dependency and write pointers:
`src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:73-129,149-259`.
The independent side-effect assessment is at
`deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md:60-86`.

## Reviewer Decisions

| Review | Final verdict | Focused evidence | Limitations |
| --- | --- | --- | --- |
| PG | `APPROVE` | `deliverables/agent/WI-20260809-ATS-041-pg-review.md:24-30,43-107` | `deliverables/agent/WI-20260809-ATS-041-pg-review.md:118-143` |
| QA-INTEG | `APPROVE` | `deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md:21-86` | `deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md:96-113` |

Focused independent commands:

- Backend Settlement final:
  `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.entity.PaymentSettlementTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementIgnoreIntegrationTest"`
  -> exit `0`; `38/38` passed; controller `13`, service `22`, entity `1`, H2
  integration `2`; `0` failed/errors/skipped.
- Frontend core:
  `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx -t "fully successful import|mixed-result error|import request fails|required reload fails|settlement ignore"`
  -> exit `0`; `5/5` passed, `81` skipped.
- Frontend adjacent contracts:
  `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx src/api/adminContracts.test.ts src/test/coverage/adminSubscriberGaps.coverage.test.tsx -t "settlement reconciliation|settlement import, reconciliation, filtering, and ignore contracts|loads every read-only ledger tab with representative rows and filters settlements|requires an ignore note"`
  -> exit `0`; `4/4` passed, `111` skipped.

Independent command records:
`deliverables/agent/WI-20260809-ATS-041-pg-review.md:109-116` and
`deliverables/agent/WI-20260809-ATS-041-qa-integ-review.md:88-94`.

## Full Gate Evidence

The implementation gate record below was supplied to DocOps as final evidence;
DocOps did not rerun product/test gates during this documentation-only closeout.

| Gate | Exact command / result |
| --- | --- |
| Backend final | `.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification build --console=plain` -> exit `0`; 1472 tests, 0 fail, 16 skipped; instruction 86.531%, branch 71.391%, line 86.830%, method 84.216%; build success |
| Frontend tests | `npm test -- --run` -> 72 files; 798/798 passed |
| Frontend coverage final | `npm run test:coverage` -> 72 files; 798/798 passed; statements 88.27%, branches 79.43%, functions 87.78%, lines 90.50% |
| Frontend type safety | `npm run typecheck` -> pass |
| Frontend lint | `npm run lint` -> pass |
| Frontend format | `npm run format` -> pass |
| Frontend build | `npm run build` -> pass; 273 modules built |
| Documentation baseline | `python .agents/skills/validate-docs/scripts/validate_docs.py` -> exit `0`; 557 traceability IDs |
| Diff baseline | `git diff --check` -> exit `0`; existing CRLF warnings only |

### Gate Incident Attribution

- The initial backend full gate exposed two failures in
  `PaymentRecoveryReadIntegrationTest` because its JPA slice omitted auditing
  configuration. The standalone run reproduced both failures.
- A test-only `JpaConfig` import at
  `src/test/java/com/atstudio/atstudio/service/PaymentRecoveryReadIntegrationTest.java:5,36-42`
  made the standalone class pass `2/2`, after which the final full backend gate
  passed. This was an adjacent test-harness correction, not a WI-041 Settlement
  product failure.
- The first two full frontend coverage runs each hit one 5-second timeout in
  the broad catalog coverage test, at 5.221s and 5.456s. A targeted rerun passed
  in 1.92s. Only that single test timeout changed to `10_000` at
  `frontend/src/test/coverage/shellCatalogRouterGaps.coverage.test.tsx:817-859`;
  assertions and global configuration were unchanged. The final full coverage
  run passed with the metrics above.

## Documentation Closeout

- `docs/design/api-spec.md:134-176` records mixed-import, reload, IGNORE,
  authority, immutability, no-external-effect, and held-policy contracts.
- `docs/design/payment-operations-runbook.md:324-356` records the operator
  sequence, exact invocation bounds, retained context, and follow-up ownership.
- `docs/design/payment-refund-receipt-settlement-policy.md:328-330,425-451`
  records first-audit immutability and verified policy boundaries.
- `docs/payment/admin-operations-guide.md:158-220` records the current ADMIN
  steps without presenting current parser behavior as a complete strict policy.
- `docs/ui/screen-flow.md:149-169` records current screen state transitions and
  the unchanged confirmation mode.
- `deliverables/user/WI-20260809-ATS-041-summary.md` provides the Korean
  user-facing closeout.

Final DocOps commands after all edits:

- `python .agents/skills/validate-docs/scripts/validate_docs.py` -> exit `0`;
  Tier 0, internal links, 557 traceability IDs, and document index passed.
- `git diff --check` -> exit `0`; no whitespace errors; existing shared-worktree
  CRLF warnings only.

## Held Boundaries and Limitations

- `CR-031-115`, `CR-031-116`, and `CR-031-118` remain held for
  WI-20260809-ATS-067. WI-041 does not approve CSV filename, MIME, byte,
  encoding, dialect, grammar, header, row-width, canonical field-bound, date,
  range, or row-ceiling policy.
- `CR-031-117` and `CR-031-119` remain held for WI-20260809-ATS-056. WI-041
  does not approve concurrent duplicate atomicity, file-level audit,
  unusable-row accounting, or aggregate count conservation.
- Shared ADMIN modal and typed confirmation work for the separate general
  local-subscription correction flow remains assigned to WI-20260809-ATS-054.
  Settlement IGNORE retains its existing note and danger confirmation.
- Focused UI evidence uses resolved/rejected mocks; mixed service and immutable
  audit evidence use synthetic data and H2. There is no live browser-to-server
  capture, production import, or real Provider result.
- H2 does not replace MySQL lock-wait, deadlock, isolation, or deployment
  rehearsal.
- No real Provider, SDK, payment, refund, receipt, mail, production, or retained
  data action was used. No secret/ignored configuration or `output/`/ZIP was
  accessed. No destructive action or Git mutation occurred.

## Risks / Rollback

Risks:

- Production MySQL scheduling and timeout behavior remains unverified by the H2
  concurrency test.
- A transport failure can leave durable import outcome unknown to the UI; the
  retained context supports investigation but is not durable-result proof.
- Held parser, duplicate, file-audit, and count questions remain intentionally
  open under their assigned WIs.

Rollback:

- Revert only the WI-041 Settlement hunks listed in this pack; preserve WI-035
  and every unrelated shared-worktree change.
- Roll back DTO/service/entity/audit repeated-IGNORE behavior and focused tests
  as one unit.
- Roll back mixed-import UI state/reset behavior and focused tests as one unit.
- Revert only the WI-041 paragraphs in the five documentation files and the two
  closeout artifacts under an approved rollback.
- No schema rollback, data deletion, audit cleanup, Provider reversal,
  production compensation, or deployment rollback is required or permitted.

## Follow-ups

- WI-20260809-ATS-056 owns `CR-031-117` and `CR-031-119` after WI-041 closeout.
- WI-20260809-ATS-067 owns `CR-031-115`, `CR-031-116`, and `CR-031-118` after
  the required WI-041 and WI-056 evidence exists.
- WI-20260809-ATS-054 owns shared ADMIN modal and typed local-correction behavior
  after its separate prerequisite chain is complete.
