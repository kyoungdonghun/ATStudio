---
version: 1.0
last_updated: 2026-07-16
project: ATS
owner: docops
category: evidence-pack
status: stable
related_wi: WI-20260716-ATS-012
dependencies:
  - path: ../user/REQ-20260716-ATS-002.md
    reason: Approved remediation scope
  - path: WI-20260716-ATS-012-handoff.md
    reason: Documentation and PDF execution contract
  - path: WI-20260716-ATS-011-evidence-pack.md
    reason: Latest dependency, formatting, and coverage evidence
  - path: ../../docs/design/remaining-remediation-design-20260716.md
    reason: P2/P3 finding ownership and current closure status
---

# Evidence Pack: WI-20260716-ATS-012

## Summary

Reconciled the approved design and WI-005 through WI-011 evidence against current Java, React, schema, and tooling sources; updated the live design/UI/payment/SR/registry/client documentation; and produced a deterministic, provenance-recorded Korean client PDF. Product code, schema, runtime, provider, database, client-demo worktree, and Git history were not changed.

The 2026-07-16 user clarification supersedes the handoff's older dependency-residual wording: the development branch has Vite 6.4.3 and both production-only and unfiltered audit totals are zero. Findings on the frozen client-demo branch are recorded only as a branch-specific environment boundary.

## Scope and DoD

- [x] Current Java/TypeScript DTOs, controllers, entities, schema, router, pages, store, package, and tooling configuration used as observed-behavior evidence.
- [x] Billing agreement examples match current flat/nested response shapes and `BILLING_AGREEMENT` amount 0 re-registration.
- [x] Active browser-local SPA history is separated from retained server history compatibility APIs without invented synchronization.
- [x] API, DB, route/page/modal, agent, SR, metadata, and managed-document counts reproduced from current files.
- [x] Admin stats and site-setting behavior reconciled.
- [x] SR tail, SR-C aliases, empty `confirm/`, registry authority, Phase 2 freshness, and deliverable SoT reconciled.
- [x] Legacy one-time surfaces have replacements and evidence/approval/removal criteria; no code was removed.
- [x] WI-005 through WI-011 facts are represented in current documents and Korean client checks.
- [x] Seven-source PDF and manifest reproduce deterministically with Unicode title and all-page render evidence.
- [x] Structural docs validation, count sync, U+FFFD, PDF, diff, and branch-boundary checks passed.
- [x] `tmp/pdfs/` intermediates removed after final visual verification.
- [ ] P2-13 code-comment parity remains open: old numeric comments in `frontend/src/router/index.tsx` are stale. WI-012 is explicitly forbidden from editing product code; source declarations and current docs agree at 53 visual UIs.

## Reference Documents

Tier 0: `docs/standards/core-principles.md`, `docs/standards/documentation-standards.md`, `docs/standards/glossary.md`, `docs/standards/development-standards.md`.

Tier 1: `docs/policies/quality-gates.md`, `docs/policies/security-policy.md`, `docs/policies/archive-policy.md`, `docs/architecture/system-design.md`.

Tier 2 and evidence: the handoff-listed design/use-case/UI/payment/SR/registry/client documents; WI-005 through WI-011 evidence packs; 2026-07-11 and 2026-07-13 audit/evidence records; current Java, React, schema, package, and Gradle files. Historical audit/evidence files were treated as time-bound records and were not rewritten.

## Three-Way Semantic Matrix

