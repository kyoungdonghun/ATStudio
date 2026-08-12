---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-035
dependencies:
  - path: WI-20260809-ATS-035-handoff.md
    reason: Approved scope, acceptance criteria, and output contract
  - path: WI-20260809-ATS-035-pg-review.md
    reason: Final security and privacy verdict
  - path: WI-20260809-ATS-035-qa-integ-review.md
    reason: Final cross-layer integration verdict
  - path: WI-20260809-ATS-035-re-review.md
    reason: Final independent reliability verdict
---

# Evidence Pack: WI-20260809-ATS-035

## Summary

- Closed `CR-031-092` / `WI-028/F-01` with exact ADMIN detail recovery for
  refund and refund-linked entitlement-correction execute ambiguity, four
  outcomes, cross-domain locks, stale-response fencing, and zero automatic
  execute replay.

## Scope / DoD Check

- [x] Refund and correction retain separate domain, durable ID, generation,
      detail, and outcome ownership.
- [x] Every explicit execute performs the exact-ID detail preflight first and
      sends at most one execute POST only from fresh `APPROVED`.
- [x] Rejected/lost execute delivery performs one bounded exact detail GET and
      never repeats execute.
- [x] `COMMITTED`, `FAILED`, `RELOAD_FAILED`, and `UNKNOWN` use exact durable
      predicates for each domain.
- [x] Execute POSTs opt out of shared authentication replay.
- [x] Durable in-flight rows hydrate to exact-ID `UNKNOWN` after reload.
- [x] Manual `status again` is read-only; pre-execution unlock requires exact
      `REQUESTED` or `APPROVED`, followed by the normal later operator action.
- [x] Ambiguity locks linked refund/correction mutations across domains.
- [x] Execute/read/view generations fence rapid clicks and stale responses.
- [x] Automatic refund and correction execute retries are zero; recovery reads
      make zero mutation and Provider calls.
- [x] PG, QA-INTEG, and RE final verdicts are `APPROVE`.
- [x] Five current English documents and both closeout deliverables are
      complete without product/test edits by DocOps.

## Reference Documents (Tier 0-2)

The approved handoff supplied these input pointers. Tier 0 and the five current
domain documents were read directly for this documentation closeout.

<!-- prettier-ignore -->
| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Language, approval, traceability, and financial integrity |
| 0 | `docs/standards/development-standards.md` | Evidence pointers, verification, coverage, and rollback |
| 0 | `docs/standards/documentation-standards.md` | Metadata, versioning, links, and English documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical WI, Subscription, and correction terms |
| 1 | `docs/policies/security-policy.md` | Handoff security pointer |
| 1 | `docs/policies/access-control-policy.md` | Handoff ADMIN authorization pointer |
| 1 | `docs/policies/quality-gates.md` | Handoff closeout-gate pointer |
| 1 | `docs/standards/evidence-pack-standard.md` | Two-set and reproducibility contract |
| 2 | `docs/design/api-spec.md` | Existing detail endpoints and API-count contract |
| 2 | `docs/design/payment-integration-design.md` | Recovery and Provider-boundary invariants |
| 2 | `docs/design/payment-operations-runbook.md` | Operator recovery procedure |
| 2 | `docs/payment/admin-operations-guide.md` | ADMIN screen procedure |
| 2 | `docs/ui/screen-flow.md` | Current UI controls and state flow |
| WI | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent request |
| WI | `deliverables/agent/WI-20260809-ATS-035-handoff.md` | Approved bounded execution contract |

## Reviewer Decisions

| Review | Final verdict | Evidence |
| --- | --- | --- |
| PG | `APPROVE` | `deliverables/agent/WI-20260809-ATS-035-pg-review.md:1-102` |
| QA-INTEG | `APPROVE` | `deliverables/agent/WI-20260809-ATS-035-qa-integ-review.md:1-63` |
| RE | `APPROVE` | `deliverables/agent/WI-20260809-ATS-035-re-review.md:1-105` |

PG also recorded a pre-existing, non-blocking ADMIN DTO-minimization debt. The
refund list/detail DTO contains the raw `idempotencyKey`, actor emails, and
`failureMessage`; the correction list/detail DTO contains actor emails and
`failureMessage`. WI-035 added callers, not fields, endpoints, recipients, or
schemas, and the recovery UI renders none of those fields. This debt is not
represented as fixed.

## Outcome Decision Table

| Outcome | Refund predicate | Correction predicate | UI / mutation rule |
| --- | --- | --- | --- |
| `COMMITTED` | Exact detail `SUCCEEDED` | Exact detail `SUCCEEDED` | Show one confirmed-success result; later presentation reload failure is reported separately. |
| `FAILED` | Exact execute/detail `FAILED` or `CANCELLED` | Exact execute/detail `FAILED` or `CANCELLED` | Show authoritative terminal failure; no automatic execute. |
| `RELOAD_FAILED` | Execute returned `SUCCEEDED`, then required exact detail or committed-result list reload failed | Same predicate for the exact correction | Preserve successful execute context; expose read-only status recovery. |
| `UNKNOWN` | Unproved result, including `PROCESSING`, `PENDING_PROVIDER_CONFIRMATION`, or unreadable recovery | Unproved result, including `PROCESSING` or unreadable recovery | Keep exact and linked mutations locked; expose read-only status recovery. |

