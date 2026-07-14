# Evidence Pack: WI-20260714-ATS-035

## Summary (one-liner)
- Aligned the fresh `tracks.waveform_data` DDL with `Track.waveformData`, supplied a separately approved manual patch for existing databases, and closed the disposable-MySQL Hibernate validation blocker.

## Scope / DoD Check
- DoD items:
  - [x] `schema.sql` defines `tracks.waveform_data` as nullable `TEXT` after `duration`.
  - [x] The dated manual patch is additive, guarded, and not auto-run by Spring Boot.
  - [x] Focused schema contract XML records 3 tests, 0 failures, and 0 errors.
  - [x] Redacted Hibernate evidence contains the `Started AtStudioApplication` marker and no missing-column/schema-validation failure marker.
  - [x] Disposable database targeting and cleanup evidence show the application database was not selected or modified.
  - [x] Reviewed WI-035/WI-021 rehearsal artifacts contain no complete JDBC URL, unmasked credential value, or compiled helper artifact.

## Reference Documents (Tier 0-2)

**Injected Context**:

| Tier | Document | Reason |
|------|----------|--------|
| 0 | `docs/standards/core-principles.md` | Governance, approval, transparency, and secret-handling baseline |
| 0 | `docs/standards/development-standards.md` | Java, JPA, MySQL, test, and traceability standards |
| 0 | `docs/standards/documentation-standards.md` | Evidence Pack and documentation rules |
| 0 | `docs/standards/glossary.md` | Canonical Track and WI terminology |
| 1 | `docs/policies/security-policy.md` | JDBC credential and sensitive-log policy |
| 1 | `docs/policies/quality-gates.md` | Verification and Evidence Pack requirements |
| 2 | `deliverables/user/REQ-20260714-ATS-001.md` | Approved remediation scope and disposable-DB boundary |
| 2 | `deliverables/agent/WI-20260714-ATS-021-handoff.md` | Disposable MySQL rehearsal contract |
| 2 | `deliverables/agent/WI-20260714-ATS-021-evidence-pack.md` | Original missing-column blocker and rehearsal method |
| 2 | `docs/design/p1-security-acceptance-hardening-design.md` | Existing DB and acceptance-environment safety constraints |

**Injection Rules Applied**:
- Rule source: `AGENTS.md`
- Assignee: `se`
- Task type: schema alignment and focused verification
- Required context: Tier 0 plus security, quality, REQ, predecessor WI, design, entity, schema, manual patch, and rehearsal artifacts from the handoff

## Evidence Pointers
- Application/schema changes:
  - `src/main/resources/schema.sql` - WI-035-owned change adds `waveform_data TEXT NULL` to `tracks`; unrelated concurrent schema changes remain untouched.
  - `src/main/resources/db/manual/20260715_track_waveform_data.sql` - separately approved, existing-database-only patch with table/column guards and postcondition verification.
  - `src/test/java/com/atstudio/atstudio/entity/TrackWaveformSchemaContractTest.java` - verifies entity mapping, fresh schema placement/type/nullability, and guarded additive patch behavior.
- Runtime evidence:
  - `deliverables/agent/WI-20260714-ATS-035/rehearsal-jdbc-validate-db.log` - disposable schema creation/apply evidence; schema SHA-256 `9a0b06dbbe0ac166ab675b823b2831d5ffa63d4d1d37316ce6414d375b51511a`; `contract.validation: OK`; `RESULT: PASS`.
  - `deliverables/agent/WI-20260714-ATS-035/hibernate-validate.log` - redacted datasource URL, MySQL 8.0.45, initialized JPA EntityManagerFactory, and `Started AtStudioApplication in 8.121 seconds`.
  - `deliverables/agent/WI-20260714-ATS-035/drop-after-hibernate-validate.log` - `drop.database: OK`, `cleanup.database.exists: 0`, `drop.attempted: true`, and `RESULT: PASS`.
- Test evidence:
  - `build/test-results/test/TEST-com.atstudio.atstudio.entity.TrackWaveformSchemaContractTest.xml` - timestamp `01:22:02`; tests `3`, failures `0`, errors `0`.

