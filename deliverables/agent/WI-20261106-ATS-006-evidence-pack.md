---
version: 1.0
last_updated: 2026-11-06
project: ATS
owner: EO
category: evidence-pack
status: active
dependencies:
  - path: WI-20261106-ATS-006-handoff.md
    reason: Approved selection and reconstruction scope
  - path: ../user/REQ-20261106-ATS-003.md
    reason: User approval for privacy review and selected Git publication
---
# Evidence Pack: WI-20261106-ATS-006

## Summary

Prepared coherent application/backup/public-document groups with exact path/hash
manifests, retained original evidence, reusable source validation and public
reconstruction guidance. MA alone performs staging, commit and remote operations.

## Scope / DoD Check

- [x] Selected source, tests, static examples and operator guides as coherent groups.
- [x] Preserved prior excluded/private originals and reviewed candidate source bytes.
- [x] Verified existing application/outer-candidate identities before reusing tests.
- [x] Prepared safe public document variants without changing active original docs.
- [x] Recorded exact local raw SHA-256 and expected Git-filtered blob identities.
- [x] Supplied the exact final proposal for PG document-delta and public-history review.
- [x] Defined the EO shareability and MA staged-blob gate before commit/push.
- [x] Reserved actual Git/remote results for the separate local MA receipt.

## Reference Documents (Tier 0-2)

Injected scope and principles came from the [handoff](WI-20261106-ATS-006-handoff.md).
Tier 0: `AGENTS.md`, core principles, documentation/development standards and glossary.
Tier 1: security, quality gates and execution policy. Tier 2: approved REQ003,
WI005 privacy scope, current decision entry, reconstruction scanner, October 26
exclusion records, November 5 application integration and November 6 outer-recovery
completion evidence. Context rules: `.claude/config/context-injection-rules.json`;
assignee EO/docops; task types documentation/review; required Tier 0-1.

Applied `lint`, `validate-docs` and `create-wi-evidence-pack` skills. The evidence
skill's handoff precondition was satisfied. No subagent or application-source
change was needed. The approved scope permits ordinary local recovery and review.

## Evidence Pointers

All local receipt paths below are retained, ignored custody; they are not public
dependencies, backups, runnable native authority or promised clean-checkout files.

- `build/WI-20261106-ATS-006/selected-candidates-initial.json`: 161 current candidates.
- `source-identity-check.json`: application 1,015, backend-v2 470 and frontend-v3 342
  inputs unchanged; all 41 outer candidate-02 source/copy hashes unchanged.
- `original-doc-custody-manifest.json`: original document bytes retained before
  preparing public exports. The working index remains unchanged at this stage.
- `public-export-map.json`: target/export mapping for the bounded public variants.
- `omitted-doc-link-inventory.json`: first-pass 23 links/dependencies to omitted
  local records; two retrospective links also redirected in the final export.
- `public-export/docs/registry/git-checkpoint-20261106.md`: safe current summary.
- `public-export/docs/registry/git-checkpoint-20261106-source.json`: 1,068 source
  inputs with reviewed local SHA-256 and expected repository-filtered blob ID.

The October 26 records are classification evidence, not blanket authority to
publish unknown later bytes. PG rechecks prior exact hashes and new content for
the public remote. The selected 161-file first pass is cleared by PG's separate
hash-bound allowlist; modified document variants require their own delta review.

## Commands & Outputs

Read-only Git inventory, `git hash-object` without write, SHA-256 comparison,
Python AST/JSON parsing and focused Markdown-link analysis were used. The export
script writes only ignored WI006 output and copies original document bytes into
its local custody directory. No staging/index/Git-history write was performed here.

The two functional groups are application+listening and guarded backup operations.
Shared Java services and frontend/API contracts stay together. Their tests and
example configuration are included. Public documents form the final group; personal
host instructions, prior private/ambiguous deliverables, screenshots/PDFs, raw logs
and JAR/SPA artifacts remain excluded without deletion.

## Tests and Quality Boundaries

Existing candidate evidence was reused only after source identity equality.
No Java/frontend suite, build, app, server, actual database, mail, payment,
backup or restore was run by this WI. The decisive prior outer-recovery run is
51 PASS (33 independent + 18 author); overlapping prior counts are not summed.

Current selected syntax checks: 41 Python AST parses and five JSON decodes passed.
Markdown/ruff/jq executables are absent; no dependency install was requested or
performed. Scoped metadata/link checks are reported separately from all-rules lint.
The repository-wide validator has no CLI path/help parser and was inspected before
use. Its earlier historical-link failure remains unresolved; no broad cleanup or
claim of global documentation PASS is made.

Git attributes can normalize CRLF to LF. The raw source identity and expected Git
blob are recorded separately. This checkpoint does not promise byte-identical JAR,
private config, archive or native pinned-bundle reconstruction from a fresh clone.

## Risks / Rollback

Public source reconstructs implementation, tests and static examples only. Omitted
private evidence is not recoverable from Git; a path/hash receipt is not a backup.
Native MySQL, Linux/root/systemd/cgroup, durability, actual publication, current
restore and service continuity remain outside this local checkpoint's proof.

Before publication, discard only the unused export proposal if needed; preserve
all source/candidate/original custody. After a commit, use an approved scoped revert
instead of reset, force push, deletion or replacing historical failures. MA must
compare the exact staged blobs to the final PG allowance and recheck remote state.

## Follow-ups

WI005 blocks final public clearance; WI006 then blocks MA Git execution and the
single planned completion receipt. No server WI is triggered by this checkpoint.
MA records actual committed revisions and remote equality in a separate local
closure receipt. This source checkpoint does not self-embed its own commit ID,
imply native execution or require recursive documentation commits.
