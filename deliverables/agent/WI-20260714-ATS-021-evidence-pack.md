# Evidence Pack: WI-20260714-ATS-021

## Summary (one-liner)
- Disposable MySQL 8 rehearsal proved fresh DDL/manual patch payment and storage contracts, but full Hibernate `ddl-auto=validate` is blocked by a pre-existing fresh schema mismatch outside the WI-021 payment/storage scope.

## Scope / DoD Check
- DoD items:
  - [x] Preflight proved MySQL 8 availability without printing secret values.
  - [x] Used uniquely named disposable databases only: `ats_wi021_20260715_v6cbliat` and `ats_wi021_20260715_valdat01`.
  - [x] Verified disposable target names do not equal the existing application DB name before create/drop.
  - [x] Applied fresh schema and ordered relevant manual patches to disposable MySQL 8.
  - [x] Proved payment command/refund/storage journal constraints, ENUMs, indexes, and storage check constraint.
  - [x] Dropped disposable databases and verified `cleanup.database.exists: 0`.
  - [x] Removed temporary `DisposableMysqlRehearsal*.class` build artifacts.
  - [x] Scanned remaining WI-021 artifacts for exact sensitive values and full JDBC URLs in logs.
  - [ ] Hibernate `ddl-auto=validate` succeeds.

Blocked item:
- Hibernate validation failed with `Schema validation: missing column [waveform_data] in table [tracks]`.
- Code pointer: `src/main/java/com/atstudio/atstudio/entity/Track.java:49` maps `waveformData` as `TEXT`.
- Schema pointer: `src/main/resources/schema.sql:184` starts the `tracks` table and `src/main/resources/schema.sql:194` has `duration`; no `waveform_data` column is present.
- No production code or schema file was modified under WI-021 scope.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | docs/standards/core-principles.md | Required Tier 0 governance |
| 0 | docs/standards/development-standards.md | Java/Spring/MySQL test standards |
| 0 | docs/standards/documentation-standards.md | Evidence and summary formatting |
| 0 | docs/standards/glossary.md | Canonical WI/project terms |
| 1 | docs/policies/security-policy.md | Secret and DB credential handling |
| 1 | docs/policies/quality-gates.md | Evidence and blocked validation reporting |
| 2 | deliverables/user/REQ-20260714-ATS-001.md | Approved scope and DB approval boundary |
| 2 | docs/design/p1-payment-db-integrity-design.md | Payment DB contract |
| 2 | docs/design/p1-security-acceptance-hardening-design.md | Storage mutation DB contract |

**Injection Rules Applied**:
- Rule source: `AGENTS.md`
- Assignee: `qa-integ`
- Task type: integration/database rehearsal
- agent_required_tiers: Tier 0 plus security/quality/design context from handoff

## Evidence Pointers
- Files created:
  - `deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal.java` - JDBC rehearsal runner; reads credentials without logging values.
  - `deliverables/agent/WI-20260714-ATS-021/run-disposable-mysql-rehearsal.ps1` - compile/run wrapper; preflight and drop-only support.
  - `deliverables/agent/WI-20260714-ATS-021/run-hibernate-validate-and-drop.ps1` - Hibernate validate wrapper; sanitizes logged JDBC URL and always drops via follow-up step.
  - `deliverables/agent/WI-20260714-ATS-021/rehearsal-jdbc.log` - successful create/apply/contract/drop proof.
  - `deliverables/agent/WI-20260714-ATS-021/rehearsal-jdbc-validate-db.log` - successful create/apply/contract proof for the DB used by Hibernate validate.
  - `deliverables/agent/WI-20260714-ATS-021/hibernate-validate.log` - redacted Hibernate validation failure log.
  - `deliverables/agent/WI-20260714-ATS-021/drop-after-hibernate-validate-retry.log` - successful final cleanup proof.
  - `deliverables/agent/WI-20260714-ATS-021/focused-contract-tests.log` - focused Gradle contract test output.
- Files intentionally removed:
  - `deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal.class`
  - `deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal$DbConfig.class`
  - `deliverables/agent/WI-20260714-ATS-021/DisposableMysqlRehearsal$JdbcParts.class`

## Commands & Outputs
- Preflight:
  - `mysql` CLI on PATH: `False`
  - Windows service `MySQL80`: running
  - MySQL Connector/J: present in Gradle cache
  - Credential source: process env or `application-local.yml`; values not printed
