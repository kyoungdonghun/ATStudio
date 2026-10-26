---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
---
# WI-20261010-ATS-015: Local Preparation Integration and Evidence Closeout

[WI HEADER]
WI ID: WI-20261010-ATS-015
REQ: REQ-20261010-ATS-006
Agent: docops
Depends On: WI-20261010-ATS-014
Blocks: -

Scheduling note: All implementation slices and MA reruns are complete. Preparation/current-state drafting may overlap final PG review, but do not close REQ or mark review PASS before WI014 final receipt. Reconcile any final review findings and hashes before completion.

[WI SUMMARY]
Why: Make current code/configuration/automation and actual verification discoverable without overwriting history.
Scope: Update docs/index.md Current Decision Entry; deploy/single-vps/{README.md,CONFIGURATION.md,runbooks/index.md,runbooks/resource-baseline.md,runbooks/restic-backup-plan.md}; scripts/deployment/README.md; REQ006 completion/progress and own summary/EP. Touch root README only if current navigation requires it. Do not modify application, scripts, templates, old REQs/EP/hash records, other docs or memory.
DoD: Accurate current implementation entry, selected/applied candidate/target-tested distinction, exact test counts/owners/bounds, remaining host adapter/schedule/restore/copy gates, document validation and whitespace/hashes.
Constraints/Forbidden: No runtime/remote/DB/restic/secrets/actual backup/installation/purchase/external send/delete/git commit/push. No descendants. Use apply_patch and preserve unrelated existing edits. No new mandatory purchase preconditions; actual service application/invitation gates remain.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Link REQ006 and WI011-014 final evidence; remove misleading current template-unchanged claims while preserving dated design history explicitly.
- [ ] Configuration table matches actual source/contract, not just old selected values.
- [ ] Automation coverage precise: source capture core, routine window policy, unconfigured host adapters, inactive timer, independent alert transport/copy/restore boundaries.
- [ ] Daily capture does not imply full-media download or actual restore proof. No cost cap or supported-user promise.
- [ ] No claim missing Gmail/WAV/phone TEST success; historical bounded successes retained.
Performance:
- [ ] Actual Linux/S3/resource/load/restore remain NOT RUN and production HOLD.
Quality:
- [ ] validate-docs skill plus focused links/metadata/newline and git diff --check.
- [ ] New current source/config/script hash inventory or pointer to reviewed inventory.
- [ ] Final unresolved code findings not disguised as future operational gates.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/documentation-standards.md
- docs/standards/development-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Tier 2:
- docs/index.md
- deploy/single-vps/README.md
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/runbooks/resource-baseline.md
- deploy/single-vps/runbooks/restic-backup-plan.md
- scripts/deployment/README.md
- scripts/deployment/backup/
- .agents/skills/validate-docs/SKILL.md
- .agents/skills/create-wi-evidence-pack/SKILL.md
REQ and final results:
- deliverables/user/REQ-20261010-ATS-006.md
- deliverables/agent/WI-20261010-ATS-011-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-012-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-013-evidence-pack.md
- deliverables/agent/WI-20261010-ATS-014-evidence-pack.md

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261010-ATS-015-summary.md
Agent-facing -> deliverables/agent/WI-20261010-ATS-015-evidence-pack.md
Use create-wi-evidence-pack skill, reference injected context. Technical docs English; REQ Korean. Close REQ only if all local DoD actually met; distinguish target deferred work.
[TRACEABILITY REQUIREMENTS]
Exact date/environment/commands/results, current hash identity and owner. Preserve historical records. No memory update. Report WI chain complete only after all dependencies. Restore owned doc hunks only if rollback requested.

