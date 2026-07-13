# Evidence Pack: WI-20260711-ATS-001

## Summary

- Established a read-only documentation audit baseline, mapped indexes/counts/traceability, compared the in-progress client Markdown set with the generated PDF, and produced evidence-backed follow-up inputs.

## Scope / DoD Check

- [x] Recorded branch `dev/kyoung` and HEAD `27d22446e5d21324dadcfcb322dbe51704dfe914`.
- [x] Recorded the complete 21-path dirty-worktree inventory at audit start.
- [x] Mapped root/category entry points and verified all category counts.
- [x] Inventoried REQ/WI/SR identifiers and artifact completeness.
- [x] Verified current-state document count, REST API count, DB table/entity count, screen claims, and agent count.
- [x] Compared seven included client Markdown sources with the 20-page PDF.
- [x] Ran all-page in-memory PDF rendering and pixel-bound checks without creating temporary files.
- [x] Ran the repository documentation validator read-only and recorded its exact result.
- [x] Classified findings as confirmed defect, stale statement, ambiguity, or unverified external dependency.
- [x] Modified no existing file; created only this Evidence Pack and the paired user summary.

## Reference Documents (Tier 0-2)

| Tier | Document / pointer | Reason |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Constitution and active Phase 2 statement |
| 0 | `docs/standards/documentation-standards.md` | Metadata, index, and documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical terminology |
| 0 | `docs/standards/development-standards.md` | Required workspace Tier 0 and user-requested lifecycle audit |
| 1 | `docs/policies/quality-gates.md` | Traceability and evidence expectations |
| 2 | `docs/index.md` and 15 category/nested indexes | Entry-point and count map |
| 2 | `docs/client/`, `docs/design/`, `docs/payment/`, `docs/SR/`, `docs/registry/` | Handoff documentation scope |
| REQ | `deliverables/user/REQ-20260711-ATS-001.md` | Approved audit scope |
| WI | `deliverables/agent/WI-20260711-ATS-001-handoff.md` | Work contract |
| Artifact | `output/pdf/atstudio-client-testing-guide.pdf` | Generated client PDF under audit |

Injection order followed the handoff and workspace rules: Tier 0, Tier 1, Tier 2/context, then the live snapshot.

## Immutable Baseline

### Git Identity

| Field | Value |
|---|---|
| Branch | `dev/kyoung` |
| Upstream relation | `origin/dev/kyoung [ahead 3]` |
| HEAD | `27d22446e5d21324dadcfcb322dbe51704dfe914` |
| Initial dirty paths | 21 |

Commands:

```powershell
git status --short --branch
git status --porcelain=v1 --untracked-files=all
git rev-parse --abbrev-ref HEAD
git rev-parse HEAD
git diff --name-status
git diff --stat
```

### Initial Dirty-Worktree Inventory

| Status | Path |
|---|---|
| M | `docs/client/0-site-policy.md` |
| D | `docs/client/1-scenarios.md` |
| D | `docs/client/2-test-cases.md` |
| D | `docs/client/3-test-methodology.md` |
| M | `docs/client/4-sr-format.md` |
| M | `docs/client/5-ai-prompt.md` |
| M | `docs/client/index.md` |
| D | `docs/client/testing-guide-friendly.html` |
| M | `docs/client/testing-guide.md` |
| M | `docs/index.md` |
| ?? | `deliverables/agent/WI-20260711-ATS-001-handoff.md` |
| ?? | `deliverables/agent/WI-20260711-ATS-002-handoff.md` |
| ?? | `deliverables/agent/WI-20260711-ATS-003-handoff.md` |
| ?? | `deliverables/agent/WI-20260711-ATS-004-handoff.md` |
| ?? | `deliverables/agent/WI-20260711-ATS-005-handoff.md` |
| ?? | `deliverables/user/REQ-20260711-ATS-001.md` |
| ?? | `docs/client/1-quick-checklist.md` |
| ?? | `docs/client/2-full-feature-checklist.md` |
| ?? | `docs/client/3-admin-checklist.md` |
| ?? | `docs/client/_internal-feature-map.md` |
| ?? | `output/pdf/atstudio-client-testing-guide.pdf` |

Concurrent-work note: before output creation, WI-002 and WI-003 summary/evidence files appeared. Branch and HEAD stayed unchanged. The client/PDF hashes below remained identical, so those additions did not invalidate this WI's content baseline.

