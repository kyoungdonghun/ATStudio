[WI HEADER]
WI ID: WI-20260716-ATS-036
REQ: REQ-20260716-ATS-004
Agent: sa
Depends On: -
Blocks: WI-20260716-ATS-038

[WI SUMMARY]
Why: Determine the V1 database/configuration baseline and identify obsolete or duplicate manual migrations, initialization paths, bootstrap runners, profiles, flags, and fallback configuration.
Scope (in/out): Read-only inspection of schema, manual SQL, JPA entities, application profiles, bootstrap/configuration code, Gradle resources, DB documents, and references. Do not execute SQL, connect mutatively to MySQL, edit files, or alter Git/branch/worktree state. Only the required WI summary and Evidence Pack may be created.
DoD: Map every active DB creation/update path and all nine manual SQL files; classify each as KEEP/REMOVE/REPLACE/ARCHIVE/REVIEW; define a clean V1 fresh-schema target and evidence needed before retirement. Identify all config/profile consumers and distinguish acceptance tooling from production bypasses.
Constraints/Forbidden: No DDL/DML, DB drop/create, `ddl-auto=update`, schema edits, file deletion, or migration execution. Do not assume Hibernate validates indexes/constraints.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `schema.sql`, all `db/manual/*.sql`, entity mappings, bootstrap runners, profiles, and initialization properties are mapped.
- [ ] Prerequisites, overlap, duplicate semantics, backfills, and data-dependent blockers are documented.
- [ ] A V1 baseline proposal covers fresh DB creation, seed ownership, `ddl-auto=validate`, index/constraint assertions, and migration-history disposition.
Performance:
- [ ] No shared DB or runtime process is started/stopped/mutated.
Quality:
- [ ] Every retirement candidate has a proof plan and rollback source in Git history.
- [ ] No product, SQL, configuration, or DB state changes occur.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred):
- docs/policies/versioning-policy.md
- docs/policies/archive-policy.md
- docs/policies/security-policy.md
- docs/policies/quality-gates.md

Tier 2 (Task Context):
- docs/design/db-schema.md
- docs/design/p1-payment-db-integrity-design.md
- docs/design/p1-payment-integrity-remediation-design.md
- docs/payment/system-overview.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- AGENTS.md

Files:
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- src/main/resources/application.yml
- application-local.example.yml
- src/main/java/com/atstudio/atstudio/config/
- src/main/java/com/atstudio/atstudio/bootstrap/
- src/main/java/com/atstudio/atstudio/entity/
- build.gradle

Repro/Logs:
- static schema/entity/config inventory and read-only Git/reference commands

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260716-ATS-036-summary.md:
- Korean summary, baseline recommendation, candidate counts, approval-sensitive DB decisions
Agent-facing -> deliverables/agent/WI-20260716-ATS-036-evidence-pack.md:
- Ordered inventory, overlaps, evidence pointers, fresh-DB proof plan, risks/rollback
Handoff Packet -> deliverables/agent/WI-20260716-ATS-036-handoff.md:
- This packet

[TRACEABILITY REQUIREMENTS]
Evidence pointers (files/lines/commands/logs): Required
Tests: Static/read-only validation only; no SQL execution
Rollback: No DB/product mutation; Git pointers for every proposed retirement
