---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: EO
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261026-ATS-007-handoff.md
    reason: Approved final integration and commit gate
  - path: ../user/REQ-20261026-ATS-001.md
    reason: Local preservation authorization
---
# Evidence Pack: WI-20261026-ATS-007

## Summary
Independently integrated the privacy, retention and source reviews; MA completed three exact-path local preservation commits. This document accompanies the final checkpoint commit. Stage two remains open.

## Scope / DoD Check
- [x] Reconciled PG raw allowances and every selected path with Git filtered object identities before staging.
- [x] MA alone staged and committed; actual commit path sets and empty index after each commit were confirmed.
- [x] Preserved original history, excluded local files and failed/replaced planning receipts.
- [x] Preserved 36 specific document byte streams with exact scoped attributes; no global normalization change.
- [x] Current entry and approved REQ are updated; no dependent WI follows WI007.
- [x] No server, DB, app, provider, new backup, network publication, push/fetch or cost operation.

## Reference Documents (Tier 0-2)
Injected Tier0: core-principles (evidence-led scope), development-standards (minimal changes), documentation-standards (metadata and two deliverable sets), glossary (canonical IDs). Tier1: AGENTS approve-once policy, security-policy, quality-gates, versioning-policy. Tier2: approved REQ, Current Decision Entry, reconstruction README, WI004/005/006 handoffs and Evidence Packs, prior WI20261025-006/008 source evidence. Role EO; injection rule source `.claude/config/context-injection-rules.json`. The WI007 handoff existed before applying create-wi-evidence-pack; lint and validate-docs were applied as scoped checks because their global scripts also traverse ignored/private trees.

## Actual Local Commits
| Commit | Paths | Result |
|---|---:|---|
| `f6742ffc970fec6d24191cb29714f1216787cb7f` | 7 | Optional NCP source/configuration/tests; default SMTP retained |
| `254e2fe36db9d80fac4c483d06b1c07ceb242af8` | 168 | Operating tools/settings and retained-source archive |
| `513455895db53caef9c4a90c42f56982646b9e7d` | 558 | Reviewed documentation/evidence/client artifacts and scoped attributes |

Initial main HEAD was `91a16e72693d7cd6ed04e0d36a5bd160af96f6d8`. The local origin/main reference was three commits behind initially; no remote freshness was checked. The final checkpoint commit's own hash belongs in MA's final report, avoiding self-reference. Exact committed paths are reproducible with `git diff-tree --no-commit-id --name-only -r <commit>`.

## Evidence Pointers
- [PG baseline allowance](WI-20261026-ATS-004-allowlist.json): 731 exact raw-hash allowances and 63 exclusions. The 48 sensitive/machine-bound historical texts and 15 unreviewed/generated binaries remain local, unchanged. Ignored private material is outside that visible-file count. MA rehashed all 63 excluded files after commit3: 63 unchanged, zero changed or missing.
- [PG review](WI-20261026-ATS-004-evidence-pack.md), plus archive-ignore, document-attributes, scanner-format and report-format delta JSON files beside it: explicit overrides; baseline stays immutable. Manifest self-files were separately reviewed to avoid recursive hashes.
- [Retained archive](../../scripts/reconstruction/retained-20261026/README.md) and [manifest](../../scripts/reconstruction/retained-20261026/manifest.json): 73 exact files and 13 visibly reference-only derivatives. Original build sources remain ignored. Reference derivatives are not executable restore replacements.
- [Source review](WI-20261026-ATS-006-evidence-pack.md) and [source identities](WI-20261026-ATS-006-source-allowlist.json): seven current raw and expected LF Git identities; prior source/JAR/XML pins match.
- [Exact whitespace preservation cases](WI-20261026-ATS-007-whitespace-preservation.json): one archived source and 82 historical documents, each pinned by raw SHA and expected Git blob. No generic exclusion.
- Ignored `build/WI-20261026-ATS-007/` contains group manifests, NUL pathspecs, commit messages, independent/scoped checks and superseded plans. These local execution receipts are intentionally not promised by a fresh Git clone; public commit objects, PG allowances and archive manifests retain the durable boundary.

## Commands and Checks
- Selected Python AST parsing: 65 files; selected JSON parsing: 36 files; PowerShell AST parsing: 3 files. No retained operational module was imported or executed. No dependencies installed.
- `python scripts/reconstruction/retained-20261026/verify.py --compare-originals .`: 86 MATCH; original and retained hashes unchanged, runtime NOT_EXECUTED. SE's fresh-directory roundtrip and four focused filesystem-safety checks were reviewed and reused.
- Read-only Git attributes/object checks: no custom filters for selected paths; all 86 archived payloads keep raw Git bytes. Scoped document attributes protect 36 exact records. Normal active-document and two Java/config LF transformations are recorded separately, not described as raw-byte identity.
- Source validation reused the repaired 16 NCP tests and 26 earlier unchanged tests with matching pins; no fresh whole-42 or whole-project run. NCP remains NOT_DEPLOYED.
- MA checked per-group current raw hashes, exact pathspec sets, all staged blob identities and actual committed path sets. Hooks contained samples only; no bypass was used.
- Initial scoped trailing-space check did not test blank-at-EOF. The later CRLF-aware check explicitly covered EOF blanks. MA used command-only `core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol`; each historical exception was first matched to its exact raw hash and staged blob, then only that path was excluded. All remaining paths passed. No global Git configuration was altered.
- Final current documents require PG delta allowance and the same exact staged gate before MA's checkpoint commit. Their local link/metadata/JSON/whitespace checks are scoped; a global Markdown linter, full doc audit, actual Word re-render and runtime acceptance were not run here.

## Preserved Recovery History
Initial group02 staging stopped because root `build/` ignore rules also hid 86 archived payloads; only 81 approved files entered the index, with no commit. A reviewed archive-local .gitignore made the payloads visible while original build files stayed ignored. Group02b then exposed preserved CRLF and two EOF blanks: a new scanner's extra blank was removed with AST equality, while the exact archived source stayed untouched. Group02c passed the explicit preservation gate and was committed. Two new PG reports had only surplus EOF blanks removed; historical files were not rewritten. Original plans, scanner/report originals and failed-attempt receipts remain separate.

## Risks / Rollback
This is a local Git preservation checkpoint, not off-device backup or complete runtime reconstruction. Credentials, JARs/native receipts and some historical build/private dependencies are intentionally absent from Git. Pattern scans and manual context review do not prove all historical data is anonymous. The 63 visible exclusions remain untracked; a clean worktree is not claimed. Rollback should use a reviewed follow-up commit for exact affected paths; do not reset, clean, amend or overwrite retained originals.

## Follow-ups
WI004-007 have no next WI in this approved chain. Stage two still awaits client policy decisions and combined NCP/Toss signup/contract/keys; target activation and delivery/LIVE validation remain open. Stage-three safeguards are not complete. The historical backup role guard needs a separately reviewed current-state candidate before any new backup execution; this task did not repair it or authorize server work.
