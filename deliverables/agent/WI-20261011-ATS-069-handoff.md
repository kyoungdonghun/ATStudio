---
version: 1.0
last_updated: 2026-10-11
project: ATS
owner: MA
category: reference
status: active
dependencies:
  - path: ../user/REQ-20261011-ATS-007.md
    reason: Explicit option2 activation and backup restore approval
---
# WI-20261011-ATS-069
[WI HEADER]
WI ID: WI-20261011-ATS-069
REQ: REQ-20261011-ATS-007
Agent: se
Depends On: User option2 approval; target facts supplied by MA
Blocks: WI070 independent review, MA backup and isolated restore, WI067 features

[WI SUMMARY]
Why: Wire the existing guarded backup design to a first cold capture and isolated restore on the named server, without pretending synthetic hooks are production implementation.
Scope (in/out): Own build/WI069-backup/ tools/tests and own two reports. Reuse/read scripts/deployment/backup and deploy/single-vps/backup contracts. Prepare reviewed deployment adapter and one-time bootstrap/restore recipe; no remote execution, actual credentials, service changes, install, cloud access, data deletion, repository prune, app start or recurring activation. Do not change existing guarded core without presenting a concrete required fix to MA first.
DoD: Executable narrow tools for current app/DB-stopped target; stop-on-drift inventory, root/private permissions, logical MySQL dump, complete public/private roots and exact release artifacts, restic encrypted private repository, exact snapshot verification, fresh isolated restore destination and isolated MySQL import comparison with no SMTP/payment. Existing data/config never overwritten. Existing app/DB state restored after bounded operation. Give MA earliest minimal read-only inventory recipe now, without secret paths/body exposure; final tools after target facts.
Constraints/Forbidden: Server atstudio-prod-01, approved management access only. No secret values in args/stdout/docs. No copy of SMTP/JWT/billing keys into cloud backup under this temporary pre-feature scope without explicit separate authorization; state that full disaster recovery key escrow remains required. New restic password stored privately with independent recovery reference, not same repository only. No existing source deletion or automatic retention pruning. Initial repository planned under1GB unless measured inventory requires report, rates20KRW/GBmonth exclVAT; budget not hard cap. No fake success/skip of unknown hooks. Use finite deadlines/resources/byte limits, preserve failures. No native gates relaxed just to pass.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Real observed cold-state fence, complete logical dump and manifest, exact restore verification and SQL row/schema checks.
- [ ] Fresh target-only restore; no restored app execution or external connections.
- [ ] Public bounded receipts contain no secrets/PII/raw SQL.
Quality:
- [ ] Synthetic success/failure tests, immutable final pins and reproduction recipe.
- [ ] Explicit target/install/recovery/credentials gates before MA execution.
- [ ] No old files modified or source change outside own scope.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md
Tier1: docs/policies/security-policy.md; docs/policies/quality-gates.md; docs/standards/evidence-pack-standard.md
Tier2: .agents/skills/test/SKILL.md; .agents/skills/create-wi-evidence-pack/SKILL.md; deploy/single-vps/runbooks/restic-backup-plan.md; deploy/single-vps/backup/README.md
Context: deliverables/user/REQ-20261011-ATS-007.md; deliverables/agent/WI-20261010-ATS-013-evidence-pack.md; scripts/deployment/backup/guarded_backup.py; deploy/single-vps/backup/config.example.json; deliverables/agent/WI-20261011-ATS-062-evidence-pack.md

[OUTPUT CONTRACT]
User: deliverables/user/WI-20261011-ATS-069-summary.md
Agent: deliverables/agent/WI-20261011-ATS-069-evidence-pack.md
Local candidate: build/WI069-backup/
Skill-generated handoff: this file. No subdelegation.

[TRACEABILITY REQUIREMENTS]
Exact commands, hashes, current facts vs pending assumptions; no claim of backup/restore before MA receipts. ATS context rules from .claude/config/context-injection-rules.json. Preserve source/native security invariants and explicit rollback.
