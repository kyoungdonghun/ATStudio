---
version: 1.0
last_updated: 2026-10-26
project: ATS
owner: SE
category: guide
status: stable
dependencies:
  - path: ../../../deliverables/agent/WI-20261026-ATS-005-handoff.md
    reason: Approved source preservation scope
  - path: ../README.md
    reason: Source reconstruction and private runtime boundaries
---

# Retained operating source — 2026-10-26

This archive preserves useful source formerly present only under ignored `build/`.
It is **not a deployable release, complete runtime backup or authorization to run
the historical commands**. Read the current decision entry before any execution.

The selection contains **73 exact-byte files** and **13 reference-only derivatives**.
Original files remain unchanged at their original locations. Two documents with
personal home paths are deliberately excluded. No JAR/class, private input, SQL
dump, raw native receipt, screenshot, browser state or credential is included.

## Contents and limits

The [manifest](manifest.json) maps every archived file to its original repository-
relative path and SHA-256. Exact entries retain their original bytes. Entries
under `references/` have target bindings removed and end in `.reference.txt`;
their original and derivative hashes are distinct. These are review material,
not executable replacements for pinned tools. The originals remain local-only.

| Selected material | Preservation scope |
|---|---|
| Backup WI011 and stage-one WI017 | Capture/inventory/restore/cost source and local test evidence. Actual repository/snapshot/host/proof bindings are removed from reference-only files. Complete native candidate reconstruction is therefore not claimed. |
| Read-only evidence/preflight WI008/WI014 | Public source and dependency modules. Historical private authority and remote frozen runtime remain separately required. |
| Account fixture/inspection and stage-two repair | Public implementation/test source. Crypto binaries, original private inputs, target-bound modules and original full candidate trees are not reconstructed. |
| Audio HTTP header repair | Standalone helper source only; not the entire audio-trial runtime. |
| Operating modes and October26 preparation | Public checker, source pins, selected safe receipts, privacy map and client-trial material. Effective environment and historical release JAR remain excluded. |

Some retained candidate manifests reference files that are deliberately absent or
reference-only. That is an explicit archival limitation, not a reason to relax
their checks. Old backup guards still expect the old ID1 role and audit counts;
their current-state repair is not part of this archive. Existing success/failure
claims remain dated. A restored script is never new server approval.

The `.gitattributes` in this directory applies `-text` only to archived bytes.
This prevents Git checkout/staging newline normalization from changing original
hashes. The repository's global attributes are unchanged.

The archive-local `.gitignore` re-includes only `files/build/` and
`references/build/`, because the inherited root `build/` rule also matches these
copies. Original repository-root `build/` contents remain ignored. The first
ordinary staging attempt exposed this omission; its failure receipt is preserved.
The correction does not use force-add or change the root ignore policy.

## Verify without running retained tools

From the repository root with Python 3.10 or later:

```text
python -I -B scripts/reconstruction/retained-20261026/verify.py
python -I -B scripts/reconstruction/retained-20261026/verify.py --compare-originals .
python -I -B scripts/reconstruction/retained-20261026/test_verify.py
```

The helper checks fixed manifest membership, bounds, path containment, regular
files/no links or reparse points, archive hashes and exact/reference identity.
The optional comparison reads only the manifest's known original source paths.
It prints counts/status, never source contents. It imports no archived module,
does not invoke operational commands and has no network, DB or secret loader.

## Reconstruct a new local source inspection tree

```text
python -I -B scripts/reconstruction/retained-20261026/verify.py --restore-to build/retained-source-inspection-new
```

The destination must not exist and its parent must already be a real directory.
The helper validates the archive before creating it, writes exact files back to
their relative `build/...` paths **inside that new tree**, and verifies every
written hash. Reference material goes into a separate `reference-only/` tree;
it is never written over the missing original module path. Existing files are
never overwritten. If a write fails, preserve the partial destination for
inspection; do not retry in it or treat it as complete. No automatic deletion.

Original relative links in archived documents refer to their original layout.
They are retained history and are not a claim that all linked private evidence
is in Git. The new tree still lacks the full application source context and
required private/runtime artifacts. Follow the existing reconstruction guide
for application code, toolchains and separately approved data recovery.

## Validation at preparation

WI005 compared all 86 retained entries to the untouched original SHA-256 values,
reconstructed 73 exact files plus 13 separately placed references in a new local
tree, and passed four focused filesystem safety checks for this new helper.
These checks do not rerun the old backup/account/audio trials. See the
[evidence pack](../../../deliverables/agent/WI-20261026-ATS-005-evidence-pack.md).
Git blob identity and final privacy approval are owned by the later commit gate.
