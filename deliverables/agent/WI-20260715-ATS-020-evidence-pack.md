# Evidence Pack: WI-20260715-ATS-020

## Summary

Re-adjudicated every canonical ATS020 P1, P2, P3, and conditional X row against
the current repository snapshot and existing evidence. All thirteen P1 rows are
closed in current code/evidence. Production readiness remains open because
retained-database, live-provider, deployment, client-acceptance, tooling, and
remaining P2/P3 gates are not all closed.

This was a read-only audit. Only this Evidence Pack and the required Korean WI
summary were created.

## Scope / DoD Check

- [x] Read every handoff INPUT POINTER in Tier order.
- [x] Classified all 13 P1, 18 P2, 2 P3, and 5 X rows.
- [x] Separated product defects, deployment proof, tooling, documentation, and optional follow-up.
- [x] Stated a concrete remediation for every non-closed row.
- [x] Preserved the approved policy of public full-track listening with subscriber-only original download.
- [x] Identified stale historical claims and missing WI closure evidence.
- [x] Did not modify application code or current-state documentation.

## Audit Snapshot

| Field | Value |
|---|---|
| Date | 2026-07-15 Asia/Seoul |
| Branch | `codex/p1-acceptance-hardening` |
| HEAD | `64db91c4a216336e52ea2cabdfa9445c6a657e9b` |
| Working tree basis | Concurrent, uncommitted WI-018 backend stream and WI-019 frontend player changes plus their Evidence Packs |
| Historical audit | `docs/audit/full-system-audit-20260713.md` |
| Approved request | `deliverables/user/REQ-20260715-ATS-001.md` |
| Handoff | `deliverables/agent/WI-20260715-ATS-020-handoff.md` |

## Input Pointers Read

### Tier 0

- `docs/standards/core-principles.md`
- `docs/standards/development-standards.md`
- `docs/standards/documentation-standards.md`
- `docs/standards/glossary.md`

### Tier 1

- `docs/policies/security-policy.md`
- `docs/policies/quality-gates.md`

### Tier 2

- `deliverables/user/REQ-20260715-ATS-001.md`
- `docs/audit/full-system-audit-20260713.md`
- `docs/audit/p0-release-blocker-closure-20260713.md`
- `docs/audit/p1-remediation-trace-matrix-20260714.md`
- `docs/audit/p1-payment-integrity-closure-20260715.md`
- `deliverables/agent/` and `deliverables/user/`, including the relevant WI handoffs, summaries, and Evidence Packs

### Current Implementation

- `src/main/java/`, `src/main/resources/`, `src/test/java/`
- `frontend/src/`, `frontend/package.json`, `build.gradle`

## Classification Rules

- **CLOSED**: the defect is absent from current code and has focused or independent evidence.
- **PARTIALLY ADDRESSED**: at least one material sub-finding is fixed but a listed residue remains.
- **CONFIRMED OPEN**: current code still demonstrates the finding.
- **ENVIRONMENT-CONDITIONAL**: repository evidence cannot close the target-environment condition.
- **DOCUMENTATION/TRACEABILITY-ONLY**: no current product defect is established; the required record or contract is incomplete.
- **SUPERSEDED**: the original predicate no longer applies and any residue is tracked elsewhere.

## P1 Matrix