| Topic | Approved design / prior WI evidence | Current implementation evidence | Current document outcome |
|---|---|---|---|
| Billing agreement | Recurring billing keys are the active subscription path; re-registration must not charge | `BillingAgreementPrepareResponse.java:11-21`, `BillingAgreementConfirmResponse.java:10-16`, `BillingAgreementResponse.java:10-18`; `BillingAgreementApplicationService.java:108-140` selects `BigDecimal.ZERO`; `frontend/src/api/payments.ts:25-67` matches shapes | `api-spec.md` v24, `payment-integration-design.md`, payment guides, UI flow, and client checks show amount 0 and flat confirm/current DTOs |
| Play history | SR-89 makes browser-local history the active SPA UX | `playerStore.ts:52-95,257-260` uses `playHistory`, max 100, de-duplication, post-start write; `PlayHistoryController.java:17-43` and `playHistory.ts` remain but have no active SPA consumer | `sound-playhistory.md:23-69`, `api-spec.md:895-945`, UI/client docs explicitly separate local SoT and server compatibility |
| API count | Current contract must state the unit | 24 files annotated `@RestController` contain 149 method-level verb mappings; `SpaForwardController` adds one non-REST forwarding mapping | Root/design/registry/internal-client documents publish 149 with inclusion/exclusion rule |
| Screen count | Route entries, page UIs, aliases, and overlays are separate units | Router has 62 `path` objects, 1 `index`, 54 `lazyPage` declarations, 8 `SubscriptionPaymentPage` route references; non-test TSX has 23 `<Modal` occurrences in 17 files | `atstudio-front-list.md:21-32` publishes 53 visual UIs and records stale inline router comments as a code-owned follow-up |
| Admin stats/settings | Inventory must follow real controllers and DTOs | `DashboardStatsResponse.java:8-11` has four fields; `AdminStatsService.java:26-35` populates them; public/admin setting controllers read/upsert `COMPANY_CERT_GUIDE`, absent public value is empty | API/UI/admin client documents match fields, route, empty default, upsert, and current validation |
| WI-005 security | Rate limits, trusted-proxy boundaries, logout/session behavior, and public exposure must be explicit | Current security/rate-limit sources and focused 43-test evidence remain intact | Security/API/client/current-state documents retain environment conditions without claiming proxy/JWT proof |
| WI-006 payment hardening | Bounded full reconciliation, key rotation compatibility, and scheduled operations | v2 key-ID encryption with v1 read compatibility; all eligible reconciliation with capped detail; cron zone defaults to Asia/Seoul | Payment design/runbook/index/client checks carry current operational behavior and retained-DB/provider conditions |
| WI-007 whitelist | Saved-channel cap, plan allowance, immutable export, and workflow states are distinct | Current services/controllers/schema and frontend implement technical cap 100, plan-based active allowance, bounded immutable export/re-download, and formula-neutralized CSV | Whitelist use case, API spec, UI/client checks preserve the distinction and transition behavior |
| WI-008 company certification | Private documents, bounded reasons, audit minimization, and BUSINESS routing | Current code accepts PDF/JPG/JPEG/PNG and canonicalizes images, bounds reasons at 500, omits document paths, records narrow review/access audit | Certification use case, security policy, API/UI/client docs match; retention/malware policy remains pending |
| WI-009 OAuth/catalog/download | Typed provider parsing and cooperating locks must not be overstated as live-provider/retained-DB proof | Typed OAuth responses and catalog/download lock behavior remain in current sources and 68-test evidence | API/use-case/client docs state local proof and environment-conditional real provider/MySQL evidence |
| WI-010 frontend | Loading/error/empty/stale-request/a11y behavior must be distinct | Current route guards, abort/generation fences, modal/player/toast/pagination/header behavior and 180-test evidence remain intact | UI and Korean client checks include retry/error, safe redirects, focus restoration, and keyboard expectations |
| WI-011 tooling | Coverage is observational; dependency claims are branch-specific | Development `package.json` declares Vite `^6.4.3`; resolved 6.4.3; current production/unfiltered audit totals are both 0 | Current docs contain no development residual 8 claim; low coverage remains visible; frozen client-demo findings are a separate environment boundary |
| SR/registry/tracking | Existing SR files and two-set deliverables are authoritative | 92 SR main rows; `confirm/` has no files; SR-C-01/02/04 are aliases to SR-35/36/37; no SR-C-03 | SR index tail/statuses/aliases corrected; registries/workboard mark deliverables as SoT and CTX/workboard as advisory |
| Legacy lifecycle | Compatibility must not be silently removed | Blocked direct/one-time endpoints and eight payment routes still exist | API/payment/SR/client docs require no callers, telemetry/observation, replacement coverage, coordinated approval/update, and rollback before removal |