### Client/PDF Content Baseline

Command shape:

```powershell
Get-ChildItem docs/client -File | Sort-Object Name
Get-FileHash -Algorithm SHA256 <each-client-file-and-pdf>
```

| Path | Bytes | SHA-256 |
|---|---:|---|
| `docs/client/_internal-feature-map.md` | 4,987 | `F40161A6B92566FF1949B114B1AC8794600CEB4E1C2820F714EAB826382C7CE4` |
| `docs/client/0-site-policy.md` | 3,345 | `C46D27AD96CB4B27CAC5CC27A59AA5ECA230628341F256878F2DD1544B7DCAFA` |
| `docs/client/1-quick-checklist.md` | 4,297 | `C81D2CF120416C899EB43B12D2112E1992840CA327A1BB9D847504486F23002F` |
| `docs/client/2-full-feature-checklist.md` | 10,991 | `BC9773B6C721260D8F4B33A52042A37337EBCB4584E39543A7E9919E8320B43D` |
| `docs/client/3-admin-checklist.md` | 4,965 | `7CE24DCE52C452ED2F7B36CD7BD4D9697AFF4DFC9A725EBDC7E83909E954CDE1` |
| `docs/client/4-sr-format.md` | 2,036 | `2405947B678223C0399C787548F1C53639EAA3A7BD1015D7C6BD06243FAB1213` |
| `docs/client/5-ai-prompt.md` | 1,892 | `49110A3443D5F9BFD9B4E66A555CC9A8327FF5F1DA6D81725E30EB87BFDD6649` |
| `docs/client/index.md` | 2,297 | `756ECC0C424D9E4AF15CFD6BDF9A47FF161D86CAC920E67A2BA6A24E83E5D49A` |
| `docs/client/testing-guide.md` | 3,271 | `50496A957EF1BC21BE67A7E8862D2307C2A1FC3D1CF8FC1F6CADA78E01B3A5E0` |
| `output/pdf/atstudio-client-testing-guide.pdf` | 196,135 | `5D5A743F9772362042EBCC5E29E3E8EC92AAE1F35EE680BBF0636823554D2DF5` |

## Documentation Inventory

Workspace inventory: 228 files under `docs/`, including 201 Markdown, 5 HTML, 20 JPG, 1 JSON, and 1 XLSX. The root count contract excludes `index.md` files and therefore totals 185.

| Category | Indexed | Actual | Entry point | Result |
|---|---:|---:|---|---|
| Architecture | 1 | 1 | `docs/architecture/index.md` | MATCH |
| Design | 24 | 24 | `docs/design/index.md` | MATCH |
| Policies | 8 | 8 | `docs/policies/index.md` | MATCH |
| Standards | 13 | 13 | `docs/standards/index.md` | MATCH |
| Templates | 18 | 18 | `docs/templates/index.md` | MATCH |
| Registry | 4 | 4 | `docs/registry/index.md` | MATCH |
| Audit | 2 | 2 | `docs/audit/index.md` | MATCH |
| Client | 8 | 8 | `docs/client/index.md` | MATCH |
| Payment | 7 | 7 | `docs/payment/index.md` | MATCH |
| SR | 92 | 92 | `docs/SR/index.md` | MATCH |
| Retrospective | 4 | 4 | `docs/retrospective/index.md` | MATCH |
| ADR | 1 | 1 | `docs/adr/index.md` | MATCH |
| UI | 3 | 3 | `docs/ui/index.md` | MATCH |
| Eval | 0 | 0 | `docs/eval/index.md` | MATCH |
| **Total** | **185** | **185** | `docs/index.md:15-34` | **MATCH** |

Additional nested entry point: `docs/design/usecase/index.md`. `rg --files docs -g 'index.md'` found 16 total index files including the root and nested use-case index.

## Current-State Count Verification

| Claim | Document pointer | Live repository check | Assessment |
|---|---|---|---|
| 147 REST APIs | `docs/index.md:71`; `docs/design/api-spec.md:3760-3782` | 147 method mappings across 24 files containing `@RestController` | Confirmed |
| 39 DB tables | `docs/index.md:71`; `docs/design/db-schema.md:1061-1105` | 39 `CREATE TABLE` statements and 39 `@Entity` files | Confirmed |
| 53 screens | `docs/index.md:71`; `docs/ui/atstudio-front-list.md:161` | 54 lazy page components, 52 excluding two error pages | Ambiguous count contract |
| 13 agents | `docs/index.md:108`; `docs/registry/project-registry.md:40` | 13 `.claude/agents/*.md` files | Confirmed |