| ID | Status | Current evidence | Policy-preserving remediation / residue |
|---|---|---|---|
| ATS020-P1-01 | CLOSED | `CanonicalImageService`; `PlaylistService` canonicalization; `SecurityConfig` denies `/uploads/tracks/audio/**`, `/uploads/company-docs/**`, and `/uploads/questions/**`; WI-20260714-ATS-009 and WI-20260714-ATS-019 evidence. | Inventory legacy public thumbnails under X-01. Do not reduce approved public track streaming. |
| ATS020-P1-02 | CLOSED | `CompanyCertificationService` verifies PDF/JPEG/PNG structure, re-encodes images, stores under `StorageRoot.PRIVATE`, and exposes admin attachment downloads; WI-20260714-ATS-010/019. | Backfill or disposition legacy documents under X-01; keep admin-only attachment delivery. |
| ATS020-P1-03 | CLOSED | Refresh state is cleared by logout, password reset, and password change; focused replay/race evidence in WI-20260714-ATS-011/019. | Keep refresh revocation atomic without changing access-token lifetime policy. |
| ATS020-P1-04 | CLOSED | `StorageMutationCoordinator`, journal/recovery, rollback cleanup, and after-commit deletion cover the audited file domains; WI-20260714-ATS-012/019 and MySQL follow-up evidence. | Retain deployment recovery rehearsal as an operations gate. |
| ATS020-P1-05 | CLOSED | Current ENUM DDL and Java actions align; disposable MySQL schema validation passed in WI-20260715-ATS-007 and independent payment review. | Apply ordered retained-DB migration only through X-01 rehearsal. |
| ATS020-P1-06 | CLOSED | Failure persistence uses isolated transaction boundaries; package and independent review evidence in WI-20260715-ATS-001/011/012. | Preserve durable failure evidence and Provider failure semantics. |
| ATS020-P1-07 | CLOSED | Command identity, row locking, unique constraints, finalize fencing, and 7/7 MySQL race proof are recorded in WI-20260715-ATS-002/007/011/012. | Preserve idempotency keys and no-duplicate-charge invariants. |
| ATS020-P1-08 | CLOSED | Renewal identity includes billing period and subscription/agreement validation; WI-20260715-ATS-003/011/012. | Preserve current subscription lifecycle and stale-order rejection. |
| ATS020-P1-09 | CLOSED | Renewal uses bounded per-agreement phases with Provider calls outside local transactions; WI-20260715-ATS-004/011/012. | Handle multi-replica ownership under P2-04, not by broadening transactions. |
| ATS020-P1-10 | CLOSED | Refund reservation locking, lease fencing, aggregate invariants, and MySQL race evidence exist; WI-20260715-ATS-005/007/011/012. | Keep refund and entitlement correction as separate audited operations. |
| ATS020-P1-11 | CLOSED | `AdminWhitelistChannelService.neutralizeFormulaCell` and focused regression evidence in WI-20260714-ATS-013/019. | Minimize PII under P2-07 without removing required operational columns. |
| ATS020-P1-12 | CLOSED | `SocialLoginPage` stages new tokens, calls `fetchMe(res.accessToken)`, then commits; WI-20260714-ATS-014/020. | Preserve atomic login-state commit. |
| ATS020-P1-13 | CLOSED | WI-20260715-ATS-013 aligned payment/client documents; current acceptance checklist explicitly keeps retained DB, live Toss, deployment, client acceptance, and production readiness open; admin checklist prohibits real company-document actions. | Future docs must retain launch gates and test-data-only operational boundaries. |

## Conditional X Matrix

| ID | Status | Current evidence | Policy-preserving remediation / residue |
|---|---|---|---|
| ATS020-X-01 | ENVIRONMENT-CONDITIONAL | Fresh MySQL schema and Hibernate validation passed, but no retained/copy DB or legacy-row inventory is evidenced. | Inventory a DB copy, apply an ordered baseline/backfill, disposition legacy files, and pass Hibernate validation; preserve backups. |
| ATS020-X-02 | PARTIALLY ADDRESSED / ENVIRONMENT-CONDITIONAL | Trusted-proxy client identity exists (WI-20260715-ATS-016), but public smoke evidence lacked two independent egress clients. | Pin trusted proxy CIDRs and prove distinct client/account rate keys through two external clients. |
| ATS020-X-03 | ENVIRONMENT-CONDITIONAL | Repository inspection cannot establish whether any environment accepted the historical fallback JWT value. | Perform secret-free environment inventory, rotate affected keys, revoke sessions, and record evidence without reproducing secrets. |
| ATS020-X-04 | CLOSED | Acceptance startup and test-user bootstrap guards fail closed outside allowed non-production/external-secret conditions; WI-20260714-ATS-037/038/040/041. | Keep production bootstrap refusal in release checks. |
| ATS020-X-05 | SUPERSEDED | Current canonical thumbnail writes remove the active-content predicate. Any retained legacy file is covered by X-01. | Inspect legacy public files and ingress/CSP while preserving public track streaming and subscriber-only original download. |

## P2 Matrix

