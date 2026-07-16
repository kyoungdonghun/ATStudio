---
version: 1.1
last_updated: 2026-07-16
project: ATS
owner: sa
category: design
status: accepted
dependencies:
  - path: ../../deliverables/user/REQ-20260716-ATS-002.md
    reason: Approved remediation requirement
  - path: ../../deliverables/agent/WI-20260716-ATS-012-handoff.md
    reason: Documentation closure contract
  - path: ../../deliverables/agent/WI-20260716-ATS-017-handoff.md
    reason: Accepted finding remediation and final verification contract
---

# Remaining Remediation Design

**Design WI:** WI-20260716-ATS-004
**Closure WI:** WI-20260716-ATS-017
**REQ:** REQ-20260716-ATS-002
**Source:** WI-20260715-ATS-020 review summary

## Decision

The remaining findings are retained as implementation, verification, or environment-conditional work. No product policy is changed by this design. The public listening policy remains full listening for all public members; subscriber downloads remain restricted to the approved subscription/download contract; card recurring payment remains the approved billing-key flow; and deployment remains single-server with no distributed scheduler lock or new PostgreSQL requirement.

## WI-010 Implementation Status

WI-010 is implemented and integration-verified on `codex/p1-acceptance-hardening`. Affected views now distinguish loading, success, legitimate empty/inactive, authorization/not-found/validation, infrastructure failure, and superseded request states. Track and payment reads, including Track available-filter tags, use cancellation plus a generation fence. Protected login returns preserve only a safe internal pathname/query, profile updates synchronize the rendered and persisted auth user, `/playlists/new` reuses the existing creation modal, and shared modal/player/toast/pagination/header accessibility contracts have focused tests. Public full-track listening, subscriber download limits, recurring-card payment, ADMIN payment routing, and BUSINESS certification routing are unchanged. Broad frontend regression remains owned by WI-014.

## WI-012 Documentation Closure Status

| Finding | Status | Direct evidence |
|---|---|---|
| P2-12 Billing and play-history SoT | CLOSED | `api-spec.md` v25, `payment-integration-design.md`, `usecase/sound-playhistory.md`, and client guides match the current Java/TypeScript DTOs and browser-local SPA behavior. |
| P2-13 Screen-count contract | CLOSED | `docs/ui/atstudio-front-list.md` records 62 path routes, 1 index redirect, 54 lazy pages, 1 modal adapter, and 53 distinct visual page UIs. `frontend/src/router/index.tsx` and `frontend-standards.md` now point to this counting contract instead of owning a stale fixed count. |
| P2-14 Metadata/registry/traceability | CLOSED | Metadata vocabulary is reconciled in `documentation-standards.md`; current tracking SoT is `deliverables/user/` plus `deliverables/agent/`; CTX/workboard are advisory. Historical records were not bulk-rewritten. |
| P2-15 Phase 2/count/freshness | CLOSED | Root/design/registry/UI indexes record active Phase 2, 149 API mappings, 41 DB tables/entities, 53 screens, 13 agents, 92 SR items, and 193 indexed documents. |
| P3-01 PDF provenance | CLOSED | `scripts/docs/generate_client_testing_pdf.py`, verifier, PDF, and manifest record the seven ordered sources, hashes, Unicode title, runtime, command, exclusions, and deterministic output. |
| P3-02 Legacy one-time lifecycle | CLOSED | API/payment/SR/client documents define recurring replacements, explicit blocked compatibility behavior, approval/evidence-based removal criteria, and rollback/document/test requirements. |

WI-011 dependency facts are current: the development branch resolves Vite 6.4.3 and both production-only and unfiltered audits report 0. The frozen client-demo branch remains read-only at Vite 6.4.1 with 5 production and 13 total findings; it is a separate environment gate, not a development-branch residual.

## WI-017 Accepted Finding Closure

