---
version: 1.0
last_updated: 2026-07-17
project: ATS
owner: qa-integ / cr
category: audit
status: blocked
dependencies:
  - path: ../WI-20260717-ATS-008-handoff.md
    reason: Review scope and output contract
  - path: ../WI-20260717-ATS-001-evidence-pack.md
    reason: Approved 56-item execution ledger
  - path: ../WI-20260716-ATS-038-evidence-pack.md
    reason: Authoritative integrated source table
---

# WI-20260717-ATS-008 Integration Re-audit

## Verdict

**BLOCK WI-009. BLOCK repository cleanup.**

The four WI-006 P2 findings and both WI-006 P3 findings are closed in the current
tree. The 56-item ledger is structurally accounted for as 52 satisfied rows and
4 cleanup-owned rows whose read-only preconditions were previously established.
However, this re-audit found one new P3 currentness defect and could not complete
the required current runtime/API/UI and high-confidence secret-scan evidence
before the review was closed. The companion WI-008 backend and frontend reports
were also not present when inspected. The handoff requires no unresolved P1/P2/P3
and complete executable evidence, so neither WI-009 aggregation nor cleanup is
authorized.

## Findings

### P1

No P1 findings.

### P2

No new P2 product finding was established from the completed checks.

### P3-01 - Active router comment understates the ADMIN route group

`frontend/src/router/index.tsx:222` labels the following block as
`Admin (9 routes)`, but lines 223-236 contain 14 ADMIN page routes. The current
screen inventory correctly records 14 ADMIN operation UIs at
`docs/ui/atstudio-front-list.md:46`. Runtime routing is unaffected, but the
active source comment contradicts the current route inventory and fails the
requested code-document-route consistency gate.

- Severity: P3
- Reproduce: inspect `frontend/src/router/index.tsx:222-236` and compare with
  `docs/ui/atstudio-front-list.md:46`.
- Impact: maintainers can derive the wrong route-group count from active source.
- Smallest safe remediation: change only the comment to `Admin (14 routes)`,
  then rerun the route-count probe, Prettier check, and this focused review.

### GATE-01 - Current runtime/API/UI evidence is incomplete

No backend or frontend runtime was started by this integration track, and no
current local/public HTTP or role-based UI result was gathered before the review
was closed. The structural route/API/schema checks below pass, but they do not
substitute for the handoff's runtime/API/UI acceptance evidence.

- Impact: the current dirty tree has no independently confirmed startup,
  request, or browser result in this report.
- Required closure: attach current backend/frontend report evidence or run the
  approved non-mutating smoke against an already prepared disposable runtime.

### GATE-02 - Current high-confidence secret scan is incomplete

The ignored `application-local.yml` was never read or printed. A current
high-confidence added-line/untracked-text scan was not completed before the
review was closed. Prior WI-006 results cannot be treated as a fresh scan after
the dirty tree expanded.

- Impact: the mandatory secret gate is unproven for the final current diff.
- Required closure: run a value-suppressing scan over added lines and
  non-ignored untracked text, excluding `application-local.yml`, and record only
  candidate counts and safe path pointers.

### GATE-03 - Companion WI-008 reports were absent at inspection

`deliverables/agent/WI-20260717-ATS-008/backend-qa.md` and
`deliverables/agent/WI-20260717-ATS-008/frontend-qa.md` did not exist when this
report's input inventory was checked. WI-009 must not aggregate incomplete
three-track evidence.

## WI-006 Finding Closure