## Current Count Evidence

| Surface | Command rule | Result |
|---|---|---:|
| REST controllers/mappings | Select Java controller files with `@RestController`, then count method-level `@(Get|Post|Put|Patch|Delete)Mapping` only in those files | 24 / 149 |
| Non-REST forward | Count the same verb annotations in `SpaForwardController.java` separately | 1 excluded |
| DB tables/entities | Count `^\s*CREATE TABLE` in `schema.sql`; count Java files containing `^\s*@Entity` | 41 / 41 |
| Router | Count `\bpath:` and `\bindex:\s*true` in `frontend/src/router/index.tsx` | 62 + 1 = 63 |
| Page UI | Count `^const ...Page = lazyPage(`, then subtract the `/playlists/new` modal adapter | 54 - 1 = 53 |
| Payment aliases | Count `<SubscriptionPaymentPage />` route references | 8 references, 7 extra aliases |
| Modals | Count `<Modal` matches and unique non-test TSX paths | 23 / 17 files |
| Agents | Count `.claude/agents/*.md` | 13 |
| SR | Parse `docs/SR/index.md` rows matching `^| SR-[0-9]+` and group status column | 92: 82 DONE, 7 OPEN, 2 NOT CONFIRMED, 1 DROPPED |
| Managed docs | Apply sync skill: Design recursive; other categories direct only; exclude category `index.md` | 193 |

Index breakdown: Architecture 1, Design 29, Policies 8, Standards 12, Templates 18, Registry 4, Audit 6, Client 8, Payment 7, SR 92, Retrospective 4, ADR 1, UI 3, Eval 0.

Metadata scan covers a different unit: all 210 Markdown files under `docs/`, including index/reference files. Of these, 96 have frontmatter and 114 do not; existing frontmatter has 0 invalid `category` values and 0 invalid `status` values. WI-012 expanded the allowed vocabulary and repaired targeted live documents only; it did not bulk-insert metadata into historical records.

## Dependency and Environment Evidence

Development worktree, read-only audit on 2026-07-16:

| Check | Result |
|---|---|
| Branch | `codex/p1-acceptance-hardening` |
| Declared/resolved Vite | `^6.4.3` / `6.4.3` |
| `npm audit --omit=dev --json` | exit 0; total 0 |
| `npm audit --json` | exit 0; total 0 |

Frozen client-demo worktree, read-only audit on 2026-07-16:

| Check | Result |
|---|---|
| Branch / HEAD | `codex/client-demo-stable` / `cd876fcf84b3cb2490c27420c6c53a87a35b982d` |
| Status | clean before and after |
| Declared/resolved Vite | `^6.0.5` / `6.4.1` |
| Production audit | exit 1; 5 total = 3 moderate + 2 high |
| Unfiltered audit | exit 1; 13 total = 1 low + 6 moderate + 6 high |
| `package.json` SHA-256 | `104fc84aeaceb8db78baf9855df5e45eda3f7f4a09babfcac982a5de412dfb43`, unchanged |
| `package-lock.json` SHA-256 | `2704fd97c5993bafd3d72bdca55216bfad7822f6558f10d9dc739d98f9d424d0`, unchanged |

The additional eight findings on the frozen branch are dev-only relative to that branch's production audit. They are not a development-branch residual. No client-demo file, process, or public runtime was changed.

WI-011 coverage observations remain visible and are not thresholds: backend instructions 76.15%, branches 59.05%, lines 76.81%; frontend statements 34.49%, branches 34.00%, functions 27.82%, lines 35.43%.

## PDF Provenance

Generator: `scripts/docs/generate_client_testing_pdf.py` version 1.3.0, Python 3.12.13, ReportLab 4.4.9, pypdf 6.10.0, `reportlab_invariant=1`. Verification script: `scripts/docs/verify_client_testing_pdf.py`.

Command:

```powershell
& 'C:\Users\jm991\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' scripts/docs/generate_client_testing_pdf.py
```

Ordered sources and manifest hashes:

| # | Source | SHA-256 |
|---:|---|---|
| 1 | `docs/client/testing-guide.md` | `8ac40c24c73882ae64d4725f8992eb3d645aebbb8d6053e844b8851be6ef63d3` |
| 2 | `docs/client/1-quick-checklist.md` | `d4dcdca8556684b26c5235f79846e88f0f4ce19e622266d2b0c8263a092e3f89` |
| 3 | `docs/client/2-full-feature-checklist.md` | `7a7bcce7b277d59f0338767b975ea3bc3b0a5613d50daa239527db30a674562e` |
| 4 | `docs/client/3-admin-checklist.md` | `c093294cde11ce10fe8a4255fdffc9ecf7b33996f2f1401fefc8b269ba1185ac` |
| 5 | `docs/client/4-sr-format.md` | `fdf33be93cc584770925a25ba755c1a502358edcf465c7d162254f38dc34a3dd` |
| 6 | `docs/client/5-ai-prompt.md` | `ac1ec26b1676265a55d09081e13f7453d86b12de74197d9a483837dc563be6be` |
| 7 | `docs/client/0-site-policy.md` | `ca8abf8d8a2b5f8a0c50fb4dd0308278d787e52c960928c90f71ff96d7d9f4a1` |

Deliberate exclusions: `docs/client/index.md` and `docs/client/_internal-feature-map.md`.

Final output:

| Field | Result |
|---|---|
| Path | `output/pdf/atstudio-client-testing-guide.pdf` |
| SHA-256 | `7a1db067628e44aa49e2f2febe455304cc88cc1ff2bc6edfdab9ae7a2fe7db1f` |
| Size / pages | 169,090 bytes / 12 pages |
| Unicode title | `AT.M 클라이언트 테스트 가이드` |
| Source text | 272/272 segments, 100.00% |
| Extracted text | 10,102 characters; U+FFFD count 0 |
| Determinism | second generation hash identical |

### Layout Correction and Render Review

Independent MA review rejected the first 14-page layout because page 4 orphaned `사업자와 관리자` and page 12 contained only the short `확인 불가` section. Subsequent review found list tails at the start of pages and, in v1.2, orphaned section headings at the bottoms of pages 5 and 8. Generator 1.3.0 changes layout only:

- `h1`, `h2`, and `h3` use `keepWithNext=True`.
- The parser collects each contiguous bullet or numbered list into `KeepTogether`.
- When a list immediately follows `H1`, `H2`, or `H3`, the parser removes that heading from the story and rebuilds the heading plus full list as one `KeepTogether` group.
- The first source still starts on a new page; later sources use a 75 mm conditional page break plus a 6 mm separator.
- The manifest records these layout controls and the final `v13` render command.
- All seven Markdown files and their hashes remained unchanged.

Poppler 26.05.0 rendered all 12 final pages at 144 DPI to 1191x1684 PNGs. Every page had a nonwhite bounding box within `(124,60)-(1066,1641)` (cover starts at y=313), nonwhite ratio 0.0182-0.0534, and `edge_nonwhite=0`.

Docops visually inspected all 12 final pages. Independent MA review additionally confirmed that page 5 completes section 3, page 6 starts section 4 with its list, page 8 completes company certification, and page 9 starts whitelist with its list; pages 6, 9, 10, 11, and 12 have no clipping, overlap, or broken Hangul. No blank or orphan-heading page remains.

The user-supplied override wrapper `dependencies/bin/override/pdftoppm.cmd` resolves to a missing `native/poppler/bin` path in this runtime. Rendering used the bundled executable at `dependencies/native/poppler/Library/bin/pdftoppm.exe`; the manifest records the exact path, version, command, and wrapper defect. Poppler emitted display-font fallback warnings for unrelated font names, but the PDF's embedded Malgun Gothic content rendered correctly.

After independent approval, all 24 docops-owned PNG intermediates (12 v1.2 plus 12 final v1.3) were removed. The MA-owned `tmp/pdfs/wi012-independent-v2/` evidence was left untouched, so the parent directory remains. The final PDF and manifest remain under `output/pdf/`.

## Validation Results

