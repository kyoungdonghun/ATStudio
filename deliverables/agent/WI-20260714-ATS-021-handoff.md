[WI HEADER]
WI ID: WI-20260714-ATS-021
REQ: REQ-20260714-ATS-001
Agent: qa-integ
Depends On: WI-20260714-ATS-004, WI-20260714-ATS-007, WI-20260714-ATS-008, WI-20260714-ATS-010, WI-20260714-ATS-012
Blocks: WI-20260714-ATS-023, WI-20260714-ATS-025, WI-20260714-ATS-026, WI-20260714-ATS-034

[WI SUMMARY]
Why: Prove the fresh schema and ordered manual patches against disposable MySQL 8 before any existing local or operating database is touched.
Scope: Tool/runtime preflight, disposable database create/apply/validate/test/drop, Hibernate `ddl-auto=validate`, unique/ENUM/index/foreign-key assertions, and ordered patch dry rehearsal.
Out: Existing local DB changes, production DB access, data migration, destructive cleanup outside the disposable database, or Testcontainers/new library introduction.
DoD: A disposable MySQL 8 schema accepts the current fresh DDL/ordered patches and the application validates the exact payment/storage/certification model; any incompatibility is recorded with reproducible SQL.
Constraints: User approved disposable MySQL create/drop only. Resolve and verify the absolute disposable target before destructive operations. Never use the configured application database unless it is newly created and uniquely named for this WI.

[ACCEPTANCE CRITERIA]
Functional:
- [ ] Preflight proves MySQL 8 availability and a uniquely named disposable target.
- [ ] Fresh schema and applicable manual patches execute without silent drift.
- [ ] Hibernate validate succeeds against the disposable schema.
- [ ] Payment command/refund/storage journal constraints and audit ENUM values match JPA/code contracts.
- [ ] Disposable database is dropped only after target ownership is revalidated.
Quality:
- [ ] Commands, server version, DDL hashes, validation output, and cleanup evidence are captured without secrets.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
Context:
- deliverables/user/REQ-20260714-ATS-001.md
- docs/design/p1-payment-db-integrity-design.md
- docs/design/p1-security-acceptance-hardening-design.md
- deliverables/agent/WI-20260714-ATS-004-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-007-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-008-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-010-evidence-pack.md
- deliverables/agent/WI-20260714-ATS-012-evidence-pack.md
Files:
- src/main/resources/schema.sql
- src/main/resources/db/manual/20260714_payment_db_integrity.sql
- src/main/resources/db/manual/20260714_storage_mutations_journal.sql
- application-local.example.yml
- JPA entities and schema contract tests

[OUTPUT CONTRACT]
User-facing -> deliverables/user/WI-20260714-ATS-021-summary.md (Korean)
Agent-facing -> deliverables/agent/WI-20260714-ATS-021-evidence-pack.md
Handoff -> deliverables/agent/WI-20260714-ATS-021-handoff.md

[TRACEABILITY REQUIREMENTS]
Exact disposable DB name, non-secret connection method, version, SQL order/hash, validate output, cleanup proof, and explicit confirmation that existing DBs were untouched are required.