| Prior finding | Current status | Independent current evidence |
|---|---|---|
| `P2-01` frontend provider identity | CLOSED | `frontend/src/api/payments.ts:31,52,59` uses `TOSS`; current fixtures use `TOSS`; active `provider: 'TOSS_BILLING'` and `type: 'MOCK'` searches returned zero. `TOSS_BILLING_AUTH` remains only the valid checkout action type. |
| `P2-02` removed `/playlists/new` promise | CLOSED | Active docs and router contain no `/playlists/new` or `PlaylistCreatePage`. The only remaining phrase is in `docs/design/remaining-remediation-design-20260716.md:35`, whose front matter and notice mark it archived at lines 7-10 and 22. |
| `P2-03` deleted manual-patch instructions | CLOSED | Active policy/design paths no longer instruct use of `db/manual`; the remaining matches are in archived `docs/design/p1-payment-db-integrity-design.md`. `src/main/resources/db/manual` contains zero files. |
| `P2-04` local-config startup guidance | CLOSED | `JwtConfig.java:27-30,38-40` explicitly names `SPRING_CONFIG_ADDITIONAL_LOCATION`; `JwtConfigTest.java:25-26,39` asserts the guidance. |
| `P3-01` stale examples/commentary | CLOSED | Active frontend standard no longer names deleted `playHistory.ts` or `DataTable`; its `hooks/` and absent `features/` statements are now accurate at lines 61 and 90-91. `application.yml` has no Thymeleaf match. |
| `P3-02` unreproducible historical count | CLOSED | `docs/policies/archive-policy.md` now forbids an exact historical-file release gate without a saved path/hash manifest and requires qualitative Git evidence otherwise. The exact `1,128` claim remains only in immutable WI-005 history and is not used as a current gate. |

## 56-Item Decision Ledger

Status totals: **52 SATISFIED, 4 CLEANUP-READY/PENDING = 56**. The cleanup-ready
classification does not override this report's overall BLOCK.

