---
version: 1.0
last_updated: 2026-09-09
project: ATS
owner: se
category: reference
status: active
dependencies:
  - path: ../user/REQ-20260909-ATS-004.md
    reason: Approved reconstruction scope
---

# WI-20260909-ATS-019 Handoff

[WI HEADER]
REQ: REQ-20260909-ATS-004
Agent: se
Depends On: none
Blocks: WI-20260909-ATS-020

[WI SUMMARY]
Why: Make required working context and environment prerequisites discoverable without this chat or PC.
Scope: A concise root README entrypoint, reconstruction guide and bounded read-only PowerShell preflight with focused tests, based on existing scripts and actual build versions/config contracts. Separate source-only reconstruction from private state restoration and production release approval.
DoD: A clean checkout can locate rules/current SoT/history, install correct tool/dependency versions, run safe checks, and identify precisely what private assets and operator steps remain. Missing settings must never be silently synthesized as a working runtime.
Forbidden: Changes to product logic/current runtime configuration, existing DBs/media/processes, extra DBs, external calls/charges/refunds/mail, private backup uploads or dumps, broad tooling redesign. Do not commit/push.

[ACCEPTANCE CRITERIA]
- [ ] README points to live AGENTS/CLAUDE/rules/SoT and reconstruction entrypoint.
- [ ] Guide covers Java/Gradle/Node/npm/Python/MySQL/FFmpeg/cloudflared from repo evidence, fresh vs retained DB, explicit local vs acceptance settings, database/public/private tuple and encryption keyring.
- [ ] Source-only and private/runtime checks are visibly separate. No automatic server startup, schema application, scheduler/network actions, or config-value output.
- [ ] Private off-device encrypted backup inventory, separate decryption access, restore hash/paired snapshot gates, known historical missing media, and production tasks 2-4 boundaries are explicit.
- [ ] Tests exercise normal/missing/invalid inputs without needing live services; do not weaken existing guards.
- Performance: small scripts, standard PowerShell/.NET only; no new dependency unless necessary and approved.

[INPUT POINTERS]
Tier 0: docs/standards/core-principles.md, docs/standards/development-standards.md, docs/standards/documentation-standards.md, docs/standards/glossary.md.
Tier 1: docs/policies/security-policy.md, docs/policies/archive-policy.md.
Tier 2: docs/design/runtime-storage-operations.md, scripts/acceptance/README.md, scripts/database/README.md, docs/payment/index.md, docs/SR/SR-93.md (resolve actual path).
Context: deliverables/user/REQ-20260909-ATS-004.md; build.gradle, frontend/package.json and lockfile; application-local.example.yml; frontend/.env.example; actual startup scripts/config classes.

[OUTPUT CONTRACT]
Owned edits: README.md; scripts/reconstruction/README.md, Test-DevelopmentRecovery.ps1, test-development-recovery.ps1 and minimal data/helper files if needed; docs/index.md; AGENTS.md/CLAUDE.md only short new entrypoint links if needed; own WI019 evidence and user summary.
Do not edit registry files owned by WI018. Read actual template YAML securely; never print real secret configuration. Use apply_patch for edits and create-wi-evidence-pack skill for completion. Report test commands/results and exact limitations/new-PC versus same-PC source-copy distinction.