| Check | Result |
|---|---|
| Docs validator | PASS: Tier 0, links, 402 supported IDs, document index |
| Index sync/count | PASS: exact managed total 193 |
| Live docs U+FFFD | 0 files / 0 matches |
| Historical U+FFFD reference | 2 literal references retained in `WI-20260711-ATS-001-evidence-pack.md`; historical evidence not rewritten |
| Metadata vocabulary | 0 invalid current category/status values |
| PDF verifier | PASS: 12 pages, Unicode title, 272/272 segments, hash match |
| PDF all-page render | PASS: 12/12 nonblank, no edge contact; docops all-page and independent MA boundary review passed |
| `git diff --check` | exit 0; non-failing working-tree line-ending warnings only |
| Client branch integrity | same HEAD, clean before/after, package and lock hashes unchanged |

The docs validator proves structural integrity only. Semantic closure is based on the matrix and source-derived counts above, not on validator PASS alone.

No application build, application test suite, application server, live provider, or retained database was run by WI-012. WI-005 through WI-011 test results are cited as dependency evidence, not rerun or relabeled as WI-012 tests.

## WI-012 Change Inventory

- Standards/root: `docs/standards/documentation-standards.md`, `docs/index.md`.
- Design: `docs/design/api-spec.md`, `docs/design/db-schema.md`, `docs/design/index.md`, `docs/design/payment-integration-design.md`, `docs/design/usecase/sound-playhistory.md`, and the WI-012 closure section of `docs/design/remaining-remediation-design-20260716.md`.
- UI: `docs/ui/index.md`, `docs/ui/atstudio-front-list.md`, `docs/ui/screen-flow.md`, `docs/ui/modal-list.md`.
- Payment: `docs/payment/index.md`, `feature-inventory.md`, `known-limits-and-next-steps.md`, `acceptance-test-checklist.md`, `user-flows.md`, `client-brief.md`.
- Registry: `docs/registry/index.md`, `project-registry.md`, `context-registry.md`, `workboard.md`, `asset-registry.md`.
- SR: `docs/SR/index.md`, `SR-42.md`, `SR-92.md`, `SR-93.md`.
- Client: the seven ordered PDF sources plus `docs/client/index.md` and `docs/client/_internal-feature-map.md`.
- PDF tooling/output: `scripts/docs/generate_client_testing_pdf.py`, `scripts/docs/verify_client_testing_pdf.py`, final PDF, and adjacent manifest.
- Deliverables: this evidence pack and `deliverables/user/WI-20260716-ATS-012-summary.md`.

Several target documents were already dirty from WI-005 through WI-011. WI-012 preserves those changes and adds only current-state reconciliation sections/edits.

## Risks and Remaining Boundaries

- `P2-13`: runtime declarations and docs agree, but stale numeric comments in `frontend/src/router/index.tsx` remain until a code-owned follow-up.
- Social-only withdrawal policy is unapproved and was not invented.
- Retained-DB migration/rehearsal, real Toss/OAuth/provider behavior, secrets, trusted proxy/multi-egress identity, production monitoring, and public runtime remain environment-conditional.
- Public Vite exposure requires a patched compatible toolchain or controlled access. Development meets the audit condition; frozen client-demo does not.
- Low coverage is an explicit baseline and requires risk-based follow-up tests.
- Legacy one-time compatibility removal requires a separately approved coordinated change and evidence window.

## Rollback

Rollback only WI-012-owned documentation hunks, client-source rewrites, scripts, PDF, manifest, and these two deliverables. For shared dirty documents, remove only the WI-012 reconciliation sections; preserve all WI-005 through WI-011 content. Do not revert product code, schema, client-demo files, historical evidence, or unrelated working-tree changes. No DB/runtime/data rollback is required.

## Related Documents

- [User Summary](../user/WI-20260716-ATS-012-summary.md)
- [Handoff](WI-20260716-ATS-012-handoff.md)
- [Approved REQ](../user/REQ-20260716-ATS-002.md)
- [Remaining Remediation Design](../../docs/design/remaining-remediation-design-20260716.md)
