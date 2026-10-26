---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: agent
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-005.md
    reason: Restic method approved, execution prohibited
---

# WI-20261010-ATS-009: Restic Recovery Plan

[WI HEADER]
WI ID: WI-20261010-ATS-009
REQ: REQ-20261010-ATS-005
Agent: pg
Depends On: -
Blocks: WI-20261010-ATS-010

[WI SUMMARY]
Why: Choose an encrypted external backup method without whole-media plaintext/encrypted staging on a 100GB host.
Scope: Write deploy/single-vps/runbooks/restic-backup-plan.md and this WI's two deliverables only. No edits to existing runbooks/templates or any executable operations.
DoD: Implementable design with consistency, peak disk and memory gates, retention cost model, secrets/recovery independence and failure/restore criteria.
Constraints: Restic choice is explicitly approved. No installation, binary execution, init, backup, dump, copy, prune, deletion, secret access/generation, cloud mutation, payment, remote contact, commit or runtime changes. Do not refresh broad provider audit. No proprietary credentials in evidence.

[ACCEPTANCE CRITERIA]
- [ ] Cite current official restic documentation, with date; infer compatibility only, do not claim iwinv was tested.
- [ ] Use private S3-compatible remote repository, direct file backup without precompressing/encrypting entire media archives, bounded local DB dump/cache/metadata. Include complete existing recovery inventory and keyring.
- [ ] Cold writer-free capture initially; DB dump and file set belong to same window. Stop admission, drain, stop sole JVM/FFmpeg/schedulers, capture, verify completion before resume. Database dump is logical, not copying live MySQL data files. Avoid holding users down for off-provider copy/full repository verification unnecessarily.
- [ ] Deal with first full backup versus daily increments, timeout/partial snapshot/nonzero exit, failure notifications and service reopening approval after incomplete backup. Restic is not a database-consistency engine.
- [ ] Retention starting design 7 daily + 4 weekly + 3 monthly + 2 prechange points as existing proposal, overlapping selections not additive by assumption; no automatic destructive command approval. Specify dry-run review, last-known-good, protection/holds, pruning/resource budget and actual stored-byte monitoring.
- [ ] Off-provider independently controlled encrypted copy, operator-owned Windows/external-disk path unresolved; explain copying restic snapshots vs merely mirroring a shared repository, independent password/recovery material and stale/offline-copy limits.
- [ ] Local reserve max(20% filesystem,10GiB), plus measured peak operations. No fixed dedupe/compression promise. Include initial transfer duration formula and maintenance-overrun decision.
- [ ] Resource budget coordinating with WI008, no backup/encoder/large ingest overlap initially; define pre-invitation isolated restore (egress deny AND no real schedulers/local writes), fresh target, metadata/data check, application acceptance and no paid disposable host without approval.
- [ ] Design-only scheduling proposal, exact target/time/recipient/credential paths remain execution inputs; preserve existing test evidence.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md; docs/standards/development-standards.md; docs/standards/documentation-standards.md; docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md.
Tier 2: deploy/single-vps/runbooks/backup-restore.md; scripts/reconstruction/README.md; docs/design/runtime-storage-operations.md.
REQ/context: deliverables/user/REQ-20261010-ATS-005.md; deliverables/user/WI-20261010-ATS-005-summary.md; deliverables/user/WI-20261010-ATS-007-summary.md.
Official sources: https://restic.readthedocs.io/en/stable/030_preparing_a_new_repo.html ; https://restic.readthedocs.io/en/stable/040_backup.html ; https://restic.readthedocs.io/en/stable/045_working_with_repos.html ; https://restic.readthedocs.io/en/stable/060_forget.html .

[OUTPUT CONTRACT]
- deploy/single-vps/runbooks/restic-backup-plan.md: concise English operational design, preferably <=250 lines.
- deliverables/user/WI-20261010-ATS-009-summary.md.
- deliverables/agent/WI-20261010-ATS-009-evidence-pack.md using create-wi-evidence-pack skill.

[TRACEABILITY REQUIREMENTS]
Link exact authoritative sources; all execution/compatibility/load/restore NOT RUN. Report unresolved choices without assuming user approval. Completion triggers WI010 after WI008 also completes. Changes are additive docs only.