## Evidence Pointers

### Product Diff

All product/test pointers below map to `CR-031-092` / `WI-028/F-01`. DocOps
read these final shared-worktree changes and did not modify them.

- `frontend/src/api/admin.ts:412-520,769-809,846-886` defines exact status
  unions, existing-detail GET wrappers, and `skipAuthReplay` only on the two
  execute POSTs.
- `frontend/src/api/client.ts:7-10,102-126` rejects opted-out `401` responses
  before refresh, queueing, or replay.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:144-220` defines separate
  intent types, four outcomes, durable mappings, and distinct feedback.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:317-409` owns intents,
  cross-domain locks, read deduplication, and execute ownership.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:482-540` hydrates durable
  refund/correction in-flight rows after list reload.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:863-1027` implements
  refund exact preflight, one execute, bounded recovery, read-only status, and
  pre-execution unlock.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:1105-1278` implements the
  same bounded sequence independently for entitlement correction.
- `frontend/src/pages/admin/PaymentOperationsPage.tsx:2358-2576` renders the
  outcome, read-only status action, exact eligibility, and linked locks.
- `src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:160-218`
  proves both existing exact detail GETs remain ADMIN-only controller methods.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentRefundService.java:59-74`
  reads the exact refund ledger row in a read-only transaction.
- `src/main/java/com/atstudio/atstudio/service/AdminPaymentEntitlementCorrectionService.java:47-92`
  reads the exact correction ledger row under the service's read-only default.

### Focused Tests

- `frontend/src/api/adminContracts.test.ts:270-327` pins the detail routes and
  auth-replay opt-out on only the execute POSTs.
