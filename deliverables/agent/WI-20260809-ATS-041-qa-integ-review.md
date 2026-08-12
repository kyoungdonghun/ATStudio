---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: QA-INTEG
category: audit
status: accepted
dependencies:
  - path: WI-20260809-ATS-041-handoff.md
    reason: Approved WI scope, acceptance criteria, and held boundaries
  - path: WI-20260809-ATS-031-consolidated-findings.md
    reason: Canonical roots CR-031-113 and CR-031-114
  - path: WI-20260809-ATS-029-findings.md
    reason: Source findings F-INTEG-029-B03 and F-INTEG-029-B04
  - path: WI-20260809-ATS-041-pg-review.md
    reason: Mandatory final PG security review
---

# QA-INTEG Review: WI-20260809-ATS-041

## Final Decision

**APPROVE**

No open P0, P1, or P2 cross-layer finding remains in the final bounded
Settlement diff for `CR-031-113` and `CR-031-114`.

## Prioritized Evidence

| Priority | Root | Decision | Evidence summary |
| --- | --- | --- | --- |
| P1 | `CR-031-113` / `F-INTEG-029-B03` | PASS | Mixed HTTP-200 import results are partial, correction context is retained, all returned errors are rendered, and call counts are bounded. |
| P1 | `CR-031-114` / `F-INTEG-029-B04` | PASS | HTTP and service validation fail closed, the authenticated ADMIN owns the durable and audit evidence, and first decision plus one audit row are immutable under sequential and concurrent retry. |
| P1 integration boundary | A/B/C | PASS | Settlement actions add no payment, refund, subscription, billing-agreement, Provider, mail, or other external mutation path. |

## A. Mixed Import HTTP 200

| Lane | Verified result |
| --- | --- |
| UI outcome | A resolved result with `failedRows > 0` produces warning feedback and a visible partial-completion status, never the full-success toast (`frontend/src/pages/admin/PaymentOperationsPage.tsx:674-713`, `frontend/src/pages/admin/PaymentOperationsPage.tsx:1639-1660`). |
| File and note | The exact selected `File` remains in React and in the same DOM input after a partial result. The displayed note remains byte-for-byte as entered while the request receives its normalized trimmed value. The synthetic proof retains `7/7` returned errors (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:637-672`). |
| Invocation count | Partial and full success each make one import call and one post-result Settlement-list reload. The tests observe two Settlement GETs total: one initial read plus one reload. Rapid double confirmation still produces one import (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:614-672`). |
| Failure recovery | Import transport failure makes one import and zero post-result reloads; the initial Settlement GET remains the only GET. Reload failure makes one import and one attempted reload. Both paths retain the same file object, DOM file value, and note and emit no success feedback (`frontend/src/pages/admin/PaymentOperationsPage.test.tsx:674-715`). |
| Full success | After, and only after, a successful reload with `failedRows == 0`, React file state is cleared and the keyed DOM input is replaced with an empty input (`frontend/src/pages/admin/PaymentOperationsPage.tsx:691-705`, `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:614-635`). |
| Durable mapping | The current service continues to save and audit valid rows while returning invalid row errors in one result. The mixed synthetic service case returns `importedRows=1`, `failedRows=1`, one error, one Settlement save, and one import audit (`src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:81-129`, `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:273-313`). |

The WI-041 product diff does not alter the import parser, DTO/response shape,
duplicate classification, row ceiling, or aggregate-count definition. The
frontend only distinguishes the existing `failedRows` result and removes the
five-error presentation truncation. `CR-031-115` through `CR-031-119` remain
held by their assigned later WIs.

## B. Settlement IGNORE

| Lane | Verified result |
| --- | --- |
| UI | The current UI trims and requires a nonblank note, then opens the shared confirmation with `confirmVariant='danger'`. One confirmation sends one normalized note and performs one reload (`frontend/src/pages/admin/PaymentOperationsPage.tsx:740-764`, `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:717-743`). No typed-phrase input was added or required; that shared correction UX remains owned by WI-054. |
| HTTP validation | `{}`, null, empty, whitespace-only, and trimmed 501-character notes each return `400` with zero service calls. Anonymous returns `401`; USER returns `403`; both invoke no service. A padded valid note reaches the service once with the exact authenticated ADMIN principal and normalized note (`src/test/java/com/atstudio/atstudio/controller/AdminPaymentControllerTest.java:232-294`). |
| HTTP authorization | Every Settlement mapping remains method-secured for ADMIN, and `/api/admin/**` is independently ADMIN-only (`src/main/java/com/atstudio/atstudio/controller/AdminPaymentController.java:106-149`, `src/main/java/com/atstudio/atstudio/config/SecurityConfig.java:136`). |
| Service validation | Null request, null/blank note, and trimmed-over-500 note fail before any User, Settlement, or audit access. Null, ID-less, and token non-ADMIN principals also fail before repository access. Missing, deleted, and role-drift authoritative users perform only the required locked User read, then fail before Settlement or audit access (`src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:239-273`, `src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:556-568`, `src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:510-642`). |
| Actor ownership | The service locks the current User row before the Settlement row, mutates with that authoritative User, and forwards the same authenticated principal to the audit lane. Unit `InOrder` proof is User lock -> Settlement lock -> one audit (`src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementServiceTest.java:460-508`). |
| First decision | Repeated same-note and conflicting-note requests use the explicit existing `INVALID_STATE_TRANSITION` path. The service and entity checks prevent status, actor, time, or note overwrite (`src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java:166-174`, `src/test/java/com/atstudio/atstudio/entity/PaymentSettlementTest.java:20-53`). |
| Durable/audit count | Sequential H2 retries retain the first actor, timestamp, normalized note, status, audit ID, audit creation time, and exactly one audit row. Concurrent H2 first decisions produce one winner, one `INVALID_STATE_TRANSITION` loser, and exactly one audit whose actor and note match the durable winner (`src/test/java/com/atstudio/atstudio/service/AdminPaymentSettlementIgnoreIntegrationTest.java:57-197`). |