| ID | Status | Current evidence |
|---|---|---|
| `INT-K01` | SATISFIED | Current claim/fence/lease, state-transition, reconciliation, audit, refund, locking, and storage-recovery sources remain in the payment and storage services; no legacy search indicated blanket removal. |
| `INT-K02` | SATISFIED | Acceptance startup guard, public URL/host/CORS controls, acceptance profile, Vite ingress, and acceptance tooling remain; production-refusal ownership is unchanged. |
| `INT-K03` | SATISFIED | OAuth attempt/state/PKCE utility and login consumers remain in the active frontend. |
| `INT-K04` | SATISFIED | `safeStorage`, request cancellation/generation fences, and explicit error/fallback paths remain; router loader uses `safeStorage` at `frontend/src/router/index.tsx:7,191`. |
| `INT-K05` | SATISFIED | `ProtectedRoute`, `SubscriberRoute`, and current route guard helpers remain at `frontend/src/router/index.tsx:3-4,110-136`. |
| `INT-K06` | SATISFIED | Recurring checkout and billing-agreement API paths remain in `PaymentController` and the SPA; one-time compatibility surfaces are absent. |
| `INT-K07` | SATISFIED | Certification/private-document and whitelist audit/export/limit contracts remain represented by current controller, entity, schema, policy, and UI paths. |
| `INT-K08` | SATISFIED | Browser-local `playHistory`, player store, full-track stream, waveform/progress, and history UI remain; server Play History is absent. |
| `INT-K09` | SATISFIED | Base/test configuration and JPA/MySQL/H2 stack remain. Independent schema/entity comparison is exact `39/39`. |
| `INT-K10` | SATISFIED | Historical zones remain governed as immutable history; current archive policy prevents unreproducible exact-count gates. |
| `INT-K11` | SATISFIED | Tracked client PDF/manifest ownership remains part of the approved generated-asset policy; no deletion appeared in the inspected status. |
| `INT-K12` | SATISFIED | Branch is `codex/p1-acceptance-hardening` at `a96d2e0c5d249723bbf449b6834299a04cf2ad30`; rollback tags remain protected by the ledger. |
| `INT-K13` | SATISFIED | Explicit demo seed workflow remains separate from `schema.sql`/`seed.sql`; `seed.sql` contains only six plans. |
| `INT-R01` | SATISFIED | Server Play History classes/client/table are absent; current docs explicitly define browser-local history. |
| `INT-R02` | SATISFIED | Download Queue backend/client/model/table and `/download-queue` are absent; `/downloads` and `/api/downloads/history` remain. |
| `INT-R03` | SATISFIED | Deprecated four-argument upgrade finalizer is absent; active fenced finalization remains. |
| `INT-R04` | SATISFIED | `previewFile`/`preview_file` is absent from active production/docs paths; full-track stream remains. |
| `INT-R05` | SATISFIED | Whitelist user/nickname snapshot fields are absent; current immutable export evidence remains. |
| `INT-R06` | SATISFIED | Thymeleaf runtime/config meaning is absent; SPA forwarding remains documented separately. |
| `INT-R07` | SATISFIED | Stale `PUT /api/settings/*` matcher is absent; current admin matcher and public read remain. |
| `INT-R08` | SATISFIED | Deleted `DataTable` source/import/current-standard example searches are clean. |
| `INT-R09` | SATISFIED | Approved unused frontend exports/types are absent; retained backend operations are documented current behavior. |
| `INT-R10` | SATISFIED | `/playlists/new` adapter/page/current promises are absent; modal creation remains the current flow. |
| `INT-R11` | SATISFIED | Three placeholder `.gitkeep` files are deleted and current frontend structure prose is accurate. |
| `INT-R12` | SATISFIED | Manual SQL directory has zero files; current V1 docs describe fresh-only schema/seed application. |
| `INT-R13` | SATISFIED | Approved demo/temp/expanded-capture/attachment output remains outside the active baseline; tracked PDF and historical ZIP are protected. |
| `INT-R14` | CLEANUP-READY/PENDING | Prunable Claude registrations/branches are cleanup-owned; actual mutation remains forbidden in WI-008. |
| `INT-R15` | CLEANUP-READY/PENDING | Merged ordinary branches are cleanup-owned; actual deletion remains forbidden in WI-008. |
| `INT-R16` | CLEANUP-READY/PENDING | Auxiliary runtime worktrees/branches are cleanup-owned and require final runtime/process confirmation before deletion. |
| `INT-P01` | SATISFIED | Active V1 crypto/property meaning is absent; V2 key-ring ownership remains. |
| `INT-P02` | SATISFIED | `schema.sql` has 39 tables, no `IF NOT EXISTS`, and matches 39 JPA table names exactly. |
| `INT-P03` | SATISFIED | `seed.sql:5-22` has one insert containing exactly six subscription-plan rows and no demo fixtures. |
| `INT-P04` | SATISFIED | Base automatic local import is absent; acceptance profile/config and explicit local-load guidance remain. |
| `INT-P05` | SATISFIED | Guarded V1 MySQL proof tests/tooling remain; the heavy MySQL suite was not rerun by this track. |
| `INT-P06` | SATISFIED | `/downloads` and `DownloadHistoryPage` are active; queue naming is absent. |
| `INT-P07` | SATISFIED | `PaymentOperationsPage` is current at `/admin/payments`; old page identity is absent. |
| `INT-P08` | SATISFIED | Production `window.confirm` search is clean; the standards correctly require controlled confirmation UI. |
| `INT-P09` | SATISFIED | Removed `mock*` checkout presentation names are absent from active production paths. |
| `INT-P10` | SATISFIED | `frontend/tsconfig.tsbuildinfo` is the sole staged deletion and is no longer intended as a tracked artifact. Staging was not modified. |
| `INT-P11` | SATISFIED | Current API/DB/UI/docs facts are aligned and `validate-docs` passes; the new P3 finding is an active source comment, not a live-doc disposition failure. |
| `INT-P12` | SATISFIED | Demo PowerShell wrapper no longer owns a hard-coded credential location; no secret value was read. |
| `INT-A01` | SATISFIED | Both selected design files carry archived metadata, notice, and replacement path. |
| `INT-A02` | SATISFIED | Historical MySQL helper/log evidence remains historical and is not a current schema source. |
| `INT-A03` | SATISFIED | Historical screenshot ZIP remains an archived asset rather than current runtime evidence. |
| `INT-V01` | SATISFIED | One-time payment controller/service/provider/DTO/frontend aliases are absent; recurring flow remains. |
| `INT-V02` | SATISFIED | Direct subscription creation endpoint/request/error compatibility path is absent. |
| `INT-V03` | SATISFIED | `/api/utils/subscription-status` and its DTO/service path are absent. |
| `INT-V04` | SATISFIED | `/api/utils/user-type` and its DTO/service path are absent. |
| `INT-V05` | SATISFIED | Admin detail GET and unused frontend wrapper are absent; emergency update/cancel remains. |
| `INT-V06` | SATISFIED | Guarded QA bootstrap remains disabled-by-default/non-production and baseline-validating. |
| `INT-V07` | SATISFIED | Backend enum is only `TOSS`; frontend response contracts use `TOSS`; provider-neutral extension interfaces remain. |
| `INT-V08` | SATISFIED | Automatic local import is absent and runtime guidance requires explicit additional-location loading. The ignored local file was not read. |
| `INT-V09` | SATISFIED | Approved frontend package identity remains `0.1.0`. |
| `INT-V10` | CLEANUP-READY/PENDING | Unique branch tips are archive-tag governed; final local branch deletion remains cleanup-owned. |
| `INT-V11` | SATISFIED | Runtime logs are generated/ignored lifecycle evidence, not current SoT; final process ownership still belongs to cleanup preflight. |
| `INT-V12` | SATISFIED | ADMIN subscription update/cancel mappings, security boundary, UI consumers, and emergency-operation documentation remain. |

