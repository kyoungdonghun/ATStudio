---
version: 1.1
last_updated: 2026-07-13
project: ATS
owner: EO
category: audit
status: stable
dependencies:
  - path: ../../deliverables/user/REQ-20260711-ATS-001.md
    reason: Approved full-system audit scope
  - path: ../../deliverables/agent/WI-20260711-ATS-016-evidence-pack.md
    reason: Security and payment adjudication
  - path: ../../deliverables/agent/WI-20260711-ATS-017-evidence-pack.md
    reason: Backend, API, DB, and operations adjudication
  - path: ../../deliverables/agent/WI-20260711-ATS-018-evidence-pack.md
    reason: Frontend and UX adjudication
  - path: ../../deliverables/agent/WI-20260711-ATS-019-evidence-pack.md
    reason: Documentation and operations adjudication
  - path: p0-release-blocker-closure-20260713.md
    reason: Current remediation status for the three historical P0 findings
tier: 3
target_agents:
  - eo
  - sa
  - se
  - re
  - pg
  - docops
  - qa
  - qa-fe
  - qa-integ
  - cr
task_types:
  - security
  - architecture
  - testing
  - documentation
  - review
---

# ATStudio Full-System Audit - 2026-07-13

> Purpose: Canonical integration of WI-20260711-ATS-001 through WI-20260711-ATS-019. This report de-duplicates aliases, resolves conflicting severity decisions, records the release verdict, and defines bounded remediation and acceptance gates. It does not implement fixes.

## Remediation Status Update - 2026-07-13

The three P0 findings below remain preserved as the 2026-07-13 audit snapshot. Their approved remediation is implemented and focused-test verified in implementation commit `d11c62d`; see [P0 Release Blocker Closure Report](p0-release-blocker-closure-20260713.md). This update does not rewrite the historical inventory, matrices, or original evidence.

The broader release verdict remains **NO-GO** because separate P1, conditional deployment/database, and quality gates remain open. P0 closure is not a release-ready declaration.

## Executive Decision

| Item | Decision |
|---|---|
| Release verdict | **REJECTED / NO-GO** |
| Confirmed inventory | 36 canonical rows: 3 P0, 13 P1, 18 P2, 2 P3 |
| Conditional inventory | 5 rows requiring deployment, retained-DB, or historical verification |
| Primary reason | Three confirmed P0 paths compromise paid-content control, account-token confidentiality, or post-withdrawal financial control. Payment, untrusted-file, session, migration, and operator-safety P1 controls are also open. |
| Passing evidence | Backend tests, frontend tests, Java/TypeScript checks, ESLint, backend build, frontend build, and configured document validation passed in their respective WIs. |
| Failing or unavailable evidence | Prettier failed on 143 files; Java/frontend coverage is not measurable; MySQL migration, concurrency, production ingress, provider, and copied-DB recovery paths were not proven. |

The passing configured suites are regression evidence, not release-readiness evidence for the uncovered paths. No P0 can be waived through documentation alone. A P1 may be excluded only when its prerequisite is demonstrably disabled and the exclusion is recorded as a release gate.

## Audit Baseline and Boundaries