REST count command:

```powershell
$files = Get-ChildItem src/main/java/com/atstudio/atstudio/controller -Recurse -Filter *.java |
  Where-Object { Select-String -Quiet -LiteralPath $_.FullName -Pattern '@RestController\b' }
# Sum @(Get|Post|Put|Patch|Delete)Mapping matches from those files -> 147
```

DB count commands:

```powershell
rg -n '^CREATE TABLE' src/main/resources/schema.sql
rg -l '^\s*@Entity\b' src/main/java/com/atstudio/atstudio/entity -g '*.java'
```

## Traceability Inventory

### Baseline Counts

| Artifact/reference type | Count |
|---|---:|
| `deliverables/user/REQ-*.md` files | 46 |
| WI user summaries | 179 |
| WI handoffs | 200 |
| WI evidence packs | 178 |
| Unique WI IDs represented by artifact filenames | 206 |
| WI IDs with summary + handoff + evidence | 167 |
| WI IDs missing at least one artifact type | 39 |
| SR files | 92 |
| Unique content references: REQ | 47 |
| Unique content references: WI | 214 |
| Unique content references: SR | 92 |

### Referential Gaps

- Confirmed missing target: `deliverables/agent/WI-20260519-ATS-005-evidence-pack.md:68` references absent `deliverables/user/REQ-20260420-ATS-001.md`.
- Historical WI IDs referenced without any summary/handoff/evidence artifact:
  - `WI-20260220-ATS-008`: `deliverables/agent/WI-20260220-ATS-004-handoff.md:6`, `WI-20260220-ATS-005-handoff.md:6`.
  - `WI-20260226-ATS-026`: `deliverables/agent/WI-20260226-ATS-025-handoff.md:6`, `:485`; evidence `:110`.
  - `WI-20260310-ATS-005`: `deliverables/user/WI-20260310-ATS-004-summary.md:47`; multiple handoffs and `WI-20260310-ATS-004-evidence-pack.md:87`.
  - `WI-20260517-ATS-013`: `deliverables/agent/WI-20260517-ATS-006-handoff.md:16`, `WI-20260517-ATS-012-handoff.md:16`.
  - `WI-20260618-ATS-002`: `deliverables/agent/WI-20260618-ATS-001-handoff.md:6`.
- Current planned WI-006/007/008 references have no artifacts yet, but all five Phase 1 handoffs mark them as blocked. They are expected future nodes, not baseline defects.
- Incomplete historical artifact sets are classified as ambiguity until a lifecycle state such as Cancelled, Superseded, or Not Created is recorded.

Validator limitation: `.agents/skills/validate-docs/scripts/validate_docs.py:153-159` explicitly validates supported REQ/WI/STD formats, not target existence. Link validation only parses Markdown links (`:97-128`), so code-span paths and bare IDs can remain stale while validation passes.

## Client Markdown / PDF Comparison

### Included Source Set

The PDF cover lists and includes, in this order:

1. `docs/client/testing-guide.md`
2. `docs/client/1-quick-checklist.md`
3. `docs/client/2-full-feature-checklist.md`
4. `docs/client/3-admin-checklist.md`
5. `docs/client/4-sr-format.md`
6. `docs/client/5-ai-prompt.md`
7. `docs/client/0-site-policy.md`

`docs/client/_internal-feature-map.md` is explicitly excluded on PDF page 1. `docs/client/index.md` is also not included; the cover's included-document list makes the bundle boundary visible.

### Text Drift Result

An in-memory Python standard-library parser decoded PDF objects, ASCII85/Flate streams, and embedded ToUnicode maps. Markdown frontmatter, table separators, and formatting syntax were removed before normalized substantive-line comparison.

| Source | Checked lines | Found in PDF | Missing |
|---|---:|---:|---:|
| `testing-guide.md` | 36 | 36 | 0 |
| `1-quick-checklist.md` | 52 | 52 | 0 |
| `2-full-feature-checklist.md` | 138 | 138 | 0 |
| `3-admin-checklist.md` | 63 | 63 | 0 |
| `4-sr-format.md` | 33 | 33 | 0 |
| `5-ai-prompt.md` | 29 | 29 | 0 |
| `0-site-policy.md` | 46 | 46 | 0 |
| **Total** | **397** | **397** | **0** |

