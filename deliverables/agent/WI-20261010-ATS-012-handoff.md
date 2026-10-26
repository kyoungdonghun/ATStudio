---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
---
# WI-20261010-ATS-012: Executable deployment templates and independent validation contract

[WI HEADER]
WI ID: WI-20261010-ATS-012
REQ: REQ-20261010-ATS-006
Agent: se
Depends On: -
Blocks: WI-20261010-ATS-014

[WI SUMMARY]
Why: Implement the approved local preparation, not activate production.
Scope: Own deploy/single-vps/{env,nginx,mysql,systemd/atstudio.service} existing files and scripts/deployment/{validate_bundle.py,test_validate_bundle.py}. No Java or backup directory edits, no other systemd units (WI013 owns new backup files). Align resource-baseline selected values: Xms256m Xmx1024m metaspace256m MemoryHigh2G Max3G, mysql buffer512M maxconn30, Hikari10 minidle2 timeout5000, multipart threshold0B, SMTP5000/10000/10000. Admission shared env APP_STORAGE_MUTATION_ADMISSION_ENABLED=true, APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT=2, APP_STORAGE_MUTATION_ADMISSION_MAX_CONCURRENT_PER_IP=1, APP_STORAGE_MUTATION_ADMISSION_RETRY_AFTER_SECONDS=2. MAIL_SMTP_CONNECTION_TIMEOUT/TIMEOUT/WRITE_TIMEOUT corresponding values. Nginx preserve general8/IP32global, sharedmedia4/IP16global, download1/IP2global speed2m, stream256k. Compose every zone in childlocations; cannot overwrite/drop parent constraints. Prebody multipart2global1IP using maps/limits and cover auth failures before app bodyparse. Preserve protectedroutes, matrix/encoded spelling protection, overwritten trustedheader, no publicalias, denyall invitation default, no caching/XAccel/buffering change. Strengthen independent validators and positive/negative fixture tests; don't merely change expected constants to silence errors. Bind proof from WI011 source through structural check where reasonable. Record only local synthetic verification; no actual Nginx/systemd/MySQL/restic/install/services.
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

Files: deploy/single-vps/env; deploy/single-vps/nginx; deploy/single-vps/mysql; deploy/single-vps/systemd/atstudio.service; scripts/deployment/validate_bundle.py; scripts/deployment/test_validate_bundle.py

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261010-ATS-012-summary.md
Agent-facing -> deliverables/agent/WI-20261010-ATS-012-evidence-pack.md
Use create-wi-evidence-pack skill. Keep historical deliverables immutable.

[TRACEABILITY REQUIREMENTS]
Record inspected source, files, commands, failures, changed-hash inventory and local-only verification. Rollback by restoring owned changes only. Notify MA of interface changes promptly. Next WI014 independently reviews all implementation.

