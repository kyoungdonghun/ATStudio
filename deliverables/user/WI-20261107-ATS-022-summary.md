---
version: 1.0
last_updated: 2026-11-07
project: ATS
owner: DOCOPS
category: summary
status: candidate-prepared
dependencies:
  - path: ../agent/WI-20261107-ATS-022-evidence-pack.md
    reason: Selection evidence and preservation boundaries
---
# WI022 — Selected source preservation

The proposed commit contains five production Java changes, twenty test/support
files, the matching nginx template, the existing client upload guide and minimal
current documentation. It preserves the administrator/listening modes and capture
polling fixes together with their regression coverage.

Historical private execution records, generated binaries and unrelated pending
files remain local. Active original documents are preserved; safe Git variants
contain the necessary current checkpoint. Existing reusable backup source has no
pending delta; one-time recovery/deployment bundles remain in their original custody.

The server is untouched. Privacy review, exact staged-byte verification and MA
commit/push results are separate from this candidate-preparation report. See the
[evidence pack](../agent/WI-20261107-ATS-022-evidence-pack.md) and
[current checkpoint](../../docs/registry/git-checkpoint-20261107.md).
