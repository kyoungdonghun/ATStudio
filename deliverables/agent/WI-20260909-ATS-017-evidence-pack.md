---
version: 1.1
last_updated: 2026-09-09
project: ATS
owner: qa
category: evidence-pack
status: handoff-ready
dependencies:
  - path: WI-20260909-ATS-017-handoff.md
    reason: Approved independent review scope and two-file write boundary
  - path: WI-20260909-ATS-016-evidence-pack.md
    reason: Predecessor classification and historical reference repairs
  - path: ../user/REQ-20260909-ATS-003.md
    reason: Approved cleanup and MA publication gates
  - path: ../../docs/registry/v1-artifact-retention-20260909.md
    reason: Exact public path, original hash and disposition mapping
---

# Evidence Pack: WI-20260909-ATS-017

## Summary

**HANDOFF_READY: independent cleanup review complete; both findings resolved.**
No unresolved artifact-loss, changed-reference, ignore, index-count,
product-preservation or screened-publication issue was found in the checked
candidate. A confirmed 14-link clean-checkout gap was repaired by MA and
independently rechecked. This is not an unconditional REQ or publication PASS.

QA checkpoints: 2026-09-09 08:59-09:03 KST, followed by the current-workspace
index recheck at 09:06 KST. Source HEAD remained
`915dbd2e34efd212df931c563ded3c9c80148d95`. The last pre-output scope check
contained 28 staged paths, matching MA's publication allowlist, and no unstaged
changes. These two WI017 outputs were not included in that checkpoint.

## Findings

### F1: P2 Clean-Checkout Report Links, Resolved

MA's initial `docs-candidate.log` recorded 14 missing generated-report links
despite `docs-workspace.log` passing. The responsible documents were
[September 8 WI001](WI-20260908-ATS-001-evidence-pack.md) at lines 116-118 and
[September 8 WI003](WI-20260908-ATS-003-evidence-pack.md) at lines 140-153.
There were three links in WI001 and eleven in WI003, pointing into ignored
`build/test-results/test/` and `build/reports/tests/test/`.

QA compared the baseline and MA's patch: all 14 repository-relative output
paths remain as code literals; recorded timestamps, hashes and historical
execution claims remain. Each document now explicitly states that generated
reports are neither shipped in Git nor included in the 205-artifact archive;
rerunning commands produces new evidence, not the historical original bytes.
No invented archive pointer or raw-report promotion was introduced. The
original cleanup set remains **205**, not 219. QA changed neither document.

The amended source-only candidate resolves the reviewed links/dependencies.
MA's `docs-candidate-repaired.log` also reports no link/index errors and 704
supported traceability IDs. Its index check verifies discoverability, not
the numeric category counts checked below.

### F2: P3 Pre-Existing Category Count Drift, Resolved

[Documentation Index](../../docs/index.md) initially reported Design **29**,
Standards **12** and an intermediate cleanup total of **202**. MA corrected
Design to **30**, Standards to **13**, and the total to **204**, explicitly
defining recursive Markdown counting for **all 14 categories**, excluding every
`index.md` and including historical/reference files and nested README files.
The nested Standards file is `docs/standards/public_data/standard_glossary/README.md`.
Registry's increase from four to five is correct. These counts are separate
from the unchanged 205-artifact cleanup population.

At 09:06 KST QA reread the **current workspace** index and independently
enumerated all categories recursively: **204 files, zero mismatches**, exactly
matching `docs-index-counts.json`. This resolves F2 in the current workspace;
the earlier candidate/staged checkpoint is not silently updated by this result.
MA owns restaging and final candidate refresh.

QA's initial 09:02 total of 203 used recursive Design counting but top-level
counting for other categories, missing the nested Standards README. That was
an incomplete counting method, not the final inventory. The earlier 203 figure
is superseded by the independently verified 204. Design's 29-versus-30 drift
was also verified at baseline `915dbd2`; no Design or Standards content change
was needed. QA changed neither index nor historical document.

## Scope / DoD Check

- [x] Manifest, public registry and private disposition have exactly 205 unique, matching paths and original hashes.
- [x] All 205 existing restored originals match SHA-256 and byte length; ZIP and manifest match their saved verification hashes.
- [x] All 13 retained originals match bytes in the workspace, staged blobs and source-only candidate.
- [x] All 192 archive-only originals are physically absent from the workspace and candidate and absent from the Git index.
- [x] Removal-receipt sets exactly equal the 13/192 disposition sets; no duplicate disposition paths.
- [x] Scoped Markdown links, YAML dependencies and target anchors resolve within the source-only candidate.
- [x] All 955 protected tracked paths and 79 recorded local assets match the before-snapshot hashes.
- [x] All three recorded process identities match using PID, name and creation time; no health or DB inference.
- [x] Staged/current scope, narrow ignores, historical repairs and bounded privacy screening reviewed.
- [x] Confirmed clean-checkout gap and pre-existing numeric mismatches resolved and independently rechecked.
- [x] Independent review, supplied execution receipts and pending publication are separated.
- [ ] Final MA integration of WI017 outputs, restaging of the corrected index and refreshed publication receipts.
- [ ] MA commit/push/remote-SHA verification and REQ closure; not QA-owned and not claimed here.

