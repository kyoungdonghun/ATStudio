---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: docops
category: evidence-pack
status: complete
dependencies:
  - path: WI-20260909-ATS-016-handoff.md
    reason: Approved role, write allowlist and ownership
  - path: ../user/REQ-20260909-ATS-003.md
    reason: Approved exact 205-path cleanup
  - path: ../../docs/registry/v1-artifact-retention-20260909.md
    reason: Public original path, hash and disposition register
---

# Evidence Pack: WI-20260909-ATS-016

> Completed: The handoff checkpoint below is historical. See MA Delivery Closeout
> at the end for final verification, publication and the bounded local-temp exception.

## Summary

**HANDOFF_READY for WI-20260909-ATS-017.** Classified the exact 205 originals:
13 unchanged historical documents retained, 192 private archive-only artifacts.
Repaired 21 raw-output links and indexed original path/hash availability.
MA supplied actual removal results; isolated validation, final preservation
comparison, independent review and Git publication are not claimed complete.
REQ-20260909-ATS-003 remains open for those gates.

## Scope / DoD Check

- [x] Every original path classified in external `disposition.json`.
- [x] Necessary safe historical document closure selected, not all 96 documents.
- [x] Raw outputs remain private; no log, screenshot, ZIP or SQL promotion.
- [x] Historical facts/dates preserved; objective archive pointers repaired.
- [x] Compact indexed register and narrow future-output ignore rules supplied.
- [x] MA actual removal receipt recorded without adopting pending test results.
- [x] Docops made no product/test/config/runtime/DB changes, archive operations,
  file deletions, or Git mutations.
- [ ] MA isolated documentation/build/test and final preservation receipts.
- [ ] WI017 independent review and MA Git publication.

## Reference Documents (Tier 0-2)

| Tier | Injected pointer | Applied rule |
|---|---|---|
| 0 | `docs/standards/core-principles.md` | Explicit scoped approval, protect unrelated data/secrets, sustainable minimum records; English docs and Korean REQ |
| 0 | `docs/standards/development-standards.md` | Exact ownership/scope and truthful verification |
| 0 | `docs/standards/documentation-standards.md` | Preserve metadata and history, relative pointers |
| 0 | `docs/standards/glossary.md` | Canonical WI and REQ terms |
| 1 | `docs/policies/archive-policy.md`, `docs/policies/security-policy.md` | Objective historical link correction and private evidence boundary |
| 2 | `.agents/skills/create-wi-evidence-pack/SKILL.md` | Existing WI016 handoff verified; this two-set evidence contract |
| 2 | `.agents/skills/validate-docs/SKILL.md`, its `scripts/validate_docs.py` | Read validation behavior; full execution remains MA-owned |

Injection source: WI016 handoff and the user's supplied Tier 0 rules.
Role: `docops`; no further subagent delegation. Context: REQ003, WI015 evidence,
document/registry indexes, existing ignore rules, and the exact external manifest.

## Evidence Pointers

Archive ID: `v1-artifacts-20260909-084405`; private directory:
`%LOCALAPPDATA%/ATStudio/archives/v1-artifacts-20260909-084405/`.
The directory and its contents are not a remote backup.

| Pointer | Purpose |
|---|---|
| [Public register](../../docs/registry/v1-artifact-retention-20260909.md) | All 205 original paths, hashes, dispositions and recovery boundary |
| External `manifest.json`, `verification.json` | Original set and MA's ZIP/restored-hash receipt |
| External `disposition.json` | Exact retain/archive allowlists, per-path reasons, changed paths and pending gates |
| External `removal-receipt.json` | Actual MA source-removal results; removed path set matches all 192 archive dispositions |
| External `preservation-before.json` | MA baseline only: 955 protected tracked source/test/build files, 79 local assets/config, three live runtime PIDs |

Modified tracked files, each within the handoff allowlist:

| File | Change |
|---|---|
| `.gitignore` | Five generated-output patterns, scoped to named output families; no deliverables or SQL blanket ignore |
| `docs/index.md` | Current/history entry points; registry count 4 to 5, total 201 to 202 |
| `docs/registry/index.md` | Indexed the ATS retention register |
| `docs/policies/archive-policy.md` | Local retention/hash/availability boundary |
| `docs/payment/index.md` | Dated HTTP evidence now explicitly points to its private archive entry |
| `deliverables/agent/WI-20260909-ATS-004-evidence-pack.md` | Thirteen precise raw-output link repairs and dated availability note |
| `deliverables/user/WI-20260909-ATS-004-summary.md` | Eight matching raw-output link repairs and dated availability note |