The selected retry contract is explicit conflict, not same-decision replay. It
requires no new schema, policy, or idempotency identity.

## C. Side-Effect and Adjacent Contracts

- `AdminPaymentSettlementService` has no Provider, billing-agreement, mail, or
  external client dependency. IGNORE reaches only the locked User,
  `PaymentSettlement`, and payment-operation audit lanes.
- Import and reconciliation retain their existing read-only joins to local
  payment/refund/subscription evidence. Their only intended writes are local
  Settlement rows and corresponding Settlement audit rows. No adjacent ledger
  status is mutated (`src/main/java/com/atstudio/atstudio/service/AdminPaymentSettlementService.java:73-237`).
- Focused tests used mocks, H2, and synthetic CSV/data. Provider, payment,
  refund, subscription, billing-agreement, receipt, Incident, and mail
  invocation counts were `0` for the changed IGNORE path and UI correction
  handling.
- The adjacent frontend checks preserve exact Settlement list filters,
  multipart import, reconciliation body, and IGNORE request shape
  (`frontend/src/api/adminContracts.test.ts:191-244`). List/filter coverage and
  reconciliation failure ownership also pass
  (`frontend/src/test/coverage/adminSubscriberGaps.coverage.test.tsx:975-1142`,
  `frontend/src/pages/admin/PaymentOperationsPage.test.tsx:745-760`).

## Independent Verification

| Command | Result |
| --- | --- |
| `.\gradlew.bat test --rerun-tasks --tests "com.atstudio.atstudio.controller.AdminPaymentControllerTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementServiceTest" --tests "com.atstudio.atstudio.entity.PaymentSettlementTest" --tests "com.atstudio.atstudio.service.AdminPaymentSettlementIgnoreIntegrationTest"` | Exit `0`; 38 passed, 0 failed, 0 errors, 0 skipped. Breakdown: controller 13, service 22, entity 1, H2 integration 2. |
| `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx -t "fully successful import|mixed-result error|import request fails|required reload fails|settlement ignore"` | Exit `0`; 1 file passed; 5 passed, 81 skipped. |
| `npm test -- src/pages/admin/PaymentOperationsPage.test.tsx src/api/adminContracts.test.ts src/test/coverage/adminSubscriberGaps.coverage.test.tsx -t "settlement reconciliation|settlement import, reconciliation, filtering, and ignore contracts|loads every read-only ledger tab with representative rows and filters settlements|requires an ignore note"` | Exit `0`; 3 files passed; 4 passed, 111 skipped. |

## Limitations and Held Boundaries

- This review did not run full `Q-ALL`, coverage, typecheck, ESLint, Prettier,
  full builds, browser automation, staging, production, or real Provider/data
  verification. It is a focused independent review of the final bounded diff.
- H2 proves the exercised transaction and pessimistic-lock behavior, including
  one concurrent schedule. It is not a MySQL lock-wait, deadlock, or deployment
  rehearsal.
- The mixed HTTP-200 UI path is represented by a resolved mocked API response
  and is paired with service-level mixed durable-count proof. It is not a live
  browser-to-server network capture.
- `CR-031-115`, `CR-031-116`, and `CR-031-118` remain held for WI-067.
  `CR-031-117` and `CR-031-119` remain held for WI-056. No parser, bounds,
  duplicate, atomicity, file-audit, unusable-row, or count-conservation policy
  is approved here.
- Shared-worktree non-Settlement hunks were excluded. No real import, Provider
  or mail effect, secret/ignored-config access, `output/` or ZIP access,
  destructive action, or Git mutation occurred.

## Related Documents

- [WI-041 Handoff](WI-20260809-ATS-041-handoff.md)
- [WI-041 PG Review](WI-20260809-ATS-041-pg-review.md)
- [WI-031 Consolidated Findings](WI-20260809-ATS-031-consolidated-findings.md)
- [WI-029 Findings](WI-20260809-ATS-029-findings.md)