## Reference Documents (Tier 0-2)

| Tier | Injected or consulted pointer | Applied rule |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Approved scope, no unrelated edits or secret disclosure; English documents and Korean conversation |
| 0 | `docs/standards/development-standards.md` | Evidence-led verification, preserve source/tests/configuration |
| 0 | `docs/standards/documentation-standards.md` | Dated historical facts and objective pointer repairs only |
| 0 | `docs/standards/glossary.md` | Canonical WI and REQ terms |
| 1 | `docs/policies/archive-policy.md` | Original/restored hash gates; local archive is neither public evidence nor remote backup |
| 1 | `docs/policies/quality-gates.md` | Traceability, bounded findings and explicit verification limits |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Existing handoff verified before producing this two-set output |
| 2 | `.agents/skills/validate-docs/SKILL.md`, `scripts/validate_docs.py` | Read existing link-check behavior; MA owns broad validator execution |
| 2 | `.agents/skills/sync-docs-index/SKILL.md` | Read-only displayed-category count rules; no fix mode |
| Context | `AGENTS.md`, REQ003, WI016 handoff/evidence, WI017 handoff | Explicit qa delegation and exclusive artifact-review scope |
| Context | Retention register, `.gitignore`, MA candidate and named receipts | Exact-set evidence and candidate-only resolution |

Injection source: MA's supplied Tier 0 statements and WI017 input pointers.
Rule source: `.claude/config/context-injection-rules.json`; role `qa`, required
tiers `[0]`, task type `review`. `.claude/config/workspace.json` confirms ATS.
No further subagent delegation or new audit was initiated.

## Evidence Pointers

Private evidence root: `%LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/`.
The following names are receipt pointers, not public download links. No private
receipt contents, local credential values or live endpoint addresses are copied
into this deliverable.

| Receipt or location | Use and authority |
|---|---|
| `manifest.json`, `verification.json`, `workspace-artifacts.zip`, `restore-check/` | QA independently rehashed the existing ZIP, manifest and all 205 restored files; no extraction or archive mutation |
| `disposition.json`, `removal-receipt.json` | QA reconciled original path/hash/disposition and exact removed/retained sets; pre-removal source hashing is MA execution evidence |
| `preservation-before.json` | QA independently rehashed 955 protected and 79 local-asset paths against this recorded baseline |
| `preservation-after.json` | Separate MA post-check receipt corroborates 955/79 unchanged and three matching process identities |
| `publication-allowlist.json` | QA matched all 28 staged paths exactly before writing WI017 outputs |
| `candidate-source.json`, `candidate-source/` | Initial 26-path tree `1abfd24aeb792762ae945af599a307a8aa424835`, then MA's two-document staged overlay; do not relabel the old tree receipt as a final tree |
| `docs-candidate.log`, `docs-candidate-repaired.log`, `docs-workspace.log` | MA's original clean-checkout failure, repaired candidate result and workspace result |
| `docs-index-counts.json`, current workspace `docs/index.md` | QA independently recounted all 14 categories recursively at 09:06 KST: 204 files, zero mismatches; not a claim that the old candidate was already refreshed |
| `backend-result.json`, `backend-clean-build.log` | MA isolated offline backend execution, not a QA rerun |
| `frontend-result.json`, `frontend-clean-tests.log`, `frontend-coverage-summary.json` | MA isolated frontend execution and reported coverage; QA read result and coverage summary text |
| `final-http.json` | QA read only four status fields, all 200; requests were executed by MA |

QA-created files only:
- `deliverables/agent/WI-20260909-ATS-017-evidence-pack.md`
- `deliverables/user/WI-20260909-ATS-017-summary.md`

## Commands & Outputs

All commands were read-only PowerShell, Node built-ins, existing `js-yaml`,
`rg`, or non-mutating Git. Inline checks ran through `@'... '@ | node`; no
verification script or output file was added to the repository. This section
records the executed methods/results for reproduction against the same receipts.

