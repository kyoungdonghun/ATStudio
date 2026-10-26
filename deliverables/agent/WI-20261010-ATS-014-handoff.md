---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
---
# WI-20261010-ATS-014: Independent Local Safety Review

[WI HEADER]
WI ID: WI-20261010-ATS-014
REQ: REQ-20261010-ATS-006
Agent: pg
Depends On: WI-20261010-ATS-011, WI-20261010-ATS-012, WI-20261010-ATS-013
Blocks: WI-20261010-ATS-015

Scheduling note: WI012 is complete. Start read-only review of that completed slice while WI011/WI013 finish; the dependency set is mandatory for final review completion. Do not certify in-progress bytes. MA will send remaining completion receipts; recheck final hashes before closing.

[WI SUMMARY]
Why: Independently inspect implementation and reproduce focused local tests before reporting preparation complete.
Scope: Review new Java admission/bindings, deployment configuration/validator, backup automation and fake tests. No product edits; findings returned to owning SE.
DoD: Severity-ranked concrete findings with exact file/line/repro, fixed revisions rechecked, independent test receipts and clear NOT RUN boundaries.
Constraints/Forbidden: No purchase/install/remote/secrets/real DB or services/restic/cloud/external send/cleanup/git operations. No descendants. Synthetic mock tests only; no existing storage. Preserve unrelated dirty work. Do not weaken validators.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Check full route/controller storage coverage, early servlet filter order, strict-firewall/identity trust, real binding and bounded map/permit release.
- [ ] Check Nginx zone composition/normalized paths, upload-before-body, protected files and localhost binding.
- [ ] Check backup gates, source consistency, immutable run dump, same snapshot inventory, nonzero/partial refusal, quiescence/resume and interrupted receipt preservation.
- [ ] Check command allowlist, bounded bytes/memory/time, privacy, no hidden full-media reread, path/symlink/mount/lock/cgroup handling, inactive templates.
- [ ] Explicitly identify host-hook functionality still requiring target integration. Do not call a failclosed stub a completed production adapter.
Performance:
- [ ] No target capacity/readiness claim from static/mock tests.
Quality:
- [ ] Reproduce Python unit suites and safe Java focused tests after coordinating build ownership; inspect test scope before running.
- [ ] Preserve last-good on failure, no automatic backup deletion/init/unlock/copy/restore.
- [ ] Any current code blockers resolved or transparently left open, not buried as future QA.

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
- deploy/single-vps/runbooks/resource-baseline.md
- deploy/single-vps/runbooks/restic-backup-plan.md
- .agents/skills/test/SKILL.md
- .agents/skills/build-check/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
REQ and evidence:
- deliverables/user/REQ-20261010-ATS-006.md
- deliverables/agent/WI-20261010-ATS-011-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-012-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-013-evidence-pack.md
Files:
- src/main/java/com/atstudio/atstudio/security/StorageMutationAdmissionFilter.java
- src/main/java/com/atstudio/atstudio/config/StorageMutationAdmissionProperties.java
- src/main/resources/application.yml
- deploy/single-vps/
- scripts/deployment/

[OUTPUT CONTRACT]
Own only deliverables/user/WI-20261010-ATS-014-summary.md and deliverables/agent/WI-20261010-ATS-014-evidence-pack.md.
Use create-wi-evidence-pack skill, include injected context and exact reproducible tests/hashes.
Notify MA promptly of concrete blockers; do not silently accept them.
[TRACEABILITY REQUIREMENTS]
Date/environment/test commands/results/findings/recheck/rollback pointers. Distinguish source changes from old tracked dirty docs. After completion immediately trigger WI015.

