---
version: 1.0
last_updated: 2026-07-13
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260711-ATS-019
---

# Evidence Pack: WI-20260711-ATS-019

## Summary

- Independently adjudicated material documentation and operations findings, separated content defects from validator coverage and counting-contract differences, selected canonical sources for disputed facts, and ordered remediation without changing existing repository assets.

## Scope / DoD Check

- [x] Read `deliverables/agent/WI-20260711-ATS-019-handoff.md` completely (54 lines; SHA-256 `8C29173455FD557D621E0FB446A6F1D93A252AE377C37ED07CAC98D46792A690`).
- [x] Reassessed material documentation/operations findings from WI-001, WI-005, WI-006, WI-007, WI-008, and WI-014 against current files.
- [x] Recomputed document, REST API, DB table/entity, screen, agent, and SR counts.
- [x] Read the validator and index-sync implementations and separated their contracts from semantic correctness.
- [x] Identified canonical sources for each disputed fact.
- [x] Produced ordered documentation/operations remediation.
- [x] Wrote only this Evidence Pack and the paired WI-019 user summary.

## Baseline and Constraints

| Field | Value |
|---|---|
| Workspace | `C:\Users\jm991\Desktop\project\ATStudio` |
| Branch | `dev/kyoung` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Worktree | Dirty before WI-019; all concurrent/user changes were preserved. |
| Execution | Read-only inspection and commands outside the two owned outputs. |
| Forbidden work respected | No source/doc/index/PDF/config edit, SQL, provider call, deployment mutation, or rollback of shared changes. |

## Reference Documents

| Tier | Document | Use |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Active Phase 2, traceability, platform integrity |
| 0 | `docs/standards/documentation-standards.md` | Metadata and index contracts |
| 0 | `docs/standards/glossary.md` | Canonical terms |
| 1 | `docs/policies/quality-gates.md` | Evidence and operational quality gates |
| 2 | `docs/index.md`, `docs/design/`, `docs/SR/`, `docs/client/`, `docs/registry/` | Handoff documentation scope |
| Context | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| Inputs | `WI-20260711-ATS-001/005/006/007/008/014-evidence-pack.md` | Upstream claims rechecked rather than copied |
| Tool contract | `.agents/skills/validate-docs/`, `.agents/skills/sync-docs-index/` | Validator and count semantics |

## Independent Inventory Results

| Claim | Current result | Adjudication |
|---|---:|---|
| REST APIs | 147 mappings across 24 `@RestController` files | Confirmed; matches `docs/design/api-spec.md:3760-3782`. |
| DB tables | 39 `CREATE TABLE`; 39 `@Entity` files | Confirmed fresh-schema/entity count. Existing-DB migration completeness is a separate failed control. |
| Agents | 13 `.claude/agents/*.md` files | Confirmed. |
| SR items | 92 files and 92 index rows; 82 DONE | Confirmed counts; index content remains corrupt at its tail. |
| Screens | 54 lazy page components; 52 excluding two error pages; 62 route element rows | Counting unit unresolved; root claim 53 is not confirmed or disproved until a unit is selected. |
| Documentation overview | 184 under sync-skill rules | Root 185 mixes recursive Standards counting into a direct-child category contract. |

Document count detail:

| Category | Root index | Direct non-index Markdown | Recursive non-index Markdown | Canonical root rule |
|---|---:|---:|---:|---|
| Design | 24 | 7 | 24 | Recursive = 24 |
| Standards | 13 | 12 | 13 | Direct = 12 |
| All other listed categories | 148 combined | 148 combined | 148 combined | Direct |
| **Total** | **185** | n/a | n/a | **184** |

The Standards category index correctly lists the nested `public_data/standard_glossary/README.md`; that does not require the root overview to count it when the operational sync rule is direct-child for Standards.

## Confirmed Content and Operations Findings

### DOCOPS-019-01 - P1 - Existing-DB release baseline is not reproducible