New files are the public register, this Evidence Pack and the
[WI016 summary](../user/WI-20260909-ATS-016-summary.md).
The 13 selected originals remain unchanged; their exact paths and original
hashes are in the public register and external `retainedPaths`.

## Selection And Reference Repair

The initial read-only pass enumerated **2,175 tracked Markdown files** with
`git ls-files -z -- '*.md'`. It extracted inline/image/reference-definition/HTML
links outside fenced code and parsed frontmatter dependencies with the existing
`frontend/node_modules/js-yaml`. It scanned the 96 original Markdown files for
dependency edges without deeply reading all historical narratives.

The tracked August 17 WI018 evidence and summary require **nine direct
document dependencies**. Their closure adds the WI013/014/015/017 handoffs:
**13 total**, comprising nine agent documents and four user summaries.
This preserves the original release record's consent/session, router,
document-reconciliation and independent-audit dependency context.
The closure contains 11 manifest-target YAML edges; none points to an
archive-only original. The two tracked WI018 documents require no edits.

Thirteen unique raw-output targets occur in 21 Markdown links across the
September 9 WI004 evidence/summary. Each now resolves to its exact register
anchor. One additional dated HTTP literal pointer in the payment index has an
explicit local-archive link. Historical literal filenames elsewhere remain
register lookup keys, not promises of local/remote file availability.
All 83 other original documents and 109 raw output/media/SQL files are archived;
bare filename mentions alone do not cause wholesale document promotion.

The selected 13 documents had zero findings in read-only screening for email
addresses, JWT/API/provider tokens, private-key blocks, credential assignments
and Korean telephone patterns. This is bounded screening, not a guarantee of
exhaustive PII/secret absence. The original Markdown pass also encountered an
unrelated existing YAML parse error in `.claude/agents/uv.md`; it was not
changed or treated as evidence of a full documentation PASS.

## Commands & Outputs

- Read-only Node/PowerShell scan over the saved manifest and Git-listed Markdown;
  path resolution is relative to each source document, with URL/anchor handling.
- Closed-set verification: **205** unique disposition entries match the manifest
  and public table; counts **13 RETAIN_GIT / 192 ARCHIVE_LOCAL**.
- Selected-document SHA-256 check: **13/13 match original manifest bytes**.
- Two repaired documents: **21 entry links plus two register notices** resolve;
  zero remaining actual links from those documents to archived originals.
- `git diff --name-only`: only the seven tracked paths above at the DocOps
  checkpoint. New registry/records are separate untracked allowlist additions.
- `removal-receipt.json` removed-path set equals the 192 archive dispositions.
  No archive extraction, source deletion or executable SQL was run by DocOps.

## MA Execution Receipt

MA supplied the following actual results, distinct from DocOps static checks:

| Check | Actual supplied result |
|---|---|
| Original preservation | All 205 source prehashes and all 205 restored prehashes verified; ZIP/manifest unchanged |
| Source removal | 192 removed; 13 original files retained untouched; zero archive-only source files remain |
| Deletion guard | Initial guard stopped before deletion when a new ignore rule hid a path. MA corrected it to use the manifest plus not-tracked checks; no safety bypass |
| Preservation baseline | 955 protected tracked source/test/build paths, 79 local assets/config and three runtime PIDs recorded before cleanup; this is not a post-cleanup comparison |
| Isolated source | MA extracted clean source from Git archive at `915dbd2` |
| Isolated backend | Full offline build with two workers was running at handoff; no final result supplied |

## Tests

DocOps ran no build, automated product tests, live probes, DB checks or full
documentation-validator sweep. Only the scoped static checks above are complete.
MA owns isolated documentation/build/tests and final source/runtime preservation.
No pending execution is marked PASS.

## Risks / Rollback

Raw originals are private local evidence, not remotely downloadable. Recovery
requires the archive custodian and original-hash verification, as described in
the register. Retained historical claims do not close production SR-93.

For rollback, MA can revert only this WI's tracked-document/ignore changes and
restore exact requested originals from the verified archive into a separate
private directory before any approved source restoration. Do not overwrite
current product/runtime/media/config, execute the SQL, or revert unrelated work.