- Upstream evidence was captured on branch `dev/kyoung` at HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`.
- The worktree was already dirty and shared. Existing client-document changes, logs, output files, and unrelated deliverables were treated as immutable.
- Current repository inventory confirmed 147 REST mappings, 39 fresh-schema tables and 39 JPA entities, 13 agents, and 92 SR items.
- The published 53-screen statistic remains unresolved because route, lazy page component, and conceptual-screen counts use different units.
- No production DB, live payment provider, SMTP, uploaded content, secret value, deployment topology, or external client session was inspected.
- This WI changed only its two deliverables, this report, one new audit-index row, and the three explicitly permitted root-index values.

## Severity and Status Rules

- **P0:** release-blocking loss of paid-content control, live authentication capability confidentiality, or financial control.
- **P1:** high-impact security, payment, durability, or core-journey defect requiring the first remediation waves.
- **P2:** material correctness, availability, UX, performance, traceability, or maintainability defect without a proven P0/P1 impact.
- **P3:** low-risk cleanup, metadata, deprecation, or semantic alignment.
- **CONFIRMED:** current repository evidence establishes the path and missing control.
- **CONDITIONAL:** the hazardous mechanism exists, but the stated impact depends on retained data, deployment configuration, provider enablement, or history not available in the repository.
- **REJECTED:** the asserted path is contradicted or its claimed severity is not supported. A rejected severity may still leave a lower-priority confirmed finding.

## Confirmed P0/P1 Inventory and Owners

| Canonical ID | Sev. | Canonical finding | Retained aliases | Accountable / implementation / verification |
|---|---:|---|---|---|
| ATS020-P0-01 | P0 | Public track detail exposes the original audio key and public static uploads serve it outside subscription, quota, download-ledger, and license controls. | BE-001, PG-004-01, ATS008-01, ATS016-SEC-01, ATS017-C01 | PG / SE-Track / RE |
| ATS020-P0-02 | P0 | SMTP failure logging includes recipient PII and full verification/reset bodies containing live capability URLs. | BE-006, PG-004-03, ATS008-02, ATS016-SEC-02, ATS017-C03 | PG / SE-Auth-Mail / RE |
| ATS020-P0-03 | P0 | Account withdrawal does not stop an active billing agreement, so a withdrawn account remains eligible for renewal charging. | BE-002, PAY-006-01, ATS016-PAY-01, ATS017-C02 | SA / SE-Account-Billing / RE |
| ATS020-P1-01 | P1 | Subscriber playlist thumbnails accept unchecked active/non-image content and publish it under the public upload tree. | PG-004-02, ATS008-03, ATS016-SEC-03, ATS017-C13 | PG / SE-Storage-Playlist / RE |
| ATS020-P1-02 | P1 | Company certification documents rely on extension, size, and client-supplied content type before an admin is expected to open them. | PG-004-04, ATS007-F06, ATS016-SEC-04, ATS017-C12 | PG / SE-Certification / RE |
| ATS020-P1-03 | P1 | Logout, password reset, and password change do not revoke the current refresh-session capability. | PG-004-07, ATS008-04, ATS016-SEC-06, ATS017-C14 | PG / SE-Auth / RE |
| ATS020-P1-04 | P1 | File writes/deletes and DB transactions lack consistent rollback, after-commit cleanup, and durable retry across track, playlist, album, inquiry, and notice domains. | BE-007, ATS008-06, ATS016-SEC-07, ATS017-C10 | SA / SE-Storage-Domains / RE |
| ATS020-P1-05 | P1 | Java emits payment-settlement audit actions and target values omitted from executable MySQL ENUM DDL. | INT-005-01, PAY-006-02, ATS016-PAY-02, ATS017-C04 | SA / SE-Payment-DB / QA Integration |
| ATS020-P1-06 | P1 | Initial billing-confirm failure mutations can roll back with the reported `BusinessException`, losing durable failure evidence. | BE-003, PAY-006-03, ATS016-PAY-03, ATS017-C05 | SA / SE-Payment / RE |
| ATS020-P1-07 | P1 | Confirm, upgrade, and renewal commands lack local serialization, command idempotency, and unique finalization invariants. | INT-005-02, PAY-006-04, ATS016-PAY-04, ATS017-C06 | SA / SE-Payment-DB / RE |
| ATS020-P1-08 | P1 | Renewal can reuse a stale non-DONE order across billing periods or replacement subscriptions. | PAY-006-05, ATS016-PAY-05, ATS017-C07 | SA / SE-Recurring / RE |
| ATS020-P1-09 | P1 | All due agreements and provider calls run in one transaction, allowing local rollback after earlier external charge success. | BE-004, PAY-006-06, ATS016-PAY-06, ATS017-C08 | SA / SE-Recurring-Ops / RE |
| ATS020-P1-10 | P1 | Refund reservation reads an unlocked aggregate and can over-reserve the source payment under concurrency. | BE-005, PAY-006-07, ATS016-PAY-07, ATS017-C09 | SA / SE-Refund-DB / RE |
| ATS020-P1-11 | P1 | Whitelist CSV exports quote values but do not neutralize spreadsheet formula-leading cells. | ATS007-F01, ATS017-C11 | PG / SE-Whitelist / RE |
| ATS020-P1-12 | P1 | A fresh social callback calls `/users/me` without the newly issued token before committing login state. | FE-001, ATS008-05, ATS016-SEC-08 | QA Frontend / SE-Frontend-Auth / RE |
| ATS020-P1-13 | P1 | Payment and client/admin operations documents overstate readiness and instruct unsafe export/file-review workflows without launch gates or temporary safety boundaries. | DOCOPS-019-02, DOCOPS-019-03 | EO / DocOps plus each technical owner / QA Integration |

### P0 Exit Conditions

1. **Protected media:** public DTOs contain no original storage key; originals are outside public resource roots; anonymous resource-handler tests deny all original paths while intended preview delivery still works.
2. **Mail confidentiality:** SMTP failure logs contain no token, URL, body, raw recipient, nickname, or sensitive subject; the delivery outcome is explicit and tested with log capture.
3. **Withdrawal billing stop:** withdrawal makes subscription/agreement non-renewable under the approved retention policy; a due-run integration test proves zero provider calls after withdrawal.

## Conditional Inventory

| Canonical ID | Conditional severity | Condition | Required release evidence |
|---|---:|---|---|
| ATS020-X-01 | P1 | Retained MySQL DB predates the available manual patches or contains legacy certification rows. Repository artifacts do not provide a complete ordered baseline/backfill. | Inventory a copied DB, restore an ordered migration/baseline chain, apply it on disposable MySQL, backfill or disposition legacy documents, then pass Hibernate validation. |
| ATS020-X-02 | P1 | Reverse proxy/tunnel topology collapses multiple clients to one `request.getRemoteAddr()` value. | Verify trusted proxy ranges and effective client identity; prove per-client/per-account rate-limit behavior in the deployed topology. |
| ATS020-X-03 | P1 | Any environment ever accepted the historical JWT fallback value. | Sanitize history where approved, rotate affected keys, revoke sessions, and record environment-by-environment evidence without reproducing secret values. |
| ATS020-X-04 | P1 | Bootstrap test users are enabled outside a protected non-production profile. | Enforce profile and external-secret guards; prove production startup refusal when the flag is enabled. |
| ATS020-X-05 | P0 escalation of ATS020-P1-01 | Public uploads execute as active documents on the authenticated SPA origin. | Verify production ingress, origin, response type/disposition, CSP, and upload isolation. The base P1 remains confirmed regardless of escalation. |

## Confirmed P2/P3 Inventory

| Canonical ID | Sev. | Consolidated finding | Principal upstream evidence |
|---|---:|---|---|
| ATS020-P2-01 | P2 | Registration and exact identity-availability endpoints lack dedicated abuse controls; high-impact exhaustion was not measured, so the final severity is P2. | PG-004-05, ATS008-07, ATS017-X03 |
| ATS020-P2-02 | P2 | ADMIN can enter member checkout and create inappropriate admin-owned billing state, but no cross-user authorization bypass or P1 financial impact is proven. | FE-002, PAY-006-08, ATS017-D02 |
| ATS020-P2-03 | P2 | Reconciliation scans only the latest 100 orders, scans all active agreements, and lacks query-aligned pagination/index evidence. | IMP-003, INT-005-05/06, PAY-006-11 |
| ATS020-P2-04 | P2 | Provider billing-key issue/delete recovery, startup crypto validation, key rotation, scheduler zone, and replica controls are incomplete. | INT-005-04/09, PAY-006-12/14 |
| ATS020-P2-05 | P2 | Whitelist removal-completion and source-to-target transition semantics are ambiguous; `CANCELLED` is a possible non-counting terminal state, so the P1 dead-end claim is not retained. | ATS007-F02, ATS017-D01 |
| ATS020-P2-06 | P2 | Whitelist plan, primary, and export invariants are unlocked; user/admin lists and exports are unbounded. | BE-012 subset, INT-005-08, ATS007-F04/F05 |
| ATS020-P2-07 | P2 | Whitelist export ignores visible keyword filtering, can mutate a broader PII set than shown, and lacks deterministic batch recovery/re-download. | ATS007-F03 |
| ATS020-P2-08 | P2 | Certification BUSINESS-route UX, upload contract, review reason, static-method boundary, concurrency, retention, and review/download audit are incomplete. | ATS007-F07/F08/F09 |
| ATS020-P2-09 | P2 | Social-only withdrawal, provider-token parsing, album track-count ordering, Range handling, and count-then-write business limits remain backend correctness gaps. | BE-008 through BE-012 |
| ATS020-P2-10 | P2 | Subscription fetch failures collapse into inactive state, and list/tab requests can commit stale responses. | FE-003, FE-005, PAY-006-13 |
| ATS020-P2-11 | P2 | Deep-link return, global profile refresh, accessibility, modal lifecycle, playback state, playlist creation routing, and retry states are incomplete. | FE-006 through FE-012 |
| ATS020-P2-12 | P2 | Billing response examples and play-history documentation do not match current Java/TypeScript contracts and active localStorage SPA behavior. | PAY-006-10, DOCOPS-019-04/05 |
| ATS020-P2-13 | P2 | Screen inventory omits implemented admin settings/stats facts and has no approved counting unit for the published statistic. | F-004, DOCOPS-019-06 |
| ATS020-P2-14 | P2 | Required frontmatter, registry/workboard paths, CTX expectations, and current `deliverables/*` tracking are inconsistent. | F-002/F-003, DOCOPS-019-07/08 |
| ATS020-P2-15 | P2 | SR index tail/statuses, root document-count rules, Phase 2 lifecycle labels, and freshness metadata are inconsistent. | F-001/F-005/F-007, DOCOPS-019-09/10/11 |
| ATS020-P2-16 | P2 | The configured Prettier gate reports drift in 143 frontend files. | WI-012, FE-014 |
| ATS020-P2-17 | P2 | Java and frontend coverage are not measurable because no JaCoCo or Vitest coverage provider/report is configured. | WI-015 |
| ATS020-P2-18 | P2 | Fresh-schema metadata, production indexes, and H2-only schema testing do not prove MySQL migration or query behavior. | INT-005-07/10, DOCOPS-019-01 subset |
| ATS020-P3-01 | P3 | Client PDF body drift was not found, but Unicode title metadata and reproducible generator/source-hash provenance are absent. | F-006, DOCOPS-019-12 |
| ATS020-P3-02 | P3 | Legacy one-time endpoints, certification directory hints, withdrawn-login semantics, and selected stale frontend documentation need deprecation or cleanup. | IMP-005, PG-004-14/15/16, FE-013 |

## Rejected or Down-ranked Claims

| Claim | Final decision | Basis |
|---|---|---|
| Subscriber active-content path is unconditional P0 token theft | Rejected as unconditional P0; confirmed P1 with conditional P0 escalation | Production authenticated-origin execution is not repository-proven. |
| ADMIN satisfying USER routes is a broad P1 non-payment bypass | Rejected | Non-payment services retain subscription/ownership checks and admin APIs remain role-exclusive. |
| ADMIN member checkout is P1 | Down-ranked to confirmed P2 | Inappropriate admin-owned billing state is reachable, but no cross-user bypass, duplicate charge, or P1 financial impact is established. |
| Public original-stream fallback is the paid-download bypass | Rejected | The documented stream policy is separate; direct static original retrieval is the confirmed bypass. |
| Current runtime accepts a fallback JWT secret | Rejected for current code | Current configuration requires `JWT_SECRET`; only historical deployment exposure remains conditional. |
| Whitelist removal has no possible completion path and is P1 | Down-ranked to P2 ambiguity | `CANCELLED` is admin-mutable and non-counting, but its external-removal meaning is not documented. |
| All unbounded queries, request races, or accessibility gaps are P1 | Rejected at P1; retained as P2 | No measured exhaustion or P1 impact was established. |
| Client Markdown and PDF body are currently out of sync | Rejected for the frozen hash set | WI-001 found all 397 substantive lines and WI-019 confirmed unchanged hashes. |
| Passing 745 backend and 51 frontend tests closes the audit risks | Rejected | Focused resource-handler, transaction, concurrency, MySQL, session, upload, and role-journey tests are missing; coverage is unknown. |
| Validator PASS proves semantic documentation correctness | Rejected | The validator checks Tier 0 existence, Markdown link targets, ID formats, and broad index coverage, not semantic alignment or numeric counts. |

## Design-Code-Documentation Matrix

| Capability | Design intent | Current code/schema | Current docs/client wording | Verdict |
|---|---|---|---|---|
| Track preview/download/license | Public preview; subscriber original download with ledger/license | Public DTO and static resource path expose the original outside `DownloadService` | Subscriber/license model remains documented | **FAIL / P0** |
| Account withdrawal and billing | Withdrawal contract omits billing-key/agreement disposition | Active agreement remains renewable after user soft withdrawal | No payment acceptance row for withdrawal | **FAIL / P0 + policy decision** |
| Email, sessions, and social auth | Secret minimization, profile completion, credential-change safety | Mail body logging, no refresh revocation, social token-order defect | Logout/session revocation semantics are incomplete | **FAIL / P0-P1** |
| Initial charge, upgrade, renewal | Durable failed orders, period idempotency, auditable state | Failure rollback, no command serialization, stale-period reuse, batch transaction | Payment docs state broader readiness than proven | **FAIL / P1** |
| Refund and settlement | Bounded refund, audited settlement operations | Unlocked refund reservation; audit ENUM mismatch | Maker-checker and MySQL flush gates unresolved | **FAIL / P1** |
| Whitelist lifecycle/export | Defined states, plan limits, auditable status export | Formula injection, unlocked invariants, ambiguous removal transition, unbounded export | Export scope and spreadsheet risk are not disclosed | **FAIL / P1-P2** |
| Company certification | BUSINESS-only sensitive-document review | Weak file authenticity controls; incomplete UX/concurrency/audit/static boundary | Admin is instructed to download/open files without temporary safety guidance | **FAIL / P1-P2** |
| Cross-domain file lifecycle | DB and storage should converge across commit/rollback | Inconsistent pre-commit delete, rollback cleanup, and durable retry | No unified operational recovery contract | **FAIL / P1** |
| Frontend role/state journeys | Correct role gates, return URL, explicit loading/error states | Member-role overlap, stale requests, state drift, missing retry/a11y details | UI inventory and some client wording are stale | **PARTIAL / P1-P2** |
| Existing DB deployment | `ddl-auto=validate` with manual schema control | Fresh DDL defect and no complete ordered retained-DB chain | Operations docs do not define copied-DB proof as a launch gate | **FAIL / P1 conditional** |
| Play history and screen inventory | Server history and published screen count | Active SPA stores history locally; route/page counts differ | Use case, API, UI inventory, SR, and client wording conflict | **FAIL / P2** |
| Quality gates | Configured regression/build/lint/doc checks | Most configured checks pass; Prettier fails; coverage absent | Validator scope is narrower than semantic correctness | **PARTIAL** |

## Quality Results

| WI | Command / check | Result | Interpretation |
|---|---|---|---|
| WI-009 | `gradlew.bat test --rerun-tasks --console=plain` | PASS: 745/745, 0 failed/errors/skipped | Fresh backend regression baseline, not coverage or MySQL proof |
| WI-010 | `npm test` | PASS: 14/14 files, 51/51 tests | Configured frontend regression baseline |
| WI-011 | `gradlew.bat compileJava`; `npm run typecheck` | PASS; Java task was up-to-date | Compile/type contract accepted; not runtime proof |
| WI-012 | `npm run lint`; `npm run format` | ESLint PASS 0/0; Prettier FAIL 143 files | Formatting gate remains open |
| WI-013 | `gradlew.bat build`; frontend `npm run build` | PASS; backend tasks up-to-date; Vite transformed 259 modules | Incremental buildability proven, not clean deployment |
| WI-014 | Document validator; `git diff --check` | PASS; zero whitespace errors; six line-ending warnings | Implemented doc checks pass; count contract remained outside validator |
| WI-015 | Coverage capability inspection | NOT MEASURABLE | No numeric coverage claim is valid |
| WI-019 | Validator, diff check, independent counts | Validator/diff PASS; root count mismatch confirmed | Semantic and counting defects remain outside validator scope |
| WI-020 | Final validator and `git diff --check` | Recorded in `WI-20260711-ATS-020-evidence-pack.md` | Final documentation-only gate |

## Release Gates

| Gate | Required evidence | Release effect |
|---|---|---|
| G0 - P0 closure | All three P0 exit conditions and focused tests pass | Mandatory; no waiver |
| G1 - Payment integrity | Real Spring transaction tests, MySQL ENUM/schema test, concurrent confirm/upgrade/renewal/refund tests, one-period/one-finalization invariants | Mandatory before enabling real recurring payments |
| G2 - Untrusted content and sessions | Image decode/re-encode, document quarantine/signature/MIME policy, session revocation, CSV neutralization, safe operator workflow | Mandatory before public uploads/admin review/export |
| G3 - Existing DB | Ordered baseline/migrations and copied-DB rehearsal with Hibernate validate; legacy certification disposition | Mandatory for retained DB deployment |
| G4 - Runtime topology | One scheduler owner or distributed claim, explicit zone, trusted proxy behavior, key-version/rotation evidence, bootstrap profile guard | Mandatory for target environment |
| G5 - Frontend journeys | Social callback, role matrix, subscription error taxonomy, latest-request-wins, and critical a11y/retry tests | Mandatory for affected enabled journeys |
| G6 - Quality | Backend/frontend tests, typecheck, ESLint, clean builds, Prettier or approved isolated baseline, and configured coverage policy after instrumentation | Mandatory quality gate |
| G7 - Documentation | API/use-case/client/runbook alignment, validator PASS, `git diff --check` PASS, and approved root counting contract | Mandatory before release-ready declaration |

## Remediation Waves

1. **Wave 0 - Stop-loss controls:** close all P0 findings and prove their focused tests.
2. **Wave 1 - Payment and DB integrity:** settlement DDL, durable failure state, command idempotency/locking, renewal period identity/isolation, refund serialization, and copied-DB baseline.
3. **Wave 2 - Security boundaries:** active-content prevention, certification quarantine, refresh revocation, CSV neutralization, file mutation coordination, and deployment-conditional security checks.
4. **Wave 3 - Core frontend and domain correctness:** social callback, payment/member role boundary, subscription error taxonomy, request ordering, whitelist/certification lifecycle and concurrency.
5. **Wave 4 - Operations and contracts:** payment runbooks, withdrawal/key-recovery procedures, safe admin/client guidance, API response examples, play-history decision, and release rehearsal evidence.
6. **Wave 5 - Quality and maintainability:** focused coverage instrumentation, formatting baseline, bounded pagination/query work, accessibility/retry, registry/SR/inventory/metadata repairs, and PDF provenance.

Each wave must close with an Evidence Pack and an explicit WI-chain check. Documentation must describe implemented and verified behavior, not lead source fixes.

## Ordered Follow-up REQ Candidates

| Order | Candidate | Bounded scope | Exit boundary |
|---:|---|---|---|
| 1 | **P0 marketplace stop-loss** | Original-media delivery, SMTP redaction/delivery outcome, withdrawal-to-renewal stop, and three focused integration tests | All ATS020-P0 rows closed |
| 2 | **Payment command and ledger integrity** | Failure persistence, command idempotency/locks, renewal period identity/per-agreement isolation, refund reservation, settlement ENUM DDL/migration | ATS020-P1-05 through P1-10 closed on disposable MySQL/fake provider |
| 3 | **Untrusted content, session, and export security** | Playlist image pipeline, certification quarantine/download policy, file coordinator, refresh revocation, CSV neutralization | ATS020-P1-01 through P1-04 and P1-11 closed |
| 4 | **Existing-DB and deployment readiness** | Ordered baseline, legacy certification disposition, copied-DB rehearsal, scheduler/zone/proxy/key/bootstrap gates | ATS020-X-01 through X-04 resolved for target environment |
| 5 | **Frontend core journeys** | Social callback, ADMIN/member checkout boundary, subscription error taxonomy, latest-request-wins, return/profile/retry/a11y focused cases | P1-12 and affected P2 frontend rows closed |
| 6 | **Whitelist and certification workflow integrity** | Removal state decision, transition matrix, plan/primary/export serialization, export recovery/scope, certification review concurrency/audit/retention | ATS020-P2-05 through P2-08 closed |
| 7 | **Operations and contract alignment** | Payment readiness language/runbooks, safe client/admin guidance, billing response examples, play-history decision, screen-count contract | P1-13 and documentation-facing P2 rows closed |
| 8 | **Quality baseline and document hygiene** | Coverage tooling/threshold proposal, isolated Prettier baseline, registry/SR/frontmatter/count metadata, PDF provenance | Remaining P2/P3 quality and documentation rows closed |

## Root Index Counting Note

WI-019 established that the sync skill counts Standards as 12 direct non-index Markdown files, producing a pre-WI-020 total of 184. The current user-modified root index instead records Standards 13 and total 185. This WI adds one audit document and therefore changes only the permitted working-tree values: Audit 2 to 3, total 185 to 186, and the date to 2026-07-13.

The underlying counting-contract discrepancy remains ATS020-P2-15. Resolving it would require changing the Standards row and total under a separately approved scope; WI-020 does not overwrite the user's existing root-index work.

Current status (2026-07-13): WI-012 explicitly included index/count alignment. The active root index now applies the documented direct-file rule for Standards (12) and includes the new P0 closure report under Audit (4), for a total of 187. The paragraph above is retained as the WI-020 historical decision.

## Evidence Map

- Documentation baseline and PDF: `WI-20260711-ATS-001`.
- Backend and historical audit: `WI-20260711-ATS-002`.
- Frontend/UI: `WI-20260711-ATS-003`.
- Security/privacy: `WI-20260711-ATS-004`.
- DB/API/operations: `WI-20260711-ATS-005`.
- Payment 3-way matrix: `WI-20260711-ATS-006`.
- Whitelist/certification 3-way matrix: `WI-20260711-ATS-007`.
- Cross-domain adjudication: `WI-20260711-ATS-008`.
- Quality execution and coverage: `WI-20260711-ATS-009` through `WI-20260711-ATS-015`.
- Independent integrations: `WI-20260711-ATS-016` through `WI-20260711-ATS-019`.
- Final reproducibility and validation: `WI-20260711-ATS-020` Evidence Pack.

## Rollback

If explicitly requested, remove only this report, the two WI-020 deliverables, the new row in `docs/audit/index.md`, and the three WI-020 value edits in `docs/index.md`. Do not revert any client-document, log, output, source, schema, data, secret, or unrelated shared-worktree change.
