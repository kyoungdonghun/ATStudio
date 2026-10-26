---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: SE
category: evidence-pack
status: stable
dependencies:
  - path: WI-20261026-ATS-005-handoff.md
    reason: Approved scoped source preservation
---
# Evidence Pack: WI-20261026-ATS-005

## Summary
Preserved 73 exact public source/document files and 13 target-binding-redacted reference derivatives from ignored build trees, retaining originals and explicit runtime reconstruction limits.

## Scope / DoD Check
- [x] Reused existing reconstruction and artifact-retention principles; no all-build copy or new backup system.
- [x] Actual source bytes retained, not only a path list. Original and archive hashes recorded separately.
- [x] Target bindings removed only in visibly reference-only derivatives; originals unchanged.
- [x] Exact/reference reconstruction roundtrip and new helper safety checks passed without importing retained operational code.
- [x] Scoped Git attributes prevent newline normalization; read-only filtered/raw Git object identities match.
- [x] Exact candidate list delivered to PG for final classification; no stage/commit by SE.

## Reference Documents (Tier 0–2)
MA injected core-principles (evidence-led scoped work), development-standards (minimal reproducible changes), documentation-standards (metadata/dependencies/language), glossary (canonical WI/REQ), current AGENTS approve-once gates. Tier1 security-policy, quality-gates, versioning-policy. Tier2 approved REQ, current entry, prior reconstruction/retention registers, WI20261025 operating preparation and WI20261026 local preparation. Role SE requirements from `.claude/config/context-injection-rules.json`. Handoff existence verified before applying create-wi-evidence-pack.

## Evidence Pointers
- [Retention README](../../scripts/reconstruction/retained-20261026/README.md).
- [Exact selected archive manifest](../../scripts/reconstruction/retained-20261026/manifest.json): 86 source identities, exact/reference mode, original/archived hashes, removed categories, two exclusions and external dependencies.
- [Validation receipt](../../scripts/reconstruction/retained-20261026/validation.json).
- [Verifier/reconstructor](../../scripts/reconstruction/retained-20261026/verify.py) and [four focused tests](../../scripts/reconstruction/retained-20261026/test_verify.py).
- `.gitattributes` under the archive applies `-text` only to `files/**` and `references/**`; global Git attributes untouched.
- Local selection/triage receipts: `build/WI-20261026-ATS-005/source-candidates.json`, `archive-candidates-final.json`; PG `build/WI-20261026-ATS-004/retained-original-candidates.json` and `retained-archive-first.json`. Their raw matched values are never printed.

## Commands & Outputs
- Candidate selection inspected 88 public tool/document paths (673,037 source bytes), not a recursive copy of all build content.
- PG scanner triage and contextual consultation: synthetic test values and a hash-shaped phone match were distinguished from two personal home-path documents. The latter remain excluded unchanged.
- AST-selected private repository/snapshot/host/boot/proof bindings identified 15 distinct values; cross-file replacement removes them from 13 separately named reference files. The selected archive scan found zero remaining occurrences of those values. This is a named-binding inspection, not a claim of perfect general secret detection; PG final gate remains required.
- `python -I -B scripts/reconstruction/retained-20261026/test_verify.py` -> 4 PASS.
- `python -I -B scripts/reconstruction/retained-20261026/verify.py --compare-originals . --restore-to build/WI-20261026-ATS-005/reconstructed-source` -> RETAINED_SOURCE_VERIFIED, 86 original matches, 73 exact and 13 separate references reconstructed, runtime NOT_EXECUTED.
- `git check-attr text` confirms unset for archive payload paths. `git hash-object --stdin-paths` with and without `--no-filters` gives identical Git object IDs for all 86 payload files; no `-w`, index or commit operation.

## Tests / Boundaries
The four tests exercise exact CRLF roundtrip, refusal to overwrite an existing destination, corrupt input rejection, traversal/unexpected-member rejection, and reference-only placement. They run only the new helper. No historical backup/account/audio code executes, no private config/customer input is read, and no network/server/DB/application/provider action or new expense occurs. Filter identity is not yet a committed-blob assertion; WI007 owns that check.

## Risks / Rollback
The source package is deliberately incomplete for native execution: frozen remote runtime, JAR/crypto files, private authority/target/cost records and some target-bound original modules remain outside Git. Historical candidate manifests may reference excluded originals; do not weaken their checks or substitute reference files to make them runnable. Full disaster recovery is not established. Original evidence files and consumed execution records are unchanged. A failed reconstruction preserves its partial new destination and refuses blind overwrite; no cleanup/delete operation is performed. Corrections should supersede the new archive metadata, never rewrite old evidence.

## Follow-ups
WI007 must integrate the PG final archive allowlist, independently check committed blob/reconstruction identity, and perform authorized local commits. The separate future ID1-role/audit guard repair remains unimplemented. No additional WI is spawned by SE.

## Archive visibility recovery — 2026-10-26
MA's first ordinary staging attempt for group02 returned exit1 because the root
`build/` ignore pattern also excluded `files/build/` and `references/build/` inside
this archive. HEAD remained unchanged for that attempt; MA preserved the failure
and partial-stage evidence. SE added only an archive-local `.gitignore` with four
explicit re-inclusion rules. The root ignore file and original source bytes are
unchanged. Read-only `git check-ignore` and untracked visibility checks cover all
86 payload paths and their original `build/` paths; see the appended validation
receipt. This correction is not a force-add, index mutation or native trial.
The first visibility probe's Windows text-mode input introduced CR characters;
its failed assertion is retained in `archive-visibility-initial.json` under the
WI005 local evidence directory. A NUL-delimited binary Git probe then passed;
no further ignore-rule edit or full test rerun was needed.