## Follow-ups

**WI016 unblocks WI-20260909-ATS-017 now.** MA should immediately issue its
skill-generated handoff and delegate independent review. Do not wait for the
already running isolated backend build to begin WI017. Append later actual MA
preservation, documentation/build/test and Git receipts without rewriting prior
historical outcomes or closing REQ003 early.

## MA Verification Update

The following checks were executed after the DocOps handoff. Logs and structured
receipts are local-only under the archive directory named above.

| Gate | Observed result | Private receipt |
|---|---|---|
| Backend clean-source build | PASS, offline, two workers; 200 suites, 1,806 passed / 19 skipped / zero failures or errors; coverage thresholds pass | `backend-clean-build.log`, `backend-result.json` |
| Frontend clean installation | PASS, `npm ci --ignore-scripts --no-fund --no-audit`; unchanged lockfile | `frontend-clean-install.log` |
| Frontend automated tests | PASS, 112 files / 1,563 tests; coverage thresholds pass | `frontend-clean-tests.log`, `frontend-result.json` |
| Frontend quality/build | Typecheck, ESLint, Prettier and build all exit 0 | `frontend-typecheck.log`, `frontend-lint.log`, `frontend-format.log`, `frontend-clean-build.log` |
| Workspace documentation | PASS | `docs-workspace.log` |
| Source-only documentation | Initial 14 generated-report link errors, then PASS after narrow repairs | `docs-candidate.log`, `docs-candidate-repaired.log` |
| Index counts | 14 categories, 204 recursive non-index Markdown files, zero mismatches | `docs-index-counts.json` |
| Product/local preservation | All 955 tracked source/test/build paths and 79 local configuration/media files match their before hashes | `preservation-after.json` |
| Runtime continuity | Same three process IDs and start identities; local frontend/backend and public root/API each return HTTP 200 | `preservation-after.json`, `final-http.json` |
| Retained Git-source bytes | All 13 selected originals also match after source-only Git export | `manifest.json`, candidate source snapshot |

The build/test source was exported from Git checkpoint `915dbd2`. No product,
test, build or dependency path differs in this cleanup candidate. Tests used
H2 and mocks; the 19 environment-gated MySQL proofs were not enabled. These
results do not rerun live Provider/SMTP/UI acceptance or establish DB state.

The source-only check exposed two additional pre-existing document issues,
without changing the original 205-path population:

- September 8 WI001/WI003 evidence linked directly to Git-ignored `build/`
  reports (14 links). MA retained the report paths, dated results and hashes,
  replaced nonexistent checkout links with explicit generated-local-output
  references, and stated that reruns produce new evidence. Those reports were
  not moved, deleted or added to the 205-original archive.
- The documentation index had stale Design/Standards counts (29/12 instead of
  30/13). MA corrected them and defined recursive Markdown counting; the final
  total is 204, not the intermediate DocOps value of 202. This count is separate
  from the artifact-cleanup population.

WI017 independent review and final Git publication remain distinct closing
gates. Final publication receipts must record the delivered revision rather
than treating the staged candidate as remotely published.

## MA Delivery Closeout

WI017 independently verified the exact archive/retention sets, retained Git
bytes, references, preservation and narrow ignores. Both its findings were
resolved; no unresolved scoped finding remained. The final 30-file staged Git
tree was exported without ignored/private material; its documentation validator
passed (`docs-final-source.log`). Product/test/build paths were unchanged from
the clean source that passed the automated gates above.

Cleanup commit `1904805841fcb9b863bf14ca2143ad3f840a9c87` was pushed to
`origin/main` and matched `git ls-remote`. Both tracked and untracked changes
were zero immediately before this separate delivery-record update. These are
actual MA publication receipts, not part of the earlier DocOps claim.

The original archive's 205 ZIP members were rehashed and size-checked again.
An attempted cleanup of four external verification directories and three
source ZIPs was rejected by the execution tool before execution. Those temporary
copies therefore remain outside the repository; no removal is claimed.
`temporary-cleanup-receipt.json` records this non-product exception. The original
archive, manifest and test evidence remain preserved locally, not remotely.

REQ003 is closed within this exact repository-cleanup scope. No additional WI,
runtime/DB/media action, live external acceptance or production approval is
implied. The [REQ completion record](../user/REQ-20260909-ATS-003.md) is the
current closeout pointer.
