[WI HEADER]
WI ID: WI-20260714-ATS-035
REQ: REQ-20260714-ATS-001
Agent: se
Depends On: WI-20260714-ATS-021
Blocks: WI-20260714-ATS-022, WI-20260714-ATS-024, WI-20260714-ATS-025, WI-20260714-ATS-027, WI-20260714-ATS-034

[WI SUMMARY]
Why: Close the fresh-schema blocker discovered by the disposable MySQL rehearsal so the acceptance profile can start with Hibernate `ddl-auto=validate`.
Scope: Align `Track.waveformData` with the `tracks.waveform_data` DDL in the canonical fresh schema, add a narrowly scoped existing-database manual patch, add or update a schema contract test, and rerun the disposable MySQL/Hibernate validation workflow.
Out: Applying DDL to the existing local/application DB, production DB changes, data migration, waveform feature redesign, or unrelated schema cleanup.
DoD: Fresh schema contains a nullable `TEXT` `waveform_data` column matching the JPA mapping; the manual patch is safe and documented for separately approved use; contract tests pass; disposable MySQL Hibernate validation succeeds; disposable DBs are dropped and existing DBs remain untouched.
Constraints: Do not execute the manual patch against any existing database. Do not print or persist DB credentials or complete JDBC URLs. Do not modify or revert unrelated concurrent edits or runtime logs.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Fresh `schema.sql` creates `tracks.waveform_data` with the type/nullability expected by `Track.waveformData`.
- [ ] A dated manual patch exists for already-created databases and does not run automatically.
- [ ] Hibernate `ddl-auto=validate` succeeds against a uniquely named disposable MySQL 8 database built from the current schema and ordered manual patches.
- [ ] Disposable databases are dropped and the configured application DB is never selected or modified.
Quality:
- [ ] Focused schema contract tests pass.
- [ ] `git diff --check` passes for owned changes.
- [ ] Evidence contains redacted commands/results and explicit DB safety proof.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
- docs/standards/documentation-standards.md
- docs/standards/glossary.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- deliverables/agent/WI-20260714-ATS-021-handoff.md
- deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
- docs/design/p1-security-acceptance-hardening-design.md
Files:
- src/main/java/com/atstudio/atstudio/entity/Track.java
- src/main/resources/schema.sql
- src/main/resources/db/manual/
- deliverables/agent/WI-20260714-ATS-021/

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-035-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-035-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-035-handoff.md

[TRACEABILITY REQUIREMENTS]
Evidence pointers, exact schema contract, manual-patch applicability, disposable database name policy, redacted validation result, cleanup proof, existing-DB non-interference, tests, and rollback are required.
