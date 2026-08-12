---
version: 1.0
last_updated: 2026-08-12
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260809-ATS-034
dependencies:
  - path: WI-20260809-ATS-034-handoff.md
    reason: Approved implementation and closeout contract
  - path: WI-20260809-ATS-034-pg-review.md
    reason: Final security decision and remediation history
  - path: WI-20260809-ATS-034-qa-integ-review.md
    reason: Final cross-layer decision and remediation history
  - path: WI-20260809-ATS-034-re-review.md
    reason: Final reliability decision and verification evidence
---

# Evidence Pack: WI-20260809-ATS-034

## Summary

- Closed `CR-031-084` / `ATS-027-F04` with owner-scoped read recovery,
  canonical aggregate proof, four frozen user outcomes, and zero automatic
  financial mutation replay.

## Scope / DoD Check

- [x] Callback success is announced only after exact `DONE` and canonical
      Subscription/Billing Agreement linkage proof.
- [x] Mutation success plus failed canonical reload remains `RELOAD_FAILED`.
- [x] Lost callback and charged-upgrade responses use USER-only owner-scoped
      reads; ambiguous results remain `UNKNOWN`.
- [x] `UNKNOWN` warns against repeating the operation and exposes read-only
      status recheck.
- [x] Cancel/reactivate response loss converges through canonical reads without
      automatic replay.
- [x] Manage preserves successful result classification and blocks all
      mutations during `UNKNOWN` or `RELOAD_FAILED`.
- [x] Outcome responses are minimal and contain no secret, Provider payload, or
      PII.
- [x] Read recovery performs zero Provider calls, zero mutation calls, and zero
      finalization calls.
- [x] WI-032 intent/audience/amount controls and WI-033 prepare identity remain
      unchanged.
- [x] PG, QA-INTEG, and RE final decisions are `APPROVE` after the recorded
      BLOCK findings were remediated.
- [x] Payment design, API specification, screen flow, Evidence Pack, and Korean
      user summary are current.

## Reference Documents (Tier 0-2)

<!-- prettier-ignore -->
| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Language, traceability, and payment integrity |
| 0 | `docs/standards/documentation-standards.md` | Metadata, structure, links, and two-set format |
| 0 | `docs/standards/development-standards.md` | Evidence pointers, tests, coverage, and rollback |
| 0 | `docs/standards/glossary.md` | Canonical Subscription and WI terminology |
| 1 | `docs/policies/quality-gates.md` | Review, regression, and closeout gates |
| 2 | `docs/design/api-spec.md` | Owner-scoped outcome API contract |
| 2 | `docs/design/payment-integration-design.md` | Four-state and recovery invariants |
| 2 | `docs/ui/screen-flow.md` | Callback and Manage retry UX |
| WI | `deliverables/user/REQ-20260809-ATS-001.md` | Approved parent request |
| WI | `deliverables/agent/WI-20260809-ATS-034-handoff.md` | Approved scope, exclusions, and output contract |

WI-032 and WI-033 review/evidence files were used only as local closeout format
references. Their historical bodies were not rewritten.

## Reviewer Decisions

| Review | Honest history | Final decision |
| --- | --- | --- |
| PG | Initial BLOCK 4: URL keys, broad `4xx`, ACTIVE-only proof, and missing agreement linkage. All remediated. | `APPROVE` |
| QA-INTEG | Initial BLOCK 4: fail callback, scheduled invariant, Manage linkage, and race. All remediated. | `APPROVE` |
| RE | First BLOCK covered mutation lock, order linkage, stale preview, and terminal errors; the next BLOCK covered charged post-Provider allowlisting. All remediated. | `APPROVE` |

No individual reviewer names are asserted; the artifacts record role decisions
only.

## Frozen Outcome Decision Table