| ID | Status / class | Current evidence | Policy-preserving remediation |
|---|---|---|---|
| ATS020-P2-01 | CONFIRMED OPEN / product | `AuthRateLimitFilter` covers login/forgot/reset/refresh, not registration or exact email/phone/nickname checks. | Add endpoint budgets with account/IP composite keys, 429 behavior, metrics, and focused abuse tests; keep public signup. |
| ATS020-P2-02 | CONFIRMED OPEN / product | Checkout routes use generic auth and `BillingAgreementApplicationService` checks user type but not ADMIN role. | Reject ADMIN in both router/page and backend prepare/confirm paths; preserve admin management APIs. |
| ATS020-P2-03 | CONFIRMED OPEN / operations | Local reconciliation uses `PageRequest.of(0, 100)` and active agreements use `findByStatus`. | Add keyset pagination, bounded batches, query-aligned indexes, and disposable-MySQL query-plan tests. |
| ATS020-P2-04 | PARTIALLY ADDRESSED / deployment | Billing-key deletion Incident/retry exists; `BillingKeyCrypto` validates only when used, supports only `v1`, scheduled jobs omit zone, and no replica lock is configured. | Add startup validation, versioned key ring/rotation, explicit zone, and conditional distributed ownership before multi-instance deployment. |
| ATS020-P2-05 | CONFIRMED OPEN / product | Removal completion target and source-to-target transition semantics remain implicit; `CANCELLED` and `REMOVAL_REQUESTED` can be set without a formal completion contract. | Approve a transition table and completion operation while retaining the manual external whitelist workflow. |
| ATS020-P2-06 | CONFIRMED OPEN / product | Count-then-write plan checks, primary reassignment, and export selection lack per-user/row serialization; user lists and status exports are unbounded. | Add locks/unique constraints, bounded paging/export limits, and concurrent invariant tests. |
| ATS020-P2-07 | CONFIRMED OPEN / privacy/operations | Admin list accepts keyword, export accepts status only and mutates all matched PENDING rows; snapshots include broad PII and have no deterministic re-download path. | Pass explicit export filter/scope, minimize PII, persist immutable content identity, and support authorized batch re-download/recovery. |
| ATS020-P2-08 | PARTIALLY ADDRESSED / product/security | BUSINESS gate and safe private file boundary are fixed; review reason is optional, review rows are not locked/versioned, and retention/review/download audit is absent. | Require reasons by transition, add optimistic locking, retention/deletion policy, and append-only review/download audit. |
| ATS020-P2-09 | PARTIALLY ADDRESSED / product | WI-018 closes full-length no-Range/start-end/open-ended/suffix Range handling with 70 focused backend tests. Password-only withdrawal still blocks social-only users, OAuth token payloads use raw maps, and album/count limits remain count-then-write/query-order concerns. | Add provider-aware reauthentication, typed OAuth DTOs, DB-side ordered aggregates, and locked/constraint-backed limits; preserve full public streaming. |
| ATS020-P2-10 | PARTIALLY ADDRESSED / frontend | Some subscription management tests distinguish 404 from service failure, while plan/profile/drawer code still catches failures as no subscription and lacks consistent latest-request-wins. | Use a shared subscription state taxonomy plus abort/request sequencing for all list/tab fetches. |
| ATS020-P2-11 | PARTIALLY ADDRESSED / frontend | WI-019 closes Promise-based play outcome, rejected play/resume, stale resolution, media error/stalled state, and toast feedback with 7 focused tests. Shared modal focus and some return/playlist routes also exist; global profile refresh, general retry coverage, and remaining accessibility/focus restoration are incomplete. | Complete the remaining profile/retry/accessibility slices without changing the WI-019 single player-state outcome model. |
| ATS020-P2-12 | PARTIALLY ADDRESSED / documentation | Billing documentation is substantially aligned, but `atstudio-front-list.md` still presents play-history APIs while the active SPA behavior is localStorage per SR-89. | Declare localStorage as current SoT and mark backend play-history contracts legacy/deprecated. |
| ATS020-P2-13 | PARTIALLY ADDRESSED / documentation | Dashboard/site-settings APIs and the 53-screen statistic are now present, but no explicit screen counting unit/provenance is recorded. | Define route/page/overlay counting rules and publish generated provenance with the statistic. |
| ATS020-P2-14 | DOCUMENTATION/TRACEABILITY-ONLY | Frontmatter, registry/workboard/CTX expectations, and current `deliverables/*` tracking remain inconsistent. | Register current artifacts and extend validation without rewriting historical evidence. |
| ATS020-P2-15 | PARTIALLY ADDRESSED / documentation | Root counts and active Phase 2 labels are current, but SR index freshness/status metadata is not uniform. | Establish one count rule and automate SR tail/status/freshness checks. |
| ATS020-P2-16 | CONFIRMED OPEN / tooling | WI-20260715-ATS-014 observed 143 current Prettier failures; WI-015 proved they are baseline debt for the scoped gate. | Use an approved formatting-baseline WI, batch-format, then restore a full-tree blocking gate; preserve both historical verdicts. |
| ATS020-P2-17 | CONFIRMED OPEN / tooling | `build.gradle` has no JaCoCo configuration and `frontend/package.json` has no Vitest coverage provider/report. | Add both coverage toolchains, record an observational baseline, then approve thresholds separately. |
| ATS020-P2-18 | PARTIALLY ADDRESSED / deployment | Fresh MySQL schema/Hibernate validation and seven races passed after waveform schema repair; retained migration, production indexes, and query-plan proof remain absent. | Rehearse on a retained DB copy and capture index plus EXPLAIN evidence for production query shapes. |

