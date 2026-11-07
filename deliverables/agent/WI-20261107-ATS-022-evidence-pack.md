---
version: 1.0
last_updated: 2026-11-07
project: ATS
owner: DOCOPS
category: evidence-pack
status: candidate-prepared
dependencies:
  - path: WI-20261107-ATS-022-handoff.md
    reason: Approved selection scope
  - path: ../user/REQ-20261107-ATS-006.md
    reason: User authorization for selected commit and push
---
# Evidence Pack: WI-20261107-ATS-022

## Summary

Selected the coherent application/test/nginx change and minimal current public
documentation, preserving private and historical originals. MA alone owns actual
Git staging, commit, push and remote verification.

## Scope / DoD Check

- [x] Explicit UTF-8 path selection with reasons, original/export SHA-256 and expected Git blob ID.
- [x] Five changed production Java files, twenty changed/new test/support files and their nginx template kept together.
- [x] Client upload guide and concise dated operational checkpoint included.
- [x] Public document variants derived from reviewed HEAD without editing active originals.
- [x] Historical, generated, private and unrelated pending files excluded without deletion.
- [x] No server, database, cloud, Git-index, commit or push action performed by this WI.

## Reference Documents (Tier 0-2)

Injected pointers: [handoff](WI-20261107-ATS-022-handoff.md), approved REQ006,
AGENTS.md, core principles, development/documentation standards, glossary,
security policy, Current Decision Entry, previous WI-20261106-ATS-006 checkpoint
and reconstruction scanner. Context rule source:
`.claude/config/context-injection-rules.json`; role docops; Tier 0-2 relevant to
selection and public preservation. The create-wi-evidence-pack skill was applied
after confirming the handoff exists.

## Evidence Pointers

- `build/WI-20261107-ATS-022/selection.json`: exact target paths, actual source/export paths, per-file raw SHA-256, expected filtered Git blob and exclusions.
- `build/WI-20261107-ATS-022/public-export-map.json`: three public variants.
- `build/WI-20261107-ATS-022/original-custody/`: exact local originals, retained before export generation.
- `build/WI-20261107-ATS-022/prepare_public_selection.py`: local-only deterministic selection/export builder; no Git mutation or network operation.
- [Current public checkpoint](../../docs/registry/git-checkpoint-20261107.md): actual dated evidence ownership and exclusions.
- [Client guide](../user/ATStudio-client-upload-guide-20261107.html): existing observed upload/edit/publication instructions without credentials.

The private receipts and build outputs above are not promised Git checkout files.
Candidate review files are frozen before staging; final PG/tree/commit decisions
are recorded separately so that final hash receipts do not rewrite their inputs.

## Commands & Outputs

Used read-only Git status/diff/show/hash-object (without write), focused source and
evidence reading, and the local export builder. The three public variants start
from reviewed HEAD `6a8732a`, add only bounded current information and update their
document metadata. Original worktree document bytes remain unchanged.

Existing tracked `scripts/deployment/backup/` has no pending source delta. New
ignored recovery/deployment adapters are bound to retained one-time requests,
claims, native units and private input packets; they are not substituted for
the reusable tracked library. Those event-specific execution bundles remain
preserved locally instead of being bulk-copied into a clean-checkout workflow.

## Tests and Verification Limits

No application test/build or server trial was rerun by selection work. WI024
independently verified all 471 source/build and 213 test inputs against BEv5's
immutable manifests, the packaged classes and nginx candidate. Its retained
XML review confirms the 66 latest-suite outcomes and separate independent ten
tests; these are not a new full-suite execution. PG checks source and exact
public variants before the MA staged-byte gate.

The registry distinguishes original failed attempts from final completion, API
verification from human playback, and boot enable from reboot verification.
No new backup/restore success or current server health is inferred.

## Risks / Rollback

Git preserves implementation and safe guidance, not private data, evidence,
native configuration or a byte-identical artifact. The local original documents
can remain modified after publication because they retain material intentionally
omitted from public variants. Do not clear that state with deletion or blanket add.

Before commit, discard an unused export proposal only; preserve all originals.
After publication, use a scoped revert if needed rather than rewriting shared
history. WI023 privacy and WI024 independent review remain required before MA
commit/push. This WI does not itself claim those operations succeeded.