## Exact Schema Contract
- Entity pointer: `src/main/java/com/atstudio/atstudio/entity/Track.java` maps `waveformData` with `@Column(columnDefinition = "TEXT")`; JPA nullability remains `true` by default.
- Fresh schema pointer: `src/main/resources/schema.sql` defines `waveform_data TEXT NULL` between `duration` and `user_id`.
- Existing database patch:
  - Runs only by explicit operator invocation; it is outside Spring Boot automatic initialization paths.
  - Requires `tracks` to exist.
  - Adds only a nullable `TEXT` column when missing.
  - Signals and stops if an existing column differs in type or nullability.
  - Performs no row insert, update, delete, truncate, or table drop.

## Commands & Outputs
- No new Gradle, bootRun, database, or shell command was run after the user's stop instruction.
- Focused test result was taken from the exact existing XML requested by the user:
  - `build/test-results/test/TEST-com.atstudio.atstudio.entity.TrackWaveformSchemaContractTest.xml`
  - Result: 3 tests, 0 failures, 0 errors at `01:22:02`.
- Existing Hibernate result was reviewed from the redacted log:
  - Success marker present: `Started AtStudioApplication`.
  - Failure markers absent: no `missing column [waveform_data]` and no `Schema validation` failure.
- Scoped ownership review:
  - WI-035 owns the waveform line in `schema.sql`, the dated manual patch, the focused contract test, the three redacted rehearsal logs, and the two completion deliverables.
  - Other concurrent `schema.sql` changes were observed and left untouched.
  - `git diff --check` was not rerun after the explicit instruction to stop running commands; no pass result is claimed for that command in this replacement turn.

## Existing DB / Data Safety
- Disposable database: `ats_wi021_20260715_w035a7b9`, matching the inherited runner's WI-owned disposable naming policy.
- `rehearsal-jdbc-validate-db.log` records:
  - `application.database.target.changed: false`
  - `application.database.name.equals.disposable: false`
  - masked application database name only
  - `selected.database: ats_wi021_20260715_w035a7b9`
- The fresh database was built from current `schema.sql`; the existing-database waveform patch was intentionally not applied to it.
- `drop-after-hibernate-validate.log` proves the disposable database was dropped and no longer exists.
- No existing local/application database was selected, patched, dropped, or otherwise modified by WI-035 finalization.

## Secret and Artifact Hygiene
- `hibernate-validate.log` stores `[REDACTED_JDBC_URL; database=ats_wi021_20260715_w035a7b9]`, not a complete JDBC URL.
- Reviewed WI-035 logs contain no password, token, exact credential value, or connection-string userinfo.
- The application database name is masked as `a***o (not used)`.
- Directory review found no `DisposableMysqlRehearsal*.class` or other compiled helper artifact under WI-021 or WI-035 deliverables.

## Changed Files
- `src/main/resources/schema.sql`
- `src/main/resources/db/manual/20260715_track_waveform_data.sql`
- `src/test/java/com/atstudio/atstudio/entity/TrackWaveformSchemaContractTest.java`
- `deliverables/agent/WI-20260714-ATS-035/rehearsal-jdbc-validate-db.log`
- `deliverables/agent/WI-20260714-ATS-035/hibernate-validate.log`
- `deliverables/agent/WI-20260714-ATS-035/drop-after-hibernate-validate.log`
- `deliverables/agent/WI-20260714-ATS-035-evidence-pack.md`
- `deliverables/user/WI-20260714-ATS-035-summary.md`

## Risks / Rollback
- Risks:
  - Existing databases remain unaligned until an operator separately approves and executes the manual patch.
  - The manual patch uses MySQL DDL, which implicitly commits; operators must test against a backup copy first.
  - The rehearsal helper retains WI-021 metadata because WI-035 reused the approved runner; the WI-035 artifact directory and disposable database suffix identify this rerun.
- Rollback:
  - Revert only the WI-035-owned `waveform_data` line, manual patch, contract test, rehearsal artifacts, and completion deliverables.
  - If the manual patch was separately executed later, rollback requires its own approved database procedure and compatibility review; this WI did not execute it.

## Follow-ups
- WI-035 no longer carries the `tracks.waveform_data` Hibernate validation blocker. The MA can resume the blocked review, documentation, and finalization WIs listed in the handoff.
