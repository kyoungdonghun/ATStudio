---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Approved Object Storage and restic implementation
  - path: WI-20261011-ATS-069-handoff.md
    reason: Candidate ownership and execution boundaries
---
# WI-20261011-ATS-070

[WI HEADER]
WI ID: WI-20261011-ATS-070
REQ: REQ-20261011-ATS-007
Agent: pg
Depends On: WI069 candidate and MA target observations
Blocks: MA first backup and isolated restore; WI067 real feature trial

[WI SUMMARY]
Why: Independently review exact backup/bootstrap/restore bytes before the first native credential or database operation.
Scope: Own build/WI070-review/ and the two named reports. Read WI069 and existing backup contracts. Independently test local synthetic/fault paths only. Review credential containment, private repository, password escrow, cold-state capture, complete file and SQL comparison, bounded resources, isolated restore, failed-operation cleanup and exact-target gating.
DoD: Findings-first review, independently reproduced tests, final immutable pins, clear ALLOW/HOLD for each step, explicit native prerequisites and verification limits. Review incrementally while SE prepares remaining files; do not treat an unfinished candidate as approved.
Forbidden: No server/cloud/network actions, actual secrets or private recovery files, database/app start, credential generation, package install, deletion, retention/prune, public exposure, source edits, subdelegation, commit or push. No application secrets in cloud under the current pre-feature recovery scope.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Reject drift, wrong target, symlinks/special files, unsafe permissions and incomplete backup inputs.
- [ ] No secret outputs/command arguments; root-only target credentials and separately recoverable password.
- [ ] Verify real cold dump and restored schema/rows/files without production overwrite or app/external calls.
Performance:
- [ ] Finite deadlines, initial transfer cap and disk/memory limits with conservative cleanup.
Quality:
- [ ] Independent failure-path tests and exact reviewed hashes.
- [ ] Separate local proof, native prerequisites, snapshot verification and full-disaster-recovery limitations.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; docs/standards/evidence-pack-standard.md
Tier2: .agents/skills/test/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md; deploy/single-vps/backup/README.md; deploy/single-vps/runbooks/restic-backup-plan.md
Context: deliverables/user/REQ-20261011-ATS-007.md; deliverables/agent/WI-20261011-ATS-069-handoff.md; build/WI069-backup/; scripts/deployment/backup/guarded_backup.py

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261011-ATS-070-summary.md
Agent-facing: deliverables/agent/WI-20261011-ATS-070-evidence-pack.md
Handoff: this skill-generated packet. Apply create-wi-evidence-pack after review.

[TRACEABILITY REQUIREMENTS]
Record commands/results/hash evidence, untouched prior evidence, rollback and exact remaining gate. No synthetic result promoted to native PASS. MA injects Tier0 and current target facts; security values are never injected.