- `frontend/src/api/client.test.ts:162-230` proves opted-out execute `401`
  cannot refresh, queue, replay, or clear the session, including concurrent
  normal refresh.
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:761-1013` covers
  reload hydration, manual unlock, exact preflight ordering, and zero POST on
  blocked preflight.
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1015-1209` covers
  lost-response durable mappings, one execute, reload failure, and read-only
  recovery.
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1212-1511` covers
  every authoritative transition out of `RELOAD_FAILED` and distinct
  post-execute feedback.
- `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:1524-1869` covers
  stale list/view fencing, read deduplication, execute/status ownership, rapid
  action, and linked-domain locks.
- `frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:1161-1322`
  retains adjacent positive request/approve/typed-execute flows.
- `src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:87-117`
  proves authentication, ADMIN authorization, and exact-ID forwarding.
- `src/test/java/com/atstudio/atstudio/service/AdminPaymentRefundServiceTest.java:90-111`
  proves present/missing exact reads call the refund Provider zero times.

### Documentation Closeout

- `docs/design/payment-operations-runbook.md:238-297` records the operator
  sequence, four outcomes, hydration, locks, fencing, and zero retries.
- `docs/payment/admin-operations-guide.md:195-293` updates both tab workflows
  and the shared read-only recovery controls.
- `docs/design/api-spec.md:107-202` documents the two existing GETs without
  changing `AdminPaymentController` 24 or backend total 146, and records the
  DTO debt honestly.
- `docs/design/payment-integration-design.md:289-331,400-457` records the
  separate state machines, cross-domain invariants, unchanged schema/API
  boundary, debt, and verification boundary.
- `docs/ui/screen-flow.md:149-176` records the current ADMIN UI behavior.

## Cross-Layer Reproduction Matrix

| Scenario | UI / control | Frontend calls | Backend / Provider lane | Durable / final UI lane |
| --- | --- | --- | --- | --- |
| Explicit eligible execute | Typed confirmation; execute/status controls owned | 1 preflight GET, at most 1 execute POST | Exact local read; refund initial execute may call Provider once, correction calls Provider zero times | Fresh `APPROVED` permits POST; exact returned/detail status maps outcome |
| Execute response loss | No automatic execute button or interceptor replay | 1 preflight GET + 1 execute POST + 1 bounded recovery GET; 0 extra POST | 0 recovery Provider calls; refund Provider invocation is at most the one initial execute | `SUCCEEDED` -> `COMMITTED`; terminal -> `FAILED`; otherwise `UNKNOWN` |
| Execute success, reload failure | Success context retained; reload-specific message | 1 preflight GET + 1 POST + required detail/list reads | No Provider retry | Exact execute success remains successful; final UI is `RELOAD_FAILED` until a later detail read converges |
| Browser/list reload in flight | Read-only `status again`; execute and linked mutations disabled | Initial list GET; later manual exact GET only | 0 mutation and 0 Provider calls from recovery | Refund `PROCESSING`/`PENDING_PROVIDER_CONFIRMATION` or correction `PROCESSING` hydrates `UNKNOWN` |
| Manual pre-execution unlock | Status action only | 1 exact detail GET; 0 approve/execute calls | Read-only local query | `REQUESTED` restores approve-only; `APPROVED` requires a later typed action and new preflight |
| Rapid/stale response | Pending controls and imperative guards retain one owner | Duplicate status/execute is rejected; current generation wins | No duplicate execute or Provider retry | Older detail/list/tab/page success or failure cannot overwrite current intent |

Frontend component tests use mocked API promises for delivery/race ordering.
Backend controller/service tests independently prove authorization, exact-ID
read behavior, and zero Provider calls from refund detail recovery. These lanes
do not constitute deployed-browser or live-Toss evidence.

## Commands and Outputs

The product results below are the supplied final green gate evidence. DocOps
did not rerun product suites during this documentation-only closeout.

| Lane | Exact command / result |
| --- | --- |
| Backend full | `./gradlew.bat test jacocoTestReport jacocoTestCoverageVerification build --console=plain` -> `BUILD SUCCESSFUL`; Gradle HTML: 1,459 tests, 0 failures, 16 skipped |
| Backend coverage | instruction 86.394%; line 86.637%; method 84.100%; branch 71.290% |
| Frontend full | `npm test` -> 72 files, 794 tests PASS |
| Frontend coverage | `npm run test:coverage` -> statements 88.2%, branches 79.39%, functions 87.73%, lines 90.44% |
| Frontend static/build | `npm run typecheck`, `npm run lint`, `npm run format`, and `npm run build` -> PASS |
| Format command correction | `npm run format:check` is not defined in `frontend/package.json`; verification used the repository command `npm run format`. This was a command correction, not a product failure. |
| Final RE focused + adjacent | `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx src/api/client.test.ts src/api/adminContracts.test.ts src/test/coverage/adminSubscriberGaps.coverage.test.tsx` -> 4 files, 132 tests PASS, 0 failures, exit 0 |

Additional independent review runs:

- PG frontend: 3 files, 87 tests PASS; PG backend: 23 tests, 0 failures,
  0 errors, 0 skipped, Gradle successful.
- QA-INTEG: `PaymentOperationsPage.test.tsx` 1 file, 76 tests PASS.

Current generated report pointers corroborate the supplied full-gate metrics:

- `build/reports/tests/test/index.html` -> 1,459 tests, 0 failures,
  16 skipped.
- `build/reports/jacoco/test/jacocoTestReport.xml` -> instruction 86.394%,
  line 86.637%, method 84.100%, branch 71.290%.
- `frontend/coverage/coverage-summary.json` -> statements 88.2%, branches
  79.39%, functions 87.73%, lines 90.44%.

DocOps closeout commands and final results:

- `python .claude/skills/validate-docs/scripts/validate_docs.py` -> exit 0;
  Tier 0 documents, internal links, 554 supported traceability IDs, and
  document index coverage all passed.
- `git diff --check` -> exit 0 with no whitespace errors. It emitted only
  non-blocking CRLF-to-LF conversion warnings for pre-existing shared-worktree
  files; no warned file was changed to address line endings in this WI.

## Risks / Rollback

- Automated React, H2/Test-Provider, controller, and service evidence does not
  prove live Toss, deployed-browser timing, production DB, or external-service
  behavior.
- Pre-existing ADMIN DTO minimization remains a separate non-blocking follow-up
  as recorded above.
- Roll back only WI-035 product/test hunks identified by Main and the seven
  documentation/deliverable paths listed in this Evidence Pack. Preserve
  WI-032 through WI-034 and all unrelated shared-worktree changes. No endpoint,
  schema, Provider reversal, or retained-data rollback is required.

## Side-Effect and Git Record

- DocOps edited only the five requested current docs and the two WI-035
  deliverables.
- No product/test code was edited by DocOps.
- No external service, live Provider, deployment, staging, commit, push,
  branch, merge, destructive action, ignored secret, ZIP, or output artifact
  was accessed or modified during closeout.

## Follow-Up Chain

- WI-035 remains a prerequisite for WI-041, WI-054, and WI-064 and later final
  audit gates. This bounded DocOps closeout did not create or delegate another
  WI; Main retains responsibility for the approved chain.
- DTO minimization requires a separate approved contract change.

## Related Documents

- [PG Review](WI-20260809-ATS-035-pg-review.md)
- [QA-INTEG Review](WI-20260809-ATS-035-qa-integ-review.md)
- [RE Review](WI-20260809-ATS-035-re-review.md)
- [WI-035 User Summary](../user/WI-20260809-ATS-035-summary.md)
- [Payment Operations Runbook](../../docs/design/payment-operations-runbook.md)
- [ADMIN Operations Guide](../../docs/payment/admin-operations-guide.md)
- [API Specification](../../docs/design/api-spec.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
