---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved continuation and exact admin approval
  - path: WI-20261010-ATS-018-evidence-pack.md
    reason: Last actual host checkpoint
---

# WI-20261010-ATS-019 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-019
REQ: REQ-20261010-ATS-007
Agent: se
Depends On: WI-20261010-ATS-018
Blocks: WI-20261010-ATS-020

[WI SUMMARY]
Why: Prepare bounded staged admin/SSH, directory, journal and DB adoption while MA restores approved access and surveys the host.
Scope: Own only a new `deploy/single-vps/runbooks/host-hardening.md`, necessary small helpers under `scripts/deployment/host-hardening/`, focused tests and these two WI deliverables. Reuse reviewed MySQL/journal templates unchanged; no product source changes or historical receipt edits.
DoD: Reviewable exact operations, fail-closed preflights, staged independent SSH acceptance, guarded rollback and local tests. Prefer a short runbook with discrete reviewed commands over a large provisioning framework.
Forbidden: Any remote action, native secret prompt, private key/config access, purchase, SQL mutation, app/web activation, destructive cleanup, git stage/commit/push or modifications outside owned paths. Never infer live PASS from local tests.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Separate `atadmin` with existing supplied public key, locked password and explicit NOPASSWD sudo; private key never leaves PC. User explicitly approved this mechanism.
- [ ] New session key login and `sudo -n` before root/password restriction; independently verified recovery path retained. Check OpenSSH first-value/Include/Match/socket behavior; validate syntax and exact connection context before reload.
- [ ] Root local console password retained. Unused forwarding restrictions must not disable normal shell administration. No blanket firewall changes.
- [ ] Nonlogin `atstudio`, immutable releases, nonnested mode0700 public/private/tmp, nginx private body temp, ACME token directory and root-private config directories follow CONFIGURATION.md. Reject existing collisions/symlink ancestry; no recursive chown/chmod.
- [ ] MySQL existing config include order proven; candidate loopback3306/X OFF/512M/max30 checked via mysqld validator, then bounded start and safe read-only value queries only. No app DB/users/secrets. Nginx remains masked.
- [ ] Journal limits/forwarders reviewed; no manual vacuum or old-log deletion. Capture actual effective policy and safe synthetic receipt if feasible.
Performance: No application capacity claim; report host headroom and no extra paid goods.
Quality:
- [ ] Validate script syntax/local failure paths if scripts are added. Preserve old hashes and provide precise rollback with no auto destructive cleanup.
- [ ] Remote specifics arrive from MA; unknowns fail closed. Do not embed endpoints, contacts, keys, fingerprints or tokens in repository.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- .claude/agents/se.md
- docs/policies/security-policy.md
- docs/policies/access-control-policy.md
- docs/policies/quality-gates.md
Tier 2:
- .agents/skills/create-wi-evidence-pack/SKILL.md
- .agents/skills/react-best-practices/AGENTS.md (conditional catalog pointer; no React changes)
- docs/standards/frontend-standards.md (conditional catalog pointer; no frontend execution)
- docs/standards/evidence-pack-standard.md
- docs/templates/eval-report-template.md
- docs/templates/wi-subagent-handoff-template.md
- docs/templates/requirements-request-template.md
- docs/index.md#current-decision-entry
- deploy/single-vps/CONFIGURATION.md
- deploy/single-vps/mysql/atstudio.cnf
- deploy/single-vps/journald/atstudio.conf
- deploy/single-vps/systemd/atstudio.service
- deploy/single-vps/runbooks/first-host-setup.md
Snapshot:
- deliverables/user/REQ-20261010-ATS-007.md
- deliverables/agent/WI-20261010-ATS-018-evidence-pack.md
- MA owns fresh host survey, user native-key prompt and actual remote execution. Existing main HEAD58b1011 and dirty/untracked work preserved. Ubuntu24.04.4 /2CPU/8GB, MySQL8.0.46 and Nginx masked/inactive, packages present; target recheck pending.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-019-summary.md
Agent-facing: deliverables/agent/WI-20261010-ATS-019-evidence-pack.md
Use the injected create-wi-evidence-pack skill; list changed paths/hashes/tests and ready/held stages.

[TRACEABILITY REQUIREMENTS]
Skill execution: approved REQ read; existing WI maximum018 checked; ATS resolved from workspace.json; se requirements and security/testing/operation task pointers resolved from context-injection-rules.json. Full mandatory Tier0 plus role/security are injected by MA in order; pointers are not a claim of runtime verification.
No remote receipt belongs to SE. Record exact commands, local results, rollback and next WI020 trigger. Stage stop points are mandatory.
