---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved existing-host package maintenance continuation
---
# WI-20261010-ATS-022: Ubuntu Package Maintenance Preparation

[WI HEADER]
WI ID: WI-20261010-ATS-022
REQ: REQ-20261010-ATS-007
Agent: se
Depends On: WI-20261010-ATS-021
Blocks: WI-20261010-ATS-023

[WI SUMMARY]
Why: Prepare a bounded Ubuntu 24.04 package update and reboot without losing verified access or exposing stopped application services.
Scope (in): One concise runbook at deploy/single-vps/runbooks/os-maintenance.md and this WI's two deliverables. Reuse existing host-hardening checks.
Scope (out): Remote execution; credentials; application code; release upgrade; purchases; package removal/autoremove; source-list changes; public services; account/data changes.
DoD: Reviewed commands and failure handling for source/audit/space checks, simulation, configuration preservation, upgrade, reboot, postboot verification and exact temporary-agent cleanup.
Constraints: Preserve dirty files and historical evidence. No endpoint, host fingerprint, key material, raw host logs or console token in repository. MA executes approved remote steps only after PG review.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Verify official noble sources, signature enforcement, package-manager idle state, storage and boot space before mutation.
- [ ] Refresh indexes and inspect exact simulation; fail on removals, source surprises or uncertain access.
- [ ] Handle Ubuntu 24.04 needrestart behavior, conffile preservation and mysql/nginx masks explicitly.
- [ ] Retain strict host pin, recovery console, fresh atadmin/sudo proof, changed boot ID and postboot security/config checks.
- [ ] Rollback limitations are explicit: configuration backups are not package/kernel rollback snapshots.
Performance:
- [ ] Review actual download/disk requirements; do not infer production capacity from maintenance success.
Quality:
- [ ] Validate documented command syntax using available local tools and report limits.
- [ ] Cite primary Ubuntu/APT documentation for nontrivial package behavior.
- [ ] Evidence Pack and summary separate local preparation from NOT RUN remote execution.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
- docs/standards/evidence-pack-standard.md
Tier 1:
- .claude/agents/se.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/react-best-practices/AGENTS.md (conditional stack pointer only; no frontend work)
- docs/standards/frontend-standards.md (conditional stack pointer only)
- deploy/single-vps/runbooks/host-hardening.md
- deploy/single-vps/runbooks/first-host-setup.md
- deploy/single-vps/CONFIGURATION.md
Context:
- deliverables/user/REQ-20261010-ATS-007.md
- deliverables/agent/WI-20261010-ATS-021-evidence-pack.md
Files:
- scripts/deployment/host-hardening/check_layout.py
- scripts/deployment/host-hardening/test_candidates.py

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-022-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-022-evidence-pack.md
Handoff: deliverables/agent/WI-20261010-ATS-022-handoff.md
Only the runbook and the two assigned deliverables may be edited.

[TRACEABILITY REQUIREMENTS]
Record sources, command checks, hashes, limitations, recovery actions and follow-up WI023. Do not assert live-host success.