| Finding | Status | Current-state evidence and retained boundary |
|---|---|---|
| F-016-01 Whitelist URL safety | CLOSED | Storage accepts normalized HTTPS YouTube hosts only, rejects credentials and non-standard ports, and both subscriber/admin views render retained unsafe values as text. Existing rows require an operator audit; they are not mutated automatically. |
| F-016-02 Entitlement correction races | CLOSED | Creation locks the subscription, rejects duplicate non-terminal corrections, execution compares the captured before-state, and retries remain idempotent. |
| F-016-03 Recent DONE reconciliation | CLOSED / ENVIRONMENT-CONDITIONAL | Recent eligible DONE orders are age/run bounded, locally refunded/cancelled orders are excluded, incident types are explicit, and an order checked during non-terminal recovery is not checked twice in the same run. Live provider evidence remains environment-conditional. |
| F-016-04 Album/playlist mutation locking | CLOSED / ENVIRONMENT-CONDITIONAL | Metadata, delete, membership, and reorder mutations use the consistent pessimistic-lock path and lock order. Unit/contract evidence is closed; retained-MySQL concurrent proof remains environment-conditional. |
| F-015-01 Certification self-service role | CLOSED | Self-service certification routes require USER plus BUSINESS qualification; BUSINESS ADMIN is denied while review routes remain ADMIN-owned. |
| F-016-05 Player subscription state | CLOSED | Loading, active, inactive, and service-error states are separate; only the structured no-active-subscription response becomes inactive, with abort and latest-request fencing. |
| F-016-06 Four stale loaders | CLOSED | Track detail, user management, user-subscription management, and download queue ignore cancellation and prevent old success/failure from replacing the current request. |
| F-016-07 / F-015-05 Admin payment contracts | CLOSED | Local IDs drive mutations, confirmation and single-submit behavior are tested, errors remain visible, and each successful mutation refreshes the current view once. Backend read/controller contracts are covered. |
| WI-013 storage compensation risk | CLOSED | Reachable journal completion, compensation retry, and idempotency paths have focused tests. |
| F-015-02 Provider identifier privacy | CLOSED | Raw provider operation keys remain server/entity-owned. Admin responses, UI, client guidance, and the deterministic PDF expose only deterministic masked `REF-` support references. |
| F-016-08 OAuth return continuity | CLOSED | A validated internal return target is stored per attempt, expires after ten minutes, is consumed once through callback/profile completion, and rejects missing, stale, external, or protocol-relative values. |
| F-016-09 / F-015-P3-03 Count ownership | CLOSED | Router and frontend standards defer to the live UI inventory; the managed document total remains 193. |
| F-015-P3-02 Playlist delete wording | CLOSED | Current use-case documentation distinguishes physical membership-row deletion from parent playlist soft deletion. |

The retained Java unchecked warning is test-harness-only: raw Spring `RestClient` Mockito stubs in `OAuth2ServiceTest`, raw `Specification` matchers in `QuestionServiceTest`, and a generic `ArgumentCaptor` in `StorageMutationCoordinatorTest`. It does not originate from production compilation and is recorded rather than broad-refactored in this remediation WI.

## Traceability Map

| WI-020 item | REQ owner | Decision / boundary |
|---|---|---|
| P2-01 | WI-005 | Account/IP abuse controls, endpoint budgets, 429 and telemetry |
| P2-02 | WI-005 | Reject ADMIN checkout at both API and UI boundaries |
| P2-03 | WI-006 | Bounded keyset pagination, batch size, and query-aligned index evidence |
| P2-04 | WI-006 | Billing-key crypto startup/rotation and single-server scheduler ownership |
| P2-05 | WI-007 | Explicit removal state transition and `CANCELLED` contract |
| P2-06 | WI-007 | Plan/primary/export invariants, uniqueness, bounded export |
| P2-07 | WI-007 | Explicit export filters, immutable batches, minimum-PII download |
| P2-08 | WI-008 | BUSINESS review reason, optimistic locking, retention, audit trail |
| P2-09 | WI-009 | Typed OAuth/provider responses and social/account/album consistency |
| P2-10 | WI-010 | Shared UI state taxonomy, abort, and latest-request-wins |
| P2-11 | WI-010 | Player error/stalled/retry/focus restoration behavior |
| P2-12 | WI-012 | Billing agreement SoT and explicit legacy play-history separation |
| P2-13 | WI-012 | Screen-counting rule and route/page/overlay registry |
| P2-14 | WI-012 | Frontmatter, registry, workboard, CTX, and deliverable traceability |
| P2-15 | WI-012 | Phase-2 root count plus freshness/status validation |
| P2-16 | WI-011 | Full-tree Prettier baseline; changed-file result is not a substitute |
| P2-17 | WI-011 | JaCoCo and Vitest coverage reporting with baseline, no forced threshold |
| P2-18 | WI-006 | Fresh-schema validation, retained-DB rehearsal, EXPLAIN/index evidence |
| X-01 | Environment condition / WI-006, WI-008 | Fresh schema may pass; retained DB needs migration/backfill rehearsal and preservation of evidence |
| X-02 | Environment condition / WI-005 | Trusted proxy CIDR and multi-egress identity evidence are deployment-dependent |
| X-03 | Environment condition / WI-005 | JWT fallback/key rotation/session expiry require environment inspection; fail closed |
| P3-01 | WI-012 | PDF manifest, source hash, generator identity, and Unicode metadata provenance |
| P3-02 | WI-012 | Legacy one-time subscription deprecation pointer and removal criteria |