- `docs/design/db-schema.md:11-19` states `ddl-auto=validate`, manual patching, and a prerequisite for earlier payment changes.
- The repository contains only `schema.sql`, `seed.sql`, and two manual patches; no Flyway/Liquibase dependency was found.
- `20260615_align_payment_whitelist_schema.sql:18-23` requires missing earlier payment migrations or a rebuild.
- `docs/design/db-schema.md:685-686` includes settlement audit enum values that executable `schema.sql:797-815` omits.
- `schema.sql:2-4,1014-1017` still says v12/38 while its current body has 39 tables and the design is v13.
- Disposition: release blocker for any retained DB whose exact baseline is unknown. Restore an ordered chain/baseline, add the audit-enum patch, and prove it on a copied MySQL DB before changing release status.

### DOCOPS-019-02 - P1 - Payment operations readiness is overstated and incomplete

- `docs/payment/known-limits-and-next-steps.md:25-36` says current delivery is complete for payment operations.
- The runbook documents one scheduler instance at `docs/design/payment-operations-runbook.md:227-249`, but current scheduled annotations have no explicit zone and scheduling is enabled on every application instance.
- The operational set lacks approved procedures for account withdrawal with active billing, provider billing-key issue/delete succeeding before local failure, multi-key rotation/rollback, copied-DB migration rehearsal, and MySQL settlement-audit flush validation.
- `docs/policies/future-policy-stubs.md:18-41` still describes Phase 1 and treats backup/release automation as deferred even though Tier 0 defines Phase 2 as active; `git revert / snapshots are sufficient` is not a DB/payment recovery procedure.
- Two-person refund approval remains unresolved at `docs/design/payment-refund-receipt-settlement-policy.md:600-607`.
- Disposition: replace completion language with implemented-but-not-release-closed status and add explicit launch gates/evidence owners. Runtime replica count, zone, secret state, and production DB state remain external verification, not confirmed misconfiguration.

### DOCOPS-019-03 - P1 - Client/admin operations guidance omits required safety boundaries

- Whitelist export is status-wide and ignores list keyword filtering (`docs/design/usecase/whitelist.md:214-235`; `docs/design/api-spec.md:3075`), while `docs/client/3-admin-checklist.md:85` only says that email/channel data is included.
- Current CSV encoding quotes values but has no formula-leading cell neutralization (`AdminWhitelistChannelService.java:171-198`).
- Company certification docs instruct admins to download/review submitted files (`docs/design/usecase/company-certification.md:129-142,182-198`; `docs/client/3-admin-checklist.md:95`). Current server validation is extension/size/count based and reuses client content type (`CompanyCertificationService.java:233-276`; `CompanyCertificationController.java:98-135`).
- Disposition: until code controls exist, operator docs must disclose export scope and prohibit spreadsheet execution/unsafe file opening. The approved target needs quarantine, signature/MIME verification, malware/active-content policy, safe response type, and export cell neutralization.

### DOCOPS-019-04 - P2 - Billing API response examples are not the current contract

- `docs/design/api-spec.md:1316-1336` omits `agreementStatus`, `subscriptionId`, and `billingCycle` from prepare and shows a contradictory `BILLING_AGREEMENT` example with non-zero amount.
- `docs/design/api-spec.md:1355-1374` shows a nested agreement object; current `BillingAgreementConfirmResponse.java:10-17` and `frontend/src/api/payments.ts:50-57` are flat.
- `docs/design/api-spec.md:1390-1423` includes `id`/`failureCount` and omits `subscription`; current `BillingAgreementResponse.java:10-18` and TypeScript lines 59-68 differ.
- Disposition: current runtime DTOs are canonical for observed behavior. Update the spec from them, or deliberately version/change the API through an approved contract decision.

### DOCOPS-019-05 - P2 - Play-history product documentation conflicts with the active SPA