## Mechanical Evidence

### Commands and results

- `python .agents/skills/validate-docs/scripts/validate_docs.py`: PASS; Tier 0
  present, zero broken internal links, 435 supported traceability IDs, complete
  document index.
- Source-derived controller count: PASS; 23 controller classes and 137
  method-level mappings (`GET 65`, `POST 36`, `PUT 21`, `DELETE 15`).
- Expanded Java/API-spec route-set comparison: PASS; `137/137`, zero routes
  present on only one side.
- `schema.sql`/JPA table-name set comparison: PASS; `39/39`, zero set
  differences, zero `IF NOT EXISTS`, zero manual SQL files.
- Legacy/residual searches: removed Play History, Download Queue, preview fields,
  export snapshots, one-time payment classes/routes, dormant utility APIs, old
  page identities, native production confirms, and stale provider values are
  absent from active production/current-doc paths. Archived and explicit
  negative-policy references were not treated as consumers.
- `git -c core.safecrlf=false diff --check HEAD`: PASS.
- `git -c core.safecrlf=false diff --cached --check`: PASS.
- Review snapshot: 218 tracked changed files, 3,472 insertions, 17,615
  deletions, 104 non-ignored untracked paths, and one staged path
  (`frontend/tsconfig.tsbuildinfo`). The index was not modified.

### Evidence not rerun

- No full backend/frontend suite or coverage run was duplicated by this track.
- No current runtime startup, HTTP/API smoke, or browser/UI smoke was completed.
- No current high-confidence secret scan was completed.
- No Git ref, index, worktree, tag, process, database, product code, or active
  document was changed.

## Injected and Read Inputs

- Tier 0: `docs/standards/core-principles.md`,
  `docs/standards/development-standards.md`,
  `docs/standards/documentation-standards.md`,
  `docs/standards/glossary.md`, and
  `docs/standards/frontend-standards.md`.
- Tier 1: quality, security, access-control, versioning, and archive policies.
- Current SoT inspected: `docs/design/api-spec.md`,
  `docs/design/db-schema.md`, targeted `docs/ui/`, `docs/payment/`,
  `docs/client/testing-guide.md`, `docs/registry/project-registry.md`, current
  controllers/entities/schema/seed/router, and affected WI-007 repair paths.
- Decision/evidence: approved REQ, WI-001 ledger, WI-038 integrated source
  table, WI-006 integration review, and WI-007/WI-008 handoffs.

## Cleanup Preconditions

Structural cleanup rows `INT-R14`, `INT-R15`, `INT-R16`, and `INT-V10` remain
pending by design. Even if their prior reachability and dry-run evidence is
unchanged, cleanup must not begin while:

1. P3-01 remains open.
2. Current runtime/API/UI and secret-scan evidence is absent.
3. The companion WI-008 backend/frontend reports are unavailable.
4. The working tree remains unstaged/uncommitted except for the approved
   generated-cache deletion.

## Final Authorization

- **WI-009 evidence aggregation: BLOCK.** Aggregate only after P3-01 is repaired
  and all three current WI-008 reports plus missing gates are present.
- **Branch/worktree/tag/log cleanup: BLOCK.** Do not delete or prune refs,
  worktrees, tags, or runtime evidence from this report.
- **Final staging/commit: BLOCK.** This review did not modify the index and does
  not authorize broad staging.

## Rollback

This review created only
`deliverables/agent/WI-20260717-ATS-008/integration-review.md`. Rollback is
deletion of this report only. No product, document, configuration, database,
Git index/ref, worktree, tag, process, or runtime rollback applies.