## Invariants

1. **Public listening:** Publicly accessible tracks remain fully listenable. A remediation must not introduce a preview-only or member-wide listening reduction.
2. **Subscriber downloads:** Download authorization is derived from the approved subscription entitlement and download contract. Public listening is not evidence of download entitlement; download responses must not expose original/private storage paths.
3. **Card recurring payment:** Recurring card payment uses the approved billing-key lifecycle, encrypted key material, idempotent state transitions, and explicit startup/configuration guards. Provider failure never becomes success, and no duplicate charge is authorized.
4. **Single server:** Scheduler ownership, local storage assumptions, and operational locks remain single-server. Distributed locking, PostgreSQL, and deployment topology changes are out of scope and require separate approval.

## State and Boundary Rules

- Whitelist removal has an explicit terminal `CANCELLED` transition; repeated removal is idempotent.
- Payment order state is monotonic and fenced by order/agreement identity; stale callbacks cannot finalize a different order.
- Export batches are immutable after selection; filter and PII minimization are recorded with the batch.
- UI requests distinguish service failure, empty/inactive state, and cancellation; stale responses cannot overwrite newer state.
- Environment-conditional findings cannot be marked CLOSED from source inspection alone. They require named environment evidence and remain `ENVIRONMENT-CONDITIONAL` until then.

## Verification Matrix

| Area | Required proof | Owner / gate |
|---|---|---|
| Abuse and role boundary | Per-endpoint 429 tests, account/IP key tests, ADMIN checkout rejection | WI-005 / G2 |
| Payment and export concurrency | Idempotency, lock/race, unique constraint, bounded batch, pagination, EXPLAIN | WI-006 / G3 |
| State transitions | Removal, primary/plan, export, review state transition tests | WI-007, WI-008 / G3 |
| OAuth/social and player | Typed provider DTOs, stale-response, retry, abort, focus/keyboard tests | WI-009, WI-010 / G4 |
| Tooling and coverage | Full Prettier, backend JaCoCo, frontend Vitest coverage baseline | WI-011 / G5 |
| Documentation and provenance | `validate_docs.py`, index/count/freshness, PDF manifest/hash, API-DB-UI-ops semantic review | WI-012 / G6 |
| Backend regression | Full Gradle test and JaCoCo report | WI-013 / G5 |
| Frontend regression | Typecheck, ESLint, Vitest, coverage, build, Prettier | WI-014 / G5 |
| Cross-layer consistency | 3-way API/DB/UI and operational-policy review | WI-015 / G6 |
| Security and release | OWASP/security review, invariant review, release readiness | WI-016, WI-017 / G7 |
| Environment conditions | Fresh/retained MySQL, trusted proxy, JWT fallback, deployment topology evidence | X-01~03 / named environment evidence |

## Approval and Closure Boundaries

The original WI-004 authorized this design; implementation and verification proceeded through the approved REQ/WI chain ending at WI-017. WI-017 does not apply retained/production DB migrations, access secrets or live providers, alter proxy configuration, or modify the frozen client-demo worktree/runtime. Social-only withdrawal remains `POLICY-PENDING` and is not implemented. Any change to the four product invariants, payment provider set, deployment topology, or environment-dependent configuration still requires a new approved REQ or explicit approval point.