Assessment: no confirmed source/PDF body drift at the frozen hashes.

### Render / Metadata Result

- Windows `Windows.Data.Pdf` loaded and rendered all 20 pages in memory.
- A 240-pixel-width pixel audit found nonblank content on every page and zero nonwhite pixels touching the outer 2-pixel edge.
- Representative rendered pages visually inspected: 1, 2, 3, 4, 10, 14, 16, 18, 19, 20. No overlap, table clipping, blank page, or broken Hangul was observed.
- Confirmed metadata defect: binary metadata contains `/Title (ATStudio \077\077\077\077\077 \077\077\077\077\077 \077\077\077)`. Visible page text is correct; the document-title metadata is not.
- No generator script or adjacent provenance manifest was found. Only the REQ/handoff point to the PDF, so regeneration is not reproducible from repository assets alone.

Relevant commands:

```powershell
rg -a -n '/Title|/Author|/Creator|/Producer|/CreationDate|/ModDate|/Subject|/Keywords' output/pdf/atstudio-client-testing-guide.pdf
Get-Command pdfinfo,pdftotext,pdftoppm -ErrorAction SilentlyContinue
python -c "import importlib.util; ..."  # confirmed common PDF modules absent
$env:PYTHONIOENCODING='utf-8'; @'<stdlib ASCII85/Flate/ToUnicode audit>'@ | python -
# Windows.Data.Pdf + InMemoryRandomAccessStream render/pixel audit; no files written
```

Fallback note: the in-app browser rejected direct `file:///...pdf` navigation under its local-file URL security policy. No browser-policy workaround was attempted.

## Evidence Pointers

- Files created by this WI:
  - `deliverables/user/WI-20260711-ATS-001-summary.md`: concise Korean findings, risks, and recommendations.
  - `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md`: baseline, inventories, commands, exact pointers, tests, and follow-up inputs.
- Primary current-state pointers:
  - `docs/index.md:15-34`, `:71`, `:108`.
  - `docs/design/api-spec.md:3647-3656`, `:3760-3782`.
  - `docs/design/db-schema.md:1061-1105`.
  - `docs/ui/atstudio-front-list.md:137`, `:161`.
  - `frontend/src/router/index.tsx:35-101`, `:117-212`.
- Primary governance/traceability pointers:
  - `docs/standards/documentation-standards.md:45-82`.
  - `.agents/skills/validate-docs/scripts/validate_docs.py:97-128`, `:147-179`.
  - `docs/registry/asset-registry.md:22`.
  - `docs/registry/workboard.md:23-36`, `:88-89`.
- Primary client/PDF pointers:
  - `docs/client/testing-guide.md:23-77`.
  - `docs/client/2-full-feature-checklist.md:19-213`.
  - `docs/client/3-admin-checklist.md:17-106`.
  - `output/pdf/atstudio-client-testing-guide.pdf` at the recorded SHA-256.

## Findings

### F-001 - Phase 2 test lifecycle still labeled Planned

- Classification: stale statement.
- Confidence: high.
- Evidence:
  - `docs/standards/development-standards.md:46` already says Frontend Active / Phase 2 implemented.
  - The same document says `Planned` at `:532`, `:664`, and `:705` for TypeScript examples, co-located tests, and Vitest test structure.
  - `AGENTS.md:40-41` and `docs/standards/core-principles.md:210-213` define React SPA as active.
  - `frontend/package.json:10-11` has active Vitest scripts; `:39` declares Vitest.
  - `rg --files frontend/src -g '*.test.ts' -g '*.test.tsx'` found 14 co-located test files.
- Impact: Tier 0 test guidance can incorrectly signal that frontend verification is optional/future work.
- Recommendation: replace the three lifecycle labels with Current/Active and align examples with the repository's Vitest setup.

### F-002 - Required metadata is structurally incomplete

- Classification: confirmed defect.
- Confidence: high.
- Evidence:
  - Standard: `docs/standards/documentation-standards.md:45-47`, `:56-58`, `:72-82`.
  - Scan result across 201 Markdown files: 119 no frontmatter, 19 missing required `dependencies`, 63 with all seven required keys.
  - Concentration: SR 93, design 20, retrospective 5, UI 1 without frontmatter.
  - All nine current `docs/client/*.md` files have all seven required keys.
  - Enum conflicts remain even where keys exist: ADR `status: accepted`, frontend standard `status: active`, screen flow `status: confirmed`, internal feature map `category: reference`; none are in the current allowed enum at `documentation-standards.md:55-56`.