| Outcome | Proof predicate | Allowed UI action | Automatic mutation replay |
| --- | --- | --- | ---: |
| `COMMITTED` | Exact `DONE` where applicable plus fresh Subscription and Billing Agreement reads proving exact target and the same `userSubscriptionId` aggregate | One success message and success navigation | 0 |
| `FAILED` | Terminal order `FAILED`/`CANCELLED`/`EXPIRED`, or a narrow terminal local-only cancel/reactivate error | Show authoritative failure; a later explicit user action may start separately | 0 |
| `RELOAD_FAILED` | Mutation response reported success, but required outcome or canonical reload failed | Preserve success context; read-only `status again` | 0 |
| `UNKNOWN` | Neither durable success nor terminal failure is proved | Warn that processing may already have completed; read-only `status again` | 0 |

## Evidence Pointers

### API And Read Model

- `src/main/java/com/atstudio/atstudio/controller/PaymentController.java:83-108`
  exposes the two wrapped outcome GETs.
- `src/main/java/com/atstudio/atstudio/dto/payment/PaymentCommandOutcomeResponse.java:8-24`
  limits the response to five intent/status/linkage fields.
- `src/main/java/com/atstudio/atstudio/service/PaymentRecoveryReadService.java:34-82`
  applies owner/purpose filters and exact current-period upgrade identity.
- `src/main/java/com/atstudio/atstudio/repository/PaymentOrderRepository.java:37-49`
  requires owner ID with exact order ID or command key.
- `frontend/src/api/payments.ts:76-128` defines the bounded outcome DTO and two
  encoded GET helpers.

### Callback And Manage UI

- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:104-215` removes
  URL keys, reads outcome, and requires canonical aggregate linkage.
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:217-277` sends at
  most one callback confirmation and reconciles both success/fail paths.
- `frontend/src/pages/subscriber/SubscriptionPaymentPage.tsx:479-503` exposes
  read-only status recheck for ambiguous/reload states.
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:237-281` freezes
  canonical source, target, pending-change, and Billing Agreement invariants.
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:329-468` blocks
  ambiguous mutations, verifies exact charged outcome linkage, and fences stale
  reads.
- `frontend/src/pages/subscriber/SubscriptionManagePage.tsx:586-715` gives the
  successful response priority and applies narrow local terminal errors while
  reconciling every CHANGE error.

### Focused Tests

- `src/test/java/com/atstudio/atstudio/service/PaymentRecoveryReadServiceTest.java:53-119`
  covers exact owner read, foreign/absent equivalence, and deterministic upgrade
  lookup.
- `src/test/java/com/atstudio/atstudio/service/PaymentRecoveryReadIntegrationTest.java:74-143`
  covers owner isolation, exact-not-latest lookup, and zero Test-Provider calls.
- `frontend/src/pages/subscriber/SubscriptionPaymentReplay.test.tsx:92-120`
  proves URL key removal before confirmation settles.
- `frontend/src/pages/subscriber/SubscriptionManagePage.test.tsx:604-1560`
  covers reload/unknown locks, linkage, response priority, terminal error sets,
  race fencing, and scheduled/downgrade source invariants.

### Documentation Closeout

- `docs/design/api-spec.md:328-391` records endpoints, owner isolation, minimal
  response, exact identity, period-boundary caveat, and read-only guarantees.
- `docs/design/payment-integration-design.md:220-290` freezes four-state,
  callback, and Manage recovery semantics.
- `docs/ui/screen-flow.md:111-140` records current retry UX and removes future
  WI-034 wording.
- `docs/index.md`, `docs/design/index.md`, and
  `docs/registry/project-registry.md` synchronize the current backend count to
  146 method-level mappings and reference API Specification v28.7 where the
  local index/registry convention includes an exact version.
- `docs/client/_internal-feature-map.md` synchronizes its exact current REST API
  count to 146 method-level mappings across 25 controller classes.

## Cross-Layer Evidence