## P3 Matrix

| ID | Status / class | Current evidence | Policy-preserving remediation |
|---|---|---|---|
| ATS020-P3-01 | DOCUMENTATION/TRACEABILITY-ONLY | Client PDF body parity was established, but Unicode title metadata and reproducible generator/source-hash provenance are not recorded. | Add deterministic generation metadata/manifest without changing approved body content. |
| ATS020-P3-02 | PARTIALLY ADDRESSED / documentation | One-time subscription endpoints are now explicitly blocked legacy paths; certification directory hints, withdrawn-login semantics, and selected stale frontend docs remain. | Publish a deprecation table and removal conditions; do not delete compatibility endpoints without approval. |

## Residual-Risk Separation

| Class | Rows / evidence | Release meaning |
|---|---|---|
| Product defects | P2-01 through P2-11 residues | Require scoped implementation WIs; no P1 re-escalation is established. |
| Deployment proof | X-01 through X-03, P2-04, P2-18 | Must be closed per target environment; repository-only proof is insufficient. |
| Tooling debt | P2-16, P2-17 | Does not rewrite WI-015 scoped PASS or WI-014 historical full-gate FAIL. |
| Documentation/traceability | P2-12 through P2-15, P3-01/02, missing WI closure packs | Requires DocOps/EO closure; historical evidence remains immutable. |
| Optional/conditional | P2-04 replica locking, P3 cleanup | Replica control becomes mandatory before multi-instance deployment; compatibility cleanup requires approval. |

## Missing Closure Evidence

- `WI-20260714-ATS-025` through `WI-20260714-ATS-034` have handoffs but no
  matching Evidence Packs. Later WIs cover portions of cross-layer review,
  documentation, tests, build, and quality checks, but the original chain is
  **DOCUMENTATION/TRACEABILITY-ONLY OPEN** until each WI is formally mapped to
  substitute evidence or closed as not executed.
- During this audit, matching Evidence Packs and concurrent uncommitted changes
  appeared for `WI-20260715-ATS-018` and `WI-20260715-ATS-019`. WI-018 records 70
  focused backend tests plus Java compilation; WI-019 records 7 focused frontend
  tests plus typecheck and scoped ESLint. They close the approved full-stream and
  player-outcome slices and were not authored or modified by WI-020.
- `WI-20260715-ATS-021` has a handoff but no Evidence Pack. Active documentation
  still contains bounded-preview wording and must be superseded without weakening
  public full-stream policy or subscriber-only original-download protection.

## Stale and Historical Claims

- The 2026-07-13 audit's open descriptions for P1-01 through P1-12 are stale as
  current-state claims, but remain valid historical findings.
- The 2026-07-14 trace matrix baseline marks P1-05 through P1-10 open; its later
  current-state appendix and the 2026-07-15 payment closure report supersede that
  baseline for current adjudication.
- Bounded-preview wording in active docs is stale after WI-018's current working-
  tree implementation. WI-021 is the pending documentation-alignment item.
- WI-20260715-ATS-014 remains a historical FAIL because of the full-tree
  Prettier gate. WI-015's scoped follow-up PASS does not overwrite it.

## Commands and Results

Read-only inspection commands included:

```text
git status --short --branch
git rev-parse HEAD
rg --files <each handoff input tree>
Get-Content / Select-String / rg against every Tier pointer and targeted code
Get-ChildItem checks for required Evidence Pack filenames
```

- Input pointer reading: PASS.
- Current snapshot identification: PASS.
- Matching Evidence Packs for WI-025..034: none found.
- Concurrent WI-018/019 Evidence Packs: found and incorporated after their files
  appeared during the audit.
- Canonical-row coverage check: PASS in both outputs; 38 unique IDs each
  (P1 13, P2 18, P3 2, X 5).
- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS; Tier 0,
  internal links, 382 supported traceability IDs, and document-index checks passed.
- No-index whitespace check for both new outputs: PASS.
- Application tests/builds: not rerun; this adjudication reuses the cited
  focused, full-suite, disposable-MySQL, and independent-review evidence.

## Risks and Rollback

- No runtime behavior, schema, configuration, or current-state documentation was changed.
- Conclusions are limited to HEAD `64db91c4...`, the observed concurrent WI-018/019 working-tree state, and the cited environment evidence.
- Live Toss, retained/production DB, production deployment, monitoring, and client acceptance remain unverified.
- Rollback is documentation-only: remove the two WI-020 deliverables. No application rollback is applicable.

## Output Pointers

- User summary: `deliverables/user/WI-20260715-ATS-020-summary.md`
- Evidence Pack: `deliverables/agent/WI-20260715-ATS-020-evidence-pack.md`