- Design/API describe member-only server persistence (`docs/design/usecase/sound-playhistory.md:11-83`; `docs/design/api-spec.md:779-840`).
- Client acceptance says logged-in playback leaves history (`docs/client/2-full-feature-checklist.md:107-114`) and the role matrix excludes guests (`docs/client/0-site-policy.md:64`).
- Active SPA records every play in local storage with no auth requirement (`frontend/src/store/playerStore.ts:50-94,189-190`); `PlayHistoryPage.tsx:1-74` consumes that store. SR-89 is DONE at `docs/SR/index.md:95`.
- Disposition: current SPA behavior is canonical for present UX. Decide whether to deprecate the server API or restore synchronization, then align use case, API, role matrix, and client checklist.

### DOCOPS-019-06 - P2 - Screen inventory has stale entries and no count contract

- `docs/ui/atstudio-front-list.md:137` says the dashboard API is undefined; the API exists at `docs/design/api-spec.md:3647-3686` and in `AdminStatsController`.
- Router has a site-settings page at `frontend/src/router/index.tsx:97,209`; the API spec has settings at `docs/design/api-spec.md:3690-3727`; the UI inventory omits it.
- `docs/ui/atstudio-front-list.md:161`, `docs/index.md:71`, and `docs/registry/project-registry.md:39` state 53, while the router comment says 49 + 2 and the live component/route counts are 54/62.
- Disposition: define route, page component, or conceptual screen as the unit before publishing a numeric project stat.

### DOCOPS-019-07 - P2 - Required documentation metadata is not enforceable from current files

- Standard: `docs/standards/documentation-standards.md:45-82`.
- Current scan of 201 Markdown files: 119 without frontmatter, 19 missing required `dependencies`, 63 with all seven required fields.
- Concentration without frontmatter: SR 93, design 20, retrospective 5, UI 1.
- Current enum conflicts: statuses `accepted`, `active`, `confirmed`; category `reference`.
- Disposition: first reconcile the standard enums with accepted repository vocabulary, then remediate by category. Do not apply a blind 138-file rewrite.

### DOCOPS-019-08 - P2 - Registry/workboard contracts do not describe current operation

- `docs/registry/asset-registry.md:22` points to absent `docs/work-items/`.
- `docs/registry/workboard.md:23-36,85-89` claims all-project tracking but points to absent `docs/project/`/`docs/work-items/` paths and contains only historical examples.
- `docs/registry/project-registry.md:29` leaves the known repository as `TBD`.
- `docs/registry/context-registry.md:19` mandates PRJ + CTX for every request/report, while the active REQ/WI flow does not use CTX.
- Disposition: choose one lifecycle contract. For current ATStudio operation, `deliverables/user/` and `deliverables/agent/` are the observed artifact sources; CTX/workboard must either be made mandatory and populated or explicitly deprecated/advisory.

### DOCOPS-019-09 - P2 - SR index tail and confirmation lifecycle are corrupt

- SR inventory itself is complete: 92 files, 92 rows, statuses DONE 82 / OPEN 7 / NOT CONFIRMED 2 / DROPPED 1.
- `docs/SR/index.md:41-42` marks SR-35/SR-36 DONE, but `:104` leaves SR-C-01 OPEN.
- `docs/SR/index.md:105` ends with one U+FFFD replacement character and an incomplete SR-C-02 row.
- `docs/SR/confirm/` contains no files despite the contract at `docs/SR/index.md:3,100`.
- Disposition: reconstruct the tail from evidence/history and decide whether SR-C rows are aliases, source findings, or separately tracked artifacts.

### DOCOPS-019-10 - P2 - Root document count mixes incompatible rules

- Root: Standards 13 and total 185 (`docs/index.md:22,34`).
- Sync skill: only Design is recursive; Standards is direct-child (`.agents/skills/sync-docs-index/SKILL.md`, Target Categories and Counting Rules).
- Current values: Standards direct 12, recursive 13; all other category values match their declared rules.
- Disposition: canonical root overview = Standards 12 / total 184 unless the sync contract is deliberately changed. Category-index coverage may still include the nested README.