- JDBC rehearsal command:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File deliverables/agent/WI-20260714-ATS-021/run-disposable-mysql-rehearsal.ps1`
  - Result: `RESULT: PASS`
  - Server: MySQL `8.0.45`, MySQL Community Server
  - Cleanup: `drop.database: OK`, `cleanup.database.exists: 0`
- Ordered SQL proof:
  - `src/main/resources/schema.sql`
    - SHA-256: `79b27d89641be3b64636669b06ae6236fe06c3ed1c90e9e5cfb252f95daf73a4`
  - `src/main/resources/db/manual/20260615_align_payment_whitelist_schema.sql`
    - SHA-256: `051b4e94249d5a996f3dcb174765052949b91f2adf6f1c1320b0581f7b693be2`
  - `src/main/resources/db/manual/20260618_company_certification_documents.sql`
    - SHA-256: `6f7b232f1330ebe9be5b9c37670ec940f0d183d38f3484f79502f4edd728dfa2`
  - `src/main/resources/db/manual/20260714_storage_mutations_journal.sql`
    - SHA-256: `97326049ff0f187c14e29bac3454ae65bc22b9b0054d23c0c0e4b5ba4f8c0d5b`
  - `src/main/resources/db/manual/20260714_payment_db_integrity.sql`
    - SHA-256: `395ed4cad8c4f747c55ce34229092a08d75e8fcb53acab41c6beee178c37d1d5`
- Contract proof from `rehearsal-jdbc.log`:
  - `payment_orders.status` contains 9 expected values.
  - `payment_operation_audit_logs.action` contains 16 expected values.
  - `payment_operation_audit_logs.target_type` contains 5 expected values.
  - `storage_mutations.domain/type/root/state` contain 6/3/2/7 expected values.
  - `payment_orders` command, attempt, renewal-period, and status-processing indexes match.
  - `subscription_payments` order and provider-transaction unique indexes match.
  - `storage_mutations.chk_storage_mutations_keys` exists.
  - ENUM insert/flush proof counts: payment status 9, audit action 16, audit target 5, storage domain 6, type 3, root 2, state 7.
- Hibernate validate:
  - Command: `gradlew.bat bootRun --args="--spring.main.web-application-type=none --spring.sql.init.mode=never"`
  - Environment: `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`; datasource URL supplied by process env and redacted from logs.
  - Result: blocked by `SchemaManagementException: missing column [waveform_data] in table [tracks]`.
  - Note: Gradle reported `BUILD SUCCESSFUL`, so the wrapper now scans the log for schema validation failure markers instead of trusting only the process exit code.
- Artifact hygiene scan:
  - `classArtifactsRemaining: 0`
  - `exactSensitiveValueHits: 0`
  - `jdbcUrlHitsInLogs: 0`

## Tests
- Focused contract tests:
  - `gradlew.bat test --tests "com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest" --tests "com.atstudio.atstudio.entity.StorageMutationContractTest"`
  - Result: pass
  - XML evidence:
    - `build/test-results/test/TEST-com.atstudio.atstudio.entity.PaymentDatabaseIntegrityContractTest.xml`: tests `4`, failures `0`, errors `0`
    - `build/test-results/test/TEST-com.atstudio.atstudio.entity.StorageMutationContractTest.xml`: tests `3`, failures `0`, errors `0`

## Existing DB / Data Safety
- Existing local/application DB was not selected for schema apply or validation.
- Rehearsal created only WI-021-owned disposable databases matching `^ats_wi021_\d{8}_[a-z0-9]{8}$`.
- The runner refused unsafe targets and verified disposable names were not the configured application DB before create/drop.
- Final cleanup evidence:
  - `rehearsal-jdbc.log`: `cleanup.database.exists: 0`
  - `drop-after-hibernate-validate-retry.log`: `cleanup.database.exists: 0`
- No production runtime logs were modified or reverted.

## Risks / Rollback
- Risks:
  - Full fresh-schema Hibernate validation remains blocked until `tracks.waveform_data` schema/entity alignment is handled under a separate approved WI.
  - The initial cleanup wrapper inherited the validate datasource env and safely refused to drop because the target appeared to be the active app DB. A retry without the validate env succeeded; the wrapper was patched to remove the env URL before drop.
- Rollback:
  - Remove `deliverables/agent/WI-20260714-ATS-021/` if the rehearsal tooling/log artifacts are no longer needed.
  - No application production code or existing DB/data rollback is required because none was modified.

## Follow-ups
- Next WI candidates:
  - Create a separate schema-alignment WI for `Track.waveformData` / `tracks.waveform_data`, then rerun WI-021 Hibernate validate.
