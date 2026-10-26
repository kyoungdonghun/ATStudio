---
version: 1.0
last_updated: 2026-10-10
project: ATS
owner: MA
category: handoff
status: approved
---
# WI-20261010-ATS-011: Java storage-mutation admission and explicit mail timeout bindings

[WI HEADER]
WI ID: WI-20261010-ATS-011
REQ: REQ-20261010-ATS-006
Agent: se
Depends On: -
Blocks: WI-20261010-ATS-014

[WI SUMMARY]
Why: Implement the approved local preparation, not activate production.
Scope: Own only src/main Java/config resources, focused src/test Java, and own deliverables. Do not edit deployment assets. Read current transaction/coordinator/controller/filter source. Implement global2/IP1 immediate admission before multipart/body/service transaction, HTTP429 Retry-After2, no waiting queue. Cover all Track/Album/Playlist/Notice/Question/CompanyCertification write/replace/delete paths including JSON/nonmultipart cleanup. Preserve auth and trusted client identity; reject/normalize variants consistently with Spring firewall. Release exactly once on sync exceptions, errors and asynchronous completion/timeout/error; no retained IP map leak. Avoid unrelated business logic changes. Bind defaults app.storage.mutation-admission.enabled=true, max-concurrent=2, max-concurrent-per-ip=1, retry-after-seconds=2 using env APP_STORAGE_MUTATION_ADMISSION_ENABLED/MAX_CONCURRENT/MAX_CONCURRENT_PER_IP/RETRY_AFTER_SECONDS. These key names are shared with WI012. Explicit MAIL_SMTP_CONNECTION_TIMEOUT=5000, MAIL_SMTP_TIMEOUT=10000, MAIL_SMTP_WRITE_TIMEOUT=10000 placeholders under spring.mail.properties.mail.smtp. Test actual binding. Explicit multipart threshold default0B if appropriate. Inventory nested/background connection consumers; do not claim pool10 is a mathematical deadlock cure. Run focused offline Gradle tests only with mock/no DB or external effects; read test/build skills. Record file hashes/change list/tests/limits in EP.
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

Files: src/main/java/com/atstudio/atstudio; src/main/resources/application.yml; src/test/java/com/atstudio/atstudio

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20261010-ATS-011-summary.md
Agent-facing -> deliverables/agent/WI-20261010-ATS-011-evidence-pack.md
Use create-wi-evidence-pack skill. Keep historical deliverables immutable.

[TRACEABILITY REQUIREMENTS]
Record inspected source, files, commands, failures, changed-hash inventory and local-only verification. Rollback by restoring owned changes only. Notify MA of interface changes promptly. Next WI014 independently reviews all implementation.