### DOCOPS-019-11 - P2/P3 - Lifecycle labels and freshness metadata are stale

- Tier 0 and `AGENTS.md` define React Phase 2 as active, but `docs/standards/development-standards.md:532,664,705` still labels TypeScript examples/tests as Planned.
- `docs/index.md:3` says 2026-06-18 while its current client links/count edits are from the 2026-07-11 rebuild.
- Disposition: update lifecycle labels with the owning standard change; update root metadata when the current client/index work is approved.

### DOCOPS-019-12 - P3 - PDF body drift is not found; metadata/provenance remain defective

- Current source and PDF hashes match WI-001's frozen hashes.
- PDF SHA-256: `5D5A743F9772362042EBCC5E29E3E8EC92AAE1F35EE680BBF0636823554D2DF5`.
- Binary metadata contains `/Title (ATStudio \077...)`; the Korean title is not preserved.
- Repository search found no generator command/manifest with ordered sources and hashes outside audit deliverables.
- Disposition: retain metadata/provenance finding. Reject a current source/PDF body-drift finding for the frozen hash set.

## Validator and Counting-Contract Separation

### Validator result

```text
python .agents/skills/validate-docs/scripts/validate_docs.py
Tier 0: PASS
Internal links: PASS
Traceability IDs: PASS - 296 supported IDs
Document index: PASS
Exit: 0
```

This PASS is authoritative only for implemented checks:

1. Four hard-coded Tier 0 paths exist.
2. Targets of parsed `[text](path)` links exist.
3. REQ/WI/STD strings match supported regular expressions.
4. A document path, filename, or stem appears in root or an ancestor index.

It does not check frontmatter schema/enums/freshness, semantic correctness, API/DB/SPA alignment, external URLs, bare/code-span paths, ID target existence, numeric overview counts, arithmetic, or strict parsed index edges. Therefore the confirmed content findings above are **outside validator scope**, not proof that the validator malfunctioned.

### Counting-contract decisions

| Dispute | Classification | Decision |
|---|---|---|
| Standards 12 vs 13 | Counting-rule difference | Root uses direct 12 under sync contract; category index can list recursive 13. |
| Total 184 vs 185 | Derived counting-rule difference | Root total is 184 under the same contract. |
| Screens 53 vs 52/54/62 | Undefined counting unit | No canonical number until route/page/conceptual unit is approved. |
| REST 147 | Confirmed current count | Count method mappings in `@RestController` files. |
| DB 39 | Confirmed fresh-schema count | Compare `CREATE TABLE` and `@Entity` sets; do not infer migration completeness. |
| Validator traceability PASS | Format match only | Does not prove referenced artifact existence. |

## Upstream Claim Adjudication

| Upstream claim | Final WI-019 disposition |
|---|---|
| WI-001 F-001 Phase 2 labels | Retained as DOCOPS-019-11. |
| WI-001 F-002 metadata | Retained as DOCOPS-019-07. |
| WI-001 F-003 registry | Retained as DOCOPS-019-08. |
| WI-001 F-004 screens | Split: stale dashboard/settings retained; numeric claim remains counting ambiguity. |
| WI-001 F-005 SR index | Retained as DOCOPS-019-09. |
| WI-001 F-006 PDF | Metadata/provenance retained; body drift rejected for frozen hashes. |
| WI-001 F-007 root date | Retained as DOCOPS-019-11. |
| WI-005 migration/schema/ops claims | Retained in DOCOPS-019-01/02; runtime state remains external verification. |
| WI-006 PAY-006-10 | Retained as DOCOPS-019-04 after direct DTO/spec comparison. |
| WI-006 recovery/withdrawal/policy inputs | Retained as missing operations-policy scope in DOCOPS-019-02, without duplicating code findings. |
| WI-007 export/certification operations inputs | Retained as DOCOPS-019-03. |
| WI-008 play-history drift | Retained as DOCOPS-019-05. |
| WI-014 Standards mismatch | Reclassified from a bare mismatch to a counting-contract difference; canonical root result remains 12/184. |