- Impact: automated lifecycle, ownership, and dependency analysis cannot rely on frontmatter.
- Recommendation: first decide whether the enum should evolve, then remediate by category-specific WIs rather than one 138-file rewrite.

### F-003 - Registry locations and current work tracking are stale

- Classification: confirmed defect plus lifecycle ambiguity.
- Confidence: high.
- Evidence:
  - `docs/registry/asset-registry.md:22` points to absent `docs/work-items/`.
  - `docs/registry/workboard.md:36` points to absent `docs/project/`; examples at `:88-89` use absent work-item paths.
  - The workboard claims system-wide/all-project coverage at `:23-24`, but `rg -n 'REQ-20260711-ATS-001|WI-20260711-ATS-001' docs/registry/workboard.md` returned no matches.
  - `docs/registry/project-registry.md:29` still lists the known repository as `TBD`.
  - `docs/registry/context-registry.md:19` requires PRJ + Context ID, while the current WI handoff has no context ID.
- Impact: registry consumers can follow nonexistent paths or assume a completeness level the workboard does not provide.
- Recommendation: align registry contracts with `deliverables/user/` and `deliverables/agent/`, record the actual repo, and choose whether CTX/workboard tracking is mandatory or deprecated.

### F-004 - Screen inventory uses conflicting count semantics and stale API entries

- Classification: stale statement plus ambiguity.
- Confidence: high.
- Evidence:
  - 53: `docs/index.md:71`, `docs/registry/project-registry.md:39`, `docs/ui/atstudio-front-list.md:161`.
  - 49: `docs/standards/frontend-standards.md:305`.
  - 49 + 2 error pages: `frontend/src/router/index.tsx:117`.
  - Live component scan: 54 lazy page components, 52 excluding two error pages.
  - Dashboard stale claim: `docs/ui/atstudio-front-list.md:137` says API undefined; `docs/design/api-spec.md:3647-3656` and `src/main/java/com/atstudio/atstudio/controller/AdminStatsController.java:13-20` define `GET /api/admin/stats`.
  - Missing site-settings row: router `frontend/src/router/index.tsx:97`, `:209`; client checklist `docs/client/3-admin-checklist.md:105-106`; APIs `docs/design/api-spec.md:3696`, `:3725`; no match in `docs/ui/atstudio-front-list.md`.
- Impact: project statistics and client coverage can appear complete while the counting unit is undefined and implemented screens/APIs are omitted.
- Recommendation: define the screen counting unit (route, page component, or client-visible conceptual screen), regenerate all three counts, and add dashboard/settings entries.

### F-005 - SR index is truncated and internally inconsistent

- Classification: confirmed defect.
- Confidence: high.
- Evidence:
  - `docs/SR/index.md:41-42` marks SR-35/SR-36, associated with SR-C-01/SR-C-02, DONE.
  - `docs/SR/index.md:104` still marks SR-C-01 OPEN.
  - `docs/SR/index.md:105` terminates at `router/index.tsx 화�` and contains the repository's only U+FFFD replacement character.
  - `docs/SR/confirm/` exists but contains no files.
- Impact: the final index row is unreadable and the confirmation lifecycle cannot be trusted.
- Recommendation: reconstruct line 105 from history/evidence, reconcile SR-C states with SR-35/36, and either populate or remove the empty `confirm/` contract.

### F-006 - PDF body is synchronized, but metadata/provenance is defective

- Classification: confirmed metadata defect; body drift not found.
- Confidence: high for current hashes.
- Evidence: 397/397 substantive source lines found, 20/20 pages rendered, 0 edge-touch signals, malformed `/Title` value.
- Impact: current human-readable content is sound, but document properties/search/accessibility and future reproducibility are weak.
- Recommendation: emit a Unicode PDF Title and record generator command, ordered source list, and source hashes.

### F-007 - Root index update date is stale

- Classification: stale metadata.
- Confidence: high.
- Evidence: `docs/index.md:3` says 2026-06-18 while current client entry links are at `:85-88` and the file is modified in the 2026-07-11 rebuild.
- Impact: consumers cannot use `last_updated` to judge freshness.
- Recommendation: update metadata in the owning client-doc WI after the rebuild is approved.