| Executed command or method | Observed result |
|---|---|
| Node `JSON.parse`, path-keyed Maps and `crypto.createHash('sha256')`; parse the public table including its linked retained rows | 205 unique rows in each of manifest/disposition/registry; exact path, size, hash, entry-ID and disposition agreement |
| Hash each `restore-check/<manifest path>` and compare file length; hash `workspace-artifacts.zip` and `manifest.json` against `verification.json` | 205 restored matches; both container/manifest receipt hashes match |
| `fs.existsSync` for all disposition paths; SHA-256 of all retained workspace, `git show :<path>` and candidate bytes | 192 absent in workspace/candidate/index; 13/13 identical in all three retained locations |
| Hash every `preservation-before.json` protected/localAssets path without printing contents | 955/955 protected and 79/79 local assets unchanged at 08:59 KST |
| `Get-CimInstance -ClassName Win32_Process -Filter "ProcessId = <recorded id>"` | Three names and UTC creation times exactly match the baseline at 09:00 KST; no command lines or runtime actions |
| `git --no-optional-locks diff HEAD --name-only -z`; scoped candidate Markdown scan plus `js-yaml.load` of frontmatter | 29 documents: 27 changed/added Markdown files plus the two tracked August 17 WI018 consumers; 177 local Markdown references, 64 YAML edges, 36 anchor checks; zero failures |
| Resolve links using candidate-root membership only, strip fenced code, check explicit IDs and heading anchors | No fallback to workspace output, restored originals or the private archive; no missing changed reference |
| Match old WI004 output targets to new registry anchors by original path | All 21 replacements correct: 13 evidence links and eight summary links; payment entry adds one precise archive link, for 22 archive-entry links total |
| `git --no-optional-locks diff HEAD -- <Sep08 WI001 evidence> <Sep08 WI003 evidence>`; compare historical output-path/timestamp/hash tokens | 3 + 11 generated-report links corrected; only objective availability repair and dated metadata changes |
| `git --no-optional-locks diff --cached --name-status`; compare `publication-allowlist.json` and candidate bytes with staged blobs | 28 paths = 19 additions + nine modifications; no tracked deletions, raw-output additions or out-of-allowlist paths; all 28 candidate files match staged bytes |
| `git --no-optional-locks diff --cached --check` and `git --no-optional-locks diff --name-only` | Exit 0; no whitespace errors and no unstaged differences at the pre-output checkpoint |
| `git --no-optional-locks check-ignore --no-index -v -z --stdin` with 12 hypothetical, unwritten paths | Five intended dated-output/ZIP positives; seven source/test/SQL/deliverable/unrelated-output/non-date negatives remain visible |
| Scan 19 added Markdown files and only added diff lines in eight existing Markdown files | Zero matches for screened email, JWT, private-key, provider-token, Korean-phone and quoted credential-literal patterns; no raw evidence promoted |
| Initial category count and `git ls-tree -r --name-only 915dbd2 -- docs/design`; then reread current workspace index and recurse through all 14 category directories | Initial 203 calculation missed a nested Standards README. Final 09:06 check: Design 30, Standards 13, Registry 5, total 204, zero mismatches; exact agreement with `docs-index-counts.json` (F2 resolved) |

The first registry parser rejected linked retained-row syntax and stopped before
reporting a result; the corrected parser handles both row forms. An initial
cross-API process-time comparison differed by sub-microsecond precision;
rechecking with the baseline's `Win32_Process` API produced three exact matches.
Neither preliminary check is counted as a product or preservation failure.

Privacy screening is bounded pattern screening, not exhaustive PII/secret
certification. Unrelated historical documents were not deeply audited; WI016's
reported pre-existing YAML issue in `.claude/agents/uv.md` was not reopened.

## Tests

**QA did not run product tests, builds, installs, HTTP requests, DB queries or
service actions.** The following completed executions are MA-owned receipts
read during review, not independently executed QA results:

| MA receipt | Recorded execution result and limit |
|---|---|
| `backend-result.json` | `gradlew.bat build --offline --no-daemon --max-workers=2 -Dorg.gradle.jvmargs=-Xmx768m`: exit 0; 200 suites, 1,825 total, 1,806 passed, 19 skipped, zero failures/errors; coverage thresholds PASS |
| Backend boundary | Isolated Git source at `915dbd2`, H2/mocked tests; environment-gated MySQL proofs were not enabled; no live-runtime verification inferred |
| `frontend-result.json` | `npm ci --ignore-scripts --no-fund --no-audit`; `npm run test:coverage -- --maxWorkers=2`: exit 0, 112 files, 1,563 passed, zero failures; typecheck/lint/format/build each exit 0 |
| Frontend coverage | Statements 90.26%, branches 82.83%, functions 91.21%, lines 92.84%; isolated source and lockfile install, mocked UI tests, no live browser/provider/SMTP claim |
| `docs-candidate-repaired.log` | Source-only validator reports PASS, 704 IDs, no broken links or index-discoverability errors; numeric F2 was subsequently verified independently against the corrected workspace index |
| `preservation-after.json`, `final-http.json` | MA records 955/79 preserved, three matching identities and four HTTP 200s; HTTP availability is not DB correctness, continuous health or production approval |

## Risks / Rollback

The public register contains metadata, not remotely recoverable raw originals.
Private originals remain local-only and require the custodian and verified
original hashes for recovery. QA has verified existing restored bytes, not
guaranteed future retention or remote backup.

The candidate and index can advance after these checkpoints. MA must integrate
the two WI017 files and corrected index, refresh the final candidate/allowlist and record
actual final validation and publication receipts. This Evidence Pack does not
claim a final candidate tree, commit, push, remote match, DB state, deployment
readiness or production SR-93 acceptance.

Rollback ownership remains MA: reverse only approved documentation/ignore
changes if required, and recover specifically authorized originals into a
separate private directory with SHA-256 verification before any approved source
restoration. Do not overwrite unrelated source/config/media, execute archived
SQL, remove ignored build data or revert other work. No rollback was executed
by QA.

## Follow-ups

WI017 has **Blocks: none**. Return these two outputs to MA now; do not wait for
publication to complete this independent review. REQ003 stays open until MA's
remaining integration, final publication and closure gates actually finish.
