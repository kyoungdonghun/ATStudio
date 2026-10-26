---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: guide
status: active
dependencies:
  - path: ../user/REQ-20261010-ATS-007.md
    reason: Approved bounded initial-server setup
---
# WI-20261010-ATS-016 Handoff

[WI HEADER]
WI ID: WI-20261010-ATS-016
REQ: REQ-20261010-ATS-007
Agent: se
Depends On: -
Blocks: WI-20261010-ATS-017
Skill: create-wi-handoff-packet used by MA; create-wi-evidence-pack required at closure.

[WI SUMMARY]
Why: Prepare minimal, auditable first-host access and package setup without losing access or exposing secrets.
Scope: Implement only bounded local operator helper(s)/runbook for first SSH verification and native secret entry, plus an explicit reviewed package/setup command plan based on existing templates. MA independently handles provider UI/host verification. No remote operations by this agent until specific MA target evidence and execution authorization.
DoD: Safe instructions/helper fail closed on absent expected host fingerprint; no private-key/passphrase exposure; actual vs synthetic evidence separate. Recommend the minimal existing Windows OpenSSH approach; do not introduce SSH libraries or silently enable services/install Git tools. Native passphrase input can remain a user handoff.
Write ownership: scripts/deployment/first-host/ (new only), deploy/single-vps/runbooks/first-host-setup.md (new), own WI016 summary/EP. Do not edit existing templates, source, REQ or docs/index.
Forbidden: purchases, browsers, credential bodies, key regeneration/decryption/export, private configuration reads, SSH host trust bypass, local service changes, remote command execution, broad firewall changes, SQL/init/dumps, application start, real email/payment, restic repository operations, deleting or committing files.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Bind intended host through external operator parameters and independently checked public host fingerprint; do not hardcode private addresses.
- [ ] Separate connectivity/host-key acquisition (untrusted) from authenticated login and explicit trust.
- [ ] Prefer simple documented native SSH invocation; describe Windows ssh-agent Disabled/Stopped and user unlock requirements without weakening encrypted-key protection.
- [ ] Plan Ubuntu24.04 official Java17/MySQL8/Nginx/FFmpeg packages, no automatic service exposure/default web page; no app DB creation. Installation and hardening remain separate guarded steps.
- [ ] Fresh-host survey precedes applying MySQL/journald/templates; preserve baseline and existing provider configuration.
Quality:
- [ ] Local syntax and meaningful negative tests where helper code is added.
- [ ] No extra architecture/library introduced; no auto-reboot or overwrite of existing SSH configuration.
- [ ] Explicit firewall/host-fingerprint/secret/DB gates and domain-before-TLS reminder.

[INPUT POINTERS]
Tier0: docs/standards/core-principles.md; docs/standards/documentation-standards.md; docs/standards/development-standards.md; docs/standards/glossary.md.
Tier1: docs/policies/security-policy.md; docs/policies/access-control-policy.md.
Tier2: deploy/single-vps/CONFIGURATION.md; deploy/single-vps/runbooks/deployment.md; deploy/single-vps/runbooks/resource-baseline.md; scripts/deployment/prepare-operator-ssh.ps1 (reuse patterns, never read private body); docs/index.md#current-decision-entry.
Context: deliverables/user/REQ-20261010-ATS-007.md; deliverables/agent/WI-20261010-ATS-014-evidence-pack.md.
Snapshot: main HEAD58b1011dad796017e47dbcc82a4225ca449bcdf6 plus dirty/untracked reviewed local prep. One real provider server was purchased with approved SSH/firewall; actual SSH and setup NOT RUN. Windows ssh-agent disabled/stopped; encrypted Ed25519 key exists. MA handles identity and prompts.

[OUTPUT CONTRACT]
User-facing: deliverables/user/WI-20261010-ATS-016-summary.md.
Agent-facing: deliverables/agent/WI-20261010-ATS-016-evidence-pack.md using create-wi-evidence-pack skill.
Report exact paths/commands/tests and next-stage guard. Do not mark actual host setup complete from a local plan.

[TRACEABILITY REQUIREMENTS]
Record baseline, local tests, reviewed hashes and no-side-effect proof where applicable. Rollback never deletes secrets/data. On completion notify MA so WI017 is generated/delegated immediately.
