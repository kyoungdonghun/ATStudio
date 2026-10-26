---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
---
# WI-20261010-ATS-013: Guarded restic cold-capture automation and fake failure verification

[WI HEADER]
WI ID: WI-20261010-ATS-013
REQ: REQ-20261010-ATS-006
Agent: se
Depends On: -
Blocks: WI-20261010-ATS-014

[WI SUMMARY]
Why: Implement the approved local preparation, not activate production.
Scope: Own new scripts/deployment/backup/ and deploy/single-vps/backup/ files, new deploy/single-vps/systemd/atstudio-backup.* templates, and own deliverables. Do not edit existing env/contract/validator/Java files. Implement small readable Python-standard-library guarded orchestration and fake tests, following full restic-backup-plan. Default plan/validate is side-effect-free; real execution requires explicit exacttarget approval/config, Linux/root/privatepath/repo/credential-ref/window/budget gates. Never run real restic, dump, network or services this turn; no installs/deps. No repo init/unlock/forget/prune/source cleanup automation. Prepare disabled timer without unapproved exacttime or boot catchup. Wholehelpergroup768Mhigh/1Gmax, CPU100% Nice10. State machine closeadmission/quiesce/fence -> boundedlogicaldump -> directfilecapture including both roots/realrelease/settings/manifest -> exact snapshot and retrievedmanifest/dumphash verification -> only then authorized resume. Failure leaves maintenance state visible; never resume under ongoing capture. Monitor disk/inodes/dump/cache/temp/time and stopownedhelperprocessgroups, privacy-safe failure receipts, lock run shared operations, no stale dumpreuse or latest snapshots. Include independent failure/missed/26h/8d status checking without external send. Templates must distinguish notconfigured from executable; add safe explicit hooks if hostdetailunknown, not pretend integration proved. Full-run receipts/lastgood atomic and failure-safe; partial exit3/unknown/noinventory/hashmismatch rejected. Need tests for success/failures/timeouts/locks/pathsymlink escapes/lowdisk/oldgoodpreservation/defaultnoaction. Restore validation preparatory only, require isolatednewtarget and scheduler/egress gate; no real restore. Externaldisk copy is guarded future operation if implementation notsafe now, report clearly. Verify official restic flags via primarydocs if uncertain.
DoD: scoped implementation, focused tests, two-set evidence; report actual limits.
Constraints/Forbidden: No purchase/install/remote/actualDB/restic/cloud/secrets/external send/real service control/deletion/commit/push. Preserve all pre-existing dirty assets; use apply_patch. No spawned descendants. Shared workspace; only assigned paths.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Approved baseline implemented within assigned scope.
- [ ] Failure paths tested with synthetic fixtures, no real side effects.
Performance:
- [ ] Target performance remains NOT RUN; no supported-user claims.
Quality:
- [ ] Tests and static review evidence, exact paths and before/after hashes.
- [ ] No unrelated changes or weakened security assertions.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md
- docs/standards/frontend-standards.md (conditional workspace injection; no frontend edits)
- .agents/skills/react-best-practices/AGENTS.md (conditional workspace injection; no frontend edits)
- deploy/single-vps/runbooks/resource-baseline.md
- deploy/single-vps/runbooks/restic-backup-plan.md
- deliverables/user/REQ-20261010-ATS-006.md
- deliverables/user/REQ-20261010-ATS-005.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md

Files: scripts/deployment/backup; deploy/single-vps/backup; deploy/single-vps/systemd/atstudio-backup.*

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261010-ATS-013-summary.md
Agent-facing -> deliverables/agent/WI-20261010-ATS-013-evidence-pack.md
Use create-wi-evidence-pack skill. Keep historical deliverables immutable.

[TRACEABILITY REQUIREMENTS]
Record inspected source, files, commands, failures, changed-hash inventory and local-only verification. Rollback by restoring owned changes only. Notify MA of interface changes promptly. Next WI014 independently reviews all implementation.