## Canonical Source Decisions

| Fact | Canonical source now | Required mirror/change |
|---|---|---|
| Current REST inventory | Method mappings in `@RestController` files | `api-spec.md` summary |
| Current response shape | Java response records, checked against frontend TypeScript consumers | API response examples |
| Fresh DB shape | JPA table set + `schema.sql` parity | `db-schema.md` and schema metadata |
| Existing DB upgrade | Ordered migration/baseline chain | Missing; must be created/approved |
| Current SPA behavior | `frontend/src/router/` and active components/stores | UI/client/use-case docs |
| Screen statistic | None until count unit is defined | Root/project registry/UI inventory |
| Root document count | `sync-docs-index` category rules | `docs/index.md` numeric overview |
| Current WI artifacts | `deliverables/user/` and `deliverables/agent/` | Registry/workboard contract |
| Release readiness | Rehearsal/config/runtime evidence plus approved runbooks | Do not infer from “implemented/complete” prose |

## Ordered Remediation

1. **P1 deployment baseline:** ordered DB migration/baseline, settlement audit ENUM patch, schema v13/39 metadata, copied-MySQL apply + Hibernate validate evidence.
2. **P1 operations safety:** payment withdrawal/recovery/key rotation/timezone/scheduler/refund approval gates; certification quarantine/safe review; whitelist export scope/formula/recovery procedure.
3. **P2 contract alignment:** billing response examples and play-history product decision/client wording.
4. **P2 traceability:** registry/workboard alignment with `deliverables/*` and CTX lifecycle decision.
5. **P2 inventory:** define screen counting unit; update dashboard/settings inventory and root/project stats.
6. **P2/P3 document hygiene:** metadata enum decision/category remediation, SR tail repair, root 12/184 and freshness date, Phase 2 labels, PDF title/provenance.

## Commands and Results

Read-only commands included:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
git diff --check
rg / Get-Content inspections over handoff inputs, docs, DTOs, router, DDL, patches, and validator source
PowerShell counts for category Markdown, REST mappings, CREATE TABLE, @Entity, lazy pages, route rows, agents, SR rows/statuses, and frontmatter fields
Get-FileHash -Algorithm SHA256 for client sources and PDF
rg -a -n '/Title|/Author|/Creator|/Producer' output/pdf/atstudio-client-testing-guide.pdf
```

Results:

- Documentation validator: PASS, exit 0, 296 supported IDs.
- `git diff --check`: PASS, zero whitespace errors; six LF-to-CRLF warnings on tracked client/root-index files.
- Category count check: FAIL against root overview only for Standards 13 -> 12 and total 185 -> 184 under the sync contract.
- Current-state counts: REST 147, schema/entity 39/39, agents 13, SR 92 with DONE 82.
- No Gradle/npm/browser/DB/provider/network test was run; this WI is a read-only documentation/operations review.

## Risks / Limitations / Rollback

- Production DB baseline, SQL mode, replica count, JVM/business timezone, secret state, storage controls, malware scanner, logging backend, and Toss/provider state were not accessed. Those remain external verification requirements.
- Static review proves content mismatch and absent repository controls; it does not prove the current production environment is misconfigured.
- Concurrent workers may change the shared worktree after this snapshot. Re-run counts and hashes before WI-020 integration.
- Rollback, only if explicitly requested: remove these two WI-019 outputs and no other files:
  - `deliverables/user/WI-20260711-ATS-019-summary.md`
  - `deliverables/agent/WI-20260711-ATS-019-evidence-pack.md`

## WI Chain

- Handoff declares WI-019 blocks WI-020.
- WI-020 should consume the classifications, canonical-source decisions, and ordered remediation above; no source correction was performed here.