| Scenario | UI/control | API and server | Provider boundary | Durable test state |
| --- | --- | --- | --- | --- |
| Callback success | No success until canonical proof; URL keys removed immediately | One confirm at most, then exact owner order GET and canonical reads | No Provider call from GET; no automatic second confirm | `DONE` plus exact Subscription/Billing Agreement aggregate linkage |
| Callback fail | Shows checking/ambiguous state rather than route-derived failure | Exact owner order GET when `orderId` exists | 0 mutation replay | Terminal status becomes `FAILED`; otherwise remains `UNKNOWN` |
| Charged upgrade loss | Manage disables all mutations and offers read retry | Exact current-period target command GET, then canonical reads | 0 charge replay and 0 Provider call from GET | Exact `DONE`, target, aggregate ID, and agreement linkage required |
| Scheduled/downgrade loss | Preserves operation context | Canonical Subscription/Billing Agreement reads only | 0 Provider calls | Source aggregate/plan/cycle and exact pending target/cycle must match |
| Cancel/reactivate loss | No automatic second mutation | Canonical reads; only narrow terminal response errors bypass reconciliation | Local-only mutation is not replayed | Exact cancelled/active Subscription and Billing Agreement pairing |

## Commands And Outputs

The product verification below is the supplied final green evidence. DocOps did
not rerun product tests during this documentation-only closeout.

| Verification lane | Final recorded result |
| --- | --- |
| Backend full test + coverage + build | `BUILD SUCCESSFUL`; 1,454 tests, 0 failures, 16 skipped |
| Backend coverage | instruction 86.332%; line 86.574%; method 83.9%; branch 71.29% |
| Frontend full test | 72 files; 721/721 tests PASS |
| Frontend coverage | statements 87.99%; lines 90.2%; functions 87.43%; branches 78.95% |
| Frontend static/build | typecheck, ESLint, Prettier, and build PASS; Prettier passed after one formatting-only correction |
| Non-blocking output | React Router v7 future-flag warning only |

DocOps closeout commands:

- `python .claude/skills/validate-docs/scripts/validate_docs.py` - exit 0;
  Tier 0 documents, internal links, 547 supported traceability IDs, and document
  index coverage all passed.
- `git diff --check` - exit 0 with no whitespace errors; existing Java working
  files emitted only non-blocking CRLF-to-LF conversion warnings.

## Risks / Rollback

- Automated H2/Test-Provider and React evidence does not prove live Toss,
  deployed browser, production DB, or operational recovery behavior.
- Exact upgrade recovery is deliberately period-bound. After the current
  period changes, an older upgrade command is not replaced by a latest-order
  guess and may require the existing operational reconciliation path.
- Callback authorization values still arrive through the Provider redirect
  boundary before immediate URL replacement; this WI minimizes browser-history
  retention but does not redesign that Provider protocol.
- Roll back only WI-034 product/test hunks and the 12 documentation closeout
  files listed here. Preserve WI-032/WI-033 safeguards and unrelated shared
  worktree changes. No schema/data rollback, Provider reversal, charge reversal,
  or refund is required.

## Side-Effect And Git Record

- No real Toss/SDK/provider charge, refund, cancellation, mail, secret access,
  retained database action, or deployment occurred.
- No commit, stage, push, branch, merge, or destructive cleanup occurred.
- The preserved output ZIP and all unrelated output files were untouched.

## Follow-Up Chain

WI-034 completion unblocks WI-035 and WI-052. This DocOps assignment created no
handoff and invoked no subagent; Main retains responsibility for the approved
WI chain.

## Related Documents

- [PG Review](WI-20260809-ATS-034-pg-review.md)
- [QA-INTEG Review](WI-20260809-ATS-034-qa-integ-review.md)
- [RE Review](WI-20260809-ATS-034-re-review.md)
- [WI-034 User Summary](../user/WI-20260809-ATS-034-summary.md)
- [API Specification](../../docs/design/api-spec.md)
- [Payment Integration Design](../../docs/design/payment-integration-design.md)
- [Screen Flows](../../docs/ui/screen-flow.md)
