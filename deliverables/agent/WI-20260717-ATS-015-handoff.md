[WI HEADER]
WI ID: WI-20260717-ATS-015
REQ: REQ-20260716-ATS-004
Agent: qa-integ
Depends On: WI-20260717-ATS-014, V1 cleanup evidence commit 50fe759
Blocks: Documentation closeout and unified-branch acceptance testing

[WI SUMMARY]
Why: Perform one final read-only audit after V1 consolidation and determine whether code, the live local MySQL schema, tests, and current-state documentation still agree.
Scope (in/out): In scope are current code/API/schema/config/document comparison, a read-only live MySQL manifest review, identification of the exact drift between the recreated V1 baseline (153 indexes, manifest c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506) and the current local database (155 indexes, manifest 910ed7a3e8444ee54f570968becc52cfba306e817d5e0986fe61f669783e76d8), stale current-state documentation, and a prioritized finding ledger. Out of scope are source/document edits, database writes or recreation, data deletion, Git ref/index mutation, server startup, acceptance execution, production deployment, and push.
DoD: Produce exact findings with evidence, distinguish schema drift from ordinary test/demo data, classify each finding as FIX, DOCUMENT, ACCEPT, or PRODUCTION-GATE, and return PASS only if no unexplained P0/P1 mismatch remains.
Constraints/Forbidden: Read-only. Do not create/drop/alter databases or tables. Do not modify data. Do not read or reproduce application-local.yml or any secret value. Do not edit tracked or untracked files. Do not stage, commit, switch branches, start public tunnels, or push.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Compare source schema/seed/config contracts with the current local MySQL manifest without database mutation.
- [ ] Explain the exact cause or tightest reproducible boundary of the 2-index and manifest-hash drift.
- [ ] Verify retired manual migration, legacy API, fallback, demo, and compatibility paths have no active residual references.
- [ ] Reconcile current code/API/DB/UI behavior with current-state docs while preserving historical REQ/WI records as history.
- [ ] Separate local data cleanup decisions from structural schema correctness.
Performance:
- [ ] Avoid starting full runtime unless a read-only static or metadata check cannot answer the question.
Quality:
- [ ] Every finding includes severity, file or database object, evidence command/pointer, impact, and recommended verification.
- [ ] No secrets or personal data appear in outputs.
- [ ] No repository or database mutation occurs.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md
- docs/policies/versioning-policy.md
- docs/policies/execution-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/user/WI-20260717-ATS-014-summary.md
- deliverables/agent/WI-20260717-ATS-014-evidence-pack.md
- deliverables/agent/WI-20260717-ATS-004-evidence-pack.md
- docs/index.md
- docs/registry/project-registry.md
- docs/design/db-schema.md
- docs/design/api-spec.md
- docs/SR/SR-42.md
- docs/SR/SR-93.md

Files:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/main/resources/application.yml
- src/main/resources/application-acceptance.yml
- src/main/resources/db/manual/
- deliverables/agent/WI-20260717-ATS-004/V1MysqlProofManager.java
- deliverables/agent/WI-20260717-ATS-004/run-v1-mysql-proof.ps1
- frontend/src/
- src/main/java/

Repro/Logs:
- Recreated V1 manifest: deliverables/agent/WI-20260717-ATS-004/run-20260717-021207-9953beed/local-manifest-final.log
- Current read-only manifest observed 2026-07-17: 39 tables, 449 columns, 155 indexes, 80 foreign keys, 0 forbidden tables, 0 forbidden columns, 6 plans, 9 Toss-only provider columns, manifest 910ed7a3e8444ee54f570968becc52cfba306e817d5e0986fe61f669783e76d8
- Current branch: codex/p1-acceptance-hardening
- Current HEAD: 50fe759749f079c6f41736a0acc685db3181c89b

[OUTPUT CONTRACT]
User-facing -> return a concise finding summary to MA; do not write files during this read-only WI.
Agent-facing -> return a detailed evidence ledger in the final response; MA will standardize it into an Evidence Pack after review.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-015-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Required for every finding
Tests: Read-only metadata/static commands only; report commands and exit state
Rollback: Not applicable because mutation is forbidden