## Documentation Validation

Exact command:

```powershell
python .agents/skills/validate-docs/scripts/validate_docs.py
```

Recorded result:

```text
Tier 0 Documents: PASSED - all required files exist
Internal Links: PASSED - no broken internal links
Traceability IDs: PASSED - 284 IDs matched supported formats
Document Index: PASSED - all documents listed in index
SUMMARY: All validations passed
Exit code: 0
```

Interpretation: this is authoritative for the four implemented validator checks, but not for frontmatter schema or referential integrity of bare IDs/code-span paths.

## Commands and Reproduction

Primary read-only command set:

```powershell
git status --short --branch
git status --porcelain=v1 --untracked-files=all
git rev-parse --abbrev-ref HEAD
git rev-parse HEAD
git diff --name-status
git diff --stat
rg --files docs -g 'index.md'
rg -n 'Planned|Phase 2|active' docs/standards/development-standards.md AGENTS.md docs/standards/core-principles.md
rg --files frontend/src -g '*.test.ts' -g '*.test.tsx' -g '*.spec.ts' -g '*.spec.tsx'
rg -n '�' docs deliverables AGENTS.md CLAUDE.md
rg -a -n '/Title|/Author|/Creator|/Producer|/Subject' output/pdf/atstudio-client-testing-guide.pdf
python .agents/skills/validate-docs/scripts/validate_docs.py
```

Additional read-only PowerShell scans counted Markdown metadata states, root-index category parity, REST mappings, schema/entity totals, agent definitions, WI artifact combinations, missing ID targets, SHA-256 values, and PDF render pixels. All scripts wrote only to stdout and memory.

Notable command/environment limitations:

- Host PowerShell lacks `[IO.Path]::GetRelativePath`; relative names were recomputed with resolved-prefix substring logic.
- `pdfinfo`, `pdftotext`, and `pdftoppm` were unavailable.
- Common Python PDF modules were unavailable in the host interpreter.
- Browser local-file navigation was blocked by policy, so the audit used standard-library PDF decoding plus Windows in-memory rendering instead.

## Tests

- Documentation validator: PASS, exit 0.
- Client/PDF normalized source comparison: PASS, 397/397 lines.
- PDF page load/render: PASS, 20/20 pages.
- PDF edge/clipping pixel signal: PASS, 0 pages touching the outer 2-pixel edge.
- No application tests were run; this WI is documentation-only and read-only.

## Risks / Rollback

### Risks

- Live deployment, production DB, Toss/provider behavior, and external URLs were not accessed; those remain unverified external dependencies.
- Concurrent agents can continue changing unrelated WI outputs. The client/PDF hash set is the fixed comparison key for this report.
- Metadata remediation has a large blast radius and should not be folded into an unrelated client-doc change.

### Rollback

- No existing file was changed.
- If explicitly requested, rollback consists only of removing:
  - `deliverables/user/WI-20260711-ATS-001-summary.md`
  - `deliverables/agent/WI-20260711-ATS-001-evidence-pack.md`

## Follow-up WI Inputs

### WI-20260711-ATS-006 - Payment 3-way alignment

- Freeze client payment wording at `docs/client/testing-guide.md:56-66`, `docs/client/2-full-feature-checklist.md:125-169`, and `docs/client/3-admin-checklist.md:62-76` using the recorded hashes.
- PDF content is synchronized with those lines; any mismatch found by WI-006 is source-vs-implementation, not source-vs-PDF.
- Live Toss/provider behavior remains unverified.

### WI-20260711-ATS-007 - Whitelist / company certification alignment

- Freeze client wording at `docs/client/2-full-feature-checklist.md:171-201`, admin wording at `docs/client/3-admin-checklist.md:78-98`, and internal pointers at `docs/client/_internal-feature-map.md:36-37`, `:46-48`.
- PDF content is synchronized; code/service/state verification is still required.

### WI-20260711-ATS-008 - Member / subscription / content / admin alignment

- Use confirmed current counts: 147 REST mappings, 39 schema/entity pairs, 13 agents.
- Treat 53 screens as a documentation-inventory count only until the screen counting unit is defined.
- Include F-001 Phase 2 lifecycle wording, the dashboard API stale entry, and the missing site-settings inventory row in the 3-way review.
