[WI HEADER]
WI ID: WI-20260717-ATS-016
REQ: REQ-20260716-ATS-004
Agent: se
Depends On: WI-20260717-ATS-015
Blocks: Approved local V1 database recreation and documentation closeout

[WI SUMMARY]
Why: Prevent JPA metadata from recreating two indexes that do not match the canonical fresh-only schema baseline.
Scope (in/out): In scope are the JPA table/unique-index declarations for `PaymentSettlement` and `SocialAccount`, and focused regression tests that enforce exact agreement with `schema.sql`. Out of scope are product behavior changes, API changes, enum policy changes, schema.sql changes, database mutation, data deletion, documentation changes, server startup, Git operations, and push.
DoD: JPA declares exactly the canonical unique keys `uq_payment_settlements_deduplication_key` and `uq_social_accounts_provider_id`; duplicate unique declarations are removed; focused tests and relevant backend tests pass; no unrelated file changes occur.
Constraints/Forbidden: Do not mutate any database. Do not read `application-local.yml`. Do not modify `schema.sql`. Do not alter entity fields or runtime behavior beyond DDL metadata alignment. Do not revert other work. Do not stage, commit, switch branches, or push. You are not alone in the codebase; accommodate existing changes.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] `PaymentSettlement` exposes one named unique constraint for `deduplication_key` matching `schema.sql`.
- [ ] `PaymentSettlement` no longer declares a duplicate unique index through both table metadata and `@Column(unique = true)`.
- [ ] `SocialAccount` unique constraint name exactly matches `schema.sql`.
- [ ] A focused contract test fails if either entity metadata drifts from the V1 schema names or reintroduces the duplicate declaration.
Performance:
- [ ] No runtime query or index-set expansion is introduced.
Quality:
- [ ] Relevant backend tests pass.
- [ ] Java formatting and compile checks pass.
- [ ] Changed files are limited to the assigned entity/test scope.

[INPUT POINTERS]
Tier 0 (Constitution - Required for all):
- docs/standards/core-principles.md

Tier 0 (Standards - Based on Assignee):
- docs/standards/development-standards.md

Tier 1 (Policies - Inferred from REQ/WI content):
- docs/policies/quality-gates.md
- docs/policies/security-policy.md

REQ/Context Docs:
- deliverables/user/REQ-20260716-ATS-004.md
- deliverables/agent/WI-20260717-ATS-015-evidence-pack.md
- deliverables/user/WI-20260717-ATS-015-summary.md
- docs/design/db-schema.md

Files:
- src/main/java/com/atstudio/atstudio/entity/PaymentSettlement.java
- src/main/java/com/atstudio/atstudio/entity/SocialAccount.java
- src/main/resources/schema.sql
- src/test/java/com/atstudio/atstudio/config/V1BackendBaselineContractTest.java

Repro/Logs:
- Current local manifest: 155 indexes, SHA-256 `910ed7a3e8444ee54f570968becc52cfba306e817d5e0986fe61f669783e76d8`.
- Canonical fresh V1 manifest: 153 indexes, SHA-256 `c48d3c75378aaf2364d89ed06833ba68e27a5a334dbc4670d1443bd938c6c506`.
- Extra indexes: `idx_payment_settlements_dedup`, `uq_social_accounts_provider_provider_id`.

[OUTPUT CONTRACT]
User-facing -> return a concise implementation and test summary to MA; MA will standardize the final user deliverable.
Agent-facing -> return exact changed files, reasoning, tests, and residual risks; MA will generate the Evidence Pack.
Handoff Packet -> deliverables/agent/WI-20260717-ATS-016-handoff.md :
- This packet.

[TRACEABILITY REQUIREMENTS]
Evidence pointers: Exact changed files and relevant lines required
Tests: Report exact commands and outcomes
Rollback: Revert only the assigned entity/test changes
