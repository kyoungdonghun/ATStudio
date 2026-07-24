[WI HEADER]
WI ID: WI-20260724-ATS-013
REQ: REQ-20260724-ATS-002
Agent: qa
Depends On: WI-20260724-ATS-010
Blocks: WI-20260724-ATS-014

[WI SUMMARY]
Why: Prove the fresh V1 schema and real MySQL behavior without touching protected databases.
Scope (in/out): Use loopback MySQL only. Create two uniquely named disposable databases matching the test-enforced `^ats_wi007_20260724_[a-z0-9]{8}$` contract. Apply only current `schema.sql` then `seed.sql`; use the first DB for the 8 `ATSTUDIO_MYSQL_PROOF_ENABLED=true` tests and drop it afterward; retain the second clean runtime DB for WI-014 through WI-016. Do not use the historical WI-007 manager unchanged because it references retired manual SQL.
DoD: Schema/seed/manifest and all 8 MySQL tests pass; protected DBs are proven unchanged; runtime DB connection material is stored only in a repo-external restricted bundle.
Constraints/Forbidden: Never target `atstudio`, system schemas, stage/prod names, or a non-loopback host. Never print URL/user/password or actual disposable DB name in committed evidence. No active repo source changes. Cleanup ownership passes to WI-017.

[ACCEPTANCE CRITERIA]
- [ ] DB source is loopback and both target names satisfy the test-enforced disposable form.
- [ ] Current schema and seed apply exactly once.
- [ ] Resulting schema manifest matches the V1 contract.
- [ ] MySQL schema validation 1/1 and concurrency races 7/7 pass.
- [ ] The proof DB is dropped after 8/8 tests; the separately seeded runtime DB starts with `ddl-auto=validate`.
- [ ] Protected DB fingerprint is unchanged.

[INPUT POINTERS]
Tier 0:
- docs/standards/core-principles.md
- docs/standards/development-standards.md
Tier 1:
- docs/policies/security-policy.md
- docs/policies/quality-gates.md
REQ/Context:
- deliverables/user/REQ-20260724-ATS-002.md
- docs/SR/SR-93.md
- docs/audit/p1-payment-integrity-closure-20260715.md
Files:
- src/main/resources/schema.sql
- src/main/resources/seed.sql
- src/test/java/com/atstudio/atstudio/service/PaymentMysqlSchemaValidationTest.java
- src/test/java/com/atstudio/atstudio/service/PaymentMysqlConcurrencyIntegrationTest.java
- deliverables/agent/WI-20260715-ATS-007/DisposableMysqlDatabaseManager.java

[OUTPUT CONTRACT]
User-facing -> `deliverables/user/WI-20260724-ATS-013-summary.md`
Agent-facing -> `deliverables/agent/WI-20260724-ATS-013-evidence-pack.md`

[TRACEABILITY REQUIREMENTS]
Record redacted source/target classification, schema counts/hash, test counts, startup result, protected-DB before/after proof, external bundle pointer, and exact cleanup contract.
